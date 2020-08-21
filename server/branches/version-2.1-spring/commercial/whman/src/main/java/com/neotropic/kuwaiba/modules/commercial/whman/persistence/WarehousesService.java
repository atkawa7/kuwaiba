/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.whman.persistence;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to manage warehouses
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Service
public class WarehousesService {
    /**
     * Reference to the Application Entity Manager
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Relationship used to assign a Warehouse or VirtualWarehouse to a GenericLocation
     */
    public static final String RELATIONSHIP_HASWAREHOUSE = "hasWarehouse";
    
    @PostConstruct
    public void init() {
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