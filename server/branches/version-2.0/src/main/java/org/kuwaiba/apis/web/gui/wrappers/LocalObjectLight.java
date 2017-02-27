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
package org.kuwaiba.apis.web.gui.wrappers;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * This class is a simple representation of a business object with a very basic information
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalObjectLight implements Transferable, Comparable<LocalObjectLight> { 
//This class does not implement Transferable because of
//LocalObjectLight interface extends from it
    
    public static DataFlavor DATA_FLAVOR =
            new DataFlavor(LocalObjectLight.class,"Object/LocalObjectLight");
    
    protected long oid;
    protected String name;
    protected String className;
        

    /**
     * This constructor is called to create dummy objects where the id is not important
     */
    public LocalObjectLight(){
        this.oid = -1;
    }

    public LocalObjectLight(long oid, String name, String className) {
        this();
        this.oid = oid;
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long id){
        this.oid = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        //firePropertyChangeEvent(Constants.PROPERTY_NAME, oldName, name);
    }


//    public void removePropertyChangeListener(PropertyChangeListener listener){
//        if (propertyChangeListeners == null)
//            return;
//        propertyChangeListeners.remove(listener);
//    }
//
//    public void firePropertyChangeEvent(String property, Object oldValue, Object newValue){
//        for (PropertyChangeListener listener : propertyChangeListeners)
//            listener.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
//    }

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
        return (getName() == null ? Constants.LABEL_NONAME : getName()) + " [" + getClassName() + "]"; //NOI18N
    }

    @Override
    public int compareTo(LocalObjectLight o) {
        return getName().compareTo(o.getName());
    }
}