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

import java.util.ArrayList;
import java.util.List;

import com.moss.identity.Id;
import com.moss.identity.IdProof;
import com.moss.identity.IdProofRecipie;
import com.moss.identity.IdProofToken;
import com.moss.identity.IdVerifier;
import com.moss.identity.standard.Password;
import com.moss.identity.standard.PasswordProofRecipie;
import com.moss.identity.tools.IdProover;
import com.moss.identity.tools.IdProovingException;
import com.moss.identity.tools.IdToolPlugin;
import com.moss.identity.veracity.VeracityId;
import com.moss.identity.veracity.VeracityIdProof;
import com.moss.rpcutil.proxy.ProxyFactory;

/**
 * This provider implements a partially-distributed authentication system.
 * It can verify an Assertion in a distributed manner. If it trusts the
 * providerUrl in a given VeractityAssertion, it can verify the validity
 * of such an Assertion. However, it requires a single authProviderUrl 
 * to be configured in order to for it to authenticate. This constraint 
 * serves primarily to simplify this protocol for rapid development.
 */
public final class VeracityIdToolPlugin implements IdToolPlugin<VeracityId, Password, VeracityIdProof> {
	
	private int servicePort = 80;
	private String profileName = null;
	private VeracityTrustMode trustMode = VeracityTrustMode.UNRESTRICTED;
	private List<String> serviceNameList = new ArrayList<String>();
	
	private VeracityCachedKeyProvider keyProvider;
	
	private final ProxyFactory proxyFactory;
	
	public VeracityIdToolPlugin(ProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
		this.keyProvider = new DefaultCachedKeyProvider();
	}
	
	public VeracityIdToolPlugin(ProxyFactory proxyFactory, String portPropertyText) {
		this.proxyFactory = proxyFactory;
		if (portPropertyText != null) {
			servicePort = new Integer(portPropertyText);
		}
	}
	
	public boolean supports(IdProofRecipie recipie) {
		return recipie instanceof PasswordProofRecipie && recipie.id() instanceof VeracityId;
	}
	

	public IdProover makeProover(IdProofRecipie recipie) throws IdProovingException {
		PasswordProofRecipie r = (PasswordProofRecipie) recipie;
		return makeProover((VeracityId)r.id(), r.password());
	}
	public boolean supports(Id identity, IdProofToken confirmation) {
		return
			identity instanceof VeracityId
			&&
			confirmation instanceof Password;
	}
	
	public IdProover makeProover(VeracityId identity, Password confirmation) throws IdProovingException {
		return new VeracityIdProover(servicePort, profileName, proxyFactory, identity, confirmation);
	}
	
	public boolean canVerify(IdProof assertion) {
		return assertion instanceof VeracityIdProof;
	}

	public IdVerifier createVerifier(VeracityIdProof proof) throws IdProovingException {
		return new VeracityVerifier(servicePort, keyProvider, proxyFactory, proof);
	}
	
	public VeracityCachedKeyProvider getKeyProvider() {
		return keyProvider;
	}

	public void setKeyProvider(VeracityCachedKeyProvider keyProvider) {
		this.keyProvider = keyProvider;
	}

	public int getServicePort() {
		return servicePort;
	}

	public void setServicePort(int servicePort) {
		this.servicePort = servicePort;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public VeracityTrustMode getTrustMode() {
		return trustMode;
	}

	public void setTrustMode(VeracityTrustMode trustMode) {
		this.trustMode = trustMode;
	}

	public List<String> getServiceNameList() {
		return serviceNameList;
	}

	public void setServiceNameList(List<String> serviceNameList) {
		this.serviceNameList = serviceNameList;
	}
}
