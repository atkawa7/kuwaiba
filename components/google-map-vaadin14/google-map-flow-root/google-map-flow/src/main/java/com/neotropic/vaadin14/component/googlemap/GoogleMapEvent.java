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
package com.neotropic.vaadin14.component.googlemap;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GoogleMapEvent {
    //<editor-fold desc="Map" defaultstate="collapsed">
    @DomEvent("map-click")
    public static class MapClickEvent extends ComponentEvent<GoogleMap> {
        private final double lat;        
        private final double lng;

        public MapClickEvent(GoogleMap source, boolean fromClient,
            @EventData("event.detail.lat") double lat, @EventData("event.detail.lng") double lng) {
            super(source, fromClient);
            this.lat = lat;
            this.lng = lng;  
        }
        
        public double getLat() {
            return lat;
        }
        
        public double getLng() {
            return lng;
        }
    }
    
    @DomEvent("map-dbl-click")
    public static class MapDblClickEvent extends ComponentEvent<GoogleMap> {
//        private final double lat;        
//        private final double lng;
        
        public MapDblClickEvent(GoogleMap source, boolean fromClient/*, 
            @EventData("detail.lat") double lat, @EventData("detail.lng") double lng*/) {
            super(source, fromClient);
//            this.lat = lat;
//            this.lng = lng;                    
        }
        
//        public double getLat() {
//            return lat;
//        }
//        
//        public double getLng() {
//            return lng;
//        }
    }
    
    @DomEvent("map-right-click")
    public static class MapRightClickEvent extends ComponentEvent<GoogleMap> {

        public MapRightClickEvent(GoogleMap source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    @DomEvent("map-center-changed")
    public static class MapCenterChangedEvent extends ComponentEvent<GoogleMap> {

        public MapCenterChangedEvent(GoogleMap source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    @DomEvent("map-mouse-move")
    public static class MapMouseMoveEvent extends ComponentEvent<GoogleMap> {

        public MapMouseMoveEvent(GoogleMap source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    @DomEvent("map-mouse-out")
    public static class MapMouseOutEvent extends ComponentEvent<GoogleMap> {

        public MapMouseOutEvent(GoogleMap source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    @DomEvent("map-mouse-over")
    public static class MapMouseOverEvent extends ComponentEvent<GoogleMap> {

        public MapMouseOverEvent(GoogleMap source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    @DomEvent("map-zoom-changed")
    public static class MapZoomChangedEvent extends ComponentEvent<GoogleMap> {

        public MapZoomChangedEvent(GoogleMap source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    //</editor-fold>
    //<editor-fold desc="Marker" defaultstate="collapsed">
    @DomEvent("marker-click")
    public static class MarkerClickEvent extends ComponentEvent<GoogleMapMarker> {
        public MarkerClickEvent(GoogleMapMarker source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    @DomEvent("marker-dbl-click")
    public static class MarkerDblClickEvent extends ComponentEvent<GoogleMapMarker> {
        public MarkerDblClickEvent(GoogleMapMarker source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    @DomEvent("marker-right-click")
    public static class MarkerRightClickEvent extends ComponentEvent<GoogleMapMarker> {
        public MarkerRightClickEvent(GoogleMapMarker source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    //</editor-fold>
}
