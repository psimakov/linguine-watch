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
import java.util.Arrays;
import java.util.List;

import com.oy.shared.lw.misc.PoliteThread;
import com.oy.shared.lw.perf.IPerfAgent;
import com.oy.shared.lw.perf.monitor.TaskExecutionMonitor;

/**
 * In this test we add/remove monitors to IPerfAgent.  
 */
public class AddRemoveMonitorThread extends PoliteThread {

	private IPerfAgent agent;
	private List added = new ArrayList();
	private List removed = new ArrayList();

	public AddRemoveMonitorThread(IPerfAgent agent, TaskExecutionMonitor [] mons){
		this.agent = agent;

		removed.addAll(Arrays.asList(mons));
	}
	 
	public void run(){
		while(!isTerminated()){
			if (Math.random() < 0.5){
				add();
			} else {
				remove();
			}
		}
	}
	
	private void add(){
		if (removed.size() > 0){
			TaskExecutionMonitor mon = (TaskExecutionMonitor) removed.remove(0);
			added.add(mon);
			mon.incStarted();
			agent.addMonitor(mon);
		}
	}
	
	private void remove(){
		if (added.size() > 0){
			TaskExecutionMonitor mon = (TaskExecutionMonitor) added.remove(0);
			removed.add(mon);
			mon.incCompleted();
			agent.removeMonitor(mon);
		}
	}
	
}
