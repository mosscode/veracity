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
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import com.swtdesigner.SwingResourceManager;

@SuppressWarnings("serial")
abstract class ManageView extends JPanel {
	
	private JTextPane fieldWelcome;
	private JButton buttonLogOut;
	private JButton buttonUpdateProfile;
	private JButton buttonChangePassword;
	
	protected ManageView() {
		
		setLayout(new BorderLayout());

		final JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new BorderLayout());
		add(panel, BorderLayout.NORTH);

		fieldWelcome = new JTextPane();
		fieldWelcome.setOpaque(false);
		fieldWelcome.setEditable(false);
		fieldWelcome.setContentType("text/html");
		fieldWelcome.setText("<html><body>Welcome, fred@domain.tld, what would you like to do?</body></html>");
		panel.add(fieldWelcome);

		final JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {0,7,0,7,0,7};
		panel_1.setLayout(gridBagLayout);
		add(panel_1, BorderLayout.CENTER);
		setBackground(Color.WHITE);

		buttonChangePassword = new JButton();
		buttonChangePassword.setHorizontalAlignment(SwingConstants.LEFT);
		buttonChangePassword.setIcon(SwingResourceManager.getIcon(ManageView.class, "/com/moss/veracity/idmanager/auth-mech-32x32.png"));
		buttonChangePassword.setText("Change my Password");
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 0;
		panel_1.add(buttonChangePassword, gridBagConstraints);

		buttonUpdateProfile = new JButton();
		buttonUpdateProfile.setHorizontalAlignment(SwingConstants.LEFT);
		buttonUpdateProfile.setIcon(SwingResourceManager.getIcon(ManageView.class, "/com/moss/veracity/idmanager/profile-32x32.png"));
		buttonUpdateProfile.setText("Update my Profile");
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_1.gridy = 2;
		gridBagConstraints_1.gridx = 0;
		panel_1.add(buttonUpdateProfile, gridBagConstraints_1);

		buttonLogOut = new JButton();
		buttonLogOut.setHorizontalAlignment(SwingConstants.LEFT);
		buttonLogOut.setIcon(SwingResourceManager.getIcon(ManageView.class, "/com/moss/veracity/idmanager/logout-32x32.png"));
		buttonLogOut.setText("Log Out");
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_3.gridy = 5;
		gridBagConstraints_3.gridx = 0;
		panel_1.add(buttonLogOut, gridBagConstraints_3);
	}
	
	protected JButton getButtonChangePassword() {
		return buttonChangePassword;
	}
	
	protected JButton getButtonUpdateProfile() {
		return buttonUpdateProfile;
	}
	
	protected JButton getButtonLogOut() {
		return buttonLogOut;
	}
	
	protected JTextPane getFieldWelcome() {
		return fieldWelcome;
	}
}
