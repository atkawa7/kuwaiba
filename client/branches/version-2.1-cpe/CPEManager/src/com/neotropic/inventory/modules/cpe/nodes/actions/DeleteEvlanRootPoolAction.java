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
package com.neotropic.inventory.modules.cpe.nodes.actions;

import com.neotropic.inventory.modules.cpe.nodes.CpeManagerRootNode;
import com.neotropic.inventory.modules.cpe.nodes.EvlanPoolNode;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeleteEvlanRootPoolAction extends GenericInventoryAction implements Presenter.Popup {
    private final JMenuItem popupPresenter;
    
    public DeleteEvlanRootPoolAction() {
        putValue(NAME, I18N.gm("modules.cpe.nodes.actions.DeleteEvlanRootPoolAction.name")); //NOI18N
        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
        
        popupPresenter = new JMenuItem();
        popupPresenter.setName((String) getValue(NAME));
        popupPresenter.setText((String) getValue(NAME));
        popupPresenter.setIcon((ImageIcon) getValue(SMALL_ICON));
        popupPresenter.addActionListener(this);
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CPE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EvlanPoolNode evlanPoolNode = Utilities.actionsGlobalContext().lookup(EvlanPoolNode.class);        
        if (evlanPoolNode == null)
            return;
        
        if (JOptionPane.showConfirmDialog(null, I18N.gm("want_to_delete_pool"), I18N.gm("warning"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (CommunicationsStub.getInstance().deletePool(evlanPoolNode.getPool().getId())) {
                ((CpeManagerRootNode.CpeManagerRootChildren) evlanPoolNode.getParentNode().getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), //NOI18N
                    NotificationUtil.INFO_MESSAGE, I18N.gm("pool_was_deleted")); //NOI18N
            }
            else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), //NOI18N
                    NotificationUtil.INFO_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }
}
