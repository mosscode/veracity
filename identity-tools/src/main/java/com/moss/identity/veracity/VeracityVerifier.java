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
package com.moss.identity.veracity;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.moss.identity.Id;
import com.moss.identity.IdVerifier;
import com.moss.identity.Profile;
import com.moss.identity.tools.IdProovingException;
import com.moss.identity.veracity.VeracityIdProof;
import com.moss.identity.veracity.VeracityProfile;
import com.moss.rpcutil.proxy.ProxyFactory;
import com.moss.veracity.api.Authentication;
import com.moss.veracity.api.VtProfile;
import com.moss.veracity.api.VtPublicSignatureKey;
import com.moss.veracity.api.util.CryptoUtil;
import com.moss.veracity.api.util.DataAssembler;
import com.moss.veracity.api.util.HexUtil;
import com.moss.veracity.api.util.NameParser;
import com.moss.veracity.api.util.ParsedName;
import com.moss.veracity.api.util.ServiceURLResolver;

public final class VeracityVerifier implements IdVerifier {
	
	private final Log log = LogFactory.getLog(this.getClass());

	private final int servicePort;
	private final VeracityCachedKeyProvider keyProvider;
	private final ProxyFactory proxyFactory;
	
	private final VeracityIdProof assertion;
	
	private final ParsedName name;
	private final URL authUrl;
	private Authentication auth;

	public VeracityVerifier(int servicePort, VeracityCachedKeyProvider keyProvider, ProxyFactory proxyFactory, VeracityIdProof assertion) throws IdProovingException {
		this.servicePort = servicePort;
		this.keyProvider = keyProvider;
		this.proxyFactory = proxyFactory;
	
		ParsedName name = NameParser.parse(assertion.getIdentity().toString());
		
		if (name == null) {
			throw new IdProovingException("Unable to parse id " + assertion.getIdentity());
		}
		
		URL authUrl = ServiceURLResolver.resolve(name.getServiceName(), servicePort);
		
		if (authUrl == null) {
			throw new IdProovingException("Unable to determine url for " + name.getServiceName());
		}

		this.assertion = assertion;
		this.name = name;
		this.authUrl = authUrl;
	}
	
	public Id id() throws IdProovingException {
		return assertion.getIdentity();
	}

	public boolean verify() throws IdProovingException {
		
		if (assertion.getExpiration() < System.currentTimeMillis()) {
			
			if (log.isDebugEnabled()) {
				log.debug("This assertion is not valid because it has expired: '" + assertion.getIdentity() + "'");
			}
			
			return false;
		}
		
		VeracityCachedKey key = null;
		
		if (keyProvider != null) {
			
			VeracityCachedKey k = keyProvider.getKey(name.getServiceName(), assertion.getKeyDigest());
			
			if (k != null && k.getData() != null) {
				
				long expiration = k.getExpiration();
				long now = System.currentTimeMillis();
				
				if (expiration < now) {
					throw new IdProovingException("Signing key expired " + ((now - expiration) / 1000f) + " seconds ago: " + HexUtil.toHex(assertion.getKeyDigest()));
				}
				else {
					key = k;	
				}
			}
		}
		
		if (key == null) {
			
			if (log.isDebugEnabled()) {
				log.debug("Fetching key: " + HexUtil.toHex(assertion.getKeyDigest()));
			}
			
			refreshService();
			
			VtPublicSignatureKey k = auth.getSignatureKey(assertion.getKeyDigest());
			
			if (k == null) {
				throw new IdProovingException("No such key found in veracity instance, key has expired: " + HexUtil.toHex(assertion.getKeyDigest()));
			}
			
			if (k.data() == null) {
				throw new RuntimeException();
			}
			
			key = keyProvider.create();
			key.setData(k.data());
			key.setExpiration(k.expiration());
			key.setServiceName(name.getServiceName());
			key.setDigest(assertion.getKeyDigest());
			
			keyProvider.putKey(key);
			
			byte[] digest = CryptoUtil.digest(key.getData());
			
			if (log.isDebugEnabled()) {
				log.debug("Fetched signing key\n    Digest: " + HexUtil.toHex(digest) + "\n    Expires in " + ((key.getExpiration() - System.currentTimeMillis()) / 1000f) + " seconds");
			}
		}
		
		DataAssembler assembler = new DataAssembler()
		.add(assertion.getIdentity().toString())
		.add(assertion.getExpiration())
		.add(assertion.getKeyDigest());
		
		if (assertion.getProfileDesc() != null) {
			assembler.add(assertion.getProfileDesc().getName());
			assembler.add(assertion.getProfileDesc().getWhenLastModified());
		}

		byte[] signedData = assembler.get();
		
		try {
			return CryptoUtil.verify(key.getData(), assertion.getSignature(), signedData);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new IdProovingException("Failed to verify veracity assertion signature");
		}
	}
	
	public Profile getProfile() throws IdProovingException {
		
		refreshService();
		
		VtProfile p = auth.getProfile(assertion.toEndorsement());
		
		VeracityProfile profile = new VeracityProfile();
		profile.fromVtProfile(p);
		
		return profile;
	}
	
	private void refreshService() throws IdProovingException {
		
		if (auth != null) {
			return;
		}
		
		auth = proxyFactory.create(Authentication.class, authUrl);
	}
}
