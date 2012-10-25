linguine-watch
==============

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



Version Info
============

Version: 1.2
Last build date: April 1, 2006
Release date: April 1, 2006


About this Package
==================

This package contains full set of Java classes for real-time monitoring of 
Java software applications. The monitoring can be done by capturing performance data 
periodically with snapshots or by reporting data in real-time to SNMP monitoring 
station. With this package you can:

- define monitoring objects (counters and gauges) and add them to your code
- automatically collect data from monitoring objects in real-time
- create periodic snapshots containing monitoring object values
- add complete SNMP v.1 monitoring support to your Java application
- generate SNMP v.1 MIB files automatically 

Several examples are included in the package. You can find their description down
below. Visit the Linguine Watch homep page at http://www.softwaresecretweapons.com
for more examples and documentation.

Files in this package
=====================

This package contains:

\dist
	jar file with classes from this package

\lib
	jar files needed for this package

\src
	all source files for this package
	
\src\com\oy\shared\lw\perf
	source files for core performance monitoring library
	
\src\com\oy\shared\lw\snmp
	source files for core SNMP v.1
	
\src\com\oy\shared\lw\samples
	sample applications included with this package	
	
\src\snmp
	original source files for SNMP Package, Copyright (C) 2004, Jonathan Sevy <jsevy@mcs.drexel.edu>


Changes from release 1.2
========================

- long [] values are not initialized; must be set to 0 explicitly
COmPLETE THIS NOW


Changes from release 1.1
========================

- added configuration of MySqlReplTest via property file
- added CmdOptions to handle properties and console input
- bug: MySqlReplStatusMonitor did not use values clonning in getValues()

Changes from release 1.0
========================

- significant changes to MySQL Replication 
 	- added check for MySQL version
	- added master_uid field to confirm that slave takes data from this master
	- added property canPurgeBinLogs to MySqlReplContext, to allow monitoring without 
  	  actually purging bin logs
	- property binLogFileNameBase was removed from MySqlReplContext, bin log file name 
  	  is now determine automatically
- added ObjectPoolMonitor to standard monitors
- added System.arraycopy to keep consistency of value in the array returned out of getValues()
- added sample that shows how to add SNMP Monitoring Support To HttpSessionListener


How to compile Linguine Watch and run samples in Eclipse 3
==========================================================

This project is quite easy to build in Eclipse 3. Open Eclipse workspace, create new Java
project and give it a name. Drag/drop the entire content of the official distribution
Linguine Watch package into the project. Select build.xml at the root of the project
and execute it in Eclipse.

The "gc" and "graph" samples require no setup. The "mysqlrepl" requires running master
and slave MySQL servers of version 4.0.20 or higher and proper connection string for each.
All output for sample applications goes into %user.home% directory, so if you run on Windows
it will be C:\Documents and Settings\%USER_NAME%.


Instrumenting Graph.java application to support SNMP
====================================================

This illustrates the use of Linguine Watch for adding real-time monitoring 
to an existing Java application. I have taken the famous Java Applet that 
has been shipped with Java JDK for years. In just several minutes I have added 
the real-time SNMP monitoring support to this application as I describe below. 

What kind of monitoring is needed here? Let's monitor the stress level
in the graph and the user mouse activity. The original source is 
under com.oy.shared.lw.samples.graph.original. The modified source is 
under com.oy.shared.lw.samples.graph.modified.

Here is the summary of modifications:

- add several import statements at the top of the file

	import com.oy.shared.lw.LinguineSNMPWatch;
	import com.oy.shared.lw.perf.monitor.SimpleGaugeMonitor;
	import com.oy.shared.lw.perf.monitor.TaskExecutionMonitor;
	import com.oy.shared.lw.perf.monitor.VirtualMachineMonitor;

- declare and instantiate graph stress and mouse activity monitors; graph stress
monitor has one value that holds total graph stress; the mouse activity monitor 
records each time a mouse is pressed down and released

	TaskExecutionMonitor mouseMon = 
		new TaskExecutionMonitor(Graph.class, "MOUSE DRAG", "Monitors mouse activity.");
	SimpleGaugeMonitor stressMon = 
		new SimpleGaugeMonitor(Graph.class, "STRESS", "Monitors stress in the graph.");

- declare and instantiate Java Virtual Machine monitor that monitors memory, threads, etc.
	VirtualMachineMonitor jvmMon = new VirtualMachineMonitor();

- declare and instantiate Linguine Watch server object
	LinguineSNMPWatch watch = new LinguineSNMPWatch();	
	
- instrument relax() function to update graph stress level

    synchronized void relax() {    
   	    double stress = 0;
   	    for (int i = 0 ; i < nedges ; i++) {
   	    	...
   	    	...
		    stress += edges[i].len - len;
   	    }
    	stressMon.setValue(Math.round(Math.abs(stress)) / nedges);
    	...
    	...
	}
	
- instrument mousePressed() and mouseReleased() to track mouse state

	public void mousePressed(MouseEvent e) {    	
    	mouseMon.incStarted();
    	...
	}
    
    and
    	
    public void mouseReleased(MouseEvent e) {
    	...
		mouseMon.incCompleted();	
    }    	

- alter start() function to start Linguine Watch server, register monitoring
objects and generate MIB file 

    public void start() {    	

		String home = System.getProperty("user.home") + "/";
    	
    	watch.getPerfAgentContext().setSnapshotFileName(home + GraphPanel.class.getName() + ".html");
    	try {
    		watch.start();
    	
	    	watch.getPerfAgent().addMonitor(stressMon);
	    	watch.getPerfAgent().addMonitor(mouseMon);
	    	watch.getPerfAgent().addMonitor(jvmMon);
		
			watch.getSNMPAgent().generateMIBFile(home + GraphPanel.class.getName() + ".mib");
		} catch(Exception ie){
			throw new RuntimeException(ie); 
		}
		
		...
		...
	}
 
- alter stop() function to unregister monitoring objects and stop Linguine Watch server 

	public void stop() {
    	watch.getPerfAgent().removeMonitor(jvmMon);
    	watch.getPerfAgent().removeMonitor(mouseMon);
    	watch.getPerfAgent().removeMonitor(stressMon);

    	watch.stop();
    	    	
		...
		...
    }
    
Now start this application. Check that MIB file and performance snapshot files
are produced in the user.home folder. Start your favorite SNMP monitoring
software, open newly generated MIB file and select oylw_stress_gauge object
for monitoring (oid 1.3.6.1.4.1.1.4349.484.1). Disrupt the graph by dragging 
or shaking it and see SNMP monitoring tool to immediately reflect it. 

Review snapshots of monitoring objects by opening HTML snapshot file in the 
user.home folder. This file reports values for all monitoring objects 
collected periodically.


Monitoring behavior of various garbage collection algorithms with SNMP
======================================================================

The gc example is located in com.oy.shared.lw.samples.gc package. This sample
application is designed to monitor how objects are created, freed and garbage collected.
In this sample I wanted to directly measure the impact of garbage collection on the runtime 
behavior of my Java application. Given that garbage collection typically causes a problem 
when Java objects are continuously allocated and freed, I needed to know: 
- when objects are allocated 
- when objects become eligible for garbage collection 
- when objects are actually freed, and memory is released 

The whole test consists of only a couple of dozen lines of Java code. The main thread 
is in the class TestGCThread. To avoid CPU starvation, while running, this thread enters 
a sleep period of variable duration (0-100 ms). After each sleep period, it creates a 
variable number of TestGCObject instances (0-10,000 at a time). The allocated objects 
can be immediately garbage collected as they are not being used further. The integers 
are used to count each time an instance of TestGCObject is created in a constructor or 
finalized in a finalize().

Run a separate test for each of the popular garbage collection algorithms.  Use SNMP to 
monitor object lifetimes in real-time.

Overriding finalize() method for Java class dramatically changes how garbage collection 
works with this class. You can easily monitor lifetime of your classes that already override
finalize(). It might not be a good idea to override finalize() for other classes just to 
enable monitoring.


Adding SNMP monitoring support to MySQL master/slave replication
================================================================

The MySQL replication is very easy to set up, but quite tricky to monitor.
Specific pain comes from the need to purge master binary logs. This sample application 
shows you not only monitor a state of the replication via SNMP, but also handles
the binary log file purging automatically. The MySQL driver must be on the classpath.

You can find the sample application in com.oy.shared.lw.samples.mysqlrepl
package. Before running it, please have master and slave MySQL servers 
ready. After that, review initManager() function and edit master and slave connection 
strings. There is detailed article accompanying this example at:
http://www.softwaresecretweapons.com/jspwiki/Wiki.jsp?page=HowToMonitorMYSQLReplicationInRealTimeWithSNMP


Can I see the monitoring data without SNMP monitoring software?
===============================================================

Yes. All sample applications also produce an HTML file snapshot that contains data for
all counters defined in the project. This file is updated once every 5 seconds and placed 
in the C:\Documents and Settings\%USER_NAME% folder.


Are there any free SNMP monitoring software products to use with Linguine Watch?
================================================================================

To see the counters at work and to plot the counter values in real-time, AdventNet SNMP 
Utilities 4 package can be used. This professional quality package has an MIB browser 
and has good SNMP monitoring support in a limited-time free trial. Any other SNMP monitoring 
package will do as long as it supports SNMP v1.0.

You will need an MIB file to properly choose the counters for monitoring. All sample applications
automatically produce MIB file. It can be found in the C:\Documents and Settings\%USER_NAME% folder.


What are the various configuration options available in Lingine Watch?
======================================================================

- class PerfAgentContext, contains core performance monitoring functionality
	
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
	
	Each of these variables has a corresponding setter and getter method.

- class HtmlSnapshotFormatter, customizes look and feel of periodic HTML snapshot file
	
	// many options are available, knowledge of HTML is required
	
- class SNMPAgentContext, contains core functionality for SNMP agent
	
	// TCP/IP port number for the SNMP agent server;
	// defaults to standard SNMP port 161  
	private int port; 
	
	// each Enterprise must place its counters under
	// a specific enterprise identified; if you do not have one
	// leave this alone; used for MIB file generation
	private int enterprisesOID;
	
	// name of the enterprise; used for MIB file generation
	private String organizationName;
	
	// contact info for the enterprise; used for MIB file generation
	private String contactInfo;
	
	// community string for SNMP agent to respond to;
	// default to "public"
	private String communityString;
	
	// standard prefix for the names of counter/gages; used for MIB file generation
	private String namespace;

Motivation for this package
===========================

Understanding how application behaves at runtime is a very important, but 
difficult task. It is quite common to use logging, debuggers, profiles, or other 
kinds of instrumentation to do it. Unfortunately these approaches can't be used in 
production as they waste CPU and memory and produce too much data.

On the other hand SNMP real-time monitoring is a low-overhead de facto standard for all
hardware devices and enterprise-class software applications. SNMP allows 
monitoring and understanding of software and hardware real-time behavior in 
production.

How can we easily add SNMP support to our Java applications? While many other
packages were out there to provide SNMP classes, they did not quite help a
software engineers to do the job end-to-end. The most important part of deciding 
on metrics to monitor and finding hook points for actually get values for these metrics
is not being explored.

With Linguine Watch package you can add SNMP monitoring to your Java
applications in minutes. We take care of all things including multi-
threading and MIB file generation. All you need to do is to focus on hot spots 
in your application that need monitoring. Just decide on the metrics, the rest
is automatic.

Please enquire further if you need help with similar projects.
 
Regards,

Dr. Pavel Simakov

Email:  <psimakov@softwaresecretweapons.com>
WWW:    http://www.softwaresecretweapons.com