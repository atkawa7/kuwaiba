/**
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
package org.kuwaiba.apis.persistence.metadata;


/**
 * Represents a category. It's used mostly to group the classes
 * into categories so it's easier to document
 * @author Adrian Fernando Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class CategoryMetadata {

    /**
     * Category name
     */
    private String name;
    /**
     * Category Displayname
     */
    private String displayName;
    /**
     * category description
     */
    private String description;
    /**
     * category creation date
     */
    private long creationDate;

    // <editor-fold defaultstate="collapsed" desc="getters and setters methods. Click on the + sign on the left to edit the code.">
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }// </editor-fold>
    
}
