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


package com.oy.shared.lw;

import java.util.ArrayList;
import java.util.List;

import com.oy.shared.lw.LinguineSNMPWatch;
import com.oy.shared.lw.misc.ITrace;
import com.oy.shared.lw.misc.PoliteThread;
import com.oy.shared.lw.perf.monitor.TaskExecutionMonitor;
import com.oy.shared.lw.perf.monitor.VirtualMachineMonitor;

public class AllTestCases {
	
	public static List all =  new ArrayList();
	public static VirtualMachineMonitor vmon = new VirtualMachineMonitor();
	
	private static TaskExecutionMonitor [] makeMany(){
		List mons = new ArrayList();		
		for (int i=0; i < 4; i++){
			mons.add(new TaskExecutionMonitor(AllTestCases.class, "TaskExecutionMonitor" + i, ""));
		}		
		return (TaskExecutionMonitor[]) mons.toArray(new TaskExecutionMonitor[]{});
	}
	 
	public static void main(String [] args){
		LinguineSNMPWatch watch = new LinguineSNMPWatch(new ITrace.MockTraceOutImpl(AllTestCases.class, ITrace.INFO));
		
		watch.getPerfAgentContext().setSnapshotFileName("d:/perf.html");
		watch.getPerfAgentContext().setCollectDelayMillis(1000);
		watch.getPerfAgentContext().setSnapshotDelayMillis(1000);
		 
		try {
			watch.start();
			
			all.add(new UpdateMonitorThread(watch.getPerfAgent()));
			all.add(new AddRemoveMonitorThread(watch.getPerfAgent(), makeMany()));
	
			watch.getPerfAgent().addMonitor(vmon);
			
			start();
			
			try {
				Thread.sleep(60 * 1000);
			} catch(Exception ie){
				throw new RuntimeException(ie); 
			}
			
			watch.getPerfAgent().removeMonitor(vmon);
			
			stop();
			
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			watch.stop();
		}
	}

	
	public static void start(){
		for (int i=0; i < all.size(); i++){
			((PoliteThread) all.get(i)).start();
		}
	}

	public static void stop(){
		for (int i=0; i < all.size(); i++){
			((PoliteThread) all.get(i)).terminate();
		}		
	}
		
}
