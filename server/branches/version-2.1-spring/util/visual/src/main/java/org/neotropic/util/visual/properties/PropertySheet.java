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


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
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
            Label label = new Label( property.getName() ); //NOI18N
            return label;
        }).setHeader("Attribute Name");
        addComponentColumn((property) -> {
            Label label = new Label(property.getAsString());
            return label;
        }).setHeader("Value");
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
            return new Button("...");
        }).setHeader("");
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
