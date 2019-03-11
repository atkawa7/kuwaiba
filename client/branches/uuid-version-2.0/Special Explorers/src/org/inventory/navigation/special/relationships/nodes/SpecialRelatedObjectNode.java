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
 *  under the License.
 */
package org.inventory.navigation.special.relationships.nodes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;


/**
 * A node in the Special Relationships explorer
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SpecialRelatedObjectNode extends ObjectNode {

    public SpecialRelatedObjectNode(LocalObjectLight lol) {
        super(lol);
        setChildren(new SpecialRelationshipChildren());
    }
    
    @Override
    public boolean refresh() {
        ((SpecialRelationshipChildren) getChildren()).addNotify();
        return super.refresh();
    }
    
    public static class SpecialRelationshipChildren extends Children.Keys<String> {
        private HashMap<String, LocalObjectLight[]> specialRelationships;

        @Override
        public void addNotify() {
            LocalObjectLight object = ((SpecialRelatedObjectNode) getNode()).getLookup().lookup(LocalObjectLight.class);
            specialRelationships = CommunicationsStub.getInstance().getSpecialAttributes(object.getClassName(), object.getId());
            
            if (specialRelationships == null) {
                 NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                 setKeys(Collections.EMPTY_LIST);
            }
            else {
                List<LocalObjectLight> listOfParents = CommunicationsStub.getInstance()
                                                           .getParents(object.getClassName(), object.getId());
                
                if (listOfParents == null) {
                     NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                     setKeys(Collections.EMPTY_LIST);
                } else {
                    List<String> relationshipNames = new ArrayList(specialRelationships.keySet());
                    
                    //We now avoid loops by ignoring the relationship that caused the parent node to be displayed
                    if (getNode().getParentNode() != null) /*The grandpa node is a relationship node, except for the first level of relationships*/
                        relationshipNames.remove(getNode().getParentNode().getDisplayName());
                    
                    Collections.sort(relationshipNames);
                    
                    if (!listOfParents.isEmpty() && listOfParents.get(0).getId() != null && !listOfParents.get(0).getId().equals("-1") && !listOfParents.get(0).getClassName().startsWith("Pool of")) { //Ignore the dummy root and the pools
                        relationshipNames.add(0, Constants.PROPERTY_PARENT);
                        specialRelationships.put(Constants.PROPERTY_PARENT, new LocalObjectLight[] { listOfParents.get(0) });
                    }
                    setKeys(relationshipNames);
                }
            }
        }
         
         @Override
         public void removeNotify() {
             setKeys(Collections.EMPTY_LIST);
         }

         @Override
         protected Node[] createNodes(String key) {
             switch (key) { //Using a switch in case we need to add some other special relationships or if in the future we will also assign color sto relationships
                case Constants.PROPERTY_PARENT:
                    return new Node[]{ new RelationshipNode(key, specialRelationships.get(key), new Color(180, 32, 32)) };
                default:
                    return new Node[]{ new RelationshipNode(key, specialRelationships.get(key)) };
             }
         }
     }
}
