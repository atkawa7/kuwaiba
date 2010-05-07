package org.inventory.core.services.interfaces;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public interface LocalAttributeMetadata {
    public String getDescription();

    public void setDescription(String description);

    public String getDisplayName();

    public void setDisplayName(String displayName);

    public Boolean getIsAdministrative();

    public void setIsAdministrative(Boolean isAdministrative);

    public Boolean getIsVisible();

    public void setIsVisible(Boolean isVisible);

    public String getName();

    public void setName(String name);

    public String getType();

    public void setType(String type);
}
