/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>
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

import org.kuwaiba.wsclient.ApplicationLogEntry;

/**
 * Represents a single activity record
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalApplicationLogEntry {
    /**
     * Entry id
     */
    private long id;
    /**
     * Entry type (see possible values below)
     */
    private int type;
    /**
     * Who performed the action
     */
    private String userName;
    /**
     * Entry timestamp
     */
    private long timestamp;
    /**
     * Property/field that was modified, if any
     */
    private String affectedProperty;
    /**
     * Old value of the modified property, if any
     */
    private String oldValue;
    /**
     * New value of the modified property, if any
     */
    private String newValue;
    /**
     * Additional notes to this action, if any
     */
    private String notes;
    
    public static String[] types = {"Create Application Object", "Delete Application Object", "Update Application Object", 
                           "Create Inventory Object", "Delete Inventory Object", "Update Inventory Object", 
                           "Create Metadata Object", "Delete Metadata Object", "Update Metadata Object", "Move object"};
    
    
    public LocalApplicationLogEntry(ApplicationLogEntry ale) {
        this.id = ale.getId();
        this.type = ale.getType();
        this.userName = ale.getUserName();
        this.timestamp = ale.getTimestamp();
        this.affectedProperty = ale.getAffectedProperty();
        this.oldValue = ale.getOldValue();
        this.newValue = ale.getNewValue();
    }

    public long getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getUserName() {
        return userName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getAffectedProperty() {
        return affectedProperty;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getNotes() {
        return notes;
    }
}
