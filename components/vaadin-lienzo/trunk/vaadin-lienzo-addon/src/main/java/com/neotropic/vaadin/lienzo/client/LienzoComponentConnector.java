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
import com.neotropic.vaadin.lienzo.client.core.shape.LienzoNode;
import com.neotropic.vaadin.lienzo.client.events.LienzoMouseOverListener;
import com.neotropic.vaadin.lienzo.client.events.LienzoNodeClickListener;
import com.neotropic.vaadin.lienzo.client.events.LienzoNodeDblClickListener;
import com.neotropic.vaadin.lienzo.client.events.LienzoNodeRightClickListener;
import com.neotropic.vaadin.lienzo.client.rpcs.AddLienzoNodeClientRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.LienzoMouseOverServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.LienzoNodeClickedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.LienzoNodeDblClickedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.LienzoNodeRightClickedServerRpc;
import com.neotropic.vaadin.lienzo.client.rpcs.RemoveLienzoNodeClientRpc;
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
    LienzoNodeClickListener, LienzoNodeRightClickListener, LienzoNodeDblClickListener {

    // ServerRpc is used to send events to server
    LienzoMouseOverServerRpc lienzoMouseOverServerRpc = 
        RpcProxy.create(LienzoMouseOverServerRpc.class, this);
    LienzoNodeClickedServerRpc lienzoNodeClickedServerRpc = 
        RpcProxy.create(LienzoNodeClickedServerRpc.class, this);
    LienzoNodeRightClickedServerRpc lienzoNodeRightClickedServerRpc = 
        RpcProxy.create(LienzoNodeRightClickedServerRpc.class, this);
    LienzoNodeDblClickedServerRpc lienzoNodeDblClickedServerRpc = 
        RpcProxy.create(LienzoNodeDblClickedServerRpc.class, this);
    
    // ClientRpc is used to receive RPC events from server
    AddLienzoNodeClientRpc addLienzoNodeClientRpc = new AddLienzoNodeClientRpc() {

        @Override
        public void addLienzoNode(LienzoNode lienzoNode) {
            getWidget().addLienzoNode(lienzoNode);
        }
    };
    RemoveLienzoNodeClientRpc removeLienzoNodeClientRpc = new RemoveLienzoNodeClientRpc() {

        @Override
        public void removeLienzoNode(long id) {
            //TODO: remove lienzo node
        }
    };
        
    
    public LienzoComponentConnector() {
        // Register ClientRpc implementation
        registerRpc(AddLienzoNodeClientRpc.class, addLienzoNodeClientRpc);
        registerRpc(RemoveLienzoNodeClientRpc.class, removeLienzoNodeClientRpc);
                                
        getWidget().setLienzoMouseOverListener(this);
        getWidget().setLienzoNodeClickListener(this);
        getWidget().setLienzoNodeRightClickListener(this);
        getWidget().setLienzoNodeDblClickListener(this);
        
        getWidget().drawLayers();
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
	boolean connectionTool = getState().enableConnectionTool;
    }
    
    @Override
    public void lienzoMouseOver(int x, int y) {
        lienzoMouseOverServerRpc.lienzoMouseOver(x, y);
    }

    @Override
    public void lienzoNodeClicked(long id) {
        lienzoNodeClickedServerRpc.lienzoNodeClicked(id);
    }

    @Override
    public void lienzoNodeRightClicked(long id) {
        lienzoNodeRightClickedServerRpc.lienzoNodeRightClicked(id);
    }

    @Override
    public void lienzoNodeDoubleClicked(long id) {
        lienzoNodeDblClickedServerRpc.lienzoNodeDblClicked(id);
    }

}
