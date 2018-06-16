/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.web.modules.navtree;

import com.google.common.eventbus.EventBus;
import com.vaadin.event.Action;
import com.vaadin.server.Page;
import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.apis.web.gui.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectRootNode;
import org.kuwaiba.apis.web.gui.nodes.properties.PropertySheetComponent;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.custom.tree.DynamicTree;

/**
 * The main component of the Navigation Tree module.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class NavigationTreeComponent extends AbstractTopComponent {
        /**
         * The tree
         */
        private DynamicTree tree;
       
        public NavigationTreeComponent(EventBus eventBus, WebserviceBeanLocal wsBean, RemoteSession session){
            super(wsBean, eventBus, session);
            
            try {
                List<RemoteObjectLight> rootChildren = wsBean.getObjectChildren("DummyRoot", -1, 0, Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
//                InventoryObjectRootNode rootNode = new InventoryObjectRootNode("Navigation Tree Root", rootChildren);
//                
//                tree = new DynamicTree(rootNode, this);
//                rootNode.setTree(tree);
//                
////                tree.addActionHandler(new Action.Handler() {
////
////                    @Override
////                    public Action[] getActions(Object target, Object sender) {
////                        if (target instanceof AbstractNode)
////                            return ((AbstractNode)target).getActions();
////                        return null;
////                    }
////
////                    @Override
////                    public void handleAction(Action action, Object sender, Object target) {
////                        ((AbstractAction) action).actionPerformed(sender, target);
////                    }
////                });
//                
//                

        } catch (ServerSideException ex) {
            NotificationsUtil.showError(ex.getMessage());
        }
    }
        
    @Override
    public void registerComponents() {
        //tree.register();
    }

    @Override
    public void unregisterComponents() {
        //tree.unregister();
    }
}
