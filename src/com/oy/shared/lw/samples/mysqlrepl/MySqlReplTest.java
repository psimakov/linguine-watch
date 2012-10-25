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

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import com.oy.shared.lw.LinguineSNMPWatch;
import com.oy.shared.lw.misc.CmdOptions;
import com.oy.shared.lw.misc.ITrace;

public class MySqlReplTest {

	static ITrace trace = new ITrace.MockTraceOutImpl(MySqlReplTest.class, ITrace.INFO);
	
	static MySqlReplManager man;
	static LinguineSNMPWatch watch;
	static Properties props;
	
	private static void init() throws Exception {
		
		// load mySQL JDBC driver; must be on the classpath		
		final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		try {
			Class.forName(JDBC_DRIVER);
		}catch(Exception e){
			throw new SQLException("Error loading database driver.");
		}
				
		// create simple conn pool; connection strings for master and slave are needed;
		// these must point to "mysql" database, not to your business database 
		MySqlReplConnectionPool pool = new MySqlReplConnectionPool.DefaultConnectionPool(
			"jdbc:mysql://localhost:3306/mysql?user=admin",
			"jdbc:mysql://localhost:3307/mysql?user=admin"			
		);				
		man.getContext().setConnectionPool(pool);

		
		// customize objects from properties
		CmdOptions.applyPropertiesToObjectFields(trace,
			"MySqlReplManager.Context", props, 
			man.getContext()
		);
		CmdOptions.applyPropertiesToObjectFields(trace,
			"MySqlReplManager.Context.ConnectionPool", props, 
			man.getContext().getConnectionPool()
		);		
		CmdOptions.applyPropertiesToObjectFields(trace,
			"LinguineSNMPWatch.PerfAgentContext", props, 
			watch.getPerfAgentContext()
		);
		CmdOptions.applyPropertiesToObjectFields(trace,
			"LinguineSNMPWatch.SNMPAgentContext", props, 
			watch.getSNMPAgentContext()
		);							
	}
	
	public static void main(String [] args){
		try {	

			trace.info(">>> Welcome to MySqlReplTest on " + new Date());
			trace.info(">>> Please type \"quit\" to exit!");
			
			if (args == null || args.length != 1){
				throw new RuntimeException("Properties file name must be specified as the only argument.");
			} 
			
			// load properties
			props = new java.util.Properties();
			trace.info("Loading properties from " + args[0]);
			FileInputStream is = new FileInputStream(new File(args[0]));
			try {
				props.load(is);
			} finally{
				is.close();
			}
			trace.info("Loaded " + props.size() + " properties");
			
			
			// create instances
			man = new MySqlReplManager(trace);
			watch = new LinguineSNMPWatch(trace);
			init();

			
			watch.start();		
			try {
			
				// after watch has started we can now add counters
				watch.getPerfAgent().addMonitor(man.getMonitor());			
				
				// after counters have been added we can generate SNMP MIB file
				String home = System.getProperty("user.home") + "/";
				watch.getSNMPAgent().generateMIBFile(
					home + MySqlReplTest.class.getName() + ".mib"
				);
				
				man.start();
				try {										
					CmdOptions.waitForUserToAbort(
						">>> MySQLReplTest > Please type \"quit\" to quit!", 
						"quit"
					);					
				} finally{
					man.stop();
				}
				watch.getPerfAgent().removeMonitor(man.getMonitor());
				
			} finally {
				watch.stop();
			}
			
		} catch(Exception e){
			e.printStackTrace();
		}
		
		trace.info(">>> Exited MySqlReplTest on " + new Date());
	}
	
}