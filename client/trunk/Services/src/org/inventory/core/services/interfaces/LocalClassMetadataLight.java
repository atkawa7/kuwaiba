package org.inventory.core.services.interfaces;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

/**
 * Represents the basic information related to a class useful to render nodes
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public interface LocalClassMetadataLight extends Transferable{
    public static final DataFlavor DATA_FLAVOR =
            new DataFlavor(LocalClassMetadataLight.class,"Object/LocalClassMetadataLight");
    public String getClassName();
    public String getPackageName();
    public Long getId();
}
