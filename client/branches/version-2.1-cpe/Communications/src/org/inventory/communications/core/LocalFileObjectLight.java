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
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.util.Constants;

/**
 * Local representation of a file object. A file object represents a file attached to an inventory object
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LocalFileObjectLight implements Comparable<LocalFileObjectLight>{
    /**
     * File object id
     */
    protected long fileOjectId;
    /**
    * The name of the file
    */
    protected String name;
    /**
    * Tags associated to the binary file that can be used to index it or find it in searches
    */
    protected String tags;
    /**
     * Creation date
     */
    protected long creationDate;
    
    private List<VetoableChangeListener> listeners;

    public LocalFileObjectLight(long fileOjectId, String name, long creationDate, String tags) {
        this.fileOjectId = fileOjectId;
        this.name = name;
        this.tags = tags;
        this.creationDate = creationDate;
        this.listeners = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws PropertyVetoException {
        firePropertyChangeEvent(Constants.PROPERTY_NAME, this.name, name);
        this.name = name;
    }

    public long getFileOjectId() {
        return fileOjectId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) throws PropertyVetoException {
        firePropertyChangeEvent(Constants.PROPERTY_TAGS, this.tags, tags);
        this.tags = tags;
    }

    public long getCreationDate() {
        return creationDate;
    }
    
    public void addActionListener(VetoableChangeListener listener) {
        listeners.add(listener);
    }
    
    public void removeActionListener(VetoableChangeListener listener) {
        listeners.remove(listener);
    }
    
    public void RemoveAllListeners() {
        listeners.clear();
    }
    
    public void firePropertyChangeEvent(String property, String oldValue, String newValue) throws PropertyVetoException {
        for (VetoableChangeListener listener : listeners)
            listener.vetoableChange(new PropertyChangeEvent(this, property, oldValue, newValue));
    }
    
    @Override
    public String toString() {
        return String.format("%s [%s]", name, tags == null || tags.trim().isEmpty() ? "No tags defined" : tags);
    }

    @Override
    public int compareTo(LocalFileObjectLight o) {
        return Long.compare(creationDate, o.getCreationDate());
    }
}
