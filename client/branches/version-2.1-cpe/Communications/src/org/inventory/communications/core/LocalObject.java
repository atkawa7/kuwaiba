/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.communications.wsclient.StringPair;

/**
 * Represents the whole information related to an object. Instances if this class
 * are actually proxies representing a business object. They can be cities, buildings, port, etc
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LocalObject extends LocalObjectLight {
    /**
     * A dictionary containing the names of the attributes and their respective values of the current object
     */
    private HashMap<String, Object> attributes;
    /**
     * The metadata associated to the class the current object belongs to
     */
    private LocalClassMetadata myMetadata;

    public LocalObject(String className, String id, HashMap<String, Object> attributes){
        this.className = className;
        this.id = id;
        this.attributes = attributes;
    }

    /**
     * This constructor takes a remote object and converts it to a proxy local object
     * using the metadata
     * @param className object class name
     * @param  id object id
     * @param  remoteAttributes A dictionary with the attributes and values as strings
     * @param classMetadata The class metadata to be used to map the attributes to actual Java data types
     */
    public LocalObject(String className, String id, List<StringPair> remoteAttributes, LocalClassMetadata classMetadata) throws IllegalArgumentException{
        this.className = className;
        this.id = id;
        this.myMetadata = classMetadata;
        
        this.attributes = new HashMap<>();
        
        for (StringPair remoteAttribute : remoteAttributes)
            attributes.put(remoteAttribute.getKey(), Utils.getRealValue(classMetadata.getTypeForAttribute(remoteAttribute.getKey()), 
                                                classMetadata.getMappingForAttribute(remoteAttribute.getKey()), remoteAttribute.getValue()));                            
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
        Object oldValue = getAttribute(name);
        attributes.put(name, t);
        firePropertyChangeEvent(name, oldValue, t);
    }

    @Override
    public String getName() {
        return (String)getAttribute(Constants.PROPERTY_NAME);
    }
}