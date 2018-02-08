/*
 * Copyright (c) 2017 johnyortega.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    johnyortega - initial API and implementation and/or initial documentation
 */
package org.inventory.core.templates.layouts.lookup;

import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * To Consider: depending of the context you must keep updated the objects content
 * in the lookup with the InstanceContent
 * @author johnyortega
 */
public class SharedContent {
    private final InstanceContent instanceContent;
    private final AbstractLookup abstractLookup;
    
    private static SharedContent instance;    
    
    private SharedContent() {
        instanceContent = new InstanceContent();
        abstractLookup = new AbstractLookup(instanceContent);                
    }
    
    public static SharedContent getInstance() {
        return instance == null ? instance = new SharedContent() : instance;
    }
    
    public InstanceContent getInstanceContent() {
        return instanceContent;
    }
    
    public AbstractLookup getAbstractLookup() {
        return abstractLookup;
    }
}
