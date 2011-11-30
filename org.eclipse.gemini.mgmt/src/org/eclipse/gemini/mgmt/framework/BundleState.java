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

package org.eclipse.gemini.mgmt.framework;

import java.io.IOException;
import java.util.ArrayList;

import javax.management.Notification;
import javax.management.openmbean.TabularData;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.jmx.framework.BundleStateMBean;

import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;

import org.eclipse.gemini.mgmt.Monitor;
import org.eclipse.gemini.mgmt.codec.Util;
import org.eclipse.gemini.mgmt.framework.codec.OSGiBundle;
import org.eclipse.gemini.mgmt.framework.codec.OSGiBundleEvent;

/** 
 * 
 */
public class BundleState extends Monitor implements CustomBundleStateMBean {
	public BundleState(BundleContext bc, StartLevel sl, PackageAdmin admin) {
		this.bc = bc;
		this.sl = sl;
		this.admin = admin;
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData listBundles() throws IOException {
		return listBundles(CustomBundleStateMBean.DEFAULT);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public TabularData listBundles(int mask) throws IOException {
		if (mask < 1 || mask > 1048575) {
			throw new IllegalArgumentException("Mask out of range!");
		}
		try {
			ArrayList<OSGiBundle> bundles = new ArrayList<OSGiBundle>();
			for (Bundle bundle : bc.getBundles()) {
				bundles.add(new OSGiBundle(bc, admin, sl, bundle));
			}
			TabularData table = OSGiBundle.tableFrom(bundles, mask);
			return table;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getExportedPackages(long bundleId) throws IOException {
		ExportedPackage[] packages = admin.getExportedPackages(bundle(bundleId));
		if (packages == null) {
			return new String[0];
		}
		String[] ep = new String[packages.length];
		for (int i = 0; i < packages.length; i++) {
			ep[i] = packages[i].getName() + ";" + packages[i].getVersion();
		}
		return ep;
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getFragments(long bundleId) throws IOException {
		return Util.getBundleFragments(bundle(bundleId), admin);
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getHeaders(long bundleId) throws IOException {
		return OSGiBundle.headerTable(bundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getHosts(long fragment) throws IOException {
		Bundle[] hosts = admin.getHosts(bundle(fragment));
		if (hosts == null) {
			return new long[0];
		}
		return Util.bundleIds(hosts);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getImportedPackages(long bundleId) throws IOException {
		return Util.getBundleImportedPackages(bundle(bundleId), bc, admin);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getLastModified(long bundleId) throws IOException {
		return bundle(bundleId).getLastModified();
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getRegisteredServices(long bundleId) throws IOException {
		return Util.serviceIds(bundle(bundleId).getRegisteredServices());
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getRequiringBundles(long bundleIdentifier) throws IOException {
		return Util.getBundlesRequiring(bundle(bundleIdentifier), bc, admin);
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getServicesInUse(long bundleIdentifier) throws IOException {
		return Util.serviceIds(bundle(bundleIdentifier).getServicesInUse());
	}

	/**
	 * {@inheritDoc}
	 */
	public int getStartLevel(long bundleId) throws IOException {
		return sl.getBundleStartLevel(bundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getState(long bundleId) throws IOException {
		return Util.getBundleState(bundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSymbolicName(long bundleId) throws IOException {
		return bundle(bundleId).getSymbolicName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLocation(long bundleId) throws IOException {
		return bundle(bundleId).getLocation();
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getRequiredBundles(long bundleIdentifier) throws IOException {
		return Util.getDependencies(bundle(bundleIdentifier), admin);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersion(long bundleId) throws IOException {
		return bundle(bundleId).getVersion().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPersistentlyStarted(long bundleId) throws IOException {
		return Util.isBundlePersistentlyStarted(bundle(bundleId), sl);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isFragment(long bundleId) throws IOException {
		return Util.isBundleFragment(bundle(bundleId), admin);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRemovalPending(long bundleId) throws IOException {
		return Util.isRequiredBundleRemovalPending(bundle(bundleId), bc, admin);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequired(long bundleId) throws IOException {
		return Util.isBundleRequired(bundle(bundleId), bc, admin);
	}

	private Bundle bundle(long bundleId) throws IOException {
		Bundle b = bc.getBundle(bundleId);
		if (b == null) {
			throw new IOException("Bundle with id: " + bundleId + " does not exist");
		}
		return b;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addListener() {
		bundleListener = getBundleListener();
		bc.addBundleListener(bundleListener);
	}

	protected BundleListener getBundleListener() {
		return new BundleListener() {
			public void bundleChanged(BundleEvent bundleEvent) {
				Notification notification = new Notification(BundleStateMBean.EVENT, objectName, sequenceNumber++);
				notification.setUserData(new OSGiBundleEvent(bundleEvent).asCompositeData());
				sendNotification(notification);
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void removeListener() {
		if (bundleListener != null) {
			bc.removeBundleListener(bundleListener);
		}
	}

	protected BundleListener bundleListener;
	protected BundleContext bc;
	protected StartLevel sl;
	protected PackageAdmin admin;

}
