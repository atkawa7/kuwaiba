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
package org.inventory.models.physicalconnections.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.models.physicalconnections.windows.MovePhysicalLinkToContainerFrame;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action allows to move a physical link into an existing container
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@ServiceProvider(service = GenericObjectNodeAction.class)
public class MoveLinksIntoContainerAction  extends GenericObjectNodeAction{

    private CommunicationsStub com = CommunicationsStub.getInstance(); 
    
    public MoveLinksIntoContainerAction() {
        putValue(NAME, I18N.gm("move_links_into_container"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        for (LocalObjectLight object : selectedObjects) {
            if(!com.isSubclassOf(object.getClassName(), Constants.CLASS_GENERICPHYSICALLINK)){
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, I18N.gm("select_only_physical_links"));
                return;
            }
        }
          
        HashMap<String, LocalObjectLight[]> specialAttributes;
        
        List<LocalObjectLight> endpointsA = new ArrayList<>();
        List<LocalObjectLight> endpointsB = new ArrayList<>();

        for (LocalObjectLight selectedObject : selectedObjects) {
            specialAttributes = com.getSpecialAttributes(selectedObject.getClassName(), selectedObject.getId());
            
            if (specialAttributes == null ) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return;
            }
            
            if (specialAttributes.containsKey("endpointA")) //NOI18N
                endpointsA.add(specialAttributes.get("endpointA")[0]); //NOI18N
            
            if (specialAttributes.containsKey("endpointB")) //NOI18N
                endpointsB.add(specialAttributes.get("endpointB")[0]); //NOI18N
        }
        
        LocalObjectLight parent;
        List<LocalObjectLight> parents = new ArrayList<>();
        
        if(!endpointsA.isEmpty() && !endpointsB.isEmpty()){
            for(int i=0; i<endpointsA.size(); i++)
                parents.add(com.getCommonParent(endpointsA.get(i).getClassName(), endpointsA.get(i).getId(), endpointsB.get(i).getClassName(), endpointsB.get(i).getId()));
            
            parent = parents.get(0);
            for (int i=1; i<parents.size(); i++) {
                if (!parent.equals(parents.get(i))) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, "There is no common parent between the selected objects");
                    return;
                }
            }

            List<List<LocalObjectLight>> allParentsA = new ArrayList<>();
            List<List<LocalObjectLight>> allParentsB = new ArrayList<>();
            List<Boolean> sameParents = new ArrayList<>();
            for(int i=0; i<endpointsA.size(); i++){
                List<LocalObjectLight> parentsA = com.getParentsUntilFirstOfClass(endpointsA.get(i).getClassName(), endpointsA.get(i).getId(), parent.getClassName());
                List<LocalObjectLight> parentsB = com.getParentsUntilFirstOfClass(endpointsB.get(i).getClassName(), endpointsB.get(i).getId(), parent.getClassName());
                allParentsA.add(parentsA);
                allParentsB.add(parentsB);
                sameParents.add(false);
            }
            //If all the endpoints has same EndPointA and EndPointB
            boolean sameParentsA = true;
            LocalObjectLight tempPrnt = allParentsA.get(0).get(allParentsA.get(0).size()-2);
            for (int i = 1; i < allParentsA.size(); i++) {
                if(!tempPrnt.equals(allParentsA.get(i).get(allParentsA.get(i).size()-2)))
                    sameParentsA = false;
            }
            
            boolean sameParentsB = true;
            tempPrnt = allParentsB.get(0).get(allParentsB.get(0).size()-2);
            for (int i = 1; i < allParentsB.size(); i++) {
                if(!tempPrnt.equals(allParentsB.get(i).get(allParentsB.get(1).size()-2)))
                    sameParentsB = false;
            }
            //Check if parents are the same but in diferent side
            if(!sameParentsA || !sameParentsB){
                for(int j = 0; j < allParentsA.size(); j++){
                    LocalObjectLight prntA = allParentsA.get(j).get(allParentsA.get(j).size()-2);
                    for (List<LocalObjectLight> allParentsB1 : allParentsB) {
                        LocalObjectLight prntB = allParentsB1.get(allParentsB1.size() - 2);
                        if(prntA.equals(prntB)){
                            sameParents.set(j, true);
                            break;
                        }
                    }
                }
                for (boolean is : sameParents) {
                    if(!is){
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, I18N.gm("select_links_with_same_end_ponits"));
                        return; 
                    }
                }
            }
            
            for(int i=0; i<endpointsA.size(); i++){
                List<LocalObjectLight> parentsA = com.getParents(endpointsA.get(i).getClassName(), endpointsA.get(i).getId());
                List<LocalObjectLight> parentsB = com.getParents(endpointsB.get(i).getClassName(), endpointsB.get(i).getId());    
                List<LocalObjectLight> existingContainers = new ArrayList<>();
                boolean childrenToEvaluatedA = true;
                int indexA = parentsA.indexOf(parent);

                while(childrenToEvaluatedA){
                    indexA--;
                    if(indexA == 0)
                        childrenToEvaluatedA = false;

                    int indexB = parentsB.indexOf(parent);
                    boolean childrenToEvaluatedB = true;
                    LocalObjectLight parentA = parentsA.get(indexA);

                    while(childrenToEvaluatedB){
                        indexB--;
                        if(indexB == 0)
                            childrenToEvaluatedB = false;

                        LocalObjectLight parentB = parentsB.get(indexB);

                        existingContainers.addAll(com.getContainersBetweenObjects(
                                parentA.getClassName(), parentA.getId(), parentB.getClassName(), parentB.getId(), Constants.CLASS_WIRECONTAINER));
                    }
                }

                MovePhysicalLinkToContainerFrame frame = MovePhysicalLinkToContainerFrame.getInstance(selectedObjects, existingContainers);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        }
    }
    
    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public String[] appliesTo() {
        return new String[] { Constants.CLASS_GENERICPHYSICALLINK };
    }    
    
    @Override
    public int numberOfNodes() {
        return -1;
    }
}
