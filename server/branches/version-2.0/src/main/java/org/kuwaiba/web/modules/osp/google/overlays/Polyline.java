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
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.web.modules.osp.google.CustomGoogleMap;
import org.kuwaiba.web.modules.osp.measure.ConnectionUtils;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public abstract class Polyline extends GoogleMapPolyline implements PropertyChangeListener, Overlay {
    private List<PropertyChangeListener> propertyChangeListeners;
    private List<PropertyChangeListener> removedListeners;
    private boolean removed = false;
    
    public static final String defaultPolylineColor = "#AAD400";
    public static final String selectedPolylineColor = "yellow";
    
    public String customColor;
    /**
     * Control points
     */
    protected List<MarkerPoint> points;
    /**
     * The polyline can be edited
     */
    private boolean editable = false;
    
    private boolean removedFromView = false;
    
    public Polyline() {
        points = new ArrayList();
        setStrokeColor(defaultPolylineColor);
    }    
    
    public List<MarkerPoint> getPoints() {
        return points;
    }    
    
    public boolean isEditable() {
        return editable;
    }
    
    public void setEditable(boolean editable) {
        this.editable = editable;
        
        if (editable) {
            if (points.isEmpty()) {
                for (int i = 0; i < getCoordinates().size(); i += 1) {
                    LatLon leftPosition = getCoordinates().get(i);
                    
                    MarkerPoint leftPoint = new MarkerPoint(leftPosition, true);
                    
                    leftPoint.addPropertyChangeListener(this);                    
                    firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_ADD_MARKER, null, leftPoint);
                    points.add(leftPoint);                    
                    
                    if (i < getCoordinates().size() - 1) {
                        LatLon rightPosition = getCoordinates().get(i + 1);
                        
                        LatLon midPosition = ConnectionUtils
                                .midPoint(leftPosition, rightPosition);
                        
                        MarkerPoint midpoint = new MarkerPoint(midPosition, 
                                false);
                        midpoint.addPropertyChangeListener(this);
                        firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_ADD_MARKER, null, midpoint);
                        points.add(midpoint);
                    }
                }
                setSpecialPoints();
            }
            customColor = getStrokeColor();
            setStrokeColor(Polyline.selectedPolylineColor);
            firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_UPDATE_POLYLINE, null, this);
            
            enableEdition();
        }
        else {
            if (!points.isEmpty()) {
                setStrokeColor(customColor);
                firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_UPDATE_POLYLINE, null, this);
                
                enableEdition();
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
     * Use this method only when the marker are remove by code.
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
        if (evt == null || evt.getPropertyName() == null)
            return;
        
        if (!(evt.getSource() instanceof MarkerPoint)) {
            updateSpecialPoints(evt);
            return;
        }
                
        if (evt.getPropertyName().equals(CustomGoogleMap.GM_EVENT_NAME_UPDATE_MARKER)) {
            MarkerPoint point = (MarkerPoint) evt.getSource();
            
            if (point.isSpecial()) {
                updateSpecialPoints(evt);
                return;
            }
            
            int pointIdx = points.indexOf(point);
            
            if (point.isPoint()) {
                int coordinateIdx = getCoordinateIndex((LatLon) evt.getOldValue());
                
                LatLon coordinate = getCoordinates().get(coordinateIdx);
                coordinate.setLat(((LatLon) evt.getNewValue()).getLat());
                coordinate.setLon(((LatLon) evt.getNewValue()).getLon());
                    
                firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_UPDATE_POLYLINE, null, this);
                // position of current point
                LatLon latLonPoint = points.get(pointIdx).getPosition();
                if (pointIdx - 2 >= 0) { // Point is not the source point
                    // position of left point
                    LatLon latLonLPoint = points.get(pointIdx - 2).getPosition();
                    // left midpoint
                    points.get(pointIdx - 1).setPosition(
                            ConnectionUtils.midPoint(latLonLPoint, latLonPoint));
                        
                    firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_UPDATE_MARKER, null, 
                            points.get(pointIdx - 1));
                }
                if (pointIdx + 2 < points.size()) { // Point is not the target point
                    // position of right point
                    LatLon latLonRPoint = points.get(pointIdx + 2).getPosition();
                    // right midpoint
                    points.get(pointIdx + 1).setPosition(
                            ConnectionUtils.midPoint(latLonPoint, latLonRPoint));
                    
                    firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_UPDATE_MARKER, null, 
                            points.get(pointIdx + 1));
                }
            }
            else {
                LatLon latLonLPoint = points.get(pointIdx - 1).getPosition();
                LatLon latLonRPoint = points.get(pointIdx + 1).getPosition();
                    
                MarkerPoint leftMidpoint = new MarkerPoint(
                        ConnectionUtils.midPoint(latLonLPoint, 
                                point.getPosition()),false);
                
                leftMidpoint.addPropertyChangeListener(this);
                firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_ADD_MARKER, null, leftMidpoint);
                    
                MarkerPoint rightMidpoint = new MarkerPoint(
                        ConnectionUtils.midPoint(latLonRPoint, 
                                point.getPosition()), false);
                
                rightMidpoint.addPropertyChangeListener(this);
                firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_ADD_MARKER, null, rightMidpoint);
                                    
                points.add(pointIdx + 1, rightMidpoint);
                points.add(pointIdx, leftMidpoint);
                                        
                int newPointIdx = getCoordinateLastIndex(latLonRPoint);
                        
                getCoordinates().add(newPointIdx, point.getPosition());
                    
                firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_UPDATE_POLYLINE, null, this);
                    
                point.setIsPoint(true); // change to point
                firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_UPDATE_MARKER, null, point);
            }
            return;
        }
        
        if (evt.getPropertyName().equals(CustomGoogleMap.GM_EVENT_NAME_REMOVE_MARKER)) {
            MarkerPoint point = (MarkerPoint) evt.getSource();
            
            if (point.isSpecial()) {
                updateSpecialPoints(evt);
                return;
            }            
            int pointIdx = points.indexOf(point);
                        
            MarkerPoint rightMidpoint = points.remove(pointIdx + 1);
            rightMidpoint.removeAllPropertyChangeListener();
            
            MarkerPoint leftMidpoint = points.remove(pointIdx - 1);
            leftMidpoint.removeAllPropertyChangeListener();
                        
            List markers = new ArrayList();
            markers.add(rightMidpoint);
            markers.add(leftMidpoint);
            
            int coordinateIdx = getCoordinateIndex(point.getPosition());
            getCoordinates().remove(coordinateIdx);
            
            pointIdx -=  1;
            point.setIsPoint(false); // change to midpoint
            
            LatLon position = ConnectionUtils
                    .midPoint(points.get(pointIdx - 1).getPosition(), 
                            points.get(pointIdx + 1).getPosition());
            point.setPosition(position);
            
            firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_REMOVE_MARKERS, null, markers);
            firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_UPDATE_MARKER, null, point);
            firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_UPDATE_POLYLINE, null, this);
            // one bug when remove three point the polyline don't updtate
            // and you need click twice for see the change
            return;
        }
    }
    
    protected int getCoordinateLastIndex(LatLon latLon) {
        for (LatLon coordinate : getCoordinates())
            if (coordinate.equals(latLon))
                return getCoordinates().lastIndexOf(coordinate);
        return -1;
    }
    
    protected int getCoordinateIndex(LatLon latLon) {
        for (LatLon coordinate : getCoordinates())
            if (coordinate.equals(latLon))
                return getCoordinates().indexOf(coordinate);
        return -1;
    }
    
    @Override
    public boolean getRemoved() {
        return removed;
    }
        
    abstract void enableEdition();
    
    abstract void setSpecialPoints();
    
    abstract void updateSpecialPoints(PropertyChangeEvent evt);
}
