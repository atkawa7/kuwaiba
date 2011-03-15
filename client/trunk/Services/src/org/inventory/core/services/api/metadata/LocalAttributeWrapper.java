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

/**
 * This is a bare, local representation of an attribute  (aka field). It's used mainly to build
 * a class hierarchy tree, and it adds some extra attributes to the Field class, such as
 * application modifiers, which are gotten from custom annotations. Don't confuse with the entity
 * class AttributeMetadata (or its client side equivalent LocalAttribuetMetadata),
 * which holds information such as display names, descriptions, etc
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface LocalAttributeWrapper {
    public static int MODIFIER_NOCOPY = 1;
    public static int MODIFIER_NOSERIALIZE = 2;
    public static int MODIFIER_READONLY = 4;

    public int getApplicationModifiers();
    public void setApplicationModifiers(int applicationModifiers);
    public int getJavaModifiers();
    public void setJavaModifiers(int javaModifiers);
    public String getName();
    public void setName(String name);
    public String getType();
    public void setType(String type);
    public boolean canCopy();
    public boolean canWrite();
    public boolean canSerialize();
}