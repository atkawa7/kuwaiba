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
package org.kuwaiba.apis.web.gui.nodes;

import com.google.common.eventbus.EventBus;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.nodes.properties.AbstractNodePorpertyValueChangeListener;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * Value change listener for the fields in the property sheet of a 
 * RemoteBusinessObject. 
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class ObjectNodePropertyChangeValueListener extends AbstractNodePorpertyValueChangeListener {
    
    ObjectNodePropertyChangeValueListener(TopComponent parentComponent, 
            BeanItem<RemoteObject> beanItem, EventBus eventBus) 
    {
        super(parentComponent, beanItem, eventBus);
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        try {
            Object value = event.getProperty().getValue();
            String[] attributes = (String[])object.getItemProperty("attributes").getValue();
            String[][] values = (String[][])object.getItemProperty("values").getValue();
            int k=0;
            for(String attibuteName : attributes){
                if(attibuteName.equals(attributeCaption))
                    break;
                k++;
            }
            // Notifies if the name has changed
            if(attributeCaption.equals(Constants.PROPERTY_NAME)) {
                Property.ValueChangeEvent eventOID = new Property.ValueChangeEvent() {

                    @Override
                    public Property getProperty() {
                        return object.getItemProperty(Constants.PROPERTY_OID);
                    }
                };                
                eventBus.post(new Property.ValueChangeEvent[]{eventOID, event});
            }
            if(k >= attributes.length){
                //if is an attribute without value
                String[] newAttributes = new String[k+1];
                String[][] newValues = new String[k+1][1];
                System.arraycopy(attributes, 0, newAttributes, 0, attributes.length);
                System.arraycopy(values, 0, newValues, 0, values.length);
                newAttributes[k] = attributeCaption;
                attributes = newAttributes;
                values = newValues;
            }
            
            switch (attributeType) {
                case "String":
                    values[k][0] = (String) value;
                    break;
                case "Boolean":
                    values[k][0] = Boolean.toString((boolean)value);
                    break;
                case "ListType":
                    values[k][0] = Long.toString(((RemoteObjectLight)value).getOid());
                    break;
            }
            
//            parentComponent.getWsBean().updateObject((String)(object.getItemProperty(Constants.PROPERTY_CLASS_NAME).getValue()),
//                    (long)(object.getItemProperty(Constants.PROPERTY_OID).getValue()),
//                    attributes, values,
//                    Page.getCurrent().getWebBrowser().getAddress(), 
//                    parentComponent.getApplicationSession().getSessionId());
            throw new ServerSideException("Fix Me!");
        } catch (ServerSideException ex) {
            Logger.getLogger(ObjectNodePropertyChangeValueListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
