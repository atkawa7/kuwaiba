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
package org.neotropic.util.visual.properties;


import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * An embeddable property sheet 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class PropertySheet extends Grid<AbstractProperty> {

    private List<IPropertyValueChangedListener> propertyValueChangedListeners;
    
    public PropertySheet() {
        
        propertyValueChangedListeners = new ArrayList<>();

        setSizeUndefined();
        addComponentColumn((property) -> {
            Label label = new Label( property.getName()); //NOI18N
            return label;
        }).setHeader("Attribute Name").setWidth("100px");
        addComponentColumn((property) -> {
            
            Label labelValue = new Label(property.getAsString());
            
            AbstractField editField = property.getInplaceEditor();
            editField.setValue(property.getValue());
            editField.setVisible(false);
            editField.getElement().addEventListener("change", ev -> {
                labelValue.setText(editField.getValue().toString());
                property.setValue( editField.getValue());
                labelValue.setVisible(true);
                editField.setVisible(false);
                firePropertyValueChangedEvent(property);
                this.getColumnByKey("advancedEditor").setVisible(true);
            });
            
            VerticalLayout lytValue = new VerticalLayout(labelValue, editField);
            lytValue.getElement().addEventListener("dblclick", e -> {
                 boolean visibilityValue = labelValue.isVisible();

                 labelValue.setVisible(!visibilityValue);
                 editField.setVisible(visibilityValue);
                 
                 this.getColumnByKey("advancedEditor").setVisible(!visibilityValue);
                 
             });
            
            return lytValue;
        }).setHeader("Value").setKey("value");
        addComponentColumn((property) -> {
            if (property.supportsAdvancedEditor()) {
                 Button btnAdvancedEditor = new Button("...", ev -> {
                     
                     AdvancedEditorDialog dialog = new AdvancedEditorDialog(property);
                     dialog.getAccept().addClickListener(clickEv -> {
                         dialog.loadNewValueIntoProperty();
                         firePropertyValueChangedEvent(dialog.getProperty());
                         dialog.close();
                     });
                     dialog.open();
                     
                 });
                 return btnAdvancedEditor;
            }
            return new HorizontalLayout();
        }).setHeader("").setKey("advancedEditor");
    }
    
    public PropertySheet(List<AbstractProperty> properties, String caption) {
        this();
        setItems(properties);     
    }

    public void clear() {
        setItems();
    }
    
    public interface IPropertyValueChangedListener{
         void updatePropertyChanged(AbstractProperty<? extends Object> property);
    }
    
    public void firePropertyValueChangedEvent(AbstractProperty property) {
        for (IPropertyValueChangedListener l : propertyValueChangedListeners) {
            l.updatePropertyChanged(property);
        }
    }
    
    public void addPropertyValueChangedListener(IPropertyValueChangedListener iPropertyValueChangedListener) {
        propertyValueChangedListeners.add(iPropertyValueChangedListener);
    }

}
