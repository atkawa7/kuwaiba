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

import entity.multiple.GenericObjectList;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import util.MetadataUtils;

/**
 * Esta clase representa un objeto extraído de la base de datos, es lo que se
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD) //Esta anotación le dice al serializador que incluya TODOS
                                      //los atributos sin importar su acceso (public, private, etc)
                                      //Por defecto, él coge sólo los public
public class RemoteObject extends RemoteObjectLight {
    private String[] attributes; //Aunque esta info se encuentra en los meta de la
                                 //clase, pero no sabemos si la saca en desorden de la bd
                                 //así que mejor nos curamos y trasteamos con los nombres también
    private String[] values;
    //La información de types ya se encuentra en el meta de la clase

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
                //getDeclaredMethods takes private and protected methods, but NOT the inherit ones
                //getMethods do the opposite. Now:
                Method m = object.getClass().getMethod("get"+MetadataUtils.capitalize(f.getName()),
                                                        new Class[]{});
                Object value = m.invoke(object, new Object[]{});
                if (value == null)  values[i]=null;
                else{
                    //Si el atributo es una relación o una enumeración la cosa se pone peluda
                    if(value instanceof GenericObjectList) //TODO: En contrar una forma de averiguar esto sin
                                                            //hacer referencia a una clase externa, 
                                                            //esta clase debe ser "stand-alone"
                        values[i]=String.valueOf(((GenericObjectList)value).getId());
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
