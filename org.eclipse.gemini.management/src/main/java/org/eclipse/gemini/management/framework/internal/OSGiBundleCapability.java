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
package org.eclipse.gemini.management.framework.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularDataSupport;

import org.eclipse.gemini.management.framework.CustomBundleWiringStateMBean;
import org.eclipse.gemini.management.internal.OSGiProperties;
import org.osgi.framework.wiring.BundleCapability;

public class OSGiBundleCapability {
	
	private final BundleCapability bundleCapability;

	public OSGiBundleCapability(BundleCapability bundleCapability) {
		this.bundleCapability = bundleCapability;
	}
	
	public CompositeData asCompositeData() {
		try {
			TabularDataSupport tabularAttributes = new TabularDataSupport(CustomBundleWiringStateMBean.ATTRIBUTES_TYPE);
			Map<String, Object> attributes = bundleCapability.getAttributes();
			for (Entry<String, Object> attribute : attributes.entrySet()) {
				tabularAttributes.put(OSGiProperties.encode(attribute.getKey(), attribute.getValue()));
			}
	
			TabularDataSupport tabularDirectives = new TabularDataSupport(CustomBundleWiringStateMBean.DIRECTIVES_TYPE);
			Map<String, String> directives = bundleCapability.getDirectives();
			for (Entry<String, String> directive : directives.entrySet()) {
				tabularDirectives.put(new CompositeDataSupport(CustomBundleWiringStateMBean.PROPERTY_TYPE, OSGiProperties.getDirectiveKeyValueItem(directive.getKey(), directive.getValue())));
			}
			
			Map<String, Object> items = new HashMap<String, Object>();
			items.put(CustomBundleWiringStateMBean.ATTRIBUTES, tabularAttributes);
			items.put(CustomBundleWiringStateMBean.DIRECTIVES, tabularDirectives);
			items.put(CustomBundleWiringStateMBean.NAMESPACE, bundleCapability.getNamespace());
		
			return new CompositeDataSupport(CustomBundleWiringStateMBean.BUNDLE_CAPABILITY_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form bundle capability open data.", e);
		}
	}

	
}