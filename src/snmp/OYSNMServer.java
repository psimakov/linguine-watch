/*
 
     This file is an exact copy of SNMPv1AgentInterface.java with a patch
     for a clean UDP socket close.  
  
 */

package snmp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.Vector;

import com.oy.shared.lw.perf.IPerfMonitor;
import com.oy.shared.lw.perf.agent.BaseMonitor;

public class OYSNMServer implements Runnable {
	
	public class SNMPServerMonitor extends BaseMonitor {
		
		private long receivedSNMPRequestCounter;
		private long sentSNMPResponseCounter;
		private long errors;
		private long wasStartedAt;
		private long snmpBadValueException;
        
        private long [] values = new long []{0, 0, 0, 0, 0};

        public SNMPServerMonitor (){
        	super(OYSNMServer.class, "SNMP SERVER", "Monitors internal state of SNMP Agent",
    			new String []{"received", "sent", "errors", "uptime", "bad value"}
        	);
        }
        
        public long [] getValues(){
        	values[0] = receivedSNMPRequestCounter;
        	values[1] = sentSNMPResponseCounter;
        	values[2] = errors;
        	values[3] = (System.currentTimeMillis() - wasStartedAt)/1000;
        	values[4] = snmpBadValueException;
        	
        	return values;
        }
    }

	
    public static int snmp_port = 161;

    // largest size for datagram packet payload; based on
    // RFC 1157, need to handle messages of at least 484 bytes
    public int receiveBufferSize = 512;

    int version = 0;

    private DatagramSocket dSocket;
    private Thread receiveThread;
    private Vector listenerVector;
    private PrintWriter errorLog;
    private boolean stopFlag;
    private SNMPServerMonitor monitor = new SNMPServerMonitor();
   
    /**
     * Construct a new agent object to listen for requests from remote SNMP
     * managers. The agent listens on the standard SNMP UDP port 161.
     */

    public IPerfMonitor getMonitor(){
    	return monitor;
    }
    
    public OYSNMServer(int version) throws SocketException {
        this(version, snmp_port, new PrintWriter(System.out));
    }

    /**
     * Construct a new agent object to listen for requests from remote SNMP
     * managers. The agent listens on the supplied port.
     */

    public OYSNMServer(int version, int localPort) throws SocketException {
        this(version, localPort, new PrintWriter(System.out));
    }

    /**
     * Construct a new agent object to listen for requests from remote SNMP
     * managers. The agent listens on the supplied port, and sends error
     * messages to the specified PrintWriter.
     */

    public OYSNMServer(int version, PrintWriter errorReceiver) throws SocketException {
        this(version, snmp_port, errorReceiver);
    }

    /**
     * Construct a new agent object to listen for requests from remote SNMP
     * managers. The agent listens on the supplied port, and sends error
     * messages to the specified PrintWriter.
     */

    public OYSNMServer(int version, int localPort, PrintWriter errorReceiver) throws SocketException {

        this.version = version;

        dSocket = new DatagramSocket(localPort);

        listenerVector = new Vector();

        receiveThread = new Thread(this);

        errorLog = errorReceiver;

    }

    /**
     * Set the specified PrintWriter to receive error messages.
     */

    public void setErrorReceiver(PrintWriter errorReceiver) {
        errorLog = errorReceiver;
    }

    public void addRequestListener(SNMPRequestListener listener) {
        // see if listener already added; if so, ignore
        for (int i = 0; i < listenerVector.size(); i++) {
            if (listener == listenerVector.elementAt(i)) {
                return;
            }
        }

        // if got here, it's not in the list; add it
        listenerVector.add(listener);
    }

    public void removeRequestListener(SNMPRequestListener listener) {
        // see if listener in list; if so, remove, if not, ignore
        for (int i = 0; i < listenerVector.size(); i++) {
            if (listener == listenerVector.elementAt(i)) {
                listenerVector.removeElementAt(i);
                break;
            }
        }

    }

    /**
     * Start listening for requests from remote managers.
     */

    public void startReceiving() {
        // if receiveThread not already running, start it
        if (!receiveThread.isAlive()) {
            receiveThread = new Thread(this);
            receiveThread.start();
            monitor.wasStartedAt = System.currentTimeMillis();
        }
    }

    /**
     * Stop listening for requests from remote managers.
     */

    public void stopReceiving() throws SocketException {
        // interrupt receive thread so it will die a natural death
        stopFlag = true;
        dSocket.close();
    }

    /**
     * The run() method for the agent interface's listener. Just waits for SNMP
     * request messages to come in on port 161 (or the port supplied in the
     * constructor), then dispatches the retrieved SNMPPDU and community name to
     * each of the registered SNMPRequestListeners by calling their
     * processRequest() methods.
     */

    public void run() {
        while (!stopFlag) {

            try {

                DatagramPacket inPacket = new DatagramPacket(new byte[receiveBufferSize], receiveBufferSize);

                dSocket.receive(inPacket);

                InetAddress requesterAddress = inPacket.getAddress();
                int requesterPort = inPacket.getPort();

                byte[] encodedMessage = inPacket.getData();

                SNMPMessage receivedMessage = new SNMPMessage(SNMPBERCodec.extractNextTLV(encodedMessage, 0).value);

                String communityName = receivedMessage.getCommunityName();
                SNMPPDU receivedPDU = receivedMessage.getPDU();
                byte requestPDUType = receivedPDU.getPDUType();

                // System.out.println("Received message; community = " +
                // communityName + ", pdu type = " +
                // Byte.toString(requestPDUType));
                // System.out.println(" read community = " + readCommunityName +
                // ", write community = " + writeCommunityName);

                SNMPSequence requestedVarList = receivedPDU.getVarBindList();

                Hashtable variablePairHashtable = new Hashtable();
                SNMPSequence responseVarList = new SNMPSequence();
                int errorIndex = 0;
                int errorStatus = SNMPRequestException.NO_ERROR;
                int requestID = receivedPDU.getRequestID();
                monitor.receivedSNMPRequestCounter++;

                try {

                    // pass the received PDU and community name to the
                    // processRequest method of any listeners;
                    // handle differently depending on whether the request is a
                    // get-next, or a get or set

                    if ((requestPDUType == SNMPBERCodec.SNMPGETREQUEST) || (requestPDUType == SNMPBERCodec.SNMPSETREQUEST)) {

                        // pass the received PDU and community name to any
                        // registered listeners
                        for (int i = 0; i < listenerVector.size(); i++) {
                            SNMPRequestListener listener = (SNMPRequestListener) listenerVector.elementAt(i);

                            // return value is sequence of variable pairs for
                            // those OIDs handled by the listener
                            SNMPSequence handledVarList = listener.processRequest(receivedPDU, communityName);

                            // add to Hashtable of handled OIDs, if not already
                            // there
                            for (int j = 0; j < handledVarList.size(); j++) {

                                SNMPSequence handledPair = (SNMPSequence) handledVarList.getSNMPObjectAt(j);
                                SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier) handledPair.getSNMPObjectAt(0);
                                SNMPObject snmpObject = (SNMPObject) handledPair.getSNMPObjectAt(1);

                                if (!variablePairHashtable.containsKey(snmpOID)) {
                                    variablePairHashtable.put(snmpOID, snmpObject);
                                }

                            }

                        }

                        // construct response containing the handled OIDs; if
                        // any OID not handled, throw exception
                        for (int j = 0; j < requestedVarList.size(); j++) {
                            SNMPSequence requestPair = (SNMPSequence) requestedVarList.getSNMPObjectAt(j);
                            SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier) requestPair.getSNMPObjectAt(0);

                            // find corresponding SNMP object in hashtable
                            if (!variablePairHashtable.containsKey(snmpOID)) {
                                errorIndex = j + 1;
                                errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;

                                if (requestPDUType == SNMPBERCodec.SNMPGETREQUEST)
                                    throw new SNMPGetException("OID " + snmpOID + " not handled", errorIndex, errorStatus);
                                else
                                    throw new SNMPSetException("OID " + snmpOID + " not handled", errorIndex, errorStatus);
                            }

                            SNMPObject snmpObject = (SNMPObject) variablePairHashtable.get(snmpOID);
                            SNMPVariablePair responsePair = new SNMPVariablePair(snmpOID, snmpObject);

                            responseVarList.addSNMPObject(responsePair);

                        }

                    } else if (requestPDUType == SNMPBERCodec.SNMPGETNEXTREQUEST) {
                        // pass the received PDU and community name to any
                        // registered listeners
                        for (int i = 0; i < listenerVector.size(); i++) {
                            SNMPRequestListener listener = (SNMPRequestListener) listenerVector.elementAt(i);

                            // return value is sequence of nested variable pairs
                            // for those OIDs handled by the listener:
                            // consists of (supplied OID, (following OID,
                            // value)) nested variable pairs
                            SNMPSequence handledVarList = listener.processGetNextRequest(receivedPDU, communityName);

                            // add variable pair to Hashtable of handled OIDs,
                            // if not already there
                            for (int j = 0; j < handledVarList.size(); j++) {

                                SNMPSequence handledPair = (SNMPSequence) handledVarList.getSNMPObjectAt(j);
                                SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier) handledPair.getSNMPObjectAt(0);
                                SNMPObject snmpObject = (SNMPObject) handledPair.getSNMPObjectAt(1);

                                if (!variablePairHashtable.containsKey(snmpOID)) {
                                    variablePairHashtable.put(snmpOID, snmpObject);
                                }

                            }

                        }

                        // construct response containing the handled OIDs; if
                        // any OID not handled, throw exception
                        for (int j = 0; j < requestedVarList.size(); j++) {
                            SNMPSequence requestPair = (SNMPSequence) requestedVarList.getSNMPObjectAt(j);
                            SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier) requestPair.getSNMPObjectAt(0);

                            // find corresponding SNMP object in hashtable
                            if (!variablePairHashtable.containsKey(snmpOID)) {
                                errorIndex = j + 1;
                                errorStatus = SNMPRequestException.VALUE_NOT_AVAILABLE;

                                throw new SNMPGetException("OID " + snmpOID + " not handled", errorIndex, errorStatus);
                            }

                            // value in hashtable is complete variable pair
                            SNMPVariablePair responsePair = (SNMPVariablePair) variablePairHashtable.get(snmpOID);

                            responseVarList.addSNMPObject(responsePair);

                        }

                    } else {
                        // some other PDU type; silently ignore
                        continue;
                    }

                } catch (SNMPRequestException e) {
                    // exception should contain the index and cause of error;
                    // return this in message
                    errorIndex = e.errorIndex;
                    errorStatus = e.errorStatus;

                    // just return request variable list as response variable
                    // list
                    responseVarList = requestedVarList;
                    monitor.errors++;
                } catch (Exception e) {
                    // don't have a specific index and cause of error; return
                    // message as general error, index 0
                    errorIndex = 0;
                    errorStatus = SNMPRequestException.FAILED;

                    // just return request variable list as response variable
                    // list
                    responseVarList = requestedVarList;

                    // also report the exception locally
                    errorLog.println("Exception while processing request: " + e.toString());
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    for (int i = 0; i < stackTrace.length; i++) {
                        errorLog.println(stackTrace[i].toString());
                    }
                    errorLog.flush();
                    monitor.errors++;
                }

                SNMPPDU pdu = new SNMPPDU(SNMPBERCodec.SNMPGETRESPONSE, requestID, errorStatus, errorIndex, responseVarList);
                SNMPMessage message = new SNMPMessage(version, communityName, pdu);
                byte[] messageEncoding = message.getBEREncoding();
                DatagramPacket outPacket = new DatagramPacket(messageEncoding, messageEncoding.length, requesterAddress, requesterPort);

                dSocket.send(outPacket);
                monitor.sentSNMPResponseCounter++;
            } catch (IOException e) {
                // just report the problem
                monitor.errors++;
                errorLog.println("IOException during request processing: " + e.getMessage());
                errorLog.flush();
            } catch (SNMPBadValueException e) {
                // just report the problem
                monitor.snmpBadValueException++;
                errorLog.println("SNMPBadValueException during request processing: " + e.getMessage());
                errorLog.flush();
            } catch (Exception e) {
                monitor.errors++;
                // just report the problem
                errorLog.println("Exception during request processing: " + e.toString());
                errorLog.flush();
            }

        }
    }

    /**
     * Set the size of the buffer used to receive response packets. RFC 1157
     * stipulates that an SNMP implementation must be able to receive packets of
     * at least 484 bytes, so if you try to set the size to a value less than
     * this, the receive buffer size will be set to 484 bytes. In addition, the
     * maximum size of a UDP packet payload is 65535 bytes, so setting the
     * buffer to a larger size will just waste memory. The default value is 512
     * bytes. The value may need to be increased if get-requests are issued for
     * multiple OIDs.
     */

    public void setReceiveBufferSize(int receiveBufferSize) {
        if (receiveBufferSize >= 484) {
            this.receiveBufferSize = receiveBufferSize;
        } else {
            this.receiveBufferSize = 484;
        }
    }

    /**
     * Returns the current size of the buffer used to receive response packets.
     */

    public int getReceiveBufferSize() {
        return this.receiveBufferSize;
    }

    public static int getSnmp_port() {
        return snmp_port;
    }

    public static void setSnmp_port(int snmp_port) {
        OYSNMServer.snmp_port = snmp_port;
    }
}
