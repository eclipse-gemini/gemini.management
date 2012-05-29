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

	/**
	 * Mask for listBundles method, that requires bundle location attribute to be 
	 * included in the returned data.
	 * 
	 */
	public final static int LOCATION = 1;
	
	/**
	 * Mask for listBundles method, that requires bundle id attribute to be 
	 * included in the returned data.
	 */
	public final static int IDENTIFIER = 2;
	
	/**
	 * Mask for listBundles method, that requires bundle symbolic name attribute to be 
	 * included in the returned data.
	 */
	public final static int SYMBOLIC_NAME = 2 << 1;
	
	/**
	 * Mask for listBundles method, that requires bundle version attribute to be 
	 * included in the returned data.
	 */
	public final static int VERSION = 2 << 2;
	
	/**
	 * Mask for listBundles method, that requires bundle start level attribute to be 
	 * included in the returned data.
	 */
	public final static int START_LEVEL = 2 << 3;
	
	/**
	 * Mask for listBundles method, that requires bundle state attribute to be 
	 * included in the returned data.
	 */
	public final static int STATE = 2 << 4;
	
	/**
	 * Mask for listBundles method, that requires bundle last modified attribute to be 
	 * included in the returned data.
	 */
	public final static int LAST_MODIFIED = 2 << 5;
	
	/**
	 * Mask for listBundles method, that requires bundle persistently started attribute to be 
	 * included in the returned data. 
	 */
	public final static int PERSISTENTLY_STARTED = 2 << 6;
	
	/**
	 * Mask for listBundles method, that requires bundle removal pending attribute to be 
	 * included in the returned data.
	 */
	public final static int REMOVAL_PENDING = 2 << 7;
	
	/**
	 * Mask for listBundles method, that requires bundle required attribute to be 
	 * included in the returned data.
	 */
	public final static int REQUIRED = 2 << 8;
	
	/**
	 * Mask for listBundles method, that requires bundle fragment attribute to be 
	 * included in the returned data.
	 */
	public final static int FRAGMENT = 2 << 9;
	
	/**
	 * Mask for listBundles method, that requires bundle registered services attribute to be 
	 * included in the returned data.
	 */
	public final static int REGISTERED_SERVICES = 2 << 10;
	
	/**
	 * Mask for listBundles method, that requires bundle services in use attribute to be 
	 * included in the returned data.
	 */
	public final static int SERVICES_IN_USE = 2 << 11;
	
	/**
	 * Mask for listBundles method, that requires bundle headers attribute to be 
	 * included in the returned data. 
	 */
	public final static int HEADERS = 2 << 12;
	
	/**
	 * Mask for listBundles method, that requires bundle exported packages attribute to be 
	 * included in the returned data.
	 */
	public final static int EXPORTED_PACKAGES = 2 << 13;
	
	/**
	 * Mask for listBundles method, that requires bundle imported packages attribute to be 
	 * included in the returned data.
	 */
	public final static int IMPORTED_PACKAGES = 2 << 14;
	
	/**
	 * Mask for listBundles method, that requires bundle fragments attribute to be 
	 * included in the returned data.
	 */
	public final static int FRAGMENTS = 2 << 15;
	
	/**
	 * Mask for listBundles method, that requires bundle hosts attribute to be 
	 * included in the returned data.
	 */
	public final static int HOSTS = 2 << 16;
	
	/**
	 * Mask for listBundles method, that requires bundle "requiring bundles" attribute to be 
	 * included in the returned data.
	 */
	public final static int REQUIRING_BUNDLES = 2 << 17;
	
	/**
	 * Mask for listBundles method, that requires bundle "required bundles" attribute to be 
	 * included in the returned data.
	 */
	public final static int REQUIRED_BUNDLES = 2 << 18;
	
	/**
	 * Mask for listBundles method, that requires bundle "activation policy used" attribute to be 
	 * included in the returned data.
	 */
	public final static int ACTIVATION_POLICY = 2 << 19;
	
	/**
	 * Mask for listBundles method, that returns all available data. Equivalent to listBundles()
	 */
	public final static int DEFAULT = LOCATION + IDENTIFIER
	+ SYMBOLIC_NAME + VERSION + START_LEVEL + STATE + LAST_MODIFIED 
	+ PERSISTENTLY_STARTED + ACTIVATION_POLICY + REMOVAL_PENDING + REQUIRED + FRAGMENT + REGISTERED_SERVICES
	+ SERVICES_IN_USE + HEADERS + EXPORTED_PACKAGES + IMPORTED_PACKAGES + FRAGMENTS 
	+ HOSTS + REQUIRING_BUNDLES + REQUIRED_BUNDLES;

	/**
	 * Answer the bundle state of the system in tabular form depending on the mask.
	 * 
	 * Each row of the returned table represents a single bundle. The Tabular
	 * Data consists of Composite Data that is type by {@link #BUNDLES_TYPE}.
	 *
	 * @param mask - representing the information that will be contained in the result
	 * @return the tabular representation of the bundle state
	 * @throws IOException
	 */
	TabularData listBundles(int mask) throws IOException;

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
