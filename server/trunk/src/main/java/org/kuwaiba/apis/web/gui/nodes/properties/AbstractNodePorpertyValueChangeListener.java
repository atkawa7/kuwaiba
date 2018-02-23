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
package org.kuwaiba.apis.web.gui.nodes.properties;

import com.google.common.eventbus.EventBus;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.FieldEvents;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * Abstract listener for the fields in the property sheet.
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 * @param <T> the kind of object in the listen class
 */
public abstract class AbstractNodePorpertyValueChangeListener<T extends Object>  
    implements Property.ValueChangeListener, FieldEvents.FocusListener{

    protected final EventBus eventBus;
    protected String attributeCaption;
    protected String attributeType;
    protected BeanItem<T> object;
    protected TopComponent parentComponent;

    public AbstractNodePorpertyValueChangeListener(TopComponent parentComponent, BeanItem<T> object, EventBus eventBus){
        this.object = object;
        this.eventBus = eventBus;
        this.parentComponent = parentComponent;
    }

    public void setAttributeCaption(String attributeCaption) {
        this.attributeCaption = attributeCaption;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }
    
    @Override
    public abstract void valueChange(Property.ValueChangeEvent event);

    @Override
    public void focus(FieldEvents.FocusEvent event){
        Object source = event.getSource();
        
        if(source instanceof PropertyPrimitive){
            attributeCaption = ((PropertyPrimitive)source).getPropertyName();
            attributeType = ((PropertyPrimitive)source).getPropertyType();
        }
        else if(source instanceof PropertyListType){
            attributeCaption = ((PropertyListType)source).getPropertyName();
            attributeType = "ListType";
        }
        else if(source instanceof PorpertyBoolean){
            attributeCaption = ((PorpertyBoolean)source).getPropertyName();
            attributeType = "Boolean";
        }
        else{
            attributeCaption = Constants.PROPERTY_CREATION_DATE;
            attributeType = Constants.PROPERTY_CREATION_DATE;
        }
    }
}
