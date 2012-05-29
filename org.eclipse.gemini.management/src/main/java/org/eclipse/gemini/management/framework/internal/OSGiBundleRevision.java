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
package org.eclipse.gemini.management.framework.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;

import org.eclipse.gemini.management.framework.CustomBundleWiringStateMBean;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;

/**
 * Utility methods for retrieving JMX data from a {@link BundleRevision}
 * 
 */
public final class OSGiBundleRevision {

	private final BundleRevision bundleRevision;

	/**
	 * @param bundleRevision
	 */
	public OSGiBundleRevision(BundleRevision bundleRevision) {
		this.bundleRevision = bundleRevision;
	}
	
	/**
	 * @param namespace - namespace to retrieve capabilities from
	 * @return {@link CompositeData} array representation of the capabilities
	 */
	public CompositeData[] capabilitiesAsCompositeDataArray(String namespace){
		return this.getBundleCapabilityItems(namespace);
	}
	
	/**
	 * @param namespace - namespace to retrieve requirements from
	 * @return {@link CompositeData} array representation of the requirements
	 */
	public CompositeData[] requirementsAsCompositeDataArray(String namespace){
		return this.getBundleRequirementItems(namespace);
	}

	/**
	 * @param namespace - namespace to retrieve capabilities from
	 * @param revisionCounter - an id number for this revision
	 * @return {@link CompositeData} representation of the capabilities
	 */
	public CompositeData capabilitiesAsCompositeData(String namespace, int revisionCounter){
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(CustomBundleWiringStateMBean.CAPABILITIES, this.getBundleCapabilityItems(namespace));
		items.put(CustomBundleWiringStateMBean.BUNDLE_REVISION_ID, revisionCounter);
		try {
			return new CompositeDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISION_CAPABILITIES_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form bundle revision capabilities open data", e);
		}
	}
	
	/**
	 * @param namespace - namespace to retrieve requirements from
	 * @param revisionCounter - an id number for this revision
	 * @return {@link CompositeData} representation of the requirements
	 */
	public CompositeData requirementsAsCompositeData(String namespace, int revisionCounter){
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(CustomBundleWiringStateMBean.REQUIREMENTS, this.getBundleRequirementItems(namespace));
		items.put(CustomBundleWiringStateMBean.BUNDLE_REVISION_ID, revisionCounter);
		try {
			return new CompositeDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISION_REQUIREMENTS_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form bundle revision requirements open data", e);
		}
	}
	
	private CompositeData[] getBundleCapabilityItems(String namespace){
		List<BundleCapability> declaredCapabilities = bundleRevision.getDeclaredCapabilities(namespace);
		CompositeData[] requirementsCompositeDatas = new CompositeData[declaredCapabilities.size()];
		for (int i = 0; i < declaredCapabilities.size(); i++) {
			requirementsCompositeDatas[i] = new OSGiBundleCapability(declaredCapabilities.get(i)).asCompositeData();
		}
		return requirementsCompositeDatas;
	}
	
	private CompositeData[] getBundleRequirementItems(String namespace){
		List<BundleRequirement> declaredRequirements = bundleRevision.getDeclaredRequirements(namespace);
		CompositeData[] requirementsCompositeDatas = new CompositeData[declaredRequirements.size()];
		for (int i = 0; i < declaredRequirements.size(); i++) {
			requirementsCompositeDatas[i] = new OSGiBundleRequirement(declaredRequirements.get(i)).asCompositeData();
		}
		return requirementsCompositeDatas;
	}

}
