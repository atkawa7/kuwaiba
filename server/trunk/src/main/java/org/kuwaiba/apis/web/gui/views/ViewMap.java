/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kuwaiba.apis.web.gui.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A representation of a view (which in turn a graphical representation of an inventory object or a function in the domain of the inventory system) as a set of java objects. 
 * In general terms, a ViewMap instance is a group of nodes and connections between those nodes, as well as auxiliary components, such as comments, or groups of nodes. This map 
 * does not contain rendering information, such as dimensions or positions, but it is rather a description of the internal structure of the view, which can be used by the consumer 
 * to perform analysis on the information contained by the view.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ViewMap {
    private List<AbstractViewNode> nodes;
    private List<AbstractViewEdge> edges;
    private HashMap<AbstractViewEdge, AbstractViewNode> sourceNodes;
    private HashMap<AbstractViewEdge, AbstractViewNode> targetNodes;

    public ViewMap() {
        this.nodes  = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.sourceNodes = new HashMap<>();
        this.targetNodes = new HashMap<>();
    }
    
    /**
     * Adds a node to the map.
     * @param node The node to be added. If the node is already in the map, nothing will be done.
     */
    public void addNode(AbstractViewNode node) {
        if (!nodes.contains(node))
            nodes.add(node);
    }
    
    /**
     * Adds an edge to the map.
     * @param edge The edge to be added. If the edge is already in the map, nothing will be done.
     */
    public void addEdge(AbstractViewEdge edge) {
        if (!edges.contains(edge))
            edges.add(edge);
    }
    
    /**
     * Sets the source node of a connection (edge). If the edge already has a source node, it will be disconnected.
     * @param edge The edge to be connected to the node.
     * @param sourceNode The node to be connected to the edge.
     */
    public void attachSourceNode(AbstractViewEdge edge, AbstractViewNode sourceNode) {
        assert (!edges.contains(edge) || !nodes.contains(sourceNode)) : "The map does not contain either the source node or the edge provided";
        sourceNodes.remove(edge);
        sourceNodes.put(edge, sourceNode);
    }
    
    /**
     * Sets the target node of a connection (edge). If the edge already has a target node, it will be disconnected.
     * @param edge The edge to be connected to the node.
     * @param targetNode The node to be connected to the edge.
     */
    public void attachTargetNode(AbstractViewEdge edge, AbstractViewNode targetNode) {
        assert (!edges.contains(edge) || !nodes.contains(targetNode)) : "The map does not contain either the target node or the edge provided";
        targetNodes.remove(edge);
        targetNodes.put(edge, targetNode);
    }
    
    /**
     * Gets the object behind a node whose identifier is the one provider.
     * @param identifier The object to search.
     * @return The node or null if such identifier does not belong to any node.
     */
    public AbstractViewNode getNode(Object identifier) {
        return nodes.stream().filter((aNode) -> {
            return aNode.getIdentifier().equals(identifier);
        }).findFirst().orElse(null);
    }
    
    /**
     * Gets the object behind a node whose identifier is the one provider.
     * @param identifier The object to search.
     * @return The node or null if such identifier does not belong to any node.
     */
    public AbstractViewEdge getEdge(Object identifier) {
        return edges.stream().filter((anEdge) -> {
            return anEdge.getIdentifier().equals(identifier);
        }).findFirst().orElse(null);
    }
}
