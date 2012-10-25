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


package com.oy.shared.lw.perf.agent;

public class QOSCalculator {

	// bucket sizes; bucket is not renewed unless these are exceeded 
	private long b_UnitsOfWork;
	private long b_Age;

	// values at the start of the latest bucket 
	private long old_completed;
	private long old_failed;
	private long old_time;
	
	//
	// completed, failed = number of completed and failed units of work (always growth)
	// unitsOfWorkPerBusket = number of completed + failed units of work that triggers start of new busket
	// ageOfBucket = number of millis that triggers start of new busket
    //  
	public QOSCalculator(long completed, long failed, long unitsOfWorkPerBusket, long ageOfBucket){
		old_completed = completed;
		old_failed = failed;
		b_UnitsOfWork = unitsOfWorkPerBusket;
		b_Age = ageOfBucket;

		old_time = System.currentTimeMillis();
	}
	
	private long qosNow(long completed, long failed){			
		try {		
			long completedNow = completed - old_completed;
			long failedNow = failed - old_failed;
			
			long qosNow;		
			if (completed < 0 || failed < 0 || completedNow < 0 || failedNow < 0){
				qosNow = -1;
			} else {
				if (completedNow + failedNow == 0){
					qosNow = 100;
				} else {
					qosNow = Math.round(100 - (100 * (double) failedNow) / ((double) (completedNow + failedNow)));  
				}			
			}
			
			return qosNow;
		} catch(Exception e){		
			return -1;
		}
	}
	
	public long getQOS(long completed, long failed){
		
		long qos = qosNow(completed, failed);		
					
		if (qos != -1){
			
			// check if we need to starts new bucket
			long bucketNow = (completed - old_completed) + (failed - old_failed);
			long now = System.currentTimeMillis();
			long ageNow = now - old_time;
			
			if (bucketNow >= b_UnitsOfWork || ageNow >= b_Age){
				old_completed = completed;
				old_failed = failed;			
				old_time = now;
			}
		}
		
		return qos;
	}	
			
}
