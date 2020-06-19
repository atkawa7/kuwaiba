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
public class LatLngBounds {
    /**
     * south-west
     */
    private LatLng southWest;
    /**
     * north-east
     */
    private LatLng northEast;
    
    public LatLngBounds() {
    }
    
    public LatLngBounds(LatLng southWest, LatLng northEast) {
        this.southWest = southWest;
        this.northEast = northEast;
    }
    
    public LatLngBounds(JsonObject latLngBounds) {
        this.southWest = new LatLng(
            latLngBounds.getNumber(Constants.Property.SOUTH), 
            latLngBounds.getNumber(Constants.Property.WEST)
        );
        this.northEast = new LatLng(
            latLngBounds.getNumber(Constants.Property.NORTH), 
            latLngBounds.getNumber(Constants.Property.EAST)
        );
    }
    
    public LatLng getSouthWest() {
        return southWest;
    }
    
    public void setSouthWest(LatLng southWest) {
        this.southWest = southWest;
    }
    
    public LatLng getNorthEast() {
        return northEast;
    }
    
    public void setNorthEast(LatLng northEast) {
        this.northEast = northEast;
    }
    
    public JsonObject toJson() {
        JsonObject latLngBounds = Json.createObject();
        latLngBounds.put(Constants.Property.EAST, northEast.getLng());
        latLngBounds.put(Constants.Property.NORTH, northEast.getLat());
        latLngBounds.put(Constants.Property.SOUTH, southWest.getLat());
        latLngBounds.put(Constants.Property.WEST, southWest.getLng());
        return latLngBounds;
    }
}
