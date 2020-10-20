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
package com.neotropic.flow.component.mxgraph;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * 
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Tag("mx-graph")
@JsModule("./mx-graph/mx-graph.js")
public class MxGraph extends Component implements HasComponents, HasStyle, HasSize {
    private static final String PROPERTY_GRID = "grid";
    private static final String PROPERTY_WIDTH = "width";
    private static final String PROPERTY_HEIGHT = "height";
    private static final String PROPERTY_OUTLINE_WIDTH = "outlineWidth";
    private static final String PROPERTY_OUTLINE_HEIGHT = "outlineHeight";
    private static final String PROPERTY_MAX_WIDTH = "maxWidth";
    private static final String PROPERTY_MAX_HEIGHT = "maxHeight";
    private static final String PROPERTY_CELLS_MOVABLE = "cellsMovable";
    private static final String PROPERTY_CELLS_EDITABLE = "cellsEditable";
    private static final String PROPERTY_OVERFLOW = "overflow";
    /**
     * Specifies if the graph should allow new connections.
     */
    private static final String PROPERTY_CONNECTABLE = "connectable"; //NOI18N
    private static final String PROPERTY_ROTATION = "rotationEnabled";
    private static final String PROPERTY_HAS_OUTLINE = "hasOutline";
    private static final String PROPERTY_BEGIN_UPDATE_ON_INIT = "beginUpdateOnInit";
    private static final String PROPERTY_SCALE = "scale";
    private static final String PROPERTY_TRANSLATIONX = "translationX";
    private static final String PROPERTY_TRANSLATIONY = "translationY";
    private static final int DEFAULT_OUTLINE_WIDTH = 200;
    /**
     * Specifies if tooltips should be enabled
     */
    private static final String PROPERTY_TOOLTIPS = "tooltips"; //NOI18N
    private static final String PROPERTY_OVERRIDE_CURRENT_STYLE = "overrideCurrentStyle"; //NOI18N
    
    private List<MxGraphNode> nodes;
    private List<MxGraphEdge> edges;
    private List<MxGraphLayer> layers;
    private List<MxCellStyle> styles;
    
    public MxGraph() {
        super();
        nodes = new ArrayList();
        edges = new ArrayList();
        layers = new ArrayList();
        styles = new ArrayList();
        getElement().getStyle().set(PROPERTY_WIDTH, "100%");
        getElement().getStyle().set(PROPERTY_HEIGHT, "100%");
    }
    
     public String getGrid() {
        return getElement().getProperty(PROPERTY_GRID);
    }
        
    public void setGrid(String grid) {
        getElement().setProperty(PROPERTY_GRID, grid);
    }
    
    public String getWidth() {
        return getElement().getProperty(PROPERTY_WIDTH);
    }
        
    public void setWidth(String prop) {
        getElement().setProperty(PROPERTY_WIDTH, prop);
//        getElement().getStyle().set(PROPERTY_WIDTH, prop);
    }
    
    public String getHeight() {
        return getElement().getProperty(PROPERTY_HEIGHT);
    }
        
    public void setHeight(String prop) {
        getElement().setProperty(PROPERTY_HEIGHT, prop);
//        getElement().getStyle().set(PROPERTY_HEIGHT, prop);
    }
    
    public String getMaxWidth() {
        return getElement().getProperty(PROPERTY_MAX_WIDTH);
    }
    
    public void setMaxWidth(String prop) {
        getElement().setProperty(PROPERTY_MAX_WIDTH, prop);
//        getElement().getStyle().set("max-width", prop);
    }
    
    public String getMaxHeight() {
        return getElement().getProperty(PROPERTY_MAX_HEIGHT);
    }
        
    public void setMaxHeight(String prop) {
        getElement().setProperty(PROPERTY_MAX_HEIGHT, prop);
//        getElement().getStyle().set("max-height", prop);
    }
    
    public String getOverflow() {
        return getElement().getProperty(PROPERTY_OVERFLOW);
    }
        
    public void setOverflow(String prop) {
        getElement().setProperty(PROPERTY_OVERFLOW, prop);
    }
    /**
     * Gets boolean indicating if new connections should be allowed.
     * @return boolean indicating if new connections should be allowed.
     */
    public boolean getConnectable() {
        return getElement().getProperty(PROPERTY_CONNECTABLE, false);
    }
    /**
     * Sets boolean indicating if new connections should be allowed.
     * @param connectable boolean indicating if new connections should be allowed.
     */
    public void setConnectable(boolean connectable) {
        getElement().setProperty(PROPERTY_CONNECTABLE, connectable);
    }
    /**
     * Gets if the tooltips are enabled
     * @return If the tooltips are enabled
     */
    public boolean getTooltips() {
        return getElement().getProperty(PROPERTY_TOOLTIPS, false);
    }
    /**
     * Sets if the tooltips should be enabled
     * @param enabled Specifies if the tooltips should be enabled
     */
    public void setTooltips(boolean enabled) {
        getElement().setProperty(PROPERTY_TOOLTIPS, enabled);
    }
    
    public boolean getOverrideCurrentStyle() {
        return getElement().getProperty(PROPERTY_OVERRIDE_CURRENT_STYLE, false);
    }
    
    public void setOverrideCurrentStyle(boolean enabled) {
        getElement().setProperty(PROPERTY_OVERRIDE_CURRENT_STYLE, enabled);
    }
    
    public void setCellsLocked(boolean cellsLocked) {
        getElement().executeJs("this.graph.setCellsLocked($0)", cellsLocked);
    }
    
    public Registration addClickGraphListener(ComponentEventListener<MxGraphClickGraphEvent> clickListener) {
        return super.addListener(MxGraphClickGraphEvent.class, clickListener);
    }
    
    public Registration addMouseMoveGraphListener(ComponentEventListener<MxGraphMouseMoveGraphEvent> clickListener) {
        return super.addListener(MxGraphMouseMoveGraphEvent.class, clickListener);
    }
    
    public Registration addRightClickGraphListener(ComponentEventListener<MxGraphRightClickGraphEvent> clickListener) {
        return super.addListener(MxGraphRightClickGraphEvent.class, clickListener);
    }
    
    public Registration addCellUnselectedListener(ComponentEventListener<MxGraphCellUnselectedEvent> eventListener) {
        return super.addListener(MxGraphCellUnselectedEvent.class, eventListener);
    }
    
    public Registration addCellSelectedListener(ComponentEventListener<MxGraphCellSelectedEvent> eventListener) {
        return super.addListener(MxGraphCellSelectedEvent.class, eventListener);
    }
    
    public Registration addGraphLoadedListener(ComponentEventListener<MxGraphGraphLoadedEvent> eventListener) {
        return super.addListener(MxGraphGraphLoadedEvent.class, eventListener);
    }
    
    public Registration addDeleteCellSelectedListener(ComponentEventListener<MxGraphDeleteCellSelectedEvent> eventListener) {
        return super.addListener(MxGraphDeleteCellSelectedEvent.class, eventListener);
    }
    
    public Registration addMouseOverEvent(ComponentEventListener<MxGraphMouseOverEvent> mouseOverEvent) {
        return addListener(MxGraphMouseOverEvent.class, mouseOverEvent);
    }
    
    public Registration addEdgeCompleteListener(ComponentEventListener<MxGraphEdgeCompleteEvent> edgeCompleteEvent) {
        return addListener(MxGraphEdgeCompleteEvent.class, edgeCompleteEvent);
    }
    
    public void addCell(MxGraphCell mxGraphCell) {  
        add(mxGraphCell);
    }
   
    public void addNode(MxGraphNode graphNode) {
        nodes.add(graphNode);
        add(graphNode);  
    }
    
    public void addEdge(MxGraphEdge graphEdge) {
        edges.add(graphEdge);
        add(graphEdge); 
    }
    public void addLayer(MxGraphLayer graphLayer) {
        layers.add(graphLayer);
        getElement().appendChild(graphLayer.getElement());     
    }
    public void addCellStyle(MxCellStyle style) {
        if (styles.contains(style))
            styles.remove(style);
        styles.add(style);     
        getElement().callJsFunction("addCellStyle", style.getName(), style.getAsJson());
    }

    public List<MxGraphNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<MxGraphNode> nodes) {
        this.nodes = nodes;
    }

    public List<MxGraphEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<MxGraphEdge> edges) {
        this.edges = edges;
    }

    public List<MxGraphLayer> getLayers() {
        return layers;
    }

    public void setLayers(List<MxGraphLayer> layers) {
        this.layers = layers;
    }
    
    public void setFullSize() {
        setWidth("100%");
        setHeight("100%");
        getElement().getStyle().set(PROPERTY_WIDTH, "100%");
        getElement().getStyle().set(PROPERTY_HEIGHT, "100%");
    }
    
    public boolean getIsCellEditable() {
        return getElement().getProperty(PROPERTY_CELLS_EDITABLE,false);
    }
        
    public void setIsCellEditable(boolean prop) {
        getElement().setProperty(PROPERTY_CELLS_EDITABLE, prop);
    }
    
    public boolean getIsCellMovable() {
        return getElement().getProperty(PROPERTY_CELLS_MOVABLE,false);
    }
        
    public void setIsCellMovable(boolean prop) {
        getElement().setProperty(PROPERTY_CELLS_MOVABLE, prop);
    }
    
    public void setOutlineWidth(String prop) {
        getElement().setProperty(PROPERTY_OUTLINE_WIDTH, prop);
    }
    
    public void setOutlineHeight(String prop) {
        getElement().setProperty(PROPERTY_OUTLINE_HEIGHT, prop);
    }
    
    public void setRotationEnabled(boolean prop) {
        getElement().setProperty(PROPERTY_ROTATION, prop);
    }
    
    public void setHasOutline(boolean prop) {
        getElement().setProperty(PROPERTY_HAS_OUTLINE, prop);
    }
    
    public void setBeginUpdateOnInit(boolean prop) {
        getElement().setProperty(PROPERTY_BEGIN_UPDATE_ON_INIT, prop);
    }
    
    public void setScale(double prop) {
        getElement().setProperty(PROPERTY_SCALE, prop);
    }
    @Synchronize(property = PROPERTY_SCALE, value = "scale-changed")
    public double getScale() {
        return getElement().getProperty(PROPERTY_SCALE, 1.0);
    }
    
     public void setTranslationX(int prop) {
        getElement().setProperty(PROPERTY_TRANSLATIONX, prop);
    }
    
    public int getTranslationX() {
        return getElement().getProperty(PROPERTY_TRANSLATIONX, 0);
    }
    
     public void setTranslationY(int prop) {
        getElement().setProperty(PROPERTY_TRANSLATIONY, prop);
    }
    
    public int getTranslationY() {
        return getElement().getProperty(PROPERTY_TRANSLATIONY, 0);
    }
    
    public void zoomIn() {
        getElement().callJsFunction("zoomIn");
    }
        
    public void zoomOut() {
        getElement().callJsFunction("zoomOut");
    }
    
    public void removeIncompleteEdges() {
        
        ListIterator<MxGraphEdge> edgesIterator = edges.listIterator();
        while(edgesIterator.hasNext()){
            MxGraphEdge edge = edgesIterator.next();
            if ((edge.getSource() == null || edge.getSource().isEmpty()) ||
                    (edge.getTarget()== null || edge.getTarget().isEmpty())){
                edgesIterator.remove();
                getElement().removeChild(edge.getElement());
            }
        }
    }
    
    /**
     * this method remove all cells(vertex and edges) in the graph
     */
    public void removeAllCells() {     
        
        nodes.stream().forEach(node ->  getElement().removeChild(node.getElement()));
        edges.stream().forEach(edge ->  getElement().removeChild(edge.getElement()));
        nodes.clear();
        edges.clear();
//        getElement().callJsFunction("removeAllCells");
    }
    
    /**
     * this method refresh all objects in the graph
     */
    public void refreshGraph() {
        getElement().callJsFunction("refreshGraph");
    }

    public void removeNode(MxGraphNode node) {
        getElement().removeChild(node.getElement());
        nodes.remove(node);
        
        for (MxGraphNode mxNode : nodes) {  // delete childrens
            String parentNodeId = mxNode.getCellParent();
            if (parentNodeId != null && parentNodeId.equals(node.getUuid()))
               removeNode(mxNode); 
        }
        
         //delete edges related to the object
        List<MxGraphEdge> edgesToDelete = new ArrayList<>();
        
        for (MxGraphEdge edge : edges) {
            if ((edge.getSource() != null && edge.getSource().equals(node.getUuid())) || 
                (edge.getTarget() != null && edge.getTarget().equals(node.getUuid())))  {
                edgesToDelete.add(edge);
            }
        }
        
        for (MxGraphEdge edge : edgesToDelete) {   
            removeEdge(edge);
        }    
    }
    
    public void removeEdge(MxGraphEdge edge) {
        getElement().removeChild(edge.getElement());
        edges.remove(edge);
    }
    
    public void removeLayer(MxGraphLayer layer) {
        getElement().removeChild(layer.getElement());
        layers.remove(layer);
        
        for (MxGraphNode node : nodes) {
            String layerNode = node.getCellLayer();
            if (layerNode != null && layerNode.equals(layer.getUuid()))
               removeNode(node); 
        }
    }
    
    public void executeStackLayout(String cellId, Boolean horizontal, Integer spacing ) {
       executeStackLayout(cellId, horizontal, spacing,  0, 0 , 0, 0);
    }
    
    public void executeStackLayout(String cellId, Boolean horizontal, Integer spacing, Integer marginTop, Integer marginRight, Integer marginBottom, Integer marginLeft) {
       getElement().callJsFunction("executeStackLayout", cellId , horizontal , spacing, marginTop, marginRight, marginBottom, marginLeft);
    }
    
    public void executeStackLayout(String cellId, Boolean horizontal, Integer spacing, Integer margin) {
       executeStackLayout( cellId , horizontal , spacing, margin, margin, margin, margin);
    }
    
    public void executePartitionLayout(String cellId, Boolean horizontal, Integer spacing, Integer border) {
       executePartitionLayout(cellId, horizontal, spacing,  border, true);
    }
    
    public void executePartitionLayout(String cellId, Boolean horizontal, Integer spacing, Integer border, Boolean resizeVertices ) {
       getElement().callJsFunction("executePartitionLayout", cellId , horizontal , spacing, border, resizeVertices);
    }
     
    public void alignCells(String alignType, String [] cellIds, Integer coordinate) {
       getElement().callJsFunction("alignCells", alignType , new Gson().toJson(cellIds), coordinate);
    }
    
    public void alignCells(String alignType, String [] cellIds) {
        alignCells(alignType, cellIds, null);
    }
    
    public void enablePanning(Boolean enablePanning) {
       getElement().callJsFunction("enablePanning", enablePanning);
    }
    
    public void beginUpdate() {
       getElement().callJsFunction("beginUpdate");
    }
    
    public void endUpdate() {
       getElement().callJsFunction("endUpdate");
    }
    
    public void setCellsMovable(boolean value) {
       getElement().callJsFunction("setCellsMovable", value);
    }
}
