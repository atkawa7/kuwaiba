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

package org.kuwaiba.tools.kadmin.utils;

import entity.core.RootObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Transient;

/**
 * Class with helper methods
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Util {
    public static List<Field> getAllFields(Class<?> aClass, boolean includePrivate){
        List<Field> myAtts = new ArrayList<Field>();
        for (Field f : aClass.getDeclaredFields()){
            boolean showPrivate = includePrivate && Modifier.isPrivate(f.getModifiers());
            if ((showPrivate || Modifier.isProtected(f.getModifiers()))
                    && !Modifier.isTransient(f.getModifiers())
                    && !Modifier.isFinal(f.getModifiers())
                    && f.getAnnotation(Transient.class) == null) //This last has to do with the introduction since EclipseLink 2.1.0
                                                                 //AttributeGroups http://wiki.eclipse.org/EclipseLink/Examples/JPA/AttributeGroup
                myAtts.add(f);
        }
        if (aClass != RootObject.class && aClass.getSuperclass() != null && aClass != Object.class)
            myAtts.addAll(getAllFields(aClass.getSuperclass(), includePrivate));
        return myAtts;
    }

    public static String capitalize(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
