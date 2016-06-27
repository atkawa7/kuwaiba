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
 *  under the License.
 */
package com.neotropic.inventory.modules.contracts.nodes;

import com.neotropic.inventory.modules.contracts.nodes.actions.ContractManagerActionFactory;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.actions.ShowObjectIdAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Represents a contract
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ContractNode extends ObjectNode {

    public ContractNode(LocalObjectLight contract) {
        super(contract);
        setChildren(new ContractChildren());
    }

    @Override
    public Action[] getActions(boolean context) {
        if (showObjectIdAction == null) 
            showObjectIdAction = new ShowObjectIdAction(getObject().getOid(), getObject().getClassName());

        return new Action[] { ContractManagerActionFactory.getDeleteContractAction(), 
                                null, 
                                showObjectIdAction
                            };
    }
    
    public static class ContractChildren extends Children.Keys<LocalObjectLight> {
        
        @Override
        public void addNotify() {
            ContractNode selectedNode = (ContractNode)getNode();
            List<LocalObjectLight> equipment = CommunicationsStub.getInstance().getSpecialAttribute(selectedNode.getObject().getClassName(), 
                    selectedNode.getObject().getOid(), "contractHas"); //I18N
            
            if (equipment == null) {
                setKeys(Collections.EMPTY_LIST);
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
            } else {
                Collections.sort(equipment);
                setKeys(equipment);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_LIST);
        }
        
        @Override
        protected Node[] createNodes(LocalObjectLight key) {
            return new Node[] { new ObjectNode(key, true)};
        }
    }
}
