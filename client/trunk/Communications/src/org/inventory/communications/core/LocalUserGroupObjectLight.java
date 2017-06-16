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

package org.inventory.communications.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.inventory.communications.wsclient.GroupInfoLight;

/**
 * Implementation for the local representation of the very basic information about an application users group
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalUserGroupObjectLight implements Comparable<LocalUserGroupObjectLight> {
    
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_CREATION_DATE = "creationDate";
    
    protected long id;
    protected String name;
    protected String description;
    protected Date creationDate;
    
    protected List<VetoableChangeListener> vetoableChangeListeners;
    protected List<PropertyChangeListener> nonVetoableChangeListeners;

    public LocalUserGroupObjectLight(GroupInfoLight group){
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.creationDate = new Date(group.getCreationDate());
        this.vetoableChangeListeners = new ArrayList<>();
        this.nonVetoableChangeListeners = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String newName) {
        try {
            String oldName = this.name;
            fireVetoablePropertyChange(PROPERTY_NAME, oldName, newName);
            this.name = newName;
            firePropertyChange(PROPERTY_NAME, oldName, newName);
        } catch (PropertyVetoException e) { }
    }
    
    public Date getCreationDate() {
        return creationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String newDescription) {
        try {
            String oldDescription = this.description;
            fireVetoablePropertyChange(PROPERTY_DESCRIPTION, oldDescription, newDescription);
            this.description = newDescription;
            firePropertyChange(PROPERTY_DESCRIPTION, oldDescription, newDescription);
        } catch (PropertyVetoException e) {}
    }
    
    public void addVetoablePropertyChangeListener(VetoableChangeListener listener) {
        vetoableChangeListeners.add(listener);
    }
    
    public void removeVetoablePropertyChangeListener(VetoableChangeListener listener) {
        vetoableChangeListeners.remove(listener);
    }
    
    public void addNonVetoablePropertyChangeListener(PropertyChangeListener listener) {
        nonVetoableChangeListeners.add(listener);
    }
    
    public void removeNonVetoablePropertyChangeListener(PropertyChangeListener listener) {
        nonVetoableChangeListeners.remove(listener);
    }
    
    public void removeAllPropertyChangeListeners() {
        vetoableChangeListeners.clear();
        nonVetoableChangeListeners.clear();
    }
    
    public void fireVetoablePropertyChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException {
        for (VetoableChangeListener listener : vetoableChangeListeners)
            listener.vetoableChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
    }
    
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException {
        for (PropertyChangeListener listener : nonVetoableChangeListeners)
            listener.propertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
    }
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(LocalUserGroupObjectLight o) {
        return getName().compareTo(o.getName());
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocalUserGroupObjectLight)
            return ((LocalUserGroupObjectLight)obj).getId() == id;
        else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 41 * hash + Objects.hashCode(this.name);
        return hash;
    }
}
