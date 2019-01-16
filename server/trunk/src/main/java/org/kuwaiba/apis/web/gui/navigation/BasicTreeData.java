/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

import org.kuwaiba.apis.web.gui.navigation.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.navigation.nodes.ChildrenProvider;
import org.kuwaiba.apis.web.gui.navigation.nodes.AbstractNode;
import com.vaadin.data.TreeData;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A TreeData implementation for trees that display inventory objects (e.g. the navigation tree or the special children explorer)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */

public class BasicTreeData extends TreeData<AbstractNode> {
    /**
     * A custom data provider that returns the children in the standard containment hierarchy of a given inventory object
     */
    private ChildrenProvider<RemoteObjectLight, RemoteObjectLight> childrenProvider;

    public BasicTreeData(ChildrenProvider childrenProvider) {
        super();
        this.childrenProvider = childrenProvider;
    }

    @Override
    public List<AbstractNode> getChildren(AbstractNode expandedItem) {
        if (expandedItem == null) // The root nodes
            return super.getChildren(expandedItem);
        else {
            List<RemoteObjectLight> children = childrenProvider.getChildren((RemoteObjectLight)expandedItem.getObject());
            List<AbstractNode> newNodes = new ArrayList<>();
            for (RemoteObjectLight child : children) {
                AbstractNode newNode = new InventoryObjectNode(child);
                if (!contains(newNode))
                    addItem(expandedItem, newNode);
                    
                newNodes.add(newNode);
            }

            return newNodes;
        }
    }

    /**
     * It was necessary to override this method, because it is possible that the same tree has the same item many times 
     * (for example, when searching an object using the search box in the navigation tree, the result might be an object AND a child of that object. 
     * If the user expands the parent, eventually will get to the child that was also result of the former search. When resetting the tree, 
     * the node will be queued to be deleted twice, hence rasing an exception)
     * @param item The item to be removed
     * @return The modified tree data
     */
    @Override
    public TreeData<AbstractNode> removeItem(AbstractNode item) {
        if (contains(item))
            return super.removeItem(item); //To change body of generated methods, choose Tools | Templates.
        else // Do nothing
            return this;
    }
    
    
}

