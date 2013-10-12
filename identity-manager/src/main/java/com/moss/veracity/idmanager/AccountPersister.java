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

import com.moss.veracity.api.Authentication;
import com.moss.veracity.api.Management;
import com.moss.veracity.api.VtAccount;
import com.moss.veracity.api.VtEndorsement;
import com.moss.veracity.api.VtToken;

/**
 * Fetches the related account info from the service and refreshes 
 * authentication/updates the account as requested.
 */
public class AccountPersister {
	
	private final Authentication auth;
	private final Management manage;
	private final String name;
	private final VtToken token;
	
	private VtEndorsement login;
	private VtAccount account;
	
	public AccountPersister(Authentication auth, Management manage, String name, VtToken token) throws LoginException {
		this.auth = auth;
		this.manage = manage;
		this.name = name;
		this.token = token;
		
		try {
			account = manage.read(name, login());
		}
		catch (LoginException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public VtAccount account() {
		return account;
	}
	
	public synchronized void save() throws Exception {
		manage.update(account, login());
	}

	private VtEndorsement login() throws LoginException {
		
		if (login != null && System.currentTimeMillis() < login.getExpiration()) {
			return login;
		}
		
		login = auth.verify(name, token, null);
		
		if (login == null) {
			throw new LoginException();
		}
		
		return login;
	}
	
	public Authentication auth() {
		return auth;
	}
	
	public Management manage() {
		return manage;
	}
}
