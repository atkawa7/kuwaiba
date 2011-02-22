/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

import org.inventory.core.services.interfaces.LocalAttributeWrapper;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the interface LocalAttributeWrapper
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=LocalAttributeWrapper.class)
public class LocalAttributeWrapperImpl implements LocalAttributeWrapper{
    private String name;
    private int javaModifiers;
    private int applicationModifiers = 0;
    private String type;

    public LocalAttributeWrapperImpl() {}


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

    public boolean canCopy(){
        return (applicationModifiers & MODIFIER_NOCOPY) != MODIFIER_NOCOPY;
    }

    public boolean canSerialize(){
        return (applicationModifiers & MODIFIER_NOSERIALIZE) != MODIFIER_NOSERIALIZE;
    }
}
