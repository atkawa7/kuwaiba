/*
 * Copyright 2020 Neotropic SAS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.neotropic.flow.component.mxgraph;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to create styles to the mxGraph Cells 
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class MxCellStyle {
      
    private Map<String, Object> properties;
    
    private String name;

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperty(Map<String, Object> properties) {
        this.properties = properties;
    }
    
    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MxCellStyle(String name) {
        this.name = name;
        this.properties = new HashMap();
    }

    @Override
    public boolean equals(Object obj) {
       if (obj instanceof MxCellStyle) {
            return this.name.equals(((MxCellStyle) obj).getName());
       }     
       return false;           
    }

    public String getAsJson() {
        String jsonStyle = new Gson().toJson(this.properties);
        return jsonStyle;
    }
   

}
