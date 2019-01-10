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

import org.kuwaiba.apis.web.gui.navigation.nodes.LabelNode;
import org.kuwaiba.apis.web.gui.navigation.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.navigation.nodes.AbstractNode;
import com.vaadin.data.TreeData;
import com.vaadin.ui.IconGenerator;
import com.vaadin.ui.Tree;
import java.awt.Color;
import java.util.HashMap;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLightList;

/**
 * The tree that is used in the Relationship Explorer and can be embedded anywhere
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class RelationshipsTree extends Tree<AbstractNode> {

    public RelationshipsTree(AbstractNode root, HashMap<String, RemoteObjectLightList> relationships,
            IconGenerator<AbstractNode> iconGenerator) {
        TreeData<AbstractNode> treeData = new TreeData<>();
        treeData.addRootItems(root);
        for (String relationshipName : relationships.keySet()) {
            LabelNode relationshipNameNode =  new LabelNode(relationshipName, Color.GREEN);
            treeData.addItem(root, relationshipNameNode);            
            for (RemoteObjectLight relationship : relationships.get(relationshipName).getList())
                treeData.addItem(relationshipNameNode, new InventoryObjectNode(relationship));
        }
        
        setTreeData(treeData);
        setItemIconGenerator(iconGenerator);
        expand(root);
        setSizeFull();
    }
}
