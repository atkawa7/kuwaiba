/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package com.neotropic.vaadin14.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("mx-graph")
@JsModule("./mx-graph/mx-graph.js")
public class MxGraph extends Component {
    private static final String PROPERTY_GRID = "grid";
    private static final String PROPERTY_WIDTH = "width";
    private static final String PROPERTY_HEIGHT = "height";
    private static final String PROPERTY_CELLS_MOVABLE = "cellsMovable";
    private static final String PROPERTY_CELLS_EDITABLE = "cellsEditable";
    
    private List<MxGraphNode> nodes;
    private List<MxGraphEdge> edges;
    
    public MxGraph() {
        nodes = new ArrayList();
        edges = new ArrayList();
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
    }
    
    public String getHeight() {
        return getElement().getProperty(PROPERTY_HEIGHT);
    }
        
    public void setHeight(String prop) {
        getElement().setProperty(PROPERTY_HEIGHT, prop);
    }
       
    public Registration addClickEdgeListener(ComponentEventListener<MxGraphClickEdgeEvent> clickEdgeListener) {
        return super.addListener(MxGraphClickEdgeEvent.class, clickEdgeListener);
    }
    
    public Registration addCellUnselectedListener(ComponentEventListener<MxGraphCellUnselectedEvent> eventListener) {
        return super.addListener(MxGraphCellUnselectedEvent.class, eventListener);
    }
    
    public Registration addCellSelectedListener(ComponentEventListener<MxGraphCellSelectedEvent> eventListener) {
        return super.addListener(MxGraphCellSelectedEvent.class, eventListener);
    }
    
    public Registration addDeleteCellSelectedListener(ComponentEventListener<MxGraphDeleteCellSelectedEvent> eventListener) {
        return super.addListener(MxGraphDeleteCellSelectedEvent.class, eventListener);
    }
    
    public void addCell(MxGraphCell mxGraphCell) {
        getElement().appendChild(mxGraphCell.getElement());     
    }
   
    public void addNode(MxGraphNode graphNode) {
        nodes.add(graphNode);
        getElement().appendChild(graphNode.getElement());     
    }
    
    public void addEdge(MxGraphEdge graphEdge) {
        edges.add(graphEdge);
        getElement().appendChild(graphEdge.getElement());     
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
    
    public void setFullSize() {
        setWidth("100%");
        setHeight("100%");
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
    }
    
    public void removeEdge(MxGraphEdge edge) {
        getElement().removeChild(edge.getElement());
        edges.remove(edge);
    }

 
}
