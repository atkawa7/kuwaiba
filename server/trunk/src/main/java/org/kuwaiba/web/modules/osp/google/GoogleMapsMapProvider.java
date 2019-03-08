/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.web.modules.osp.google;

import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.web.modules.osp.AbstractMapProvider;
import org.kuwaiba.web.modules.osp.GeoCoordinate;
import com.vaadin.tapio.googlemaps.GoogleMapsComponent;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.AbstractComponent;
import java.util.ArrayList;
import java.util.HashMap;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.views.ViewEventListener;
import org.kuwaiba.web.modules.osp.OSPConstants;

/**
 * A wrapper of the Google Maps Vaadin component.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class GoogleMapsMapProvider extends AbstractMapProvider {
    /**
     * The map component
     */
    private GoogleMapsComponent<BusinessObjectLight, BusinessObjectLight> map;
    /**
     * The list of nodes
     */
    private HashMap<BusinessObjectLight, GoogleMapMarker> nodes;
    /**
     * The list of edges
     */
    private HashMap<BusinessObjectLight, GoogleMapPolyline> edges;
    /**
     * A map with pairs connection - source node, being the source node, the business object behind the marker.
     */
    private HashMap<GoogleMapPolyline, BusinessObjectLight> sourceNodes;
    /**
     * A map with pairs connection - target node, being the target node, the business object behind the marker.
     */
    private HashMap<GoogleMapPolyline, BusinessObjectLight> targetNodes;
    /**
     * The list of node click listeners.
     */
    private List<ViewEventListener> nodeClickListeners;
    /**
     * The list of edge click listeners.
     */
    private List<ViewEventListener> edgeClickListeners;
    
    public GoogleMapsMapProvider() {
        this.nodes = new HashMap<>();
        this.edges = new HashMap<>();
        this.sourceNodes = new HashMap<>();
        this.targetNodes = new HashMap<>();
        this.nodeClickListeners = new ArrayList<>();
        this.edgeClickListeners = new ArrayList<>();
    }
    
    @Override
    public void initialize(Properties properties) {
        this.map = new GoogleMapsComponent((String)properties.get("apiKey"), null, 
                properties.get("language") != null ? (String)properties.get("language") : OSPConstants.DEFAULT_LANGUAGE);
        this.map.setZoom(properties.get("zoom") != null ? (int)properties.get("zoom") : OSPConstants.DEFAULT_ZOOM);
        GeoCoordinate center = (GeoCoordinate)properties.get("center");
        this.map.setCenter(properties.get("center") != null ? new LatLon(center.getLatitude(), 
                center.getLongitude()) : new LatLon(OSPConstants.DEFAULT_CENTER_LATITUDE, OSPConstants.DEFAULT_CENTER_LONGITUDE));
        this.map.showEdgeLabels(true);
        this.map.showMarkerLabels(true);
        this.map.setSizeFull();
    }
    
    @Override
    public void reload(Properties properties) {
        if (properties.get("zoom") != null)
            this.map.setZoom((int)properties.get("zoom"));
        if (properties.get("center") != null) 
            this.map.setCenter(new LatLon(((GeoCoordinate)properties.get("center")).getLatitude(), 
                        ((GeoCoordinate)properties.get("center")).getLongitude()));
        
    }
      
    @Override
    public void addMarker(BusinessObjectLight businessObject, GeoCoordinate position, String iconUrl) {
        nodes.put(businessObject, this.map.addMarker(businessObject, businessObject.toString(), 
                new LatLon(position.getLatitude(), position.getLongitude()), true, iconUrl));
    }

    @Override
    public void removeMarker(BusinessObjectLight businessObject) {
        GoogleMapMarker aMarker = this.nodes.get(businessObject);
        if (aMarker != null) {
            this.map.removeMarker(aMarker);
            this.nodes.remove(businessObject);
        }
    }
    
    @Override
    public void addPolyline(BusinessObjectLight businessObject, BusinessObjectLight sourceObject, BusinessObjectLight targetObject, 
            List<GeoCoordinate> controlPoints, Properties properties) {
        GoogleMapMarker sourceMarker = this.nodes.get(sourceObject);
        if (sourceMarker != null) {
            GoogleMapMarker targetMarker = this.nodes.get(targetObject);
            if (targetMarker != null) {
                List<LatLon> gMapsCoordinates = new ArrayList<>();
                if (controlPoints.isEmpty()) { //It's an entirely new connection without controlpoints. The addon requires that at least the endpoint coordinates are provided in this case
                    gMapsCoordinates.add(sourceMarker.getPosition());
                    gMapsCoordinates.add(targetMarker.getPosition());
                } else
                    controlPoints.forEach((aGeoCoordinate) -> {
                        gMapsCoordinates.add(new LatLon(aGeoCoordinate.getLatitude(), aGeoCoordinate.getLongitude()));
                    });
                
                GoogleMapPolyline aPolyline = new GoogleMapPolyline(businessObject.toString(), gMapsCoordinates);
                aPolyline.setStrokeWeight(3);
                aPolyline.setStrokeColor(properties.getProperty("color") == null ? "#000000" : properties.getProperty("color")); //NOI18N
                this.map.addEdge(businessObject, aPolyline, sourceMarker, targetMarker);
                this.sourceNodes.put(aPolyline, sourceObject);
                this.targetNodes.put(aPolyline, targetObject);
                this.edges.put(businessObject, aPolyline);
            }
        }
    }
    
    @Override
    public void removePolyline(BusinessObjectLight businessObject) {
        GoogleMapPolyline aPolyline = this.edges.get(businessObject);
        if (aPolyline != null) {
            this.map.removePolyline(aPolyline);
            this.edges.remove(businessObject);
            this.sourceNodes.remove(aPolyline);
            this.targetNodes.remove(aPolyline);
        }
    }

    @Override
    public List<OSPNode> getMarkers() {
        List<OSPNode> res = new ArrayList<>();
        this.nodes.entrySet().stream().forEach((anEntry) -> {
            res.add(new OSPNode(anEntry.getKey(), new GeoCoordinate(anEntry.getValue().getPosition().getLat(), 
                    anEntry.getValue().getPosition().getLon())));
        });
        return res;
    }

    @Override
    public List<OSPEdge> getPolylines() {
        List<OSPEdge> res = new ArrayList<>();
        this.edges.entrySet().stream().forEach((anEntry) -> {
            List<GeoCoordinate> controlPoints = new ArrayList<>();
            
            anEntry.getValue().getCoordinates().forEach((aGMapsCoordinate) -> {
                controlPoints.add(new GeoCoordinate(aGMapsCoordinate.getLat(), aGMapsCoordinate.getLon()));
            });
            
            res.add(new OSPEdge(anEntry.getKey(), sourceNodes.get(anEntry.getValue()), 
                    targetNodes.get(anEntry.getValue()), controlPoints));
        });
        return res;
    }
    
    @Override
    public void clear() {
        this.map.removeEdges();
        this.map.clearMarkers();
    }
    @Override
    public AbstractComponent getComponent() {
        return this.map;
    }

    @Override
    public int getZoom() {
        return this.map.getZoom();
    }

    @Override
    public GeoCoordinate getCenter() {
        return new GeoCoordinate(this.map.getCenter().getLat(), this.map.getCenter().getLon());
    }

    @Override
    public void addMarkerClickListener(ViewEventListener ev) {
        this.map.addMarkerClickListener((clickedMarker) -> {
            for (BusinessObjectLight businessObject : nodes.keySet()) {
                if (nodes.get(businessObject).equals(clickedMarker)) {
                    ev.eventProcessed(businessObject, ViewEventListener.EventType.TYPE_CLICK);
                    break;
                }
            }
        });
    }

    @Override
    public void addMarkerRightClickListener(ViewEventListener ev) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void addPolylineClickListener(ViewEventListener ev) {
        this.map.addEdgeClickListener((clickedPolyline) -> {
            for (BusinessObjectLight businessObject : edges.keySet()) {
                if (edges.get(businessObject).equals(clickedPolyline)) {
                    ev.eventProcessed(businessObject, ViewEventListener.EventType.TYPE_CLICK);
                    break;
                }
            }
        });
    }

    @Override
    public void addPolylineRightClickListener(ViewEventListener ev) {
        Notifications.showInfo("This event is not supported by this provider");
    }

    @Override
    public void removeListeners() {
        this.nodeClickListeners.clear();
        this.edgeClickListeners.clear();
    }

}
