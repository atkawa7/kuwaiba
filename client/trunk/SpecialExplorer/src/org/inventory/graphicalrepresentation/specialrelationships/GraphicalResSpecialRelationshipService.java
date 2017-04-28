/**
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
package org.inventory.graphicalrepresentation.specialrelationships;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.graphicalrepresentation.specialrelationships.scene.GraphicalRepSpecialRelationshipsScene;
import org.inventory.graphicalrepresentation.specialrelationships.wrappers.LocalObjectLightWrapper;

/**
 * Provides the business logic for the related TopComponent
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class GraphicalResSpecialRelationshipService {
    private final GraphicalRepSpecialRelationshipsScene scene;
    private final LocalObjectLightWrapper root;
    
    public GraphicalResSpecialRelationshipService(
        GraphicalRepSpecialRelationshipsScene scene, LocalObjectLightWrapper lolWrapper) {
        this.scene = scene;
        root = lolWrapper;        
    }
    
    public LocalObjectLightWrapper getRoot() {
        return root;
    }
    
    public void addSpecialRelatedObject(LocalObjectLightWrapper node) {
        scene.addNode(node);
        scene.reorganizeNodes();
    }
    
    private HashMap<String, LocalObjectLight[]> getSpecialRelationships(LocalObjectLight lol) {
        HashMap<String, LocalObjectLight[]> specialRelationships = CommunicationsStub.getInstance()
            .getSpecialAttributes(lol.getClassName(), lol.getOid());
        
        if (specialRelationships == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
        else {
            List<LocalObjectLight> listOfParents = CommunicationsStub.getInstance()
                .getParents(lol.getClassName(), lol.getOid());
            
            if (listOfParents == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            } else {
                
                if (!listOfParents.isEmpty() && listOfParents.get(0).getOid() != -1) { //Ignore the dummy root
                    
                    specialRelationships.put(Constants.PROPERTY_PARENT, new LocalObjectLight[] { listOfParents.get(0) });
                    return specialRelationships;
                }
            }
        }
        return null;
    }
    
    public void showSpecialRelationshipChildren(LocalObjectLightWrapper lolWrapper) {
        
        if (scene.findNodeEdges(lolWrapper, true, false).isEmpty()) {
            
            LocalObjectLight lol = lolWrapper.getLocalObjectLightWrapped();
            
            HashMap<String, LocalObjectLight[]> specialRelationships = getSpecialRelationships(lol);

            if (specialRelationships != null) {

                List<String> relationshipNames = new ArrayList(specialRelationships.keySet());
                Collections.sort(relationshipNames);

                String propertyParent = (String) relationshipNames.remove(relationshipNames.indexOf(Constants.PROPERTY_PARENT));
                relationshipNames.add(0, propertyParent);

                for (String relationshipName : relationshipNames) {
                    for (LocalObjectLight specialRelatedObjNode : specialRelationships.get(relationshipName)) {

                        LocalObjectLightWrapper specialRelateObjWrapper = new LocalObjectLightWrapper(specialRelatedObjNode);

                        scene.addNode(specialRelateObjWrapper);

                        String edge = scene.getEdgeCounter() + " " + relationshipName;

                        scene.addEdge(edge);
                        scene.setEdgeSource(edge, lolWrapper);
                        scene.setEdgeTarget(edge, specialRelateObjWrapper);
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
