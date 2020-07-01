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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.util.Constants;
import org.inventory.communications.wsclient.RemoteValidator;

/**
 * This class is a simple representation of a business object with a very basic information
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LocalObjectLight implements Transferable, Comparable<LocalObjectLight> { //This class does not implement Transferable because of
                                                               //LocalObjectLight interface extends from it
    public static DataFlavor DATA_FLAVOR =
            new DataFlavor(LocalObjectLight.class,"Object/LocalObjectLight");
    
    protected String id;
    protected String name;
    protected String className;
    /**
     * The list of property change listeners
     */
    protected List<PropertyChangeListener> propertyChangeListeners;
    /**
     * Collection of flags
     */
    protected List<LocalValidator> validators;

    /**
     * This constructor is called to create dummy objects where the id is not important
     */
    public LocalObjectLight() {
        this.id = "-1";
        this.propertyChangeListeners = new ArrayList<>();
        this.validators = new ArrayList<>();
        this.name = Constants.LABEL_NONAME;
    }

    public LocalObjectLight(String oid, String name, String className) {
        this.id = oid;
        this.name = name;
        this.className = className;
        this.validators = new ArrayList<>();
        this.propertyChangeListeners = new ArrayList<>();
    }

    public LocalObjectLight(String className, String name, String id, List<RemoteValidator> validators){
        this.id = id;
        this.name = name;
        this.className = className;
        this.validators = new ArrayList<>();
        this.propertyChangeListeners = new ArrayList<>();
        if (validators != null) {
            validators.forEach((remoteValidator) -> {
                this.validators.add(new LocalValidator(remoteValidator.getName(), remoteValidator.getProperties()));
            });
        }
    }

    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }

    public String getId() {
        return id;
    }

    public void setOid(String id) {
        this.id = id;
    }

    /**
     * Returns a validator given its name
     * @param name Returns a validator with the given name
     * @return The validator instance or null of the validator was not found
     */
    public LocalValidator getValidator(String name) {
        if (this.validators == null)
            return null;
        
        for (LocalValidator validator : this.validators) {
            if (validator.getName().equals(name))
                return validator;
        }
        
        return null;
    }
    
    /**
     * Gets the validators that matched the class of the object
     * @return The list of validators
     */
    public List<LocalValidator> getValidators() {
        return validators;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        firePropertyChangeEvent(Constants.PROPERTY_NAME, oldName, name);
    }

    public void addPropertyChangeListener(PropertyChangeListener newListener) {
        if (propertyChangeListeners == null)
            propertyChangeListeners = new ArrayList<>();
        if (propertyChangeListeners.contains(newListener))
            return;
        propertyChangeListeners.add(newListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeListeners == null)
            return;
        propertyChangeListeners.remove(listener);
    }

    public void firePropertyChangeEvent(String property, Object oldValue, Object newValue) {
        synchronized(propertyChangeListeners) {
            Iterator<PropertyChangeListener> listenersIterator = propertyChangeListeners.iterator();
            while (listenersIterator.hasNext())
                listenersIterator.next().propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof LocalObjectLight))
            return false;
        
        return className.equals(((LocalObjectLight)obj).getClassName()) &&
                id.equals(((LocalObjectLight)obj).getId());
    }
        
    //Transferable methods
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DATA_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == DATA_FLAVOR;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if(flavor == DATA_FLAVOR)
            return this;
        else
            throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public String toString() { //An object has a low probability to have validators, so perhaps it's not worth caching this operation
        LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(className, false); //This info is usually cached already
        String prefix = "", suffix = "";
        for (LocalValidator aValidator : validators) {
            String thisValidatorPrefix = aValidator.getProperties().getProperty("prefix");
            if (thisValidatorPrefix != null)
                prefix += thisValidatorPrefix + " ";
            String thisValidatorSuffix = aValidator.getProperties().getProperty("suffix");
            if (thisValidatorSuffix != null)
                prefix += " " + thisValidatorPrefix;
        }
        
        return prefix + getName() + suffix + " [" + classMetadata + "]";
    }

    @Override
    public int compareTo(LocalObjectLight o) {
        return getName().compareTo(o.getName());
    }
}