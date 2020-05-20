/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package com.neotropic.flow.component.googlemap;

import elemental.json.Json;
import elemental.json.JsonObject;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class Size {
    private double width;
    private double height;
    
    public Size(double width, double height) {
        this.width = width;
        this.height = height;
    }
    
    public Size(JsonObject size) {
        this.width = size.getNumber(Constants.Property.WIDTH);
        this.height = size.getNumber(Constants.Property.HEIGHT);
    }
    
    public double getWidth() {
        return width;
    }
    
    public void setWidth(double width) {
        this.width = width;
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double height) {
        this.height = height;
    }
    
    public JsonObject toJson() {
        JsonObject size = Json.createObject();
        size.put(Constants.Property.WIDTH, width);
        size.put(Constants.Property.HEIGHT, height);
        return size;
    }
}
