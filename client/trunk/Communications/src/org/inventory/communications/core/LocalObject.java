/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
import java.util.List;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;

/**
 * Represents the whole information related to an object. Instances if this class
 * are actually proxies representing a business object. They can be cities, buildings, port, etc
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalObject extends LocalObjectLight {
    private HashMap<String, Object> attributes;
    //Reference to the metadata associated to this object's class
    private LocalClassMetadata myMetadata;

    public LocalObject(String className, long oid, String[] atts, Object[] vals){
        HashMap<String,Object> dict = new HashMap<>();
        this.className = className;
        this.oid = oid;
        for(int i = 0; i < atts.length;i++)
            dict.put(atts[i], vals[i]);
        this.attributes = dict;
    }

    /**
     * This constructor takes a remote object and converts it to a proxy local object
     * using the metadata
     * @param className object class name
     * @param  id object id
     * @param  attributeNames list of attributes in that object
     * @param  attributeValues list of values for the list of attributes
     * @param lcmdt
     */
    public LocalObject(String className, long id, List<String> attributeNames, 
            List<List<String>> attributeValues, LocalClassMetadata lcmdt) throws IllegalArgumentException{
        this.className = className;
        this.oid = id;
        this.myMetadata = lcmdt;
        
        attributes = new HashMap<>();
        
        int i = 0;
        for (String attribute : attributeNames){
            attributes.put(attribute,
                    Utils.getRealValue(lcmdt.getTypeForAttribute(attribute), 
                    lcmdt.getMappingForAttribute(attribute), attributeValues.get(i)));
            i++;
        }
    }

    public LocalClassMetadata getObjectMetadata() {
        return myMetadata;
    }

    /**
     * Helper method to get the type of a given attribute
     * @param name Attribute name
     * @return A Java type
     * @exception IllegalArgumentException If the attribute does not exist
     */
    public Class getTypeOf(String name){
        if (attributes.get(name) == null)
            throw new IllegalArgumentException(String.format("The requested attribute does not exist: %s", name));
        else
            return attributes.get(name).getClass();
    }   

    public HashMap<String,Object> getAttributes() {
        return this.attributes;
    }

    public Object getAttribute(String name){
        return attributes.get(name);
    }
    
    public void setAttribute(String name, Object t) {
        firePropertyChangeEvent(name, getAttribute(name), t);
        attributes.put(name, t);
    }

    @Override
    public String getName() {
        return (String)getAttribute(Constants.PROPERTY_NAME);
    }
    
    @Override
    public String toString(){
        return getAttribute(Constants.PROPERTY_NAME) + "[" + getClassName() + "]";
    }
}