/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.osp.google.overlays;

import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.web.modules.osp.measure.ConnectionUtils;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Polygon extends GoogleMapPolygon implements PropertyChangeListener, Overlay {
    private List<PropertyChangeListener> propertyChangeListeners;
    private List<PropertyChangeListener> removedListeners;
    private boolean removed = false;
    
    private static final String defaultPolygonColor = "#AAD400";
    private static final String selectedPolygonColor  = "yellow";
    private String customPolygonColor;
    
    private final Polyline polyline;
    private boolean editable;
    
    private boolean removedFromView = false;
    
    public Polygon(PointMarker point, PropertyChangeListener propertyChangeListener) {
        polyline = new PolylineImpl();
        polyline.setStrokeColor(Polyline.defaultPolylineColor);
        
        customPolygonColor = Polygon.defaultPolygonColor;
        this.setFillColor(customPolygonColor);
        this.setFillOpacity(0.5);
        this.setStrokeColor(customPolygonColor);
        
        
        point.addPropertyChangeListener(this);
        polyline.getPoints().add(point);
                
        addPropertyChangeListener(propertyChangeListener);
        
        polyline.addPropertyChangeListener(propertyChangeListener);
    }
    
    public boolean isEditable() {
        return editable;
    }
    
    public void setEditable(boolean editable) {
        this.editable = editable;
        
        if (polyline.getPoints().isEmpty())
            polyline.addPropertyChangeListener(this);
        
        polyline.setEditable(editable);
        
        if (editable) {            
            setFillColor(Polygon.selectedPolygonColor);
            setStrokeColor(Polygon.selectedPolygonColor);
            firePropertyChangeEvent("updatePolygon", null, this);
            
            firePropertyChangeEvent("showPolyline", null, polyline);
        }
        else {
            setFillColor(customPolygonColor);
            setStrokeColor(customPolygonColor);
            firePropertyChangeEvent("updatePolygon", null, this);
            
            firePropertyChangeEvent("hidePolyline", null, polyline);
        }
    }
    
    public Polyline getPolyline() {
        return polyline;
    }
    
    public void addPoint(PointMarker point) {
        List<PointMarker> points = polyline.getPoints();
        
        points.add(point);
        
        if (points.size() >= 2) { 
            if (points.size() == 2) { // size = 2 can draw a polyline
                for (PointMarker pointMarker : points)
                    polyline.getCoordinates().add(pointMarker.getPosition());
                
                firePropertyChangeEvent("addPolyline", null, polyline);
            }
            else {
                if (points.size() >= 3) { 
                    if (points.size() == 3) { // size = 3 can draw a polygon
                        for (PointMarker pointMarker : points)
                            getCoordinates().add(pointMarker.getPosition());
                        firePropertyChangeEvent("addPolygon", null, this);
                    }
                    else {
                        getCoordinates().add(point.getPosition());
                        firePropertyChangeEvent("updatePolygon", null, this);
                    }
                }
                polyline.getCoordinates().add(point.getPosition());
                firePropertyChangeEvent("updatePolyline", null, polyline);
            }
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeListeners == null)
            propertyChangeListeners = new ArrayList<>();
        if (propertyChangeListeners.contains(listener))
            return;
        propertyChangeListeners.add(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeListeners == null)
            return;
        
        if (removedListeners == null)
            removedListeners = new ArrayList();
        if (removedListeners.contains(listener))
            return;
        removedListeners.add(listener);
    }
    /**
     * Use this method only when the polygon are remove by code.
     */
    public void removeAllPropertyChangeListener() {
        if (propertyChangeListeners == null)
            return;
        
        propertyChangeListeners.clear();
    }
    
    public void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
        if (propertyChangeListeners == null)
            return;
        
        for (PropertyChangeListener listener : propertyChangeListeners) {
            listener.propertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
            if (removed)
                break;
        }
        if (removed) {
            propertyChangeListeners.clear();
            return;
        }
            
        if (removedListeners != null && !removedListeners.isEmpty()) {
            for (PropertyChangeListener listener : removedListeners)
                propertyChangeListeners.remove(listener);
            removedListeners.clear();
        }
    }

    @Override
    public void removedFromView(boolean removed) {
        this.removed = removed;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("updatePolyline")) {
            getCoordinates().clear();
            
            for (int i = 0; i < polyline.getCoordinates().size() - 1; i += 1)
                getCoordinates().add(polyline.getCoordinates().get(i));      
            
            firePropertyChangeEvent("updatePolygon", null, this);
            return;
        }
        
        if (evt.getPropertyName().equals("markerClicked")) {
            Marker point = (PointMarker) evt.getSource();
            point.removedFromView(true);
            
            polyline.getCoordinates().add(point.getPosition());
            
            firePropertyChangeEvent("hidePolyline", null, polyline);
            firePropertyChangeEvent("removeMarkers", null, polyline.getPoints());
                
            polyline.getPoints().clear();
            
            firePropertyChangeEvent("endingPolygonDraw", null, null);
            return;
        }
    }

    @Override
    public boolean getRemoved() {
        return removed;
    }
    
    private class PolylineImpl extends Polyline {

        @Override
        void enableEdition() {
            List<PointMarker> viewablePoints = new ArrayList();
            for (int i = 0; i < points.size() - 1; i += 1)
                viewablePoints.add(points.get(i));
            
            if (isEditable())
                firePropertyChangeEvent("showMarkers", null, viewablePoints);
            else
                firePropertyChangeEvent("hideMarkers", null, viewablePoints);
        }

        @Override
        void setSpecialPoints() {
            points.get(0).setIsSpecial(true);
            
            points.get(points.size() - 1).setIsSpecial(true);
            points.get(points.size() - 1).setVisible(false);
            firePropertyChangeEvent("updateMarker", null, points.get(points.size() - 1));
        }

        @Override
        void updateSpecialPoints(PropertyChangeEvent evt) {
            if (evt.getSource().equals(points.get(0)) && 
                    evt.getPropertyName().equals("updateMarker")) {
                PointMarker firstPoint = points.get(0);
                
                LatLon firstPosition = firstPoint.getPosition();
                
                LatLon secondPosition = points.get(2).getPosition();
                
                PointMarker firstMidpoint = points.get(1);
                LatLon firstMidPosition = ConnectionUtils
                        .midPoint(firstPosition, secondPosition);
                firstMidpoint.setPosition(firstMidPosition);
                
                PointMarker lastPoint = points.get(points.size() - 1);
                lastPoint.setPosition(firstPosition);
                                
                LatLon secondLastPosition = points.get(points.size() - 3)
                        .getPosition();
                
                PointMarker lastMidpoint = points.get(points.size() - 2);
                LatLon lastMidPosition = ConnectionUtils
                        .midPoint(firstPosition, secondLastPosition);
                lastMidpoint.setPosition(lastMidPosition);
                                
                getCoordinates().get(0)
                        .setLat(firstPosition.getLat());
                getCoordinates().get(0)
                        .setLon(firstPosition.getLon());
                
                getCoordinates().get(getCoordinates().size() - 1)
                        .setLat(firstPosition.getLat());
                getCoordinates().get(getCoordinates().size() - 1)
                        .setLon(firstPosition.getLon());
                // better update markers
                List<PointMarker> markers = new ArrayList();
                markers.add(firstMidpoint);
                markers.add(lastPoint);
                markers.add(lastMidpoint);
                
                firePropertyChangeEvent("updateMarkers", null, markers);
                firePropertyChangeEvent("updatePolyline", null, this);
                return;
            }
            if (evt.getSource().equals(points.get(0)) && 
                    evt.getPropertyName().equals("removeMarker")) {                
                
                PointMarker lastPoint = points.get(points.size() - 1);
                PointMarker secondLastPoint = points.get(points.size() - 3);
                PointMarker secondPoint = points.get(2);
                secondPoint.setIsSpecial(true);
                                
                lastPoint.setPosition(secondPoint.getPosition());
                
                LatLon lastMidPosition = ConnectionUtils
                        .midPoint(secondPoint.getPosition(), secondLastPoint.getPosition());
                // last midpoint
                points.get(points.size() - 2).setPosition(lastMidPosition);
                
                List markers = new ArrayList();
                points.get(0).removedFromView(true); // dbl click over point
                markers.add(points.remove(1)); // first midpoint
                markers.add(points.remove(0)); // first point
                
                getCoordinates().remove(0);
                LatLon lastPosition = getCoordinates()
                        .get(getCoordinates().size() - 1);
                lastPosition.setLat(secondPoint.getPosition().getLat());
                lastPosition.setLon(secondPoint.getPosition().getLon());
                
                
                firePropertyChangeEvent("removeMarkers", null, markers);
                firePropertyChangeEvent("updateMarker", null, lastPoint);
                firePropertyChangeEvent("updatePolyline", null, this);
                return;
            }
        }
    }
}
