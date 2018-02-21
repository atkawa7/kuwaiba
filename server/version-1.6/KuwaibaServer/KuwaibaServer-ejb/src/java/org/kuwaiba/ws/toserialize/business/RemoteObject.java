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
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;

/**
 * Instances of this class are proxies that represents the entities in the database. This is a wrapper of
 * the idem class in the Persistence Abstraction API
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteObject implements Serializable {
    /**
     * Object's id
     */
    private long oid;
    /**
     * Object's class
     */
    private String className;
    /**
     * Attribute names in this object. This information is already in the meta, but we don't know
     * if it's sorted correctly there, so we take it here too
     */
    private String[] attributes;
    /**
     * Values for the aforementioned attributes
     */
    private String[][] values;

    /**
     * Default constructor. Never used
     */
    private RemoteObject(){}

    /**
     *
     * @param object The object to be serialized
     */
    public RemoteObject(RemoteBusinessObject object){
        this.oid = object.getId();
        this.className = object.getClassName();
        attributes = new String[object.getAttributes().size()];
        values = new String[object.getAttributes().size()][];
        int i = 0;
        for (String key : object.getAttributes().keySet()){
            attributes[i] = key;
            values[i] = object.getAttributes().get(key).toArray(new String[0]);
            i++;
        }
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void setAttributes(String[] attributes) {
        this.attributes = attributes;
    }

    public String[][] getValues() {
        return values;
    }

    public void setValues(String[][] values) {
        this.values = values;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public static List<RemoteObject> toRemoteObjectArray(List<RemoteBusinessObject> toBeWrapped){
        if (toBeWrapped == null)
            return null;

        List<RemoteObject> res = new ArrayList<>();
        
        for (RemoteBusinessObject rawObject : toBeWrapped)
            res.add(new RemoteObject(rawObject));
        
        return res;
    }
}
