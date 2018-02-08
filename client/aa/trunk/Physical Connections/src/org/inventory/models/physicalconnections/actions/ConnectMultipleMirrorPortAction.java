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
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action allows to connect directly two ports
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ConnectMultipleMirrorPortAction extends GenericObjectNodeAction {

    public ConnectMultipleMirrorPortAction() {
        putValue(NAME, I18N.gm("connect_mirror_ports"));
    } 
    
    @Override
    public void actionPerformed(ActionEvent e) {

        List<LocalObjectLight> children = CommunicationsStub.getInstance().getObjectChildren(selectedObjects.get(0).getOid(), selectedObjects.get(0).getClassName());
        List<LocalObjectLight> endPoints =  new ArrayList<>();
        
        List<LocalObjectLight> endPointsA = new ArrayList<>();
        List<LocalObjectLight> endPointsB = new ArrayList<>();
        
        List<String> aObjectsClasses = new ArrayList<>();
        List<String> bObjectsClasses = new ArrayList<>();
        List<Long> aObjectsIds = new ArrayList<>();
        List<Long> bObjectsIds = new ArrayList<>();
        
        for (LocalObjectLight child : children) {
            if(child.getClassName().equals("OpticalPort") || child.getClassName().equals("ElectricalPort")) 
                endPoints.add(child);
        }
        
        if(!endPoints.isEmpty() && endPoints.size() % 2 == 0){
            for (int i=0; i < endPoints.size(); i++) {
                LocalObjectLight endPointA = endPoints.get(i);
                if(endPointA != null){
                    for (int j=i+1; j < endPoints.size(); j++) {
                        LocalObjectLight endPointB = endPoints.get(j);
                        if(endPointB != null && endPointB.getOid() != endPointA.getOid()){
                            if(endPointA.getClassName().equals(endPointB.getClassName()) && matchMirrorPortsNames(endPointA.getName(), endPointB.getName())){
                                endPointsA.add(endPointA);
                                endPointsB.add(endPoints.get(j));
                                endPoints.set(j, null);
                                endPoints.set(i, null);
                                break;
                            }
                        }
                    }//end for
                }
            }//end for
            if(endPointsA.size() != endPointsB.size())
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE,  I18N.gm("not_same_number_back_front_ports"));
            else{
                for (int i = 0; i < endPointsA.size(); i++) {
                    aObjectsClasses.add(endPointsA.get(i).getClassName());
                    aObjectsIds.add(endPointsA.get(i).getOid());

                    bObjectsClasses.add(endPointsB.get(i).getClassName());
                    bObjectsIds.add(endPointsB.get(i).getOid());
                }

                if (CommunicationsStub.getInstance().connectMirrorPort(aObjectsClasses, aObjectsIds, bObjectsClasses, bObjectsIds))
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, aObjectsIds.size() + I18N.gm("port_mirrored_successfully"));
                else
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE,CommunicationsStub.getInstance().getError());
            }
        }
        else
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE,  I18N.gm("not_same_number_back_front_ports"));
    }
   
    @Override
    public String[] getValidators() {
        return null;
    }  

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
      
    private boolean matchMirrorPortsNames(String back, String front){
        back = back.toLowerCase();
        front = front.toLowerCase();
        String frontNumericPart = "";
        for (int i = 1; i < front.length(); i++){
            if(Utils.isNumeric(front.substring(i-1, i)))
                frontNumericPart += front.substring(i-1,i);
        }
        String backNumericPart = "";
        
        for (int i = 1; i < back.length(); i++){
            if(Utils.isNumeric(back.substring(i - 1, i)))
                backNumericPart += back.substring(i - 1, i);
        }

        return backNumericPart.equals(frontNumericPart);
    }

    @Override
    public String[] appliesTo() {
        return new String[] { Constants.CLASS_GENERICDISTRIBUTIONFRAME };
    }
    
    @Override
    public int numberOfNodes() {
        return 1;
    }
}