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
package com.moss.veracity.idmanager;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.moss.anthroponymy.StFirstMiddleLastName;
import com.moss.veracity.api.VtImage;
import com.moss.veracity.api.VtProfile;

public class EditProfileScreen extends EditProfileView implements PostDisplay {
	
	private VtProfile profile;

	/**
	 * Enter a new profile.
	 */
	public EditProfileScreen(Action cancel, EditProfileAction apply) {
		init(cancel, apply);
	}

	/**
	 * Edit an existing profile.
	 */
	public EditProfileScreen(VtProfile profile, Action cancel, EditProfileAction apply) {
		this.profile = profile;
		init(cancel, apply);
	}
	
	public void postDisplay() {
		getNameEditor().getFieldFirst().requestFocusInWindow();
	}

	private void init(Action cancel, final EditProfileAction apply) {
		
		if (profile == null) {
			profile = new VtProfile();
		}
		
		if (profile.getProfileName() == null) {
			profile.setProfileName("default");
		}
		
		if (profile.getName() == null) {
			profile.setName(new StFirstMiddleLastName("", "", ""));
		}
	
		if (profile.getImage() == null) {
			profile.setImage(new VtImage());
		}
		
		getNameEditor().setModel(profile.getName());
		getIconEditor().setModel(profile.getImage());
		
		getFieldProfileTitle().setText("Editing my Profile");
		
		Action wrapperAction = new AbstractAction((String)apply.getValue(Action.NAME)) {
			public void actionPerformed(ActionEvent e) {
				apply.profileEdited(profile);
				apply.actionPerformed(e);
			}
		};
		
		getButtonUpdateProfile().setAction(wrapperAction);
		
		getButtonCancel().setAction(cancel);
	}
}
