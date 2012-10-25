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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import com.oy.shared.lw.misc.ITrace;
import com.oy.shared.lw.samples.mysqlrepl.MySqlReplContext;

public class MySqlReplCmd {
	
	private final static NumberFormat formatter = new DecimalFormat("0000000000");
	
	// field names for master/slave queries
	final String SHOW_MASTER_STATUS_FILE = "File";
	final String SHOW_SLAVE_STATUS_MASTER_LOG_FILE = "Master_Log_File";
	final String SHOW_MASTER_LOGS_LOG_NAME = "Log_name";
	final String SHOW_VARIABLE_VALUE = "Value";
    
	private ITrace trace;
	private MySqlReplContext ctx; 
	
	public MySqlReplCmd(ITrace trace, MySqlReplContext ctx){
		this.trace = trace;
		this.ctx = ctx;
	}

	public boolean initMaster() throws SQLException {
		boolean hasBinLogEnabled = false;
		
		Connection conn = borrowNewToRoot();
		try {
			PreparedStatement stmt;
						
			// check mySQL version
			stmt = newPreparedStatement(conn, 
				"SHOW VARIABLES LIKE 'version';"
			);
			try {
				ResultSet rs = stmt.executeQuery();
				if (!rs.next()){
					throw new RuntimeException("Failed to check database engine version.");
				} else {
					trace.info("Found mysql engine version " + rs.getString(SHOW_VARIABLE_VALUE));
				}				
			} finally {
				stmt.close();
			}			
			
			// select db
			execUpdate(conn, "USE " + ctx.getRootDbName() + ";");
			execUpdate(conn, "CREATE DATABASE IF NOT EXISTS " + ctx.getDbName() + ";");
			execUpdate(conn, "USE " + ctx.getDbName() + ";");
			
			// ensure table
			execUpdate(conn, 
	    		"CREATE TABLE IF NOT EXISTS " + ctx.getTableName() + " (" + 
	    		"ID INTEGER NOT NULL, SEQUENCE INTEGER, UPDATED_ON DATETIME, MASTER_UID INTEGER, " + 
	    		"primary key (ID), index ID_idx (ID)" + 
	    		") " + ctx.getTableCreatePostfix() + ";"
			);	 
			
			// check that master is enabled
			stmt = newPreparedStatement(conn, 
				"SHOW MASTER STATUS;"
			);
			try {
				ResultSet rs = stmt.executeQuery();
				if (!rs.next()){
					trace.info("Did not find mysql master running.");
				} else {
					hasBinLogEnabled = true;
					trace.info("Master is running on bin log file " + rs.getString(SHOW_MASTER_STATUS_FILE));	
				}				
			} finally {
				stmt.close();
			}			
			
		} finally{
			releaseToRoot(conn);
		}
		
		return hasBinLogEnabled;
	}	
	
	public MySqlReplDbStatus incMasterStatus(Connection toMaster, int uid) throws SQLException {
		
		PreparedStatement stmt;
		ResultSet rs;
		
		// locate old record
		boolean hasOldStatus = false;
		int oldSeq = 0;
		stmt = newPreparedStatement(toMaster, 
			"SELECT * FROM " + ctx.getTableName() + " WHERE ID = 0;"
		);
		try {
			rs = stmt.executeQuery();
			if (rs.next()) {
				oldSeq = rs.getInt(2);				
				Date oldDate = (Date) rs.getObject(3);
				int oldUid = rs.getInt(4);
				
				if (oldUid != uid){
					trace.info("Took over old master with different uid " + oldUid);
				}
				
				hasOldStatus = true;
				
				trace.debug("Found master seq " + oldSeq + " dated " + oldDate);
			}									
			rs.close();
		} finally {
			stmt.close();
		}
		
		// create new record or update old record
		int newSeq;
		Date newDate = new Date();
		if (!hasOldStatus){
			newSeq = 0;
			stmt = newPreparedStatement(toMaster, 
				"INSERT INTO " + ctx.getTableName() + " VALUES(?, ?, ?, ?);"
			);
		} else {
			newSeq = oldSeq + 1;
			stmt = newPreparedStatement(toMaster, 
				"UPDATE " + ctx.getTableName() + " SET ID = ?, SEQUENCE = ?, UPDATED_ON = ?, MASTER_UID = ? WHERE ID = 0;"
			);
		}			
		try {
			stmt.setInt(1, 0);
			stmt.setInt(2, newSeq);
			stmt.setObject(3, newDate);
			stmt.setInt(4, uid);
			stmt.executeUpdate();
			
			trace.debug("Set master seq to " + newSeq + " dated " + newDate);
		} finally {
			stmt.close();
		}	
		
		return new MySqlReplDbStatus(newSeq, newDate, null);
	}
	
	public MySqlReplDbStatus getSlaveStatus(Connection toSlave, int uid) 
	throws SQLException, ESlaveStateException {			
		
		int seq;
		Date date;
		String currentLogFile = null;
		
		PreparedStatement stmt;
		ResultSet rs;
		
		// locate old record		
		stmt = newPreparedStatement(toSlave, 
			"SELECT * FROM " + ctx.getTableName() + " WHERE ID = 0;"
		);
		try {
			rs = stmt.executeQuery();
			if (!rs.next()) {
				throw new ESlaveStateException(
					"Failed to locate master status row in slave database."
				);
			}
			seq = rs.getInt(2);
			date = (Date) rs.getObject(3);
			int oldUid = rs.getInt(4);
			
			rs.close();
			
			if (oldUid != uid){
				throw new ESlaveStateException(
					"The slave reports to a different master " + oldUid
				);				
			}
			
			trace.debug("Found slave seq " + seq + " dated " + date);
		} finally {
			stmt.close();
		}
		
		// figure out what is the current active bin log file
		stmt = newPreparedStatement(toSlave, 
			"SHOW SLAVE STATUS;"
		);
		try {
			rs = stmt.executeQuery();
			if (rs.next()) {				
				currentLogFile = rs.getString(SHOW_SLAVE_STATUS_MASTER_LOG_FILE);
				
				trace.debug("Found slave bin log " + currentLogFile);
			}									
			rs.close();
		} finally {
			stmt.close();
		}
				
		return new MySqlReplDbStatus(seq, date, currentLogFile);
	}

	private boolean isValidMasterBinLog(Connection toMaster, String name) throws SQLException {
		PreparedStatement stmt = newPreparedStatement(toMaster, 
			"SHOW MASTER LOGS;"
		);
		try {			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				if (name.equalsIgnoreCase(rs.getString(SHOW_MASTER_LOGS_LOG_NAME))){ 
					return true;
				}
			}
			rs.close();
		} finally {
			stmt.close();
		}	
		
		return false;
	}
	
	public void purgeBinLogs(Connection toMaster, MySqlReplDbStatus slaveStatus, MySqlReplStatusMonitor monitor) throws SQLException {
		PreparedStatement stmt;
		ResultSet rs;			
		
		// pull extention out of bin log files
		int slaveIdx = getBinLogFileIndex(slaveStatus.getCurrentLogFile());
		if (slaveIdx == -1){
			throw new RuntimeException(
				"Failed to purge bin log files, can't determine bin log index from log name " + slaveStatus.getCurrentLogFile()
			);
		}
		 
		String fileBaseName = getBinLogFileNameBase(slaveStatus.getCurrentLogFile());
		String name = formatBinLogFileName(slaveIdx - 2, fileBaseName);
		String purgeToName = formatBinLogFileName(slaveIdx - 1, fileBaseName);
		
		// check if log exists			
		boolean before = isValidMasterBinLog(toMaster, name);		

		// purge logs					
		if (isValidMasterBinLog(toMaster, purgeToName)){
			stmt = newPreparedStatement(toMaster, 
				"PURGE MASTER LOGS TO ?;"
			);
			try {
				stmt.setString(1, purgeToName);
				rs = stmt.executeQuery();							
				rs.close();
			} finally {
				stmt.close();
			}  
		}
		
		// check if log has been purged			
		boolean after = isValidMasterBinLog(toMaster, name);	
		
		if (before && !after){
			trace.info(">>> Purged bin logs to " + name);
			monitor.setPurgeTo(slaveIdx - 1);
		}
	}
	
	private int execUpdate(Connection conn, String sql) throws SQLException {
		PreparedStatement stmt = newPreparedStatement(conn, sql);
		try {
			return stmt.executeUpdate();
		} finally{
			stmt.close();
		}	
	}	
	
	public void releaseToSlave(Connection conn){
		ctx.getConnectionPool().releaseToSlave(conn);
	}

	public void releaseToMaster(Connection conn){
		ctx.getConnectionPool().releaseToMaster(conn);
	}

	public void releaseToRoot(Connection conn){
		ctx.getConnectionPool().releaseToMaster(conn);
	}
	
	public Connection borrowNewToSlave() throws SQLException {
		Connection conn = ctx.getConnectionPool().borrowToSlave(); 
		execUpdate(conn, "USE " + ctx.getDbName() + ";");
		return conn;
	}
	
	public Connection borrowNewToMaster() throws SQLException {
		Connection conn = ctx.getConnectionPool().borrowToMaster();
		execUpdate(conn, "USE " + ctx.getDbName() + ";");
		return conn;
	}
	
	private Connection borrowNewToRoot() throws SQLException {
		Connection conn = ctx.getConnectionPool().borrowToMaster();
		execUpdate(conn, "USE " + ctx.getRootDbName() + ";");
		return conn;
	}	
	
	
	private static int getBinLogFileIndex(String fileName){
		
		if (fileName == null){
			return -1;
		}
		
		int idx = fileName.lastIndexOf('.');
		if (idx == -1 || idx == fileName.length() - 1){
			return -1;
		}
		
		String ext = fileName.substring(idx + 1);
		try {
			return Integer.parseInt(ext); 
		} catch(Exception e){
			throw new RuntimeException("Failed to get bin log file index for file " + fileName);
		}
	}
	
	private static String getBinLogFileNameBase(String fileName){
		String msg = "Failed to get bin log file base name " + fileName;
		
		if (fileName == null){
			throw new RuntimeException(msg);
		}
		
		int idx = fileName.lastIndexOf('.');
		if (idx == -1 || idx == fileName.length() - 1){
			throw new RuntimeException(msg);
		}
		
		return fileName.substring(0, idx + 1);
	}
	
	
	private String formatBinLogFileName(int idx, String baseName){		
		String name = formatter.format(idx);		
		if (idx < 1000){
			return baseName + name.substring(7);
		}
		if (idx < 10000){
			return baseName + name.substring(6);
		}
		if (idx < 100000){
			return baseName + name.substring(5);
		}		
		if (idx < 1000000){
			return baseName + name.substring(4);
		}
		if (idx < 10000000){
			return baseName + name.substring(3);
		}
		if (idx < 100000000){
			return baseName + name.substring(2);
		}
		if (idx < 1000000000){
			return baseName + name.substring(1);
		}				
		
		throw new RuntimeException("Master bin log index is too big " + idx);
	}

	private PreparedStatement newPreparedStatement(Connection conn, String text) throws SQLException {
		return conn.prepareStatement(text);
	}		
	
}
