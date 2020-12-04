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
package org.neotropic.kuwaiba.modules.optional.physcon.views;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractDetailedView;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewEdge;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewEventListener;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewMap;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.views.util.UtilHtml;

/**
 * Rack view, shows the front view of the rack the way it should look like in the real world.
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class RackView extends AbstractDetailedView<BusinessObjectLight, VerticalLayout> {

    /**
     * Reference to the main canvas of the view
     */
    private MxGraph mxGraph;
    /**
     * Reference to the translation service.
     */
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager
     */
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager
     */
    private MetadataEntityManager mem;
    /**
     * Reference to the Physical Connection Service
     */
    private PhysicalConnectionsService physicalConnectionsService;
    /*
    map to store the device with their respective layout
     */
    private HashMap<BusinessObject, byte[]> layoutDevices;
    /*
     Stores the content in the rack
     */
    private List<RackSegment> rackContent;
    /*
     Main layout graph
     */
    private HorizontalLayout lytGraph;
    /*
     Used to know the ordering
     */
    private boolean orderDescending;
    /*
     Reference to the main node
     */
    private MxGraphNode rackNode;
    
    private int heightSeparator, rackUnits;
    /*
      Flag that indicates if the layout of the equipment should be shown.
     */
    private boolean showLayouts;

    /*
     Some internal Constants
     */
    private final int DEFAULT_PORT_SIZE = 20;
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

    public MxGraph getMxGraph() {
        return mxGraph;
    }

    public void setMxGraph(MxGraph mxGraph) {
        this.mxGraph = mxGraph;
    }

    public boolean isOrderDescending() {
        return orderDescending;
    }

    public void setOrderDescending(boolean orderDescending) {
        this.orderDescending = orderDescending;
    }  
    
    public RackView(BusinessObjectLight businessObject) {
        super(businessObject);
    }
    
    public RackView(BusinessObjectLight businessObject, boolean showLayouts, BusinessEntityManager bem, ApplicationEntityManager aem,
            MetadataEntityManager mem, TranslationService ts, PhysicalConnectionsService physicalConnectionsService) {
        this(businessObject);
        this.bem = bem;        
        this.aem = aem;
        this.mem = mem;
        this.ts = ts;
        this.physicalConnectionsService = physicalConnectionsService;
        this.showLayouts = showLayouts;
    }
    
    @Override
    public String appliesTo() {
        return "Rack";
    }
    
    @Override
    public String getName() {
        return ts.getTranslatedString("module.visualization.rack-view-name");
    }
    
    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.visualization.rack-view-description");
    }
    
    @Override
    public String getVersion() {
        return "1.0";
    }
    
    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>"; //NOI18N
    }
    
    @Override
    public byte[] getAsXml() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public byte[] getAsImage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public HorizontalLayout getAsComponent() throws InvalidArgumentException {
        buildWithBusinessObject(businessObject);
        loadDeviceLayouts();
        if (businessObject != null) {
            try {                
                BusinessObject rack = bem.getObject(businessObject.getClassName(), businessObject.getId());                
                lytGraph = new HorizontalLayout();
                lytGraph.setSizeFull();
                lytGraph.setMargin(false);
                lytGraph.setSpacing(false);
                lytGraph.setPadding(false);
                mxGraph = new MxGraph();
                mxGraph.setWidth("800px");               
                mxGraph.setHeight("530px");
                mxGraph.setOverflow("scroll");
                mxGraph.setGrid("img/grid.gif");
                mxGraph.setHasOutline(true);
                mxGraph.setOutlineWidth("100px");
                mxGraph.setBeginUpdateOnInit(true);
                mxGraph.addGraphLoadedListener(eventListener -> {
                    mxGraph.enablePanning(true);
                });                       
                
                rackUnits = (Integer) rack.getAttributes().get(Constants.PROPERTY_RACK_UNITS);
                orderDescending = rack.getAttributes().containsKey(Constants.PROPERTY_RACK_UNITS_NUMBERING)  ? (boolean) rack.getAttributes().get(Constants.PROPERTY_RACK_UNITS_NUMBERING) : false;
                int[] rackNumbers = new int[rackUnits];
                for (int i = 0; i < rackUnits; i++) {
                    rackNumbers[i] = orderDescending ? rackUnits - i : i + 1;
                }
                
                int unitHeight = 58, unitWidth = 640, deviceRackUnits, deviceRackPosition, currentRackUnitPosition = 0, currentRackUnitSize = 0;
                heightSeparator = 0;
                MxGraphNode rackUnit = new MxGraphNode();
                MxGraphNode deviceNode = null;
                rackNode = new MxGraphNode();
                MxGraphNode mainBox = new MxGraphNode();
                mainBox.setUuid("main");
                mainBox.setLabel(businessObject.getName());
                mainBox.setGeometry(20, 20, unitWidth, unitHeight * rackUnits);
                rackNode.setUuid("rackNode");
                rackNode.setCellParent(mainBox.getUuid());
                rackNode.setLabel(businessObject.getName());
                rackNode.setGeometry(100, 50, unitWidth, unitHeight * rackUnits);                
                mxGraph.addNode(mainBox);
                
                viewMap.getNodes().sort(Comparator.comparing(item -> ((int) ((BusinessObject) item.getIdentifier()).getAttributes().get(Constants.PROPERTY_POSITION))));
                
                MxGraphNode nodeNumber;
                MxGraphNode nodeUnitNumbers = new MxGraphNode();
                nodeUnitNumbers.setUuid("nodeNumbers");
                nodeUnitNumbers.setGeometry(50, 50, 50, unitHeight * rackUnits);
                nodeUnitNumbers.setCellParent(mainBox.getUuid());
                nodeUnitNumbers.setIsResizable(false);
                mxGraph.addNode(nodeUnitNumbers);
                mxGraph.addNode(rackNode);
                for (int i = 0; i < rackUnits; i++) {                    
                    nodeNumber = new MxGraphNode();
                    nodeNumber.setUuid("nodeNumber" + rackNumbers[i]);
                    nodeNumber.setLabel((rackNumbers[i]) + "");
                    nodeNumber.setGeometry(0, 0, 50, unitHeight);
                    nodeNumber.setCellParent("nodeNumbers");
                    nodeNumber.setVerticalLabelPosition(MxConstants.ALIGN_CENTER);
                    mxGraph.addNode(nodeNumber);
                    if (i == (rackUnits - 1)) {
                        nodeNumber.addCellAddedListener(eventListener -> {
                            mxGraph.executeStackLayout(nodeUnitNumbers.getUuid(), false, heightSeparator);
                        });
                    }
                }
                int initialSegmentPosition = 1;                
                List<BusinessObject> rackSegmentDevices = new ArrayList<>();
                rackContent = new ArrayList<>();
                for (int i = 0; i < rackUnits; i++) {
                    int position = i + 1;                    
                    List<AbstractViewNode> unitNodes = viewMap.getNodes().stream()
                            .filter(item -> ((int) ((BusinessObject) item.getIdentifier()).getAttributes().get(Constants.PROPERTY_POSITION)) == position).collect(Collectors.toList());
                    for (AbstractViewNode device : unitNodes) {
                        
                        BusinessObject theObject = (BusinessObject) device.getIdentifier();                        
                        deviceRackUnits = ((int) theObject.getAttributes().get(Constants.PROPERTY_RACK_UNITS));
                        deviceRackPosition = ((int) theObject.getAttributes().get(Constants.PROPERTY_POSITION));
                        
                        if (deviceRackPosition > currentRackUnitPosition) { //create new rack unit
                            
                            if (deviceNode != null) {// execute the layout when the last device in the rackunit is added
                                String lastRackUnitUuid = rackUnit.getUuid();
                                deviceNode.addCellAddedListener(eventListener -> {
                                    mxGraph.executeStackLayout(lastRackUnitUuid, true, 0);
                                });
                                RackSegment rackSegment = new RackSegment(rackUnit,
                                        orderDescending ? initialSegmentPosition : initialSegmentPosition,
                                        orderDescending ? currentRackUnitPosition : currentRackUnitPosition, rackSegmentDevices);
                                rackContent.add(rackSegment);
                                rackSegmentDevices = new ArrayList<>();
                            }  // add the empty rack units and the rack unit to the current device
                            for (int j = currentRackUnitPosition + 1; j <= deviceRackPosition; j++) {                                
                                rackUnit = new MxGraphNode();
                                rackUnit.setUuid("rackUnit" + j);
                                rackUnit.setLabel("");
                                rackUnit.setGeometry(0, 0, unitWidth, unitHeight);
                                rackUnit.setCellParent(rackNode.getUuid());                                
                                rackUnit.setFillColor("white");
                                mxGraph.addNode(rackUnit);
                                
                                if (j != deviceRackPosition) {  // empty rack unit
                                    rackContent.add(new RackSegment(rackUnit, j, j, null));
                                }
                            }
                            MxGraphNode lastRackUnit = rackUnit; // to evade static reference
                            int units = deviceRackUnits;
                            rackUnit.addCellAddedListener(evtAdded -> {
                                lastRackUnit.addOverlayButton("moveUp", ts.getTranslatedString("module.visualization.rack-view-move-up"), "images/arrow_up.png", MxConstants.ALIGN_RIGHT, MxConstants.ALIGN_TOP, 0, 10);                                
                                lastRackUnit.addOverlayButton("moveDown", ts.getTranslatedString("module.visualization.rack-view-move-down"), "images/arrow_down.png", MxConstants.ALIGN_RIGHT, MxConstants.ALIGN_BOTTOM, 0, -10);                                
                                lastRackUnit.addOverlayButton("move", ts.getTranslatedString("module.visualization.rack-view-move-to-position"), "images/move-unit.png", MxConstants.ALIGN_RIGHT, MxConstants.ALIGN_BOTTOM, 15, -(unitHeight * units) / 2);                                
                            });
                            rackUnit.addClickOverlayButtonListener(evt -> {
                                switch (evt.getButtonId()) {
                                    case "move": {
                                        BoldLabel lblMoveToPosition = new BoldLabel(ts.getTranslatedString("module.visualization.rack-view-move-to"));
                                        NumberField nbrPosition = new NumberField(ts.getTranslatedString("module.visualization.rack-view-position"));
                                        nbrPosition.setMin(1);
                                        nbrPosition.setRequiredIndicatorVisible(true);
                                        HorizontalLayout lytPosition = new HorizontalLayout(lblMoveToPosition, nbrPosition);
                                        lytPosition.setAlignItems(FlexComponent.Alignment.BASELINE);
                                        lytPosition.setWidthFull();
                                        Dialog dlgMove = new Dialog();
                                        dlgMove.setWidth("300px");
                                        dlgMove.open();
                                        Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"), btnEvt -> {                                            
                                            Double desiredPosition = nbrPosition.getValue();
                                            if (desiredPosition == null) {
                                                return;
                                            }
                                            moveSegment(lastRackUnit, desiredPosition.intValue());                                            
                                            dlgMove.close();
                                        });
                                        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), evtBtn -> {
                                            dlgMove.close();
                                        });
                                        VerticalLayout lytContent = new VerticalLayout(lytPosition, new HorizontalLayout(btnOk, btnCancel));
                                        lytContent.setSpacing(true);
                                        dlgMove.add(lytContent);
                                        dlgMove.open();
                                        break;
                                    }
                                    case "moveUp": {
                                        RackSegment rackSegment = rackContent.stream().filter(item -> item.getSegmentNode().equals(lastRackUnit)).findFirst().get();                                        
                                        if (orderDescending) {
                                            moveSegment(lastRackUnit, rackSegment.getInitialPosition() + 1);
                                        } else {
                                            moveSegment(lastRackUnit, rackSegment.getInitialPosition() - 1);
                                        }
                                        break;
                                    }                                    
                                    case "moveDown": {
                                        RackSegment rackSegment = rackContent.stream().filter(item -> item.getSegmentNode().equals(lastRackUnit)).findFirst().get();                                        
                                        if (orderDescending) {
                                            moveSegment(lastRackUnit, rackSegment.getInitialPosition() - 1);
                                        } else {
                                            moveSegment(lastRackUnit, rackSegment.getInitialPosition() + 1);
                                        }
                                        break;
                                    }
                                    default:
                                        break;
                                }
                            });
                            currentRackUnitSize = deviceRackUnits;
                            rackUnit.setHeight(currentRackUnitSize * unitHeight + (heightSeparator * (currentRackUnitSize - 1)));
                            currentRackUnitPosition = deviceRackUnits + deviceRackPosition - 1;
                            initialSegmentPosition = deviceRackPosition;
                            
                        } else {   //update the lenght of the rack unit box
                            if (currentRackUnitPosition < (deviceRackUnits + deviceRackPosition - 1)) {
                                currentRackUnitPosition = deviceRackUnits + deviceRackPosition - 1;
                            }

                            // update the rack unit size if any device has more rack units 
                            if (deviceRackUnits > currentRackUnitSize) {
                                currentRackUnitSize = deviceRackUnits;
                                rackUnit.setHeight(currentRackUnitSize * unitHeight + (heightSeparator * (currentRackUnitSize - 1)));
                            }
                        }
                        rackSegmentDevices.add(theObject);
                        
                        ClassMetadata theClass = mem.getClass(theObject.getClassName());
                        
                        deviceNode = new MxGraphNode();
                        deviceNode.setStrokeColor(MxConstants.NONE);
                        deviceNode.setFillColor(UtilHtml.toHexString(new Color(theClass.getColor())));
                        deviceNode.setUuid(theObject.getId());
                        deviceNode.setLabel(theObject.getName());
                        deviceNode.setGeometry(0, 0, (unitWidth / unitNodes.size()), unitHeight * deviceRackUnits);
                        deviceNode.setCellParent(rackUnit.getUuid());
                        deviceNode.setVerticalLabelPosition(MxConstants.ALIGN_CENTER);
                        MxGraphNode currentDeviceNode = deviceNode;
                        int units = deviceRackUnits;
                        deviceNode.addCellAddedListener(evtAdded -> {
                            currentDeviceNode.addOverlayButton("showPorts", ts.getTranslatedString("module.visualization.rack-view-show-ports"), "images/view_port.png", MxConstants.ALIGN_RIGHT, MxConstants.ALIGN_CENTER, 35, -(unitHeight * units) / 2);                            
                        });
                        
                        deviceNode.addClickOverlayButtonListener(evt -> {
                            if (evt.getButtonId().equals("showPorts")) {
                                try {
                                    List<BusinessObjectLight> ports = bem.getChildrenOfClassLightRecursive(theObject.getId(), theObject.getClassName(), Constants.CLASS_GENERICPORT, -1);
                                    
                                    Grid<BusinessObjectLight> tblPorts = new Grid();
                                    tblPorts.setWidthFull();
                                    tblPorts.setMaxHeight("450px");
                                    tblPorts.setItems(ports);
                                    tblPorts.addComponentColumn(item -> {
                                        try {
                                            String txtName = item.getName() == null || item.getName().isEmpty() ? "<Name Not Set>" : item.getName();
                                            VerticalLayout lytName = new VerticalLayout(new BoldLabel(txtName));
                                            List<BusinessObjectLight> parents = bem.getParents(item.getClassName(), item.getId());
                                            String path = "";
                                            for (BusinessObjectLight parent : parents) {
                                                if (parent.getId().equals(rack.getId())) {
                                                    break;
                                                }
                                                path += parent.getName() + "/";
                                            }
                                            Label lblPath = new Label(path);
                                            lblPath.addClassName("text-secondary-b");
                                            lytName.setSpacing(false);
                                            lytName.setMargin(false);
                                            lytName.setMargin(false);
                                            lytName.setPadding(false);
                                            lytName.add(lblPath);
                                            return lytName;
                                        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                                            Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                                            return new Label("");
                                        }
                                    }).setHeader(ts.getTranslatedString("module.visualization.rack-view-port-name"));
                                    
                                    tblPorts.addComponentColumn(item -> {
                                        try {
                                            List<BusinessObjectLight> physicalPath = physicalConnectionsService.getPhysicalPath(item.getClassName(), item.getId());
                                            if (physicalPath.size() > 0) {
                                                BusinessObjectLight endPoint = physicalPath.get(physicalPath.size()-1);
                                                BusinessObjectLight parent = bem.getParent(endPoint.getClassName(), endPoint.getId());
                                                Label lblEndPoint = new Label(parent.getName() + " : " + endPoint.getName());
                                                Button btnPhysicalPath = new Button(new Icon(VaadinIcon.FILE_TREE_SUB), evtPhysicalPath -> {
                                                    try {
                                                        Dialog dlgPhysicalPath = new Dialog();
                                                        dlgPhysicalPath.setWidth("95%");
                                                        dlgPhysicalPath.setHeight("600px");

                                                        PhysicalPathView view = new PhysicalPathView(item, bem, aem, mem, ts, physicalConnectionsService);
                                                        view.setComponentWidth("100%");
                                                        view.setComponentHeight("250px");
                                                        Button btnClosePhysicalPath = new Button(ts.getTranslatedString("module.general.messages.close"), evtDlgPhysicalPath -> 
                                                                dlgPhysicalPath.close());
                                                        BoldLabel lblPhysicalPath = new BoldLabel(ts.getTranslatedString("module.visualization.physical-path-view-name") + " : " + item.toString());                                                       
                                                        Label lblPort = new Label();
          
                                                        dlgPhysicalPath.add(new VerticalLayout(lblPhysicalPath, view.getAsComponent(), btnClosePhysicalPath));
                                                        dlgPhysicalPath.open();
                                                    } catch (InvalidArgumentException ex) {
                                                        Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                                                         new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                                                            AbstractNotification.NotificationType.ERROR, ts).open();
                                                    }
                                                });
                                                btnPhysicalPath.getElement().setProperty("title", ts.getTranslatedString("module.visualization.physical-path-view-show"));     
                                                HorizontalLayout lytEndPoint = new HorizontalLayout(lblEndPoint, btnPhysicalPath);
                                                lytEndPoint.setFlexGrow(1, lblEndPoint);
                                                lytEndPoint.setAlignItems(FlexComponent.Alignment.BASELINE);
                                                return lytEndPoint;
                                            } else {
                                                return new Label(ts.getTranslatedString("module.visualization.rack-view-disconnected"));
                                            }
                                        } catch (MetadataObjectNotFoundException | InvalidArgumentException 
                                                | IllegalStateException | BusinessObjectNotFoundException | ApplicationObjectNotFoundException ex) {
                                            Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                                            return new Label("");
                                        } 
                                    }).setHeader(ts.getTranslatedString("module.visualization.rack-view-connected-to"));
                                    tblPorts.addComponentColumn(item -> {
                                        try {
                                            HashMap<String, List<BusinessObjectLight>> uses = bem.getSpecialAttributes(item.getClassName(), item.getId(), "uses");
                                            if (!uses.containsKey("uses")) {
                                                return new Label("N/A");
                                            }
                                            VerticalLayout lytUses = new VerticalLayout();
                                            for (BusinessObjectLight obj : uses.get("uses")) {
                                                lytUses.add(new Label(obj.getName()));
                                            }
                                            lytUses.setMargin(false);
                                            lytUses.setMargin(false);
                                            lytUses.setPadding(false);
                                            return lytUses;
                                        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                                            Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                                            return new Label("");
                                        }
                                    }).setHeader(ts.getTranslatedString("module.visualization.rack-view-services"));
                                    Dialog dlgSummary = new Dialog();
                                    dlgSummary.setWidth("80%");
                                    dlgSummary.setHeight("550px");
                                    BoldLabel lblSummary = new BoldLabel(ts.getTranslatedString("module.visualization.rack-view-port-summary"));
                                    Label lblDeviceLayout = new Label(ts.getTranslatedString("module.visualization.rack-view-selected-device-has-no-layout"));
                                    Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), evtBtn -> {
                                        dlgSummary.close();
                                    });
                                    VerticalLayout lytSummary = new VerticalLayout(lblSummary, lblDeviceLayout, tblPorts, btnClose);
                                    lytSummary.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
                                    dlgSummary.add(lytSummary);
                                    dlgSummary.open();
                                } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                                    Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        });
                        deviceNode.addClickCellListener(clickListener -> {                            
                            
                        });
                        mxGraph.addNode(deviceNode);
                        
                        if (layoutDevices.containsKey(theObject) && showLayouts) {
                            byte[] structure = layoutDevices.get(theObject);
                            if (structure.length > 0) {
                                renderShape(theObject, structure, unitWidth / unitNodes.size(), unitHeight * deviceRackUnits, mxGraph, deviceNode, false, false);
                            }                            
                        }                        
                    }
                }
                if (deviceNode != null) {
                    RackSegment rackSegment = new RackSegment(rackUnit,
                            orderDescending ? initialSegmentPosition : initialSegmentPosition,
                            orderDescending ? currentRackUnitPosition : currentRackUnitPosition, rackSegmentDevices);
                    rackContent.add(rackSegment);
                }
                // add the last empty rack units 
                for (int j = currentRackUnitPosition + 1; j <= rackUnits; j++) {                    
                    rackUnit = new MxGraphNode();
                    rackUnit.setUuid("rackUnit" + j);
                    rackUnit.setLabel("");
                    rackUnit.setGeometry(0, 0, unitWidth, unitHeight);
                    rackUnit.setCellParent(rackNode.getUuid());                    
                    rackUnit.setFillColor("white");
                    rackUnit.setStrokeColor("black");
                    mxGraph.addNode(rackUnit);                    
                    rackContent.add(new RackSegment(rackUnit, j, j, null));
                    
                }
                String lastRackUnitUuid = rackUnit.getUuid(); // when the last rack unit is added execute the layout for the last rack unit
                if (deviceNode != null && lastRackUnitUuid != null) {                    
                    deviceNode.addCellAddedListener(eventListener -> {
                        mxGraph.executeStackLayout(lastRackUnitUuid, true, 0);
                    });                    
                }
                
                MxGraphNode dummyNode = new MxGraphNode();
                dummyNode.setGeometry(0, 0, 0, 0);
                mxGraph.addNode(dummyNode);
                //  execute the layout and disable moving when the last cell is added
                dummyNode.addCellAddedListener(eventListener -> {
                    if (orderDescending) {
                        for (int i = 0; i < rackContent.size(); i++) {
                            rackNode.setChildrenCellPosition(rackContent.get(i).getSegmentNode().getUuid(), rackContent.size() - 1 - i);
                        }
                        Collections.reverse(rackContent);
                    }
                    mxGraph.executeStackLayout(rackNode.getUuid(), false, heightSeparator);
                    mxGraph.executeStackLayout(mainBox.getUuid(), true, 5);
                    mxGraph.setCellsResizable(false);
                    mxGraph.setCellsMovable(false);
                    mxGraph.setCellsSelectable(false);
                    mxGraph.setCellsEditable(false);
                    mxGraph.endUpdate();
                    
                });

                lytGraph.add(mxGraph);
                return lytGraph;
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            }
            
        }
        return new HorizontalLayout(new Label(ts.getTranslatedString("module.visualization.view.no-business-object-associated")));
    }
    
    /**
     * Move the given segment(MxGrapohNode) to the desired position. 
     * Changes are made for each device in the segment internally and then graphically
     * @param segmentNode
     * @param desiredPosition 
     */
    private void moveSegment(MxGraphNode segmentNode, int desiredPosition) {
        
        if (desiredPosition > rackUnits) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.visualization.rack-view-position-exceeds-units"), 
                            AbstractNotification.NotificationType.WARNING, ts).open();
            return;
        }        
        if (desiredPosition <= 0) // first position     
            return;
        
        RackSegment rackSegment = rackContent.stream().filter(item -> item.getSegmentNode().equals(segmentNode)).findFirst().get();
        int segmentIndex = rackContent.indexOf(rackSegment);
        int originalPosition = rackSegment.getInitialPosition();
        if (desiredPosition == rackSegment.getInitialPosition()) {
            return;
        }
        int segmentSize = rackSegment.getFinalPosition() - rackSegment.getInitialPosition() + 1;
        boolean unitAvailable = true;
        int newFinalPosition = desiredPosition + segmentSize - 1;
        // Validate space free
        List<RackSegment> emptySegmentsToMove = new ArrayList<>();
        int newSegmentIndex = -1;
        
        for (int j = desiredPosition; j <= newFinalPosition; j++) {
            int pos = j;
            RackSegment rs = rackContent.stream()
                    .filter(item -> item.getInitialPosition() <= pos && item.getFinalPosition() >= pos)
                    .findFirst().get();
            
            if (rs == null) // empty rack unit            
                return;          
            if (rs.equals(rackSegment)) 
                continue;           
            if (newSegmentIndex == -1) 
                newSegmentIndex = rackContent.indexOf(rs); // Store the new Segment position
            
            // If the desired segment is other (can be the same when we move the segment down) and it isnt empty then break                                                                      
            if (rs.getDevices() != null) {                
                unitAvailable = false;
                break;
            }
            emptySegmentsToMove.add(rs);
        }
        if (!unitAvailable) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.visualization.rack-view-rack-unit-occupied"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            return;
        }
        
        rackSegment.setFinalPosition(newFinalPosition);
        rackSegment.setInitialPosition(desiredPosition);
        updateDevicesPosition(desiredPosition, rackSegment);

        // Move the segment to the new position
        rackContent.remove(segmentIndex);
        rackContent.add(newSegmentIndex, rackSegment);
        // Update view
        rackNode.setChildrenCellPosition(segmentNode.getUuid(), newSegmentIndex);
        
        int startPositionEmptyUnits = originalPosition;
        // Validation for the special case that we move a segment up a number of units less than its own number.
        if (originalPosition > desiredPosition && originalPosition <= newFinalPosition) {
            startPositionEmptyUnits = newFinalPosition + 1;
        }

        // Now move the empty units to the previous used unit position
        for (int j = 0; j < emptySegmentsToMove.size(); j++) {
            RackSegment rs = emptySegmentsToMove.get(j);
            rs.setFinalPosition(startPositionEmptyUnits + j);
            rs.setInitialPosition(startPositionEmptyUnits + j);
            rackContent.remove(rs);
            int segmentDestination = segmentIndex > newSegmentIndex ? segmentIndex : segmentIndex + j;
            rackContent.add(segmentDestination, rs);
            rackNode.setChildrenCellPosition(rs.getSegmentNode().getUuid(), segmentDestination);
        }
        
        mxGraph.setCellsMovable(true);
        mxGraph.executeStackLayout(rackNode.getUuid(), false, heightSeparator);
        mxGraph.setCellsMovable(false);
        new SimpleNotification(ts.getTranslatedString("module.general.messages.information"), ts.getTranslatedString("module.visualization.rack-view-rack-position-updated"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
    }
    
    private void updateDevicesPosition(int desiredPosition, RackSegment rackSegment) {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put(Constants.PROPERTY_POSITION, desiredPosition + "");
        
        rackSegment.getDevices().forEach(item -> {
            try {
                bem.updateObject(item.getClassName(), item.getId(), attributes);
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException | InvalidArgumentException ex) {
                Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            }
        });
    }
    
    private void renderShape(BusinessObject theObject, byte[] structure, int unitWidth, int unitHeight, MxGraph mxGraph, MxGraphNode deviceNode, boolean renderCustomShape, boolean renderSlot) throws FactoryConfigurationError, NumberFormatException {
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
            double propY = renderSlot ? .45 : .2, propX = renderSlot ? 0.44 : renderCustomShape ? 0.22 : 0.195,
                    propSize = renderSlot ? 0.45 : 0.20; // Fix coordinates

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(tagLayout)) {
                        
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
                        
                        int color, borderColor, textColor;
                        double width = 50, height = 50, x = 0, y = 0;
                        String name, label;
                        boolean isSlot;
                        
                        if (shapeType != null) {
                            
                            MxGraphNode nodeShape = new MxGraphNode();
                            nodeShape.setCellParent(deviceNode.getUuid());
                            
                            attrValue = reader.getAttributeValue(null, PROPERTY_X);
                            if (attrValue != null) 
                                x = Double.valueOf(attrValue)* percentWidth;
                            
                            
                            attrValue = reader.getAttributeValue(null, PROPERTY_Y);
                            if (attrValue != null) 
                                y = Double.valueOf(attrValue) * percentHeight;
                            
                            
                            attrValue = reader.getAttributeValue(null, PROPERTY_WIDTH);
                            if (attrValue != null) 
                                width = Double.valueOf(attrValue) * percentWidth;
                            
                            
                            attrValue = reader.getAttributeValue(null, PROPERTY_HEIGHT);
                            if (attrValue != null) 
                                height = Double.valueOf(attrValue) * percentHeight;
                                                       
                            
                            double widthAdjust;
                            width = (width * propSize);
                            height = height * propSize;
                            if (width > unitWidth) 
                                width = unitWidth;
                            
                            if (height > unitHeight) 
                                height = unitHeight;
                            
                            x = x * propX;
                            y = y * propY;
                            if (x > unitWidth) 
                                x = unitWidth - width;
                            
                            if (y > unitHeight) 
                                y = unitHeight - height;
                            
                            
                            nodeShape.setGeometry((int) x, (int) y, (int) width, (int) height);
                            nodeShape.setStrokeColor(MxConstants.NONE);
                            name = reader.getAttributeValue(null, PROPERTY_NAME);
                            
                            if ("custom".equals(shapeType)) {
                                
                                String id = reader.getAttributeValue(null, Constants.PROPERTY_ID);
                                String className = reader.getAttributeValue(null, Constants.PROPERTY_CLASS_NAME);
                                BusinessObject lstTypeObject = new BusinessObject(className, id, "");
                                byte[] customShapeStructure = null;
                                if (layoutDevices.containsKey(lstTypeObject)) {
                                    customShapeStructure = layoutDevices.get(lstTypeObject);
                                } else {
                                    List<ViewObjectLight> views = aem.getListTypeItemRelatedViews(id, className, 1);
                                    if (!views.isEmpty()) {
                                        ViewObject view = aem.getListTypeItemRelatedView(id, className, views.get(0).getId());
                                        customShapeStructure = view.getStructure();
                                        layoutDevices.put(lstTypeObject, customShapeStructure);
                                    }
                                }
                                if (customShapeStructure != null) {
                                    mxGraph.addNode(nodeShape);                                    
                                    renderShape(theObject, customShapeStructure, unitWidth, unitHeight, mxGraph, nodeShape, true, false);                                    
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
                                    attrValue = reader.getAttributeValue(null, PROPERTY_IS_SLOT);
                                    if (attrValue != null) {
                                        isSlot = (Boolean.valueOf(attrValue));
                                        if (isSlot) {
                                            
                                            nodeShape.setLabel("");
                                            nodeShape.addCellAddedListener(eventListener -> {
                                                nodeShape.addOverlayButton("showContent", ts.getTranslatedString("module.visualization.rack-view-show-content"), "images/show-slot.png", MxConstants.ALIGN_CENTER, MxConstants.ALIGN_CENTER, 0, -8);                                                
                                            });
                                            nodeShape.addClickOverlayButtonListener(evt -> {
                                                if (evt.getButtonId().equals("showContent")) {
                                                    try {
                                                        if (name == null || name.isEmpty()) {
                                                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.visualization.rack-view-rack-slot-name-not-set"), 
                                                                AbstractNotification.NotificationType.ERROR, ts).open();
                                                            return;
                                                        }
                                                        List<BusinessObjectLight> childrenNodes = bem.getChildrenOfClassLight(theObject.getId(), theObject.getClassName(), Constants.CLASS_SLOT, -1);
                                                        BusinessObjectLight theSlot = childrenNodes.stream().filter(item -> item.getName().equals(name)).findAny().get();
                                                        if (theSlot == null) {
                                                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                                                                    String.format("%s %s", ts.getTranslatedString("module.visualization.rack-view-rack-no-slot-with-name"), name), 
                                                                AbstractNotification.NotificationType.INFO, ts).open();
                                                            return;
                                                        }
                                                        
                                                        List<BusinessObjectLight> lstChildrenSlot = bem.getObjectChildren(theSlot.getClassName(), theSlot.getId(), 1);
                                                        if (lstChildrenSlot.isEmpty()) {
                                                            return;
                                                        }                                            
                                                        BusinessObject slotDevice = bem.getObject(lstChildrenSlot.get(0).getClassName(), lstChildrenSlot.get(0).getId());
                                                        String modelId = (String) slotDevice.getAttributes().get("model");                                                       
                                                        if (modelId == null || modelId.isEmpty()) {
                                                            return;
                                                        }
                                                        
                                                        BusinessObject dummyObject = new BusinessObject("", modelId, "");
                                                        byte[] slotDeviceStructure = null;
                                                        if (layoutDevices.containsKey(slotDevice)) {
                                                            slotDeviceStructure = layoutDevices.get(slotDevice);
                                                        } else { // It should not enter here, all layouts are loaded at the beginning but it is left commented in case the way of loading layouts is changed
//                                                        List<ViewObjectLight> views = aem.getListTypeItemRelatedViews(id, className, 1);
//                                                        if (!views.isEmpty()) {
//                                                            ViewObject view = aem.getListTypeItemRelatedView(id, className, views.get(0).getId());
//                                                            slotDeviceStructure = view.getStructure();
//                                                            layoutDevices.put(lstTypeObject, customShapeStructure);
//                                                        }
                                                        }
                                                        if (slotDeviceStructure != null) {

                                                            // Take by default the first children
                                                            Dialog dlgSlotContent = new Dialog();
                                                            MxGraph mxGraphSlot = new MxGraph();
                                                            mxGraphSlot.setBeginUpdateOnInit(true);
                                                            mxGraphSlot.setRotationEnabled(true);
                                                            mxGraph.setHasOutline(true);
                                                            mxGraphSlot.setWidth("900px");
                                                            mxGraphSlot.setHeight("500px");
                                                            mxGraphSlot.setOverflow("scroll");
                                                            mxGraphSlot.setGrid("img/grid.gif");
                                                            mxGraphSlot.addGraphLoadedListener(eventListener -> {
                                                                mxGraphSlot.enablePanning(true);
                                                            });
                                                            
                                                            MxGraphNode slotNode = new MxGraphNode();
                                                            slotNode.setUuid(theSlot.getId());
                                                            slotNode.setIsSelectable(false);
                                                            slotNode.setStrokeColor(MxConstants.NONE);
                                                            slotNode.setGeometry(50, 50, 400, 300);
                                                            slotNode.setFillColor(MxConstants.NONE);
                                                            mxGraphSlot.addNode(slotNode);
                                                            
                                                            Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), evtBtn -> {
                                                                dlgSlotContent.close();
                                                            });
                                                            BoldLabel lblSlotContentTitle = new BoldLabel(ts.getTranslatedString("module.visualization.rack-view-rack-slot-content-device") + " (" + theSlot.toString() + ")");
                                                            Label lblSlotContent = new Label(slotDevice.toString());
                                                            HorizontalLayout lytInfo = new HorizontalLayout(lblSlotContentTitle, lblSlotContent);
                                                         
                                                            VerticalLayout lytContent = new VerticalLayout(lytInfo, mxGraphSlot, btnClose);
                                                            lytContent.setSpacing(true);
                                                            lytContent.setPadding(true);
                                                            dlgSlotContent.add(lytContent);
                                                            
                                                            renderShape(slotDevice, slotDeviceStructure, 900, 400, mxGraphSlot, slotNode, false, true);
                                                            
                                                            MxGraphNode dummyNode = new MxGraphNode();
                                                            dummyNode.setGeometry(0, 0, 0, 0);
                                                            mxGraphSlot.addNode(dummyNode);
                                                            //  execute the layout and disable moving when the last cell is added
                                                            dummyNode.addCellAddedListener(eventListener -> {
                                                                mxGraphSlot.setCellsMovable(false);
                                                                mxGraphSlot.endUpdate();                                                                
                                                            });
                                                            dlgSlotContent.open();
                                                        }
                                                        
                                                    } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                                                        Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                                                    }
                                                }
                                            });
                                            nodeShape.setVerticalLabelPosition(MxConstants.ALIGN_CENTER);                                         
                                        }
                                    }
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
                                mxGraph.addNode(nodeShape);
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            
        }
    }
    
    @Override
    public void buildWithSavedView(byte[] view) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void buildWithBusinessObject(Object businessObject) {
        if (businessObject != null) {
            List<BusinessObject> devices = loadDevices((BusinessObjectLight) businessObject);
            if (devices == null) 
                return;
            
            BusinessObjectViewNode equipment;           
            this.viewMap = new ViewMap();
            for (BusinessObject device : devices) {
                equipment = new BusinessObjectViewNode(device);
                viewMap.addNode(equipment);
            }
        }
    }
    
    private List<BusinessObject> loadDevices(BusinessObjectLight businessObject) {
        String message = "";
        try {
            BusinessObject rack = bem.getObject(businessObject.getClassName(), businessObject.getId());
            
            if (rack == null) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.visualization.rack-view-cant-find-object"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            } else {
                Integer objectRackUnits = (Integer) rack.getAttributes().get(Constants.PROPERTY_RACK_UNITS);
                if (objectRackUnits == null || objectRackUnits <= 0) {
                    message += String.format(ts.getTranslatedString("modopule.visualization.rack-view-rack-attribute-doesnt-exist"), Constants.PROPERTY_RACK_UNITS, rack);
                } else {
                    List<BusinessObjectLight> devicesLight = bem.getObjectChildren(rack.getClassName(), rack.getId(), -1);
                    if (devicesLight != null) {
                        List<BusinessObject> devices = new ArrayList<>();
                        
                        for (BusinessObjectLight deviceLight : devicesLight) {
                            BusinessObject device = bem.getObject(deviceLight.getClassName(), deviceLight.getId());
                            if (device != null) {
                                int devicePosition = 0, deviceRackUnits = 0;
                                if (!device.getAttributes().containsKey(Constants.PROPERTY_POSITION)) 
                                    message += String.format(ts.getTranslatedString("module.visualization.rack-view-rack-attribute-doesnt-exist-in-class"), Constants.PROPERTY_POSITION, device.getClassName());
                                  else {
                                    try {
                                        devicePosition = ((int) device.getAttributes().get(Constants.PROPERTY_POSITION));
                                    } catch (Exception e) {
                                        message += String.format(ts.getTranslatedString("module.visualization.rack-view-rack-attribute-must-be-integer"), Constants.PROPERTY_POSITION, device.getClassName());
                                    }
                                }
                                if (!device.getAttributes().containsKey(Constants.PROPERTY_RACK_UNITS)) 
                                    message += String.format(ts.getTranslatedString("module.visualization.rack-view-rack-attribute-doesnt-exist-in-class"), Constants.PROPERTY_RACK_UNITS, device.getClassName());
                                 else {
                                    try {
                                        deviceRackUnits = ((int) device.getAttributes().get(Constants.PROPERTY_RACK_UNITS));
                                    } catch (Exception e) {
                                        message += String.format(ts.getTranslatedString("module.visualization.rack-view-rack-attribute-must-be-integer"), Constants.PROPERTY_RACK_UNITS, device.getClassName());
                                    }
                                }
                                if (!message.isEmpty()) 
                                    break;
                                
                                devices.add(device);
                                
                                if (devicePosition < 0) 
                                    message += String.format(ts.getTranslatedString("module.visualization.rack-view-rack-value-greater-zero"), Constants.PROPERTY_POSITION, device.toString());
                                 else {
                                    if (devicePosition > objectRackUnits) 
                                        message += String.format(ts.getTranslatedString("module.visualization.rack-view-rack-value-greater-number-rack-units"), Constants.PROPERTY_POSITION, device.toString());    
                                }
                                
                                if (deviceRackUnits < 0) 
                                    message += String.format(ts.getTranslatedString("module.visualization.rack-view-rack-value-greater-zero"), Constants.PROPERTY_RACK_UNITS, device.toString());
                                 else {
                                    if (deviceRackUnits > objectRackUnits) 
                                        message += String.format(ts.getTranslatedString("module.visualization.rack-view-rack-value-greater-number-rack-units"), Constants.PROPERTY_RACK_UNITS, device.toString());
                                    
                                }
                            } else 
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), String.format("%s : %s", ts.getTranslatedString("module.visualization.rack-view-rack-cant-find-child-object") , deviceLight), 
                                        AbstractNotification.NotificationType.INFO, ts).open();
                            
                        }
                        if (message.isEmpty()) {
                            HashMap<Integer, BusinessObjectLight> rackUnitsMap = new HashMap();
                            
                            for (BusinessObject device : devices) {
                                int devicePosition = (int) device.getAttributes().get(Constants.PROPERTY_POSITION);
                                int deviceRackUnits = (int) device.getAttributes().get(Constants.PROPERTY_RACK_UNITS);
                                
                                for (int i = devicePosition; i < devicePosition + deviceRackUnits; i++) {
                                    if (!rackUnitsMap.containsKey(devicePosition)) {
                                        rackUnitsMap.put(i, device);
                                    } else {
                                        BusinessObjectLight lol = rackUnitsMap.get(devicePosition);
                                        
                                        if (!lol.equals(device)) {
                                            message += String.format("module.visualization.rack-view-rack-position-used-by", i, device.toString(), lol.toString());
                                        }
                                    }
                                }
                            }
                            return devices;
                        } else {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), String.format("%s %s", ts.getTranslatedString("module.visualization.rack-view-error-building-view"), message), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                            return null;
                        }
                    }
                }
            }
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), message, 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            return null;
        } catch (BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException e) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), String.format("%s %s", ts.getTranslatedString("module.visualization.rack-view-error-building-view"), e), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            return null;
        }
    }
    
    private void addDefaultLayout(BusinessObjectLight parentObject, MxGraphNode parentNode) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        List<BusinessObjectLight> childrens = bem.getObjectChildren(parentObject.getClassName(), parentObject.getId(), -1);
        List<BusinessObjectLight> ports = new ArrayList<>();
        List<BusinessObjectLight> oTherChildren = new ArrayList<>();
        
        for (BusinessObjectLight children : childrens) {
            if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, children.getClassName())) {
                ports.add(children);
            } else {
                oTherChildren.add(children);
            }
        }

        //add the ports
        if (!ports.isEmpty()) {
            MxGraphNode portsNode = new MxGraphNode();
            
            portsNode.setGeometry(0, 0, DEFAULT_PORT_SIZE * ports.size(), 50);
            portsNode.setCellParent(parentNode.getUuid());
            portsNode.setVerticalLabelPosition(MxConstants.ALIGN_CENTER);
            mxGraph.addNode(portsNode);
            
            MxGraphNode portNode = new MxGraphNode();
            for (BusinessObjectLight port : ports) {
                portNode = new MxGraphNode();
                
                if (!bem.getSpecialAttribute(port.getClassName(), port.getId(), "endpointA").isEmpty() || !bem.getSpecialAttribute(port.getClassName(), port.getId(), "endpointB").isEmpty()) {
                    portNode.setFillColor("red");  // -red- its the default color to connected ports
                } else {
                    ClassMetadata theClass = mem.getClass(port.getClassName());
                    portNode.setFillColor(UtilHtml.toHexString(new Color(theClass.getColor())));                    
                }                
                portNode.setGeometry(0, 0, DEFAULT_PORT_SIZE, DEFAULT_PORT_SIZE);
                portNode.setCellParent(portsNode.getUuid());
                portNode.setVerticalLabelPosition(MxConstants.ALIGN_CENTER);                
                mxGraph.addNode(portNode);
            }
            portNode.addCellAddedListener(evt -> {
                mxGraph.executeStackLayout(portsNode.getUuid(), true, 5, 10);
                if (oTherChildren.isEmpty()) {  // by last, execute the layout in the parent if the device has only ports                   
                    mxGraph.executeStackLayout(parentNode.getUuid(), true, 5, 30);                    
                    
                }
            });            
        }
        //add the ports
        if (!oTherChildren.isEmpty()) {
            MxGraphNode childrenNode = new MxGraphNode();
            for (BusinessObjectLight children : oTherChildren) {
                childrenNode = new MxGraphNode();
                
                childrenNode.setGeometry(0, 0, 10, 50);
                childrenNode.setLabel(children.toString());
                childrenNode.setCellParent(parentNode.getUuid());
                childrenNode.setVerticalLabelPosition(MxConstants.ALIGN_CENTER);
                mxGraph.addNode(childrenNode);
                addDefaultLayout(children, childrenNode);
            }
            childrenNode.addCellAddedListener(evt -> {
                mxGraph.executeStackLayout(parentNode.getUuid(), true, 5, 30);
            });            
        }        
        
    }
    
    @Override
    public void buildEmptyView() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public AbstractViewNode addNode(Object businessObject, Properties properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public AbstractViewEdge addEdge(Object businessObject, Object sourceBusinessObject, Object targetBusinessObject, Properties properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void removeNode(Object businessObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void removeEdge(Object businessObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void addNodeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void addEdgeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Load the layout devices under the current rack
     */
    public void loadDeviceLayouts() {        
        try {          
            byte[] structure = aem.getDeviceLayoutStructure(businessObject.getId(), businessObject.getClassName());          
            if (structure == null) 
                return;
            
//          <editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
//            try {
//                FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/device_structure" + businessObject.getId() + ".xml");
//                fos.write(structure);
//                fos.close();
//            } catch (IOException e) {
//            }
//                     </editor-fold>

            layoutDevices = new HashMap();
            
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            XMLStreamReader xmlsr = xmlif.createXMLStreamReader(bais);
            
            QName tagDevice = new QName("device"); //NOI18N
            QName tagModel = new QName("model"); //NOI18N
            QName tagView = new QName("view"); //NOI18N
            QName tagStructure = new QName("structure"); //NOI18N

            while (xmlsr.hasNext()) {
                int event = xmlsr.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (xmlsr.getName().equals(tagDevice)) {
                        String id = xmlsr.getAttributeValue(null, Constants.PROPERTY_ID);
                        
                        if (!id.equals(businessObject.getId())) {
                            String className = xmlsr.getAttributeValue(null, Constants.PROPERTY_CLASS_NAME);
                            String name = xmlsr.getAttributeValue(null, Constants.PROPERTY_NAME);
                            String parentId = xmlsr.getAttributeValue(null, "parentId"); //NOI18N
                            BusinessObject theObject = new BusinessObject(className, id, name);
                            
                            if (xmlsr.hasNext()) {
                                event = xmlsr.next();
                                
                                if (event == XMLStreamConstants.START_ELEMENT) {
                                    if (xmlsr.getName().equals(tagModel)) {
                                        
                                        if (xmlsr.hasNext()) {
                                            event = xmlsr.next();
                                            
                                            if (event == XMLStreamConstants.START_ELEMENT) {
                                                if (xmlsr.getName().equals(tagView)) {
                                                    
                                                    if (xmlsr.hasNext()) {
                                                        event = xmlsr.next();
                                                        if (event == XMLStreamConstants.START_ELEMENT) {
                                                            if (xmlsr.getName().equals(tagStructure)) {
                                                                byte[] modelStructure = DatatypeConverter.parseBase64Binary(xmlsr.getElementText());
                                                                layoutDevices.put(theObject, modelStructure);
                                                            }
                                                        }
                                                        
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }          
            xmlsr.close();
        } catch (XMLStreamException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.visualization.rack-view-error-retrieving-layouts"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
    * Represents a block in a rack view. Stores the MxgraphNode that encloses one
    * rack unit or more in the case of devices using more than one rack unit
    * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
    */
   private class RackSegment {

       private MxGraphNode segmentNode;
       private int initialPosition;
       private int finalPosition;
       private List<BusinessObject> devices;

       public int getInitialPosition() {
           return initialPosition;
       }

       public void setInitialPosition(int initialPosition) {
           this.initialPosition = initialPosition;
       }

       public int getFinalPosition() {
           return finalPosition;
       }

       public void setFinalPosition(int finalPosition) {
           this.finalPosition = finalPosition;
       }

       public List<BusinessObject> getDevices() {
           return devices;
       }

       public void setDevices(List<BusinessObject> devices) {
           this.devices = devices;
       }

       public MxGraphNode getSegmentNode() {
           return segmentNode;
       }

       public RackSegment() {
       } 

       public void setSegmentNode(MxGraphNode segmentNode) {
           this.segmentNode = segmentNode;
       }

       public RackSegment(MxGraphNode segmentNode, int initialPosition, int finalPosition, List<BusinessObject> devices) {
           this.segmentNode = segmentNode;
           this.initialPosition = initialPosition;
           this.finalPosition = finalPosition;
           this.devices = devices;
       }

       @Override
       public boolean equals(Object o) {
           if (o == null)
               return false;
           if((o instanceof MxGraphNode))       
               return segmentNode == null ? false : segmentNode.equals(o);
           if(!(o instanceof RackSegment))
               return false;
           return segmentNode == null ? false : segmentNode.equals(((RackSegment)o).getSegmentNode());
       }

       @Override
       public int hashCode() {
           int hash = 3;
           hash = 23 * hash + Objects.hashCode(this.segmentNode);
           return hash;
       }

       @Override
       public String toString() {
           return initialPosition + "-" + finalPosition + " -> " + devices; 
       }
   }
}
