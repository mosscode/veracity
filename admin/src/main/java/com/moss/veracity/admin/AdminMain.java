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

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class AdminMain {
	
	private JPanel view;
	private Environment env;
	private LoginScreen loginScreen;
	private AdminScreen adminScreen;
	
	public AdminMain() {
		
		loginScreen = new LoginScreen();
		loginScreen.add(new LoginHandler());
		
		view = new JPanel();
		view.setLayout(new BorderLayout());
		view.add(loginScreen.getView(), BorderLayout.CENTER);
	}
	
	private class LoginHandler implements LoginListener {
		public void loginAttempted(LoginInfo info) {
			
			try {
				env = new DefaultEnvironment(info);
				adminScreen = new AdminScreen(env);
				adminScreen.add(new LogoutHandler());
				
				view.removeAll();
				view.add(adminScreen.getView(), BorderLayout.CENTER);
				view.invalidate();
				view.getParent().validate();
				view.getParent().repaint();
			}
			catch (Exception ex) {
				JOptionPane.showMessageDialog(view.getRootPane(), "Login failed: " + ex.getMessage());
				ex.printStackTrace();
				return;
			}
			
			Runnable r = new Runnable() {
				public void run() {
					adminScreen.postDisplayActions();
				}
			};
			
			SwingUtilities.invokeLater(r);
		}
	}
	
	private class LogoutHandler implements LogoutListener {
		public void logout() {
			
			loginScreen.prepareForReuse();
			
			view.removeAll();
			view.add(loginScreen.getView(), BorderLayout.CENTER);
			view.invalidate();
			view.getParent().validate();
			view.getParent().repaint();
		}
	}
	
	public JPanel getView() {
		return view;
	}

	public static void main(String[] args) throws Exception {
		
		AdminMain main = new AdminMain();
		
		JFrame frame = new JFrame();
		frame.setTitle("Veracity Admin Client");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(main.getView());
		frame.setSize(600, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
