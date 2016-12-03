/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.prototypes.nodes;

import com.neotropic.kuwaiba.prototypes.actions.AbstractAction;
import com.vaadin.ui.Tree;
import java.util.Collection;
import java.util.Objects;

/**
 * A node that represents a business domain object from the model.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 * @param <T> The type of the business object
 */
public abstract class AbstractNode<T> {
    /**
     * Business object behind this node (model)
     */
    protected T object;
    /**
     * Node's displayName. If null, the toString method of the business object will be used
     */
    private String displayName;
    /**
     * Reference to the tree containing this node
     */
    protected Tree tree;

    public AbstractNode(T object, Tree tree) {
        this.object = object;
        this.tree = tree;
    }
    
    public AbstractNode(Tree tree) {
        this.tree = tree;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Object getObject() {
        return object;
    }

    public Tree getTree() {
        return tree;
    }
    
    /**
     * Deletes the node and its children recursively
     */
    public void delete() {
        Collection<?> children = tree.getChildren(this);
        
        if (children != null) {
            synchronized (children) {
                for (Object child : children) //A lambda expression is not thread-safe and will cause a ConcurrentModificationException, even if synchronized
                    ((AbstractNode)child).delete();
            }
        }
        
        tree.removeItem(this);
    }
    
    /**
     * Actions associated to this node
     * @return An array of actions
     */
    public abstract AbstractAction[] getActions();
    
    /**
     * What to do when commanded to refresh the node.
     * @param recursive Refresh the children nodes.
     */
    public abstract void refresh(boolean recursive);
    
    /**
     * Adds a child node
     * @param node 
     */
    public void add(AbstractNode node) {
        tree.addItem(node);
        tree.setParent(node, this);
    }
    
    /**
     * Removes a node
     * @param node 
     */
    public void remove(AbstractNode node) {
        tree.removeItem(node);
    }
    
    @Override
    public String toString() {
        return displayName == null ? object.toString() : displayName;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractNode) 
            return object.equals(((AbstractNode)obj).getObject());
        else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.object);
        return hash;
    }
}
