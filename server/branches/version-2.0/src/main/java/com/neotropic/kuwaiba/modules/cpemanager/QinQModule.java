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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;
import org.omg.PortableServer.POAPackage.ObjectNotActive;

/**
 * This class implements the functionality corresponding to manage QinQ technology
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class QinQModule implements GenericCommercialModule {
    
    /**
     * Relationship to associate a port with vlans
     */
    public static final String RELATIONSHIP_PORT_BELONGS_TO_VLAN = "portBelongsToVlan";
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
    public String createEVlan(String objectId, String objectClassName, HashMap<String, String> attributesToBeSet) throws ServerSideException{
        try{
            int MAX_CVLANS = 4096; //by default every EVLAN has 4096 CVLANs
            int RESERVERD_CVLANS = MAX_CVLANS - 5; //and 5 of those 4096 CVLANs are created as reserved by default
            
            String newVlanId = bem.createSpecialObject(Constants.CLASS_EVLAN, objectClassName, objectId, attributesToBeSet, -1);
            
            List<BusinessObjectLight> listTypeItems = aem.getListTypeItems("CVLANState");
            String reservedStateListTypeId = null;
            String freeStateListTypeId = null;
            //the first 4090 CVLANs are created with state free, the last 5 will have reserved state
            for (BusinessObjectLight listTypeItem : listTypeItems) {
                if(listTypeItem.getName().equals("reserved"))
                    reservedStateListTypeId = listTypeItem.getId();
                else if(listTypeItem.getName().equals("free"))
                    freeStateListTypeId = listTypeItem.getId();
            }
            
            if(reservedStateListTypeId == null || freeStateListTypeId == null)
                throw new InvalidArgumentException("States: -reserved- and -free- has not been created as listTypes of class CVLANState");
             
            HashMap<String, String> cvlanAttributes = new HashMap<>();
            for (int i=0; i<MAX_CVLANS; i++){
                cvlanAttributes.put(Constants.PROPERTY_NAME, Integer.toString(i));
                if(i > RESERVERD_CVLANS)
                    cvlanAttributes.put("CVLANState", reservedStateListTypeId);
                else
                    cvlanAttributes.put("CVLANState", freeStateListTypeId);
                
                createCVlan(newVlanId, attributesToBeSet);
            }
            
            return newVlanId;
        }catch(Exception ex){
            throw new ServerSideException(ex.getMessage()); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    /**
     * Gets the EVLANs of a given inventory object 
     * @param objectId the inventory object id 
     * @param objectClassName the inventory object class name
     * @return the list of the EVLANs that belongs to the inventory object
     */
    public List<BusinessObjectLight> getEVlans(String objectId, String objectClassName) throws ServerSideException{
        List<BusinessObjectLight> currentEVlans = new ArrayList<>();
        try{
            List<BusinessObjectLight> objectSpecialChildren = bem.getObjectSpecialChildren(objectClassName, objectId);
            for (BusinessObjectLight child : objectSpecialChildren) {
                if(child.getClassName().equalsIgnoreCase(Constants.CLASS_EVLAN))
                    currentEVlans.add(child);
            }
            return currentEVlans;
        }catch(Exception ex){
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    /**
     * Gets the EVLANs of a given inventory object, filtered by a given type
     * @param objectId the inventory object id 
     * @param objectClassName the inventory object class name
     * @param typeToFilter type to filter EVLANs (is a list type: EvlanType, e.g. values: HSI, IPTV, VoIP)
     * @return a filter list of the EVLANs that belongs to the inventory object
     * @throws ServerSideException 
     */
    public List<BusinessObjectLight> getEVlansByType(String objectId, String objectClassName, String typeToFilter) throws ServerSideException {
        List<BusinessObjectLight> currentEVlans = new ArrayList<>();
        try{
            List<BusinessObjectLight> objectSpecialChildren = bem.getObjectSpecialChildren(objectClassName, objectId);
            for (BusinessObjectLight child : objectSpecialChildren) {
                if(child.getClassName().equalsIgnoreCase(Constants.CLASS_EVLAN)){
                    String attributeValue = bem.getAttributeValueAsString(child.getClassName(), child.getId(), "EVLANType");
                    if(attributeValue != null && attributeValue.equals(typeToFilter))
                        currentEVlans.add(child);
                }
            }
            return currentEVlans;
        }catch(Exception ex){
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    /**
     * Relates a EVLAN with an interface (a GenericPort)
     * @param evlanId EVLAN id
     * @param interfaceClassName interface class name
     * @param interfaceId interface id
     * @throws ServerSideException 
     */
    public void relateEVlanWithInterface(String evlanId, String interfaceClassName, String interfaceId) throws ServerSideException {
        try{
           bem.createSpecialRelationship(interfaceClassName, interfaceId, Constants.CLASS_EVLAN, evlanId, RELATIONSHIP_PORT_BELONGS_TO_VLAN, false);
        }catch(Exception ex){
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    /**
     * Release a EVLAN from an interface (a GenericPort)
     * @param evlanId EVLAN id
     * @param interfaceClassName interface class name
     * @param interfaceId interface id
     * @throws ServerSideException 
     */
    public void releaseEVlanFromInterface(String evlanId, String interfaceClassName, String interfaceId) throws ServerSideException {
        try{
            bem.releaseSpecialRelationship(Constants.CLASS_EVLAN, evlanId, interfaceClassName, interfaceId);
        }catch(Exception ex){
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    /**
     * Deletes EVLANs
     * @param eVlanIds the EVLAN ids
     * @param forceDelete deletes the EVLAN and all its CVLANs
     * @throws ServerSideException
     */
    public void deleteEVlans(String[] eVlanIds, boolean forceDelete) throws ServerSideException {
        try{
            HashMap<String,List<String>> objects = new HashMap<>();
            List<String> newIdList = new ArrayList<>();
            for(String evlanId : eVlanIds)
                newIdList.add(evlanId);

            objects.put(Constants.CLASS_EVLAN, newIdList);

            bem.deleteObjects(objects, forceDelete);

        }catch(Exception ex){
            throw new ServerSideException(ex.getLocalizedMessage());
        }
    }
    
    /**
     * Creates a CVLAN as special child of a given EVLAN
     * @param eVlanId EVLAN id
     * @param attributesToBeSet attributes to be set to the CVLAN
     * @return the id of the new created CVLAN
     * @throws ServerSideException 
     */
    public String createCVlan(String eVlanId, HashMap<String, String> attributesToBeSet) throws ServerSideException {
        try{
            return bem.createSpecialObject(Constants.CLASS_CVLAN, Constants.CLASS_EVLAN, eVlanId, attributesToBeSet, -1);
        }catch(Exception ex){
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    /**
     * Gets the CVLANS from a EVLAN 
     * @param eVlanId the EVLAN id
     * @return the list of CVLANs that belongs to the EVLAN
     * @throws ServerSideException 
     */
    public List<BusinessObjectLight> getCVlans(String eVlanId) throws ServerSideException {
        List<BusinessObjectLight> cVlans = new ArrayList<>();
        try{
            List<BusinessObjectLight> objectSpecialChildren = bem.getObjectSpecialChildren(Constants.CLASS_EVLAN, eVlanId);
            for(BusinessObjectLight child  : objectSpecialChildren){
                if(child.getClassName().equals(Constants.CLASS_CVLAN))
                    cVlans.add(child);
            }
            return cVlans;
        }catch(Exception ex){
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    /**
     * Gets the CVLANS from a EVLAN filtered by the CVLAN's state
     * @param eVlanId the EVLAN id
     * @param stateToFilter state to filter for, is a list type (CVLANStateType)
     * @return a filtered list of CVLANs that belongs to the EVLAN
     */
    public List<BusinessObjectLight> getCVlansByState(String eVlanId, String stateToFilter) throws ServerSideException {
        List<BusinessObjectLight> cVlans = new ArrayList<>();
        try{
            List<BusinessObjectLight> objectSpecialChildren = bem.getObjectSpecialChildren(Constants.CLASS_EVLAN, eVlanId);
            for(BusinessObjectLight child  : objectSpecialChildren){
                if(child.getClassName().equals(Constants.CLASS_CVLAN)){
                    String attributeValue = bem.getAttributeValueAsString(Constants.CLASS_CVLAN, eVlanId, "CVLANState"); //NOI18N
                    if(attributeValue.equals(stateToFilter))
                        cVlans.add(child);
                }
            }
            return cVlans;
        }catch(Exception ex){
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    /**
     * Deletes CVLANs from a EVLAN
     * @param cVlanIds the given EVLANs ids to be delete
     * @param forceDelete deletes the CVLANs even if they already related?
     */
    public void deleteCVlans(String[] cVlanIds, boolean forceDelete) throws ServerSideException{
        try{
            HashMap<String,List<String>> objects = new HashMap<>();
            List<String> newIdList = new ArrayList<>();
            for(String evlanId : cVlanIds)
                newIdList.add(evlanId);

            objects.put(Constants.CLASS_CVLAN, newIdList);

            bem.deleteObjects(objects, forceDelete);

        }catch(Exception ex){
            throw new ServerSideException(ex.getLocalizedMessage());
        }
    }
    
    /**
     * Relates a CVLAN to a interface in a ONT
     * @param cVlanId the CVLAN id
     * @param interfaceClassName the interface class name in the ONT
     * @param interfaceId the interface id in the ONT
     */
    public void relateCVlanToOntInterface(String cVlanId, String interfaceClassName, String interfaceId) throws ServerSideException{
        try{
            List<BusinessObjectLight> parents = bem.getParents(interfaceClassName, interfaceId);
            boolean parentIsOnt = false;
            for(BusinessObjectLight parent : parents){
                if(parent.getClassName().equals("ONT")){
                    parentIsOnt = true;
                    break;
                }
            }
            if(parentIsOnt)
                bem.createSpecialRelationship(interfaceClassName, interfaceId, Constants.CLASS_CVLAN, cVlanId, RELATIONSHIP_PORT_BELONGS_TO_VLAN, false);
            else
                throw new InvalidArgumentException(String.format("The Interface you are trying to relate to the CVLAN is not child of an ONT"));
        }catch(Exception ex){
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    /**
     * Release a CVLAN from a interface in a ONT
     * @param cVlanId the CVLAN id
     * @param interfaceClassName the interface class name in the ONT
     * @param interfaceId the interface id in the ONT
     */
    public void releaseCVlanFromOntInterface(String cVlanId, String interfaceClassName, String interfaceId) throws ServerSideException{
        try{
            bem.releaseSpecialRelationship(Constants.CLASS_CVLAN, cVlanId, interfaceClassName, interfaceId);
        }catch(Exception ex){
            throw new ServerSideException(ex.getMessage());
        }
    }
}