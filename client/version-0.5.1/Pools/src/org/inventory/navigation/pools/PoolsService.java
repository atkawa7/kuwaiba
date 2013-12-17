/**
 * Copyright 2010 - 2013 Neotropic SAS <contact@neotropic.co>.
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

import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Lookup;

/**
 * Business logic associated to the PoolsTopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
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
    
    public LocalObjectLight[] getRootChildren(){
        List<LocalObjectLight> rootChildren = com.getPools();
        if(rootChildren != null)
            return rootChildren.toArray(new LocalObjectLight[0]);
        else{
            NotificationUtil nu = Lookup.getDefault().
                lookup(NotificationUtil.class);
            if (nu == null)
                System.out.println(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("DBG_CREATION_ERROR")+com.getError());
            else
                nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_TITLE_CREATION"), NotificationUtil.ERROR, com.getError());
            return null;
        }
    }
        
    void refresh() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
}
