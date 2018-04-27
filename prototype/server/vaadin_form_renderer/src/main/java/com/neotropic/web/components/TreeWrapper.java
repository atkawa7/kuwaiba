/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package com.neotropic.web.components;

import com.neotropic.forms.KuwaibaClient;
import org.inventory.communications.wsclient.RemoteObjectLight;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.ExpandEvent;
import com.vaadin.event.ExpandEvent.ExpandListener;
import com.vaadin.ui.Tree;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class TreeWrapper {
    private final Tree<RemoteObjectLight> tree;    
    private final TreeData<RemoteObjectLight> treeData;
    private final TreeDataProvider<RemoteObjectLight> treeDataProvider;
    
    public TreeWrapper() {
        tree = new Tree();
        tree.setSizeUndefined();
        
        treeData = new TreeData();
        treeDataProvider = new TreeDataProvider(treeData);
        
        RemoteObjectLight root = new RemoteObjectLight();
        root.setClassName("DummyRoot"); //NOI18N
        root.setName("DummyRoot"); //NOI18N
        root.setOid(-1);
        
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
        List<RemoteObjectLight> children = KuwaibaClient.getInstance().getObjectChildren(parent.getClassName(), parent.getOid());
        
        if (children == null)
            return;
        
        for (RemoteObjectLight child : children) {
            
            if (!treeData.contains(child))
                treeData.addItem(parent, child);
            
            List<RemoteObjectLight> subchildren = KuwaibaClient.getInstance().getObjectChildren(child.getClassName(), child.getOid());
            
            if (subchildren == null)
                continue;
            
            for (RemoteObjectLight subchild : subchildren) {
                if (!treeData.contains(subchild))
                    treeData.addItem(child, subchild);
            }
        }
    }
}
