/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.kuwaiba.tools;

import org.kuwaiba.core.exceptions.EntityManagerNotAvailableException;
import org.kuwaiba.entity.core.ApplicationObject;
import org.kuwaiba.entity.core.MetadataObject;
import org.kuwaiba.entity.multiple.GenericObjectList;
import org.kuwaiba.entity.session.User;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import org.kuwaiba.util.HierarchyUtils;
import org.kuwaiba.util.MetadataUtils;

/**
 * This bean contains useful helper methods
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Stateless
public class ToolsBean implements ToolsBeanRemote{
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<String> diagnoseAccessors() {
        List<String> res = new ArrayList<String>();
        if (em !=  null){
            for (EntityType entity : em.getMetamodel().getEntities()){
                for (Field field : MetadataUtils.getAllFields(entity.getJavaType(), true))
                    if (field.getType().equals(Boolean.class)){
                        try {
                            entity.getJavaType().getMethod("is" + MetadataUtils.capitalize(field.getName()), new Class[]{});
                            entity.getJavaType().getMethod("set" + MetadataUtils.capitalize(field.getName()), new Class[]{field.getType()});
                        } catch (NoSuchMethodException ex) {
                            res.add(entity.getJavaType().getSimpleName() + ": "+ex.getMessage());
                        }
                    }
                    else{
                        try {
                            entity.getJavaType().getMethod("get" + MetadataUtils.capitalize(field.getName()), new Class[]{});
                            entity.getJavaType().getMethod("set" + MetadataUtils.capitalize(field.getName()), new Class[]{field.getType()});
                        } catch (Exception ex) {
                            res.add(entity.getJavaType().getSimpleName() + ": "+ex.getMessage());
                        }
                    }
            }
        }
        return res;
    }

        /**
     * This method resets class metadata information
     *
     */
    @Override
    public void buildMetaModel() throws Exception{

        if (em != null){

            //Delete existing class metadata
            //Query query = em.createNamedQuery("flushClassMetadata");
            //query.executeUpdate();

            //Delete existing attribute metadata
            //query = em.createNamedQuery("flushAttributeMetadata");
            //query.executeUpdate();

            //Delete existing package metadata
            //query = em.createNamedQuery("flushPackageMetadata");
            //query.executeUpdate();

            String sql = "SELECT x.name FROM ClassMetadata x";

            List<String> alreadyPersisted = em.createQuery(sql).getResultList();

            Set<EntityType<?>> ent = em.getMetamodel().getEntities();
            //HashMap<String, EntityType> alreadyPersisted = new HashMap<String, EntityType>();


            for (EntityType entity : ent){
                if(HierarchyUtils.isSubclass(entity.getJavaType(), MetadataObject.class))
                    continue;
                if(HierarchyUtils.isSubclass(entity.getJavaType(), ApplicationObject.class)){
                    if (!HierarchyUtils.isSubclass(entity.getJavaType(), GenericObjectList.class))
                        continue;
                }
                if (alreadyPersisted.contains(entity.getJavaType().getSimpleName()))
                    continue;
                HierarchyUtils.persistClass(entity,em);
            }
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    @Override
    public void resetAdmin() throws EntityManagerNotAvailableException {
        if (em != null){
            User admin;
            try{
                admin = (User) em.createQuery("SELECT x FROM User x WHERE x.username='admin'").getSingleResult();
                admin.setPassword("60CEC2D1577E8C0F551F2272B7B163C5");
                em.merge(admin);
            }catch (NoResultException ex){
                admin = new User();
                admin.setUsername("admin");
                admin.setPassword("60CEC2D1577E8C0F551F2272B7B163C5");
                em.persist(admin);
            }
        }else
            throw new EntityManagerNotAvailableException();
    }
}
