/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
package org.inventory.navigation.applicationnodes;

import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectChildren;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.SpecialNode;
import org.openide.util.Lookup;

/**
 * Children to SpecialNodes
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SpecialChildren extends ObjectChildren {  

    @Override
    public void addNotify(){
        assert getNode() instanceof ObjectNode : "This node is not instance of ObjectNode";
        collapsed = false;
        LocalObjectLight parentObject = ((ObjectNode)getNode()).getObject();

        LocalObjectLight[] specialChildren = CommunicationsStub.getInstance().
                getObjectSpecialChildren(parentObject.getClassName(), parentObject.getOid());
        if (specialChildren == null){
            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            nu.showSimplePopup("Error", NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
            return;
        }
            
        for (LocalObjectLight lol : specialChildren)
            add(new SpecialNode[]{new SpecialNode(lol)});

    }
}
