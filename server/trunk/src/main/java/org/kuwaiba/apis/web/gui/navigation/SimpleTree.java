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
package org.kuwaiba.apis.web.gui.navigation;

import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.ui.IconGenerator;
import com.vaadin.ui.Tree;
import java.util.List;

/**
 * A tree that extends the features of the default one and makes use of the Nodes API
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class SimpleTree extends Tree<AbstractNode> {

    /**
     *  Constructor for trees with only one root node
     * @param roots The root nodes of the tree
     * @param childrenProvider The object that will provide the children of an expanded node
     * @param iconGenerator To generate the icons
     */
    public SimpleTree(ChildrenProvider childrenProvider, IconGenerator<AbstractNode> iconGenerator, 
            AbstractNode... roots) {
        InventoryObjectTreeData treeData = new InventoryObjectTreeData(childrenProvider);
        treeData.addRootItems(roots);
        
        setDataProvider(new TreeDataProvider(treeData));
        setSizeFull();
        setItemIconGenerator(iconGenerator);
    }
    
    /**
     * Resets the tree to the roots provided 
     * @param newRoots The roots to replace the current one
     */
    public void resetTo(List<AbstractNode> newRoots) {
        this.getTreeData().clear();
        this.getTreeData().addRootItems(newRoots);
        this.setTreeData(getTreeData());
    }
    
    /**
     * Resets the tree to the roots provided 
     * @param newRoots The roots to replace the current one
     */
    public void resetTo(AbstractNode... newRoots) {
        this.getTreeData().clear();
        this.getTreeData().addRootItems(newRoots);
        this.setTreeData(getTreeData());
    }
}