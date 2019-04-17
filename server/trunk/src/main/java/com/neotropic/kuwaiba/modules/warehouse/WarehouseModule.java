/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.neotropic.kuwaiba.modules.warehouse;

import com.neotropic.kuwaiba.modules.GenericCommercialModule;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.Pool;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * Warehouse module preliminary implementation. Used to manage the elements that are not in operation
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WarehouseModule implements GenericCommercialModule {
    /**
     * Application Entity Manager Instance
     */
    private ApplicationEntityManager aem;
    /**
     * Business Entity Manager Instance
     */
    private BusinessEntityManager bem;
    /**
     * Metadata Entity Manager Instance
     */
    private MetadataEntityManager mem;
    /**
     * Relationship used to assign a Warehouse or VirtualWarehouse to a GenericLocation
     */
    public static final String RELATIONSHIP_HASWAREHOUSE = "hasWarehouse";
    
    public static final String MODULE_NAME = "Warehouse Module";
    
    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public String getDescription() {
        return "Warehouses management module";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>";
    }

    @Override
    public String getCategory() {
        return "";
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
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_HASWAREHOUSE, "Has Warehouse");
    }
    
    /**
     * Gets the Warehouse Module Root Pools
     * @return A list of root pools
     * @throws MetadataObjectNotFoundException If the classes Warehouse or VirtualWarehouse could not be found.
     * @throws InvalidArgumentException If any pool does not have uuid
     */
    public List<Pool> getWarehouseRootPools() throws MetadataObjectNotFoundException, InvalidArgumentException {
        List<Pool> warehousePools = bem.getRootPools(Constants.CLASS_WAREHOUSE, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        List<Pool> virtualWarehousePools = bem.getRootPools(Constants.CLASS_VIRTUALWAREHOUSE, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        // If the Warehouse root pool does not exist then it is created
        if (warehousePools.isEmpty()) {
            aem.createRootPool(Constants.NODE_WAREHOUSE, Constants.NODE_WAREHOUSE, Constants.CLASS_WAREHOUSE, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
            warehousePools = bem.getRootPools(Constants.CLASS_WAREHOUSE, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        }
        // If the VirtualWarehouse root pool does not exist then it is created
        if (virtualWarehousePools.isEmpty()) {
            aem.createRootPool(Constants.NODE_VIRTUALWAREHOUSE, Constants.NODE_VIRTUALWAREHOUSE, Constants.CLASS_VIRTUALWAREHOUSE, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
            virtualWarehousePools = bem.getRootPools(Constants.CLASS_VIRTUALWAREHOUSE, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        }
        List<Pool> warehouseRootPools = new ArrayList();
        warehouseRootPools.addAll(warehousePools);
        warehouseRootPools.addAll(virtualWarehousePools);
        
        return warehouseRootPools;

    }
    
}
