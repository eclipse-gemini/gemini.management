/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.gemini.mgmt.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gemini.mgmt.framework.CustomBundleWiringStateMBean;
import org.eclipse.gemini.mgmt.framework.internal.OSGiBundleWiring;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

/**
 * Util methods when working with a {@link BundleWiring}
 *
 */
public final class BundleWiringUtil {
	
	/**
	 * Convert a key-value directive in to the required format for representation over JMX
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Map<String, ?> getDirectiveKeyValueItem(String key, Object value){
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(CustomBundleWiringStateMBean.KEY, key);
		items.put(CustomBundleWiringStateMBean.VALUE, value);
		return items;
	}
	
	/**
	 * Add all related wirings to the provided map.
	 * 
	 * @param mappings of 
	 * @param wiring
	 * @param namespace
	 */
	public static void processWiring(Map<BundleRevision, OSGiBundleWiring> mappings, BundleWiring wiring, String namespace){
		BundleRevision bundleRevision = wiring.getRevision();
		if(!mappings.containsKey(bundleRevision)) {
			mappings.put(bundleRevision, new OSGiBundleWiring(wiring));
			processRequiredWirings(mappings, wiring, namespace);
			processProvidedWirings(mappings, wiring, namespace);
		}
	}
	
	private static void processRequiredWirings(Map<BundleRevision, OSGiBundleWiring> mappings, BundleWiring wiring, String namespace){
		List<BundleWire> requiredWires = wiring.getRequiredWires(namespace);
		if(requiredWires != null) {
			for (BundleWire bundleWire : requiredWires) {
				BundleWiringUtil.processWiring(mappings, bundleWire.getProviderWiring(), namespace);
			}
		}
	}
	
	private static void processProvidedWirings(Map<BundleRevision, OSGiBundleWiring> mappings, BundleWiring wiring, String namespace){
		List<BundleWire> providedWires = wiring.getProvidedWires(namespace);
		if(providedWires != null) {
			for (BundleWire bundleWire : providedWires) {
				BundleWiringUtil.processWiring(mappings, bundleWire.getRequirerWiring(), namespace);
			}
		}
	}
}
