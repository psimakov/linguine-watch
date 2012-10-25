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

package com.oy.shared.lw.samples.mysqlrepl;

import java.sql.SQLException;

import com.oy.shared.lw.misc.ITrace;
import com.oy.shared.lw.perf.IPerfMonitor;
import com.oy.shared.lw.samples.mysqlrepl.impl.MySqlReplCmd;
import com.oy.shared.lw.samples.mysqlrepl.impl.MySqlReplStatusMonitor;
import com.oy.shared.lw.samples.mysqlrepl.impl.MySqlReplThread;

public class MySqlReplManager {
		
    private MySqlReplStatusMonitor monitor = new MySqlReplStatusMonitor();
    
    private ITrace trace;    
	private MySqlReplContext ctx = new MySqlReplContext();
    private MySqlReplThread thread;
    private MySqlReplCmd cmd;
	
    
    public MySqlReplManager() throws SQLException {
    	this(new ITrace.MockTraceEmptyImpl());
    }
    
	public MySqlReplManager(ITrace trace) {
		this.trace = trace;
		
		cmd = new MySqlReplCmd(this.trace, ctx);
	} 	
	
	public void start() throws SQLException {
		ctx.setCanModify(false);
		
		boolean hasBinLogEnabled = cmd.initMaster();
		ctx.setHasBinLogEnabled(hasBinLogEnabled);
		
		thread = new MySqlReplThread(trace, ctx, monitor, cmd);		
		thread.start();
	}
	
	public void stop(){
		thread.terminate();		
		
		ctx.setCanModify(true);
	}
	
	public MySqlReplContext getContext(){
		return ctx;
	}
	
	public IPerfMonitor getMonitor(){
		return monitor;
	}
		
}