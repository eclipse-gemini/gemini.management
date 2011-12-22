/*******************************************************************************
 * Copyright (c) 2010 Oracle.
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
 *     Hal Hildebrand - Initial JMX support 
 ******************************************************************************/

package org.eclipse.gemini.mgmt.framework.internal;

import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;

import org.osgi.jmx.framework.FrameworkMBean;

/**
 * This class represents the CODEC for the resulting composite data from the
 * batch operations on the bundles in the <link>FrameworkMBean</link>. It serves
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
 * <td>Error</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>Completed</td>
 * <td>Array of long</td>
 * </tr>
 * <tr>
 * <td>BundleInError</td>
 * <td>long</td>
 * </tr>
 * <tr>
 * <td>Remaining</td>
 * <td>Array of long</td>
 * </tr>
 * </table>
 */
public final class BundleBatchActionResult {

	/**
	 * The list of bundles successfully completed
	 */
	private Long[] completed;
	
	/**
	 * The error message of a failed result
	 */
	private String errorMessage;
	
	/**
	 * True if the action completed without error
	 */
	private boolean success = true;
	
	/**
	 * The bundle in error or -1L if no bundle is in error
	 */
	private long bundleInError;

	/**
	 * The ids of the bundles remaining to be processed
	 */
	private Long[] remaining;
	
	/**
	 * Construct a result signifying the successful completion of the batch
	 * operation.
	 */
	public BundleBatchActionResult() {
		success = true;
	}

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
	public BundleBatchActionResult(String errorMessage, Long[] completed, long bundleInError, Long[] remaining) {
		success = false;
		this.errorMessage = errorMessage;
		this.completed = completed;
		this.bundleInError = bundleInError;
		this.remaining = remaining;
	}

	/**
	 * Answer the receiver encoded as CompositeData
	 * 
	 * @return the CompositeData encoding of the receiver.
	 */
	public CompositeData asCompositeData() {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(FrameworkMBean.SUCCESS, success);
		items.put(FrameworkMBean.ERROR, errorMessage);
		items.put(FrameworkMBean.COMPLETED, completed);
		items.put(FrameworkMBean.BUNDLE_IN_ERROR, bundleInError);
		items.put(FrameworkMBean.REMAINING, remaining);

		try {
			return new CompositeDataSupport( FrameworkMBean.BATCH_ACTION_RESULT_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form batch result open data", e);
		}
	}

}
