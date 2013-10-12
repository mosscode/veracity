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
package com.moss.veracity.api.util;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class CryptoUtil {
	
	private static final String PROVIDER = "SUN";
	private static final String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";
	private static final String KEY_GEN_ALGORITHM = "DSA";
	private static final String SIGNATURE_ALGORITHM = "SHA1withDSA";
	private static final String DIGEST_ALGORITHM = "SHA-1";

	/**
	 * Generates DSA key pair.
	 */
	public static KeyPair create() {
		
		KeyPairGenerator keyGen;
		try {
		
			SecureRandom random;
			try {	
				random = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM, PROVIDER);
			}	
			catch (NoSuchAlgorithmException ex) {
				throw new RuntimeException("Required secure random algorithm " + SECURE_RANDOM_ALGORITHM + " is not unavailable: cannot generate keypair", ex);
			}
	
			keyGen = KeyPairGenerator.getInstance(KEY_GEN_ALGORITHM, PROVIDER);
			keyGen.initialize(1024, random);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		return keyGen.generateKeyPair();
	}
	
	/**
	 * @returns A SHA-1 digest of the supplied data.
	 */
	public static byte[] digest(byte[] data) {
		try {
			MessageDigest d = MessageDigest.getInstance(DIGEST_ALGORITHM);
			d.update(data);
			return d.digest();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Create a signature of the supplied data.
	 * @param keyPair - the key pair used to sign the data
	 * @param data - data to be signed
	 * @return the generated signature data
	 */
	public static byte[] sign(PrivateKey privateKey, byte[] data) {
		
		Signature sig;
		try {
			sig = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
			sig.initSign(privateKey);
			sig.update(data);
			return sig.sign();
		}
		catch (InvalidKeyException ex) {
			throw new RuntimeException("The private key to be used for signing data is invalid: cannot sign data", ex);
		}
		catch (SignatureException ex) {
			throw new RuntimeException("An error occurred while attempting to sign the data", ex);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Verify that a signature matches the signed data.
	 * @param publicKey - the public key to be used for verifying the signature
	 * @param signature - the signature to be verified
	 * @param signedData
	 * @returns True if the signature is valid.
	 */
	public static boolean verify(byte[] publicKey, byte[] signature, byte[] signedData) {
		
		PublicKey pubKey;
		try {
			
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_GEN_ALGORITHM, PROVIDER);
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKey);
			pubKey = keyFactory.generatePublic(pubKeySpec);
			
			Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
	        sig.initVerify(pubKey);
	        sig.update(signedData);

	        return sig.verify(signature);
		}
		catch (InvalidKeySpecException ex) {
			throw new RuntimeException("The public key to be used for verifying a signature is invalid: cannot verify signature", ex);
		}
        catch (SignatureException ex) {
        	throw new RuntimeException("An error occurred while attempting to verify the signature", ex);
        }
        catch (Exception ex) {
        	throw new RuntimeException(ex);
        }
	}
}
