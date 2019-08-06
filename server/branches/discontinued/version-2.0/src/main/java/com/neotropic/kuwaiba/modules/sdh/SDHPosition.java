/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * It's a simple class representing a single position used by a container within a transport link
 * @author Charles Edward Bedon Cortazar<charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SDHPosition implements Serializable {
    /**
     * Id of the connection being used (a TransportLink or a ContainerLink)
     */
    private long connectionId;
    /**
     * Id of the connection being used (a TransportLink or a ContainerLink)
     */
    private String connectionClass;
    /**
     * Actual position (STM timeslot or VC4 timeslot)
     */
    private int position;

    public SDHPosition() {    } //Required by JAX-WS
    
    public SDHPosition(String connectionClass, long connectionId, int position) {
        this.connectionId = connectionId;
        this.connectionClass = connectionClass;
        this.position = position;
    }

    public long getLinkId() {
        return connectionId;
    }

    public void setLinkId(long connectionId) {
        this.connectionId = connectionId;
    }

    public String getLinkClass() {
        return connectionClass;
    }

    public void setLinkClass(String linkClass) {
        this.connectionClass = linkClass;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
