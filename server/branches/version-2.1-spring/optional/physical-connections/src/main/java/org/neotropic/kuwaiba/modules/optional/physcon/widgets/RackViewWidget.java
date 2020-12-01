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
package org.neotropic.kuwaiba.modules.optional.physcon.widgets;

import com.neotropic.flow.component.mxgraph.MxGraph;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.modules.optional.physcon.views.RackView;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.widgets.AbstractDashboardWidget;

/**
 * Shows the object view of the given business Object
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class RackViewWidget extends AbstractDashboardWidget implements PropertySheet.IPropertyValueChangedListener {
    /**
     * The main business object
     */
    private BusinessObjectLight businessObject;
    /**
     * Utility class that help to load resources like icons and images
     */
    private ResourceFactory resourceFactory;

    private RackView rackView;
    
    private RackView detailedRackView;
    
    private MxGraph currentMxGraphView;
    
    private PhysicalConnectionsService physicalConnectionsService;
    
    private boolean detailedViewRendered;;

    public RackViewWidget(BusinessObjectLight businessObject, MetadataEntityManager mem, ApplicationEntityManager aem,
            BusinessEntityManager bem, TranslationService ts, ResourceFactory resourceFactory, PhysicalConnectionsService physicalConnectionsService) {
        super(mem, aem, bem, ts);
        this.resourceFactory = resourceFactory;
        this.businessObject = businessObject;
        this.physicalConnectionsService = physicalConnectionsService;
        setTitle(ts.getTranslatedString(ts.getTranslatedString("module.visualization.rack-view-name")));
        createCover();
        coverComponent.addClassName("widgets-colors-magenta");
    }

    @Override
    public void createContent() {
        try {
            rackView = new RackView(businessObject, false, bem, aem, mem, ts, physicalConnectionsService);
            detailedRackView = new RackView(businessObject, true, bem, aem, mem, ts, physicalConnectionsService);
            detailedViewRendered = false;
            HorizontalLayout lytRackView = new HorizontalLayout(rackView.getAsComponent());
            currentMxGraphView = rackView.getMxGraph();
            lytRackView.setMargin(false);
            lytRackView.setPadding(false);
            lytRackView.setSpacing(false);
            HorizontalLayout lytDetailedRackView = new HorizontalLayout();
            lytDetailedRackView.setMargin(false);
            lytDetailedRackView.setPadding(false);
            lytDetailedRackView.setSpacing(false);
            lytDetailedRackView.setVisible(false);
            
            Button btnShowRackView = new Button(ts.getTranslatedString("module.visualization.object-view-show-simple-rack-view"), new Icon(VaadinIcon.CLIPBOARD_CHECK), ev -> {
                lytRackView.setVisible(true);
                lytDetailedRackView.setVisible(false);
                currentMxGraphView = rackView.getMxGraph();
            });
            Button btnShowDetailedRackView = new Button(ts.getTranslatedString("module.visualization.object-view-show-detailed-rack-view"), new Icon(VaadinIcon.SERVER), (selectedItem) -> {
                try {
                    if (!detailedViewRendered) {                           
                        lytDetailedRackView.add(detailedRackView.getAsComponent());          
                        detailedViewRendered = true;
                    } 
                    currentMxGraphView = detailedRackView.getMxGraph();
                    lytRackView.setVisible(false);
                    lytDetailedRackView.setVisible(true);
                }
                catch (InvalidArgumentException ex) {
                        Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }); 
            
            BusinessObject rack = bem.getObject(businessObject.getClassName(), businessObject.getId());
            Button btnConnections = new Button(ts.getTranslatedString("module.visualization.rack-view-connections-rack"), new Icon(VaadinIcon.CONNECT_O), evt -> {
                    try {
                        openExploreConnectionsDlg(rack);
                    } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                        Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                
            Button btnZoomIn = new Button(ts.getTranslatedString("module.visualization.rack-view-zoom-in"), new Icon(VaadinIcon.PLUS), evt -> {
                if (currentMxGraphView != null)
                    currentMxGraphView.zoomIn();
            });
            Button btnZoomOut = new Button(ts.getTranslatedString("module.visualization.rack-view-zoom-out"), new Icon(VaadinIcon.MINUS), evt -> {
                if (currentMxGraphView != null)
                    currentMxGraphView.zoomOut();
            });   
            Button btnEditRack = new Button(ts.getTranslatedString("module.visualization.rack-view-edit-rack"), new Icon(VaadinIcon.EDIT), evt -> {
       
                try {
                    PropertySheet propertySheet = new PropertySheet(ts);
                    BusinessObject theRack = bem.getObject(businessObject.getClassName(), businessObject.getId());
                    propertySheet.setItems(PropertyFactory.propertiesFromBusinessObject(theRack, ts, aem, mem));             
                    propertySheet.addPropertyValueChangedListener(this);
                    
                    Dialog dlgPropSheet = new Dialog();
                    dlgPropSheet.setWidth("700px");
                    Button btnClosePropSheet = new Button(ts.getTranslatedString("module.general.messages.close"), evtPropSheet -> {
                        dlgPropSheet.close();
                     });
                    VerticalLayout lytPropSheet = new VerticalLayout(propertySheet, btnClosePropSheet);
                    lytPropSheet.setWidthFull();
                    dlgPropSheet.add(lytPropSheet);
                    dlgPropSheet.open();
                    
                } catch (InventoryException ex) {
                    Logger.getLogger(RackViewWidget.class.getName()).log(Level.SEVERE, null, ex);
                }
            }); 
            // Build info
            BoldLabel lblTitleName = new BoldLabel(ts.getTranslatedString("module.visualization.rack-view-name"));
            Label lblName = new Label(rack.getName());
            BoldLabel lblTitleSerial = new BoldLabel(ts.getTranslatedString("module.visualization.rack-view-serial"));
            Label lblSerial = new Label(rack.getAttributes().containsKey("serialNumber") ? (String) rack.getAttributes().get("serialNumber") : "Not Set");
            BoldLabel lblTitleVendor = new BoldLabel(ts.getTranslatedString("module.visualization.rack-view-vendor"));
            Label lblVendor = new Label(rack.getAttributes().containsKey("vendor") ? (String) rack.getAttributes().get("vendor") : "Not Set");
            BoldLabel lblTitleOrdering = new BoldLabel(ts.getTranslatedString("module.visualization.rack-view-ordering"));
            Label lblOrdering = new Label(rackView.isOrderDescending() ? ts.getTranslatedString("module.visualization.rack-view-descending") 
                                                     : ts.getTranslatedString("module.visualization.rack-view-ascending"));
            VerticalLayout lytInfo = new VerticalLayout(lblTitleName, lblName, 
                     lblTitleSerial, lblSerial, lblTitleVendor, lblVendor, lblTitleOrdering, lblOrdering);
            lytInfo.setPadding(false);
            lytInfo.setMargin(false);

            lytInfo.setSpacing(false);
            lytInfo.setMargin(false);
            lytInfo.setWidth("220px");
            
            Image imgMoveUp = new Image("images/arrow_up.png", "");
            Image imgMoveDown = new Image("images/arrow_down.png", "");
            Image imgMove = new Image("images/move-unit.png", "");
            Image imgShowPorts = new Image("images/view_port.png", "");
            Image imgShowSlotContent = new Image("images/show-slot.png", "");
            Label moveUpInfo = new Label(ts.getTranslatedString("module.visualization.rack-view-move-up-info"));
            Label moveDownInfo = new Label(ts.getTranslatedString("module.visualization.rack-view-move-down-info"));
            Label moveInfo = new Label(ts.getTranslatedString("module.visualization.rack-view-move-to-position"));
            Label showPortInfo = new Label(ts.getTranslatedString("module.visualization.rack-view-show-ports"));
            Label SlotContentInfo = new Label(ts.getTranslatedString("module.visualization.rack-view-slot-info"));
            
            VerticalLayout lytIconsInfo = new VerticalLayout(new HorizontalLayout(imgMoveUp, moveUpInfo),
                        new HorizontalLayout(imgMoveDown, moveDownInfo),
                        new HorizontalLayout(imgMove, moveInfo), 
                        new HorizontalLayout(imgShowPorts, showPortInfo), 
                        new HorizontalLayout(imgShowSlotContent, SlotContentInfo));
            lytIconsInfo.setPadding(false);
            lytIconsInfo.setMargin(false);
            lytIconsInfo.setClassName("lytInfoIcons");
            VerticalLayout lytTools = new VerticalLayout(btnShowRackView, btnShowDetailedRackView, 
                    btnConnections, btnZoomIn, btnZoomOut, btnEditRack, lytIconsInfo);
            lytTools.setWidth("220px");
            lytTools.setAlignItems(FlexComponent.Alignment.START);
            lytTools.setMargin(false);
            lytTools.setSpacing(false);
           
            HorizontalLayout lytViews = new HorizontalLayout(lytTools, lytRackView, lytDetailedRackView);
            lytViews.setSizeFull();
            lytViews.setMargin(false);
            BoldLabel lblTitle = new BoldLabel(String.format("%s : %s", ts.getTranslatedString("module.visualization.rack-view-name"), rack.getName()));
            VerticalLayout lytContent = new VerticalLayout(lblTitle, lytViews);
            lytContent.setSpacing(false);
            lytContent.setMargin(false);
            contentComponent = lytContent;

        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * Builds a dialog with the connections in the given rack object
     * @param rack The rack to which the connections are to be displayed
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException
     * @throws InvalidArgumentException 
     */
    private void openExploreConnectionsDlg(BusinessObject rack) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        List<BusinessObjectLight> links = bem.getSpecialChildrenOfClassLightRecursive(rack.getId(), rack.getClassName(), Constants.CLASS_GENERICPHYSICALLINK, -1);
        Grid<BusinessObjectLight> tblConnections = new Grid();
        tblConnections.setWidthFull();
        tblConnections.setMaxHeight("450px");
        tblConnections.setItems(links);
        tblConnections.addComponentColumn(item -> {          
            VerticalLayout lytName = new VerticalLayout(new BoldLabel(item.getName()));
            lytName.setSpacing(false);
            lytName.setMargin(false);
            lytName.setMargin(false);
            lytName.setPadding(false);
            return lytName;         
        }).setHeader(ts.getTranslatedString("module.visualization.rack-view-link-name")).setWidth("80px");
        
        tblConnections.addColumn(item -> {
            try {
                List<BusinessObjectLight> endpoint = bem.getSpecialAttribute(item.getClassName(), item.getId(), "endpointA");
                if (!endpoint.isEmpty()) {
                    List<BusinessObjectLight> parents = bem.getParentsUntilFirstOfClass(endpoint.get(0).getClassName(), endpoint.get(0).getId(), Constants.CLASS_RACK);
                    if (parents != null && parents.size() > 1) {
                        return parents.get(parents.size() - 2).getName() + " : " + endpoint.get(0).getName();
                    }
                    return ts.getTranslatedString("module.visualization.rack-view-disconnected");
                } else {
                    return ts.getTranslatedString("module.visualization.rack-view-disconnected");
                }
            } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                return "";
            }
        }).setHeader(ts.getTranslatedString("module.visualization.rack-view-source-device-port"));
        tblConnections.addColumn(item -> {
            try {
                List<BusinessObjectLight> endpoint = bem.getSpecialAttribute(item.getClassName(), item.getId(), "endpointB");
                if (!endpoint.isEmpty()) {
                    List<BusinessObjectLight> parents = bem.getParentsUntilFirstOfClass(endpoint.get(0).getClassName(), endpoint.get(0).getId(), Constants.CLASS_RACK);
                    if (parents != null && parents.size() > 1) {
                        return parents.get(parents.size() - 2).getName() + " : " + endpoint.get(0).getName();
                    }
                    return ts.getTranslatedString("module.visualization.rack-view-disconnected");
                } else {
                    return ts.getTranslatedString("module.visualization.rack-view-disconnected");
                }
            } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                return "";
            }
        }).setHeader(ts.getTranslatedString("module.visualization.rack-view-target-device-port"));
        
        Dialog dlgSummary = new Dialog();
        dlgSummary.setWidth("90%");
        dlgSummary.setHeight("900px");
        BoldLabel lblSummary = new BoldLabel(ts.getTranslatedString("module.visualization.rack-view-connections-rack"));
        Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), evt -> {
            dlgSummary.close();
        });
        VerticalLayout lytSummary = new VerticalLayout(lblSummary, tblConnections, btnClose);
        lytSummary.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        dlgSummary.add(lytSummary);
        dlgSummary.open();       
    }

    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        try {                   
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                bem.updateObject(businessObject.getClassName(), businessObject.getId(), attributes);
              
                //special case when the name is updated the label must be refreshed in the canvas
                if (property.getName().equals(Constants.PROPERTY_NAME)) {
                    
                }
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();
        }
    }
}
