/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.cpemanager;

import com.neotropic.kuwaiba.modules.GenericCommercialModule;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessObject;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.exceptions.ServerSideException;

/**
 * This class implements the functionality corresponding to manage QinQ technology
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class QinQModule implements GenericCommercialModule {
    /**
     * The MetadataEntityManager instance
     */
    private MetadataEntityManager mem;
    /**
     * The BusinessEntityManager instance
     */
    private BusinessEntityManager bem;
    /**
     * The ApplicationEntityManager instance
     */
    private ApplicationEntityManager aem;
            
    @Override
    public String getName() {
        return "QinQ Module"; //NOI18N
    }

    @Override
    public String getDescription() {
        return "QinQ Module, ";
    }
    
    @Override
    public String getVersion() {
        return "1.0";
    }
    
    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>"; //NOI18N
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
        this.aem = aem;
    }
   
    /**
     * Creates a EVLAN as special child of an inventory object (a GenericCommunicationsElement) 
     * @param objectId the inventory object id 
     * @param objectClassName the inventory object class name
     * @param attributesToBeSet attributes to be set to the EVLAN
     * @return the id of the new created EVLAN
     */
    public String createEVlank(String objectId, String objectClassName, HashMap<String, String> attributesToBeSet) throws ServerSideException{
        
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Gets the EVLANs of a given inventory object 
     * @param objectId the inventory object id 
     * @param objectClassName the inventory object class name
     * @return the list of the EVLANs that belongs to the inventory object
     */
    public List<BusinessObjectLight> getEVlans(String objectId, String objectClassName){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates. 
    }
    
    /**
     * Gets the EVLANs of a given inventory object, filtered by a given type
     * @param objectId the inventory object id 
     * @param objectClassName the inventory object class name
     * @param typeToFilter type to filter EVLANs (HSI, IPTV, VoIP)
     * @return a filter list of the EVLANs that belongs to the inventory object
     * @throws ServerSideException 
     */
    public List<BusinessObject> getEVlanByType(String objectId, String objectClassName, String typeToFilter) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Relates a EVLAN with an interface (a GenericPort)
     * @param evalanId EVLAN id
     * @param interfaceClassName interface class name
     * @param interfaceId interface id
     * @throws ServerSideException 
     */
    public void relateEVlanWithInterface(String evalanId, String interfaceClassName, String interfaceId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Release a EVLAN from an interface (a GenericPort)
     * @param evalanId EVLAN id
     * @param interfaceClassName interface class name
     * @param interfaceId interface id
     * @throws ServerSideException 
     */
    public void releaseEVlanFromInterface(String evalanId, String interfaceClassName, String interfaceId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Deletes EVLANs
     * @param evlanId
     * @param forceDelete deletes the EVLAN and all its CVLANs
     * @throws InventoryException If the object can not be found
     *                            If either the object class or the attribute can not be found
     *                            If the class could not be found
     *                            If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     * @throws NotAuthorizedException
     */
    public void deleteEVlans(String[] evlanId, boolean[] forceDelete) 
            throws InventoryException, NotAuthorizedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Creates a CVLAN as special child of a given EVLAN
     * @param evlanId EVLAN id
     * @param attributesToBeSet attributes to be set to the CVLAN
     * @return the id of the new created CVLAN
     * @throws ServerSideException 
     */
    public String createCVlank(String evlanId, HashMap<String, String> attributesToBeSet) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Gets the CVLANS from a EVLAN 
     * @param evlanId the EVLAN id
     * @return the list of CVLANs that belongs to the EVLAN
     * @throws ServerSideException 
     */
    public List<BusinessObjectLight> getCVlans(String evlanId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates. 
    }
    
    /**
     * Gets the CVLANS from a EVLAN filtered by the CVLAN's state
     * @param evlanId the EVLAN id
     * @param stateToFilter state to filter for
     * @return a filtered list of CVLANs that belongs to the EVLAN
     */
    public List<BusinessObjectLight> getCVlansByState(String evlanId, String stateToFilter){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates. 
    }
    
    /**
     * Deletes CVLANs from a EVLAN
     * @param evlanId the given EVLAN id parent of the CVLANs
     * @param cvlanIds the given EVLANs ids to be delete
     */
    public void deleteCVlans(String evlanId, String[] cvlanIds){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates. 
    }
    
    /**
     * Relates a CVLAN to a interface in a ONT
     * @param cvlanId the CVLAN id
     * @param interfaceClassName the interface class name in the ONT
     * @param interfaceId the interface id in the ONT
     */
    public void relateCVlanToOnt(String cvlanId, String interfaceClassName, String interfaceId){
       throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Release a CVLAN from a interface in a ONT
     * @param cvlanId the CVLAN id
     * @param interfaceClassName the interface class name in the ONT
     * @param interfaceId the interface id in the ONT
     */
    public void releaseCVlanFromOnt(String cvlanId, String interfaceClassName, String interfaceId){
       throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}