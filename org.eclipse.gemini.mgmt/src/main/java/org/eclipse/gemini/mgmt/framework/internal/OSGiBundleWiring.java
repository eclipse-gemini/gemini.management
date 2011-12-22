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

/**
 * 
 *
 */
public final class OSGiBundleWiring {

	private final BundleWiring wiring;

	/**
	 * 
	 * @param wiring
	 */
	public OSGiBundleWiring(BundleWiring wiring) {
		this.wiring = wiring;
	}
	
	/**
	 * 
	 * @param namespace
	 * @return
	 */
	public CompositeData asCompositeData(String namespace) {
		Map<String, Object> items = new HashMap<String, Object>();
		this.addCapabilityAndRequirementItems(items, namespace);
		this.addProvidedAndRequiredWireItems(items, namespace);
		try {
			return new CompositeDataSupport(CustomBundleWiringStateMBean.BUNDLE_WIRING_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form bundle wiring open data", e);
		}
	}

	/**
	 * 
	 * @param namespace
	 * @param revisionCounter
	 * @return
	 */
	public CompositeData asCompositeData(String namespace, OSGiBundleRevisionIdTracker revisionTracker) {
		Map<String, Object> items = new HashMap<String, Object>();
		int myRevisionId = revisionTracker.getRevisionId(wiring.getRevision());
		this.addCapabilityAndRequirementItems(items, namespace);
		this.addRevisionedProvidedAndRequiredWireItems(items, namespace, revisionTracker);
		items.put(CustomBundleWiringStateMBean.BUNDLE_REVISION_ID, myRevisionId);
		try {
			return new CompositeDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISION_WIRING_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form bundle revisions wiring open data", e);
		}
	}

	/**
	 * 
	 * @param namespace
	 * @param bundleId
	 * @param revisionCounter
	 * @return
	 */
	public CompositeData asCompositeData(String namespace, long bundleId, OSGiBundleRevisionIdTracker revisionTracker) {
		Map<String, Object> items = new HashMap<String, Object>();
		int myRevisionId = revisionTracker.getRevisionId(wiring.getRevision());
		this.addCapabilityAndRequirementItems(items, namespace);
		this.addRevisionedProvidedAndRequiredWireItems(items, namespace, revisionTracker);
		items.put(CustomBundleWiringStateMBean.BUNDLE_ID, bundleId);
		items.put(CustomBundleWiringStateMBean.BUNDLE_REVISION_ID, myRevisionId);
		try {
			return new CompositeDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISION_WIRING_CLOSURE_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form bundle revisions wiring closures open data", e);
		}
	}

	private void addCapabilityAndRequirementItems(Map<String, Object> items, String namespace) {
		List<BundleRequirement> requirements = wiring.getRequirements(namespace);
		CompositeData[] requirementsCompositeDate = new CompositeData[requirements.size()];
		for (int i = 0; i < requirements.size(); i++) {
			requirementsCompositeDate[i] = new OSGiBundleRequirement(requirements.get(i)).asCompositeData();
		}
		items.put(CustomBundleWiringStateMBean.REQUIREMENTS, requirementsCompositeDate);
		
		List<BundleCapability> capabilities = wiring.getCapabilities(namespace);
		CompositeData[] capabilitiesCompositeDate = new CompositeData[capabilities.size()];
		for (int i = 0; i < capabilities.size(); i++) {
			capabilitiesCompositeDate[i] = new OSGiBundleCapability(capabilities.get(i)).asCompositeData();
		}
		items.put(CustomBundleWiringStateMBean.CAPABILITIES, capabilitiesCompositeDate);
	}

	private void addProvidedAndRequiredWireItems(Map<String, Object> items, String namespace) {
		List<BundleWire> requiredWires = wiring.getRequiredWires(namespace);
		CompositeData[] requiredWiresCompositeDate = new CompositeData[requiredWires.size()];
		for (int i = 0; i < requiredWires.size(); i++) {
			requiredWiresCompositeDate[i] = new OSGiBundleWire(requiredWires.get(i)).asCompositeData();
		}
		items.put(CustomBundleWiringStateMBean.REQUIRED_WIRES, requiredWiresCompositeDate);
		
		List<BundleWire> providedWires = wiring.getProvidedWires(namespace);
		CompositeData[] providedWiresCompositeDate = new CompositeData[providedWires.size()];
		for (int i = 0; i < providedWires.size(); i++) {
			providedWiresCompositeDate[i] = new OSGiBundleWire(providedWires.get(i)).asCompositeData();
		}
		items.put(CustomBundleWiringStateMBean.PROVIDED_WIRES, providedWiresCompositeDate);
	}
	
	private void addRevisionedProvidedAndRequiredWireItems(Map<String, Object> items, String namespace, OSGiBundleRevisionIdTracker revisionTracker) {
		List<BundleWire> requiredWires = wiring.getRequiredWires(namespace);
		CompositeData[] requiredWiresCompositeDate = new CompositeData[requiredWires.size()];
		for (int i = 0; i < requiredWires.size(); i++) {
			requiredWiresCompositeDate[i] = new OSGiBundleWire(requiredWires.get(i)).asCompositeData(revisionTracker);
		}
		items.put(CustomBundleWiringStateMBean.REVISION_REQUIRED_WIRES, requiredWiresCompositeDate);
		
		List<BundleWire> providedWires = wiring.getProvidedWires(namespace);
		CompositeData[] providedWiresCompositeDate = new CompositeData[providedWires.size()];
		for (int i = 0; i < providedWires.size(); i++) {
			providedWiresCompositeDate[i] = new OSGiBundleWire(providedWires.get(i)).asCompositeData(revisionTracker);
		}
		items.put(CustomBundleWiringStateMBean.REVISION_PROVIDED_WIRES, providedWiresCompositeDate);
	}
	
}
