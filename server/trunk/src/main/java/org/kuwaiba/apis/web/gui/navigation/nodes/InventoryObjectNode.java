/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.web.gui.navigation.nodes;

import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A node representing an inventory object. This node is typically used in navigation trees or special explorers
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class InventoryObjectNode extends AbstractNode<RemoteObjectLight>{

    public InventoryObjectNode(RemoteObjectLight object) {
        super(object);
    }

    @Override
    public AbstractAction[] getActions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void refresh(boolean recursive) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static List<AbstractNode> asNodeList(List<RemoteObjectLight> inventoryObjects) {
        List<AbstractNode> res = new ArrayList<>();
        
        inventoryObjects.stream().forEach(inventoryObject -> { 
                res.add(new InventoryObjectNode(inventoryObject));
        });
        
        return res;
    }
}
