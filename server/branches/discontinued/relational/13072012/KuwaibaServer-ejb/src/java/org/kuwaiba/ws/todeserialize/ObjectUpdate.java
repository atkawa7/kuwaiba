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
package org.kuwaiba.ws.todeserialize;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;


/**
 * This class represents an update over an object
 *
 * TODO: Using a RemoteObject could be good instead of this as well
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD) //This annotation the serializer to include all
                                      //attributes no matter its access modifier (public, private, etc)
                                      //default only takes the public ones
public class ObjectUpdate {
    private Long oid;
    private String classname;
    private String[] updatedAttributes;
    private String[] newValues;

    public ObjectUpdate() {
    }
    
    public String getClassname() {
        return classname;
    }

    public void setNewValues(String[] newValues) {
        this.newValues = newValues;
    }

    public String[] getNewValues() {
        return newValues;
    }

    public Long getOid() {
        return oid;
    }

    public String[] getUpdatedAttributes() {
        return updatedAttributes;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }

    public void setUpdatedAttributes(String[] updatedAttributes) {
        this.updatedAttributes = updatedAttributes;
    }

}