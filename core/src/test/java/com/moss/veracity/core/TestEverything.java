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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.BasicConfigurator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.moss.anthroponymy.StFirstMiddleLastName;
import com.moss.rpcutil.proxy.ProxyFactory;
import com.moss.veracity.api.AccountExistsException;
import com.moss.veracity.api.Authentication;
import com.moss.veracity.api.Management;
import com.moss.veracity.api.NoSuchAccountException;
import com.moss.veracity.api.NotAuthorizedException;
import com.moss.veracity.api.VtAccount;
import com.moss.veracity.api.VtAuthMode;
import com.moss.veracity.api.VtConfiguration;
import com.moss.veracity.api.VtEndorsement;
import com.moss.veracity.api.VtImage;
import com.moss.veracity.api.VtPassword;
import com.moss.veracity.api.VtPasswordMechanism;
import com.moss.veracity.api.VtProfile;
import com.moss.veracity.api.VtPublicSignatureKey;
import com.moss.veracity.api.util.CryptoUtil;
import com.moss.veracity.api.util.DataAssembler;
import com.moss.veracity.core.LaunchParameters;
import com.moss.veracity.core.Veracity;

public abstract class TestEverything {
	
	private static final int DEFAULT_TESTING_PORT = 5063;
	
	private static File testData;
	private static Veracity veracity;
	
	protected abstract ProxyFactory proxyFactory();
	
	private Authentication auth() {
		return proxyFactory().create(
			Authentication.class, 
			"http://localhost:" + DEFAULT_TESTING_PORT + "/AuthenticationImpl?wsdl"
		);
	}

	private Management management() {
		return proxyFactory().create(
			Management.class, 
			"http://localhost:" + DEFAULT_TESTING_PORT + "/ManagementImpl?wsdl"
		);
	}
	
	@BeforeClass
	public static void before() throws Exception {
		
		BasicConfigurator.configure();
		
		testData = new File("target/test-data");
		
		if (testData.exists() && !deleteDir(testData)) {
			throw new RuntimeException("Cannot delete directory " + testData);
		}
		
		veracity = new Veracity(
				new LaunchParameters()
					.withHttpPort(DEFAULT_TESTING_PORT)
					.withDataDir(testData)
				);
	}
	
	@AfterClass
	public static void after() throws Exception {
		veracity.shutdown();
		deleteDir(testData);
	}
	
	@Test
	public void signatureKey() throws Exception {
		
		VtEndorsement account = auth().verify("root@localhost", new VtPassword("pass"), null);
		VtPublicSignatureKey key = auth().getSignatureKey(account.getKeyDigest());
		
		Assert.assertNotNull(key);
		Assert.assertNotNull(key.data());
		Assert.assertTrue(key.expiration() > 0);
	}

	@Test
	public void defaultAccount() throws Exception {
		
		VtEndorsement account = auth().verify("root@localhost", new VtPassword("pass"), null);
		VtPublicSignatureKey key = auth().getSignatureKey(account.getKeyDigest());
		
		Assert.assertNotNull(account);
		
		byte[] data = new DataAssembler()
		.add(account.getName())
		.add(account.getExpiration())
		.add(account.getKeyDigest())
		.get();
		
		boolean valid = CryptoUtil.verify(key.data(), account.getSignature(), data);
		
		Assert.assertTrue(valid);
	}
	
	@Test
	public void fakeAccount() throws Exception {
		
		VtEndorsement account = auth().verify("asdf", new VtPassword("pass"), null);
		
		Assert.assertNull(account);
	}
	
	@Test
	public void invalidTokenAccount() throws Exception {
		
		VtEndorsement account = auth().verify("root@localhost", new VtPassword("sadf"), null);
		
		Assert.assertNull(account);
	}
	
	@Test
	public void config() throws Exception {
		
		VtEndorsement account = auth().verify("root@localhost", new VtPassword("pass"), null);
		VtConfiguration config = management().getConfiguration(account);
		
		Assert.assertNotNull(config);
		Assert.assertTrue(config.getEndorsementExpiration() > 0);
		Assert.assertTrue(config.getSigningKeyExpiration() > 0);
		
		long newSigningKeyExpiration = config.getSigningKeyExpiration() + 1;
		long newEndorsementExpiration = config.getEndorsementExpiration() + 1;
		
		config.setSigningKeyExpiration(newSigningKeyExpiration);
		config.setEndorsementExpiration(newEndorsementExpiration);
		
		management().configure(config, account);
		
		config = management().getConfiguration(account);
		
		Assert.assertNotNull(config);
		Assert.assertTrue(config.getEndorsementExpiration() == newEndorsementExpiration);
		Assert.assertTrue(config.getSigningKeyExpiration() == newSigningKeyExpiration);
	}
	
	@Test
	public void createAccount() throws Exception {
		
		VtEndorsement admin = auth().verify("root@localhost", new VtPassword("pass"), null);
		
		VtAccount account = new VtAccount();
		account.setName(UUID.randomUUID().toString() + "@localhost");
		account.setAuthMode(VtAuthMode.USER);
		account.add(new VtPasswordMechanism("asl;kjasdfl;jas"));
		
		management().create(account, admin);
		
		VtAccount readAccount = management().read(account.getName(), admin);
		
		Assert.assertEquals(account.getName(), readAccount.getName());
		Assert.assertEquals(account.getAuthMode(), readAccount.getAuthMode());
		Assert.assertEquals(account.getMechanisms().size(), readAccount.getMechanisms().size());
		
		String password = ((VtPasswordMechanism)account.getMechanisms().get(0)).getPassword();
		String readPassword = ((VtPasswordMechanism)readAccount.getMechanisms().get(0)).getPassword();
		
		Assert.assertEquals(password, readPassword);
	}
	
	@Test(expected=AccountExistsException.class)
	public void createAccountExists() throws Exception {
		
		VtEndorsement admin = auth().verify("root@localhost", new VtPassword("pass"), null);
		
		VtAccount account = new VtAccount();
		account.setName(admin.getName());
		account.setAuthMode(VtAuthMode.USER);
		account.add(new VtPasswordMechanism("asl;kjasdfl;jas"));
		
		management().create(account, admin);
	}
	
	@Test
	public void anonymousCreation() throws Exception {
		
		VtEndorsement admin = new VtEndorsement();
		admin.setExpiration(System.currentTimeMillis() + 3600 * 20);
		admin.setName("root@localhost");
		admin.setSignature(new byte[] { 0x0f });
		
		VtAccount account = new VtAccount();
		account.setName(UUID.randomUUID().toString() + "@localhost");
		account.setAuthMode(VtAuthMode.USER);
		account.add(new VtPasswordMechanism("asl;kjasdfl;jas"));
		
		management().create(account, admin);
	}
	
	@Test(expected=NotAuthorizedException.class)
	public void anonymousCreationAdmin() throws Exception {
		
		VtEndorsement admin = new VtEndorsement();
		admin.setExpiration(System.currentTimeMillis() + 3600 * 20);
		admin.setName("root@localhost");
		admin.setSignature(new byte[] { 0x0f });
		
		VtAccount account = new VtAccount();
		account.setName(UUID.randomUUID().toString() + "@localhost");
		account.setAuthMode(VtAuthMode.ADMIN);
		account.add(new VtPasswordMechanism("asl;kjasdfl;jas"));
		
		management().create(account, admin);
	}
	
	public void read() throws Exception {
		
		VtEndorsement admin = auth().verify("root@localhost", new VtPassword("pass"), null);
		
		VtAccount account = management().read("root@localhost", admin);
		
		Assert.assertNotNull(account);
		Assert.assertNotNull(account.getName());
		Assert.assertNotNull(account.getAuthMode());
		
		VtPasswordMechanism mechanism = (VtPasswordMechanism)account.getMechanisms().get(0);
		
		Assert.assertNotNull(mechanism.getPassword());
	}
	
	@Test(expected=NoSuchAccountException.class)
	public void createReadAccountNonexistent() throws Exception {
		
		VtEndorsement admin = auth().verify("root@localhost", new VtPassword("pass"), null);
		
		management().read(UUID.randomUUID().toString() + "@localhost", admin);
	}
	
	@Test(expected=NotAuthorizedException.class)
	public void createReadNotAuthorized() throws Exception {
		
		VtEndorsement admin = new VtEndorsement();
		admin.setExpiration(System.currentTimeMillis() + 3600 * 20);
		admin.setName("root@localhost");
		admin.setSignature(new byte[] { 0x0f });
		
		management().read("root@localhost", admin);
	}
	
	@Test
	public void update() throws Exception {
		
		VtEndorsement admin = auth().verify("root@localhost", new VtPassword("pass"), null);
		
		VtAccount account = new VtAccount();
		account.setName(UUID.randomUUID().toString() + "@localhost");
		account.setAuthMode(VtAuthMode.USER);
		account.add(new VtPasswordMechanism("asl;kjasdfl;jas"));
		
		management().create(account, admin);
		
		((VtPasswordMechanism)account.getMechanisms().get(0)).setPassword("foo");
		
		management().update(account, admin);
		
		VtAccount readAccount = management().read(account.getName(), admin);
		
		Assert.assertEquals(account.getName(), readAccount.getName());
		Assert.assertEquals(account.getAuthMode(), readAccount.getAuthMode());
		Assert.assertEquals(account.getMechanisms().size(), readAccount.getMechanisms().size());
		
		String modified = extractPassword(account);
		String read = extractPassword(readAccount);
		Assert.assertEquals(modified, read);
	}
	
	private String extractPassword(VtAccount account) {
		VtPasswordMechanism m = (VtPasswordMechanism)account.getMechanisms().get(0);
		return m.getPassword();
	}
	
	@Test(expected=NoSuchAccountException.class)
	public void updateAccountNonexistent() throws Exception {
		
		VtEndorsement admin = auth().verify("root@localhost", new VtPassword("pass"), null);
		
		VtAccount account = new VtAccount();
		account.setName(UUID.randomUUID().toString() + "@localhost");
		account.setAuthMode(VtAuthMode.USER);
		account.add(new VtPasswordMechanism("asl;kjasdfl;jas"));
		
		management().update(account, admin);
	}
	
	@Test(expected=NotAuthorizedException.class)
	public void updateNotAuthorized() throws Exception {
		
		VtEndorsement admin = auth().verify("root@localhost", new VtPassword("pass"), null);
		
		VtEndorsement fake = new VtEndorsement();
		fake.setExpiration(System.currentTimeMillis() + 3600 * 20);
		fake.setName("root@localhost");
		fake.setSignature(new byte[] { 0x0f });
		
		VtAccount account = management().read("root@localhost", admin);
		
		management().update(account, fake);
	}
	
	@Test
	public void createAuthRetrieveProfile() throws Exception {
		
		VtEndorsement admin = auth().verify("root@localhost", new VtPassword("pass"), null);
		
		VtAccount account = new VtAccount();
		account.setName(UUID.randomUUID().toString() + "@localhost");
		account.setAuthMode(VtAuthMode.USER);
		account.add(new VtPasswordMechanism("asl;kjasdfl;jas"));
		
		VtProfile workProfile = new VtProfile();
		workProfile.setProfileName("work");
		workProfile.setName(new StFirstMiddleLastName("John", "C", "Riley"));
		workProfile.setImage(getImage("com/moss/veracity/core/grapes.jpg"));
		account.add(workProfile);
		
		VtProfile funProfile = new VtProfile();
		funProfile.setProfileName("fun");
		funProfile.setName(new StFirstMiddleLastName("Earl", "Q", "Runstoff"));
		funProfile.setImage(getImage("com/moss/veracity/core/puppy.jpg"));
		account.add(funProfile);
		
		management().create(account, admin);
		
		assertEquals(workProfile, management().read(account.getName(), admin).getProfiles().get(0));
		
		workProfile.setProfileName("asdf");
		management().update(account, admin);
		
		Assert.assertTrue(workProfile.getWhenLastModified() < management().read(account.getName(), admin).getProfiles().get(0).getWhenLastModified());
		
		VtEndorsement endorsement = auth().verify(
			account.getName(), 
			new VtPassword(((VtPasswordMechanism)account.getMechanisms().get(0)).getPassword()), 
			funProfile.getProfileName()
		);
		
		Assert.assertNotNull(endorsement);
		
		VtProfile profile = auth().getProfile(endorsement);
		
		Assert.assertNotNull(profile);
		
		assertEquals(funProfile, profile);
	}
	
	@Test
	public void listAccountNames() throws Exception {
		
		VtEndorsement admin = auth().verify("root@localhost", new VtPassword("pass"), null);
		
		List<String> names = management().listAccountNames(admin);
		
		Assert.assertNotNull(names);
		Assert.assertTrue(!names.isEmpty());
		Assert.assertTrue(names.contains("root@localhost"));
	}
	
	private void assertEquals(VtProfile p1, VtProfile p2) {
		Assert.assertEquals(p1.getProfileName(), p2.getProfileName());
		Assert.assertEquals(p1.getName(), p2.getName());
		Assert.assertArrayEquals(p1.getImage().getData(), p2.getImage().getData());
	}
	
	private VtImage getImage(String path) throws IOException {
		
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(path);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		byte[] buffer = new byte[1024 * 10]; //10k buffer
		for(int numRead = in.read(buffer); numRead!=-1; numRead = in.read(buffer)){
			out.write(buffer, 0, numRead);
		}
		
		in.close();
		out.close();
		
		VtImage image = new VtImage();
		image.setData(out.toByteArray());
		
		return image;
	}
	
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
}
