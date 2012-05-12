package org.inventory.core.services.api;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeListener;


/**
 * This interface expose the business object shown in the explorer views (typically beanTreeView)
 * It only has basic information about the object (complete information is contained into a LocalObject instance)
 * 
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public interface LocalObjectLight extends Transferable {
    public static final DataFlavor DATA_FLAVOR =
            new DataFlavor(LocalObjectLight.class,"Object/LocalObjectLight");
    public String getClassName();
    public Long getOid();
    public String getName();
    public void setName(String text);
    public int getValidator(String label);
    public void firePropertyChangeEvent(String property, Object oldValue, Object newValue);
    public void addPropertyChangeListener(PropertyChangeListener listener);
    public void removePropertyChangeListener(PropertyChangeListener listener);
}
