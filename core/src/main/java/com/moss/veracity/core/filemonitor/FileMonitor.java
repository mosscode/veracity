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
package com.moss.veracity.core.filemonitor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

public final class FileMonitor {
	
	public static FileMonitor monitor(File file, long interval) {
		return new FileMonitor(file, interval);
	}
	
	private final Log log = LogFactory.getLog(this.getClass());
	private final Object sync = new Object();
	private final List<FileListener> listeners = new ArrayList<FileListener>();
	private final Timer timer;
	private final File file;
	
	private boolean exists = false;
	private long lastModified = 0;
	private boolean paused = false;
	
	private FileMonitor(File file, long interval) {
		this.file = file.getAbsoluteFile();
		
		reset();
		
		timer = new Timer(true);
		timer.scheduleAtFixedRate(new MonitorTask(), 0, interval);
	}
	
	public void addListener(FileListener l) {
		synchronized (sync) {
			listeners.add(l);
		}
	}
	
	public void removeListener(FileListener l) {
		synchronized (sync) {
			listeners.remove(l);
		}
	}
	
	public void setPaused(boolean paused) {
		synchronized (sync) {
			this.paused = paused;
			if (!paused) {
				sync.notify();
			}
		}
	}
	
	public void reset() {
		synchronized (sync) {
			
			if (log.isDebugEnabled()) {
				log.debug("Accepting current state of this file as its unchanged state: " + file);
			}
			
			exists = file.exists();
			lastModified = file.lastModified();
		}
	}
	
	public void stop() {
		
		if (log.isDebugEnabled()) {
			log.debug("Stopping file monitor, file no longer monitored: " + file);
		}
		
		timer.cancel();
	}
	
	private class MonitorTask extends TimerTask {
		public void run() {
			delta();
		}
	}
	
	private void delta() {
		synchronized (sync) {
			
			if (paused) {
				
				if (log.isDebugEnabled()) {
					log.debug("File monitor paused");
				}
				
				try {
					sync.wait();
				}
				catch (InterruptedException ex) {
					if (log.isWarnEnabled()) {
						log.warn("Interrupted while waiting for resume", ex);
					}
				}
				
				if (log.isDebugEnabled()) {
					log.debug("File monitor resuming");
				}
			}
			
			if (!exists && file.exists()) {
				
				if (log.isDebugEnabled()) {
					log.debug("Detected creation of file: " + file);
				}
				
				fireChanged();
				reset();
			}
			else if (exists && !file.exists()) {
				
				if (log.isDebugEnabled()) {
					log.debug("Detected deletion of file: " + file);
				}
				
				fireChanged();
				reset();
			}
			else if (lastModified != file.lastModified()) {
				
				if (log.isDebugEnabled()) {
					log.debug("Detected modification of file: " + file);
				}
				
				fireChanged();
				reset();
			}
		}
	}
	
	private void fireChanged() {
		for (FileListener l : listeners) {
			try {
				l.fileChanged();
			}
			catch (Exception ex) {
				if (log.isErrorEnabled()) {
					log.error("Notification failed", ex);
				}
				else {
					ex.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		BasicConfigurator.configure();
		
		FileMonitor m = FileMonitor.monitor(new File("pom.xml"), 1000);
		m.addListener(new FileListener() {
			public void fileChanged() {
				System.out.println("Changed");
			}
		});
		
		m.setPaused(true);
		
		Thread.sleep(5000l);
		
		m.setPaused(false);
		
		Thread.sleep(10000000l);
	}
}
