/*******************************************************************************
 * Copyright (c) 2010 SAP.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at 
 *     http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     SAP employees 
 ******************************************************************************/
package org.eclipse.gemini.management.framework;

import java.io.IOException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;

import org.osgi.jmx.Item;
import org.osgi.jmx.framework.BundleStateMBean;

public interface CustomBundleStateMBean extends BundleStateMBean{

	//New methods from the JMX Update RFC 169
	
	/**
	 * The key PERSISTENTLY_STARTED, used in {@link #PERSISTENTLY_STARTED_ITEM}.
	 */
	String ACTIVATION_POLICY_USED = "ActivationPolicyUsed";

	/**
	 * The item containing the indication of persistently started in
	 * {@link #BUNDLE_TYPE}. The key is {@link #PERSISTENTLY_STARTED} and the
	 * the type is {@link SimpleType#BOOLEAN}.
	 */
	Item ACTIVATION_POLICY_ITEM = new Item(ACTIVATION_POLICY_USED,	"Whether the bundle is using an activation policy", SimpleType.BOOLEAN);
	
	CompositeType CUSTOM_BUNDLE_TYPE = Item.compositeType("BUNDLE",
			"This type encapsulates OSGi bundles", EXPORTED_PACKAGES_ITEM,
			FRAGMENT_ITEM, FRAGMENTS_ITEM, HEADERS_ITEM, HOSTS_ITEM,
			IDENTIFIER_ITEM, IMPORTED_PACKAGES_ITEM, LAST_MODIFIED_ITEM,
			LOCATION_ITEM, PERSISTENTLY_STARTED_ITEM, ACTIVATION_POLICY_ITEM, 
			REGISTERED_SERVICES_ITEM, REMOVAL_PENDING_ITEM, REQUIRED_ITEM, 
			REQUIRED_BUNDLES_ITEM, REQUIRING_BUNDLES_ITEM, START_LEVEL_ITEM, 
			STATE_ITEM, SERVICES_IN_USE_ITEM, SYMBOLIC_NAME_ITEM, VERSION_ITEM);

	/**
	 * The Tabular Type for a list of bundles. The row type is
	 * {@link #BUNDLE_TYPE}.
	 */
	TabularType CUSTOM_BUNDLES_TYPE = Item.tabularType("BUNDLES", "A list of bundles",
			BUNDLE_TYPE, new String[] { BundleStateMBean.IDENTIFIER });
	
	/**
	 * 
	 * @param bundleId
	 * @return
	 * @throws IOException
	 */
	CompositeData getBundle(long bundleId) throws IOException;

	/**
	 * 
	 * @param bundleTypeItems
	 * @return
	 * @throws IOException
	 */
	TabularData listBundles(String... bundleTypeItems) throws IOException;
	
	/**
	 * 
	 * @return
	 */
	boolean isActivationPolicyUsed(long bundleId) throws IOException;
	
	/**
	 * 
	 * @param bundleId
	 * @param key
	 * @return
	 * @throws IOException
	 */
	String getHeader(long bundleId, String key) throws IOException;
	
	/**
	 * 
	 * @param bundleId
	 * @param locale
	 * @return
	 * @throws IOException
	 */
	TabularData getHeaders(long bundleId, String locale) throws IOException;
	
	/**
	 * 
	 * @param bundleId
	 * @param key
	 * @param locale
	 * @return
	 * @throws IOException
	 */
	CompositeData getHeaders(long bundleId, String key, String locale) throws IOException;
}
