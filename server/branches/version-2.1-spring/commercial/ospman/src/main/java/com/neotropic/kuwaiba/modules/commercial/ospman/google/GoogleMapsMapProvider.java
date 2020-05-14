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

import com.vaadin.flow.component.Component;
import java.util.List;
import java.util.Properties;
import com.neotropic.flow.component.googlemap.GoogleMap;
import com.neotropic.kuwaiba.modules.commercial.ospman.AbstractMapProvider;
import com.neotropic.kuwaiba.modules.commercial.ospman.GeoCoordinate;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewEventListener;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GoogleMapsMapProvider extends AbstractMapProvider {
    /**
     * The map component.
     */
    private GoogleMap googleMap;
    
    @Override
    public void initialize(Properties properties) {
        String apiKey = (String) properties.get("apiKey"); //NOI18N
        String clientId = (String) properties.get("clientId"); //NOI18N
        GeoCoordinate center = (GeoCoordinate) properties.get("center"); //NOI18N
        int zoom = (int) properties.get("zoom"); //NOI18N
        
        this.googleMap = new GoogleMap(apiKey, clientId);
        this.googleMap.setCenterLat(center.getLatitude());
        this.googleMap.setCenterLng(center.getLongitude());
        this.googleMap.setZoom(zoom);
    }

    @Override
    public void reload(Properties properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addMarker(BusinessObjectLight businessObject, GeoCoordinate position, String iconUrl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeMarker(BusinessObjectLight businessObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPolyline(BusinessObjectLight businessObject, BusinessObjectLight sourceObject, BusinessObjectLight targetObject, List<GeoCoordinate> controlPoints, Properties properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removePolyline(BusinessObjectLight businessObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<OSPNode> getMarkers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<OSPEdge> getPolylines() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getZoom() {
        return (int) this.googleMap.getZoom();
    }

    @Override
    public GeoCoordinate getCenter() {
        return new GeoCoordinate(
            this.googleMap.getCenterLat(), 
            this.googleMap.getCenterLng()
        );
    }

    @Override
    public Component getComponent() {
        return this.googleMap;
    }
    
    @Override
    public void removeListeners() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addMarkerClickListener(ViewEventListener ev) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addMarkerRightClickListener(ViewEventListener ev) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPolylineClickListener(ViewEventListener ev) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPolylineRightClickListener(ViewEventListener ev) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
