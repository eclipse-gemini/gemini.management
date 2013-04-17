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

package org.eclipse.gemini.management.framework.internal;

import static org.osgi.framework.Constants.OBJECTCLASS;
import static org.osgi.framework.Constants.SERVICE_ID;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

import org.eclipse.gemini.management.internal.OSGiProperties;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.ServiceStateMBean;

/**
 * <p>
 * This class represents the CODEC for the composite data representing an OSGi
 * <link>ServiceReference</link>
 * <p>
 * It serves as both the documentation of the type structure and as the
 * codification of the mechanism to convert to/from the CompositeData.
 * <p>
 * The structure of the composite data is:
 * <table border="1">
 * <tr>
 * <td>Identifier</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>ObjectClass</td>
 * <td>Array of String</td>
 * </tr>
 * <tr>
 * <td>BundleIdentifier</td>
 * <td>long</td>
 * </tr>
 * <tr>
 * <td>UsingBundles</td>
 * <td>Array of long</td>
 * </tr>
 * </table>
 */
public final class OSGiService {

	private long bundle;
	
	private long identifier;
	
	private String[] interfaces;
	
	private Long[] usingBundles;
	
	private TabularData properties;

	/**
	 * Construct an OSGiService from the underlying
	 * <link>ServiceReference</link>
	 * 
	 * @param reference
	 *            - the reference of the service
	 * @throws  
	 */
	public OSGiService(ServiceReference<?> reference) {
		this.bundle = reference.getBundle().getBundleId();
		this.identifier = (Long) reference.getProperty(SERVICE_ID);
		this.interfaces = (String[]) reference.getProperty(OBJECTCLASS);
		this.properties = OSGiProperties.tableFrom(reference);
		this.usingBundles = longArrayFrom(OSGiService.getBundlesUsing(reference));
	}
	
	private Long[] longArrayFrom(long[] array) {
		if (array == null) {
			return new Long[0];
		}
		Long[] result = new Long[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}
	
	/**
	 * 
	 * @param serviceRef
	 * @return
	 */
	public static long[] getBundlesUsing(ServiceReference<?> serviceRef) {
		Bundle[] bundles = serviceRef.getUsingBundles();
		if(bundles == null){
			return new long[0];
		}
		long[] ids = new long[bundles.length];
		for (int i = 0; i < bundles.length; i++) {
			ids[i] = bundles[i].getBundleId();
		}
		return ids;
	}
	
	/**
	 * Construct the TabularData representing a list of services
	 * 
	 * @param services
	 *            - the list of services
	 * 
	 * @return the TabularData representing the list of OSGiServices
	 */
	public static TabularData tableFrom(List<OSGiService> services) {
		TabularDataSupport table = new TabularDataSupport(ServiceStateMBean.SERVICES_TYPE);
		for (OSGiService service : services) {
			table.put(service.asCompositeData());
		}
		return table;
	}
	
	/**
	 * Answer the TabularData representing the list of OSGiService state
	 * 
	 * @param bundles
	 *            - the list of bundles to represent
	 * @param mask 
	 * 
	 * @return the Tabular data which represents the list of bundles
	 * @throws IOException 
	 */
	public static TabularData tableFrom(List<OSGiService> services, String... serviceTypeItems) throws IOException {
		List<String> serviceTypes = Arrays.asList(serviceTypeItems);
		TabularDataSupport table = new TabularDataSupport(ServiceStateMBean.SERVICES_TYPE);
		for (OSGiService service : services) {
			table.put(service.asCompositeData(serviceTypes));
		}
		return table;
	}

	/**
	 * Answer the receiver encoded as CompositeData
	 * 
	 * @return the CompositeData encoding of the receiver.
	 */
	public CompositeData asCompositeData() {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(ServiceStateMBean.BUNDLE_IDENTIFIER, bundle);
		items.put(ServiceStateMBean.IDENTIFIER, identifier);
		items.put(ServiceStateMBean.OBJECT_CLASS, interfaces);
		items.put(ServiceStateMBean.PROPERTIES, properties);
		items.put(ServiceStateMBean.USING_BUNDLES, usingBundles);

		try {
			return new CompositeDataSupport(ServiceStateMBean.SERVICE_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form service open data", e);
		}
	}

	/**
	 * Answer the receiver encoded as CompositeData
	 * @param computeServiceType 
	 * 
	 * @return the CompositeData encoding of the receiver.
	 */
	private CompositeData asCompositeData(List<String> serviceTypes) {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(ServiceStateMBean.IDENTIFIER, identifier);
		
		items.put(ServiceStateMBean.BUNDLE_IDENTIFIER, 	serviceTypes.contains(ServiceStateMBean.BUNDLE_IDENTIFIER) ? bundle : null);
		items.put(ServiceStateMBean.OBJECT_CLASS, 		serviceTypes.contains(ServiceStateMBean.OBJECT_CLASS) ? 		interfaces : null);
		items.put(ServiceStateMBean.USING_BUNDLES, 		serviceTypes.contains(ServiceStateMBean.USING_BUNDLES) ? 	usingBundles : null);
		items.put(ServiceStateMBean.PROPERTIES, 		serviceTypes.contains(ServiceStateMBean.PROPERTIES) ? 		properties : null);
		try {
			return new CompositeDataSupport(ServiceStateMBean.SERVICE_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form service open data", e);
		}
	}

}
