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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class LoginScreen {

	private final List<LoginListener> listeners;
	private final LoginView view;
	private final LoginPanel loginPanel;
	
	public LoginScreen() {
		listeners = new ArrayList<LoginListener>();
		view = new LoginView();
		loginPanel = view.getLoginPanel();
		
		EnterListener l = new EnterListener();
		loginPanel.getFieldIdentity().addActionListener(l);
		loginPanel.getFieldPassword().addActionListener(l);
		loginPanel.getFieldExplicitService().addActionListener(l);
		
		loginPanel.getFieldUseExplicitService().setSelected(false);
		loginPanel.getFieldExplicitService().setEnabled(false);
		loginPanel.getFieldUseExplicitService().addActionListener(new ServiceToggleListener());
	}
	
	private class EnterListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			String identity = loginPanel.getFieldIdentity().getText();
			String password = new String(loginPanel.getFieldPassword().getPassword());
			boolean useExplicit = loginPanel.getFieldUseExplicitService().isSelected();
			String explicitService = loginPanel.getFieldExplicitService().getText();
			
			LoginInfo info = new LoginInfo(
				identity,
				password,
				useExplicit ? explicitService : null
			);
			
			for (LoginListener l : listeners) {
				l.loginAttempted(info);
			}
		}
	}
	
	private class ServiceToggleListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			if (loginPanel.getFieldUseExplicitService().isSelected()) {
				loginPanel.getFieldExplicitService().setEnabled(true);
				loginPanel.getFieldExplicitService().setText("http://localhost:5063");
			}
			else {
				loginPanel.getFieldExplicitService().setEnabled(false);
				loginPanel.getFieldExplicitService().setText("");
			}
		}
	}
	
	public void add(LoginListener l) {
		listeners.add(l);
	}
	
	public JPanel getView() {
		return view;
	}
	
	public void prepareForReuse() {
		view.getLoginPanel().getFieldPassword().setText("");
	}
	
	public static void main(String[] args) throws Exception {
		
		LoginScreen screen = new LoginScreen();
		
		screen.add(new LoginListener() {
			public void loginAttempted(LoginInfo info) {
				System.out.println("login attempted");
			}
		});
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(screen.getView());
		frame.setSize(600, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
