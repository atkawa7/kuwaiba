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
package org.kuwaiba.ws.toserialize.business;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Instances of this class are proxies that represents the entities in the database. This is a wrapper of
 * the idem class in the Persistence Abstraction API
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD) //This annotation tell the serializer to include all fiels
                                      //no matter their modifier. Default takes only public ones
public class RemoteObject extends RemoteObjectLight {
    /**
     * Attribute names in this object. This information is already in the meta, but we don't know
     * if it's sorted correctly there, so we take it here too
     */
    private ArrayList<String> attributes;
    /**
     * Values for the aforementioned attributes
     */
    private ArrayList<ArrayList<String>> values;

    /**
     * Default constructor. Never used
     */
    private RemoteObject(){}

    /**
     *
     * @param object The object to be serialized
     */
    public RemoteObject(org.kuwaiba.apis.persistence.business.RemoteObject object){
        super(object.getId(), object.getClassName(), object.isLocked());
        attributes = new ArrayList<String>(object.getAttributes().size());
        values = new ArrayList<ArrayList<String>>(object.getAttributes().size());
        for (String key : object.getAttributes().keySet()){
            attributes.add(key);
            values.add(object.getAttributes().get(key));
        }
    }

    public ArrayList<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<String> attributes) {
        this.attributes = attributes;
    }

    public ArrayList<ArrayList<String>> getValues() {
        return values;
    }

    public void setValues(ArrayList<ArrayList<String>> values) {
        this.values = values;
    }
}
