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
package com.neotropic.vaadin14.component.spring.splice;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiFunction;
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
 * Toggle node --------->| > | | Key |<-------- Label node
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
    // <editor-fold defaultstate="collapsed" desc="Style Constants">
    private final int VERTICAL_SPACING = 0;
    private final int HORIZONTAL_SPACING = 5;
    private final int MARGIN_LEFT = 20;
    private final int TOGGLE_WIDTH = 16;
    private final int TOGGLE_HEIGHT= 16;
    private final String FONT_COLOR = "#000000";
    private final int CHARACTER_LIMIT = 30;
    private final int LABEL_WIDTH = 210;
    private final int LABEL_HEIGHT = 16;
    
    private final LinkedHashMap<String, String> RECTANGLE_STYLE = new LinkedHashMap();
    {
        RECTANGLE_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        RECTANGLE_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        RECTANGLE_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
    }
    private final LinkedHashMap<String, String> LABEL_STYLE = new LinkedHashMap();
    {
        LABEL_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_LABEL);
        LABEL_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        LABEL_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
        LABEL_STYLE.put(MxConstants.STYLE_FONTCOLOR, FONT_COLOR);
        LABEL_STYLE.put(MxConstants.STYLE_FONTSIZE, "10");
        LABEL_STYLE.put(MxConstants.STYLE_ALIGN, MxConstants.ALIGN_LEFT);
    }
    private final LinkedHashMap<String, String> ANGLE_DOWN_STYLE = new LinkedHashMap();
    {
        ANGLE_DOWN_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_IMAGE);
        ANGLE_DOWN_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
        ANGLE_DOWN_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        ANGLE_DOWN_STYLE.put(MxConstants.STYLE_IMAGE, "images/angle-down.svg"); //NOI18N
        ANGLE_DOWN_STYLE.put(MxConstants.STYLE_IMAGE_WIDTH, String.valueOf(TOGGLE_WIDTH));
        ANGLE_DOWN_STYLE.put(MxConstants.STYLE_IMAGE_HEIGHT, String.valueOf(TOGGLE_HEIGHT));
    }
    private final LinkedHashMap<String, String> ANGLE_RIGHT_STYLE = new LinkedHashMap();
    {
        ANGLE_RIGHT_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_IMAGE);
        ANGLE_RIGHT_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
        ANGLE_RIGHT_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        ANGLE_RIGHT_STYLE.put(MxConstants.STYLE_IMAGE, "images/angle-right.svg"); //NOI18N
        ANGLE_RIGHT_STYLE.put(MxConstants.STYLE_IMAGE_WIDTH, String.valueOf(TOGGLE_WIDTH));
        ANGLE_RIGHT_STYLE.put(MxConstants.STYLE_IMAGE_HEIGHT, String.valueOf(TOGGLE_HEIGHT));
    }
    // </editor-fold>
    private final MxGraph graph;
    private final HashMap<T, MxGraphNode> nodes = new HashMap();
    private final HashMap<T, MxGraphNode> keyNodes = new HashMap();
    private final HashMap<T, MxGraphNode> subtreeNodes = new HashMap();
    
    private final LinkedHashMap<T, T> parents = new LinkedHashMap();
    private final Function<T, List<T>> functionChildren; 
    private final Function<T, String> functionId;
    private final Function<T, String> functionLabel;
    private final BiFunction<T, MxGraph, MxGraphNode> funcGetLabelNode;
    
    public MxTree(MxGraph graph,
        Supplier<List<T>> supplierRoots, Function<T, List<T>> functionChildren,
        Function<T, String> functionLabel,
        BiFunction<T, MxGraph, MxGraphNode> funcGetLabelNode,
        Function<T, String> functionId) {
        
        super();
        this.graph = graph;
        this.functionChildren = functionChildren;
        this.functionLabel = functionLabel;
        this.funcGetLabelNode = funcGetLabelNode;
        this.functionId = functionId;
        
        setRawStyle(RECTANGLE_STYLE);
        setIsSelectable(false);
        
        addCellAddedListener(event -> {
            graph.setCellsLocked(false);            
            this.overrideStyle();
            setConnectable(false);
            graph.setCellsLocked(true);
            
            event.unregisterListener();
        });
        graph.add(this);
        
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
        
        node.setRawStyle(RECTANGLE_STYLE);
        node.setCellParent(parentNode.getUuid());
        
        node.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            node.overrideStyle();
            node.setConnectable(false);
            graph.setCellsLocked(true);
            
            event.unregisterListener();
        });
        graph.add(node);
        addKeyNode(key, node);
    }
    
    private void addKeyNode(T key, MxGraphNode node) {
        MxGraphNode keyNode = new MxGraphNode();
        node.setIsSelectable(false);
        keyNodes.put(key, keyNode);
        
        keyNode.setRawStyle(RECTANGLE_STYLE);
        keyNode.setCellParent(node.getUuid());
        
        keyNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            keyNode.overrideStyle();
            keyNode.setConnectable(false);
            graph.setCellsLocked(true);
            
            event.unregisterListener();
        });
        graph.add(keyNode);
        addToggleNode(key, node, keyNode);
    }
    
    private void addSubtreeNode(T key, MxGraphNode node) {
        MxGraphNode subtreeNode = new MxGraphNode();
        subtreeNode.setIsSelectable(false);
        subtreeNodes.put(key, subtreeNode);
        
        subtreeNode.setRawStyle(RECTANGLE_STYLE);
        subtreeNode.setCellParent(node.getUuid());
        
        subtreeNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            subtreeNode.overrideStyle();
            subtreeNode.setConnectable(false);
            graph.setCellsLocked(true);
            
            event.unregisterListener();
        });
        graph.add(subtreeNode);
    }
    
    private void addToggleNode(T key, MxGraphNode node, MxGraphNode keyNode) {
        ToggleNode toggleNode = new ToggleNode();
        toggleNode.setGeometry(0, 0, TOGGLE_WIDTH, TOGGLE_HEIGHT);
        toggleNode.setRawStyle(ANGLE_RIGHT_STYLE);
        toggleNode.setCellParent(keyNode.getUuid());
        
        toggleNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            toggleNode.overrideStyle();
            toggleNode.setConnectable(false);
            graph.setCellsLocked(true);
            
            event.unregisterListener();
        });
        toggleNode.addClickCellListener(event -> {
            
            if (toggleNode.checked()) {
                graph.setCellsLocked(false);
                toggleNode.cheked(false);
                toggleNode.setRawStyle(ANGLE_RIGHT_STYLE);
                toggleNode.overrideStyle();
                
                collapseRecursively(key);
                expandRecursively(key);
                                
                graph.setCellsLocked(true);
            } else {
                toggleNode.cheked(true);
                toggleNode.setRawStyle(ANGLE_DOWN_STYLE);
                toggleNode.overrideStyle();
                
                if (functionChildren != null && subtreeNodes.get(key) == null) {
                    
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
                graph.setCellsLocked(false);
                expand(key);
                graph.setCellsLocked(true);
            }
        });
        graph.add(toggleNode);
        
        buildLabelNode(key, keyNode);
    }
    
    private MxGraphNode getLabelNode(T key) {
        MxGraphNode labelNode = funcGetLabelNode != null ? funcGetLabelNode.apply(key, graph) : null;
        if (labelNode == null) {
            labelNode = new MxGraphNode();
            labelNode.setGeometry(0, 0, LABEL_WIDTH, LABEL_HEIGHT);
            labelNode.setRawStyle(LABEL_STYLE);
            String id = getKeyId(key);
            if (id != null)
                labelNode.setUuid(id);
            String label = getLabel(key);
            if (label != null)
                labelNode.setLabel(label);
        }
        return labelNode;
    }
    
    private void buildLabelNode(T key, MxGraphNode keyNode) {
        MxGraphNode labelNode = getLabelNode(key);
        labelNode.setCellParent(keyNode.getUuid());
        labelNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            
            if (labelNode.getRawStyle() != null)
                labelNode.overrideStyle();
            
            executeLayoutToKey(key);
            
            graph.setCellsLocked(true);
            
            event.unregisterListener();
        });
        graph.add(labelNode);
    }
    
    private void expand(T key) {
        MxGraphNode subtreeNode = subtreeNodes.get(key);
        if (subtreeNode != null && subtreeNode.getCollapsed()) {
            subtreeNode.setCollapsed(false);
            executeLayoutRecursively(key);
        }
    }
    
    private void collapseRecursively(T key) {
        if (key != null) {
            MxGraphNode subtreeNode = subtreeNodes.get(key);
            subtreeNode.setCollapsed(true);
            subtreeNode.setGeometry();
            graph.executeStackLayout(subtreeNode.getUuid(), false, VERTICAL_SPACING, 0, 0, 0, MARGIN_LEFT);
            
            MxGraphNode node = nodes.get(key);
            node.updateCellSize();
            graph.executeStackLayout(node.getUuid(), false, 0);
            
            collapseRecursively(parents.get(key));
        }
        else
            graph.executeStackLayout(this.getUuid(), false, VERTICAL_SPACING, 0, 0, 0, MARGIN_LEFT);            
    }
    
    private void expandRecursively(T key) {
        List<T> keyParents = new ArrayList();
        T parent = parents.get(key);
        while (parent != null) {
            keyParents.add(parent);
            parent = parents.get(parent);
        }
        Collections.reverse(keyParents);
        keyParents.forEach(keyParent -> expand(keyParent));
    }
    
    private void executeLayoutToKey(T key) {
        MxGraphNode keyNode = keyNodes.get(key);
        if (keyNode != null) {
            graph.executeStackLayout(keyNode.getUuid(), true, HORIZONTAL_SPACING);
            executeLayoutRecursively(key);
        }
    }
    
    private void executeLayoutRecursively(T key) {
        if (key != null) {
            MxGraphNode subtreeNode = subtreeNodes.get(key);
            if (subtreeNode != null)
                graph.executeStackLayout(subtreeNode.getUuid(), false, VERTICAL_SPACING, 0, 0, 0, MARGIN_LEFT);
            
            MxGraphNode node = nodes.get(key);
            if (node != null)
                graph.executeStackLayout(node.getUuid(), false, 0);
            
            executeLayoutRecursively(parents.get(key));
        }
        else
            graph.executeStackLayout(this.getUuid(), false, VERTICAL_SPACING, 0, 0, 0, MARGIN_LEFT);
    }
    
    private String getKeyId(T key) {
        String keyId = null;
        if (key != null && functionId != null) {
            keyId = functionId.apply(key);
            if (keyId != null)
                return keyId;
        }
        return keyId;
    }
    
    private String getLabel(T key) {
        String keyLabel = null;
        if  (key != null) {
            keyLabel = functionLabel != null ? functionLabel.apply(key) : null;
            if (keyLabel == null)
                keyLabel = key.toString();
            if (keyLabel.length() > CHARACTER_LIMIT)
                keyLabel = String.format("%s ...", keyLabel.substring(0, CHARACTER_LIMIT + 1));
            return keyLabel;
        }
        return keyLabel;
    }
    /**
     * A toggle node
     */
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
