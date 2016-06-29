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
package org.inventory.navigation.applicationnodes.objectnodes.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.navigation.applicationnodes.objectnodes.AbstractChildren;
import org.inventory.navigation.applicationnodes.objectnodes.SpecialObjectNode;
import org.openide.util.actions.Presenter;

/**
 * Creates a new special object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class CreateSpecialBusinessObjectAction extends AbstractAction 
            implements Presenter.Popup {
    private SpecialObjectNode node;
    private CommunicationsStub com;

    public CreateSpecialBusinessObjectAction(SpecialObjectNode node) {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW_SPECIAL_OBJECT"));
        com = CommunicationsStub.getInstance();
        this.node = node;
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {
        LocalObjectLight myLol = com.createSpecialObject(
                ((JMenuItem)ev.getSource()).getName(),
                node.getObject().getClassName(),
                node.getObject().getOid(), 0);
        if (myLol == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            ((AbstractChildren)node.getChildren()).addNotify();
                
            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE,
                        java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CREATED"));
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleChildren = new JMenu(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW"));

        List<LocalClassMetadataLight> items = com.getSpecialPossibleChildren(node.getObject().getClassName());
        
        if (items.isEmpty())
            mnuPossibleChildren.setEnabled(false);
        else
            for(LocalClassMetadataLight item: items) {
                    JMenuItem smiChildren = new JMenuItem(item.getClassName());
                    smiChildren.setName(item.getClassName());
                    smiChildren.addActionListener(this);
                    mnuPossibleChildren.add(smiChildren);
            }
		
        MenuScroller.setScrollerFor(mnuPossibleChildren, 20, 100);
		
        return mnuPossibleChildren;
    }
}