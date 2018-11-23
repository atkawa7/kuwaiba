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
package org.kuwaiba.apis.web.gui.navigation.trees;

import org.kuwaiba.apis.web.gui.navigation.nodes.ChildrenProvider;
import org.kuwaiba.apis.web.gui.navigation.nodes.AbstractNode;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.server.SerializableFunction;
import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.ui.IconGenerator;
import com.vaadin.ui.Tree;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.components.grid.TreeGridDragSource;
import java.util.List;
import java.util.stream.Collectors;
import org.kuwaiba.apis.web.gui.navigation.InventoryObjectTreeData;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A tree that extends the features of the default one and makes use of the Nodes API
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
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
        
        //Enable the tree as a drag source
        TreeGridDragSource<AbstractNode> dragSource = new TreeGridDragSource<>((TreeGrid<AbstractNode>)this.getCompositionRoot());
        dragSource.setEffectAllowed(EffectAllowed.MOVE);
        dragSource.setDragDataGenerator(RemoteObjectLight.DATA_TYPE, new SerializableFunction<AbstractNode, String>() {
            @Override
            public String apply(AbstractNode t) { //Now we serialize the object to be transferred
                return ((RemoteObjectLight)t.getObject()).getId() + "~a~" + ((RemoteObjectLight)t.getObject()).getClassName() + "~a~" + ((RemoteObjectLight)t.getObject()).getName();
            }
        });
        
        setDataProvider(new TreeDataProvider(treeData));
        setSizeFull();
        setItemIconGenerator(iconGenerator);
    }
    
    /**
     *  Constructor for trees with only one root node
     * @param roots The root nodes of the tree
     * @param childrenProvider The object that will provide the children of an expanded node
     * @param iconGenerator To generate the icons
     */
    public SimpleTree(ChildrenProvider childrenProvider, IconGenerator<AbstractNode> iconGenerator, 
            List<AbstractNode> roots) {
        InventoryObjectTreeData treeData = new InventoryObjectTreeData(childrenProvider);
        treeData.addRootItems(roots);
        
        //Enable the tree as a drag source
        TreeGridDragSource<AbstractNode> dragSource = new TreeGridDragSource<>((TreeGrid<AbstractNode>)this.getCompositionRoot());
        dragSource.setEffectAllowed(EffectAllowed.MOVE);
        dragSource.setDragDataGenerator(RemoteObjectLight.DATA_TYPE, new SerializableFunction<AbstractNode, String>() {
            @Override
            public String apply(AbstractNode t) { //Now we serialize the object to be transferred
                return ((RemoteObjectLight)t.getObject()).getId() + "~a~" + ((RemoteObjectLight)t.getObject()).getClassName() + "~a~" + ((RemoteObjectLight)t.getObject()).getName();
            }
        });
        
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
    
    /**
     * Represents as a string the set of selected objects in the tree so they can be transferred in a drag&drop operation
     * @return The serializes representation of the selected objects
     */
    private String serializeSelectedObjects() {
        return getSelectedItems().stream().map(n -> 
                ((RemoteObjectLight)n.getObject()).getId() + "~a~" + ((RemoteObjectLight)n.getObject()).getClassName()+ "~a~" + ((RemoteObjectLight)n.getObject()).getName()
        ).collect(Collectors.joining("~o~"));
    }
}