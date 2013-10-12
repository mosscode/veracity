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
import javax.swing.JTextField;
import javax.swing.SwingConstants;

abstract class ChooseIdentityView extends JPanel {
	
	private JLabel labelIdentityName;
	private JLabel fieldValidationMessage;
	private JButton buttonCancel;
	private JLabel fieldHostedDomainName;
	private JButton buttonContinue;
	private JTextField fieldIdentityName;
	
	protected ChooseIdentityView() {

		setLayout(new BorderLayout());

		final JPanel panel = new JPanel();
		panel.setOpaque(false);
		add(panel, BorderLayout.NORTH);

		final JLabel chooseYourIdentityLabel = new JLabel();
		chooseYourIdentityLabel.setFont(new Font("Sans", Font.BOLD, 14));
		chooseYourIdentityLabel.setText("Choose your Identity Name");
		panel.add(chooseYourIdentityLabel);

		final JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {0,7,0,7};
		gridBagLayout.columnWidths = new int[] {0,7,0,7};
		panel_1.setLayout(gridBagLayout);
		add(panel_1, BorderLayout.CENTER);

		labelIdentityName = new JLabel();
		labelIdentityName.setText("Identity Name:");
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 0;
		panel_1.add(labelIdentityName, gridBagConstraints);

		fieldIdentityName = new JTextField();
		fieldIdentityName.setColumns(12);
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.gridy = 0;
		gridBagConstraints_1.gridx = 2;
		panel_1.add(fieldIdentityName, gridBagConstraints_1);

		fieldHostedDomainName = new JLabel();
		fieldHostedDomainName.setText("@hosted-domain.com");
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.gridy = 0;
		gridBagConstraints_2.gridx = 4;
		panel_1.add(fieldHostedDomainName, gridBagConstraints_2);

		fieldValidationMessage = new JLabel();
		fieldValidationMessage.setForeground(Color.RED);
		fieldValidationMessage.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_3.gridwidth = 5;
		gridBagConstraints_3.gridy = 2;
		gridBagConstraints_3.gridx = 0;
		panel_1.add(fieldValidationMessage, gridBagConstraints_3);

		final JPanel panel_2 = new JPanel();
		panel_2.setOpaque(false);
		add(panel_2, BorderLayout.SOUTH);
		setBackground(Color.WHITE);

		buttonCancel = new JButton();
		buttonCancel.setText("Cancel");
		panel_2.add(buttonCancel);

		buttonContinue = new JButton();
		buttonContinue.setText("Continue");
		panel_2.add(buttonContinue);
	}
	
	protected JTextField getFieldIdentityName() {
		return fieldIdentityName;
	}
	
	protected JButton getButtonContinue() {
		return buttonContinue;
	}
	
	protected JLabel getFieldHostedDomainName() {
		return fieldHostedDomainName;
	}
	
	protected JButton getButtonCancel() {
		return buttonCancel;
	}
	
	protected JLabel getFieldValidationMessage() {
		return fieldValidationMessage;
	}
	
	protected JLabel getLabelIdentityName() {
		return labelIdentityName;
	}
}
