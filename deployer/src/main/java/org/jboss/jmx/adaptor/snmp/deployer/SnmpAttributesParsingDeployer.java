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

package org.jboss.jmx.adaptor.snmp.deployer;


import org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer;
import org.jboss.jmx.adaptor.snmp.deployer.metadata.attributes.SnmpAttributesMetaData;

/**
 * An ObjectModelFactoryDeployer for translating snmp-attribtues.xml descriptors into
 * SnmpAttributesMetaData instances.
 * 
 * @author jean.deruelle@gmail.com
 */
public class SnmpAttributesParsingDeployer  extends SchemaResolverDeployer<SnmpAttributesMetaData>
{
   public SnmpAttributesParsingDeployer ()
   {
      super(SnmpAttributesMetaData.class);
      setName("snmp-attributes.xml");
   }

   /**
    * Get the virtual file path for the sip-app descriptor in the
    * DeploymentContext.getMetaDataPath.
    * 
    * @return the current virtual file path for the snmp-attributes descriptor
    */
   public String getSnmpAttributesXmlPath()
   {
      return getName();
   }
   /**
    * Set the virtual file path for the sip-app descriptor in the
    * DeploymentContext.getMetaDataLocation. The standard path is snmp-attributes.xml
    * to be found in the META-INF metdata path.
    * 
    * @param sipXmlPath - new virtual file path for the sip-notifications descriptor
    */
   public void setSnmpAttributesXmlPath(String snmpAttributesXmlPath)
   {
      setName(snmpAttributesXmlPath);
   }

}