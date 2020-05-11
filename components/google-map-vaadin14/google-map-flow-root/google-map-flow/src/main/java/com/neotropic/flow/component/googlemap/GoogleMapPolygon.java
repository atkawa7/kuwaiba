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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("google-map-polygon")
@JsModule("./google-map-polygon.js")
public class GoogleMapPolygon extends Component {
    public GoogleMapPolygon() {
    }
    public GoogleMapPolygon(List<List<LatLng>> paths) {
        getElement().setPropertyJson(
            Constants.Property.PATHS, GoogleMapPolygon.pathsAsJsonArray(paths));
    }
    public static JsonValue pathsAsJsonArray(List<List<LatLng>> paths) {
        if (paths != null) {
            JsonArray arrayPaths = Json.createArray();
            for (int i = 0; i < paths.size(); i++) {
                JsonArray arrayPath = Json.createArray();
                List<LatLng> path = paths.get(i);
                for (int j = 0; j < path.size(); j++) {
                    JsonObject coordinate = Json.createObject();
                    coordinate.put("lat", path.get(j).getLat());
                    coordinate.put("lng", path.get(j).getLng());
                    arrayPath.set(j, coordinate);
                }
                arrayPaths.set(i, arrayPath);
            }
            return arrayPaths;
        }
        return Json.createNull();
    }
    public static List<List<LatLng>> pathsAsList(JsonArray paths) {
        if (paths != null) {
            List<List<LatLng>> listPaths = new ArrayList();
            for (int i = 0; i < paths.length(); i++) {
                List<LatLng> listPath = new ArrayList();
                JsonArray path = paths.getArray(i);
                for (int j = 0; j < path.length(); j++) {
                    JsonObject coordinate = path.getObject(j);
                    double lat = coordinate.getNumber("lat");
                    double lng = coordinate.getNumber("lng");
                    listPath.add(new LatLng(lat, lng));
                }
                listPaths.add(listPath);
            }
            return listPaths;
        }
        return null;
    }
    public boolean getEditable() {
        return getElement().getProperty(Constants.Property.EDITABLE, false);
    }
    public void setEditable(boolean editable) {
        getElement().setProperty(Constants.Property.EDITABLE, editable);
    }
    @Synchronize(property="paths", value="polygon-paths-changed")
    public List<List<LatLng>> getPaths() {
        return GoogleMapPolygon.pathsAsList(
            (JsonArray) getElement().getPropertyRaw(Constants.Property.PATHS)
        );
    }
    public void setPath(List<List<LatLng>> paths) {
        getElement().setPropertyJson(
            Constants.Property.PATHS, GoogleMapPolygon.pathsAsJsonArray(paths));
    }
    public Registration addPolygonClickListener(ComponentEventListener<GoogleMapEvent.PolygonClickEvent> listener) {
        return addListener(GoogleMapEvent.PolygonClickEvent.class, listener);
    }
    public Registration addPolygonDblClickListener(ComponentEventListener<GoogleMapEvent.PolygonDblClickEvent> listener) {
        return addListener(GoogleMapEvent.PolygonDblClickEvent.class, listener);
    }
    public Registration addPolygonMouseOutListener(ComponentEventListener<GoogleMapEvent.PolygonMouseOutEvent> listener) {
        return addListener(GoogleMapEvent.PolygonMouseOutEvent.class, listener);
    }
    public Registration addPolygonMouseOverListener(ComponentEventListener<GoogleMapEvent.PolygonMouseOverEvent> listener) {
        return addListener(GoogleMapEvent.PolygonMouseOverEvent.class, listener);
    }
    public Registration addPolygonRightClickListener(ComponentEventListener<GoogleMapEvent.PolygonRightClickEvent> listener) {
        return addListener(GoogleMapEvent.PolygonRightClickEvent.class, listener);
    }
    public Registration addPolygonPathsChangedListener(ComponentEventListener<GoogleMapEvent.PolygonPathsChangedEvent> listener) {
        return addListener(GoogleMapEvent.PolygonPathsChangedEvent.class, listener);
    }
}
