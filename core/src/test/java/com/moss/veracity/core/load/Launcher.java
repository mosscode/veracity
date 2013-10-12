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

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.moss.veracity.core.load.operation.Operation;
import com.moss.veracity.core.load.operation.OperationListener;
import com.moss.veracity.core.load.operation.OperationManager;

public class Launcher {

	public static void main(String[] args) throws Exception {
		
		System.setProperty("org.apache.cxf.Logger", "org.apache.cxf.common.logging.Log4jLogger");
		
		Logger.getRootLogger().removeAllAppenders();
		BasicConfigurator.configure();
		Logger.getLogger(Launcher.class.getPackage().getName()).setLevel(Level.INFO);
		Logger.getLogger("org.apache.cxf").setLevel(Level.WARN);
		Logger.getLogger("org.springframework").setLevel(Level.WARN);
		
		String identity = "root@localhost";
		String password = "pass";
		URL baseUrl = new URL("http://localhost:5063");
		
		VtOperationFactory factory = new VtOperationFactory(
			identity,
			password,
			baseUrl
		);
		
		final long start = System.currentTimeMillis();
		
		class MetricsListener extends TimerTask implements OperationListener {
			
			private int completed;
			private int failed;
			
			public void started(Operation op) {}
			
			public void completed(Operation op) {
				completed++;
			}
			
			public void failed(Operation op, Exception ex) {
				failed++;
			}
			
			public void run() {
				long now = System.currentTimeMillis();
				long runtime = now - start;
				float msPerOp = runtime / completed;
				
				System.out.println("--------------------------------------------------------------------------------");
				System.out.println("Completed: " + completed);
				System.out.println("Failed: " + failed);
				System.out.println("Milliseconds Per Op: " + msPerOp);
			}
		};
		
		MetricsListener metrics = new MetricsListener();
		
		OperationManager manager = new OperationManager(factory, metrics, 5, 50);
		
		Thread t = new Thread(manager, "OperationManagerThread");
		t.start();
		
		Timer timer = new Timer(true);
		long fiveSeconds = 1000 * 5;
		timer.scheduleAtFixedRate(metrics, fiveSeconds, fiveSeconds);
	}
}
