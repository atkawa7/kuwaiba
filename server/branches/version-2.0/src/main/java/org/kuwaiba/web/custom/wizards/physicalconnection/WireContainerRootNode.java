/*
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
package org.kuwaiba.web.custom.wizards.physicalconnection;

import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.nodes.AbstractRootNode;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class WireContainerRootNode extends AbstractRootNode {
    private final List<RemoteObjectLight> existingWireContainersList;
    
    public WireContainerRootNode(String caption, List<RemoteObjectLight> existingWireContainersList) {
        super(caption);
        this.existingWireContainersList = existingWireContainersList;
    }
    
    @Override
    public void expand() {
        
        if (getTree() == null) //If the tree has not been set previously, do nothing
            return;
        
        for (RemoteObjectLight child : existingWireContainersList) {
            InventoryObjectNode objectNode = new InventoryObjectNode(child);
            objectNode.setTree(getTree());
            getTree().addItem(objectNode);
            getTree().setParent(objectNode, this);
        }
    }

    @Override
    public void collapse() {}

    @Override
    public AbstractAction[] getActions() {
        return new AbstractAction[0];
    }

    @Override
    public void refresh(boolean recursive) {}
    
}
