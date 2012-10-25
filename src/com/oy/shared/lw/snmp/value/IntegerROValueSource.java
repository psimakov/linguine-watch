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


package com.oy.shared.lw.snmp.value;

import com.oy.shared.lw.snmp.rfc.ValueType;

import snmp.SNMPInteger;
import snmp.SNMPObject;

public abstract class IntegerROValueSource extends BaseROValueSource {

    public IntegerROValueSource(String name, String desc) {
        super(name, desc);
    }

    public final ValueType getType() {
        return ValueType.SNMPINTEGER;
    }

    public final SNMPObject get() {
        return new SNMPInteger(getValue());
    }

    public abstract long getValue();

}
