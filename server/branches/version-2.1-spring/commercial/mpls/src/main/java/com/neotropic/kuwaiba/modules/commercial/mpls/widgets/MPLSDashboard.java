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
import com.neotropic.kuwaiba.modules.commercial.mpls.persistence.MPLSService;
import com.neotropic.kuwaiba.modules.commercial.mpls.tools.MplsTools;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.server.StreamResourceRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.icons.BasicIconGenerator;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.NavigationTree;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.nodes.InventoryObjectNode;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * MPLS Main Dashboard
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
public class MPLSDashboard extends VerticalLayout  {
    
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
    
    private ResourceFactory resourceFactory;
    
    private MPLSService mplsService;
        
    BusinessObjectLight selectedSourceEquipment;
    
    BusinessObjectLight selectedTargetEquipment;
    
    BusinessObjectLight selectedEndPointA;
    
    BusinessObjectLight selectedEndPointB;  
    
    ViewObject currentView;
    
    MplsTools mplsTools;
    
    MPLSView mplsView;

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
    
    public MPLSDashboard(TranslationService ts, MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem, ResourceFactory resourceFactory, MPLSService mplsService) {
        super();
        this.ts = ts;
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
        this.resourceFactory = resourceFactory;
        this.mplsService = mplsService;
        setSizeFull();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent); 
        createContent();
    }
   
    @Override
    public void onDetach(DetachEvent ev) {
        
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

        mplsView = new MPLSView(mem, aem, bem, ts, resourceFactory);       
              
        mplsTools = new MplsTools(bem, null);
        mplsTools.addNewObjectListener(event -> {   
             BusinessObjectLight tmpObject = event.getObject();
             if(tmpObject != null) {
                String uri = StreamResourceRegistry.getURI(resourceFactory.getClassIcon(tmpObject.getClassName())).toString();       
                mplsView.getMxgraphCanvas().attachNodeWidget(tmpObject, tmpObject.getId(), 20, 20, uri);
             }
        });
        mplsTools.addNewConnectionListener(event -> {                           
            openConnectionAssistant();           
        });
        mplsTools.addSaveViewListener(event -> {
            saveCurrentView();
        });
        mplsTools.setToolsEnabled(false);
        VerticalLayout mainContent = new VerticalLayout(mplsTools, mplsView.getAsComponent());
        
        addAndExpand(mainContent);
        setSizeFull();
    }
    
    public void resetDashboard() {
        mplsView.buildEmptyView();
        if (currentView != null)
            mplsView.buildWithSavedView(currentView.getStructure());
    }
    
    private void openConnectionAssistant() {
         
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
                    mplsView.getMxgraphCanvas().attachEdgeWidget(mplsLink, selectedSourceEquipment, selectedTargetEquipment, null, selectedEndPointA.getName(), selectedEndPointB.getName());
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
}