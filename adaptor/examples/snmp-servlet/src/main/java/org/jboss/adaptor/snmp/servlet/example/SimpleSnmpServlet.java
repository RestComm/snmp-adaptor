/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.adaptor.snmp.servlet.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jboss.jmx.adaptor.snmp.agent.SnmpAgentServiceMBean;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * This application is meant to be a showcase on how to expose your applications metrics through SNMP
 * and how to send traps from your application through SNMP 
 * 
 * @author Jean Deruelle
 *
 */
public class SimpleSnmpServlet extends HttpServlet implements ServletContextListener {
	private static Logger logger = Logger.getLogger(SimpleSnmpServlet.class);
	private static final long serialVersionUID = 1L;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("SimpleSnmpServlet has been started");
		super.init(servletConfig);				
		SnmpServiceTest bean = new SnmpServiceTest();
		getServletContext().setAttribute("bean", bean);
		MBeanServer server = MBeanServerLocator.locateJBoss();
		try {
			ObjectName oname = new ObjectName("test.com:service=SnmpTest");
			server.registerMBean(bean, oname);
		} catch (MalformedObjectNameException e) {
			logger.error("couldn't access the snmp agent mbean", e);
		} catch (InstanceAlreadyExistsException e) {
			logger.error("couldn't access the snmp agent mbean", e);
		} catch (MBeanRegistrationException e) {
			logger.error("couldn't access the snmp agent mbean", e);
		} catch (NotCompliantMBeanException e) {
			logger.error("couldn't access the snmp agent mbean", e);
		} 
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {
		
		SnmpServiceTest bean = (SnmpServiceTest) getServletContext().getAttribute("bean");
		
		// This hsould be at the end otherwise the response will have been committed and 
        // the session can't be accessed anymore 
        // Write some web page content
    	PrintWriter	out;
        response.setContentType("text/html");
        out = response.getWriter();
        out.println("<HTML><HEAD><TITLE>");
        out.println("SNMP Servlet");
        out.println("</TITLE></HEAD><BODY>");
        out.println("<P>Count <b>" + bean.getNextValue() + "</b></p>");
        if(bean.getMessage() != null) {
        	out.println("<P>Message <b>" + bean.getMessage() + "</b></p>");
        }
        out.println("</BODY></HTML>");
        out.close();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		MBeanServer server = MBeanServerLocator.locateJBoss();
		try {
			ObjectName oname = new ObjectName("test.com:service=SnmpTest");
			server.unregisterMBean(oname);
		} catch (MalformedObjectNameException e) {
			logger.error("couldn't access the snmp agent mbean", e);
		} catch (MBeanRegistrationException e) {
			logger.error("couldn't access the snmp agent mbean", e);
		} catch (InstanceNotFoundException e) {
			logger.error("couldn't access the snmp agent mbean", e);
		}  
		logger.info("SimpleSnmpServlet context destroyed");		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		logger.info("SimpleSnmpServlet context initialized");
		MBeanServer server = MBeanServerLocator.locateJBoss();		
		try {
			ObjectName oname = new ObjectName("jboss.jmx:name=SnmpAgent,service=snmp,type=adaptor");
			SnmpAgentServiceMBean snmpAgentServiceMBean = MBeanServerInvocationHandler.newProxyInstance(server, oname, SnmpAgentServiceMBean.class, true);
			logger.info("attribute file name " + snmpAgentServiceMBean.getRequestHandlerResName());
			Map<String, Object> trapValues = new HashMap<String, Object>();
			trapValues.put("u:startTime", new Date(System.currentTimeMillis()));
			Notification n = new Notification("snmp.servlet.test.coldstart", this, snmpAgentServiceMBean.getNextJMXNotificationSequenceNumber());
			n.setUserData(trapValues);
			snmpAgentServiceMBean.sendJMXNotification(n);
		} catch (MalformedObjectNameException e) {
			logger.error("couldn't access the snmp agent mbean", e);
		} 		
	}
}