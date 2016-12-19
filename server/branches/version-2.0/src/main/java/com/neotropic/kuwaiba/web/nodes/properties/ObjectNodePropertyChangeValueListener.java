/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.web.nodes.properties;

import com.google.common.eventbus.EventBus;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.web.properties.AbstractNodePorpertyValueChangeListener;
import org.kuwaiba.web.properties.PropertySheet;

/**
 * Value change listener for the fields in the property sheet of a 
 * RemoteBusinessObject. 
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class ObjectNodePropertyChangeValueListener extends AbstractNodePorpertyValueChangeListener {
    
    ObjectNodePropertyChangeValueListener(BeanItem<RemoteBusinessObject> beanItem, EventBus eventBus) {
        super(beanItem, eventBus);
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {

        Object value = event.getProperty().getValue();
        HashMap <String, List<String>> attributes = (HashMap <String, List<String>>)object.getItemProperty("attributes").getValue();
        List<String> updatedValue = new ArrayList<>();
         //only notifies the tree if the name has changed
        if(attributeCaption.equals(Constants.PROPERTY_NAME)){
            eventBus.post(event); 
            object.getItemProperty(attributeCaption).setValue(value);
            updatedValue.add((String)object.getItemProperty(attributeCaption).getValue());
        }
        
        if(attributeCaption.equals(Constants.PROPERTY_CREATION_DATE))
            updatedValue.add(Long.toString(((Date)value).getTime()));
        else{
            if(attributeType != null) 
                updatedValue.add((String) value);
            else
                updatedValue.add(Long.toString(((RemoteBusinessObjectLight)value).getId()));
        }
        List<String> get = attributes.get(attributeCaption);
        if(get == null)
            attributes.put(attributeCaption, updatedValue);
        else
            attributes.replace(attributeCaption, updatedValue);
        
        
        BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();

        try {
            bem.updateObject((String)(object.getItemProperty(Constants.PROPERTY_CLASS_NAME).getValue()),
                    (Long)(object.getItemProperty(Constants.PROPERTY_ID).getValue()) , attributes);
        } catch (InventoryException ex) {
            Logger.getLogger(PropertySheet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
