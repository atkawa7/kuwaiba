/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.apis.forms.components.impl;

import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.ExpandEvent;
import com.vaadin.event.ExpandEvent.ExpandListener;
import com.vaadin.ui.Tree;
import java.util.List;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class TreeWrapper {
    private final Tree<RemoteObjectLight> tree;    
    private final TreeData<RemoteObjectLight> treeData;
    private final TreeDataProvider<RemoteObjectLight> treeDataProvider;
    
    private final HierarchyProvider hierarchyProvider;
    
    public TreeWrapper() {
        hierarchyProvider = ObjectHierarchyProvider.getInstance();
        
        tree = new Tree();
        tree.setSizeUndefined();
        
        treeData = new TreeData();
        treeDataProvider = new TreeDataProvider(treeData);
        
        RemoteObjectLight root = new RemoteObjectLight(Constants.DUMMY_ROOT, "-1", Constants.DUMMY_ROOT);
                
        tree.setDataProvider(treeDataProvider);
                
        treeData.addItem(null, root);
        addChildren(root);
        
        tree.addExpandListener(new ExpandListener<RemoteObjectLight> () {
            
            @Override
            public void itemExpand(ExpandEvent<RemoteObjectLight> event) {
                RemoteObjectLight item = event.getExpandedItem();
                if (item != null)
                    addChildren(item);
            }
        });
    }
    
    public Tree getTree() {
        return tree;
    }
    
    public void addChildren(RemoteObjectLight parent) {
        List<RemoteObjectLight> children = hierarchyProvider.getChildren(parent);
        
        if (children == null)
            return;
        
        for (RemoteObjectLight child : children) {
            
            if (!treeData.contains(child))
                treeData.addItem(parent, child);
            
            List<RemoteObjectLight> subchildren = hierarchyProvider.getChildren(child);
            
            if (subchildren == null)
                continue;
            
            for (RemoteObjectLight subchild : subchildren) {
                if (!treeData.contains(subchild))
                    treeData.addItem(child, subchild);
            }
        }
    }
}
