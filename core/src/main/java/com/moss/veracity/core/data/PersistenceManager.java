/**
 * Copyright (C) 2013, Moss Computing Inc.
 *
 * This file is part of veracity.
 *
 * veracity is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * veracity is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with veracity; see the file COPYING.  If not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */
package com.moss.veracity.core.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.moss.veracity.api.util.HexUtil;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;

public final class PersistenceManager {
	
	private static final String ENCODING = "UTF8";
	
	private static final String ACCOUNTS_DB = "Accounts";
	private static final String PUBLIC_SIGNING_KEYS_DB = "PublicSigningKeys";
	
	private final Log log = LogFactory.getLog(this.getClass());

	private JAXBContext jaxbContext;
	private Environment env;
	private Database accountsDb;
	private Database publicSigningKeysDb;
	
	private final List<PersistenceListener> listeners;
	
	public PersistenceManager(File envDir) {
		
		envDir = envDir.getAbsoluteFile();
		
		if (log.isDebugEnabled()) {
			log.debug("Loading environment using envDir: " + envDir);
		}
		
		if (!envDir.exists() && !envDir.mkdirs()) {
			throw new RuntimeException("Cannot create envDir: " + envDir);
		}
		
		try {
			
			List<Class> classes = new ArrayList<Class>();
			
			/*
			 * Root classes
			 */
			classes.add(PublicSignatureKey.class);
			classes.add(Account.class);
			
			/*
			 * Hierarchy Leafs
			 */
			classes.add(PasswordMechanism.class);
			
			if (log.isDebugEnabled()) {
				log.debug("Building JAXB context");
			}
			
			jaxbContext = JAXBContext.newInstance(classes.toArray(new Class[0]));
			
			if (log.isDebugEnabled()) {
				log.debug("Opening environment");
			}
			
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setAllowCreate(true);
			envConfig.setTransactional(true);
			envConfig.setSharedCache(true);
			
			env = new Environment(envDir, envConfig);

			{
				if (log.isDebugEnabled()) {
					log.debug("Opening database " + ACCOUNTS_DB);
				}
				
				DatabaseConfig dbConfig = new DatabaseConfig();
				dbConfig.setAllowCreate(true);
				dbConfig.setTransactional(true);
				
				accountsDb = env.openDatabase(null, ACCOUNTS_DB, dbConfig);
			}
			
			{
				if (log.isDebugEnabled()) {
					log.debug("Opening database " + PUBLIC_SIGNING_KEYS_DB);
				}
				
				DatabaseConfig dbConfig = new DatabaseConfig();
				dbConfig.setAllowCreate(true);
				dbConfig.setTransactional(true);
				
				publicSigningKeysDb = env.openDatabase(null, PUBLIC_SIGNING_KEYS_DB, dbConfig);
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		listeners = new ArrayList<PersistenceListener>();
	}
	
	public void addListener(PersistenceListener l) {
		synchronized (listeners) {
			listeners.add(l);
		}
	}
	
	public void removeListener(PersistenceListener l) {
		synchronized (listeners) {
			listeners.remove(l);
		}
	}
	
	public Transaction begin() throws Exception {
		try {
			return env.beginTransaction(null, null);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public PublicSignatureKey publicSigningKey(byte[] digest, Transaction t, LockMode mode) {
		
		if (digest == null) {
			throw new NullPointerException();
		}

		try {
			DatabaseEntry key = new DatabaseEntry(digest);
			DatabaseEntry data = new DatabaseEntry();

			if (OperationStatus.SUCCESS == publicSigningKeysDb.get(t, key, data, mode)) {
				return read(PublicSignatureKey.class, data);
			}
			else {
				return null;
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public void save(PublicSignatureKey publicSigningKey, Transaction t) {
		
		try {
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			
			key.setData(publicSigningKey.digest());
			write(publicSigningKey, data);
			publicSigningKeysDb.put(t, key, data);
			
			fireKeyCreated(publicSigningKey);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public Account account(String name, Transaction t, LockMode mode) {
		
		if (name == null) {
			throw new NullPointerException();
		}

		try {
			DatabaseEntry key = new DatabaseEntry(name.getBytes(ENCODING));
			DatabaseEntry data = new DatabaseEntry();

			if (OperationStatus.SUCCESS == accountsDb.get(t, key, data, mode)) {
				return read(Account.class, data);
			}
			else {
				return null;
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public boolean accountExists(String name, Transaction t, LockMode mode) {
		
		if (name == null) {
			throw new NullPointerException();
		}

		try {
			DatabaseEntry key = new DatabaseEntry(name.getBytes(ENCODING));
			DatabaseEntry data = new DatabaseEntry();

			return OperationStatus.SUCCESS == accountsDb.get(t, key, data, mode);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public void save(Account account, Transaction t) {
		
		try {
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			
			key.setData(account.getName().getBytes(ENCODING));
			write(account, data);
			accountsDb.put(t, key, data);
			
			fireAccountModified(account);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public void visitAccounts(AccountVisitor visitor) {

		Transaction t = null;
		Cursor loopCursor = null;
		
		try {
			TransactionConfig tConfig = new TransactionConfig();
			t = env.beginTransaction(null, tConfig);

			CursorConfig cursorConfig = new CursorConfig();
			loopCursor = accountsDb.openCursor(t, cursorConfig);

			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();

			while (OperationStatus.SUCCESS == loopCursor.getNext(key, data, null)) {

				Account account = read(Account.class, data);
				boolean save = visitor.visit(account);

				if (save) {
					write(account, data);
					loopCursor.putCurrent(data);
				}
			}

			loopCursor.close();
			loopCursor = null;

			t.commit();
			t = null;
		}
		catch (Exception ex) {

			try {
				if (loopCursor != null) {
					loopCursor.close();
				}

				if (t != null) {
					t.abort();
				}
			}
			catch (Exception e) {
				ex.printStackTrace();
			}

			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Deletes all public signing keys with an expiration date before <b>maxExpiration</b>.
	 */
	public void deleteExpiredPublicSigningKeys(long maxExpiration, Transaction t) {
		
		Cursor cursor = null;
		try {
			cursor = publicSigningKeysDb.openCursor(t, null);
			
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			
			while (OperationStatus.SUCCESS == cursor.getNext(key, data, LockMode.DEFAULT)) {
				
				PublicSignatureKey publicSigningKey = read(PublicSignatureKey.class, data);
				
				if (publicSigningKey.expiration() < maxExpiration) {
					
					if (log.isDebugEnabled()) {
						log.debug("Deleting expired key " + HexUtil.toHex(publicSigningKey.digest()) + ":" + new Date(publicSigningKey.expiration()));
					}
					
					cursor.delete();
				}
			}
			
			cursor.close();
			cursor = null;
		}
		catch (Exception ex) {
			
			if (cursor != null) {
				try {
					cursor.close();
				}
				catch (DatabaseException e) {
					if (log.isErrorEnabled()) {
						log.error("Could not close cursor", e);
					}
				}
			}
			
			throw new RuntimeException(ex);
		}
	}
	
	public void close() {
		
		boolean closeFailed = false;
		
		try {
			if (log.isDebugEnabled()) {
				log.debug("Closing database " + ACCOUNTS_DB);
			}
			
			accountsDb.close();
		}
		catch (DatabaseException ex) {
			closeFailed = true;
			if (log.isErrorEnabled()) {
				log.error("Could not close accounts db", ex);
			}
		}
		
		try {
			if (log.isDebugEnabled()) {
				log.debug("Closing database " + PUBLIC_SIGNING_KEYS_DB);
			}
			
			publicSigningKeysDb.close();
		}
		catch (DatabaseException ex) {
			closeFailed = true;
			if (log.isErrorEnabled()) {
				log.error("Could not close public signing keys db", ex);
			}
		}
		
		try {
			if (log.isDebugEnabled()) {
				log.debug("Closing environment: " + env.getHome());
			}
			
			env.close();
		}
		catch (DatabaseException ex) {
			closeFailed = true;
			if (log.isErrorEnabled()) {
				log.error("Could not close environment", ex);
			}
		}
		
		if (closeFailed) {
			throw new RuntimeException("Failed to close store persistence context, see logs for more info");
		}
	}
	
	private <T> T read(Class<T> clazz, DatabaseEntry entry) throws JAXBException {
		Unmarshaller u = jaxbContext.createUnmarshaller();
		return (T)u.unmarshal(new ByteArrayInputStream(entry.getData()));
	}
	
	private void write(Object o, DatabaseEntry entry) throws JAXBException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		Marshaller m = jaxbContext.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(o, out);
		
		entry.setData(out.toByteArray());
	}
	
	private void fireKeyCreated(PublicSignatureKey key) {
		
		List<PersistenceListener> listeners;
		synchronized (this.listeners) {
			listeners = new ArrayList<PersistenceListener>(this.listeners);
		}
		
		for (PersistenceListener l : listeners) {
			try {
				l.publicKeyCreated(key);
			}
			catch (Exception ex) {
				if (log.isErrorEnabled()) {
					log.error("Persistence listener threw exception", ex);
				}
				else {
					ex.printStackTrace();
				}
			}
		}
	}
	
	private void fireAccountModified(Account account) {
		
		List<PersistenceListener> listeners;
		synchronized (this.listeners) {
			listeners = new ArrayList<PersistenceListener>(this.listeners);
		}
		
		for (PersistenceListener l : listeners) {
			try {
				l.accountModified(account);
			}
			catch (Exception ex) {
				if (log.isErrorEnabled()) {
					log.error("Persistence listener threw exception", ex);
				}
				else {
					ex.printStackTrace();
				}
			}
		}
	}
}
