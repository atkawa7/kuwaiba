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

//import com.vaadin.data.util.BeanItem;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
//import com.vaadin.ui.Table;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Date;
import java.util.List;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * Represents the fields of a property sheet for the properties of a given object
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 * @param <T> type of objects that can be set as a list in the properties 
 */
public class Sheet<T extends Object> /*extends Table*/ {
    private final AbstractNodePorpertyValueChangeListener<T> valueChangeListener;
    //protected BeanItem<T> object;
    private final Label descriptionText;  
        
    public Sheet(/*BeanItem<T> object,*/ AbstractNodePorpertyValueChangeListener<T> valueChangeListener) {
        //this.object = object;
        this.valueChangeListener = valueChangeListener;
        descriptionText = new Label();     
//        addStyleName("components-inside");
//        addStyleName(ValoTheme.TABLE_NO_HEADER);
//        addContainerProperty("Attribute", Label.class, null);
//        addContainerProperty("Value", Component.class, null);
        addSelectedObjectId();
    }
    
    public void createDateProperty(String propertyCaption, String description, Date date, int i){
        PorpertyDate dateField = new PorpertyDate(date);
        dateField.setEnabled(false);
//        this.addItem(new Object[] {new Label(propertyCaption), dateField}, i);
    }
    
    public void createPrimitiveField(String propertyCaption, String description, Object value, String type, int i){
        
        switch (type) {
            case "String":
            case "Float":
            case "Long":
            case "Integer":
                PropertyPrimitive stringProperty = new PropertyPrimitive(propertyCaption, type, (String)value);
                stringProperty.addFocusListener(valueChangeListener);
                //stringProperty.addValueChangeListener(valueChangeListener);
                stringProperty.addFocusListener(new descriptionListener(description));
                //this.addItem(new Object[] {new Label(propertyCaption), stringProperty}, i);
                break;
//           case "Integer":
//                PrimitiveProperty numericProperty = new PrimitiveProperty(Integer.valueOf((String)value));
//                numericProperty.addFocusListener(valueChangeListener);
//                numericProperty.addValueChangeListener(valueChangeListener);
//                this.addItem(new Object[] {new Label(propertyCaption), numericProperty}, i);
//                break;
            case "Boolean":
                if(((String)value).isEmpty())
                    value = "false";
                PorpertyBoolean booleanProperty = new PorpertyBoolean(propertyCaption, Boolean.valueOf((String)value));
                //booleanProperty.addValueChangeListener(valueChangeListener);
                booleanProperty.addFocusListener(valueChangeListener);
                //this.addItem(new Object[] {new Label(propertyCaption), booleanProperty}, i);
                break;
        }
    } 
    
    public void createListTypeField(String propertyCaption, String description, List<T> list, T actualValue, int i){
        PropertyListType listProperty = new PropertyListType(propertyCaption, list, actualValue);
        //listProperty.addValueChangeListener(valueChangeListener);
        listProperty.addFocusListener(valueChangeListener);
        //this.addItem(new Object[] {new Label(propertyCaption), listProperty}, i);
        
    }
    
    private void addDescription(){
        Label description = new Label("Description: ");
        descriptionText.setValue("");
        description.addStyleName(ValoTheme.LABEL_BOLD);
        descriptionText.addStyleName(ValoTheme.LABEL_BOLD);
        //this.addItem(new Object[] {description, descriptionText}, 110);
    }
    
    private void addSelectedObjectId(){
//        if(object != null){
//            Label objectId = new Label(Long.toString((long)object.getItemProperty(Constants.PROPERTY_OID).getValue()));
//            objectId.addStyleName(ValoTheme.LABEL_BOLD);
//            Label id = new Label(Constants.PROPERTY_ID);
//            id.addStyleName(ValoTheme.LABEL_BOLD);
//            this.addItem(new Object[] {id, objectId}, 0);
//        }
    }
   
    private class descriptionListener implements FieldEvents.FocusListener{

        private final String propertyDescription;

        public descriptionListener(String propertyDescription) {
            this.propertyDescription = propertyDescription;
        }
        
        @Override
        public void focus(FieldEvents.FocusEvent event) {
           descriptionText.setValue(propertyDescription);
        }
    }
}