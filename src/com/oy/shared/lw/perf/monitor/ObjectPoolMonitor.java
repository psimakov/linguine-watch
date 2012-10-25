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

public class ObjectPoolMonitor extends BaseMonitor {

	private long [] m_Values = new long[]{0, 0, 0, 0, 0, 0};
	private long [] m_Clone = new long[]{0, 0, 0, 0, 0, 0};
	
	public ObjectPoolMonitor (Class module, String name, String desc){
		super(
			module, name, desc,
			new String []{"added", "removed", "size", "borrowed", "returned", "outstanding"}	
		);
	}	
	
	public synchronized void incAdded(){ m_Values[0]++; }
	
	public synchronized void incRemoved(){ m_Values[1]++; }
	
	public synchronized void incBorrowed(){ m_Values[3]++; }	
	
	public synchronized void incReturned(){ m_Values[4]++; }
	
	public synchronized long [] getValues(){
		
		// compute "size"
		m_Values[2] = m_Values[0] - m_Values[1]; 
		
		// compute "outstading"
		m_Values[5] = m_Values[3] - m_Values[4];		
		
		return getValues(m_Values, m_Clone); 
	}
	
}
