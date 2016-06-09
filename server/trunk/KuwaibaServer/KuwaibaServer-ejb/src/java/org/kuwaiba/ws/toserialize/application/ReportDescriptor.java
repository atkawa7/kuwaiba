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
package org.kuwaiba.ws.toserialize.application;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * A class containing the most basic information about a report
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */

@XmlAccessorType(XmlAccessType.FIELD)
public final class ReportDescriptor implements Serializable {
    /**
     * Report id
     */
    private long id;
    /**
     * Report name
     */
    private String name;
    /**
     * Class this report is related to. Null if it's not a class report
     */
    private String className;
    /**
     * Report description
     */
    private String description;
    

    //No-arg constructor required
    public ReportDescriptor() {   }

    public ReportDescriptor(long id, String name, String className, String description) {
        this.id = id;
        this.name = name;
        this.className = className;
        this.description = description;
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    
}
