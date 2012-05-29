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

package org.eclipse.gemini.management;

import javax.management.ObjectName;

import org.osgi.framework.BundleContext;

public class TestObjectNameTranslator implements ObjectNameTranslator {

	public TestObjectNameTranslator(BundleContext context) {

	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectName translate(ObjectName objectName) {
        return objectName;
    }

}
