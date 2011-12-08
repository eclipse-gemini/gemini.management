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

import org.eclipse.gemini.mgmt.Monitor;
import org.eclipse.gemini.mgmt.codec.Util;
import org.eclipse.gemini.mgmt.framework.codec.OSGiBundle;
import org.eclipse.gemini.mgmt.framework.codec.OSGiBundleEvent;

/** 
 * 
 */
public class BundleState extends Monitor implements CustomBundleStateMBean {
	
	protected BundleListener bundleListener;
	protected BundleContext bundleContext;
	
	public BundleState(BundleContext bc) {
		this.bundleContext = bc;
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
			for (Bundle bundle : bundleContext.getBundles()) {
				bundles.add(new OSGiBundle(bundleContext, bundle));
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
		return Util.getBundleExportedPackages(getBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getFragments(long bundleId) throws IOException {
		return Util.getBundleFragments(getBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getHeaders(long bundleId) throws IOException {
		return OSGiBundle.headerTable(getBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getHosts(long fragment) throws IOException {
		return Util.getBundleHosts(getBundle(fragment));
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getImportedPackages(long bundleId) throws IOException {
		return Util.getBundleImportedPackages(getBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public long getLastModified(long bundleId) throws IOException {
		return getBundle(bundleId).getLastModified();
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getRegisteredServices(long bundleId) throws IOException {
		return Util.serviceIds(getBundle(bundleId).getRegisteredServices());
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getRequiringBundles(long bundleId) throws IOException {
		return Util.getRequiringBundles(getBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getServicesInUse(long bundleIdentifier) throws IOException {
		return Util.serviceIds(getBundle(bundleIdentifier).getServicesInUse());
	}

	/**
	 * {@inheritDoc}
	 */
	public int getStartLevel(long bundleId) throws IOException {
		return Util.getBundleStartLevel(getBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getState(long bundleId) throws IOException {
		return Util.getBundleState(getBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSymbolicName(long bundleId) throws IOException {
		return getBundle(bundleId).getSymbolicName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLocation(long bundleId) throws IOException {
		return getBundle(bundleId).getLocation();
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getRequiredBundles(long bundleIdentifier) throws IOException {
		return Util.getRequiredBundles(getBundle(bundleIdentifier));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersion(long bundleId) throws IOException {
		return getBundle(bundleId).getVersion().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPersistentlyStarted(long bundleId) throws IOException {
		return Util.isBundlePersistentlyStarted(getBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isFragment(long bundleId) throws IOException {
		return Util.isBundleFragment(getBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRemovalPending(long bundleId) throws IOException {
		return Util.isRemovalPending(bundleId, bundleContext);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequired(long bundleId) throws IOException {
		return Util.isRequired(bundleId, bundleContext);
	}

	private Bundle getBundle(long bundleId) throws IOException {
		Bundle b = bundleContext.getBundle(bundleId);
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
		bundleContext.addBundleListener(bundleListener);
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
			bundleContext.removeBundleListener(bundleListener);
		}
	}

}
