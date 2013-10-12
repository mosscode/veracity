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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;

public class AccountsView extends JPanel {

	private JSplitPane splitPane;
	private JPanel panelContainer;
	private JTextField fieldSearch;
	private JTree treeAccounts;
	private static final long serialVersionUID = 1L;
	public AccountsView() {
		super();
		setLayout(new BorderLayout());

		splitPane = new JSplitPane();
		add(splitPane);

		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		splitPane.setLeftComponent(panel);

		final JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane);

		treeAccounts = new JTree();
		scrollPane.setViewportView(treeAccounts);

		final JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.NORTH);

		final JLabel searchLabel = new JLabel();
		searchLabel.setText("Search:");
		panel_1.add(searchLabel);

		fieldSearch = new JTextField();
		fieldSearch.setColumns(14);
		panel_1.add(fieldSearch);

		panelContainer = new JPanel();
		panelContainer.setLayout(new BorderLayout());
		splitPane.setRightComponent(panelContainer);
	}
	public JTextField getFieldSearch() {
		return fieldSearch;
	}
	public JTree getTreeAccounts() {
		return treeAccounts;
	}
	public JPanel getPanelContainer() {
		return panelContainer;
	}
	public JSplitPane getSplitPane() {
		return splitPane;
	}
}
