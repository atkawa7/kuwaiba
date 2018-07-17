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
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A tree that extends the features of the default one and makes use of the Nodes API
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class DynamicTree extends Tree<AbstractNode> {

    /**
     *  Default constructor
     * @param root The root of the hierarchy
     * @param childrenProvider The object that will provide the children of an expanded node
     * @param iconGenerator To generate the icons
     */
    public DynamicTree(RemoteObjectLight root, ChildrenProvider childrenProvider, IconGenerator<AbstractNode> iconGenerator) {
        StandardContainmentTreeData treeData = new StandardContainmentTreeData(childrenProvider);
        treeData.addRootItems(new AbstractNode<RemoteObjectLight>(root) {
            @Override
            public AbstractAction[] getActions() { return new AbstractAction[0]; }
            
            @Override
            public void refresh(boolean recursive) { }
        });
        
        setDataProvider(new TreeDataProvider(treeData));
        setSizeFull();
        setItemIconGenerator(iconGenerator);
    }   
}