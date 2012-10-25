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

public final class MySqlReplContext {

	// the name of the root database for use with "USE %DATABASENAME%;" command;
	// this is needed so we can create a new database for housekeeping
	private String rootDbName = "mysql";
	
	// the name of the housekeeping database that will store timestamp and sequence
	// numbers used to track replication status
	private String dbName = "MASTER";
	
	// the name of the housekeeping database
	private String tableName = "_MASTERSTATUS";
	
	// the table type for the CREATE TABLE command 
	private String tableCreatePostfix = "type = InnoDB";
		
	// change master timestamp and sequence number and check slave status
	// every this number of milliseconds
	private int checkDelayMillis = 5000;
	
	// purge master logs after confirming that slave is upto date
	// every this number of milliseconds
	private int purgeDelayMillis = 5 * 60 + 1000;
	
	// if set to true will purge binary logs once confirmed with slave
	// that they hev been processed; is et to false will only report replication
	// status, increment master staus, but will not purge bin logs
	private boolean canPurgeBinLogs = true;
	
	// user can provide custom connection pool
	private MySqlReplConnectionPool pool;
	
	private boolean hasBinLogEnabled;
	
	private boolean canModify = true;
	
	public MySqlReplContext(){
		
	}
	
	void setCanModify(boolean value){
		canModify = value;
	}
	
	private void assertCanModify(){
		if (!canModify){
			throw new IllegalStateException("Can't modify context after agent has started.");
		}
	}	
	
	public void setDbName(String value){
		assertCanModify();
		dbName = value;
	}
	
	public void setRootDbName(String value){
		assertCanModify();
		rootDbName = value;
	}	
	
	public String getDbName(){
		return dbName;
	}
	
	public String getRootDbName(){
		return rootDbName;
	}	
	
	public void setTableName(String value){
		assertCanModify();
		tableName = value;
	}
	
	public String getTableName(){
		return tableName;
	}	
	
	public void setPurgeDelayMillis(int value){
		assertCanModify();
		purgeDelayMillis = value;
	}
	
	public void setCheckDelayMillis(int value){
		assertCanModify();
		checkDelayMillis = value;
	}

	public int getPurgeDelayMillis(){
		return purgeDelayMillis;
	}
	
	public int getCheckDelayMillis(){
		return checkDelayMillis;
	}

	public void setTableCreatePostfix(String value){
		assertCanModify();
		tableCreatePostfix = value;
	}
	
	public String getTableCreatePostfix(){
		return tableCreatePostfix;
	}
	
	public boolean getCanPurgeBinLogs(){
		return canPurgeBinLogs;
	}
	
	public void setCanPurgeBinLogs(boolean value){
		assertCanModify();
		canPurgeBinLogs = value;
	}
	
	public MySqlReplConnectionPool getConnectionPool(){
		return pool;
	}
	
	public void setConnectionPool(MySqlReplConnectionPool value){
		assertCanModify();
		pool = value;
	}
	
	public boolean getHasBinLogEnabled(){
		return hasBinLogEnabled;
	}
	
	void setHasBinLogEnabled(boolean value){
		hasBinLogEnabled = value;
	}
	
}
