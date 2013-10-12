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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.LineBorder;

abstract class LoginView extends JPanel {
	
	private JTextPane fieldInstructions;
	private JButton buttonSignup;
	private JTextField fieldUsername;
	private JButton buttonLogin;
	private JPasswordField fieldPassword;

	protected LoginView() {

		setLayout(new BorderLayout());

		final JPanel panel_2 = new JPanel();
		panel_2.setOpaque(false);
		final GridBagLayout gridBagLayout_1 = new GridBagLayout();
		gridBagLayout_1.rowHeights = new int[] {0};
		panel_2.setLayout(gridBagLayout_1);
		add(panel_2);

		final JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel_2.add(panel, new GridBagConstraints());
		panel.setBorder(new LineBorder(Color.black, 1, false));
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {7,7,7,0,7,7,7};
		gridBagLayout.columnWidths = new int[] {7,7,7,7,7};
		panel.setLayout(gridBagLayout);

		final JLabel identityLabel = new JLabel();
		identityLabel.setText("Identity");
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.gridx = 1;
		gridBagConstraints_3.gridy = 1;
		gridBagConstraints_3.fill = GridBagConstraints.HORIZONTAL;
		panel.add(identityLabel, gridBagConstraints_3);

		fieldUsername = new JTextField();
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridx = 3;
		panel.add(fieldUsername, gridBagConstraints);

		final JLabel passwordLabel = new JLabel();
		passwordLabel.setText("Password");
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_1.gridy = 3;
		gridBagConstraints_1.gridx = 1;
		panel.add(passwordLabel, gridBagConstraints_1);

		fieldPassword = new JPasswordField();
		fieldPassword.setColumns(14);
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_2.gridy = 3;
		gridBagConstraints_2.gridx = 3;
		panel.add(fieldPassword, gridBagConstraints_2);

		final JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_4.gridwidth = 3;
		gridBagConstraints_4.gridy = 5;
		gridBagConstraints_4.gridx = 1;
		panel.add(panel_1, gridBagConstraints_4);

		buttonLogin = new JButton();
		buttonLogin.setText("Login");
		panel_1.add(buttonLogin);

		buttonSignup = new JButton();
		buttonSignup.setText("Sign Up");
		panel_1.add(buttonSignup);

		final JPanel panel_4 = new JPanel();
		panel_4.setOpaque(false);
		panel_4.setLayout(new BorderLayout());
		add(panel_4, BorderLayout.NORTH);
		setBackground(Color.WHITE);

		final JPanel panel_3 = new JPanel();
		panel_3.setOpaque(false);
		panel_4.add(panel_3, BorderLayout.NORTH);
		panel_3.setLayout(new FlowLayout());

		final JLabel identityManagerLabel = new JLabel();
		identityManagerLabel.setFont(new Font("Sans", Font.BOLD, 16));
		identityManagerLabel.setText("Identity Manager");
		panel_3.add(identityManagerLabel);

		final JPanel panel_5 = new JPanel();
		panel_5.setOpaque(false);
		panel_5.setLayout(new BorderLayout());
		panel_4.add(panel_5, BorderLayout.SOUTH);

		fieldInstructions = new JTextPane();
		fieldInstructions.setContentType("text/html");
		fieldInstructions.setEditable(false);
		fieldInstructions.setOpaque(false);
		fieldInstructions.setText("<html><body style=\"text-align: center;\">Please login below to manage your identity, or click Sign Up to create a new identity.</body></html>");
		panel_5.add(fieldInstructions, BorderLayout.NORTH);
	}
	
	protected JButton getButtonLogin() {
		return buttonLogin;
	}
	
	protected JPasswordField getFieldPassword() {
		return fieldPassword;
	}
	
	protected JTextField getFieldUsername() {
		return fieldUsername;
	}
	
	protected JButton getButtonSignup() {
		return buttonSignup;
	}
	protected JTextPane getFieldInstructions() {
		return fieldInstructions;
	}
}
