 /*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.vaadin.lienzo;

import com.google.gson.Gson;
import com.neotropic.vaadin.lienzo.client.LienzoComponentState;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvEdgeWidget;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvNodeWidget;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvFrameWidget;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetAddListener;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetClickListener;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetDblClickListener;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetRightClickListener;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetUpdateListener;
import com.neotropic.vaadin.lienzo.client.events.FrameWidgetClickListener;
import com.neotropic.vaadin.lienzo.client.events.FrameWidgetDblClickListener;
import com.neotropic.vaadin.lienzo.client.events.FrameWidgetRightClickListener;
import com.neotropic.vaadin.lienzo.client.events.FrameWidgetUpdateListener;
import com.neotropic.vaadin.lienzo.client.events.LienzoMouseOverListener;
import com.neotropic.vaadin.lienzo.client.events.NodeWidgetClickListener;
import com.neotropic.vaadin.lienzo.client.events.NodeWidgetDblClickListener;
import com.neotropic.vaadin.lienzo.client.events.NodeWidgetRightClickListener;
import com.neotropic.vaadin.lienzo.client.events.NodeWidgetUpdateListener;
import com.neotropic.vaadin.lienzo.client.rpcs.AddEdgeWidgetClientRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.AddFrameWidgetClientRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.AddNodeWidgetClientRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.EdgeWidgetAddedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.EdgeWidgetClickedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.EdgeWidgetDblClickedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.EdgeWidgetRightClickedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.EdgeWidgetUpdatedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.FrameWidgetClickedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.FrameWidgetDblClickedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.FrameWidgetRightClickedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.FrameWidgetUpdatedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.LienzoMouseOverServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.NodeWidgetClickedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.NodeWidgetDblClickedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.NodeWidgetRightClickedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.NodeWidgetUpdatedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.RemoveEdgeWidgetClientRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.RemoveFrameWidgetClientRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.RemoveNodeWidgetClientRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.UpdateEdgeWidgetClientRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.UpdateFrameWidgetClientRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.UpdateNodeWidgetClientRpc;

import com.vaadin.ui.AbstractComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class representing LienzoPanel Server-side component
 * @author Johny Andres Ortega Ruiz johny.ortega@kuwaiba.org
 * @param <N> Node Object Type
 * @param <E> Edge Object Type
 */
// This is the server-side UI component that provides public API 
// for LienzoComponent
public class LienzoComponent<N, E> extends AbstractComponent {
    private final HashMap<N, String> nodeObjects = new HashMap();
    private final HashMap<E, String> edgeObjects = new HashMap();
    
    private Map<Long, SrvFrameWidget> frames = new HashMap<>();
    private Map<String, SrvNodeWidget> nodes = new HashMap<>();
    private final Map<String, SrvEdgeWidget> edges = new HashMap<>();
    
    private final Map<SrvEdgeWidget, SrvNodeWidget> edgeSourceNodes = new HashMap<>();
    private final Map<SrvEdgeWidget, SrvNodeWidget> edgeTargetNodes = new HashMap<>();
    
    private final Map<SrvNodeWidget, List<SrvEdgeWidget>> nodeInputEdges = new HashMap<>();
    private final Map<SrvNodeWidget, List<SrvEdgeWidget>> nodeOutputEdges = new HashMap<>();
    
    private final LienzoMouseOverServerRpc lienzoMouseOverServerRpc = new LienzoMouseOverServerRpc() {

        @Override
        public void lienzoMouseOver(int x, int y) {
            for (LienzoMouseOverListener listener : lienzoMouseOverListeners)
                listener.lienzoMouseOver(x, y);
        }
    };
    
    private final NodeWidgetClickedServerRpc nodeWidgetClickedServerRpc = new NodeWidgetClickedServerRpc() {

        @Override
        public void lienzoNodeClicked(String id) {
            for (NodeWidgetClickListener listener : nodeWidgetClickListeners)
                listener.nodeWidgetClicked(id);
        }
    };    
    private final NodeWidgetRightClickedServerRpc nodeWidgetRightClickedServerRpc = new NodeWidgetRightClickedServerRpc() {

        @Override
        public void lienzoNodeRightClicked(String id) {
            for (NodeWidgetRightClickListener listener : nodeWidgetRightClickListeners)
                listener.nodeWidgetRightClicked(id);
        }
    };    
    private final NodeWidgetDblClickedServerRpc nodeWidgetDblClickedServerRpc = new NodeWidgetDblClickedServerRpc() {

        @Override
        public void lienzoNodeDblClicked(String id) {
            for (NodeWidgetDblClickListener listener : nodeWidgetDblClickListeners)
                listener.nodeWidgetDoubleClicked(id);
        }
    };
    private final NodeWidgetUpdatedServerRpc nodeWidgetUpdatedServerRpc = new NodeWidgetUpdatedServerRpc() {
    
        @Override
        public void nodeWidgetUpdated(SrvNodeWidget clntNode) {
            SrvNodeWidget srvNode = nodes.get(clntNode.getId());
            srvNode.setX(clntNode.getX());
            srvNode.setY(clntNode.getY());
            
            for (NodeWidgetUpdateListener listener : nodeWidgetUpdateListeners)
                listener.nodeWidgetUpdated(srvNode);
        }
    };
    
    private final FrameWidgetClickedServerRpc frameWidgetClickedServerRpc = new FrameWidgetClickedServerRpc() {

        @Override
        public void frameWidgetClicked(long id) {
            for (FrameWidgetClickListener listener : frameWidgetClickListeners)
                listener.frameWidgetClicked(id);
        }
    };
    private final FrameWidgetDblClickedServerRpc frameWidgetDblClickedServerRpc = new FrameWidgetDblClickedServerRpc() {

        @Override
        public void frameWidgetDblClicked(long id) {
            for (FrameWidgetDblClickListener listener : frameWidgetDblClickListeners)
                listener.frameWidgetDblClicked(id);
        }
    };
    private final FrameWidgetRightClickedServerRpc frameWidgetRightClickedServerRpc = new FrameWidgetRightClickedServerRpc() {

        @Override
        public void frameWidgetRightClicked(long id) {
            for (FrameWidgetRightClickListener listener : frameWidgetRightClickListeners)
                listener.frameWidgetRightClicked(id);
        }
    };
    private final FrameWidgetUpdatedServerRpc frameWidgetUpdatedServerRpc = new FrameWidgetUpdatedServerRpc() {

        @Override
        public void frameWidgetUpdated(SrvFrameWidget svrFrameWidget) {
            SrvFrameWidget oldSrvFrame = frames.get(svrFrameWidget.getId());
            
            oldSrvFrame.setCaption(svrFrameWidget.getCaption());
            oldSrvFrame.setX(svrFrameWidget.getX());
            oldSrvFrame.setY(svrFrameWidget.getY());
            oldSrvFrame.setWidth(svrFrameWidget.getWidth());
            oldSrvFrame.setHeight(svrFrameWidget.getHeight());
            
            for (FrameWidgetUpdateListener listener : frameWidgetUpdateListeners)
                listener.frameWidgetUpdated(oldSrvFrame);
        }
    };
    
    private final EdgeWidgetAddedServerRpc edgeWidgetAddedServerRpc = new EdgeWidgetAddedServerRpc() {

        @Override
        public void edgeWidgetAdded(SrvEdgeWidget clntEdge) {
            for (EdgeWidgetAddListener listener : edgeWidgetAddListeners)
                listener.edgeWidgetAdded(clntEdge);
        }
    };
    private final EdgeWidgetClickedServerRpc edgeWidgetClickedServerRpc = new EdgeWidgetClickedServerRpc() {

        @Override
        public void edgeWidgetClicked(String id) {
            for (EdgeWidgetClickListener listener : edgeWidgetClickListeners)
                listener.edgeWidgetClicked(id);
        }
    };
    private final EdgeWidgetDblClickedServerRpc edgeWidgetDblClickedServerRpc = new EdgeWidgetDblClickedServerRpc() {

        @Override
        public void edgeWidgetDblClicked(String id) {
            for (EdgeWidgetDblClickListener listener : edgeWidgetDblClickListeners)
                listener.edgeWidgetDblClicked(id);
        }
    };
    private final EdgeWidgetRightClickedServerRpc edgeWidgetRightClickedServerRpc = new EdgeWidgetRightClickedServerRpc() {

        @Override
        public void edgeWidgetRightClicked(String id) {
            for (EdgeWidgetRightClickListener listener : edgeWidgetRightClickListeners)
                listener.edgeWidgetRightClicked(id);
        }
    };
    private final EdgeWidgetUpdatedServerRpc edgeWidgetUpdatedServerRpc = new EdgeWidgetUpdatedServerRpc() {

        @Override
        public void edgeWidgetUpdated(SrvEdgeWidget clntEdge) {
            SrvEdgeWidget srvEdge = edges.get(clntEdge.getId());
            srvEdge.setControlPoints(clntEdge.getControlPoints());
            
            for (EdgeWidgetUpdateListener listener : edgeWidgetUpdateListeners)
                listener.edgeWidgetUpdated(clntEdge);
        }
    };
    
    List<LienzoMouseOverListener> lienzoMouseOverListeners = new ArrayList();
    
    List<NodeWidgetClickListener> nodeWidgetClickListeners = new ArrayList();
    List<NodeWidgetRightClickListener> nodeWidgetRightClickListeners = new ArrayList();
    List<NodeWidgetDblClickListener> nodeWidgetDblClickListeners = new ArrayList();
    List<NodeWidgetUpdateListener> nodeWidgetUpdateListeners = new ArrayList();
    
    List<FrameWidgetClickListener> frameWidgetClickListeners = new ArrayList();
    List<FrameWidgetDblClickListener> frameWidgetDblClickListeners = new ArrayList();
    List<FrameWidgetRightClickListener> frameWidgetRightClickListeners = new ArrayList();
    List<FrameWidgetUpdateListener> frameWidgetUpdateListeners = new ArrayList();
    
    List<EdgeWidgetAddListener> edgeWidgetAddListeners = new ArrayList();
    List<EdgeWidgetClickListener> edgeWidgetClickListeners = new ArrayList();
    List<EdgeWidgetDblClickListener> edgeWidgetDblClickListeners = new ArrayList();
    List<EdgeWidgetRightClickListener> edgeWidgetRightClickListeners = new ArrayList();
    List<EdgeWidgetUpdateListener> edgeWidgetUpdateListeners = new ArrayList();
        
    public LienzoComponent() {
        // To receive events from the client, we register ServerRpc
        registerRpc(lienzoMouseOverServerRpc);
        
        registerRpc(nodeWidgetClickedServerRpc);
        registerRpc(nodeWidgetRightClickedServerRpc);
        registerRpc(nodeWidgetDblClickedServerRpc);
        registerRpc(nodeWidgetUpdatedServerRpc);
        
        registerRpc(frameWidgetClickedServerRpc);
        registerRpc(frameWidgetDblClickedServerRpc);
        registerRpc(frameWidgetRightClickedServerRpc);
        registerRpc(frameWidgetUpdatedServerRpc);
        
        registerRpc(edgeWidgetAddedServerRpc);
        registerRpc(edgeWidgetClickedServerRpc);
        registerRpc(edgeWidgetDblClickedServerRpc);
        registerRpc(edgeWidgetRightClickedServerRpc);
        registerRpc(edgeWidgetUpdatedServerRpc);
    }
    
    // We must override getState() to cast the state to LienzoComponentState
    @Override
    public LienzoComponentState getState() {
        return (LienzoComponentState) super.getState();
    }
    
    public boolean isEnableConnectionTool() {
        return getState().enableConnectionTool;
    }
    
    public void setEnableConnectionTool(boolean enableConnectionTool) {
        getState().enableConnectionTool = enableConnectionTool;
    }
    
    public void addLienzoMouseOverListener(LienzoMouseOverListener listener) {
        lienzoMouseOverListeners.add(listener);
    }
    public void removeLienzoMouseOverListener(LienzoMouseOverListener listener) {
        lienzoMouseOverListeners.remove(listener);
    }
    
    public void addNodeWidgetClickListener(NodeWidgetClickListener listener) {
        nodeWidgetClickListeners.add(listener);
    }
    public void removeNodeWidgetClickListener(NodeWidgetClickListener listener) {
        nodeWidgetClickListeners.remove(listener);
    }    
    public void addNodeWidgetRightClickListener(NodeWidgetRightClickListener listener) {
        nodeWidgetRightClickListeners.add(listener);
    }
    public void removeNodeWidgetRightClickListener(NodeWidgetRightClickListener listener) {
        nodeWidgetRightClickListeners.remove(listener);
    }    
    public void addNodeWidgetDblClickListener(NodeWidgetDblClickListener listener) {
        nodeWidgetDblClickListeners.add(listener);
    }
    public void removeNodeWidgetDblClickListener(NodeWidgetDblClickListener listener) {
        nodeWidgetDblClickListeners.remove(listener);
    }
    public void addNodeWidgetUpdateListener(NodeWidgetUpdateListener listener) {
        nodeWidgetUpdateListeners.add(listener);
    }
    public void removeNodeWidgetUpdateListener(NodeWidgetUpdateListener listener) {
        nodeWidgetUpdateListeners.remove(listener);
    }
    
    public void addFrameWidgetClickListener(FrameWidgetClickListener listener) {
        frameWidgetClickListeners.add(listener);
    }
    public void removeFrameWidgetClickListener(FrameWidgetClickListener listener) {
        frameWidgetClickListeners.remove(listener);
    }    
    public void addFrameWidgetDblClickListener(FrameWidgetDblClickListener listener) {
        frameWidgetDblClickListeners.add(listener);
    }
    public void removeFrameWidgetDblClickListener(FrameWidgetDblClickListener listener) {
        frameWidgetDblClickListeners.remove(listener);
    }    
    public void addFrameWidgetRightClickListener(FrameWidgetRightClickListener listener) {
        frameWidgetRightClickListeners.add(listener);
    }
    public void removeFrameWidgetRightClickListener(FrameWidgetRightClickListener listener) {
        frameWidgetRightClickListeners.remove(listener);
    }    
    public void addFrameWidgetUpdateListener(FrameWidgetUpdateListener listener) {
        frameWidgetUpdateListeners.add(listener);
    }
    public void removerFrameWidgetUpdateListener(FrameWidgetUpdateListener listener) {
        frameWidgetUpdateListeners.remove(listener);
    }
    
    public void addEdgeWidgetAddListener(EdgeWidgetAddListener listener) {
        edgeWidgetAddListeners.add(listener);
    }
    public void removeEdgeWidgetAddListener(EdgeWidgetAddListener listener) {
        edgeWidgetAddListeners.remove(listener);
    }
    public void addEdgeWidgetClickListener(EdgeWidgetClickListener listener) {
        edgeWidgetClickListeners.add(listener);
    }
    public void removeEdgeWidgetClickListener(EdgeWidgetClickListener listener) {
        edgeWidgetClickListeners.remove(listener);
    }
    public void addEdgeWidgetDblClickListener(EdgeWidgetDblClickListener listener) {
        edgeWidgetDblClickListeners.add(listener);
    }
    public void removeEdgeWidgetDblClickListener(EdgeWidgetDblClickListener listener) {
        edgeWidgetDblClickListeners.remove(listener);
    }
    public void addEdgeWidgetRightClickListener(EdgeWidgetRightClickListener listener) {
        edgeWidgetRightClickListeners.add(listener);
    }
    public void removeEdgeWidgetRightClickListener(EdgeWidgetRightClickListener listener) {
        edgeWidgetRightClickListeners.remove(listener);
    }
    public void addEdgeWidgetUpdateListener(EdgeWidgetUpdateListener listener) {
        edgeWidgetUpdateListeners.add(listener);
    }
    public void removeEdgeWidgetUpdateListener(EdgeWidgetUpdateListener listener) {
        edgeWidgetUpdateListeners.remove(listener);
    }    
    
    public void addBackground(String url, double x, double y) {
        getState().backgroundUrl = url;
        getState().backgroundX = x;
        getState().backgroundY = y;        
    }    
    public void removeBackground() {
        getState().backgroundUrl = null;
    }
    
    public N getNodeObject(String id) {
        if (id != null) {
            for (N nodeObject : nodeObjects.keySet()) {
                if (id.equals(nodeObjects.get(nodeObject)))
                    return nodeObject;
            }
        }
        return null;
    }
    
    public SrvNodeWidget getNodeWidget(N nodeObject) {
        if (nodeObjects.containsKey(nodeObject))
            return nodes.get(nodeObjects.get(nodeObject));
        return null;
    }
    public void addNodeWidget(N nodeObject, SrvNodeWidget node) {
        if (node == null)        
            return;
        String id = new Gson().toJson(nodeObject);
        node.setId(id);
        
        nodeObjects.put(nodeObject, node.getId());
        nodes.put(node.getId(), node);
        getRpcProxy(AddNodeWidgetClientRpc.class).addNodeWidget(node);
    }
    public void updateNodeWidget(N nodeObject) {
        if (nodeObjects.containsKey(nodeObject) && nodes.containsKey(nodeObjects.get(nodeObject)))
            getRpcProxy(UpdateNodeWidgetClientRpc.class).updateNodeWidget(nodes.get(nodeObjects.get(nodeObject)));
    }
    public void removeNodeWidget(N nodeObject) {
        if (nodeObjects.containsKey(nodeObject) && nodes.containsKey(nodeObjects.get(nodeObject))) {
            SrvNodeWidget node = nodes.remove(nodeObjects.get(nodeObject));
            nodeObjects.remove(nodeObject);
            
            if (nodeOutputEdges.containsKey(node)) {
                for (SrvEdgeWidget edge : nodeOutputEdges.remove(node)) {
                    edges.remove(edge.getId());
                    edgeSourceNodes.remove(edge);
                    
                    SrvNodeWidget target = edgeTargetNodes.remove(edge);
                    nodeInputEdges.get(target).remove(edge);
                }
            }
            if (nodeInputEdges.containsKey(node)) {
                for (SrvEdgeWidget edge : nodeInputEdges.remove(node)) {
                    edges.remove(edge.getId());
                    edgeTargetNodes.remove(edge);
                    
                    SrvNodeWidget source = edgeSourceNodes.remove(edge);
                    nodeOutputEdges.get(source).remove(edge);
                }
            }
            getRpcProxy(RemoveNodeWidgetClientRpc.class).removeNodeWidget(node);
        }
    }    

    public SrvFrameWidget getFrameWidget(long id) {
        return frames.get(id);
    }    
    public void addFrameWidget(SrvFrameWidget frame) {
        frames.put(frame.getId(), frame);
        getRpcProxy(AddFrameWidgetClientRpc.class).addFrameWidget(frame);
    }    
    public void updateFrameWidget(SrvFrameWidget srvFrame) {
        getRpcProxy(UpdateFrameWidgetClientRpc.class).updateFrameWidget(srvFrame);
    }    
    public void removeFrameWidget(long id) {
        SrvFrameWidget srvFrame = frames.remove(id);
        getRpcProxy(RemoveFrameWidgetClientRpc.class).removeFrameWidget(srvFrame);
    }
    
    public E getEdgeObject(String id) {
        if (id != null) {
            for (E edgeObject : edgeObjects.keySet()) {
                if (id.equals(edgeObjects.get(edgeObject)))
                    return edgeObject;
            }
        }
        return null;
    }
    
    public SrvEdgeWidget getEdge(E id) {
        if (edgeObjects.containsKey(id))
            return edges.get(edgeObjects.get(id));
        return null;
    } 
    
    public List<SrvEdgeWidget> getNodeEdgeWidgets(SrvNodeWidget srvNodeWidget){
        List<SrvEdgeWidget> edgeWidgets = new ArrayList<>();
        if (nodeOutputEdges.containsKey(srvNodeWidget))
            edgeWidgets.addAll(nodeOutputEdges.get(srvNodeWidget));

        if (nodeInputEdges.containsKey(srvNodeWidget))
            edgeWidgets.addAll(nodeInputEdges.get(srvNodeWidget));
        
        return edgeWidgets;
    }
    
    public void addEdgeWidget(E edgeObject, SrvEdgeWidget srvEdge) {
        String id = new Gson().toJson(edgeObject);
        srvEdge.setId(id);
        
        edgeObjects.put(edgeObject, srvEdge.getId());
        edges.put(srvEdge.getId(), srvEdge);
        
        edgeSourceNodes.put(srvEdge, srvEdge.getSource());
        edgeTargetNodes.put(srvEdge, srvEdge.getTarget());
        
        if (!nodeOutputEdges.containsKey(srvEdge.getSource()))
            nodeOutputEdges.put(srvEdge.getSource(), new ArrayList());
        nodeOutputEdges.get(srvEdge.getSource()).add(srvEdge);
        
        if (!nodeInputEdges.containsKey(srvEdge.getTarget()))
            nodeInputEdges.put(srvEdge.getTarget(), new ArrayList());
        nodeInputEdges.get(srvEdge.getTarget()).add(srvEdge);
        
        getRpcProxy(AddEdgeWidgetClientRpc.class).addEdgeWidget(srvEdge);
    }    
    public void updateEdgeWidget(E edgeObject) {
        if (edgeObjects.containsKey(edgeObject) && edges.containsKey(edgeObjects.get(edgeObject)))
            getRpcProxy(UpdateEdgeWidgetClientRpc.class).updateEdgeWidget(edges.get(edgeObjects.get(edgeObject)));
    }    
    public void removeEdgeWidget(E edgeObject) {
        if (edgeObjects.containsKey(edgeObject) && edges.containsKey(edgeObjects.get(edgeObject))) {
            SrvEdgeWidget srvEdge = edges.remove(edgeObjects.get(edgeObject));
            edgeObjects.remove(edgeObject);
            
            SrvNodeWidget source = edgeSourceNodes.remove(srvEdge);
            SrvNodeWidget target = edgeTargetNodes.remove(srvEdge);
            
            if (nodeOutputEdges.containsKey(source))
                nodeOutputEdges.get(source).remove(srvEdge);
            
            if (nodeInputEdges.containsKey(target))
                nodeInputEdges.get(target).remove(srvEdge);
                        
            getRpcProxy(RemoveEdgeWidgetClientRpc.class).removeEdgeWidget(srvEdge);
        }
    }
}
