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

package util;

import core.annotations.Dummy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ClassWrapper {
    public static int TYPE_ROOT = 0;
    public static int TYPE_INVENTORY = 1;
    public static int TYPE_APPLICATION = 2;
    public static int TYPE_METADATA = 3;
    public static int TYPE_OTHER = 4;

    private String name;
    private int javaModifiers;
    private int applicationModifiers;
    private int classType;
    private List<Class> interfaces;
    private List<ClassWrapper> directSubClasses;
    private List<AttributeWrapper> attributes;


    public ClassWrapper(Class toBeWrapped, int classType) {
        this.name = toBeWrapped.getSimpleName();
        this.javaModifiers = toBeWrapped.getModifiers();
        this.applicationModifiers = toBeWrapped.getAnnotation(Dummy.class) == null ? 0 : 1;
        this.interfaces = Arrays.asList(toBeWrapped.getInterfaces());
        attributes = new ArrayList<AttributeWrapper>();
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

    public List<Class> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<Class> interfaces) {
        this.interfaces = interfaces;
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
