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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.moss.veracity.api.VtPasswordMechanism;

public class ChoosePasswordScreen extends ChoosePasswordView implements PostDisplay {

	private final VtPasswordMechanism password;
	
	private boolean valid;
	
	/**
	 * Enter a new password.
	 */
	public ChoosePasswordScreen(Action cancel, ChoosePasswordAction apply) {
		this.password = null;
		init(cancel, apply);
	}

	/**
	 * Edit an existing password.
	 */
	public ChoosePasswordScreen(VtPasswordMechanism password, Action cancel, ChoosePasswordAction apply) {
		this.password = password;
		init(cancel, apply);
	}
	
	public void postDisplay() {
		if (password != null) {
			getFieldOldPassword().requestFocusInWindow();
		}
		else {
			getFieldNewPassword().requestFocusInWindow();
		}
	}

	private void init(Action cancel, final ChoosePasswordAction apply) {
		
		Action wrapperAction = new AbstractAction((String)apply.getValue(Action.NAME)) {
			public void actionPerformed(ActionEvent e) {
				if (valid) {
					String password = new String(getFieldNewPassword().getPassword());
					apply.passwordChosen(password);
					apply.actionPerformed(e);
				}
			}
		};
		
		if (password == null) {
			getLabelOldPassword().setVisible(false);
			getFieldOldPassword().setVisible(false);
			
		}
		
		getFieldOldPassword().setAction(wrapperAction);
		getFieldNewPassword().setAction(wrapperAction);
		getFieldNewPasswordAgain().setAction(wrapperAction);
		
		getButtonCancel().setAction(cancel);
		getButtonChangePassword().setAction(wrapperAction);
		
		ValidationListener l = new ValidationListener();
		getFieldOldPassword().getDocument().addDocumentListener(l);
		getFieldNewPassword().getDocument().addDocumentListener(l);
		getFieldNewPasswordAgain().getDocument().addDocumentListener(l);
		l.update();
	}
	
	private class ValidationListener implements DocumentListener {
		
		private void update() {
			
			String oldPassword = new String(getFieldOldPassword().getPassword());
			String newPassword = new String(getFieldNewPassword().getPassword());
			String newPasswordAgain = new String(getFieldNewPasswordAgain().getPassword());
			
			if (password != null && !password.getPassword().equals(oldPassword)) {
				getLabelOldPassword().setText("<html><body><span style=\"color: red;\">*</span>Old Password</body></html>");
				getLabelNewPassword().setText("<html><body><span style=\"color: red;\">*</span>New Password</body></html>");
				getLabelNewPasswordAgain().setText("<html><body>New Password Again</body></html>");
				getButtonChangePassword().setEnabled(false);
				getFieldValidationMessage().setText("The old password is incorrect.");
				valid = false;
			}
			else if (newPassword.trim().length() < 8) {
				getLabelOldPassword().setText("<html><body>Old Password</body></html>");
				getLabelNewPassword().setText("<html><body><span style=\"color: red;\">*</span>New Password</body></html>");
				getLabelNewPasswordAgain().setText("<html><body>New Password Again</body></html>");
				getButtonChangePassword().setEnabled(false);
				getFieldValidationMessage().setText("The new password is too short.");
				valid = false;
			}
			else if (!newPassword.equals(newPasswordAgain)) {
				getLabelOldPassword().setText("<html><body>Old Password</body></html>");
				getLabelNewPassword().setText("<html><body><span style=\"color: red;\">*</span>New Password</body></html>");
				getLabelNewPasswordAgain().setText("<html><body><span style=\"color: red;\">*</span>New Password Again</body></html>");
				getButtonChangePassword().setEnabled(false);
				getFieldValidationMessage().setText("The new passwords do not match.");
				valid = false;
			}
			else {
				getLabelOldPassword().setText("<html><body>Old Password</body></html>");
				getLabelNewPassword().setText("<html><body>New Password</body></html>");
				getLabelNewPasswordAgain().setText("<html><body>New Password Again</body></html>");
				getButtonChangePassword().setEnabled(true);
				getFieldValidationMessage().setText("");
				valid = true;
			}
		}

		public void changedUpdate(DocumentEvent e) {
			update();
		}

		public void insertUpdate(DocumentEvent e) {
			update();
		}

		public void removeUpdate(DocumentEvent e) {
			update();
		}
	}
}
