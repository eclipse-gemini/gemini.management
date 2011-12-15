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
package org.eclipse.gemini.mgmt.internal;

import java.io.IOException;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

public final class ServiceUtil {
	
	/**
	 * 
	 * @param serviceRef
	 * @return
	 * @throws IOException
	 */
	public static long[] getBundlesUsing(ServiceReference<?> serviceRef) {
		Bundle[] bundles = serviceRef.getUsingBundles();
		long[] ids = new long[bundles.length];
		for (int i = 0; i < bundles.length; i++) {
			ids[i] = bundles[i].getBundleId();
		}
		return ids;
	}
}
