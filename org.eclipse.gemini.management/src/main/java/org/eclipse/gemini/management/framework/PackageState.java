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

package org.eclipse.gemini.management.framework;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.management.openmbean.TabularData;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import org.osgi.jmx.framework.PackageStateMBean;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

import org.eclipse.gemini.management.framework.internal.OSGiPackage;

/** 
 * 
 */
@Deprecated
public final class PackageState implements PackageStateMBean {
	
	private PackageAdmin admin;
	
	/**
	 * 
	 * @param bundleContext
	 */
	public PackageState(BundleContext bundleContext) {
		this.admin = (PackageAdmin) bundleContext.getService(bundleContext.getServiceReference(PackageAdmin.class));
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getExportingBundles(String packageName, String version) throws IOException {
		if (packageName == null) {
			throw new IOException("Package name cannot be null");
		}
		Version v = Version.emptyVersion;
		if (version != null) {
			try {
				v = Version.parseVersion(version);
			} catch (Throwable e) {
				throw new IOException("Invalid package version: " + version);
			}
		}
		ExportedPackage[] exportedPackages = admin.getExportedPackages(packageName);
		if (exportedPackages == null) {
			return new long[0];
		}
		ArrayList<Long> bundleIdentifiers = new ArrayList<Long>();
		for (ExportedPackage pkg : exportedPackages) {
			if (pkg.getVersion().equals(v)) {
				bundleIdentifiers.add(pkg.getExportingBundle().getBundleId());
			}
		}
		long[] bundles = new long[bundleIdentifiers.size()];
		int i = 0;
		for (long id : bundleIdentifiers) {
			bundles[i++] = id;
		}
		return bundles;
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getImportingBundles(String packageName, String version, long exportingBundle) throws IOException {
		if (packageName == null) {
			throw new IOException("Package name cannot be null");
		}
		Version v = Version.emptyVersion;
		if (version != null) {
			try {
				v = Version.parseVersion(version);
			} catch (Throwable e) {
				throw new IOException("Invalid package version: " + version);
			}
		}
		ExportedPackage[] exportedPackages = admin.getExportedPackages(packageName);
		if (exportedPackages == null) {
			return new long[0];
		}
		for (ExportedPackage pkg : exportedPackages) {
			if (pkg.getVersion().equals(v) && pkg.getExportingBundle().getBundleId() == exportingBundle) {
				Bundle[] bundles = pkg.getImportingBundles();
				long[] ids = new long[bundles.length];
				for (int i = 0; i < bundles.length; i++) {
					ids[i] = bundles[i].getBundleId();
				}
				return ids;
			}
		}
		return new long[0];
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData listPackages() {
		Set<OSGiPackage> packages = new HashSet<OSGiPackage>();
		for(ExportedPackage pkg : admin.getExportedPackages((Bundle) null)){
			packages.add(new OSGiPackage(pkg.getName(), pkg.getVersion().toString(), pkg.isRemovalPending(), new Bundle[] { pkg.getExportingBundle() }, pkg.getImportingBundles()));
		}
		return OSGiPackage.tableFrom(packages);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRemovalPending(String packageName, String version, long exportingBundle) throws IOException {
		if (packageName == null) {
			throw new IOException("Package name cannot be null");
		}
		Version v = Version.emptyVersion;
		if (version != null) {
			try {
				v = Version.parseVersion(version);
			} catch (Throwable e) {
				throw new IOException("Invalid package version: " + version);
			}
		}
		ExportedPackage[] exportedPackages = admin.getExportedPackages(packageName);
		if (exportedPackages == null) {
			return false;
		}
		for (ExportedPackage pkg : exportedPackages) {
			if (pkg.getVersion().equals(v) && pkg.getExportingBundle().getBundleId() == exportingBundle) {
				return pkg.isRemovalPending();
			}
		}
		return false;
	}

}
