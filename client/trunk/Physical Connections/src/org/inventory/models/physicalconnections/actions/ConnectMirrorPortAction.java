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
package org.inventory.models.physicalconnections.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action allows to connect directly two ports
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ActionsGroupType(group=ActionsGroupType.Group.MIRROR_PORT)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ConnectMirrorPortAction extends GenericObjectNodeAction {

    public ConnectMirrorPortAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/models/physicalconnections/Bundle").getString("LBL_CONNECT_MIRROR_PORT"));
    } 
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight[] siblings = CommunicationsStub.getInstance().getSiblings(selectedObjects.get(0).getClassName(), selectedObjects.get(0).getOid());
        if (siblings == null){
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
        JComboBox cmbSiblings = new JComboBox(siblings);
        cmbSiblings.setName("cmbSiblings"); //NOI18N
        JComplexDialogPanel dialog = new JComplexDialogPanel(new String[]{"The other ports in the parent device are"},
                new JComponent[]{cmbSiblings});
        if (JOptionPane.showConfirmDialog(null, dialog, "Mirror Port Connection", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
            LocalObjectLight selectedObject = (LocalObjectLight)((JComboBox)dialog.getComponent("cmbSiblings")).getSelectedItem();
            if (selectedObject != null){
                List<String> aObjectsClasses = new ArrayList<>();
                List<String> bObjectsClasses = new ArrayList<>();
                List<Long> aObjectsIds = new ArrayList<>();
                List<Long> bObjectsIds = new ArrayList<>();
                
                aObjectsClasses.add(selectedObjects.get(0).getClassName());
                aObjectsIds.add(selectedObjects.get(0).getOid());

                bObjectsClasses.add(selectedObject.getClassName());
                bObjectsIds.add(selectedObject.getOid());
                
                if (CommunicationsStub.getInstance().connectMirrorPort(aObjectsClasses, aObjectsIds, bObjectsClasses, bObjectsIds))
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, "Port mirrored successfully");
                else
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE,CommunicationsStub.getInstance().getError());
            }
        }
    }

    @Override
    public String[] getValidators() {
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