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
package core.toserialize;

import core.todeserialize.ObjectUpdate;
import java.lang.reflect.Field;
import javax.persistence.EntityManager;
import util.HierarchyUtils;
import util.MetadataUtils;

/**
 * Represents an object's update, but deserialized (from the application's point of view)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class RemoteObjectUpdate {
    private Class objectClass = null;
    private Long oid;
    private Field[] updatedAttributes;
    private Object[] newValues;

    public RemoteObjectUpdate(ObjectUpdate object, EntityManager em)
            throws ClassNotFoundException,NoSuchFieldException{

        this.objectClass = Class.forName(object.getClassname());
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

     /**
     * Generates a native SQL Query to be executed in order to perform the update
     * @return The native SQL text to update a given object
     */
/*    public String generateQueryText(){
    String query="UPDATE "+this.objectClass.getSimpleName()+" obj SET ";
    for (int i=0; i<this.updatedAttributes.length;i++){
        String value = "";
        String att="";
        if(this.updatedAttributes[i].getType().equals(String.class))
            value="'"+(String)this.newValues[i]+"'";
        else
            value = this.newValues[i].toString();
        if (HierarchyUtils.isSubclass(updatedAttributes[i].getType(),GenericObjectList.class) ||
                           HierarchyUtils.
                                isSubclass(updatedAttributes[i].getType(),GenericRelation.class)){
            att= this.updatedAttributes[i].getName()+"_id";
            if (value.equals("0")) //If this is a relationship and the id is 0 its because the user chose "None". This is, a NULL value
                value="NULL";
        }
        else
            att= this.updatedAttributes[i].getName();
        query+=att+"="+value+",";
    }
    query = query.substring(0, query.length()-1);
    query +=" WHERE obj.id="+this.oid;
    return query;
    }
 *
 */
}
