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

package org.kuwaiba.apis.web.gui.navigation.trees;

import com.vaadin.data.TreeData;
import com.vaadin.ui.IconGenerator;
import com.vaadin.ui.Tree;
import java.util.List;
import org.kuwaiba.apis.web.gui.navigation.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.navigation.nodes.InventoryObjectNode;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * An utility tree that can display a containment chain {@literal (example Europe -> Germany -> Berlin -> Building A)}. It's used in the "Hierarchy Information" option. 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ContainmentTree extends Tree<AbstractNode> {

    public ContainmentTree(List<RemoteObjectLight> containtmentChain, IconGenerator<AbstractNode> iconGenerator) {
        TreeData<AbstractNode> treeData = new TreeData<>();
        AbstractNode currentParentNode = null;
        
        for (RemoteObjectLight containmentItem : containtmentChain) {
            InventoryObjectNode currentNode = new InventoryObjectNode(containmentItem);
            treeData.addItem(currentParentNode, currentNode);
            currentParentNode = currentNode;
            expand(currentNode);
        }
        setTreeData(treeData);
        setItemIconGenerator(iconGenerator);
        setSizeFull();
    }
    
    public void expandAll() {
        getTreeData().getRootItems().stream().forEach((aRoot) -> { expandRecursively(aRoot); });
    }
    
    /**
     * Expands a node recursively till it reaches the leaves
     * @param node The node to be expanded
     */
    public void expandRecursively (AbstractNode node) {
        if (!getTreeData().getChildren(node).isEmpty())  {
            expand(node);
            getTreeData().getChildren(node).stream().forEach((aChild) -> { expandRecursively(aChild); });
        }
    }
    
}
