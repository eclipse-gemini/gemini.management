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

package org.eclipse.gemini.mgmt;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Test;
import org.junit.Assert;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.easymock.EasyMock;

public class DefaultObjectNameTranslatorTest {
    
    @Test
    public void testDefaultObjectNameTranslator() throws Exception {
        BundleContext bundleContext = EasyMock.createMock(BundleContext.class);
        Bundle bundle = EasyMock.createMock(Bundle.class);
        EasyMock.expect(bundleContext.getBundle()).andReturn(bundle);
        Dictionary<String, String> headers = new Hashtable<String, String>();
        EasyMock.expect(bundle.getHeaders()).andReturn(headers);
        EasyMock.replay(bundleContext, bundle);
        ObjectNameTranslator defaultObjectNameTranslator = DefaultObjectNameTranslator.initialiseObjectNameTranslator(bundleContext);
        Assert.assertTrue("Default ObjectNameTranslator has the wrong type", defaultObjectNameTranslator instanceof DefaultObjectNameTranslator);
        EasyMock.verify(bundleContext, bundle);
    }

}