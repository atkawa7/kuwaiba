/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.inventory.core.services.api.LocalObjectLight;
import org.kuwaiba.wsclient.RemoteObjectLight;
import org.kuwaiba.wsclient.Validator;
import org.openide.util.lookup.ServiceProvider;

/**
 * This class is a simple representation of a business object with a very basic information
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=LocalObjectLight.class)
public class LocalObjectLightImpl implements LocalObjectLight{ //This class does not implement Transferable because of
                                                               //LocalObjectLight interface extends from it

    protected long oid;
    protected String name;
    protected String className;
    /**
     * The list of property change listeners
     */
    protected List<PropertyChangeListener> propertyChangeListeners;
    /**
     * Collection of flags
     */
    protected HashMap<String, Integer> validators;

    /**
     * This constructor is called to create dummy objects where the id is not important
     */
    public LocalObjectLightImpl(){
        this.oid = -1;
    }

    public LocalObjectLightImpl(long oid, String name, String className) {
        this.oid = oid;
        this.name = name;
        this.className = className;
        this.validators = new HashMap<String, Integer>();
    }

    public LocalObjectLightImpl(RemoteObjectLight rol){
        this.className = rol.getClassName();
        this.name = rol.getName();
        this.oid = rol.getOid();
        this.propertyChangeListeners = new ArrayList<PropertyChangeListener>();

        if (rol.getValidators() != null){
            this.validators = new HashMap<String, Integer>();
            for (Validator validator : rol.getValidators())
                validators.put(validator.getLabel(), validator.getValue());
        }
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public long getOid() {
        return oid;
    }

    @Override
    public void setOid(long id){
        this.oid = id;
    }

    @Override
    public int getValidator(String label){
        if (this.validators == null)
            return 0;
        Integer res = this.validators.get(label);
        if(res == null)
            return 0;
        else
            return res;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }


    @Override
    public void addPropertyChangeListener(PropertyChangeListener newListener){
        if (propertyChangeListeners == null)
            propertyChangeListeners = new ArrayList<PropertyChangeListener>();
        if (propertyChangeListeners.contains(newListener))
            return;
        propertyChangeListeners.add(newListener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener){
        if (propertyChangeListeners == null)
            return;
        propertyChangeListeners.remove(listener);
    }

    @Override
    public void firePropertyChangeEvent(String property, Object oldValue, Object newValue){
        for (PropertyChangeListener listener : propertyChangeListeners)
            listener.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
    }

   @Override
   public boolean equals(Object obj){
       if(obj == null)
           return false;
       if (!(obj instanceof LocalObjectLight))
           return false;
       return (this.getOid() == ((LocalObjectLight)obj).getOid());
   }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (int) (this.oid ^ (this.oid >>> 32));
        //hash = 47 * hash + (this.className != null ? this.className.hashCode() : 0);
        return hash;
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
    public String toString(){
        return getName() +" ["+getClassName()+"]"; //NOI18N
    }
}