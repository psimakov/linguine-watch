package com.oy.shared.lw.adapter;

public class Perf2SNMPAdapterContext {

	/**
	 * 
	 * This function is used to convert name of the module/class/counter into OID.
	 * Provide other function that works for you or hard code OID here if needed.
	 * The OID of a specific name will not change unless you change the name.
	 * 
	 * The only reason to have custom mapping here if some of counter names accidently 
	 * produce the same OID code and you are not allowed to change any of the names.  
	 * 
	 */
	public int convertNameToOID(String name){
        int oid = 0;
        
        if (name != null) {
            int offset = 0;
            char val[] = name.toCharArray();
            for (int i = 0; i < name.length(); i++) {
                oid = oid + val[offset++];
            }
        }
        
        if (oid <= 0) {
            oid = 1;
        }

        return oid;
	}
	
}
