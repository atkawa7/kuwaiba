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

import com.neotropic.vaadin14.component.MxConstants;
import com.neotropic.vaadin14.component.MxGraph;
import com.neotropic.vaadin14.component.MxGraphCell;
import com.neotropic.vaadin14.component.MxGraphCellSelectedEvent;
import com.neotropic.vaadin14.component.MxGraphCellUnselectedEvent;
import com.neotropic.vaadin14.component.MxGraphDeleteCellSelectedEvent;
import com.neotropic.vaadin14.component.MxGraphEdge;
import com.neotropic.vaadin14.component.MxGraphNode;
import com.neotropic.vaadin14.component.Point;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;


/**
 * Wrapper to manage mxgraph instance and his objects
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 * @param <N> Node object type
 * @param <E> Edge object type
 */
public class MxGraphCanvas<N, E> {
    
    MxGraph mxGraph;  
    
    private HashMap<N, MxGraphNode> nodes;
    /**
     * A dictionary with the edges in the current view
     */
    private HashMap<E, MxGraphEdge> edges;
    
    private HashMap<E,N> sourceEdgeNodes;
    
    private HashMap<E,N> targetEdgeNodes;
    
    private String selectedCellId;
    
    private String selectedCellType;
    
    Command comObjectSelected;
    
    Command comObjectDeleted;
    
    Command comObjectUnselected;
    
    public MxGraph getMxGraph() {
        return mxGraph;
    }

    public void setMxGraph(MxGraph mxGraph) {
        this.mxGraph = mxGraph;
    }

    public HashMap<N, MxGraphNode> getNodes() {
        return nodes;
    }

    public void setNodes(HashMap<N, MxGraphNode> nodes) {
        this.nodes = nodes;
    }

    public HashMap<E, MxGraphEdge> getEdges() {
        return edges;
    }

    public void setEdges(HashMap<E, MxGraphEdge> edges) {
        this.edges = edges;
    }

    public HashMap<E, N> getSourceEdgeNodes() {
        return sourceEdgeNodes;
    }

    public void setSourceEdgeNodes(HashMap<E, N> sourceEdgeNodes) {
        this.sourceEdgeNodes = sourceEdgeNodes;
    }

    public HashMap<E, N> getTargetEdgeNodes() {
        return targetEdgeNodes;
    }

    public void setTargetEdgeNodes(HashMap<E, N> targetEdgeNodes) {
        this.targetEdgeNodes = targetEdgeNodes;
    }  

    public String getSelectedCellId() {
        return selectedCellId;
    }

    public void setSelectedCellId(String selectedCellId) {
        this.selectedCellId = selectedCellId;
    }

    public String getSelectedCellType() {
        return selectedCellType;
    }

    public void setSelectedCellType(String selectedCellType) {
        this.selectedCellType = selectedCellType;
    }

    public void setComObjectSelected(Command comObjectSelected) {
        this.comObjectSelected = comObjectSelected;
    }

    public void setComObjectDeleted(Command comObjectDeleted) {
        this.comObjectDeleted = comObjectDeleted;
    }

    public void setComObjectUnselected(Command comObjectUnselected) {
        this.comObjectUnselected = comObjectUnselected;
    }
    
    public MxGraphCanvas() {
        initGraph();
    }   
    
    private void initGraph() {       
       mxGraph = new MxGraph();
       mxGraph.setFullSize();
       mxGraph.getElement().getStyle().set("height", "100%");
       mxGraph.getElement().getStyle().set("width", "100%");
       mxGraph.setGrid("img/grid.gif");
       nodes = new HashMap<>();
       edges = new HashMap<>();     
       sourceEdgeNodes = new HashMap<>();
       targetEdgeNodes = new HashMap<>();
       
       mxGraph.addCellUnselectedListener((MxGraphCellUnselectedEvent t) -> {
           this.selectedCellId = null;
           this.selectedCellType = null;
           if (comObjectUnselected != null)
               comObjectUnselected.execute();
       });
       
       mxGraph.addCellSelectedListener((MxGraphCellSelectedEvent t) -> {
           this.selectedCellId = t.getCellId();
           this.selectedCellType = t.isVertex() ? MxGraphCell.PROPERTY_VERTEX : MxGraphCell.PROPERTY_EDGE;
           if (comObjectSelected != null)
               comObjectSelected.execute();
       });
       
       mxGraph.addDeleteCellSelectedListener((MxGraphDeleteCellSelectedEvent t) -> {
           if (comObjectDeleted != null)
               comObjectDeleted.execute();
       });
    } 
    
    public MxGraphNode findMxGraphNode(N node) {
        return nodes.get(node);
    }
    
    public MxGraphEdge findMxGraphEdge(E edge) {
        return edges.get(edge);
    }
    
    public N findSourceEdgeObject(E edge) {
        return sourceEdgeNodes.get(edge);
    }
    
    public N findTargetEdgeObject(E edge) {
        return targetEdgeNodes.get(edge);
    }
    /**
     * Creates a new node in the canvas
     * @param node the object that represent the node
     * @param nodeId the node id
     * @param xCoordinate the x coordinate in the canvas
     * @param yCoordinate the y coordinate in the canvas
     * @param imageUri the uri image
     * @return the new node
     */
    public MxGraphNode addNode(N node, String nodeId, int xCoordinate, int yCoordinate, String imageUri) {

        if (!nodes.containsKey(node)) {

            MxGraphNode newNode = new MxGraphNode();
            if (imageUri != null && !imageUri.isEmpty()) 
                newNode.setImage(imageUri);
            
            newNode.setUuid(nodeId);
            newNode.setLabel(node.toString());
            newNode.setWidth(Constants.DEFAULT_ICON_WIDTH);
            newNode.setHeight(Constants.DEFAULT_ICON_HEIGHT);
            newNode.setX((xCoordinate)); //The position is scaled
            newNode.setY((yCoordinate));
            newNode.setShape(MxConstants.SHAPE_IMAGE);
            nodes.put(node, newNode);
            mxGraph.addNode(newNode);
            mxGraph.refreshGraph();
            return newNode;
        }
        return null;
    }
    /**
     * creates a new edge in the canvas
     * @param edgeObject the object that represents the edge
     * @param edgeId the edge id
     * @param sourceObject the source edge object
     * @param targetObject the target edge object
     * @param points the control points list
     * @param sourceLabel the source label
     * @param targetLabel the target label
     * @return the new edge
     */
    public MxGraphEdge addEdge(E edgeObject, String edgeId, N sourceObject, N targetObject, List<Point> points,String sourceLabel, String targetLabel) {
       
         if (!edges.containsKey(edgeObject)) {       
            MxGraphNode sourceNode = findMxGraphNode(sourceObject);
            MxGraphNode targetNode = findMxGraphNode(targetObject);
            MxGraphEdge newEdge = new MxGraphEdge();

            newEdge.setUuid(edgeId);
            newEdge.setSource(sourceNode.getUuid());
            newEdge.setTarget(targetNode.getUuid());
            newEdge.setSourceLabel(sourceLabel);
            newEdge.setTargetLabel(targetLabel);
            newEdge.setLabel(edgeObject.toString());
            newEdge.setPoints(points);

            edges.put(edgeObject, newEdge);
            sourceEdgeNodes.put(edgeObject, sourceObject);
            targetEdgeNodes.put(edgeObject, targetObject);
            mxGraph.addEdge(newEdge);
            mxGraph.refreshGraph();
            return newEdge;       
        }        
        return null;
    }
    /**
     * Removes a node from the canvas
     * @param businessObject the object to be removed
     */
    public void removeNode(N businessObject) {
        
        mxGraph.removeNode(nodes.get(businessObject));
        nodes.remove(businessObject);
        
        //delete edges related to the object
        List<E> edgesToDelete = new ArrayList<>();
        
        for (Map.Entry<E, N> entry : sourceEdgeNodes.entrySet()) {
            if (entry.getValue().equals(businessObject)) {
                edgesToDelete.add(entry.getKey());
            }
        }
        for (Map.Entry<E, N> entry : targetEdgeNodes.entrySet()) {
            if (entry.getValue().equals(businessObject)) {
                edgesToDelete.add(entry.getKey());
            }
        }
        
        for (E edge : edgesToDelete) {   
            mxGraph.removeEdge(edges.get(edge));
            edges.remove(edge);
            sourceEdgeNodes.remove(edge);
            targetEdgeNodes.remove(edge);
        }    
    }
    /**
     * Removes an edge from the canvas
     * @param businessObject the object to be removed
     */
    public void removeEdge(E businessObject) {       
        mxGraph.removeEdge(edges.get(businessObject));
        edges.remove(businessObject);
                    
        edges.remove(businessObject);
        sourceEdgeNodes.remove(businessObject);
        targetEdgeNodes.remove(businessObject);            
    }
      
}
