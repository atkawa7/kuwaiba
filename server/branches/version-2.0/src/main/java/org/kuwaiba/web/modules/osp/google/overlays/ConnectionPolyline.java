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

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * Polyline that represent a physical connection
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ConnectionPolyline extends GoogleMapPolyline {
//public class ConnectionPolyline extends Polyline {
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
    }
    
    @Override
    public String toString() {
        return connectionInfo.toString();
    }
}