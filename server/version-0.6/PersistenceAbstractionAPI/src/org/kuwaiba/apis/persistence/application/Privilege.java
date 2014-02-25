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

package org.kuwaiba.apis.persistence.application;

import java.io.Serializable;

/**
 * Codes assigned to the different available privileges
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Privilege implements Serializable{
    /**
     * User/ group can login into the application. It's a dummy permission
     */
    public static final int PRIVILEGE_LOGIN = 0;
    /**
     * User/group can create objects
     */
    public static final int PRIVILEGE_CREATE_OBJECT = 1;
    /**
     * User/group can create classes
     */
    public static final int PRIVILEGE_CREATE_CLASS = 2;
    /**
     * Privilege id
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

    public Privilege(long code, String methodGroup, String methodName, String methodManager, long[] dependsOf) {
        this.code = code;
        this.methodGroup = methodGroup;
        this.methodName = methodName;
        this.methodManager = methodManager;
        this.dependsOf = dependsOf;
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
