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
package org.neotropic.kuwaiba.visualization.widgets;

import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.awt.Color;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.visualization.api.wizard.NewPhysicalConnectionWizard;
import org.neotropic.kuwaiba.visualization.views.ObjectView;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.views.util.UtilHtml;
import org.neotropic.util.visual.widgets.AbstractDashboardWidget;
import org.neotropic.util.visual.wizard.Wizard;

/**
 * Shows the object view of the given business Object
 *
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class ObjectViewWidget extends AbstractDashboardWidget {

    /**
     * The main business object
     */
    private BusinessObjectLight businessObject;
    /**
     * Utility class that help to load resources like icons and images
     */
    private ResourceFactory resourceFactory;
    /**
     * Reference to the main object view
     */
    private ObjectView objectView;
    /**
     * Connection service
     */
    private PhysicalConnectionsService physicalConnectionsService;

    public ObjectViewWidget(BusinessObjectLight businessObject, MetadataEntityManager mem, ApplicationEntityManager aem,
            BusinessEntityManager bem, TranslationService ts, ResourceFactory resourceFactory, PhysicalConnectionsService physicalConnectionsService) {
        super(mem, aem, bem, ts);
        this.resourceFactory = resourceFactory;
        this.businessObject = businessObject;
        this.physicalConnectionsService = physicalConnectionsService;
        setTitle(ts.getTranslatedString("Object View"));
        createCover();
        coverComponent.addClassName("widgets-colors-magenta");
    }

    @Override
    public void createContent() {
        try {

            Button btnSaveView = new Button(ts.getTranslatedString("module.general.messages.save"), new Icon(VaadinIcon.ARROW_CIRCLE_DOWN_O), ev -> {
                saveView();
            });
            Button btnConnect = new Button(ts.getTranslatedString("module.visualization.object-view-connect"), new Icon(VaadinIcon.CONNECT), (selectedItem) -> {
                Dialog dlgSelectRootObjects = new Dialog();
                ComboBox<AbstractViewNode> cmbASideRoot = new ComboBox<>("A Side", objectView.getAsViewMap().getNodes());
                cmbASideRoot.setAllowCustomValue(false);
                cmbASideRoot.setLabel(ts.getTranslatedString("module.visualization.object-view-select-a-side"));
                cmbASideRoot.setWidth("250px");
                ComboBox<AbstractViewNode> cmbBSideRoot = new ComboBox<>("B Side", objectView.getAsViewMap().getNodes());
                cmbBSideRoot.setAllowCustomValue(false);
                cmbBSideRoot.setLabel(ts.getTranslatedString("module.visualization.object-view-select-b-side"));
                cmbBSideRoot.setWidth("250px");
                Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"));

                dlgSelectRootObjects.setWidth("70%");
                dlgSelectRootObjects.setHeight("40%");
                dlgSelectRootObjects.setModal(true);

                btnOk.addClickListener((event) -> {

                    if (cmbASideRoot.getValue() == null || cmbBSideRoot.getValue() == null) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),ts.getTranslatedString("module.visualization.object-view-select-both-sides")).open();
                        return;
                    }

                    if (cmbASideRoot.getValue().equals(cmbBSideRoot.getValue())) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),ts.getTranslatedString("module.visualization.object-view-selected-nodes-different")).open();
                        return;
                    }

                    dlgSelectRootObjects.close();

                    NewPhysicalConnectionWizard wizard = new NewPhysicalConnectionWizard((BusinessObjectLight) cmbASideRoot.getValue().getIdentifier(),
                            (BusinessObjectLight) cmbBSideRoot.getValue().getIdentifier(), bem, aem, mem, physicalConnectionsService, resourceFactory, ts);

                    wizard.setSizeFull();

                    Dialog dlgWizard = new Dialog();
                    dlgWizard.setModal(true);
                    dlgWizard.setWidth("80%");
                    dlgWizard.setHeight("80%");
                    dlgWizard.add(wizard);
                    wizard.addEventListener((wizardEvent) -> {
                        switch (wizardEvent.getType()) {
                            case Wizard.WizardEvent.TYPE_FINAL_STEP:
                                BusinessObjectLight newConnection = (BusinessObjectLight) wizardEvent.getInformation().get("connection");
                                BusinessObjectLight aSide = (BusinessObjectLight) wizardEvent.getInformation().get("rootASide");
                                BusinessObjectLight bSide = (BusinessObjectLight) wizardEvent.getInformation().get("rootBSide");
                                MxGraphEdge edge = new MxGraphEdge();
                                edge.setSource(aSide.getId());
                                edge.setTarget(bSide.getId());
                                edge.setLabel(newConnection.getName());
                                try {
                                    ClassMetadata theClass = mem.getClass(newConnection.getClassName());
                                    edge.setStrokeColor(UtilHtml.toHexString(new Color(theClass.getColor())));
                                } catch (MetadataObjectNotFoundException ex) {
                                    //In case of error, use a default black line
                                }
                                objectView.getMxGraph().addEdge(newConnection, aSide, bSide, edge);
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), String.format(ts.getTranslatedString("module.visualization.object-view-connection-created"), newConnection.getName())).open();
                            case Wizard.WizardEvent.TYPE_CANCEL:
                                dlgWizard.close();
                        }
                    });
                    dlgWizard.open();
                });

                VerticalLayout lytContent = new VerticalLayout(cmbASideRoot, cmbBSideRoot, btnOk);   
                lytContent.setSpacing(true);
                lytContent.setSizeFull();

                dlgSelectRootObjects.add(lytContent);
                dlgSelectRootObjects.open();
            });
            Button btnZoomIn = new Button(ts.getTranslatedString("module.visualization.rack-view-zoom-in"), new Icon(VaadinIcon.PLUS), evt -> {
                    objectView.getMxGraph().getMxGraph().zoomIn();
                });
                
            Button btnZoomOut = new Button(ts.getTranslatedString("module.visualization.rack-view-zoom-out"), new Icon(VaadinIcon.MINUS), evt -> {
                    objectView.getMxGraph().getMxGraph().zoomOut();
                });
            HorizontalLayout lytTools = new HorizontalLayout(btnSaveView, btnConnect, btnZoomIn, btnZoomOut);
            objectView = new ObjectView(businessObject, bem, aem, mem, ts, resourceFactory);

            Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), evt -> {
                getWdwContent().close();
            });
            VerticalLayout lytContent = new VerticalLayout(lytTools, objectView.getAsComponent(), btnClose);
            contentComponent = lytContent;

        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage()).open();
        }
    }

    public void saveView() {

        if (businessObject != null) {
            try {
                byte[] viewStructure = objectView.getAsXml();
                List<ViewObjectLight> objectViews = aem.getObjectRelatedViews(businessObject.getId(), businessObject.getClassName(), -1);

                if (objectViews.isEmpty()) {

                    long viewId = aem.createObjectRelatedView(businessObject.getId(), businessObject.getClassName(), null, null, "ObjectView", viewStructure, null); //NOI18N
                    if (viewId != -1) { //Success
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.visualization.object-view-view-saved")).open();
                    } else {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error")).open();
                    }
                } else {
                    ViewObject viewObject = aem.getObjectRelatedView(businessObject.getId(),
                            businessObject.getClassName(), objectViews.get(0).getId());
                    aem.updateObjectRelatedView(businessObject.getId(), businessObject.getClassName(),
                            viewObject.getId(), null, null, viewStructure, null);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.visualization.object-view-view-saved")).open();
                }
            } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                Logger.getLogger(RackViewWidget.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
