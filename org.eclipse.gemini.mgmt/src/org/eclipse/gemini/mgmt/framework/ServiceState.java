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
 *     Christopher Frost - Updates for RFC 169
 ******************************************************************************/

package org.eclipse.gemini.mgmt.framework;

import static org.osgi.framework.Constants.OBJECTCLASS;
import static org.osgi.framework.Constants.SERVICE_ID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.Notification;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.eclipse.gemini.mgmt.Monitor;
import org.eclipse.gemini.mgmt.codec.OSGiProperties;
import org.eclipse.gemini.mgmt.codec.Util;
import org.eclipse.gemini.mgmt.framework.codec.OSGiService;
import org.eclipse.gemini.mgmt.framework.codec.OSGiServiceEvent;
import org.osgi.framework.AllServiceListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.jmx.framework.ServiceStateMBean;
import org.osgi.util.tracker.ServiceTracker;

/** 
 * 
 */
public class ServiceState extends Monitor implements ServiceStateMBean {

	protected ServiceListener serviceListener;
	
	protected BundleContext bundleContext;
	
	/**
	 * Constructor
	 * 
	 * @param bundleContext
	 */
	public ServiceState(BundleContext bc) {
		this.bundleContext = bc;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getBundleIdentifier(long serviceId) throws IOException {
		return ref(serviceId).getBundle().getBundleId();
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getProperties(long serviceId) throws IOException {
		return OSGiProperties.tableFrom(ref(serviceId));
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getObjectClass(long serviceId) throws IOException {
		return (String[]) ref(serviceId).getProperty(OBJECTCLASS);
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData listServices() {
		ArrayList<OSGiService> services = new ArrayList<OSGiService>();
		for (Bundle bundle : bundleContext.getBundles()) {
			ServiceReference<?>[] refs = bundle.getRegisteredServices();
			if (refs != null) {
				for (ServiceReference<?> ref : refs) {
					services.add(new OSGiService(ref));
				}
			}
		}
		return OSGiService.tableFrom(services);
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getUsingBundles(long serviceId) throws IOException {
		Bundle[] bundles = ref(serviceId).getUsingBundles();
		long[] ids = new long[bundles.length];
		for (int i = 0; i < bundles.length; i++) {
			ids[i] = bundles[i].getBundleId();
		}
		return ids;
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData getService(long serviceId) throws IOException {
		for (Bundle bundle : bundleContext.getBundles()) {
			ServiceReference<?>[] refs = bundle.getRegisteredServices();
			if (refs != null) {
				for (ServiceReference<?> ref : refs) {
					if(serviceId == (Long) ref.getProperty(Constants.SERVICE_ID)){
						return new OSGiService(ref).asCompositeData();
					}
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * @param <T>
	 */
	public CompositeData getProperty(long serviceId, String key) throws IOException {
		for (Bundle bundle : bundleContext.getBundles()) {
			ServiceReference<?>[] refs = bundle.getRegisteredServices();
			if (refs != null) {
				for (ServiceReference<?> ref : refs) {
					if(serviceId == (Long) ref.getProperty(Constants.SERVICE_ID)){
						return OSGiProperties.encode(key, ref.getProperty(key));
					}
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData listServices(String clazz, String filter) throws IOException {
		ArrayList<OSGiService> services = new ArrayList<OSGiService>();
		try {
			ServiceReference<?>[] allServiceReferences = bundleContext.getAllServiceReferences(clazz, filter);
			for (ServiceReference<?> ref : allServiceReferences) {
				services.add(new OSGiService(ref));
			}
			return OSGiService.tableFrom(services);
		} catch (InvalidSyntaxException e) {
			throw new IOException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData listServices(String clazz, String filter, String... serviceTypeItems) throws IOException {
		ArrayList<OSGiService> services = new ArrayList<OSGiService>();
		List<String> serviceTypeNames = Arrays.asList(serviceTypeItems);
		try {
			ServiceReference<?>[] allServiceReferences = bundleContext.getAllServiceReferences(clazz, filter);
			for (ServiceReference<?> reference : allServiceReferences) {
				Long identifier;
				if(serviceTypeNames.contains(ServiceStateMBean.IDENTIFIER)){
					identifier = (Long) reference.getProperty(SERVICE_ID);
				} else {
					identifier = null;
				}
				String[] interfaces;
				if(serviceTypeNames.contains(ServiceStateMBean.OBJECT_CLASS)){
					interfaces = (String[]) reference.getProperty(OBJECTCLASS);
				} else {
					interfaces = null;
				}
				Long bundle;
				if(serviceTypeNames.contains(ServiceStateMBean.BUNDLE_IDENTIFIER)){
					bundle = reference.getBundle().getBundleId();
				} else {
					bundle = null;
				}
				long[] usingBundles;
				if(serviceTypeNames.contains(ServiceStateMBean.USING_BUNDLES)){
					usingBundles = Util.bundleIds(reference.getUsingBundles());
				} else {
					usingBundles = null;
				}
				services.add(new OSGiService(reference));
			}
			return OSGiService.tableFrom(services);
		} catch (InvalidSyntaxException e) {
			throw new IOException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getServiceIds() throws IOException {
		ServiceReference<?>[] allServiceReferences;
		try {
			allServiceReferences = bundleContext.getAllServiceReferences(null, null);
			long[] serviceIds = new long[allServiceReferences.length];
			for (int i = 0; i < allServiceReferences.length; i++) {
				serviceIds[i] = (Long) allServiceReferences[i].getProperty(Constants.SERVICE_ID);
			}
			return serviceIds;
		} catch (InvalidSyntaxException e) {
			//passing in null so should never happen
			throw new IOException(e);
		}
	}
	
	//End methods for the MBean
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addListener() {
		serviceListener = this.getServiceListener();
		bundleContext.addServiceListener(serviceListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void removeListener() {
		if (serviceListener != null) {
			bundleContext.removeServiceListener(serviceListener);
		}
	}

	protected ServiceListener getServiceListener() {
		return new AllServiceListener() {
			public void serviceChanged(ServiceEvent serviceEvent) {
				Notification notification = new Notification(ServiceStateMBean.EVENT, objectName, sequenceNumber++);
				notification.setUserData(new OSGiServiceEvent(serviceEvent).asCompositeData());
				sendNotification(notification);
			}
		};
	}
	
	protected ServiceReference<?> ref(long serviceId) throws IOException {
		Filter filter;
		try {
			filter = bundleContext.createFilter("(" + Constants.SERVICE_ID + "=" + serviceId + ")");
		} catch (InvalidSyntaxException e) {
			throw new IOException("Invalid filter syntax: " + e);
		}
		ServiceTracker<?, ?> tracker = new ServiceTracker<Object, Object>(bundleContext, filter, null);
		tracker.open();
		ServiceReference<?> serviceReference = tracker.getServiceReference();
		if (serviceReference == null) {
			throw new IOException("Service <" + serviceId + "> does not exist");
		}
		tracker.close();
		return serviceReference;
	}

}
