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
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@ServiceProvider(service=LocalObjectLight.class)
public class LocalObjectLightImpl implements LocalObjectLight{ //This class implements Transferable because of
                                                               //LocalObjectLight interface extends from it

    protected Long oid;
    protected String className;
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
    }

    public LocalObjectLightImpl(RemoteObjectLight rol){
        this.className = rol.getClassName();
        this.oid = rol.getOid();
        this.displayName = rol.getDisplayName();
        this.propertyChangeListeners = new ArrayList<PropertyChangeListener>();
        if (rol.getValidators() != null){
            validators = new HashMap();
            for (Validator validator : rol.getValidators())
                validators.put(validator.getLabel(), validator.isValue());
        }
    }

    public String getDisplayname(){
        return this.displayName;
    }

    public String getClassName() {
        return className;
    }

    public Long getOid() {
        return oid;
    }

    public void setDisplayName(String text){
        this.displayName = text;
        
    }

    public Boolean getValidator(String label){
        Boolean res = (Boolean)this.validators.get(label);
        if(res == null)
            return false;
        else
            return res;
    }

    public void addPropertyChangeListener(PropertyChangeListener newListener){
        if (propertyChangeListeners == null)
            propertyChangeListeners = new ArrayList<PropertyChangeListener>();
        if (propertyChangeListeners.contains(newListener))
            return;
        propertyChangeListeners.add(newListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener){
        if (propertyChangeListeners == null)
            return;
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
       if (!(obj instanceof LocalObjectLight))
           return false;
       if (this.getOid() == null || ((LocalObjectLightImpl)obj).getOid() == null)
           return false;
       return (this.getOid().longValue() == ((LocalObjectLightImpl)obj).getOid().longValue());
   }

   /**
    * Return the hashcode necessary for comparing objects. ATTENTION: In prior versions the attribute
    * <b>displayName</b> was used, but since it was no longer used by the subclass @LocalObjectImpl
    * (The attribute is private here and it's not inherited), the <code>equals</code> method failed, so
    * I changed it to <code>oid</code>
    * @return
    */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.oid != null ? this.oid.hashCode() : 0);
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

    @Override
    public String toString(){
        return getDisplayname() +" ["+getClassName()+"]"; //NOI18N
    }
}