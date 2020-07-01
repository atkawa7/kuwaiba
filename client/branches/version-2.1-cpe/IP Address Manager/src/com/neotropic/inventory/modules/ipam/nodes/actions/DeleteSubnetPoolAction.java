/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package com.neotropic.inventory.modules.ipam.nodes.actions;

import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolChildren;
import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolNode;
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
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Deletes a subnet pool
 * @author Adrian Fernando Molina Fernandez {@literal <adrian.martinez@kuwaiba.org>}
 */
public class DeleteSubnetPoolAction extends GenericInventoryAction implements Presenter.Popup {
    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
    private static DeleteSubnetPoolAction instance;
    private final JMenuItem popupPresenter;
    
    private DeleteSubnetPoolAction(){
        putValue(NAME, I18N.gm("delete"));
        com = CommunicationsStub.getInstance();
        
        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
                
        popupPresenter = new JMenuItem();
        popupPresenter.setName((String) getValue(NAME));
        popupPresenter.setText((String) getValue(NAME));
        popupPresenter.setIcon((ImageIcon) getValue(SMALL_ICON));
        popupPresenter.addActionListener(this);
    }
    
    public static DeleteSubnetPoolAction getInstance() {
        return instance == null ? instance = new DeleteSubnetPoolAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator selectedNodes = Utilities.actionsGlobalContext().lookupResult(SubnetPoolNode.class).allInstances().iterator();
        SubnetPoolNode selectedNode = null;
        
        if (!selectedNodes.hasNext())
            return;

        while (selectedNodes.hasNext())
            selectedNode = (SubnetPoolNode)selectedNodes.next();
        
        if(selectedNode != null){
            SubnetPoolNode parentNode = (SubnetPoolNode)selectedNode.getParentNode();

            if (com.deleteSubnetPool(selectedNode.getSubnetPool().getId())){

                ((SubnetPoolChildren)parentNode.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, 
                        I18N.gm("subnet_pool_deleted"));
            }
            else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
        }
        else
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
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
