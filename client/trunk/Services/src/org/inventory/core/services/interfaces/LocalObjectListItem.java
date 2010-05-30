package org.inventory.core.services.interfaces;

/**
 * Provides an interface for those attribute values related tos lists of objects
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public interface LocalObjectListItem {
    //Represents the null ID
    public static final Long NULL_ID = new Long(0);
    
    public LocalObjectListItem getNull();
    public String getDisplayName();
    public Long getId();
    public String getName();
    public String getClassName();
}
