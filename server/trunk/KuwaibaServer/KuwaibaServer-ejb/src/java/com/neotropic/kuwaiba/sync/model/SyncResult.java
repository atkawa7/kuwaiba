/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.neotropic.kuwaiba.sync.model;

/**
 * This class represents a single result from comparing the info from a sync data 
 * source and the corresponding information in the inventory database
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SyncResult {
    /**
     * The type of difference found. See EVENT enumeration for possible values
     */
    private EVENT type;
    /**
     * Textual description of the difference
     */
    private String description;
    /**
     * Relevant information that can be used to . Although its format depends on every 
     * particular implementation, a JSON/YML format is suggested
     */
    private String extraInformation;

    public EVENT getType() {
        return type;
    }

    public void setType(EVENT type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExtraInformation() {
        return extraInformation;
    }

    public void setExtraInformation(String extraInformation) {
        this.extraInformation = extraInformation;
    }
    
    public enum EVENT {
        NEW,
        DELETED,
        UPDATED,
        PARTIALLY_UPDATED
    }
}
