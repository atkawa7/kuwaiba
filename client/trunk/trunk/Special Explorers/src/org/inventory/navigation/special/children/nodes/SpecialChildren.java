/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.special.children.nodes;

import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.nodes.Node;

/**
 * Children to SpecialNodes
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SpecialChildren extends AbstractChildren {  

    @Override
    public void addNotify(){
        LocalObjectLight parentObject = ((ObjectNode)getNode()).getObject();

        List<LocalObjectLight> specialChildren = CommunicationsStub.getInstance().
                getObjectSpecialChildren(parentObject.getClassName(), parentObject.getOid());
       
        if (specialChildren == null){
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            setKeys(Collections.EMPTY_SET);
        } else {
            Collections.sort(specialChildren);
            setKeys(specialChildren);
        }
    }

    @Override
    protected Node[] createNodes(LocalObjectLight key) {
        return new SpecialObjectNode[] { new SpecialObjectNode(key) };
    }
}
