/*******************************************************************************
 * Copyright (c) 2010 Oracle.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at 
 *     http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Hal Hildebrand - Initial JMX support 
 ******************************************************************************/

package org.eclipse.gemini.mgmt.provisioning;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipInputStream;

import javax.management.openmbean.TabularData;

import org.eclipse.gemini.mgmt.internal.OSGiProperties;
import org.osgi.jmx.service.provisioning.ProvisioningServiceMBean;
import org.osgi.service.provisioning.ProvisioningService;

/** 
 * 
 */
public final class Provisioning implements ProvisioningServiceMBean {
	
	private ProvisioningService provisioning;

	public Provisioning(ProvisioningService provisioning) {
		this.provisioning = provisioning;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addInformationFromZip(String zipURL) throws IOException {
		InputStream is = new URL(zipURL).openStream();
		ZipInputStream zis = new ZipInputStream(is);
		try {
			provisioning.addInformation(zis);
		} finally {
			zis.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addInformation(TabularData info) throws IOException {
		provisioning.addInformation(OSGiProperties.propertiesFrom(info));
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public TabularData listInformation() throws IOException {
		return OSGiProperties.tableFrom(provisioning.getInformation());
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInformation(TabularData info) throws IOException {
		provisioning.setInformation(OSGiProperties.propertiesFrom(info));
	}

}
