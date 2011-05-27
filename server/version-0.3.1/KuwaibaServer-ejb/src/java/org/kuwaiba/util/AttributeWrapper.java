/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package org.kuwaiba.util;

import org.kuwaiba.core.annotations.NoCopy;
import org.kuwaiba.core.annotations.NoSerialize;
import org.kuwaiba.core.annotations.ReadOnly;
import java.lang.reflect.Field;

/**
 * This is a bare representation of an attribute (aka field). It's used mainly to build
 * a class hierarchy tree, and it adds some extra attributes to the Field class, such as
 * application modifiers, which are gotten from custom annotations. Don't confuse with the entity
 * class AttributeMetadata, which holds information such as display names, descriptions, etc
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class AttributeWrapper {
    public static int MODIFIER_NOCOPY = 1;
    public static int MODIFIER_NOSERIALIZE = 2;
    public static int MODIFIER_READONLY = 4;

    private String name;
    private int javaModifiers;
    private int applicationModifiers = 0;
    private Class type;
    /**
     * Used to know if this attribute is inherited from a parent class or it's been define "locally"
     */
    private Class memberOf;

    public AttributeWrapper(Field field) {
        this.javaModifiers = field.getModifiers();
        this.name = field.getName();
        this.type = field.getType();
        if (field.getAnnotation(NoCopy.class) != null)
            this.applicationModifiers |= MODIFIER_NOCOPY;
        if (field.getAnnotation(NoSerialize.class) != null)
            this.applicationModifiers |= MODIFIER_NOSERIALIZE;
        if (field.getAnnotation(ReadOnly.class) != null)
            this.applicationModifiers |= MODIFIER_READONLY;
        this.memberOf = field.getDeclaringClass();
    }

    public Class getMemberOf() {
        return memberOf;
    }

    public void setMemberOf(Class memberOf) {
        this.memberOf = memberOf;
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

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }
}
