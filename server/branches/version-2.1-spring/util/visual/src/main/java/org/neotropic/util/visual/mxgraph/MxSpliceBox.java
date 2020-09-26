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
package org.neotropic.util.visual.mxgraph;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Function;

/**
 * Mx Splice Box
 * <pre>{@code
 * +-------------------------------+
 * |                               |
 * |  +----------+  +-----------+  |
 * |  |  +----+  |  |  +-----+  |  |
 * |  |  | in |  |  |  | out |  |  |
 * |  |  +----+  |  |  +-----+  |  |
 * |  .          .  .           .  |
 * |  .          .  .           .  |
 * |  .          .  .           .  |
 * |  +----------+  +-----------+  |
 * |                               |
 * +-------------------------------+
 * }</pre>
 * A splice box is a node with two nodes that are located using a horizontal 
 * stack layout; those contain a set of nodes to represent the in/out ports.
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 * @param <T> Splice box and port object type
 */
public class MxSpliceBox<T> extends MxGraphNode {
    //<editor-fold defaultstate="collapsed" desc="Style Constants.">
    private final int CHARACTER_LIMIT = 30;
    private final int LABEL_WIDTH = 210;
    private final int LABEL_HEIGHT = 40;
    private final int SPACING = 10;
    private final int MARGIN = SPACING;
    private final String STROKE_COLOR = "#000000";
    private final String FILL_COLOR = "#C0C0C0";
    private final String FONT_COLOR = "#000000";
    private final LinkedHashMap<String, String> SPLICE_BOX_NODE_STYLE = new LinkedHashMap();
    {
        SPLICE_BOX_NODE_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        SPLICE_BOX_NODE_STYLE.put(MxConstants.STYLE_VERTICAL_ALIGN, MxConstants.ALIGN_TOP);
        SPLICE_BOX_NODE_STYLE.put(MxConstants.STYLE_SPACING_TOP, "10");
        SPLICE_BOX_NODE_STYLE.put(MxConstants.STYLE_STROKECOLOR, STROKE_COLOR);
        SPLICE_BOX_NODE_STYLE.put(MxConstants.STYLE_FILLCOLOR, FILL_COLOR);
        SPLICE_BOX_NODE_STYLE.put(MxConstants.STYLE_FONTCOLOR, FONT_COLOR);
        SPLICE_BOX_NODE_STYLE.put(MxConstants.STYLE_FONTSIZE, "10");
    }
    private final LinkedHashMap<String, String> PORT_SET_NODE_STYLE = new LinkedHashMap();
    {
        PORT_SET_NODE_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        PORT_SET_NODE_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        PORT_SET_NODE_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
    }
    private final LinkedHashMap<String, String> PORT_NODE_STYLE = new LinkedHashMap();
    {
        PORT_NODE_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_LABEL);
        PORT_NODE_STYLE.put(MxConstants.STYLE_ALIGN, MxConstants.ALIGN_LEFT);
        PORT_NODE_STYLE.put(MxConstants.STYLE_SPACING_LEFT, "10");
        PORT_NODE_STYLE.put(MxConstants.STYLE_STROKECOLOR, STROKE_COLOR);
        PORT_NODE_STYLE.put(MxConstants.STYLE_FILLCOLOR, FILL_COLOR);
        PORT_NODE_STYLE.put(MxConstants.STYLE_FONTCOLOR, FONT_COLOR);
        PORT_NODE_STYLE.put(MxConstants.STYLE_FONTSIZE, "10");
    }
    private final LinkedHashMap<String, String> NULL_PORT_STYLE = new LinkedHashMap();
    {
        NULL_PORT_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        NULL_PORT_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        NULL_PORT_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
    }
    //</editor-fold>
    /**
     * Port in/out set.
     */
    private final LinkedHashMap<T, T> portsInOut;
    /**
     * Function to get the port label.
     */
    private final Function<T, String> funcGetPortLabel;
    /**
     * Function to get the port id.
     */
    private final Function<T, String> funcGetPortId;
    /**
     * Function to get the port color.
     */
    private final Function<T, String> funcGetPortColor;
    /**
     * Function to get whether the cell is connectable.
     * If is null all the ports are connectable
     */
    private final Function<T, Boolean> funcGetPortConnectable;
    /**
     * Function to get a port node
     */
    private final Function<T, MxGraphNode> funcGetPortNode;
    /**
     * Function to get the port tooltip
     */
    private final Function<T, String> funcGetPortTooltip;
    
    private final MxGraph graph;
    private MxGraphNode portInSetNode;
    private MxGraphNode portOutSetNode;
    private int portsOutAdded = 0;
    
    public MxSpliceBox(MxGraph graph, LinkedHashMap<T, T> portsInOut, String spliceBoxLabel, String spliceBoxColor, 
        Function<T, String> funcGetPortLabel, Function<T, String> funcGetPortId, Function<T, String> funcGetPortColor,
        Function<T, Boolean> funcGetPortConnectable, Function<T, MxGraphNode> funcGetPortNode, Function<T, String> funcGetPortTooltip) {
        super();
        Objects.requireNonNull(graph);
        Objects.requireNonNull(portsInOut);
        this.graph = graph;
        this.portsInOut = portsInOut;
        this.funcGetPortLabel = funcGetPortLabel;
        this.funcGetPortId = funcGetPortId;
        this.funcGetPortColor = funcGetPortColor;
        this.funcGetPortConnectable = funcGetPortConnectable;
        this.funcGetPortNode = funcGetPortNode;
        this.funcGetPortTooltip = funcGetPortTooltip;
        
        if (spliceBoxLabel != null)
            setLabel(spliceBoxLabel);
        setGeometry(0, 0, 
            2 * MARGIN + 2 * LABEL_WIDTH + SPACING, 
            4 * MARGIN + portsInOut.size() * LABEL_HEIGHT + (portsInOut.size() - 1) * SPACING
        );
        if (spliceBoxColor != null)
            SPLICE_BOX_NODE_STYLE.put(MxConstants.STYLE_FILLCOLOR, spliceBoxColor);
        setRawStyle(SPLICE_BOX_NODE_STYLE);
        
        addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            setIsSelectable(false);
            setConnectable(false);
            overrideStyle();
            addPortInSetNode();
            graph.setCellsLocked(true);
            //The cell is added only once, eliminating the unnecessary listener.
            event.unregisterListener();
        });
        graph.addNode(this);
    }
    
    private void addPortInSetNode() {
        portInSetNode = new MxGraphNode();
        
        portInSetNode.setGeometry(0, 0,
            LABEL_WIDTH, portsInOut.size() * LABEL_HEIGHT + (portsInOut.size() - 1) * SPACING);
        portInSetNode.setRawStyle(PORT_SET_NODE_STYLE);
        portInSetNode.setCellParent(this.getUuid());
        
        portInSetNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            portInSetNode.setIsSelectable(false);
            portInSetNode.setConnectable(false);
            portInSetNode.overrideStyle();
            addPortOutSetNode();
            graph.setCellsLocked(true);
            //The cell is added only once, eliminating the unnecessary listener.
            event.unregisterListener();
        });
        graph.addNode(portInSetNode);
    }
    
    private void addPortOutSetNode() {
        portOutSetNode = new MxGraphNode();
        
        portOutSetNode.setGeometry(0, 0, 
            LABEL_WIDTH, portsInOut.size() * LABEL_HEIGHT + (portsInOut.size() - 1) * SPACING);
        portOutSetNode.setRawStyle(PORT_SET_NODE_STYLE);
        portOutSetNode.setCellParent(this.getUuid());
        
        portOutSetNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            portOutSetNode.setIsSelectable(false);
            portOutSetNode.setConnectable(false);
            portOutSetNode.overrideStyle();
            graph.setCellsLocked(true);
            portsInOut.forEach((inPort, outPort) -> addPortInNode(inPort, outPort));
            //The cell is added only once, eliminating the unnecessary listener.
            event.unregisterListener();
        });
        graph.addNode(portOutSetNode);
    }
    
    private void addPortInNode(T portIn, T portOut) {
        MxGraphNode portInNode = getPortNode(portIn);
        portInNode.setCellParent(portInSetNode.getUuid());
        
        portInNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            portInNode.overrideStyle();
            portInNode.setConnectable(getPortConnectable(portIn));
            portInNode.setTooltip(getPortTooltip(portIn));
                
            graph.setCellsLocked(true);
            addPortOutNode(portOut);
            //The cell is added only once, eliminating the unnecessary listener.
            event.unregisterListener();
        });
        graph.addNode(portInNode);
    }
    
    private void addPortOutNode(T portOut) {
        MxGraphNode portOutNode = getPortNode(portOut);
        portOutNode.setCellParent(portOutSetNode.getUuid());
        
        portOutNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            portOutNode.overrideStyle();
            portOutNode.setConnectable(getPortConnectable(portOut));
            portOutNode.setTooltip(getPortTooltip(portOut));
            
            portsOutAdded++;
            if (portsOutAdded == portsInOut.size()) {
                graph.executeStackLayout(portInSetNode.getUuid(), false, SPACING);
                graph.executeStackLayout(portOutSetNode.getUuid(), false, SPACING);   
                graph.executeStackLayout(this.getUuid(), true, SPACING, MARGIN * 3, MARGIN, MARGIN, MARGIN);
            }
            graph.setCellsLocked(true);
            //The cell is added only once, eliminating the unnecessary listener.
            event.unregisterListener();
        });
        graph.addNode(portOutNode);
    }
    
    private MxGraphNode getPortNode(T port) {
        MxGraphNode portNode = null;
        if (port != null && funcGetPortNode != null)
            portNode = funcGetPortNode.apply(port);
        
        if (portNode == null)
            portNode = new MxGraphNode();
                
        portNode.setGeometry(0, 0, LABEL_WIDTH, LABEL_HEIGHT);
                
        String portId = getPortId(port);
        if (portId != null)
            portNode.setUuid(portId);
        
        String label = getPortLabel(port);
        if (label != null)
            portNode.setLabel(label);
        
        LinkedHashMap<String, String> rawStyle = new LinkedHashMap(port != null ? PORT_NODE_STYLE : NULL_PORT_STYLE);
        String portColor = getPortColor(port);
        if (portColor != null)
            rawStyle.put(MxConstants.STYLE_FILLCOLOR, portColor);
        portNode.setRawStyle(rawStyle);
        
        return portNode;
    }
    
    private String getPortId(T port) {
        return port != null && funcGetPortId != null ? funcGetPortId.apply(port) : null;
    }
    
    private String getPortColor(T port) {
        String portColor = null;
        if (port != null && funcGetPortColor != null) {
            portColor = funcGetPortColor.apply(port);
            if (portColor != null && !portColor.isEmpty())
                return portColor;
        }
        return portColor;
    }
    
    private String getPortLabel(T port) {
        String portLabel = null;
        if  (port != null) {
            portLabel = funcGetPortLabel != null ? funcGetPortLabel.apply(port) : null;
            if (portLabel == null)
                portLabel = port.toString();
            if (portLabel.length() > CHARACTER_LIMIT)
                portLabel = String.format("%s ...", portLabel.substring(0, CHARACTER_LIMIT + 1));
            return portLabel;
        }
        return portLabel;
    }
    
    private Boolean getPortConnectable(T port) {
        if (port == null)
            return false;
        else
            return funcGetPortConnectable != null ? funcGetPortConnectable.apply(port) : true;
    }
    
    private String getPortTooltip(T port) {
        return port != null && funcGetPortTooltip != null ? funcGetPortTooltip.apply(port) : null;
    }
}
