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

import com.moss.veracity.api.Authentication;
import com.moss.veracity.api.Management;
import com.moss.veracity.api.VtAccount;
import com.moss.veracity.api.VtAuthMode;
import com.moss.veracity.api.VtMechanism;
import com.moss.veracity.api.VtPassword;
import com.moss.veracity.api.VtPasswordMechanism;
import com.moss.veracity.api.VtToken;
import com.swtdesigner.SwingResourceManager;

public class LoginScreen extends LoginView implements PostDisplay {
	
	private final String hostedDomainName;
	private final Authentication auth;
	private final Management manage;
	private final Display display;

	public LoginScreen(String hostedDomainName, Authentication auth, Management manage, Display display) {
		this.hostedDomainName = hostedDomainName;
		this.auth = auth;
		this.manage = manage;
		this.display = display;
		
		ActionListener l = new LoginListener();
		getFieldPassword().addActionListener(l);
		getButtonLogin().addActionListener(l);
		getButtonSignup().addActionListener(new SignupListener());
	}
	
	public void postDisplay() {
		getFieldUsername().requestFocusInWindow();
	}

	private class LoginListener implements ActionListener, Runnable {
		
		private Thread thread;
		
		public synchronized void actionPerformed(ActionEvent e) {
			
			if (thread != null) {
				return;
			}
			
			thread = new Thread(this, "LoginThread");
			thread.start();
		}
		
		public void run() {
			
			WaitScreen wait = new WaitScreen("Logging In");
			display.show(wait);
			
			String identity = getFieldUsername().getText();
			String password = new String(getFieldPassword().getPassword());
			
			getFieldUsername().setText("");
			getFieldPassword().setText("");
			
			try {
				
				AccountPersister persister = new AccountPersister(
					auth, 
					manage, 
					identity,
					new VtPassword(password)
				);
				
				ManageScreen manageScreen = new ManageScreen(hostedDomainName, persister, display);
				
				display.show(manageScreen);
			}
			catch (Exception ex) {
				
				ex.printStackTrace();
				
				Action failAction = new AbstractAction("Ok, I'll try again.") {
					public void actionPerformed(ActionEvent e) {
						display.show(LoginScreen.this);
					}
				};
				
				String description;
				
				if (ex instanceof LoginException) {
					description = "You have suppplied invalid login information."; 
				}
				else {
					description = "An error occurred while attempting to log in."; 
					ex.printStackTrace();
				}
				
				FailureScreen failure = new FailureScreen(
					"Login Failed", 
					description, 
					failAction
				);
					
				display.show(failure);
			}
			
			thread = null;
		}
	}
	
	private class SignupListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			ChooseIdentityScreen screen = new ChooseIdentityScreen(
				manage, 
				hostedDomainName, 
				new CancelAction(), 
				new IdentityAction()
			);
			
			display.show(screen);
		}
	}
	
	private class CancelAction extends AbstractAction {
		
		public CancelAction() {
			super("Cancel");
		}

		public void actionPerformed(ActionEvent e) {
			display.show(LoginScreen.this);
		}
	}
	
	private class IdentityAction extends ChooseIdentityAction {
		
		public void actionPerformed(ActionEvent e) {
			
			ChoosePasswordScreen screen = new ChoosePasswordScreen(
				new CancelAction(), 
				new PasswordAction(chosenIdentity)
			);
			
			display.show(screen);
		}
	}
	
	private class PasswordAction extends ChoosePasswordAction {
		
		private final String name;
		
		public PasswordAction(String name) {
			this.name = name;
		}

		public void actionPerformed(ActionEvent e) {
			
			VtMechanism mechanism = new VtPasswordMechanism(password);
			ProfileAction profileAction = new ProfileAction(name, mechanism);
			
			EditProfileScreen screen = new EditProfileScreen(new CancelAction(), profileAction);
			display.show(screen);
		}
	}
	
	private class ProfileAction extends EditProfileAction implements Runnable {
		
		private final String name;
		private final VtMechanism mechanism;
		
		private Thread thread;
		
		public ProfileAction(String name, VtMechanism mechanism) {
			this.name = name;
			this.mechanism = mechanism;
		}

		public synchronized void actionPerformed(ActionEvent e) {
			
			if (thread != null) {
				return;
			}
			
			thread = new Thread(this, "CreateAccountThread");
			thread.start();
		}
		
		public void run() {
			
			VtAccount account = new VtAccount();
			account.setName(name);
			account.setAuthMode(VtAuthMode.USER);
			account.getMechanisms().add(mechanism);
			account.getProfiles().add(editedProfile);
			
			WaitScreen wait = new WaitScreen("Creating Account " + account.getName());
			display.show(wait);
			
			try {
				manage.create(account, null);
			}
			catch (Exception ex) {
				
				ex.printStackTrace();
				
				Action failAction = new AbstractAction("Continue") {
					public void actionPerformed(ActionEvent e) {
						display.show(LoginScreen.this);
					}
				};
				
				FailureScreen failure = new FailureScreen(
					"Account Creation Failed", 
					"An error occurred while attempting to create account " + account.getName(), 
					failAction
				);
					
				display.show(failure);
			}
			
			try {
				
				final AccountPersister persister;
				{
					String password = ((VtPasswordMechanism)mechanism).getPassword();
					VtToken token = new VtPassword(password);
					persister = new AccountPersister(auth, manage, account.getName(), token);
				}
				
				Action logoutAction = new AbstractAction("Log Out", SwingResourceManager.getIcon(AccountCreatedView.class, "/com/moss/veracity/idmanager/logout-32x32.png")) {
					public void actionPerformed(ActionEvent e) {
						display.show(LoginScreen.this);
					}
				};
				
				Action manageAction = new AbstractAction("Manage Account", SwingResourceManager.getIcon(AccountCreatedView.class, "/com/moss/veracity/idmanager/auth-mech-32x32.png")) {
					public void actionPerformed(ActionEvent e) {
						
						ManageScreen screen = new ManageScreen(
							hostedDomainName, 
							persister, 
							display
						);
						
						display.show(screen);
					}
				};
				
				AccountCreatedScreen screen = new AccountCreatedScreen(
					account.getName(), 
					logoutAction, 
					manageAction
				);
				
				display.show(screen);
			}
			catch (Exception ex) {
				
				ex.printStackTrace();
				
				Action failAction = new AbstractAction("Continue") {
					public void actionPerformed(ActionEvent e) {
						display.show(LoginScreen.this);
					}
				};
				
				FailureScreen failure = new FailureScreen(
					"Account Access Error", 
					"An error occurred while verifying created account " + account.getName(), 
					failAction
				);
					
				display.show(failure);
			}
		}
	}
}
