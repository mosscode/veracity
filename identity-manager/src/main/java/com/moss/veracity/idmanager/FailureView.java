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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

abstract class FailureView extends JPanel {

	private JButton buttonOk;
	private JLabel fieldDescription;
	private JLabel fieldTitle;
	
	protected FailureView() {
		super();
		setBackground(Color.WHITE);
		setLayout(new BorderLayout());

		final JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridBagLayout());
		add(panel, BorderLayout.CENTER);

		final JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		panel_1.setBorder(new LineBorder(Color.black, 1, false));
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {7,0,7};
		gridBagLayout.rowHeights = new int[] {7,7,7,0,7,7,0,7};
		panel_1.setLayout(gridBagLayout);
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 0;
		panel.add(panel_1, gridBagConstraints);

		fieldTitle = new JLabel();
		fieldTitle.setText("Login Failed");
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.gridx = 1;
		gridBagConstraints_2.gridy = 1;
		panel_1.add(fieldTitle, gridBagConstraints_2);

		fieldDescription = new JLabel();
		fieldDescription.setText("An error occurred while attempting to log in.");
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.gridy = 4;
		gridBagConstraints_1.gridx = 1;
		panel_1.add(fieldDescription, gridBagConstraints_1);

		buttonOk = new JButton();
		buttonOk.setText("Ok, I'll try again.");
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.gridy = 6;
		gridBagConstraints_3.gridx = 1;
		panel_1.add(buttonOk, gridBagConstraints_3);
	}
	
	protected JLabel getFieldDescription() {
		return fieldDescription;
	}

	protected JButton getButtonOk() {
		return buttonOk;
	}
	
	protected JLabel getFieldTitle() {
		return fieldTitle;
	}
}
