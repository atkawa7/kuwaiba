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

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.apis.web.gui.navigation.DynamicTree;

/**
 * The main component of the Navigation Tree module.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@CDIView("navtree")
class NavigationTreeComponent extends AbstractTopComponent {
    /**
     * View identifier
     */
    public static String VIEW_NAME = "navtree";
    /**
     * Combo box containing the current classes from ConfigurationItem beginning 
     */
    private ComboBox<RemoteClassMetadataLight> cmbClasses;
    /**
     * Text field to filter the services
     */
    private TextField txtFilter;
    /**
     * Layout for all the graphic components on the left side
     */
    private VerticalLayout lytLeftPanel;
    /**
     * The backend bean
     */
    @Inject
    private WebserviceBean wsBean;
    /**
     * The tree
     */
    private DynamicTree tree;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
//                try {
//                List<RemoteObjectLight> rootChildren = wsBean.getObjectChildren("DummyRoot", -1, 0, Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
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

//        } catch (ServerSideException ex) {
//            Notifications.showError(ex.getMessage());
//        }
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
