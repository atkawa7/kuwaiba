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
import com.vaadin.ui.Component;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A wrapper of the Google Maps Vaadin component.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class GoogleMapsMapProvider extends AbstractMapProvider {
    /**
     * The map component
     */
    private GoogleMapsComponent map;
    /**
     * The list of nodes
     */
    private HashMap<BusinessObjectLight, GoogleMapMarker> nodes;
    /**
     * The list of edges
     */
    private HashMap<BusinessObjectLight, GoogleMapPolyline> edges;
    
    public GoogleMapsMapProvider() {
        this.nodes = new HashMap<>();
    }
    
    @Override
    public void initialize(Properties properties) {
        this.map = new GoogleMapsComponent((String)properties.get("apiKey"), null, (String)properties.get("language"));
    }
      
    @Override
    public void addMarker(BusinessObjectLight businessObject, GeoCoordinate position, String iconUrl) {
        nodes.put(businessObject, this.map.addMarker(businessObject.toString(), new LatLon(position.getLatitude(), position.getLongitude()), true, iconUrl));
    }

    @Override
    public void addPolyline(BusinessObjectLight businessObject, List<GeoCoordinate> controlPoints) {
        GoogleMapPolyline aPolyline = map.addPolyline(businessObject.toString());
        List<LatLon> gMapsCoordinates = new ArrayList<>();
        controlPoints.forEach((aGeoCoordinate) -> {
            gMapsCoordinates.add(new LatLon(aGeoCoordinate.getLatitude(), aGeoCoordinate.getLongitude()));
        });
        aPolyline.setCoordinates(gMapsCoordinates);
        edges.put(businessObject, aPolyline);
    }

    @Override
    public void connectPolylineSource(BusinessObjectLight edge, BusinessObjectLight sourceObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connectPolylineTarget(BusinessObjectLight edge, BusinessObjectLight targetObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<OSPNode> getMarkers() {
        List<OSPNode> res = new ArrayList<>();
        this.nodes.entrySet().stream().forEach((anEntry) -> {
            res.add(new OSPNode(anEntry.getKey(), new GeoCoordinate(anEntry.getValue().getPosition().getLat(), anEntry.getValue().getPosition().getLat())));
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
            
            res.add(new OSPEdge(anEntry.getKey(), controlPoints));
        });
        return res;
    }

    @Override
    public Component getComponent() {
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

}
