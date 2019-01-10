/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.warehouses.nodes;

import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WarehouseChildren extends Children.Keys<LocalPool> {
    
    @Override
    public void addNotify() {
        LocalObjectLight warehouse = ((WarehouseNode) this.getNode()).getObject();
        
        List<LocalPool> warehousePools = CommunicationsStub.getInstance().
            getPoolsInObject(warehouse.getClassName(), warehouse.getId(), Constants.CLASS_INVENTORYOBJECT);
        
        if (warehousePools == null) {
            setKeys(Collections.EMPTY_LIST);
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        } else {
            Collections.sort(warehousePools);
            setKeys(warehousePools);
        }
    }
    
    @Override
    public void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }
    
    @Override
    protected Node[] createNodes(LocalPool localPool) {
        return new Node[] {new WarehousePoolNode(localPool)};
    }
    
}
