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
package org.kuwaiba.interfaces.ws.toserialize.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.application.TemplateObject;
import org.kuwaiba.apis.persistence.business.BusinessObject;
import org.kuwaiba.apis.persistence.util.StringPair;

/**
 * Instances of this class are proxies that represents the entities in the database. This is a wrapper of
 * the idem class in the Persistence Abstraction API
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteObject extends RemoteObjectLight implements Serializable {
    /**
     * A key-value dictionary with the attributes and values of the current object. 
     * Take into account that if an attribute does not appear in this list, it's because
     * it's value is null
     */
    private List<StringPair> attributes;
    

    /**
     * Default constructor. To be used only by the WS layer provider
     */
    public RemoteObject(){}

    /**
     *
     * @param object The object to be serialized
     */
    public RemoteObject(BusinessObject object) {
        super(object.getClassName(), object.getId(), object.getName());
        this.attributes = new ArrayList<>();
        
        for (String attribute : object.getAttributes().keySet())
            attributes.add(new StringPair(attribute, object.getAttributes().get(attribute)));
    }
    
    public RemoteObject(TemplateObject object) {
        super(object.getClassName(), object.getId(), object.getName());
        this.attributes = new ArrayList<>();
        
        for (String attribute : object.getAttributes().keySet())
            attributes.add(new StringPair(attribute, object.getAttributes().get(attribute)));
    }

    public List<StringPair> getAttributes() {
        return attributes;
    }
    
    public String getAttribute(String attributeName) {
        for (StringPair attribute : attributes) {
            if (attribute.getKey().equals(attributeName))
                return attribute.getValue();
        }
        
        return null;
    }

    public void setAttributes(List<StringPair> attributes) {
        this.attributes = attributes;
    }

    public static List<RemoteObject> toRemoteObjectArray(List<BusinessObject> toBeWrapped){
        if (toBeWrapped == null)
            return null;

        List<RemoteObject> res = new ArrayList<>();
        
        for (BusinessObject rawObject : toBeWrapped)
            res.add(new RemoteObject(rawObject));
        
        return res;
    }
        
    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int compareTo(RemoteObjectLight o) {
        return super.compareTo(o);
    }
}
