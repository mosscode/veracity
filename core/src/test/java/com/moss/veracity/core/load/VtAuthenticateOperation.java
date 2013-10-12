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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.moss.veracity.api.VtEndorsement;
import com.moss.veracity.api.VtProfile;
import com.moss.veracity.api.VtPublicSignatureKey;
import com.moss.veracity.api.util.CryptoUtil;
import com.moss.veracity.api.util.DataAssembler;
import com.moss.veracity.core.load.operation.Operation;

public class VtAuthenticateOperation implements Operation<VtOperationContext> {
	
	private final Log log;
	
	public VtAuthenticateOperation() {
		log = LogFactory.getLog(this.getClass());
	}

	public void perform(VtOperationContext context) throws Exception {
		
		if (log.isDebugEnabled()) {
			log.debug("Authenticating " + context.name());
		}
		
		VtEndorsement endorsement = context.auth().verify(context.name(), context.token(), "default");
		
		if (endorsement == null) {
			throw new Exception("Authentication failed for account " + context.name());
		}
		
		VtPublicSignatureKey key = context.auth().getSignatureKey(endorsement.getKeyDigest());
		
		if (log.isDebugEnabled()) {
			log.debug("Verifying " + context.name());
		}
		
		DataAssembler assembler = new DataAssembler()
		.add(endorsement.getName())
		.add(endorsement.getExpiration())
		.add(endorsement.getKeyDigest());

		if (endorsement.getProfileDesc() != null) {
			assembler.add(endorsement.getProfileDesc().getName());
			assembler.add(endorsement.getProfileDesc().getWhenLastModified());
		}

		byte[] signedData = assembler.get();

		if (!CryptoUtil.verify(key.data(), endorsement.getSignature(), signedData)) {
			throw new RuntimeException("Verification of endorsement for " + context.name() + " failed");
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Retrieving profile 'default' for " + context.name());
		}
		
		VtProfile profile = context.auth().getProfile(endorsement);
		
		if (profile == null) {
			throw new RuntimeException("Could not find profile 'default' for " + context.name());
		}
	}
}
