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
 
package com.oy.shared.lw.samples.mysqlrepl.impl;

import java.sql.Connection;
import java.util.Random;

import com.oy.shared.lw.misc.ITrace;
import com.oy.shared.lw.misc.PoliteThread;
import com.oy.shared.lw.samples.mysqlrepl.MySqlReplContext;

public class MySqlReplThread extends PoliteThread {
	
	private long lastPurgeTick = -1;

	private ITrace trace;
	private MySqlReplContext ctx;
	private MySqlReplStatusMonitor monitor;
	private MySqlReplCmd cmd;
	
	private Random rand = new Random();
	private int uid;

	
	public MySqlReplThread(ITrace trace, MySqlReplContext ctx, MySqlReplStatusMonitor monitor, MySqlReplCmd cmd){
		this.trace = trace;
		this.ctx = ctx; 
		this.monitor = monitor;
		this.cmd = cmd;
		
		uid = rand.nextInt();
	}
	
	public void run(){
		MySqlReplDbStatus slaveStatus;
		MySqlReplDbStatus masterStatus;
		
		while(!isTerminated()){			
			masterStatus = incMasterStatus();
			
			waitForReplicationToWork();
			if (isTerminated()) break; 
			
			slaveStatus = null;
			if (ctx.getHasBinLogEnabled()){
				slaveStatus = getSlaveStatus(masterStatus);				
				if (ctx.getCanPurgeBinLogs()){
					purgeMasterBinLogs(slaveStatus);				
				}				
			}
			
			// update counters
			if (masterStatus != null){
				monitor.setMasterSeq(masterStatus.getSeq());
			}
			
			if (slaveStatus != null){
				monitor.setSlaveSeq(slaveStatus.getSeq());				
			}
			
		}
	}
	
	private MySqlReplDbStatus incMasterStatus(){
		MySqlReplDbStatus masterStatus = null;
		
		try {
			Connection toMaster = cmd.borrowNewToMaster();
			try {
				masterStatus = cmd.incMasterStatus(toMaster, uid);
			} finally {
				cmd.releaseToMaster(toMaster);
			}
		} catch(Throwable t){
			monitor.incErrors();
			trace.error(t);					
		}
		
		return masterStatus;
	}

	private void waitForReplicationToWork(){
		try {
			Thread.sleep(ctx.getCheckDelayMillis());
		} catch(InterruptedException ie){					
		}		
	}
	
	private MySqlReplDbStatus getSlaveStatus(MySqlReplDbStatus masterStatus){
		MySqlReplDbStatus slaveStatus = null;
		
		Connection toSlave = null;		
		try {
			toSlave = cmd.borrowNewToSlave();			
			try {
				slaveStatus = cmd.getSlaveStatus(toSlave, uid);
				
				// check that master is ahead of slave
				if (masterStatus != null && slaveStatus != null){
					int lag = masterStatus.getSeq() - slaveStatus.getSeq();
					if(lag < 0){
						throw new ESlaveStateException("Slave ahead of master with lag of " + lag);
					}
				}

			} finally {			
				cmd.releaseToSlave(toSlave);
			}			
		} catch(Throwable t){
			monitor.incErrors();
			trace.error("Failed to get mysql slave status.", t);													
		}
		
		return slaveStatus;
	}
	
	private void purgeMasterBinLogs(MySqlReplDbStatus slaveStatus) {		
		try {
			long now = System.currentTimeMillis();
			boolean needPurge = lastPurgeTick == -1 || (now - lastPurgeTick > ctx.getPurgeDelayMillis());
			if (needPurge && slaveStatus != null){
				Connection toMaster = cmd.borrowNewToMaster(); 
				try {
					cmd.purgeBinLogs(toMaster, slaveStatus, monitor);
					lastPurgeTick = now;
				} finally {
					cmd.releaseToMaster(toMaster);
				}
			}
		} catch(Throwable t){
			monitor.incErrors();
			trace.error(t);					
		}		
	}
	
}
