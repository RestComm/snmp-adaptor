/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class SnmpServiceTest implements SnmpServiceTestMBean {

	private static Logger logger = Logger.getLogger(SnmpServiceTest.class); 
	AtomicInteger count;
	String message;
	private List<String> messageHistory;
	private List<Integer> countHistoryList;
	private int[] countHistory;
	private Map<String, Integer> messageCountHistory;
	
	public SnmpServiceTest() {
		count = new AtomicInteger(0);
		messageHistory = new ArrayList<String>();
		countHistoryList = new ArrayList<Integer>();
		messageCountHistory = new HashMap<String, Integer>();
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.adaptor.snmp.servlet.example.SnmpServiceMBean#getCount()
	 */
	@Override
	public long getCount() {
		return count.get();
	}
	
	public AtomicInteger getAtomicCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see org.jboss.adaptor.snmp.servlet.example.SnmpServiceMBean#getMessage()
	 */
	@Override
	public String getMessage() {
		return message;
	}

	/* (non-Javadoc)
	 * @see org.jboss.adaptor.snmp.servlet.example.SnmpServiceMBean#setMessage(java.lang.String)
	 */
	@Override
	public void setMessage(String message) {
		this.message = message;
		messageHistory.add(message);
	}
	
	/**
	 * @return the messageHistory
	 */
	public List<String> getMessageHistory() {
		return messageHistory;
	}

	/* (non-Javadoc)
	 * @see org.jboss.adaptor.snmp.servlet.example.SnmpServiceMBean#start()
	 */
	@Override
	public void start() throws Exception {
		logger.info("Service " + SnmpServiceTest.class.getName() + " started.");
	}

	/* (non-Javadoc)
	 * @see org.jboss.adaptor.snmp.servlet.example.SnmpServiceMBean#stop()
	 */
	@Override
	public void stop() {
		logger.info("Service " + SnmpServiceTest.class.getName() + " stopped.");
	}

	@Override
	public String[] getMessageHistoryAsArray() {
		return messageHistory.toArray(new String[messageHistory.size()]);
	}

	@Override
	public int[] getCountHistory() {
		return countHistory;
	}
	
	@Override
	public Map<String, Integer> getMessageCountHistory() {
		return messageCountHistory;
	}
	
	public static int[] boxedArray(Integer[] array) {
        int[] result = new int[array.length];
        for (Integer i = 0; i < array.length; i++)
                result[i] = array[i];
        return result;
}

	public int getNextValue() {
		int nextcount = getAtomicCount().incrementAndGet();
		countHistoryList.add(nextcount);
		if(message != null) {
			messageCountHistory.put(message, nextcount);
		}
		countHistory = boxedArray(countHistoryList.toArray(new Integer[countHistoryList.size()]));
		return nextcount;
	}
}
