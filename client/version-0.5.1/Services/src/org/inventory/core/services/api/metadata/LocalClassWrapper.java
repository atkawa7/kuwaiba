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

package org.inventory.core.services.api.metadata;

import java.util.List;

/**
 * This class wraps a class within the data model and it's aimed to be used only for visualization
 * purposes in applications such as the class hierarchy viewer (see module ClassManager). Do mistake this
 * for LocalClassMetadata, which represents the information related to attribute display names, visibility,
 * containment hierarchy and the like. This one is a bare representation of a class
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface LocalClassWrapper {

    public final static int TYPE_ROOT = 0;
    public final static int TYPE_INVENTORY = 1;
    public final static int TYPE_APPLICATION = 2;
    public final static int TYPE_METADATA = 3;
    public final static int TYPE_OTHER = 4;

    public static int MODIFIER_DUMMY = 1;
    public static int MODIFIER_NOCOUNT = 2;

    public int getApplicationModifiers();
    public void setApplicationModifiers(int applicationModifiers);
    public List<LocalAttributeWrapper> getAttributes();
    public void setAttributes(List<LocalAttributeWrapper> attributes);
    public List<LocalClassWrapper> getDirectSubClasses();
    public void setDirectSubClasses(List<LocalClassWrapper> directSubClasses);
    public int getJavaModifiers();
    public void setJavaModifiers(int javaModifiers);
    public String getName();
    public void setName(String name);
    public int getClassType();
    public void setClassType(int classType);
    public boolean isDummy();
    public boolean isCountable();
}