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
package org.kuwaiba.apis.web.gui.navigation.nodes;

import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import java.util.Objects;

/**
 * A node that represents a business domain object from the model.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
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

    public AbstractNode(T object) {
        this.object = object;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Retrieves the business object behind the node
     * @return The business object, whose type is specified in the template (T)
     */
    public T getObject() {
        return object;
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
     
    @Override
    public String toString() {
        return displayName == null ? object.toString() : displayName;
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractNode ? (getObject().equals(((AbstractNode)obj).getObject())): false;
        
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.object);
        return hash;
    }
}
