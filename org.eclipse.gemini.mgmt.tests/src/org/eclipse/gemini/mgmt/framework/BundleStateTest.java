package org.eclipse.gemini.mgmt.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;

import org.eclipse.gemini.mgmt.Activator;
//import com.sap.core.js.conf.bundlestate.util.BundleState;
import org.eclipse.gemini.mgmt.codec.Util;
import org.eclipse.gemini.mgmt.framework.codec.OSGiBundle;
import org.eclipse.gemini.mgmt.framework.BundleState;

public class BundleStateTest {
	
	@Test
	public void nameAndVersionTest() throws Exception {
		BundleContext bc = FrameworkUtil.getBundle(Activator.class).getBundleContext();
		ServiceReference<MBeanServer> ref = bc.getServiceReference(MBeanServer.class);
		if (ref == null) {
			bc.registerService(MBeanServer.class.getCanonicalName(), ManagementFactory.getPlatformMBeanServer(), null);
		}
		CompositeData bundleInfo;
		String symbolicName;
		String version;
		Object key;
		Object[] keysArray;
		Bundle bundle;
		int mask = BundleState.SYMBOLIC_NAME + BundleState.IDENTIFIER + BundleState.VERSION;
		long start = System.currentTimeMillis();
		TabularDataSupport table = jmxInvokeListBundles(mask);
		long end = System.currentTimeMillis();
		assertTrue((end - start) < 1000);
		Set<?> keys = table.keySet();
		Iterator<?> iter = keys.iterator();
		while (iter.hasNext()) {
			key = iter.next();
			keysArray = ((Collection<?>) key).toArray();
			bundleInfo = (CompositeData) table.get(keysArray);
			symbolicName = (String) bundleInfo.get(BundleStateMBean.SYMBOLIC_NAME);
			version = (String) bundleInfo.get(BundleStateMBean.VERSION);
			bundle = bc.getBundle((Long) keysArray[0]);
			assertEquals(symbolicName, bundle.getSymbolicName());
			assertEquals(version, bundle.getVersion().toString());
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void fullMaskTest() throws Exception {
		CompositeData bundleInfo;
		String location;
		String symbolicName;
		String version;
		int startLevel;
		String state;
		long lastModified;
		boolean persistenlyStarted;
		boolean removalPending;
		boolean required;
		boolean fragment;
		Long[] registeredServices;
		Long[] servicesInUse;
		Map<String, CompositeData> headers;
		String[] exportedPackages;
		String[] importedPackages;
		Long[] fragments;
		Long[] hosts;
		Long[] requiringBundles;
		Long[] requiredBundles;
		Object key;
		Object[] keysArray;
		Bundle bundle;
		int mask = BundleState.LOCATION + BundleState.IDENTIFIER
				+ BundleState.SYMBOLIC_NAME + BundleState.VERSION
				+ BundleState.START_LEVEL + BundleState.STATE
				+ BundleState.LAST_MODIFIED + BundleState.PERSISTENTLY_STARTED
				+ BundleState.REMOVAL_PENDING + BundleState.REQUIRED
				+ BundleState.FRAGMENT + BundleState.REGISTERED_SERVICES
				+ BundleState.SERVICES_IN_USE + BundleState.HEADERS
				+ BundleState.EXPORTED_PACKAGES + BundleState.IMPORTED_PACKAGES
				+ BundleState.FRAGMENTS + BundleState.HOSTS
				+ BundleState.REQUIRING_BUNDLES + BundleState.REQUIRED_BUNDLES;
		TabularDataSupport table = jmxInvokeListBundles(mask);
		Set<?> keys = table.keySet();
		Iterator<?> iter = keys.iterator();
		BundleContext bc = FrameworkUtil.getBundle(BundleState.class).getBundleContext();
		StartLevel sl = (StartLevel) bc.getService(bc.getServiceReference(StartLevel.class.getCanonicalName()));
		PackageAdmin admin = (PackageAdmin) bc.getService(bc.getServiceReference(PackageAdmin.class.getCanonicalName()));
		while (iter.hasNext()) {
			key = iter.next();
			keysArray = ((Collection<?>) key).toArray();
			bundleInfo = (CompositeData) table.get(keysArray);

			location = (String) bundleInfo.get(BundleStateMBean.LOCATION);
			symbolicName = (String) bundleInfo.get(BundleStateMBean.SYMBOLIC_NAME);
			version = (String) bundleInfo.get(BundleStateMBean.VERSION);
			startLevel = ((Integer) bundleInfo.get(BundleStateMBean.START_LEVEL)).intValue();
			state = (String) bundleInfo.get(BundleStateMBean.STATE);
			lastModified = ((Long) bundleInfo.get(BundleStateMBean.LAST_MODIFIED)).longValue();
			persistenlyStarted = ((Boolean) bundleInfo.get(BundleStateMBean.PERSISTENTLY_STARTED)).booleanValue();
			removalPending = ((Boolean) bundleInfo.get(BundleStateMBean.REMOVAL_PENDING)).booleanValue();
			required = ((Boolean) bundleInfo.get(BundleStateMBean.REQUIRED)).booleanValue();
			fragment = ((Boolean) bundleInfo.get(BundleStateMBean.FRAGMENT)).booleanValue();
			registeredServices = (Long[]) bundleInfo.get(BundleStateMBean.REGISTERED_SERVICES);
			servicesInUse = (Long[]) bundleInfo.get(BundleStateMBean.SERVICES_IN_USE);
			headers = (Map<String, CompositeData>) bundleInfo.get(BundleStateMBean.HEADERS);
			exportedPackages = (String[]) bundleInfo.get(BundleStateMBean.EXPORTED_PACKAGES);
			importedPackages = (String[]) bundleInfo.get(BundleStateMBean.IMPORTED_PACKAGES);
			fragments = (Long[]) bundleInfo.get(BundleStateMBean.FRAGMENTS);
			hosts = (Long[]) bundleInfo.get(BundleStateMBean.HOSTS);
			requiringBundles = (Long[]) bundleInfo.get(BundleStateMBean.REQUIRING_BUNDLES);
			requiredBundles = (Long[]) bundleInfo.get(BundleStateMBean.REQUIRED_BUNDLES);

			bundle = bc.getBundle((Long) keysArray[0]);
			assertEquals(location, bundle.getLocation());
			assertEquals(symbolicName, bundle.getSymbolicName());
			assertEquals(version, bundle.getVersion().toString());
			assertEquals(startLevel, sl.getBundleStartLevel(bundle));
			assertEquals(state, stateToString(bundle.getState()));
			assertEquals(lastModified, bundle.getLastModified());
			assertEquals(persistenlyStarted, Util.isBundlePersistentlyStarted(bundle, sl));
			assertEquals(removalPending, Util.isRemovalPending(bundle.getBundleId(), bc));
			assertEquals(required, Util.isRequired(bundle.getBundleId(), bc));
			assertEquals(fragment, Util.isBundleFragment(bundle, admin));

			long[] rs = new long[registeredServices.length];
			for (int i = 0; i < registeredServices.length; i++) {
				rs[i] = registeredServices[i].longValue();
			}
			long[] rs2 = Util.serviceIds(bundle.getRegisteredServices());
			Arrays.sort(rs);
			Arrays.sort(rs2);
			assertTrue(Arrays.equals(rs, rs2));

			long[] siu = new long[servicesInUse.length];
			for (int i = 0; i < servicesInUse.length; i++) {
				siu[i] = servicesInUse[i].longValue();
			}
			Arrays.sort(siu);
			long[] siu2 = Util.serviceIds(bundle.getServicesInUse());
			Arrays.sort(siu2);
			assertTrue(Arrays.equals(siu, siu2));

			assertEquals((TabularData) headers,	OSGiBundle.headerTable(Util.getBundleHeaders(bundle)));

			String[] exportedPackages2 = Util.getBundleExportedPackages(bundle,	admin);
			Arrays.sort(exportedPackages);
			Arrays.sort(exportedPackages2);
			assertTrue(Arrays.equals(exportedPackages, exportedPackages2));

			String[] importedPackages2 = Util.getBundleImportedPackages(bundle, bc, admin);
			Arrays.sort(importedPackages);
			Arrays.sort(importedPackages2);
			assertTrue(Arrays.equals(importedPackages, importedPackages2));

			long[] frags = new long[fragments.length];
			for (int i = 0; i < fragments.length; i++) {
				frags[i] = fragments[i].longValue();
			}
			Arrays.sort(frags);
			long[] frags2 = Util.getBundleFragments(bundle, admin);
			Arrays.sort(frags2);
			assertTrue(Arrays.equals(frags, frags2));

			long[] hst = new long[hosts.length];
			for (int i = 0; i < hosts.length; i++) {
				hst[i] = hosts[i].longValue();
			}
			Arrays.sort(hst);
			long[] hst2 = Util.bundleIds(admin.getHosts(bundle));
			Arrays.sort(hst2);
			assertTrue(Arrays.equals(hst, hst2));

			long[] reqB = new long[requiringBundles.length];
			for (int i = 0; i < requiringBundles.length; i++) {
				reqB[i] = requiringBundles[i].longValue();
			}
			Arrays.sort(reqB);
			long[] reqB2 = Util.getRequiringBundles(bundle.getBundleId(), bc);
			Arrays.sort(reqB2);
			assertTrue(Arrays.equals(reqB, reqB2));

			long[] requiredB = new long[requiredBundles.length];
			for (int i = 0; i < requiredBundles.length; i++) {
				requiredB[i] = requiredBundles[i].longValue();
			}
			Arrays.sort(requiredB);
			long[] requiredB2 = Util.getRequiredBundles(bundle.getBundleId(), bc);
			Arrays.sort(requiredB2);
			assertTrue(Arrays.equals(requiredB, requiredB2));
		}
	}

	@Test
	public void randomMaskTest() throws Exception {
		CompositeData bundleInfo;
		String version;
		String state;
		long lastModified;
		boolean persistenlyStarted;
		boolean removalPending;
		Long[] registeredServices;
		Long[] servicesInUse;
		String[] exportedPackages;
		String[] importedPackages;
		Long[] hosts;
		Long[] requiringBundles;
		Object key;
		Object[] keysArray;
		Bundle bundle;
		int mask = BundleState.IDENTIFIER + BundleState.VERSION
				+ BundleState.STATE + BundleState.LAST_MODIFIED
				+ BundleState.PERSISTENTLY_STARTED + BundleState.REMOVAL_PENDING
				+ BundleState.REGISTERED_SERVICES + BundleState.SERVICES_IN_USE
				+ BundleState.EXPORTED_PACKAGES + BundleState.IMPORTED_PACKAGES
				+ BundleState.HOSTS + BundleState.REQUIRING_BUNDLES;
		TabularDataSupport table = jmxInvokeListBundles(mask);
		Set<?> keys = table.keySet();
		Iterator<?> iter = keys.iterator();
		BundleContext bc = FrameworkUtil.getBundle(BundleState.class).getBundleContext();
		StartLevel sl = (StartLevel) bc.getService(bc.getServiceReference(StartLevel.class.getCanonicalName()));
		PackageAdmin admin = (PackageAdmin) bc.getService(bc.getServiceReference(PackageAdmin.class.getCanonicalName()));
		while (iter.hasNext()) {
			key = iter.next();
			keysArray = ((Collection<?>) key).toArray();
			bundleInfo = (CompositeData) table.get(keysArray);

			version = (String) bundleInfo.get(BundleStateMBean.VERSION);
			state = (String) bundleInfo.get(BundleStateMBean.STATE);
			lastModified = ((Long) bundleInfo.get(BundleStateMBean.LAST_MODIFIED)).longValue();
			persistenlyStarted = ((Boolean) bundleInfo.get(BundleStateMBean.PERSISTENTLY_STARTED)).booleanValue();
			removalPending = ((Boolean) bundleInfo.get(BundleStateMBean.REMOVAL_PENDING)).booleanValue();
			registeredServices = (Long[]) bundleInfo.get(BundleStateMBean.REGISTERED_SERVICES);
			servicesInUse = (Long[]) bundleInfo.get(BundleStateMBean.SERVICES_IN_USE);
			exportedPackages = (String[]) bundleInfo.get(BundleStateMBean.EXPORTED_PACKAGES);
			importedPackages = (String[]) bundleInfo.get(BundleStateMBean.IMPORTED_PACKAGES);
			hosts = (Long[]) bundleInfo.get(BundleStateMBean.HOSTS);
			requiringBundles = (Long[]) bundleInfo.get(BundleStateMBean.REQUIRING_BUNDLES);
			bundle = bc.getBundle((Long) keysArray[0]);

			assertEquals(version, bundle.getVersion().toString());
			assertEquals(state, stateToString(bundle.getState()));
			assertEquals(lastModified, bundle.getLastModified());
			assertEquals(persistenlyStarted, Util.isBundlePersistentlyStarted(bundle, sl));
			assertEquals(removalPending, Util.isRemovalPending(bundle.getBundleId(), bc));

			long[] rs = new long[registeredServices.length];
			for (int i = 0; i < registeredServices.length; i++) {
				rs[i] = registeredServices[i].longValue();
			}
			long[] rs2 = Util.serviceIds(bundle.getRegisteredServices());
			Arrays.sort(rs);
			Arrays.sort(rs2);
			assertTrue(Arrays.equals(rs, rs2));

			long[] siu = new long[servicesInUse.length];
			for (int i = 0; i < servicesInUse.length; i++) {
				siu[i] = servicesInUse[i].longValue();
			}
			Arrays.sort(siu);
			long[] siu2 = Util.serviceIds(bundle.getServicesInUse());
			Arrays.sort(siu2);
			assertTrue(Arrays.equals(siu, siu2));

			String[] exportedPackages2 = Util.getBundleExportedPackages(bundle,	admin);
			Arrays.sort(exportedPackages);
			Arrays.sort(exportedPackages2);
			assertTrue(Arrays.equals(exportedPackages, exportedPackages2));

			String[] importedPackages2 = Util.getBundleImportedPackages(bundle,	bc, admin);
			Arrays.sort(importedPackages);
			Arrays.sort(importedPackages2);
			assertTrue(Arrays.equals(importedPackages, importedPackages2));

			long[] hst = new long[hosts.length];
			for (int i = 0; i < hosts.length; i++) {
				hst[i] = hosts[i].longValue();
			}
			Arrays.sort(hst);
			long[] hst2 = Util.bundleIds(admin.getHosts(bundle));
			Arrays.sort(hst2);
			assertTrue(Arrays.equals(hst, hst2));

			long[] reqB = new long[requiringBundles.length];
			for (int i = 0; i < requiringBundles.length; i++) {
				reqB[i] = requiringBundles[i].longValue();
			}
			Arrays.sort(reqB);
			long[] reqB2 = Util.getRequiringBundles(bundle.getBundleId(), bc);
			Arrays.sort(reqB2);
			assertTrue(Arrays.equals(reqB, reqB2));
		}
	}

	private TabularDataSupport jmxInvokeListBundles(int mask)
			throws MalformedURLException, IOException,
			MalformedObjectNameException, InstanceNotFoundException,
			MBeanException, ReflectionException {
		JMXConnector connector;
		String url = "service:jmx:rmi:///jndi/rmi://localhost:21045/jmxrmi";
		JMXServiceURL jmxURL = new JMXServiceURL(url);
		connector = JMXConnectorFactory.connect(jmxURL);
		MBeanServerConnection connection = connector.getMBeanServerConnection();
		ObjectName name = new ObjectName(CustomBundleStateMBean.OBJECTNAME);
		TabularDataSupport table = (TabularDataSupport) connection.invoke(name,
				"listBundles", new Object[] { new Integer(mask) },
				new String[] { "int" });
		return table;
	}

	@Test
	public void illegalMaskTest() throws Exception {
//		CompositeData bundleInfo;
//		String version;
//		String state;
//		long lastModified;
//		boolean persistenlyStarted;
//		boolean removalPending;
//		Long[] registeredServices;
//		Long[] servicesInUse;
//		String[] exportedPackages;
//		String[] importedPackages;
//		Long[] hosts;
//		Long[] requiringBundles;
//		Object key;
//		Object[] keysArray;
//		Bundle bundle;
		int mask = 1048576;
		try {
			@SuppressWarnings("unused")
			TabularDataSupport table = jmxInvokeListBundles(mask);
			fail("Expected exception did not occur!");
		} catch (Exception e) {
			assertTrue(e.getCause() instanceof java.lang.IllegalArgumentException);
		}
	}

	private String stateToString(int state) {
		String strState = null;
		switch (state) {
		case 1:
			strState = "UNINSTALLED";
			break;
		case 2:
			strState = "INSTALLED";
			break;
		case 4:
			strState = "RESOLVED";
			break;
		case 8:
			strState = "STARTING";
			break;
		case 16:
			strState = "STOPPING";
			break;
		case 32:
			strState = "ACTIVE";
			break;
		default:
			strState = "UNKNOWN";
			break;
		}
		return strState;
	}
}

