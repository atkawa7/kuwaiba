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
package com.neotropic.kuwaiba.modules.commercial.ospman.providers.google;

import com.neotropic.flow.component.googlemap.GoogleMap;
import com.neotropic.flow.component.googlemap.GoogleMapPolyline;
import com.neotropic.flow.component.googlemap.LatLng;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapConstants;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Helper to draw a edge.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class EdgeHelper {
    private GoogleMapNode source;
    private GoogleMapNode target;
    private final GoogleMap googleMap;
    private final BiConsumer<HashMap<String, Object>, Runnable> callbackEdgeComplete;
    private final Collection<GoogleMapNode> nodes;
    private final Collection<GoogleMapEdge> edges;
    private final List<LatLng> path = new ArrayList();
    private final HashMap<GoogleMapNode, Registration> clickRegistrations = new HashMap();
    private Registration mapMouseMoveRegistration;
    private GoogleMapPolyline polyline;
    private GoogleMapPolyline tmpPolyline;
    private Registration mapClickListenerRegistration;
    private Registration mapRightClickListenerRegistration;

    protected EdgeHelper(
        BiConsumer<HashMap<String, Object>, Runnable> callbackEdgeComplete, 
        Collection<GoogleMapNode> nodes, 
        Collection<GoogleMapEdge> edges,
        GoogleMap googleMap) {

        Objects.requireNonNull(callbackEdgeComplete);
        Objects.requireNonNull(nodes);
        Objects.requireNonNull(edges);
        Objects.requireNonNull(googleMap);
        this.googleMap = googleMap;
        this.callbackEdgeComplete = callbackEdgeComplete;
        this.nodes = nodes;
        this.edges = edges;
    }
    
    public void init() {
        googleMap.setClickableIcons(false);
        source = null;
        target = null;
        polyline = null;
        path.clear();
        
        mapMouseMoveRegistration = googleMap.addMapMouseMoveListener(mapMouseMoveEvent -> {
            if (source != null) {
                if (path.size() > 1)
                    path.remove(path.size() - 1);
                
                path.add(new LatLng(mapMouseMoveEvent.getLat(), mapMouseMoveEvent.getLng()));
                
                if (polyline == null) {
                    polyline = new GoogleMapPolyline();
                    polyline.setClickable(false);
                    polyline.setPath(path);
                    googleMap.newPolyline(polyline);
                }
                else
                    polyline.setPath(path);
            }
        });
        mapClickListenerRegistration = googleMap.addMapClickListener(mapClickListenerEvent -> {
            if (source != null && polyline != null) {
                path.add(path.size() - 1, new LatLng(mapClickListenerEvent.getLat(), mapClickListenerEvent.getLng()));
                polyline.setPath(path);
            }
        });
        mapRightClickListenerRegistration = googleMap.addMapRightClickListener(mapRightClickListenerEvent -> {
            cancel();
            init();
        });
        
        nodes.forEach(node -> {
            node.setDraggable(false);
            node.setClickableNode(false);
            clickRegistrations.put(node, node.addMarkerClickListener(markerClickEvent -> {
                markerClickEvent.unregisterListener();
                clickRegistrations.remove(node);

                if (source == null) {
                    source = node;
                    path.add(new LatLng(source.getLat(), source.getLng()));
                } else if (target == null) {
                    cancel();
                    target = node;

                    List<GeoCoordinate> controlPoints = new ArrayList();

                    path.remove(path.size() - 1);
                    path.add(new LatLng(target.getLat(), target.getLng()));

                    path.forEach(latLng -> 
                        controlPoints.add(new GeoCoordinate(latLng.getLat(), latLng.getLng()))
                    );
                    HashMap<String, Object> parameters = new HashMap();
                    parameters.put(MapConstants.BUSINESS_OBJECT_SOURCE, source.getViewNode().getIdentifier());
                    parameters.put(MapConstants.BUSINESS_OBJECT_TARGET, target.getViewNode().getIdentifier());
                    parameters.put(MapConstants.PROPERTY_CONTROL_POINTS, controlPoints);

                    tmpPolyline = new GoogleMapPolyline();
                    tmpPolyline.setClickable(false);
                    tmpPolyline.setPath(path);
                    googleMap.newPolyline(tmpPolyline);

                    callbackEdgeComplete.accept(
                        parameters, 
                        () -> {
                            googleMap.removePolyline(tmpPolyline);
                            tmpPolyline = null;
                            init();
                        }
                    );
                }
            }));
        });
        edges.forEach(edge -> {
            edge.setEditable(false);
            edge.setClickable(false);
            edge.setClickableEdge(false);
        });
    }

    public void cancel() {
        googleMap.setClickableIcons(true);
        if (mapMouseMoveRegistration != null)
            mapMouseMoveRegistration.remove();

        if (mapClickListenerRegistration != null)
            mapClickListenerRegistration.remove();

        if (mapRightClickListenerRegistration != null)
            mapRightClickListenerRegistration.remove();

        if (polyline != null)
            googleMap.removePolyline(polyline);

        if (tmpPolyline != null)
            googleMap.removePolyline(tmpPolyline);

        clickRegistrations.values().forEach(clickRegistration -> clickRegistration.remove());
        clickRegistrations.clear();
        nodes.forEach(node -> {
            node.setDraggable(true);
            node.setClickableNode(true);
        });
        edges.forEach(edge -> {                
            edge.setClickable(true);
            edge.setClickableEdge(true);
        });
    }
}