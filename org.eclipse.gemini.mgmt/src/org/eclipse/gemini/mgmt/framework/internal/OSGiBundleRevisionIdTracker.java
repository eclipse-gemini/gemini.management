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

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.wiring.BundleRevision;

/**
 * This class is thread-safe
 *
 */
public final class OSGiBundleRevisionIdTracker {

	private final List<BundleRevision> mappings = new ArrayList<BundleRevision>();
	
	private final Object myLock = new Object();
	
	/**
	 * Returns the unique id for the given bundleRevision, assigning a new id 
	 * if the bundleRevision has not been seen before
	 * 
	 * @param revision
	 * @return
	 */
	public int getRevisionId(BundleRevision revision){
		synchronized (myLock) {
			if(!mappings.contains(revision)){
				mappings.add(revision);
			}
			return mappings.indexOf(revision);
		}
	}
	
}
