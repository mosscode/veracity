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
import javax.swing.border.TitledBorder;


abstract class EditProfileView extends JPanel {
	
	private JButton buttonCancel;
	private JLabel fieldProfileTitle;
	private JButton buttonUpdateProfile;
	private FirstMiddeLastEditor nameEditor;
	private ImageEditor iconEditor;
	
	protected EditProfileView() {

		setLayout(new BorderLayout());

		final JPanel panel = new JPanel();
		panel.setOpaque(false);
		add(panel, BorderLayout.NORTH);

		fieldProfileTitle = new JLabel();
		fieldProfileTitle.setFont(new Font("Sans", Font.BOLD, 14));
		panel.add(fieldProfileTitle);

		final JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {7,7,7,7};
		panel_1.setLayout(gridBagLayout);
		add(panel_1, BorderLayout.CENTER);

		nameEditor = new FirstMiddeLastEditor();
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridx = 0;
		panel_1.add(nameEditor, gridBagConstraints);

		iconEditor = new ImageEditor();
		iconEditor.setContentAreaFilled(false);
		iconEditor.setOpaque(false);
		iconEditor.setBorder(new TitledBorder(null, "Icon Selection", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_1.gridy = 4;
		gridBagConstraints_1.gridx = 0;
		panel_1.add(iconEditor, gridBagConstraints_1);

		final JLabel label = new JLabel();
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.weighty = 1;
		gridBagConstraints_2.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints_2.gridy = 5;
		gridBagConstraints_2.gridx = 0;
		panel_1.add(label, gridBagConstraints_2);

		final JPanel panel_2 = new JPanel();
		panel_2.setOpaque(false);
		add(panel_2, BorderLayout.SOUTH);
		setBackground(Color.WHITE);

		buttonCancel = new JButton();
		buttonCancel.setText("Cancel");
		panel_2.add(buttonCancel);

		buttonUpdateProfile = new JButton();
		buttonUpdateProfile.setText("Update Profile");
		panel_2.add(buttonUpdateProfile);
	}
	
	protected ImageEditor getIconEditor() {
		return iconEditor;
	}
	
	protected FirstMiddeLastEditor getNameEditor() {
		return nameEditor;
	}
	
	protected JButton getButtonUpdateProfile() {
		return buttonUpdateProfile;
	}

	protected JLabel getFieldProfileTitle() {
		return fieldProfileTitle;
	}
	
	protected JButton getButtonCancel() {
		return buttonCancel;
	}
}
