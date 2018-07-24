/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.sync.connectors.snmp.ip;

import static com.neotropic.kuwaiba.modules.ipam.IPAMModule.RELATIONSHIP_IPAMHASADDRESS;
import com.neotropic.kuwaiba.sync.model.SyncFinding;
import com.neotropic.kuwaiba.sync.model.SyncUtil;
import com.neotropic.kuwaiba.sync.model.TableData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.Pool;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessObject;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;
import org.openide.util.Exceptions;

/**
 * Synchronizer for the ipAddrTable data
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class IPSynchronizer {
    /**
     * The class name of the object
     */
    private final String className;
    /**
     * Device id
     */
    private final long id;
    /**
     * Current structure of the device
     */
    private final HashMap<Long, List<BusinessObjectLight>> currentObjectStructure;
    /**
     * To load the structure of the actual device
     */
    private final List<BusinessObjectLight> currentVirtualPorts;
    /**
     * The current first level children of the actual device
     */
    private List<BusinessObjectLight> currentFirstLevelChildren;
    /**
     * The current ports in the device
     */
    private final List<BusinessObjectLight> currentPorts;
    /**
     * The ipAddrTable table loaded into the memory
     */
    private final HashMap<String, List<String>> ipAddrTable;
    /**
     * The ifXTable table loaded into the memory
     */
    private final HashMap<String, List<String>> ifXTable;
    /**
     * reference to the bem
     */
    private BusinessEntityManager bem;
    /**
     * Reference to de aem
     */
    private ApplicationEntityManager aem;
    /**
     * Reference to de mem
     */
    private MetadataEntityManager mem;
    List<BusinessObjectLight> currentIps;

    public IPSynchronizer(BusinessObjectLight obj, List<TableData> data) {
         try {
            PersistenceService persistenceService = PersistenceService.getInstance();
            bem = persistenceService.getBusinessEntityManager();
            aem = persistenceService.getApplicationEntityManager();
            mem = persistenceService.getMetadataEntityManager();
        } catch (IllegalStateException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE,
                    ex.getClass().getSimpleName() + ": {0}", ex.getMessage()); //NOI18N
            bem = null;
            aem = null;
            mem = null;
        }
        this.className = obj.getClassName();
        this.id = obj.getId();
        ipAddrTable = (HashMap<String, List<String>>)data.get(0).getValue();
        ifXTable = (HashMap<String, List<String>>)data.get(1).getValue();
        currentObjectStructure = new HashMap<>();
        currentPorts = new ArrayList<>();
        currentFirstLevelChildren = new ArrayList<>();
        currentVirtualPorts = new ArrayList<>();
        currentIps = new ArrayList<>();
    }
    
    
    private void getCurrentIPAddress() throws MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, 
            BusinessObjectNotFoundException, InvalidArgumentException, InvalidArgumentException, OperationNotPermittedException, ApplicationObjectNotFoundException
    {
        List<String> ipAddresses = ipAddrTable.get("ipAdEntAddr");
        
        long subnetId = 46282;
        String subentClass = "SubnetIPv4";
        BusinessObject subnet = bem.getObject(subentClass, subnetId);
        
        List<BusinessObjectLight> children = bem.getObjectSpecialChildren(subentClass, subnetId);
        
        for (BusinessObjectLight child : children) {
            if(child.getClassName().equals(Constants.CLASS_IP_ADDRESS))
                currentIps.add(child);
        }
    }
    
    public List<SyncFinding> execute() throws OperationNotPermittedException, InvalidArgumentException, ApplicationObjectNotFoundException{
        try {
            readCurrentStructure(bem.getObjectChildren(className, id, -1), 1);
            readCurrentStructure(bem.getObjectSpecialChildren(className, id), 2);
            

            getCurrentIPAddress();
            associateIPAddress();
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    private void associateIPAddress() throws BusinessObjectNotFoundException, 
            OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException
    {
        List<String> ipAddresses = ipAddrTable.get("ipAdEntAddr");
        List<String> addrPortsIds = ipAddrTable.get("ipAdEntIfIndex");
        List<String> masks = ipAddrTable.get("ipAdEntNetMask");
        List<String> ifportIds = ifXTable.get("instance");
        List<String> portNames = ifXTable.get("ifName");
        for(int i=0; i < addrPortsIds.size(); i++){
            String portId = addrPortsIds.get(i);
            String ipAddress = ipAddresses.get(i);
            //We search for the ip address
            BusinessObjectLight currentIpAddress = searchIpAddress(ipAddress);
            
            //We create the ip address
            long subnetId = 46282;
            String subentClass = "SubnetIPv4";
            BusinessObject subnet = bem.getObject(subentClass, subnetId);

            if(subnet != null && ipAddress.contains("185")){
                    if(currentIpAddress == null){
                        HashMap<String, String> ipAttributes = new HashMap<>();
                        ipAttributes.put(Constants.PROPERTY_NAME, ipAddress);
                        long createSpecialObject = bem.createSpecialObject(Constants.CLASS_IP_ADDRESS, subnet.getClassName(), subnetId, ipAttributes, -1);
                        currentIpAddress = bem.getObject(createSpecialObject);
                    }
                
                    for(int j=0; j < ifportIds.size(); j++){
                        if(ifportIds.get(j).equals(portId)){
                            String portName = portNames.get(j);
                            BusinessObjectLight currentPort = searchInCurrentStructure(portName);
                            if(currentPort != null && currentIpAddress != null){
                                List<BusinessObjectLight> currentRelatedIPAddresses = bem.getSpecialAttribute(
                                        currentPort.getClassName(), 
                                        currentPort.getId(), RELATIONSHIP_IPAMHASADDRESS);
                                //We check if the port is already related with the ip
                                boolean alreadyRelated = false;
                                for (BusinessObjectLight currentRelatedIPAddress : currentRelatedIPAddresses) {
                                    if(currentRelatedIPAddress.getName().equals(currentIpAddress.getName())){ 
                                        alreadyRelated = true;
                                        break;
                                    }
                                }
                                if(!alreadyRelated)
                                    bem.createSpecialRelationship(currentPort.getClassName(),currentPort.getId(), 
                                        currentIpAddress.getClassName(), currentIpAddress.getId(), RELATIONSHIP_IPAMHASADDRESS, true);
                            }
                        }
                    }
            }
        }
    }
    
    /**
     * 
     * @param children
     * @param childrenType 1 children, 2 special children
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException 
     */
    private void readCurrentStructure(List<BusinessObjectLight> children, int childrenType) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException
    {
        for (BusinessObjectLight child : children) {
            if (child.getClassName().equals(Constants.CLASS_ELECTRICALPORT) || child.getClassName().equals(Constants.CLASS_SFPPORT) || child.getClassName().contains(Constants.CLASS_OPTICALPORT)) 
                currentPorts.add(child);
            else if (child.getClassName().equals(Constants.CLASS_VIRTUALPORT) || child.getClassName().equals(Constants.CLASS_MPLSTUNNEL))
                currentVirtualPorts.add(child);
            
            if (childrenType == 1) 
                readCurrentStructure(bem.getObjectChildren(child.getClassName(), child.getId(), -1), 1);
            else if (childrenType == 2) 
                readCurrentStructure(bem.getObjectSpecialChildren(child.getClassName(), child.getId()), 2);
        }
    }
    
    /**
     * Checks if a given port exists in the current structure
     * @param ifName a given name for port, virtual port or MPLS Tunnel
     * @return the object, null doesn't exists in the current structure
     */
    private BusinessObjectLight searchInCurrentStructure(String ifName){
        for(BusinessObjectLight currentPort: currentPorts){
            if(currentPort.getName().toLowerCase().equals(SyncUtil.wrapPortName(ifName.toLowerCase())))
                return currentPort;
        }
        for(BusinessObjectLight currentVirtualPort: currentVirtualPorts){
            if(currentVirtualPort.getName().toLowerCase().equals(SyncUtil.wrapPortName(ifName.toLowerCase())))
                return currentVirtualPort;
        }
        return null;
    }
    
    private BusinessObjectLight searchIpAddress(String ipAddress){
        for(BusinessObjectLight currentIpAddress : currentIps){
            if(currentIpAddress.getName().equals(ipAddress))
                return currentIpAddress;
        }
        return null;
    }
}
