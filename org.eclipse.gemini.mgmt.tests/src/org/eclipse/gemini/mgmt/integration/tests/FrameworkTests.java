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

import org.eclipse.gemini.mgmt.AbstractOSGiMBeanTest;
import org.eclipse.gemini.mgmt.framework.Framework;
import org.osgi.jmx.framework.FrameworkMBean;

/**
 * Integration tests for the {@link Framework} implementation of {@link FrameworkMBean}
 *
 */
public final class FrameworkTests extends AbstractOSGiMBeanTest {

	protected String mBeanObjectName = FrameworkMBean.OBJECTNAME;
	
}
