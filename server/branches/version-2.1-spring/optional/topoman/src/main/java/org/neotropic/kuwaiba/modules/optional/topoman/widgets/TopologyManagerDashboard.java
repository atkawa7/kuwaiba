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
package org.neotropic.kuwaiba.modules.optional.topoman.widgets;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraphCell;
import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.StreamResourceRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.properties.PropertyValueConverter;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.kuwaiba.modules.optional.physcon.persistence.PhysicalConnectionsService;
import org.neotropic.kuwaiba.modules.optional.topoman.TopologyView;
import static org.neotropic.kuwaiba.modules.optional.topoman.TopologyView.FREE_SHAPE;
import static org.neotropic.kuwaiba.modules.optional.topoman.TopologyView.ICON;
import static org.neotropic.kuwaiba.modules.optional.topoman.TopologyView.URL_IMG_CLOUD;
import org.neotropic.kuwaiba.modules.optional.topoman.actions.DeleteTopologyViewVisualAction;
import org.neotropic.kuwaiba.modules.optional.topoman.actions.NewTopologyViewVisualAction;
import org.neotropic.kuwaiba.modules.optional.topoman.tools.BasicStyleEditor;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;

/**
 * Topology designer Main Dashboard.
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
public class TopologyManagerDashboard extends VerticalLayout implements PropertySheet.IPropertyValueChangedListener {

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
     * listener to remove topology view action
     */
    private ActionCompletedListener listenerDeleteAction;
    /**
     * listener to add new topology view Action
     */
    private ActionCompletedListener listenerNewViewAction;
        /**
     * reference of the visual action to remove a topology view
     */
    private DeleteTopologyViewVisualAction deleteTopologyViewVisualAction;
    /**
     * reference of the visual action to add a topology view
     */
    private NewTopologyViewVisualAction newTopologyViewVisualAction ;
    /**
     * factory to instance object icons
     */
    private ResourceFactory resourceFactory;
    /**
     * source Equipment in create new connection dialog
     */
    private BusinessObjectLight selectedSourceEquipment;
    /**
     * target Equipment in create new connection dialog
     */
    private BusinessObjectLight selectedTargetEquipment;
    /**
     * current view in the canvas
     */
    private ViewObject currentView;

    /**
     * Instance of the main canvas view
     */
    private TopologyView topologyView;
    /**
     * list of topology views
     */
    private List<ViewObjectLight> topologyViews;
    /**
     * Reference to the grid that shows the topology views
     */
    private Grid<ViewObjectLight> tblViews;
    /**
     * Dialog that lists the whole list of the views
     */
    Dialog wdwTopologyViews;
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
    
    Button btnSaveView;
    
    Button btnRemoveObjectFromView;
    
    Button btnAddCloud;
    
    Button btnAddRectShape;
    
    Button btnAddEllipseShape;
    
    Button btnAddLabel;
    
    Button btnCopyView;
     
    public static String CLASS_VIEW = "TopologyModuleView";

    PhysicalConnectionsService connectionsService;
    
    VerticalLayout lytObjectProperties;
    
    TopologyViewSearch viewSearch;
    
    BasicStyleEditor styleEditor;

    public ViewObject getCurrentView() {
        return currentView;
    }

    public void setCurrentView(ViewObject currentView) {
        this.currentView = currentView;
        resetDashboard();
    }

    public TopologyManagerDashboard(TranslationService ts, MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem,
            ResourceFactory resourceFactory, NewTopologyViewVisualAction newTopologyViewVisualAction, DeleteTopologyViewVisualAction deleteTopologyViewVisualAction) {
        super(); 
        this.ts = ts;
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
        this.resourceFactory = resourceFactory;
        this.newTopologyViewVisualAction = newTopologyViewVisualAction;
        this.deleteTopologyViewVisualAction = deleteTopologyViewVisualAction;
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
        this.deleteTopologyViewVisualAction.unregisterListener(listenerDeleteAction);
        this.newTopologyViewVisualAction.unregisterListener(listenerNewViewAction);
    }

    public void showActionCompledMessages(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
        } else {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
        }
    }

    private void createContent() {

        Button btnOpenView = new Button(new Icon(VaadinIcon.FOLDER_OPEN_O), ev -> {
            openListTopologyViewDialog();
        });
        setButtonTitle(btnOpenView, ts.getTranslatedString("module.topoman.open-topo-view"));
        btnOpenView.setClassName("icon-button");
        Button btnNewView = new Button(new Icon(VaadinIcon.PLUS), ev -> {
             this.newTopologyViewVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
        });
        setButtonTitle(btnNewView, ts.getTranslatedString("module.topoman.actions.new-view.name"));
        btnNewView.setClassName("icon-button");
        btnCopyView = new Button(new Icon(VaadinIcon.COPY_O), ev -> {
            copyCurrentView();
        });
        setButtonTitle(btnCopyView, ts.getTranslatedString("module.topoman.copy-topo-view"));
        btnNewView.setClassName("icon-button");
        btnRemoveView = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE_O), evt -> {
            if (currentView != null)
                this.deleteTopologyViewVisualAction.getVisualComponent(new ModuleActionParameterSet( new ModuleActionParameter("viewId", currentView.getId()))).open();
        });
        btnRemoveView.setClassName("icon-button");
        btnRemoveView.setEnabled(false);
        setButtonTitle(btnRemoveView, ts.getTranslatedString("module.topoman.remove-view"));

        topologyView = new TopologyView(mem, aem, bem, ts, resourceFactory);
        topologyView.getMxgraphCanvas().setComObjectSelected(() -> {
                      
            String objectId = topologyView.getMxgraphCanvas().getSelectedCellId();
            if (MxGraphCell.PROPERTY_VERTEX.equals(topologyView.getMxgraphCanvas().getSelectedCellType())) {
                selectedObject = ((BusinessObjectViewNode) topologyView.getAsViewMap().findNode(objectId)).getIdentifier();             
            } else {
                 selectedObject = ((BusinessObjectViewEdge) topologyView.getAsViewMap().findEdge(objectId)).getIdentifier();            
            }
            updateProperties();
            setGeneralToolsEnabled(true);
            setSelectionToolsEnabled(true);
        });
        topologyView.getMxgraphCanvas().setComObjectUnselected(() -> {
            selectedObject = null;
            updateProperties();
            setSelectionToolsEnabled(false);
        });
        
        viewSearch = new TopologyViewSearch(ts, bem, new ArrayList(topologyView.getMxgraphCanvas().getNodes().keySet()),
                                     new ArrayList(topologyView.getMxgraphCanvas().getEdges().keySet()));
        viewSearch.addNewObjectListener(event -> {   
             BusinessObjectLight tmpObject = event.getObject();
             if (tmpObject == null)
                 return;
             addInventoryNodeToView(tmpObject, 100, 50);
             topologyView.syncViewMap();
        });
        btnSaveView = new Button(new Icon(VaadinIcon.DOWNLOAD), evt -> {
            saveCurrentView();
        });
        setButtonTitle(btnSaveView, ts.getTranslatedString("module.general.messages.save"));
        btnRemoveObjectFromView = new Button(new Icon(VaadinIcon.FILE_REMOVE),
                e -> {
                    deleteSelectedObject(); 
        }); 
        setButtonTitle(btnRemoveObjectFromView, ts.getTranslatedString("module.topoman.remove-object-from-view"));
        btnAddCloud = new Button(new Icon(VaadinIcon.CLOUD),
                e -> {
                    addIconNodeToView(URL_IMG_CLOUD);
        }); 
         setButtonTitle(btnAddCloud, ts.getTranslatedString("module.topoman.add-cloud"));
        btnAddRectShape = new Button(new Icon(VaadinIcon.THIN_SQUARE),
                e -> {
                    addShapeNodeToView(MxConstants.SHAPE_RECTANGLE);
        });
        setButtonTitle(btnAddRectShape, ts.getTranslatedString("module.topoman.add-rectangle"));
        btnAddEllipseShape = new Button(new Icon(VaadinIcon.CIRCLE_THIN),
                e -> {
                    addShapeNodeToView(MxConstants.SHAPE_ELLIPSE);
        }); 
        setButtonTitle(btnAddEllipseShape, ts.getTranslatedString("module.topoman.add-ellipse"));
        btnAddLabel = new Button(new Icon(VaadinIcon.TEXT_LABEL),
                e -> {
                    addShapeNodeToView(MxConstants.SHAPE_LABEL);
        }); 
        setButtonTitle(btnAddLabel, ts.getTranslatedString("module.topoman.add-label"));
        Button btnZoomIn = new Button(new Icon(VaadinIcon.PLUS), evt -> {
             topologyView.getMxgraphCanvas().getMxGraph().zoomIn();
        });
        btnZoomIn.getElement().setProperty("title", ts.getTranslatedString("module.visualization.rack-view-zoom-in"));
                
        Button btnZoomOut = new Button(new Icon(VaadinIcon.MINUS), evt -> {
             topologyView.getMxgraphCanvas().getMxGraph().zoomOut();
         });   
        btnZoomOut.getElement().setProperty("title", ts.getTranslatedString("module.visualization.rack-view-zoom-out"));
        Button btnToogleGrid = new Button(new Icon(VaadinIcon.GRID), evt -> {
             if (topologyView.getMxgraphCanvas().getMxGraph().getGrid() != null &&
                     topologyView.getMxgraphCanvas().getMxGraph().getGrid().isEmpty())
                topologyView.getMxgraphCanvas().getMxGraph().setGrid("images/grid.gif");
             else
                topologyView.getMxgraphCanvas().getMxGraph().setGrid("");
         }); 
        btnToogleGrid.getElement().setProperty("title", ts.getTranslatedString("module.topoman.show-hide-grid"));
        
        HorizontalLayout lytTools = new HorizontalLayout(btnNewView, btnOpenView, btnCopyView, 
                                    btnRemoveView, btnSaveView, viewSearch, btnRemoveObjectFromView,btnAddCloud,
                                    btnAddRectShape, btnAddEllipseShape, btnAddLabel, btnZoomIn, btnZoomOut, btnToogleGrid);
        lytTools.setAlignItems(Alignment.BASELINE);
        setGeneralToolsEnabled(false);
        setSelectionToolsEnabled(false);

        configureEdgeCreation();
        initializeActions();
        initializeTblViews();   

        VerticalLayout lytDashboard = new VerticalLayout(lytTools, topologyView.getAsComponent());
        lytDashboard.setWidth("80%");
               
        //properties  
        styleEditor = new BasicStyleEditor(ts);
        styleEditor.updateControlsVisibility(null);
        VerticalLayout lytProperties = new VerticalLayout(new H4("Properties"), styleEditor);
        lytProperties.setSpacing(false);
        setMarginPaddingLayout(lytProperties, false);
        lytProperties.setWidth("20%");

        HorizontalLayout lytMain = new HorizontalLayout(lytProperties, lytDashboard);
        lytMain.setSizeFull();
        setMarginPaddingLayout(lytMain, false);
        setSpacing(false);
        setMargin(false);
        addAndExpand(lytMain);
        setSizeFull();
    }

    private void updateProperties() {
        if (selectedObject != null) {
            if (selectedObject.getClassName().equals(FREE_SHAPE)) {
                MxGraphNode node = topologyView.getMxgraphCanvas().findMxGraphNode(selectedObject);
                if (node != null) {
                    styleEditor.update(node);
                }
            } else if (selectedObject.getClassName().equals("edge")) {
                MxGraphEdge edge = topologyView.getMxgraphCanvas().findMxGraphEdge(selectedObject);
                if (edge != null) {
                    styleEditor.update(edge);
                }
            }
        } else 
            styleEditor.update(null);
    }

    private void setMarginPaddingLayout(ThemableLayout lyt, boolean enable) {
        lyt.setMargin(enable);
        lyt.setPadding(enable);
    }

    /**
     * resets the topology view instance and creates a empty one
     */
    public void resetDashboard() {
        topologyView.buildEmptyView();
        if (currentView != null) {
            topologyView.buildWithSavedView(currentView.getStructure());
        }
    }

    /**
     * Save the current view in the canvas
     */
    private void saveCurrentView() {
        try {
            if (currentView != null) {
                aem.updateGeneralView(currentView.getId(), currentView.getName(), currentView.getDescription(), topologyView.getAsXml(), null);
                currentView.setStructure(topologyView.getAsXml());
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.topoman.view-saved")).open();
            }
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            Logger.getLogger(TopologyManagerDashboard.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error")).open();
        }

    }

    /**
     * Removes the selected object in the view.
     */
    private void deleteSelectedObject() {
        if (selectedObject != null) {
            if (MxGraphCell.PROPERTY_VERTEX.equals(topologyView.getMxgraphCanvas().getSelectedCellType())) {
                topologyView.removeNode(selectedObject);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.topoman.object-deleted")).open();
            } else {
                topologyView.removeEdge(selectedObject);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.topoman.link-deleted")).open();
            }
            selectedObject = null;
            setSelectionToolsEnabled(false);
        }
    }

    private void initializeTblViews() {
        loadViews();
        tblViews = new Grid<>();
        ListDataProvider<ViewObjectLight> dataProvider = new ListDataProvider<>(topologyViews);
        tblViews.setDataProvider(dataProvider);
        tblViews.addColumn(ViewObjectLight::getName).setFlexGrow(3).setKey(ts.getTranslatedString("module.general.labels.name"));
        tblViews.addComponentColumn(item -> createActionsColumn(item)).setKey("component-column");
        tblViews.addItemDoubleClickListener(listener -> {
            openTopologyView(listener.getItem());
        });
        HeaderRow filterRow = tblViews.appendHeaderRow();

        TextField txtViewNameFilter = new TextField(ts.getTranslatedString("module.general.labels.filter"), ts.getTranslatedString("module.general.labels.filter-placeholder"));
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
            openTopologyView(item);
        });
        btnEdit.setClassName("icon-button");

        lytActions.add(btnEdit);
        return lytActions;
    }

    /**
     * loads the given topology view into the view
     * @param item the topology view to be loaded
     */
    private void openTopologyView(ViewObjectLight item) {
        try {
            ViewObject view = aem.getGeneralView(item.getId());
            setCurrentView(view);
            if (wdwTopologyViews != null) {
                this.wdwTopologyViews.close();
            }
            selectedObject = null;
            setGeneralToolsEnabled(true);
            this.btnRemoveView.setEnabled(true);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.topoman.actions.view-loaded")).open();
        } catch (ApplicationObjectNotFoundException ex) {
            Logger.getLogger(TopologyManagerDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadViews() {
        try {
            topologyViews = aem.getGeneralViews(CLASS_VIEW, -1);
        } catch (InvalidArgumentException | NotAuthorizedException ex) {
            Logger.getLogger(TopologyManagerDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initialize the general actions that provides the functionalty to create 
     * and remove topology views 
     */
    private void initializeActions() {
        listenerDeleteAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            refreshTopologyViews();
            showActionCompledMessages(ev);
            setCurrentView(null);  
            selectedObject = null;
            setGeneralToolsEnabled(false);
            setSelectionToolsEnabled(false);
        };
        this.deleteTopologyViewVisualAction.registerActionCompletedLister(listenerDeleteAction);
        
        listenerNewViewAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            refreshTopologyViews();
            showActionCompledMessages(ev);
            btnRemoveView.setEnabled(true);
            if (wdwTopologyViews != null)
                wdwTopologyViews.close();
            selectedObject = null;
//            updatePropertySheet();
            ActionResponse response = ev.getActionResponse();
            try {
                ViewObject newView = aem.getGeneralView((long) response.get("viewId"));
                setCurrentView(newView);
                setGeneralToolsEnabled(true);
            } catch (ApplicationObjectNotFoundException ex) {
                Logger.getLogger(TopologyManagerDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }

        };
        this.newTopologyViewVisualAction.registerActionCompletedLister(listenerNewViewAction);        
    }

    private void refreshTopologyViews() {
        loadViews();
        tblViews.setItems(topologyViews);
        tblViews.getDataProvider().refreshAll();
    }
    /**
     * open the dialog that shows the list of available views.
     */
    private void openListTopologyViewDialog() {
        wdwTopologyViews = new Dialog();

        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), ev -> {
            wdwTopologyViews.close();
        });
        VerticalLayout lytContent = new VerticalLayout(tblViews, btnCancel);
        lytContent.setAlignItems(Alignment.CENTER);
        wdwTopologyViews.add(lytContent);
        wdwTopologyViews.setWidth("600px");
        wdwTopologyViews.open();
    }

    @Override
    public void updatePropertyChanged(AbstractProperty property) {
        try {
            if (selectedObject != null) {
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));

                bem.updateObject(selectedObject.getClassName(), selectedObject.getId(), attributes);
//                updatePropertySheet();
                saveCurrentView();

                //special case when the name is updated the label must be refreshed in the canvas
                if (property.getName().equals(Constants.PROPERTY_NAME)) {
                    if (MxGraphCell.PROPERTY_VERTEX.equals(topologyView.getMxgraphCanvas().getSelectedCellType())) {
                        topologyView.getMxgraphCanvas().getNodes().get(selectedObject).setLabel((String) property.getValue());
                    } else {
                        topologyView.getMxgraphCanvas().getEdges().get(selectedObject).setLabel((String) property.getValue());
                    }
                    topologyView.getMxgraphCanvas().getMxGraph().refreshGraph();
                }

                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update")).open();
            }
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ex.getLocalizedMessage()).open();
        }
    }

    /**
     * add a single node to the view
     *
     * @param node the node to be added
     */
    private void addInventoryNodeToView(BusinessObjectLight node, int x, int y) {
        if (topologyView.getAsViewMap().findNode(node) == null) {
            String uri = StreamResourceRegistry.getURI(resourceFactory.getClassIcon(node.getClassName())).toString();
            Properties props = new Properties();
            props.put("imageUrl", uri);
            props.put("x", x);
            props.put("y", y);
            addNode(props, node);
        } else {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.object-already-included")).open();
        }
    }
    
    /**
     * add a single icon node to the view
     *
     * @param node the node to be added
     */
    private void addIconNodeToView(String urlIcon) {
        BusinessObjectLight obj = new BusinessObjectLight(ICON, UUID.randomUUID().toString(), ICON);
        Properties props = new Properties();
        props.put("imageUrl", urlIcon);
        props.put("x", 50);
        props.put("y", 100);
        addNode(props, obj);
    }
    
     /**
     * add a single icon node to the view
     *
     * @param node the node to be added
     */
    private void addShapeNodeToView(String shape) {
        BusinessObjectLight obj = new BusinessObjectLight(FREE_SHAPE, UUID.randomUUID().toString(), FREE_SHAPE);
        Properties props = new Properties();
        props.put("x", 50);
        props.put("y", 100);
        props.put("shape", shape);
        addNode(props, obj);
    }
    
    /**
     * adds and edge with his nodes o the topology view
     * @param connection The link definition
     */
    private void addEdgeToView(String aSideId, String bSideId) {
       if (aSideId != null && bSideId != null) {
            Properties props = new Properties();
            BusinessObjectLight edge = new BusinessObjectLight("edge", aSideId + bSideId, "edge");
            BusinessObjectLight endPointA = new BusinessObjectLight("", aSideId, "");
            BusinessObjectLight endPointB = new BusinessObjectLight("", bSideId, "");
            props.put("controlPoints", new ArrayList());
            topologyView.addEdge(edge, endPointA, endPointB, props);
       }
    }

    private void addNode(Properties props, BusinessObjectLight node) {      
        topologyView.addNode(node, props);
    }

    private void copyCurrentView() {
        if (currentView != null) {            
            try {
                aem.createGeneralView(CLASS_VIEW, currentView.getName() + " Copy", currentView.getDescription(), currentView.getStructure(), null);
                refreshTopologyViews();
                new SimpleNotification("", String.format(ts.getTranslatedString("module.topoman.copy-created"), currentView.getName() + " Copy")).open();
            } catch (InvalidArgumentException ex) {
                Logger.getLogger(TopologyManagerDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void configureEdgeCreation() {
        topologyView.getMxgraphCanvas().getMxGraph().addEdgeCompleteListener(evt -> {
            addEdgeToView(evt.getSourceId(), evt.getTargetId());
        });
    }
    
    
    /**
     * Set the title/tool tip for the given button
     * @param button the button to be set
     * @param title the title to be added
     */
    public static void setButtonTitle(Button button, String title) {
        button.getElement().setProperty("title", title);     
    }

    private void setSelectionToolsEnabled(boolean b) {
        btnRemoveObjectFromView.setEnabled(b);
    }

    private void setGeneralToolsEnabled(boolean b) {
        btnRemoveView.setEnabled(b);
        btnCopyView.setEnabled(b);
        btnSaveView.setEnabled(b);
        btnAddCloud.setEnabled(b);
        btnAddRectShape.setEnabled(b);
        btnAddRectShape.setEnabled(b);
        btnAddEllipseShape.setEnabled(b);
        btnAddLabel.setEnabled(b);
        viewSearch.setEnabled(b);
    }

}
