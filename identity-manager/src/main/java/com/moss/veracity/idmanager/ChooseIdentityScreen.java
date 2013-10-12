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

import com.moss.veracity.api.Management;
import com.moss.veracity.api.util.NameParser;
import com.moss.veracity.api.util.ParsedName;

public class ChooseIdentityScreen extends ChooseIdentityView implements PostDisplay {
	
	private final Management manage;
	private final String hostedDomainName;
	
	private boolean valid;
	
	public ChooseIdentityScreen(Management manage, final String hostedDomainName, Action cancel, final ChooseIdentityAction choose) {
		
		this.manage = manage;
		this.hostedDomainName = hostedDomainName;
		
		getFieldHostedDomainName().setText("@" + hostedDomainName);
		
		Action wrappedAction = new AbstractAction((String)choose.getValue(Action.NAME)) {
			public void actionPerformed(ActionEvent e) {
				if (valid) {
					String chosen = getFieldIdentityName().getText().trim() + "@" + hostedDomainName;
					choose.identityChosen(chosen);
					choose.actionPerformed(e);
				}
			}
		};
		
		getButtonCancel().setAction(cancel);
		getButtonContinue().setAction(wrappedAction);
		getFieldIdentityName().setAction(wrappedAction);
		
		ValidationListener l = new ValidationListener();
		getFieldIdentityName().getDocument().addDocumentListener(l);
		
		l.update();
	}
	
	public void postDisplay() {
		getFieldIdentityName().requestFocusInWindow();
	}

	private class ValidationListener implements DocumentListener {
		
		private void update() {
			
			String chosen = getFieldIdentityName().getText().trim();
			String validationMessage = null;
			
			if (chosen.length() == 0) {
				validationMessage = "This identity name is too short";
			}

			if (validationMessage == null) {
				ParsedName name = NameParser.parse(chosen + "@" + hostedDomainName);

				if (name == null) {
					validationMessage = "This identity name contains invalid characters";
				}
			}
			
			if (validationMessage == null) {
				try {
					if (manage.accountExists(chosen + "@" + hostedDomainName)) {
						validationMessage = "This identity name already exists";
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
			if (validationMessage != null) {
				getFieldValidationMessage().setText(validationMessage);
				getLabelIdentityName().setText("<html><body><span style=\"color: red;\">*</span>Identity Name:</body></html>");
				getButtonContinue().setEnabled(false);
				valid = false;
			}
			else {
				getFieldValidationMessage().setText("");
				getLabelIdentityName().setText("<html><body>Identity Name:</body></html>");
				getButtonContinue().setEnabled(true);
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
