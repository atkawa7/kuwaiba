/**
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.web.gui.nodes.containment;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Notification;
import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.navigation.AbstractNode;
import org.kuwaiba.apis.web.gui.nodes.properties.Sheet;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.apis.web.gui.navigation.DynamicTree;
import org.kuwaiba.web.modules.containment.actions.ActionsFactory;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ContainmentNode  {
//    /**
//     * The id.
//     * For this node is assigned automatically by the tree when add this node
//     */
//    private Object id;
//    
//    public ContainmentNode(RemoteClassMetadataLight object) {
//        super(object);
//    }
//    
//    @Override
//    public void setTree(DynamicTree tree) {
//        this.tree = tree;
////        id = this.tree.addItem(this);
////        this.tree.setItemIcon(this, FontAwesome.SQUARE);
//    }
//    
//    private Object getId() {
//        return id;
//    }
//    
//    @Override
//    public void expand() {
////        if (getTree() == null)
////            return;
////        
////        try {
////            collapse();
////            if (getTree().areChildrenAllowed(this)) {
////                ClassInfoLight currentObject = (ClassInfoLight) getObject();
////
////                TopComponent topComponent = getTree().getTopComponent();
////
////                List<ClassInfoLight> children = topComponent.getWsBean()
////                        .getPossibleChildrenNoRecursive(currentObject.getClassName(), 
////                                Page.getCurrent().getWebBrowser().getAddress(), 
////                                topComponent.getApplicationSession().getSessionId());
////
////                for (ClassInfoLight child : children) {
////                    ContainmentNode childNode = new ContainmentNode(child);
////                    childNode.setTree(getTree());
////                    
////                    childNode.getTree().setChildrenAllowed(childNode, false);
////                    childNode.getTree().setParent(childNode, this);
////                    childNode.getTree().setItemIcon(childNode, 
////                            new ThemeResource("img/mod_containtment_icon_flag_black.png"));
////                }
////            }
////        }
////        catch(ServerSideException ex) {
////            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
////        }
//    }
//    
//    @Override
//    public AbstractAction[] getActions() {
////        if (!getTree().areChildrenAllowed(this))
////            return new AbstractAction[] {
////                ActionsFactory.createRemoveContainmentNodeAction() };
////        else
////            return new AbstractAction[0];
//return null;
//    }
//
//    @Override
//    public void refresh(boolean recursive) {}
//    
//    @Override
//    public boolean equals(Object obj){
//        if (obj == null)
//            return false;
//        if (!(obj instanceof ContainmentNode))
//            return false;
//        if (((ContainmentNode)obj).getId() == getId())
//            return true;
//        return false;
//    }
//    
//    //@Override
//    public Sheet createPropertySheet(){
//        return null;
//    }
}
