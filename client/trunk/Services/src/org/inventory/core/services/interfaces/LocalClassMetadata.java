package org.inventory.core.services.interfaces;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public interface LocalClassMetadata extends LocalClassMetadataLight{
    public String getDisplayNameForAttribute(String att);
    public LocalAttributeMetadata[] getAttributes();
    public String getDescriptionForAttribute(String att);
    public Boolean isVisible(String att);
    public Boolean isAdministrative(String att);
    public Boolean isMultiple(String att);
    public String getTypeForAttribute(String att);
}
