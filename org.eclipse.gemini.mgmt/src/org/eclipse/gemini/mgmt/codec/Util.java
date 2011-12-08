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

package org.eclipse.gemini.mgmt.codec;

import static org.osgi.framework.Constants.SERVICE_ID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.framework.startlevel.BundleStartLevel;

/**
 * Static utilities
 * 
 */
public class Util {
	/**
	 * Answer the bundle ids of the bundles
	 * 
	 * @param bundles
	 * @return the bundle ids of the bundles
	 */
	public static long[] bundleIds(Bundle[] bundles) {
		if (bundles == null) {
			return new long[0];
		}
		long[] ids = new long[bundles.length];
		for (int i = 0; i < bundles.length; i++) {
			ids[i] = bundles[i].getBundleId();
		}
		return ids;
	}

	/**
	 * 
	 * @param bundleId
	 * @param bundleContext
	 * @return
	 * @throws IOException
	 */
    public static long[] getRequiredBundles(Bundle bundle) throws IOException {
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        List<BundleWire> consumedWires = wiring.getRequiredWires(BundleRevision.BUNDLE_NAMESPACE);
        long[] providerWires = new long[consumedWires.size()];
        int i = 0;
        for (BundleWire bundleWire : consumedWires) {
            providerWires[i] = bundleWire.getProviderWiring().getBundle().getBundleId();
        }
        return providerWires;
    }

    /**
     * 
     * @param bundleId
     * @param bundleContext
     * @return
     * @throws IOException
     */
    public static long[] getRequiringBundles(Bundle bundle) throws IOException {
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        List<BundleWire> providedWirings = wiring.getProvidedWires(BundleRevision.BUNDLE_NAMESPACE);
        long[] consumerWirings = new long[providedWirings.size()];
        int i = 0;
        for (BundleWire bundleWire : providedWirings) {
            consumerWirings[i] = bundleWire.getRequirerWiring().getBundle().getBundleId();
        }
        return consumerWirings;
    }
	
	/**
	 * Answer the string representation of the exported packages of the bundle
	 * 
	 * @param b
	 * @param admin
	 * @return the string representation of the exported packages of the bundle
	 */
	public static String[] getBundleExportedPackages(Bundle bundle) {
		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		List<BundleWire> providedWires = wiring.getProvidedWires(BundleRevision.PACKAGE_NAMESPACE);
		List<String> packages = new ArrayList<String>();
        for(BundleWire wire: providedWires){
        	String packageName = String.format("%s;%s", wire.getCapability().getAttributes().get(BundleRevision.PACKAGE_NAMESPACE), wire.getCapability().getAttributes().get(Constants.VERSION_ATTRIBUTE));
        	if(!packages.contains(packageName)){
        		packages.add(packageName);
        	}
        }
        return packages.toArray(new String[packages.size()]);
	}

	/**
	 * Answer the ids of the fragments hosted by the bundle
	 * 
	 * @param bundle
	 * @param admin
	 * @return the ids of the fragments hosted by the bundle
	 */
	public static long[] getBundleFragments(Bundle bundle) {
		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		List<BundleWire> consumedWires = wiring.getRequiredWires(BundleRevision.HOST_NAMESPACE);
        long[] providerWires = new long[consumedWires.size()];
        int i = 0;
        for (BundleWire bundleWire : consumedWires) {
            providerWires[i] = bundleWire.getProviderWiring().getBundle().getBundleId();
        }
        return providerWires;
	}

	/**
	 * Answer the ids of the hosts this fragment is attached to
	 * 
	 * @param bundle
	 * @param admin
	 * @return the ids of the hosts this fragment is attached to
	 */
	public static long[] getBundleHosts(Bundle bundle) {
		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		List<BundleWire> consumedWires = wiring.getProvidedWires(BundleRevision.HOST_NAMESPACE);
        long[] providerWires = new long[consumedWires.size()];
        int i = 0;
        for (BundleWire bundleWire : consumedWires) {
            providerWires[i] = bundleWire.getRequirerWiring().getBundle().getBundleId();
        }
        return providerWires;
	}
	
	/**
	 * Answer the map of bundle headers
	 * 
	 * @param b
	 * @return the map of bundle headers
	 */
	public static Map<String, String> getBundleHeaders(Bundle b) {
		Map<String, String> headers = new Hashtable<String, String>();
		Dictionary<?, ?> h = b.getHeaders();
		for (Enumeration<?> keys = h.keys(); keys.hasMoreElements();) {
			String key = (String) keys.nextElement();
			headers.put(key, (String) h.get(key));
		}
		return headers;
	}

	/**
	 * Answer the string representation of the packages imported by a bundle
	 * 
	 * @param b
	 * @param bundleContext
	 * @param admin
	 * @return the string representation of the packages imported by a bundle
	 */
	public static String[] getBundleImportedPackages(Bundle bundle) {
		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		List<BundleWire> providedWires = wiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE);
        List<String> packages = new ArrayList<String>();
        for(BundleWire wire: providedWires){
            String packageName = String.format("%s;%s", wire.getCapability().getAttributes().get(BundleRevision.PACKAGE_NAMESPACE), wire.getCapability().getAttributes().get(Constants.VERSION_ATTRIBUTE));
            if(!packages.contains(packageName)){
                packages.add(packageName);
            }
        }
        return packages.toArray(new String[packages.size()]);
	}

	/**
	 * Answer the string representation of the bundle state
	 * 
	 * @param b
	 * @return the string representation of the bundle state
	 */
	public static String getBundleState(Bundle b) {
		switch (b.getState()) {
		case Bundle.ACTIVE:
			return "ACTIVE";
		case Bundle.INSTALLED:
			return "INSTALLED";
		case Bundle.RESOLVED:
			return "RESOLVED";
		case Bundle.STARTING:
			return "STARTING";
		case Bundle.STOPPING:
			return "STOPPING";
		case Bundle.UNINSTALLED:
			return "UNINSTALLED";
		default:
			return "UNKNOWN";
		}
	}

	/**
	 * Answer true if the bundle is a fragment
	 * 
	 * @param bundle
	 * @param admin
	 * @return true if the bundle is a fragment
	 */
	public static boolean isBundleFragment(Bundle bundle) {
		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		return 0 != (wiring.getRevision().getTypes() & BundleRevision.TYPE_FRAGMENT);
	}

	public static int getBundleStartLevel(Bundle bundle) {
		BundleStartLevel startLevel = bundle.adapt(BundleStartLevel.class);
		return startLevel.getStartLevel();
	}
	
	/**
	 * Answer true if the bundle has been persitently started
	 * 
	 * @param bundle
	 * @param sl
	 * @return true if the bundle has been persitently started
	 */
	public static boolean isBundlePersistentlyStarted(Bundle bundle) {
		BundleStartLevel startLevel = bundle.adapt(BundleStartLevel.class);
		return startLevel.isPersistentlyStarted();
	}

	/**
	 * Answer true if the bundle is required
	 * 
	 * @param bundle
	 * @param bc
	 * @return true if the bundle is required
	 */
	public static boolean isRequired(long bundleId, BundleContext bc) {
		BundleWiring wiring = bc.getBundle(bundleId).adapt(BundleWiring.class);
		return wiring.getProvidedWires(BundleRevision.BUNDLE_NAMESPACE).size() > 0;
	}

	/**
	 * Answer true if the bundle is pending removal
	 * 
	 * @param bundle
	 * @param bc
	 * @return true if the bundle is pending removal
	 */
	public static boolean isRemovalPending(long bundleId, BundleContext bc) {
        BundleWiring wiring = bc.getBundle(bundleId).adapt(BundleWiring.class);
        return (!wiring.isCurrent()) && wiring.isInUse();
	}

	/**
	 * Answer the ids of the service references
	 * 
	 * @param refs
	 * @return the ids of the service references
	 */
	public static long[] serviceIds(ServiceReference<?>[] refs) {
		if (refs == null) {
			return new long[0];
		}
		long[] ids = new long[refs.length];
		for (int i = 0; i < refs.length; i++) {
			ids[i] = (Long) refs[i].getProperty(SERVICE_ID);
		}
		return ids;
	}

	/**
	 * Answer a Long array from the supplied array of longs
	 * 
	 * @param array
	 * @return a Long array from the supplied array of longs
	 */
	public static Long[] LongArrayFrom(long[] array) {
		if (array == null) {
			return new Long[0];
		}
		Long[] result = new Long[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

}
