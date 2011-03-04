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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import util.MetadataUtils;

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

}
