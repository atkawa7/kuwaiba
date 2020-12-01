/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>
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
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import elemental.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * An embeddable property sheet.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
public class PropertySheet extends Grid<AbstractProperty> {

    private TranslationService ts;
    
    private List<IPropertyValueChangedListener> propertyValueChangedListeners;
    
    private Button currentBtnCancelInEditProperty;
    
    private boolean readOnly;
    
    private String title;

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
     
    public PropertySheet(TranslationService ts) {
        this.ts = ts;
        addClassName("grid-compact");
        setHeightByRows(true);
        propertyValueChangedListeners = new ArrayList<>();

        addComponentColumn((property) -> {
            HorizontalLayout lytName = new HorizontalLayout();
            lytName.setClassName("property-sheet-name-cell");
            Label lblName = new BoldLabel(property.toString()); 
            Label lblType = new BoldLabel(property.getType()); 
            lblType.setClassName("text-secondary");
            if (property.isReadOnly())
                lblType.addClassName("italic");
            lblName.setTitle(property.getDescription() == null || property.getDescription().isEmpty()
                    ? property.toString() : property.getDescription());
            lytName.add(lblName, lblType);
            return lytName;
        }).setHeader(ts.getTranslatedString("module.general.labels.attribute-name")).setKey("name").setFlexGrow(3);
        
        addComponentColumn((property) -> {
            VerticalLayout lytValue = new VerticalLayout();
            lytValue.setPadding(false);
            Label lblValue = new Label(property.getAsString()); 
            lblValue.setWidthFull();
            lytValue.add(lblValue);
            
            if (!property.isReadOnly() && !readOnly && property.supportsInplaceEditor()) {
                Button btnEdit = new Button(new Icon(VaadinIcon.CHECK_CIRCLE_O));
                Button btnCancel = new Button(new Icon(VaadinIcon.CLOSE_SMALL));              
                AbstractField editField = property.getInplaceEditor();
                // if the property doesnt have a binder, then  set the value manually
                if (!property.hasBinder())
                    editField.setValue(property.getAsString().equals(AbstractProperty.NULL_LABEL) ? "" : property.getValue());
               
                btnEdit.addClassName("icon-button");             
                btnCancel.addClassName("icon-button");
                HorizontalLayout hlytEditField = new HorizontalLayout(editField, btnEdit, btnCancel);
                hlytEditField.setVisible(false);
                hlytEditField.setPadding(false);
                hlytEditField.setSpacing(true);
                hlytEditField.setAlignItems(FlexComponent.Alignment.CENTER);
                
                editField.getElement().addEventListener("keydown", event ->
                {   
                    JsonObject data =  event.getEventData();
                    if (13 == data.getNumber("event.which")) 
                            btnEdit.click();                  
                    if (27 == data.getNumber("event.which")) 
                            btnCancel.click();
                    
                }).addEventData("event.which");

                btnEdit.addClickListener(e -> {
                    lblValue.setVisible(true);
                    hlytEditField.setVisible(false);
                    this.getColumnByKey("advancedEditor").setVisible(true);
                    this.getColumnByKey("name").setFlexGrow(3);
                    if (editField.getValue() == null) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                                ts.getTranslatedString("module.general.messages.error-null-value"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                        return;
                    }

                    lblValue.setText(editField.getValue().toString());
                    if (!property.hasBinder())
                        property.setValue(editField.getValue());
                    
                    this.currentBtnCancelInEditProperty = null;
                    firePropertyValueChangedEvent(property);

                });

                btnCancel.addClickListener(ev -> {
                    if (!property.hasBinder())
                        editField.setValue(property.getValue());
                    
                    lblValue.setVisible(true);
                    hlytEditField.setVisible(false);
                    this.getColumnByKey("advancedEditor").setVisible(true);
                    this.getColumnByKey("name").setFlexGrow(3);
                });

                lblValue.getElement().addEventListener("dblclick", e -> {
                    if (this.currentBtnCancelInEditProperty != null) 
                        currentBtnCancelInEditProperty.click();
                    
                    this.currentBtnCancelInEditProperty = btnCancel;

                    boolean visibilityValue = lblValue.isVisible();
                    this.getColumnByKey("name").setFlexGrow(1);
                    lblValue.setVisible(!visibilityValue);
                    hlytEditField.setVisible(visibilityValue);

                    this.getColumnByKey("advancedEditor").setVisible(!visibilityValue);

                });

                lytValue.add(hlytEditField);
            }
       
            return lytValue;
        }).setHeader(String.format("%s (%s)", 
                ts.getTranslatedString("module.general.labels.value"),
                ts.getTranslatedString("module.propertysheet.labels.dbl-click-edit")))
                .setKey("value").setFlexGrow(5);
        
        addComponentColumn((property) -> {
            if (!property.isReadOnly() && !readOnly && property.supportsAdvancedEditor()) {
                 Button btnAdvancedEditor = new Button("...", ev -> {
                     AdvancedEditorDialog dialog = new AdvancedEditorDialog(property);
                     dialog.getAccept().addClickListener(clickEv -> {
                         dialog.loadNewValueIntoProperty();
                         firePropertyValueChangedEvent(dialog.getProperty());
                         dialog.close();
                     });
                     dialog.open();
                 });
                 btnAdvancedEditor.setClassName("compact-button");
                 return btnAdvancedEditor;
            }
            return new HorizontalLayout();
        }).setHeader("").setKey("advancedEditor").setWidth("50px");
    }
    
    public PropertySheet(TranslationService ts, List<AbstractProperty> properties) {
        this(ts, properties, "");     
    }
    
    public PropertySheet(TranslationService ts, List<AbstractProperty> properties, String title) {
        this(ts);
        setItems(properties); 
        this.title = title;
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
