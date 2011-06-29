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
package org.jboss.jmx.adaptor.snmp.agent;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.jmx.adaptor.snmp.config.attribute.ManagedBean;
import org.jboss.jmx.adaptor.snmp.config.attribute.MappedAttribute;
import org.jboss.logging.Logger;
import org.snmp4j.smi.OID;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public class AttributeTableMapper {

	private SortedSet<OID> tables = new TreeSet<OID>();
	/**
	 * keep an index of the OID from attributes.xml
	 */	
	private SortedMap<OID, BindEntry> tableMappings = new TreeMap<OID, BindEntry>();

	private MBeanServer server;
	private Logger log;

	public AttributeTableMapper(MBeanServer server, Logger log) {
		this.server = server;
		this.log = log;
	}
	
	/**
	 * 
	 * @param oid
	 * @return
	 */
	public BindEntry getTableBinding(OID oid) {
		for (Entry<OID,BindEntry> entry : tableMappings.entrySet()) {			
			if (oid.startsWith(entry.getKey())) {
				BindEntry bindEntry = entry.getValue();
				int[] oidValue = oid.getValue();
				int[] subOid = new int[oid.size() - entry.getKey().size()];
				System.arraycopy(oidValue, entry.getKey().size(), subOid, 0, oid.size() - entry.getKey().size());
				bindEntry.setTableIndexOID(new OID(subOid));
				return bindEntry;
			}			
		}
		return null;
	}
	
//	public OID getNextTable(OID oid) {
//		return tableIndexes.get(oid);
//	}	

	/**
	 * 
	 * @param mmb
	 * @param oname
	 */
	public void addTableMapping(ManagedBean mmb, MappedAttribute ma) {
		String oid;
		String oidPrefix = mmb.getOidPrefix();
		if (oidPrefix != null) {
			oid = oidPrefix + ma.getOid();
		} else {
			oid = ma.getOid();
		}
		OID coid = new OID(oid.substring(0, oid.lastIndexOf(".")));
		BindEntry be = new BindEntry(coid, mmb.getName(), ma.getName());
		be.setReadWrite(ma.isReadWrite());
		be.setTable(ma.isAttributeTable());

		if (log.isTraceEnabled())
			log.trace("New bind entry   " + be);
		if (tables.contains(coid)) {
			log.info("Duplicate oid " + coid + RequestHandlerImpl.SKIP_ENTRY);
		}
		if (mmb == null || mmb.equals("")) {
			log.info("Invalid mbean name for oid " + coid + RequestHandlerImpl.SKIP_ENTRY);
		}
		if (ma == null || ma.equals("")) {
			log.info("Invalid attribute name " + ma + " for oid " + coid
					+ RequestHandlerImpl.SKIP_ENTRY);
		}
		tables.add(coid);
		tableMappings.put(new OID(oid), be);	
	}

	public boolean belongsToTables(OID oid) {
		for (OID attributeOID : tables) {			
			if (oid.startsWith(attributeOID)) {
				return true;
			}			
		}
		return false;
	}

	public void removeTableMapping(ManagedBean mmb, ObjectName oname) {
		
	}
}
