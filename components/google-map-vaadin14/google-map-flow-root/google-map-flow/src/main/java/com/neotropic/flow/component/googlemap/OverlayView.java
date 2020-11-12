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
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("overlay-view")
@JsModule("./overlay-view.js")
public class OverlayView extends Component implements HasComponents {
        
    public OverlayView() {
    }
    
    public OverlayView(boolean mapBounds) {
        getElement().setProperty(Constants.Property.MAP_BOUNDS, mapBounds);
    }
    
    public OverlayView(LatLngBounds bounds) {
        if (bounds != null)
            getElement().setPropertyJson(Constants.Property.BOUNDS, bounds.toJson());
    }
    
    public LatLngBounds getBounds() {
        return new LatLngBounds(
            (JsonObject) getElement().getPropertyRaw(Constants.Property.BOUNDS)
        );
    }
    
    public void setBounds(LatLngBounds bounds) {
        if (bounds != null)
            getElement().setPropertyJson(Constants.Property.BOUNDS, bounds.toJson());
        else
            getElement().setPropertyJson(Constants.Property.BOUNDS, Json.createNull());
    }
    
    public boolean getMapBounds() {
        return getElement().getProperty(Constants.Property.MAP_BOUNDS, false);
    }
    
    public void setMapBounds(boolean mapBounds) {
        getElement().setProperty(Constants.Property.MAP_BOUNDS, mapBounds);
    }
    
    public void fromLatLngToDivPixel(LatLng latLng, Consumer<Point> consumer) {
        getElement()
            .executeJs("return this.fromLatLngToDivPixel($0, $1)", latLng.getLat(), latLng.getLng())
            .then(JsonObject.class, point -> {
                consumer.accept(new Point(
                    point.getNumber(Constants.Property.X), 
                    point.getNumber(Constants.Property.Y)
                ));
            });
    }
    
    public void fromDivPixelToLatLng(Point point, Consumer<LatLng> consumer) {
        getElement()
            .executeJs("return this.fromDivPixelToLatLng($0, $1)", point.getX(), point.getY())
            .then(JsonObject.class, latLng -> {
                consumer.accept(new LatLng(
                    latLng.getNumber(Constants.Property.LAT),
                    latLng.getNumber(Constants.Property.LNG)
                ));
            });
    }
    /**
     * Executes callback to get the pixel coordinates of the given geographical location.
     * @param geoCoordinates List of geographical coordinates.
     * @param callback Callback to get the pixel coordinates of the given geographical location.
     */
    public void fromLatLngToDivPixel(List<LatLng> geoCoordinates, Consumer<List<Point>> callback) {
        Objects.requireNonNull(geoCoordinates);
        Objects.requireNonNull(callback);
        if (!geoCoordinates.isEmpty()) {
            JsonArray array = Json.createArray();
            for (int i = 0; i < geoCoordinates.size(); i++)
                array.set(i, geoCoordinates.get(i).toJson());
            getElement()
                .executeJs("return this.projectionFromLatLngToDivPixel($0)", array) //NOI18N
                .then(JsonArray.class, pixelCoordinates -> {
                    if (pixelCoordinates != null && pixelCoordinates.length() > 0) {
                        List<Point> list = new ArrayList();
                        for (int i = 0; i < pixelCoordinates.length(); i++)
                            list.add(new Point(pixelCoordinates.getObject(i)));
                        callback.accept(list);
                    }
                    else
                        callback.accept(null);
                }
            );
        }
    }
    /**
     * Executes callback to get the pixel coordinates of the given geographical location.
     * @param geoCoordinates List of geographical coordinates.
     * @param callback Callback to get the pixel coordinates of the given geographical location.
     */
    public void fromLatLngToDivPixel(HashMap<String, List<LatLng>> geoCoordinates, Consumer<HashMap<String, List<Point>>> callback) {
        Objects.requireNonNull(geoCoordinates);
        Objects.requireNonNull(callback);
        if (!geoCoordinates.isEmpty()) {
            String[] ids = geoCoordinates.keySet().toArray(new String[0]);
            /**
             * JSON Object
             * {
             *   "id": [{"lat": 0.0, "lng": 0.0}, ..],
             *   ..
             * }
             */
            JsonObject object = Json.createObject();
            for (int i = 0; i < ids.length; i++) {
                List<LatLng> coordinates = geoCoordinates.get(ids[i]);
                if (!coordinates.isEmpty()) {
                    JsonArray jsonCoordinates = Json.createArray();
                    for (int j = 0; j < coordinates.size(); j++) {
                        LatLng coordinate = coordinates.get(j);
                        JsonObject jsonCoordinate = Json.createObject();
                        jsonCoordinate.put(Constants.JsonKey.LAT, coordinate.getLat());
                        jsonCoordinate.put(Constants.JsonKey.LNG, coordinate.getLng());
                        jsonCoordinates.set(j, jsonCoordinate);
                    }
                    object.put(ids[i], jsonCoordinates);
                }
            }
            if (object.keys().length > 0) {
                getElement()
                    .executeJs("return this.projectionMapFromLatLngToDivPixel($0)", object)
                    .then(JsonObject.class, pixelCoordinates -> {
                        /**
                         * pixelCoordinates
                         * {
                         *   "id": [{"x": 0.0, "y": 0.0}, ..],
                         *   ..
                         * }
                         */
                        if (pixelCoordinates != null && pixelCoordinates.keys().length > 0) {
                            HashMap<String, List<Point>> points = new HashMap();
                            for (int i = 0; i < pixelCoordinates.keys().length; i++) {
                                String id = pixelCoordinates.keys()[i];
                                JsonArray jsonPoints = pixelCoordinates.getArray(id);
                                List<Point> idPoints = new ArrayList();
                                for (int j = 0; j < jsonPoints.length(); j++) {
                                    JsonObject jsonPoint = jsonPoints.getObject(j);
                                    idPoints.add(new Point(
                                        jsonPoint.getNumber(Constants.JsonKey.X),
                                        jsonPoint.getNumber(Constants.JsonKey.Y)
                                    ));
                                }
                                points.put(id, idPoints);
                            }
                            callback.accept(points);
                        }
                        else
                            callback.accept(null);
                    }
                );
            }
        }
    }
    /**
     * Executes callback to get the geographical coordinates from pixel coordinates.
     * @param pixelCoordinates List of pixel coordinates.
     * @param callback Callback to get the geographical coordinates from pixel coordinates.
     */
    public void fromDivPixelToLatLng(List<Point> pixelCoordinates, Consumer<List<LatLng>> callback) {
        Objects.requireNonNull(pixelCoordinates);
        Objects.requireNonNull(callback);
        if (!pixelCoordinates.isEmpty()) {
            JsonArray array = Json.createArray();
            for (int i = 0; i < pixelCoordinates.size(); i++)
                array.set(i, pixelCoordinates.get(i).toJson());
            getElement()
                .executeJs("return this.projectionFromDivPixelToLatLng($0)", array) //NOI18N
                .then(JsonArray.class, geoCoordinates -> {
                    if (geoCoordinates != null && geoCoordinates.length() > 0) {
                        List<LatLng> list = new ArrayList();
                        for (int i = 0; i < geoCoordinates.length(); i++)
                            list.add(new LatLng(geoCoordinates.getObject(i)));
                        callback.accept(list);
                    }
                    else
                        callback.accept(null);
                }
            );
        }
    }
    /**
     * Executes callback to get the geographical coordinates from pixel coordinates.
     * @param pixelCoordinates List of pixel coordinates.
     * @param callback Callback to get the geographical coordinates from pixel coordinates.
     */
    public void fromDivPixelToLatLng(HashMap<String, List<Point>> pixelCoordinates, Consumer<HashMap<String, List<LatLng>>> callback) {
        Objects.requireNonNull(pixelCoordinates);
        Objects.requireNonNull(callback);
        if (!pixelCoordinates.isEmpty()) {
            String[] ids = pixelCoordinates.keySet().toArray(new String[0]);
            /**
             * JSON Object
             * {
             *   "id": [{"x": 0.0, "y": 0.0}, ..],
             *   ..
             * }
             */
            JsonObject object = Json.createObject();
            for (int i = 0; i < ids.length; i++) {
                List<Point> points = pixelCoordinates.get(ids[i]);
                if (!points.isEmpty()) {
                    JsonArray jsonPoints = Json.createArray();
                    for (int j = 0; j < points.size(); j++) {
                        Point point = points.get(j);
                        JsonObject jsonPoint = Json.createObject();
                        jsonPoint.put(Constants.JsonKey.X, point.getX());
                        jsonPoint.put(Constants.JsonKey.Y, point.getY());
                        jsonPoints.set(j, jsonPoint);
                    }
                    object.put(ids[i], jsonPoints);
                }
            }
            if (object.keys().length > 0) {
                getElement()
                    .executeJs("return this.projectionMapFromDivPixelToLatLng($0)", object)
                    .then(JsonObject.class, geoCoordinates -> {
                        /**
                         * geoCoordinates
                         * {
                         *   "id": [{"lat": 0.0, "lng": 0.0}, ..]
                         *   ..
                         * }
                         */
                        if (geoCoordinates != null && geoCoordinates.keys().length > 0) {
                            HashMap<String, List<LatLng>> coordinates = new HashMap();
                            for (int i = 0; i < geoCoordinates.keys().length; i++) {
                                String id = geoCoordinates.keys()[i];
                                JsonArray jsonCoordinates = geoCoordinates.getArray(id);
                                List<LatLng> idCoordinates = new ArrayList();
                                for (int j = 0; j < jsonCoordinates.length(); j++) {
                                    JsonObject jsonCoordinate = jsonCoordinates.getObject(j);
                                    idCoordinates.add(new LatLng(
                                        jsonCoordinate.getNumber(Constants.JsonKey.LAT),
                                        jsonCoordinate.getNumber(Constants.JsonKey.LNG)
                                    ));
                                }
                                coordinates.put(id, idCoordinates);
                            }
                            callback.accept(coordinates);
                        }
                        else
                            callback.accept(null);
                    }
                );
            }
        }
    }
    public Registration addWidthChangedListener(ComponentEventListener<GoogleMapEvent.OverlayViewWidthChangedEvent> listener) {
        return addListener(GoogleMapEvent.OverlayViewWidthChangedEvent.class, listener);
    }
}
