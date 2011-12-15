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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

import org.eclipse.gemini.mgmt.framework.CustomBundleStateMBean;
import org.eclipse.gemini.mgmt.internal.BundleUtil;
import org.osgi.framework.Bundle;
import org.osgi.jmx.Item;
import org.osgi.jmx.framework.BundleStateMBean;


/**
 * <p>
 * This class represents the CODEC for the composite data representing a single
 * OSGi <link>Bundle</link>.
 * <p>
 * It serves as both the documentation of the type structure and as the
 * codification of the mechanism to convert to/from the CompositeData.
 * <p>
 */
public final class OSGiBundle {
	
	private static final String[] HEADER_PROPERTY_ITEM_NAMES = new String[] {BundleStateMBean.KEY, BundleStateMBean.VALUE };
	
	private Bundle bundle;

	/**
	 * Construct an OSGiBundle representation
	 * 
	 * @param b - the Bundle to represent
	 */
	public OSGiBundle(Bundle b) {
		this.bundle = b;
	}

	/**
	 * Answer the TabularData representing the list of OSGiBundle state
	 * 
	 * @param bundles - the list of bundles to represent
	 * @param mask 
	 * 
	 * @return the Tabular data which represents the list of bundles
	 * @throws IOException 
	 */
	public static TabularData tableFrom(List<OSGiBundle> bundles, int mask) throws IOException {
		CompositeType computeBundleType = OSGiBundle.computeBundleType(mask);
		TabularDataSupport table = new TabularDataSupport(Item.tabularType("BUNDLES", "A list of bundles", computeBundleType, new String[] { BundleStateMBean.IDENTIFIER }));
		for (OSGiBundle bundle : bundles) {
			table.put(bundle.asCompositeData(computeBundleType, mask));
		}
		return table;
	}
	
	private static CompositeType computeBundleType(int mask) {
		List<Item> bundleTypes = new ArrayList<Item>();
		bundleTypes.add(BundleStateMBean.IDENTIFIER_ITEM);
		if((mask | CustomBundleStateMBean.LOCATION) == mask) {
			bundleTypes.add(BundleStateMBean.LOCATION_ITEM);
		}
		if((mask | CustomBundleStateMBean.SYMBOLIC_NAME) == mask) {
			bundleTypes.add(BundleStateMBean.SYMBOLIC_NAME_ITEM);
		}
		if((mask | CustomBundleStateMBean.VERSION) == mask) {
			bundleTypes.add(BundleStateMBean.VERSION_ITEM);
		}
		if((mask | CustomBundleStateMBean.START_LEVEL) == mask) {
			bundleTypes.add(BundleStateMBean.START_LEVEL_ITEM);
		}
		if((mask | CustomBundleStateMBean.STATE) == mask) {
			bundleTypes.add(BundleStateMBean.STATE_ITEM);
		}
		if((mask | CustomBundleStateMBean.LAST_MODIFIED) == mask) {
			bundleTypes.add(BundleStateMBean.LAST_MODIFIED_ITEM);
		}
		if((mask | CustomBundleStateMBean.PERSISTENTLY_STARTED) == mask) {
			bundleTypes.add(BundleStateMBean.PERSISTENTLY_STARTED_ITEM);
		}
		if((mask | CustomBundleStateMBean.REMOVAL_PENDING) == mask) {
			bundleTypes.add(BundleStateMBean.REMOVAL_PENDING_ITEM);
		}
		if((mask | CustomBundleStateMBean.REQUIRED) == mask) {
			bundleTypes.add(BundleStateMBean.REQUIRED_ITEM);
		}
		if((mask | CustomBundleStateMBean.FRAGMENT) == mask) {
			bundleTypes.add(BundleStateMBean.FRAGMENT_ITEM);
		}
		if((mask | CustomBundleStateMBean.REGISTERED_SERVICES) == mask) {
			bundleTypes.add(BundleStateMBean.REGISTERED_SERVICES_ITEM);
		}
		if((mask | CustomBundleStateMBean.SERVICES_IN_USE) == mask) {
			bundleTypes.add(BundleStateMBean.SERVICES_IN_USE_ITEM);
		}
		if((mask | CustomBundleStateMBean.HEADERS) == mask) {
			bundleTypes.add(BundleStateMBean.HEADERS_ITEM);
		}
		if((mask | CustomBundleStateMBean.EXPORTED_PACKAGES) == mask) {
			bundleTypes.add(BundleStateMBean.EXPORTED_PACKAGES_ITEM);
		}
		if((mask | CustomBundleStateMBean.IMPORTED_PACKAGES) == mask) {
			bundleTypes.add(BundleStateMBean.IMPORTED_PACKAGES_ITEM);
		}
		if((mask | CustomBundleStateMBean.FRAGMENTS) == mask) {
			bundleTypes.add(BundleStateMBean.FRAGMENTS_ITEM);
		}
		if((mask | CustomBundleStateMBean.HOSTS) == mask) {
			bundleTypes.add(BundleStateMBean.HOSTS_ITEM);
		}
		if((mask | CustomBundleStateMBean.REQUIRING_BUNDLES) == mask) {
			bundleTypes.add(BundleStateMBean.REQUIRING_BUNDLES_ITEM);
		}
		if((mask | CustomBundleStateMBean.REQUIRED_BUNDLES) == mask) {
			bundleTypes.add(BundleStateMBean.REQUIRED_BUNDLES_ITEM);
		}
		CompositeType currentCompositeType = Item.compositeType("BUNDLE", "This type encapsulates OSGi bundles", bundleTypes.toArray(new Item[]{}));
		return currentCompositeType;
	}

	/**
	 * Answer the TabularData representing the list of bundle headers for a
	 * bundle
	 * 
	 * @param b
	 * @return the bundle headers
	 */
	public static TabularData headerTable(Bundle b) {
		TabularDataSupport table = new TabularDataSupport(BundleStateMBean.HEADERS_TYPE);
		Dictionary<String, String> map = b.getHeaders();
		for (Enumeration<String> headers = map.keys(); headers.hasMoreElements();) {
			String key = (String) headers.nextElement();
			table.put(headerData(key, (String) map.get(key)));
		}
		return table;
	}

	/**
	 * Answer the TabularData representing the supplied map of bundle headers
	 * 
	 * @param headers
	 * @return the bundle headers
	 */
	public static TabularData headerTable(Map<String, String> headers) {
		TabularDataSupport table = new TabularDataSupport(BundleStateMBean.HEADERS_TYPE);
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			table.put(headerData(entry.getKey(), entry.getValue()));
		}
		return table;
	}

	private static CompositeData headerData(String key, String value) {
		Object[] itemValues = new Object[HEADER_PROPERTY_ITEM_NAMES.length];
		itemValues[0] = key;
		itemValues[1] = value;

		try {
			return new CompositeDataSupport(BundleStateMBean.HEADER_TYPE, HEADER_PROPERTY_ITEM_NAMES, itemValues);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form bundle header open data", e);
		}
	}

	/**
	 * Answer the receiver encoded as CompositeData
	 * @param mask 
	 * 
	 * @return the CompositeData encoding of the receiver.
	 * @throws IOException 
	 */
	public CompositeData asCompositeData() throws IOException {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(BundleStateMBean.IDENTIFIER, getIdentifier());
		items.put(BundleStateMBean.LOCATION, getLocation());
		items.put(BundleStateMBean.SYMBOLIC_NAME, getSymbolicName());
		items.put(BundleStateMBean.VERSION, getVersion());
		items.put(BundleStateMBean.START_LEVEL, getStartLevel());
		items.put(BundleStateMBean.STATE, getState());
		items.put(BundleStateMBean.LAST_MODIFIED, getLastModified());
		items.put(BundleStateMBean.PERSISTENTLY_STARTED, isPersistentlyStarted());
		items.put(BundleStateMBean.REMOVAL_PENDING, isRemovalPending());
		items.put(BundleStateMBean.REQUIRED, isRequired());
		items.put(BundleStateMBean.FRAGMENT, isFragment());
		items.put(BundleStateMBean.REGISTERED_SERVICES, BundleUtil.LongArrayFrom(getRegisteredServices()));
		items.put(BundleStateMBean.SERVICES_IN_USE, BundleUtil.LongArrayFrom(getServicesInUse()));
		items.put(BundleStateMBean.HEADERS, headerTable(getHeaders()));
		items.put(BundleStateMBean.EXPORTED_PACKAGES, getExportedPackages());
		items.put(BundleStateMBean.IMPORTED_PACKAGES, getImportedPackages());
		items.put(BundleStateMBean.FRAGMENTS, BundleUtil.LongArrayFrom(getFragments()));
		items.put(BundleStateMBean.HOSTS, BundleUtil.LongArrayFrom(getHosts()));
		items.put(BundleStateMBean.REQUIRING_BUNDLES, BundleUtil.LongArrayFrom(getRequiringBundles()));
		items.put(BundleStateMBean.REQUIRED_BUNDLES, BundleUtil.LongArrayFrom(getRequiredBundles()));
		try {
			return new CompositeDataSupport(BundleStateMBean.BUNDLE_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form bundle open data", e);
		}
	}
	
	/**
	 * Answer the receiver encoded as CompositeData
	 * @param mask 
	 * 
	 * @return the CompositeData encoding of the receiver.
	 * @throws IOException 
	 */
	private CompositeData asCompositeData(CompositeType computeBundleType, int mask) throws IOException {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(BundleStateMBean.IDENTIFIER, getIdentifier());
		if((mask | CustomBundleStateMBean.LOCATION) == mask) {
			items.put(BundleStateMBean.LOCATION, getLocation());
		}
		if((mask | CustomBundleStateMBean.SYMBOLIC_NAME) == mask) {
			items.put(BundleStateMBean.SYMBOLIC_NAME, getSymbolicName());
		}
		if((mask | CustomBundleStateMBean.VERSION) == mask) {
			items.put(BundleStateMBean.VERSION, getVersion());
		}
		if((mask | CustomBundleStateMBean.START_LEVEL) == mask) {
			items.put(BundleStateMBean.START_LEVEL, getStartLevel());
		}
		if((mask | CustomBundleStateMBean.STATE) == mask) {
			items.put(BundleStateMBean.STATE, getState());
		}
		if((mask | CustomBundleStateMBean.LAST_MODIFIED) == mask) {
			items.put(BundleStateMBean.LAST_MODIFIED, getLastModified());
		}
		if((mask | CustomBundleStateMBean.PERSISTENTLY_STARTED) == mask) {
			items.put(BundleStateMBean.PERSISTENTLY_STARTED, isPersistentlyStarted());
		}
		if((mask | CustomBundleStateMBean.REMOVAL_PENDING) == mask) {
			items.put(BundleStateMBean.REMOVAL_PENDING, isRemovalPending());
		}
		if((mask | CustomBundleStateMBean.REQUIRED) == mask) {
			items.put(BundleStateMBean.REQUIRED, isRequired());
		}
		if((mask | CustomBundleStateMBean.FRAGMENT) == mask) {
			items.put(BundleStateMBean.FRAGMENT, isFragment());
		}
		if((mask | CustomBundleStateMBean.REGISTERED_SERVICES) == mask) {
			items.put(BundleStateMBean.REGISTERED_SERVICES, BundleUtil.LongArrayFrom(getRegisteredServices()));
		}
		if((mask | CustomBundleStateMBean.SERVICES_IN_USE) == mask) {
			items.put(BundleStateMBean.SERVICES_IN_USE, BundleUtil.LongArrayFrom(getServicesInUse()));
		}
		if((mask | CustomBundleStateMBean.HEADERS) == mask) {
			items.put(BundleStateMBean.HEADERS, headerTable(getHeaders()));
		}
		if((mask | CustomBundleStateMBean.EXPORTED_PACKAGES) == mask) {
			items.put(BundleStateMBean.EXPORTED_PACKAGES, getExportedPackages());
		}
		if((mask | CustomBundleStateMBean.IMPORTED_PACKAGES) == mask) {
			items.put(BundleStateMBean.IMPORTED_PACKAGES, getImportedPackages());
		}
		if((mask | CustomBundleStateMBean.FRAGMENTS) == mask) {
			items.put(BundleStateMBean.FRAGMENTS, BundleUtil.LongArrayFrom(getFragments()));
		}
		if((mask | CustomBundleStateMBean.HOSTS) == mask) {
			items.put(BundleStateMBean.HOSTS, BundleUtil.LongArrayFrom(getHosts()));
		}
		if((mask | CustomBundleStateMBean.REQUIRING_BUNDLES) == mask) {
			items.put(BundleStateMBean.REQUIRING_BUNDLES, BundleUtil.LongArrayFrom(getRequiringBundles()));
		}
		if((mask | CustomBundleStateMBean.REQUIRED_BUNDLES) == mask) {
			items.put(BundleStateMBean.REQUIRED_BUNDLES, BundleUtil.LongArrayFrom(getRequiredBundles()));
		}

		try {
			return new CompositeDataSupport(computeBundleType, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form bundle open data", e);
		}
	}

	/**
	 * @return The list of exported packages by this bundle, in the form of
	 *         <packageName>;<version>
	 * 
	 */
	private String[] getExportedPackages() {
		return BundleUtil.getBundleExportedPackages(bundle);
	}

	/**
	 * @return the list of identifiers of the bundle fragments which use this
	 *         bundle as a host
	 */
	private long[] getFragments() {
		return  BundleUtil.getBundleFragments(bundle);			
	}

	/**
	 * @return the map of headers for this bundle
	 */
	private Map<String, String> getHeaders() {
		return BundleUtil.getBundleHeaders(bundle);
	}

	/**
	 * @return list of identifiers of the bundles which host this fragment
	 */
	private long[] getHosts() {
		return BundleUtil.getBundleHosts(bundle);
	}

	/**
	 * @return the identifier of this bundle
	 */
	private long getIdentifier() {
		return bundle.getBundleId();
	}

	/**
	 * @return The list of imported packages by this bundle, in the form of
	 *         <packageName>;<version>
	 */
	private String[] getImportedPackages() {
		return BundleUtil.getBundleImportedPackages(bundle);
	}

	/**
	 * @return the last modified time of this bundle
	 */
	private long getLastModified() {
		return bundle.getLastModified();
	}

	/**
	 * @return the name of this bundle
	 */
	private String getLocation() {
		return bundle.getLocation();
	}

	/**
	 * @return the list of identifiers of the services registered by this bundle
	 */
	private long[] getRegisteredServices() {
		return BundleUtil.serviceIds(bundle.getRegisteredServices());
	}

	/**
	 * @return the list of identifiers of bundles required by this bundle
	 * @throws IOException 
	 */
	private long[] getRequiredBundles() throws IOException {
		return BundleUtil.getRequiredBundles(bundle);
	}

	/**
	 * @return the list of identifiers of bundles which require this bundle
	 * @throws IOException 
	 */
	private long[] getRequiringBundles() throws IOException {
		return BundleUtil.getRequiringBundles(bundle);
	}

	/**
	 * @return the list of identifiers of services in use by this bundle
	 */
	private long[] getServicesInUse() {
		return BundleUtil.serviceIds(bundle.getServicesInUse());
	}

	/**
	 * @return the start level of this bundle
	 */
	private int getStartLevel() {
		return BundleUtil.getBundleStartLevel(bundle);
	}

	/**
	 * @return the state of this bundle
	 */
	private String getState() {
		return BundleUtil.getBundleState(bundle);
	}

	/**
	 * @return the symbolic name of this bundle
	 */
	private String getSymbolicName() {
		return bundle.getSymbolicName();
	}

	/**
	 * @return the version of this bundle
	 */
	private String getVersion() {
		return bundle.getVersion().toString();
	}

	/**
	 * @return true if this bundle represents a fragment
	 */
	private boolean isFragment() {
		return BundleUtil.isBundleFragment(bundle);
	}

	/**
	 * @return true if this bundle is persistently started
	 */
	private boolean isPersistentlyStarted() {
		return BundleUtil.isBundlePersistentlyStarted(bundle);
	}

	/**
	 * @return true if this bundle is pending removal
	 */
	private boolean isRemovalPending() {
		return BundleUtil.isRemovalPending(bundle);
	}

	/**
	 * @return true if this bundle is required
	 */
	private boolean isRequired() {
		return BundleUtil.isRequired(bundle);
	}
	
}
