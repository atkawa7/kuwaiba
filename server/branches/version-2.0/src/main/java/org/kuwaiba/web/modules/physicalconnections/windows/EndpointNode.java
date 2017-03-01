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
package org.kuwaiba.web.modules.physicalconnections.windows;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import java.util.List;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.custom.tree.DynamicTree;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class EndpointNode extends InventoryObjectNode {
    private boolean free = true;
    private RemoteObjectLight objectLink = null;
        
    public EndpointNode(RemoteObjectLight object) {
        super(object);
    }
    
    public boolean isFree() {
        return free;
    }
    
    public RemoteObjectLight getObjectLink() {
        return objectLink;
    }
    
    public void setObjectLink(RemoteObjectLight objectLink) {
        this.objectLink = objectLink;
    }
    
    public void setFree(boolean free) {
        this.free = free;
        updateDisplayName();
    }
        
    @Override
    public void setTree(DynamicTree tree) {
        super.setTree(tree);
        tree.setItemIcon(this, FontAwesome.SQUARE);
        initFreeProperty();
    }
    
    private void updateDisplayName() {
        if (!free)
            getTree().setItemCaption(this, getDisplayName() + " (" + ConnectLinksWindow.IN_USE + ")");
        else
            getTree().setItemCaption(this, getDisplayName());
    }

    @Override
    public void expand() {
        if (getTree() == null) //If the tree has not been set previously, do nothing
            return;
        collapse();
        try {
            TopComponent topComponent = getTree().getTopComponent();
            RemoteObjectLight currentObject = (RemoteObjectLight) getObject();
            
            List<RemoteObjectLight> children = topComponent.getWsBean().getObjectChildren(
                    currentObject.getClassName(), currentObject.getOid(), -1,
                    Page.getCurrent().getWebBrowser().getAddress(),
                    topComponent.getApplicationSession().getSessionId());
            
            for (RemoteObjectLight child : children) {
                EndpointNode childNode = new EndpointNode(child);
                childNode.setTree(getTree());
                getTree().setParent(childNode, this);
            }
        }catch (ServerSideException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }
    /**
     * Initialize free property: searches if the object wrapped by the node is 
     * the endpoint of a link.
     */
    private void initFreeProperty() {
        RemoteObjectLight theObject = (RemoteObjectLight) getObject();
        TopComponent topComponent = getTree().getTopComponent();
        
        boolean isGenericPort = topComponent.getWsBean().isSubclassOf(
                theObject.getClassName(), 
                "GenericPort", //NOI18N
                displayName, displayName);
        
        if (isGenericPort) {
            try {
                String [] attributesNames = new String[] {
                    "endpointA", //NOI18N
                    "endpointB"  //NOI18N
                };
                
                for (String attributeName : attributesNames) {
                    List<RemoteObjectLight> endPointLst = topComponent.getWsBean().getSpecialAttribute(
                            theObject.getClassName(),
                            theObject.getOid(),
                            attributeName,
                            Page.getCurrent().getWebBrowser().getAddress(),
                            topComponent.getApplicationSession().getSessionId());
                    
                    if (!endPointLst.isEmpty()) {
                        objectLink = endPointLst.get(0);
                        setFree(false);
                        return;
                    }
                }
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        }
    }    
}
