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

import entity.core.metamodel.ClassMetadata;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

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
        String sentence = "SELECT x FROM ClassMetadata x WHERE x.parent_id = "+classId;
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
}
