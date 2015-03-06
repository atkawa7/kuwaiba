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

package org.inventory.customization.listmanager;

import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * Provides business logic to the associated TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ListManagerService {
    private CommunicationsStub com;
    private ListManagerTopComponent lmtc;

    public ListManagerService(ListManagerTopComponent _lmtc) {
        this.com = CommunicationsStub.getInstance();
        this.lmtc = _lmtc;
    }

    public LocalClassMetadataLight[] getInstanceableListTypes(){
        LocalClassMetadataLight[] res = com.getInstanceableListTypes();
        if (res == null){
            lmtc.getNotifier().showSimplePopup("Tree Creation", NotificationUtil.ERROR_MESSAGE, com.getError());
            return new LocalClassMetadataLight[0];
        }
        return res;
    }

    void refreshLists() {
        com.refreshCache(false, false, true, false);
    }
}
