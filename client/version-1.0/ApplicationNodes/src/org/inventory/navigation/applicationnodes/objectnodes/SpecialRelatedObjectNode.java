/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
 *  under the License.
 */
package org.inventory.navigation.applicationnodes.objectnodes;

import java.util.Collections;
import java.util.HashMap;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;


/**
 * A node in the Special Relationships explorer
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SpecialRelatedObjectNode extends ObjectNode {

    public SpecialRelatedObjectNode(LocalObjectLight lol) {
        super(lol);
        setChildren(new SpecialRelationshipChildren());
    }
    
    public static class SpecialRelationshipChildren extends Children.Keys<String> {
         private HashMap<String, LocalObjectLight[]> children;

         @Override
         public void addNotify() {
            LocalObjectLight object = ((SpecialRelatedObjectNode)getNode()).getLookup().lookup(LocalObjectLight.class);
            children = CommunicationsStub.getInstance().getSpecialAttributes(object.getClassName(), object.getOid());
            if (children == null) {
                 NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                 setKeys(Collections.EMPTY_LIST);
            }
            else 
                setKeys(children.keySet());
         }
         
         @Override
         public void removeNotify() {
             setKeys(Collections.EMPTY_LIST);
         }

         @Override
         protected Node[] createNodes(String key) {
             return new Node[]{ new LabelNode(key, children.get(key)) };
         }
     }
}
