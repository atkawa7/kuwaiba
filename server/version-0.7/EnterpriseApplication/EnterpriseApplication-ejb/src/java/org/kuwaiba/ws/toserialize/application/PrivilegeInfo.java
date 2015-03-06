/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
import org.kuwaiba.apis.persistence.application.Privilege;

/**
 * Wrapper for entity class Privilege.
 * @author Adrian Fernando Martinez Molina <adrian.martinez@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class PrivilegeInfo implements Serializable{
    /**
     * 
     */
    private long id;
    /**
     * Privilege code 
     */
    private long code;
    /**
     * 
     */
    private String methodGroup;
    /**
     * 
     */
    private String methodName;
    /**
     * 
     */
    private String methodManager;
    /**
     * 
     */
    private long [] dependsOf;

    //No-arg constructor required
    public PrivilegeInfo() { }
    
    public PrivilegeInfo(Privilege privilege) {
        id=privilege.getId();
        code=privilege.getCode();
        methodGroup=privilege.getMethodGroup();
        methodName=privilege.getMethodName();
        methodManager=privilege.getMethodManager();
        dependsOf = privilege.getDependsOf();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getMethodGroup() {
        return methodGroup;
    }

    public void setMethodGroup(String methodGroup) {
        this.methodGroup = methodGroup;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodManager() {
        return methodManager;
    }

    public void setMethodManager(String methodManager) {
        this.methodManager = methodManager;
    }

    public long[] getDependsOf() {
        return dependsOf;
    }

    public void setDependsOf(long[] dependsOf) {
        this.dependsOf = dependsOf;
    }
}
