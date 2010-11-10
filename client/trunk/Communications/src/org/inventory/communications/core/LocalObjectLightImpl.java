package org.inventory.communications.core;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.webservice.RemoteObjectLight;
import org.inventory.webservice.Validator;

/**
 * This class is a simple representation of a business object with a very basic information
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalObjectLightImpl implements LocalObjectLight{ //This class implements Transferable because of
                                                               //LocalObjectLight interface extends from it

    protected Long oid;
    protected String className;
    protected String packageName;
    protected String displayName;
    /**
     * The list of property change listeners
     */
    protected List<PropertyChangeListener> propertyChangeListeners;
    /**
     * Collection of flags
     */
    protected HashMap validators;
    /**
     * Properties
     */
    public static String PROP_DISPLAYNAME="displayname";

    public LocalObjectLightImpl(){
        this.propertyChangeListeners = new ArrayList<PropertyChangeListener>();
    }

    public LocalObjectLightImpl(RemoteObjectLight rol){
        this.className = rol.getClassName();
        this.packageName = rol.getPackageName();
        this.oid = rol.getOid();
        this.displayName = rol.getDisplayName();
        this.propertyChangeListeners = new ArrayList<PropertyChangeListener>();
        if (rol.getValidators() != null){
            validators = new HashMap();
            for (Validator validator : rol.getValidators())
                validators.put(validator.getLabel(), validator.isValue());
        }
    }

    public final String getDisplayname(){
        return this.displayName;
    }

    public String getClassName() {
        return className;
    }

    public Long getOid() {
        return oid;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setDisplayName(String text){
        this.displayName = text;
        firePropertyChangeEvent(PROP_DISPLAYNAME, oid, text);
    }

    public Boolean getValidator(String label){
        Boolean res = (Boolean)this.validators.get(label);
        if(res == null)
            return false;
        else
            return res;
    }

    public void addPropertyChangeListener(PropertyChangeListener newListener){
        if (propertyChangeListeners.contains(newListener))
            return;
        propertyChangeListeners.add(newListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener){
        propertyChangeListeners.remove(listener);
    }

    public void firePropertyChangeEvent(String property, Object oldValue, Object newValue){
        for (PropertyChangeListener listener : propertyChangeListeners)
            listener.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
    }

   @Override
   public boolean equals(Object obj){
       if(obj == null)
           return false;
        if (obj.getClass().equals(this.getClass()))
            return (this.getOid() == ((LocalObjectLightImpl)obj).getOid());
        else
            return false;
   }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.displayName != null ? this.displayName.hashCode() : 0);
        return hash;
    }

    //Transferable methods
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DATA_FLAVOR};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == DATA_FLAVOR;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if(flavor == DATA_FLAVOR)
            return this;
        else
            throw new UnsupportedFlavorException(flavor);
    }
}