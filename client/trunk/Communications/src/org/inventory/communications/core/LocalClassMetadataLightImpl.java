package org.inventory.communications.core;

import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.webservice.ClassInfoLight;

/**
 *
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

}
