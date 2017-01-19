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

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Marker extends GoogleMapMarker implements Overlay {
    private List<PropertyChangeListener> propertyChangeListeners;
    private List<PropertyChangeListener> removedListeners;
    private boolean removed = false;
    
    public Marker() {
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
    public boolean getRemoved() {
        return removed;
    }
}
