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

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class Constants {
    public static class Property {
        public static String API_KEY = "apiKey";
        public static String CLIENT_ID = "clientId";
        public static String LIBRARIES = "libraries";
        public static String LAT = "lat";
        public static String LNG = "lng";
        public static String ZOOM = "zoom";
        public static String WIDTH = "width";
        public static String MIN_WIDTH = "min-width";
        public static String HEIGHT = "height";
        public static String MIN_HEIGHT = "min-height";
        public static String STROKE_COLOR = "strokeColor";
        public static String STROKE_OPACITY = "strokeOpacity";
        public static String STROKE_WEIGHT = "strokeWeight";
        public static String ICON = "icon";
        public static String TITLE = "title";
        public static String LABEL = "label";
        public static String _DRAGGABLE = "_draggable";
        public static String DRAGGABLE = "draggable";
        public static String VISIBLE = "visible";
        public static String PATH = "path";
        public static String EDITABLE = "editable";
        public static String MAP_TYPE_ID = "mapTypeId";
    }
    
    public static class Default {
        public static double LAT = 2.4573831;
        public static double LNG = -76.6699746;
        public static double ZOOM = 10;
        public static String STROKE_COLOR = "#FF0000";
        public static double STROKE_OPACITY = 1.0;
        public static double STROKE_WEIGHT = 2;
        public static boolean DRAGGABLE = false;
        public static boolean VISIBLE = true;
        public static boolean EDITABLE = false;
        public static String MAP_TYPE_ID = MapTypeId.ROADMAP;
    }
    public static class MapTypeId {
        public static String HYBRID = "hybrid";
        public static String ROADMAP = "roadmap";
        public static String SATELLITE = "satellite";
        public static String TERRAIN = "terrain";
    }
}
