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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class AccountView extends JPanel {

	private JButton buttonRefresh;
	private ImageSelector imageSelector;
	private JLabel fieldIdentity;
	private JComboBox fieldAuthorization;
	private JTextField fieldLastName;
	private JTextField fieldMiddleInitial;
	private JTextField fieldFirstName;
	private JPasswordField fieldPassword;
	private JButton buttonUpdate;
	private static final long serialVersionUID = 1L;
	public AccountView() {
		super();
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {7,7,7,7,7,7,7,0,7,0,7,7,7,0,7};
		gridBagLayout.columnWidths = new int[] {7,7,7,0,7};
		setLayout(gridBagLayout);

		final JLabel fieldSigningKeyExpirationLabel = new JLabel();
		fieldSigningKeyExpirationLabel.setText("Identity");
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridx = 1;
		add(fieldSigningKeyExpirationLabel, gridBagConstraints);

		fieldIdentity = new JLabel();
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.weightx = 1.0;
		gridBagConstraints_1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_1.gridy = 1;
		gridBagConstraints_1.gridx = 4;
		add(fieldIdentity, gridBagConstraints_1);

		final JLabel accessLevelLabel = new JLabel();
		accessLevelLabel.setText("Authorization");
		final GridBagConstraints gridBagConstraints_15 = new GridBagConstraints();
		gridBagConstraints_15.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_15.gridy = 3;
		gridBagConstraints_15.gridx = 1;
		add(accessLevelLabel, gridBagConstraints_15);

		fieldAuthorization = new JComboBox();
		final GridBagConstraints gridBagConstraints_16 = new GridBagConstraints();
		gridBagConstraints_16.weightx = 1.0;
		gridBagConstraints_16.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_16.gridy = 3;
		gridBagConstraints_16.gridx = 4;
		add(fieldAuthorization, gridBagConstraints_16);

		final JLabel labelEndorsementExpirationTimeout = new JLabel();
		labelEndorsementExpirationTimeout.setText("Password");
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_2.gridy = 5;
		gridBagConstraints_2.gridx = 1;
		add(labelEndorsementExpirationTimeout, gridBagConstraints_2);

		fieldPassword = new JPasswordField();
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.weightx = 1.0;
		gridBagConstraints_3.ipadx = 90;
		gridBagConstraints_3.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_3.gridy = 5;
		gridBagConstraints_3.gridx = 4;
		add(fieldPassword, gridBagConstraints_3);

		final JLabel label = new JLabel();
		label.setText("First Name");
		final GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
		gridBagConstraints_6.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_6.gridy = 7;
		gridBagConstraints_6.gridx = 1;
		add(label, gridBagConstraints_6);

		fieldFirstName = new JTextField();
		final GridBagConstraints gridBagConstraints_9 = new GridBagConstraints();
		gridBagConstraints_9.weightx = 1.0;
		gridBagConstraints_9.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_9.gridy = 7;
		gridBagConstraints_9.gridx = 4;
		add(fieldFirstName, gridBagConstraints_9);

		final JLabel label_1 = new JLabel();
		label_1.setText("Middle Initial");
		final GridBagConstraints gridBagConstraints_7 = new GridBagConstraints();
		gridBagConstraints_7.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_7.gridy = 9;
		gridBagConstraints_7.gridx = 1;
		add(label_1, gridBagConstraints_7);

		fieldMiddleInitial = new JTextField();
		final GridBagConstraints gridBagConstraints_10 = new GridBagConstraints();
		gridBagConstraints_10.weightx = 1.0;
		gridBagConstraints_10.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_10.gridy = 9;
		gridBagConstraints_10.gridx = 4;
		add(fieldMiddleInitial, gridBagConstraints_10);

		final JLabel label_2 = new JLabel();
		label_2.setText("Last Name");
		final GridBagConstraints gridBagConstraints_8 = new GridBagConstraints();
		gridBagConstraints_8.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_8.gridy = 11;
		gridBagConstraints_8.gridx = 1;
		add(label_2, gridBagConstraints_8);

		fieldLastName = new JTextField();
		final GridBagConstraints gridBagConstraints_11 = new GridBagConstraints();
		gridBagConstraints_11.weightx = 1.0;
		gridBagConstraints_11.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_11.gridy = 11;
		gridBagConstraints_11.gridx = 4;
		add(fieldLastName, gridBagConstraints_11);

		final JLabel imageLabel = new JLabel();
		imageLabel.setText("Image");
		final GridBagConstraints gridBagConstraints_17 = new GridBagConstraints();
		gridBagConstraints_17.anchor = GridBagConstraints.NORTH;
		gridBagConstraints_17.fill = GridBagConstraints.BOTH;
		gridBagConstraints_17.gridy = 14;
		gridBagConstraints_17.gridx = 1;
		add(imageLabel, gridBagConstraints_17);

		imageSelector = new ImageSelector();
		final GridBagConstraints gridBagConstraints_12 = new GridBagConstraints();
		gridBagConstraints_12.fill = GridBagConstraints.BOTH;
		gridBagConstraints_12.gridy = 14;
		gridBagConstraints_12.gridx = 4;
		add(imageSelector, gridBagConstraints_12);

		final JPanel panel = new JPanel();
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints_4.weighty = 1;
		gridBagConstraints_4.gridy = 15;
		gridBagConstraints_4.gridx = 1;
		add(panel, gridBagConstraints_4);

		final JPanel panel_1 = new JPanel();
		final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.gridwidth = 4;
		gridBagConstraints_5.gridy = 16;
		gridBagConstraints_5.gridx = 1;
		add(panel_1, gridBagConstraints_5);

		buttonRefresh = new JButton();
		buttonRefresh.setText("Refresh Account");
		panel_1.add(buttonRefresh);

		buttonUpdate = new JButton();
		buttonUpdate.setText("Update Account");
		panel_1.add(buttonUpdate);
	}
	public JButton getButtonUpdate() {
		return buttonUpdate;
	}
	public JTextField getFieldLastName() {
		return fieldLastName;
	}
	public JTextField getFieldMiddleInitial() {
		return fieldMiddleInitial;
	}
	public JTextField getFieldFirstName() {
		return fieldFirstName;
	}
	public JPasswordField getFieldPassword() {
		return fieldPassword;
	}
	public JComboBox getFieldAuthorization() {
		return fieldAuthorization;
	}
	public JLabel getFieldIdentity() {
		return fieldIdentity;
	}
	public ImageSelector getImageSelector() {
		return imageSelector;
	}
	public JButton getButtonRefresh() {
		return buttonRefresh;
	}
	
}
