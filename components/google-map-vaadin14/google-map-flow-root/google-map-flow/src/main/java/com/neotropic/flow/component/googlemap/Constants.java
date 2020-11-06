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

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class Constants {
    public static class Property {
        public static String API_KEY = "apiKey"; //NOI18N
        public static String CLIENT_ID = "clientId"; //NOI18N
        public static String LIBRARIES = "libraries"; //NOI18N
        public static String LAT = "lat"; //NOI18N
        public static String LNG = "lng"; //NOI18N
        public static String ZOOM = "zoom"; //NOI18N
        public static String WIDTH = "width"; //NOI18N
        public static String MIN_WIDTH = "min-width"; //NOI18N
        public static String HEIGHT = "height"; //NOI18N
        public static String MIN_HEIGHT = "min-height"; //NOI18N
        public static String STROKE_COLOR = "strokeColor"; //NOI18N
        public static String STROKE_OPACITY = "strokeOpacity"; //NOI18N
        public static String STROKE_WEIGHT = "strokeWeight"; //NOI18N
        public static String ICON = "icon"; //NOI18N
        public static String TITLE = "title"; //NOI18N
        public static String LABEL = "label"; //NOI18N
        public static String _DRAGGABLE = "_draggable"; //NOI18N
        public static String DRAGGABLE = "draggable"; //NOI18N
        public static String VISIBLE = "visible"; //NOI18N
        public static String PATH = "path"; //NOI18N
        public static String PATHS = "paths"; //NOI18N
        public static String EDITABLE = "editable"; //NOI18N
        public static String MAP_TYPE_ID = "mapTypeId"; //NOI18N
        public static String DRAWING_MODE = "drawingMode"; //NOI18N
        public static String DRAWING_CONTROL = "drawingControl"; //NOI18N
        public static String DISABLE_DEFAULT_UI = "disableDefaultUi"; //NOI18N
        public static String ZOOM_CONTROL = "zoomControl"; //NOI18N
        public static String MAP_TYPE_CONTROL = "mapTypeControl"; //NOI18N
        public static String SCALE_CONTROL = "scaleControl"; //NOI18N
        public static String STREET_VIEW_CONTROL = "streetViewControl"; //NOI18N
        public static String ROTATE_CONTROL = "rotateControl"; //NOI18N
        public static String FULLSCREEN_CONTROL = "fullscreenControl"; //NOI18N
        public static String ANIMATION = "animation"; //NOI18N
        public static String DISABLE_AUTO_PAN = "disableAutoPan"; //NOI18N
        public static String MAX_WIDTH = "maxWidth"; //NOI18N
        public static String PIXEL_OFFSET = "pixelOffset"; //NOI18N
        public static String POSITION = "position"; //NOI18N
        public static String Z_INDEX = "zIndex"; //NOI18N
        public static String STYLES = "styles"; //NOI18N
        public static String BOUNDS = "bounds"; //NOI18N
        public static String EAST = "east"; //NOI18N
        public static String NORTH = "north"; //NOI18N
        public static String SOUTH = "south"; //NOI18N
        public static String WEST = "west"; //NOI18N
        public static String CLICKABLE = "clickable"; //NOI18N
        public static String FILL_COLOR = "fillColor"; //NOI18N
        public static String FILL_OPACITY = "fillOpacity"; //NOI18N
        public static String STROKE_POSITION = "strokePosition"; //NOI18N
        public static String X = "x"; //NOI18N
        public static String Y = "y"; //NOI18N
        public static String MAX_ZOOM = "maxZoom"; //NOI18N
        public static String MIN_ZOOM = "minZoom"; //NOI18N
    }
    
    public static class Default {
        public static double LAT = 2.4573831;
        public static double LNG = -76.6699746;
        public static double ZOOM = 10;
        public static String STROKE_COLOR = "#FF0000"; //NOI18N
        public static double STROKE_OPACITY = 1.0;
        public static double STROKE_WEIGHT = 2;
        public static boolean DRAGGABLE = false;
        public static boolean VISIBLE = true;
        public static boolean EDITABLE = false;
        public static String MAP_TYPE_ID = MapTypeId.ROADMAP;
        public static double FILL_OPACITY = 0.35;
        public static double Z_INDEX = 0;
    }
    public static class MapTypeId {
        public static String HYBRID = "hybrid"; //NOI18N
        public static String ROADMAP = "roadmap"; //NOI18N
        public static String SATELLITE = "satellite"; //NOI18N
        public static String TERRAIN = "terrain"; //NOI18N
    }
    public static class JsonKey {
        public static String X = "x"; //NOI18N
        public static String Y = "y"; //NOI18N
        public static String LAT = "lat"; //NOI18N
        public static String LNG = "lng"; //NOI18N
    }
}
