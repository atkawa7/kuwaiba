/*
 * Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.navigation.pools;

import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPool;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * Business logic associated to the PoolsTopComponent
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class PoolsService {
    
    /**
     * Reference to the main TC
     */
    private PoolsTopComponent pstc;
    private CommunicationsStub com;

    public PoolsService(PoolsTopComponent pstc) {
        this.pstc = pstc;
        com = CommunicationsStub.getInstance();
    }
    
    public List<LocalPool> getRootChildren(){
        List<LocalPool> rootChildren = com.getRootPools(null, LocalPool.POOL_TYPE_GENERAL_PURPOSE, false);
        if(rootChildren != null)
            return rootChildren;
        else {
            pstc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            return new ArrayList<>();
        }
    }
}
