/*******************************************************************************
 * Copyright (c) 2010 Oracle.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at 
 *     http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Hal Hildebrand - Initial JMX support 
 ******************************************************************************/

package org.eclipse.gemini.management.framework.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

import org.osgi.framework.Bundle;
import org.osgi.jmx.framework.PackageStateMBean;

/**
 * <p>
 * This class represents the CODEC for the composite data representing an OSGi
 * <link>ExportedPackage</link>
 * <p>
 * It serves as both the documentation of the type structure and as the
 * codification of the mechanism to convert to/from the CompositeData.
 * <p>
 * The structure of the composite data is:
 * <table border="1">
 * <tr>
 * <td>Name</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>Version</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>PendingRemoval</td>
 * <td>boolean</td>
 * </tr>
 * <tr>
 * <td>BundleIdentifier</td>
 * <td>long</td>
 * </tr>
 * <tr>
 * <td>ImportingBundles</td>
 * <td>Array of long</td>
 * </tr>
 * </table>
 */
public final class OSGiPackage {

	private Long[] exportingBundles;
	private Long[] importingBundles;
	private String name;
	private boolean removalPending;
	private String version;
	
	/**
	 * Construct and OSGiPackage from the supplied data
	 * 
	 * @param name
	 * @param version
	 * @param removalPending
	 * @param exportingBundles
	 * @param importingBundles
	 */
	public OSGiPackage(String name, String version, boolean removalPending, Bundle[] exportingBundles, Bundle[] importingBundles) {
		this.name = name;
		this.version = version;
		this.removalPending = removalPending;
		this.exportingBundles = OSGiPackage.bundleIds(exportingBundles);
		this.importingBundles = OSGiPackage.bundleIds(importingBundles);
	}

	/**
	 * Answer the bundle ids of the bundles
	 * 
	 * @param bundles
	 * @return the bundle ids of the bundles
	 */
	private static Long[] bundleIds(Bundle[] bundles) {
		if (bundles == null) {
			return new Long[0];
		}
		Long[] ids = new Long[bundles.length];
		for (int i = 0; i < bundles.length; i++) {
			ids[i] = bundles[i].getBundleId();
		}
		return ids;
	}
	
	/**
	 * Construct the tabular data from the list of OSGiPacakges
	 * 
	 * @param packages
	 * @return the tabular data representation of the OSGPacakges
	 */
	public static TabularData tableFrom(Set<OSGiPackage> packages) {
		TabularDataSupport table = new TabularDataSupport(PackageStateMBean.PACKAGES_TYPE);
		for (OSGiPackage pkg : packages) {
			table.put(pkg.asCompositeData());
		}
		return table;
	}

	/**
	 * Answer the receiver encoded as CompositeData
	 * 
	 * @return the CompositeData encoding of the receiver.
	 */
	private CompositeData asCompositeData() {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(PackageStateMBean.NAME, name);
		items.put(PackageStateMBean.VERSION, version);
		items.put(PackageStateMBean.REMOVAL_PENDING, removalPending);
		items.put(PackageStateMBean.EXPORTING_BUNDLES, exportingBundles);
		items.put(PackageStateMBean.IMPORTING_BUNDLES, importingBundles);

		try {
			return new CompositeDataSupport(PackageStateMBean.PACKAGE_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form package open data", e);
		}
	}
//
//	/**
//	 * @return the identifier of the exporting bundles
//	 */
//	public long[] getExportingBundles() {
//		return exportingBundles;
//	}
//
//	/**
//	 * @return the list of identifiers of the bundles importing this package
//	 */
//	public long[] getImportingBundles() {
//		return importingBundles;
//	}
//
//	/**
//	 * @return the name of the package
//	 */
//	public String getName() {
//		return name;
//	}
//
//	/**
//	 * @return the version of the package
//	 */
//	public String getVersion() {
//		return version;
//	}
//
//	/**
//	 * @return true if the package is pending removal
//	 */
//	public boolean isRemovalPending() {
//		return removalPending;
//	}
	
}
