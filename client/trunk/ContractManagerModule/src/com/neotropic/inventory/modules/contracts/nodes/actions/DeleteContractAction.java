/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
import com.neotropic.inventory.modules.contracts.nodes.ContractPoolNode;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Utilities;

/**
 * Deletes a contract
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DeleteContractAction extends AbstractAction {

    public DeleteContractAction() {
        putValue(NAME, "Delete Contract");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends ContractNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ContractNode.class).allInstances().iterator();
            
        if (!selectedNodes.hasNext())
            return;
        
        
        List<String> classes = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        
        ContractNode selectedNode = selectedNodes.next();
        
        classes.add(selectedNode.getObject().getClassName());
        ids.add(selectedNode.getObject().getOid());
        
        if (CommunicationsStub.getInstance().deleteObjects(classes, ids)) {
            NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "The selected contracts were deleted");
            ((ContractPoolNode.ContractPoolChildren)selectedNode.getParentNode().getChildren()).addNotify();
        }
        else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.INFO_MESSAGE, CommunicationsStub.getInstance().getError());
            
    }
    
}
