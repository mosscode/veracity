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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public final class IdentityManagerServlet implements HttpHandler {
	
	private static final String JAR_NAME = "myid.jar";
	public static final String LAUNCH_PATH = "/myid";
	public static final String JAR_PATH = "/" + JAR_NAME;
	
	private final Log log = LogFactory.getLog(this.getClass());
	private final byte[] appletHtml;
	private final File appletFile;
	
	public IdentityManagerServlet() throws IOException {
		
		String appletTemplate;
		{
			String resource = "com/moss/veracity/core/applet-template.html";

			InputStream in = this.getClass().getClassLoader().getResourceAsStream(resource);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			byte[] buffer = new byte[1024 * 10]; //10k buffer
			for(int numRead = in.read(buffer); numRead!=-1; numRead = in.read(buffer)){
				out.write(buffer, 0, numRead);
			}
			
			in.close();
			out.close();
			
			appletTemplate = new String(out.toByteArray());
		}
		
		appletHtml = appletTemplate
		.replaceAll("\\$\\{title\\}", "Identity Manager")
		.replaceAll("\\$\\{code\\}", "com.moss.veracity.idmanager.IdManagerApplet.class")
		.replaceAll("\\$\\{archive\\}", JAR_NAME)
		.getBytes();

		{
			String resource = "META-INF/maven/com.moss.veracity/veracity-core/pom.properties";
			InputStream in = this.getClass().getClassLoader().getResourceAsStream(resource);
			
			if (in != null) {
				Properties p = new Properties();
				p.load(in);
				String veracityVersion = p.getProperty("version");
				appletFile = new File("veracity-identity-manager-" + veracityVersion + "-bundle.jar");
			}
			else {
				
				if (log.isWarnEnabled()) {
					log.warn("Not running from a distribution, attempting to find an identity manager jar file.");
				}
				
				File userDir = new File(System.getProperty("user.dir"));
				File chosenFile = null;
				
				for (File file : userDir.listFiles()) {
					if (file.getName().startsWith("veracity-identity-manager-") && file.getName().endsWith("-bundle.jar")) {
						chosenFile = file;
						break;
					}
				}
				
				appletFile = chosenFile;
			}
		}

		if (appletFile == null) {
			if (log.isWarnEnabled()) {
				log.warn("Cannot find identity manager applet jar in working path, identity manager applet will not be available: " + appletFile);
			}
		}
		else {
			if (log.isDebugEnabled()) {
				log.debug(appletFile.getAbsolutePath());
			}
		}
	}
	
	public void handle(HttpExchange exchange) throws IOException {
		
		String uri = exchange.getRequestURI().toString();
		readAndClose(exchange.getRequestBody());

		int responseCode = 200;
		
		String contentType = "text/html";
		long responseLength = 0;
		InputStream responseData = null;
		
		if (uri.endsWith(LAUNCH_PATH) || uri.endsWith(LAUNCH_PATH + "/")) {
			
			if (log.isDebugEnabled()) {
				log.debug("Serving up identity manager applet html: " + new String(appletHtml));
			}
			
			contentType = "text/html";
			responseLength = appletHtml.length;
			responseData = new ByteArrayInputStream(appletHtml);
		}
		else if (uri.endsWith(JAR_NAME)) {
		
			if (appletFile == null || !appletFile.exists()) {
				
				if (log.isWarnEnabled()) {
					log.warn("Cannot find identity manager applet jar in working path, identity manager applet will not be available: " + appletFile);
				}
				
				responseCode = 500;
			}
			else {
				if (log.isDebugEnabled()) {
					log.debug("Serving up identity manager applet: " + appletFile);
				}

				contentType = "application/java-archive";
				responseLength = appletFile.length();
				responseData = new FileInputStream(appletFile);
			}
		}
		else {
			responseCode = 404;
		}
		
		exchange.getResponseHeaders().set("Content-Type", contentType);
		exchange.sendResponseHeaders(responseCode, responseLength);
		
		/*
		 * Write response
		 */
		
		OutputStream out = exchange.getResponseBody();

		byte[] buffer = new byte[1024*100];
		for(int numRead = responseData.read(buffer);numRead!=-1;numRead = responseData.read(buffer)){
			out.write(buffer, 0, numRead);
		}
		
		responseData.close();
		out.close();
		
		exchange.close();
	}
	
	private void readAndClose(InputStream in) throws IOException {
		byte[] buffer = new byte[1024 * 10]; //10k buffer
		for(int numRead = in.read(buffer); numRead!=-1; numRead = in.read(buffer));
		in.close();
	}
}
