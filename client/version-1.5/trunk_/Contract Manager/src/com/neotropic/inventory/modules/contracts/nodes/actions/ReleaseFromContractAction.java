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
package com.neotropic.inventory.modules.contracts.nodes.actions;

import com.neotropic.inventory.modules.contracts.nodes.ContractNode;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action releases de relationship between the object and the service
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ReleaseFromContractAction extends GenericObjectNodeAction implements ComposedAction {
    
    public ReleaseFromContractAction() {
        putValue(NAME, "Release from Contract...");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight selectedObj = selectedObjects.get(0); //Uses the last selected only
        List<LocalObjectLight> contracts = CommunicationsStub.getInstance()
            .getSpecialAttribute(selectedObj.getClassName(), selectedObj.getOid(), "contractHas");
        
        if (contracts == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.INFO_MESSAGE, 
                CommunicationsStub.getInstance().getError());
        } else {
            if (contracts.isEmpty())
                JOptionPane.showMessageDialog(null, "There are not contracts related to the selected object", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            else {
                List<SubMenuItem> subMenuItems = new ArrayList();
                for (LocalObjectLight contract : contracts) {
                    SubMenuItem subMenuItem = new SubMenuItem(contract.toString());
                    subMenuItem.addProperty(Constants.PROPERTY_ID, contract.getOid());
                    subMenuItems.add(subMenuItem);
                }
                SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
            }
        }
    }

    @Override
    public String getValidator() {
        return null;
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CONTRACT_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
    
    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            if (JOptionPane.showConfirmDialog(null, 
                    "The selected objects will no longer be related to this contract\n Are you sure you want to continue?", "Warning", 
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

                Iterator<? extends ObjectNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();

                boolean success = true;
                while (selectedNodes.hasNext()) {
                    ObjectNode selectedNode = selectedNodes.next();
                    if (CommunicationsStub.getInstance().releaseObjectFromContract(selectedNode.getObject().getClassName(), 
                        selectedNode.getObject().getOid(), (Long) ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getProperty(Constants.PROPERTY_ID))) {
                        if (selectedNode.getParentNode() instanceof ContractNode)
                            ((ContractNode.ContractChildren)selectedNode.getParentNode().getChildren()).addNotify();
                    } else {
                        success = false;
                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    }
                }

                if (success)
                    NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "The selected devices were released from the contract");
            }
        }
    }
}