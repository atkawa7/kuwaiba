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
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.modules.osp.google.CustomGoogleMap;

/**
 * Polyline that represent a physical connection
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ConnectionPolyline extends Polyline {
    /**
     * Saved. is used to know if the connection was stored in the view 
     * of the data base
     */
    private boolean saved = false;
    private RemoteObjectLight connectionInfo;
    
    private final MarkerNode source;
    private MarkerNode target;
            
    public ConnectionPolyline(MarkerNode source) {
        this.source = source;
        getCoordinates().add(source.getPosition());
        target = null;
    }
    
    public ConnectionPolyline(MarkerNode source, MarkerNode target) {
        this.source = source;
        this.target = target;
        
        this.source.addPropertyChangeListener(this);
        this.target.addPropertyChangeListener(this);        
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    public void setSaved(boolean saved) {
        this.saved = saved;
    }
    
    public RemoteObjectLight getConnectionInfo() {
        return connectionInfo;
    }
    
    public void setConnectionInfo(RemoteObjectLight connectionInfo) {
        this.connectionInfo = connectionInfo;
    }
    
    public MarkerNode getSource() {
        return source;
    }

    public MarkerNode getTarget() {
        return target;        
    }

    public void setTarget(MarkerNode target) {
        this.target = target;
        getCoordinates().add(target.getPosition());
        
        this.source.addPropertyChangeListener(this);
        this.target.addPropertyChangeListener(this);
    }
        
    @Override
    void enableEdition() {
        List<MarkerPoint> viewablePoints = new ArrayList();
        
        for (int i = 1; i < points.size() - 1; i += 1)
                viewablePoints.add(points.get(i));
        
        if (isEditable())
            firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_SHOW_MARKERS, null, viewablePoints);
        else
            firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_HIDE_MARKERS, null, viewablePoints);
    }
    
    @Override
    void setSpecialPoints() {
        // point is special because represent the source node
        points.get(0).setIsSpecial(true);
        points.get(0).setVisible(false);
        firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_UPDATE_MARKER, null, points.get(0));
        // point is special because represent the target node
        points.get(points.size() - 1).setIsSpecial(true);
        points.get(points.size() - 1).setVisible(false);
        firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_UPDATE_MARKER, null, 
                points.get(points.size() - 1));
        
    }
    
    @Override
    void updateSpecialPoints(PropertyChangeEvent evt) {
        if (evt == null || evt.getPropertyName() == null)
            return;
        
        if (evt.getPropertyName().equals(CustomGoogleMap.GM_EVENT_NAME_UPDATE_MARKER)) {
            // update polyline coordinates when the source node change the position
            if (evt.getSource().equals(source)) {
                if (points.isEmpty()) {
                    LatLon coordinate = getCoordinates().get(0);
                    coordinate.setLat(((LatLon) evt.getNewValue()).getLat());
                    coordinate.setLon(((LatLon) evt.getNewValue()).getLon());

                    firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_UPDATE_POLYLINE, null, this);
                }
                else {
                    // disable before update
                    points.get(0).setIsSpecial(false); 
                    
                    points.get(0).
                            firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_UPDATE_MARKER, 
                                    points.get(0).getPosition(), 
                                    source.getPosition());
                    // enable after update
                    points.get(0).setIsSpecial(true);
                }
                return;
            }
            // update polyline coordinates when the target node change the position
            if (evt.getSource().equals(target)) {
                if (points.isEmpty()) {
                    LatLon coordinate = getCoordinates()
                            .get(getCoordinates().size() - 1);
                    coordinate.setLat(((LatLon) evt.getNewValue()).getLat());
                    coordinate.setLon(((LatLon) evt.getNewValue()).getLon());

                    firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_UPDATE_POLYLINE, null, this);
                }
                else {
                    // disable before update
                    points.get(points.size() - 1).setIsSpecial(false);

                    points.get(points.size() - 1).
                            firePropertyChangeEvent(CustomGoogleMap.GM_EVENT_NAME_UPDATE_MARKER, 
                                    points.get(points.size() - 1).getPosition(), 
                                    target.getPosition());
                    // enable after update
                    points.get(points.size() - 1).setIsSpecial(true);
                }
                return;
            }
        }
    }
    
    @Override
    public String toString() {
        return connectionInfo.toString();
    }
}
