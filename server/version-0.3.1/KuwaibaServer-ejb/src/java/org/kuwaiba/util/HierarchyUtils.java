/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
import org.kuwaiba.core.annotations.RelatableToService;
import org.kuwaiba.core.annotations.NoSerialize;
import org.kuwaiba.entity.connections.GenericConnection;
import org.kuwaiba.entity.core.ApplicationObject;
import org.kuwaiba.entity.core.InventoryObject;
import org.kuwaiba.entity.core.MetadataObject;
import org.kuwaiba.entity.core.RootObject;
import org.kuwaiba.entity.core.ViewableObject;
import org.kuwaiba.entity.core.metamodel.AttributeMetadata;
import org.kuwaiba.entity.core.metamodel.ClassMetadata;
import org.kuwaiba.entity.core.metamodel.PackageMetadata;
import org.kuwaiba.entity.equipment.ports.GenericPort;
import org.kuwaiba.entity.location.GenericPhysicalNode;
import org.kuwaiba.entity.multiple.GenericObjectList;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

/**
 * Provide misc methods to manipulate the class hierarchy
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class HierarchyUtils {

    /**
     * This method returns is a given class is sub class of another
     * @param child Class to be tested
     * @param allegedParent Class supposed to be the parent class
     * @return true if the given class is a subclass or if child and allegedParent are equals
     */
    public static boolean isSubclass (Class child, Class allegedParent){
        if (child == null)
            return false;

        Class myClass=child;
        while (!myClass.equals(Object.class)){
            if (myClass.equals(allegedParent))
                return true;
            
            myClass = myClass.getSuperclass();
            //This usually happens when after a recursive operation when the child is an interface
            if (myClass == null)
                return false;
        }
        return false;
    }
    
    /**
     * Says if given class implements an interface
     * @param toBeTested 
     * @param implementedInterface the interface that toBeTested may be implementing
     * @return toBeTested (or any of its superclasses) implements implementedInterface
     */
    public static boolean implementsInterface(Class toBeTested, Class implementedInterface){
        Class myClass=toBeTested;
        while (!myClass.equals(Object.class)){
            Class[] interfaces = myClass.getInterfaces();
            for (Class myInterface : interfaces){
                if (myInterface.equals(implementedInterface))
                    return true;
            }
            myClass = myClass.getSuperclass();
        }
        return false;
    }

    public static List<Class> getInstanceableSubclasses(Class myClass, Collection<Class> allClasses){
        List<Class> res = new ArrayList<Class>();
        for (Class aClass : allClasses){
            if (aClass.equals(myClass))
                continue;
            if (isSubclass(aClass, myClass)){
                if (!Modifier.isAbstract(aClass.getModifiers()))
                    res.add(aClass);
            }
        }
        return res;
    }


     /**
     * Creates a class metadata record for a given entity class
     * @param entity The class to be persisted (with its superclasses)
     * @param em The current entity manager
     * @return the new class id
     */
    public static Long persistClass(EntityType entity, EntityManager em){

        if (entity.getJavaType().equals(RootObject.class))
            return null;
        if (entity.getJavaType().equals(ApplicationObject.class))
            return null;

        String sentence = "SELECT x.id FROM ClassMetadata x WHERE x.name = '"+entity.getJavaType().getSimpleName()+"'";
        Query query = em.createQuery(sentence);
        try{
            Long res = (Long)query.getSingleResult();
            return res;
        }catch(NoResultException nre){ // if the class doesn't exists, go on

        }

        List<AttributeMetadata> atts = new ArrayList<AttributeMetadata>();
        List<Field> metaAtts = MetadataUtils.getAllFields(entity.getJavaType(), false);
        PackageMetadata pm;
        sentence = "SELECT x FROM PackageMetadata x WHERE x.name = '"+entity.getJavaType().getPackage().getName()+"'";
        query = em.createQuery(sentence);
        Long parentId;
        try{
            pm = (PackageMetadata)query.getSingleResult();
        }catch(NoResultException nre){ // if the packagemetadata has not been create yet, we do
            pm = new PackageMetadata(entity.getJavaType().getPackage().getName(),
                    entity.getJavaType().getPackage().getName(),"");
            em.persist(pm);
        }

        sentence = "SELECT x.id FROM ClassMetadata x WHERE x.name = '"+entity.getJavaType().getSuperclass().getSimpleName()+"'";
        query = em.createQuery(sentence);
        try{
            parentId = (Long)query.getSingleResult();
        }catch(NoResultException nre){ // if the parent class has not been create yet, we do it
            if ((EntityType)entity.getSupertype() == null) //The parent is not an entity
                parentId = ClassMetadata.ROOT_CLASS_ID;
            else
                parentId = persistClass((EntityType)entity.getSupertype(), em);
        }
        for(Field att : metaAtts){
            //Ignore the fields marked as non serializables or having a private/static modifier
            if (att.getAnnotation(NoSerialize.class) != null)
                continue;
            if (Modifier.isPrivate(att.getModifiers()) || Modifier.isStatic(att.getModifiers()))
                continue;
            atts.add(new AttributeMetadata(att));
        }

        ClassMetadata cm = new ClassMetadata(entity.getJavaType().getSimpleName(),
                                             pm,
                                             entity.getJavaType().getSimpleName(),
                                             false,Modifier.isAbstract(entity.getJavaType().getModifiers()),
                                             entity.getJavaType().getAnnotation(Dummy.class) != null,
                                             isSubclass(entity.getJavaType(),GenericPhysicalNode.class),
                                             isSubclass(entity.getJavaType(),GenericConnection.class),
                                             isSubclass(entity.getJavaType(),GenericPort.class),
                                             isSubclass(entity.getJavaType(), GenericObjectList.class),
                                             entity.getJavaType().getAnnotation(NoCount.class) !=null ,
                                             entity.getJavaType().getAnnotation(RelatableToService.class) !=null ,
                                             isSubclass(entity.getJavaType(), ViewableObject.class), null, atts);

        em.persist(cm);
        return cm.getId();
    }

    /**
     * Gets a field for a given class no matter if it's a private one of if it belongs to a superclass
     * @param aClass A class to look for the field
     * @param  fieldName A string with the name of the field
     */
    public static Field getField (Class aClass, String fieldName) throws NoSuchFieldException{
        for (Field f : aClass.getDeclaredFields()){
            if(f.getName().equals(fieldName))
                return f;
        }

        if (aClass.getSuperclass().equals(Object.class))
            throw new NoSuchFieldException(fieldName);

        return getField(aClass.getSuperclass(), fieldName);
    }

    /**
     * Gets all possible direct subclasses of a given class
     * @param parentClass the class from where we're going to get its subclasses
     * @param allClasses a list of all available classes
     * @return
     */
    public static List<Class> getDirectSubClasses(Class parentClass, List<Class> allClasses){
        List<Class> subClasses = new ArrayList<Class>();
        for (Class aClass : allClasses){
            if (aClass.getSuperclass().equals(parentClass))
                subClasses.add(aClass);
        }
        return subClasses;
    }

    /**
     *
     * @param root
     * @param remainingClasses
     * @return
     */
    public static ClassWrapper createTree(Class root, List<Class> remainingClasses){
        int classType;
        if (isSubclass(root, InventoryObject.class))
            classType = ClassWrapper.TYPE_INVENTORY;
        else
            if (isSubclass(root, ApplicationObject.class))
                classType = ClassWrapper.TYPE_APPLICATION;
            else
                if (isSubclass(root, MetadataObject.class))
                    classType = ClassWrapper.TYPE_METADATA;
                else
                    classType = ClassWrapper.TYPE_OTHER;

        ClassWrapper thisClass = new ClassWrapper(root, classType);
        List<Class> subClasses = getDirectSubClasses(root, remainingClasses);

        for (Class aSubClass : subClasses){
            remainingClasses.remove(aSubClass);
            thisClass.getDirectSubClasses().add(createTree(aSubClass, remainingClasses));
        }
        return thisClass;
    }
}
