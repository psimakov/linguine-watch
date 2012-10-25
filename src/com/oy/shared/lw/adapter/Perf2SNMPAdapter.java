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


package com.oy.shared.lw.adapter;

import java.util.HashMap;
import java.util.Map;

import com.oy.shared.lw.misc.ITrace;
import com.oy.shared.lw.perf.IPerfAgent;
import com.oy.shared.lw.perf.IPerfAgentListener;
import com.oy.shared.lw.perf.IPerfMonitor;
import com.oy.shared.lw.snmp.ISNMPAgent;
import com.oy.shared.lw.snmp.IValueGroup;
import com.oy.shared.lw.snmp.IValueSource;
import com.oy.shared.lw.snmp.agent.BaseValueGroup;
import com.oy.shared.lw.snmp.value.IntegerROValueSource;

public class Perf2SNMPAdapter implements IPerfAgentListener {
   
    class ValueAdapter extends IntegerROValueSource {

    	private IPerfMonitor counter;
    	private int index;
    	
        public ValueAdapter(IPerfMonitor counter, int index) {
        	super(
    			counter.getColumnNames()[index], 
    			"[" + counter.getColumnNames()[index] + "] " + counter.getDesc() 
			);
        	
            this.counter = counter;
            this.index = index;
        }

        public long getValue(){
            return counter.getValues()[index];
        }
    }
    
    private Map m_Groups = new HashMap();
    
    private ITrace m_Trace;
    
    private IPerfAgent m_PAgent;
    private ISNMPAgent m_SAgent;
    private IPerfMonitor m_SMonitor;
    private Perf2SNMPAdapterContext m_Ctx;
    
    public Perf2SNMPAdapter(Perf2SNMPAdapterContext ctx) {
    	this(new ITrace.MockTraceEmptyImpl(), ctx);
    }
    
    public Perf2SNMPAdapter(ITrace trace, Perf2SNMPAdapterContext ctx) {
        m_Trace = trace; 
        m_Ctx = ctx;
    }
    
    public void connect(IPerfAgent pAgent, ISNMPAgent sAgent, IPerfMonitor sMonitor){
    	synchronized(m_Groups){
	        m_SAgent = sAgent;
	        m_PAgent = pAgent;
	        m_SMonitor = sMonitor;
	        	    
	        m_PAgent.addMonitor(m_SMonitor);
	        
	        m_PAgent.addListener(this);
	        
	        IPerfMonitor [] all = m_PAgent.enumMonitors();
	        for (int i=0; i < all.length; i++){
	        	afterAddMonitor(all[i]);
	        }
    	}
    }

    public void disconnect(){
    	synchronized(m_Groups){
    		
    		m_PAgent.removeListener(this);
    		
    		m_PAgent.removeMonitor(m_SMonitor);
    		
    		IPerfMonitor [] all = m_PAgent.enumMonitors();
	        for (int i=0; i < all.length; i++){
	        	afterRemoveMonitor(all[i]);
	        }
    		
	    	m_SAgent = null;
	        m_PAgent = null;
	        m_SMonitor = null;
    	}
    }
    
    private String getOIDForPerfCounter(IPerfMonitor perfCounter) {
        return 
        	getOIDForPerfCounterModule(perfCounter) + "." +
        	m_Ctx.convertNameToOID(perfCounter.getName());
    }

    private String getOIDForPerfCounterModule(IPerfMonitor perfCounter) {
        return 
        	ISNMPAgent.DEFAULT_MGMT_OID_PATH + "." + 
        	m_SAgent.getEnterprisesOID() + "." + 
        	m_Ctx.convertNameToOID(perfCounter.getModule());
    }

    public void afterAddMonitor(IPerfMonitor counter) {
    	synchronized(m_Groups){
	        IValueGroup moduleGroup = new BaseValueGroup(counter.getModule().toLowerCase());
	        IValueGroup counterGroup = new BaseValueGroup(counter.getName().toLowerCase());
	
	        String[] columns = counter.getColumnNames();
	        for (int i = 0; i < columns.length; i++) {
	        	IValueSource source = new ValueAdapter(counter, i);
	            String oid = getOIDForPerfCounter(counter) + "." + (i + 1);          
	            registerPerfCounter(oid, source);
	        }
	
	        try{
	            registerPerfGroup(getOIDForPerfCounter(counter), counterGroup);
	            registerPerfGroup(getOIDForPerfCounterModule(counter), moduleGroup);
	        }catch (IllegalArgumentException e) {
	            m_Trace.error("Error while registering perfomance counter group with name \"" + counter.getName() + "\"", e);
	            throw e;
	        }
	        
	        m_Groups.put(counter, counterGroup);
    	}
    }
    
    public void afterRemoveMonitor(IPerfMonitor counter) {
    	synchronized(m_Groups){
	        IValueGroup group = (IValueGroup) m_Groups.get(counter);
	        if (group == null){
	        	m_Trace.error("Error while unregistering perfomance counter group with name \"" + counter.getName() + "\"", new RuntimeException());
	        } else {
		        m_SAgent.remove(getOIDForPerfCounter(counter));
		        m_Groups.remove(counter);
	        }
    	}
    }    

    private void registerPerfGroup(String oid, IValueGroup scopeGroup) {
        if(!m_SAgent.contains(oid)){
            m_SAgent.add(oid, scopeGroup);
        }
    }

    private void registerPerfCounter(String oid, IValueSource source) {
        m_SAgent.add(oid, source);
    }

}
