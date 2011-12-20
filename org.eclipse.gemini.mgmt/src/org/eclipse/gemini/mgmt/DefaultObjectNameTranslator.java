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

import javax.management.ObjectName;

import org.osgi.framework.BundleContext;

/**
 * {@link DefaultObjectNameTranslator} is a default implementation of {@link ObjectNameTranslator} which maps each
 * {@link ObjectName} to itself.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
final class DefaultObjectNameTranslator implements ObjectNameTranslator {

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectName translate(ObjectName objectName) {
        return objectName;
    }

    /**
     * Creates an {@link ObjectNameTranslator} instance based on the headers of the given bundle. This is either one
     * configured by an attached fragment or, by default, an instance of this class.
     * 
     * @param bundleContext the bundle which may contain a header to define an {@link ObjectNameTranslator}
     * @return an {@link ObjectNameTranslator}
     * @throws ClassNotFoundException if the configured class cannot be loaded
     * @throws InstantiationException if the configured class cannot be instantiated
     * @throws IllegalAccessException if the configured class or its default constructor is not accessible
     */
    static ObjectNameTranslator initialiseObjectNameTranslator(BundleContext bundleContext) throws ClassNotFoundException, InstantiationException,
        IllegalAccessException {
        String ontClassName = bundleContext.getBundle().getHeaders().get(ObjectNameTranslator.HEADER_NAME);
        if (ontClassName == null) {
            return new DefaultObjectNameTranslator();
        }
        /*
         * Attempt to load and instantiate the specified class, allowing exceptions to percolate and fail the bundle
         * start.
         */
        @SuppressWarnings("unchecked")
        Class<ObjectNameTranslator> ontClass = (Class<ObjectNameTranslator>) bundleContext.getBundle().loadClass(ontClassName);
        return ontClass.newInstance();
    }

}