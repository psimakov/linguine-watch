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


package com.oy.shared.lw.snmp.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import snmp.SNMPBadValueException;
import snmp.SNMPGetException;
import snmp.SNMPObject;
import snmp.SNMPObjectIdentifier;
import snmp.SNMPSetException;
import snmp.SNMPVariablePair;

import com.oy.shared.lw.snmp.IValueSource;
import com.oy.shared.lw.snmp.rfc.ErrorStatus;
import com.oy.shared.lw.snmp.rfc.RequestType;

public class OIDTreeLeaf implements Comparable {
    private static final String SCALAR_TYPE_SUFIX = ".0";
    String oid;
    String name;
    private IValueSource valueSource;

    public OIDTreeLeaf(String name, String oid) {
        this(name, oid, null);
    }

    public OIDTreeLeaf(String name, String oid, IValueSource valueSource) {
        if (!isOIDValid(oid)) {
            throw new IllegalArgumentException("OID \"" + oid + "\" is invalid. It can only contain digits separated by dots");
        }
        this.name = name;
        this.oid = oid;
        this.valueSource = valueSource;
    }

    static boolean isOIDValid(String oid) {
        if(oid.endsWith(".0")){
            return false;
        }
        StringTokenizer tokenizer = new StringTokenizer(oid, ".");
        while (tokenizer.hasMoreTokens()) {
            try {
                if(Integer.parseInt(tokenizer.nextToken())<0){
                    return false;
                }
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        return true;
    }

    public String getOid() {
        return oid;
    }

    public IValueSource getValueSource() {
        return valueSource;
    }

    public String toString() {
        return oid + "\t" + name + "\t[" + valueSource.getType().getTypeName() + "]";
    }

    public String getName() {
        return name;
    }
 
    public Iterator iterator() {
        return new ArrayList().iterator();
    }

    int getAllChildCount() {
        return 0;
    }

    public SNMPVariablePair execute(RequestType operationType, SNMPObject snmpValue) throws SNMPGetException, SNMPSetException {
        if (operationType == RequestType.SNMP_GET_REQUEST) {
            try {
                return new SNMPVariablePair(getSNMPObjectIdentifier(), valueSource.get());
            } catch (Throwable e) {
                throw new SNMPGetException(e.toString(), 1, ErrorStatus.badValue.getValue());
            }
        } else if (operationType == RequestType.SNMP_SET_REQUEST) {
            throw new SNMPSetException("Operation is unavailable", 1, ErrorStatus.readOnly.getValue());
        }
        throw new SNMPGetException("Operation is unavailable", 1, ErrorStatus.genErr.getValue());
    }

    private SNMPObjectIdentifier getSNMPObjectIdentifier() {
        try {
            return new SNMPObjectIdentifier(oid + SCALAR_TYPE_SUFIX);
        } catch (SNMPBadValueException e) {
            throw new RuntimeException("Error getting object identifier.");
        }
    }

    public boolean contains(OIDTreeLeaf node) {
        return false;
    }

    public OIDTreeLeaf getChildByOID(String oid) {
        if (this.oid.equals(oid)) {
            return this;
        }
        return null;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth == null) {
            return false;
        }

        if (oth.getClass() != getClass()) {
            return false;
        }

        OIDTreeLeaf other = (OIDTreeLeaf) oth;
        if (this.oid == null) {
            if (other.oid != null) {
                return false;
            }
        } else {
            if (!this.oid.equals(other.oid)) {
                return false;
            }
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else {
            if (!this.name.equals(other.name)) {
                return false;
            }
        }
        if (this.valueSource == null) {
            if (other.valueSource != null) {
                return false;
            }
        } else {
            if (!this.valueSource.equals(other.valueSource)) {
                return false;
            }
        }

        return true;
    }

    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;
        if (oid != null) {
            result = PRIME * result + oid.hashCode();
        }
        if (name != null) {
            result = PRIME * result + name.hashCode();
        }
        if (valueSource != null) {
            result = PRIME * result + valueSource.hashCode();
        }

        return result;
    }

    public int compareTo(Object o) {
        if (o instanceof OIDTreeLeaf) {
            int thisVal = getSumOfAllDigits();
            int anotherVal = ((OIDTreeLeaf) o).getSumOfAllDigits();
            return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
        } else {
            return 0;
        }
    }

    private int getSumOfAllDigits() {
        StringTokenizer tokenizer = new StringTokenizer(oid, ".");
        int result = 0;
        while (tokenizer.hasMoreTokens()) {
            result += Integer.parseInt(tokenizer.nextToken());
        }
        return result;
    }

    public void setName(String name) {
       this.name = name;
    }

}
