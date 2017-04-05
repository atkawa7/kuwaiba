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

import com.neotropic.vaadin.lienzo.client.LienzoComponentState;
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

import com.vaadin.ui.AbstractComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class representing LienzoPanel Server-side component
 * @author Johny Andres Ortega Ruiz johny.ortega@kuwaiba.org
 */
// This is the server-side UI component that provides public API 
// for LienzoComponent
public class LienzoComponent extends AbstractComponent {
    protected Map<Long, LienzoNode> nodes = new HashMap<>();
            
    private LienzoMouseOverServerRpc lienzoMouseOverServerRpc = new LienzoMouseOverServerRpc() {

        @Override
        public void lienzoMouseOver(int x, int y) {
            for (LienzoMouseOverListener listener : lienzoMouseOverListeners)
                listener.lienzoMouseOver(x, y);
        }
    };
    
    private LienzoNodeClickedServerRpc lienzoNodeClickedServerRpc = new LienzoNodeClickedServerRpc() {

        @Override
        public void lienzoNodeClicked(long id) {
            for (LienzoNodeClickListener listener : lienzoNodeClickListeners)
                listener.lienzoNodeClicked(id);
        }
        
    };
    
    private LienzoNodeRightClickedServerRpc lienzoNodeRightClickedServerRpc = new LienzoNodeRightClickedServerRpc() {

        @Override
        public void lienzoNodeRightClicked(long id) {
            for (LienzoNodeRightClickListener listener : lienzoNodeRightClickListeners)
                listener.lienzoNodeRightClicked(id);
        }
    };
    
    private LienzoNodeDblClickedServerRpc lienzoNodeDblClickedServerRpc = new LienzoNodeDblClickedServerRpc() {

        @Override
        public void lienzoNodeDblClicked(long id) {
            for (LienzoNodeDblClickListener listener : lienzoNodeDblClickListeners)
                listener.lienzoNodeDoubleClicked(id);
        }
    };
    
    List<LienzoMouseOverListener> lienzoMouseOverListeners = new ArrayList();
    List<LienzoNodeClickListener> lienzoNodeClickListeners = new ArrayList();
    List<LienzoNodeRightClickListener> lienzoNodeRightClickListeners = new ArrayList();
    List<LienzoNodeDblClickListener> lienzoNodeDblClickListeners = new ArrayList();
        
    public LienzoComponent() {
        // To receive events from the client, we register ServerRpc
        registerRpc(lienzoMouseOverServerRpc);
        registerRpc(lienzoNodeClickedServerRpc);
        registerRpc(lienzoNodeRightClickedServerRpc);
        registerRpc(lienzoNodeDblClickedServerRpc);
    }
    
    // We must override getState() to cast the state to LienzoComponentState
    @Override
    public LienzoComponentState getState() {
        return (LienzoComponentState) super.getState();
    }
    
    public void addLienzoMouseOverListener(LienzoMouseOverListener listener) {
        lienzoMouseOverListeners.add(listener);
    }
    
    public void removeLienzoMouseOverListener(LienzoMouseOverListener listener) {
        lienzoMouseOverListeners.remove(listener);
    }
    
    public void addLienzoNodeClickListener(LienzoNodeClickListener listener) {
        lienzoNodeClickListeners.add(listener);
    }
    
    public void removeLienzoNodeClickListener(LienzoNodeClickListener listener) {
        lienzoNodeClickListeners.remove(listener);
    }
    
    public void addLienzoNodeRightClickListener(LienzoNodeRightClickListener listener) {
        lienzoNodeRightClickListeners.add(listener);
    }
    
    public void removeLienzoNodeRightClickListener(LienzoNodeRightClickListener listener) {
        lienzoNodeRightClickListeners.remove(listener);
    }
    
    public void addLienzoNodeDblClickListener(LienzoNodeDblClickListener listener) {
        lienzoNodeDblClickListeners.add(listener);
    }
    
    public void removeLienzoNodeDblClickListener(LienzoNodeDblClickListener listener) {
        lienzoNodeDblClickListeners.remove(listener);
    }
    
    public void addLienzoNode(LienzoNode node) {
        if (node == null)        
            return;
        
        nodes.put(node.getId(), node);
        getRpcProxy(AddLienzoNodeClientRpc.class).addLienzoNode(node);
    }
    
    public void removeLienzoNode(Long id) {
        if (id == null)
            return;
        
        LienzoNode node = nodes.remove(id);
        getRpcProxy(RemoveLienzoNodeClientRpc.class).removeLienzoNode(node.getId());
    }
}
