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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.log.LogService;

public class DefaultObjectNameTranslatorTest {

    private static final String NOT_A_OBJECT_NAME_TRANSLATOR_CLASS_NAME = DefaultObjectNameTranslatorTest.class.getName();

	private static final ClassNotFoundException TEST_CLASS_NOT_FOUND_EXCEPTION = new ClassNotFoundException("Test");

    private static final String TEST_OBJECT_NAME_TRANSLATOR_CLASS_NAME = TestObjectNameTranslator.class.getName();

    private static final String UNLOADABLE_CLASS_NAME = "BAD";

    private static final String UNINSTANTIABLE_OBJECT_NAME_TRANSLATOR_CLASS_NAME = UninstantiableObjectNameTranslator.class.getName();

    private BundleContext mockBundleContext;

    private Bundle mockBundle;

    private Dictionary<String, String> headers;

	private BundleWire mockBundleWire;

	private BundleWiring mockBundleWiring;
	
	private LogService stubLogService;

    @Before
    public void setUp() throws Exception {
        mockBundleContext = EasyMock.createMock(BundleContext.class);
        mockBundle = EasyMock.createMock(Bundle.class);
        mockBundleWiring = EasyMock.createMock(BundleWiring.class);
        mockBundleWire = EasyMock.createMock(BundleWire.class);
        List<BundleWire> listMockBundleWires = new ArrayList<BundleWire>();
        listMockBundleWires.add(mockBundleWire);
        headers = new Hashtable<String, String>();
        EasyMock.expect(mockBundleContext.getBundle()).andReturn(mockBundle);
        EasyMock.expect(mockBundle.adapt(BundleWiring.class)).andReturn(mockBundleWiring);
        EasyMock.expect(mockBundleWiring.getProvidedWires(BundleRevision.HOST_NAMESPACE)).andReturn(listMockBundleWires);
        EasyMock.expect(mockBundleWire.getRequirerWiring()).andReturn(mockBundleWiring);
        EasyMock.expect(mockBundleWiring.getBundle()).andReturn(mockBundle);
        EasyMock.expect(mockBundle.getHeaders()).andReturn(headers);
        this.stubLogService = new LogService() {
			@Override
			public void log(ServiceReference arg0, int arg1, String arg2, Throwable arg3) {
			}
			
			@Override
			public void log(ServiceReference arg0, int arg1, String arg2) {
			}
			
			@Override
			public void log(int arg0, String arg1, Throwable arg2) {
			}
			
			@Override
			public void log(int arg0, String arg1) {
			}
		};
    }

    private void replayMocks() {
        EasyMock.replay(mockBundleContext, mockBundle, mockBundleWiring, mockBundleWire);
    }

    @After
    public void tearDown() {
        EasyMock.verify(mockBundleContext, mockBundle, mockBundleWiring, mockBundleWire);
    }

    @Test
    public void testDefaultObjectNameTranslator() throws Exception {
        replayMocks();
        ObjectNameTranslator defaultObjectNameTranslator = DefaultObjectNameTranslator.initialiseObjectNameTranslator(mockBundleContext, this.stubLogService);
        Assert.assertTrue("Default ObjectNameTranslator has the wrong type", defaultObjectNameTranslator instanceof DefaultObjectNameTranslator);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testConfiguredObjectNameTranslator() throws Exception {
        EasyMock.expect((Class) mockBundle.loadClass(TEST_OBJECT_NAME_TRANSLATOR_CLASS_NAME)).andReturn(TestObjectNameTranslator.class);
        replayMocks();
        headers.put(ObjectNameTranslator.HEADER_NAME, TEST_OBJECT_NAME_TRANSLATOR_CLASS_NAME);
        ObjectNameTranslator testObjectNameTranslator = DefaultObjectNameTranslator.initialiseObjectNameTranslator(mockBundleContext, this.stubLogService);
        Assert.assertTrue("Test ObjectNameTranslator has the wrong type", testObjectNameTranslator instanceof TestObjectNameTranslator);
    }

    @SuppressWarnings("rawtypes")
    @Test(expected = ClassNotFoundException.class)
    public void testUnloadableObjectNameTranslator() throws Exception {
        EasyMock.expect((Class) mockBundle.loadClass(UNLOADABLE_CLASS_NAME)).andThrow(TEST_CLASS_NOT_FOUND_EXCEPTION);
        replayMocks();
        headers.put(ObjectNameTranslator.HEADER_NAME, UNLOADABLE_CLASS_NAME);
        DefaultObjectNameTranslator.initialiseObjectNameTranslator(mockBundleContext, this.stubLogService);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testWrongClassObjectNameTranslator() throws Exception {
        EasyMock.expect((Class) mockBundle.loadClass(NOT_A_OBJECT_NAME_TRANSLATOR_CLASS_NAME)).andReturn(DefaultObjectNameTranslatorTest.class);
        replayMocks();
        headers.put(ObjectNameTranslator.HEADER_NAME, NOT_A_OBJECT_NAME_TRANSLATOR_CLASS_NAME);
        ObjectNameTranslator defaultObjectNameTranslator = DefaultObjectNameTranslator.initialiseObjectNameTranslator(mockBundleContext, this.stubLogService);
        Assert.assertTrue("Test ObjectNameTranslator has the wrong type", defaultObjectNameTranslator instanceof DefaultObjectNameTranslator);
    }

    @SuppressWarnings("rawtypes")
    public void testUninstantiableObjectNameTranslator() throws Exception {
        EasyMock.expect((Class) mockBundle.loadClass(UNINSTANTIABLE_OBJECT_NAME_TRANSLATOR_CLASS_NAME)).andReturn(UninstantiableObjectNameTranslator.class);
        replayMocks();
        headers.put(ObjectNameTranslator.HEADER_NAME, UNINSTANTIABLE_OBJECT_NAME_TRANSLATOR_CLASS_NAME);
        ObjectNameTranslator defaultObjectNameTranslator = DefaultObjectNameTranslator.initialiseObjectNameTranslator(mockBundleContext, this.stubLogService);
        Assert.assertTrue("Default ObjectNameTranslator has the wrong type", defaultObjectNameTranslator instanceof DefaultObjectNameTranslator);
    }

}