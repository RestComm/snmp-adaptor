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
package org.jboss.jmx.adaptor.snmp.deployer;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.jmx.adaptor.snmp.deployer.metadata.SnmpMetaData;
import org.jboss.jmx.adaptor.snmp.deployer.metadata.attributes.SnmpAttributesMetaData;
import org.jboss.jmx.adaptor.snmp.deployer.metadata.notifications.SnmpNotificationsMetaData;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public class MergedSnmpMetaDataDeployer extends AbstractDeployer {

	/**
	 * Create a new MergedJBossConvergedSipMetaDataDeployer.
	 */
	public MergedSnmpMetaDataDeployer() {
		setStage(DeploymentStages.POST_CLASSLOADER);
		// web.xml metadata
		addInput(SnmpAttributesMetaData.class);
		// sip.xml metadata
		addInput(SnmpNotificationsMetaData.class);
		// Output is the merge JBossConvergedSipMetaData view
		setOutput(SnmpMetaData.class);
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
		SnmpAttributesMetaData attributesMetaData = unit
				.getAttachment(SnmpAttributesMetaData.class);
		SnmpNotificationsMetaData notificationsMetaData = unit
				.getAttachment(SnmpNotificationsMetaData.class);
		if (attributesMetaData == null && notificationsMetaData == null)
			return;

		// Create a merged view
		SnmpMetaData snmpMetaData = new SnmpMetaData(notificationsMetaData, attributesMetaData);

		// Output the merged JBossConvergedSipMetaData
		unit.getTransientManagedObjects().addAttachment(
				SnmpMetaData.class, snmpMetaData);		
	}

}
