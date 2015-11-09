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
package org.kuwaiba.management.software;

import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.kuwaiba.management.software.nodes.SoftwareManagerRootNode;

/**
 * Software Manager Service
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SoftwareManagerService {
    private SoftwareManagerTopComponent smtc;
    private CommunicationsStub com;

    public SoftwareManagerService(SoftwareManagerTopComponent smtc) {
        this.smtc = smtc;
        this.com = CommunicationsStub.getInstance();
    }
    
    public void setTreeRoot(){
        LocalObjectLight[] softwareAssets = com.getObjectsOfClassLight("GenericSoftwareAsset");
        if (softwareAssets == null)
            this.smtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else
            smtc.getExplorerManager().setRootContext(new SoftwareManagerRootNode(softwareAssets));
    }
}
