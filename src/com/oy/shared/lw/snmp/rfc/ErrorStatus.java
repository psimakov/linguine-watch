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

public final class ErrorStatus {
	
    public static ErrorStatus noError = new ErrorStatus(0);
    public static ErrorStatus tooBig = new ErrorStatus(1);
    public static ErrorStatus noSuchName = new ErrorStatus(2);
    public static ErrorStatus badValue = new ErrorStatus(3);
    public static ErrorStatus readOnly = new ErrorStatus(4);
    public static ErrorStatus genErr = new ErrorStatus(5);
    
    private final int value;

    private ErrorStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
