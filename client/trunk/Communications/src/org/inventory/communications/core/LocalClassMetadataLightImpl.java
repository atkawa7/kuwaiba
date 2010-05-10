package org.inventory.communications.core;

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
    protected String className;
    protected String packageName;

    public LocalClassMetadataLightImpl(ClassInfoLight cil){
        this.id = cil.getId();
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
}