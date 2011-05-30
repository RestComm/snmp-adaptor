/*
 * Copyright (c) 2003,  Intracom S.A. - www.intracom.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * This package and its source code is available at www.jboss.org
**/
package org.jboss.jmx.adaptor.snmp.agent;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.Notification;

import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.jmx.adaptor.snmp.config.manager.Manager;
import org.jboss.jmx.adaptor.snmp.config.user.User;
import org.jboss.logging.Logger;
import org.jboss.xb.binding.MappingObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * <tt>TrapEmitter</tt> is a class that manages SNMP trap emission.
 *
 * Currently, it allows to send V1 or V2 traps to one or more subscribed SNMP
 * managers defined by their IP address, listening port number and expected
 * SNMP version.
 *
 * @version $Revision: 110496 $
 *
 * @author  <a href="mailto:spol@intracom.gr">Spyros Pollatos</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
**/
public class TrapEmitter
{
   /** The logger object */
   private static final Logger log = Logger.getLogger(TrapEmitter.class);
   
   /** Reference to the utilised trap factory*/
   private TrapFactory trapFactory = null;
   
   private SnmpAgentService snmpAgentService;
   
   /** Holds the manager subscriptions. Accessed through synch'd wrapper */
   private Set managers = Collections.synchronizedSet(new HashSet());  
    
   /**
    * Builds a TrapEmitter object for sending SNMP V1 or V2 traps. <P>
   **/
   public TrapEmitter(SnmpAgentService snmpAgentService) {
      this.snmpAgentService = snmpAgentService;
   }
    
   /**
    * Complete emitter initialisation
   **/               
   public void start()
      throws Exception
   {
      // Load persisted manager subscriptions
      load();
      
      // Instantiate the trap factory
      this.trapFactory = (TrapFactory) Class.forName(this.snmpAgentService.getTrapFactoryClassName(),
                                                     true,
                                                     this.getClass().getClassLoader()).newInstance();
      
      // Initialise
      this.trapFactory.set(this.snmpAgentService.getNotificationMapResName(),
                           this.snmpAgentService.getClock(),
                           this.snmpAgentService.getTrapCounter());
      
      // Start the trap factory
      this.trapFactory.start();
   }
    
   /**
    * Perform shutdown
   **/
   public void stop()
      throws Exception
   {
      synchronized(this.managers) {
            
         // Drop all held manager records
         this.managers.clear();
      }
   }
    
   /**
    * Intercepts the notification and after translating it to a trap sends it
    * along.
    *
    * @param n notification to be sent
    * @throws Exception if an error occurs during the preparation or
    * sending of the trap
   **/    
   public void send(Notification n)
      throws Exception
   {
      // Beeing paranoid
      synchronized(this.trapFactory) {
         if(this.trapFactory == null) {
            log.error("Received notifications before trap factory set. Discarding.");
            return;     
         }
      }
           
      // Cache the translated notification
      PDUv1 v1TrapPdu = null; 
      PDU v2cTrapPdu = null; 
      ScopedPDU v3TrapPdu = null;
      
      //The snmp session
      Snmp snmp = null;
      
      //The target to send to
      Target t = null;
      
      // Send trap. Synchronise on the subscription collection while 
      // iterating 
      synchronized(this.managers) { 	  
    	  
         // Iterate over sessions and emit the trap on each one
         Iterator i = this.managers.iterator();
         while (i.hasNext()) {
            //ManagerRecord t = (ManagerRecord)i.next();
        	 t = (Target)i.next();
        	 
            try {
            	snmp = createSnmpSession(t.getAddress());
            	switch (t.getVersion()) {
            		case SnmpConstants.version1:
                  //case SnmpAgentService.SNMPV1:
                     if (v1TrapPdu == null)
                        v1TrapPdu = this.trapFactory.generateV1Trap(n);
                     
                     // fix the agent ip in the trap depending on which local address is bound
                     //Should work, but need to upgrade to snmp4j v.1.10.2
                     //v1TrapPdu.setAgentAddress((IpAddress)t.getAddress());
                   
                     // Advance the trap counter
                     this.snmpAgentService.getTrapCounter().advance();
                            
                     // Send
                     //s.getSession().send(v1TrapPdu);
                     log.debug("Sending trap: "+v1TrapPdu.toString() + "\n to target: "+ t.toString());
                     snmp.send(v1TrapPdu, t);
                     break;
                  
               		case SnmpConstants.version2c:
                  //case SnmpAgentService.SNMPV2:
                     if (v2cTrapPdu == null)
                        v2cTrapPdu = this.trapFactory.generateV2cTrap(n);
                     
                     // Advance the trap counter
                     this.snmpAgentService.getTrapCounter().advance();
                            
                     // Send
                     //t.getSession().send(v2TrapPdu);
                     snmp.send(v2cTrapPdu, t);
                     break;
                     
               		case SnmpConstants.version3:

                        if (v3TrapPdu == null)
                            v3TrapPdu = this.trapFactory.generateV3Trap(n);
                        
//                      if (contextEngineID != null) {
//                    	trapPdu.setContextEngineID(contextEngineID);
//                      }
//                      if (contextName != null) {
//                    	  trapPdu.setContextName(contextName);
//                      }
                        
                         // Advance the trap counter
                         this.snmpAgentService.getTrapCounter().advance();
                                
                         // Send
                         snmp.send(v3TrapPdu, t);
                	 break;
                     
                  default:    
                     log.error("Skipping session: Unknown SNMP version found");    
               }            
            } 
            catch(MappingFailedException e) {
              log.error("Translating notification - " + e.getMessage());
            }    
            catch(Exception e) {
              log.error("SNMP send error for " + 
                        t.getAddress().toString() + ":" +
                        ": <" + e +
                        ">");                    
            }
         }
         if (snmp != null){
        	 snmp.close();
         }
         else {
        	 log.warn("No SNMP managers to send traps to");
         }
      }
   }

   /**
    * Load manager subscriptions
   **/ 
   private void load() throws Exception
   {
      log.debug("Reading resource: '" + this.snmpAgentService.getManagersResName() + "'");
      
      // configure ObjectModelFactory for mapping XML to POJOs
      // we'll be simply getting an ArrayList of Manager objects
      MappingObjectModelFactory momf = new MappingObjectModelFactory();
      momf.mapElementToClass("manager-list", ArrayList.class);
      momf.mapElementToClass("manager", Manager.class);

      ArrayList managerList = null;
      InputStream is = null;
      try
      {
         // locate managers.xml
         final String resName = this.snmpAgentService.getManagersResName();
         is = SecurityActions.getThreadContextClassLoaderResource(resName);
         
         // create unmarshaller
         Unmarshaller unmarshaller = UnmarshallerFactory.newInstance()
               .newUnmarshaller();
         
         // let JBossXB do it's magic using the MappingObjectModelFactory
         managerList = (ArrayList)unmarshaller.unmarshal(is, momf, null);         
      }
      catch (Exception e)
      {
         log.error("Accessing resource '" + snmpAgentService.getManagersResName() + "'");
         throw e;
      }
      finally
      {
         if (is != null)
         {
            // close the XML stream
            is.close();            
         }
      }
      log.debug("Found " + managerList.size() + " monitoring managers");        
        
      for (Iterator i = managerList.iterator(); i.hasNext(); )
      {
         // Read the monitoring manager's particulars
         Manager m = (Manager)i.next();
         fixManagerVersion(m);
//         try
//         {
            // Create a record of the manager's interest
        	 
        	 Target target = createTarget(m);
        	 if (target == null){
        		 log.warn("createTarget: manager m: "+m.toString() + " is null!");
        	 	continue;
        	 }
        	 
//            ManagerRecord mr = new ManagerRecord(
//                    InetAddress.getByName(m.getAddress()),
//                    m.getPort(),
//                    toInetAddressWithDefaultBinding(m.getLocalAddress()),
//                    m.getLocalPort(),
//                    m.getVersion()
//                );
                
            // Add the record to the list of monitoring managers. If 
            // successfull open the session to the manager as well.
            if (this.managers.add(target) == false)
            {
               log.warn("Ignoring duplicate manager: " + m);  
            }
            //else
            //{            
               // Open the session to the manager
               //mr.openSession();
            //}                
//         }
//         catch (Exception e)
//         {
//            log.warn("Error enabling monitoring manager: " + m, e);                
//         } 
      }
   }
   
   /**
    * Function used to change the SNMP versions received from managers
    * config file (1,2,3) to the actual versions used to distinguish in
    * snmp4j (0,1,3)
    * @param m the Manager who's version we want to fix
    */
   private void fixManagerVersion(Manager m){
	  if (m != null){
		  switch (m.getVersion()){
		  case 1:
			  m.setVersion(SnmpConstants.version1);
			  break;
		  case 2:
			  m.setVersion(SnmpConstants.version2c);
		  }
		  
	  }
   }

   /**
    * cater for possible global -b option, if no override has been specified
    */
   private InetAddress toInetAddressWithDefaultBinding(String host)
      throws UnknownHostException
   {
      if (host == null || host.length() == 0) {
         
         String defaultBindAddress = System.getProperty(ServerConfig.SERVER_BIND_ADDRESS);
         if (defaultBindAddress != null && !defaultBindAddress.equals("0.0.0.0"))
            return InetAddress.getByName(defaultBindAddress);
         else
            return InetAddress.getLocalHost();
      }
      else
         return InetAddress.getByName(host);
   }
   
   private Target createTarget(Manager m){
	   Target target = null;
//	   String newAddr;
//	   if (m.getAddress() != null){
//		   System.out.println("*************************************");
//		   System.out.println("Address" + m.getAddress());
//		   System.out.println("Port" + m.getPort());
//		   System.out.println("*************************************");
//		   newAddr = m.getAddress()+"/"+m.getPort();		   
//	   }
//	   else {return null;}
	   
	   int version = m.getVersion();
	   try{
		   if (version == SnmpConstants.version1 || version == SnmpConstants.version2c){
			   
			   //change 'public' to a constant somewhere
			   //target = new CommunityTarget(new TcpAddress(newAddr), new OctetString("public"));
			   
				   if (m.getAddress() != null){
			   target = new CommunityTarget(new UdpAddress(InetAddress.getByName(m.getAddress()), m.getPort()), new OctetString(m.getCommunityString()));
			   //try defining retries/timeout period in notifications.xml
			   //Timeout and Retries needs to be a field later on
			   //target.setRetries(3);
			   target.setTimeout(8000);
				   }
		   }
		   else if (version == SnmpConstants.version3) {
			   //won't be used at the moment
			   target = new UserTarget();
			   target.setRetries(1);
			   target.setTimeout(8000);
			   target.setAddress(new UdpAddress(InetAddress.getByName(m.getAddress()), m.getPort()));
			   ((UserTarget)target).setSecurityName(new OctetString(((User)snmpAgentService.getUserList().get(0)).getSecurityName()));
			   ((UserTarget)target).setSecurityLevel(SecurityLevel.AUTH_PRIV);
			   ((UserTarget)target).setSecurityModel(SecurityModel.SECURITY_MODEL_USM);
		   }
		   else {
			   //unrecognized version
			   return null;
		   }
	   
	   } catch (UnknownHostException e) {} //something goes here
	   if (target != null){
		   target.setVersion(version);
	   }
	   return target;
   }
   
   private Snmp createSnmpSession(Address address) throws IOException {
	    AbstractTransportMapping transport;
	    if (address instanceof TcpAddress) {
	      transport = new DefaultTcpTransportMapping();
	    }
	    else {
	      transport = new DefaultUdpTransportMapping();
	    }
	    // Could save some CPU cycles:
	    // transport.setAsyncMsgProcessingSupported(false);
	    
	    Snmp snmp =  new Snmp(transport);
	    OctetString localEngineID =new OctetString(snmp.getLocalEngineID());
	    USM usm = new USM(SecurityProtocols.getInstance(), localEngineID, 0);	    	   
	    SecurityProtocols.getInstance().addDefaultProtocols();
	    // all other privacy and authentication protocols are provided by the above method
	    SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());	   		   
	    SecurityModels.getInstance().addSecurityModel(usm);
	    
	    for (Iterator<User> userIt = snmpAgentService.getUserList().iterator(); userIt.hasNext(); ) {
	    	  User user = userIt.next();
	        	 
	    	  UsmUser usmUser = new UsmUser(new OctetString(user.getSecurityName()),
	               user.getAuthenticationProtocolID(),
	               new OctetString(user.getAuthenticationPassphrase()),
	               user.getPrivacyProtocolID(),
	               new OctetString(user.getPrivacyPassphrase()));
	    	  usm.addUser(usmUser.getSecurityName(), usm.getLocalEngineID(),usmUser);
	      }
	       
	       
// SNMPv3 stuff ~ add and fix later
//	    
//	    ((MPv3)snmp.getMessageProcessingModel(MPv3.ID)).
//	        setLocalEngineID(localEngineID.getValue());
//
//	    if (version == SnmpConstants.version3) {
//	      USM usm = new USM(SecurityProtocols.getInstance(),
//	                        localEngineID,
//	                        engineBootCount);
//	      SecurityModels.getInstance().addSecurityModel(usm);
//	      addUsmUser(snmp);
//	    }
	    return snmp;
	  }
   
} // class TrapEmitter
