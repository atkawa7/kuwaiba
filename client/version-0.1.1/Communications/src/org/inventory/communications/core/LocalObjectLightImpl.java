package org.inventory.communications.core;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.webservice.RemoteObjectLight;

/**
 * Este clase representa los elementos que aparecen en los árboles de navegación
 * es sólo información de despliegue (sin detalle)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalObjectLightImpl implements LocalObjectLight{ //This class implements Transferable because of
                                                               //LocalObjectLight interface extends from it

    private Long oid;
    protected String className;
    protected String packageName;
    protected Boolean hasChildren;
    private String displayName;

    public LocalObjectLightImpl(){}

    public LocalObjectLightImpl(RemoteObjectLight rol){
        this.className = rol.getClassName();
        this.packageName = rol.getPackageName();
        this.oid = rol.getOid();
        this.hasChildren = rol.isHasChildren();
        this.displayName = rol.getDisplayName();
    }

    public final String getDisplayname(){
        return this.displayName;
    }

    public Boolean getHasChildren() {
        return hasChildren;
    }

    public String getClassName() {
        return className;
    }

    public Boolean hasChildren() {
        return hasChildren;
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
    }

   @Override
   public boolean equals(Object obj){
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