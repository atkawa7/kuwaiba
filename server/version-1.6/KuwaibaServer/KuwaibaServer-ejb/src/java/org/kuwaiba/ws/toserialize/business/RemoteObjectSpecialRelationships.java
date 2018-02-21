/*
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
package org.kuwaiba.ws.toserialize.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;

/**
 * Wraps the special relationships of an object so a Hashmap doesn't have to be used
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteObjectSpecialRelationships implements Serializable {
    private List<String> relationships;
    private List<RemoteObjectLightList> relatedObjects;

    public RemoteObjectSpecialRelationships() { }

    public RemoteObjectSpecialRelationships(HashMap<String, List<RemoteBusinessObjectLight>> relationships) {
        this.relationships = new ArrayList<>();
        this.relatedObjects = new ArrayList<>();

        for (String relationshipName : relationships.keySet()){
            this.relationships.add(relationshipName);
            this.relatedObjects.add(new RemoteObjectLightList(RemoteObjectLight.toRemoteObjectLightArray(relationships.get(relationshipName))));
        }
    }
    public List<String> getRelationships() {
        return relationships;
    }

    public List<RemoteObjectLightList> getRelatedObjects() {
        return relatedObjects;
    }
}
