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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.persistence.PhysicalConnectionsService;
import org.neotropic.kuwaiba.visualization.views.FiberSplitterView;
import org.neotropic.kuwaiba.visualization.views.PhysicalTreeView;
import org.neotropic.kuwaiba.visualization.views.SpliceBoxView;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Dialog to the node tool set
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowNode extends Dialog {
    public WindowNode(AbstractViewNode<BusinessObjectLight> node, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, 
        TranslationService ts, PhysicalConnectionsService physicalConnectionsService, 
        NewBusinessObjectVisualAction newBusinessObjectVisualAction) {
        
        HorizontalLayout lytNodeTools = new HorizontalLayout();
        lytNodeTools.add(new Button(new Icon(VaadinIcon.ROAD_BRANCH), event -> {
            close();
            new WindowMidSpanAccess(node.getIdentifier(), aem, bem, mem, ts, newBusinessObjectVisualAction).open();
        }));
        lytNodeTools.add(new Button(ts.getTranslatedString("module.ospman.view-node.tool.connect"), new Icon(VaadinIcon.PLUG), 
            event -> {
                close();
                WindowPhysicalConnections dialog = new WindowPhysicalConnections(node.getIdentifier(), ts, bem);
                dialog.open();
            }
        ));
        lytNodeTools.add(new Button(ts.getTranslatedString("module.ospman.view-node.tool.view-content"), new Icon(VaadinIcon.EYE),
            event -> {
                close();
                new ViewDialog(node.getIdentifier(), aem, bem, mem, ts, physicalConnectionsService).open();
            }
        ));
        lytNodeTools.add(new Button(ts.getTranslatedString("module.ospman.view-node.tool.remove"), new Icon(VaadinIcon.TRASH), 
            event -> {
                close();
            }
        ));
        add(lytNodeTools);
    }
    
    private class ViewDialog extends Dialog {
        public ViewDialog(BusinessObjectLight businessObject, 
            ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem,
            TranslationService ts, PhysicalConnectionsService physicalConnectionsService) {
            
            try {
                Grid<BusinessObjectLight> tblContents = new Grid<>();
                tblContents.setItems(bem.getObjectChildren(
                    businessObject.getClassName(), businessObject.getId(), -1));
                tblContents.addColumn(BusinessObjectLight::getName).setHeader(
                    ts.getTranslatedString("module.ospman.containers.name"));
                tblContents.addColumn(BusinessObjectLight::getClassName).setHeader(
                    ts.getTranslatedString("module.ospman.containers.type"));
                tblContents.addItemClickListener((ev) -> {
                    switch (ev.getItem().getClassName()) {
                            case "SpliceBox": //NOI18N
                                close();
                                Dialog wdwSpliceBoxDetailedView = new Dialog();
                                SpliceBoxView viewSpliceBox = new SpliceBoxView(ev.getItem(), bem, aem, mem, ts);
                                try {
                                    wdwSpliceBoxDetailedView.add(viewSpliceBox.getAsComponent());
                                } catch(InvalidArgumentException ex) {
                                    wdwSpliceBoxDetailedView.add(new Label(ex.getLocalizedMessage()));
                                }
                                wdwSpliceBoxDetailedView.open();
                                break;
                            case "FiberSplitter": //NOI18N
                                close();
                                Dialog wdwSplitterDetailedView = new Dialog();
                                FiberSplitterView viewSplitter = new FiberSplitterView(ev.getItem(), bem, aem, mem, ts);
                                try {
                                    wdwSplitterDetailedView.add(viewSplitter.getAsComponent());
                                } catch(InvalidArgumentException ex) {
                                    wdwSplitterDetailedView.add(new Label(ex.getLocalizedMessage()));
                                }
                                wdwSplitterDetailedView.open();
                                break;
                            case "OpticalPort": //NOI18N
                                close();
                                Dialog wdwPhysicalView = new Dialog();
                                //PhysicalPathView physicalPathView = new PhysicalPathView(businessObject, bem, aem, mem, ts, physicalConnectionsService);
                                PhysicalTreeView physicalTreeView = new PhysicalTreeView(businessObject, bem, aem, mem, ts, physicalConnectionsService);
                                try {
                                    wdwPhysicalView.add(physicalTreeView.getAsComponent());
                                } catch (InvalidArgumentException ex) {
                                    wdwPhysicalView.add(new Label(ex.getLocalizedMessage()));
                                }
                                wdwPhysicalView.open();
                                break;
                            default:
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.information"), 
                                        ts.getTranslatedString("module.ospman.messages.no-detailed-view")).open();
                        }
                });
                
                VerticalLayout lytContent = new VerticalLayout(tblContents);
                lytContent.addClassName("widgets-layout-dialog-list");
                add(lytContent);
            } catch(InventoryException ex) {
                add(new Label(ex.getLocalizedMessage()));
            } catch(Exception ex) {
                add(new Label(ts.getTranslatedString("module.general.messages.unexpected-error")));
                Logger.getLogger(WindowNode.class.toString()).log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
    }
}
