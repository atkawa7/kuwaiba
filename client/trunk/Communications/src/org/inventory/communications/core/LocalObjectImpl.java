package org.inventory.communications.core;

import java.util.HashMap;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.webservice.RemoteObject;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalObjectImpl extends LocalObjectLightImpl implements LocalObject {
    private HashMap<String, Object> attributes;
    //Esto usa más memoria que el approach anterior donde sólo se guardaba un meta
    //y se colocaba en la cache, pero este me parece que es más eficiente, ya que
    //disminuye el tiempo de búsqueda de la metadata correspondiente al objeto
    private LocalClassMetadata myMetadata;

    public LocalObjectImpl(){}

    public LocalObjectImpl(String className, String[] atts, Object[] vals){
        HashMap<String,Object> dict = new HashMap<String, Object>();
        this.className = className;
        for(int i =0; i < atts.length;i++)
            dict.put(atts[i], vals[i]);
        this.attributes = dict;
    }

    //lcmdt puede ser nulo, en los casos en los que no se crea un objeto para
    //desplegarlo, sino para un ObjectUpdate, en cuyo caso, la meta no es necesaria
    public LocalObjectImpl(RemoteObject ro, LocalClassMetadata lcmdt){
        this.className = ro.getClassName();
        this.myMetadata = lcmdt;

        attributes = new HashMap<String, Object>();
        String[] atts = ro.getAttributes().toArray(new String[0]);
        String[] vals = ro.getValues().toArray(new String[0]);

        //Por alguna razón que desconozco un ciclo (atts.iterator.next()) nunca avanzaba y la vaina se quedaba en eun ciclo infinito
        for (int i =0; i<atts.length; i++){
            Object value;
            try{
                if (vals[i] == null) value = null;
                else
                    if (this.myMetadata.getTypeForAttribute(atts[i]).equals("String"))
                        value = vals[i];
                    else{
                        if(this.myMetadata.isMultiple(atts[i]))
                            value = Long.valueOf(vals[i]);
                        else
                            value = Class.forName("java.lang."+this.getObjectMetadata().
                                getTypeForAttribute(atts[i])).getMethod("valueOf", String.class).
                                invoke(null, vals[i]);
                    }
            }catch(Exception e){
                value = vals[i];
                e.printStackTrace();
            }
            attributes.put(atts[i], value);
        }
    }

    public LocalClassMetadata getObjectMetadata() {
        return myMetadata;
    }

    public void setObjectMetadata(LocalClassMetadata metaForClass) {
        this.myMetadata = metaForClass;
    }


    //ESTE MÉTODO ES UN WORKAROUND: Por otra razón que desconozco, no me dejó crear un constructor en LocalObjectImpl que recibiera los valores
    //individuales, decía que cannot find class test.RemoteObject, pero ni idea, porque en ningún lado se
    //estaba llamando el constructor que pedía un RemoteObject como parámetro.
    //Creo que al final va a tocar crear una nueva aplicación RCP y adicionar los módulos manualmente uno por uno
    public void setLocalObject(String className, String[] attributes, Object[] values){
        HashMap<String,Object> dict = new HashMap<String, Object>();
        this.className = className;
        for(int i =0; i < attributes.length;i++)
            dict.put(attributes[i], values[i]);
        this.attributes = dict;
    }

    /*
     * Función helper que proporciona el tipo de un atributo en particular
     *
     */
    public Class getTypeOf(String name){
        return attributes.get(name).getClass();
    }


    @Override
    public Long getOid(){
        return (Long)attributes.get("id");
    }
    public void setOid(Long id){
        this.attributes.put("id", id);
    }

    @Override
    public String toString(){
        return this.getAttribute("name")==null?"":this.getAttribute("name").toString();
    }

    public HashMap<String,Object> getAttributes() {
        return this.attributes;
    }

    public Object getAttribute(String name){
        return attributes.get(name);
    }
}
