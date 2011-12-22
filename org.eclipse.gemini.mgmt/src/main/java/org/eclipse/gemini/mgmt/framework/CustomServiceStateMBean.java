/*******************************************************************************
 * Copyright (c) 2011 VMware.
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
 *     Christopher Frost - VMware 
 ******************************************************************************/
package org.eclipse.gemini.mgmt.framework;

import java.io.IOException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.osgi.jmx.framework.ServiceStateMBean;

/**
 *
 */
public interface CustomServiceStateMBean extends ServiceStateMBean {

	//New methods from the JMX Update RFC 169

	/**
	 * 
	 * @param serviceId
	 * @return
	 * @throws IOException
	 */
	public CompositeData getService(long serviceId) throws IOException;

	/**
	 * Get a single property by it's key and 
	 * 
	 * @param serviceId
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public CompositeData getProperty(long serviceId, String key) throws IOException;

	/**
	 * 
	 * @param clazz
	 * @param filter
	 * @return
	 * @throws IOException
	 */
	public TabularData listServices(String clazz, String filter) throws IOException;

	/**
	 * 
	 * @param clazz
	 * @param filter
	 * @param serviceTypeItems
	 * @return
	 * @throws IOException
	 */
	public TabularData listServices(String clazz, String filter, String... serviceTypeItems) throws IOException;

	/**
	 * 
	 * @return array of the bundle IDs of the bundles using this service
	 * @throws IOException
	 */
	public long[] getServiceIds() throws IOException;
	
}
