/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.custom.tree;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import java.util.List;
import java.util.Objects;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;

/**
 * Represents a node of a tree
 * @author duckman
 */
public class Node {
    private RemoteBusinessObjectLight businessObject;
    private Tree tree;
    private String displayName;
    
    public Node(RemoteBusinessObjectLight businessObject, Tree tree) {
        this.businessObject = businessObject;
        this.tree = tree;
    }
    
    public String getName() {
        return businessObject.getName();
    }
    
    public String getDisplayName() {
        return displayName == null ? businessObject.toString() : displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public RemoteBusinessObjectLight getObject() {
        return businessObject;
    }
    
    private void setObject(RemoteBusinessObjectLight businessObject) {
        this.businessObject = businessObject;
    }
    
    public void expand() {
        try {
            BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
            List<RemoteBusinessObjectLight> children = bem.getObjectChildren(businessObject.getClassName(), businessObject.getId(), -1);
            
            for (RemoteBusinessObjectLight child : children) {
                Node aNewNode = new Node(child, tree);
                tree.addItem(aNewNode);
                tree.setParent(aNewNode, this);
            }
            
        }catch (InventoryException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }
    
    public void collapse() {
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            return ((Node)obj).getObject().equals(businessObject);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.businessObject);
        return hash;
    }
    
}
