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

import com.oy.shared.lw.misc.ITrace;
import com.oy.shared.lw.perf.IPerfAgent;
import com.oy.shared.lw.perf.agent.PerfAgent;
import com.oy.shared.lw.perf.agent.PerfAgentContext;

public class LinguinePerfWatch {

	private ITrace trace;
	
	private PerfAgent pAgent;
	
	private PerfAgentContext perfCtx = new PerfAgentContext();
	
	public LinguinePerfWatch (){
		this(new ITrace.MockTraceEmptyImpl());
	}
	
	public LinguinePerfWatch (ITrace trace){
		this.trace= trace;
	}
	
	public PerfAgentContext getPerfAgentContext(){
		return perfCtx;
	}
	
	public IPerfAgent getPerfAgent(){
		return pAgent;
	}
	
	public void start() throws Exception {
		pAgent = new PerfAgent(trace, perfCtx);
	}
	
	public void stop(){
		pAgent.stop();
	}
	
}
