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
package com.neotropic.kuwaiba.modules.sdh;

import java.io.Serializable;
import java.util.List;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;

/**
  * Instances of this class define a tributary link
  * @author Charles Edward Bedon Cortazar<charles.bedon@kuwaiba.org>
*/
public class SDHTributaryLinkDefinition implements Serializable {
    /**
     * Link object
     */
    private RemoteBusinessObjectLight link;

    /**
     * The positions used by the container
     */
    private List<SDHPosition> positions;

    public SDHTributaryLinkDefinition(RemoteBusinessObjectLight link, List<SDHPosition> positions) {
        this.link = link;
        this.positions = positions;
    }       

    public RemoteBusinessObjectLight getContainerName() {
        return link;
    }

    public void setContainerName(RemoteBusinessObjectLight link) {
        this.link = link;
    }

    public List<SDHPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<SDHPosition> positions) {
        this.positions = positions;
    }
}
