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

package com.oy.shared.lw.snmp.generator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.oy.shared.lw.snmp.IValueSource;

public class MIBContentFormatter {
		
	private static final String header = "-MIB DEFINITIONS ::= BEGIN\n" + "\n" + "      IMPORTS\n" + "              OBJECT-TYPE\n" + "                      FROM RFC-1212;\n";
	
	private static final String mibParentName = "enterprises";
	
    private static final String footer = "\nEND";
	
	private static SimpleDateFormat mibDateFormat = new SimpleDateFormat(
		"yyyyMMddHHmm");
	
	private static DateFormat humanDateFormat = new SimpleDateFormat(
		"EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
	
    /**
     * an ASN.1 identifier consists of one or more letters or digits, and its
     * initial character must be a lower-case letter.
     */
    public static String formatMIBIdentifier(String name, String prefix, boolean isLeaf) {
        name = name.trim();
        
        if (isLeaf){        
	        if (!name.startsWith(prefix)) {
	            name = prefix + name;
	        }
        }

        return formatMIBName(name);
    }
    
    public static String formatMIBName(String name){
    	StringBuffer sb = new StringBuffer(name);
        if (!isAlphaNumeric(sb.charAt(sb.length() - 1))) {
            sb.replace(sb.length() - 1, sb.length(), "_");
        }
        if (Character.isUpperCase(sb.charAt(0))) {
            sb.replace(0, 1, String.valueOf(sb.charAt(0)).toLowerCase());
        }
        for (int i = 0; i < sb.length(); i++) {
            if (!isAlphaNumeric(sb.charAt(i)) || Character.isWhitespace(sb.charAt(i))) {
                sb.replace(i, i + 1, "_");
            }
        }

        return sb.toString();	
    }
    

    public static String formatMIBDate() {
        return mibDateFormat.format(new Date()) + "Z";
    }
    
    public static String formatHumanDate() {
        return humanDateFormat.format(new Date());
    }    
    
    public static String formatMIBLeaf(String oid, String name, String parentNodeName, IValueSource source) {
        return 
			"--\n" +
			"-- Value definition\n" +
			"--\n" +                    		
        	"\t-- " + oid + "\n" + 
        	"\t" + name + " OBJECT-TYPE\n" + 
        	"\tSYNTAX " + source.getType().getTypeName() + "\n" + 
        	"\tMAX-ACCESS read-only" + "\n" + 
        	"\tSTATUS current\n" + 
        	"\tDESCRIPTION\n" + "\t\t\"" + source.getDescription() + "\"\n" + 
        	"\t::= { " + parentNodeName + " " + oid.substring(oid.lastIndexOf('.') + 1) + " }\n";
    }

    public static String formatMIBGroup(String oid, String name, String parentNodeName) {
        return 
			"--\n" +
			"-- Group definition\n" +
			"--\n" +                    		
        	"\t-- " + oid + "\n\t" + 
        	name + " OBJECT IDENTIFIER  ::= { " + parentNodeName + " " + oid.substring(oid.lastIndexOf('.') + 1) + " }\n";
    }    
    
    public static String formatMIBModuleHeader(String prefix, String organizationName, String contactInfo, int enterprisesOID){
    	return
	    	"MODULE-" +
	    	formatMIBName(organizationName).toUpperCase() +
	    	header +
	    	"\t" +
	    	MIBContentFormatter.formatMIBIdentifier(organizationName, prefix, false) +
	    	" MODULE-IDENTITY\n" +
	    	"\tLAST-UPDATED \"" +
	    	MIBContentFormatter.formatMIBDate() +
	    	"\"  --  " +
	    	MIBContentFormatter.formatHumanDate() +
	    	"\n\tORGANIZATION \"" +
	    	organizationName +
	    	"\"\n\tCONTACT-INFO \"" +
	    	contactInfo +
	    	"\"\n\tDESCRIPTION \"\"\n\t::= {" +
	    	mibParentName +
	    	" " +
	    	enterprisesOID +
	    	" }\n";
    }
    
    public static String formatMIBModuleFooter(){
    	return footer;
    }
    
    private static boolean isAlphaNumeric(char c) {
        if ("_".indexOf(c) != -1) {
            return true;
        }

        boolean blnNumeric = false;
        boolean blnAlpha = false;

        if (c >= '0' && c <= '9') {
            blnNumeric = true;
        }
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
            blnAlpha = true;
        }
        return (blnNumeric || blnAlpha);
    }    

}