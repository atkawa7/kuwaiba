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
package org.kuwaiba.apis.web.gui.nodes.classmetadatanodes;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.nodes.AbstractNode;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.metadata.ClassInfoLight;
import org.kuwaiba.web.custom.tree.DynamicTree;

/**
 * A node wrapping a ClassInfoLight
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ClassMetadataNode extends AbstractNode<ClassInfoLight> {
    
    public ClassMetadataNode(ClassInfoLight object) {
        super(object);
    }
    
    @Override
    public void setTree(DynamicTree tree) {
        super.setTree(tree);
        tree.setItemIcon(this, FontAwesome.SQUARE);
    }

    @Override
    public void expand() {
        if (getTree() == null)
            return;
        collapse();
        try {
            ClassInfoLight currentObject = (ClassInfoLight) getObject();
            
            TopComponent topComponent = getTree().getTopComponent();
            
            List<ClassInfoLight> children = topComponent.getWsBean()
                    .getPossibleChildrenNoRecursive(currentObject.getClassName(), 
                            Page.getCurrent().getWebBrowser().getAddress(), 
                            topComponent.getApplicationSession().getSessionId());
            
            for (ClassInfoLight child : children) {
                ClassMetadataNode childNode = new ClassMetadataNode(child);
                childNode.setTree(getTree());
                getTree().setParent(childNode, this);
            }
        }
        catch(ServerSideException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }
    
    @Override
    public AbstractAction[] getActions() {
        return new AbstractAction[0];
    }

    @Override
    public void refresh(boolean recursive) {}
    
    
}
