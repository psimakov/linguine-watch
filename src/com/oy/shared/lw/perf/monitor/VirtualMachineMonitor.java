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


package com.oy.shared.lw.perf.monitor;

import com.oy.shared.lw.perf.agent.BaseMonitor;

public class VirtualMachineMonitor extends BaseMonitor {

	private long [] m_Values = new long[]{0, 0, 0, 0};
	private long [] m_Clone = new long[]{0, 0, 0, 0};
	
	public VirtualMachineMonitor (){
		super(
			VirtualMachineMonitor.class, "JVM MONITOR", 
			"This monitor reports cpu's, threads, free and total memory (MB) as reported by Java Virtual Machine runtime.",
			new String []{"cpu's", "threads", "mb free", "mb total"}	
		);
	}
	
	public synchronized long [] getValues(){
		m_Values[0] = Runtime.getRuntime().availableProcessors();
		m_Values[1] = Thread.activeCount();
		m_Values[2] = Runtime.getRuntime().freeMemory() >> 20;
		m_Values[3] = Runtime.getRuntime().totalMemory() >> 20; 
		
		return getValues(m_Values, m_Clone);		
	}
	
}
