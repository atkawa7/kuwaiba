/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

/**
 * Light version of LocalObjectView. Reference implementation
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalObjectViewLight {

    /**
     * Id for a default view
     */
    public static final int TYPE_DEFAULT = 0;

    /**
     * View id
     */
    protected long id;
    /**
     * View name
     */
    protected String name;
    /**
     * View description
     */
    protected String description;
    /**
     * View class
     */
    protected String className;

    public LocalObjectViewLight(long id, String name, String description, String className) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.className = className;
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

    public String getClassName() {
        return className;
    }

    public void setType(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return name;
    }
}
