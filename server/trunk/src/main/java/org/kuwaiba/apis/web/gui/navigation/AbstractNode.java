/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.web.gui.navigation;

import java.util.ArrayList;
import com.vaadin.server.Page;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.web.gui.nodes.properties.NodeProperty;
import org.kuwaiba.apis.web.gui.nodes.properties.Sheet;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.kuwaiba.services.persistence.util.Util;
import org.kuwaiba.apis.web.gui.navigation.DynamicTree;
import org.kuwaiba.apis.web.gui.nodes.ObjectNodePropertyChangeValueListener;
import org.kuwaiba.apis.web.gui.nodes.PropertySheet;

/**
 * A node that represents a business domain object from the model.
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
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
    protected String displayName;

    public AbstractNode() {}
    
    public AbstractNode(T object) {
        this.object = object;
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
       
    /**
     * What to do when expanding the node is requested. 
     * @return The children 
     */
    public abstract List<T> expand();
    /**
     * What to do when collapsing the node is requested. Always check if the tree has been set!
     */
    public abstract void collapse();
    
    /**
     * Deletes the node and its children
     */
    public void delete() {

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
    }
    
    /**
     * Removes a node
     * @param node 
     */
    public void remove(AbstractNode node) {
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
    
    public PropertySheet createPropertySheet(){
        return null;
    }
}
