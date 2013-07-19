/**
 *  Copyright 2010, 2011, 2012, 2013 Neotropic SAS <contact@neotropic.co>
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

import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.Constants;
import org.openide.nodes.AbstractNode;

/**
 * Data model manager Top component service.
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class DataModelManagerService {

    private DataModelManagerTopComponent dmmtc;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    private AbstractNode attributeRoot;

    public DataModelManagerService(DataModelManagerTopComponent dmmtc) {
        this.dmmtc = dmmtc;
    }
    
    public LocalClassMetadataLight[] getRootChildren(){
        List<LocalClassMetadataLight> rootChildren = new ArrayList<LocalClassMetadataLight>(); 
        LocalClassMetadata inventoryObjectClass = com.getMetaForClass(Constants.CLASS_INVENTORYOBJECT, true);
        LocalClassMetadata genericObjectListClass = com.getMetaForClass(Constants.CLASS_GENERICOBJECTLIST, true);
        
        if(inventoryObjectClass == null){
            dmmtc.getNotifier().showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/customization/datamodelmanager/Bundle").getString("LBL_TITLE_CREATION"), 
                    NotificationUtil.ERROR, 
                    String.format(java.util.ResourceBundle.getBundle("org/inventory/customization/datamodelmanager/Bundle").getString("DBG_CLASSNOTFOUND_ERROR"), Constants.CLASS_INVENTORYOBJECT));
            return rootChildren.toArray(new LocalClassMetadataLight[0]);
        }
        if(genericObjectListClass == null) {
            dmmtc.getNotifier().showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/customization/datamodelmanager/Bundle").getString("LBL_TITLE_CREATION"), 
                    NotificationUtil.ERROR, 
                    String.format(java.util.ResourceBundle.getBundle("org/inventory/customization/datamodelmanager/Bundle").getString("DBG_CLASSNOTFOUND_ERROR"), Constants.CLASS_GENERICOBJECTLIST));
            return rootChildren.toArray(new LocalClassMetadataLight[0]);
        }
        
        rootChildren.add(inventoryObjectClass);
        rootChildren.add(genericObjectListClass);
        return rootChildren.toArray(new LocalClassMetadataLight[0]);
    }
}
