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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularData;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWiring;

/**
 *
 */
public final class BundleWiringState implements CustomBundleWiringStateMBean {

	private final BundleContext bundleContext;

	public BundleWiringState(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData[] getCurrentRevisionDeclaredRequirements(long bundleId, String namespace) throws IOException {
		BundleWiring wiring = bundleContext.getBundle(bundleId).adapt(BundleWiring.class);
		List<BundleRequirement> requirements = wiring.getRequirements(null);
		List<CompositeData> declaredRequirements = new ArrayList<CompositeData>();
		
		for (BundleRequirement bundleRequirement : requirements) {
			Map<String, ?> requirementsValues = new HashMap<String, Object>();
			
			
			
			declaredRequirements.add(new CompositeDataSupport(CustomBundleWiringStateMBean.REQUIREMENT_TYPE_ARRAY, requirementsValues));
		}
		
		return declaredRequirements.toArray(new CompositeData[declaredRequirements.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData[] getCurrentRevisionDeclaredCapabilities(long bundleId, String namespace) throws IOException {
		BundleWiring wiring = bundleContext.getBundle(bundleId).adapt(BundleWiring.class);
		List<BundleCapability> capabilities = wiring.getCapabilities(null);
		List<CompositeData> declaredCapabilities = new ArrayList<CompositeData>();
		for (BundleCapability bundleCapability : capabilities) {
			
		}
		return declaredCapabilities.toArray(new CompositeData[declaredCapabilities.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData getCurrentWiring(long bundleId, String namespace) throws IOException {
		BundleWiring wiring = bundleContext.getBundle(bundleId).adapt(BundleWiring.class);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getCurrentWiringClosure(long rootBundleId, String namespace) throws IOException {
		BundleWiring wiring = bundleContext.getBundle(rootBundleId).adapt(BundleWiring.class);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getRevisionsDeclaredRequirements(long bundleId, String namespace) throws IOException {
		BundleWiring wiring = bundleContext.getBundle(bundleId).adapt(BundleWiring.class);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getRevisionsDeclaredCapabilities(long bundleId, String namespace) throws IOException {
		BundleWiring wiring = bundleContext.getBundle(bundleId).adapt(BundleWiring.class);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayType getRevisionsWiring(long bundleId, String namespace) throws IOException {
		BundleWiring wiring = bundleContext.getBundle(bundleId).adapt(BundleWiring.class);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayType getRevisionsWiringClosure(long rootBundleId, String namespace) throws IOException {
		BundleWiring wiring = bundleContext.getBundle(rootBundleId).adapt(BundleWiring.class);
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
