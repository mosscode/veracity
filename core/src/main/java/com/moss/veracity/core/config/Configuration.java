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
package com.moss.veracity.core.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.moss.veracity.api.VtConfiguration;
import com.moss.veracity.api.VtUpdateSource;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class Configuration implements Serializable {
	
	private static final long ONE_MINUTE = 1000 * 60;
	private static final long DEFAULT_SIGNING_KEY_EXPIRATION = ONE_MINUTE * 60;
	private static final long DEFAULT_ENDORSEMENT_EXPIRATION = ONE_MINUTE * 5;
	private static final long DEFAULT_SIGNING_KEY_REFRESH = ONE_MINUTE * 30;
	
	private long signingKeyRefresh = DEFAULT_SIGNING_KEY_REFRESH;
	
	private long signingKeyExpiration = DEFAULT_SIGNING_KEY_EXPIRATION;
	
	private long endorsementExpiration = DEFAULT_ENDORSEMENT_EXPIRATION;
	
	/*
	 * The list of peers we're configured to mirror.
	 */
	@XmlElement(name="updateSource")
	private List<UpdateSource> updateSources = new ArrayList<UpdateSource>();
	
	public void fromDto(VtConfiguration dto) {
		signingKeyExpiration = dto.getSigningKeyExpiration();
		endorsementExpiration = dto.getEndorsementExpiration();
		signingKeyRefresh = dto.getSigningKeyRefresh();
		
		updateSources.clear();
		for (VtUpdateSource u : dto.getUpdateSources()) {
			UpdateSource s = new UpdateSource(u.getHostname(), u.getPort());
			updateSources.add(s);
		}
	}
	
	public VtConfiguration toDto() {
		VtConfiguration config = new VtConfiguration();
		config.setEndorsementExpiration(endorsementExpiration);
		config.setSigningKeyExpiration(signingKeyExpiration);
		config.setSigningKeyRefresh(signingKeyRefresh);
		return config;
	}
	
	public Configuration copy() {
		Configuration copy = new Configuration();
		copy.setEndorsementExpiration(endorsementExpiration);
		copy.setSigningKeyExpiration(signingKeyExpiration);
		copy.setSigningKeyRefresh(signingKeyRefresh);
		for (UpdateSource source : updateSources) {
			copy.getUpdateSources().add(source.copy());
		}
		return copy;
	}

	public long getSigningKeyExpiration() {
		return signingKeyExpiration;
	}

	public void setSigningKeyExpiration(long signingKeyExpiration) {
		this.signingKeyExpiration = signingKeyExpiration;
	}

	public long getEndorsementExpiration() {
		return endorsementExpiration;
	}

	public void setEndorsementExpiration(long assertionExpiration) {
		this.endorsementExpiration = assertionExpiration;
	}

	public long getSigningKeyRefresh() {
		return signingKeyRefresh;
	}

	public void setSigningKeyRefresh(long signingKeyRefresh) {
		this.signingKeyRefresh = signingKeyRefresh;
	}

	public List<UpdateSource> getUpdateSources() {
		return updateSources;
	}

	public void setUpdateSources(List<UpdateSource> updateSources) {
		this.updateSources = updateSources;
	}
}
