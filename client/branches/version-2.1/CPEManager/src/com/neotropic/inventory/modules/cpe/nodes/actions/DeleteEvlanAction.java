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

import com.neotropic.inventory.modules.cpe.nodes.EvlanNode;
import com.neotropic.inventory.modules.cpe.nodes.EvlanPoolNode;
import java.awt.event.ActionEvent;
import static javax.swing.Action.NAME;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.util.Utilities;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeleteEvlanAction extends GenericInventoryAction {
    
    public DeleteEvlanAction() {
        putValue(NAME, I18N.gm("modules.cpe.nodes.actions.DeleteEvlanAction.name"));
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CPE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (JOptionPane.showConfirmDialog(null, I18N.gm("modules.cpe.nodes.actions.DeleteEvlanAction.toDelete"), 
            I18N.gm("warning"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            EvlanNode evlanNode = Utilities.actionsGlobalContext().lookup(EvlanNode.class);
            if (evlanNode == null)
                return;
            if (CommunicationsStub.getInstance().deleteObject(evlanNode.getObject().getClassName(), evlanNode.getObject().getId(), false)) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, I18N.gm("modules.cpe.nodes.actions.DeleteEvlanAction.deleted"));
                ((EvlanPoolNode.EvlanPoolChildren) evlanNode.getParentNode().getChildren()).addNotify();
            }
            else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.INFO_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
}
