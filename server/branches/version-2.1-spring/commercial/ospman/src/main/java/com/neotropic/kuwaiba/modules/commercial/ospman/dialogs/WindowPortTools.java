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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.visualization.views.PhysicalPathView;
import org.neotropic.kuwaiba.visualization.views.PhysicalTreeView;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Set of tools to manage ports
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowPortTools extends Dialog {
    private final String SPECIAL_ATTR_ENDPOINT_A = "endpointA";
    private final String SPECIAL_ATTR_ENDPOINT_B = "endpointB";
    
    public WindowPortTools(BusinessObjectLight port, 
        ApplicationEntityManager aem,
        BusinessEntityManager bem,
        MetadataEntityManager mem,
        TranslationService ts,
            PhysicalConnectionsService physicalConnectionsService,
        BiConsumer<List<BusinessObjectLight>, String> consumerReleaseFiber
        ) {
        super();
        Label lblPortTools = new Label(ts.getTranslatedString("module.ospman.port-tools.title"));
        Button btnReleasePort = new Button(ts.getTranslatedString("module.ospman.port-tools.tool.release-port"), event -> {
            if (consumerReleaseFiber != null) {
                try {
                    HashMap<String, List<BusinessObjectLight>> endpoints = bem.getSpecialAttributes(
                        port.getClassName(), port.getId(), SPECIAL_ATTR_ENDPOINT_A, SPECIAL_ATTR_ENDPOINT_B);
                    List<BusinessObjectLight> endpointsA = endpoints.get(SPECIAL_ATTR_ENDPOINT_A);
                    List<BusinessObjectLight> endpointsB = endpoints.get(SPECIAL_ATTR_ENDPOINT_B);
                    if (endpointsA != null && !endpointsA.isEmpty())
                        consumerReleaseFiber.accept(Arrays.asList(port, endpointsB.get(0)), SPECIAL_ATTR_ENDPOINT_A);
                    else if (endpointsB != null && !endpointsB.isEmpty())
                        consumerReleaseFiber.accept(Arrays.asList(port, endpointsB.get(0)), SPECIAL_ATTR_ENDPOINT_B);
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage()
                    ).open();
                }
            }
            close();
        });
        
        Button btnShowPhysicalPath = new Button(ts.getTranslatedString("module.ospman.port-tools.tool.show-physical-path"), event -> {
            try {
                Label lblTitle = new Label(ts.getTranslatedString("module.ospman.port-tools.physical-path.title"));
                PhysicalPathView physicalView = new PhysicalPathView(port, bem, aem, mem, ts, physicalConnectionsService);
                Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"));
                VerticalLayout lyt = new VerticalLayout(lblTitle, physicalView.getAsComponent(), btnClose);
                lyt.setHorizontalComponentAlignment(Alignment.CENTER, btnClose);
                
                Dialog wdwPhysicalPath = new Dialog(lyt);
                
                btnClose.addClickListener(btnCloseEvent -> wdwPhysicalPath.close());
                
                wdwPhysicalPath.open();
                close();
            } catch (InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage()).open();
            }
        });
        
        Button btnShowPhysicalTree = new Button(ts.getTranslatedString("module.ospman.port-tools.tool.show-physical-tree"), event -> {
            try {
                Label lblTitle = new Label(ts.getTranslatedString("module.ospman.port-tools.physical-tree.title"));
                PhysicalTreeView physicalTreeView = new PhysicalTreeView(port, bem, aem, mem, ts, physicalConnectionsService);
                Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"));
                VerticalLayout lyt = new VerticalLayout(lblTitle, physicalTreeView.getAsComponent(), btnClose);
                lyt.setHorizontalComponentAlignment(Alignment.CENTER, btnClose);
                
                Dialog wdwPhysicalTree = new Dialog(lyt);
                
                btnClose.addClickListener(btnCloseEvent -> wdwPhysicalTree.close());
                
                wdwPhysicalTree.open();
                close();
            } catch (InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage()).open();
            }
        });
        
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), event -> close());
        
        VerticalLayout lyt = new VerticalLayout(
            lblPortTools, btnReleasePort, btnShowPhysicalPath, btnShowPhysicalTree, btnCancel
        );
        lyt.setSpacing(false);
        lyt.setMargin(false);
        lyt.setPadding(false);
        lyt.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, btnCancel);
        
        add(lyt);
    }
}
