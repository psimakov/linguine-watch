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

package com.oy.shared.lw.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Properties;

public class CmdOptions {

	/**
	 * This method converts String [] args in main() method into the list of properties.
	 * The args must be in the form of A=123 B=567 C="fdfsdf f sdf"; 
	 * the names will be capitalized
	 */
	public static Properties parseFromMainArgs(String [] args){
		Properties props = new Properties();		
		if (args != null && args.length != 0){
			for (int i=0; i < args.length; i++){
				int idx = args[i].indexOf("=");
				if (idx == -1){
					throw new RuntimeException("Failed to parse property " + args[i]);
				}
				String name = args[i].substring(0, idx);
				String value = "";
				if (args[i].length() > idx + 1){
					value = args[i].substring(idx + 1);
				}
				props.put(name.toUpperCase(), value);
			}
		}
		return props;
	}
	
	
	/** 
	 * This method will set object fields from the properties;
	 * for the property myclass.name=boo the "myclass" is the namespace, the "name"
	 * is the field name that will be set on Object via reflection
	 */
	public static void applyPropertiesToObjectFields(ITrace trace, String namespace, Properties props, Object object){
		try {
			Field [] all = object.getClass().getDeclaredFields();
			for (int i=0; i < all.length; i++){
				
				all[i].setAccessible(true);
				Class type = all[i].getType();
				
				String name = namespace + "." + all[i].getName();				
				if (props.containsKey(name)){
					String value = props.getProperty(name).trim();
					Object param = null;
					try {
						if (type.equals(boolean.class)){							
							param = 
								("YES".equals(value.toUpperCase()) || "TRUE".equals(value.toUpperCase())) ? 
								new Boolean(true) : 
								new Boolean(false);
						}
						if (type.equals(int.class)){
							param = Integer.valueOf(value);
						}
						if (type.equals(long.class)){
							param = Long.valueOf(value);
						}
						if (type.equals(String.class)){
							param = value;
						}
						if (param == null){
							throw new RuntimeException("No conversion function for " + type.getClass().getName());
						}
					} catch(Exception e){
						throw new RuntimeException(
							"Failed to parse property \"" + name + "\" value \"" + value + 
							"\" to type \"" + type.getClass().getName() + "\""
						);
					}
					
					all[i].set(object, param);
					trace.info("Applied property value \"" + value + "\" to \"" + name + "\"");
				}
			}	
		} catch(Exception e){
			throw new RuntimeException("Failed to set properties for " + namespace, e);
		}
	}	
	
	/**
	 * This method waits for user input on the console, if user types password
	 * this method exist, if not the msg is printed out and the thing continues. 
	 */
	public static void waitForUserToAbort(String msg, String password) throws IOException {
		InputStreamReader unbuffered = new InputStreamReader(System.in);
	    BufferedReader keyboard = new BufferedReader(unbuffered);
	    while(true){	    	
	        String ln = keyboard.readLine();
	        if (ln != null && ln.equals(password)) {
	            break;
	        } else {
	        	System.err.println(msg);
	        }
	    }
	}
	
}
