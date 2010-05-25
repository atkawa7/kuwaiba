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
 *  under the License.
 */
package util;

import core.annotations.Dummy;
import entity.core.RootObject;
import entity.core.metamodel.AttributeMetadata;
import entity.core.metamodel.ClassMetadata;
import entity.core.metamodel.PackageMetadata;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class HierarchyUtils {

    /**
     * This method returns is a given class is sub class of another
     * @param child Class to be tested
     * @param allegedParent Class suppossed to be the parent class
     * @return true if the given class is
     */
    public static boolean isSubclass (Class child, Class allegedParent){
        Class myClass=child;
        while (!myClass.equals(Object.class)){
            if (myClass.equals(allegedParent))
                return true;
            myClass = myClass.getSuperclass();
        }
        return false;
    }

    public static List<ClassMetadata> getInstanceableSubclasses(Long classId, EntityManager em){
        String sentence = "SELECT x FROM ClassMetadata x WHERE x.parent = "+classId;
        Query query = em.createQuery(sentence);
        List<Object> subClasses = query.getResultList();
        List<ClassMetadata> result = new ArrayList<ClassMetadata>();
        for (Object obj : subClasses){
            ClassMetadata cm = (ClassMetadata)obj;
            if(cm.getIsAbstract())
                result.addAll(getInstanceableSubclasses(cm.getId(), em));
            else
                result.add(cm);
        }
        return result;
    }


     /**
     * This method tries to persists a class and all superclasses not  already persisted
     * @param aClass The class to be persisted (with its superclasses)
     * @param alreadyPersisted The list of classes already persisted
     * @param em The current entity manager
     */
    public static Long persistClass(EntityType entity, EntityManager em){

        String sentence = "SELECT x.id FROM ClassMetadata x WHERE x.name = '"+entity.getJavaType().getSimpleName()+"'";
        Query query = em.createQuery(sentence);
        try{
            Long res = (Long)query.getSingleResult();
            return res;
        }catch(NoResultException nre){ // if the class doesn't exists, go on

        }

        List<AttributeMetadata> atts = new ArrayList<AttributeMetadata>();
        Set<Attribute> metaAtts = entity.getAttributes();
        PackageMetadata pm;
        sentence = "SELECT x FROM PackageMetadata x WHERE x.name = '"+entity.getJavaType().getPackage().getName()+"'";
        query = em.createQuery(sentence);
        Long parentId;
        try{
            pm = (PackageMetadata)query.getSingleResult();
        }catch(NoResultException nre){ // if the packagemetadata has not been create yet, we do
            pm = new PackageMetadata(entity.getJavaType().getPackage().getName(),"");
            em.persist(pm);
        }

        sentence = "SELECT x.id FROM ClassMetadata x WHERE x.name = '"+entity.getJavaType().getSuperclass().getSimpleName()+"'";
        query = em.createQuery(sentence);
        try{
            parentId = (Long)query.getSingleResult();
        }catch(NoResultException nre){ // if the parent class has not been create yet, we do
            if ((EntityType)entity.getSupertype() == null) //The parent is not an entity
                parentId = ClassMetadata.ROOT_CLASS_ID;
            else
                parentId = persistClass((EntityType)entity.getSupertype(), em);
        }
        for(Attribute att : metaAtts)
            atts.add(new AttributeMetadata(att));

        ClassMetadata cm = new ClassMetadata(entity.getJavaType().getSimpleName(),
                                             pm,
                                             java.util.ResourceBundle.getBundle("internacionalization/Bundle").getString("LBL_CLASS")+entity.getJavaType().getSimpleName(),
                                             false,Modifier.isAbstract(entity.getJavaType().getModifiers()),
                                             (entity.getJavaType().getAnnotation(Dummy.class)!=null),
                                             null,atts,parentId
                                             );

        em.persist(cm);
        return cm.getId();
    }
}
