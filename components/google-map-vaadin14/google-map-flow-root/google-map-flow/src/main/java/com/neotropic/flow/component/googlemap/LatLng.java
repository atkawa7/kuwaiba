/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
public class LatLng {
    private double lat;
    private double lng;
    
    public LatLng() {
    }
    
    public LatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }
    
    public LatLng(JsonObject latLng) {
        this.lat = latLng.getNumber(Constants.Property.LAT);
        this.lng = latLng.getNumber(Constants.Property.LNG);
    }
    
    public double getLat() {
        return lat;
    }
    
    public void setLat(double lat) {
        this.lat = lat;
    }
    
    public double getLng() {
        return lng;
    }
    
    public void setLng(double lng) {
        this.lng = lng;
    }
    
    public JsonObject toJson() {
        JsonObject latLng = Json.createObject();
        latLng.put(Constants.Property.LAT, lat);
        latLng.put(Constants.Property.LNG, lng);
        return latLng;
    }
}
