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

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphRightClickCellEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectNode;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * The fiber node is a set of nodes like shown below.
 * <pre>{@code
 *                 +------------------
 * Fiber Wrapper ->|        ^         
 *                 |   Margin Top     
 *                 |        v         
 *                 | +----------------
 * Fiber ----------->| +--------------
 *                 | | | Fiber A      
 *                 | | +--------------
 *                 | |      ^         
 *                 | |     Cut        
 *                 | |      v         
 *                 | | +--------------
 *                 | | | Fiber B      
 *                 | | +--------------
 *                 | +----------------
 *                 |        ^         
 *                 |  Margin Bottom   
 *                 |        v         
 *                 +------------------
 * }</pre>
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FiberWrapperNode extends MxBusinessObjectNode {
    public static final String ATTR_ENDPOINT_A = "endpointA"; //NOI18N
    public static final String ATTR_ENDPOINT_B = "endpointB"; //NOI18N
    public static final int FIBER_HEIGHT = 6;
    public static final int FIBER_FILL_OPACITY = 100;
    public static final int FIBER_SPLICED_FILL_OPACITY = 25;
    public static final String ATTR_COLOR = "color"; //NOI18N
    public static final String ATTR_VALUE = "value"; //NOI18N
    public static final String INFO_OVERLAY_ID = "info"; //NOI18N
    public static final int INFO_WIDTH = 16;
    public static final int INFO_HEIGHT = INFO_WIDTH;
    public static final int INFO_SPACING = 6;
    public static final String INFO_IMG = "img/info-circle-o.svg"; //NOI18N
    public static final String COLOR_LIGHT_GREY = "LightGrey"; //NOI18N
    private final String COLOR_WHITE = "White"; //NOI18N
    private final int HEIGHT = 16;
    private final int FIBER_WIDTH = 100;
    private final int CUT_WIDTH = 4;
    private final int MARGIN_TOP_BOTTOM = (HEIGHT - FIBER_HEIGHT) / 2;
    private final String FOLDABLE = String.valueOf(0);
    private final LinkedHashMap<String, String> NODE_STYLE = new LinkedHashMap();
    {
        NODE_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        NODE_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        NODE_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
        NODE_STYLE.put(MxConstants.STYLE_FOLDABLE, FOLDABLE);
    }
    private final LinkedHashMap<String, String> FIBER_STYLE = new LinkedHashMap(NODE_STYLE);
    
    private final MxGraph graph;
    private String color;
    private FiberNode fiberNode;
    private FiberNode fiberANode;
    private FiberNode fiberBNode;
    private Registration registrationFiber;
    /**
     * Reference to the Business Entity Manager
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the Translation Service
     */
    private final TranslationService ts;
    
    public FiberWrapperNode(MxGraph graph,
        BusinessObjectLight fiberObject, BusinessObjectLight fiberAObject, BusinessObjectLight fiberBObject,
        String color, BusinessEntityManager bem, TranslationService ts) {
        
        super(fiberObject);
        if (graph == null)
            Objects.requireNonNull(graph);
        
        if (fiberObject == null) {
            Objects.requireNonNull(fiberAObject);
            Objects.requireNonNull(fiberBObject);
        }
        if (color != null) {
            this.color = color.toLowerCase().equals(COLOR_WHITE.toLowerCase()) ? COLOR_LIGHT_GREY : color;
            FIBER_STYLE.put(MxConstants.STYLE_FILLCOLOR, this.color);
        }
        this.graph = graph;
        this.bem = bem;
        this.ts = ts;
        setGeometry(0, 0, FIBER_WIDTH, HEIGHT);
        setRawStyle(NODE_STYLE);
        
        fiberNode = new FiberObjectNode(fiberObject);
        fiberNode.setCellParent(this.getUuid());

        fiberNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            
            if (fiberObject != null)
                graph.executeStackLayout(this.getUuid(), true, 0, MARGIN_TOP_BOTTOM, 0, MARGIN_TOP_BOTTOM, 0);
            
            graph.setCellsLocked(true);
            event.unregisterListener();
        });        
        
        addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            
            this.overrideStyle();
            this.setIsSelectable(false);
            this.setConnectable(false);
            
            graph.setCellsLocked(true);
            event.unregisterListener();
        });
        graph.addCell(this);
        graph.addNode(fiberNode);
        if (fiberObject == null)
            cutFiber(fiberAObject, fiberBObject, false);
    }
    public String getColor() {
        return color;
    }
    public void cutFiber(BusinessObjectLight fiberAObject, BusinessObjectLight fiberBObject, boolean newCut) {
        Objects.requireNonNull(fiberAObject);
        Objects.requireNonNull(fiberBObject);
        if (registrationFiber != null)
            registrationFiber.remove();
                
        if (newCut) {
            fiberNode.setIsSelectable(false);
            fiberNode.setConnectable(false);
            fiberNode.setRawStyle(NODE_STYLE);
            fiberNode.overrideStyle();
            fiberNode.setTooltip(null);
            fiberNode.setBusinessObject(null);
        }
        fiberANode = new FiberCutObjectNode(fiberAObject);
        fiberANode.setCellParent(fiberNode.getUuid());
        
        fiberBNode = new FiberCutObjectNode(fiberBObject);
        fiberBNode.setCellParent(fiberNode.getUuid());

        fiberBNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            graph.executeStackLayout(fiberNode.getUuid(), false, CUT_WIDTH);
            graph.executeStackLayout(this.getUuid(), true, 0);
            graph.setCellsLocked(true);
            event.unregisterListener();
        });
        graph.addNode(fiberANode);
        graph.addNode(fiberBNode);
    }
    
    public Registration addFiberRightClickListener(ComponentEventListener<MxGraphRightClickCellEvent> event) {
        return registrationFiber = fiberNode.addRightClickCellListener(event);
    }
    /**
     * Gets the fiber color
     * @param fiber A fiber
     * @param aem Reference to the Application Entity Manager
     * @param bem Reference to the Business Entity Manager
     * @param mem Reference to the Metadata Entity Manager
     * @param ts Reference to the Translation Service
     * @return the fiber color
     */
    public static String getColor(BusinessObjectLight fiber, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
        try {
            ClassMetadata fiberClass = mem.getClass(fiber.getClassName());
            if (fiberClass.hasAttribute(ATTR_COLOR)) {
                ClassMetadata colorClass = mem.getClass(fiberClass.getType(ATTR_COLOR));
                if (colorClass.hasAttribute(ATTR_VALUE)) {
                    BusinessObject fiberObject = bem.getObject(fiber.getClassName(), fiber.getId());
                    String colorId = (String) fiberObject.getAttributes().get(ATTR_COLOR);
                    if (colorId != null) {
                        BusinessObject colorObject = aem.getListTypeItem(fiberClass.getType(ATTR_COLOR), colorId);
                        return (String) colorObject.getAttributes().get(ATTR_VALUE);
                    }
                }
            }
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), //NOI18N
                ex.getLocalizedMessage()
            ).open();
        }
        return null;
    }
    /**
     * Fiber Node to get style information
     */
    public class FiberNode extends MxBusinessObjectNode {
        
        public FiberNode(BusinessObjectLight businessObject) {
            super(businessObject);
        }
        public String getFillColor() {
            return FIBER_STYLE.get(MxConstants.STYLE_FILLCOLOR);
        }
        public String getStrokeColor() {
            return FIBER_STYLE.get(MxConstants.STYLE_STROKECOLOR);
        }
        protected void spliceFiber() {
            LinkedHashMap<String, String> fiberStyle = new LinkedHashMap(FIBER_STYLE);
            if (this.getBusinessObject() != null) {
                try {
                    BusinessObjectLight fiberObject = this.getBusinessObject();
                    List<BusinessObjectLight> endpointA = bem.getSpecialAttribute(
                        fiberObject.getClassName(), fiberObject.getId(), ATTR_ENDPOINT_A);
                    List<BusinessObjectLight> endpointB = bem.getSpecialAttribute(
                        fiberObject.getClassName(), fiberObject.getId(), ATTR_ENDPOINT_B);
                    if (!endpointA.isEmpty() || !endpointB.isEmpty()) {
                        if (!endpointA.isEmpty()) {
                            this.addOverlayButton(INFO_OVERLAY_ID, String.format("%s %s",
                                ts.getTranslatedString("module.ospman.mid-span-access.fiber.endpoint-a"), endpointA.get(0).getName()),
                                INFO_IMG, MxConstants.ALIGN_LEFT, MxConstants.ALIGN_MIDDLE, (INFO_WIDTH/2 + INFO_SPACING) * -1, 0, INFO_WIDTH, INFO_HEIGHT);
                        }
                        if (!endpointB.isEmpty()) {
                            this.addOverlayButton(INFO_OVERLAY_ID, String.format("%s %s",
                                ts.getTranslatedString("module.ospman.mid-span-access.fiber.endpoint-b"), endpointB.get(0).getName()),
                                INFO_IMG, MxConstants.ALIGN_RIGHT, MxConstants.ALIGN_MIDDLE, INFO_WIDTH/2 + INFO_SPACING, 0, INFO_WIDTH, INFO_HEIGHT);
                        }
                        fiberStyle.put(MxConstants.STYLE_FILL_OPACITY, String.valueOf(FIBER_SPLICED_FILL_OPACITY));
                        fiberStyle.put(MxConstants.STYLE_STROKECOLOR, this.getFillColor());
                        setIsSelectable(false);
                        setConnectable(false);
                    }
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage()
                    ).open();
                }
            }
            setRawStyle(fiberStyle);
        }
    }
    /**
     * Fiber Object as MxGraph Node
     */
    private class FiberObjectNode extends FiberNode {
        public FiberObjectNode(BusinessObjectLight fiberObject) {
            super(fiberObject);
            setGeometry(0, 0, FIBER_WIDTH, FIBER_HEIGHT);
            addCellAddedListener(event -> {
                graph.setCellsLocked(false);
                
                if (fiberObject != null) {
                    spliceFiber();
                    setTooltip(fiberObject.getName());
                } else {
                    setRawStyle(NODE_STYLE);
                    setIsSelectable(false);
                    setConnectable(false);
                }
                overrideStyle();

                graph.setCellsLocked(true);
                event.unregisterListener();
            });
        }
    }
    /**
     * Cut Fiber Object as MxGraph Node
     */
    private class FiberCutObjectNode extends FiberNode {
        public FiberCutObjectNode(BusinessObjectLight fiberObject) {
            super(fiberObject);
            setGeometry(0, 0, FIBER_WIDTH, FIBER_HEIGHT);
            addCellAddedListener(event -> {
                graph.setCellsLocked(false);
                
                spliceFiber();
                setTooltip(fiberObject.getName());
                overrideStyle();
                
                graph.setCellsLocked(true);
                event.unregisterListener();
            });
        }
    }
}
