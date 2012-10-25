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

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.oy.shared.lw.perf.IPerfMonitor;
import com.oy.shared.lw.perf.monitor.ObjectLifetimeMonitor;

public class TestHttpSessionListener implements HttpSessionListener {

	private static ObjectLifetimeMonitor monitor = new ObjectLifetimeMonitor(
		TestHttpSessionListener.class, "SESSIONS", 
		"This monitor reports number of session in my web app."
	); 
	
	public static IPerfMonitor getMonitor(){
		return monitor;
	}
	
	public void sessionCreated(HttpSessionEvent se){
		monitor.incCreated();
	}
    
	public void sessionDestroyed(HttpSessionEvent se){
		monitor.incFreed();	
	}     
	
}
