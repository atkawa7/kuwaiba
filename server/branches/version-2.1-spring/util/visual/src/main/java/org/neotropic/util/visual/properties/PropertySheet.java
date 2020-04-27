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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * An embeddable property sheet 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class PropertySheet extends Grid<AbstractProperty> {

    private TranslationService ts;
    
    private List<IPropertyValueChangedListener> propertyValueChangedListeners;
    
    private Button currentBtnCancelInEditProperty;
    
    public PropertySheet(TranslationService ts) {
        
        addThemeVariants(GridVariant.LUMO_COMPACT); 
        
        this.ts = ts;
        propertyValueChangedListeners = new ArrayList<>();

        addComponentColumn((property) -> {
            VerticalLayout lytName = new VerticalLayout();
            lytName.setSpacing(false);
            lytName.setPadding(false);
            Label lblName = new BoldLabel(property.toString()); //NOI18N
            Label lblType = new BoldLabel(property.getType()); 
            lblType.setClassName("text-secundary");
            lblName.setTitle(property.getDescription() == null || property.getDescription().isEmpty()
                    ? property.toString() : property.getDescription());
            lytName.add(lblName, lblType);
            return lytName;
        }).setHeader(ts.getTranslatedString("module.general.labels.attributenamme")).setFlexGrow(2);
        
        addComponentColumn((property) -> {
            
            VerticalLayout lytValue = new VerticalLayout();
            Label lblValue = new Label(property.getAsString()); 
            lblValue.setWidthFull();
            lytValue.add(lblValue);
            
            if (property.supportsInplaceEditor()) {
                AbstractField editField = property.getInplaceEditor();
                // if the property doesnt have a binder, then  set the value manually
                if (!property.hasBinder()) {
                    editField.setValue(property.getValue());
                }

                Button btnEdit = new Button(new Icon(VaadinIcon.CHECK_CIRCLE_O));
                Button btnCancel = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
                HorizontalLayout hlytEditField = new HorizontalLayout(editField, btnEdit, btnCancel);

                hlytEditField.setVisible(false);
                hlytEditField.setPadding(false);
                hlytEditField.setSpacing(false);

                btnEdit.addClickListener(e -> {
                    lblValue.setVisible(true);
                    hlytEditField.setVisible(false);
                    this.getColumnByKey("advancedEditor").setVisible(true);

                    if (editField.getValue() == null) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.error-null-value")).open();
                        return;
                    }

                    lblValue.setText(editField.getValue().toString());
                    if (!property.hasBinder()) {
                        property.setValue(editField.getValue());
                    }
                    this.currentBtnCancelInEditProperty = null;
                    firePropertyValueChangedEvent(property);

                });

                btnCancel.addClickListener(ev -> {
                    if (!property.hasBinder()) {
                        editField.setValue(property.getValue());
                    }
                    lblValue.setVisible(true);
                    hlytEditField.setVisible(false);
                    this.getColumnByKey("advancedEditor").setVisible(true);
                });

                lblValue.getElement().addEventListener("dblclick", e -> {
                    if (this.currentBtnCancelInEditProperty != null) 
                        currentBtnCancelInEditProperty.click();
                    
                    this.currentBtnCancelInEditProperty = btnCancel;

                    boolean visibilityValue = lblValue.isVisible();

                    lblValue.setVisible(!visibilityValue);
                    hlytEditField.setVisible(visibilityValue);

                    this.getColumnByKey("advancedEditor").setVisible(!visibilityValue);

                });

                lytValue.add(hlytEditField);
            }
       
            return lytValue;
        }).setHeader(ts.getTranslatedString("module.general.labels.value"))
                .setKey("value").setFlexGrow(4);
        
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
    
    public PropertySheet(TranslationService ts, List<AbstractProperty> properties, String caption) {
        this(ts);
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