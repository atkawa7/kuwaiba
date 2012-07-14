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
package org.kuwaiba.ws.toserialize;

import org.kuwaiba.ws.todeserialize.ObjectUpdate;
import java.lang.reflect.Field;
import javax.persistence.EntityManager;
import org.kuwaiba.util.HierarchyUtils;
import org.kuwaiba.util.MetadataUtils;

/**
 * Represents an object update. It's basically a RemoteObject containing only the changes to be done
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class RemoteObjectUpdate {
    private Class objectClass = null;
    private Long oid;
    private Field[] updatedAttributes;
    private Object[] newValues;

    public RemoteObjectUpdate(Class objectClass, ObjectUpdate object, EntityManager em)
            throws ClassNotFoundException,NoSuchFieldException{

        this.objectClass = objectClass;
        this.oid=object.getOid();
               
        newValues = new Object[object.getUpdatedAttributes().length];
        updatedAttributes = new Field[object.getUpdatedAttributes().length];
        
        for (int i = 0; i < newValues.length;i++){
            updatedAttributes[i] = HierarchyUtils.getField(objectClass, object.getUpdatedAttributes()[i]);

            //TODO: This is a dumb reprocess, polish so we don't have to convert the types into strings again
            newValues[i] = MetadataUtils.getRealValue(updatedAttributes[i].getType().getSimpleName(),
                    object.getNewValues()[i], em);
        }
    }

    public Object[] getNewValues() {
        return newValues;
    }

    public Class getObjectClass() {
        return objectClass;
    }

    public Long getOid() {
        return oid;
    }

    public Field[] getUpdatedAttributes() {
        return updatedAttributes;
    }
}
