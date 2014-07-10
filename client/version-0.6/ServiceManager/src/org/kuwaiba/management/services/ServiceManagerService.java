/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.management.services;

import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.kuwaiba.management.services.nodes.ServiceManagerRootNode;

/**
 * Service Manager Service
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ServiceManagerService {
    private ServiceManagerTopComponent smtc;
    private CommunicationsStub com;

    public ServiceManagerService(ServiceManagerTopComponent smtc) {
        this.smtc = smtc;
        this.com = CommunicationsStub.getInstance();
    }
    
    public void setTreeRoot(){
        LocalObjectLight[] customers = CommunicationsStub.getInstance().getObjectsOfClassLight("GenericCustomer");
        if (customers == null)
            this.smtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
        else
            smtc.getExplorerManager().setRootContext(new ServiceManagerRootNode(customers));
    }
    
}
