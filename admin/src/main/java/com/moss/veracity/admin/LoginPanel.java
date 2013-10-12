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
package com.moss.veracity.admin;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;

	private JCheckBox fieldUseExplicitService;
	private JPasswordField fieldPassword;
	private JTextField fieldExplicitService;
	private JTextField fieldIdentity;
	
	LoginPanel() {
		
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {7,7,0,0,7};
		gridBagLayout.rowHeights = new int[] {0,7,7,7,0,7};
		setLayout(gridBagLayout);

		final JLabel identityLabel = new JLabel();
		identityLabel.setText("Identity");
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 1;
		add(identityLabel, gridBagConstraints);

		fieldIdentity = new JTextField();
		fieldIdentity.setColumns(14);
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.gridy = 0;
		gridBagConstraints_2.gridx = 3;
		add(fieldIdentity, gridBagConstraints_2);

		final JLabel passwordLabel = new JLabel();
		passwordLabel.setText("Password");
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_3.gridy = 2;
		gridBagConstraints_3.gridx = 1;
		add(passwordLabel, gridBagConstraints_3);

		fieldPassword = new JPasswordField();
		fieldPassword.setColumns(14);
		final GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
		gridBagConstraints_6.gridy = 2;
		gridBagConstraints_6.gridx = 3;
		add(fieldPassword, gridBagConstraints_6);

		fieldUseExplicitService = new JCheckBox();
		fieldUseExplicitService.setText("Use explicit service");
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.gridy = 4;
		gridBagConstraints_1.gridx = 1;
		add(fieldUseExplicitService, gridBagConstraints_1);

		fieldExplicitService = new JTextField();
		fieldExplicitService.setColumns(14);
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.gridy = 4;
		gridBagConstraints_4.gridx = 3;
		add(fieldExplicitService, gridBagConstraints_4);
	}
	
	JTextField getFieldIdentity() {
		return fieldIdentity;
	}
	
	JPasswordField getFieldPassword() {
		return fieldPassword;
	}
	
	JTextField getFieldExplicitService() {
		return fieldExplicitService;
	}
	
	JCheckBox getFieldUseExplicitService() {
		return fieldUseExplicitService;
	}
}
