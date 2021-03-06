/**
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

import java.util.HashMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.metadata.GenericObjectList;

/**
 * Wrapper of GenericObjectList
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class ObjectList {
    /**
     * Class representing the list type (Vendor, AntennaType, etc)
     */
    private String className;
    /**
     * List type display name
     */
    private String displayName;
    /**
     * 1 for Many to One<br/>
     * 2 for Many to Many
     */
    private int type;
    /**
     * Items
     */
    private HashMap<Long,String> list;

    public ObjectList() {
    }

    public ObjectList(GenericObjectList listType){
        this.className = listType.getClassName();
        this.type = listType.getType();
        this.displayName = listType.getDislayName();
        this.list = new HashMap<Long, String>();
        for (org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight item : listType.getList())
            this.list.put(item.getId(), item.getName());
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public HashMap<Long, String> getList() {
        return list;
    }

    public void setList(HashMap<Long, String> list) {
        this.list = list;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
