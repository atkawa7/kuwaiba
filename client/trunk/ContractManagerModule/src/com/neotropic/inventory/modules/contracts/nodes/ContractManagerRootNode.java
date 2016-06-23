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
package com.neotropic.inventory.modules.contracts.nodes;

import com.neotropic.inventory.modules.contracts.nodes.actions.ContractManagerActionFactory;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Represents the root node of the tree in the COntract manager module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ContractManagerRootNode extends AbstractNode {

    public ContractManagerRootNode(ContractManagerRootChildren children) {
        super(children);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { ContractManagerActionFactory.getCreateContractPoolAction() };
    }
    
    
    
    public static class ContractManagerRootChildren extends Children.Keys<LocalPool> {

        @Override
        public void addNotify() {
            List<LocalPool> contractPools = CommunicationsStub.getInstance().getPools(Constants.CLASS_GENERICCONTRACT);

            if (contractPools == null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
            else {
                Collections.sort(contractPools);
                setKeys(contractPools);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
        
        @Override
        protected Node[] createNodes(LocalPool key) {
            return new Node[] { new ContractPoolNode(key) };
        }
    }
    
}
