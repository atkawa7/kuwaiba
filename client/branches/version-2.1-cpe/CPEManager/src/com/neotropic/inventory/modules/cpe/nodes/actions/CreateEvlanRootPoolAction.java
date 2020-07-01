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
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.openide.util.Utilities;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CreateEvlanRootPoolAction extends GenericInventoryAction {
    
    public CreateEvlanRootPoolAction() {
        putValue(NAME, I18N.gm("modules.cpe.nodes.actions.CreateEvlanRootPoolAction.name")); //NOI18N
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CPE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CpeManagerRootNode cpeManagerRootNode = Utilities.actionsGlobalContext().lookup(CpeManagerRootNode.class);
        if (cpeManagerRootNode == null)
            return;
        
        JTextField txtPoolName = new JTextField();
        txtPoolName.setName(NAME);
        txtPoolName.setColumns(10);
        
        JTextField txtPoolDescription = new JTextField();
        txtPoolDescription.setName(NAME);
        txtPoolDescription.setColumns(10);
        
        JComplexDialogPanel pnlPoolProperties = new JComplexDialogPanel(new String[] {I18N.gm("pool_name"), I18N.gm("pool_description")}, new JComponent[] {txtPoolName, txtPoolDescription}); //NOI18N
        
        if (JOptionPane.showConfirmDialog(null, pnlPoolProperties, I18N.gm("modules.cpe.nodes.actions.CreateEvlanRootPoolAction.title"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) { //NOI18N
            LocalPool localPool = CommunicationsStub.getInstance().createRootPool(txtPoolName.getText(), txtPoolDescription.getText(), Constants.CLASS_EVLAN, LocalPool.POOL_TYPE_MODULE_ROOT);
            if (localPool != null) {
                ((CpeManagerRootNode.CpeManagerRootChildren) cpeManagerRootNode.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), //NOI18N
                    NotificationUtil.INFO_MESSAGE, I18N.gm("modules.cpe.nodes.actions.CreateEvlanRootPoolAction.text")); //NOI18N
            }
            else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), //NOI18N
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
    
}
