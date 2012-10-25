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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public interface MySqlReplConnectionPool {

	public Connection borrowToMaster() throws SQLException;
	public Connection borrowToSlave() throws SQLException;
	
	public void releaseToMaster(Connection conn);
	public void releaseToSlave(Connection conn);
	
	class DefaultConnectionPool implements MySqlReplConnectionPool {
		
		// connection string to the master database
		private String masterConnStr;
		
		// connection string to the slave database
		private String slaveConnStr;
		
		public DefaultConnectionPool(String masterConnStr, String slaveConnStr){
			this.masterConnStr = masterConnStr;
			this.slaveConnStr = slaveConnStr;
		}

		public Connection borrowToMaster() throws SQLException{
			return DriverManager.getConnection(masterConnStr);
		}
		
		public Connection borrowToSlave() throws SQLException{
			return DriverManager.getConnection(slaveConnStr);
		}

		private void releaseAny(Connection conn){
			try {
				conn.close();
			} catch(Exception e){}
		}
		
		public void releaseToMaster(Connection conn){
			releaseAny(conn);
		}
		
		public void releaseToSlave(Connection conn){
			releaseAny(conn);	
		}
		
	}
	
}
