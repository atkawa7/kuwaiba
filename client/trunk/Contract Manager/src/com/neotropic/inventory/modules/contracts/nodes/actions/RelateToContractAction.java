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
package com.neotropic.inventory.modules.contracts.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.windows.SelectValueFrame;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action allows the user relate the current object to a service as a resource
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELATE_TO)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class RelateToContractAction extends GenericObjectNodeAction implements ComposedAction {

    public RelateToContractAction() {
        putValue(NAME, I18N.gm("relate_to_contract"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> contracts = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_GENERICCONTRACT);

        if (contracts ==  null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            if (contracts.isEmpty()) {
                JOptionPane.showMessageDialog(null, I18N.gm("no_contracts_created"), 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                SelectValueFrame frame = new SelectValueFrame(I18N.gm("available_contracts"), I18N.gm("select_contract_from_list"), I18N.gm("create_relationship"), contracts);
                frame.addListener(this);
                frame.setVisible(true);
            }
        }
    }

    @Override
    public LocalValidator[] getValidators() {
        return null; //Enable this action for any object
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CONTRACT_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e.getSource() instanceof SelectValueFrame) {
            SelectValueFrame frame = (SelectValueFrame) e.getSource();
            Object selectedValue = frame.getSelectedValue();
            
            if (selectedValue == null)
                JOptionPane.showMessageDialog(null, I18N.gm("select_contract_from_list"));
            else {
                String [] objectsClassName = new String[selectedObjects.size()];
                String [] objectsId = new String[selectedObjects.size()];
                
                for (int i = 0; i < selectedObjects.size(); i += 1) {
                    objectsClassName[i] = selectedObjects.get(i).getClassName();
                    objectsId[i] = selectedObjects.get(i).getId();
                }
                
                if (CommunicationsStub.getInstance().associateObjectsToContract(
                    objectsClassName, objectsId, 
                    ((LocalObjectLight) selectedValue).getClassName(), 
                    ((LocalObjectLight) selectedValue).getId())) {
                    
                    JOptionPane.showMessageDialog(null, String.format(I18N.gm("selected_devices_were_related_to"), selectedValue));
                    frame.dispose();
                } else
                    JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), 
                        I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public String[] appliesTo() {
        return null; //Enable this action for any object
    }
    
    @Override
    public int numberOfNodes() {
        return -1;
    }
}
