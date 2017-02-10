/**
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
package org.kuwaiba.web.modules.lists;

import com.google.common.eventbus.EventBus;
import com.vaadin.event.Action;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalSplitPanel;
import java.util.ArrayList;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.apis.web.gui.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.nodes.listmanagernodes.ListTypeRootNode;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.metadata.ClassInfoLight;
import org.kuwaiba.web.custom.tree.DynamicTree;

/**
 * The main component of the List Manager module.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ListManagerComponent extends AbstractTopComponent {
    private DynamicTree listTypesTree;
    
    public ListManagerComponent(EventBus eventBus, WebserviceBeanLocal wsBean, RemoteSession session) {
        super(wsBean, eventBus, session);
        
        try {
            ClassInfoLight [] listTypes = wsBean.getInstanceableListTypes(
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    session.getSessionId());
            
            ArrayList<ClassInfoLight> rootChildren = new ArrayList();
            for (ClassInfoLight listType : listTypes)
                rootChildren.add(listType);
            
            ListTypeRootNode rootNode = new ListTypeRootNode("Available List Types", rootChildren);
            
            VerticalSplitPanel pnlSplitExplorer = new VerticalSplitPanel();
            pnlSplitExplorer.setSplitPosition(100);
            
            listTypesTree = new DynamicTree(rootNode, this);
            listTypesTree.setDragMode(Tree.TreeDragMode.NONE);
            rootNode.setTree(listTypesTree);
            
            listTypesTree.addActionHandler(new Action.Handler() {

                @Override
                public Action[] getActions(Object target, Object sender) {
                    if (target instanceof AbstractNode)
                        return ((AbstractNode)target).getActions();
                    return null;
                }

                @Override
                public void handleAction(Action action, Object sender, Object target) {
                    ((AbstractAction) action).actionPerformed(sender, target);
                }
            });
                        
            pnlSplitExplorer.addComponent(listTypesTree);
            setCompositionRoot(pnlSplitExplorer);
            
            rootNode.expand();
        } catch (ServerSideException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
        
        this.setSizeFull();
    }

    @Override
    public void registerComponents() {
        listTypesTree.register();
    }

    @Override
    public void unregisterComponents() {
        listTypesTree.unregister();
    }
    
}
