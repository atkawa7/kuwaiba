/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import java.util.List;
import java.util.Objects;

/**
 * Implementation for the local representation of an application user with the most basic information
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
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
    
    protected List<VetoableChangeListener> vetoableChangeListeners;
    protected List<PropertyChangeListener> nonVetoableChangeListeners;

    public LocalUserObjectLight(long id, String userName, String firstName, 
            String lastName, boolean enabled, int type) {
        this.id = id;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
        this.type = type;
        this.vetoableChangeListeners = new ArrayList<>();
        this.nonVetoableChangeListeners = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        try {
            String oldName = this.userName;
            fireVetoablePropertyChange(PROPERTY_USER_NAME, oldName, userName);
            this.userName = userName;
            firePropertyChange(PROPERTY_USER_NAME, oldName, userName);
        } catch (PropertyVetoException ex) { }
        
    }
    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        try {
            String oldFirstName = this.firstName; 
            fireVetoablePropertyChange(PROPERTY_FIRST_NAME, oldFirstName, firstName);
            this.firstName = firstName;
            firePropertyChange(PROPERTY_FIRST_NAME, oldFirstName, firstName);
        } catch (PropertyVetoException ex) { }
        
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        try {
            String oldLastName = this.lastName;
            fireVetoablePropertyChange(PROPERTY_LAST_NAME, oldLastName, lastName);
            this.lastName = lastName;
            firePropertyChange(PROPERTY_LAST_NAME, oldLastName, lastName);
        } catch (PropertyVetoException ex) { }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        try {
            int oldType = this.type;
            fireVetoablePropertyChange(PROPERTY_TYPE, oldType, type);
            this.type = type;
            firePropertyChange(PROPERTY_TYPE, oldType, type);
        } catch (PropertyVetoException ex) { }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        try {
            boolean oldEnabled = this.enabled;
            fireVetoablePropertyChange(PROPERTY_ENABLED, oldEnabled, enabled);
            this.enabled = enabled;
            firePropertyChange(PROPERTY_ENABLED, oldEnabled, enabled);
        } catch (PropertyVetoException ex) { }
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
    
    public void removeAllChangeListeners() {
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
    
    public static class UserType {
        
        public static UserType[] DEFAULT_USER_TYPES = new LocalUserObjectLight.UserType[] { new LocalUserObjectLight.UserType("GUI User", 1), 
                new LocalUserObjectLight.UserType("Web Service Interface User", 2), 
                new LocalUserObjectLight.UserType("Southbound Interface User", 3) };
        
        private String label;
        private int type;

        public UserType(String label, int type) {
            this.label = label;
            this.type = type;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
        
        public static UserType getDefaultUserTypeForRawType(int rawType) {
            return DEFAULT_USER_TYPES[rawType - 1];
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
}
