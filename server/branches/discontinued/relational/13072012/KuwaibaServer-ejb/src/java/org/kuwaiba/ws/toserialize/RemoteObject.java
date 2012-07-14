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
package org.kuwaiba.ws.toserialize;

import org.kuwaiba.core.annotations.NoSerialize;
import org.kuwaiba.entity.core.RootObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.util.MetadataUtils;

/**
 * Instances of this class are proxies that represents the entities in the database
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD) //This annotation tell the serializer to include all fiels
                                      //no matter its modifier. Default takes only public ones
public class RemoteObject extends RemoteObjectLight {
    private List<String> attributes; //This information is already in the meta, but we don't know
                                 //if its sorted correctly there, so we take it too here
    private List<String> values;

    /**
     * Defaul constructor. Not used
     */
    private RemoteObject(){}

    public RemoteObject(Object object){
        List<Field> allAttributes = MetadataUtils.getAllFields(object.getClass(), false);
        attributes = new ArrayList<String>();
        values = new ArrayList<String>();
        
        this.className = object.getClass().getSimpleName();
        if (object instanceof RootObject)
            this.oid = ((RootObject)object).getId();

        for (Field f : allAttributes ){

            //Administrative fields and those decorated as NoSerialize shouldn't be serialized
            if(f.getAnnotation(NoSerialize.class) != null)
                continue;
            attributes.add(f.getName());

            try{
                //getDeclaredMethods takes private and protected methods, but NOT the inherited ones
                //getMethods do the opposite. Now:
                //IMPORTANT: if a given attribute doesn't have a getter using camel case, it wil be ignored
                //Update 07/03/2011: Accessors to booleans fields have a special naming convention: let's say out attribute is called "field".
                //then the getter would be "isField" and the setter "setField" to converge into the standard
                //Java naming convention
                Method m;
                if (f.getType().equals(Boolean.class))
                    m = object.getClass().getMethod("is"+MetadataUtils.capitalize(f.getName()),
                                                        new Class[]{});
                else
                    m = object.getClass().getMethod("get"+MetadataUtils.capitalize(f.getName()),
                                                        new Class[]{});
                Object value = m.invoke(object, new Object[]{});
                if (value == null)  values.add(null);
                else{
                    //If this attribute is a reference to any other business object, we use a lazy approach
                    //by setting as value the object id
                    if(value instanceof RootObject)
                        values.add(String.valueOf(((RootObject)value).getId()));
                    else
                        if (value instanceof Date)
                            values.add(String.valueOf(((Date)value).getTime()));
                        else
                            values.add(value.toString());
                }
            } catch (NoSuchMethodException nsme){
                Logger.getLogger(RemoteObject.class.getName()).log(Level.WARNING, "NoSuchM: {0}", nsme.getMessage());
            }
            catch (IllegalAccessException iae){
                Logger.getLogger(RemoteObject.class.getName()).log(Level.WARNING, "IllegalAccess: {0}", iae.getMessage());
            }
            catch(InvocationTargetException ite){
                Logger.getLogger(RemoteObject.class.getName()).log(Level.WARNING, "invocationTarget: {0}", ite.getMessage());
            }
            catch(SecurityException se){
                Logger.getLogger(RemoteObject.class.getName()).log(Level.WARNING, "Security: {0}", se.getMessage());
            }
            catch (IllegalArgumentException iae2){
                Logger.getLogger(RemoteObject.class.getName()).log(Level.WARNING, "IllegalArgument: {0}", iae2.getMessage());
            }
        }
    }

    /**
     * This method is useful to transform the returned value from queries (Entities)
     * into serialize RemoteObject
     * @param objs objects to be transformed
     * @return an array with RO
     */
    public static RemoteObject[] toArray(List objs){
        RemoteObject[] res = new RemoteObject[objs.size()];
        int i=0;
        for (Object obj : objs){
            res[i] = new RemoteObject(obj);
            i++;
        }
        return res;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
