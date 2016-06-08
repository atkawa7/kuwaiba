/*
 * Copyright (c) 2016 adrian.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    adrian - initial API and implementation and/or initial documentation
 */
package com.neotropic.inventory.modules.ipam.nodes.properties;

import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.PropertySupport;

/**
 * Property sheet for Subnet Pool nodes
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SubnetPoolProperty extends PropertySupport.ReadOnly {
    
    private Object value;

    public SubnetPoolProperty(String name, Class type, String displayName, 
            String shortDescription, Object value) 
    {
        super(name, type, displayName, shortDescription);
        this.value = value;
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
