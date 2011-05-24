/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.customization.attributecustomizer;

import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * Implements the business logic for the associated component
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class AttributeCustomizerService {
    AttributeCustomizerTopComponent actc;
    CommunicationsStub com = CommunicationsStub.getInstance();

    public AttributeCustomizerService(AttributeCustomizerTopComponent _actc) {
        this.actc = _actc;
    }

    public LocalClassMetadataLight[] getInstanceableMeta() {
        LocalClassMetadataLight[] allMeta = com.getAllLightMeta(true);
        if (allMeta == null){
            actc.getNotifier().showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/customization/attributecustomizer/Bundle").getString("LBL_TITLE_CREATION"),
                    NotificationUtil.ERROR, com.getError());
            return new LocalClassMetadata[0];
        }
        List<LocalClassMetadataLight> res = new ArrayList<LocalClassMetadataLight>();
        for (LocalClassMetadataLight lcm : allMeta)
            if(!(lcm.isAbstract() || lcm.getClassName().equals("DummyRoot")))
                res.add(lcm);
        return res.toArray(new LocalClassMetadataLight[0]);
    }

}
