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
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;

import org.eclipse.gemini.mgmt.framework.CustomBundleWiringStateMBean;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

/**
 * Utility methods to get a single wire as JXM data
 * 
 */
public final class OSGiBundleWire {

	private final BundleWire wire;

	/**
	 * 
	 * @param wire {@link BundleWire}
	 */
	public OSGiBundleWire(BundleWire wire) {
		this.wire = wire;
	}
	
	public CompositeData asCompositeData() {
		Map<String, Object> items = new HashMap<String, Object>();
		
		BundleWiring providerWiring = wire.getProviderWiring();
		BundleWiring requirerWiring = wire.getRequirerWiring();
		
		if(providerWiring != null && requirerWiring != null){
			items.put(CustomBundleWiringStateMBean.BUNDLE_REQUIREMENT, new OSGiBundleRequirement(wire.getRequirement()).asCompositeData());
			items.put(CustomBundleWiringStateMBean.BUNDLE_CAPABILITY, new OSGiBundleCapability(wire.getCapability()).asCompositeData());
			items.put(CustomBundleWiringStateMBean.PROVIDER_BUNDLE_ID, providerWiring.getBundle().getBundleId());
			items.put(CustomBundleWiringStateMBean.REQUIRER_BUNDLE_ID, requirerWiring.getBundle().getBundleId());
		}
		try {
			return new CompositeDataSupport(CustomBundleWiringStateMBean.BUNDLE_WIRE_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form wire open data", e);
		}
	}

	public CompositeData asCompositeData(OSGiBundleRevisionIdTracker revisionTracker) {
		Map<String, Object> items = new HashMap<String, Object>();

		BundleWiring providerWiring = wire.getProviderWiring();
		BundleWiring requirerWiring = wire.getRequirerWiring();
		
		if(providerWiring != null && requirerWiring != null){
			items.put(CustomBundleWiringStateMBean.BUNDLE_REQUIREMENT, new OSGiBundleRequirement(wire.getRequirement()).asCompositeData());
			items.put(CustomBundleWiringStateMBean.BUNDLE_CAPABILITY, new OSGiBundleCapability(wire.getCapability()).asCompositeData());
			items.put(CustomBundleWiringStateMBean.PROVIDER_BUNDLE_ID, providerWiring.getBundle().getBundleId());
			items.put(CustomBundleWiringStateMBean.PROVIDER_BUNDLE_REVISION_ID, revisionTracker.getRevisionId(providerWiring.getRevision()));
			items.put(CustomBundleWiringStateMBean.REQUIRER_BUNDLE_ID, requirerWiring.getBundle().getBundleId());
			items.put(CustomBundleWiringStateMBean.REQUIRER_BUNDLE_REVISION_ID, revisionTracker.getRevisionId(requirerWiring.getRevision()));
		}
		try {
			return new CompositeDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISION_WIRE_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form wire open data", e);
		}
	}
	
}
