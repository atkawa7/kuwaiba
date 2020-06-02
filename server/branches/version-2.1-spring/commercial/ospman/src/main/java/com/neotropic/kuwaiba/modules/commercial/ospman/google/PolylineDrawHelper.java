/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ospman.google;

import com.neotropic.flow.component.googlemap.DrawingManager;
import com.neotropic.flow.component.googlemap.GoogleMap;
import com.neotropic.flow.component.googlemap.GoogleMapEvent.DrawingManagerPolylineCompleteEvent;
import com.neotropic.flow.component.googlemap.GoogleMapEvent.MarkerClickEvent;
import com.neotropic.flow.component.googlemap.GoogleMapEvent.MarkerMouseOverEvent;
import com.neotropic.flow.component.googlemap.GoogleMapMarker;
import com.neotropic.flow.component.googlemap.LatLng;
import com.neotropic.flow.component.googlemap.OverlayType;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A tool to help draws polylines.
 * Draws a polyline, start with a marker click event which active the
 * draw polyline tool of google maps, double click to end the polyline draw 
 * (complete polyline event) and an event of marker mouse over. 
 * 
 * To the final user, this process is a simple double click on the source marker, 
 * draw polyline and double click on the target marker.
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PolylineDrawHelper {
    /**
     * Detected source marker using marker click events.
     */
    private GoogleMapMarker source;
    /**
     * Got polyline path using polyline complete event.
     */
    private List<LatLng> path;
    /**
     * Detected target marker using marker mouse over events.
     */
    private GoogleMapMarker target;
    /**
     * Set of the marker click event registration.
     */
    private final List<Registration> registrationMarkerClickEventListener = new ArrayList();
    /**
     * Set of the polyline complete event registration.
     */
    private final List<Registration> registrationPolylineCompleteEventListener = new ArrayList();
    /**
     * Set of the marker mouse over event registration.
     */
    private final List<Registration> registrationMarkerMouseOverEventListener = new ArrayList();
    /**
     * Google Maps Flow Component.
     */
    private final GoogleMap googleMap;
    /**
     * Drawing Manager Flow Component child of the Google Maps Flow Component.
     */
    private final DrawingManager drawingManager;
    /**
     * Consumer to notify the polyline complete.
     */
    private final Consumer<PolylineDrawHelper> consumer;
    /**
     * Creates a new instance of PolylineDrawHelper class
     * @param googleMap Google Maps Flow Component
     * @param drawingManager Drawing Manager Flow Component child of the Google Maps Flow Component
     * @param consumer Consumer to notify the polyline complete
     */
    public PolylineDrawHelper(GoogleMap googleMap, DrawingManager drawingManager, Consumer<PolylineDrawHelper> consumer) {
        this.googleMap = googleMap;
        this.drawingManager = drawingManager;
        this.consumer = consumer;
    }
    /**
     * Starts the process to draw a polyline.
     */
    public void start() {
        cancel();
        addMarkerClickEventListener();
        addPolylineCompleteEventListener();
    }
    /**
     * Cancels the draw polyline process.
     */
    public void cancel() {
        source = null;
        path = null;
        target = null;
        unregisterListeners();
    }
    /**
     * Gets the source marker.
     * @return the source marker
     */
    public GoogleMapMarker getSource() {
        return source;
    }
    /**
     * Gets the polyline path.
     * @return the polyline path
     */
    public List<LatLng> getPath() {
        return path;
    }
    /**
     * Gets the target marker.
     * @return the target marker
     */
    public GoogleMapMarker getTarget() {
        return target;
    }
    /**
     * Adds a marker click event listener to all the markers in the map to 
     * detect the source marker.
     */
    private void addMarkerClickEventListener() {
        ComponentEventListener<MarkerClickEvent> markerClickListener = clickEvent -> {
            source = clickEvent.getSource();
            drawingManager.setDrawingMode(OverlayType.POLYLINE);
            unregisterMarkerClickEventListener();
            addMarkerMouseOverEventListener();
        };
        googleMap.getChildren().forEach(child -> {
            if (child instanceof GoogleMapMarker) {
                registrationMarkerClickEventListener.add(((GoogleMapMarker) child)
                    .addMarkerClickListener(markerClickListener)
                );
            }
        });
    }
    /**
     * Adds a polyline complete event listener to get the draw path.
     */
    private void addPolylineCompleteEventListener() {
        ComponentEventListener<DrawingManagerPolylineCompleteEvent> polylineCompleteEvent = event -> {
            path = event.getPath();
            drawingManager.setDrawingMode(null);
            unregisterPolylineCompleteListeners();
        };
        registrationPolylineCompleteEventListener.add(drawingManager
            .addDrawingManagerPolylineCompleteListener(polylineCompleteEvent)
        );
    }
    /**
     * Adds a marker mouse over event listener to all the markers in the map to 
     * detect the target marker.
     */
    private void addMarkerMouseOverEventListener() {
        ComponentEventListener<MarkerMouseOverEvent> markerMouseOverEventListener = mouseOverEvent -> {
            target = mouseOverEvent.getSource();
            unregisterMarkerMouseOverEventListener();
            
            path.set(0, new LatLng(source.getLat(), source.getLng()));
            path.set(path.size() - 1, new LatLng(target.getLat(), target.getLng()));
            
            consumer.accept(this);
        };
        googleMap.getChildren().forEach(child -> {
            if (child instanceof GoogleMapMarker) {
                registrationMarkerMouseOverEventListener.add(((GoogleMapMarker) child)
                    .addMarkerMouseOverListener(markerMouseOverEventListener)
                );
            }
        });
    }
    /**
     * Once the source marker is detected the registered marker click events 
     * are not necessary.
     */
    private void unregisterMarkerClickEventListener() {
        registrationMarkerClickEventListener.forEach(registration -> registration.remove());
        registrationMarkerClickEventListener.clear();
    }
    /**
     * Once the path is got the registered polyline complete events are not
     * necessary.
     */
    private void unregisterPolylineCompleteListeners() {
        registrationPolylineCompleteEventListener.forEach(registration -> 
            registration.remove()
        );
        registrationPolylineCompleteEventListener.clear();
    }
    /**
     * Once the target marker is detected the registered marker mouse over events 
     * are not necessary.
     */
    private void unregisterMarkerMouseOverEventListener() {
        registrationMarkerMouseOverEventListener.forEach(registration -> 
            registration.remove()
        );
        registrationMarkerMouseOverEventListener.clear();
    }
    /**
     * Removes unnecessary listener.
     * This can happen when the draw polyline tool is selected and then change 
     * the choice for another tool
     */
    public void unregisterListeners() {
        unregisterPolylineCompleteListeners();
        unregisterMarkerClickEventListener();
        unregisterMarkerMouseOverEventListener();
    }
}
