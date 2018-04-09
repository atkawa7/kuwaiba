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
package org.kuwaiba.apis.web.gui.nodes.listmanagernodes;

import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Notification;
import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.nodes.AbstractNode;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.ClassInfoLight;
import org.kuwaiba.web.custom.tree.DynamicTree;
import org.kuwaiba.web.modules.lists.actions.ActionsFactory;

/**
 * Represents a list type node of a tree
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ListTypeNode extends AbstractNode<ClassInfoLight> {
    
    public ListTypeNode(ClassInfoLight object) {
        super(object);
    }
    
    @Override
    public void setTree(DynamicTree tree) {
        super.setTree(tree);
        //tree.setItemIcon(this, new ThemeResource("img/mod_list_icon_list_type.png"));
    }
    
    @Override
    public void expand() {
        if (getTree() == null) //If the tree has not been set previously, do nothing
            return;
        
        try {
            collapse();
            TopComponent topComponent = getTree().getTopComponent();
            ClassInfoLight currentObject = (ClassInfoLight)getObject();
            
            List<RemoteObjectLight> children = topComponent.getWsBean()
                    .getListTypeItems(currentObject.getClassName(), 
                            Page.getCurrent().getWebBrowser().getAddress(), 
                            topComponent.getApplicationSession().getSessionId());
                        
            for (RemoteObjectLight child : children) {
                ListTypeChildNode childNode = new ListTypeChildNode(child);
                childNode.setTree(getTree());
                //getTree().setParent(childNode, this);
            }
        } catch (ServerSideException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }
        
    @Override
    public AbstractAction[] getActions() {
        return new AbstractAction[] { ActionsFactory.createCreateListTypeChildAction() };
    }

    @Override
    public void refresh(boolean recursive) {}

}
