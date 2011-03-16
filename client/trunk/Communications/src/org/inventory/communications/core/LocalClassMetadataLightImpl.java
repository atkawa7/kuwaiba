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
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.utils.Utils;
import org.kuwaiba.wsclient.ClassInfo;
import org.kuwaiba.wsclient.ClassInfoLight;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the common interface to represent the classmetadata in a simple
 * way so it can be shown in trees and lists. This is done because to bring the whole
 * metadata is not necessary (ie. Container Hierarchy Manager)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@ServiceProvider(service=LocalClassMetadataLight.class)
public class LocalClassMetadataLightImpl
        implements LocalClassMetadataLight,Transferable{

    protected Long id;
    protected Boolean isAbstract;
    protected Boolean isPhysicalNode;
    protected Boolean isPhysicalEndpoint;
    protected Boolean isViewable;
    protected String className;
    protected String displayName;
    protected Image smallIcon;

    public LocalClassMetadataLightImpl() {    }

    public LocalClassMetadataLightImpl(ClassInfo ci){
        this (ci.getId(),ci.getClassName(),ci.getDisplayName(),ci.getSmallIcon(),
                ci.isPhysicalNode(),ci.isPhysicalEndpoint(), ci.isAbstractClass(), ci.isViewable());
    }

    public LocalClassMetadataLightImpl(String className, Long oid){
        this.id = oid;
        this.className = className;
    }

    public LocalClassMetadataLightImpl(ClassInfoLight cil){
        this.id = cil.getId();
        this.isPhysicalNode = cil.isPhysicalNode();
        this.isPhysicalEndpoint = cil.isPhysicalEndpoint();
        this.isAbstract = cil.isAbstractClass();
        this.isViewable = cil.isViewable();
        this.className = cil.getClassName();
        this.displayName = cil.getDisplayName();
        this.smallIcon = cil.getSmallIcon()==null ? null : Utils.getImageFromByteArray(cil.getSmallIcon());
    }

    public LocalClassMetadataLightImpl(Long _id, String _className, String _displayName,
            byte[] _smallIcon,Boolean _isPhysicalNode, Boolean _isPhysicalEndpoint, Boolean _isAbstract, Boolean _isViewable){
        this.id = _id;
        this.isPhysicalNode = _isPhysicalNode;
        this.isPhysicalEndpoint = _isPhysicalEndpoint;
        this.isAbstract = _isAbstract;
        this.isViewable = _isViewable;
        this.className = _className;
        this.displayName = _displayName;
        this.smallIcon = _smallIcon==null ? null : Utils.getImageFromByteArray(_smallIcon);
    }

    public String getClassName() {
        return className;
    }

    public Long getOid() {
        return id;
    }

    @Override
    public String toString(){
        return className;
    }

    public Boolean isAbstract() {
        return isAbstract;
    }

    public Boolean isPhysicalNode() {
        return isPhysicalNode;
    }

    public Boolean isPhysicalEndpoint() {
        return isPhysicalEndpoint;
    }

    public Boolean isViewable(){
        return this.isViewable;
    }

   /**
    * The equals method is overwritten in order to make the comparison based on the id, which is
    * the actual unique identifier (this is used when filtering the list of possible children in the Hierarchy Manager)
    */
   @Override
   public boolean equals(Object obj){
       if(obj == null)
           return false;
       if (!(obj instanceof LocalClassMetadataLight))
           return false;
       if (this.getOid() == null || ((LocalClassMetadataLight)obj).getOid() == null)
           return false;
       return (this.getOid().longValue() == ((LocalClassMetadataLight)obj).getOid().longValue());
   }

    @Override
    public int hashCode() {
        int hash = 9;
        hash = 81 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
    public String getDisplayName(){
        return displayName;
    }
    
    public Image getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(Image newIcon){
        this.smallIcon = newIcon;
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