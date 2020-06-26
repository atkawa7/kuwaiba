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

import com.neotropic.kuwaiba.modules.commercial.mpls.MplsManagerUI;
import com.neotropic.kuwaiba.modules.commercial.mpls.MplsView;
import com.neotropic.kuwaiba.modules.commercial.mpls.actions.DeleteMplsViewVisualAction;
import com.neotropic.kuwaiba.modules.commercial.mpls.actions.NewMplsViewVisualAction;
import com.neotropic.kuwaiba.modules.commercial.mpls.persistence.MplsConnectionDefinition;
import com.neotropic.kuwaiba.modules.commercial.mpls.persistence.MplsService;
import com.neotropic.kuwaiba.modules.commercial.mpls.tools.MplsTools;
import com.neotropic.vaadin14.component.MxGraphCell;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
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
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
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
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;

/**
 * MPLS Main Dashboard.
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
public class MplsDashboard extends VerticalLayout implements PropertySheet.IPropertyValueChangedListener {
    
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
     * listener to remove mpls view action
     */
    private ActionCompletedListener listenerDeleteAction;
    /**
     * listener to add new view Action
     */
    private ActionCompletedListener listenerNewViewAction;
    /**
     * reference of the visual action to remove a mpls view
     */
    private DeleteMplsViewVisualAction deleteMPLSViewVisualAction;
    /**
     * reference of the visual action to add a mpls view
     */
    private NewMplsViewVisualAction newMPLSViewVisualAction;
    /**
     * factory to instance object icons
     */
    private ResourceFactory resourceFactory;
    /**
     * service to persistence actions
     */
    private MplsService mplsService;
     /**
     * source Equipment in create new connection dialog
     */   
    private BusinessObjectLight selectedSourceEquipment;
    /**
     * target Equipment in create new connection dialog
     */
    private BusinessObjectLight selectedTargetEquipment;
    /**
     * source end point in create new connection dialog
     */
    private BusinessObjectLight selectedEndPointA;
    /**
     * target End point in create new connection dialog
     */
    private BusinessObjectLight selectedEndPointB;  
    /**
     * current view in the canvas
     */
    private ViewObject currentView;
    /**
     * canvas toolbar
     */
    private MplsTools mplsTools;
    /**
     * Instance of the main canvas view
     */
    private MplsView mplsView;
    /**
     * list of mpls views
     */
    private List<ViewObjectLight> mplsViews;
    /**
     * Reference to the grid that shows the mpls views 
     */
    private Grid<ViewObjectLight> tblViews;
    /**
     * Dialog that lists the whole list of the views
     */
    Dialog wdwMPLSViews;
    /**
     * reference to the current selected object in the canvas
     */
    private BusinessObjectLight selectedObject;
    /**
     * main property sheet instance
     */
    PropertySheet propertySheet;
    /**
     * button to remove views
     */
    Button btnRemoveView;
    
    Label lblCurrentViewName;
    Label lblCurrentViewDescription;
    VerticalLayout lytViewInfo;

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
    
    public MplsDashboard(TranslationService ts, MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem, 
            ResourceFactory resourceFactory, MplsService mplsService, DeleteMplsViewVisualAction deleteMPLSViewVisualAction, 
            NewMplsViewVisualAction newMPLSViewVisualAction) {
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
        setPadding(false);
        setMargin(false);
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
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }

    private void createContent() {    
        Button btnOpenView = new Button(new Icon(VaadinIcon.FOLDER_OPEN_O), ev -> {
             openListMplsViewDialog();
        });
        MplsTools.setButtonTitle(btnOpenView, ts.getTranslatedString("module.mpls.open-mpls-view"));
        btnOpenView.setClassName("icon-button");
        Button btnNewView = new Button(new Icon(VaadinIcon.PLUS), ev -> {
             this.newMPLSViewVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
        });
        MplsTools.setButtonTitle(btnNewView, ts.getTranslatedString("module.mpls.new-mpls-view"));
        btnNewView.setClassName("icon-button");
        btnRemoveView = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE_O), evt -> {
            if (currentView != null)
                this.deleteMPLSViewVisualAction.getVisualComponent(new ModuleActionParameterSet( new ModuleActionParameter("viewId", currentView.getId()))).open();
        });
        btnRemoveView.setClassName("icon-button");
        btnRemoveView.setEnabled(false);
        MplsTools.setButtonTitle(btnRemoveView, ts.getTranslatedString("module.mpls.remove-view"));
        
        mplsView = new MplsView(mem, aem, bem, ts, resourceFactory);   
        mplsView.getMxgraphCanvas().setComObjectSelected(() -> {
            
            String objectId = mplsView.getMxgraphCanvas().getSelectedCellId();
            if (MxGraphCell.PROPERTY_VERTEX.equals(mplsView.getMxgraphCanvas().getSelectedCellType())){
                 selectedObject = ((BusinessObjectViewNode) mplsView.getAsViewMap().findNode(objectId)).getIdentifier();
            } else {
                 selectedObject = ((BusinessObjectViewEdge) mplsView.getAsViewMap().findEdge(objectId)).getIdentifier();            
            }
            updatePropertySheet();
            mplsTools.setGeneralToolsEnabled(true);
            mplsTools.setSelectionToolsEnabled(true);
        });
        mplsView.getMxgraphCanvas().setComObjectUnselected(() -> {
            selectedObject = null;
            mplsTools.setSelectionToolsEnabled(false);
        });
        mplsView.getMxgraphCanvas().setComObjectDeleted(() -> {
            openConfirmDialogDeleteObject();   // delete from database by default
        });
        
        mplsTools = new MplsTools(bem, ts, new ArrayList(mplsView.getMxgraphCanvas().getNodes().keySet()),
                                     new ArrayList(mplsView.getMxgraphCanvas().getEdges().keySet()));
        mplsTools.addNewObjectListener(event -> {   
             BusinessObjectLight tmpObject = event.getObject();
             if (tmpObject == null)
                 return;
             try {
                 
                if(tmpObject.getClassName().equals(Constants.CLASS_MPLSLINK)) {                  
                   MplsConnectionDefinition connectionDetails = mplsService.getMPLSLinkDetails(tmpObject.getId());                    
                   if (connectionDetails.getDeviceA() != null && connectionDetails.getDeviceB() != null) {
                       addNodeToView(connectionDetails.getDeviceA(), 100, 50);
                       addNodeToView(connectionDetails.getDeviceB(), 400, 50);
                       addEdgeToView(connectionDetails);
                   }                   
                }  else 
                        addNodeToView(tmpObject, 100, 50);               
                
                mplsView.syncViewMap();
             } catch (InvalidArgumentException ex) {
                 Notification.show(ex.getMessage());
             }
        });
        mplsTools.addNewConnectionListener(event -> {                           
            openNewConnectionDialog();           
        });
        mplsTools.addSaveViewListener(event -> {
            saveCurrentView();
        });
        mplsTools.addDeleteObjectListener(event -> {
           deleteSelectedObject(false); 
        });
        mplsTools.addDeleteObjectPermanentlyObjectListener(event -> {
            openConfirmDialogDeleteObject();
        });
        mplsTools.AddDetectConnectionsListener(event -> {
            detectRelationships();
        });
        mplsTools.setGeneralToolsEnabled(false);
        
        HorizontalLayout lytTools = new HorizontalLayout(btnNewView, btnOpenView, btnRemoveView, mplsTools);
        lytTools.setAlignItems(Alignment.BASELINE);
        
        initializeActions();
        initializeTblViews();
            
        Label lblHintControlPoints = new Label(ts.getTranslatedString("module.mpls.hint-create-delete-control-point"));
        lblHintControlPoints.setClassName("hintMplsView");
        BoldLabel lblCurrentViewNameTitle = new BoldLabel(ts.getTranslatedString("module.mpls.view-name"));
        BoldLabel lblCurrentViewDescriptionTitle = new BoldLabel(ts.getTranslatedString("module.mpls.view-description"));
        lblCurrentViewName = new Label();
        lblCurrentViewDescription = new Label();
        
        HorizontalLayout lytViewName = new HorizontalLayout(lblCurrentViewNameTitle, lblCurrentViewName);
        HorizontalLayout lytViewDescription = new HorizontalLayout(lblCurrentViewDescriptionTitle, lblCurrentViewDescription);
        lytViewInfo = new VerticalLayout(lytViewName, lytViewDescription);
        setMarginPaddingLayout(lytViewInfo, false);
        lytViewInfo.setVisible(false);
        
        HorizontalLayout lytFooterView = new HorizontalLayout(lytViewInfo, lblHintControlPoints);
        setMarginPaddingLayout(lytFooterView, false);
        
        VerticalLayout lytDashboard = new VerticalLayout(lytTools, mplsView.getAsComponent(), lytFooterView);
        lytDashboard.setWidth("65%");
        //prop sheet section
        H4 headerListTypes = new H4(ts.getTranslatedString("module.propertysheet.labels.header"));
        propertySheet = new PropertySheet(ts, new ArrayList<>(), "");
        propertySheet.addPropertyValueChangedListener(this);
        
        VerticalLayout lytSheet = new VerticalLayout(headerListTypes, propertySheet);
        lytSheet.setSpacing(false);
        setMarginPaddingLayout(lytSheet, false);
        lytSheet.setWidth("35%");

        HorizontalLayout lytMain = new HorizontalLayout(lytDashboard, lytSheet);
        lytMain.setSizeFull();
        setMarginPaddingLayout(lytMain, false);
                     
        addAndExpand(lytMain);
        setSizeFull();
    }

    private void setMarginPaddingLayout(ThemableLayout lytViewInfo, boolean enable) {
        lytViewInfo.setMargin(enable);
        lytViewInfo.setPadding(enable);
    }
    
    /**
     * resets the mpls view instance and creates a empty one
     */
    public void resetDashboard() {
        mplsView.buildEmptyView();
        if (currentView != null)
            mplsView.buildWithSavedView(currentView.getStructure());
    }
    
    /**
     * Create and open the dialog form to create a new Connection
     */
    private void openNewConnectionDialog() {
         
        selectedSourceEquipment = null;
        selectedTargetEquipment = null;
        selectedEndPointA = null;
        selectedEndPointB = null;
        Dialog dlgConnection = new Dialog();
        TextField txtConnectionName = new TextField(ts.getTranslatedString("module.mpls.connection-name"));
        txtConnectionName.setWidthFull();
        ComboBox<BusinessObjectLight> cbxSourceObject = new ComboBox<>(ts.getTranslatedString("module.mpls.source-equipment"));                                 
        ComboBox<BusinessObjectLight>  cbxTargetObject = new ComboBox<>(ts.getTranslatedString("module.mpls.target-equipment"));
        cbxSourceObject.setWidthFull();
        cbxTargetObject.setWidthFull();
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
        sourceTree.setVisible(false);
        targetTree.setVisible(false);
        
        cbxSourceObject.addValueChangeListener(listener -> {
            if (listener.getValue() != null && listener.getValue().equals(selectedTargetEquipment)) {
                cbxSourceObject.setValue(null);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.mpls.must-select-different-equipments")).open();
                return;
            }
            selectedSourceEquipment = listener.getValue();
            selectedEndPointA = null;
            sourceTree.setVisible(true);
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
            targetTree.setVisible(true);
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
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.mpls.must-be-genericport-endpointA")).open();
                    return;
                }

                if (!mem.isSubclassOf(Constants.CLASS_GENERICPORT, selectedEndPointB.getClassName())) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.mpls.must-be-genericport-endpointB")).open();
                    return;
                }

                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(Constants.PROPERTY_NAME, txtConnectionName.getValue());
                Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                String newTransportLink = mplsService.createMPLSLink(selectedEndPointA.getClassName(), selectedEndPointA.getId(),
                        selectedEndPointB.getClassName(), selectedEndPointB.getId(), attributes, session.getUser().getUserName());
                if (newTransportLink == null) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.mpls.actions.mpls-link-error-creating")).open();
                } else {
                    BusinessObjectLight mplsLink = bem.getObject(Constants.CLASS_MPLSLINK, newTransportLink);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.actions.mpls-link-created")).open();
                    mplsView.getMxgraphCanvas().addEdge(mplsLink, mplsLink.getId(), selectedSourceEquipment, selectedTargetEquipment, null, selectedEndPointA.getName(), selectedEndPointB.getName());
                    mplsView.syncViewMap();
                    dlgConnection.close();
                }
            } catch (InvalidArgumentException | MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                Logger.getLogger(MplsDashboard.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error")).open();
            } 
            
        });
        
        HorizontalLayout lytButtons = new HorizontalLayout(btnCancel, btnCreateConnection);
        VerticalLayout lytForm = new VerticalLayout(txtConnectionName, cbxSourceObject, cbxTargetObject);
        lytForm.setWidth("500px");
        HorizontalLayout lytContent = new HorizontalLayout(lytForm, lytTrees);
        lytContent.setWidthFull();
        lytContent.setSpacing(false);
        lytContent.setPadding(false);
        
        dlgConnection.add(new VerticalLayout(lytContent, lytButtons));
        dlgConnection.setWidth("1100px");
        dlgConnection.open();
    }  
  
    /**
     * Function that creates a new HierarchicalDataProvider for a tree grid.
     * @param root the main toot of the tree
     * @return the new data Provider the the given root
     */
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
                        Logger.getLogger(MplsDashboard.class.getName()).log(Level.SEVERE, null, ex);
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
      
    /**
     * Save the current view in the canvas
     */
    private void saveCurrentView() {
        try {
            if (currentView != null) {
            aem.updateGeneralView(currentView.getId(), currentView.getName(), currentView.getDescription(), mplsView.getAsXml(), null);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.view-saved")).open();
            }
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            Logger.getLogger(MplsDashboard.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error")).open();
        }
          
    }
    
    /**
     * Creates a confirm dialog to ask for the remove object action
     */
    private void openConfirmDialogDeleteObject() {
        ConfirmDialog dlgConfirmDelete = new ConfirmDialog(ts.getTranslatedString("module.general.labels.confirmation"),
                ts.getTranslatedString("module.mpls.delete-permanently-message"),
                ts.getTranslatedString("module.general.messages.ok"));
        dlgConfirmDelete.open();
        dlgConfirmDelete.getBtnConfirm().addClickListener(evt -> {
            deleteSelectedObject(true);
            dlgConfirmDelete.close();
        });
    }

    /**
     * removes the selected object in the view
     * @param deletePermanently Boolean that if true specifies that the object
     * is permanently deleted or false if it was only deleted from the view. 
     */
    private void deleteSelectedObject(boolean deletePermanently) {            
        if( selectedObject != null) {
            try {
                if (MxGraphCell.PROPERTY_VERTEX.equals(mplsView.getMxgraphCanvas().getSelectedCellType())) {
                    if (deletePermanently)
                        bem.deleteObject(selectedObject.getClassName(), selectedObject.getId(), false);
                    mplsView.removeNode(selectedObject);                                 
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.object-deleted")).open();
                } else {
                    if (deletePermanently) {
                        Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                        mplsService.deleteMPLSLink(selectedObject.getId(), true, session.getUser().getUserName());       
                    }
                    mplsView.removeEdge(selectedObject);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.mpls-link-deleted")).open();
                }
                if (deletePermanently)
                        saveCurrentView();     
                selectedObject = null;              
                updatePropertySheet();
            } catch (BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                Logger.getLogger(MplsDashboard.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage()).open();
            }
        }
    }

    /**
     * detects the relationships of the nodes that are currently in the view, 
     * if they have MPLS Links that are not in the view then they are added 
     * with the corresponding end points
     */
    private void detectRelationships() {
            List<BusinessObjectLight> mplsLinksAdded = new ArrayList<>();
            List<BusinessObjectLight> nodesAdded = new ArrayList<>();
        try {
                            
            Properties props;
            List<BusinessObjectLight> viewNodes = new ArrayList(mplsView.getMxgraphCanvas().getNodes().keySet());
            for (BusinessObjectLight node : viewNodes) {
                
                List<AnnotatedBusinessObjectLight> objectMplsLinks = bem.getAnnotatedSpecialAttribute(node.getClassName(), node.getId(), MplsService.RELATIONSHIP_MPLSLINK);
                
                for (AnnotatedBusinessObjectLight link : objectMplsLinks) {
                    if (mplsView.getMxgraphCanvas().getEdges().containsKey(link.getObject()))
                        continue;
                    MplsConnectionDefinition connectionDetails = mplsService.getMPLSLinkDetails(link.getObject().getId());                
                                    
                    BusinessObjectLight theOtherEndPoint;
                    
                    if (node.equals(connectionDetails.getDeviceA()))
                        theOtherEndPoint = connectionDetails.getDeviceB();
                    else
                        theOtherEndPoint = connectionDetails.getDeviceA();
                    
                    if (theOtherEndPoint == null) // if the other object is null then omite the edge
                        continue;
                    
                    if (!mplsView.getMxgraphCanvas().getNodes().containsKey(theOtherEndPoint)) {                        
                        addNodeToView(theOtherEndPoint, 100 , 50);
                        nodesAdded.add(theOtherEndPoint);                              
                    }
                
                    addEdgeToView(connectionDetails);                        
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
            Logger.getLogger(MplsDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void initializeTblViews() {        
        loadViews();
        tblViews = new Grid<>();
        ListDataProvider<ViewObjectLight> dataProvider = new ListDataProvider<>(mplsViews);
        tblViews.setDataProvider(dataProvider);
        tblViews.addColumn(ViewObjectLight::getName).setFlexGrow(3).setKey(ts.getTranslatedString("module.general.labels.name"));
        tblViews.addComponentColumn(item -> createActionsColumn(item)).setKey("component-column");
        tblViews.addItemDoubleClickListener(listener -> {
            openMplsView(listener.getItem());
        });
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
              
        Button btnEdit = new Button(new Icon(VaadinIcon.EDIT), evt -> {
            openMplsView(item);
        });
        btnEdit.setClassName("icon-button");
        
        lytActions.add(btnEdit);      
        return lytActions;      
    }
    /**
     * loads the given mpls view into the view
     * @param item the mpls view to be loaded
     */
    private void openMplsView(ViewObjectLight item) {
        try {
            ViewObject view = aem.getGeneralView(item.getId());
            setCurrentView(view);
            lblCurrentViewName.setText(view.getName());
            lblCurrentViewDescription.setText(view.getDescription());
            lytViewInfo.setVisible(true);
            if (wdwMPLSViews != null)
                this.wdwMPLSViews.close();
            this.mplsTools.setGeneralToolsEnabled(true);
            selectedObject = null;
            updatePropertySheet();
            this.btnRemoveView.setEnabled(true);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.actions.view-loaded")).open();
        } catch (ApplicationObjectNotFoundException ex) {
            Logger.getLogger(MplsManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void loadViews() {
        try {
            mplsViews = aem.getGeneralViews(MplsService.VIEW_CLASS,-1);             
        } catch (InvalidArgumentException | NotAuthorizedException ex) {
            Logger.getLogger(MplsManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initialize the general actions that provides the functionalty to create 
     * and remove mpls views 
     */
    private void initializeActions() {
        listenerDeleteAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            loadViews();
            tblViews.setItems(mplsViews);
            tblViews.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
            mplsTools.setGeneralToolsEnabled(false);
            mplsTools.setSelectionToolsEnabled(false);
            btnRemoveView.setEnabled(false);
            setCurrentView(null);
            lytViewInfo.setVisible(false);
            lblCurrentViewName.setText("");
            lblCurrentViewDescription.setText("");  
            selectedObject = null;
            updatePropertySheet();
        };
        this.deleteMPLSViewVisualAction.registerActionCompletedLister(listenerDeleteAction);
        
        listenerNewViewAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            loadViews();
            tblViews.setItems(mplsViews);
            tblViews.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
            mplsTools.setGeneralToolsEnabled(true);
            btnRemoveView.setEnabled(true);
            lytViewInfo.setVisible(true);
            if (wdwMPLSViews != null)
                wdwMPLSViews.close();
            selectedObject = null;
            updatePropertySheet();
            ActionResponse response = ev.getActionResponse();
            try {
                ViewObject newView = aem.getGeneralView((long) response.get("viewId"));
                setCurrentView(newView);
                lblCurrentViewName.setText(newView.getName());
                lblCurrentViewDescription.setText(newView.getDescription());               
            } catch (ApplicationObjectNotFoundException ex) {
                Logger.getLogger(MplsManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }

        };
        this.newMPLSViewVisualAction.registerActionCompletedLister(listenerNewViewAction);        
    }

    /**
     * open the dialog that shows the list of available MPLS views.
     */
    private void openListMplsViewDialog() {
        wdwMPLSViews = new Dialog();
              
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), ev -> {
            wdwMPLSViews.close();
        });      
        VerticalLayout lytContent = new VerticalLayout(tblViews, btnCancel);
        lytContent.setAlignItems(Alignment.CENTER);
        wdwMPLSViews.add(lytContent);
        wdwMPLSViews.setWidth("600px");
        wdwMPLSViews.open();
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
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ex.getLocalizedMessage()).open();
        }
    }
    
    private void updatePropertySheet() {
        try {        
            if (selectedObject != null) {
                BusinessObject aWholeListTypeItem = bem.getObject(selectedObject.getClassName(), selectedObject.getId());
                propertySheet.setItems(PropertyFactory.propertiesFromBusinessObject(aWholeListTypeItem, ts, aem, mem));
            } else 
                propertySheet.clear();
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage()).open();
            Logger.getLogger(MplsDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * add a single node to the mpls view
     * @param node the node to be added
     */
    private void addNodeToView(BusinessObjectLight node, int x, int y) {
        if (mplsView.getAsViewMap().findNode(node) == null) {
            String uri = StreamResourceRegistry.getURI(resourceFactory.getClassIcon(node.getClassName())).toString();
            Properties props = new Properties();
            props.put("imageUrl", uri);
            props.put("x", x);
            props.put("y", y);
            mplsView.addNode(node, props);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.object-added")).open();                  
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.object-already-included")).open();                         
    }
    
    /**
     * adds and edge with his nodes o the mpls view
     * @param connection The link definition
     */
    private void addEdgeToView(MplsConnectionDefinition connection) {
        if (mplsView.getAsViewMap().findEdge(connection.getConnectionObject()) == null) {
            Properties props = new Properties();
            props.put("controlPoints", new ArrayList());
            props.put("sourceLabel", connection.getEndpointA() == null ? "" : connection.getEndpointA().getName());
            props.put("targetLabel", connection.getEndpointB() == null ? "" : connection.getEndpointB().getName());
            mplsView.addEdge(connection.getConnectionObject(), connection.getDeviceA(), connection.getDeviceB(), props);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.mpls-link-added")).open();                  
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.edge-already-included")).open();                   
        
    }
    
}