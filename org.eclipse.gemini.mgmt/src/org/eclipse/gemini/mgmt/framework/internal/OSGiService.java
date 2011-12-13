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

package org.eclipse.gemini.mgmt.framework.internal;

import static org.osgi.framework.Constants.OBJECTCLASS;
import static org.osgi.framework.Constants.SERVICE_ID;
import static org.eclipse.gemini.mgmt.internal.Util.LongArrayFrom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.mgmt.internal.Util;
import org.osgi.jmx.Item;
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
	
	private long[] usingBundles;

	/**
	 * Construct an OSGiService from the underlying
	 * <link>ServiceReference</link>
	 * 
	 * @param reference
	 *            - the reference of the service
	 * @throws  
	 */
	public OSGiService(ServiceReference<?> reference) {
		this.identifier = (Long) reference.getProperty(SERVICE_ID);
		this.interfaces = (String[]) reference.getProperty(OBJECTCLASS);
		this.bundle = reference.getBundle().getBundleId();
		this.usingBundles = Util.getBundlesUsing(reference);
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
		TabularDataSupport table = new TabularDataSupport(Item.tabularType("SERVICES", "The table of all services", OSGiService.computeServiceType(serviceTypes), ServiceStateMBean.IDENTIFIER));
		for (OSGiService service : services) {
			table.put(service.asCompositeData(serviceTypes));
		}
		return table;
	}
	
	private static CompositeType computeServiceType(List<String> serviceTypes) {
		List<Item> serviceTypeItems = new ArrayList<Item>();
		serviceTypeItems.add(ServiceStateMBean.IDENTIFIER_ITEM);
		if(serviceTypes.contains(ServiceStateMBean.OBJECT_CLASS)){
			serviceTypeItems.add(ServiceStateMBean.OBJECT_CLASS_ITEM);
		}
		if(serviceTypes.contains(ServiceStateMBean.BUNDLE_IDENTIFIER)){
			serviceTypeItems.add(ServiceStateMBean.BUNDLE_IDENTIFIER_ITEM);
		}
		if(serviceTypes.contains(ServiceStateMBean.USING_BUNDLES)){
			serviceTypeItems.add(ServiceStateMBean.USING_BUNDLES_ITEM);
		}
		CompositeType currentCompositeType = Item.compositeType("SERVICE", "This type encapsulates an OSGi service", serviceTypeItems.toArray(new Item[]{}));
		return currentCompositeType;
	}

	/**
	 * Answer the receiver encoded as CompositeData
	 * 
	 * @return the CompositeData encoding of the receiver.
	 */
	public CompositeData asCompositeData() {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(ServiceStateMBean.IDENTIFIER, identifier);
		items.put(ServiceStateMBean.OBJECT_CLASS, interfaces);
		items.put(ServiceStateMBean.BUNDLE_IDENTIFIER, bundle);
		items.put(ServiceStateMBean.USING_BUNDLES, LongArrayFrom(usingBundles));

		try {
			return new CompositeDataSupport(ServiceStateMBean.SERVICE_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form service open data", e);
		}
	}

	/**
	 * Answer the receiver encoded as CompositeData
	 * 
	 * @return the CompositeData encoding of the receiver.
	 */
	private CompositeData asCompositeData(List<String> serviceTypes) {
		Map<String, Object> items = new HashMap<String, Object>();
		if(serviceTypes.contains(ServiceStateMBean.IDENTIFIER)){
			items.put(ServiceStateMBean.IDENTIFIER, identifier);
		}
		if(serviceTypes.contains(ServiceStateMBean.OBJECT_CLASS)){
			items.put(ServiceStateMBean.OBJECT_CLASS, interfaces);
		}
		if(serviceTypes.contains(ServiceStateMBean.BUNDLE_IDENTIFIER)){
			items.put(ServiceStateMBean.BUNDLE_IDENTIFIER, bundle);
		}
		if(serviceTypes.contains(ServiceStateMBean.USING_BUNDLES)){
			items.put(ServiceStateMBean.USING_BUNDLES, LongArrayFrom(usingBundles));
		}
		try {
			return new CompositeDataSupport(ServiceStateMBean.SERVICE_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form service open data", e);
		}
	}

//	/**
//	 * @return the identifier of the bundle the service belongs to
//	 */
//	public long getBundle() {
//		return bundle;
//	}
//
//	/**
//	 * @return the identifier of the service
//	 */
//	public long getIdentifier() {
//		return identifier;
//	}
//
//	/**
//	 * @return the interfaces implemented by the service
//	 */
//	public String[] getInterfaces() {
//		return interfaces;
//	}
//
//	/**
//	 * @return the identifiers of the bundles which are using the service
//	 */
//	public long[] getUsingBundles() {
//		return usingBundles;
//	}

}
