/*
 *  Copyright 2010-2016, Neotropic SAS <contact@neotropic.co>
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
package org.inventory.navigation.applicationnodes.objectnodes.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.nodes.PropertySupport;

/**
 * This class allows to edit date-like properties.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DateTypeProperty extends PropertySupport.ReadWrite<Date> {
    private Date value;
    private ObjectNode node;
    
    public DateTypeProperty(Date date, String name, Class<Date> type, 
                                String displayName, String shortDescription, ObjectNode node) {
        super(name, type, displayName, shortDescription);
        this.value = date;
        this.node = node;
    }
    
    @Override
    public Date getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }

    @Override
    public void setValue(Date value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        try {
            Utils.updateObject(node.getObject().getClassName(), node.getObject().getOid(), getName(), value);
            this.value = value;
        } catch (Exception ex) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, ex.getMessage());
        }
        
    }
    @Override
    public PropertyEditor getPropertyEditor(){        
        return new DatePickerPropertyEditorSupport(value);
    }

    @Override
    public boolean canWrite(){
        //The creation date is read-only data
        return !getName().equals(Constants.PROPERTY_CREATIONDATE);   
    }
}
