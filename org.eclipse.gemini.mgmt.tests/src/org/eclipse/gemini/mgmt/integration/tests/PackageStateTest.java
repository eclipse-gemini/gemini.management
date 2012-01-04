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

import org.eclipse.gemini.mgmt.framework.PackageState;
import org.junit.Test;
import org.osgi.jmx.framework.PackageStateMBean;

/**
 * Integration tests for the {@link PackageState} implementation {@link PackageStateMBean}
 *
 */
public final class PackageStateTest extends AbstractOSGiMBeanTest {
	
	public PackageStateTest() {
		super.mBeanObjectName = PackageStateMBean.OBJECTNAME;
	}
	
	@Test
	public void somethingTest(){
		
	}
}
