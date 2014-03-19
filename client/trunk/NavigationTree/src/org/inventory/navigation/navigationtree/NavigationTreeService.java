/*
 *  Copyright 2010 - 2013 Neotropic SAS <contact@neotropic.co>.
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
 *  under the License.
 */
package org.inventory.navigation.navigationtree;

import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * Provides the business logic for the related TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class NavigationTreeService {
    private NavigationTreeTopComponent component;
    private CommunicationsStub com = CommunicationsStub.getInstance();

    public NavigationTreeService(NavigationTreeTopComponent component){
        this.component = component;
    }
    
    public LocalObjectLight[] getRootChildren(){
        List<LocalObjectLight> rootChildren = com.getObjectChildren(-1, -1);
        if(rootChildren != null)
            return rootChildren.toArray(new LocalObjectLight[0]);
        else{
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            return new LocalObjectLight[0];
        }
    }
}