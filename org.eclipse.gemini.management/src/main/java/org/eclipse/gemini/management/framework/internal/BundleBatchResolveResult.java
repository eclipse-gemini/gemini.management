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

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;

import org.osgi.jmx.framework.FrameworkMBean;

/**
 * This class represents the CODEC for the resulting composite data from the
 * batch resolve on the bundles in the <link>FrameworkMBean</link>. It serves
 * as both the documentation of the type structure and as the codification of
 * the mechanism to convert to/from the CompositeData.
 * <p>
 * The structure of the composite data is:
 * <table border="1">
 * <tr>
 * <td>Success</td>
 * <td>Boolean</td>
 * </tr>
 * <tr>
 * <td>Completed</td>
 * <td>Array of long</td>
 * </tr>
 * </table>
 */
public final class BundleBatchResolveResult {

	/**
	 * The list of bundles successfully completed
	 */
	private Long[] completed;
	
	/**
	 * True if the action completed without error
	 */
	private boolean success = true;

	/**
	 * Construct a result indictating the failure of a batch operation.
	 * 
	 * @param errorMessage
	 *            - the message indicating the error
	 * @param completed
	 *            - the list of bundle identifiers indicating bundles that have
	 *            successfully completed the batch operation
	 * @param bundleInError
	 *            - the identifier of the bundle which produced the error
	 * @param remaining
	 *            - the list of bundle identifiers which remain unprocessed
	 */
	public BundleBatchResolveResult(Long[] completed, boolean success) {
		this.success = success;
		this.completed = completed;
	}

	/**
	 * Answer the receiver encoded as CompositeData
	 * 
	 * @return the CompositeData encoding of the receiver.
	 */
	public CompositeData asCompositeData() {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(FrameworkMBean.SUCCESS, success);
		items.put(FrameworkMBean.COMPLETED, completed);

		try {
			return new CompositeDataSupport( FrameworkMBean.BATCH_RESOLVE_RESULT_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form batch result open data", e);
		}
	}

}
