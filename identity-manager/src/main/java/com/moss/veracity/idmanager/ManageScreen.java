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
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.moss.veracity.api.VtMechanism;
import com.moss.veracity.api.VtPasswordMechanism;
import com.moss.veracity.api.VtProfile;

public class ManageScreen extends ManageView {
	
	private final String hostedDomainName;
	private final AccountPersister persister;
	private final Display display;
	
	public ManageScreen(String hostedDomainName, AccountPersister persister, Display display) {
		this.hostedDomainName = hostedDomainName;
		this.persister = persister;
		this.display = display;
		getFieldWelcome().setText("<html><body style=\"text-align: center;\">Welcome, " + persister.account().getName() + ", what would you like to do?</body></html>");
		getButtonChangePassword().addActionListener(new ChangePasswordListener());
		getButtonUpdateProfile().addActionListener(new EditProfileListener());
		getButtonLogOut().addActionListener(new LogoutListener());
	}

	private class ChangePasswordListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			final VtPasswordMechanism pass;
			{
				VtPasswordMechanism passwordMechanism = null;
				
				for (VtMechanism m : persister.account().getMechanisms()) {

					if (! (m instanceof VtPasswordMechanism)) {
						continue;
					}

					passwordMechanism = (VtPasswordMechanism)m;
				}
				
				pass = passwordMechanism;
			}
			
			Action cancelAction = new AbstractAction("Cancel") {
				public void actionPerformed(ActionEvent e) {
					display.show(ManageScreen.this);
				}
			};
			
			ChoosePasswordAction changeAction = new ChoosePasswordAction() {
				public void actionPerformed(ActionEvent e) {
					
					if (pass == null) {
						persister.account().getMechanisms().add(new VtPasswordMechanism(password));
					}
					else {
						pass.setPassword(password);
					}
					
					saveChanges();
				}
			};
			
			ChoosePasswordScreen screen;
			
			if (pass == null) {
				screen = new ChoosePasswordScreen(cancelAction, changeAction);
			}
			else {
				screen = new ChoosePasswordScreen(pass, cancelAction, changeAction);
			}
			
			display.show(screen);
		}
	}
	
	private class EditProfileListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			final VtProfile profile;
			{
				VtProfile defaultProfile = null;

				for (VtProfile p : persister.account().getProfiles()) {
					if ("default".equals(p.getProfileName())) {
						defaultProfile = p;
					}
				}
				
				profile = defaultProfile;
			}
			
			Action cancelAction = new AbstractAction("Cancel") {
				public void actionPerformed(ActionEvent e) {
					display.show(ManageScreen.this);
				}
			};
			
			final EditProfileAction applyAction = new EditProfileAction() {
				public void actionPerformed(ActionEvent e) {
					
					if (profile == null) {
						persister.account().getProfiles().add(editedProfile);
					}
					
					saveChanges();
				}
			};
			
			EditProfileScreen screen;
			
			if (profile == null) {
				screen = new EditProfileScreen(cancelAction, applyAction);
			}
			else {
				screen = new EditProfileScreen(profile, cancelAction, applyAction);
			}
			
			display.show(screen);
		}
	}
	
	private void saveChanges() {
		
		try {
			display.show(new WaitScreen("Saving Changes"));
			
			persister.save();
			
			display.show(ManageScreen.this);
		}
		catch (Exception ex) {
			
			ex.printStackTrace();
			
			Action failAction = new AbstractAction("Continue") {
				public void actionPerformed(ActionEvent e) {
					display.show(ManageScreen.this);
				}
			};
			
			FailureScreen failure = new FailureScreen(
				"Failed to Save Changes",
				"An error occurred while attempting to save your changes.",
				failAction
			);
			
			display.show(failure);
		}
	}
	
	private class LogoutListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			LoginScreen screen = new LoginScreen(
				hostedDomainName,
				persister.auth(), 
				persister.manage(), 
				display
			);
			
			display.show(screen);
		}
	}
}
