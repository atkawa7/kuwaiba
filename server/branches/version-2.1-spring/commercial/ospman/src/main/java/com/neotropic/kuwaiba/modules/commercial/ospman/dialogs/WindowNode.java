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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.server.Command;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.visualization.views.FiberSplitterView;
import org.neotropic.kuwaiba.visualization.views.PhysicalPathView;
import org.neotropic.kuwaiba.visualization.views.PhysicalTreeView;
import org.neotropic.kuwaiba.visualization.views.SpliceBoxView;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Dialog to the node tool set
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowNode extends Dialog {
    public WindowNode(AbstractViewNode<BusinessObjectLight> node, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, 
        TranslationService ts, PhysicalConnectionsService physicalConnectionsService, 
        NewBusinessObjectVisualAction newBusinessObjectVisualAction, Command cmdDeleteNode) {
        
        
        HorizontalLayout lytNodeTools = new HorizontalLayout();
        lytNodeTools.add(new Button(ts.getTranslatedString("module.ospman.mid-span-access.title"), new Icon(VaadinIcon.ROAD_BRANCH), event -> {
            close();
            new WindowMidSpanAccess(node.getIdentifier(), aem, bem, mem, ts, newBusinessObjectVisualAction, physicalConnectionsService).open();
        }));
        lytNodeTools.add(new Button(ts.getTranslatedString("module.ospman.view-node.tool.view-content"), new Icon(VaadinIcon.EYE),
            event -> {
                close();
                new ViewsWindow(node.getIdentifier(), aem, bem, mem, ts, physicalConnectionsService).open();
            }
        ));
        lytNodeTools.add(new Button(ts.getTranslatedString("module.ospman.view-node.tool.remove"), new Icon(VaadinIcon.TRASH), 
            event -> {
                if (cmdDeleteNode != null)
                    cmdDeleteNode.execute();
                close();
            }
        ));
        add(lytNodeTools);
    }
    
    private class ViewsWindow extends Dialog {
        
        public ViewsWindow(BusinessObjectLight businessObject, 
            ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem,
            TranslationService ts, PhysicalConnectionsService physicalConnectionsService) {
            super();
            setMinWidth("70%");
            try {
                TreeGrid<BusinessObjectLight> tblContents = new TreeGrid<>();
                tblContents.setItems(
                    bem.getObjectChildren(businessObject.getClassName(), businessObject.getId(), -1),
                    item -> {
                        try {
                            return bem.getChildrenOfClassLightRecursive(
                                item.getId(), item.getClassName(), 
                                Constants.CLASS_GENERICPHYSICALPORT, -1
                            );
                        } catch (InventoryException ex) {
                            return Collections.EMPTY_LIST;
                        }
                    }
                );
                tblContents.addHierarchyColumn(BusinessObjectLight::getName).setHeader(
                    ts.getTranslatedString("module.ospman.containers.name"));
                tblContents.addColumn(BusinessObjectLight::getClassName).setHeader(
                    ts.getTranslatedString("module.ospman.containers.type"));
                tblContents.addComponentColumn(item -> {
                    try {
                        if (mem.isSubclassOf("SpliceBox", item.getClassName())) { //NOI18N
                            return new Button(ts.getTranslatedString("module.ospman.views.splice-box"), event -> {
                                try {
                                    SpliceBoxView spliceBoxView = new SpliceBoxView(item, bem, aem, mem, ts);
                                    new ViewWindow(
                                        ts.getTranslatedString("module.ospman.views.splice-box"), 
                                        spliceBoxView.getAsComponent(), 
                                        ts
                                    ).open();
                                } catch (InvalidArgumentException ex) {
                                    new SimpleNotification(
                                            ts.getTranslatedString("module.general.messages.error"),
                                            ex.getLocalizedMessage(), 
                                            AbstractNotification.NotificationType.ERROR, ts
                                    ).open();
                                }
                            });
                        } else if (mem.isSubclassOf("FiberSplitter", item.getClassName())) { //NOI18N
                            return new Button(ts.getTranslatedString("module.ospman.views.splitter"), event -> {
                                try {
                                    FiberSplitterView fiberSplitterView = new FiberSplitterView(item, bem, aem, mem, ts);
                                    new ViewWindow(
                                        ts.getTranslatedString("module.ospman.views.splitter"), 
                                        fiberSplitterView.getAsComponent(), 
                                        ts
                                    ).open();
                                } catch (InvalidArgumentException ex) {
                                    new SimpleNotification(
                                            ts.getTranslatedString("module.general.messages.error"),
                                            ex.getLocalizedMessage(), 
                                            AbstractNotification.NotificationType.ERROR, ts
                                    ).open();
                                }
                            });
                        } else if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, item.getClassName())) {
                            return new HorizontalLayout(
                                    new Button(ts.getTranslatedString("module.ospman.views.physical-path"), event -> {
                                        try {
                                            PhysicalPathView physicalPathView = new PhysicalPathView(item, bem, aem, mem, ts, physicalConnectionsService);
                                            new ViewWindow(
                                                ts.getTranslatedString("module.ospman.views.physical-path"), 
                                                physicalPathView.getAsComponent(), 
                                                ts
                                            ).open();
                                        } catch (InvalidArgumentException ex) {
                                            new SimpleNotification(
                                                    ts.getTranslatedString("module.general.messages.error"),
                                                    ex.getLocalizedMessage(), 
                                                    AbstractNotification.NotificationType.ERROR, ts
                                            ).open();
                                        }
                                    }),
                                    new Button(ts.getTranslatedString("module.ospman.views.physical-tree"), event -> {
                                        try {
                                            PhysicalTreeView physicalTreeView = new PhysicalTreeView(item, bem, aem, mem, ts, physicalConnectionsService);
                                            new ViewWindow(
                                                ts.getTranslatedString("module.ospman.views.physical-tree"), 
                                                physicalTreeView.getAsComponent(), 
                                                ts
                                            ).open();
                                        } catch (InvalidArgumentException ex) {
                                            new SimpleNotification(
                                                    ts.getTranslatedString("module.general.messages.error"),
                                                    ex.getLocalizedMessage(), 
                                                    AbstractNotification.NotificationType.ERROR, ts
                                            ).open();
                                        }
                                    })
                            );
                        }
                    } catch (MetadataObjectNotFoundException ex) {
                        new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"),
                                ex.getLocalizedMessage(), 
                                AbstractNotification.NotificationType.ERROR, ts
                        ).open();
                    }
                    return new Div();
                }).setHeader(ts.getTranslatedString("module.ospman.views.views"));
                
                Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), event -> this.close());
                VerticalLayout lytContent = new VerticalLayout(tblContents, btnClose);
                lytContent.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, btnClose);
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
    
    private class ViewWindow extends Dialog {
        public ViewWindow(String title, Component viewComponent, TranslationService ts) {
            super();
            setMinWidth("80%");
            //setMinHeight("80%");
            Label lblTitle = new Label(title);
            Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), event -> this.close());
            
            VerticalLayout lytComponent = new VerticalLayout(viewComponent);
            lytComponent.setSizeFull();
            lytComponent.setSpacing(false);
            lytComponent.setPadding(false);
            lytComponent.setMargin(false);
            
            VerticalLayout lytView = new VerticalLayout(lblTitle, viewComponent, btnClose);
            lytView.setSizeFull();
            lytView.setSpacing(false);
            lytView.setPadding(false);
            lytView.setMargin(false);
            lytView.setHorizontalComponentAlignment(
                FlexComponent.Alignment.CENTER, lblTitle, lytComponent, btnClose
            );
            add(lytView);
        }
    }
}
