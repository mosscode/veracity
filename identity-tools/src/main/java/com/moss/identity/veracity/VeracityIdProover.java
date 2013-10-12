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

import com.moss.identity.Id;
import com.moss.identity.IdProof;
import com.moss.identity.Profile;
import com.moss.identity.standard.Password;
import com.moss.identity.tools.IdProover;
import com.moss.identity.tools.IdProovingException;
import com.moss.identity.veracity.VeracityId;
import com.moss.identity.veracity.VeracityIdProof;
import com.moss.identity.veracity.VeracityProfile;
import com.moss.identity.veracity.VeracityProfileDescription;
import com.moss.rpcutil.proxy.ProxyFactory;
import com.moss.veracity.api.Authentication;
import com.moss.veracity.api.VtEndorsement;
import com.moss.veracity.api.VtPassword;
import com.moss.veracity.api.VtProfile;
import com.moss.veracity.api.VtToken;
import com.moss.veracity.api.util.NameParser;
import com.moss.veracity.api.util.ParsedName;
import com.moss.veracity.api.util.ServiceURLResolver;

public final class VeracityIdProover implements IdProover {
	
	/*
	 * Core data
	 */
	private final VeracityId identity;
	private final Password confirmation;
	
	/*
	 * misc. operating params
	 */
	private final int servicePort;
	private final String profileName;
	private final ProxyFactory proxyFactory;
	
	
	private URL authUrl;
	private Authentication auth;
	private VeracityIdProof assertion;
	private VeracityProfile profile;
	
	public VeracityIdProover(int servicePort, String profileName, ProxyFactory proxyFactory, VeracityId identity, Password confirmation) throws IdProovingException {
		this.servicePort = servicePort;
		this.profileName = profileName;
		this.proxyFactory = proxyFactory;
		
		this.identity = identity;
		this.confirmation = confirmation;
		
		if (identity == null) {
			throw new IdProovingException("Identity is required for authentication");
		}
		
		if (confirmation == null) {
			throw new IdProovingException("Confirmation is required for authentication");
		}
		
		ParsedName name = NameParser.parse(identity.toString());
		
		if (name == null) {
			throw new IdProovingException("Unable to parse id " + identity);
		}
		
		authUrl = ServiceURLResolver.resolve(name.getServiceName(), servicePort);
		
		if (authUrl == null) {
			throw new IdProovingException("Unable to determine url for " + name.getServiceName());
		}

		auth = proxyFactory.create(Authentication.class, authUrl);
	}
	
	/**
	 * Convenience constructor.  This is a qQuickie way to get a handle on a veracity assertion factory with the sensible defaults
	 */
	public VeracityIdProover(VeracityId identity, Password confirmation) throws IdProovingException {
		this(80, "default", VeracityProxyFactory.create(), identity, confirmation);
	}
	
	public Id getIdentity() {
		return identity;
	}
	
	public IdProof giveProof() throws IdProovingException {
		
		if (isValid(assertion)) {
			return assertion;
		}
		
		String id = identity.toString();
		VtToken token = new VtPassword(confirmation.getPassword());
		
		VtEndorsement e;
		try {
			e = auth.verify(id, token, profileName);
		} catch (Exception e1) {
			throw new IdProovingException(e1);
		}
		
		if (e == null) {
			throw new IdProovingException("Authorization with veracity provider url " + authUrl + " denied");
		}
		
		assertion = new VeracityIdProof();
		assertion.fromEndorsement(e);

		return assertion;
	}
	
	public Profile profile() throws IdProovingException {
		
		refreshProfile();
		
		if (profile == null) {
			return null;
		}
		
		return profile;
	}
	
	private void refreshAuth() throws IdProovingException {
		
		if (isValid(assertion)) {
			return;
		}
		
		String id = identity.toString();
		VtToken token = new VtPassword(confirmation.getPassword());
		
		VtEndorsement e = auth.verify(id, token, profileName);
		
		if (e == null) {
			throw new IdProovingException("Authorization with veracity provider url " + authUrl + " denied");
		}
		
		assertion = new VeracityIdProof();
		assertion.fromEndorsement(e);
	}
	
	private void refreshProfile() throws IdProovingException {
		
		/*
		 * If the profile name, is null we can't possibly have requested an 
		 * endorsement that contains one
		 */
		
		if (profileName == null) {
			profile = null;
			return;
		}
		
		/*
		 * If the retrieved assertion doesn't have a reference to a profile,
		 * then give up. (this shouldn't happen, but hey)
		 */
		
		refreshAuth();
		
		VeracityProfileDescription desc = assertion.getProfileDesc();
		
		if (desc == null) {
			profile = null;
			return;
		}
		
		/*
		 * If the cached profile is not null and not out of date, reuse it.
		 */
		
		if (profile != null && desc.getWhenLastModified() == profile.getWhenLastModified()) {
			return;
		}
		
		/*
		 * The assertion says it refers to a profile, and we either don't have
		 * it or its out of date, so fetch it.
		 */

		VtProfile p = auth.getProfile(assertion.toEndorsement());
		
		profile = new VeracityProfile();
		profile.fromVtProfile(p);
	}
	
	private boolean isValid(VeracityIdProof e) {
		
		long now = System.currentTimeMillis();
		long tenSecondsFromNow = now + (1000 * 10);
		
		return
			e != null
			&&
			e.getExpiration() > tenSecondsFromNow;
	}
}
