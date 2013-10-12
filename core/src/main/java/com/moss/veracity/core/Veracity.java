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
package com.moss.veracity.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.bind.JAXBContext;

import org.apache.activemq.broker.BrokerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;
import org.mortbay.jetty.Server;

import com.moss.rpcutil.jetty.SwitchingContentHandler;
import com.moss.rpcutil.jetty.hessian.HessianContentHandler;
import com.moss.rpcutil.jetty.jaxws.BSHandler;
import com.moss.rpcutil.jetty.jaxws.JAXWSContentHandler;
import com.moss.veracity.api.Authentication;
import com.moss.veracity.api.Management;
import com.moss.veracity.api.VeracityException;
import com.moss.veracity.api.VtAuthMode;
import com.moss.veracity.core.cluster.UpdateTransmitter;
import com.moss.veracity.core.cluster.jms.AccountModifiedTransmission;
import com.moss.veracity.core.cluster.jms.PublicKeyTransmission;
import com.moss.veracity.core.cluster.jms.UpdateReceiver;
import com.moss.veracity.core.cluster.jms.UpdateTransmitterJMSImpl;
import com.moss.veracity.core.config.ConfigListener;
import com.moss.veracity.core.config.ConfigManager;
import com.moss.veracity.core.config.Configuration;
import com.moss.veracity.core.data.Account;
import com.moss.veracity.core.data.FirstMiddleLastName;
import com.moss.veracity.core.data.PasswordMechanism;
import com.moss.veracity.core.data.PersistenceManager;
import com.moss.veracity.core.data.Profile;
import com.moss.veracity.core.service.AuthenticationImpl;
import com.moss.veracity.core.service.EndorsementManager;
import com.moss.veracity.core.service.ManagementImpl;
import com.sleepycat.je.Transaction;
import com.sun.net.httpserver.HttpHandler;

/*
 * Mirroring Terminology
 * 
 *     When two veracity instances are configured to incorporate 
 *     one-another's changes such that they are mirrors of each 
 *     other, they are called Peers.
 *     
 *     When one veracity instance notifies another of a particular
 *     change, this is called an Update. 
 *     
 *     The sender of an Update is called an UpdateSource. The receiver of an 
 *     update is called an UpdateSink. 
 *     
 *     The act of sending an update is called an UpdateTransmission. A 
 *     component which sends an update is an UpdateTransmitter. The 
 *     act of receiving an update is an UpdateReception. A component 
 *     which receives updates is an UpdateReceiver.
 */
public final class Veracity {
	
	private static final String DEFAULT_USER_NAME = "root@localhost";
	private static final String DEFAULT_USER_PASSWORD = "pass";
	
	private final Log log;
	
	private Server jetty;
	
	private PersistenceManager persistence;
	private EndorsementManager endorsementManager;
	private ConfigManager configManager;
	private Timer keyExpirationTimer;
	private UpdateReceiver updateReceiver;
	private BrokerService broker;
	private UpdateTransmitter updateTransmitter;
	private Thread shutdownHook;
	
	public Veracity() throws Exception {
		this(new LaunchParameters());
	}
	
	public Veracity(LaunchParameters parameters) throws Exception {
		
		log = LogFactory.getLog(this.getClass());
		
		File dataDir = parameters.dataDirectory();
		File configFile = new File(dataDir, "veracity.xml");
		File imageDir = new File(dataDir, "images");
		File persistenceDir = new File(dataDir, "env");
		File brokerDir = new File(dataDir, "broker");
		
		if (!dataDir.exists() && !dataDir.mkdirs()) {
			throw new Exception("Could not create dir: " + dataDir);
		}
		
		if (!imageDir.exists() && !imageDir.mkdirs()) {
			throw new Exception("Could not create dir: " + imageDir);
		}
		
		try {
			
			configManager = new ConfigManager(configFile);

			persistence = new PersistenceManager(persistenceDir);

			long tenSeconds = 1000 * 10;
			long thirtyMinutes = 1000 * 60 * 30;
			keyExpirationTimer = new Timer(true);
			keyExpirationTimer.scheduleAtFixedRate(new KeyExpirationTimerTask(), tenSeconds, thirtyMinutes);

			createDefaultUser();
			
			JAXBContext updateJaxbContext = JAXBContext.newInstance(PublicKeyTransmission.class, AccountModifiedTransmission.class, PasswordMechanism.class);
			
			updateReceiver = new UpdateReceiver(updateJaxbContext, persistence);
			
			new Thread("InitialReceiverConfigThread") {
				public void run() {
					updateReceiver.configure(configManager.config());
				}
			}.start();
			
			configManager.addListener(new ConfigListener() {
				public void configChanged(Configuration config) {
					updateReceiver.configure(config);
				}
			});
			
			URI brokerVmUri = new URI("vm://localhost?create=false");
			URI brokerTcpUri = new URI("tcp://" + parameters.bindAddress() + ":" + parameters.syncPort());
			
			if (log.isDebugEnabled()) {
				log.debug("Starting Broker with public uri: " + brokerTcpUri);
			}
			
			broker = new BrokerService();
			broker.addConnector(brokerVmUri);
			broker.addConnector(brokerTcpUri);
			broker.setPersistent(true);
			broker.setDataDirectoryFile(brokerDir);
			broker.setUseShutdownHook(false);
			broker.setUseJmx(false);
			broker.start();
			
			updateTransmitter = new UpdateTransmitterJMSImpl(brokerVmUri, updateJaxbContext);
			
			endorsementManager = new EndorsementManager(persistence, updateTransmitter);
			endorsementManager.configure(configManager.config());

			configManager.addListener(new ConfigListener() {
				public void configChanged(Configuration config) {
					endorsementManager.configure(config);
				}
			});
			
			if (log.isDebugEnabled()) {
				log.debug("Creating http server");
			}
			
			jetty = new Server(parameters.httpPort());
			
			if (log.isDebugEnabled()) {
				log.debug("Publishing Authentication service");
			}
			
			{
				Authentication authService = new AuthenticationImpl(persistence, endorsementManager);
				String path = "/" + authService.getClass().getSimpleName();
				SwitchingContentHandler handler = new SwitchingContentHandler(path);
				handler.addHandler(new JAXWSContentHandler(authService));
				handler.addHandler(new HessianContentHandler(Authentication.class, authService));
				jetty.addHandler(handler);
			}
			
			if (log.isDebugEnabled()) {
				log.debug("Publishing Management service");
			}

			{
				Management manageService = new ManagementImpl(persistence, configManager, updateTransmitter);
				String path = "/" + manageService.getClass().getSimpleName();
				SwitchingContentHandler handler = new SwitchingContentHandler(path);
				handler.addHandler(new JAXWSContentHandler(manageService));
				handler.addHandler(new HessianContentHandler(Management.class, manageService));
				jetty.addHandler(handler);
			}
			
			if (log.isDebugEnabled()) {
				log.debug("Creating identity manager servlet context");
			}
			
			HttpHandler idManagerServlet = new IdentityManagerServlet();
			jetty.addHandler(new BSHandler(IdentityManagerServlet.LAUNCH_PATH, idManagerServlet));
			jetty.addHandler(new BSHandler(IdentityManagerServlet.JAR_PATH, idManagerServlet));
			
			if (log.isDebugEnabled()) {
				log.debug("Starting http server");
			}
			
			jetty.start();
			
			/*
			 * Cleanup
			 */
			
			shutdownHook = new ShutdownThread();
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		}
		catch (Exception ex) {
			shutdown();
			throw ex;
		}
	}
	
	public void shutdown() {
		
		if (shutdownHook != null) {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		}
		
		boolean failure = doShutdown();
		
		if (failure) {
			throw new RuntimeException("Shutdown failure occurred, see logs for details");
		}
	}
	
	private boolean doShutdown() {
		
		boolean failure = false;
		
		if (jetty != null) {
			try {
//				httpServer.stop(0);
				jetty.stop();
			}
			catch (Exception ex) {
				if (log.isErrorEnabled()) {
					log.error("Failed to shut down http server cleanly", ex);
				}
				else {
					ex.printStackTrace();
				}
				failure = true;
			}
		}
		
		if (updateTransmitter != null) {
			try {
				updateTransmitter.close();
			}
			catch (Exception ex) {
				if (log.isErrorEnabled()) {
					log.error("Failed to shut down update transmitter cleanly", ex);
				}
				else {
					ex.printStackTrace();
				}
				failure = true;
			}
		}
		
		if (broker != null) {
			try {
				broker.stop();
			}
			catch (Exception ex) {
				if (log.isErrorEnabled()) {
					log.error("Failed to shut down broker cleanly", ex);
				}
				else {
					ex.printStackTrace();
				}
				failure = true;
			}
		}
		
		if (updateReceiver != null) {
			try {
				updateReceiver.close();
			}
			catch (Exception ex) {
				if (log.isErrorEnabled()) {
					log.error("Failed to shut down update receiver cleanly", ex);
				}
				else {
					ex.printStackTrace();
				}
				failure = true;
			}
		}
		
		if (configManager != null) {
			try {
				configManager.close();
			}
			catch (Exception ex) {
				if (log.isErrorEnabled()) {
					log.error("Failed to shut down config manager cleanly", ex);
				}
				else {
					ex.printStackTrace();
				}
				failure = true;
			}
		}
		
		if (persistence != null) {
			try {
				persistence.close();
			}
			catch (Exception ex) {
				if (log.isErrorEnabled()) {
					log.error("Failed to shut down persistence cleanly", ex);
				}
				else {
					ex.printStackTrace();
				}
				failure = true;
			}
		}
		
		return failure;
	}
	
	private void createDefaultUser() {
		
		if (persistence.accountExists(DEFAULT_USER_NAME, null, null)) {
			return;
		}
		
		if (log.isDebugEnabled()) {
			log.debug("creating default admin user");
		}
		
		PasswordMechanism password = new PasswordMechanism();
		password.setPassword(DEFAULT_USER_PASSWORD);

		Profile profile = new Profile();
		profile.setImageData(getResource("com/moss/veracity/core/root.jpg"));
		profile.setProfileName("default");
		profile.setName(new FirstMiddleLastName("Veracity", "Admin"));

		Account account = new Account();
		account.setName(DEFAULT_USER_NAME);
		account.setAuthMode(VtAuthMode.ADMIN);
		account.add(password);
		account.add(profile);
		
		persistence.save(account, null);
	}
	
	private byte[] getResource(String path) {
		
		try {

			InputStream in = this.getClass().getClassLoader().getResourceAsStream(path);
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024 * 10]; //10k buffer
			for(int numRead = in.read(buffer); numRead!=-1; numRead = in.read(buffer)){
				out.write(buffer, 0, numRead);
			}

			in.close();
			out.close();

			return out.toByteArray();

		}
		catch (Exception ex) {
			throw new VeracityException(ex);
		}
	}
	
	private class KeyExpirationTimerTask extends TimerTask {

		private final Log log = LogFactory.getLog(this.getClass());

		public void run() {

			if (log.isDebugEnabled()) {
				log.debug("Key expiration timer fired: " + new Date());
			}

			Transaction t = null;
			try {
				t = persistence.begin();
				persistence.deleteExpiredPublicSigningKeys(System.currentTimeMillis(), t);
				t.commit();
				t = null;
			}
			catch (Exception ex) {
				if (t != null) {
					try {
						t.abort();
					}
					catch (Exception e) {
						log.error("Failure to abort tx", e);
					}
				}

				throw new RuntimeException(ex);
			}
		}
	}
	
	private class ShutdownThread extends Thread {
		
		public ShutdownThread() {
			super("VeracityShutdownThread");
		}

		public void run() {
			doShutdown();
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		File log4jConfigFile = new File("log4j.xml");
		
		if (log4jConfigFile.exists()) {
			DOMConfigurator.configureAndWatch(log4jConfigFile.getAbsolutePath(), 1000);
		}
		
		final Log log = LogFactory.getLog(Veracity.class);
		
		File homeDir = new File(System.getProperty("user.home"));
		File currentDir = new File(System.getProperty("user.dir"));
		
		List<File> configLocations = new LinkedList<File>();
		configLocations.addAll(Arrays.asList(new File[] {
				new File("/etc/veracity.config"),
				new File(homeDir, ".veracity.config"),
				new File(currentDir, "config.xml")
		}));
		
		String customConfigFileProperty = System.getProperty("veracity.configFile");
		if(customConfigFileProperty!=null){
			configLocations.clear();
			configLocations.add(new File(customConfigFileProperty));
		}
		
		File configFile = null;
		
		Iterator<File> i = configLocations.iterator();
		while((configFile==null||!configFile.exists()) && i.hasNext()){
			configFile = i.next();
		}
		
		LaunchParameters parameters;
		
		if(!configFile.exists()){
			if (log.isDebugEnabled()) {
				log.debug("Creating default config file at " + configFile.getAbsolutePath());
			}
			parameters = new LaunchParameters();
			parameters.save(configFile);
		}else{
			if (log.isDebugEnabled()) {
				log.debug("Loading parameters from config file at " + configFile.getAbsolutePath());
			}
			parameters = LaunchParameters.load(configFile);
		}
		
		parameters.readSystemProperties();
		
		new Veracity(parameters);
	}
}
