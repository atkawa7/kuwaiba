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
package org.inventory.communications.core;

import java.util.HashMap;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.utils.Utils;
import org.inventory.webservice.RemoteObject;

/**
 * Represents the whole information related to an object. Instances if this class
 * are actually proxies representing a business object. They can be cities, buildings, port, etc
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalObjectImpl extends LocalObjectLightImpl implements LocalObject {
    private HashMap<String, Object> attributes;
    //Reference to the metadata associated to this object's class
    private LocalClassMetadata myMetadata;
    /**
     * Object's oid. Since this is a reference to the object in the database, it won't
     * be treated as a common attribute anymore
     */
    private Long oid;

    public LocalObjectImpl(){}

    public LocalObjectImpl(String className, Long _oid, String[] atts, Object[] vals){
        HashMap<String,Object> dict = new HashMap<String, Object>();
        this.className = className;
        this.oid = _oid;
        for(int i =0; i < atts.length;i++)
            dict.put(atts[i], vals[i]);
        this.attributes = dict;
    }

    /**
     * This constructor takes a semideserialized remote object and converts it to a proxy local object
     * using the metadata
     * @param ro
     * @param lcmdt
     */
    public LocalObjectImpl(RemoteObject ro, LocalClassMetadata lcmdt){
        this.className = ro.getClassName();
        this.myMetadata = lcmdt;
        this.oid = ro.getOid();

        attributes = new HashMap<String, Object>();
        String[] atts = ro.getAttributes().toArray(new String[0]);
        String[] vals = ro.getValues().toArray(new String[0]);

        for (int i =0; i<atts.length; i++)
            attributes.put(atts[i], Utils.getRealValue(lcmdt.getTypeForAttribute(atts[i]), vals[i]));
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
        return this.oid;
    }
    public void setOid(Long id){
        this.oid = id;
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
