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
package core.toserialize;

import core.annotations.Administrative;
import core.annotations.NoSerialize;
import entity.core.RootObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import util.MetadataUtils;

/**
 * Instances of this class are proxies that represents the entities in the database
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD) //This annotation tell the serializer to include all fiels
                                      //no matter its modifier. Default takes only public ones
public class RemoteObject extends RemoteObjectLight {
    private String[] attributes; //This information is already in the meta, but we don't know
                                 //if its sorted correctly there, so we take it too here
    private String[] values;


    public RemoteObject(){}

    public RemoteObject(Object object){
        List<Field> allAttributes = MetadataUtils.getAllFields(object.getClass());
        attributes = new String [allAttributes.size()];
        values = new String [allAttributes.size()];
        
        this.className = object.getClass().getSimpleName();

        int i = 0;
        for (Field f : allAttributes ){

            //Administrative fields and those decorated as NoSerialize shouldn't be serialized
            if(f.getAnnotation(Administrative.class) != null ||
                    f.getAnnotation(NoSerialize.class) != null)
                continue;
            attributes[i]=f.getName();

            try{
                //getDeclaredMethods takes private and protected methods, but NOT the inherited ones
                //getMethods do the opposite. Now:
                //IMPORTANT: if a given attribute doesn't have a getter using camel case, it wil be ignored
                Method m = object.getClass().getMethod("get"+MetadataUtils.capitalize(f.getName()),
                                                        new Class[]{});
                Object value = m.invoke(object, new Object[]{});
                if (value == null)  values[i]=null;
                else{
                    //If this attribute is a reference to any other business object, we use a lazy approach
                    //by setting as value the object id
                    if(value instanceof RootObject)
                        values[i]=String.valueOf(((RootObject)value).getId());
                    else
                        if (value instanceof Date)
                            values[i] = String.valueOf(((Date)value).getTime());
                        else
                            values[i]=value.toString();

                    if (attributes[i].equals("id"))
                        this.oid = (Long)value;
                }
            } catch (NoSuchMethodException nsme){
                System.out.println("NoSuchM:"+nsme.getMessage());
            }
            catch (IllegalAccessException iae){
                System.out.println("IllegalAccess "+iae.getMessage());
            }
            catch(InvocationTargetException ite){
                System.out.println("invocationTarget "+ite.getMessage());
            }
            catch(SecurityException se){
                System.out.println("Security "+se.getMessage());
            }
            catch (IllegalArgumentException iae2){
                System.out.println("IllegalArgument "+iae2.getMessage());
            }
            i++;
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
}
