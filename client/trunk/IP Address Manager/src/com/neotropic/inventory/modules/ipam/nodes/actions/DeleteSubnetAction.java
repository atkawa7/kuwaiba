/*
 * Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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

import com.neotropic.inventory.modules.ipam.nodes.SubnetNode;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Deletes a subnet
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class DeleteSubnetAction extends GenericInventoryAction implements Presenter.Popup {
    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
    private static DeleteSubnetAction instance;
    private final JMenuItem popupPresenter;
    
    private DeleteSubnetAction(){
        putValue(NAME, I18N.gm("delete"));
        com = CommunicationsStub.getInstance();
        
        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
                
        popupPresenter = new JMenuItem();
        popupPresenter.setName((String) getValue(NAME));
        popupPresenter.setText((String) getValue(NAME));
        popupPresenter.setIcon((ImageIcon) getValue(SMALL_ICON));
        popupPresenter.addActionListener(this);
    }
    
    public static DeleteSubnetAction getInstance() {
        return instance == null ? instance = new DeleteSubnetAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends SubnetNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(SubnetNode.class).allInstances().iterator();
        
        if (!selectedNodes.hasNext())
            return;
        
        while (selectedNodes.hasNext()) {
            SubnetNode selectedNode = (SubnetNode)selectedNodes.next();
            
            if (com.deleteSubnet(selectedNode.getObject().getClassName(), selectedNode.getObject().getOid())){
                ((AbstractChildren)selectedNode.getParentNode().getChildren()).addNotify();
                
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, 
                        I18N.gm("subnet_deleted"));
            }
            else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());            
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }    

    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }
}
