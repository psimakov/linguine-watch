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


package com.oy.shared.lw.perf;

public interface IPerfMonitor {

	public String getModule();				// returns module name
	public String getName();				// returns counter name
	public String getDesc();				// counter description
	
	public String [] getColumnNames();		// returns column names
	public long [] getValues();				// returns column values
	
	public boolean hasValues();				// indicates that monitor has valid values;
											// monitors deleted from agent report false here  
	
	class MockPerfCounterImpl implements IPerfMonitor {
		
		private String module;
		private String name;
		private String desc;
		private String [] columns;
		
		MockPerfCounterImpl(String module, String name, String desc, String [] columns){
			this.module = module;
			this.name = name;
			this.desc = desc;
			this.columns = columns;
		}
		
		public String getModule(){
			return module;
		}		
		
		public String getName(){
			return name;
		}
		
		public String [] getColumnNames(){
			return columns;
		}
		
		public long [] getValues(){
			return new long [columns.length];
		}
			
		public String getDesc(){
			return desc;
		}
		
		public String toText(){
			return "";
		}		
		
		public boolean hasValues(){
			return true;
		}
	}
	
}
