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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import com.oy.shared.lw.misc.ITrace;
import com.oy.shared.lw.snmp.ISNMPAgent;
import com.oy.shared.lw.snmp.IValueGroup;
import com.oy.shared.lw.snmp.IValueSource;

public class OIDTree extends OIDTreeNode {

    private final ITrace trace;
    private String contactInfo;
    private int enterprisesOID;

    public OIDTree(ITrace trace, String name, String contactInfo, int enterprisesOID) {
        super(name, ISNMPAgent.DEFAULT_MGMT_OID_PATH + "." + enterprisesOID);
        
        this.contactInfo = contactInfo;        
        this.trace = trace;
        this.enterprisesOID= enterprisesOID;
    }
    
    public int getEnterprisesOID(){
    	return enterprisesOID;
    }
    
    public String getContactInfo(){
    	return contactInfo;
    }
    
    static String cutTrailingDigit(String oid) {
        return oid.substring(0, oid.lastIndexOf('.'));
    }

    void dumpTree(ITrace trace) {
        trace.debug(this.toString());
        dumpTree(this, " ", trace);
    }

    private void dumpTree(OIDTreeLeaf node, String spaces, ITrace trace) {
        for (Iterator iter = node.iterator(); iter.hasNext();) {
            OIDTreeLeaf treeNode = (OIDTreeLeaf) iter.next();
            trace.debug(spaces + treeNode.toString());
            dumpTree(treeNode, spaces + "    ", trace);
        }
    }

    private List expandTree(OIDTreeLeaf node, List list) {
        for (Iterator iter = node.iterator(); iter.hasNext();) {
            OIDTreeLeaf treeNode = (OIDTreeLeaf) iter.next();
            list.add(treeNode);
            expandTree(treeNode, list);
        }
        return list;
    }

    public Enumeration getBroadFirstEnumerator() {
        ArrayList list = new ArrayList();
        expandTree(this, list);
        return new Enumerator(list);
    }

    public OIDTreeLeaf getNext(OIDTreeLeaf node) {
        List wholeTreeList = expandTree(this, new ArrayList());

        boolean nodeFound = false;
        for (Iterator iter = wholeTreeList.iterator(); iter.hasNext();) {
            OIDTreeLeaf element = (OIDTreeLeaf) iter.next();
            if (element == node) {
                nodeFound = true;
                continue;
            }
            if(nodeFound && !(element instanceof OIDTreeNode)){
                return element;
            }
        }
        return null;
    }

    public void addValueSource(String oid, IValueSource source) {
        trace.debug("Adding new ValueSource " + source + " to OID \"" + oid + "\"");
        add(oid, source);
        trace.debug("New ValueSource was added to OID " + oid);
    }

    public void addGroup(String oid, IValueGroup group) {
        trace.debug("Adding new group " + group + " to OID \"" + oid + "\"");
        add(oid, group);
        trace.debug("New group was added to OID " + oid);
    }

    private OIDTreeLeaf add(String oid, Object object) {
        OIDTreeLeaf fetchedNode = getChildByOID(oid);
        if (fetchedNode != null && !(fetchedNode instanceof OIDTreeStub && object instanceof IValueGroup)) {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Trying to add " + object + " to already registered OID \"" + oid + "\" with " +  fetchedNode );
            trace.error(illegalArgumentException);
            throw illegalArgumentException;
        }else if(fetchedNode instanceof OIDTreeStub && object instanceof IValueGroup){
            fetchedNode.setName(((IValueGroup)object ).getName());
            return fetchedNode;
        }
        OIDTreeLeaf newNode = createNewNode(oid, object);
        OIDTreeNode parentNode = getParentNode(newNode);
        parentNode.add(newNode);
        return newNode;
    }

    private OIDTreeLeaf createNewNode(String oid, Object object) {
        if (object instanceof OIDTreeRoot) {
            return new OIDTreeStub(((IValueGroup) object).getName(), oid);
        }else if (object instanceof IValueGroup) {
            return new OIDTreeNode(((IValueGroup) object).getName(), oid);
        } else if (object instanceof IValueSource) {
            return new OIDTreeLeaf(((IValueSource) object).getName(), oid, ((IValueSource) object));
        } else {
            throw new RuntimeException("Error creating new node.");
        }
    }

    public void remove(String oid) {
        trace.debug("Removing OID \"" + oid + "\"");

        OIDTreeLeaf fetchedNode = getChildByOID(oid);
        if (fetchedNode == null) {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("There is no OID \"" + oid + "\"");
            trace.error(illegalArgumentException);
            throw illegalArgumentException;
        }
        this.getParentNode(fetchedNode).remove(fetchedNode);
        dumpTree(trace);
        trace.debug("OID \"" + oid + "\" was removed");
    }

    public OIDTreeNode getParentNode(OIDTreeLeaf node) {
        if (hasParent(node)) {
            String parentOID = OIDTree.cutTrailingDigit(node.getOid());
            OIDTreeNode parentNode = (OIDTreeNode) getChildByOID(parentOID);
            if (parentNode == null) {
                return (OIDTreeNode) add(parentOID, new OIDTreeRoot());
            }
            return parentNode;
        } else {
            return this;
        }
    }

    private boolean hasParent(OIDTreeLeaf node) {
        return node.getOid().indexOf('.') != -1;
    }

    private class Enumerator implements Enumeration {

        private List list;
        private int position;

        public Enumerator(List list) {
            this.list = list;
        }

        public boolean hasMoreElements() {
            return position < list.size();
        }

        public Object nextElement() {
            return list.get(position++);
        }

    }
    
    public boolean contains(String oid) {
        return 
        	getChildByOID(oid) != null 
        		&& 
        	!(getChildByOID(oid) instanceof OIDTreeStub);
    }
}
