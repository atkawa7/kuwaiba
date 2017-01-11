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
import org.kuwaiba.web.custom.wizards.physicalconnection.PhysicalConnectionWizard;
import org.kuwaiba.web.modules.osp.measure.ConnectionUtils;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ConnectionPolyline extends GoogleMapPolyline implements PropertyChangeListener {
    private List<PropertyChangeListener> propertyChangeListeners;
    //private ObjectNode connection;
    private NodeMarker source;
    private NodeMarker target;
    private PhysicalConnectionWizard wizard;
    /**
     * Control points
     */
    private List<PointMarker> points;
    
    public ConnectionPolyline(NodeMarker source) {
        this.source = source;
        getCoordinates().add(source.getPosition());
        target = null;
    }
    /*
    public void setPoints(List<PointMarker> points) {
        if (this.points == null || this.points.size() != points.size())
            this.points = points;
    }
    */
                      
    public void enableEdition(boolean enableEdition) {
        if (enableEdition) {
            if (points == null) {
                points = new ArrayList();
                                
                for (int i = 0; i < getCoordinates().size(); i += 1) {
                    LatLon position1 = getCoordinates().get(i);
                    PointMarker pointMarker = new PointMarker(position1, true);
                    
                    if (position1.equals(source.getPosition()) || 
                            position1.equals(target.getPosition()))
                        pointMarker.setVisible(false);
                    
                    pointMarker.addPropertyChangeListener(this);
                    
                    firePropertyChangeEvent("addMarker", null, pointMarker);
                    points.add(pointMarker);
                        
                    if (i < getCoordinates().size() - 1) {
                        LatLon position2 = getCoordinates().get(i + 1);
                        LatLon midposition = ConnectionUtils.midPoint(position1, position2);

                        PointMarker midpoint = new PointMarker(midposition, false);
                        midpoint.addPropertyChangeListener(this);

                        firePropertyChangeEvent("addMarker", null, midpoint);
                        points.add(midpoint);
                    }
                }
            }
            else {
                List<PointMarker> pointsToShow = new ArrayList();
                
                for (int i = 1; i < points.size() - 1; i += 1)
                    pointsToShow.add(points.get(i));
                
                firePropertyChangeEvent("showMarkers", null, pointsToShow);
            }
                
        }
        else {
            if (points != null) {
                List<PointMarker> pointsToHide = new ArrayList();
                
                for (int i = 1; i < points.size() - 1; i += 1)
                    pointsToHide.add(points.get(i));
                
                firePropertyChangeEvent("hideMarkers", null, pointsToHide);
            }
        }
    }
        
    public List<PointMarker> getPoints() {
        return points;
    }
    
    public NodeMarker getSource() {
        return source;
    }

    public void setSource(NodeMarker source) {
        this.source = source;
    }

    public NodeMarker getTarget() {
        return target;        
    }

    public void setTarget(NodeMarker target) {
        this.target = target;
        getCoordinates().add(target.getPosition());
        
        this.source.addPropertyChangeListener(this);
        this.target.addPropertyChangeListener(this);
    }

    public PhysicalConnectionWizard getWizard() {
        return wizard;
    }

    public void setWizard(PhysicalConnectionWizard wizard) {
        this.wizard = wizard;
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
        propertyChangeListeners.remove(listener);
    }
    
    public void removeAllPropertyChangeListener() {
        if (propertyChangeListeners != null)
            propertyChangeListeners.clear();
    }
    
    public void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
        for (PropertyChangeListener listener : propertyChangeListeners)
            listener.propertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource().equals(source)) {
            if (points == null) {
                int coordinateIdx = getCoordinateIndex((LatLon) evt.getOldValue());
                LatLon coordinate = getCoordinates().get(coordinateIdx);
                coordinate.setLat(((LatLon) evt.getNewValue()).getLat());
                coordinate.setLon(((LatLon) evt.getNewValue()).getLon());
                
                firePropertyChangeEvent("coordinates", null, null);
            }
            else {
                points.get(0).
                        firePropertyChangeEvent("position", 
                                points.get(0).getPosition(), 
                                source.getPosition());
            }

        }
        if (evt.getSource().equals(target)) {
            if (points == null) {
                int coordinateIdx = getCoordinateIndex((LatLon) evt.getOldValue());
                LatLon coordinate = getCoordinates().get(coordinateIdx);
                coordinate.setLat(((LatLon) evt.getNewValue()).getLat());
                coordinate.setLon(((LatLon) evt.getNewValue()).getLon());
                
                firePropertyChangeEvent("coordinates", null, null);
            }
            else {
                points.get(points.size() - 1).
                        firePropertyChangeEvent("position", 
                                points.get(points.size() - 1).getPosition(), 
                                target.getPosition());
            }
        }
        if (evt.getSource() instanceof PointMarker) {
            PointMarker pointMarker = (PointMarker) evt.getSource();
            if (evt.getPropertyName().equals("position")) {
                int pointMarkerIdx = points.indexOf(pointMarker);
                
                if (pointMarker.isPoint()) {
                    int coordinateIdx = getCoordinateIndex((LatLon) evt.getOldValue());
                    
                    LatLon coordinate = getCoordinates().get(coordinateIdx);
                    coordinate.setLat(((LatLon) evt.getNewValue()).getLat());
                    coordinate.setLon(((LatLon) evt.getNewValue()).getLon());
                    
                    firePropertyChangeEvent("coordinates", null, null);
                    // position of current point
                    LatLon latLonPoint = points.get(pointMarkerIdx).getPosition();
                    if (pointMarkerIdx - 2 >= 0) { // Point is not the source point
                        // position of left point                                        
                        LatLon latLonLPoint = points.get(pointMarkerIdx - 2).getPosition();
                        // left midpoint
                        points.get(pointMarkerIdx - 1).setPosition(
                                ConnectionUtils.midPoint(latLonLPoint, latLonPoint));
                        
                        firePropertyChangeEvent("updateMarker", null, 
                                points.get(pointMarkerIdx - 1));
                    }
                    if (pointMarkerIdx + 2 < points.size()) { // Point is not the target point
                        // position of right point
                        LatLon latLonRPoint = points.get(pointMarkerIdx + 2).getPosition();
                        // right midpoint
                        points.get(pointMarkerIdx + 1).setPosition(
                                ConnectionUtils.midPoint(latLonPoint, latLonRPoint));

                        firePropertyChangeEvent("updateMarker", null, 
                                points.get(pointMarkerIdx + 1));
                    }
                }
                else {
                    LatLon latLonLPoint = points.get(pointMarkerIdx - 1).getPosition();
                    LatLon latLonRPoint = points.get(pointMarkerIdx + 1).getPosition();
                    
                    PointMarker leftMidpoint = new PointMarker(
                            ConnectionUtils.midPoint(latLonLPoint, 
                                    pointMarker.getPosition()),false);
                    leftMidpoint.addPropertyChangeListener(this);
                    
                    firePropertyChangeEvent("addMarker", null, leftMidpoint);
                    
                    PointMarker rightMidpoint = new PointMarker(
                            ConnectionUtils.midPoint(latLonRPoint, 
                                    pointMarker.getPosition()), false);
                    rightMidpoint.addPropertyChangeListener(this);
                    
                    firePropertyChangeEvent("addMarker", null, rightMidpoint);
                    
                    points.add(pointMarkerIdx + 1, rightMidpoint);
                    points.add(pointMarkerIdx, leftMidpoint);
                                        
                    int newPointIdx = getCoordinateIndex(latLonRPoint);
                        
                    getCoordinates().add(newPointIdx, pointMarker.getPosition());
                    
                    firePropertyChangeEvent("coordinates", null, null);
                    
                    pointMarker.setIsPoint(true);
                    firePropertyChangeEvent("updateMarker", null, pointMarker);
                }
            }
        }
    }
    
    private int getCoordinateIndex(LatLon latLon) {
        for (LatLon coordinate : getCoordinates())
            if (coordinate.equals(latLon))
                return getCoordinates().indexOf(coordinate);
        return -1;
    }
}
