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
import org.osgi.framework.wiring.BundleRevision;

/**
 * Utility methods for retrieving JMX data from a {@link BundleRevision}
 * 
 */
public class OSGiBundleRevision {

	private final BundleRevision bundleRevision;

	/**
	 * @param bundleRevision
	 */
	public OSGiBundleRevision(BundleRevision bundleRevision) {
		this.bundleRevision = bundleRevision;
	}
	
	/**
	 * @param namespace - namespace to retrieve capabilities from
	 * @return {@link CompositeData} representation of the capabilities
	 */
	public CompositeData capabilitiesAsCompositeData(String namespace){
		Map<String, Object> items = this.getBundleCapabilityItems(namespace);
		try {
			return new CompositeDataSupport(CustomBundleWiringStateMBean.BUNDLE_CAPABILITIES_TYPE, items);
		} catch (OpenDataException e) {
			e.printStackTrace(System.out);
			throw new IllegalStateException("Cannot form bundle capabilities open data", e);
		}
	}
	
	/**
	 * @param namespace - namespace to retrieve requirements from
	 * @return {@link CompositeData} representation of the requirements
	 */
	public CompositeData requirementsAsCompositeData(String namespace){
		Map<String, Object> items = this.getBundleRequirementItems(namespace);
		try {
			return new CompositeDataSupport(CustomBundleWiringStateMBean.BUNDLE_REQUIREMENTS_TYPE, items);
		} catch (OpenDataException e) {
			e.printStackTrace(System.out);
			throw new IllegalStateException("Cannot form bundle requirements open data", e);
		}
	}

	/**
	 * @param namespace - namespace to retrieve capabilities from
	 * @param revisionCounter - an id number for this revision
	 * @return {@link CompositeData} representation of the capabilities
	 */
	public CompositeData capabilitiesAsCompositeData(String namespace, int revisionCounter){
		Map<String, Object> items = this.getBundleCapabilityItems(namespace);
		items.put(CustomBundleWiringStateMBean.BUNDLE_REVISION_ID, revisionCounter);
		try {
			return new CompositeDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISION_CAPABILITIES_TYPE, items);
		} catch (OpenDataException e) {
			e.printStackTrace(System.out);
			throw new IllegalStateException("Cannot form bundle revision capabilities open data", e);
		}
	}
	
	/**
	 * @param namespace - namespace to retrieve requirements from
	 * @param revisionCounter - an id number for this revision
	 * @return {@link CompositeData} representation of the requirements
	 */
	public CompositeData requirementsAsCompositeData(String namespace, int revisionCounter){
		Map<String, Object> items = this.getBundleRequirementItems(namespace);
		items.put(CustomBundleWiringStateMBean.BUNDLE_REVISION_ID, revisionCounter);
		try {
			return new CompositeDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISION_REQUIREMENTS_TYPE, items);
		} catch (OpenDataException e) {
			e.printStackTrace(System.out);
			throw new IllegalStateException("Cannot form bundle revision requirements open data", e);
		}
	}
	
	private Map<String, Object> getBundleCapabilityItems(String namespace){
		Map<String, Object> items = new HashMap<String, Object>();
		List<BundleCapability> declaredCapabilities = bundleRevision.getDeclaredCapabilities(namespace);
		
		CompositeData[] requirementsCompositeDatas = new CompositeData[declaredCapabilities.size()];
		int i = 0;
		for (BundleCapability bundleCapability : declaredCapabilities) {
			requirementsCompositeDatas[i] = new OSGiBundleCapability(bundleCapability).asCompositeData();
			i++;
		}
		items.put(CustomBundleWiringStateMBean.BUNDLE_ID, this.bundleRevision.getBundle().getBundleId());
		items.put(CustomBundleWiringStateMBean.CAPABILITIES, requirementsCompositeDatas);
		return items;
	}
	
	private Map<String, Object> getBundleRequirementItems(String namespace){
		Map<String, Object> items = new HashMap<String, Object>();
		List<BundleRequirement> declaredRequirements = bundleRevision.getDeclaredRequirements(namespace);
		
		CompositeData[] requirementsCompositeDatas = new CompositeData[declaredRequirements.size()];
		int i = 0;
		for (BundleRequirement bundleRequirement : declaredRequirements) {
			requirementsCompositeDatas[i] = new OSGiBundleRequirement(bundleRequirement).asCompositeData();
			i++;
		}
		
		items.put(CustomBundleWiringStateMBean.BUNDLE_ID, this.bundleRevision.getBundle().getBundleId());
		items.put(CustomBundleWiringStateMBean.REQUIREMENTS, requirementsCompositeDatas);
		return items;
	}

}
