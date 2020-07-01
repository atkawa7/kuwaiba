/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
 */
package org.inventory.models.physicalconnections.actions;

import java.awt.event.ActionEvent;
import static javax.swing.Action.NAME;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action allows to remove the port mirroring multiple relationship between two ports
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.MIRROR_PORT)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ReleaseMirrorMultiplePortAction extends GenericObjectNodeAction {
    
    public ReleaseMirrorMultiplePortAction() {
        putValue(NAME, I18N.gm("disconnect_mirror_multiple_port"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(null, 
                "Are you sure you want to disconnect this mirror multiple port?", I18N.gm("warning"), 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        
            if (CommunicationsStub.getInstance().releaseMirrorMultiplePort(selectedObjects.get(0).getClassName(), selectedObjects.get(0).getId()))
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, "Miror multiple port disconnected successfully");
            else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());        
        }
    }

    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public String[] appliesTo() {
        return new String [] {Constants.CLASS_GENERICPORT};
    }
    
    @Override
    public int numberOfNodes() {
        return 1;
    }
}