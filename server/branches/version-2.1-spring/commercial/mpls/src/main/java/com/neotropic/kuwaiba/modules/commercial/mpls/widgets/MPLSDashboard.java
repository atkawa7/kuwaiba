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

package com.neotropic.kuwaiba.modules.commercial.mpls.widgets;

import com.neotropic.kuwaiba.modules.commercial.mpls.*;
import static com.neotropic.kuwaiba.modules.commercial.mpls.MPLSModule.CLASS_VIEW;
import com.neotropic.kuwaiba.modules.commercial.mpls.actions.DeleteMPLSViewVisualAction;
import com.neotropic.kuwaiba.modules.commercial.mpls.actions.NewMPLSViewVisualAction;
import com.neotropic.kuwaiba.modules.commercial.mpls.persistence.MPLSConnectionDefinition;
import com.neotropic.kuwaiba.modules.commercial.mpls.persistence.MPLSService;
import com.neotropic.kuwaiba.modules.commercial.mpls.tools.MplsTools;
import com.neotropic.vaadin14.component.MxGraphCell;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.StreamResourceRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.ActionResponse;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.AnnotatedBusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.icons.BasicIconGenerator;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.NavigationTree;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.nodes.InventoryObjectNode;
import org.neotropic.kuwaiba.modules.core.navigation.properties.PropertyFactory;
import org.neotropic.kuwaiba.modules.core.navigation.properties.PropertyValueConverter;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.kuwaiba.visualization.views.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.views.BusinessObjectViewNode;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.views.AbstractViewEdge;
import org.neotropic.util.visual.views.AbstractViewNode;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * MPLS Main Dashboard
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
public class MPLSDashboard extends VerticalLayout implements PropertySheet.IPropertyValueChangedListener {
    
    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager.
     */
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    private BusinessEntityManager bem;
    
    /**
     * listener to attribute actions
     */
    private ActionCompletedListener listenerDeleteAction;
    /**
     * listener to attribute actions
     */
    private ActionCompletedListener listenerNewViewAction;
    
    private DeleteMPLSViewVisualAction deleteMPLSViewVisualAction;
    
    private NewMPLSViewVisualAction newMPLSViewVisualAction;
    
    private ResourceFactory resourceFactory;
    
    private MPLSService mplsService;
        
    private BusinessObjectLight selectedSourceEquipment;
    
    private BusinessObjectLight selectedTargetEquipment;
    
    private BusinessObjectLight selectedEndPointA;
    
    private BusinessObjectLight selectedEndPointB;  
    
    private ViewObject currentView;
    
    private MplsTools mplsTools;
    
    private MPLSView mplsView;
    
    private List<ViewObjectLight> mplsViews;
    
    private Grid<ViewObjectLight> tblViews;
    
    Dialog dlgMPLSViews;
    
    private BusinessObjectLight selectedObject;
    
    PropertySheet propertySheet;

    public ViewObject getCurrentView() {
        return currentView;
    }

    public void setCurrentView(ViewObject currentView) {
        this.currentView = currentView;
        resetDashboard();
    }

    public MplsTools getMplsTools() {
        return mplsTools;
    }

    public void setMplsTools(MplsTools mplsTools) {
        this.mplsTools = mplsTools;
    }  
    
    public MPLSDashboard(TranslationService ts, MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem, ResourceFactory resourceFactory, MPLSService mplsService, DeleteMPLSViewVisualAction deleteMPLSViewVisualAction, NewMPLSViewVisualAction newMPLSViewVisualAction) {
        super();
        this.ts = ts;
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
        this.resourceFactory = resourceFactory;
        this.mplsService = mplsService;
        this.newMPLSViewVisualAction = newMPLSViewVisualAction;
        this.deleteMPLSViewVisualAction = deleteMPLSViewVisualAction;
        setSizeFull();
    }        

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent); 
        createContent();
    }
   
    @Override
    public void onDetach(DetachEvent ev) {
        this.deleteMPLSViewVisualAction.unregisterListener(listenerDeleteAction);
        this.newMPLSViewVisualAction.unregisterListener(listenerNewViewAction);
    }
    
    public void showActionCompledMessages(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try {
                
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
                                            
            } catch (Exception ex) {
                Logger.getLogger(MPLSDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }

    private void createContent() {    
        
        Button btnOpenView = new Button(ts.getTranslatedString("module.mpsl.open-mpls-view"), new Icon(VaadinIcon.EDIT), ev -> {
             openListMplsViewDialog();
        });
        Button btnNewView = new Button(ts.getTranslatedString("module.mpsl.new-mpls-view"), new Icon(VaadinIcon.PLUS), ev -> {
             this.newMPLSViewVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
        });
        HorizontalLayout lytGeneralActions = new HorizontalLayout(btnOpenView, btnNewView);

        mplsView = new MPLSView(mem, aem, bem, ts, resourceFactory);   
        mplsView.getMxgraphCanvas().setComObjectSelected(() -> {
            
            String objectId = mplsView.getMxgraphCanvas().getSelectedCellId();
            if (MxGraphCell.PROPERTY_VERTEX.equals(mplsView.getMxgraphCanvas().getSelectedCellType())){
                 selectedObject = ((BusinessObjectViewNode) mplsView.getAsViewMap().getNode(objectId)).getIdentifier();
            } else {
                 selectedObject = ((BusinessObjectViewEdge) mplsView.getAsViewMap().getEdge(objectId)).getIdentifier();            
            }
            updatePropertySheet();
            
        });
        mplsView.getMxgraphCanvas().setComObjectDeleted(() -> {
            openDeleteObjectDialog(true);   // delete from database by default
        });
        initializeActions();
        initializeTblViews();
              
        mplsTools = new MplsTools(bem, ts, null);
        mplsTools.addNewObjectListener(event -> {   
             BusinessObjectLight tmpObject = event.getObject();
             if(tmpObject != null) {
                String uri = StreamResourceRegistry.getURI(resourceFactory.getClassIcon(tmpObject.getClassName())).toString();       
                mplsView.getMxgraphCanvas().addNode(tmpObject, tmpObject.getId(), 20, 20, uri);
                mplsView.syncViewMap();
             }
        });
        mplsTools.addNewConnectionListener(event -> {                           
            openNewConnectionDialog();           
        });
        mplsTools.AddExistingConnectionListener(event -> {                           
            openExistingConnectionDialog();           
        });
        mplsTools.addSaveViewListener(event -> {
            saveCurrentView();
        });
        mplsTools.addDeleteObjectListener(event -> {
            openDeleteObjectDialog(false);
        });
        mplsTools.addDeleteObjectPermanentlyObjectListener(event -> {
            openDeleteObjectDialog(true);
        });
        mplsTools.AddDetectConnectionsListener(event -> {
            detectRelationships();
        });
        mplsTools.setToolsEnabled(false);
        VerticalLayout lytDashboard = new VerticalLayout(lytGeneralActions, mplsTools, mplsView.getAsComponent());
        lytDashboard.setWidth("65%");
        //prop sheet section
        H4 headerListTypes = new H4(ts.getTranslatedString("module.propertysheet.labels.header"));
        propertySheet = new PropertySheet(ts, new ArrayList<>(), "");
        propertySheet.addPropertyValueChangedListener(this);
        
        VerticalLayout lytSheet = new VerticalLayout(headerListTypes, propertySheet);
        lytSheet.setSpacing(false);
        lytSheet.setPadding(false);
        lytSheet.setWidth("35%");

        HorizontalLayout lytMain = new HorizontalLayout(lytDashboard, lytSheet);
        lytMain.setSizeFull();
        
        addAndExpand(lytMain);
        setSizeFull();
    }
    
    public void resetDashboard() {
        mplsView.buildEmptyView();
        if (currentView != null)
            mplsView.buildWithSavedView(currentView.getStructure());
    }
    
    private void openNewConnectionDialog() {
         
        selectedSourceEquipment = null;
        selectedTargetEquipment = null;
        selectedEndPointA = null;
        selectedEndPointB = null;
        Dialog dlgConnection = new Dialog();
        TextField txtConnectionName = new TextField(ts.getTranslatedString("module.mpls.connection-name"));
        
        ComboBox<BusinessObjectLight> cbxSourceObject = new ComboBox<>(ts.getTranslatedString("module.mpls.source-equipment"));
        ComboBox<BusinessObjectLight>  cbxTargetObject = new ComboBox<>(ts.getTranslatedString("module.mpls.target-equipment"));
        
        cbxSourceObject.setAllowCustomValue(false);
        cbxSourceObject.setClearButtonVisible(true);
        cbxSourceObject.setItems(mplsView.getMxgraphCanvas().getNodes().keySet());
        
        cbxTargetObject.setAllowCustomValue(false);
        cbxTargetObject.setClearButtonVisible(true);
        cbxTargetObject.setItems(mplsView.getMxgraphCanvas().getNodes().keySet());
        
        HierarchicalDataProvider dataProviderSourceTree = buildHierarchicalDataProvider(new BusinessObjectLight("", "", ""));
        HierarchicalDataProvider dataProviderTargetTree = buildHierarchicalDataProvider(new BusinessObjectLight("", "", ""));
        TreeGrid<InventoryObjectNode> sourceTree = new NavigationTree(dataProviderSourceTree , new BasicIconGenerator(resourceFactory));
        TreeGrid<InventoryObjectNode> targetTree = new NavigationTree(dataProviderTargetTree , new BasicIconGenerator(resourceFactory));
        
        cbxSourceObject.addValueChangeListener(listener -> {
            if (listener.getValue() != null && listener.getValue().equals(selectedTargetEquipment)) {
                cbxSourceObject.setValue(null);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.mpls.must-select-different-equipments")).open();
                return;
            }
            selectedSourceEquipment = listener.getValue();
            selectedEndPointA = null;
            sourceTree.setDataProvider(buildHierarchicalDataProvider(listener.getValue() == null ? new BusinessObjectLight("", "", "") : listener.getValue()));
        });
        cbxTargetObject.addValueChangeListener(listener -> {
            if (listener.getValue() != null && listener.getValue().equals(selectedSourceEquipment)) {
                cbxTargetObject.setValue(null);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.mpls.must-select-different-equipments")).open();
                return;
            }
            selectedTargetEquipment = listener.getValue();
            selectedEndPointB = null;
            targetTree.setDataProvider(buildHierarchicalDataProvider(listener.getValue() == null ? new BusinessObjectLight("", "", "") : listener.getValue()));
        });

        sourceTree.addItemClickListener( item -> {
             selectedEndPointA = item.getItem().getObject();
        });
        targetTree.addItemClickListener( item -> {
             selectedEndPointB = item.getItem().getObject();
        });
        
        HorizontalLayout lytTrees = new HorizontalLayout(new VerticalLayout(new H5(ts.getTranslatedString("module.mpls.source-end-point")), sourceTree) 
                                 , new VerticalLayout(new H5(ts.getTranslatedString("module.mpls.target-end-point")),targetTree));
        lytTrees.setSizeFull();
              
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), evt -> {
            dlgConnection.close();
        });
        Button btnCreateConnection = new Button(ts.getTranslatedString("module.mpls.create-connection"), evt -> {
            
            if (selectedEndPointA == null) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.mpls.must-select-end-point-a")).open();
                return;
            }
            if (selectedEndPointB == null) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.mpls.must-select-end-point-b")).open();
                return;
            }
            try {

                if (!mem.isSubclassOf(Constants.CLASS_GENERICPORT, selectedEndPointA.getClassName())) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), "Must select a instance of GENERICPORT in endpointA").open();
                    return;
                }

                if (!mem.isSubclassOf(Constants.CLASS_GENERICPORT, selectedEndPointB.getClassName())) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), "Must select a instance of GENERICPORT in endpointB").open();
                    return;
                }

                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(Constants.PROPERTY_NAME, txtConnectionName.getValue());
//                UI.getCurrent().getSession().getAttribute(name)
                String newTransportLink = mplsService.createMPLSLink(selectedEndPointA.getClassName(), selectedEndPointA.getId(),
                        selectedEndPointB.getClassName(), selectedEndPointB.getId(), attributes, "m");
                if (newTransportLink == null) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), "Error Creating link").open();
                } else {
                    BusinessObjectLight mplsLink = bem.getObject(Constants.CLASS_MPLSLINK, newTransportLink);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), "MPLS Link Created").open();
                    mplsView.getMxgraphCanvas().addEdge(mplsLink, mplsLink.getId(), selectedSourceEquipment, selectedTargetEquipment, null, selectedEndPointA.getName(), selectedEndPointB.getName());
                    mplsView.syncViewMap();
                    dlgConnection.close();
                }
            } catch (InvalidArgumentException | MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                Logger.getLogger(MPLSDashboard.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), "Unexpected Error").open();
            } 
            
        });
        
        HorizontalLayout lytButtons = new HorizontalLayout(btnCancel, btnCreateConnection);
        VerticalLayout lytForm = new VerticalLayout(txtConnectionName, cbxSourceObject, cbxTargetObject);
        lytForm.setWidth("300px");
        HorizontalLayout lytContent = new HorizontalLayout(lytForm, lytTrees);
        lytContent.setWidthFull();
        lytContent.setSpacing(false);
        lytContent.setPadding(false);
        
        dlgConnection.add(new VerticalLayout(lytContent, lytButtons));
        dlgConnection.setWidth("900px");
        dlgConnection.open();
    }  
  
    public HierarchicalDataProvider buildHierarchicalDataProvider(BusinessObjectLight root) {
        return new AbstractBackEndHierarchicalDataProvider<InventoryObjectNode, Void>() {
            @Override
            protected Stream<InventoryObjectNode> fetchChildrenFromBackEnd(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    if (parent.getClassName() == null || parent.getClassName().isEmpty()) 
                        return new ArrayList().stream();
                    BusinessObjectLight object = parent.getObject();
                    try {
                        List<BusinessObjectLight> children = bem.getObjectChildren(object.getClassName(), object.getId(), -1);
                        List<InventoryObjectNode> theChildren = new ArrayList();
                        for (BusinessObjectLight child : children)
                            theChildren.add(new InventoryObjectNode(child, child.getClassName()));
                        return theChildren.stream();
                    } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                        Notification.show(ex.getMessage());
                        return new ArrayList().stream();
                    } 
                } else {   
                       
                        return Arrays.asList(new InventoryObjectNode(root, root.getClassName())).stream();                   
                }              
            }

            @Override
            public int getChildCount(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    try {
                        BusinessObjectLight object = parent.getObject();
                        return (int) bem.getObjectChildrenCount(object.getClassName(), object.getId());
                    } catch (InvalidArgumentException ex) {
                        Logger.getLogger(MPLSDashboard.class.getName()).log(Level.SEVERE, null, ex);
                        return 0;
                    }
                    
                } else
                    return 1;
            }

            @Override
            public boolean hasChildren(InventoryObjectNode node) {
                return true;
            }
        };
    }
          
    private void saveCurrentView() {
        try {
            if (currentView != null) {
            aem.updateGeneralView(currentView.getId(), currentView.getName(), currentView.getDescription(), mplsView.getAsXml(), null);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.view-saved")).open();
            }
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            Logger.getLogger(MPLSDashboard.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error")).open();
        }
          
    }

    private void openDeleteObjectDialog(boolean deletePermanently) {
        
        if (deletePermanently) {
            ConfirmDialog dlgConfirmDelete = new ConfirmDialog(ts.getTranslatedString("module.general.labels.confirmcaption"),
                        ts.getTranslatedString("module.mpsl.delete-permanently-message"),
                        ts.getTranslatedString("module.general.labels.delete"));
            dlgConfirmDelete.open();
            dlgConfirmDelete.getBtnConfirm().addClickListener(evt -> {
                deleteSelectedObject(deletePermanently);
                dlgConfirmDelete.close();
            });
        } else
           deleteSelectedObject(deletePermanently); 
    }

    private void deleteSelectedObject(boolean deletePermanently) {            
        if( selectedObject != null) {
            try {
                if (MxGraphCell.PROPERTY_VERTEX.equals(mplsView.getMxgraphCanvas().getSelectedCellType())) {
                    if (deletePermanently)
                        bem.deleteObject(selectedObject.getClassName(), selectedObject.getId(), false);
                    mplsView.deleteNode(selectedObject);                                 
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpsl.object-deleted")).open();
                } else {
                    if (deletePermanently)
                        mplsService.deleteMPLSLink(selectedObject.getId(), true, "m");                
                    mplsView.deleteEdge(selectedObject);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpsl.mpls-link-deleted")).open();
                }
                if (deletePermanently)
                        saveCurrentView();     
                selectedObject = null;
            } catch (BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                Logger.getLogger(MPLSDashboard.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage()).open();
            }
        }
    }

    private void openExistingConnectionDialog() {
        
        try {
            List<BusinessObjectLight> mplsLinks = bem.getObjectsOfClassLight(Constants.CLASS_MPLSLINK, -1);
            ComboBox<BusinessObjectLight>  cbxMplsLinks = new ComboBox<>(ts.getTranslatedString("module.mpsl.target-equipment"));
            cbxMplsLinks.setAllowCustomValue(false);
            cbxMplsLinks.setItems(mplsLinks);
            Dialog dlgConnection = new Dialog();
//            BoldLabel lblSelectedView
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), evt -> {
            dlgConnection.close();
        });
        Button btnCreateConnection = new Button(ts.getTranslatedString("module.mpsl.add-connection"), evt -> {
        
                try {
                    BusinessObjectLight mplsLinkSelected = cbxMplsLinks.getValue();
                    MPLSConnectionDefinition connectionDetails = mplsService.getMPLSLinkDetails(mplsLinkSelected.getId());
                    
                    String uri = StreamResourceRegistry.getURI(resourceFactory.getClassIcon(connectionDetails.getDeviceA().getClassName())).toString();                                 
                    Properties props = new Properties();
                    props.put("imageUrl", uri);
                    props.put("x", 50 );
                    props.put("y", 50 );
                    mplsView.addNode(connectionDetails.getDeviceA(), props);
                    
                    uri = StreamResourceRegistry.getURI(resourceFactory.getClassIcon(connectionDetails.getDeviceB().getClassName())).toString();                                 
                    props = new Properties();
                    props.put("imageUrl", uri);
                    props.put("x", 400 );
                    props.put("y", 50 );
                    mplsView.addNode(connectionDetails.getDeviceB(), props);
                    
                    props = new Properties();
                    props.put("controlPoints", new ArrayList());
                    props.put("sourceLabel", connectionDetails.getEndpointA() == null ? "" : connectionDetails.getEndpointA().getName());
                    props.put("targetLabel", connectionDetails.getEndpointB() == null ? "" : connectionDetails.getEndpointB().getName());
                    mplsView.addEdge(mplsLinkSelected, connectionDetails.getDeviceA(), connectionDetails.getDeviceB(), props);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), "MPLS Link Added").open();
                    dlgConnection.close();
                } catch (InvalidArgumentException ex) {
                    Logger.getLogger(MPLSDashboard.class.getName()).log(Level.SEVERE, null, ex);
                }
        
        });
         HorizontalLayout lytButtons = new HorizontalLayout(btnCancel, btnCreateConnection);
         
         dlgConnection.add(cbxMplsLinks, lytButtons);
         dlgConnection.setWidth("400px");
         dlgConnection.open();
            
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(MPLSDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void detectRelationships() {
            List<BusinessObjectLight> mplsLinksAdded = new ArrayList<>();
            List<BusinessObjectLight> nodesAdded = new ArrayList<>();
        try {
                            
            Properties props;
            List<BusinessObjectLight> viewNodes = new ArrayList(mplsView.getMxgraphCanvas().getNodes().keySet());
            for (BusinessObjectLight node : viewNodes) {
                
                List<AnnotatedBusinessObjectLight> objectMplsLinks = bem.getAnnotatedSpecialAttribute(node.getClassName(), node.getId(), MPLSService.RELATIONSHIP_MPLSLINK);
                
                for (AnnotatedBusinessObjectLight link : objectMplsLinks) {
                    if (mplsView.getMxgraphCanvas().getEdges().containsKey(link.getObject()))
                        continue;
                    MPLSConnectionDefinition connectionDetails = mplsService.getMPLSLinkDetails(link.getObject().getId());                
                                    
                    BusinessObjectLight theOtherEndPoint;
                    
                    if (node.equals(connectionDetails.getDeviceA()))
                        theOtherEndPoint = connectionDetails.getDeviceB();
                    else
                        theOtherEndPoint = connectionDetails.getDeviceA();
                    
                    if (!mplsView.getMxgraphCanvas().getNodes().containsKey(theOtherEndPoint)) { 
                        String uri = StreamResourceRegistry.getURI(resourceFactory.getClassIcon(theOtherEndPoint.getClassName())).toString();                                 
                        props = new Properties();
                        props.put("imageUrl", uri);
                        props.put("x", 50 );
                        props.put("y", 50 );
                        mplsView.addNode(theOtherEndPoint, props);
                        nodesAdded.add(theOtherEndPoint);
                               
                    }
                
                    props = new Properties();
                    props.put("controlPoints", new ArrayList());
                    props.put("sourceLabel", connectionDetails.getEndpointA() == null ? "" : connectionDetails.getEndpointA().getName());
                    props.put("targetLabel", connectionDetails.getEndpointB() == null ? "" : connectionDetails.getEndpointB().getName());   
                    mplsView.addEdge(link.getObject(), connectionDetails.getDeviceA(), connectionDetails.getDeviceB(), props);                        
                    mplsLinksAdded.add(link.getObject());
               }
            }
            if (nodesAdded.size() > 0)    
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), nodesAdded.size() + " Object(s) Added").open();
            if (mplsLinksAdded.size() > 0)    
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), mplsLinksAdded.size() + " MPLS Link(s) Added").open();
            else 
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),"No MPLS Link found for objects in view").open();
        }
        catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(MPLSDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
        private void initializeTblViews() {
        
        loadViews();
        tblViews = new Grid<>();
        ListDataProvider<ViewObjectLight> dataProvider = new ListDataProvider<>(mplsViews);
        tblViews.setDataProvider(dataProvider);
        tblViews.addColumn(ViewObjectLight::getName).setFlexGrow(3).setKey(ts.getTranslatedString("module.general.labels.name"));
        tblViews.addComponentColumn(item -> createActionsColumn(item)).setKey("component-column");
        
        HeaderRow filterRow = tblViews.appendHeaderRow();
        
        TextField txtViewNameFilter = new TextField(ts.getTranslatedString("module.general.labels.filter"), ts.getTranslatedString("module.general.labels.filterplaceholder"));
        txtViewNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
        txtViewNameFilter.setWidthFull();
        txtViewNameFilter.addValueChangeListener(event -> dataProvider.addFilter(
        project -> StringUtils.containsIgnoreCase(project.getName(),
                txtViewNameFilter.getValue())));
        
        filterRow.getCell(tblViews.getColumnByKey(ts.getTranslatedString("module.general.labels.name"))).setComponent(txtViewNameFilter);
        
    }

    private HorizontalLayout createActionsColumn(ViewObjectLight item) {
        HorizontalLayout lytActions = new HorizontalLayout();
        
        Button btnDelete = new Button(new Icon(VaadinIcon.TRASH), evt -> {
             this.deleteMPLSViewVisualAction.getVisualComponent(new ModuleActionParameterSet( new ModuleActionParameter("viewId", item.getId()))).open();
        });
        btnDelete.setClassName("icon-button");
        Button btnEdit = new Button(new Icon(VaadinIcon.EDIT), evt -> {
            try {
                ViewObject view = aem.getGeneralView(item.getId());
                setCurrentView(view);
                if (dlgMPLSViews != null)
                    this.dlgMPLSViews.close();
                this.mplsTools.setToolsEnabled(true);
            } catch (ApplicationObjectNotFoundException ex) {
                Logger.getLogger(MPLSUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        btnEdit.setClassName("icon-button");
        
        lytActions.add(btnDelete, btnEdit);
        
        return lytActions;       
    }
    
    public void loadViews() {
        try {
            mplsViews = aem.getGeneralViews(CLASS_VIEW,-1);             
        } catch (InvalidArgumentException | NotAuthorizedException ex) {
            Logger.getLogger(MPLSUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initializeActions() {
        listenerDeleteAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            loadViews();
            tblViews.setItems(mplsViews);
            tblViews.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
            mplsTools.setToolsEnabled(false);
            setCurrentView(null);
            if (dlgMPLSViews != null)
                dlgMPLSViews.close();
        };
        this.deleteMPLSViewVisualAction.registerActionCompletedLister(listenerDeleteAction);
        
        listenerNewViewAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            loadViews();
            tblViews.setItems(mplsViews);
            tblViews.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
            mplsTools.setToolsEnabled(true);
            if (dlgMPLSViews != null)
                dlgMPLSViews.close();
            ActionResponse response = ev.getActionResponse();
            try {
                ViewObject newView = aem.getGeneralView((long) response.get("viewId"));
                setCurrentView(newView);
            } catch (ApplicationObjectNotFoundException ex) {
                Logger.getLogger(MPLSUI.class.getName()).log(Level.SEVERE, null, ex);
            }

        };
        this.newMPLSViewVisualAction.registerActionCompletedLister(listenerNewViewAction);        
    }

    private void openListMplsViewDialog() {
        dlgMPLSViews = new Dialog();
              
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), ev -> {
            dlgMPLSViews.close();
        });      
        VerticalLayout lytContent = new VerticalLayout(tblViews, btnCancel);
        lytContent.setAlignItems(Alignment.CENTER);
        dlgMPLSViews.add(lytContent);
        dlgMPLSViews.setWidth("600px");
        dlgMPLSViews.open();
    }
    
    @Override
    public void updatePropertyChanged(AbstractProperty property) {
        try {
            if (selectedObject != null) {              
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));

                bem.updateObject(selectedObject.getClassName(), selectedObject.getId(), attributes);
                updatePropertySheet();
                resetDashboard();

                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update")).open();
            }
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException
                | OperationNotPermittedException | InvalidArgumentException ex) {
            Logger.getLogger(MPLSDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void updatePropertySheet() {
        try {
           
            if (selectedObject != null) {
                BusinessObject aWholeListTypeItem = bem.getObject(selectedObject.getClassName(), selectedObject.getId());
                propertySheet.setItems(PropertyFactory.propertiesFromBusinessObject(aWholeListTypeItem, ts, aem, mem));
            }
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage()).open();
            Logger.getLogger(MPLSDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}