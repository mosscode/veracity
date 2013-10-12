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

import java.security.KeyPair;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.moss.veracity.api.VeracityException;
import com.moss.veracity.api.util.HexUtil;
import com.moss.veracity.core.cluster.UpdateTransmitter;
import com.moss.veracity.core.config.Configuration;
import com.moss.veracity.core.data.PersistenceManager;
import com.moss.veracity.core.data.PublicSignatureKey;

public final class EndorsementManager {

	private final Log log = LogFactory.getLog(this.getClass());
	private final PersistenceManager persistence;
	private final UpdateTransmitter transmitter;
	private final Object keyLock;
	
	private Configuration config;
	private KeyData keyData;
	
	public EndorsementManager(PersistenceManager persistence, UpdateTransmitter transmitter) {
		this.persistence = persistence;
		this.transmitter = transmitter;
		this.keyLock = new Object();
	}

	public void configure(Configuration config) {
		synchronized (keyLock) {
			
			if (log.isDebugEnabled()) {
				log.debug("Accepting new configuration");
			}
			
			this.config = config;
			
			if (keyData != null) {
				
				if (log.isDebugEnabled()) {
					log.debug("Forcing key refresh to accomodate reconfiguration");
				}
				
				keyData = null;
				getModel();
			}
		}
	}
	
	public EndorsementModel getModel() {
		
		if (config == null) {
			throw new RuntimeException("EndorsementManager has not been configured.");
		}
		
		final KeyData data;
		
		synchronized (keyLock) {
			
			boolean refresh = true;
			
			if (keyData != null) {
				
				if (log.isDebugEnabled()) {
					log.debug("Inspecting current signing key");
				}
				
				long now = System.currentTimeMillis();
				long millisTillRefresh = keyData.refreshInstant - now;
				long millisTillExpiry = keyData.expirationInstant - now;
				long millisTillExpiryImminent = millisTillExpiry - (config.getEndorsementExpiration() + 1000);
				
				if (log.isDebugEnabled()) {
					log.debug("Millis until key refresh required: " + millisTillRefresh);
					log.debug("Millis until key expiry imminent: " + millisTillExpiryImminent);
				}
				
				if (millisTillRefresh > 0 && millisTillExpiryImminent > 0) {
					if (log.isDebugEnabled()) {
						
						long ttl;
						if (millisTillRefresh < millisTillExpiryImminent) {
							ttl = millisTillRefresh;
						}
						else {
							ttl = millisTillExpiryImminent;
						}
						
						log.debug("Re-using key " + HexUtil.toHex(keyData.publicKeyDigest) + " for another " + (ttl / 1000f) + " seconds");
					}
					refresh = false;
				}
			}
			
			if (refresh) {
				
				if (log.isDebugEnabled()) {
					log.debug("Creating new key");
				}
				
				try {
					keyData = new KeyData(config.getSigningKeyRefresh(), config.getSigningKeyExpiration());
				}
				catch (Exception ex) {
					throw new VeracityException(ex);
				}
				
				PublicSignatureKey publicSignatureKey = new PublicSignatureKey(
					keyData.keyPair.getPublic().getEncoded(),
					keyData.expirationInstant,
					keyData.publicKeyDigest
				);
				
				persistence.save(publicSignatureKey, null);
				transmitter.keyCreated(publicSignatureKey);
			}
			
			data = keyData.duplicate();
		}
		
		EndorsementModel model = new EndorsementModel() {

			public long endorsementExpiration() {
				return config.getEndorsementExpiration();
			}

			public KeyPair keyPair() {
				return data.keyPair;
			}

			public long keyExpirationInstant() {
				return data.expirationInstant;
			}

			public byte[] keyDigest() {
				return data.publicKeyDigest;
			}
		};
		
		return model;
	}
}
