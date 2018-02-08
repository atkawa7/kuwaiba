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

import com.neotropic.inventory.modules.contracts.nodes.ContractPoolNode;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * This action allows to create a contract
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class CreateContractAction extends GenericInventoryAction implements Presenter.Popup {
    private CommunicationsStub com = CommunicationsStub.getInstance();
    public CreateContractAction() {
        putValue(NAME, "New Contract");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        Iterator<? extends ContractPoolNode> selectedNodes = 
                Utilities.actionsGlobalContext().lookupResult(ContractPoolNode.class).allInstances().iterator();

        if (!selectedNodes.hasNext())
            return;
        
        ContractPoolNode selectedNode = selectedNodes.next();
        
        LocalObjectLight newPoolItem = com.createPoolItem(selectedNode.getPool().getOid(), 
                ((JMenuItem)e.getSource()).getText());

        if (newPoolItem == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {       
            ((ContractPoolNode.ContractPoolChildren)selectedNode.getChildren()).addNotify();
            NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Contract created successfully");
        }
    }    

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleContracts = new JMenu(this);
        
        Iterator<? extends ContractPoolNode> selectedNodes = 
                Utilities.actionsGlobalContext().lookupResult(ContractPoolNode.class).allInstances().iterator();

        if (selectedNodes.hasNext()) {
            ContractPoolNode selectedNode = selectedNodes.next();

            List<LocalClassMetadataLight> possibleContracts = com.getLightSubclasses(selectedNode.getPool().getClassName(), 
                    false, true);
            if (possibleContracts == null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else {
                if (possibleContracts.isEmpty())
                    mnuPossibleContracts.setEnabled(false);
                else {
                    for (LocalClassMetadataLight possibleContract : possibleContracts) {
                        JMenuItem mnuItemPossibleContract = new JMenuItem(possibleContract.getClassName()); 
                        mnuItemPossibleContract.addActionListener(this);
                        mnuPossibleContracts.add(mnuItemPossibleContract);
                    }
                }
            }
        }
        
        return mnuPossibleContracts;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CONTRACT_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
