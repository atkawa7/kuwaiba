/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
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

package org.kuwaiba.apis.persistence.metadata;

import java.util.List;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;

/**
 * This class represents a list type attribute (packing many list items) (people in charge of an equipment, antenna types, etc)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GenericObjectList {
    /**
     * Class id the items are instance of
     */
    private long id;
    /**
     * Class name the items are instance of
     */
    private String className;
    /**
     * List type display name
     */
    private String displayName;
    /**
     * Type of relationship:<br/>
     * 1 for Many To One
     * 2 for Many To Many
     */
    private int type;
    /**
     * Items in this list
     */
    private List<RemoteBusinessObjectLight> list;

    public GenericObjectList() {
    }
    
    public GenericObjectList(long id, String name) {
        this.id = id;
        this.className = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
  
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String dislayName) {
        this.displayName = dislayName;
    }

    public List<RemoteBusinessObjectLight> getList() {
        return list;
    }

    public void setList(List<RemoteBusinessObjectLight> list) {
        this.list = list;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
