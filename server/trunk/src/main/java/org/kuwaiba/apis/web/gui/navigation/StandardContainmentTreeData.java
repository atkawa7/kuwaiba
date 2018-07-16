/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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

import com.vaadin.data.TreeData;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A TreeData implementation that manages 
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */

public class StandardContainmentTreeData extends TreeData<AbstractNode> {
    /**
     * A custom data provider that returns the children in the standard containment hierarchy of a given inventory object
     */
    private ChildrenProvider<RemoteObjectLight, RemoteObjectLight> childrenProvider;

    public StandardContainmentTreeData(ChildrenProvider childrenProvider) {
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
}

