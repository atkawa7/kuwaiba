/**
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

package com.neotropic.inventory.modules.mpls.actions;

import java.awt.event.ActionEvent;
import static javax.swing.Action.NAME;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * Deletes an MPLS link
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service = GenericObjectNodeAction.class)
public class DeleteMPLSConnectionAction extends GenericObjectNodeAction /*implements Presenter.Popup*/ {
//    private final JMenuItem popupPresenter;

    public DeleteMPLSConnectionAction() {
        this.putValue(NAME, "Delete MPLS Link"); 
//        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
//                
//        popupPresenter = new JMenuItem();
//        popupPresenter.setName((String) getValue(NAME));
//        popupPresenter.setText((String) getValue(NAME));
//        popupPresenter.setIcon((ImageIcon) getValue(SMALL_ICON));
//        popupPresenter.addActionListener(this);
    }  

    @Override
    public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(null, 
                "Are you sure you want to do this?", 
                "Delete MPLS Link", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            for (LocalObjectLight selectedObject : selectedObjects) {
                if (CommunicationsStub.getInstance().deleteMPLSLink(selectedObject.getClassName(), selectedObject.getOid())) 
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, String.format("%s deleted sucessfully", selectedObject));
                else 
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }

    @Override
    public String[] getValidators() {
        return null;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_MPLS_MODULE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public String[] appliesTo() {
        return new String[] {Constants.CLASS_MPLSLINK};
    }
    
    @Override
    public int numberOfNodes() {
        return -1;
    }
}
