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
package com.neotropic.kuwaiba.modules.commercial.ospman;

import com.neotropic.kuwaiba.modules.commercial.ospman.AbstractMapProvider.OSPNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.PhysicalConnectionsDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.commands.OneArgCommand;
import org.neotropic.kuwaiba.visualization.views.FiberSplitterView;
import org.neotropic.kuwaiba.visualization.views.SpliceBoxView;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Component with a set of tools to markers: like delete
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class NodeInfoWindowContent extends VerticalLayout {
    private OneArgCommand<OSPNode> cmdDeleteNode;
    
    public NodeInfoWindowContent(OSPNode ospNode, OspInfoWindowContainer container, 
            TranslationService ts, MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem) {
        HorizontalLayout hlyTools = new HorizontalLayout();
        
        hlyTools.add(new Button("Connect", new Icon(VaadinIcon.PLUG), event -> {
            container.closeInfoWindow();
            PhysicalConnectionsDialog dialog = new PhysicalConnectionsDialog(ospNode.getBusinessObject(), ts, bem);
            dialog.open();
        }));
        hlyTools.add(new Button("Remove", new Icon(VaadinIcon.TRASH), event -> {
            container.closeInfoWindow();
            if (cmdDeleteNode != null)
                cmdDeleteNode.execute(ospNode);
        }));
        hlyTools.add(new Button("View Contents", new Icon(VaadinIcon.EYE), event -> {
            container.closeInfoWindow();
            Dialog wdwContents = new Dialog();
            try {
                Grid<BusinessObjectLight> tblContents = new Grid<>();
                tblContents.setItems(bem.getObjectChildren(ospNode.getBusinessObject().getClassName(), 
                        ospNode.getBusinessObject().getId(), -1));
                tblContents.addColumn(BusinessObjectLight::getName).setHeader("Name");
                tblContents.addColumn(BusinessObjectLight::getClassName).setHeader("Type");
                tblContents.addItemClickListener((ev) -> {
                    switch (ev.getItem().getClassName()) {
                            case "SpliceBox":
                                wdwContents.close();
                                Dialog wdwSpliceBoxDetailedView = new Dialog();
                                SpliceBoxView viewSpliceBox = new SpliceBoxView(ev.getItem(), bem, aem, mem);
                                try {
                                    wdwSpliceBoxDetailedView.add(viewSpliceBox.getAsComponent());
                                } catch(InvalidArgumentException ex) {
                                    wdwSpliceBoxDetailedView.add(new Label(ex.getLocalizedMessage()));
                                }
                                wdwSpliceBoxDetailedView.open();
                                break;
                            case "FiberSplitter":
                                wdwContents.close();
                                Dialog wdwSplitterDetailedView = new Dialog();
                                FiberSplitterView viewSplitter = new FiberSplitterView(ev.getItem(), bem, aem, mem);
                                try {
                                    wdwSplitterDetailedView.add(viewSplitter.getAsComponent());
                                } catch(InvalidArgumentException ex) {
                                    wdwSplitterDetailedView.add(new Label(ex.getLocalizedMessage()));
                                }
                                wdwSplitterDetailedView.open();
                                break;
                            default:
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.information"), 
                                        "The selected object does not have any detailed view").open();
                        }
                });
                
                VerticalLayout lytContent = new VerticalLayout(tblContents);
                lytContent.addClassName("widgets-layout-dialog-list");
                wdwContents.add(lytContent);
            } catch(InventoryException ex) {
                wdwContents.add(new Label(ex.getLocalizedMessage()));
            } catch(Exception ex) {
                wdwContents.add(new Label(ts.getTranslatedString("module.general.messages.unexpected-error")));
                Logger.getLogger(NodeInfoWindowContent.class.toString()).log(Level.SEVERE, ex.getLocalizedMessage());
            }
            wdwContents.open();
        }));
        
        setSizeFull();
        setMargin(false);
        setPadding(false);
        
        add(hlyTools);
        setHorizontalComponentAlignment(Alignment.CENTER, hlyTools);
    }
    
    public void setDeleteNodeCommand(OneArgCommand<OSPNode> cmdDeleteNode) {
        this.cmdDeleteNode = cmdDeleteNode;
    }
}
