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
package com.moss.veracity.core.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.moss.veracity.api.VeracityException;
import com.moss.veracity.api.VtAccount;
import com.moss.veracity.api.VtAuthMode;
import com.moss.veracity.api.VtMechanism;
import com.moss.veracity.api.VtPasswordMechanism;
import com.moss.veracity.api.VtProfile;

@XmlRootElement
public final class Account {
	
	private String name;
	private VtAuthMode authMode;
	private List<Mechanism> mechanisms = new ArrayList<Mechanism>();
	private List<Profile> profiles = new ArrayList<Profile>();
	
	public void fromDto(VtAccount dto) {
		
		authMode = dto.getAuthMode();
		
		mechanisms.clear();
		
		for (VtMechanism dtoMechanism : dto.getMechanisms()) {
			
			if (dtoMechanism instanceof VtPasswordMechanism) {
				
				VtPasswordMechanism dtoPasswordMechanism = (VtPasswordMechanism)dtoMechanism;
				
				PasswordMechanism mechanism = new PasswordMechanism();
				mechanism.setPassword(dtoPasswordMechanism.getPassword());
				
				add(mechanism);
			}
			else {
				throw new VeracityException("Unsupported mechanism: " + dtoMechanism);
			}
		}
		
		profiles.clear();
		
		List<String> names = new ArrayList<String>();
		
		for (VtProfile dtoProfile : dto.getProfiles()) {
			
			if (!names.contains(dtoProfile.getProfileName())) {
				names.add(dtoProfile.getProfileName());
			}
			else {
				throw new VeracityException("A profile's name must be unique.");
			}
			
			Profile p = new Profile(dtoProfile);
			p.updateTimestamp();
			add(p);
		}
	}
	
	public VtAccount toDto() {
		
		VtAccount dto = new VtAccount();
		
		dto.setName(name);
		dto.setAuthMode(authMode);
		
		for (Mechanism mechanism : mechanisms) {
			if (mechanism instanceof PasswordMechanism) {
				
				PasswordMechanism passwordMechanism = (PasswordMechanism) mechanism;
				
				VtPasswordMechanism dtoMechanism = new VtPasswordMechanism(passwordMechanism.getPassword());
				
				dto.add(dtoMechanism);
			}
			else {
				throw new VeracityException("Mechanism not supported: " + mechanism);
			}
		}
		
		for (Profile profile : profiles) {
			dto.add(profile.toDto());
		}
		
		return dto;
	}
	
	public void add(Mechanism m) {
		mechanisms.add(m);
	}
	
	public void add(Profile p) {
		profiles.add(p);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Mechanism> getMechanisms() {
		return mechanisms;
	}

	public void setMechanisms(List<Mechanism> mechanisms) {
		this.mechanisms = mechanisms;
	}

	public VtAuthMode getAuthMode() {
		return authMode;
	}

	public void setAuthMode(VtAuthMode authMode) {
		this.authMode = authMode;
	}

	public List<Profile> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<Profile> profiles) {
		this.profiles = profiles;
	}
}
