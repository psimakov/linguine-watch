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

public class ObjectLifetimeMonitor extends BaseMonitor {

	private long [] m_Values = new long[]{0, 0, 0, 0, 0};
	private long [] m_Clone = new long[]{0, 0, 0, 0, 0};
	
	public ObjectLifetimeMonitor (Class module, String name, String desc){
		super(
			module, name, desc,
			new String []{"created", "serialized", "deserialized", "freed", "live"}	
		);
	}	
	
	public synchronized void incCreated(){ m_Values[0]++; }
	
	public synchronized void incSerialized(){ m_Values[1]++; }
	
	public synchronized void incDeSerialized(){ m_Values[2]++; }	
	
	public synchronized void incFreed(){ m_Values[3]++; }
	
	public synchronized long [] getValues(){
		
		// compute "live"
		m_Values[4] = m_Values[0] - m_Values[3]; 
		
		return getValues(m_Values, m_Clone); 
	}
	
}
