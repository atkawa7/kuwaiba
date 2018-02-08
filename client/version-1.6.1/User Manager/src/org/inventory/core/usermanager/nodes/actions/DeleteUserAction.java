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
 */

package org.inventory.core.usermanager.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.inventory.core.usermanager.nodes.GroupNode;
import org.inventory.core.usermanager.nodes.UserNode;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Deletes a user
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DeleteUserAction extends GenericInventoryAction implements Presenter.Popup {
    private final JMenuItem popupPresenter;

    public DeleteUserAction() {
        putValue(NAME, "Delete Users");
        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
                
        popupPresenter = new JMenuItem();
        popupPresenter.setName((String) getValue(NAME));
        popupPresenter.setText((String) getValue(NAME));
        popupPresenter.setIcon((ImageIcon) getValue(SMALL_ICON));
        popupPresenter.addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends UserNode> selectedNodes = 
                Utilities.actionsGlobalContext().lookupResult(UserNode.class).allInstances().iterator();

        if (JOptionPane.showConfirmDialog(null, "Only users not associated to other groups will be deleted. Are you sure you want to do this?", 
                "Delete User", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        
            List<Long> usersToDelete = new ArrayList<>();
            Node lastSelectedNode = null;
            
            while (selectedNodes.hasNext()) {
                lastSelectedNode = selectedNodes.next();
                usersToDelete.add(lastSelectedNode.getLookup().lookup(LocalUserObject.class).getId());
            }
            
            if (!usersToDelete.isEmpty()) {
                if(CommunicationsStub.getInstance().deleteUsers(usersToDelete)) {
                    ((GroupNode.UserChildren)lastSelectedNode.getParentNode().getChildren()).addNotify(); //lastSelectedNode eill never be null in this if
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "User deleted successfully");
                } else
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_USER_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }
}
