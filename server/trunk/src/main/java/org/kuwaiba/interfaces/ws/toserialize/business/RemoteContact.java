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

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.business.Contact;
import org.kuwaiba.apis.persistence.util.StringPair;

/**
 * Wrapper of Contact
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteContact extends RemoteContactLight {
    /**
     * Contact attributes
     */
    private List<StringPair> properties;

    /**
     * Default constructor. Never used
     */
    private RemoteContact(){}

    /**
     * Default functional constructor
     * @param contact The object to be serialized
     */
    public RemoteContact(Contact contact){
        super(contact.getClassName(), contact.getId(), contact.getName());
        this.properties = contact.getProperties();
    }

    public List<StringPair> getProperties() {
        return properties;
    }

    public void setProperties(List<StringPair> properties) {
        this.properties = properties;
    }

    
}
