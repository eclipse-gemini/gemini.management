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
package org.eclipse.gemini.mgmt.framework.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularDataSupport;

import org.eclipse.gemini.mgmt.framework.CustomBundleWiringStateMBean;
import org.eclipse.gemini.mgmt.internal.BundleWiringUtil;
import org.eclipse.gemini.mgmt.internal.OSGiProperties;
import org.osgi.framework.wiring.BundleRequirement;

public class OSGiBundleRequirement {
	
	private final BundleRequirement bundleRequirement;

	public OSGiBundleRequirement(BundleRequirement bundleRequirement) {
		this.bundleRequirement = bundleRequirement;
	}
	
	public CompositeData asCompositeData() {
		try {
			TabularDataSupport tabularAttributes = new TabularDataSupport(CustomBundleWiringStateMBean.ATTRIBUTES_TYPE);
			Map<String, Object> attributes = bundleRequirement.getAttributes();
			for (Entry<String, Object> attribute : attributes.entrySet()) {
				tabularAttributes.put(OSGiProperties.encode(attribute.getKey(), attribute.getValue()));
			}
	
			TabularDataSupport tabularDirectives = new TabularDataSupport(CustomBundleWiringStateMBean.DIRECTIVES_TYPE);
			Map<String, String> directives = bundleRequirement.getDirectives();
			for (Entry<String, String> directive : directives.entrySet()) {
				tabularDirectives.put(new CompositeDataSupport(CustomBundleWiringStateMBean.PROPERTY_TYPE, BundleWiringUtil.getDirectiveKeyValueItem(directive.getKey(), directive.getValue())));
			}
			
			Map<String, Object> items = new HashMap<String, Object>();
			items.put(CustomBundleWiringStateMBean.ATTRIBUTES, tabularAttributes);
			items.put(CustomBundleWiringStateMBean.DIRECTIVES, tabularDirectives);
			items.put(CustomBundleWiringStateMBean.NAMESPACE, bundleRequirement.getNamespace());
			
			return new CompositeDataSupport(CustomBundleWiringStateMBean.BUNDLE_REQUIREMENT_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form bundle requirment open data", e);
		}
	}
	
}