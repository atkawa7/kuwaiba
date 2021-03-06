/*
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

package org.kuwaiba.apis.persistence.business;

import java.util.HashMap;
import java.util.List;

/**
 * Contains a business object detailed information
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class RemoteObject extends RemoteObjectLight {

    /**
     * Map of attributes and values. Note that there's a little of overhead here, since
     * the attribute value could be a list of values (many-to-many, one-to-many relationships)
     */
    private HashMap <String, List<String>> attributes;

    public RemoteObject(long id, String name, String className) {
        super(id, name, className);
    }

    public RemoteObject(String className, long id, String name, HashMap<String, List<String>> attributes) {
        setId(id);
        setName(name);
        setClassName(className);
        this.attributes = attributes;
    }


    public HashMap<String, List<String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, List<String>> attributes) {
        this.attributes = attributes;
    }
}
