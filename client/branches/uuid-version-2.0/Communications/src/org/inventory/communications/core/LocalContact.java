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
package org.inventory.communications.core;

import java.util.List;
import org.inventory.communications.wsclient.StringPair;


/**
 * Represents a contact in the inventory address book. Contacts (technical, commercial and executive) are always 
 * associated to a customer.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LocalContact extends LocalObject {
    /**
     * The company (customer) this contact belongs to
     */
    private LocalObjectLight customer;

    public LocalContact(String className, String id, List<StringPair> attributes, LocalClassMetadata classMetadata, LocalObjectLight customer) throws IllegalArgumentException {
        super(className, id, attributes, classMetadata);
        this.customer = customer;
    }

    public LocalObjectLight getCustomer() {
        return customer;
    }
}
