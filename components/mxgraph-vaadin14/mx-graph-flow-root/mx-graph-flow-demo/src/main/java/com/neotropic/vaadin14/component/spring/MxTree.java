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
package com.neotropic.vaadin14.component.spring;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <pre>{@code
 *              +-----------------------------+
 * MxTree ----->.                             .
 *              .  ..                         .
 *              .                             .
 *              |  +-----------------------+  |
 * Node ---------->|                       |  |
 *              |  |  +-----------------+  |  |
 * key node --------->|                 |  |  | 
 *              |  |  |  +---+ +-----+  |  |  |
 * Toggle node --------->| > | | Key |<-------- Y node
 *              |  |  |  +---+ +-----+  |  |  |
 *              |  |  |                 |  |  |
 *              |  |  +-----------------+  |  |
 *              |  |                       |  |
 *              |  |  +-----------------+  |  |
 *              |  |  |                 |  |  |
 *              |  |  .  Node 0         .  |  |
 * Subtree node ----->.  ..             .  |  |
 *              |  |  .  Node n         .  |  |
 *              |  |  |                 |  |  |
 *              |  |  +-----------------+  |  |
 *              |  +-----------------------+  |
 *              .                             .
 *              .  ..                         .
 *              .                             .
 *              +-----------------------------+
 * }</pre>
 * A MxTree has a set of Nodes.
 * A Node has a Key node and a Subtree node.
 * A Key node has a Toggle node and a Y node.
 * A Subtree node has a set of Nodes.
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MxTree<T> extends MxGraphNode {
    /**
     * Constants
     */
    private final int VERTICAL_SPACING = 0;
    private final int HORIZONTAL_SPACING = 5;
    private final int MARGIN_LEFT = 20;
    private final int TOGGLE_WIDTH = 16;
    private final int TOGGLE_HEIGHT= 16;
    
    private final LinkedHashMap<String, String> rectangleStyle = new LinkedHashMap();
    private final LinkedHashMap<String, String> labelStyle = new LinkedHashMap();
    private final LinkedHashMap<String, String> angleDownStyle = new LinkedHashMap();
    private final LinkedHashMap<String, String> angleRightStyle = new LinkedHashMap();
    {
        rectangleStyle.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        rectangleStyle.put(MxConstants.STYLE_FILL_OPACITY, "0");
        rectangleStyle.put(MxConstants.STYLE_STROKE_OPACITY, "0");
        rectangleStyle.put(MxConstants.STYLE_EDITABLE, "0");
        
        labelStyle.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_LABEL);
        labelStyle.put(MxConstants.STYLE_FILL_OPACITY, "0");
        labelStyle.put(MxConstants.STYLE_STROKE_OPACITY, "0");
        labelStyle.put(MxConstants.STYLE_EDITABLE, "0");
        
        angleDownStyle.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_IMAGE);
        angleDownStyle.put(MxConstants.STYLE_STROKE_OPACITY, "0");
        angleDownStyle.put(MxConstants.STYLE_IMAGE, "images/angle-down.svg"); //NOI18N
        angleDownStyle.put(MxConstants.STYLE_IMAGE_WIDTH, "16");
        angleDownStyle.put(MxConstants.STYLE_IMAGE_HEIGHT, "16");
        angleDownStyle.put(MxConstants.STYLE_EDITABLE, "0");
        
        angleRightStyle.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_IMAGE);
        angleRightStyle.put(MxConstants.STYLE_STROKE_OPACITY, "0");
        angleRightStyle.put(MxConstants.STYLE_IMAGE, "images/angle-right.svg"); //NOI18N
        angleRightStyle.put(MxConstants.STYLE_IMAGE_WIDTH, "16");
        angleRightStyle.put(MxConstants.STYLE_IMAGE_HEIGHT, "16");
        angleRightStyle.put(MxConstants.STYLE_EDITABLE, "0");
    }
    private final LinkedHashMap<T, T> parents = new LinkedHashMap();
    private final HashMap<T, MxGraphNode> nodes = new HashMap();
    private final HashMap<T, MxGraphNode> keyNodes = new HashMap();
    private final HashMap<T, MxGraphNode> subtreeNodes = new HashMap();
    private final Function<T, List<T>> functionChildren; 
    private final MxGraph graph;
    
    public MxTree(MxGraph graph, Supplier<List<T>> supplierRoots, Function<T, List<T>> functionChildren) {
        super();
        this.graph = graph;
        this.functionChildren = functionChildren;
                
        this.setRawStyle(rectangleStyle);
        setIsSelectable(false);
        graph.add(this);
        addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            this.overrideStyle();
            graph.setCellsLocked(true);
            
            event.unregisterListener();
        });
        if (supplierRoots != null) {
            List<T> roots = supplierRoots.get();
            if (roots != null)
                roots.forEach(root -> addNode(root, this));
        }
    }
    
    private void addNode(T key, MxGraphNode parentNode) {
        MxGraphNode node = new MxGraphNode();
        node.setIsSelectable(false);
        nodes.put(key, node);
        
        node.setRawStyle(rectangleStyle);
        node.setCellParent(parentNode.getUuid());
        
        graph.add(node);
        
        node.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            node.overrideStyle();
            graph.setCellsLocked(true);
            
            event.unregisterListener();
        });
        addKeyNode(key, node);
    }
    
    private void addKeyNode(T key, MxGraphNode node) {
        MxGraphNode keyNode = new MxGraphNode();
        node.setIsSelectable(false);
        keyNodes.put(key, keyNode);
        
        keyNode.setRawStyle(rectangleStyle);
        keyNode.setCellParent(node.getUuid());
        
        graph.add(keyNode);
        
        keyNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            keyNode.overrideStyle();
            graph.setCellsLocked(true);
            
            event.unregisterListener();
        });
        addToggleNode(key, node, keyNode);
    }
    
    private void addSubtreeNode(T key, MxGraphNode node) {
        MxGraphNode subtreeNode = new MxGraphNode();
        subtreeNode.setIsSelectable(false);
        subtreeNodes.put(key, subtreeNode);
        
        subtreeNode.setRawStyle(rectangleStyle);
        subtreeNode.setCellParent(node.getUuid());
        
        graph.add(subtreeNode);
        
        subtreeNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            subtreeNode.overrideStyle();
            graph.setCellsLocked(true);
            
            event.unregisterListener();
        });
    }
    
    private void addToggleNode(T key, MxGraphNode node, MxGraphNode keyNode) {
        ToggleNode toggleNode = new ToggleNode();
        
        toggleNode.setGeometry(0, 0, TOGGLE_WIDTH, TOGGLE_HEIGHT);
        toggleNode.setRawStyle(angleRightStyle);
        toggleNode.setCellParent(keyNode.getUuid());
        
        graph.add(toggleNode);
        
        toggleNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            toggleNode.overrideStyle();
            graph.setCellsLocked(true);
            
            event.unregisterListener();
        });
        toggleNode.addClickCellListener(event -> {
            
            if (toggleNode.checked()) {
                toggleNode.cheked(false);
                toggleNode.setRawStyle(angleRightStyle);
                toggleNode.overrideStyle();
                
            } else {
                toggleNode.cheked(true);
                toggleNode.setRawStyle(angleDownStyle);
                toggleNode.overrideStyle();
                
                if (functionChildren != null) {
                    List<T> keyChildren = functionChildren.apply(key);
                    if (keyChildren != null) {
                        keyChildren.forEach(child -> {
                            parents.put(child, key);
                            
                            MxGraphNode subtreeNode = subtreeNodes.get(key);
                            if (subtreeNode == null) {
                                addSubtreeNode(key, node);
                                subtreeNode = subtreeNodes.get(key);
                            }
                            addNode(child, subtreeNode);
                            
                        });
                    }
                }
            }
        });
        buildY(key, keyNode);
    }
    
    private void buildY(T key, MxGraphNode keyNode) {
        MxGraphNode nodeY = new MxGraphNode();
        nodeY.setRawStyle(labelStyle);
        nodeY.setLabel(key.toString());
        nodeY.setCellParent(keyNode.getUuid());
        
        graph.add(nodeY);
        
        nodeY.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            nodeY.overrideStyle();
            nodeY.updateCellSize();
            executeLayout(key);
            graph.setCellsLocked(true);
            
            event.unregisterListener();
        });
    }
    
    private void executeLayout(T key) {
        if (keyNodes.containsKey(key)) {
            
            MxGraphNode keyNode = keyNodes.get(key);
            if (keyNode != null) {
                graph.executeStackLayout(keyNode.getUuid(), true, HORIZONTAL_SPACING);
                keyNode.updateCellSize();
                
                if (nodes.containsKey(key)) {
                    
                    MxGraphNode node = nodes.get(key);
                    if (node != null) {
                        graph.executeStackLayout(node.getUuid(), false, 0);
                        executeLayoutRecursively(key);
                    }
                }
            }
        }
    }
    
    private void executeLayoutRecursively(T key) {
        if (parents.containsKey(key)) {
            T parent = parents.get(key);
            if (subtreeNodes.containsKey(parent)) {
                MxGraphNode subtreeNode = subtreeNodes.get(parent);
                if (subtreeNode != null) {
                    graph.executeStackLayout(subtreeNode.getUuid(), false, VERTICAL_SPACING, 0, 0, 0, MARGIN_LEFT);

                    if (nodes.containsKey(parent)) {
                        MxGraphNode node = nodes.get(parent);
                        graph.executeStackLayout(node.getUuid(), false, VERTICAL_SPACING);
                        executeLayoutRecursively(parent);
                    }
                }
            }
        }
        else
            graph.executeStackLayout(this.getUuid(), false, VERTICAL_SPACING, 0, 0, 0, MARGIN_LEFT);            
    }
    
    private class ToggleNode extends MxGraphNode {
        private boolean checked = false;
        
        public ToggleNode() {
            super();
        }
        
        public void cheked(boolean checked) {
            this.checked = checked;
        }
        
        public boolean checked() {
            return checked;
        }
    }
}
