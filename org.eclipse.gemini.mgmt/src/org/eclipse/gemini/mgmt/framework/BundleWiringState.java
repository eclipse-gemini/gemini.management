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
package org.eclipse.gemini.mgmt.framework;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

import org.eclipse.gemini.mgmt.framework.internal.OSGiBundleRevision;
import org.eclipse.gemini.mgmt.framework.internal.OSGiBundleRevisionIdTracker;
import org.eclipse.gemini.mgmt.framework.internal.OSGiBundleWiring;
import org.eclipse.gemini.mgmt.internal.BundleWiringUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleRevisions;
import org.osgi.framework.wiring.BundleWiring;

/**
 * MBean that represents the current wiring state of the 
 */
public final class BundleWiringState implements CustomBundleWiringStateMBean {

	private final BundleContext bundleContext;

	/**
	 * 
	 * @param bundleContext
	 */
	public BundleWiringState(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData[] getCurrentRevisionDeclaredRequirements(long bundleId, String namespace) throws IOException {
		namespace = processNamespace(namespace);
		BundleRevision bundleRevision = getBundle(bundleId).adapt(BundleRevision.class);
		return new OSGiBundleRevision(bundleRevision).requirementsAsCompositeDataArray(namespace);
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData[] getCurrentRevisionDeclaredCapabilities(long bundleId, String namespace) throws IOException {
		namespace = processNamespace(namespace);
		BundleRevision bundleRevision = getBundle(bundleId).adapt(BundleRevision.class);
		return new OSGiBundleRevision(bundleRevision).capabilitiesAsCompositeDataArray(namespace);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public CompositeData getCurrentWiring(long bundleId, String namespace) throws IOException {
		namespace = processNamespace(namespace);
		BundleWiring wiring = getBundle(bundleId).adapt(BundleWiring.class);
		return new OSGiBundleWiring(wiring).asCompositeData(namespace);
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getCurrentWiringClosure(long rootBundleId, String namespace) throws IOException {
		namespace = processNamespace(namespace);
		BundleWiring wiring = getBundle(rootBundleId).adapt(BundleWiring.class);
		Map<BundleRevision, OSGiBundleWiring> mappings = new HashMap<BundleRevision, OSGiBundleWiring>();
		BundleWiringUtil.processWiring(mappings, wiring, namespace);
		TabularDataSupport table = new TabularDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISIONS_WIRINGS_CLOSURES_TYPE);
		OSGiBundleRevisionIdTracker revisionTracker = new OSGiBundleRevisionIdTracker();
		for(Entry<BundleRevision, OSGiBundleWiring> osgiBundleWiring : mappings.entrySet()){
			table.put(osgiBundleWiring.getValue().asCompositeData(namespace, osgiBundleWiring.getKey().getBundle().getBundleId(), revisionTracker));
		}
		return table;
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getRevisionsDeclaredRequirements(long bundleId, String namespace) throws IOException {
		namespace = processNamespace(namespace);
		List<BundleRevision> bundleRevisions = getBundle(bundleId).adapt(BundleRevisions.class).getRevisions();
		TabularDataSupport table = new TabularDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISIONS_REQUIREMENTS_TYPE);
		int revisionCounter = 0;
		for (BundleRevision bundleRevision : bundleRevisions) {
			table.put(new OSGiBundleRevision(bundleRevision).requirementsAsCompositeData(namespace, revisionCounter));
			revisionCounter++;
		}
		return table;
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getRevisionsDeclaredCapabilities(long bundleId, String namespace) throws IOException {
		namespace = processNamespace(namespace);
		List<BundleRevision> bundleRevisions = getBundle(bundleId).adapt(BundleRevisions.class).getRevisions();
		TabularDataSupport table = new TabularDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISIONS_CAPABILITIES_TYPE);
		int revisionCounter = 0;
		for (BundleRevision bundleRevision : bundleRevisions) {
			table.put(new OSGiBundleRevision(bundleRevision).capabilitiesAsCompositeData(namespace, revisionCounter));
			revisionCounter++;
		}
		return table;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public TabularData getRevisionsWiring(long bundleId, String namespace) throws IOException {
		namespace = processNamespace(namespace);
		List<BundleRevision> bundleRevisions = getBundle(bundleId).adapt(BundleRevisions.class).getRevisions();
		TabularDataSupport table = new TabularDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISIONS_WIRINGS_TYPE);
		OSGiBundleRevisionIdTracker revisionTracker = new OSGiBundleRevisionIdTracker();
		for (BundleRevision bundleRevision : bundleRevisions) {
			table.put(new OSGiBundleWiring(bundleRevision.getWiring()).asCompositeData(namespace, revisionTracker));
		}
		return table;
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getRevisionsWiringClosure(long rootBundleId, String namespace) throws IOException {
		namespace = processNamespace(namespace);
		List<BundleRevision> bundleRevisions = getBundle(rootBundleId).adapt(BundleRevisions.class).getRevisions();
		Map<BundleRevision, OSGiBundleWiring> mappings = new HashMap<BundleRevision, OSGiBundleWiring>();
		for (BundleRevision bundleRevision : bundleRevisions) {
			BundleWiringUtil.processWiring(mappings, bundleRevision.getWiring(), namespace);
		}
		TabularDataSupport table = new TabularDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISIONS_WIRINGS_CLOSURES_TYPE);
		OSGiBundleRevisionIdTracker revisionTracker = new OSGiBundleRevisionIdTracker();
		for(Entry<BundleRevision, OSGiBundleWiring> osgiBundleWiring : mappings.entrySet()){
			table.put(osgiBundleWiring.getValue().asCompositeData(namespace, osgiBundleWiring.getKey().getBundle().getBundleId(), revisionTracker));
		}
		return table;
	}
	
	// End of MBean methods

	private Bundle getBundle(long bundleId) throws IOException {
		Bundle b = bundleContext.getBundle(bundleId);
		if (b == null) {
			throw new IOException("Bundle with id: " + bundleId + " does not exist");
		}
		return b;
	}
	
	private String processNamespace(String namespace){
		if(CustomBundleWiringStateMBean.ALL_NAMESPACE.equals(namespace)){
			return null;
		}
		return namespace;
	}
	
}