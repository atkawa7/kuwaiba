/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.communications.core;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.utils.Utils;
import org.inventory.webservice.ClassInfo;
import org.inventory.webservice.ClassInfoLight;

/**
 * Implementation of the common interface to represent the classmetadata in a simple
 * way so it can be shown in trees and lists. This is done because to bring the whole
 * metadata is not necessary (ie. Container Hierarchy Manager)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalClassMetadataLightImpl
        implements LocalClassMetadataLight,Transferable{

    protected Long id;
    protected Boolean isAbstract;
    protected String className;
    protected String displayName;
    protected String description;
    protected String packageName;
    protected Image smallIcon;

    public LocalClassMetadataLightImpl(ClassInfo ci){
        this (ci.getId(),ci.getClassName(),ci.getPackage(),ci.getDisplayName(),
                ci.getDescription(),ci.getSmallIcon());
    }

    public LocalClassMetadataLightImpl(ClassInfoLight cil){
        this.id = cil.getId();
        this.isAbstract = cil.isIsAbstract();
        this.className = cil.getClassName();
        this.packageName = cil.getPackage();
        this.displayName = cil.getDisplayName();
        this.description = cil.getDescription();
        this.smallIcon = cil.getSmallIcon()==null?null:Utils.getImageFromByteArray(cil.getSmallIcon());
    }

    public LocalClassMetadataLightImpl(Long _id, String _className, String _packageName,
            String _displayName, String _description, byte[] _smallIcon){
        this.id=_id;
        this.className = _className;
        this.packageName = _packageName;
        this.displayName = _displayName;
        this.description = _description;
        this.smallIcon = _smallIcon==null?null:Utils.getImageFromByteArray(_smallIcon);
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
        if (obj == null)
           return false;
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
    
    public String getDisplayName(){
        return displayName;
    }
    
    public String getDescription(){
        return description;
    }
    
    public Image getSmallIcon() {
        return smallIcon;
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