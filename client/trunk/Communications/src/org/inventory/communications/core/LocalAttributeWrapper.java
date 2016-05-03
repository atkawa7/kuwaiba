/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
 * 
 */

package org.inventory.communications.core;

import java.util.Random;

/**
 * Implementation of the interface LocalAttributeWrapper
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalAttributeWrapper {
    
    public static int MODIFIER_NOCOPY = 1;
    public static int MODIFIER_NOSERIALIZE = 2;
    public static int MODIFIER_READONLY = 4;
    
    /**
     * This prefix is used to identify a LocalAttributeWrapper as unique. This is 
     * necessary because when represented as a pin in the class hierarchy viewer, the 
     * "equals" method is called to check its uniqueness and only the name is not enough
     */
    private int prefix;
    private String name;
    private int javaModifiers;
    private int applicationModifiers = 0;
    private String type;

    public LocalAttributeWrapper() {
        this.prefix = new Random().nextInt(1000000);
    }

    public int getApplicationModifiers() {
        return applicationModifiers;
    }

    public void setApplicationModifiers(int applicationModifiers) {
        this.applicationModifiers = applicationModifiers;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPrefix() {
        return prefix;
    }

    public void setPrefix(int prefix) {
        this.prefix = prefix;
    }

    public boolean canCopy(){
        return (applicationModifiers & MODIFIER_NOCOPY) != MODIFIER_NOCOPY;
    }

    public boolean canWrite() {
        return (applicationModifiers & MODIFIER_READONLY) != MODIFIER_READONLY;
    }

    public boolean canSerialize(){
        return (applicationModifiers & MODIFIER_NOSERIALIZE) != MODIFIER_NOSERIALIZE;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null)
            return false;
        if (!(obj instanceof LocalAttributeWrapper))
            return false;
        return (((LocalAttributeWrapper)obj).getName() + ((LocalAttributeWrapper)obj).getPrefix()).equals(getName()+getPrefix());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
