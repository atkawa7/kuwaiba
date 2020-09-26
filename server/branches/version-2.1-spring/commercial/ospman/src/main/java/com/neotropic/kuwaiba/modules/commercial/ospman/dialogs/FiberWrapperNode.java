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
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectNode;

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
    private final String COLOR_LIGHT_GREY = "LightGrey";
    private final String COLOR_WHITE = "White";
    private final int HEIGHT = 16;
    private final int FIBER_WIDTH = 100;
    public static final int FIBER_HEIGHT = 6;
    private final int CUT_WIDTH = 4;
    private final int MARGIN_TOP_BOTTOM = (HEIGHT - FIBER_HEIGHT) / 2;
    private final LinkedHashMap<String, String> NODE_STYLE = new LinkedHashMap();
    {
        NODE_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        NODE_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        NODE_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
    }
    private final LinkedHashMap<String, String> FIBER_STYLE = new LinkedHashMap(NODE_STYLE);
    
    private final MxGraph graph;
    private String color;
    private MxBusinessObjectNode fiberNode;
    private MxBusinessObjectNode fiberANode;
    private MxBusinessObjectNode fiberBNode;
    private Registration registrationFiber;
        
    public FiberWrapperNode(MxGraph graph, BusinessObjectLight fiberObject, BusinessObjectLight fiberAObject, BusinessObjectLight fiberBObject, String color) {
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
        setGeometry(0, 0, FIBER_WIDTH, HEIGHT);
        setRawStyle(NODE_STYLE);
        
        fiberNode = new FiberObjectNode(fiberObject);
        fiberNode.setCellParent(this.getUuid());

        fiberNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            
            if (fiberObject != null)
                graph.executeStackLayout(this.getUuid(), true, 0, MARGIN_TOP_BOTTOM, 0, MARGIN_TOP_BOTTOM, 0);
            else
                cutFiber(fiberAObject, fiberBObject, false);
            
            graph.setCellsLocked(true);
            event.unregisterListener();
        });        
        
        addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            this.overrideStyle();
            this.setIsSelectable(false);
            this.setConnectable(false);
            graph.addNode(fiberNode);
            graph.setCellsLocked(true);
            event.unregisterListener();
        });
        graph.addNode(this);
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
        
        fiberANode.addCellAddedListener(fiberAEvent -> {
            graph.setCellsLocked(false);
            
            fiberBNode = new FiberCutObjectNode(fiberBObject);
            fiberBNode.setCellParent(fiberNode.getUuid());
            
            fiberBNode.addCellAddedListener(fiberBEvent -> {
                graph.setCellsLocked(false);
                graph.executeStackLayout(fiberNode.getUuid(), false, CUT_WIDTH);
                graph.executeStackLayout(this.getUuid(), true, 0);
                graph.setCellsLocked(true);
                fiberBEvent.unregisterListener();
            });
            graph.addNode(fiberBNode);
            
            graph.setCellsLocked(true);
            fiberAEvent.unregisterListener();
        });
        graph.addNode(fiberANode);
    }
    
    public Registration addFiberRightClickListener(ComponentEventListener<MxGraphRightClickCellEvent> event) {
        return registrationFiber = fiberNode.addRightClickCellListener(event);
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
    }
    /**
     * Fiber Object as MxGraph Node
     */
    public class FiberObjectNode extends FiberNode {
        public FiberObjectNode(BusinessObjectLight fiberObject) {
            super(fiberObject);
            setGeometry(0, 0, FIBER_WIDTH, FIBER_HEIGHT);
            addCellAddedListener(event -> {
                graph.setCellsLocked(false);
                
                if (fiberObject != null) {
                    setRawStyle(FIBER_STYLE);
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
    public class FiberCutObjectNode extends FiberNode {
        public FiberCutObjectNode(BusinessObjectLight fiberObject) {
            super(fiberObject);
            setGeometry(0, 0, FIBER_WIDTH, FIBER_HEIGHT);
            setRawStyle(FIBER_STYLE);
            
            addCellAddedListener(event -> {
                graph.setCellsLocked(false);
                overrideStyle();
                setTooltip(fiberObject.getName());
                graph.setCellsLocked(true);
                event.unregisterListener();
            });
        }
    }
}
