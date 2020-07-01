/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
package org.inventory.communications.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.util.Constants;

/**
 * This class represent a Bookmark
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class LocalFavoritesFolder implements Comparable<LocalFavoritesFolder> {
    private long id;
    private String name;
    protected List<PropertyChangeListener> propertyChangeListeners;
    
    public LocalFavoritesFolder() {
        id = -1;
        this.propertyChangeListeners = new ArrayList<>();
    }
    
    public LocalFavoritesFolder(long id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        firePropertyChangeEvent(Constants.PROPERTY_NAME, oldName, name);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener newListener){
        if (propertyChangeListeners == null)
            propertyChangeListeners = new ArrayList<>();
        if (propertyChangeListeners.contains(newListener))
            return;
        propertyChangeListeners.add(newListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener){
        if (propertyChangeListeners == null)
            return;
        propertyChangeListeners.remove(listener);
    }

    public void firePropertyChangeEvent(String property, Object oldValue, Object newValue){
        for (PropertyChangeListener listener : propertyChangeListeners)
            listener.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
    }
    
    @Override
    public int compareTo(LocalFavoritesFolder lb) {
        return getName().compareTo(lb.getName());
    }
    
    @Override
    public String toString() {
        return getName() == null ? Constants.LABEL_NONAME : getName();
    }
        
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        
        if (!(obj instanceof LocalFavoritesFolder))
            return false;
        
        return this.getId() == ((LocalFavoritesFolder) obj).getId();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}
