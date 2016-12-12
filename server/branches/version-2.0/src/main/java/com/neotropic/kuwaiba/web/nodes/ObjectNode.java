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
package com.neotropic.kuwaiba.web.nodes;

import com.neotropic.kuwaiba.web.nodes.actions.AbstractAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;

/**
 * Represents a node of a tree
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class ObjectNode extends AbstractNode<RemoteBusinessObjectLight>{
  
    public ObjectNode(RemoteBusinessObjectLight object, Tree tree) {
        super(object, tree);
        tree.setItemIcon(this, FontAwesome.SQUARE);
    }
    
    @Override
    public String getDisplayName() {
        return displayName == null ? getObject().toString() : displayName;
    }
   
    public Long getId(){
        return ((RemoteBusinessObjectLight)getObject()).getId();
    }
    
    public String getClassName(){
        return ((RemoteBusinessObjectLight)getObject()).getClassName();
    }
    
    public void expand() {
        try {
            BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
            
            List<RemoteBusinessObjectLight> children = bem.getObjectChildren(
                    ((RemoteBusinessObjectLight)getObject()).getClassName(), 
                    ((RemoteBusinessObjectLight)getObject()).getId(), -1
            );
            
            for (RemoteBusinessObjectLight child : children) {
                ObjectNode childNode = new ObjectNode(child, getTree());
                getTree().addItem(childNode);
//                getTree().setItemStyleGenerator(new Tree.ItemStyleGenerator(){
//                    @Override
//                    public String getStyle(Tree source, Object itemId) {
//                        if(((RemoteBusinessObjectLight)getObject()).getClassName().equals("City"))
//                            return "bold";
//                        else
//                            return"clear";
//                    }
//                });
                getTree().setParent(childNode, this);
            }
        }catch (InventoryException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }
    
    public void collapse() {
        List<ObjectNode> nodesToRemove = getChildren(this, new ArrayList<>());
        if(!nodesToRemove.isEmpty()){
            for (ObjectNode node : nodesToRemove) 
                getTree().removeItem(node);
        }
    }
    
    private List<ObjectNode> getChildren(ObjectNode node, List<ObjectNode> nodes){
        Collection<ObjectNode> children = (Collection<ObjectNode>) getTree().getChildren(node);
        if(children != null){
            for (ObjectNode child : children) {
                Collection<ObjectNode> subChildren = (Collection<ObjectNode>) getTree().getChildren(child);
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
        if (obj instanceof ObjectNode) {
            return ((ObjectNode)obj).getObject().equals((RemoteBusinessObjectLight)getObject());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode((RemoteBusinessObjectLight)getObject());
        return hash;
    }

    @Override
    public AbstractAction[] getActions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void refresh(boolean recursive) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
