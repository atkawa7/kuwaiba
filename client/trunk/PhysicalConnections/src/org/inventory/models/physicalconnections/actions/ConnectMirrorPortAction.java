/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.models.physicalconnections.actions;

import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action allows to connect directly two ports
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ConnectMirrorPortAction extends GenericObjectNodeAction {

    public ConnectMirrorPortAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/models/physicalconnections/Bundle").getString("LBL_CONNECT_MIRROR_PORT"));
    } 
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight[] siblings = CommunicationsStub.getInstance().getSiblings(object.getClassName(), object.getOid());
        if (siblings == null){
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
        JComboBox cmbSiblings = new JComboBox(siblings);
        cmbSiblings.setName("cmbSiblings"); //NOI18N
        JComplexDialogPanel dialog = new JComplexDialogPanel(new String[]{"The other ports in the parent device are"},
                new JComponent[]{cmbSiblings});
        if (JOptionPane.showConfirmDialog(null, dialog, "Mirror Port Connection", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
            LocalObjectLight selectedObject = (LocalObjectLight)((JComboBox)dialog.getComponent("cmbSiblings")).getSelectedItem();
            if (selectedObject != null){
                if (CommunicationsStub.getInstance().connectMirrorPort(object.getClassName(), object.getOid(),
                        selectedObject.getClassName(), selectedObject.getOid()))
                    NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "Port mirrored successfully");
                else
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE,CommunicationsStub.getInstance().getError());
            }
        }
    }

    @Override
    public String getValidator() {
        return Constants.VALIDATOR_PHYSICAL_ENDPOINT;
    }  
}
