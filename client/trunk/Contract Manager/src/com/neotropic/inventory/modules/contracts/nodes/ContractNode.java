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
 *  under the License.
 */
package com.neotropic.inventory.modules.contracts.nodes;

import com.neotropic.inventory.modules.contracts.nodes.actions.ContractManagerActionFactory;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.ShowMoreInformationAction;
import org.openide.actions.PasteAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;

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
        return new Action[] { 
            SystemAction.get(PasteAction.class), 
            null, 
            ContractManagerActionFactory.getDeleteContractAction(), 
            null, 
            ShowMoreInformationAction.getInstance(getObject().getOid(), getObject().getClassName())
        };
    }
    
    
    @Override
    public PasteType getDropType(Transferable _obj, final int action, int index) {
        final Node dropNode = NodeTransfer.node(_obj,
                NodeTransfer.DND_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
        
        //When there's no an actual drag/drop operation, but a simple node selection
        if (dropNode == null) 
            return null;
        
        //The clipboard does not contain an Favorites Item Node
        if (!ObjectNode.class.isInstance(dropNode))
            return null;
        
        //Can't move to the same parent, only copy
        if (this.equals(dropNode.getParentNode()) && (action == DnDConstants.ACTION_MOVE)) 
            return null;
        
        return new PasteType() {
            @Override
            public Transferable paste() throws IOException {
                if (action == DnDConstants.ACTION_COPY) {
                    if (dropNode instanceof ObjectNode) {
                        ObjectNode objNode = (ObjectNode) dropNode;                        
                        
                        if (objNode.getParentNode() instanceof ContractNode) {
                                                        
                            if (CommunicationsStub.getInstance().associateObjectsToContract(
                                new String [] {objNode.getObject().getClassName()}, 
                                new Long [] {objNode.getObject().getOid()}, 
                                getObject().getClassName(), getObject().getOid())) {
                                
                                ((ContractChildren) getChildren()).addNotify();
                            } else
                                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                        }
                    }
                }
                if (action == DnDConstants.ACTION_MOVE) {
                    if (dropNode instanceof ObjectNode) {
                        ObjectNode objNode = (ObjectNode) dropNode;
                                                
                        if (objNode.getParentNode() instanceof ContractNode) {
                            ContractNode contractNode = (ContractNode) objNode.getParentNode();
                            
                            if (CommunicationsStub.getInstance().associateObjectsToContract(
                                new String [] {objNode.getObject().getClassName()}, 
                                new Long [] {objNode.getObject().getOid()},
                                getObject().getClassName(), getObject().getOid())) {
                                
                                ((ContractChildren) getChildren()).addNotify();
                                
                                if (CommunicationsStub.getInstance().releaseObjectFromContract(
                                    contractNode.getObject().getClassName(), 
                                    contractNode.getObject().getOid(), 
                                    objNode.getObject().getOid())) {

                                    ((ContractChildren) contractNode.getChildren()).addNotify();
                                } else
                                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                            } else
                                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                        }
                    }
                }
                return null;
            }
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
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
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
