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


package com.oy.shared.lw.perf.agent;

import com.oy.shared.lw.perf.IPerfMonitor;

public abstract class BaseMonitor implements IPerfMonitor {

	private String m_Module;
	private String m_Name; 
	private String m_Desc;
	private String [] m_Columns;
	
	public BaseMonitor (Class module, String name, String desc, String [] columnNames){
		if (module == null || module.equals("")){
			throw new RuntimeException("Monitor module name can't be empty.");
		}
		if (name == null || name.equals("")){
			throw new RuntimeException("Monitor name can't be empty.");
		}
		
		m_Module = module.getName();
		m_Name = name;			
		m_Desc = desc;
		m_Columns = columnNames;
	}
	
	public String getModule(){ return m_Module; }
	
	public String getName(){ return m_Name; } 
	
	public String getDesc(){ return m_Desc; }
	
	public String [] getColumnNames(){ return m_Columns; }
	
	public boolean hasValues(){ return true; }
	
	protected static long [] getValues(long [] from, long [] to){
		if (from.length != to.length){
			throw new IllegalArgumentException("Error copying arrays, sizes differ.");
		}
		System.arraycopy(from, 0, to, 0, to.length);
		return to;
	}
	
	public abstract long [] getValues();
	
}
