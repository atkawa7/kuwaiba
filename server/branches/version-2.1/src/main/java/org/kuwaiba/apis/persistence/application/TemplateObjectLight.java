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

package org.kuwaiba.apis.persistence.application;

import org.kuwaiba.apis.persistence.business.BusinessObjectLight;

/**
 * Instances of this class represent a simplified representation of an inventory object defined within a template. 
 * This is actually very similar, if not identical, to #{@link BusinessObjectLight}, the only 
 * difference is that these objects don't exist, they are used as templates to create actual inventory objects. Also,
 * validators are not supported
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class TemplateObjectLight implements Comparable<TemplateObjectLight> {
    /**
     * The id of the template object
     */
    private String id;
    /**
     * The name of the template object
     */
    private String name;
    /**
     * The class the object is instance of
     */
    private String className;

    /**
     * Default constructor
     * @param id The id of the template object
     * @param name The name of the template object
     * @param className The class the object is instance of
     */
    public TemplateObjectLight(String className, String id, String name) {
        this.id = id;
        this.name = name;
        this.className = className;
    } 

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public int compareTo(TemplateObjectLight o) {
        return o.getName().compareTo(name);
    }
}
