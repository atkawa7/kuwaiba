/**
 * Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.navigation.applicationnodes.objectnodes;

import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.Node;

/**
 * Represents the children for the navigation tree
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectChildren extends AbstractChildren {
    
    public ObjectChildren() {
        setKeys(Collections.EMPTY_SET);
    }
    
    public ObjectChildren(LocalObjectLight[] lols){
        setKeys(lols);
    }

    /**
     * Creates children nodes on demand
     */
    @Override
    public void addNotify() {
        CommunicationsStub com = CommunicationsStub.getInstance();
        List <LocalObjectLight> children;
        //The tree root is not an AbstractNode, but a RootObjectNode
        if (this.getNode() instanceof RootObjectNode)
            children = com.getObjectChildren(-1, -1);
        else {
            LocalObjectLight node = ((ObjectNode)this.getNode()).getObject();
        
            children = com.getObjectChildren(node.getOid(),
                    com.getMetaForClass(node.getClassName(), false).getOid());
        }
        if (children == null) {
            setKeys(Collections.EMPTY_LIST);
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, "An error has occurred retrieving this object's children: "+com.getError());
        }
        else {
            Collections.sort(children);
            setKeys(children);
        }
    }
    
    @Override
    protected Node[] createNodes(LocalObjectLight key) {
        return new Node[] { new ObjectNode(key)};
    }
}