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
import com.oy.shared.lw.perf.agent.QOSCalculator;

public class TaskExecutionMonitor extends BaseMonitor {

	private long [] m_Values = new long[]{0, 0, 0, 0, 0};
	private long [] m_Clone = new long[]{0, 0, 0, 0, 0};
	private QOSCalculator m_Qos;
	
	public TaskExecutionMonitor (Class module, String name, String desc){
		this(module, name, desc, 1000, 60 * 1000);
	}
	
	public TaskExecutionMonitor (
		Class module, String name, String desc, long unitsOfWorkPerBusket, long ageOfBucket
	){
		super(
			module, name, desc,
			new String []{"started", "completed", "failed", "in progress", "qos"}	
		);
		
		m_Qos = new QOSCalculator(0, 0, unitsOfWorkPerBusket, ageOfBucket);
	}
	
	public synchronized void incStarted(){ m_Values[0]++; }
	
	public synchronized void incCompleted(){ m_Values[1]++; }
	
	public synchronized void incFailed(){ m_Values[2]++; }	
	
	public synchronized long [] getValues(){
		
		// compute "in progress"
		m_Values[3] = m_Values[0] - m_Values[1] - m_Values[2];
		
		// update qos
		m_Values[4] = m_Qos.getQOS(m_Values[1], m_Values[2]);
		
		return getValues(m_Values, m_Clone);		
	}
}
