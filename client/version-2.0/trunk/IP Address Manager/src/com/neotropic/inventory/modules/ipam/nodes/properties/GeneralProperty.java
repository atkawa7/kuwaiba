/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.inventory.modules.ipam.nodes.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.nodes.PropertySupport;

/**
 * Property sheet for Subnet Pool nodes
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
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
        HashMap<String, Object> attributesToUpdate = new HashMap<>();
        attributesToUpdate.put(getName(), t);

        if(!CommunicationsStub.getInstance().updateObject(node.getObject().getClassName(), 
                node.getObject().getId(), attributesToUpdate))
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
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
