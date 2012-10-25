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

import com.oy.shared.lw.adapter.Perf2SNMPAdapter;
import com.oy.shared.lw.adapter.Perf2SNMPAdapterContext;

import com.oy.shared.lw.misc.ITrace;
import com.oy.shared.lw.perf.IPerfAgent;
import com.oy.shared.lw.perf.agent.PerfAgent;
import com.oy.shared.lw.perf.agent.PerfAgentContext;

import com.oy.shared.lw.snmp.ISNMPAgent;
import com.oy.shared.lw.snmp.agent.SNMPAgent;
import com.oy.shared.lw.snmp.agent.SNMPAgentContext;

public class LinguineSNMPWatch {

	private ITrace trace;
	
	private PerfAgent pAgent;
	private SNMPAgent sAgent;
	private Perf2SNMPAdapter adapter;
	
	private PerfAgentContext perfCtx = new PerfAgentContext();
	private SNMPAgentContext snmpCtx = new SNMPAgentContext();
	private Perf2SNMPAdapterContext adapterCtx = new Perf2SNMPAdapterContext();
	
	public LinguineSNMPWatch (){
		this(new ITrace.MockTraceEmptyImpl());
	}
	
	public LinguineSNMPWatch (ITrace trace){
		this.trace= trace;
	}
	
	public PerfAgentContext getPerfAgentContext(){
		return perfCtx;
	}
	
	public SNMPAgentContext getSNMPAgentContext(){
		return snmpCtx;
	}
	
	public IPerfAgent getPerfAgent(){
		return pAgent;
	}
	
	public ISNMPAgent getSNMPAgent(){
		return sAgent;
	}
	
	public void start() throws Exception {
		sAgent = new SNMPAgent(trace, snmpCtx);			
		pAgent = new PerfAgent(trace, perfCtx);
		
		adapter = new Perf2SNMPAdapter(adapterCtx);
		adapter.connect(pAgent, sAgent, sAgent.getMonitor());
	}
	
	public void stop(){
		adapter.disconnect();
		
		pAgent.stop();
		sAgent.stop();
	}
}
