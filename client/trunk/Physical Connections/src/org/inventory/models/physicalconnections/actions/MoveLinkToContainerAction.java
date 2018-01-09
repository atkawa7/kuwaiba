/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.models.physicalconnections.windows.MovePhysicalLinkToContainerFrame;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action allows to move a physical link into an existing container
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
@ServiceProvider(service = GenericObjectNodeAction.class)
public class MoveLinkToContainerAction  extends GenericObjectNodeAction{

    public MoveLinkToContainerAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/models/physicalconnections/Bundle").getString("LBL_MOVE_LINKS"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        for (LocalObjectLight object : selectedObjects) {
            if(!CommunicationsStub.getInstance().isSubclassOf(object.getClassName(), Constants.CLASS_GENERICPHYSICALLINK)){
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, "Please select only physical links");
                return;
            }
        }
                
        HashMap<String, LocalObjectLight[]> specialAttributes = CommunicationsStub.getInstance().getSpecialAttributes(selectedObjects.get(0).getClassName(), selectedObjects.get(0).getOid());

        if (specialAttributes == null ) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
        
        LocalObjectLight endpointA = null;
        LocalObjectLight endpointB = null;
            
        if (specialAttributes.containsKey("endpointA")) //NOI18N
            endpointA = specialAttributes.get("endpointA")[0]; //NOI18N
            
        if (specialAttributes.containsKey("endpointB")) //NOI18N
            endpointB = specialAttributes.get("endpointB")[0]; //NOI18N
        
        LocalObjectLight parent;
        
        if(endpointA != null && endpointB != null){
            parent = CommunicationsStub.getInstance().getCommonParent(endpointA.getClassName(), endpointA.getOid(), endpointB.getClassName(), endpointB.getOid());
        
            if (parent == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return;
            }

            List<LocalObjectLight> parentsA = CommunicationsStub.getInstance().getParents(endpointA.getClassName(), endpointA.getOid());
            List<LocalObjectLight> parentsB = CommunicationsStub.getInstance().getParents(endpointB.getClassName(), endpointB.getOid());
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

                    existingContainers.addAll(CommunicationsStub.getInstance().getContainersBetweenObjects(
                            parentA.getClassName(), parentA.getOid(), parentB.getClassName(), parentB.getOid(), Constants.CLASS_WIRECONTAINER));
                }
            }

            MovePhysicalLinkToContainerFrame frame = new MovePhysicalLinkToContainerFrame(selectedObjects, existingContainers);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }
    
    @Override
    public String[] getValidators() {
        return null;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public String[] appliesTo() {
        return new String[] {Constants.CLASS_GENERICPHYSICALLINK};
    }    
    
    @Override
    public int numberOfNodes() {
        return -1;
    }
}
