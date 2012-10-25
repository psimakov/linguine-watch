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

package com.oy.shared.lw.samples.gc;

import com.oy.shared.lw.LinguineSNMPWatch;
import com.oy.shared.lw.misc.PoliteThread;

/**
 * This test are designed to monitor behavior of Java garbage collector.
 * Please note that overriding finalize() in TestGCCLass significanly changes
 * the behavior of garbage collection. 
 * 
 * To see this test at work set the following JVM options and run this test, 
 * while monitoring it via SNMP.
 * 
 * Options:
 * 		1) Copying [default]:
 * 			-Xms256M -Xmx256M
 * 		2) Incremental: 
 * 			-Xms256M -Xmx256M -Xincgc
 * 		3) Parallel copying:
 * 			-Xms256M -Xmx256M -XX:+UseParNewGC
 * 		4) Parallel scavenging:
 * 			-Xms256M -Xmx256M -XX:+UseParallelGC
 *      5) Concurrent mark and sweep:
 * 			-Xms256M -Xmx256M -XX:+UseConcMarkSweepGC
 *
 */

public class TestGCThread extends PoliteThread {
	
	public static void main(String [] args){
		new TestGCThread().start();
	}
	
	public TestGCThread() {
		LinguineSNMPWatch watch = new LinguineSNMPWatch();
		
		try {
			watch.start();		
			watch.getPerfAgent().addMonitor(TestGCObject.monitor);

			String home = System.getProperty("user.home") + "/";
			watch.getSNMPAgent().generateMIBFile(
				home + TestGCThread.class.getName() + ".mib"
			);
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public void run(){		
		while(!isTerminated()){
			
			try {
				int delay = 1 + (int) Math.round(100 * Math.random());				
				try {
					Thread.sleep(delay);
				} catch(InterruptedException ie){ }

				int count = 1 + (int) Math.round(Math.random() * 10 * 1000);
				for (int i=0; i < count; i++){
					new TestGCObject();
				}
			} catch(Exception e){
			}			
		}
	}
	
}
