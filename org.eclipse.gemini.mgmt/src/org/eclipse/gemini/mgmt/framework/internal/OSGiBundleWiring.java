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
package org.eclipse.gemini.mgmt.framework.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;

import org.eclipse.gemini.mgmt.framework.CustomBundleWiringStateMBean;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

public class OSGiBundleWiring {

	private final BundleWiring wiring;

	public OSGiBundleWiring(BundleWiring wiring) {
		this.wiring = wiring;
	}
	
	public CompositeData asCompositeData(String namespace) {
		Map<String, Object> items = this.getItems(namespace);
		try {
			return new CompositeDataSupport(CustomBundleWiringStateMBean.BUNDLE_WIRING_TYPE, items);
		} catch (OpenDataException e) {
			e.printStackTrace(System.out);
			throw new IllegalStateException("Cannot form bundle wiring open data", e);
		}
	}

	public CompositeData asCompositeData(String namespace, int revisionCounter) {
		Map<String, Object> items = this.getItems(namespace);
		items.put(CustomBundleWiringStateMBean.BUNDLE_REVISION_ID, revisionCounter);
		try {
			return new CompositeDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISION_WIRING_TYPE, items);
		} catch (OpenDataException e) {
			e.printStackTrace(System.out);
			throw new IllegalStateException("Cannot form bundle revisions wirings open data", e);
		}
	}
	
	private Map<String, Object> getItems(String namespace) {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(CustomBundleWiringStateMBean.BUNDLE_ID, wiring.getBundle().getBundleId());
		
		List<BundleRequirement> requirements = wiring.getRequirements(namespace);
		List<CompositeData> requirementsCompositeDate = new ArrayList<CompositeData>();
		for (BundleRequirement bundleRequirement : requirements) {
			requirementsCompositeDate.add(new OSGiBundleRequirement(bundleRequirement).asCompositeData());
		}
		
		List<BundleCapability> capabilities = wiring.getCapabilities(namespace);
		List<CompositeData> capabilitiesCompositeDate = new ArrayList<CompositeData>();
		for (BundleCapability bundleCapability : capabilities) {
			capabilitiesCompositeDate.add(new OSGiBundleCapability(bundleCapability).asCompositeData());
		}
		
		List<BundleWire> requiredWires = wiring.getRequiredWires(namespace);
		List<CompositeData> requiredWiresCompositeDate = new ArrayList<CompositeData>();
		for (BundleWire bundleWire : requiredWires) {
			requiredWiresCompositeDate.add(new OSGiBundleWire(bundleWire).asCompositeData());
		}
		
		List<BundleWire> providedWires = wiring.getProvidedWires(namespace);
		List<CompositeData> providedWiresCompositeDate = new ArrayList<CompositeData>();
		for (BundleWire bundleWire : providedWires) {
			providedWiresCompositeDate.add(new OSGiBundleWire(bundleWire).asCompositeData());
		}
		
		items.put(CustomBundleWiringStateMBean.REQUIREMENTS, requirementsCompositeDate.toArray(new CompositeData[requirementsCompositeDate.size()]));
		items.put(CustomBundleWiringStateMBean.CAPABILITIES, capabilitiesCompositeDate.toArray(new CompositeData[capabilitiesCompositeDate.size()]));
		items.put(CustomBundleWiringStateMBean.REQUIRED_WIRES, requiredWiresCompositeDate.toArray(new CompositeData[requiredWiresCompositeDate.size()]));
		items.put(CustomBundleWiringStateMBean.PROVIDED_WIRES, providedWiresCompositeDate.toArray(new CompositeData[providedWiresCompositeDate.size()]));
		return items;
	}
	
}
