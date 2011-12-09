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

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.osgi.framework.BundleContext;

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
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData[] getCurrentRevisionDeclaredCapabilities(long bundleId, String namespace) throws IOException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData getCurrentWiring(long bundleId, String namespace) throws IOException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getCurrentWiringClosure(long rootBundleId, String namespace) throws IOException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getRevisionsDeclaredRequirements(long bundleId, String namespace) throws IOException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getRevisionsDeclaredCapabilities(long bundleId, String namespace) throws IOException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayType getRevisionsWiring(long bundleId, String namespace) throws IOException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayType getRevisionsWiringClosure(long rootBundleId, String namespace) throws IOException {
		return null;
	}

}
