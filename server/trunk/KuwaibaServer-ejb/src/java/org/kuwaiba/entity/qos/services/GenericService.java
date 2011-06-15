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
 *  under the License.
 */

package org.kuwaiba.entity.qos.services;

import java.util.List;
import org.kuwaiba.entity.core.AdministrativeItem;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import org.kuwaiba.core.annotations.NoSerialize;
import org.kuwaiba.entity.core.InventoryObject;

/**
 * Represents a simple service
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericService extends AdministrativeItem {
    protected String serviceId;
    @NoSerialize
    @OneToMany
    protected List<InventoryObject> directResources;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public List<InventoryObject> getDirectResources() {
        return directResources;
    }

    public void setDirectResources(List<InventoryObject> directResources) {
        this.directResources = directResources;
    }

}
