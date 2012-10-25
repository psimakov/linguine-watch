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

import java.util.Date;

public interface ITrace {
	
	public final static int NONE = -1, ERROR = 0, INFO = 1, DEBUG = 2;
	
	public void error(Throwable t);
	public void error(String message, Throwable t);   
    public void info(String message);           
    public void debug(String message);
	
    class MockTraceEmptyImpl implements ITrace {
    	public void error(Throwable t){}
    	public void error(String message, Throwable t){}   
        public void info(String message){}          
        public void debug(String message){}
    }
    
    class MockTraceOutImpl implements ITrace {
    	
    	private String clazz;
    	private int level;
    	
    	public MockTraceOutImpl(Class clazz, int level){
   			this.clazz = Util.getShortClassName(clazz);
   			this.level = level;
		}
    	    	
    	public synchronized void error(String message, Throwable t){
    		if (level >= ERROR){
	    		System.out.println(new Date());
	    		
	    		head("ERROR", t.getClass().getName());
	    		if(message != null && !message.equals("")){
	    			System.out.println("Custom message: " + message);
	    		}
				message = t.getMessage();
				if(message == null || message.equals("")){
					System.out.println("Exception message: " + message);
				}
	
				System.out.println("Sack trace:");
	    		t.printStackTrace(System.out);
    		}
    	}
    	
    	public void error(Throwable t){
    		if (level >= ERROR){
    			error(null, t);
    		}
		}
    	
        public synchronized void info(String message){
        	if (level >= INFO){
        		head("INFO", message);
        	}
    	}
        
        public synchronized void debug(String message) {
        	if (level >= DEBUG){
        		head("DEBUG", message);
        	}
        }
        
        private void head(String kind, String message){
        	System.out.println(
    			new Date().toString() + '\t' +
				clazz + '\t' +
				kind + '\t' +
				message
			);
        }
    
    }
}
