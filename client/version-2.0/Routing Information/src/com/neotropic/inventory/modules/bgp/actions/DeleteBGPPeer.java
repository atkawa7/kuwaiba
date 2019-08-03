/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
package com.neotropic.inventory.modules.bgp.actions;

import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;

/**
 * Deletes a BGPPeer (the BGPPeers are created with BGP synchronization provider most of times)
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@ServiceProvider(service = GenericObjectNodeAction.class)
public class DeleteBGPPeer extends GenericObjectNodeAction implements Presenter.Popup{

    public DeleteBGPPeer() {
        putValue(NAME, "Delete BGPPeer");
        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
    }
    
    @Override
    public JMenuItem getPopupPresenter() { 
        return new JMenuItem(this);
    }
    
    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public String[] appliesTo() {
        return new String[] {"BGPPeer"};
    }

    @Override
    public int numberOfNodes() {
        return 1;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_NAVIGATION_TREE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(null, 
                "Are you sure you want to do this?", 
                "Delete BGPPeer(s)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            for (LocalObjectLight selectedObject : selectedObjects) {
                if (CommunicationsStub.getInstance().deleteBgpPeer(selectedObject.getId())) 
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, String.format("%s deleted sucessfully", selectedObject));
                else 
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }
    
}
