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
import com.neotropic.flow.component.mxgraph.MxGraphClickCellEvent;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final String IMG_ANGLE_DOWN = "img/angle-down.svg"; //NOI18N
    private final String IMG_ANGLE_RIGHT = "img/angle-right.svg"; //NOI18N
    private final int FONT_SIZE = 10;
    private final String FOLDABLE = String.valueOf(0);
    private final String RESIZABLE = String.valueOf(0);
    
    private final LinkedHashMap<String, String> RECTANGLE_STYLE = new LinkedHashMap();
    {
        RECTANGLE_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        RECTANGLE_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        RECTANGLE_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
        RECTANGLE_STYLE.put(MxConstants.STYLE_FOLDABLE, FOLDABLE);
    }
    private final LinkedHashMap<String, String> LABEL_STYLE = new LinkedHashMap();
    {
        LABEL_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_LABEL);
        LABEL_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        LABEL_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
        LABEL_STYLE.put(MxConstants.STYLE_FONTCOLOR, FONT_COLOR);
        LABEL_STYLE.put(MxConstants.STYLE_FONTSIZE, String.valueOf(FONT_SIZE));
        LABEL_STYLE.put(MxConstants.STYLE_ALIGN, MxConstants.ALIGN_LEFT);
        LABEL_STYLE.put(MxConstants.STYLE_FOLDABLE, FOLDABLE);
    }
    private final LinkedHashMap<String, String> ANGLE_DOWN_STYLE = new LinkedHashMap();
    {
        ANGLE_DOWN_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_IMAGE);
        ANGLE_DOWN_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
        ANGLE_DOWN_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        ANGLE_DOWN_STYLE.put(MxConstants.STYLE_IMAGE, IMG_ANGLE_DOWN);
        ANGLE_DOWN_STYLE.put(MxConstants.STYLE_IMAGE_WIDTH, String.valueOf(TOGGLE_WIDTH));
        ANGLE_DOWN_STYLE.put(MxConstants.STYLE_IMAGE_HEIGHT, String.valueOf(TOGGLE_HEIGHT));
        ANGLE_DOWN_STYLE.put(MxConstants.STYLE_FOLDABLE, FOLDABLE);
        ANGLE_DOWN_STYLE.put(MxConstants.STYLE_RESIZABLE, RESIZABLE);
    }
    private final LinkedHashMap<String, String> ANGLE_RIGHT_STYLE = new LinkedHashMap();
    {
        ANGLE_RIGHT_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_IMAGE);
        ANGLE_RIGHT_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
        ANGLE_RIGHT_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        ANGLE_RIGHT_STYLE.put(MxConstants.STYLE_IMAGE, IMG_ANGLE_RIGHT);
        ANGLE_RIGHT_STYLE.put(MxConstants.STYLE_IMAGE_WIDTH, String.valueOf(TOGGLE_WIDTH));
        ANGLE_RIGHT_STYLE.put(MxConstants.STYLE_IMAGE_HEIGHT, String.valueOf(TOGGLE_HEIGHT));
        ANGLE_RIGHT_STYLE.put(MxConstants.STYLE_FOLDABLE, FOLDABLE);
        ANGLE_RIGHT_STYLE.put(MxConstants.STYLE_RESIZABLE, RESIZABLE);
    }
    private final LinkedHashMap<String, String> LEAF_STYLE = new LinkedHashMap();
    {
        LEAF_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        LEAF_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        LEAF_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
        LEAF_STYLE.put(MxConstants.STYLE_FOLDABLE, FOLDABLE);
        LEAF_STYLE.put(MxConstants.STYLE_RESIZABLE, RESIZABLE);
    }
    // </editor-fold>
    private final MxGraph graph;
    private final HashMap<T, MxGraphNode> nodes = new HashMap();
    private final HashMap<T, MxGraphNode> keyNodes = new HashMap();
    private final HashMap<T, MxGraphNode> subtreeNodes = new HashMap();
    private final HashMap<T, ToggleNode> toogleNodes = new HashMap();
    
    private List<T> roots;
    private final LinkedHashMap<T, T> parents = new LinkedHashMap();
    private final LinkedHashMap<T, List<T>> children = new LinkedHashMap();
    private final Function<T, List<T>> functionChildren; 
    private final Function<T, String> functionId;
    private final Function<T, String> functionLabel;
    private final BiFunction<T, MxGraph, MxGraphNode> funcGetLabelNode;
    private final Function<T, Boolean> funcLeaf;
    
    public MxTree(MxGraph graph,
        Supplier<List<T>> supplierRoots, Function<T, List<T>> functionChildren,
        Function<T, String> functionLabel,
        BiFunction<T, MxGraph, MxGraphNode> funcGetLabelNode,
        Function<T, String> functionId, Function<T, Boolean> funcLeaf) {
        
        super();
        this.graph = graph;
        this.functionChildren = functionChildren;
        this.functionLabel = functionLabel;
        this.funcGetLabelNode = funcGetLabelNode;
        this.functionId = functionId;
        this.funcLeaf = funcLeaf;
        
        setRawStyle(RECTANGLE_STYLE);
        setIsSelectable(false);
        
        addCellAddedListener(event -> {
            graph.setCellsLocked(false);            
            this.overrideStyle();
            setConnectable(false);
            graph.setCellsLocked(true);
            
            event.unregisterListener();
        });
        graph.addCell(this);
        
        if (supplierRoots != null) {
            this.roots = supplierRoots.get();
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
        graph.addCell(node);
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
        graph.addCell(keyNode);
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
        graph.addCell(subtreeNode);
    }
    
    private void addToggleNode(T key, MxGraphNode node, MxGraphNode keyNode) {
        ToggleNode toggleNode = new ToggleNode();
        toogleNodes.put(key, toggleNode);
        toggleNode.setGeometry(0, 0, TOGGLE_WIDTH, TOGGLE_HEIGHT);
        toggleNode.setCellParent(keyNode.getUuid());
        
        if (!leaf(key)) {
            toggleNode.setRawStyle(ANGLE_RIGHT_STYLE);
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
                    graph.setCellsLocked(true);
                    
                    graph.setCellsLocked(false);
                    collapseRecursively(key);
                    expandRecursively(key);
                    collapse(key, children.get(key), true);
                    graph.setCellsLocked(true);
                } else {
                    graph.setCellsLocked(false);
                    toggleNode.cheked(true);
                    toggleNode.setRawStyle(ANGLE_DOWN_STYLE);
                    toggleNode.overrideStyle();
                    graph.setCellsLocked(true);
                    
                    if (functionChildren != null && subtreeNodes.get(key) == null) {

                        List<T> keyChildren = functionChildren.apply(key);
                        if (keyChildren != null) {
                            children.put(key, keyChildren);
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
                    else {
                        graph.setCellsLocked(false);
                        fireExpandEventRecursively(key, true);
                        graph.setCellsLocked(true);
                    }
                    graph.setCellsLocked(false);
                    expandNode(key);
                    graph.setCellsLocked(true);
                }
            });
        } else {
            toggleNode.setIsSelectable(false);
            toggleNode.setRawStyle(LEAF_STYLE);
            toggleNode.addCellAddedListener(event -> {
                graph.setCellsLocked(false);
                toggleNode.overrideStyle();
                toggleNode.setConnectable(false);
                graph.setCellsLocked(true);
                event.unregisterListener();
            });
        }
        graph.addCell(toggleNode);
        
        buildLabelNode(key, keyNode);
    }
    
    private MxGraphNode getLabelNode(T key, List<Boolean> defaultLabelNode) {
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
        else
            defaultLabelNode.set(0, false);
        return labelNode;
    }
    
    private void buildLabelNode(T key, MxGraphNode keyNode) {
        List<Boolean> defaultLabelNode = Arrays.asList(true);
        MxGraphNode labelNode = getLabelNode(key, defaultLabelNode);
        labelNode.setCellParent(keyNode.getUuid());
        labelNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            
            if (labelNode.getRawStyle() != null)
                labelNode.overrideStyle();
            
            executeLayoutToKey(key);
            T parent = parents.get(key);
            if (parent != null) {
                List<T> theChildren = children.get(parent);
                //If is the last children fire the tree expand event
                if (theChildren != null && !theChildren.isEmpty() && theChildren.get(theChildren.size() - 1).equals(key))
                    expand(key, theChildren, true);
            }
            // If is the last root fire the tree add event
            if (roots != null && !roots.isEmpty() && roots.get(roots.size() - 1).equals(key))
                fireTreeAddEndEvent(roots);
            graph.setCellsLocked(true);
            event.unregisterListener();
        });
        if (defaultLabelNode.get(0))
            graph.addCell(labelNode);
    }
    private void fireExpandEventRecursively(T parent, boolean fromClient) {
        if (parent != null) {
            List<T> theChildren = children.get(parent);
            if (theChildren != null) {
                expand(parent, theChildren, fromClient);
                ToggleNode toggleNode = toogleNodes.get(parent);
                if (toggleNode != null && toggleNode.checked())
                    theChildren.forEach(child -> fireExpandEventRecursively(child, fromClient));
            }
        }
    }
    private void expandNode(T key) {
        MxGraphNode subtreeNode = subtreeNodes.get(key);
        if (subtreeNode != null && subtreeNode.getCollapsed()) {
            subtreeNode.setCollapsed(false);
            executeLayoutRecursively(key);
        }
    }
    
    private void collapseRecursively(T key) {
        if (key != null) {
            MxGraphNode subtreeNode = subtreeNodes.get(key);
            if (subtreeNode != null) {
                subtreeNode.setCollapsed(true);
                subtreeNode.setGeometry();
                graph.executeStackLayout(subtreeNode.getUuid(), false, VERTICAL_SPACING, 0, 0, 0, MARGIN_LEFT);
            }
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
        keyParents.forEach(keyParent -> expandNode(keyParent));
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
    public void expand(T key) {
        MxGraphNode toogleNode = toogleNodes.get(key);
        if (toogleNode != null)
            ComponentUtil.fireEvent(toogleNode, new MxGraphClickCellEvent(toogleNode, false));
    }
    private boolean leaf(T key) {
        return key != null && funcLeaf != null ? funcLeaf.apply(key) : false;
    }
    public void expand(T parent, List<T> children, boolean fromClient) {
        fireEvent(new ExpandEvent<T>(this, fromClient, parent, children));
    }
    public void collapse(T parent, List<T> children, boolean fromClient) {
        fireEvent(new CollapseEvent<T>(this, fromClient, parent, children));
    }
    private void fireTreeAddEndEvent(List<T> roots) {
        fireEvent(new TreeAddEndEvent(this, true, roots));
    }
    public Registration addExpandListener(ComponentEventListener<ExpandEvent<T>> listener) {
        return addListener(ExpandEvent.class, (ComponentEventListener) listener);
    }
    public Registration addCollapseListener(ComponentEventListener<CollapseEvent<T>> listener) {
        return addListener(CollapseEvent.class, (ComponentEventListener) listener);
    }
    public Registration addTreeAddEndListener(ComponentEventListener<TreeAddEndEvent<T>> listener) {
        return addListener(TreeAddEndEvent.class, (ComponentEventListener) listener);
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
    /**
     * MxTree Expand Event
     */
    public class ExpandEvent<T> extends ComponentEvent<MxTree> {
        private T parent;
        private List<T> children;
        
        public ExpandEvent(MxTree source, boolean fromClient, T parent, List<T> children) {
            super(source, fromClient);
            this.parent = parent;
            this.children = children;
        }
        public T getParent() {
            return parent;
        }
        public List<T> getChildren() {
            return children;
        }
    }
    /**
     * MxTree Collapse Event
     */
    public class CollapseEvent<T> extends ComponentEvent<MxTree> {
        private T parent;
        private List<T> children;
        
        public CollapseEvent(MxTree source, boolean fromClient, T parent, List<T> children) {
            super(source, fromClient);
            this.parent = parent;
            this.children = children;
        }
        public T getParent() {
            return parent;
        }
        public List<T> getChildren() {
            return children;
        }
    }
    /**
     * Event to fired when tree is loaded
     */
    public class TreeAddEndEvent<T> extends ComponentEvent<MxTree> {
        private List<T> roots;
        
        public TreeAddEndEvent(MxTree source, boolean fromClient, List<T> roots) {
            super(source, fromClient);
            this.roots = roots;
        }
        public List<T> getRoots() {
            return roots;
        }
    }
}
