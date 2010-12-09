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
 */

package entity.equipment.applicationlayer;

import entity.core.ConfigurationItem;
import entity.multiple.systems.OperatingSystem;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;

/**
 * This is a generic application layer equipment typically a sort of computer
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class GenericApplicationElement extends ConfigurationItem{
    protected String service;
    
    @OneToOne
    protected OperatingSystem operatingSystem;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

}
