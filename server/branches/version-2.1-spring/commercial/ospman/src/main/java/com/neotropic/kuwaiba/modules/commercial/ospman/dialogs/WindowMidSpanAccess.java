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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.persistence.PhysicalConnectionsService;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Window to manage mid-span access and splice of links
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowMidSpanAccess extends Dialog implements ActionCompletedListener {
    private ComboBox<BusinessObjectLight> cmbCable;
    private ComboBox<BusinessObjectLight> cmbDevice;
    private final BusinessObjectLight node;
    private final BusinessEntityManager bem;
    private final TranslationService ts;
    private final NewBusinessObjectVisualAction newBusinessObjectVisualAction;
    
    public WindowMidSpanAccess(BusinessObjectLight node, BusinessEntityManager bem, TranslationService ts, 
        NewBusinessObjectVisualAction newBusinessObjectVisualAction) {
        
        super();
        Objects.requireNonNull(node);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(bem);
        this.node = node;
        this.bem = bem;
        this.ts = ts;
        this.newBusinessObjectVisualAction = newBusinessObjectVisualAction;
        
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setWidth("90%");
        setHeight("90%");
    }

    @Override
    public void open() {
        try {
            H4 lblTitle = new H4(String.format("%s | %s", ts.getTranslatedString("module.ospman.mid-span-access.title"), node.getName()));
            
            cmbCable = new ComboBox(ts.getTranslatedString("module.ospman.mid-span-access.cable"));
            cmbCable.setWidth("250px");
            setCables();
            
            cmbDevice = new ComboBox(ts.getTranslatedString("module.ospman.mid-span-access.device"));
            cmbDevice.setWidth("250px");
            setDevices();
            Button btnNewDevice = new Button(new Icon(VaadinIcon.PLUS), event -> {
                newBusinessObjectVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter(NewBusinessObjectVisualAction.PARAM_BUSINESS_OBJECT, node))
                ).open();
            });
            btnNewDevice.setWidth("16px");
            
            Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), event -> close());
            
            HorizontalLayout lytRow0 = new HorizontalLayout(cmbCable);
            lytRow0.setWidthFull();
            lytRow0.setSpacing(false);
            lytRow0.setMargin(false);
            lytRow0.setPadding(false);
            
            HorizontalLayout lytRow1 = new HorizontalLayout(cmbDevice, btnNewDevice);
            lytRow0.setWidthFull();
            lytRow1.setSpacing(false);
            lytRow1.setMargin(false);
            lytRow1.setPadding(false);
            
            HorizontalLayout lytRow2 = new HorizontalLayout();
            lytRow2.setSizeFull();
            
            VerticalLayout lytSelector = new VerticalLayout(lytRow0, lytRow1, lytRow2, btnClose);
            lytSelector.setWidth("20%");
            lytSelector.setHeight("97%");
            lytSelector.setMargin(false);
            lytSelector.setPadding(false);
            lytSelector.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, btnClose);
            
            Div divSplice = new Div();
            divSplice.setWidthFull();
            divSplice.setHeight("97%");
            divSplice.getStyle().set("border-style", "solid"); //NOI18N
            divSplice.getStyle().set("border-width", "1px"); //NOI18N
            divSplice.getStyle().set("border-color", "var(--paper-grey-900)"); //NOI18N
            
            HorizontalLayout lytMain = new HorizontalLayout(lytSelector, divSplice);
            lytMain.setMargin(false);
            lytMain.setPadding(false);
            lytMain.setSizeFull();
            
            VerticalLayout lytWindow = new VerticalLayout(lblTitle, lytMain);
            lytWindow.setMargin(false);
            lytWindow.setPadding(false);
            lytWindow.setSizeFull();
            lytWindow.setHorizontalComponentAlignment(FlexComponent.Alignment.START, lytMain);
            
            add(lytWindow);
            this.newBusinessObjectVisualAction.registerActionCompletedLister(this);
            super.open();
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage()
            ).open();
        }
    }

    @Override
    public void close() {
        this.newBusinessObjectVisualAction.unregisterListener(this);
        super.close();
    }
    
    private void setCables() throws InventoryException {
        HashMap<String, List<BusinessObjectLight>> attrs = bem.getSpecialAttributes(node.getClassName(), node.getId(), 
            PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA, 
            PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB);
        List<BusinessObjectLight> cables = new ArrayList();
        attrs.values().forEach(value -> cables.addAll(value));
        cmbCable.setItems(cables);
    }
    
    private void setDevices() throws InventoryException {
        List<BusinessObjectLight> devices = bem.getChildrenOfClassLight(
            node.getId(), node.getClassName(), 
            Constants.CLASS_CONFIGURATIONITEM, -1);
        
        cmbDevice.setItems(devices);
    }

    @Override
    public void actionCompleted(ActionCompletedEvent event) {
        if (event.getStatus() == ActionCompletedEvent.STATUS_SUCCESS) {
            try {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), event.getMessage()).open();
                setDevices();
            } catch (InventoryException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage()).open();
            }
        }
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), event.getMessage()).open();
    }
}
