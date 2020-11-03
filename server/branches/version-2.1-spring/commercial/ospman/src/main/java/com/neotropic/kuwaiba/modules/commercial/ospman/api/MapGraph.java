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
package com.neotropic.kuwaiba.modules.commercial.ospman.api;

import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.vaadin.flow.component.DetachEvent;
import java.util.HashMap;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Component to override some methods of mx-graph and to have major control of
 * the cycle life of the listeners
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MapGraph extends MxGraph {
    /**
     * Set of business object nodes
     */
    private final HashMap<BusinessObjectLight, MapNode> businessObjectNodes = new HashMap();
    /**
     * Set of business object edges
     */
    private final HashMap<BusinessObjectLight, MapEdge> businessObjectEdges = new HashMap();
    
    private boolean detached = false;
    
    public MapGraph() {
        addGraphLoadedListener(event -> {
            enablePanning(false);
            event.unregisterListener();
        });
    }

    @Override
    public void addNode(MxGraphNode node) {
        super.addNode(node);
        if (node instanceof MapNode) {
            MapNode mapNode = (MapNode) node;
            businessObjectNodes.put(mapNode.getBusinessObject(), mapNode);
        }
    }

    @Override
    public void removeNode(MxGraphNode node) {
        super.removeNode(node);
        if (node instanceof MapNode)
            businessObjectNodes.remove(((MapNode) node).getBusinessObject());
    }
    
    @Override
    public void addEdge(MxGraphEdge edge) {
        super.addEdge(edge);
        if (edge instanceof MapEdge) {
            MapEdge mapEdge = (MapEdge) edge;
            businessObjectEdges.put(mapEdge.getBusinessObject(), mapEdge);
        }
    }

    @Override
    public void removeEdge(MxGraphEdge edge) {
        super.removeEdge(edge);
        if (edge instanceof MapEdge)
            businessObjectEdges.remove(((MapEdge) edge).getBusinessObject());
    }
    
    public MapNode findNode(BusinessObjectLight businessObject) {
        return businessObjectNodes.get(businessObject);
    }
    
    public MapEdge findEdge(BusinessObjectLight businessObject) {
        return businessObjectEdges.get(businessObject);
    }
    
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        detached = true;
    }
    
    public boolean getDetached() {
        return detached;
    }
}
