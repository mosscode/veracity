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
package com.moss.veracity.core.service;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.moss.veracity.api.AccountExistsException;
import com.moss.veracity.api.Management;
import com.moss.veracity.api.NoSuchAccountException;
import com.moss.veracity.api.NotAuthorizedException;
import com.moss.veracity.api.VeracityException;
import com.moss.veracity.api.VtAccount;
import com.moss.veracity.api.VtAuthMode;
import com.moss.veracity.api.VtConfiguration;
import com.moss.veracity.api.VtEndorsement;
import com.moss.veracity.api.XmlSeeAlso;
import com.moss.veracity.api.util.HexUtil;
import com.moss.veracity.api.util.NameParser;
import com.moss.veracity.core.cluster.UpdateTransmitter;
import com.moss.veracity.core.config.ConfigManager;
import com.moss.veracity.core.config.Configuration;
import com.moss.veracity.core.data.Account;
import com.moss.veracity.core.data.AccountVisitor;
import com.moss.veracity.core.data.PersistenceManager;
import com.moss.veracity.core.data.PublicSignatureKey;

@WebService(
	endpointInterface="com.moss.veracity.api.Management",
	targetNamespace="http://core.veracity.moss.com/"
)
public final class ManagementImpl implements Management {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	private final PersistenceManager persistence;
	private final ConfigManager configManager;
	private final UpdateTransmitter transmitter;
	
	public ManagementImpl(PersistenceManager persistence, ConfigManager configManager, UpdateTransmitter transmitter) {
		this.persistence = persistence;
		this.configManager = configManager;
		this.transmitter = transmitter;
	}

	public void configure(VtConfiguration configuration, VtEndorsement admin) throws NotAuthorizedException {
		
		authorize(admin, VtAuthMode.ADMIN);
		
		Configuration config = configManager.config();
		config.fromDto(configuration);
		configManager.updateConfig(config);
	}
	
	public VtConfiguration getConfiguration(VtEndorsement admin) throws NotAuthorizedException {
		
		authorize(admin, VtAuthMode.ADMIN);
		
		Configuration config = configManager.config();
		return config.toDto();
	}

	public void create(VtAccount dto, VtEndorsement endorsement) throws NotAuthorizedException, AccountExistsException {
		
		if (dto.getAuthMode() != VtAuthMode.USER) {
			authorize(endorsement, VtAuthMode.ADMIN);
		}
		
		Account account = persistence.account(dto.getName(), null, null);
		
		if (account != null) {
			throw new AccountExistsException();
		}
		
		if (NameParser.parse(dto.getName()) == null) {
			throw new VeracityException("Invalid account name: not parsable '" + dto.getName() + "'");
		}
		
		account = new Account();
		account.setName(dto.getName());
		account.fromDto(dto);

		persistence.save(account, null);
		transmitter.accountModified(account);
	}
	
	public VtAccount read(String name, VtEndorsement admin) throws NotAuthorizedException, NoSuchAccountException {
		
		VtAuthMode mode;
		
		if (name.equals(admin.getName())) {
			mode = VtAuthMode.USER;
		}
		else {
			mode = VtAuthMode.ADMIN;
		}
		
		authorize(admin, mode);
		
		Account account = persistence.account(name, null, null);
		
		if (account == null) {
			throw new NoSuchAccountException();
		}
		
		return account.toDto();
	}
	
	public void update(VtAccount dto, VtEndorsement admin) throws NotAuthorizedException, NoSuchAccountException {
		
		if (dto.getName().equals(admin.getName())) {
			authorize(admin, VtAuthMode.USER);
		}
		else {
			authorize(admin, VtAuthMode.ADMIN);
		}
		
		Account account = persistence.account(dto.getName(), null, null);
		
		if (account == null) {
			throw new NoSuchAccountException();
		}
		
		account.fromDto(dto);
		
		persistence.save(account, null);
		transmitter.accountModified(account);
	}
	
	public List<String> listAccountNames(VtEndorsement admin) throws NotAuthorizedException {
		
		authorize(admin, VtAuthMode.ADMIN);
		
		final List<String> names = new ArrayList<String>();
		
		persistence.visitAccounts(new AccountVisitor() {
			public boolean visit(Account account) {
				names.add(account.getName());
				return false;
			}
		});
		
		return names;
	}
	
	public boolean accountExists(String name) {
		return persistence.accountExists(name, null, null);
	}

	public void register(XmlSeeAlso registry) {}

	private void authorize(VtEndorsement endorsement, VtAuthMode requiredMode) throws NotAuthorizedException {
		
		if (endorsement == null) {
			throw new NotAuthorizedException();
		}
		
		if (endorsement.getKeyDigest() == null) {
			throw new NotAuthorizedException();
		}
		
		if (endorsement.getName() == null) {
			throw new NotAuthorizedException();
		}
		
		if (endorsement.getSignature() == null) {
			throw new NotAuthorizedException();
		}
		
		Account account = persistence.account(endorsement.getName(), null, null);
		
		if (account == null) {
			throw new NotAuthorizedException("No such identity: " + endorsement.getName());
		}
		
		PublicSignatureKey key = persistence.publicSigningKey(endorsement.getKeyDigest(), null, null);
		
		if (account == null) {
			throw new NotAuthorizedException("No such key: " + HexUtil.toHex(endorsement.getKeyDigest()));
		}
		
		Verifier v = new Verifier();
		
		if (!v.isValid(endorsement, key)) {
			throw new NotAuthorizedException("Signature is invalid");
		}
		
		if (requiredMode.ordinal() > account.getAuthMode().ordinal()) {
			throw new NotAuthorizedException("Insufficient permissions: " + account.getAuthMode());
		}
	}

	public void transmitAllUpdates(VtEndorsement admin) throws NotAuthorizedException {
		
		authorize(admin, VtAuthMode.ADMIN);
		
		Thread t = new Thread(new TransmitAllUpdatesRunnable(), "TransmitAllUpdatesThread");
		t.start();
	}
	
	private class TransmitAllUpdatesRunnable implements Runnable {
		public void run() {
			
			if (log.isDebugEnabled()) {
				log.debug("Transmitting all updates");
			}
			
			/*
			 * TODO
			 */
		}
	} 
}
