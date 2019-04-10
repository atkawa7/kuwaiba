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

import com.neotropic.inventory.modules.contracts.nodes.ContractNode;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action releases de relationship between the object and the service
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELEASE_FROM)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ReleaseFromContractAction extends GenericObjectNodeAction implements ComposedAction {
    
    public ReleaseFromContractAction() {
        putValue(NAME, I18N.gm("release_from_contract"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight selectedObj = selectedObjects.get(0); //Uses the last selected only
        List<LocalObjectLight> contracts = CommunicationsStub.getInstance()
            .getSpecialAttribute(selectedObj.getClassName(), selectedObj.getId(), "contractHas"); //NOI18N
        
        if (contracts == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.INFO_MESSAGE, 
                CommunicationsStub.getInstance().getError());
        } else {
            if (contracts.isEmpty())
                JOptionPane.showMessageDialog(null, I18N.gm("no_cotracts_related_to_selected_object"), 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            else {
                List<SubMenuItem> subMenuItems = new ArrayList<>();
                for (LocalObjectLight contract : contracts) {
                    SubMenuItem subMenuItem = new SubMenuItem(contract.toString());
                    subMenuItem.addProperty(Constants.PROPERTY_ID, contract.getId());
                    subMenuItems.add(subMenuItem);
                }
                SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
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
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            if (JOptionPane.showConfirmDialog(null, 
                    I18N.gm("want_to_release_from_contract"), I18N.gm("warning"), 
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

                Iterator<? extends ObjectNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();

                boolean success = true;
                while (selectedNodes.hasNext()) {
                    ObjectNode selectedNode = selectedNodes.next();
                    if (CommunicationsStub.getInstance().releaseObjectFromContract(selectedNode.getObject().getClassName(), 
                        selectedNode.getObject().getId(), (String) ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getProperty(Constants.PROPERTY_ID))) {
                        if (selectedNode.getParentNode() instanceof ContractNode)
                            ((ContractNode.ContractChildren)selectedNode.getParentNode().getChildren()).addNotify();
                    } else {
                        success = false;
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    }
                }

                if (success)
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, I18N.gm("objects_released_from_cotract"));
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