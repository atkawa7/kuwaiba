/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.inventory.customization.datamodelmanager;

import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * Data model manager Top component service.
 * @author Adrian Martinez Molina <charles.bedon@kuwaiba.org>
 */
public class DataModelManagerService {

    private DataModelManagerTopComponent dmmtc;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    LocalClassMetadataLight[] roots;

    public DataModelManagerService(DataModelManagerTopComponent dmmtc) {
        this.dmmtc = dmmtc;
    }
    
    public LocalClassMetadataLight[] getRoots() {
        roots = com.getAllLightMeta(true);
        return roots;
    }
    
    public LocalClassMetadataLight[] getRootChildren(){
        LocalClassMetadata inventoryObjectClass = com.getMetaForClass(Constants.CLASS_INVENTORYOBJECT, true);
        if(inventoryObjectClass == null){
            dmmtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            return new LocalClassMetadataLight[0];
        }
        
        LocalClassMetadata genericObjectListClass = com.getMetaForClass(Constants.CLASS_GENERICOBJECTLIST, true);
        
        if(genericObjectListClass == null) {
            dmmtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            return new LocalClassMetadataLight[0];
        }
        
        return new LocalClassMetadataLight[]{genericObjectListClass, inventoryObjectClass};
    }
}
