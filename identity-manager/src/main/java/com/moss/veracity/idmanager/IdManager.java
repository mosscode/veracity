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

import java.net.URL;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.xml.ws.Service;

import com.moss.veracity.api.Authentication;
import com.moss.veracity.api.Management;

public class IdManager {
	
	public IdManager(URL codeBase, Display display) {
		
		display.show(new WaitScreen("Loading Identity Manager"));
		
		try {
			
			String hostedDomainName = codeBase.getHost();
			
			URL serviceUrl = new URL("http://" + hostedDomainName + ":" + codeBase.getPort());
			URL authUrl = new URL(serviceUrl + "/AuthenticationImpl?wsdl");
			URL manageUrl = new URL(serviceUrl + "/ManagementImpl?wsdl");
			
			Authentication auth = Service.create(authUrl, Authentication.QNAME).getPort(Authentication.class);
			Management manage = Service.create(manageUrl, Management.QNAME).getPort(Management.class);
			
			LoginScreen screen = new LoginScreen(hostedDomainName, auth, manage, display);
			display.show(screen);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		Display display = new DefaultDisplay(frame.getContentPane());
		new IdManager(new URL("http://localhost:5063"), display);
	}
}
