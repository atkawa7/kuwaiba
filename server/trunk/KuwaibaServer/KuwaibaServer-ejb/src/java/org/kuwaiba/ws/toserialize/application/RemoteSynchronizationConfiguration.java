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
package org.kuwaiba.ws.toserialize.application;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.ws.todeserialize.StringPair;

/**
 * Wrapper of SynchronizationGroup
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */

@XmlAccessorType(XmlAccessType.FIELD)
public final class RemoteSynchronizationConfiguration implements Serializable {
    /**
     * The list of configuration parameters
     */ 
    private List<StringPair> parameters;

    public RemoteSynchronizationConfiguration() { }

    public RemoteSynchronizationConfiguration(List<StringPair> parameters) {
        this.parameters = parameters;
    }

    public List<StringPair> getParameters() {
        return parameters;
    }

    public void setParameters(List<StringPair> parameters) {
        this.parameters = parameters;
    }
}
