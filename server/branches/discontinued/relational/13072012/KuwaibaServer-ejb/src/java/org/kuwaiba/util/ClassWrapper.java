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

import org.kuwaiba.core.annotations.Dummy;
import org.kuwaiba.core.annotations.NoCount;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a bare wrapper for a class in order to facilitate the serialization process
 * to an XML format.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ClassWrapper {
    public static final int TYPE_ROOT = 0;
    public static final int TYPE_INVENTORY = 1;
    public static final int TYPE_APPLICATION = 2;
    public static final int TYPE_METADATA = 3;
    public static final int TYPE_OTHER = 4;

    public static final int MODIFIER_DUMMY = 1;
    public static final int MODIFIER_NOCOUNT = 2;

    private String name;
    private int javaModifiers;
    private int applicationModifiers = 0;
    private int classType;
    private List<ClassWrapper> directSubClasses;
    private List<AttributeWrapper> attributes;


    public ClassWrapper(Class toBeWrapped, int classType) {
        this.name = toBeWrapped.getSimpleName();
        this.javaModifiers = toBeWrapped.getModifiers();
        if (toBeWrapped.getAnnotation(Dummy.class) != null)
            this.applicationModifiers |= MODIFIER_DUMMY;
        if (toBeWrapped.getAnnotation(NoCount.class) != null)
            this.applicationModifiers |= MODIFIER_NOCOUNT;
        this.attributes = new ArrayList<AttributeWrapper>();
        for (Field field : MetadataUtils.getAllFields(toBeWrapped, true))
            attributes.add(new AttributeWrapper(field));
        this.directSubClasses = new ArrayList<ClassWrapper>();
        this.classType = classType;
    }


    public int getApplicationModifiers() {
        return applicationModifiers;
    }

    public void setApplicationModifiers(int applicationModifiers) {
        this.applicationModifiers = applicationModifiers;
    }

    public List<AttributeWrapper> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeWrapper> attributes) {
        this.attributes = attributes;
    }

    public List<ClassWrapper> getDirectSubClasses() {
        return directSubClasses;
    }

    public void setDirectSubClasses(List<ClassWrapper> directSubClasses) {
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
}
