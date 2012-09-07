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
import java.util.List;

import javax.management.Notification;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.jmx.framework.BundleStateMBean;

import org.eclipse.gemini.management.Monitor;
import org.eclipse.gemini.management.framework.internal.OSGiBundle;
import org.eclipse.gemini.management.framework.internal.OSGiBundleEvent;
import org.eclipse.gemini.management.internal.BundleUtil;

/** 
 * 
 */
public final class BundleState extends Monitor implements BundleStateMBean {
	
	private BundleListener bundleListener;
	
	private BundleContext bundleContext;
	
	/**
	 * 
	 * @param bundleContext
	 */
	public BundleState(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public CompositeData getBundle(long bundleId) throws IOException {
		return new OSGiBundle(retrieveBundle(bundleId)).asCompositeData();
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getBundleIds() throws IOException {
		Bundle[] bundles = bundleContext.getBundles();
		long[] ids = new long[bundles.length];
		for (int i = 0; i < bundles.length; i++) {
			ids[i] = bundles[i].getBundleId();
		}
		return ids;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public TabularData listBundles() throws IOException {
		try {
			TabularDataSupport table = new TabularDataSupport(BundleStateMBean.BUNDLES_TYPE);
			for (Bundle bundle : bundleContext.getBundles()) {
				table.put(new OSGiBundle(bundle).asCompositeData());
			}
			return table;
		} catch (Throwable e) {
			throw new IOException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData listBundles(String... bundleTypeItems) throws IOException {
		try {
			ArrayList<OSGiBundle> bundles = new ArrayList<OSGiBundle>();
			for (Bundle bundle : bundleContext.getBundles()) {
				bundles.add(new OSGiBundle(bundle));
			}
			TabularData table = OSGiBundle.tableFrom(bundles, bundleTypeItems);
			return table;
		} catch (Throwable e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String[] getExportedPackages(long bundleId) throws IOException {
		return BundleUtil.getBundleExportedPackages(retrieveBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getFragments(long bundleId) throws IOException {
		BundleWiring wiring = retrieveBundle(bundleId).adapt(BundleWiring.class);
		List<BundleWire> requiredWires = wiring.getProvidedWires(BundleRevision.HOST_NAMESPACE);
        return bundleWiresToRequirerIds(requiredWires);
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getHeaders(long bundleId) throws IOException {
		return OSGiBundle.headerTable(retrieveBundle(bundleId).getHeaders());
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getHeaders(long bundleId, String locale) throws IOException {
		return OSGiBundle.headerTable(retrieveBundle(bundleId).getHeaders(locale));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHeader(long bundleId, String key) throws IOException {
		return retrieveBundle(bundleId).getHeaders().get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHeader(long bundleId, String key, String locale) throws IOException {
		return retrieveBundle(bundleId).getHeaders(locale).get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getHosts(long fragment) throws IOException {
		BundleWiring wiring = retrieveBundle(fragment).adapt(BundleWiring.class);
		List<BundleWire> providedWires = wiring.getRequiredWires(BundleRevision.HOST_NAMESPACE);
        return bundleWiresToProviderIds(providedWires);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String[] getImportedPackages(long bundleId) throws IOException {
		return BundleUtil.getBundleImportedPackages(retrieveBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public long getLastModified(long bundleId) throws IOException {
		return retrieveBundle(bundleId).getLastModified();
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getRegisteredServices(long bundleId) throws IOException {
		return serviceIds(retrieveBundle(bundleId).getRegisteredServices());
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getRequiringBundles(long bundleId) throws IOException {
        BundleWiring wiring = retrieveBundle(bundleId).adapt(BundleWiring.class);
        List<BundleWire> providedWires = wiring.getProvidedWires(BundleRevision.BUNDLE_NAMESPACE);
        return bundleWiresToRequirerIds(providedWires);
    }
	/**
	 * {@inheritDoc}
	 */
	public long[] getServicesInUse(long bundleIdentifier) throws IOException {
		return serviceIds(retrieveBundle(bundleIdentifier).getServicesInUse());
	}

	/**
	 * {@inheritDoc}
	 */
	public int getStartLevel(long bundleId) throws IOException {
		return BundleUtil.getBundleStartLevel(retrieveBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getState(long bundleId) throws IOException {
		return BundleUtil.getBundleState(retrieveBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSymbolicName(long bundleId) throws IOException {
		return retrieveBundle(bundleId).getSymbolicName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLocation(long bundleId) throws IOException {
		return retrieveBundle(bundleId).getLocation();
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getRequiredBundles(long bundleIdentifier) throws IOException {
        BundleWiring wiring = retrieveBundle(bundleIdentifier).adapt(BundleWiring.class);
        List<BundleWire> requiredWires = wiring.getRequiredWires(BundleRevision.BUNDLE_NAMESPACE);
        return bundleWiresToProviderIds(requiredWires);
    }

	/**
	 * {@inheritDoc}
	 */
	public String getVersion(long bundleId) throws IOException {
		return retrieveBundle(bundleId).getVersion().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isActivationPolicyUsed(long bundleId) throws IOException {
		return BundleUtil.isBundleActivationPolicyUsed(retrieveBundle(bundleId));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isPersistentlyStarted(long bundleId) throws IOException {
		return BundleUtil.isBundlePersistentlyStarted(retrieveBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isFragment(long bundleId) throws IOException {
		return BundleUtil.isBundleFragment(retrieveBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRemovalPending(long bundleId) throws IOException {
		return BundleUtil.isRemovalPending(retrieveBundle(bundleId));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequired(long bundleId) throws IOException {
		return BundleUtil.isRequired(retrieveBundle(bundleId));
	}

	//End methods for the MBean
	
	private Bundle retrieveBundle(long bundleId) throws IOException {
		Bundle b = bundleContext.getBundle(bundleId);
		if (b == null) {
			throw new IOException("Bundle with id: " + bundleId + " does not exist");
		}
		return b;
	}

	private long[] bundleWiresToRequirerIds(List<BundleWire> wires){
        long[] consumerWirings = new long[wires.size()];
        int i = 0;
        for (BundleWire bundleWire : wires) {
            consumerWirings[i] = bundleWire.getRequirerWiring().getBundle().getBundleId();
            i++;
        }
        return consumerWirings;
	}

	private long[] bundleWiresToProviderIds(List<BundleWire> wires){
        long[] consumerWirings = new long[wires.size()];
        int i = 0;
        for (BundleWire bundleWire : wires) {
            consumerWirings[i] = bundleWire.getProviderWiring().getBundle().getBundleId();
            i++;
        }
        return consumerWirings;
	}

	private long[] serviceIds(ServiceReference<?>[] refs) {
		if (refs == null) {
			return new long[0];
		}
		long[] ids = new long[refs.length];
		for (int i = 0; i < refs.length; i++) {
			ids[i] = (Long) refs[i].getProperty(Constants.SERVICE_ID);
		}
		return ids;
	}
	
	//Monitor methods
	
	/**
	 * {@inheritDoc}
	 */
	protected void addListener() {
		bundleListener = getBundleListener();
		bundleContext.addBundleListener(bundleListener);
	}

	private BundleListener getBundleListener() {
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
	protected void removeListener() {
		if (bundleListener != null) {
			bundleContext.removeBundleListener(bundleListener);
		}
	}

}
