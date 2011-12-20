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

public class TestObjectNameTranslator implements ObjectNameTranslator {

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectName translate(ObjectName objectName) {
        return objectName;
    }

}
