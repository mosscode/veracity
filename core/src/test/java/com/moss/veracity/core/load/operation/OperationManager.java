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
package com.moss.veracity.core.load.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OperationManager implements Runnable {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	private final OperationFactory factory;
	private final OperationListener listener;
	private final int threads;
	private final long stagger;
	
	private final BlockingQueue<Operation> queue;
	private final List<OperationWorker> workers;
	private boolean stop;
	private Thread thread;
	
	public OperationManager(OperationFactory factory, OperationListener listener, int threads, long stagger) {
		this.factory = factory;
		this.listener = listener;
		this.threads = threads;
		this.stagger = stagger;
		
		this.queue = new ArrayBlockingQueue<Operation>(threads);
		this.workers = new ArrayList<OperationWorker>(threads);
	}
	
	public void run() {
		
		if (log.isDebugEnabled()) {
			log.debug("Launching " + threads + " worker threads");
		}
		
		stop = false;
		thread = Thread.currentThread();
		
		while (!stop) {
			
			if (workers.size() < threads) {
				
				OperationWorker w = new OperationWorker(queue, listener, factory.createContext(), stagger);
				workers.add(w);
				
				try {
					Thread.sleep(stagger);
				} 
				catch (InterruptedException ex) { 
					throw new RuntimeException(ex); 
				}
				
				Thread t = new Thread(w, "OperationWorker-" + (workers.size() - 1));
				t.start();
			}
			
			Operation op = factory.createOperation();
			
			try {
				queue.put(op);
			}
			catch (InterruptedException ex) {}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Stopping worker threads");
		}
		
		for (OperationWorker worker : workers) {
			worker.stop();
		}
	}
	
	public void stop() {
		stop = true;
		thread.interrupt();
	}
}
