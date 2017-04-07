/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementation for the local representation of an application user with the most basic information
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalUserObjectLight implements Comparable<LocalUserObjectLight> {

    public static final String PROPERTY_USER_NAME = "username";
    public static final String PROPERTY_PASSWORD = "password";
    public static final String PROPERTY_FIRST_NAME = "firstName";
    public static final String PROPERTY_LAST_NAME = "lastName";
    public static final String PROPERTY_ENABLED = "enabled";
    public static final String PROPERTY_TYPE = "type";
    
    private long id;
    private String userName;
    private String firstName;
    private String lastName;
    private int type;
    private boolean enabled;
    
    protected List<VetoableChangeListener> changeListeners;

    public LocalUserObjectLight(long id, String userName, String firstName, 
            String lastName, boolean enabled, int type) {
        this.id = id;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
        this.type = type;
        this.changeListeners = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        try {
            firePropertyChange(PROPERTY_USER_NAME, this.userName, userName);
            this.userName = userName;
        } catch (PropertyVetoException ex) { }
        
    }
    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        try {
            firePropertyChange(PROPERTY_FIRST_NAME, this.firstName, firstName);
            this.firstName = firstName;
        } catch (PropertyVetoException ex) { }
        
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        try {
            firePropertyChange(PROPERTY_LAST_NAME, this.lastName, lastName);
            this.lastName = lastName;
        } catch (PropertyVetoException ex) { }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        try {
            firePropertyChange(PROPERTY_TYPE, this.type, type);
            this.type = type;
        } catch (PropertyVetoException ex) { }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        try {
            firePropertyChange(PROPERTY_ENABLED, this.enabled, enabled);
            this.enabled = enabled;
        } catch (PropertyVetoException ex) { }
    }
    
    public void addPropertyChangeListener(VetoableChangeListener listener) {
        changeListeners.add(listener);
    }
    
    public void removePropertyChangeListener(VetoableChangeListener listener) {
        changeListeners.remove(listener);
    }
    
    public void removeAllPropertyChangeListeners() {
        changeListeners.clear();
    }
    
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException {
        for (VetoableChangeListener listener : changeListeners)
            listener.vetoableChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
    }
    
    @Override
    public String toString() {
        return userName;
    }

    @Override
    public int compareTo(LocalUserObjectLight o) {
        return toString().compareTo(o.toString());
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
        hash = 41 * hash + Objects.hashCode(this.userName);
        return hash;
    }
}
