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

package org.kuwaiba.apis.persistence;

import java.io.Serializable;

/**
 * Contains a business object basic information
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class RemoteObjectLight implements Serializable{

    public static final String PROPERTY_ID = "id"; //NOI18N
    public static final String PROPERTY_NAME = "name"; //NOI18N
    public static final String PROPERTY_DYSPLAY_NAME = "displayName"; //NOI18N

    private Long id;
    private String name;
    private String displayName;

    
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
