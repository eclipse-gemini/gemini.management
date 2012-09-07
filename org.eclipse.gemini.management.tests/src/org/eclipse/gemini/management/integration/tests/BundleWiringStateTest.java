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
package org.eclipse.gemini.management.integration.tests;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.eclipse.gemini.management.framework.BundleWiringState;
import org.eclipse.gemini.management.framework.ServiceState;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.jmx.framework.wiring.BundleWiringStateMBean;

/**
 * Integration tests for the {@link BundleWiringState} implementation of {@link BundleWiringStateMBean}
 *
 */
public final class BundleWiringStateTest extends AbstractOSGiMBeanTest {

	private CompositeData wireInfo;
	private Object key;
	private Object[] keysArray;
	private Long bundleId;
	//private Integer bundleRevisionId;
	private CompositeData[] capabilities;
	private CompositeData[] requirements;
	private CompositeData[] revisionProvidedWires;
	private CompositeData[] revisionRequiredWires;

	public BundleWiringStateTest() {
		super.mBeanObjectName = BundleWiringStateMBean.OBJECTNAME;
	}
	
	@Before
	public void before(){
		this.wireInfo = null;
		this.key = null;
		this.keysArray = null;	
		this.bundleId = null;
		//this.bundleRevisionId = null;
		this.capabilities = null;
		this.requirements = null;
		this.revisionProvidedWires = null;
		this.revisionRequiredWires = null;
	}
	
	@Test
	public void currentWiringClosureTest() throws Exception {
		TabularData table = jmxFetchData("getCurrentWiringClosure", new Object[]{new Long(0), BundleRevision.PACKAGE_NAMESPACE}, new String[]{"long", "java.lang.String"}, TabularData.class);
		Set<?> keys = table.keySet();
		Iterator<?> iter = keys.iterator();
		BundleContext bc = FrameworkUtil.getBundle(ServiceState.class).getBundleContext();
		while (iter.hasNext()) {
			key = iter.next();
			keysArray = ((Collection<?>) key).toArray();
			wireInfo = table.get(keysArray);
			
			
			this.bundleId = (Long) wireInfo.get(BundleWiringStateMBean.BUNDLE_ID);
			//this.bundleRevisionId = (Integer) wireInfo.get(CustomBundleWiringStateMBean.BUNDLE_REVISION_ID);
			this.capabilities = (CompositeData[]) wireInfo.get(BundleWiringStateMBean.CAPABILITIES);
			this.requirements = (CompositeData[]) wireInfo.get(BundleWiringStateMBean.REQUIREMENTS);
			this.revisionProvidedWires = (CompositeData[]) wireInfo.get(BundleWiringStateMBean.PROVIDED_WIRES);
			this.revisionRequiredWires = (CompositeData[]) wireInfo.get(BundleWiringStateMBean.REQUIRED_WIRES);
			

			BundleWiring wiring = bc.getBundle(this.bundleId).adapt(BundleWiring.class);

			assertEquals(wiring.getCapabilities(BundleRevision.PACKAGE_NAMESPACE).size(), this.capabilities.length);
			assertEquals(wiring.getRequirements(BundleRevision.PACKAGE_NAMESPACE).size(), this.requirements.length);
			assertEquals(wiring.getProvidedWires(BundleRevision.PACKAGE_NAMESPACE).size(), this.revisionProvidedWires.length);
			assertEquals(wiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE).size(), this.revisionRequiredWires.length);
		}
	}
	
}
