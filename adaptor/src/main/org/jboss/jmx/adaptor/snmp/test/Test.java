package org.jboss.jmx.adaptor.snmp.test;

import java.io.IOException;
import java.net.UnknownHostException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class Test {
	
	public static void main(String[] args){
		
		//original oid "1.2.3.4.5"
		if (args[0].equals("get")){
			get(args);
		}	
		else if (args[0].equals("getnext")){
			getnext(args);
		}
		else if (args[0].equals("getbulk")){
			getbulk();
		}
		else if (args[0].equals("set")){
			set(args);
		}
		else if (args[0].equals("getv3")){
			getv3();
		}
		else if (args[0].equals("testnullpdu")){
			testnull();
		}
		else 
		{
			System.out.println("*Usage*\n" +
					           "<get/getnext> <oid>\n" +
							   "<set> <oid> <value>\n" +
							   "<getbulk> (test snmpReceivedGetBulk)\n" +
							   "<testnullpdu> (test sending a null pdu)\n");
		
			//improper action
		}
}	
	   public static void getbulk(){
			PDU pdu = new PDU();
			pdu.setType(PDU.GETBULK);
			pdu.add(new VariableBinding(new OID("1.2.3.4.1.1")));
			pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.0")));
			pdu.add(new VariableBinding(new OID("1.3.1.1")));
			pdu.setMaxRepetitions(7);
			pdu.setNonRepeaters(1);
			CommunityTarget target = new CommunityTarget();
			OctetString community = new OctetString("public");
			target.setCommunity(community);
			target.setVersion(SnmpConstants.version2c);
			Address targetAddress = GenericAddress.parse("udp:127.0.0.1/1161");
			target.setAddress(targetAddress);
			target.setRetries(0);
			target.setTimeout(2000);
		
			try {
				DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
				transport.listen();
				System.out.println("READY: "+System.currentTimeMillis());
				Snmp snmp = new Snmp(transport);
				long t1 = System.currentTimeMillis();
				System.out.println("SENDING: "+t1);
				System.out.println("PDU: "+pdu);
				ResponseEvent responseEvent = snmp.send(pdu, target);
				long t2=System.currentTimeMillis();
				System.out.println("SENT: "+t2);
				System.out.println("ELAPSED: "+(t2-t1));
				System.out.println("response " + responseEvent.toString());

				PDU responsePDU = responseEvent.getResponse();
				if (responsePDU == null){
					System.out.println("Request timed out");
				}
				else{
					System.out.println("Received response "+responsePDU);
				}
					System.out.println("Peer Address: "+responseEvent.getPeerAddress());
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e) {
				System.out.println("Some Other exception!!");
			}
		   
		   
	   }
	
   public static void getv3(){
	   ScopedPDU pdu = new ScopedPDU();
	   pdu.add(new VariableBinding(new OID("1.3.6")));
	   pdu.setType(PDU.GETNEXT);
	   Address targetAddress = GenericAddress.parse("udp:127.0.0.1/1161");
	   UserTarget target = new UserTarget();
	   target.setVersion(SnmpConstants.version3);
	   target.setAddress(targetAddress);
	   target.setRetries(2);
	   target.setTimeout(10000);
	   target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
	   target.setSecurityName(new OctetString("MD5DES"));
	   
		try {
			DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
			transport.listen();
			System.out.println("READY: "+System.currentTimeMillis());
 			Snmp snmp = new Snmp(transport);
			USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(), 0);
			SecurityModels.getInstance().addSecurityModel(usm);
			long t1 = System.currentTimeMillis();
			System.out.println("SENDING: "+t1);
			System.out.println("PDU: "+pdu);
			ResponseEvent responseEvent = snmp.send(pdu, target);
			long t2=System.currentTimeMillis();
			System.out.println("SENT: "+t2);
			System.out.println("ELAPSED: "+(t2-t1));
			System.out.println("response " + responseEvent.toString());

			PDU responsePDU = responseEvent.getResponse();
			if (responsePDU == null){
				System.out.println("Request timed out");
			}
			else{
				System.out.println("Received response "+responsePDU);
			}
				System.out.println("Peer Address: "+responseEvent.getPeerAddress());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			System.out.println("Some Other exception!!");
		}
	   

   }
	
   public static void getnext(String [] oids){
	ScopedPDU pdu = new ScopedPDU();
	pdu.setType(PDU.GETNEXT);
	for (int i =1; i < oids.length; i++){
		pdu.add(new VariableBinding(new OID(oids[i])));	
	}
	
	//CommunityTarget target = new CommunityTarget();
	//OctetString community = new OctetString("public");
	//target.setCommunity(community);
	//target.setVersion(SnmpConstants.version2c);
	UserTarget target = new UserTarget();
	target.setVersion(SnmpConstants.version3);
	Address targetAddress = GenericAddress.parse("udp:127.0.0.1/1161");
	target.setAddress(targetAddress);
	target.setRetries(2);
	target.setTimeout(2000);
	try {
		DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
		transport.listen();
		System.out.println("READY: "+System.currentTimeMillis());
		Snmp snmp = new Snmp(transport);
		long t1 = System.currentTimeMillis();
		System.out.println("SENDING: "+t1);
		System.out.println("PDU: "+pdu);
		ResponseEvent responseEvent = snmp.send(pdu, target);
		long t2=System.currentTimeMillis();
		System.out.println("SENT: "+t2);
		System.out.println("ELAPSED: "+(t2-t1));
		System.out.println("response " + responseEvent.toString());

		PDU responsePDU = responseEvent.getResponse();
		if (responsePDU == null){
			System.out.println("Request timed out");
		}
		else{
			System.out.println("Received response "+responsePDU);
		}
			System.out.println("Peer Address: "+responseEvent.getPeerAddress());
	} catch (UnknownHostException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	} catch (Exception e) {
		System.out.println("Some Other exception!!");
	}
   }
   
	public static void testnull(){
		   PDU pdu = null;
		   	   
		   CommunityTarget target = new CommunityTarget();
		   OctetString community = new OctetString("public");
		   target.setCommunity(community);
		   target.setVersion(SnmpConstants.version2c);
		   Address targetAddress = GenericAddress.parse("udp:127.0.0.1/1161");	   
		   target.setAddress(targetAddress);
		   target.setRetries(2);
		   target.setTimeout(2000);
		   try {
			    DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
				transport.listen();
				System.out.println("READY: "+System.currentTimeMillis());
				Snmp snmp = new Snmp(transport);
				//snmp.listen();
				long t1 = System.currentTimeMillis();
				System.out.println("SENDING: "+t1);
				System.out.println("PDU: "+pdu);
				ResponseEvent responseEvent = snmp.send(pdu, target);
				long t2=System.currentTimeMillis();
				System.out.println("SENT: "+t2);
				System.out.println("ELAPSED: "+(t2-t1));
				System.out.println("response " + responseEvent.toString());
				
				// extract the response PDU (could be null if timed out)
		        PDU responsePDU = responseEvent.getResponse();
		        if (responsePDU == null) {
	            System.out.println("Request timed out");
		        }
		        else {
		    //        System.out.println("Received response "+response);
		        }
				System.out.println("Peer Address: "+responseEvent.getPeerAddress());
				System.out.println("responsePdu = " + responsePDU);

		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			System.out.println("Some Other exception!!");
			System.out.println(e);
		}
		
	   }
	
   
	   /**
	    * Sends a test GET request
	    * 
	    * @jmx:managed-operation
	    */

	public static void get(String [] oids){
		   PDU pdu = new PDU();
		   pdu.setType(PDU.GET);
			for (int i =1; i < oids.length; i++){
				pdu.add(new VariableBinding(new OID(oids[i])));	
			}
		   	   
		   CommunityTarget target = new CommunityTarget();
		   target.setCommunity(new OctetString("public"));
		   target.setVersion(SnmpConstants.version2c);
		   Address targetAddress = GenericAddress.parse("udp:127.0.0.1/1161");	   
		   target.setAddress(targetAddress);
           target.setRetries(2);
           target.setTimeout(2000);

		   try {
				DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();

				transport.listen();
				System.out.println("READY: "+System.currentTimeMillis());
				Snmp snmp = new Snmp(transport);
				
				//snmp.listen();
				long t1 = System.currentTimeMillis();
				System.out.println("SENDING: "+t1);
				System.out.println("PDU: "+pdu);
				ResponseEvent responseEvent = snmp.send(pdu, target);
				long t2=System.currentTimeMillis();
				System.out.println("SENT: "+t2);
				System.out.println("ELAPSED: "+(t2-t1));
				System.out.println("response " + responseEvent.toString());
				
				// extract the response PDU (could be null if timed out)
		        PDU responsePDU = responseEvent.getResponse();
		        if (responsePDU == null) {
	            System.out.println("Request timed out");
		        }
		        else {
		    //        System.out.println("Received response "+response);
		        }
				System.out.println("Peer Address: "+responseEvent.getPeerAddress());
				System.out.println("responsePdu = " + responsePDU);

		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			System.out.println("Some Other exception!!" + e);
		}
		
	   }
	
	   public static void set(String []oids){
	       PDU pdu = new PDU();
	       for (int i = 1; i < oids.length; i+=2){
	   			pdu.add(new VariableBinding(new OID(oids[i]), new OctetString(oids[i+1])));	
	   		}
		//   OID oidn = new OID(oid);
		//   Variable var = new OctetString(value);
		//   VariableBinding varBind = new VariableBinding(oidn, var);
		//   pdu.add(varBind);
	       pdu.setType(PDU.SET);
	
	       CommunityTarget target = new CommunityTarget();
	       target.setCommunity(new OctetString("private"));
	       target.setVersion(SnmpConstants.version2c);
	       Address targetAddress = GenericAddress.parse("udp:127.0.0.1/1161");
	       target.setAddress(targetAddress);
           target.setRetries(2);
           target.setTimeout(1000);
		try{
		DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
		transport.listen();
		Snmp snmp = new Snmp(transport);	
		long t1 = System.currentTimeMillis();
                System.out.println("SENDING: "+t1);
		System.out.println("PDU: " + pdu);
                ResponseEvent responseEvent = snmp.set(pdu, target);
                long t2=System.currentTimeMillis();
                System.out.println("SENT: "+t2);
                System.out.println("ELAPSED: "+(t2-t1));
                System.out.println("response " + responseEvent.toString());
		
		PDU responsePDU = responseEvent.getResponse();
  
		if (responsePDU == null){
			System.out.println("Response is null. Check RequestHandlerImpl");
			System.out.println("Request timed out.");
		}
		else {
			System.out.println("Received response "+responsePDU);
                }
                        System.out.println("Peer Address: "+responseEvent.getPeerAddress());


		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			System.out.println("Some Other exception!!");
		}
		
	   }

}	
