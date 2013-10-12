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

import com.moss.identity.Id;
import com.moss.identity.IdProof;
import com.moss.veracity.api.VtEndorsement;
import com.moss.veracity.api.VtProfileDescription;

@SuppressWarnings("serial")
public final class VeracityIdProof extends IdProof {
	
	/*
	 * signed data
	 */
	
	private VeracityId identity;
	
	private long expiration;
	
	private byte[] keyDigest;
	
	private VeracityProfileDescription profileDesc;
	
	/*
	 * unsigned data
	 */
	
	private byte[] signature;
	
	@Override
	public Id id() {
		return getIdentity();
	}
	
	public void fromEndorsement(VtEndorsement e) {
		
		identity = new VeracityId(e.getName());
		expiration = e.getExpiration();
		keyDigest = e.getKeyDigest();
		
		if (e.getProfileDesc() != null) {
			profileDesc = new VeracityProfileDescription();
			profileDesc.setName(e.getProfileDesc().getName());
			profileDesc.setWhenLastModified(e.getProfileDesc().getWhenLastModified());
		}
		
		signature = e.getSignature();
	}
	
	public VtEndorsement toEndorsement() {
		
		VtEndorsement e = new VtEndorsement();
		e.setName(identity.toString());
		e.setExpiration(expiration);
		e.setKeyDigest(keyDigest);
		
		if (profileDesc != null) {
			VtProfileDescription desc = new VtProfileDescription();
			desc.setName(profileDesc.getName());
			desc.setWhenLastModified(profileDesc.getWhenLastModified());
			e.setProfileDesc(desc);
		}
		
		e.setSignature(signature);
		
		return e;
	}
	
	public boolean expired() {
		return expiration < System.currentTimeMillis();
	}
	
	public VeracityId getIdentity() {
		return identity;
	}

	public void setIdentity(VeracityId identity) {
		this.identity = identity;
	}

	public long getExpiration() {
		return expiration;
	}

	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	public byte[] getKeyDigest() {
		return keyDigest;
	}

	public void setKeyDigest(byte[] keyDigest) {
		this.keyDigest = keyDigest;
	}

	public VeracityProfileDescription getProfileDesc() {
		return profileDesc;
	}

	public void setProfileDesc(VeracityProfileDescription profileDesc) {
		this.profileDesc = profileDesc;
	}
}
