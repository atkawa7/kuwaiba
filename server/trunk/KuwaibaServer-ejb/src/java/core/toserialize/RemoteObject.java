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

import entity.multiple.GenericObjectList;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
        List<Field> allAttributes = MetadataUtils.getAllAttributes(object.getClass(),
                                                                    new ArrayList<Field>());
        attributes = new String [allAttributes.size()];
        values = new String [allAttributes.size()];
        
        this.className = object.getClass().getSimpleName();

        int i = 0;
        for (Field f : allAttributes ){
            attributes[i]=f.getName();
            
            try{
                //getDeclaredMethods takes private and protected methods, but NOT the inherited ones
                //getMethods do the opposite. Now:
                Method m = object.getClass().getMethod("get"+MetadataUtils.capitalize(f.getName()),
                                                        new Class[]{});
                Object value = m.invoke(object, new Object[]{});
                if (value == null)  values[i]=null;
                else{
                    //If this attribute is a list type, get the id
                    if(value instanceof GenericObjectList) 
                        values[i]=String.valueOf(((GenericObjectList)value).getId());
                    else
                        if (value instanceof Date)
                            values[i] = String.valueOf(((Date)value).getTime());
                        else
                            values[i]=value.toString();
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
}
