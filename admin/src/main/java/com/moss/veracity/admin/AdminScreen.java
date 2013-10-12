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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.moss.anthroponymy.StFirstMiddleLastName;
import com.moss.veracity.api.VtAccount;
import com.moss.veracity.api.VtAuthMode;
import com.moss.veracity.api.VtConfiguration;
import com.moss.veracity.api.VtImage;
import com.moss.veracity.api.VtMechanism;
import com.moss.veracity.api.VtPasswordMechanism;
import com.moss.veracity.api.VtProfile;

public class AdminScreen {

	/*
	 * environment
	 */
	
	private final Environment env;
	
	private final List<LogoutListener> logoutListeners;
	
	private final AdminView view;
	private final WelcomeView accountWelcomeView;
	private final AccountView accountView;
	private final ProfileView profileView;
	private final DefaultMutableTreeNode accountsModelRoot;
	private final DefaultTreeModel accountsModel;
	
	public AdminScreen(Environment env) {
		
		this.env = env;
		
		this.logoutListeners = new ArrayList<LogoutListener>();
	
		view = new AdminView();
		accountWelcomeView = new WelcomeView();
		accountView = new AccountView();
		profileView = new ProfileView();
		
		view.getFieldLogin().setText(env.login().getName());
		view.getFieldService().setText(env.baseUrl().toString());
		
		view.getServiceView().getButtonRefresh().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshService();
			}
		});
		
		view.getServiceView().getButtonUpdate().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateService();
			}
		});
		
		view.getAccountsView().getPanelContainer().add(accountWelcomeView, BorderLayout.CENTER);
		
		accountsModelRoot = new DefaultMutableTreeNode();
		accountsModel = new DefaultTreeModel(accountsModelRoot);
		view.getAccountsView().getTreeAccounts().setModel(accountsModel);
		view.getAccountsView().getTreeAccounts().setRootVisible(false);
		view.getAccountsView().getTreeAccounts().setShowsRootHandles(true);
		view.getAccountsView().getTreeAccounts().setCellRenderer(new AccountNodeRenderer());
		view.getAccountsView().getTreeAccounts().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		view.getAccountsView().getTreeAccounts().getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				
				JPanel nextView = null;
				
				TreePath path = e.getPath();
				if (e.getPath() == null) {
					nextView = accountWelcomeView;
				}
				else {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
					Object value = node.getUserObject();
					
					if (value instanceof String) {
						String selectedName = (String)value;
						displayAccount(selectedName);
						nextView = accountView;
					}
					else if (value instanceof VtAccount) {
						String selectedName = ((VtAccount)value).getName();
						displayAccount(selectedName);
						nextView = accountView;
					}
					else if (value instanceof VtProfile) {
						
//						DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
//						VtAccount account = 
//						
//						String selectedName = ((VtAccount)value).getName();
//						displayAccount(selectedName);
//						nextView = accountView;
					}
				}
				
				if (nextView != null) {
					view.getAccountsView().getPanelContainer().removeAll();
					view.getAccountsView().getPanelContainer().add(nextView, BorderLayout.CENTER);					
				}
			}
		});
		
		for (VtAuthMode mode : VtAuthMode.values()) {
			view.getCreateAccountView().getFieldAuthorization().addItem(mode);
			accountView.getFieldAuthorization().addItem(mode);
		}
		
		view.getCreateAccountView().getButtonCreate().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createAccount();
			}
		});
		
		accountView.getButtonRefresh().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayAccount(accountView.getFieldIdentity().getText());
			}
		});
		
		accountView.getButtonUpdate().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateAccount();
			}
		});
		
		refreshService();
		
		refreshAccounts();
		
		view.getButtonLogout().addActionListener(new LogoutHandler());
	}
	
	public void add(LogoutListener l) {
		logoutListeners.add(l);
	}
	
	private class LogoutHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			for (LogoutListener l : logoutListeners) { 
				l.logout();
			}
		}
	}
	
	public void postDisplayActions() {
		view.getAccountsView().getSplitPane().setDividerLocation(.40);
	}
	
	private void refreshService() {
		
		try {
			VtConfiguration config = env.management().getConfiguration(env.login());
			view.getServiceView().getFieldEndorsementExpirationTimeout().setValue(config.getEndorsementExpiration());
			view.getServiceView().getFieldSigningKeyExpirationTimeout().setValue(config.getSigningKeyExpiration());
			view.getServiceView().getFieldSigningKeyRefreshTimeout().setValue(config.getSigningKeyRefresh());
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private void updateService() {
		
		try {
			VtConfiguration config = new VtConfiguration();
			config.setEndorsementExpiration( ((Number)view.getServiceView().getFieldEndorsementExpirationTimeout().getValue()).longValue());
			config.setSigningKeyExpiration( ((Number)view.getServiceView().getFieldSigningKeyExpirationTimeout().getValue()).longValue());
			config.setSigningKeyRefresh( ((Number)view.getServiceView().getFieldSigningKeyRefreshTimeout().getValue()).longValue());
			env.management().configure(config, env.login());
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private void refreshAccounts() {
		
		final List<String> accountNames;
		try {
			accountNames = env.management().listAccountNames(env.login());
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		Collections.sort(accountNames);
		
		Runnable refresh = new Runnable() {

			public void run() {
				
				accountsModelRoot.removeAllChildren();
				
				for (String name : accountNames) {
					DefaultMutableTreeNode accountNode = new DefaultMutableTreeNode(name);
					accountsModelRoot.add(accountNode);
				}
				
				accountsModel.nodeStructureChanged(accountsModelRoot);
			}
		};
		
		SwingUtilities.invokeLater(refresh);
	}
	
	private void displayAccount(final String accountName) {
		
		final VtAccount account;
		try {
			account = env.management().read(accountName, env.login());
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		Runnable refresh = new Runnable() {

			public void run() {
				
				DefaultMutableTreeNode node = null;
				
				for (int i=0; i<accountsModelRoot.getChildCount(); i++) {
					
					DefaultMutableTreeNode thisNode = (DefaultMutableTreeNode) accountsModelRoot.getChildAt(i);
					
					Object value = thisNode.getUserObject();
					
					if (value instanceof String) {
						
						String name = (String)value;
						
						if (accountName.equals(name)) {
							node = thisNode;
							break;
						}
					}
					else if (value instanceof VtAccount) {
						
						VtAccount account = (VtAccount) value;
						
						if (accountName.equals(account.getName())) {
							node = thisNode;
							break;
						}
					}
				}
				
				if (node == null) {
					throw new RuntimeException("Node not found: " + accountName);
				}
				
				node.setUserObject(account);
				
				node.removeAllChildren();
				
				for (VtProfile p : account.getProfiles()) {
					node.add(new DefaultMutableTreeNode(p));
				}
				
				accountsModel.nodeStructureChanged(node);
				
				accountView.getFieldIdentity().setText(account.getName());
				accountView.getFieldAuthorization().setSelectedItem(account.getAuthMode());
				
				VtProfile defaultProfile = null;

				for (VtProfile p : account.getProfiles()) {
					if (p.getProfileName().equals("default")) {
						defaultProfile = p;
					}
				}
				
				if (defaultProfile != null) {
					
					if (defaultProfile.getName() != null) {
						accountView.getFieldFirstName().setText(defaultProfile.getName().getFirstname());
						accountView.getFieldMiddleInitial().setText(defaultProfile.getName().getMiddleInitial());
						accountView.getFieldLastName().setText(defaultProfile.getName().getLastname());
					}
					
					if (defaultProfile.getImage() != null && defaultProfile.getImage().getData() != null) {
						accountView.getImageSelector().setImage(defaultProfile.getImage().getData());
					}
				}
			}
		};
		
		SwingUtilities.invokeLater(refresh);
	}
	
	private void createAccount() {
		
		String identity = view.getCreateAccountView().getFieldIdentity().getText();
		VtAuthMode mode = (VtAuthMode) view.getCreateAccountView().getFieldAuthorization().getSelectedItem();
		String password = new String(view.getCreateAccountView().getFieldPassword().getPassword());
		String first = view.getCreateAccountView().getFieldFirstName().getText();
		String middle = view.getCreateAccountView().getFieldMiddleInitial().getText();
		String last = view.getCreateAccountView().getFieldLastName().getText();
		
		if (mode == null) {
			JOptionPane.showMessageDialog(null, "An authorization level is required");
			return;
		}
		
		VtImage image = new VtImage();
		image.setData(view.getCreateAccountView().getImageSelector().getImage());
		
		VtProfile profile = new VtProfile();
		profile.setProfileName("default");
		profile.setName(new StFirstMiddleLastName(first, middle, last));
		profile.setImage(image);
		
		VtAccount account = new VtAccount();
		account.setName(identity);
		account.setAuthMode(mode);
		account.add(new VtPasswordMechanism(password));
		account.add(profile);
		
		try {
			env.management().create(account, env.login());
			refreshAccounts();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not create account: " + ex.getMessage());
		}
	}
	
	private void updateAccount() {
		
		String identity = accountView.getFieldIdentity().getText();
		VtAccount account = null;
		
		for (int i=0; i<accountsModelRoot.getChildCount(); i++) {
			
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) accountsModelRoot.getChildAt(i);
			Object value = node.getUserObject();
			
			if (value instanceof VtAccount && ((VtAccount)value).getName().equals(identity)) {
				account = (VtAccount) value;
				break;
			}
		}
		
		if (account == null) {
			throw new RuntimeException("Cannot find account: " + identity);
		}
		
		VtAuthMode mode = (VtAuthMode) accountView.getFieldAuthorization().getSelectedItem();
		String password = new String(accountView.getFieldPassword().getPassword());
		String first = accountView.getFieldFirstName().getText();
		String middle = accountView.getFieldMiddleInitial().getText();
		String last = accountView.getFieldLastName().getText();
		
		VtImage image = new VtImage();
		image.setData(accountView.getImageSelector().getImage());
		
		VtAccount updatedAccount = new VtAccount();
		updatedAccount.setName(identity);
		updatedAccount.setAuthMode(mode);
		updatedAccount.getMechanisms().addAll(account.getMechanisms());
		updatedAccount.getProfiles().addAll(account.getProfiles());
		
		if (!password.equals("")) {
			
			VtPasswordMechanism mechanism = null;
			
			for (VtMechanism m : updatedAccount.getMechanisms()) {
				if (m instanceof VtPasswordMechanism) {
					mechanism = (VtPasswordMechanism)m;
					break;
				}
			}
			
			if (mechanism == null) {
				mechanism = new VtPasswordMechanism(password);
				updatedAccount.add(mechanism);
			}
			else {
				mechanism.setPassword(password);
			}
		}
		
		VtProfile defaultProfile = null;
		
		for (VtProfile p : updatedAccount.getProfiles()) {
			if (p.getProfileName().equals("default")) {
				defaultProfile = p;
				break;
			}
		}
		
		if (defaultProfile == null) {
			defaultProfile = new VtProfile();
			defaultProfile.setProfileName("default");
		}
		
		defaultProfile.setName(new StFirstMiddleLastName(first, middle, last));
		defaultProfile.setImage(image);
		
		try {
			env.management().update(updatedAccount, env.login());
			refreshAccounts();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not create account: " + ex.getMessage());
		}
	}
	
	public JPanel getView() {
		return view;
	}
}
