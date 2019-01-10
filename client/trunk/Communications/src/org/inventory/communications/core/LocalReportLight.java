/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.util.Constants;

/**
 * Represents a report descriptor, that is, a local representation of a report with its basic information
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LocalReportLight {
    /**
     * The output is a CSV text file
     */
    public static int TYPE_CSV = 1;
    /**
     * The output is an HTML text file
     */
    public static int TYPE_HTML = 2;
    /**
     * The output is a PDF file
     */
    public static int TYPE_PDF = 3;
    /**
     * The output is a XLSX spreadsheet
     */
    public static int TYPE_XLSX = 4;
    /**
     * Report id
     */
    private long id;
    /**
     * Report name
     */
    private String name;
    /**
     * Report description
     */
    private String description;
    /**
     * Is the report enabled?
     */
    private Boolean enabled;
    /**
     * Report type
     */
    private Integer type;
    /**
     * List of listeners
     */
    private List<PropertyChangeListener> propertyChangeListeners;
    
    public LocalReportLight(long id, String name, String description, Boolean enabled, Integer type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.type = type;
        this.propertyChangeListeners = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        firePropertyChangeListener(this, Constants.PROPERTY_NAME, oldName, name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        String oldDescription = this.description;
        this.description = description;
        firePropertyChangeListener(this, Constants.PROPERTY_DESCRIPTION, oldDescription, description);
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        boolean oldEnabled = this.enabled;
        this.enabled = enabled;
        firePropertyChangeListener(this, Constants.PROPERTY_ENABLED, oldEnabled, enabled);
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        int oldType = this.type;
        this.type = type;
        firePropertyChangeListener(this, Constants.PROPERTY_TYPE, oldType, type);
    }
    
    @Override
    public String toString() {
        return name == null || name.isEmpty() ? Constants.LABEL_NONAME : name;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener aListener) {
        propertyChangeListeners.add(aListener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener aListener) {
        propertyChangeListeners.remove(aListener);
    }
    
    public void removreAllListeners() {
        propertyChangeListeners.clear();
    }
    
    public void firePropertyChangeListener(Object source, String propertyName, Object oldValue, Object newValue) {
        for (PropertyChangeListener aListener : propertyChangeListeners)
            aListener.propertyChange(new PropertyChangeEvent(source, propertyName, oldValue, newValue));
    }
}
