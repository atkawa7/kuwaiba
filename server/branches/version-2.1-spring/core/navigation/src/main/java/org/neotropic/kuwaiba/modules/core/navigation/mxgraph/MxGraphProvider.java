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

package org.neotropic.kuwaiba.modules.core.navigation.mxgraph;

import com.neotropic.vaadin14.component.MxGraph;
import com.neotropic.vaadin14.component.MxGraphEdge;
import com.neotropic.vaadin14.component.MxGraphNode;
import com.neotropic.vaadin14.component.Point;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;

/**
 * Wraper to manage mxgraph instance and his objects
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class MxGraphProvider<T> {
    
    MxGraph mxGraph;  
    
    private HashMap<T, MxGraphNode> nodes;
    /**
     * A dictionary with the edges in the current view
     */
    private HashMap<T, MxGraphEdge> edges;

    public MxGraph getMxGraph() {
        return mxGraph;
    }

    public void setMxGraph(MxGraph mxGraph) {
        this.mxGraph = mxGraph;
    }

    public HashMap<T, MxGraphNode> getNodes() {
        return nodes;
    }

    public void setNodes(HashMap<T, MxGraphNode> nodes) {
        this.nodes = nodes;
    }

    public HashMap<T, MxGraphEdge> getEdges() {
        return edges;
    }

    public void setEdges(HashMap<T, MxGraphEdge> edges) {
        this.edges = edges;
    }
    
    public MxGraphNode findNodeWidget(T node) {
        return nodes.get(node);
    }
    
    public MxGraphEdge findEdgeWidget(T edge) {
        return edges.get(edge);
    }

    public MxGraphProvider() {
        initGraph();
    }   
    
    private void initGraph() {
        
       mxGraph = new MxGraph();
       mxGraph.setFullSize();
       mxGraph.getElement().getStyle().set("height", "100%");

       nodes = new HashMap<>();
       edges = new HashMap<>();
       
    }  

    public MxGraphNode attachNodeWidget(T node, String nodeId,int xCoordinate, int yCoordinate, String imageUri) {
        
        if (!nodes.containsKey(node)) {
                         
        MxGraphNode newNode = new MxGraphNode();
        if (imageUri != null && !imageUri.isEmpty())
            newNode.setImage(imageUri);
        
        newNode.setUuid(nodeId);
        newNode.setLabel(node.toString());
        newNode.setWidth(ResourceFactory.DEFAULT_ICON_WIDTH);
        newNode.setHeight(ResourceFactory.DEFAULT_ICON_HEIGHT);
        newNode.setX(50 + (xCoordinate)); //The position is scaled
        newNode.setY(50 + (yCoordinate)); 
        nodes.put(node, newNode);
        mxGraph.addNode(newNode);
        mxGraph.refreshGraph();
        return newNode;
        
        }
        
        return null;
        
    }
    
    public MxGraphEdge attachEdgeWidget(T edgeObject, T sourceObject, T targetObject, List<Point> points,String sourceLabel, String targetLabel) {
       
         if (!edges.containsKey(edgeObject)) {
        
        MxGraphNode sourceNode = findNodeWidget(sourceObject);
        MxGraphNode targetNode = findNodeWidget(targetObject);
        MxGraphEdge newEdge = new MxGraphEdge();
        
        newEdge.setSource(sourceNode.getUuid());
        newEdge.setTarget(targetNode.getUuid());
        newEdge.setSourceLabel(sourceLabel);
        newEdge.setTargetLabel(targetLabel);
        newEdge.setLabel(edgeObject.toString());
        newEdge.setPoints(points);
        
        edges.put(edgeObject, newEdge);
        mxGraph.addEdge(newEdge);
        mxGraph.refreshGraph();
        return newEdge;
        
         }
         
        return null;
    }
       
}
