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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.oy.shared.lw.misc.ITrace;
import com.oy.shared.lw.misc.PoliteThread;
import com.oy.shared.lw.perf.IPerfAgent;
import com.oy.shared.lw.perf.IPerfAgentListener;
import com.oy.shared.lw.perf.IPerfMonitor;


public class PerfAgent implements IPerfAgent {
		
	class PerfMonitorStub implements IPerfMonitor {
		
		private String m_Module;
		private String m_Name;
		private String m_Desc;
		private String [] m_ColumnNames;
		
		public PerfMonitorStub(IPerfMonitor monitor){
			m_Module = monitor.getModule();
			m_Name = monitor.getName();
			m_Desc = monitor.getDesc();
			m_ColumnNames = monitor.getColumnNames();
		}
		
		public String getModule(){ return m_Module; }
		public String getName(){ return m_Name;}
		public String getDesc(){ return m_Desc;}		
		public String [] getColumnNames(){ return m_ColumnNames; }
		public boolean hasValues(){ return false; }
		public long [] getValues(){ throw new RuntimeException("Not implemented."); }			
	}
	
	class ServerThread extends PoliteThread {
		
        public void run() {
            while (!isTerminated()) {            	
            	try {            	
	            	collectNow();            	
	            	
	            	long now = System.currentTimeMillis();
	            	if (m_LastSnapshot == -1 || now - m_LastSnapshot > m_Ctx.getSnapshotDelayMillis()){
	            		snapshotNow();	            		
	            		m_LastSnapshot = now;
	            	}
            	
	            	if (m_Ctx.getCollectDelayMillis() != 0){
	            		Thread.sleep(m_Ctx.getCollectDelayMillis());
	            	}
            	} catch(InterruptedException ie){
            	} catch(Throwable t){ 
            		m_Trace.error(t);
            	}
            }
        }

    }
		
	private static NumberFormat m_NumFormat = NumberFormat.getInstance();
	private static SimpleDateFormat m_DateFormat = new SimpleDateFormat(
		"yy/MM/dd HH:mm:ss"
	);
	
	private Set m_Listeners = new HashSet();
	private List m_Monitors = new LinkedList();
	private List m_History = new LinkedList();
	private StringBuffer m_Buffer = new StringBuffer();
	private int m_Sequence = 0;
	private int m_Collect = 0;	
	private String m_OldPerf;		
	private long m_LastSnapshot = -1;
	
	private ServerThread m_Thread;
	private ITrace m_Trace;	
	private PerfAgentContext m_Ctx;
	
	
	public PerfAgent(PerfAgentContext ctx) throws Exception {
		this(new ITrace.MockTraceEmptyImpl(), ctx);
	}
	
	public PerfAgent(ITrace trace, PerfAgentContext ctx) throws Exception {
		m_Trace = trace;
		m_Ctx = ctx;
	
		m_Ctx.setCanModify(false);
		
    	m_Thread = new ServerThread();
    	m_Thread.start();    
    	
    	m_Trace.info("Perf Agent Started");
    }
	
	public void stop(){
		m_Thread.terminate();
		m_Thread = null;
		
		m_Ctx.setCanModify(true);
		
		m_Trace.info("Perf Agent Stopped");
	}		
	
	public void addListener(IPerfAgentListener listener){
		synchronized(m_Monitors){
			if (m_Listeners.contains(listener)){
				throw new RuntimeException("Error adding, already added.");
			}
			m_Listeners.add(listener);
		}
	}
	
	public void removeListener(IPerfAgentListener listener){
		synchronized(m_Monitors){
			if (!m_Listeners.contains(listener)){
				throw new RuntimeException("Error removing, not added.");
			}
			m_Listeners.remove(listener);
		}
	}

		
	private void renderHeaderRow(StringBuffer sb, HtmlSnapshotFormatter form){		
		sb.append(form.getHeadRowPrefix());
		
		sb.append(form.getHeadCellPrefix());
		sb.append(form.getNamePrefix());
		sb.append("ID");
		sb.append(form.getNamePostfix());	
		
		sb.append(form.getValuesPrefix());
		sb.append(m_NumFormat.format(m_Collect));
		sb.append(form.getValuesPostfix());			
		sb.append(form.getCellPostfix());
		
		sb.append(form.getHeadCellPrefix());
		sb.append(form.getNamePrefix());
		sb.append("TIME");
		sb.append(form.getNamePostfix());
		
		sb.append(form.getValuesPrefix());
		sb.append("yy/mm/dd hh:mm:ss");
		sb.append(form.getValuesPostfix());			
		sb.append(form.getCellPostfix());			
					
		for (int i=0; i < m_Monitors.size(); i++){
			IPerfMonitor mon = (IPerfMonitor) m_Monitors.get(i);

			sb.append(form.getHeadCellPrefix());	
			
			sb.append(form.getNamePrefix());					
			form.escapeAndWriteCellValue(sb, mon.getName());
			sb.append(form.getNamePostfix());

			sb.append(form.getValuesPrefix());
			String [] cols = mon.getColumnNames();
			for (int j=0; j < cols.length; j++){
				if (j != 0){
					sb.append(form.getValueSeparator());
				}
				form.escapeAndWriteCellValue(sb, cols[j]);
			}
			sb.append(form.getValuesPostfix());
			
			sb.append(form.getCellPostfix());
		}
		sb.append(form.getRowPostfix());	
	}
	
	private void renderDataRows(StringBuffer sb, HtmlSnapshotFormatter form, int maxItems){
		for (int i=0; i < m_History.size(); i++){
			if ( (i >= maxItems) && (maxItems != -1) ) {
				break;
			} 
			
			if ((m_Sequence - i) % 2 == 0){
				sb.append(form.getEvenRowPrefix());	
			} else {
				sb.append(form.getOddRowPrefix());
			}				
							
			sb.append(form.getCellPrefix());
			sb.append(m_NumFormat.format(m_Sequence - i));
			sb.append(form.getCellPostfix());
			sb.append((String) m_History.get(m_History.size() - 1 - i));
			sb.append(form.getRowPostfix());
		}
	}
	
	public void makeSnapshot(StringBuffer sb){
		makeSnapshot(sb, -1);
	}
	
	public void makeSnapshot(StringBuffer sb, int maxItems){		
		synchronized(m_Monitors){			
			HtmlSnapshotFormatter form = m_Ctx.getFormatter();
			
			sb.append(form.getSnapshotPrefix());			
			renderHeaderRow(sb, form);
			renderDataRows(sb, form, maxItems);
			sb.append(form.getSnapshotPostfix());
		}
	}	
	
	private void assertValidMonitor(IPerfMonitor monitor){
		if (monitor.getModule() == null || "".equals(monitor.getModule())){
			throw new RuntimeException("Bad monitor name, empty module name is not allowed.");
		}
		
		if (monitor.getName() == null || "".equals(monitor.getName())){
			throw new RuntimeException("Bad monitor name, empty name is not allowed.");
		}
	}
		
	private int indexOfName(IPerfMonitor monitor){
		for (int i=0; i < m_Monitors.size(); i++){
			IPerfMonitor mon = (IPerfMonitor) m_Monitors.get(i);
			
			String name1 = mon.getModule() + mon.getName(); 
			String name2 = monitor.getModule() + monitor.getName();
			
			if (name1.equals(name2)){
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * wehn we add monitor we might have a stub for it when it wasremoved last;
	 * in this case we need to replace stub with real monitor; this
	 * is done so the position of a collected value in the history row does not 
	 * change 
	 */
	public void addMonitor(IPerfMonitor monitor){
		synchronized(m_Monitors){	
			m_Trace.debug("Adding " + monitor.getName());
			
			assertValidMonitor(monitor);			
			if (m_Monitors.indexOf(monitor) != -1){
				throw new RuntimeException("Error adding monitor, already added " + monitor);
			}			
			
			int idx = indexOfName(monitor);
			if (idx != -1){
				IPerfMonitor mon = (IPerfMonitor) m_Monitors.get(idx);
				if (mon instanceof PerfMonitorStub){
					m_Monitors.set(idx, monitor);
				} else {
					throw new RuntimeException("Error adding monitor, monitor with same name already exists.");
				}
			} else {
				m_Monitors.add(monitor);	
			}			
		}
		
		try {
			afterAddMonitor(monitor);
		} catch(Exception e){
			m_Trace.error(e);
		}		
	}

	/**
	 * when we remove monitors will keep a stub object instead; this
	 * is done so the position of a collected value in the history row does not 
	 * change  
	 */
	public void removeMonitor(IPerfMonitor monitor){
		synchronized(m_Monitors){
			m_Trace.debug("Removing " + monitor.getName());
			
			int idx = m_Monitors.indexOf(monitor);
			if(idx == -1) { 			
				throw new RuntimeException("Error removing monitor, not added " + monitor);
			}			
			
			m_Monitors.set(idx, new PerfMonitorStub(monitor));					
		}
		
		try{
			afterRemoveMonitor(monitor);
		} catch(Exception e){
			m_Trace.error(e);
		}		
	}	
	
	private void renderValues(StringBuffer sb, int count, long [] values){
		HtmlSnapshotFormatter form = m_Ctx.getFormatter();
		
		sb.append(form.getCellPrefix());		
		sb.append(form.getValuesPrefix());
		for (int i=0; i < count; i++){
			if (i != 0){
				sb.append(m_Ctx.getFormatter().getValueSeparator());
			}
			if (values != null){
				sb.append(m_NumFormat.format(values[i]));	
			} else {
				sb.append(form.getEmptyCellText());
			}			
		}
		sb.append(form.getValuesPostfix());
		sb.append(form.getCellPostfix());
	}
	
	private void snapshotNow(){
    	synchronized(m_Monitors){
    		if (m_Ctx.getSnapshotFileName() != null){
	    		m_Buffer.setLength(0);    		
				makeSnapshot(m_Buffer, -1);						
				try {
					BufferedWriter out = new BufferedWriter(
						new FileWriter(m_Ctx.getSnapshotFileName(), false)
					);
			        out.write(m_Buffer.toString());
			        out.close();
				} catch(IOException ie){
					m_Trace.error("Error while writing snapshot to disk.", ie);
				}
    		}
    	}
	}
	
	private void collectNow(){
    	synchronized(m_Monitors){
        	
    		m_Collect++;
    		
    		// calculate current value string
    		m_Buffer.setLength(0); 
    		for (int i=0; i < m_Monitors.size(); i++){
    			IPerfMonitor mon = (IPerfMonitor) m_Monitors.get(i);
    			long [] values = null;    			
    			if (mon.hasValues()){
    				values = mon.getValues();
    				if (values == null || values.length != mon.getColumnNames().length){
    					values = null;
    					m_Trace.error("Error collectinbg data from monitor, number of values does not match number of columns.", new RuntimeException());
    				}
    			}    			
    			renderValues(m_Buffer, mon.getColumnNames().length, values);
    		}
    		
    		// update history
    		String perf = m_Buffer.toString();
    		if (m_OldPerf == null || !m_OldPerf.equals(perf)){
    			m_OldPerf = perf;
    			
    			// add current timestamp
    			m_Buffer.setLength(0);
    			
    			HtmlSnapshotFormatter form = m_Ctx.getFormatter();
    			
    			m_Buffer.append(form.getCellPrefix());
    			m_Buffer.append(m_DateFormat.format(new Date()));
    			m_Buffer.append(form.getCellPostfix());
    			m_Buffer.append(perf);
    			
    			String row = m_Buffer.toString();
    			
    			// add to history
	    		m_History.add(row);
				if (m_History.size() >= m_Ctx.getMaxHistoryRows()){
					m_History.remove(0);
				}
								
				m_Sequence++;
    		}
    	}
	}
	
	public IPerfMonitor [] enumMonitors() {
		synchronized(m_Monitors){
			return (IPerfMonitor []) m_Monitors.toArray(new IPerfMonitor [] {});
		}
	}
	
	protected void afterAddMonitor(IPerfMonitor monitor){
		Iterator iter = m_Listeners.iterator();		
		while(iter.hasNext()){
			((IPerfAgentListener) iter.next()).afterAddMonitor(monitor);
		}
	}
	
	protected void afterRemoveMonitor(IPerfMonitor monitor){
		Iterator iter = m_Listeners.iterator();		
		while(iter.hasNext()){
			((IPerfAgentListener) iter.next()).afterRemoveMonitor(monitor);
		}
	}
		
}
