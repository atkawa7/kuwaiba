/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.warehouses.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.pools.nodes.PoolNode;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class NewWarehoseAction extends GenericInventoryAction implements Presenter.Popup {
    
    private PoolNode poolNode;
    private CommunicationsStub com;

    public NewWarehoseAction(PoolNode node) {
        this.poolNode = node;
        //putValue(NAME, "New Warehouse");
        com = CommunicationsStub.getInstance();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight newObject = com.createPoolItem(poolNode.getPool().getId(), ((JMenuItem)e.getSource()).getName());
        if (newObject == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            ((AbstractChildren)poolNode.getChildren()).addNotify();
            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "Warehouse created successfully");
        }
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleChildren = new JMenu("New Warehouse");
        List<LocalClassMetadataLight> items = com.getLightSubclasses(poolNode.getPool().getClassName(), false, true);
        
        if (items == null) {
            mnuPossibleChildren.setEnabled(false);
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        } else {
            if (items.isEmpty())
                mnuPossibleChildren.setEnabled(false);
            else {
                for(LocalClassMetadataLight item: items){
                        JMenuItem smiChildren = new JMenuItem(item.getClassName());
                        smiChildren.setName(item.getClassName());
                        smiChildren.addActionListener(this);
                        mnuPossibleChildren.add(smiChildren);
                }
                MenuScroller.setScrollerFor(mnuPossibleChildren, 20, 100);
            }
        }
		
        return mnuPossibleChildren;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_POOLS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
