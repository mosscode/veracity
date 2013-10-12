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
package com.moss.veracity.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class VtAccount implements Serializable {

	private String name;
	private VtAuthMode authMode;
	private List<VtMechanism> mechanisms = new ArrayList<VtMechanism>();
	private List<VtProfile> profiles = new ArrayList<VtProfile>();
	
	public void add(VtMechanism mechanism) {
		mechanisms.add(mechanism);
	}
	
	public void add(VtProfile profile) {
		profiles.add(profile);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public VtAuthMode getAuthMode() {
		return authMode;
	}

	public void setAuthMode(VtAuthMode authMode) {
		this.authMode = authMode;
	}

	public List<VtMechanism> getMechanisms() {
		return mechanisms;
	}

	public void setMechanisms(List<VtMechanism> mechanisms) {
		this.mechanisms = mechanisms;
	}

	public List<VtProfile> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<VtProfile> profiles) {
		this.profiles = profiles;
	}
}
