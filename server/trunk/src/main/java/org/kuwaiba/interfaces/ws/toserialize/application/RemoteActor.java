/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.interfaces.ws.toserialize.application;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Wrapper of {@link org.kuwaiba.apis.persistence.application.process.Actor}. An actor is someone (a person, organizational unit or external agent, like a customer) 
 * who is responsible to cary on with an activity.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteActor implements Serializable {

    public RemoteActor() { }
    
    /**
     * The id of the actor. If it is an external agent, this value will be -1
     */
    private long id;
    /**
     * Alias used to identify the actor. Most of the times, it will be the name of the user/group
     */
    private String name;
    /**
     * Actor type. See Actor.TYPE_* for valid values
     */
    private int type;

    public RemoteActor(long id, String name, int type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteActor other = (RemoteActor) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
        
    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
