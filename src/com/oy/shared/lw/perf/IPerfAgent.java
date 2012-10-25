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

public interface IPerfAgent {

	// individual counters can be added, removed and enumerated
	public void addMonitor(IPerfMonitor monitor);
	public void removeMonitor(IPerfMonitor monitor);	
	public IPerfMonitor [] enumMonitors();
	
	// listeners can be added to observer addition and removal
	// of monitors
	public void addListener(IPerfAgentListener listener);
	public void removeListener(IPerfAgentListener listener);
	
	// a snapshot in HTML or other formats can be prepared explicitly
	// or inplicitly by snapshot providing file name in PerfAgentContext;
	// snapshot file formatting can be changed in HtmlSnapshotFormatter 
	public void makeSnapshot(StringBuffer sb);
	public void makeSnapshot(StringBuffer sb, int maxItems);
	
	class MockPerfAgent implements IPerfAgent {	
		public void addMonitor(IPerfMonitor monitor){}
		public void removeMonitor(IPerfMonitor monitor){}	
		public IPerfMonitor [] enumMonitors(){ return new IPerfMonitor [] {}; }
		public void addListener(IPerfAgentListener listener) {}
		public void removeListener(IPerfAgentListener listener) {}
		public void makeSnapshot(StringBuffer sb){}
		public void makeSnapshot(StringBuffer sb, int maxItems){}		
	}
}
