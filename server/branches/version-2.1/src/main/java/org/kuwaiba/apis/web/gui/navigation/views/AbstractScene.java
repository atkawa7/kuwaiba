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

package org.kuwaiba.apis.web.gui.navigation.views;

import com.neotropic.vaadin14.component.MxGraph;
import com.neotropic.vaadin14.component.MxGraphEdge;
import com.neotropic.vaadin14.component.MxGraphNode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.HashMap;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * The root class of all scenes (that is, those components based on Lienzo that display nodes and connections)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractScene extends VerticalLayout {
    /**
     * The component that is used to render the view at low level
     */
    protected MxGraph mxGraph;
    /**
     * A dictionary with the nodes in the current view
     */
    protected HashMap<RemoteObjectLight, MxGraphNode> nodes;
    /**
     * A dictionary with the edges in the current view
     */
    protected HashMap<RemoteObjectLight, MxGraphEdge> edges;
    /**
     * A reference to the backend bean
     */
    protected WebserviceBean wsBean;
    /**
     * Reference to the current session
     */
    protected RemoteSession session;

    public AbstractScene(WebserviceBean wsBean, RemoteSession session) {
        this.wsBean = wsBean;
        this.session = session;
        this.mxGraph = new MxGraph();
        this.mxGraph.setFullSize();
        this.nodes = new HashMap<>();
        this.edges = new HashMap<>();
    }
    
    public MxGraphNode findNodeWidget(RemoteObjectLight node) {
        return nodes.get(node);
    }
    
    public MxGraphEdge findEdgeWidget(RemoteObjectLight edge) {
        return edges.get(edge);
    }
    
    public MxGraphNode findNodeWidget(long nodeId) {
//////        for (RemoteObjectLight aNode : nodes.keySet()) {
//////            if (aNode.getId() == nodeId)
//////                return nodes.get(aNode);
//////        }
        return null;
    }    
    
    /**
     * Renders the view from an XML document (most likely a saved view). 
     * This method is usually called after the render(RemoteObjectLight) method and it's commonly used to set 
     * the position of the nodes and control points of the elements already created in that method.
     * @param structure The XML document
     * @throws IllegalArgumentException If the XML document is malformed
     */
    public abstract void render(byte[] structure) throws IllegalArgumentException;
    /**
     * Renders the default view. Most of the times, this view is built on-the-fly (that is, it's generated again every time this method is called)
     */
    public abstract void render();
}
