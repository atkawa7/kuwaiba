/**
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

package org.kuwaiba.apis.persistence.integrity;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author adrian
 */
public class LocalClassWrapper {

    public static int MODIFIER_DUMMY = 1;
    public static int MODIFIER_NOCOUNT = 2;

    private String name;
    private int javaModifiers;
    private int applicationModifiers;
    private int classType;
    private String classPackage;
    private List<LocalClassWrapper> directSubClasses;
    private List<LocalAttributeWrapper> attributes;

    public LocalClassWrapper() {
        directSubClasses = new ArrayList<LocalClassWrapper>();
        attributes = new ArrayList<LocalAttributeWrapper>();
    }

    public int getApplicationModifiers() {
        return applicationModifiers;
    }

    public void setApplicationModifiers(int applicationModifiers) {
        this.applicationModifiers = applicationModifiers;
    }

    public List<LocalAttributeWrapper> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<LocalAttributeWrapper> attributes) {
        this.attributes = attributes;
    }

    public List<LocalClassWrapper> getDirectSubClasses() {
        return directSubClasses;
    }

    public void setDirectSubClasses(List<LocalClassWrapper> directSubClasses) {
        this.directSubClasses = directSubClasses;
    }

    public int getJavaModifiers() {
        return javaModifiers;
    }

    public void setJavaModifiers(int javaModifiers) {
        this.javaModifiers = javaModifiers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getClassType() {
        return classType;
    }

    public void setClassType(int classType) {
        this.classType = classType;
    }

    public boolean isDummy(){
        return (applicationModifiers & MODIFIER_DUMMY) == MODIFIER_DUMMY;
    }

    public boolean isCountable() {
        return (applicationModifiers & MODIFIER_NOCOUNT) != MODIFIER_NOCOUNT;
    }

    public String getClassPackage() {
        return classPackage;
    }

    public void setClassPackage(String classPackage) {
        this.classPackage = classPackage;
    }
    
    @Override
    public boolean equals(Object obj){
        if (obj == null)
        return false;
        if (!(obj instanceof LocalClassWrapper))
            return false;
        return ((LocalClassWrapper)obj).getName().equals(getName());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
