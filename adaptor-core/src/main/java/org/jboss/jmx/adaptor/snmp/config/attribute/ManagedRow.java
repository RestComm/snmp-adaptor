/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.jmx.adaptor.snmp.config.attribute;

/**
 * This class denotes a Row contained in a table. This object has a list of associated "Instances" of data.
 * Essentially it is just a conceptual grouping of attributes. 
 * This functionality can simplify retrieving certain attributes and organization of the MIB.
 * The name can be arbitrary. It is simply a label used for organization.
 * 
 * Rows are never writable.
 * 
 * @author <a href="mailto:hwr@pilhuhn.de">Heiko W. Rupp</a>
 * @version $Revision: 81038 $
 */
public class ManagedRow
{
	private String name;
	private String oidPrefix;
	private int size;
	
	public String getName()
   {
		return this.name;
	}
   
	public void setName(String name)
   {
		this.name = name;
	}
	
	// Table OID
	public String getPrefix()
   {
		return this.oidPrefix;
	}
   
	public void setPrefix(String oid)
   {
		this.oidPrefix = oid;
	}
	
	public int getSize(){
		return this.size;
	}
	public void setSize(int n){
		this.size = n;
	}
	
	public String toString()
   {
		StringBuffer buf = new StringBuffer();
		buf.append("[row name=").append(name);
		buf.append(", oid=").append(oidPrefix);
		buf.append("]");
		return buf.toString();
	}
}
