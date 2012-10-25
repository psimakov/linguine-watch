/*
	Linguine Watch Performance Monitoring Library
	Copyright (C) 2005 Pavel Simakov
	http://www.softwaresecretweapons.com

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
*/

package com.oy.shared.lw.samples.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.oy.shared.lw.LinguineSNMPWatch;

public class TestServletContextListener implements ServletContextListener {

	private LinguineSNMPWatch watch;
	
	public void contextInitialized(ServletContextEvent event) {
		try {
			
			// instantiate
			watch = new LinguineSNMPWatch();
			
			// configure
			watch.getPerfAgentContext().setCollectDelayMillis(1000);
			watch.getPerfAgentContext().setSnapshotFileName("c:/perf.html");		
			
			watch.getSNMPAgentContext().setPort(161);
			watch.getSNMPAgentContext().setOrganizationName("ACME Corp.");
			watch.getSNMPAgentContext().setContactInfo("http://www.acme.com");
			
			// start
			watch.start();
			
			// register monitors
			watch.getPerfAgent().addMonitor(TestHttpSessionListener.getMonitor());
			
			// generate MIB file 
			watch.getSNMPAgent().generateMIBFile("c:/perf.mib");
			
		} catch(Exception e){
			throw new RuntimeException("Failed to start monitoring.", e);
		}			
	}

	public void contextDestroyed(ServletContextEvent event) {
		watch.getPerfAgent().removeMonitor(TestHttpSessionListener.getMonitor());
		
		watch.stop();
	}
	
}
