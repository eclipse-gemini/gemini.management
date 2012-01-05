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
package org.eclipse.gemini.mgmt.integration.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.eclipse.gemini.mgmt.framework.PackageState;
import org.eclipse.gemini.mgmt.framework.ServiceState;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.jmx.framework.PackageStateMBean;
import org.osgi.jmx.framework.ServiceStateMBean;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

import com.sun.org.apache.xerces.internal.impl.Version;

/**
 * Integration tests for the {@link PackageState} implementation {@link PackageStateMBean}
 *
 */
public final class PackageStateTest extends AbstractOSGiMBeanTest {
	
	private CompositeData packageInfo;
	private Object key;
	private Object[] keysArray;
	private Long[] exportingBundles;
	private Long[] importingBundles;
	private String name;
	private Boolean removalPending;
	private String version;
	
	public PackageStateTest() {
		super.mBeanObjectName = PackageStateMBean.OBJECTNAME;
	}
	
	@Before
	public void before(){
		this.packageInfo = null;
		this.key = null;
		this.keysArray = null;
		this.exportingBundles = null;
		this.importingBundles = null;
		this.name = null;
		this.removalPending = null;
		this.version = null;
	}
	
	@Test
	public void listTest() throws Exception {
		TabularData table = jmxFetchData("listPackages", new Object[]{}, new String[]{}, TabularData.class);
		Set<?> keys = table.keySet();
		Iterator<?> iter = keys.iterator();
		BundleContext bundleContext = FrameworkUtil.getBundle(ServiceState.class).getBundleContext();

		PackageAdmin admin = (PackageAdmin) bundleContext.getService(bundleContext.getServiceReference(PackageAdmin.class));
		ExportedPackage[] exportedPackages = admin.getExportedPackages((Bundle) null);
		Map<String, ExportedPackage> packages = new HashMap<String, ExportedPackage>();
		for (ExportedPackage exportedPackage : exportedPackages) {
			packages.put(getPackageIdentifier(exportedPackage.getExportingBundle().getBundleId(), exportedPackage.getName(), exportedPackage.getVersion().toString()), exportedPackage);
		}
		
		while (iter.hasNext()) {
			key = iter.next();
			keysArray = ((Collection<?>) key).toArray();
			packageInfo = table.get(keysArray);
			
			
			this.exportingBundles = (Long[]) packageInfo.get(PackageStateMBean.EXPORTING_BUNDLES);
			this.importingBundles = (Long[]) packageInfo.get(PackageStateMBean.IMPORTING_BUNDLES);
			this.name = (String) packageInfo.get(PackageStateMBean.NAME);
			this.removalPending = (Boolean) packageInfo.get(PackageStateMBean.REMOVAL_PENDING);
			this.version = (String) packageInfo.get(PackageStateMBean.VERSION);
			
			ExportedPackage exportedPackage = packages.get(getPackageIdentifier(this.exportingBundles[0], this.name, this.version));
			assertEquals(exportedPackage.getExportingBundle().getBundleId(), this.exportingBundles[0].longValue());
			
			Bundle[] bundles = exportedPackage.getImportingBundles();
			Long[] ids = new Long[bundles.length];
			for (int i = 0; i < bundles.length; i++) {
				ids[i] = bundles[i].getBundleId();
			}
			Arrays.sort(this.importingBundles);
			Arrays.sort(ids);
			
			assertArrayEquals(ids, this.importingBundles);
			assertEquals(exportedPackage.getName(), this.name);
			assertEquals(exportedPackage.isRemovalPending(), this.removalPending.booleanValue());
			assertEquals(exportedPackage.getVersion().toString(), this.version);


		}
	}
	
	private String getPackageIdentifier(Long bundleId, String name, String version){
		return bundleId + ";" + name + ";" + version;
	}
}
