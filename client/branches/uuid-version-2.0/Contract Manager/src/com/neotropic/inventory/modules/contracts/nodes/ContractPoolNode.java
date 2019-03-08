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
package com.neotropic.inventory.modules.contracts.nodes;

import com.neotropic.inventory.modules.contracts.nodes.actions.ContractManagerActionFactory;
import java.awt.Image;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPool;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.pools.nodes.PoolNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 * Represents a contract pool
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ContractPoolNode extends PoolNode {

    private Image icon = ImageUtilities.loadImage("com/neotropic/inventory/modules/contracts/res/contractPool.png");
    
    public ContractPoolNode(LocalPool pool) {
        super(pool);
        setChildren(new ContractPoolChildren());
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { ContractManagerActionFactory.getCreateContractAction(), 
                              null,
                                ContractManagerActionFactory.getDeleteContractPoolAction(),
                            };
    }
    
    @Override
    public String getDisplayName() {
        return String.format(I18N.gm("pool_of"), getPool().getName(), getPool().getClassName());
    }
    
    @Override
    public Image getIcon(int i){
        return icon;
    }
    
    @Override
    public Image getOpenedIcon(int i){
        return getIcon(i);
    }
    
    public static class ContractPoolChildren extends Children.Keys<LocalObjectLight> {
        
        @Override
        public void addNotify() {
            ContractPoolNode selectedNode = (ContractPoolNode)getNode();
            List<LocalObjectLight> contracts = CommunicationsStub.getInstance().getPoolItems(selectedNode.getPool().getId());
            
            if (contracts == null) {
                setKeys(Collections.EMPTY_LIST);
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
            } else {
                Collections.sort(contracts);
                setKeys(contracts);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_LIST);
        }
        
        @Override
        protected Node[] createNodes(LocalObjectLight key) {
            return new Node[] { new ContractNode(key)};
        }
    }
}