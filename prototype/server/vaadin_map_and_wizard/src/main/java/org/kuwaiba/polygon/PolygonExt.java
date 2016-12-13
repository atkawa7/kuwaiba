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
package org.kuwaiba.polygon;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapInfoWindow;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PolygonExt extends GoogleMapPolygon {
    private MapPolygon mapPolygon;
    private GoogleMapInfoWindow infoWindow;
    
    public PolygonExt() {
        mapPolygon = null;
        infoWindow = null;
    }
    
    public PolygonExt(MapPolygon mapPolygon) {
        this.mapPolygon = mapPolygon;
    }
    
    public MapPolygon getMapPolygon() {
        return mapPolygon;
    }
    
    public void setMapPolygon(MapPolygon mapPolygon) {
        this.mapPolygon = mapPolygon;
    }
    
    public void setInfoWindow(GoogleMapInfoWindow infoWindow) {
        if (this.infoWindow == null)
            this.infoWindow = infoWindow; 
    }
    
    public GoogleMapInfoWindow getInfoWindow() {
        return infoWindow;
    }
}
