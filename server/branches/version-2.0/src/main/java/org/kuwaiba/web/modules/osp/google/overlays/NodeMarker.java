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

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * Custom GoogleMapMarker for Kuwaiba
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class NodeMarker extends GoogleMapMarker {
    private List<PropertyChangeListener> propertyChangeListeners;
    
    private RemoteObjectLight remoteObjectLight;
    
    public NodeMarker(RemoteObjectLight remoteObjectLight) {
        this.remoteObjectLight = remoteObjectLight;
        setDraggable(true);
    }
    
    public RemoteObjectLight getRemoteObjectLight() {
        return remoteObjectLight;
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
}
