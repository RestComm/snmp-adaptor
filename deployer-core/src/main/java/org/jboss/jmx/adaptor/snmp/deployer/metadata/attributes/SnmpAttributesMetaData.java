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
package org.jboss.jmx.adaptor.snmp.deployer.metadata.attributes;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.jmx.adaptor.snmp.config.attribute.ManagedBean;
import org.jboss.jmx.adaptor.snmp.deployer.metadata.SnmpMetaDataConstants;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * @author jean.deruelle@gmail.com
 *
 */
@XmlRootElement(name="attribute-mappings", namespace=SnmpMetaDataConstants.JBOSS_SNMP_NS)
@JBossXmlSchema(
      xmlns={@XmlNs(namespaceURI = SnmpMetaDataConstants.JBOSS_SNMP_NS, prefix = SnmpMetaDataConstants.JBOSS_SNMP_NS)},
//      ignoreUnresolvedFieldOrClass=false,
      namespace=SnmpMetaDataConstants.JBOSS_SNMP_NS,
      attributeFormDefault=XmlNsForm.UNSET,      
      elementFormDefault=XmlNsForm.UNSET,
      normalizeSpace=false,
      strict=false)
public class SnmpAttributesMetaData{
	private List<ManagedBean> managedBeans;

	/**
	 * @param managedBeans the managedBeans to set
	 */
	@XmlElement(name="mbean")
	public void setManagedBeans(List<ManagedBean> managedBeans) {
		this.managedBeans = managedBeans;
	}

	/**
	 * @return the managedBeans
	 */
	public List<ManagedBean> getManagedBeans() {
		return managedBeans;
	}
	
	
}
