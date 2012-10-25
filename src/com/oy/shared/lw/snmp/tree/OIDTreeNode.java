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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class OIDTreeNode extends OIDTreeLeaf {
    List list = new ArrayList();

    public OIDTreeNode(String name, String oid) {
        super(name, oid);
    }

    public Iterator iterator() {
        return list.iterator();
    }

    public void add(OIDTreeLeaf node) {
        list.add(node);
        Collections.sort(list);
    }

    public void remove(OIDTreeLeaf oidNode) {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            OIDTreeLeaf child = (OIDTreeLeaf) iter.next();
            if (child == oidNode) {
                iter.remove();
            }
        }
    }

    public boolean contains(OIDTreeLeaf node) {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            OIDTreeLeaf child = (OIDTreeLeaf) iter.next();
            if (child.equals(node)) {
                return true;
            }
        }
        return false;
    }

    public int getChildCount() {
        return list.size();
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

        OIDTreeNode other = (OIDTreeNode) oth;
        if (this.list == null) {
            if (other.list != null) {
                return false;
            }
        } else {
            if (!this.list.equals(other.list)) {
                return false;
            }
        }
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

        return true;
    }

    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;
        if (list != null) {
            result = PRIME * result + list.hashCode();
        }
        if (oid != null) {
            result = PRIME * result + oid.hashCode();
        }
        if (name != null) {
            result = PRIME * result + name.hashCode();
        }

        return result;
    }

    public OIDTreeLeaf getChildByOID(String oid) {
        OIDTreeLeaf treeLeaf = super.getChildByOID(oid);
        if (treeLeaf != null) {
            return treeLeaf;
        }

        for (Iterator iter = list.iterator(); iter.hasNext();) {
            OIDTreeLeaf child = (OIDTreeLeaf) iter.next();
            OIDTreeLeaf result = child.getChildByOID(oid);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    int getAllChildCount() {
        int counter = 0;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            OIDTreeLeaf child = (OIDTreeLeaf) iter.next();
            counter += child.getAllChildCount();
            counter++;
        }
        return counter;
    }

    public String toString() {
        return "[group]" + oid + "\t" + name + "\t" ;
    }

}
