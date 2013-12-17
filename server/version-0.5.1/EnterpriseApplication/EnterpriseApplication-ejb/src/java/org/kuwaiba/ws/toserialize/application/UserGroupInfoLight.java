/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.application.GroupProfile;

/**
 * Wrapper for entity class UserGroup. This light version has the basic info necessary
 * to display in a table or list. Members of each group can be retrieved querying for a UserGroupInfo object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UserGroupInfoLight implements Serializable {
    /**
     * Object id
     */
    protected long oid;
    /**
     * UserGroup's name
     */
    protected String name;

    public UserGroupInfoLight(){}

    public UserGroupInfoLight(GroupProfile group){
        this.oid = group.getId();
        this.name = group.getName();
    }

    public long getOid() {
        return oid;
    }

    public void setId(long _oid) {
        this.oid = _oid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
