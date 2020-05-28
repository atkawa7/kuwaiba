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
import java.util.Arrays;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * Relates ports using the MIRROR_MULTIPLE special relationship
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.MIRROR_PORT)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ConnectMirrorMultiplePortAction extends GenericObjectNodeAction {
    
    public ConnectMirrorMultiplePortAction() {
        putValue(NAME, I18N.gm("connect_mirror_multiple_port"));
    }

    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public String[] appliesTo() {
        return new String[] { Constants.CLASS_GENERICPORT };
    }

    @Override
    public int numberOfNodes() {
        return 1;        
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight port = selectedObjects.get(0);
        LocalObjectLight[] siblings = CommunicationsStub.getInstance().getSiblings(
            port.getClassName(), port.getId());
        if (siblings == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, 
                CommunicationsStub.getInstance().getError());
            return;
        }
        final JComboBox cmbSiblings = new JComboBox(siblings);
        cmbSiblings.setName("cmbSiblings");
        
        JComplexDialogPanel dialog = new JComplexDialogPanel(
            new String[] {"The other ports in the parent device are"}, 
            new JComponent[] {cmbSiblings});
        if (JOptionPane.showConfirmDialog(null, dialog, "Mirror Port Connection", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            LocalObjectLight selectedObject = (LocalObjectLight) cmbSiblings.getSelectedItem();
            if (selectedObject != null) {
                if (CommunicationsStub.getInstance().connectMirrorMultiplePort(
                    port.getClassName(), port.getId(), 
                    Arrays.asList(selectedObject.getClassName()), 
                    Arrays.asList(selectedObject.getId()))) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), 
                        NotificationUtil.INFO_MESSAGE, "Port mirrored multiple successfully");
                } else {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
                }
            }
        }
    }
    
}