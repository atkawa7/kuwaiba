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
package com.neotropic.vaadin.lienzo.client;

import com.neotropic.vaadin.lienzo.LienzoComponent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
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
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

/**
 * @author Johny Andres Ortega Ruiz johny.ortega@kuwaiba.org
 */
// Connector binds client-side widget class to server-side component class
// Connector lives in the client and the @Connect annotation specifies the
// corresponding server-side component
@Connect(LienzoComponent.class)
public class LienzoComponentConnector extends AbstractComponentConnector implements 
    LienzoMouseOverListener, 
    NodeWidgetClickListener, NodeWidgetRightClickListener, NodeWidgetDblClickListener, NodeWidgetUpdateListener, 
    FrameWidgetClickListener, FrameWidgetDblClickListener, FrameWidgetRightClickListener, FrameWidgetUpdateListener,
    EdgeWidgetAddListener, EdgeWidgetClickListener, EdgeWidgetDblClickListener, EdgeWidgetRightClickListener, EdgeWidgetUpdateListener {

    // ServerRpc is used to send events to server
    LienzoMouseOverServerRpc lienzoMouseOverServerRpc = 
        RpcProxy.create(LienzoMouseOverServerRpc.class, this);
    
    NodeWidgetClickedServerRpc nodeWidgetClickedServerRpc = 
        RpcProxy.create(NodeWidgetClickedServerRpc.class, this);
    NodeWidgetRightClickedServerRpc nodeWidgetRightClickedServerRpc = 
        RpcProxy.create(NodeWidgetRightClickedServerRpc.class, this);
    NodeWidgetDblClickedServerRpc nodeWidgetDblClickedServerRpc = 
        RpcProxy.create(NodeWidgetDblClickedServerRpc.class, this);
    NodeWidgetUpdatedServerRpc nodeWidgetUpdatedServerRpc = 
        RpcProxy.create(NodeWidgetUpdatedServerRpc.class, this);
    
    FrameWidgetClickedServerRpc frameWidgetClickedServerRpc = 
        RpcProxy.create(FrameWidgetClickedServerRpc.class, this);
    FrameWidgetDblClickedServerRpc frameWidgetDblClickedServerRpc = 
        RpcProxy.create(FrameWidgetDblClickedServerRpc.class, this);
    FrameWidgetRightClickedServerRpc frameWidgetRightClickedServerRpc = 
        RpcProxy.create(FrameWidgetRightClickedServerRpc.class, this);
    FrameWidgetUpdatedServerRpc frameWidgetUpdatedServerRpc = 
        RpcProxy.create(FrameWidgetUpdatedServerRpc.class, this);
    
    EdgeWidgetAddedServerRpc edgeWidgetAddedServerRpc = 
        RpcProxy.create(EdgeWidgetAddedServerRpc.class, this);
    EdgeWidgetClickedServerRpc edgeWidgetClickedServerRpc = 
        RpcProxy.create(EdgeWidgetClickedServerRpc.class, this);
    EdgeWidgetDblClickedServerRpc edgeWidgetDblClickedServerRpc = 
        RpcProxy.create(EdgeWidgetDblClickedServerRpc.class, this);
    EdgeWidgetRightClickedServerRpc edgeWidgetRightClickedServerRpc = 
        RpcProxy.create(EdgeWidgetRightClickedServerRpc.class, this);
    EdgeWidgetUpdatedServerRpc edgeWidgetUpdatedServerRpc = 
        RpcProxy.create(EdgeWidgetUpdatedServerRpc.class, this);
    // ClientRpc is used to receive RPC events from server
    AddNodeWidgetClientRpc addNodeWidgetClientRpc = new AddNodeWidgetClientRpc() {

        @Override
        public void addNodeWidget(SrvNodeWidget srvNode) {
            getWidget().addNodeFromServer(srvNode);
        }
    };    
    UpdateNodeWidgetClientRpc updateNodeWidgetClientRpc = new UpdateNodeWidgetClientRpc() {

        @Override
        public void updateNodeWidget(SrvNodeWidget srvNode) {
            getWidget().updateNodeFromServer(srvNode);
        }
    };    
    RemoveNodeWidgetClientRpc removeNodeWidgetClientRpc = new RemoveNodeWidgetClientRpc() {

        @Override
        public void removeNodeWidget(SrvNodeWidget srvNode) {
            getWidget().removeNodeFromServer(srvNode);
        }
    };
    
    AddFrameWidgetClientRpc addFrameWidgetClientRpc = new AddFrameWidgetClientRpc() {

        @Override
        public void addFrameWidget(SrvFrameWidget srvFrame) {
            getWidget().addFrameFromServer(srvFrame);
        }
    };
    UpdateFrameWidgetClientRpc updateFrameWidgetClientRpc = new UpdateFrameWidgetClientRpc(){

        @Override
        public void updateFrameWidget(SrvFrameWidget srvFrame) {
            getWidget().updateFrameFromServer(srvFrame);
        }
    };
    RemoveFrameWidgetClientRpc removeFrameWidgetClientRpc = new RemoveFrameWidgetClientRpc() {

        @Override
        public void removeFrameWidget(SrvFrameWidget srvFrame) {
            getWidget().removeFrameFromServer(srvFrame);
        }
    };
    
    AddEdgeWidgetClientRpc addEdgeWidgetClientRpc = new AddEdgeWidgetClientRpc() {

        @Override
        public void addEdgeWidget(SrvEdgeWidget srvEdge) {
            getWidget().addEdgeFromServer(srvEdge);
        }
    };
    UpdateEdgeWidgetClientRpc updateEdgeWidgetClientRpc = new UpdateEdgeWidgetClientRpc() {

        @Override
        public void updateEdgeWidget(SrvEdgeWidget srvEdge) {
            getWidget().updateEdgeFromServer(srvEdge);
        }
    };
    RemoveEdgeWidgetClientRpc removeEdgeWidgetClientRpc = new RemoveEdgeWidgetClientRpc() {

        @Override
        public void removeEdgeWidget(SrvEdgeWidget srvEdge) {
            getWidget().removeEdgeFromServer(srvEdge);
        }
    };
    
    public LienzoComponentConnector() {
        // Register ClientRpc implementation
        registerRpc(AddNodeWidgetClientRpc.class, addNodeWidgetClientRpc);
        registerRpc(UpdateNodeWidgetClientRpc.class, updateNodeWidgetClientRpc);
        registerRpc(RemoveNodeWidgetClientRpc.class, removeNodeWidgetClientRpc);
        
        registerRpc(AddFrameWidgetClientRpc.class, addFrameWidgetClientRpc);
        registerRpc(UpdateFrameWidgetClientRpc.class, updateFrameWidgetClientRpc);
        registerRpc(RemoveFrameWidgetClientRpc.class, removeFrameWidgetClientRpc);
        
        registerRpc(AddEdgeWidgetClientRpc.class, addEdgeWidgetClientRpc);
        registerRpc(UpdateEdgeWidgetClientRpc.class, updateEdgeWidgetClientRpc);
        registerRpc(RemoveEdgeWidgetClientRpc.class, removeEdgeWidgetClientRpc);
        
        getWidget().setLienzoMouseOverListener(this);
        
        getWidget().setNodeWidgetClickListener(this);
        getWidget().setNodeWidgetRightClickListener(this);
        getWidget().setNodeWidgetDblClickListener(this);
        getWidget().setNodeWidgetUpdateListener(this);
        
        getWidget().setFrameWidgetClickListener(this);
        getWidget().setFrameWidgetDblClickListener(this);
        getWidget().setFrameWidgetRightClickListener(this);
        getWidget().setFrameWidgetUpdateListener(this);
        
        getWidget().setEdgeWidgetAddListener(this);
        getWidget().setEdgeWidgetClickListener(this);
        getWidget().setEdgeWidgetDblClickListener(this);
        getWidget().setEdgeWidgetRightClickListener(this);
        getWidget().setEdgeWidgetUpdateListener(this);
    }
    
    // We must implement createWidget() to create correct type of widget
    @Override
    protected Widget createWidget() {
        return GWT.create(LienzoComponentWidget.class);
    }
    
    // We must implement getWidget() to cast to correct type
    @Override
    public LienzoComponentWidget getWidget() {
        return (LienzoComponentWidget) super.getWidget();
    }

    // We must implement getState() to cast to correct type
    @Override
    public LienzoComponentState getState() {
        return (LienzoComponentState) super.getState();
    }

    // Whenever the state changes in the server-side, this method is called
    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        // State is directly readable in the client after it is set in server        
        getWidget().setEnableConnectionTool(getState().enableConnectionTool);
        getWidget().setLabelsFontSize(getState().labelsFontSize);
        getWidget().setLabelsPaddingLeft(getState().labelsPaddingLeft);
        getWidget().setLabelsPaddingTop(getState().labelsPaddingTop);
        getWidget().setBackground(
            getState().backgroundUrl, 
            getState().backgroundX, 
            getState().backgroundY);
    }
    
    @Override
    public void lienzoMouseOver(int x, int y) {
        lienzoMouseOverServerRpc.lienzoMouseOver(x, y);
    }

    @Override
    public void nodeWidgetClicked(String id) {
        nodeWidgetClickedServerRpc.lienzoNodeClicked(id);
    }

    @Override
    public void nodeWidgetRightClicked(String id) {
        nodeWidgetRightClickedServerRpc.lienzoNodeRightClicked(id);
    }

    @Override
    public void nodeWidgetDoubleClicked(String id) {
        nodeWidgetDblClickedServerRpc.lienzoNodeDblClicked(id);
    }
    
    @Override
    public void nodeWidgetUpdated(SrvNodeWidget srvNode) {
        nodeWidgetUpdatedServerRpc.nodeWidgetUpdated(srvNode);
    }

    @Override
    public void frameWidgetClicked(long id) {
        frameWidgetClickedServerRpc.frameWidgetClicked(id);
    }

    @Override
    public void frameWidgetDblClicked(long id) {
        frameWidgetDblClickedServerRpc.frameWidgetDblClicked(id);
    }

    @Override
    public void frameWidgetRightClicked(long id) {
        frameWidgetRightClickedServerRpc.frameWidgetRightClicked(id);
    }

    @Override
    public void frameWidgetUpdated(SrvFrameWidget srvFrameWidget) {
        frameWidgetUpdatedServerRpc.frameWidgetUpdated(srvFrameWidget);
    }

    @Override
    public void edgeWidgetAdded(SrvEdgeWidget srvEdge) {
        edgeWidgetAddedServerRpc.edgeWidgetAdded(srvEdge);
    }

    @Override
    public void edgeWidgetClicked(String id) {
        edgeWidgetClickedServerRpc.edgeWidgetClicked(id);
    }

    @Override
    public void edgeWidgetDblClicked(String id) {
        edgeWidgetDblClickedServerRpc.edgeWidgetDblClicked(id);
    }

    @Override
    public void edgeWidgetRightClicked(String id) {
        edgeWidgetRightClickedServerRpc.edgeWidgetRightClicked(id);
    }
    
    @Override
    public void edgeWidgetUpdated(SrvEdgeWidget srvEdge) {
        edgeWidgetUpdatedServerRpc.edgeWidgetUpdated(srvEdge);
    }
}
