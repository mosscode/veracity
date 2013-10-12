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

import java.io.File;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.moss.jaxbhelper.JAXBHelper;

@XmlRootElement
public final class LaunchParameters {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	public static final LaunchParameters load(File configFile) throws Exception {
		JAXBHelper helper = new JAXBHelper(LaunchParameters.class);
		return helper.readFromFile(configFile);
	}
	
	@XmlElement
	private String bindAddress = "0.0.0.0";
	
	@XmlElement
	private Integer httpPort = 80;

	@XmlElement
	private String dataDirectory = "data";
	
	@XmlElement
	private Integer syncPort = 61616;
	
	public LaunchParameters() {}
	
	public LaunchParameters(String bindAddress, String dataDirectory,
			int httpPort, int syncPort) {
		super();
		this.bindAddress = bindAddress;
		this.dataDirectory = dataDirectory;
		this.httpPort = httpPort;
		this.syncPort = syncPort;
	}
	private LaunchParameters(LaunchParameters other){
		this.bindAddress = other.bindAddress;
		this.httpPort = other.httpPort;
		this.dataDirectory = other.dataDirectory;
		this.syncPort = other.syncPort;
	}
	
	public void readSystemProperties(){
		String data = System.getProperty("veracity.dataDir");
		String http = System.getProperty("veracity.httpPort");
		String sync = System.getProperty("veracity.syncPort");
		
		if (data != null) {
			if (log.isDebugEnabled()) {
				log.debug("Overriding data directory with system property value.  Effective value is " + data);
			}
			dataDirectory = data;
		}
		
		if (http != null) {
			if (log.isDebugEnabled()) {
				log.debug("Overriding http port with system property value.  Effective value is " + http);
			}
			httpPort = new Integer(http);
		}
		
		if (sync != null) {
			if (log.isDebugEnabled()) {
				log.debug("Overriding sync port with system property value. Effective value is " + sync);
			}
			syncPort = new Integer(sync);
		}
	}
	
	public final void save(File configFile) throws Exception {
		JAXBHelper helper = new JAXBHelper(LaunchParameters.class);
		helper.writeToFile(helper.writeToXmlString(this), configFile);
	}
	public LaunchParameters withDataDir(File dir){
		LaunchParameters c = new LaunchParameters(this);
		c.dataDirectory = dir.getAbsolutePath();
		return c;
	}
	
	public LaunchParameters withHttpPort(Integer port){
		LaunchParameters c = new LaunchParameters(this);
		c.httpPort = port;
		return c;
	}
	
	public LaunchParameters withSyncPort(Integer port){
		LaunchParameters c = new LaunchParameters(this);
		c.syncPort = port;
		return c;
	}
	
	public File dataDirectory() {
		return new File(dataDirectory);
	}
	public final String bindAddress() {
		return bindAddress;
	}
	public final Integer httpPort() {
		return httpPort;
	}
	public final Integer syncPort() {
		return syncPort;
	}
}
