/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ospman.dialogs;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.persistence.PhysicalConnectionsService;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Dialog to create a new container connection
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DialogNewContainer extends Dialog {
    
    public DialogNewContainer(BusinessObjectLight source, BusinessObjectLight target, 
        TranslationService ts, ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, 
        PhysicalConnectionsService physicalConnectionService, Consumer<BusinessObjectLight> containerConsumer) {
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        try {
            FormLayout formLayout = new FormLayout();
            
            TextField txtName = new TextField();
            txtName.setRequired(true);
            
            ComboBox<ClassMetadataLight> cmbClass = new ComboBox(ts.getTranslatedString("module.ospman.labels.connection-class"));
            cmbClass.setRequired(true);
            cmbClass.setPlaceholder(ts.getTranslatedString("module.ospman.placeholder.select-container-class"));
            cmbClass.setItems(mem.getSubClassesLight(Constants.CLASS_GENERICPHYSICALCONTAINER, false, false));
            
            ComboBox<TemplateObjectLight> cmbTemplate = new ComboBox();
            cmbTemplate.setItemLabelGenerator(TemplateObjectLight::getName);
            cmbTemplate.setPlaceholder(ts.getTranslatedString("module.ospman.placeholder.select-container-template"));
            cmbClass.addValueChangeListener(event -> {
                try {
                    cmbTemplate.clear();
                    cmbTemplate.setItems(aem.getTemplatesForClass(event.getValue().getName()));
                } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage()
                    ).open();
                }
            });
            formLayout.addFormItem(txtName, ts.getTranslatedString("module.general.labels.name"));
            formLayout.addFormItem(cmbClass, ts.getTranslatedString("module.ospman.new-connection-class"));
            formLayout.addFormItem(cmbTemplate, ts.getTranslatedString("module.ospman.new-connection-template"));
            
            VerticalLayout content = new VerticalLayout();
            
            HorizontalLayout buttons = new HorizontalLayout();
            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), event -> close());
            Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"), event -> {
                try {
                    String containerName = txtName.getValue();
                    String containerClass = cmbClass.getValue() != null ? cmbClass.getValue().getName() : null;
                    String templateId = cmbTemplate.getValue() != null ? cmbTemplate.getValue().getId() : null;
                    
                    if (containerName != null && containerClass != null) {
                        Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                        String containerId = physicalConnectionService.createPhysicalConnection(
                                source.getClassName(), source.getId(),
                                target.getClassName(), target.getId(),
                                containerName, containerClass, templateId,
                                session.getUser().getUserName());
                        
                        containerConsumer.accept(bem.getObjectLight(containerClass, containerId));
                        close();
                    } else {
                        
                    }
                } catch (IllegalStateException | InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getMessage()
                    ).open();
                }
            });
            btnOk.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnOk.setClassName("primary-button");
            buttons.add(btnCancel, btnOk);
            
            content.add(formLayout, buttons);
            content.setHorizontalComponentAlignment(FlexComponent.Alignment.END, buttons);
            
            add(content);
            
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage()
            ).open();
        }
    }
}
