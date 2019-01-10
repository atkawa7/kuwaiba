/*
 * Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.ipam.nodes.actions;


import com.neotropic.inventory.modules.ipam.windows.InterfaceFrame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * Actions to relate a Service Instance to a Bridge Domain Interface
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELATE_TO)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class RelateGenericNetworkElementToInterface extends GenericObjectNodeAction {

    public RelateGenericNetworkElementToInterface() {
        putValue(NAME, I18N.gm("relate_to_bdi"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> interfaces = new ArrayList<>();
        List<LocalObjectLight> objects = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_BRIDGEDOMAININTERFACE);
        if(objects != null){
            for(LocalObjectLight o : objects)
                interfaces.add(o);
        }
        objects = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_VRFINSTANCE);
        if(objects != null){
            for(LocalObjectLight o : objects)
                interfaces.add(o);
        }
        objects = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_MPLSTUNNEL);
        if(objects != null){
            for(LocalObjectLight o : objects)
                interfaces.add(o);
        }
        objects = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_FRAMERELAYCIRCUIT);
        if(objects != null){
            for(LocalObjectLight o : objects)
                interfaces.add(o);
        }
        if (interfaces.isEmpty()) {
            JOptionPane.showMessageDialog(null, I18N.gm("no_interfaces_created_create_at_least_one"), 
                I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            InterfaceFrame frame = new InterfaceFrame(selectedObjects, interfaces);
            frame.setVisible(true);
        }
    }
    
    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public String[] appliesTo() {        
        return new String [] {Constants.CLASS_GENERICNETWORKELEMENT};
    }
    
    @Override
    public int numberOfNodes() {
        return -1;
    }
}
