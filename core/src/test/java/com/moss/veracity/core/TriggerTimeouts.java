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
package com.moss.veracity.core;

import java.net.URL;

import javax.xml.ws.Service;

import com.moss.veracity.api.Authentication;
import com.moss.veracity.api.VtEndorsement;
import com.moss.veracity.api.VtPassword;
import com.moss.veracity.api.VtPublicSignatureKey;
import com.moss.veracity.api.VtToken;
import com.moss.veracity.api.util.CryptoUtil;
import com.moss.veracity.api.util.DataAssembler;

public class TriggerTimeouts {

	public static void main(String[] args) throws Exception {
		
		System.out.println("Materializing auth service");
		
		Authentication auth = Service.create(
			new URL("http://localhost:5063/AuthenticationImpl?wsdl"), 
			Authentication.QNAME
		).getPort(Authentication.class);
		
		System.out.println("Fetching key");
		
		System.out.println("Obtaining endorsement");
		
		String name = "root@localhost";
		VtToken token = new VtPassword("pass");
		String profile = "default";
		
		VtEndorsement endorsement = auth.verify(name, token, profile);
		
		if (endorsement == null) {
			throw new RuntimeException("No endorsement returned");
		}
		System.out.println("    Endorsement expires in " + ((endorsement.getExpiration() - System.currentTimeMillis()) / 1000f) + " seconds");
		
		VtPublicSignatureKey key = auth.getSignatureKey(endorsement.getKeyDigest());
		System.out.println("    Key expires in " + ((key.expiration() - System.currentTimeMillis()) / 1000f) + " seconds");
		
		while (true) {
			System.out.println("Verifying endorsement");
		
			DataAssembler assembler = new DataAssembler()
			.add(endorsement.getName())
			.add(endorsement.getExpiration())
			.add(endorsement.getKeyDigest());

			if (endorsement.getProfileDesc() != null) {
				assembler.add(endorsement.getProfileDesc().getName());
				assembler.add(endorsement.getProfileDesc().getWhenLastModified());
			}

			byte[] signedData = assembler.get();

			CryptoUtil.verify(key.data(), endorsement.getSignature(), signedData);
			
			Thread.sleep(1000);
		}
	}
}
