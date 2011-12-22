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
package org.eclipse.gemini.mgmt.framework;

import org.eclipse.gemini.mgmt.AbstractOSGiMBeanTest;
import org.osgi.jmx.framework.ServiceStateMBean;

/**
 * Integration tests for the {@link ServiceState} implementation of {@link CustomServiceStateMBean} and {@link ServiceStateMBean}
 *
 */
public final class ServiceStateTests extends AbstractOSGiMBeanTest {

	protected String mBeanObjectName = ServiceStateMBean.OBJECTNAME;
	
}
