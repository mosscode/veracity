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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;

abstract class ChoosePasswordView extends JPanel {
	
	private JLabel labelNewPasswordAgain;
	private JLabel labelNewPassword;
	private JLabel fieldValidationMessage;
	private JLabel labelOldPassword;
	private JButton buttonCancel;
	private JButton buttonChangePassword;
	private JPasswordField fieldNewPasswordAgain;
	private JPasswordField fieldNewPassword;
	private JPasswordField fieldOldPassword;
	
	protected ChoosePasswordView() {

		setLayout(new BorderLayout());

		final JPanel panel = new JPanel();
		panel.setOpaque(false);
		add(panel, BorderLayout.NORTH);

		final JLabel changingMyPasswordLabel = new JLabel();
		changingMyPasswordLabel.setFont(new Font("Sans", Font.BOLD, 14));
		changingMyPasswordLabel.setText("Changing my Password");
		panel.add(changingMyPasswordLabel);

		final JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {7,7,7,0,7};
		gridBagLayout.rowHeights = new int[] {7,7,7,0,7,0,7,7,7};
		panel_1.setLayout(gridBagLayout);
		add(panel_1, BorderLayout.CENTER);

		labelOldPassword = new JLabel();
		labelOldPassword.setHorizontalAlignment(SwingConstants.RIGHT);
		labelOldPassword.setText("Old Password:");
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridx = 1;
		panel_1.add(labelOldPassword, gridBagConstraints);

		fieldOldPassword = new JPasswordField();
		fieldOldPassword.setColumns(14);
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.gridy = 1;
		gridBagConstraints_3.gridx = 3;
		panel_1.add(fieldOldPassword, gridBagConstraints_3);

		labelNewPassword = new JLabel();
		labelNewPassword.setHorizontalAlignment(SwingConstants.RIGHT);
		labelNewPassword.setText("New Password:");
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_1.gridy = 3;
		gridBagConstraints_1.gridx = 1;
		panel_1.add(labelNewPassword, gridBagConstraints_1);

		fieldNewPassword = new JPasswordField();
		fieldNewPassword.setColumns(14);
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.gridy = 3;
		gridBagConstraints_4.gridx = 3;
		panel_1.add(fieldNewPassword, gridBagConstraints_4);

		labelNewPasswordAgain = new JLabel();
		labelNewPasswordAgain.setHorizontalAlignment(SwingConstants.RIGHT);
		labelNewPasswordAgain.setText("New Password (Again):");
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_2.gridy = 5;
		gridBagConstraints_2.gridx = 1;
		panel_1.add(labelNewPasswordAgain, gridBagConstraints_2);

		fieldNewPasswordAgain = new JPasswordField();
		fieldNewPasswordAgain.setColumns(14);
		final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.gridy = 5;
		gridBagConstraints_5.gridx = 3;
		panel_1.add(fieldNewPasswordAgain, gridBagConstraints_5);

		fieldValidationMessage = new JLabel();
		fieldValidationMessage.setForeground(Color.RED);
		fieldValidationMessage.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
		gridBagConstraints_6.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_6.gridwidth = 3;
		gridBagConstraints_6.gridy = 7;
		gridBagConstraints_6.gridx = 1;
		panel_1.add(fieldValidationMessage, gridBagConstraints_6);

		final JPanel panel_2 = new JPanel();
		panel_2.setOpaque(false);
		add(panel_2, BorderLayout.SOUTH);
		setBackground(Color.WHITE);

		buttonCancel = new JButton();
		buttonCancel.setText("Cancel");
		panel_2.add(buttonCancel);

		buttonChangePassword = new JButton();
		buttonChangePassword.setText("Change Password");
		panel_2.add(buttonChangePassword);
	}

	protected JPasswordField getFieldOldPassword() {
		return fieldOldPassword;
	}
	
	protected JPasswordField getFieldNewPassword() {
		return fieldNewPassword;
	}
	
	protected JPasswordField getFieldNewPasswordAgain() {
		return fieldNewPasswordAgain;
	}
	
	protected JButton getButtonChangePassword() {
		return buttonChangePassword;
	}
	
	protected JButton getButtonCancel() {
		return buttonCancel;
	}

	protected  JLabel getLabelOldPassword() {
		return labelOldPassword;
	}
	
	protected JLabel getFieldValidationMessage() {
		return fieldValidationMessage;
	}
	
	protected JLabel getLabelNewPassword() {
		return labelNewPassword;
	}
	
	protected JLabel getLabelNewPasswordAgain() {
		return labelNewPasswordAgain;
	}
}
