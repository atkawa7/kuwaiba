/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package core.toserialize;

import entity.config.UserGroup;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Wrapper for entity class UserGroup. This light version has the basic info necessary
 * to display in a table or list. Members of each group can be retrieve at UserGroupInfo
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UserGroupInfoLight {
    /**
     * Object id
     */
    protected Long oid;
    /**
     * Group's name
     */
    protected String name;

    public UserGroupInfoLight(){}

    public UserGroupInfoLight(UserGroup group){
        this.oid = group.getId();
        this.name = group.getName();
    }

    public Long getOid() {
        return oid;
    }

    public void setId(Long _oid) {
        this.oid = _oid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
