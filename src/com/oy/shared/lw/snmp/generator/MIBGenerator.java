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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.oy.shared.lw.misc.ITrace;
import com.oy.shared.lw.snmp.tree.OIDTree;
import com.oy.shared.lw.snmp.tree.OIDTreeLeaf;
import com.oy.shared.lw.snmp.tree.OIDTreeNode;

public class MIBGenerator {    

	private List leafs = new ArrayList();
	private List groups = new ArrayList();
	
    private final OIDTree mibTreeRoot;    
    private final ITrace trace;
    private final String namespace;
    
    public MIBGenerator(ITrace trace, OIDTree mibTreeRoot, String namespace) {
        this.trace = trace;
        this.mibTreeRoot = mibTreeRoot;
        this.namespace = namespace;
    }

    public void generate(StringBuffer sb) {
        trace.debug("Generating MIB content");
        
        leafs.clear();
        groups.clear();
        
        sb.append(MIBContentFormatter.formatMIBModuleHeader(
    		namespace, 
    		mibTreeRoot.getName(), mibTreeRoot.getContactInfo(), 
    		mibTreeRoot.getEnterprisesOID()
		));

        Enumeration broadFirstEnumerator = mibTreeRoot.getBroadFirstEnumerator();
        while (broadFirstEnumerator.hasMoreElements()) {
            Object element = broadFirstEnumerator.nextElement();
            if (element instanceof OIDTreeNode) {
                if (isPregeneratedNodeBeforeLocalRoot(element)) {
                    continue;
                }
                
                OIDTreeNode node = (OIDTreeNode) element;
                String parentNodeName = getParentNodeName(node);
                String name = createUniqueNameFor(namespace, node.getName(), groups, false);
                
                node.setName(name);
                
                sb.append(MIBContentFormatter.formatMIBGroup(
            		node.getOid(), name, parentNodeName
        		));                    
            } else if (element instanceof OIDTreeLeaf) {
            	
            	OIDTreeLeaf leaf = (OIDTreeLeaf) element; 
            	String parentNodeName = getParentNodeName(leaf);
            	
            	OIDTreeNode parentNode = mibTreeRoot.getParentNode(leaf);
            	String name = createUniqueNameFor(namespace, parentNode.getName() + "_" + leaf.getName(), leafs, true);
            	
            	sb.append(MIBContentFormatter.formatMIBLeaf(
        			leaf.getOid(), name, parentNodeName, leaf.getValueSource()
        		));
            } else {
            	throw new RuntimeException("Error generating MIB content.");
            }
        }
        sb.append(MIBContentFormatter.formatMIBModuleFooter());
        
            trace.debug("Generating MIB content done");
    }

    private boolean isPregeneratedNodeBeforeLocalRoot(Object element) {
        String oid = ((OIDTreeNode) element).getOid();
        boolean result = (oid.length() < mibTreeRoot.getOid().length() - 1 && mibTreeRoot.getOid().startsWith(oid));
        return result;
    }

    private String getParentNodeName(OIDTreeLeaf element) {
        OIDTreeNode parentNode = mibTreeRoot.getParentNode(element);
        String parentNodeName;
        if (parentNode == null) {
            parentNodeName = mibTreeRoot.getName();
        } else {
            parentNodeName = parentNode.getName();
        }
        return MIBContentFormatter.formatMIBIdentifier(parentNodeName, namespace, false);
    }
  
    private static String createUniqueNameFor(String prefix, String name, List list, boolean isLeaf){
    	name = MIBContentFormatter.formatMIBIdentifier(name, prefix, isLeaf);
    	
        int counter = 0;
        String newName = name;
        while (list.contains(newName)) {
            newName = name + "_" + counter++;
        }
        list.add(newName);
        
        return newName;
    }
    
}
