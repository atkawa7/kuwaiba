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
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Utility functions to polygons and polylines.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GeometryPoly {
    /**
     * GoogleMap component
     */
    private final GoogleMap googleMap;
    
    public GeometryPoly(GoogleMap googleMap) {
        Objects.requireNonNull(googleMap);
        this.googleMap = googleMap;
    }
    /**
     * Callback to calculate whether the given point exist inside the specified path.
     * @param point Point to calculate if exist inside the specified path.
     * @param paths Polygon paths.
     * @param callback Callback to execute.
     */
    public void callbackContainsLocation(LatLng point, List<List<LatLng>> paths, Consumer<Boolean> callback) {
        Objects.requireNonNull(point);
        Objects.requireNonNull(paths);
        Objects.requireNonNull(callback);
        
        googleMap.getElement().executeJs("return this.createGeometryPoly().containsLocation($0, $1)", //NOI18N
            point.toJson(), getPaths(paths))
            .then(Boolean.class, result -> callback.accept(result));
    }
    /**
     * Callback to calculate whether the given points exist inside the specified path.
     * @param points Points to calculate if exist inside the specified path.
     * @param paths Polygon paths.
     * @param callback Callback to execute.
     */
    public void callbackContainsLocations(HashMap<String, LatLng> points, List<List<LatLng>> paths, Consumer<HashMap<String, Boolean>> callback) {
        Objects.requireNonNull(points);
        Objects.requireNonNull(paths);
        Objects.requireNonNull(callback);
        
        JsonObject jsonPoints = Json.createObject();
        points.forEach((id, point) -> jsonPoints.put(id, point.toJson()));
        
        googleMap.getElement().executeJs("return this.createGeometryPoly().containsLocations($0, $1)",
            jsonPoints, getPaths(paths))
            .then(JsonObject.class, result -> {
                HashMap<String, Boolean> contains = new HashMap();
                points.keySet().forEach(id -> contains.put(id, result.getBoolean(id)));
                callback.accept(contains);
            });
    }
    /**
     * Callback to calculate whether the given point exist inside the specified path.
     * @param point Point to calculate if exist inside the specified path.
     * @param paths Polyline path or polygon paths.
     * @param tolerance Null to use the default value 10e-9.
     * @param isPolyline True if is polyline.
     * @param callback Callback to execute.
     */
    public void callbackIsLocationOnEdge(LatLng point, List<List<LatLng>> paths, Double tolerance, Boolean isPolyline, Consumer<Boolean> callback) {
        Objects.requireNonNull(point);
        Objects.requireNonNull(paths);
        Objects.requireNonNull(isPolyline);
        Objects.requireNonNull(callback);
        
        if (tolerance == null) {
            googleMap.getElement().executeJs("return this.createGeometryPoly().isLocationOnEdge($0, $1, $2)", //NOI18N
                point.toJson(), getPaths(paths), isPolyline)
                .then(Boolean.class, result -> callback.accept(result));
        } else {
            googleMap.getElement().executeJs("return this.createGeometryPoly().isLocationOnEdge($0, $1, $2, $3)", //NOI18N
                point.toJson(), getPaths(paths), isPolyline, tolerance)
                .then(Boolean.class, result -> callback.accept(result));
        }
    }
    /**
     * Gets a JSON Array.
     * @param paths Polyline path or polygon paths.
     * @retrurn JSON Array.
     */
    private JsonArray getPaths(List<List<LatLng>> paths) {
        Objects.requireNonNull(paths);
        JsonArray array = Json.createArray();
        for (int i = 0; i < paths.size(); i++) {
            JsonArray path = Json.createArray();
            for (int j = 0; j < paths.get(i).size(); j++)
                path.set(j, paths.get(i).get(j).toJson());
            array.set(i, path);
        }
        return array;
    }
}
