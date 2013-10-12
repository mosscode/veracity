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
package com.moss.veracity.api.dns;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

public class ServiceLookup {

	private static final String SERVICE_INFO = "_veracity._tcp.";

	public static final URL lookup(String veracityServiceName) throws Exception {
		
		String name = SERVICE_INFO + veracityServiceName;
		
		Lookup lookup = new Lookup(name, Type.SRV);
		lookup.run();
		Record[] answers = lookup.getAnswers();
		
		if (answers == null) {
			return null;
		}
		
		List<URL> options = new ArrayList<URL>();
		for (Record record : answers) {
			
			if (! (record instanceof SRVRecord)) {
				continue;
			}
			
			SRVRecord srv = (SRVRecord)record;
			String hostname = srv.getAdditionalName().toString();
			int port = srv.getPort();
			
			URL url = new URL("http://" + hostname + ":" + port);
			
			options.add(url);
		}
		
		if (options.isEmpty()) {
			return null;
		}
		
		if (options.size() == 1) {
			return options.get(0);
		}
		
		Random r = new Random(System.currentTimeMillis());
		int index = r.nextInt(options.size());
		URL option = options.get(index);
		
		return option;
	}
}
