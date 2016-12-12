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
package org.kuwaiba.web.properties;

import com.google.common.eventbus.EventBus;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * Represents a property sheet for the properties of a given object
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 * @param <T> type of objects that can be set as a list in the properties 
 */
public class PropertySheet<T extends Object> extends Table{

    private final EventBus eventBus;
    private Label attributeDescription;
    private BeanItem<RemoteBusinessObject> bean;
            
    public PropertySheet(RemoteBusinessObject remoteBusinessObject, EventBus eventBus) {
        this.eventBus = eventBus;
        bean = new BeanItem<> (remoteBusinessObject);
        addStyleName("components-inside");
        addStyleName(ValoTheme.TABLE_NO_HEADER);
        addContainerProperty("Attribute", Label.class, null);
        addContainerProperty("Value", Component.class, null);
    }
    
    public void createDateProperty(String attributeName, Date date, int i){
        DateProperty dateField = new DateProperty(attributeName, date);
        dateField.addValueChangeListener(new ValueChange(attributeName));
        this.addItem(new Object[] {new Label(attributeName), dateField}, i);
    }
    
    public void createPrimitiveField(String attributeName, Object value, String type, int i){
        switch (type) {
            case "String":
            case "Integer":
            case "Float":
            case "Long":
                PrimitiveProperty primitiveField = new PrimitiveProperty(attributeName, (String)value);
                primitiveField.addValueChangeListener(new ValueChange(attributeName));
                this.addItem(new Object[] {new Label(attributeName), primitiveField}, i);
//        else if(type.equals("Integer"))
//            this.addItem(new Object[] {new Label(attributeName), new primitiveProperty(Integer.valueOf((String)value))}, i);
                break;
            case "Boolean":
                this.addItem(new Object[] {new Label(attributeName), new BooleanProperty((boolean)value)}, i);
                break;
        }
    } 
    
    public void createListTypeField(String attributeName, List<T> list, T actualValueId, int i){
        ListTypeProperty listProperty = new ListTypeProperty(list, actualValueId);
        listProperty.addValueChangeListener(new ValueChange(attributeName));
        this.addItem(new Object[] {new Label(attributeName), listProperty}, i);
    }

    public void createDefaults(int i) {
        //TODO put this in a single row
        this.addItem(new Object[] {new Label("Description"), attributeDescription}, i);
    }
    
    private class PrimitiveProperty extends TextField{

        private String attributeName;
        
        public PrimitiveProperty(String attributeName, String value) {
            super();
            this.attributeName = attributeName;
            setValue(value == null ? "" : (String)value);
        }
        
        public PrimitiveProperty(int value) {
            super();
            setConverter(Integer.class);
            setValue(Integer.toString(value));
            //this.addValidator(new MyValidator());
        }
        
        public String getAttributeName() {
            return attributeName;
        }
    }
    
    private class DateProperty extends DateField {
        
        private String attributeName;
        
        public DateProperty(String attributeName, Date date) {
            super();
            this.attributeName = attributeName;
            this.setResolution(Resolution.MINUTE);
            this.setValue(date);
        }

        public String getAttributeName() {
            return attributeName;
        }
    }
    
    private class ListTypeProperty<T extends Object> extends ComboBox {

        public ListTypeProperty(List<T> list, T actualValue) {
            this.setTextInputAllowed(false);
            //this.setNullSelectionAllowed(false);
            for (T item : list) 
                addItem(item);
            this.select(actualValue);
        }
    }    
    
    private class BooleanProperty extends CheckBox{
        public BooleanProperty(boolean value) {
            setValue(value);
        }
        
    }
    
    private class MyValidator implements Validator {
        @Override
        public void validate(Object value)
                throws InvalidValueException {
            if (!(value instanceof Integer &&
                    (value instanceof Double))) {
                throw new InvalidValueException("The value is not a number");
            }     
        }
    }
    
    private class ValueChange implements ValueChangeListener{
        
        private String selectedAttributeName;
        
        public ValueChange(String selectedAttributeName) {
            this.selectedAttributeName = selectedAttributeName;
        }

        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            Object value = event.getProperty().getValue();
            HashMap <String, List<String>> attributes = null;
            if(selectedAttributeName.equals("name") ||
                    selectedAttributeName.equals("className"))
                bean.getItemProperty(selectedAttributeName).setValue(value);
         
            else{
                attributes = (HashMap <String, List<String>>)bean.getItemProperty("attributes").getValue();
                List<String> updatedValue = new ArrayList<>();
                if(selectedAttributeName.equals("creationDate"))
                        updatedValue.add(Long.toString(((Date)value).getTime()));
                else
                    updatedValue.add((String) value);
                List<String> get = attributes.get(selectedAttributeName);
                if(get == null)
                    attributes.put(selectedAttributeName, updatedValue);
                else{
                    
                    attributes.replace(selectedAttributeName, updatedValue);
                }
            }
            //only notifies the node in the tree if the name has changed
            if(selectedAttributeName.equals(Constants.PROPERTY_NAME))
                eventBus.post(event);
            
            BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
            
            
            try {
                bem.updateObject((String)(bean.getItemProperty(Constants.PROPERTY_CLASS_NAME).getValue()),
                        (Long)(bean.getItemProperty(Constants.PROPERTY_ID).getValue()) , attributes);
            } catch (InventoryException ex) {
                Logger.getLogger(PropertySheet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

       
    }
}
