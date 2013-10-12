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
package com.moss.veracity.core.cluster.jms;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.moss.veracity.api.util.HexUtil;
import com.moss.veracity.core.config.Configuration;
import com.moss.veracity.core.config.UpdateSource;
import com.moss.veracity.core.data.PersistenceManager;

public final class UpdateReceiver {

	private final Log log = LogFactory.getLog(this.getClass());
	
	private final JAXBContext jaxbContext;
	private final PersistenceManager persistence;
	private final List<UpdateListener> listeners;
	
	public UpdateReceiver(JAXBContext jaxbContext, PersistenceManager persistence) {
		this.jaxbContext = jaxbContext;
		this.persistence = persistence;
		listeners = new ArrayList<UpdateListener>();
	}

	public void configure(Configuration config) {
		
		if (log.isDebugEnabled()) {
			log.debug("Reconfiguring update sources");
		}
		
		List<URI> configuredUris = new ArrayList<URI>();
		
		for (UpdateSource source : config.getUpdateSources()) {
			try {
				URI uri = new URI("tcp://" + source.getHostname() + ":" + source.getPort());
				URI failoverUri = new URI("failover:(" + uri + ")?maxReconnectAttempts=-1&maxReconnectDelay=1000");
				configuredUris.add(failoverUri);
			}
			catch (Exception ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
		}
		
		List<UpdateListener> remove = new ArrayList<UpdateListener>();
		
		for (UpdateListener listener : listeners) {
			if (!configuredUris.contains(listener.uri)) {
				remove.add(listener);
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Removing " + remove.size() + " update sources");
		}
		
		for (UpdateListener listener : remove) {
			try {
				listener.close();
				listeners.remove(listener);
			}
			catch (Exception ex) {
				if (log.isErrorEnabled()) {
					log.error("Failed to close update listener: " + listener, ex);
				}
				else {
					ex.printStackTrace();
				}
			}
		}
		
		List<URI> add = new ArrayList<URI>();
		
		for (URI uri : configuredUris) {
			
			boolean found = false;
			
			for (UpdateListener l : listeners) {
				if (l.uri.equals(uri)) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				add.add(uri);
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Adding " + add.size() + " update sources");
		}
		
		for (URI uri : add) {
			try {
				
				if (log.isDebugEnabled()) {
					log.debug("Adding update source: " + uri);
				}
				
				UpdateListener listener = new UpdateListener(uri);
				listeners.add(listener);
			}
			catch (Exception ex) {
				if (log.isErrorEnabled()) {
					log.error("Failed to add update source: " + uri, ex);
				}
				else {
					ex.printStackTrace();
				}
			}
		}
	}

	public void close() throws Exception {
		
		if (log.isDebugEnabled()) {
			log.debug("Removing " + listeners.size() + " update sources");
		}
		
		boolean failure = false;
		
		for (UpdateListener l : listeners) {
			try {
				l.close();
			}
			catch (Exception ex) {
				if (log.isErrorEnabled()) {
					log.error("Failed to remove update source: " + l, ex);
				}
				else {
					ex.printStackTrace();
				}
				failure = true;
			}
		}
		
		if (failure) {
			throw new Exception("Failed to close all update sources, see logs for details.");
		}
	}
	
	private final class UpdateListener implements MessageListener {
	
		final URI uri;
		final ConnectionFactory connectionFactory;
		final Connection connection;
		final Session session;
		final Topic topic;
		final MessageConsumer consumer;
		
		UpdateListener(URI brokerUri) throws Exception {
			try {
				uri = brokerUri;
				connectionFactory = new ActiveMQConnectionFactory(brokerUri);
				connection = connectionFactory.createConnection();
				session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				topic = session.createTopic(UpdateTopic.NAME);
				consumer = session.createConsumer(topic);
				consumer.setMessageListener(this);
				connection.start();
			}
			catch (Exception ex) {
				try {
					close();
				}
				catch (Exception e) {}
				throw ex;
			}
		}
		
		void close() {
			
			boolean failure = false;
			
			if (log.isDebugEnabled()) {
				log.debug("Closing update source: " + uri);
			}
			
			if (consumer != null) {
				try {
					consumer.close();
				}
				catch (Exception ex) {
					if (log.isErrorEnabled()) {
						log.error("Failed to close consumer", ex);
					}
					else {
						ex.printStackTrace();
					}
					failure = true;
				}
			}
			
			if (session != null) {
				try {
					session.close();
				}
				catch (Exception ex) {
					if (log.isErrorEnabled()) {
						log.error("Failed to close session", ex);
					}
					else {
						ex.printStackTrace();
					}
					failure = true;
				}
			}
			
			if (connection != null) {
				try {
					connection.close();
				}
				catch (Exception ex) {
					if (log.isErrorEnabled()) {
						log.error("Failed to close connection", ex);
					}
					else {
						ex.printStackTrace();
					}
					failure = true;
				}
			}
			
			if (failure) {
				throw new RuntimeException("Failed to completely close update source: " + uri + ", see logs for details.");
			}
		}
		
		public String toString() {
			return uri.toString();
		}

		public void onMessage(Message message) {
			try {
				
				if (log.isDebugEnabled()) {
					log.debug("Processing message from update source " + uri);
				}
				
				TextMessage textMessage = (TextMessage)message;
				String text = textMessage.getText();
				StringReader reader = new StringReader(text);
				Unmarshaller u = jaxbContext.createUnmarshaller();
				Object o = u.unmarshal(reader);
				
				if (o instanceof PublicKeyTransmission) {
					PublicKeyTransmission t = (PublicKeyTransmission)o;
					publicKeyCreated(t);
				}
				else if (o instanceof AccountModifiedTransmission) {
					AccountModifiedTransmission t = (AccountModifiedTransmission)o;
					accountModified(t);
				}
				else {
					throw new RuntimeException("Unsupported transmission type: " + o);
				}
				
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		
		private void publicKeyCreated(PublicKeyTransmission t) {
			
			if (log.isDebugEnabled()) {
				log.debug("Received Update: Public Key Creation (" + HexUtil.toHex(t.key().digest()) + ")");
			}
			
			persistence.save(t.key(), null);
		}
		
		private void accountModified(AccountModifiedTransmission t) {
			
			if (log.isDebugEnabled()) {
				log.debug("Received Update: Account Modification (" + t.account().getName() + ")");
			}
			
			persistence.save(t.account(), null);
		}
	}
}
