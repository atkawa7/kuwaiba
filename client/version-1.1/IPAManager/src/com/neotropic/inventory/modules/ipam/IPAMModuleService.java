/**
 * Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.ipam;

import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPool;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * IPAM service for top component 
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class IPAMModuleService {
    
    private CommunicationsStub com;
    private IPAMModuleTopComponent ipamtc;

    IPAMModuleService(IPAMModuleTopComponent ipamtc) {
        this.ipamtc = ipamtc;
        com = CommunicationsStub.getInstance();
    }
    
    public LocalPool[] getRootChildren(){
        List<LocalPool> rootChildren = com.getSubnetPools(-1, null);
        if(rootChildren != null)
            return rootChildren.toArray(new LocalPool[0]);
        else{
            ipamtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            return new LocalPool[0];
        }
    }
}
