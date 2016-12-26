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
package org.kuwaiba.apis.web.gui.nodes;

import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.custom.tree.DynamicTree;

/**
 * Represents a node of a tree
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class InventoryObjectNode extends AbstractNode<RemoteObjectLight>{
    
    public InventoryObjectNode(RemoteObjectLight object) {
        super(object);
    }
    
    @Override
    public String getDisplayName() {
        return displayName == null ? getObject().toString() : displayName;
    }
    
    @Override
    public void setTree(DynamicTree tree) {
        super.setTree(tree);
        tree.setItemIcon(this, FontAwesome.SQUARE);
    }
    
    @Override
    public void expand() {
        if (getTree() == null) //If the tree has not been set previously, do nothing
            return;
        
        try {
            TopComponent topComponent = getTree().getTopComponent();
            RemoteObjectLight currentObject = (RemoteObjectLight)getObject();
            
            List<RemoteObjectLight> children = topComponent.getWsBean().getObjectChildren(
                    currentObject.getClassName(), currentObject.getOid(), -1,
                    Page.getCurrent().getWebBrowser().getAddress(),
                    topComponent.getApplicationSession().getSessionId());
            
            for (RemoteObjectLight child : children) {
                InventoryObjectNode childNode = new InventoryObjectNode(child);
                childNode.setTree(getTree());
                getTree().setParent(childNode, this);
            }
        }catch (ServerSideException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void collapse() {
        List<InventoryObjectNode> nodesToRemove = getChildren(this, new ArrayList<>());
        if(!nodesToRemove.isEmpty()){
            for (InventoryObjectNode node : nodesToRemove) 
                getTree().removeItem(node);
        }
    }
    
    private List<InventoryObjectNode> getChildren(InventoryObjectNode node, List<InventoryObjectNode> nodes){
        Collection<InventoryObjectNode> children = (Collection<InventoryObjectNode>) getTree().getChildren(node);
        if(children != null){
            for (InventoryObjectNode child : children) {
                Collection<InventoryObjectNode> subChildren = (Collection<InventoryObjectNode>) getTree().getChildren(child);
                if(subChildren != null){
                    nodes.add(child);
                    nodes.addAll(getChildren(child, nodes));
                }
                else
                    nodes.add(child);
            }
        }
        return nodes;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InventoryObjectNode) {
            return ((InventoryObjectNode)obj).getObject().equals(getObject());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode((RemoteObjectLight)getObject());
        return hash;
    }

    @Override
    public AbstractAction[] getActions() {
        return new AbstractAction[0];
    }

    @Override
    public void refresh(boolean recursive) {}
}
