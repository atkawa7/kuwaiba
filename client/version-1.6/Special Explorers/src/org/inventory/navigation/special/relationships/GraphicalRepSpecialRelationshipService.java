/*
 * Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.special.relationships;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.special.relationships.nodes.LocalObjectLightWrapper;
import org.inventory.navigation.special.relationships.scene.SpecialRelationshipsGraphExplorerScene;

/**
 * Provides the business logic for the related TopComponent
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class GraphicalRepSpecialRelationshipService {
    private final SpecialRelationshipsGraphExplorerScene scene;
    private final LocalObjectLightWrapper root;
    
    public GraphicalRepSpecialRelationshipService(
        SpecialRelationshipsGraphExplorerScene scene, LocalObjectLightWrapper lolWrapper) {
        this.scene = scene;
        root = lolWrapper;        
    }
    
    public LocalObjectLightWrapper getRoot() {
        return root;
    }
    
    private HashMap<String, LocalObjectLight[]> getSpecialRelationships(LocalObjectLight lol) {
        HashMap<String, LocalObjectLight[]> specialRelationships = CommunicationsStub.getInstance()
            .getSpecialAttributes(lol.getClassName(), lol.getOid());
        
        if (specialRelationships == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return null;
        } else {
            LocalObjectLight parent = CommunicationsStub.getInstance().getParent(lol.getClassName(), lol.getOid());
            
            //Ignore the dummy root and the pools
            if (parent != null && parent.getOid() != -1 && !parent.getClassName().startsWith("Pool of")) 
                specialRelationships.put(Constants.PROPERTY_PARENT, new LocalObjectLight[] { parent });
            //else
            //    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
        return specialRelationships;
    }
        
    public void refreshScene() {
        scene.clear();
        scene.validate();
        
        scene.render(getRoot());
        showSpecialRelationshipChildren(getRoot());
    }
    
    public void showSpecialRelationshipChildren(LocalObjectLightWrapper lolWrapper) {
        
        if (scene.findNodeEdges(lolWrapper, true, false).isEmpty()) {
            
            LocalObjectLight lol = lolWrapper.getLocalObjectLightWrapped();
            
            HashMap<String, LocalObjectLight[]> specialRelationships = getSpecialRelationships(lol);

            if (specialRelationships != null) {

                List<String> relationshipNames = new ArrayList(specialRelationships.keySet());
                Collections.sort(relationshipNames);
                
                if (relationshipNames.remove(Constants.PROPERTY_PARENT))
                    relationshipNames.add(0, Constants.PROPERTY_PARENT);
                
                for (String relationshipName : relationshipNames) {
                    for (LocalObjectLight specialRelatedObjNode : specialRelationships.get(relationshipName)) {

                        LocalObjectLightWrapper specialRelateObjWrapper = new LocalObjectLightWrapper(specialRelatedObjNode);
                        
                        boolean relationshipExist = false;
                        
                        for (String inputEdge : scene.findNodeEdges(lolWrapper, false, true)) {
                            // Search if the current relationship not exist in the canvas
                            String inputRelationshipName = inputEdge.substring(inputEdge.indexOf(" ") + 1);
                            
                            if (inputRelationshipName.equals(relationshipName)) {
                                LocalObjectLight sourceLol =  scene.getEdgeSource(inputEdge).getLocalObjectLightWrapped();
                                if (sourceLol.getOid() == specialRelatedObjNode.getOid()) {
                                    relationshipExist = true;
                                    break;
                                }
                            }
                        }
                        
                        if (!relationshipExist) {
                            scene.addNode(specialRelateObjWrapper);

                            String edge = scene.getEdgeCounter() + " " + relationshipName;

                            scene.addEdge(edge);
                            scene.setEdgeSource(edge, lolWrapper);
                            scene.setEdgeTarget(edge, specialRelateObjWrapper);
                        }
                    }
                }
            }
            scene.reorganizeNodes();
        }
    }
    
    public void hideSpecialRelationshipChildrenRecursive(LocalObjectLightWrapper source, LocalObjectLightWrapper lolWrapper) {
        if (scene.findWidget(lolWrapper) != null) {
            
            String [] edges = scene.findNodeEdges(lolWrapper, true, false).toArray(new String[0]);
            
            for (String edge : edges)
                hideSpecialRelationshipChildrenRecursive(source, scene.getEdgeTarget(edge));
            
            if (!source.equals(lolWrapper))
                scene.removeNodeWithEdges(lolWrapper);
        }        
    }
        
    public void hideSpecialRelationshipChildren(LocalObjectLightWrapper lolWrapper) {
        hideSpecialRelationshipChildrenRecursive(lolWrapper, lolWrapper);
        scene.reorganizeNodes();
    }
}
