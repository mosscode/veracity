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

import java.io.StringWriter;
import java.net.URI;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.moss.veracity.api.util.HexUtil;
import com.moss.veracity.core.cluster.UpdateTransmitter;
import com.moss.veracity.core.data.Account;
import com.moss.veracity.core.data.PublicSignatureKey;

public final class UpdateTransmitterJMSImpl implements UpdateTransmitter {
	
	private final Log log = LogFactory.getLog(this.getClass());

	private final JAXBContext jaxbContext;
	private final ConnectionFactory connectionFactory;
	private final Connection connection;
	
	public UpdateTransmitterJMSImpl(URI brokerUri, JAXBContext updateJaxbContext) throws JMSException {
		jaxbContext = updateJaxbContext;
		
		if (log.isDebugEnabled()) {
			log.debug("Connecting to the local broker");
		}
		
		connectionFactory = new ActiveMQConnectionFactory(brokerUri);
		connection = connectionFactory.createConnection();
	}
	
	public void keyCreated(PublicSignatureKey key) {
		
		if (log.isDebugEnabled()) {
			log.debug("Transmitting Update: Public Key Creation (" + HexUtil.toHex(key.digest()) + ")");
		}
		
		PublicKeyTransmission t = new PublicKeyTransmission(key);
		sendMessage(t);
	}
	
	public void accountModified(Account account) {
		
		if (log.isDebugEnabled()) {
			log.debug("Transmitting Update: Account Modification (" + account.getName() + ")");
		}
		
		AccountModifiedTransmission t = new AccountModifiedTransmission(account);
		sendMessage(t);
	}
	
	public void close() {
		
		if (log.isDebugEnabled()) {
			log.debug("Disconnecting from local broker");
		}
		
		try {
			connection.close();
		}
		catch (JMSException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private void sendMessage(Object o) {
		
		Session session = null;
		MessageProducer producer = null;
		try {
			StringWriter writer = new StringWriter();
			Marshaller m = jaxbContext.createMarshaller();
			m.marshal(o, writer);
			String text = writer.getBuffer().toString();
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Topic topic = session.createTopic(UpdateTopic.NAME);
			producer = session.createProducer(topic);
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			
			TextMessage message = session.createTextMessage(text);
			producer.send(message);
			
			producer.close();
			session.close();
		}
		catch (Exception ex) {
			
			if (producer != null) {
				try {
					producer.close();
				}
				catch (JMSException e) {
					if (log.isErrorEnabled()) {
						log.error("Failed to close producer after failure", e);
					}
					else {
						ex.printStackTrace();
					}
				}
			}
			
			if (session != null) {
				try {
					session.close();
				}
				catch (JMSException e) {
					if (log.isErrorEnabled()) {
						log.error("Failed to close session after failure", e);
					}
					else {
						ex.printStackTrace();
					}
				}
			}
			
			throw new RuntimeException("Message transmission failed: " + o, ex);
		}
	}
}
