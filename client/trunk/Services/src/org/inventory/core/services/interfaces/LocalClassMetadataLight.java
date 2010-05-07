package org.inventory.core.services.interfaces;

/**
 * Represents the basic information related to a class useful to render nodes
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public interface LocalClassMetadataLight {
    public String getClassName();
    public String getPackageName();
    public Long getId();
}
