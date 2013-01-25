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

import java.util.List;
import java.util.logging.Logger;

import javax.management.ObjectName;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.log.LogService;

/**
 * {@link DefaultObjectNameTranslator} is a default implementation of {@link ObjectNameTranslator} which maps each
 * {@link ObjectName} to itself.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
final class DefaultObjectNameTranslator implements ObjectNameTranslator {

//	private static final Logger LOGGER = Logger.getLogger(DefaultObjectNameTranslator.class.getCanonicalName());
	
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
    static ObjectNameTranslator initialiseObjectNameTranslator(BundleContext bundleContext, LogService logger) throws ClassNotFoundException, InstantiationException,
        IllegalAccessException {
        Bundle bundle = bundleContext.getBundle();
		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		List<BundleWire> requiredWires = wiring.getProvidedWires(BundleRevision.HOST_NAMESPACE);
		for (BundleWire bundleWire : requiredWires) {
			Bundle fragment = bundleWire.getRequirerWiring().getBundle();
			String objectNameTranslator = fragment.getHeaders().get(ObjectNameTranslator.HEADER_NAME);
			if(objectNameTranslator != null){
		        Class<?> objectNameTranslatorClass = bundle.loadClass(objectNameTranslator);
		        if(ObjectNameTranslator.class.isAssignableFrom(objectNameTranslatorClass)){
			        try {
						return (ObjectNameTranslator) objectNameTranslatorClass.getConstructor(BundleContext.class).newInstance(bundleContext);
					} catch (Exception e) {
						if (logger != null) {
							logger.log(LogService.LOG_WARNING, String.format("Unable to create ObjectNameTranslator from fragment %d '%s'", fragment.getBundleId(), e.getMessage()));
						}
					} 
		        } else {
		        	if (logger != null) {
		        		logger.log(LogService.LOG_WARNING, String.format("Unable to create ObjectNameTranslator as specified class '%s' is not an assignable to '%s'", objectNameTranslator, ObjectNameTranslator.class.getName()));
		        	}
		        }
			}
		}
		return new DefaultObjectNameTranslator();
    }

}