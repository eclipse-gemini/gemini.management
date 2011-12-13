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
import java.util.List;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

import org.eclipse.gemini.mgmt.framework.internal.OSGiBundleRevision;
import org.eclipse.gemini.mgmt.framework.internal.OSGiBundleWiring;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleRevisions;
import org.osgi.framework.wiring.BundleWiring;

/**
 *
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

	/** DONE
	 * {@inheritDoc}
	 */
	public CompositeData getCurrentRevisionDeclaredRequirements(long bundleId, String namespace) throws IOException {
		BundleRevision bundleRevision = getBundle(bundleId).adapt(BundleRevision.class);
		return new OSGiBundleRevision(bundleRevision).requirementsAsCompositeData(namespace);
	}

	/** DONE
	 * {@inheritDoc}
	 */
	public CompositeData getCurrentRevisionDeclaredCapabilities(long bundleId, String namespace) throws IOException {
		BundleRevision bundleRevision = getBundle(bundleId).adapt(BundleRevision.class);
		return new OSGiBundleRevision(bundleRevision).capabilitiesAsCompositeData(namespace);
	}
	
	/** DONE
	 * {@inheritDoc}
	 */
	public CompositeData getCurrentWiring(long bundleId, String namespace) throws IOException {
		BundleWiring wiring = getBundle(bundleId).adapt(BundleWiring.class);
		return new OSGiBundleWiring(wiring).asCompositeData(namespace);
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getCurrentWiringClosure(long rootBundleId, String namespace) throws IOException {
		BundleWiring wiring = getBundle(rootBundleId).adapt(BundleWiring.class);
		OSGiBundleWiring osgiWiring = new OSGiBundleWiring(wiring);
		
		//Navigate the wires and check for cycles
		
		return null;
	}

	/** DONE
	 * {@inheritDoc}
	 */
	public TabularData getRevisionsDeclaredRequirements(long bundleId, String namespace) throws IOException {
		List<BundleRevision> bundleRevisions = getBundle(bundleId).adapt(BundleRevisions.class).getRevisions();
		TabularDataSupport table = new TabularDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISIONS_REQUIREMENTS_TYPE);
		int revisionCounter = 0;
		for (BundleRevision bundleRevision : bundleRevisions) {
			table.put(new OSGiBundleRevision(bundleRevision).requirementsAsCompositeData(namespace, revisionCounter));
			revisionCounter++;
		}
		return table;
	}

	/** DONE
	 * {@inheritDoc}
	 */
	public TabularData getRevisionsDeclaredCapabilities(long bundleId, String namespace) throws IOException {
		List<BundleRevision> bundleRevisions = getBundle(bundleId).adapt(BundleRevisions.class).getRevisions();
		TabularDataSupport table = new TabularDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISIONS_CAPABILITIES_TYPE);
		int revisionCounter = 0;
		for (BundleRevision bundleRevision : bundleRevisions) {
			table.put(new OSGiBundleRevision(bundleRevision).capabilitiesAsCompositeData(namespace, revisionCounter));
			revisionCounter++;
		}
		return table;
	}
	
	/** DONE
	 * {@inheritDoc}
	 */
	public TabularData getRevisionsWiring(long bundleId, String namespace) throws IOException {
		List<BundleRevision> bundleRevisions = getBundle(bundleId).adapt(BundleRevisions.class).getRevisions();
		TabularDataSupport table = new TabularDataSupport(CustomBundleWiringStateMBean.BUNDLE_REVISIONS_WIRINGS_TYPE);
		int revisionCounter = 0;
		for (BundleRevision bundleRevision : bundleRevisions) {
			table.put(new OSGiBundleWiring(bundleRevision.getWiring()).asCompositeData(namespace, revisionCounter));
			revisionCounter++;
		}
		return table;
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getRevisionsWiringClosure(long rootBundleId, String namespace) throws IOException {
		List<BundleRevision> bundleRevisions = getBundle(rootBundleId).adapt(BundleRevisions.class).getRevisions();
		for (BundleRevision bundleRevision : bundleRevisions) {
			OSGiBundleWiring osgiWiring = new OSGiBundleWiring(bundleRevision.getWiring());
			
			//Navigate the wires and check for cycles
		}
		return null;
	}
	
	// End of MBean methods

	private Bundle getBundle(long bundleId) throws IOException {
		Bundle b = bundleContext.getBundle(bundleId);
		if (b == null) {
			throw new IOException("Bundle with id: " + bundleId + " does not exist");
		}
		return b;
	}

}
