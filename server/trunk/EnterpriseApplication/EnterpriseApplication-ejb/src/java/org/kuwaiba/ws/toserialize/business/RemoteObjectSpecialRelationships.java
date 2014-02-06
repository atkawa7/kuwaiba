/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;

/**
 * Wraps the special relationships of an object so a Hashmap doesn't have to be used
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteObjectSpecialRelationships implements Serializable {
    private String[] relationships;
    private RemoteObjectLight[][] relatedObjects;

    public RemoteObjectSpecialRelationships() { }

    public RemoteObjectSpecialRelationships(HashMap<String, List<RemoteBusinessObjectLight>> relationships) {
        this.relationships = new String[relationships.size()];
        this.relatedObjects = new RemoteObjectLight[relationships.size()][];
        //For some reason, it's not possible to 
        int i = 0;
        for (String relationship : relationships.keySet()){
            this.relationships[i] = relationship;
            this.relatedObjects[i] = RemoteObjectLight.toRemoteObjectLightArray(relationships.get(relationship));
            i++;
        }
    }
    public String[] getRelationships() {
        return relationships;
    }

    public RemoteObjectLight[][] getRelatedObjects() {
        return relatedObjects;
    }

    public void setRelationships(String[] relationships) {
        this.relationships = relationships;
    }

    public void setRelatedObjects(RemoteObjectLight[][] relatedObjects) {
        this.relatedObjects = relatedObjects;
    }
}
