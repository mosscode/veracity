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

import java.net.URL;

import javax.xml.ws.Service;

import com.moss.jaxwslite.ServiceFactory;
import com.moss.veracity.api.Authentication;
import com.moss.veracity.api.Management;
import com.moss.veracity.api.VtEndorsement;
import com.moss.veracity.api.VtPassword;
import com.moss.veracity.api.dns.ServiceLookup;
import com.moss.veracity.api.util.NameParser;
import com.moss.veracity.api.util.ParsedName;

public class DefaultEnvironment implements Environment {
	
	private final String identity;
	private final String password;
	private final URL baseUrl;
	private final Authentication auth;
	private final Management management;
	
	private VtEndorsement login;
	
	public DefaultEnvironment(LoginInfo info) throws Exception {
		
		ParsedName name = NameParser.parse(info.getIdentity());
		
		if (name == null) {
			throw new Exception("Cannot parse identity: " + info.getIdentity());
		}
		
		identity = info.getIdentity();
		password = info.getPassword();
		
		if (info.getExplicitService() == null) {
			baseUrl = new URL("http://" + name.getServiceName());
		}
		else {
			baseUrl = new URL(info.getExplicitService());
		}
		
		URL authUrl = new URL(baseUrl + "/AuthenticationImpl?wsdl");;
		Service authService = Service.create(authUrl, Authentication.QNAME);
		auth = authService.getPort(Authentication.class);
		
//		URL managementUrl = new URL(baseUrl + "/ManagementImpl?wsdl");
//		Service managementService = Service.create(managementUrl, Management.QNAME);
//		management = managementService.getPort(Management.class);
		
		management = ServiceFactory.createDefault(baseUrl + "/ManagementImpl?wsdl", Management.QNAME.getNamespaceURI(), Management.class);
		
		login();
	}

	public Authentication auth() {
		return auth;
	}

	public URL baseUrl() {
		return baseUrl;
	}
	
	public Management management() {
		return management;
	}

	public VtEndorsement login() {
		
		if (login != null && System.currentTimeMillis() < login.getExpiration()) {
			return login;
		}
		
		login = auth.verify(identity, new VtPassword(password), null);
		
		if (login == null) {
			throw new RuntimeException("Login failed");
		}
		
		return login;
	}
}
