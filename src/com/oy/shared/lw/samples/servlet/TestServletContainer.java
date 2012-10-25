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

import com.oy.shared.lw.misc.PoliteThread;

public class TestServletContainer {

	class TestThread extends PoliteThread {
		
		public void run(){
			while(!isTerminated()){
				int count = 1 + (int) (Math.random() * 10);
				while(count > 0){
					session.sessionCreated(null);
					session.sessionDestroyed(null);
					count--;
				}
			}
		}
		
	}
	
	private TestServletContextListener ctx = new TestServletContextListener();
	private TestHttpSessionListener session = new TestHttpSessionListener();
	
	public static void main(String [] args){
		new TestServletContainer().main();
	}
	
	public void main(){
		TestThread test = new TestThread();

		ctx.contextInitialized(null);

		test.start();		
		
		try {
		Thread.sleep(30 * 1000);
		} catch (InterruptedException ie){}
		
		ctx.contextDestroyed(null);
		
		test.terminate();
	}
	
}
