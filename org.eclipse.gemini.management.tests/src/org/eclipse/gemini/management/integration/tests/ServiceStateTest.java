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
import static org.junit.Assert.assertArrayEquals;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.eclipse.gemini.management.framework.CustomServiceStateMBean;
import org.eclipse.gemini.management.framework.ServiceState;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.jmx.framework.ServiceStateMBean;

/**
 * Integration tests for the {@link ServiceState} implementation of {@link CustomServiceStateMBean} and {@link ServiceStateMBean}
 *
 */
public final class ServiceStateTest extends AbstractOSGiMBeanTest {

	private CompositeData serviceInfo;
	private Object key;
	private Object[] keysArray;
	private long providingBundle;
	private long serviceId;
	private Long[] usingBundles;
	
	public ServiceStateTest() {
		super.mBeanObjectName = ServiceStateMBean.OBJECTNAME;
		super.addFrameworkAndUUID = true;
	}
	
	@Before
	public void before(){
		this.serviceInfo = null;
		this.key = null;
		this.keysArray = null;
		this.providingBundle = -1;
		this.serviceId = -1;
		this.usingBundles = null;
	}
	
	@Test
	public void listTest() throws Exception {
		TabularData table = jmxFetchData("listServices", new Object[]{}, new String[]{}, TabularData.class);
		Set<?> keys = table.keySet();
		Iterator<?> iter = keys.iterator();
		BundleContext bc = FrameworkUtil.getBundle(ServiceState.class).getBundleContext();
		while (iter.hasNext()) {
			key = iter.next();
			keysArray = ((Collection<?>) key).toArray();
			serviceInfo = table.get(keysArray);

			this.providingBundle = (Long) serviceInfo.get(ServiceStateMBean.BUNDLE_IDENTIFIER);
			this.serviceId = (Long) serviceInfo.get(ServiceStateMBean.IDENTIFIER);
			this.usingBundles = (Long[]) serviceInfo.get(ServiceStateMBean.USING_BUNDLES);

			ServiceReference<?> serviceReference = bc.getAllServiceReferences(null, "(" + Constants.SERVICE_ID + "=" + this.serviceId + ")")[0];
			
			assertEquals(providingBundle, serviceReference.getBundle().getBundleId());
			assertEquals(serviceId, ((Long) serviceReference.getProperty(Constants.SERVICE_ID)).longValue());
			Bundle[] usingBundles2 = serviceReference.getUsingBundles();
			if(usingBundles2 == null){
				assertEquals(0, this.usingBundles.length);
			} else {
				Long[] usingBundlesIds = new Long[usingBundles2.length];
				for (int i = 0; i < usingBundles2.length; i++) {
					usingBundlesIds[i] = usingBundles2[i].getBundleId();
				}
				assertArrayEquals(this.usingBundles, usingBundlesIds);
			}
		}
	}
	
}
