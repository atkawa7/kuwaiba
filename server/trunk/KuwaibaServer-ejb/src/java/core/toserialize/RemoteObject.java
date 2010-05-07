/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package core.toserialize;

import entity.multiple.GenericObjectList;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

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
        List<Field> allAttributes = getAllAttributes(object.getClass(), new ArrayList<Field>());
        attributes = new String [allAttributes.size()];
        values = new String [allAttributes.size()];
        
        this.className = object.getClass().getSimpleName();

        int i = 0;
        for (Field f : allAttributes ){
            attributes[i]=f.getName();
            
            try{
                //Cuidado: getDeclaredMethods agarra los privados y protegidos pero NO los heredados
                //El solito hace lo contrario. Ahora bien
                Method m = object.getClass().getMethod("get"+capitalize(f.getName()), new Class[]{});
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

    /*
     * TODO: No debería ir aquí sino en un clase de utilidades
     * Es necesaria porque no existe forma de saber los atributos heredados
     * Pilas, tambén se debe revisar porqué tocó hacerle la comparación con cadenas a lo modificadores y no sirvió el otro con booleanos
     */
    public List<Field> getAllAttributes(Class<?> aClass, List<Field> attributesSoFar){
        //System.out.println("Atributos para: "+aClass.getName());
        for (Field f : aClass.getDeclaredFields()){
            //System.out.print("Analizando "+f.getName()+"...");
            if(!(Modifier.toString(f.getModifiers()).startsWith("private") || Modifier.toString(f.getModifiers()).startsWith("protected transient"))){
            //if(Modifier.isPrivate(f.getModifiers())||Modifier.isTransient(f.getModifiers())){
                attributesSoFar.add(f);
            }
            //System.out.println("Valor:"+Modifier.toString(f.getModifiers()));
        }
        //if (!aClass.getSuperclass().getName().equalsIgnoreCase("RemoteObject"))
        if (aClass.getSuperclass() != null)
            getAllAttributes(aClass.getSuperclass(), attributesSoFar);
        return attributesSoFar;
    }

    /*
     * TODO: No debería ir aquí sino en un clase de utilidades
     * Es necesaria para poder simular los getters al concatenar "get" y el nombre del atributo en mayúscula
     */
    public String capitalize(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
