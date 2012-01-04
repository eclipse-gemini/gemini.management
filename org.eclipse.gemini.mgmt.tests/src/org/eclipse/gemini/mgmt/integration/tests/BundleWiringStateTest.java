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

import org.eclipse.gemini.mgmt.framework.BundleWiringState;
import org.eclipse.gemini.mgmt.framework.CustomBundleWiringStateMBean;
import org.junit.Test;

/**
 * Integration tests for the {@link BundleWiringState} implementation of {@link CustomBundleWiringStateMBean}
 *
 */
public final class BundleWiringStateTest extends AbstractOSGiMBeanTest {

	public BundleWiringStateTest() {
		super.mBeanObjectName = CustomBundleWiringStateMBean.OBJECTNAME;
	}
	
	@Test
	public void somethingTest(){
		
	}
	
}
