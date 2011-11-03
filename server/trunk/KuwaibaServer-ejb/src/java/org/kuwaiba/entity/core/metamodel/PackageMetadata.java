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
package org.kuwaiba.entity.core.metamodel;

import org.kuwaiba.entity.core.MetadataObject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;

/**
 * This class represents the different packages used to hold the entity classes. From the application
 * point of view, it's like a category, and will be used later, when the data model goes dynamic
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@NamedQuery(name="flushPackageMetadata", query="DELETE FROM PackageMetadata x")
public class PackageMetadata extends MetadataObject{

    private String displayName;
    @Column(length=500)
    private String description;

    public PackageMetadata() {
    }

    public PackageMetadata(String _name,String _displayName, String _description){
        this.name = _name;
        this.displayName = _displayName;
        this.description = _description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Retrieves the current default display name
     * @return the current display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the current default display name
     * @param displayName new display name to be set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
