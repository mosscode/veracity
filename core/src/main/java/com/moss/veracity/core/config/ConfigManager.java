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
package com.moss.veracity.core.config;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

import com.moss.veracity.core.filemonitor.FileListener;
import com.moss.veracity.core.filemonitor.FileMonitor;

public final class ConfigManager {
	
	private final Log log = LogFactory.getLog(this.getClass());
	private final JAXBContext jaxbContext;
	private final File file;
	private final FileMonitor monitor;
	private final Object sync = new Object();
	private final List<ConfigListener> listeners = new ArrayList<ConfigListener>();
	
	private Configuration current;
	
	public ConfigManager(File file) {
		
		if (file == null) {
			throw new NullPointerException();
		}
		
		this.file = file.getAbsoluteFile();
		
		try {
			jaxbContext = JAXBContext.newInstance(Configuration.class);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		try {
			current = read();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		monitor = FileMonitor.monitor(file, 1000);
		monitor.addListener(new ConfigFileListener());
	}

	public Configuration config() {
		synchronized (sync) {
			return current.copy();
		}
	}
	
	public void updateConfig(Configuration config) {
		
		synchronized (sync) {
			try {
				
				if (log.isDebugEnabled()) {
					log.debug("Updating configuration file: " + file);
				}
				
				monitor.setPaused(true);

				write(config);
				
				current = config.copy();
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			finally {
				monitor.setPaused(false);
			}

			fireChanged(config);
		}
	}
	
	public void addListener(ConfigListener l) {
		synchronized (sync) {
			listeners.add(l);
		}
	}
	
	public void removeListener(ConfigListener l) {
		synchronized (sync) {
			listeners.remove(l);
		}
	}
	
	public void close() {
		monitor.stop();
	}
	
	private Configuration read() throws Exception {
		if (file.exists()) {

			if (log.isDebugEnabled()) {
				log.debug("Reading configuration: " + file);
			}

			Unmarshaller u = jaxbContext.createUnmarshaller();
			return (Configuration)u.unmarshal(file);
		}
		else {

			if (log.isDebugEnabled()) {
				log.debug("Configuration file missing, creating default configuration: " + file);
			}

			Configuration config = new Configuration();
			write(config);
			return config;
		}
	}
	
	private void write(Configuration config) throws Exception {
		Marshaller m = jaxbContext.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(config, new FileWriter(file));
		
		if (monitor != null) {
			monitor.reset();
		}
	}
	
	private class ConfigFileListener implements FileListener {
		public void fileChanged() {
			
			synchronized (sync) {

				Configuration config;
				try {
					config = read();
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
				
				current = config.copy();

				fireChanged(config);
			}
		}
	}
	
	private void fireChanged(Configuration config) {
		for (ConfigListener l : listeners) {
			l.configChanged(config);
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		BasicConfigurator.configure();
		
		ConfigManager manager = new ConfigManager(new File("test.xml"));
		
		Thread.sleep(3000l);
		
		manager.updateConfig(new Configuration());
		
		Thread.sleep(3000l);
		
		manager.close();
	}
}
