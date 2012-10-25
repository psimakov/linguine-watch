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


package com.oy.shared.lw.snmp.agent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.net.SocketException;

import snmp.SNMPBadValueException;
import snmp.SNMPGetException;
import snmp.SNMPObject;
import snmp.SNMPObjectIdentifier;
import snmp.SNMPPDU;
import snmp.SNMPRequestException;
import snmp.SNMPRequestListener;
import snmp.SNMPSequence;
import snmp.SNMPSetException;
import snmp.SNMPVariablePair;

import snmp.OYSNMServer;

import com.oy.shared.lw.misc.ITrace;
import com.oy.shared.lw.misc.PoliteThread;
import com.oy.shared.lw.perf.IPerfMonitor;

import com.oy.shared.lw.snmp.ISNMPAgent;
import com.oy.shared.lw.snmp.IValueGroup;
import com.oy.shared.lw.snmp.IValueSource;
import com.oy.shared.lw.snmp.generator.MIBGenerator;
import com.oy.shared.lw.snmp.rfc.ErrorStatus;
import com.oy.shared.lw.snmp.rfc.RequestType;
import com.oy.shared.lw.snmp.tree.OIDTree;
import com.oy.shared.lw.snmp.tree.OIDTreeLeaf;


public class SNMPAgent implements SNMPRequestListener, ISNMPAgent {

	class ServerThread extends PoliteThread {

        public void run() {

            int numChars;
            char[] charArray = new char[256];

            try {
                while (!isTerminated()) {
                	                	
                	// read error stream if can
                	numChars = errorReader.read(charArray, 0, charArray.length);
                	if (numChars == -1){
                		break;
                	}
                	
                	// push error stream to log
                    StringBuffer errorMessage = new StringBuffer();
                    errorMessage.append("Problem receiving request:");
                    errorMessage.append(new String(charArray, 0, numChars));
                    
                    trace.debug(errorMessage.toString());
                }
            }catch(InterruptedIOException ie){
            	// ignore
            } catch (IOException e) {
                trace.error("Problem processing error stream!", e);
            }
        }
    }
       
    public static final int DEFAULT_SNMP_SERVER_PORT = 161;        
    public static final int SNMP_VERSION = 1;
    
    private String communityName;
    private static final int RECEIVE_BUFFER_SIZE = 5120;

    private ITrace trace;
    private OYSNMServer server;

    private PipedReader errorReader;
    private ServerThread readerThread;

    final OIDTree mibTreeRoot;
    private boolean isRunning;
    private int portNumber;
    private SNMPAgentContext ctx;

    public SNMPAgent(SNMPAgentContext ctx) throws Exception {
    	this(new ITrace.MockTraceEmptyImpl(), ctx);
    }
    
    public SNMPAgent(ITrace trace, SNMPAgentContext ctx) throws Exception {
        this.portNumber = ctx.getPort();
        this.trace = trace;
        this.communityName = ctx.getCommunityString();
        this.ctx = ctx;

        this.ctx.setCanModify(false);
        
        mibTreeRoot = new OIDTree(
    		trace, ctx.getOrganizationName(), ctx.getContactInfo(), ctx.getEnterprisesOID() 
		);        

        // reader thread
        errorReader = new PipedReader();
        PipedWriter errorWriter = new PipedWriter(errorReader);
        readerThread = new ServerThread();
        readerThread.start();
        trace.debug("Error reader for SNMP Server started");
        
        // snmp agent
        server = new OYSNMServer(
    		SNMP_VERSION, 
    		portNumber, 
    		new PrintWriter(errorWriter)
		);
        server.addRequestListener(this);
        server.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);
        server.startReceiving();
        isRunning = true;
        
        trace.info("SNMP Agent Started");        
    }

    public void stop() {
        if (!isRunning) {
        	throw new RuntimeException("Error stopping, not running.");
        }

        readerThread.terminate();
        readerThread = null;

        try {
            errorReader.close();
        } catch (IOException ie) {
            trace.error("An exception has occured while stopping SNMP Daemon", ie);
        }

        try {
            server.stopReceiving();
        } catch (SocketException e) {
            trace.error("An exception has occured while stopping SNMP Daemon", e);
        }
        isRunning = false;

        this.ctx.setCanModify(true);
        
        trace.info("SNMP Agent Stopped");
    }
    
    public IPerfMonitor getMonitor(){
    	return server.getMonitor();
    }
    
    public int getEnterprisesOID(){
    	return ctx.getEnterprisesOID();
    }
    
    private void logRequest(SNMPPDU pdu, String communityName) {
        trace.debug("SNMP request:");
        trace.debug("\tcommunity name:\t" + communityName);
        trace.debug("\trequest ID:\t" + pdu.getRequestID());
        trace.debug("\ttype:\t" + RequestType.getRequest(pdu.getPDUType()));
    }

    private SNMPRequestException updateException(int i, SNMPRequestException e) {
        trace.error("An exception has occured while fetching value from a ValueSource", e);
        e.errorIndex = i + 1;
        return e;
    }

    private static boolean isOIDScalar(SNMPObjectIdentifier snmpOID) {
        return snmpOID.toString().endsWith(".0");
    }

    private static SNMPObjectIdentifier cutTrailingZerro(SNMPObjectIdentifier snmpOID) {
        try {
            return new SNMPObjectIdentifier(snmpOID.toString().substring(0, snmpOID.toString().lastIndexOf('.')));
        } catch (SNMPBadValueException e) {
        	throw new RuntimeException("Error trailing zero.");
        }
    }
        
    public String getCommunityName() {
        return communityName;
    }    
    
    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }    
    
	public void generateMIBFile(String fileName) throws IOException {
		StringBuffer sb = new StringBuffer();
		generateMIBFile(sb);
		
		BufferedWriter out = new BufferedWriter(
			new FileWriter(fileName, false)
		);			
		try {
			out.write(sb.toString());
		} finally {
			out.close();
		}
	}
	
    public void generateMIBFile(StringBuffer sb){
    	if (!isRunning) {
        	throw new RuntimeException("Error stopping, not running.");
        }
    	
    	synchronized (mibTreeRoot) {
	    	MIBGenerator gen = new MIBGenerator(trace, mibTreeRoot, ctx.getNamespace());
	    	gen.generate(sb);
    	}
    }

    public SNMPSequence processRequest(SNMPPDU pdu, String communityName) throws SNMPGetException, SNMPSetException {
        synchronized (mibTreeRoot) {
            logRequest(pdu, communityName);
            
            SNMPSequence responseList = new SNMPSequence();
            if (!this.communityName.equals(communityName)) {
                trace.debug("Community name doesn't match");
                return responseList;
            }

            SNMPSequence varBindList = pdu.getVarBindList();
            for (int i = 0; i < varBindList.size(); i++) {

                SNMPSequence variablePair = (SNMPSequence) varBindList.getSNMPObjectAt(i);
                SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier) variablePair.getSNMPObjectAt(0);
                SNMPObject snmpValue = variablePair.getSNMPObjectAt(1);

                trace.debug("\t" + snmpOID + " -> " + snmpValue);

                if (isOIDScalar(snmpOID)) {
                    snmpOID = cutTrailingZerro(snmpOID);
                }

                OIDTreeLeaf foundNode = mibTreeRoot.getChildByOID(snmpOID.toString());
                if (foundNode != null) {
                    try {
                        SNMPVariablePair executeResult = foundNode.execute(RequestType.getRequest(pdu.getPDUType()), snmpValue);
                        responseList.addSNMPObject(executeResult);
                    } catch (SNMPGetException e) {
                        throw (SNMPGetException) updateException(i, e);
                    } catch (SNMPSetException e) {
                        throw (SNMPSetException) updateException(i, e);
                    } catch (SNMPBadValueException e) {
                        trace.error(e);
                        throw new RuntimeException("Error processing request.");
                    }
                }else{
                    trace.debug("OID \"" + snmpOID.toString() + "\" was not found in the tree");
                }
            }
            
            trace.debug("SNMP Response:");
            for (int i = 0; i < responseList.size(); i++) {
                SNMPVariablePair pair = (SNMPVariablePair) responseList.getSNMPObjectAt(i);
                trace.debug("\t" + pair.getSNMPObjectAt(0) + " -> " + pair.getSNMPObjectAt(1));
            }
            trace.debug("End of request processing");
            
            return responseList;
        }
    }

    public SNMPSequence processGetNextRequest(SNMPPDU pdu, String communityName) throws SNMPGetException {
        synchronized (mibTreeRoot) {
            logRequest(pdu, communityName);
            SNMPSequence responseList = new SNMPSequence();
            if (!communityName.equals(this.communityName)) {
                trace.debug("Community name doesn't match");
                return responseList;
            }

            SNMPSequence varBindList = pdu.getVarBindList();
            for (int i = 0; i < varBindList.size(); i++) {

                SNMPSequence variablePair = (SNMPSequence) varBindList.getSNMPObjectAt(i);
                SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier) variablePair.getSNMPObjectAt(0);
                SNMPObject snmpValue = variablePair.getSNMPObjectAt(1);

                trace.debug("\t" + snmpOID + " -> " + snmpValue);

                SNMPObjectIdentifier suppliedSNMPOID = snmpOID;
                if (isOIDScalar(snmpOID)) {
                    snmpOID = cutTrailingZerro(snmpOID);
                }

                OIDTreeLeaf foundNode = mibTreeRoot.getChildByOID(snmpOID.toString());
                if (foundNode != null) {
                    OIDTreeLeaf nextNode = mibTreeRoot.getNext(foundNode);
                    if (nextNode != null) {
                        foundNode = nextNode;
                    } else {
                        throw new SNMPGetException(i, ErrorStatus.noSuchName.getValue());
                    }
                }
                if (foundNode != null) {
                    try {
                        SNMPVariablePair innerPair = foundNode.execute(RequestType.SNMP_GET_REQUEST, snmpValue);
                        SNMPVariablePair outerPair = new SNMPVariablePair(suppliedSNMPOID, innerPair);
                        responseList.addSNMPObject(outerPair);
                    } catch (SNMPGetException e) {
                        updateException(i, e);
                        throw e;
                    } catch (Exception e) {
                        trace.error(e);
                        throw new RuntimeException("Error processing request.");
                    }
                }
            }
            trace.debug("SNMP Response:");
            for (int i = 0; i < responseList.size(); i++) {
                SNMPVariablePair outerPair = (SNMPVariablePair) responseList.getSNMPObjectAt(i);
                SNMPVariablePair innerPair = (SNMPVariablePair) outerPair.getSNMPObjectAt(1);
                trace.debug(outerPair.getSNMPObjectAt(0) + " -> " + innerPair.getSNMPObjectAt(0) + " -> " + innerPair.getSNMPObjectAt(1));
            }
            trace.debug("End of request processing");
            return responseList;
        }
    }

    public boolean contains(String oid) {
    	synchronized (mibTreeRoot) {
    		return mibTreeRoot.contains(oid);
    	}
    }
    
    public void add(String absoluteOID, IValueGroup group) {
    	synchronized (mibTreeRoot) {
    		mibTreeRoot.addGroup(absoluteOID, group);
    	}
    }

    public void add(String absoluteOID, IValueSource source) {
    	synchronized (mibTreeRoot) {
    		mibTreeRoot.addValueSource(absoluteOID, source);
    	}        
    }

    public void remove(String absoluteOID) {
    	synchronized (mibTreeRoot) {
    		mibTreeRoot.remove(absoluteOID);
    	}
    }
}