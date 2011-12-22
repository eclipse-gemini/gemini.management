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

package org.eclipse.gemini.mgmt;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

/** 
 * 
 */
abstract public class Monitor extends NotificationBroadcasterSupport implements	MBeanRegistration {

	protected ObjectName objectName;

	protected volatile long sequenceNumber = 0;

	protected MBeanServer server;
	
	/**
	 * {@inheritDoc}
	 */
	public void postDeregister() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void postRegister(Boolean registrationDone) {
		addListener();
	}

	/**
	 * {@inheritDoc}
	 */
	public void preDeregister() throws Exception {
		removeListener();
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
		objectName = name;
		this.server = server;
		return name;
	}

	/**
	 * Add a listener for the monitored object 
	 */
	abstract protected void addListener();

	/**
	 * Remove the listener for the monitored object
	 */
	abstract protected void removeListener();

}