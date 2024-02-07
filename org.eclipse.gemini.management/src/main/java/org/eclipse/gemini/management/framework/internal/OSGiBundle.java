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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

import org.eclipse.gemini.management.internal.BundleUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
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
	
	private Bundle bundle;

	/**
	 * Construct an OSGiBundle representation
	 * 
	 * @param b - the Bundle to represent
	 */
	public OSGiBundle(Bundle b) {
		this.bundle = b;
	}

	public static TabularData tableFrom(List<OSGiBundle> bundles, String... bundleTypeItems) throws IOException {
		List<String> bundleTypes = Arrays.asList(bundleTypeItems);
		TabularDataSupport table = new TabularDataSupport(BundleStateMBean.BUNDLES_TYPE);
		for (OSGiBundle bundle : bundles) {
			table.put(bundle.asCompositeData(bundleTypes));
		}
		return table;
	}
	
	private CompositeData asCompositeData(List<String> bundleTypes) throws IOException {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(BundleStateMBean.IDENTIFIER, getIdentifier());
		items.put(BundleStateMBean.LOCATION, 				bundleTypes.contains(BundleStateMBean.LOCATION) ? 				getLocation() : null);
		items.put(BundleStateMBean.SYMBOLIC_NAME, 			bundleTypes.contains(BundleStateMBean.SYMBOLIC_NAME) ? 			getSymbolicName() : null);
		items.put(BundleStateMBean.VERSION, 				bundleTypes.contains(BundleStateMBean.VERSION) ? 				getVersion() : null);
		items.put(BundleStateMBean.START_LEVEL, 			bundleTypes.contains(BundleStateMBean.START_LEVEL) ? 			getStartLevel() : null);
		items.put(BundleStateMBean.STATE, 					bundleTypes.contains(BundleStateMBean.STATE) ? 					getState() : null);
		items.put(BundleStateMBean.LAST_MODIFIED, 			bundleTypes.contains(BundleStateMBean.LAST_MODIFIED) ? 			getLastModified() : null);
		items.put(BundleStateMBean.PERSISTENTLY_STARTED, 	bundleTypes.contains(BundleStateMBean.PERSISTENTLY_STARTED) ? 	isPersistentlyStarted() : null);
		items.put(BundleStateMBean.ACTIVATION_POLICY_USED, 	bundleTypes.contains(BundleStateMBean.ACTIVATION_POLICY_USED) ? isActivationPolicyUsed() : null);
		items.put(BundleStateMBean.REMOVAL_PENDING, 		bundleTypes.contains(BundleStateMBean.REMOVAL_PENDING) ? 		isRemovalPending() : null);
		items.put(BundleStateMBean.REQUIRED, 				bundleTypes.contains(BundleStateMBean.REQUIRED) ? 				isRequired() : null);
		items.put(BundleStateMBean.FRAGMENT, 				bundleTypes.contains(BundleStateMBean.FRAGMENT) ? 				isFragment() : null);
		items.put(BundleStateMBean.REGISTERED_SERVICES, 	bundleTypes.contains(BundleStateMBean.REGISTERED_SERVICES) ? 	getRegisteredServices() : null);
		items.put(BundleStateMBean.SERVICES_IN_USE, 		bundleTypes.contains(BundleStateMBean.SERVICES_IN_USE) ? 		getServicesInUse() : null);
		items.put(BundleStateMBean.HEADERS, 				bundleTypes.contains(BundleStateMBean.HEADERS) ? 				getHeaders() : null);
		items.put(BundleStateMBean.EXPORTED_PACKAGES, 		bundleTypes.contains(BundleStateMBean.EXPORTED_PACKAGES) ? 		getExportedPackages() : null);
		items.put(BundleStateMBean.IMPORTED_PACKAGES, 		bundleTypes.contains(BundleStateMBean.IMPORTED_PACKAGES) ? 		getImportedPackages() : null);
		items.put(BundleStateMBean.FRAGMENTS, 				bundleTypes.contains(BundleStateMBean.FRAGMENTS) ? 				getFragments() : null);
		items.put(BundleStateMBean.HOSTS, 					bundleTypes.contains(BundleStateMBean.HOSTS) ? 					getHosts() : null);
		items.put(BundleStateMBean.REQUIRING_BUNDLES, 		bundleTypes.contains(BundleStateMBean.REQUIRING_BUNDLES) ? 		getRequiringBundles() : null);
		items.put(BundleStateMBean.REQUIRED_BUNDLES, 		bundleTypes.contains(BundleStateMBean.REQUIRED_BUNDLES) ? 		getRequiredBundles() : null);
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
		items.put(BundleStateMBean.ACTIVATION_POLICY_USED, isActivationPolicyUsed());
		items.put(BundleStateMBean.REMOVAL_PENDING, isRemovalPending());
		items.put(BundleStateMBean.REQUIRED, isRequired());
		items.put(BundleStateMBean.FRAGMENT, isFragment());
		items.put(BundleStateMBean.REGISTERED_SERVICES, getRegisteredServices());
		items.put(BundleStateMBean.SERVICES_IN_USE, getServicesInUse());
		items.put(BundleStateMBean.HEADERS, headerTable(getHeaders()));
		items.put(BundleStateMBean.EXPORTED_PACKAGES, getExportedPackages());
		items.put(BundleStateMBean.IMPORTED_PACKAGES, getImportedPackages());
		items.put(BundleStateMBean.FRAGMENTS, getFragments());
		items.put(BundleStateMBean.HOSTS, getHosts());
		items.put(BundleStateMBean.REQUIRING_BUNDLES, getRequiringBundles());
		items.put(BundleStateMBean.REQUIRED_BUNDLES, getRequiredBundles());
		try {
			return new CompositeDataSupport(BundleStateMBean.BUNDLE_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form bundle open data", e);
		}
	}
	
	/**
	 * Answer the TabularData representing the supplied map of bundle headers
	 * 
	 * @param headers
	 * @return the bundle headers
	 */
	public static TabularData headerTable(Dictionary<String, String> headersDictionary) {
		TabularDataSupport table = new TabularDataSupport(BundleStateMBean.HEADERS_TYPE);
		for(Enumeration<String> headers = headersDictionary.keys(); headers.hasMoreElements();) {
			String key = (String) headers.nextElement();
			table.put(getHeaderCompositeData(key, (String) headersDictionary.get(key)));
		}
		return table;
	}

	private static CompositeData getHeaderCompositeData(String key, String value) {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(BundleStateMBean.KEY, key);
		items.put(BundleStateMBean.VALUE, value);		
		try {
			return new CompositeDataSupport(BundleStateMBean.HEADER_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form bundle header open data", e);
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
	 * @return the map of headers for this bundle
	 */
	private Dictionary<String, String> getHeaders() {
		return bundle.getHeaders();
	}

	/**
	 * @return the list of identifiers of the bundle fragments which use this
	 *         bundle as a host
	 */
	private Long[] getFragments() {
		if (isFragment()) {
			return new Long[0];
		}
		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		List<BundleWire> requiredWires = wiring.getRequiredWires(BundleRevision.HOST_NAMESPACE);
	    return OSGiBundle.bundleWiresToProviderIds(requiredWires);
	}

	/**
	 * @return list of identifiers of the bundles which host this fragment
	 */
	private Long[] getHosts() {
		if (isFragment()) {
			return new Long[0];
		}
		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		List<BundleWire> providedWires = wiring.getProvidedWires(BundleRevision.HOST_NAMESPACE);
        return OSGiBundle.bundleWiresToRequirerIds(providedWires);
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
	private Long[] getRegisteredServices() {
		return serviceIds(bundle.getRegisteredServices());
	}
	
	/**
	 * @return the list of identifiers of bundles required by this bundle
	 * @throws IOException 
	 */
	private Long[] getRequiredBundles() throws IOException {
        return BundleUtil.getRequiredBundles(bundle);
	}

	/**
	 * @return the list of identifiers of bundles which require this bundle
	 * @throws IOException 
	 */
	private Long[] getRequiringBundles() throws IOException {
        return BundleUtil.getRequiringBundles(bundle);
	}

	/**
	 * @return the list of identifiers of services in use by this bundle
	 */
	private Long[] getServicesInUse() {
		return serviceIds(bundle.getServicesInUse());
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
	 * @return true if this bundle is persistently started
	 */
	private boolean isActivationPolicyUsed() {
		return BundleUtil.isBundleActivationPolicyUsed(bundle);
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

	public static Long[] bundleWiresToRequirerIds(List<BundleWire> wires){
        Long[] requirerIds = new Long[wires.size()];
        int i = 0;
        for (BundleWire bundleWire : wires) {
            requirerIds[i] = bundleWire.getRequirerWiring().getBundle().getBundleId();
            i++;
        }
        return requirerIds;
	}

	public static Long[] bundleWiresToProviderIds(List<BundleWire> wires){
        Long[] providerIds = new Long[wires.size()];
        int i = 0;
        for (BundleWire bundleWire : wires) {
            providerIds[i] = bundleWire.getProviderWiring().getBundle().getBundleId();
            i++;
        }
        return providerIds;
	}

	public static Long[] serviceIds(ServiceReference<?>[] refs) {
		if (refs == null) {
			return new Long[0];
		}
		List<Long> idsList = new ArrayList<Long>();
		for (int i = 0; i < refs.length; i++) {
			Object serviceIdProperty = refs[i].getProperty(Constants.SERVICE_ID);
			if(serviceIdProperty != null){
				idsList.add((Long) serviceIdProperty);
			}
		}
		return idsList.toArray(new Long[idsList.size()]);
	}
	
}
