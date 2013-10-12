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

import java.util.Date;

import javax.jws.WebService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.moss.veracity.api.Authentication;
import com.moss.veracity.api.VeracityException;
import com.moss.veracity.api.VtAuthMode;
import com.moss.veracity.api.VtEndorsement;
import com.moss.veracity.api.VtProfile;
import com.moss.veracity.api.VtProfileDescription;
import com.moss.veracity.api.VtPublicSignatureKey;
import com.moss.veracity.api.VtToken;
import com.moss.veracity.api.XmlSeeAlso;
import com.moss.veracity.api.util.CryptoUtil;
import com.moss.veracity.api.util.DataAssembler;
import com.moss.veracity.api.util.NameParser;
import com.moss.veracity.api.util.ParsedName;
import com.moss.veracity.core.data.Account;
import com.moss.veracity.core.data.Mechanism;
import com.moss.veracity.core.data.PersistenceManager;
import com.moss.veracity.core.data.Profile;
import com.moss.veracity.core.data.PublicSignatureKey;
import com.sleepycat.je.LockMode;

@WebService(
	endpointInterface="com.moss.veracity.api.Authentication",
	targetNamespace="http://core.veracity.moss.com/"
)
public final class AuthenticationImpl implements Authentication {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	private final PersistenceManager persistence;
	private final EndorsementManager endorsementManager;
	
	public AuthenticationImpl(PersistenceManager persistence, EndorsementManager endorsementManager) {
		this.persistence = persistence;
		this.endorsementManager = endorsementManager;
	}

	public VtEndorsement verify(String name, VtToken auth, String profileName) {
		
		ParsedName parsed = NameParser.parse(name);
		
		if (parsed == null) {
			if (log.isDebugEnabled()) {
				log.debug("supplied name not parsable: " + name);
			}
			return null;
		}
		
		Account account = persistence.account(name, null, LockMode.DEFAULT);
		
		if (account == null) {
			if (log.isDebugEnabled()) {
				log.debug("account not found: " + name);
			}
			return null;
		}
		
		if (VtAuthMode.DISABLED == account.getAuthMode()) {
			if (log.isDebugEnabled()) {
				log.debug("account is disabled: " + name);
			}
			return null;
		}
		
		VtProfileDescription profileDesc = null;
		
		if (profileName != null) {
		
			for (Profile p : account.getProfiles()) {
				if (p.getProfileName().equals(profileName)) {
					profileDesc = new VtProfileDescription();
					profileDesc.setName(profileName);
					profileDesc.setWhenLastModified(p.getWhenLastModified());
				}
			}
			
			if (profileDesc == null) {
				if (log.isDebugEnabled()) {
					log.debug("Could not find a matching profile for profileName " + profileName);
				}
				return null;
			}
		}
		
		Mechanism mechanism = null;
		
		for (Mechanism m : account.getMechanisms()) {
			if (m.supports(auth)) {
				mechanism = m;
				break;
			}
		}
		
		if (mechanism == null) {
			if (log.isDebugEnabled()) {
				log.debug("auth mechanism not supported by this account: " + auth);
			}
		}
		
		if (!mechanism.verify(auth)) {
			if (log.isDebugEnabled()) {
				log.debug("authentication failed for account name " + name);
			}
			return null;
		}
		
		EndorsementModel model = endorsementManager.getModel();
		
		VtEndorsement signed;
		try {
			
			long expirationInstant = System.currentTimeMillis() + model.endorsementExpiration();
			
			signed = new VtEndorsement();
			signed.setName(name);
			signed.setExpiration(expirationInstant);
			signed.setKeyDigest(model.keyDigest());
			signed.setProfileDesc(profileDesc);
			
			DataAssembler assembler = new DataAssembler()
			.add(signed.getName())
			.add(signed.getExpiration())
			.add(signed.getKeyDigest());
			
			if (signed.getProfileDesc() != null) {
				assembler.add(signed.getProfileDesc().getName());
				assembler.add(signed.getProfileDesc().getWhenLastModified());
			}

			byte[] data = assembler.get();
			
			byte[] signature = CryptoUtil.sign(model.keyPair().getPrivate(), data);

			if (!CryptoUtil.verify(model.keyPair().getPublic().getEncoded(), signature, data)) {
				throw new VeracityException("Cannot verify self-signed signature!");
			}
			
			signed.setSignature(signature);
		}
		catch (Exception ex) {
			if(ex instanceof VeracityException) {
				throw (VeracityException)ex;
			}
			throw new VeracityException(ex);
		}
		
		Date date = new Date(signed.getExpiration());
		if (log.isDebugEnabled()) {
			long ttl = signed.getExpiration() - System.currentTimeMillis();
			log.debug("Issuing endorsement for " + name + " expiring at: " + date + "(ttl: " + ttl + ")"); 
		}

        return signed;
	}
	
	public VtPublicSignatureKey getSignatureKey(byte[] digest) {

		PublicSignatureKey key = persistence.publicSigningKey(digest, null, null);
		
		if (key == null) {
			return null;
		}
		else {
			return new VtPublicSignatureKey(key.key(), key.expiration());
		}
	}

	public VtProfile getProfile(VtEndorsement e) {
		
		if (e.getProfileDesc() == null || e.getProfileDesc().getName() == null) {
			if (log.isDebugEnabled()) {
				log.debug("Invalid profile description");
			}
			return null;
		}
		
		Verifier v = new Verifier();
		PublicSignatureKey key = persistence.publicSigningKey(e.getKeyDigest(), null, null);
		
		if (!v.isValid(e, key)) {
			if (log.isDebugEnabled()) {
				log.debug("Signature is invalid");
			}
			return null;
		}
		
		Account account = persistence.account(e.getName(), null, LockMode.DEFAULT);
		
		if (account == null) {
			if (log.isDebugEnabled()) {
				log.debug("Unknown account: " + e.getName());
			}
			return null;
		}
		
		VtProfile profile = null;
		
		for (Profile p : account.getProfiles()) {
			if (p.getProfileName().equals(e.getProfileDesc().getName())) {
				profile = p.toDto();
			}
		}
		
		return profile;
	}

	public void register(XmlSeeAlso registry) {}
}
