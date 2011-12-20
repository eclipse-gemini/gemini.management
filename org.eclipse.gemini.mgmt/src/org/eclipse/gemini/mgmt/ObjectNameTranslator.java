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

/**
 * {@link ObjectNameTranslator} maps JMX object names defined by Gemini Management before they are used to publish
 * mbeans. The purpose is to allow multiple instances of Gemini Management to run in a single OSGi framework without
 * their object names colliding.
 * <p />
 * For more background, see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=366235#c1">bug 366235</a>.
 * <p />
 * One and only one implementation of this interface may be configured into Gemini Management by attaching a fragment to
 * Gemini Management including the implementation class and a manifest header like this:
 * <p />
 * <code>GeminiManagement-ObjectNameTranslator: MyObjectNameTranslator</code>
 * <p />
 * The default behaviour of Gemini Management when no object name translator is configured is the same as if an object
 * name translator was configured which translates each object name to itself.
 * <p />
 * <strong>Concurrent Semantics</strong><br />
 * Implementations of this interface must be thread safe.
 */
public interface ObjectNameTranslator {

    public static final String HEADER_NAME = "GeminiManagement-ObjectNameTranslator";
    
    /**
     * Translates the given {@link ObjectName}. The translation must be injective, which means that translations of
     * distinct object names must be distinct.
     * 
     * @param objectName the {@link ObjectName} to be translated
     * @return the translated {@link ObjectName}
     */
    ObjectName translate(ObjectName objectName);

}
