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

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.gemini.mgmt.Activator;
import org.junit.BeforeClass;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class AbstractOSGiMBeanTest {

	protected String mBeanObjectName;
	
	@BeforeClass
	public static void setup(){
		BundleContext bc = FrameworkUtil.getBundle(Activator.class).getBundleContext();
		ServiceReference<MBeanServer> ref = bc.getServiceReference(MBeanServer.class);
		if (ref == null) {
			bc.registerService(MBeanServer.class.getCanonicalName(), ManagementFactory.getPlatformMBeanServer(), null);
		}
	}

	protected TabularData jmxInvokeBundleState(String operation, Object[] arguments, String[] types) throws Exception {
		JMXConnector connector;
		String url = "service:jmx:rmi:///jndi/rmi://localhost:21045/jmxrmi";
		JMXServiceURL jmxURL = new JMXServiceURL(url);
		connector = JMXConnectorFactory.connect(jmxURL);
		MBeanServerConnection connection = connector.getMBeanServerConnection();
		ObjectName name = new ObjectName(mBeanObjectName);
		return (TabularData) connection.invoke(name, operation, arguments, types);
	}
	
}
