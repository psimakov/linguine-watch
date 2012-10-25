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

final public class PerfAgentContext {
	
	// frequency for collecting data from the counters; 
	// once per this number of milliseconds 
	private int collectDelayMillis;

	// frequency for saving HTML snapshot file; 
	// once per this number of milliseconds 
	private int snapshotDelayMillis;

	// name of HTML snapshot file to periodically save data into;
	// no snapshot file if null
	private String snapshotFileName;
		
	// max number of history rows to keep in memory and in 
	// the HTML snapshot file 
	private int maxHistoryRows;
	 
	private HtmlSnapshotFormatter formatter;		
	
	private boolean canModify = true;
	
	public PerfAgentContext(){
		this(null);
	}
	
	public PerfAgentContext(String snapshotFileName){
		setCollectDelayMillis(5 * 1000);
		setSnapshotDelayMillis(5 * 1000);
		setMaxHistoryRows(360);
		setFormatter(new HtmlSnapshotFormatter());
		setSnapshotFileName(snapshotFileName);
	}

	void setCanModify(boolean value){
		canModify = value;
	}
	
	private void assertCanModify(){
		if (!canModify){
			throw new IllegalStateException("Can't modify context after agent has started.");
		}
	}
	
	public int getMaxHistoryRows(){
		return maxHistoryRows;
	}
	
	public void setMaxHistoryRows(int value){
		assertCanModify();
		maxHistoryRows = value;
	}

	
	public int getCollectDelayMillis(){
		return collectDelayMillis;
	}
	
	public void setCollectDelayMillis(int value){
		assertCanModify();
		collectDelayMillis = value;
	}
	
	public int getSnapshotDelayMillis(){
		return snapshotDelayMillis;
	}
	
	public void setSnapshotDelayMillis(int value){
		assertCanModify();
		snapshotDelayMillis = value;
	}
	
	public HtmlSnapshotFormatter getFormatter(){
		return formatter;
	}
	
	public void setFormatter(HtmlSnapshotFormatter value){
		assertCanModify();
		formatter = value;
	}
	
	public String getSnapshotFileName(){
		return snapshotFileName;
	}
	
	public void setSnapshotFileName(String value){
		assertCanModify();
		snapshotFileName = value;
	}
	
}
