package org.inventory.communications.core;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.webservice.ClassInfoLight;

/**
 * Implementation of the common interface to represent the classmetadata in a simple
 * way so it can be shown in trees and lists. This is done because to bring the whole
 * metadata is not necessary (ie. Container Hierarchy Manager)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalClassMetadataLightImpl implements LocalClassMetadataLight{

    protected Long id;
    protected Boolean isAbstract;
    protected String className;
    protected String packageName;


    public LocalClassMetadataLightImpl(ClassInfoLight cil){
        this.id = cil.getId();
        this.isAbstract = cil.isIsAbstract();
        this.className = cil.getClassName();
        this.packageName = cil.getPackage();
    }

    public LocalClassMetadataLightImpl(Long _id, String _className, String _packageName){
        this.id=_id;
        this.className = _className;
        this.packageName = _packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString(){
        return className;
    }

    public Boolean getIsAbstract() {
        return isAbstract;
    }



   /*
    * The equals method is overwritten in order to make the comparison based on the id, which is
    * the actual unique identifier (this is used when filtering the list of possible children in the Hierarchy Manager)
    */
   @Override
   public boolean equals(Object obj){
        if (obj.getClass().equals(this.getClass()))
            return this.getId().equals(((LocalClassMetadataLightImpl)obj).getId());
        else
            return false;
   }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 41 * hash + (this.className != null ? this.className.hashCode() : 0);
        return hash;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{LocalClassMetadataLight.DATA_FLAVOR};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(LocalClassMetadataLight.DATA_FLAVOR);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor))
            return this;
        else
            throw new UnsupportedFlavorException(flavor);
    }
}