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


package com.oy.shared.lw.snmp;

import java.io.IOException;

public interface ISNMPAgent {

	public static final String DEFAULT_MGMT_OID_PATH = "1.3.6.1.4.1";
	 
	public int getEnterprisesOID();
	
	// add groups here, groups have values as children;
	// for each group added there must be a containing group added
	// all the way to DEFAULT_MGMT_OID_PATH
    public void add(String absoluteOID, IValueGroup group);
    
    // add values here, for each value added there must be 
    // a containing group added
    public void add(String absoluteOID, IValueSource source);
    
    public void remove(String absoluteOID);
    
    public boolean contains(String absoluteOID);
    
    // MIB file with all currently defined groups and values can
    // be prepared at any time; various details of this file 
    // content can be configured in SNMPAgentContext
	public void generateMIBFile(String fileName) throws IOException ;
    public void generateMIBFile(StringBuffer sb);
 
    class MockSNMPAgent implements ISNMPAgent {
    	public int getEnterprisesOID(){ return 1; }
        public void add(String absoluteOID, IValueGroup group){}
        public void add(String absoluteOID, IValueSource source){}
        public void remove(String absoluteOID){}
        public boolean contains(String absoluteOID){ return false; }
    	public void generateMIBFile(String fileName) throws IOException {}
        public void generateMIBFile(StringBuffer sb) {}
    }
}
