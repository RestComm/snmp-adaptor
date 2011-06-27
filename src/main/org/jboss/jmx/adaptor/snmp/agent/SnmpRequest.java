package org.jboss.jmx.adaptor.snmp.agent;

import org.jboss.logging.Logger;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.mp.StateReference;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.smi.OctetString;

public class SnmpRequest implements CommandResponder {
	
	/** Logger object */
	protected Logger log;
	
	/** the request handler instance handling get/set requests */
	private RequestHandler requestHandler;
	
	
	// the local engine ID for our CommandResponder. This is needed for
	// v3 communication
	private OctetString localContextEngineID;

	public SnmpRequest(RequestHandler requestHandler, Logger log){
		this.requestHandler = requestHandler;
		this.log = log;
	}
	// constructor with engine ID as well
	public SnmpRequest(RequestHandler requestHandler, Logger log, OctetString ceID){
		this.requestHandler = requestHandler;
		this.log = log;		
		this.localContextEngineID = ceID;
	}
	
	public void setRequestHandler(RequestHandler requestHandler){
		this.requestHandler = requestHandler;
	}
	
	public RequestHandler getRequestHandler(){
		return this.requestHandler;
	}

	@Override
	public void processPdu(CommandResponderEvent event){
		PDU pdu = event.getPDU();

		if (pdu == null) {
			log.warn("Null request PDU received ... skipping");
			return;
		}
			
		if(log.isDebugEnabled()) {
			log.debug("Received Snmp request of type: "+PDU.getTypeString(pdu.getType()));
		}
		int type = pdu.getType();
		
		PDU response = null;
		//switch based on pdu.getType() == ___
			switch (type) {
			case PDU.GET:
			case PDU.GETNEXT:
				response = requestHandler.snmpReceivedGet(pdu);
				break;
			case PDU.GETBULK:
				response = requestHandler.snmpReceivedGetBulk(pdu);
				break;
			case PDU.SET:
				response = requestHandler.snmpReceivedSet(pdu);
				break;
			default:
				log.warn("Cannot process request PDU of type: " + 
						PDU.getTypeString(type) + "unsupported");
				return;
			}
			if (response != null) {
				
				//VERY IMPORTANT LINE
			    response.setRequestID(event.getPDU().getRequestID());
				try {
					sendResponse(event, response);
				} catch (MessageException e) {
					log.warn("Response may not have been sent correctly. " +
							"An error occured snmp4j message processing: " +
							e.getMessage());
				}
			}	
	}
	
	private void sendResponse(CommandResponderEvent requestEvent, PDU response) throws MessageException{
		
//		if (response.getBERLength() > requestEvent.getMaxSizeResponsePDU()) {
//			// response is tooBig
//			if (response.getType() != PDU.REPORT) {
//				if (requestEvent.getPDU().getType() == PDU.GETBULK) {
//					while ((response.size() > 0) &&
//					   (response.getBERLength() >
//					    requestEvent.getMaxSizeResponsePDU())) 
//					{
//						response.trim();
//					}
//				}
//				else {
//					response.clear();
//					response.setRequestID(requestEvent.getPDU().getRequestID());
//					response.setErrorStatus(PDU.tooBig);
//				}
//			}
//			if (response.getBERLength() > requestEvent.getMaxSizeResponsePDU()) {
//				//still too big and can't send a response
//				return;
//			}
//		}
//
//		StatusInformation status = new StatusInformation();
//		StateReference stateRef = requestEvent.getStateReference();
//		if (stateRef == null) {
//			log.warn("No state reference available for requestEvent="+	
//		    requestEvent+". Cannot return response="+response);
//		}
//		else {
//			stateRef.setTransportMapping(requestEvent.getTransportMapping());
//			int resp = requestEvent.getMessageDispatcher().returnResponsePdu(requestEvent.getMessageProcessingModel(),
//			               requestEvent.getSecurityModel(),
//			               requestEvent.getSecurityName(),
//			               requestEvent.getSecurityLevel(),
//			               response,
//			               requestEvent.getMaxSizeResponsePDU(),
//			               stateRef,
//			               status);
//		}
	
		response.setErrorIndex(0);
		response.setErrorStatus(0);
		response.setType(PDU.RESPONSE);
	    StatusInformation statusInformation = new StatusInformation();
	    StateReference ref = requestEvent.getStateReference();
	    requestEvent.getMessageDispatcher().returnResponsePdu(requestEvent.
	                                                 getMessageProcessingModel(),
	                                                 requestEvent.getSecurityModel(),
	                                                 requestEvent.getSecurityName(),
	                                                 requestEvent.getSecurityLevel(),
	                                                 response,
	                                                 requestEvent.getMaxSizeResponsePDU(),
	                                                 ref,
	                                                 statusInformation);
	}
	
	public OctetString getLocalEngineID(){
		return this.localContextEngineID;
	}
}
