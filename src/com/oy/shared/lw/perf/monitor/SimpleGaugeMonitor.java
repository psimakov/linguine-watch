package com.oy.shared.lw.perf.monitor;

import com.oy.shared.lw.perf.agent.BaseMonitor;

final public class SimpleGaugeMonitor extends BaseMonitor {
	
	private long [] m_Values = new long []{0};
	private long [] m_Clone = new long []{0};
	
	public SimpleGaugeMonitor(Class module, String name, String desc){
		super(module, name, desc, new String [] {"gauge"});
	}
	
	public synchronized void setValue(long value){
		m_Values[0] = value;
	}
	
	public synchronized long [] getValues(){
		return getValues(m_Values, m_Clone);
	}
	
}