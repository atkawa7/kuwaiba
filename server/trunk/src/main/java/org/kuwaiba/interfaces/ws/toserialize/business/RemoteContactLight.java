/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.business.ContactLight;

/**
 * Wrapper of ContactLight
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteContactLight implements Serializable {
    /**
     * Contact id
     */
    private long id;
    /**
     * Contact name
     */
    private String name;
    /**
     * Contact class name
     */
    private String className;
    /**
     * Customer this contact is associated to
     */
    private RemoteObjectLight customer;
    
    /**
     * Default constructor. Not used
     */
    protected RemoteContactLight(){}

    public RemoteContactLight(String className, long id, String name) {
        this.id = id;
        this.name = name;
        this.className = className;
    }


    public RemoteContactLight(ContactLight contact){
        this.className = contact.getClassName();
        this.name = contact.getName();
        this.id = contact.getId();
        this.customer = new RemoteObjectLight(contact.getCustomer());
    }

    public String getClassName() {
        return className;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<RemoteContactLight> toContactLightArray(List<ContactLight> toBeWrapped){
        if (toBeWrapped == null)
            return null;

        List<RemoteContactLight> res = new ArrayList<>();
        for (ContactLight aContactLight: toBeWrapped)
            res.add(new RemoteContactLight(aContactLight));

        return res;
    }

    public RemoteObjectLight getCustomer() {
        return customer;
    }

    public void setCustomer(RemoteObjectLight customer) {
        this.customer = customer;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 83 * hash + Objects.hashCode(this.name);
        return hash;
    }
    
    @Override
    public boolean equals (Object obj) {
        return obj instanceof RemoteContactLight && ((RemoteContactLight)obj).getId() == id;
    }
    
    @Override
    public String toString() {
        return String.format("%s [%s]", name, className);
    }
}