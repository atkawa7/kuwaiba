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

import com.neotropic.inventory.modules.cpe.nodes.EvlanPoolNode;
import com.neotropic.inventory.modules.cpe.nodes.EvlanPoolNode.EvlanPoolChildren;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.util.Utilities;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CreateEvlanAction extends GenericInventoryAction {
    
    public CreateEvlanAction() {
        putValue(NAME, I18N.gm("modules.cpe.nodes.actions.CreateEvlanAction"));
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
        LocalObjectLight poolItem = CommunicationsStub.getInstance().createPoolItem(evlanPoolNode.getPool().getId(), Constants.CLASS_EVLAN);
        if (poolItem != null) {
            LocalObjectLight egvlan = CommunicationsStub.getInstance().createObject(Constants.CLASS_EGVLAN, poolItem.getClassName(), poolItem.getId(), new HashMap(), "462502cf-c492-4b42-b9a4-4cec3c1c24af");
            if (egvlan != null) {
                ((EvlanPoolChildren) evlanPoolNode.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                    NotificationUtil.INFO_MESSAGE, CommunicationsStub.getInstance().getError());
            }
            else {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
        else {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
}
