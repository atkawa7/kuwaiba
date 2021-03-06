/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
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
        List<LocalObjectLight> customersPools = com.getPools(Constants.CLASS_GENERICCUSTOMER);
        LocalObjectLight[] customers = com.getObjectsOfClassLight(Constants.CLASS_GENERICCUSTOMER);
                
        if (customers == null)
            this.smtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else{
            List<LocalObjectLight> serviceManagerNodes = new ArrayList<LocalObjectLight>();
            serviceManagerNodes.addAll(Arrays.asList(customers)); 
            for (LocalObjectLight customersPool : customersPools){
                List<LocalObjectLight> poolItems = com.getPoolItems(customersPool.getOid());
                for(LocalObjectLight customer : customers){
                    if(poolItems.contains(customer)){
                        serviceManagerNodes.remove(customer);
                    }
                }
                serviceManagerNodes.add(customersPool);
            }
            smtc.getExplorerManager().setRootContext(new ServiceManagerRootNode(serviceManagerNodes.toArray(new LocalObjectLight[serviceManagerNodes.size()])));
        }
    }
    
}
