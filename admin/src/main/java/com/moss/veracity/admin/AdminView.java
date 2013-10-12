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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class AdminView extends JPanel {

	private JButton buttonLogout;
	private CreateAccountView createAccountView;
	private AccountsView accountsView;
	private ServiceView serviceView;
	private JLabel fieldLogin;
	private JLabel fieldService;
	private JTabbedPane tabbedPane;
	private static final long serialVersionUID = 1L;
	public AdminView() {
		super();
		setLayout(new BorderLayout());

		tabbedPane = new JTabbedPane();
		add(tabbedPane, BorderLayout.CENTER);

		serviceView = new ServiceView();
		tabbedPane.addTab("Service", null, serviceView, null);

		accountsView = new AccountsView();
		tabbedPane.addTab("Accounts", null, accountsView, null);

		createAccountView = new CreateAccountView();
		tabbedPane.addTab("Create Account", null, createAccountView, null);

		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		add(panel, BorderLayout.NORTH);

		final JPanel panel_1 = new JPanel();
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {0,7};
		gridBagLayout.rowHeights = new int[] {0,7,0,7};
		panel_1.setLayout(gridBagLayout);
		final FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_1.setLayout(flowLayout);
		panel.add(panel_1, BorderLayout.WEST);

		final JLabel serviceLabel = new JLabel();
		serviceLabel.setText("Service:");
		panel_1.add(serviceLabel, new GridBagConstraints());

		fieldService = new JLabel();
		fieldService.setText("http://localhost:5063");
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_1.gridy = 0;
		gridBagConstraints_1.gridx = 2;
		panel_1.add(fieldService, gridBagConstraints_1);

		final JLabel loginLabel = new JLabel();
		loginLabel.setText("Login:");
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridx = 0;
		panel_1.add(loginLabel, gridBagConstraints);

		fieldLogin = new JLabel();
		fieldLogin.setText("root@localhost");
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_2.gridy = 2;
		gridBagConstraints_2.gridx = 2;
		panel_1.add(fieldLogin, gridBagConstraints_2);

		buttonLogout = new JButton();
		buttonLogout.setText("Logout");
		panel.add(buttonLogout, BorderLayout.EAST);
	}
	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}
	public JLabel getFieldService() {
		return fieldService;
	}
	public JLabel getFieldLogin() {
		return fieldLogin;
	}
	public ServiceView getServiceView() {
		return serviceView;
	}
	public AccountsView getAccountsView() {
		return accountsView;
	}
	public CreateAccountView getCreateAccountView() {
		return createAccountView;
	}
	public JButton getButtonLogout() {
		return buttonLogout;
	}

}
