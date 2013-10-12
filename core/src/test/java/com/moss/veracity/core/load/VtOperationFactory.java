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
package com.moss.veracity.core.load;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.xml.ws.Service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.moss.anthroponymy.StFirstMiddleLastName;
import com.moss.veracity.api.Authentication;
import com.moss.veracity.api.Management;
import com.moss.veracity.api.VtAccount;
import com.moss.veracity.api.VtAuthMode;
import com.moss.veracity.api.VtEndorsement;
import com.moss.veracity.api.VtImage;
import com.moss.veracity.api.VtPassword;
import com.moss.veracity.api.VtPasswordMechanism;
import com.moss.veracity.api.VtProfile;
import com.moss.veracity.api.VtToken;
import com.moss.veracity.core.load.operation.Operation;
import com.moss.veracity.core.load.operation.OperationContext;
import com.moss.veracity.core.load.operation.OperationFactory;

public class VtOperationFactory implements OperationFactory {
	
	private final Log log;
	private final Authentication auth;
	private final Management manage;
	private final String adminName;
	private final VtToken universalToken;
	private final List<VtAccount> accounts;
	private final Random random;
	
	private VtEndorsement endorsement;
	
	public VtOperationFactory(String identity, String password, URL baseUrl) throws Exception {
		log = LogFactory.getLog(this.getClass());
		
		if (log.isDebugEnabled()) {
			log.debug("Materializing authentication service");
		}
		
		auth = Service.create(new URL("http://" + baseUrl.getHost() + ":" + baseUrl.getPort() + "/AuthenticationImpl?wsdl"), Authentication.QNAME).getPort(Authentication.class);
		
		if (log.isDebugEnabled()) {
			log.debug("Materializing management service");
		}
		
		manage = Service.create(new URL("http://" + baseUrl.getHost() + ":" + baseUrl.getPort() + "/ManagementImpl?wsdl"), Management.QNAME).getPort(Management.class);
		
		adminName = identity;
		universalToken = new VtPassword(password);
		
		if (log.isDebugEnabled()) {
			log.debug("Populating the veracity service with accounts for testing (100)");
		}
		
		accounts = new ArrayList<VtAccount>();
		
		final byte[] profileImage = loadResource("com/moss/veracity/core/root.jpg");
		
		for (int i=0; i<100; i++) {
			
			VtImage image = new VtImage();
			image.setData(profileImage);
			
			VtProfile profile = new VtProfile();
			profile.setName(new StFirstMiddleLastName("Wannado", "F", "Mercer"));
			profile.setImage(image);
			profile.setProfileName("default");
			profile.setWhenLastModified(System.currentTimeMillis());
			
			VtAccount account = new VtAccount();
			account.setName(UUID.randomUUID() + "@localhost");
			account.setAuthMode(VtAuthMode.USER);
			account.getMechanisms().add(new VtPasswordMechanism(password));
			account.getProfiles().add(profile);
			
			accounts.add(account);
			manage.create(account, adminEndorsement());
		}
		
		random = new Random(System.currentTimeMillis());
	}
	
	private VtEndorsement adminEndorsement() {
		
		if (endorsement == null || endorsement.getExpiration() < System.currentTimeMillis()) {
			endorsement = auth.verify(adminName, universalToken, null);
			
			if (endorsement == null) {
				throw new RuntimeException("Failed to authenticate");
			}
		}
		
		return endorsement;
	}
	
	private byte[] loadResource(String resPath) {
		try {
			InputStream in = this.getClass().getClassLoader().getResourceAsStream(resPath);
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024];
			for(int numRead = in.read(buffer); numRead!=-1; numRead = in.read(buffer)){
				out.write(buffer, 0, numRead);
			}

			return out.toByteArray();
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public OperationContext createContext() {
		VtAccount account = accounts.get(random.nextInt(accounts.size()));
		String name = account.getName();
		return new VtOperationContext(auth, name, universalToken);
	}

	public Operation createOperation() {
		return new VtAuthenticateOperation();
	}
}
