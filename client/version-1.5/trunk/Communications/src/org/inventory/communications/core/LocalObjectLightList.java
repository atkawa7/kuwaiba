/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.communications.core;

import java.util.ArrayList;
import org.inventory.communications.wsclient.RemoteBusinessObjectLight;
import org.inventory.communications.wsclient.RemoteBusinessObjectLightList;

/**
 * A list of localobjectlight lists
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalObjectLightList extends ArrayList<LocalObjectLight> {

    public LocalObjectLightList(RemoteBusinessObjectLightList objectChain) {
        for (RemoteBusinessObjectLight anElement : objectChain.getList()) 
            add(new LocalObjectLight(anElement.getId(), anElement.getName(), anElement.getClassName()));
    }
}
