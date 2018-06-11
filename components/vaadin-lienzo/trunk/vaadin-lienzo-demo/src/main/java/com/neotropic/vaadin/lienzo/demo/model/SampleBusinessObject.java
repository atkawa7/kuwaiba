/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.vaadin.lienzo.demo.model;

/**
 * A simple bean representing a business object to be represented by a tree node
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SampleBusinessObject {
    /**
     * Data type used to tag the instances while transferred 
     */
    public static String DATA_TYPE = "object/sample_object";
    /**
     * Object name
     */
    private String name;
    /**
     * Object id
     */
    private int id;

    public SampleBusinessObject(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public boolean equals (Object obj) {
        return (obj instanceof SampleBusinessObject ? ((SampleBusinessObject)obj).getId() == this.id: false);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.id;
        return hash;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s)", name, id);
    }
    
    public String serialize() {
        return id + ";" + name;
    }
    
    public static SampleBusinessObject deserialize(String serializedObject) {
        String[] serializedTokens = serializedObject.split(";");
        return new SampleBusinessObject(Integer.valueOf(serializedTokens[0]), serializedTokens[1]);
    }
}
