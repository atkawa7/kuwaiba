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
package org.kuwaiba.web.modules.navtree;

import com.google.common.eventbus.EventBus;
import com.vaadin.server.Page;
import com.vaadin.ui.VerticalSplitPanel;
import java.util.List;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectRootNode;
import org.kuwaiba.apis.web.gui.nodes.properties.ObjectNodeProperty;
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
        /**
         * The property sheet (for now)
         */
        private ObjectNodeProperty propertySheet;
        
        public NavigationTreeComponent(EventBus eventBus, WebserviceBeanLocal wsBean, RemoteSession session) {
            super(wsBean, eventBus, session);
            
            try {
                List<RemoteObjectLight> rootChildren = wsBean.getObjectChildren("DummyRoot", -1, 0, Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                InventoryObjectRootNode rootNode = new InventoryObjectRootNode("Navigation Tree Root", rootChildren);
                
                tree = new DynamicTree(rootNode, this);
                rootNode.setTree(tree);
                
                propertySheet = new ObjectNodeProperty(eventBus);

                VerticalSplitPanel pnlSplitExplorer = new VerticalSplitPanel(tree, propertySheet);
                pnlSplitExplorer.setSplitPosition(70);

                setCompositionRoot(pnlSplitExplorer);
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
            }
            
            
            this.setSizeFull();
        }
        
        @Override
        public void registerComponents() {
            tree.register();
            propertySheet.register();
        }
        
        @Override
        public void unregisterComponents() {
            tree.unregister();
            propertySheet.unregister();
        }
    }