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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.easymock.EasyMock;

public class DefaultObjectNameTranslatorTest {

    private static final ClassNotFoundException TEST_CLASS_NOT_FOUND_EXCEPTION = new ClassNotFoundException("Test");

    private static final String TEST_OBJECT_NAME_TRANSLATOR_CLASS_NAME = TestObjectNameTranslator.class.getName();

    private static final String UNLOADABLE_CLASS_NAME = "BAD";

    private static final String UNINSTANTIABLE_OBJECT_NAME_TRANSLATOR_CLASS_NAME = UninstantiableObjectNameTranslator.class.getName();

    private BundleContext mockBundleContext;

    private Bundle mockBundle;

    private Dictionary<String, String> headers;

    @Before
    public void setUp() throws Exception {
        mockBundleContext = EasyMock.createMock(BundleContext.class);
        mockBundle = EasyMock.createMock(Bundle.class);
        headers = new Hashtable<String, String>();
        EasyMock.expect(mockBundleContext.getBundle()).andReturn(mockBundle);
        EasyMock.expect(mockBundle.getHeaders()).andReturn(headers);
    }

    private void replayMocks() {
        EasyMock.replay(mockBundleContext, mockBundle);
    }

    @After
    public void tearDown() {
        EasyMock.verify(mockBundleContext, mockBundle);
    }

    @Test
    public void testDefaultObjectNameTranslator() throws Exception {
        replayMocks();
        ObjectNameTranslator defaultObjectNameTranslator = DefaultObjectNameTranslator.initialiseObjectNameTranslator(mockBundleContext);
        Assert.assertTrue("Default ObjectNameTranslator has the wrong type", defaultObjectNameTranslator instanceof DefaultObjectNameTranslator);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testConfiguredObjectNameTranslator() throws Exception {
        EasyMock.expect((Class) mockBundle.loadClass(TEST_OBJECT_NAME_TRANSLATOR_CLASS_NAME)).andReturn(TestObjectNameTranslator.class);
        replayMocks();
        headers.put(ObjectNameTranslator.HEADER_NAME, TEST_OBJECT_NAME_TRANSLATOR_CLASS_NAME);
        ObjectNameTranslator testObjectNameTranslator = DefaultObjectNameTranslator.initialiseObjectNameTranslator(mockBundleContext);
        Assert.assertTrue("Test ObjectNameTranslator has the wrong type", testObjectNameTranslator instanceof TestObjectNameTranslator);
    }

    @SuppressWarnings("rawtypes")
    @Test(expected = ClassNotFoundException.class)
    public void testUnloadableObjectNameTranslator() throws Exception {
        EasyMock.expect((Class) mockBundle.loadClass(UNLOADABLE_CLASS_NAME)).andThrow(TEST_CLASS_NOT_FOUND_EXCEPTION);
        replayMocks();
        headers.put(ObjectNameTranslator.HEADER_NAME, UNLOADABLE_CLASS_NAME);
        DefaultObjectNameTranslator.initialiseObjectNameTranslator(mockBundleContext);
    }

    @SuppressWarnings("rawtypes")
    @Test(expected = IllegalAccessException.class)
    public void testUninstantiableObjectNameTranslator() throws Exception {
        EasyMock.expect((Class) mockBundle.loadClass(UNINSTANTIABLE_OBJECT_NAME_TRANSLATOR_CLASS_NAME)).andReturn(UninstantiableObjectNameTranslator.class);
        replayMocks();
        headers.put(ObjectNameTranslator.HEADER_NAME, UNINSTANTIABLE_OBJECT_NAME_TRANSLATOR_CLASS_NAME);
        DefaultObjectNameTranslator.initialiseObjectNameTranslator(mockBundleContext);
    }

}