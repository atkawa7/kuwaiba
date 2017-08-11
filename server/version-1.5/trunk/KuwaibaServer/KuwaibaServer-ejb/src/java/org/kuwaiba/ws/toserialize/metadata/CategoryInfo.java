/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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

package org.kuwaiba.ws.toserialize.metadata;

import java.io.Serializable;

/**
 * This is a wrapper class for CategoryMetadata, containing the info required for the clients
 * to render the object category in the right way
 *
 * @author Adrian Martinez Molina <adrian.martinez@gmail.com>
 */
public class CategoryInfo implements Serializable {

     /**
     * Category name
     */
    private String name;
    /**
     * Category display name
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

    public CategoryInfo() {
    }

    public CategoryInfo(String name, String displayName, String description,
                        long creationDate) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.creationDate = creationDate;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

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
    
}
