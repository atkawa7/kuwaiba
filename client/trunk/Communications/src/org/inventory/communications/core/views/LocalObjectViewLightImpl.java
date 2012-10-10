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

package org.inventory.communications.core.views;

import org.inventory.core.services.api.visual.LocalObjectViewLight;

/**
 * Light version of LocalObjectView
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalObjectViewLightImpl implements LocalObjectViewLight {

    /**
     * View id
     */
    private long id;
    /**
     * View type
     */
    private int type;
    /**
     * View name
     */
    private String name;
    /**
     * View description
     */
    private String description;

    public LocalObjectViewLightImpl(){}

    public LocalObjectViewLightImpl(long id, String name, String description, int type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getViewType() {
        return type;
    }

    public void setViewType(int type) {
        this.type = type;
    }

    @Override
    public String toString(){
        if(name != null)
            return name;
        return "";
    }
}
