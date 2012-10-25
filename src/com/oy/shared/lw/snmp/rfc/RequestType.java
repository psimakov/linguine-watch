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


package com.oy.shared.lw.snmp.rfc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RequestType {

	public static List list = new ArrayList();
	
    public static RequestType SNMP_GET_REQUEST = new RequestType((byte)0xA0, "GET");
    public static RequestType SNMP_GETNEXT_REQUEST = new RequestType((byte)0xA1, "GETNEXT");
    public static RequestType SNMP_GETRESPONSE = new RequestType((byte)0xA2, "GETRESPONSE");
    public static RequestType SNMP_SET_REQUEST = new RequestType((byte)0xA3, "SET");
    public static RequestType SNMP_TRAP = new RequestType((byte)0xA4, "TRAP");
    

    private final byte type;
    private String description;

    private RequestType(byte type, String description) {
        this.type = type;
        this.description = description;
        
        list.add(this);
    }

    public static RequestType getRequest(int type){
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            RequestType element = (RequestType) iter.next();
            if(element.getValue() == type){
                return element;
            }
        }
        throw new RuntimeException("Error getting request type.");
    }
    
    public int getValue() {
        return type;
    }
    
    public String toString(){
        return description;
    }

}
