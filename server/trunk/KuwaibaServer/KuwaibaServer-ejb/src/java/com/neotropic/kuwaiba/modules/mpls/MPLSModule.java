/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.modules.mpls;

import com.neotropic.kuwaiba.modules.GenericCommercialModule;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;

/**
 * This class implements the functionality corresponding to the MPLS module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class MPLSModule implements GenericCommercialModule {

    /**
     * The MetadataEntityManager instance
     */
    private MetadataEntityManager mem;
    /**
     * The BusinessEntityManager instance
     */
    private BusinessEntityManager bem;
    
    //Constants
    
    @Override
    public String getName() {
        return "MPLS Networks Dummy Module"; //NOI18N
    }

    @Override
    public String getDescription() {
        return "MPLS Dummy Module. Not yet implemented";
    }
    
    @Override
    public String getVersion() {
        return "0.1";
    }
    
    @Override
    public String getVendor() {
        return "Neotropic SAS"; //NOI18N
    }

    @Override
    public String getCategory() {
        return "network/transport";
    }

    @Override
    public ModuleType getModuleType() {
        return ModuleType.TYPE_PERPETUAL_LICENSE;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void configureModule(ApplicationEntityManager aem, MetadataEntityManager mem, BusinessEntityManager bem) {
        this.mem = mem;
        this.bem = bem;
    }
    
    //The actual methods
    
}
