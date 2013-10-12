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

import com.moss.veracity.api.VeracityException;
import com.moss.veracity.api.VtImage;
import com.moss.veracity.api.VtProfile;

public final class Profile {
	
	private static final long serialVersionUID = 1L;
	private static final int MAX_IMAGE_SIZE = 1024 * 1000; // 1MB

	private String profileName;
	
	private FirstMiddleLastName name;
	
	private byte[] imageData;
	
	private long whenLastModified;
	
	public Profile() {}
	
	public Profile(VtProfile dto) {
		
		profileName = dto.getProfileName();
		
		if (dto.getName() != null) {
			name = new FirstMiddleLastName(dto.getName());
		}
		
		if (dto.getImage() != null && dto.getImage().getData() != null) {
			
			if (dto.getImage().getData().length > MAX_IMAGE_SIZE) {
				throw new VeracityException("Image exceeds maximum size of " + MAX_IMAGE_SIZE);
			}
			
			imageData = dto.getImage().getData();
		}
	}
	
	public VtProfile toDto() {
		
		VtImage image = new VtImage();
		image.setData(this.imageData);
		
		VtProfile dto = new VtProfile();
		dto.setProfileName(profileName);
		dto.setName(name.toDto());
		dto.setWhenLastModified(whenLastModified);
		dto.setImage(image);
		
		return dto;
	}
	
	public void updateTimestamp() {
		whenLastModified = System.currentTimeMillis();
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public FirstMiddleLastName getName() {
		return name;
	}

	public void setName(FirstMiddleLastName name) {
		this.name = name;
	}

	public long getWhenLastModified() {
		return whenLastModified;
	}

	public void setWhenLastModified(long whenLastModified) {
		this.whenLastModified = whenLastModified;
	}

	public byte[] getImageData() {
		return imageData;
	}

	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}
}
