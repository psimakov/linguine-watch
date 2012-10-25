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

public class ValueType {
    
    public static final ValueType SNMPINTEGER = new ValueType((byte)0x02, "INTEGER");
    public static final ValueType SNMPBITSTRING = new ValueType((byte)0x03, "BITSTRING");
    public static final ValueType SNMPOCTETSTRING = new ValueType((byte)0x04, "OCTET STRING");
    public static final ValueType SNMPNULL = new ValueType((byte)0x05, "NULL");
    public static final ValueType SNMPOBJECTIDENTIFIER = new ValueType((byte)0x06, "OBJECTIDENTIFIER");
    public static final ValueType SNMPSEQUENCE = new ValueType((byte)0x30, "SEQUENCE");
    
    public static final ValueType SNMPIPADDRESS = new ValueType((byte)0x40, "IPADDRESS");
    public static final ValueType SNMPCOUNTER32 = new ValueType((byte)0x41, "COUNTER32");
    public static final ValueType SNMPGAUGE32 = new ValueType((byte)0x42, "GAUGE32");
    public static final ValueType SNMPTIMETICKS = new ValueType((byte)0x43, "TimeTicks");
    public static final ValueType SNMPOPAQUE = new ValueType((byte)0x44, "OPAQUE");
    public static final ValueType SNMPNSAPADDRESS = new ValueType((byte)0x45, "NSAPADDRESS");
    public static final ValueType SNMPCOUNTER64 = new ValueType((byte)0x46, "COUNTER64");
    public static final ValueType SNMPUINTEGER32 = new ValueType((byte)0x47, "UINTEGER32");
   
    private final byte typeCode;
    private String typeName;

    private ValueType(byte type, String description) {
        this.typeCode = type;
        this.typeName = description;
    }
    
    public byte getTypeCode(){
    	return typeCode;
    }

    public String getTypeName(){
        return typeName;
    }

}
