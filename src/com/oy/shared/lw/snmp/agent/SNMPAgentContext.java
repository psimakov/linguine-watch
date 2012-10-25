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

package com.oy.shared.lw.snmp.agent;

final public class SNMPAgentContext {
	
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

	private boolean canModify = true;
	
	public SNMPAgentContext(){
		setPort(SNMPAgent.DEFAULT_SNMP_SERVER_PORT);
		setEnterprisesOID(1);
		setOrganizationName("linguine watch");
		setContactInfo("http://www.softwaresecretweapons.com");
		setCommunityString("public");
		setNamespace("oylw_");
	}
	
	void setCanModify(boolean value){
		canModify = value;
	}
	
	private void assertCanModify(){
		if (!canModify){
			throw new IllegalStateException("Can't modify context after agent has started.");
		}
	}
	
	public int getPort(){
		return port;
	}
	
	public void setPort(int value){
		assertCanModify();
		port = value;
	}
	
	public int getEnterprisesOID(){
		return enterprisesOID;
	}
	
	public void setEnterprisesOID(int value){
		assertCanModify();
		enterprisesOID = value;
	}
	
	public String getOrganizationName(){
		return organizationName;
	}
	
	public void setOrganizationName(String value){
		assertCanModify();
		organizationName = value;
	}

	public String getContactInfo(){
		return contactInfo;
	}
	
	public void setContactInfo(String value){
		assertCanModify();
		contactInfo = value;
	}
	
	public String getCommunityString(){
		return communityString;
	}
	
	public void setCommunityString(String value){
		assertCanModify();
		communityString = value;
	}
		
	public String getNamespace(){
		return namespace;
	}
	
	public void setNamespace(String value){
		assertCanModify();
		namespace = value;
	}
}
