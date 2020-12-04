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
package org.neotropic.kuwaiba.modules.optional.layouteditor.widgets;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraphCell;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.neotropic.flow.component.mxgraph.Point;
import com.neotropic.flow.component.mxgraph.Rectangle;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.text.html.StyleSheet;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.modules.optional.layouteditor.actions.DeleteLayoutViewVisualAction;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.kuwaiba.visualization.mxgraph.BasicStyleEditor;
import org.neotropic.kuwaiba.visualization.mxgraph.MxGraphGeometryEditor;
import org.neotropic.util.visual.slider.SliderMxGraphZoom;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.mxgraph.MxGraphCanvas;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.properties.StringProperty;
import org.neotropic.util.visual.views.util.UtilHtml;

/**
 * Layout editor Main Dashboard.
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
public class LayoutEditorDashboard extends VerticalLayout implements PropertySheet.IPropertyValueChangedListener {

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
     * listener to remove layout view action
     */
    private ActionCompletedListener listenerDeleteAction;
    /**
     * listener to add new layout view Action
     */
    private ActionCompletedListener listenerNewViewAction;
        /**
     * reference of the visual action to remove a layout view
     */
    private DeleteLayoutViewVisualAction deleteTopologyViewVisualAction;
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
     * list of topology views
     */
    private List<BusinessObjectLight> deviceLayouts;
    /**
     * Reference to the grid that shows the layout views
     */
    private Grid<BusinessObjectLight> tblViews;
    /**
     * Dialog that lists the whole list of the views
     */
    Dialog wdwLayoutViews;
    /**
     * reference to the current selected object in the canvas
     */
    private BusinessObjectLight selectedObject;
    /**
     * Prop Sheet for view properties
     */
    PropertySheet propSheetLayoutView;
    
    Button btnSaveView;
        
    Button btnRemoveObjectFromView;
        
    Button btnAddRectShape;
    
    Button btnAddEllipseShape;
    
    Button btnAddLabel; 

    PhysicalConnectionsService connectionsService;
    
    VerticalLayout lytObjectProperties;
    
    BasicStyleEditor styleEditor;
    
    MxGraphGeometryEditor geometryEditor;
    
    Accordion accordionProperties;
    
    MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> mxGraphCanvas;
    
    BusinessObjectLight currentListTypeItem;
    
    /**
     * Reference to the grid that shows the custom layouts to be added in a device layout
     */
    private Grid<BusinessObjectLight> tblCustomShapes;
    /**
     * Reference to the grid that opens the custom layouts
     */
    private Grid<BusinessObjectLight> tblEditCustomShapes;
    
    /**
     * list of topology views
     */
    private List<BusinessObjectLight> customShapes;
     /*
    map to store the device with their respective layout
     */
    private HashMap<BusinessObject, byte[]> layoutDevices;
    
    public static String PROPERTY_TYPE = "type"; //NOI18N
    public static String PROPERTY_NAME = "name"; //NOI18N 
    public static String PROPERTY_X = "x"; //NOI18N
    public static String PROPERTY_Y = "y"; //NOI18N
    public static String PROPERTY_WIDTH = "width"; //NOI18N
    public static String PROPERTY_HEIGHT = "height"; //NOI18N
    public static String PROPERTY_COLOR = "color"; //NOI18N
    public static String PROPERTY_BORDER_WIDTH = "borderWidth"; //NOI18N
    public static String PROPERTY_BORDER_COLOR = "borderColor"; //NOI18N
    public static String PROPERTY_IS_EQUIPMENT = "isEquipment"; //NOI18N
    public static String PROPERTY_OPAQUE = "opaque"; //NOI18N
    public static String SHAPE_CUSTOM = "custom";
    public static String SHAPE_RECTANGLE = "rectangle";
    public static String SHAPE_POLYGON = "polygon";
    public static String SHAPE_ELLIPSE = "ellipse";
    public static String SHAPE_LABEL = "label";
    public static final String PROPERTY_IS_SLOT = "isSlot";
    public static String PROPERTY_ELLIPSE_COLOR = "ellipseColor"; //NOI18N
    public static String PROPERTY_OVAL_COLOR = "ovalColor"; //NOI18N
    public static String PROPERTY_NUM_OF_SIDES = "numberOfSides"; //NOI18N
    public static String PROPERTY_OUTLINE_COLOR = "outlineColor"; //NOI18N
    public static String PROPERTY_INTERIOR_COLOR = "interiorColor"; //NOI18N
    public static int UNIT_WIDTH = 1086 * 3; //NOI18N
    public static int UNIT_HEIGHT = 100 * 3;//NOI18N
    public static String NODE_GUIDE = "NODE_GUIDE";
    public static String CLASS_CUSTOM = "CustomShape";
    public static String INNER_SHAPE = "innerShape";
    
    public ViewObject getCurrentView() {
        return currentView;
    }

    public LayoutEditorDashboard(TranslationService ts, MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem,
            ResourceFactory resourceFactory, DeleteLayoutViewVisualAction deleteTopologyViewVisualAction) {
        super(); 
        this.ts = ts;
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
        this.resourceFactory = resourceFactory;
        this.deleteTopologyViewVisualAction = deleteTopologyViewVisualAction;
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setMargin(false);
        mxGraphCanvas = new MxGraphCanvas<>();
        mxGraphCanvas.getMxGraph().setGrid("img/grid.gif");
        layoutDevices = new HashMap<>();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        createContent();
    }

    @Override
    public void onDetach(DetachEvent ev) {
        this.deleteTopologyViewVisualAction.unregisterListener(listenerDeleteAction);
    }

    public void showActionCompledMessages(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
    }

    private void createContent() {
        
        buildCanvasSkeleton();

        Button btnOpenView = new Button(new Icon(VaadinIcon.FOLDER_OPEN_O), ev -> {
            openDeviceLayoutsListDialog();
        });
        setButtonTitle(btnOpenView, ts.getTranslatedString("Open device Layout"));
        btnOpenView.setClassName("icon-button");
        
         Button btnEditCustomShape = new Button(new Icon(VaadinIcon.COMPILE), ev -> {
            openCustomShapesListDialog();
        });
        setButtonTitle(btnEditCustomShape, ts.getTranslatedString("Open Custom Shape"));
        btnEditCustomShape.setClassName("icon-button");

        mxGraphCanvas.setComObjectSelected(() -> {
                      
            String objectId = mxGraphCanvas.getSelectedCellId();
            if (MxGraphCell.PROPERTY_VERTEX.equals(mxGraphCanvas.getSelectedCellType()) && !objectId.startsWith("*")) {
                selectedObject =  mxGraphCanvas.getNodes().keySet().stream().filter(item -> item.getId().equals(objectId) ).findAny().get();
                setGeneralToolsEnabled(true);
                setSelectionToolsEnabled(true);
            } else {
                selectedObject = null;
                setSelectionToolsEnabled(false);
            }
                
            updateShapeProperties();          
        });
        mxGraphCanvas.setComObjectUnselected(() -> {
            selectedObject = null;
            updateShapeProperties();
            setSelectionToolsEnabled(false);
        });
             
        btnSaveView = new Button(new Icon(VaadinIcon.DOWNLOAD), evt -> {
            saveCurrentView();
        });
        btnSaveView.setClassName("icon-button");
        setButtonTitle(btnSaveView, ts.getTranslatedString("module.general.messages.save"));
        btnRemoveObjectFromView = new Button(new Icon(VaadinIcon.FILE_REMOVE),
                e -> {
                    deleteSelectedObject(); 
        });
        btnRemoveObjectFromView.setClassName("icon-button");
        setButtonTitle(btnRemoveObjectFromView, ts.getTranslatedString("module.topoman.remove-object-from-view"));
        
        btnAddRectShape = new Button(new Icon(VaadinIcon.THIN_SQUARE),
                e -> {
                    addShapeNodeToView(MxConstants.SHAPE_RECTANGLE);
        });
        btnAddRectShape.setClassName("icon-button");
        setButtonTitle(btnAddRectShape, ts.getTranslatedString("module.topoman.add-rectangle"));
        btnAddEllipseShape = new Button(new Icon(VaadinIcon.CIRCLE_THIN),
                e -> {
                    addShapeNodeToView(MxConstants.SHAPE_ELLIPSE);
        }); 
        btnAddEllipseShape.setClassName("icon-button");
        setButtonTitle(btnAddEllipseShape, ts.getTranslatedString("module.topoman.add-ellipse"));
        btnAddLabel = new Button(new Icon(VaadinIcon.TEXT_LABEL),
                e -> {
                    addShapeNodeToView(MxConstants.SHAPE_LABEL);
        }); 
        btnAddLabel.setClassName("icon-button");
        setButtonTitle(btnAddLabel, ts.getTranslatedString("module.topoman.add-label"));
              
        Button btnToogleGrid = new Button(new Icon(VaadinIcon.GRID), evt -> {
             if (mxGraphCanvas.getMxGraph().getGrid() != null &&
                     mxGraphCanvas.getMxGraph().getGrid().isEmpty())
                mxGraphCanvas.getMxGraph().setGrid("images/grid.gif");
             else
                mxGraphCanvas.getMxGraph().setGrid("");
         }); 
        btnToogleGrid.setClassName("icon-button");
        btnToogleGrid.getElement().setProperty("title", ts.getTranslatedString("module.topoman.show-hide-grid"));
        
        SliderMxGraphZoom sliderZoom = new SliderMxGraphZoom(mxGraphCanvas.getMxGraph());
        HorizontalLayout lytTools = new HorizontalLayout(btnOpenView, btnEditCustomShape,
                                    btnSaveView, btnRemoveObjectFromView,
                                    btnAddRectShape, btnAddEllipseShape, btnAddLabel, 
                                    btnToogleGrid, sliderZoom);
        lytTools.setAlignItems(Alignment.CENTER);

        initializeActions();
        initializeTblViews();   

        VerticalLayout lytDashboard = new VerticalLayout(lytTools, mxGraphCanvas.getMxGraph());
        setMarginPaddingLayout(lytDashboard, false);
        lytDashboard.setSpacing(false);
        lytDashboard.setWidth("75%");
                        
        PropertySheet.IPropertyValueChangedListener listenerPropSheetTopoView = new PropertySheet.IPropertyValueChangedListener() {
            @Override
            public void updatePropertyChanged(AbstractProperty property) {
                if (currentView != null) {
                    
                    if (property.getName().equals(Constants.PROPERTY_NAME))
                        currentView.setName(property.getAsString());
                    if (property.getName().equals(Constants.PROPERTY_DESCRIPTION))
                        currentView.setDescription(property.getAsString());                  
                    saveCurrentView();                  
                }
            }
        };
        propSheetLayoutView = new PropertySheet(ts, new ArrayList<>(), "");
        propSheetLayoutView.addPropertyValueChangedListener(listenerPropSheetTopoView);
        
        accordionProperties = new Accordion();
        accordionProperties.setWidthFull();
        
        loadCustomShapes();
        initializeTblCustomShapes();
        initializeTblEditCustomShapes();
          
        BoldLabel lblViewProperties = new BoldLabel(ts.getTranslatedString("Custom Shapes"));
        lblViewProperties.addClassName("lbl-accordion");
        HorizontalLayout lytSummaryViewProp = new HorizontalLayout(lblViewProperties); 
        lytSummaryViewProp.setWidthFull();       
        AccordionPanel apViewProp = new AccordionPanel(lytSummaryViewProp, tblCustomShapes);
        accordionProperties.add(apViewProp);
  
        styleEditor = new BasicStyleEditor(ts, new ArrayList(Arrays.asList(
                          MxConstants.STYLE_STROKECOLOR, MxConstants.STYLE_FILLCOLOR,
                          MxConstants.STYLE_FONTSIZE, MxConstants.STYLE_FONTCOLOR)));
        styleEditor.updateControlsVisibility(null);
        geometryEditor = new MxGraphGeometryEditor(ts);
        geometryEditor.updateControlsVisibility(null);
        VerticalLayout lytProperties = new VerticalLayout(styleEditor, geometryEditor);
        lytProperties.setPadding(false);
        lytProperties.setMargin(false);
        lytProperties.setSpacing(false);
                
        BoldLabel lblStyleEditor = new BoldLabel(ts.getTranslatedString("module.topoman.shape-properties"));
        lblStyleEditor.addClassName("lbl-accordion");
        HorizontalLayout lytSummaryStyleEditor = new HorizontalLayout(lblStyleEditor); 
        lytSummaryStyleEditor.setWidthFull();       
        AccordionPanel apStyleEditor = new AccordionPanel(lytSummaryStyleEditor, lytProperties);
        accordionProperties.add(apStyleEditor);
        
        Label lblHelp = new Label(ts.getTranslatedString("module.topoman.help"));
        lblHelp.addClassName("lbl-accordion");
        HorizontalLayout lytSummaryHelp = new HorizontalLayout(lblHelp); 
        lytSummaryHelp.setWidthFull();           
        AccordionPanel apHelp = new AccordionPanel(lytSummaryHelp, new Label());
        accordionProperties.add(apHelp);

        HorizontalLayout lytMain = new HorizontalLayout(accordionProperties, lytDashboard);
        lytMain.setSizeFull();
        setMarginPaddingLayout(lytMain, false);
        setSpacing(false);
        setMargin(false);
        addAndExpand(lytMain);
        setSizeFull();
        
        setGeneralToolsEnabled(false);
        setSelectionToolsEnabled(false);
    }

    private void updateShapeProperties() {
        if (selectedObject != null) {
            if (!selectedObject.getId().startsWith("*")) {
                MxGraphNode node = mxGraphCanvas.findMxGraphNode(selectedObject);
                if (node != null) {
                    styleEditor.update(node);
                    geometryEditor.update(node);
                    accordionProperties.open(1);
                }
            } else {
                styleEditor.update(null); 
                geometryEditor.update(null);
            }
        } else {
            styleEditor.update(null);
            geometryEditor.update(null);        
        }      
    }

    private void setMarginPaddingLayout(ThemableLayout lyt, boolean enable) {
        lyt.setMargin(enable);
        lyt.setPadding(enable);
    }

    /**
     * resets the layout view instance and creates a empty one
     */
    public void resetDashboard() {
        List<BusinessObjectLight> objectsToRemove = mxGraphCanvas.getNodes().keySet().stream().filter(node -> !NODE_GUIDE.equals(node.getClassName())).collect(Collectors.toList());
        for (BusinessObjectLight obj : objectsToRemove)
            mxGraphCanvas.removeNode(obj);
    }

    /**
     * Save the current view in the canvas
     */
    private void saveCurrentView() {
        try {
            if (currentView != null) {
                byte [] structure = getAsXML();
//                currentModel.get
                aem.updateListTypeItemRelatedView(currentListTypeItem.getId(), currentListTypeItem.getClassName(), currentView.getId(), currentView.getName(), currentView.getDescription(), structure, null);
                currentView.setStructure(structure);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("View Saved"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            }
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
            Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Removes the selected object in the view.
     */
    private void deleteSelectedObject() {
        if (selectedObject != null) {
            if (MxGraphCell.PROPERTY_VERTEX.equals(mxGraphCanvas.getSelectedCellType())) {
                mxGraphCanvas.removeNode(selectedObject);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.topoman.object-deleted"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            } 
            selectedObject = null;
            setSelectionToolsEnabled(false);
        }
    }

    private void initializeTblViews() {
        loadViews();
        tblViews = new Grid<>();
        ListDataProvider<BusinessObjectLight> dataProvider = new ListDataProvider<>(deviceLayouts);
        tblViews.setDataProvider(dataProvider);
        tblViews.addColumn(BusinessObjectLight::toString).setFlexGrow(3).setKey(ts.getTranslatedString("module.general.labels.name"));
        tblViews.addItemClickListener(listener -> {
            openDeviceLayout(listener.getItem());
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
    
    private void initializeTblCustomShapes() {
        tblCustomShapes = new Grid<>();
        ListDataProvider<BusinessObjectLight> dataProvider = new ListDataProvider<>(customShapes);
        tblCustomShapes.setDataProvider(dataProvider);
        tblCustomShapes.addColumn(BusinessObjectLight::toString).setFlexGrow(3).setKey(ts.getTranslatedString("module.general.labels.name"));
        tblCustomShapes.addComponentColumn(item -> createActionsColumnTblCustomLayouts(item)).setKey("component-column");
        HeaderRow filterRow = tblCustomShapes.appendHeaderRow();

        TextField txtViewNameFilter = new TextField(ts.getTranslatedString("module.general.labels.filter"), ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtViewNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
        txtViewNameFilter.setWidthFull();
        txtViewNameFilter.addValueChangeListener(event -> dataProvider.addFilter(
                project -> StringUtils.containsIgnoreCase(project.getName(),
                        txtViewNameFilter.getValue())));

        filterRow.getCell(tblCustomShapes.getColumnByKey(ts.getTranslatedString("module.general.labels.name"))).setComponent(txtViewNameFilter);
    }
    
    private void initializeTblEditCustomShapes() {
        tblEditCustomShapes = new Grid<>();
        ListDataProvider<BusinessObjectLight> dataProvider = new ListDataProvider<>(customShapes);
        tblEditCustomShapes.setDataProvider(dataProvider);
        tblEditCustomShapes.addColumn(BusinessObjectLight::toString).setFlexGrow(3).setKey(ts.getTranslatedString("module.general.labels.name"));
        tblEditCustomShapes.addItemClickListener(listener -> openCustomShape(listener.getItem()));
        HeaderRow filterRow = tblEditCustomShapes.appendHeaderRow();

        TextField txtViewNameFilter = new TextField(ts.getTranslatedString("module.general.labels.filter"), ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtViewNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
        txtViewNameFilter.setWidthFull();
        txtViewNameFilter.addValueChangeListener(event -> dataProvider.addFilter(
                project -> StringUtils.containsIgnoreCase(project.getName(),
                        txtViewNameFilter.getValue())));

        filterRow.getCell(tblEditCustomShapes.getColumnByKey(ts.getTranslatedString("module.general.labels.name"))).setComponent(txtViewNameFilter);
    }
    
       private HorizontalLayout createActionsColumnTblCustomLayouts(BusinessObjectLight item) {
        HorizontalLayout lytActions = new HorizontalLayout();

        Button btnAdd = new Button(new Icon(VaadinIcon.PLUS_CIRCLE_O), evt -> {
            addCustomShape(item);
        });
        btnAdd.setClassName("icon-button");

        lytActions.add(btnAdd);
        return lytActions;
    }

    /**
     * loads the given layout view into the view
     * @param item the layout view to be loaded
     */
    private void openDeviceLayout(BusinessObjectLight item) {
        try {
            TemplateObject theTemplate = aem.getTemplateElement(item.getClassName(), item.getId());
            String listTypeItemId =  theTemplate.getAttributes().get(Constants.ATTRIBUTE_MODEL);
            ClassMetadata templateClass = mem.getClass(theTemplate.getClassName());
            String type = templateClass.getAttribute(Constants.ATTRIBUTE_MODEL).getType();
            currentListTypeItem = aem.getListTypeItem(type, listTypeItemId);
            List<ViewObjectLight> views = aem.getListTypeItemRelatedViews(currentListTypeItem.getId(), type, 1);

            if (!views.isEmpty()) {
                currentView = aem.getListTypeItemRelatedView(currentListTypeItem.getId(), type, views.get(0).getId());
            } else 
                return;
            resetDashboard();
            byte[] deviceStructure = currentView.getStructure();
            try {
                FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/LAYOUT_OPEN" + currentView.getId() + ".xml");
                fos.write(deviceStructure);
                fos.close();
            } catch (IOException e) {
            }
            renderShape(item, deviceStructure, UNIT_WIDTH, UNIT_HEIGHT, mxGraphCanvas.getNodes().get(new BusinessObjectLight("", "*main", "")), false);
            
            if (wdwLayoutViews != null) {
                this.wdwLayoutViews.close();
            }
            selectedObject = null;
            updatePropertySheetView();
            updateShapeProperties();
            accordionProperties.open(0);
            setGeneralToolsEnabled(true);
            tblCustomShapes.setVisible(true);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("Layout Loaded"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
            Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * loads the given custom shape into the view
     * @param item the layout view to be loaded
     */
    private void openCustomShape(BusinessObjectLight item) {
        try {
            currentListTypeItem =  item;
            List<ViewObjectLight> views = aem.getListTypeItemRelatedViews(currentListTypeItem.getId(), currentListTypeItem.getClassName(), 1);

            if (!views.isEmpty()) {
                currentView = aem.getListTypeItemRelatedView(currentListTypeItem.getId(), currentListTypeItem.getClassName(), views.get(0).getId());
            } else 
                return;
            resetDashboard();
            byte[] deviceStructure = currentView.getStructure();
           
            renderShape(item, deviceStructure, UNIT_WIDTH, UNIT_HEIGHT, mxGraphCanvas.getNodes().get(new BusinessObjectLight("", "*main", "")), false);
            
            if (wdwLayoutViews != null) {
                this.wdwLayoutViews.close();
            }
            selectedObject = null;
            updatePropertySheetView();
            updateShapeProperties();
            accordionProperties.open(0);
            setGeneralToolsEnabled(true);
            tblCustomShapes.setVisible(false);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("Layout Loaded"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
            Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * loads the given layout view into the view
     * @param item the layout view to be loaded
     */
    private void addCustomShape(BusinessObjectLight item) {
        try {
            List<ViewObjectLight> views = aem.getListTypeItemRelatedViews(item.getId(), CLASS_CUSTOM, 1);
            ViewObject shapeView;
            if (!views.isEmpty()) {
                shapeView = aem.getListTypeItemRelatedView(item.getId(), CLASS_CUSTOM, views.get(0).getId());
            } else 
                return;
            byte[] deviceStructure = shapeView.getStructure();
            try {
                FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/CUSTOM_SHAPE_ADD" + currentView.getId() + ".xml");
                fos.write(deviceStructure);
                fos.close();
            } catch (IOException e) {
            }
            
            ByteArrayInputStream bais = new ByteArrayInputStream(deviceStructure);
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
            
            QName tagLayout = new QName("layout"); //NOI18N
            String attrValue;

            int layoutWidth = 0, layoutHeight = 0;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(tagLayout)) {

                        attrValue = reader.getAttributeValue(null, "width"); //NOI18N
                        if (attrValue != null) {
                            layoutWidth = Integer.valueOf(attrValue);
                        }
                        attrValue = reader.getAttributeValue(null, "height"); //NOI18N
                        if (attrValue != null) {
                            layoutHeight = Integer.valueOf(attrValue);
                        }    
                    }
                }
            }
            
            MxGraphNode nodeShape = new MxGraphNode();
            String nodeUuid = UUID.randomUUID().toString();
            nodeShape.setUuid(nodeUuid);
            nodeShape.setCellParent("*main");
            nodeShape.setGeometry(50, 50, layoutWidth, layoutHeight);
            mxGraphCanvas.addNode(new BusinessObjectLight((SHAPE_CUSTOM), nodeUuid, item.getId()), nodeShape);                                 
                                    
            renderShape(item, deviceStructure, UNIT_WIDTH, UNIT_HEIGHT, nodeShape, true);

//            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.topoman.actions.view-loaded"), 
//                            AbstractNotification.NotificationType.INFO, ts).open();
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException | XMLStreamException ex) {
            Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void loadViews() {
        try {
            deviceLayouts = aem.getDeviceLayouts();
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
     public void loadCustomShapes() {
        try {
            customShapes = aem.getListTypeItems(CLASS_CUSTOM);
        } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
            Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
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
            currentView = null;
            resetDashboard();
            selectedObject = null;
            setGeneralToolsEnabled(true);
            setSelectionToolsEnabled(false);
        };
        this.deleteTopologyViewVisualAction.registerActionCompletedLister(listenerDeleteAction);
               
    }

    private void refreshTopologyViews() {
        loadViews();
        tblViews.setItems(deviceLayouts);
        tblViews.getDataProvider().refreshAll();
    }
    /**
     * open the dialog that shows the list of available views.
     */
    private void openDeviceLayoutsListDialog() {
        wdwLayoutViews = new Dialog();

        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), ev -> {
            wdwLayoutViews.close();
        });
        VerticalLayout lytContent = new VerticalLayout(tblViews, btnCancel);
        lytContent.setAlignItems(Alignment.CENTER);
        wdwLayoutViews.add(lytContent);
        wdwLayoutViews.setWidth("600px");
        wdwLayoutViews.open();
    }
    
     /**
     * open the dialog that shows the list of available views.
     */
    private void openCustomShapesListDialog() {
        wdwLayoutViews = new Dialog();

        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), ev -> {
            wdwLayoutViews.close();
        });
        VerticalLayout lytContent = new VerticalLayout(tblEditCustomShapes, btnCancel);
        lytContent.setAlignItems(Alignment.CENTER);
        wdwLayoutViews.add(lytContent);
        wdwLayoutViews.setWidth("600px");
        wdwLayoutViews.open();
    }
    
    private void updatePropertySheetView() {
        if (currentView != null) {
            ArrayList<AbstractProperty> viewProperties = new ArrayList<>();
            viewProperties.add(new StringProperty(Constants.PROPERTY_NAME,
                    Constants.PROPERTY_NAME, "", currentView.getName(), ts));
            viewProperties.add(new StringProperty(Constants.PROPERTY_DESCRIPTION,
                    Constants.PROPERTY_DESCRIPTION, "", currentView.getDescription(), ts));
            propSheetLayoutView.setItems(viewProperties);
        } else
            propSheetLayoutView.clear();
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
                    if (MxGraphCell.PROPERTY_VERTEX.equals(mxGraphCanvas.getSelectedCellType())) {
                        mxGraphCanvas.getNodes().get(selectedObject).setLabel((String) property.getValue());
                    } else {
                        mxGraphCanvas.getEdges().get(selectedObject).setLabel((String) property.getValue());
                    }
                    mxGraphCanvas.getMxGraph().refreshGraph();
                }

                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            }
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
      
     /**
     * add a single icon node to the view
     *
     * @param node the node to be added
     */
    private void addShapeNodeToView(String shape) {
        BusinessObjectLight obj = new BusinessObjectLight(shape, UUID.randomUUID().toString(), shape);
        Properties props = new Properties();
        props.put("x", 50);
        props.put("y", 50);
        props.put("shape", shape);
        if (shape.equals(MxConstants.SHAPE_LABEL))
            props.put("label", "New Label");        
        addNode(obj, props);
    }
 
     public void addNode(BusinessObjectLight businessObject, Properties properties) {
   
                int x = (int) properties.get("x");
                int y = (int) properties.get("y");
                Integer width = (Integer) properties.get("w");
                Integer height = (Integer) properties.get("h");
                String urlImage = (String) properties.get("imageUrl");
                String shape = (String) properties.get("shape");
                String label = (String) properties.get("label");

                MxGraphNode newMxNode = new MxGraphNode();
                newMxNode.setUuid(businessObject.getId());
                newMxNode.setLabel(label);
                newMxNode.setWidth(width == null ? ((int) Constants.DEFAULT_ICON_WIDTH) : width);
                newMxNode.setHeight(height == null ? ((int) Constants.DEFAULT_ICON_HEIGHT) : height);
                newMxNode.setX(x);
                newMxNode.setY(y);
                if (urlImage == null) {// is a Free shape 
                    newMxNode.setStrokeColor("#000000");
                    newMxNode.setFillColor(MxConstants.NONE);
                    newMxNode.setShape(shape);
                    LinkedHashMap<String, String> mapStyle = new LinkedHashMap();
                    for (String style : BasicStyleEditor.supportedNodeStyles) {
                        String prop = (String) properties.get(style);
                        if ( prop != null && !prop.isEmpty() )
                           mapStyle.put(style, prop);
                    }
                    newMxNode.setRawStyle(mapStyle);
                    newMxNode.addCellAddedListener(eventListener -> {
//                       newMxNode.setSelfPosition(0);
                       mxGraphCanvas.getMxGraph().refreshGraph();
                    });
                } else {
                    newMxNode.setShape(MxConstants.SHAPE_IMAGE);
                    newMxNode.setImage(urlImage);
                    newMxNode.setIsResizable(false);
                }

             mxGraphCanvas.addNode(businessObject, newMxNode);
           
       
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
        btnSaveView.setEnabled(b);
        btnAddRectShape.setEnabled(b);
        btnAddRectShape.setEnabled(b);
        btnAddEllipseShape.setEnabled(b);
        btnAddLabel.setEnabled(b);
        tblCustomShapes.setEnabled(b);
    }

    private void buildCanvasSkeleton() { 
//11.034482759
//        int rackUnitWidth = 1086 * 3;  Desktop sizes
//        int rackUnitHeight = 100 * 3;
//        int spanHeight = 15;
        int unitHeight = UNIT_HEIGHT, unitWidth = UNIT_WIDTH, deviceRackUnits, deviceRackPosition, currentRackUnitPosition = 0, currentRackUnitSize = 0;
        int rackUnits = 5;
        MxGraphNode rackUnit = new MxGraphNode();
        MxGraphNode deviceNode = null;
        MxGraphNode rackNode = new MxGraphNode();
        MxGraphNode mainBox = new MxGraphNode();
        mainBox.setUuid("*main");
        mainBox.setLabel("");
        mainBox.setGeometry(0, 0, unitWidth, unitHeight * rackUnits);
        mainBox.setIsResizable(false);
        mainBox.setIsMovable(false);
        mainBox.setFillColor(MxConstants.NONE);

 
        rackNode.setUuid("*rackNode");
        rackNode.setCellParent(mainBox.getUuid());
        rackNode.setLabel("");
        rackNode.setGeometry(0, 0, unitWidth, unitHeight * rackUnits);  
        rackNode.setIsResizable(false);
        rackNode.setIsMovable(false);
        rackNode.setFillColor(MxConstants.NONE);
        // added "*" to differentiate the nodes of the device layout with the nodes of the skeleton-guide

        mxGraphCanvas.addNode(new BusinessObjectLight(NODE_GUIDE, "*main", ""), mainBox);
        
        MxGraphNode nodeNumber;
        MxGraphNode nodeUnitNumbers = new MxGraphNode();
        nodeUnitNumbers.setUuid("*nodeNumbers");
        nodeUnitNumbers.setGeometry(50, 50, 50, unitHeight * rackUnits);
        nodeUnitNumbers.setCellParent(mainBox.getUuid());
//        mxGraphCanvas.addNode(new BusinessObjectLight("", UUID.randomUUID().toString(), ""), nodeUnitNumbers);
        mxGraphCanvas.addNode(new BusinessObjectLight(NODE_GUIDE, "*rackNode" , ""), rackNode);

        for (int i = 0; i < rackUnits; i++) {                    
            nodeNumber = new MxGraphNode();
            nodeNumber.setUuid("*nodeNumber" + i);
            nodeNumber.setLabel((i+1) + "");
            nodeNumber.setGeometry(0, i * unitHeight, 50, unitHeight);
            nodeNumber.setCellParent("nodeNumbers");
            nodeNumber.setVerticalLabelPosition(MxConstants.ALIGN_CENTER);
//            mxGraphCanvas.addNode(new BusinessObjectLight(NODE_GUIDE, UUID.randomUUID().toString(), ""), nodeNumber);
            
            rackUnit = new MxGraphNode();
            rackUnit.setUuid("*rackUnit" + i);
            rackUnit.setLabel("");
            rackUnit.setGeometry(0, i * unitHeight, unitWidth, unitHeight);
            rackUnit.setCellParent(rackNode.getUuid());
            rackUnit.setIsResizable(false);
            rackUnit.setIsMovable(false);
            rackUnit.setFillColor(MxConstants.NONE);
//            rackUnit.setFillColor("white");
            mxGraphCanvas.addNode(new BusinessObjectLight(NODE_GUIDE, "*rackUnit" + i, ""), rackUnit);

            if (i == (rackUnits - 1)) {
                nodeNumber.addCellAddedListener(eventListener -> {
//                    mxGraphCanvas.getMxGraph().executeStackLayout(nodeUnitNumbers.getUuid(), false, 0);
                    mxGraphCanvas.getMxGraph().executeStackLayout(rackNode.getUuid(), false, 15);
                });
            }
        }
              
        
    }
    
    public byte[] getAsXML() {
        try {
            double propY =  1, propX =  1,
                    propSize = 1; 
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName tagView = new QName("view"); //NOI18N
            xmlew.add(xmlef.createStartElement(tagView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), Constants.VIEW_FORMAT_VERSION)); //NOI18N
            
            QName tagLayout = new QName("layout"); //NOI18N
            xmlew.add(xmlef.createStartElement(tagLayout, null, null));
            
            List<MxGraphNode> deviceLayoutNodes = mxGraphCanvas.getNodes().values().stream().filter(node -> !node.getUuid().startsWith("*")).collect(Collectors.toList());
            Rectangle layoutBounds = getLayoutBounds(deviceLayoutNodes);
            
            xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString((int) (layoutBounds.getX() / propX)))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString((int) (layoutBounds.getY() / propY)))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("width"), Integer.toString((int) (layoutBounds.getWidth() / propSize)))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("height"), Integer.toString((int) (layoutBounds.getHeight() / propSize)))); //NOI18N
                       
            mxGraphCanvas.getNodes().forEach((objectNode, MxNode) -> {

                if (objectNode.getClassName().equals(NODE_GUIDE) || objectNode.getClassName().equals(INNER_SHAPE))
                        return;
                try {
                    QName tagShape = new QName("shape"); //NOI18N
                    xmlew.add(xmlef.createStartElement(tagShape, null, null));

                    String shapeType = objectNode.getClassName();

                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_TYPE), shapeType));
                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_X), Integer.toString((int) ((MxNode.getX() - layoutBounds.getX()) / propX))));
                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_Y), Integer.toString((int) ((MxNode.getY() - layoutBounds.getY()) / propY))));
                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_WIDTH), Integer.toString((int) (MxNode.getWidth() / propSize))));
                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_HEIGHT), Integer.toString((int) (MxNode.getHeight() / propSize))));
                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_OPAQUE), shapeType.equals(MxConstants.SHAPE_RECTANGLE) ? "true" : "false"));
                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_IS_EQUIPMENT), "false"));
                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_NAME), ""));

                    HashMap mapStyle = MxNode.getRawStyleAsMap();
                    StyleSheet s = new StyleSheet();
                    if ("container".equals(shapeType)) {
//
//                    List<Shape> shapesSet = ((ContainerShapeWidget) child).getShapesSet();
//                    for (Shape innerShape : shapesSet) {
//                        Widget innerShapeWidget = findWidget(innerShape);
//                        
//                        if (innerShapeWidget == null)
//                            continue;
//                        
//                        int index = nodeLayer.getChildren().indexOf(innerShapeWidget);
//                        
//                        QName qnameChild = new QName("child"); //NOI18N
//                        xmlew.add(xmlef.createStartElement(qnameChild, null, null));
//                        xmlew.add(xmlef.createAttribute(new QName("index"), Integer.toString(index))); //NOI18N                        
//                        xmlew.add(xmlef.createEndElement(qnameChild, null));
//                    }
                    } else if (SHAPE_CUSTOM.equals(shapeType)) {
                        xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_ID), objectNode.getName()));
                        xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_CLASSNAME), CLASS_CUSTOM));

                    } else {
                        s = new StyleSheet();
                        String fillColor = (String) mapStyle.get(MxConstants.STYLE_FILLCOLOR);
                        Color clrFillColor = s.stringToColor(fillColor.equals(MxConstants.NONE) ? "#ffffff00" : fillColor);
//                    clrFillColor = Color.decode(fillColor.equals(MxConstants.NONE) ? "#ffffff00" : fillColor) ;
                        xmlew.add(xmlef.createAttribute(new QName(PROPERTY_COLOR), clrFillColor.getRGB() + ""));
                        String strokeColor = (String) mapStyle.get(MxConstants.STYLE_STROKECOLOR);
                        Color clrStrokeColor;// = Color.decode(strokeColor.equals(MxConstants.NONE) ? "#000000" : fillColor) ;
                        clrStrokeColor = s.stringToColor(strokeColor.equals(MxConstants.NONE) ? "#000000" : strokeColor);
                        xmlew.add(xmlef.createAttribute(new QName(PROPERTY_BORDER_COLOR), clrStrokeColor.getRGB() + ""));
//                    xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_IS_EQUIPMENT), Boolean.toString(shape.isEquipment())));

                        if (SHAPE_RECTANGLE.equals(shapeType)) {
                            xmlew.add(xmlef.createAttribute(new QName(PROPERTY_IS_SLOT), "false"));
                        } else if (SHAPE_LABEL.equals(shapeType)) {
                            xmlew.add(xmlef.createAttribute(new QName("label"), MxNode.getLabel())); //NOI18N
                            Color fontColor = s.stringToColor((String) mapStyle.get(MxConstants.STYLE_FONTCOLOR));
                            xmlew.add(xmlef.createAttribute(new QName("textColor"), fontColor.getRGB() + "")); //NOI18N
                            xmlew.add(xmlef.createAttribute(new QName("fontSize"), Math.round(new Double((String) mapStyle.get(MxConstants.STYLE_FONTSIZE))) + "")); //NOI18N
                        } else if (SHAPE_ELLIPSE.equals(shapeType)) {
                            xmlew.add(xmlef.createAttribute(
                                    new QName(PROPERTY_ELLIPSE_COLOR), clrFillColor.getRGB() + ""));
                            xmlew.add(xmlef.createAttribute(
                                    new QName(PROPERTY_OVAL_COLOR), clrStrokeColor.getRGB() + ""));
                        } else if (SHAPE_POLYGON.equals(shapeType)) {
                            xmlew.add(xmlef.createAttribute(
                                    new QName(PROPERTY_INTERIOR_COLOR), clrFillColor.getRGB() + ""));
                            xmlew.add(xmlef.createAttribute(
                                    new QName(PROPERTY_OUTLINE_COLOR), clrFillColor.getRGB() + ""));
                        }
                    }
                    xmlew.add(xmlef.createEndElement(tagShape, null));
                } catch (XMLStreamException ex) {
                    Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            xmlew.add(xmlef.createEndElement(tagLayout, null));
            
            xmlew.add(xmlef.createEndElement(tagView, null));
            xmlew.close();
             try (FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/LYTEDITOR.xml")) {
            fos.write(baos.toByteArray());
        } catch (Exception e) {
        }
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            return null;
        }
    }
    
    public Rectangle getLayoutBounds(List<MxGraphNode> children) {
        double xmin = Integer.MAX_VALUE;
        double ymin = Integer.MAX_VALUE;
        double xmax = Integer.MIN_VALUE;
        double ymax = Integer.MIN_VALUE;
                
        for (MxGraphNode child : children) {
            Point childPoint = new Point(child.getX(), child.getY());
            double childW = child.getWidth();
            double childH = child.getHeight();
            /*
                0-----1
                |     |
                |     |
                3-----2
            */
            Point [] points = new Point[4];
            points[0] = new Point(childPoint.getX(), childPoint.getY());
            points[1] = new Point(childPoint.getX() + childW, childPoint.getY());
            points[2] = new Point(childPoint.getX() + childW, childPoint.getY() + childH);
            points[3] = new Point(childPoint.getX(), childPoint.getY() + childH);
                        
            for (Point point : points) {
                if (xmin > point.getX()) {xmin = point.getX();}

                if (ymin > point.getY()) {ymin = point.getY();}

                if (xmax < point.getX()) {xmax = point.getX();}

                if (ymax < point.getY()) {ymax = point.getY();}
            }
        }
        return new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);
    }
    
     private void renderShape(BusinessObjectLight theObject, byte[] structure, int unitWidth, int unitHeight, MxGraphNode deviceNode, boolean renderCustomShape) throws FactoryConfigurationError, NumberFormatException {
        try {

            //          <editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
//            try {
//                FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/device_structure" + deviceNode.getUuid() + ".xml");
//                fos.write(structure);
//                fos.close();
//            } catch (IOException e) {
//            }
//                     </editor-fold>

            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
            
            QName tagLayout = new QName("layout"); //NOI18N
            QName tagShape = new QName("shape"); //NOI18N
            String attrValue;
            double percentWidth = 1, percentHeight = 1, percentX = 1, percentY = 1;
            double propY =  1, propX = renderCustomShape ? 1 : 1,
                    propSize = 1; // Fix coordinates
//            double propY =  .2, propX = renderCustomShape ? 0.22 : 0.195,
//                    propSize = 0.20; // Fix coordinates
            double layoutX = 0, layoutY = 0;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(tagLayout)) {
                        attrValue = reader.getAttributeValue(null, "x"); //NOI18N
                        if (attrValue != null) {
                            layoutX = Integer.valueOf(attrValue) * percentWidth;
                        }
                        attrValue = reader.getAttributeValue(null, "y"); //NOI18N
                        if (attrValue != null) {
                            layoutY = Integer.valueOf(attrValue) * percentHeight;
                        }
                        attrValue = reader.getAttributeValue(null, "width"); //NOI18N
                        int width = 0;
                        if (attrValue != null) {
                            width = Integer.valueOf(attrValue);
                        }
                        int height = 0;
                        attrValue = reader.getAttributeValue(null, "height"); //NOI18N
                        if (attrValue != null) {
                            height = Integer.valueOf(attrValue);
                        }                        
                        if (renderCustomShape) {
                            percentWidth = deviceNode.getWidth() / (width * propSize );
                            percentHeight = deviceNode.getHeight() / (height * propSize);
                        }
                    }
                    if (reader.getName().equals(tagShape)) {
                        String shapeType = reader.getAttributeValue(null, "type");
                        
                        int borderWidth, color, borderColor, textColor, fontSize;
                        double width = 50, height = 50, x = 0, y = 0;
                        String name, label;
                        
                        if (shapeType != null) {
                            
                            MxGraphNode nodeShape = new MxGraphNode();
                            String nodeUuid = UUID.randomUUID().toString();
                            nodeShape.setUuid(nodeUuid);
                            nodeShape.setCellParent(deviceNode.getUuid());
                                                          
                            attrValue = reader.getAttributeValue(null, PROPERTY_X);
                            if (attrValue != null) {
                                x = Double.valueOf(attrValue) * percentWidth + (!renderCustomShape ? layoutX : 0);
                            }
                            
                            attrValue = reader.getAttributeValue(null, PROPERTY_Y);
                            if (attrValue != null) {
                                y = Double.valueOf(attrValue) * percentHeight + (!renderCustomShape ? layoutY : 0);
                            }
                            
                            attrValue = reader.getAttributeValue(null, PROPERTY_WIDTH);
                            if (attrValue != null) {
                                width = Double.valueOf(attrValue) * percentWidth;
                            }
                            
                            attrValue = reader.getAttributeValue(null, PROPERTY_HEIGHT);
                            if (attrValue != null) {
                                height = Double.valueOf(attrValue) * percentHeight;
                            }                            
                            
                            double widthAdjust;
                            width = (width * propSize);
                            height = height * propSize;
                            if (width > unitWidth) {
                                width = unitWidth;
                            }
//                            if (height > unitHeight) {
//                                height = unitHeight;
//                            }
                            x = x * propX;
                            y = y * propY;
//                            if (x > unitWidth) {
//                                x = unitWidth - width;
//                            }
//                            if (y > unitHeight) {
//                                y = unitHeight - height;
//                            }
                            
                            nodeShape.setGeometry((int) x, (int) y, (int) width, (int) height);
//                            nodeShape.setLabel(" w: "+ width + " h: "+ height);
                            nodeShape.setStrokeColor(MxConstants.NONE);
                            if (renderCustomShape) {
//                                nodeShape.setIsResizable(false);
                                nodeShape.setIsMovable(false);
                                nodeShape.setIsSelectable(false);
                            }
                            name = reader.getAttributeValue(null, PROPERTY_NAME);
                            
                            if (SHAPE_CUSTOM.equals(shapeType)) {
                                
                                String id = reader.getAttributeValue(null, Constants.PROPERTY_ID);
                                String className = reader.getAttributeValue(null, Constants.PROPERTY_CLASS_NAME);
                                BusinessObject lstTypeObject = new BusinessObject(className, id, "");
                                byte[] customShapeStructure = null;
                                if (layoutDevices.containsKey(lstTypeObject)) {
                                    customShapeStructure = layoutDevices.get(lstTypeObject);
                                } else {
                                    try {
                                        List<ViewObjectLight> views = aem.getListTypeItemRelatedViews(id, className, 1);
                                        if (!views.isEmpty()) {
                                            ViewObject view = aem.getListTypeItemRelatedView(id, className, views.get(0).getId());
                                            customShapeStructure = view.getStructure();
                                            layoutDevices.put(lstTypeObject, customShapeStructure);
                                        }
                                    } catch(ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
                                        
                                    }
                                }
                                if (customShapeStructure != null) {
                                    mxGraphCanvas.addNode(new BusinessObjectLight((renderCustomShape ? INNER_SHAPE : SHAPE_CUSTOM), nodeUuid, id), nodeShape);                                 
                                    renderShape(theObject, customShapeStructure, unitWidth, unitHeight, nodeShape, true);                                    
                                }
                            } else {
                                attrValue = reader.getAttributeValue(null, PROPERTY_COLOR);
                                if (attrValue != null) {
                                    color = (Integer.valueOf(attrValue));
                                    nodeShape.setFillColor(UtilHtml.toHexString(new Color(color)));
                                }
                                
                                attrValue = reader.getAttributeValue(null, PROPERTY_BORDER_COLOR);
                                if (attrValue != null) {
                                    borderColor = (Integer.valueOf(attrValue));
                                    nodeShape.setStrokeColor(UtilHtml.toHexString(new Color(borderColor)));
                                }
                                
                                if (SHAPE_RECTANGLE.equals(shapeType)) {
                                    nodeShape.setShape(MxConstants.SHAPE_RECTANGLE);                                  
                                } else if (SHAPE_LABEL.equals(shapeType)) {
                                    
                                    nodeShape.setShape(MxConstants.SHAPE_LABEL);
                                    
                                    label = reader.getAttributeValue(null, "label"); //NOI18N                                    
                                    nodeShape.setLabel(label);
                                    
                                    attrValue = reader.getAttributeValue(null, "textColor"); //NOI18N
                                    if (attrValue != null) {
                                        textColor = (Integer.valueOf(attrValue));
                                        nodeShape.setFontColor(UtilHtml.toHexString(new Color(textColor)));
                                    }
                                    
                                    // Static FontSize 
//                                    attrValue = reader.getAttributeValue(null, "fontSize"); //NOI18N
                                    nodeShape.setFontSize(height * 0.65);
                                    
                                } else if (SHAPE_ELLIPSE.equals(shapeType)) {
                                    nodeShape.setShape(MxConstants.SHAPE_ELLIPSE);
                                    attrValue = reader.getAttributeValue(null, PROPERTY_ELLIPSE_COLOR);
                                    if (attrValue != null) {
                                        color = (Integer.valueOf(attrValue));
                                        nodeShape.setFillColor(UtilHtml.toHexString(new Color(color)));
                                    }
                                    
                                    attrValue = reader.getAttributeValue(null, PROPERTY_OVAL_COLOR);
                                    if (attrValue != null) {
                                        color = (Integer.valueOf(attrValue));
                                        nodeShape.setStrokeColor(UtilHtml.toHexString(new Color(color)));
                                    }
                                } else if (SHAPE_POLYGON.equals(shapeType)) {
                                    
                                    nodeShape.setShape(MxConstants.SHAPE_TRIANGLE);
                                    attrValue = reader.getAttributeValue(null, PROPERTY_INTERIOR_COLOR);
                                    if (attrValue != null) {
                                        color = (Integer.valueOf(attrValue));
                                        nodeShape.setFillColor(UtilHtml.toHexString(new Color(color)));
                                    }
                                    
                                    attrValue = reader.getAttributeValue(null, PROPERTY_OUTLINE_COLOR);
                                    if (attrValue != null) {
                                        color = (Integer.valueOf(attrValue));
                                        nodeShape.setStrokeColor(UtilHtml.toHexString(new Color(color)));
                                    }
                                }
                                mxGraphCanvas.addNode(new BusinessObjectLight(renderCustomShape ? INNER_SHAPE : shapeType, nodeUuid, ""), nodeShape);
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException ex) {
            Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            
        } 
    }

}
