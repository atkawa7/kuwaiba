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

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.nodes.PropertySupport;

/**
 * Property sheet for Subnet Pool nodes
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class GeneralProperty extends PropertySupport.ReadWrite {
    
    private Object value;
    private ObjectNode node;

    public GeneralProperty(String name, Class type, String displayName, 
            String shortDescription, ObjectNode node, Object value) {
        super(name, type, displayName, shortDescription);
        this.value = value;
        this.node = node;
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        LocalObject update = new LocalObject(node.getObject().getClassName(), node.getObject().getOid(), 
                new String[]{this.getName()}, new Object[]{t});

        if(!CommunicationsStub.getInstance().saveObject(update))
            NotificationUtil.getInstance().showSimplePopup("Error", 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            value = t;
            if (getName().equals(Constants.PROPERTY_NAME))
                node.getObject().setName((String)t);
        }
    }
    
    @Override
    public PropertyEditor getPropertyEditor(){        
        return super.getPropertyEditor();
    }
}
