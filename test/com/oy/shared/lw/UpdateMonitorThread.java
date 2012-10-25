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

import com.oy.shared.lw.misc.PoliteThread;
import com.oy.shared.lw.perf.IPerfAgent;
import com.oy.shared.lw.perf.monitor.ObjectLifetimeMonitor;
import com.oy.shared.lw.perf.monitor.PipeMonitor;
import com.oy.shared.lw.perf.monitor.TaskExecutionMonitor;

public class UpdateMonitorThread extends PoliteThread {

	private TaskExecutionMonitor task;
	private PipeMonitor pipe;
	private ObjectLifetimeMonitor life;
	private IPerfAgent agent;
	
	public UpdateMonitorThread(IPerfAgent agent){
		this.agent = agent;
		
		task = new TaskExecutionMonitor(
			UpdateMonitorThread.class,
			"TaskMonitorTest",
			"This is my test counter that should fail 30% of tasks."
		);		
		
		pipe = new PipeMonitor(
			UpdateMonitorThread.class,
			"PipeMonitorTest",
			"This is my test pipe."
		);
		
		life = new ObjectLifetimeMonitor(
			UpdateMonitorThread.class,
			"LifeTimeMonitomrTest",
			"This is my object lifetime test counter."
		);
	}
	
	public void run(){
		agent.addMonitor(task);
		agent.addMonitor(pipe);
		agent.addMonitor(life);
		
		while(!isTerminated()){
			
			task.incStarted();
			try {
				life.incCreated();
				
				// random delay
				int delay = (int) Math.round(100 * Math.random());				
				try {
					Thread.sleep(delay);
				} catch(InterruptedException ie){ }

				// some work
				boolean fail = Math.random() < 0.3;
				if (fail){
					pipe.incBytesWritten((long) (1000 * Math.random()));
					throw new RuntimeException("Fake failure.");
				} else {
					pipe.incBytesRead((long) (1000 * Math.random()));
				}							
				
				life.incFreed();
				task.incCompleted();
			} catch(Exception e){
				task.incFailed();
			}			
		}
		
		agent.removeMonitor(life);
		agent.removeMonitor(pipe);
		agent.removeMonitor(task);
	}
	
}
