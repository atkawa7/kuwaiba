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
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.beans.WebserviceBean;
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
     * To load the structure of the actual device
     */
    private final List<BusinessObjectLight> currentVirtualPorts;
    /**
     * The current map pf subnets and sub-subnets
     */
    private final HashMap<BusinessObjectLight, List<BusinessObjectLight>> subnets;
    /**
     * The current subnets with its ips
     */
    private final HashMap<BusinessObjectLight, List<BusinessObjectLight>> ips;
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
     * Reference to the root node of the IPv4 
     */
    private Pool ipv4Root;
    /**
     * reference to the bem
     */
    private BusinessEntityManager bem;
    /**
     * Reference to the aem
     */
    private ApplicationEntityManager aem;
    /**
     * Reference to the mem
     */
    private MetadataEntityManager mem;
    
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
        currentPorts = new ArrayList<>();
        subnets = new HashMap<>();
        ips = new HashMap<>();
        currentVirtualPorts = new ArrayList<>();
    }
    
    /**
     * Executes the synchronization of the ipAddrTable
     * @return list of findings
     * @throws OperationNotPermittedException
     * @throws InvalidArgumentException
     * @throws ApplicationObjectNotFoundException
     * @throws ArraySizeMismatchException 
     */
    public List<SyncFinding> execute() throws OperationNotPermittedException, 
            InvalidArgumentException, ApplicationObjectNotFoundException, 
            ArraySizeMismatchException
    {
        try {
            readCurrentStructure(bem.getObjectChildren(className, id, -1), 1);
            readCurrentStructure(bem.getObjectSpecialChildren(className, id), 2);
            //we get the rood nodes for the ipv4
            List<Pool> ipv4RootPools = aem.getRootPools(Constants.CLASS_SUBNET_IPV4, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
            ipv4Root = ipv4RootPools.get(0);
            readcurrentFolder(ipv4RootPools);
            readCurrentSubnets(ipv4Root);
            associateIPAddress();
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
   
    /**
     * Search for a given IP address got it from the ipAddrTableMIB data
     * if doesn't exists it will be created
     * @param ipAddr the ip address
     * @param mask the ip address mask
     * @return the ip from kuwaiba
     * @throws InvalidArgumentException
     * @throws BusinessObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws ApplicationObjectNotFoundException
     * @throws ArraySizeMismatchException 
     */
    private BusinessObjectLight searchIP(String ipAddr, String mask) 
            throws InvalidArgumentException, BusinessObjectNotFoundException, 
            MetadataObjectNotFoundException, OperationNotPermittedException, 
            ApplicationObjectNotFoundException, ArraySizeMismatchException
    {
        //We will consider only a /24 subnet 
        String []ipAddrSegments = ipAddr.split("\\.");
        String newSubnet =  ipAddrSegments[0] + "." + ipAddrSegments[1] + "." + ipAddrSegments[2];
        BusinessObjectLight currentSubnet = null;
        //we look for the subnet
        for(BusinessObjectLight subnet : subnets.keySet()){
            if(subnet.getName().equals(newSubnet + ".0/24")){
                currentSubnet = subnet;
                break;
            }
        }//we create the subnet if doesn't exists
        if(currentSubnet == null){
            String [] attributeNames = {"name", "description", "networkIp", "broadcastIp", "hosts"};
            String [] attributeValues = {newSubnet + ".0/24", "sync", newSubnet + ".0", newSubnet + ".255", "254"};
            currentSubnet = bem.getObject(bem.createPoolItem(ipv4Root.getId(), ipv4Root.getClassName(), attributeNames, attributeValues, 0));
            //we must add the new subnet into the current subnets and ips
            subnets.put(currentSubnet, new ArrayList<>()); 
            ips.put(currentSubnet, new ArrayList<>());
        }
        //with the subnet found we must search the if the IP address exists
        List<BusinessObjectLight> currentIps = ips.get(currentSubnet);
        if(!currentIps.isEmpty()){
            for (BusinessObjectLight currentIp : currentIps) {
                if(currentIp.getName().equals(ipAddr)){
                    //we must check the mask if the IP already exists and updated if is need it
                    BusinessObject ip = bem.getObject(currentIp.getId()); 
                    if(!ip.getAttributes().get(Constants.PROPERTY_MASK).equals(mask)){
                        ip.getAttributes().put(Constants.PROPERTY_MASK, mask);
                        bem.updateObject(ip.getClassName(), ip.getId(), ip.getAttributes());
                    }
                    return currentIp;
                }
            }
        }//we create the ip address if doesn't exists in subnet
        
        HashMap<String, String> ipAttributes = new HashMap<>();
        ipAttributes.put(Constants.PROPERTY_NAME, ipAddr);
        ipAttributes.put(Constants.PROPERTY_MASK, mask);
        long createdIp = bem.createSpecialObject(Constants.CLASS_IP_ADDRESS, currentSubnet.getClassName(), currentSubnet.getId(), ipAttributes, -1);
        BusinessObject ip = bem.getObject(createdIp);
        ips.get(currentSubnet).add(ip);
        return ip;
    }
    
    
    /**
     * Reads the MIB data an associate IP addresses with ports
     * @throws BusinessObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     * @throws ApplicationObjectNotFoundException
     * @throws ArraySizeMismatchException 
     */
    private void associateIPAddress() throws BusinessObjectNotFoundException, 
            OperationNotPermittedException, MetadataObjectNotFoundException, 
            InvalidArgumentException, ApplicationObjectNotFoundException, 
            ArraySizeMismatchException
    {
        List<String> ipAddresses = ipAddrTable.get("ipAdEntAddr");
        List<String> addrPortsIds = ipAddrTable.get("ipAdEntIfIndex");
        List<String> masks = ipAddrTable.get("ipAdEntNetMask");
        List<String> ifportIds = ifXTable.get("instance");
        List<String> portNames = ifXTable.get("ifName");
        for(int i=0; i < addrPortsIds.size(); i++){
            String portId = addrPortsIds.get(i);
            String ipAddress = ipAddresses.get(i);
            String mask = masks.get(i);
            //We search for the ip address
            BusinessObjectLight currentIpAddress = searchIP(ipAddress, mask);
            if(currentIpAddress != null){
                for(int j=0; j < ifportIds.size(); j++){
                    if(ifportIds.get(j).equals(portId)){
                        String portName = portNames.get(j);
                        BusinessObjectLight currentPort = searchInCurrentStructure(portName);
                        if(currentPort != null && currentIpAddress != null){
                            List<BusinessObjectLight> currentRelatedIPAddresses = bem.getSpecialAttribute(
                                    currentPort.getClassName(), 
                                    currentPort.getId(), RELATIONSHIP_IPAMHASADDRESS);
                            //We check if the interface is already related with the ip
                            boolean alreadyRelated = false;
                            for (BusinessObjectLight currentRelatedIPAddress : currentRelatedIPAddresses) {
                                if(currentRelatedIPAddress.getName().equals(currentIpAddress.getName())){ 
                                    alreadyRelated = true;
                                    break;
                                }
                            }//If not related, we related interface with the ip
                            if(!alreadyRelated)
                                bem.createSpecialRelationship(currentPort.getClassName(),currentPort.getId(), 
                                    currentIpAddress.getClassName(), currentIpAddress.getId(), RELATIONSHIP_IPAMHASADDRESS, true);
                        }
                        else
                            System.out.println(String.format("sync the ifMIB, the port %s was not found", portName));
                    }
                }
            }
        }
    }
    
    /**
     * Reads the device's current structure (ports, and logical ports)
     * @param children a given set of children
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
    * Reads the current folders in the IPAM 
    * @param ifName a given name for port, virtual port or MPLS Tunnel
    * @return the object, null doesn't exists in the current structure
    */
    private void readcurrentFolder(List<Pool> folders) 
            throws ApplicationObjectNotFoundException, 
            MetadataObjectNotFoundException, BusinessObjectNotFoundException
    {
        for (Pool folder : folders) {
            if(!folders.isEmpty())
                readcurrentFolder(aem.getPoolsInPool(folder.getId(), folder.getClassName()));
            readCurrentSubnets(folder);
        }
    }
    
    /**
     * Gets the subnets in a given the folder from the IPAM module
     * @param folder a given folder from the IPAM
     * @throws ApplicationObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException 
     */
    private void readCurrentSubnets(Pool folder) 
            throws ApplicationObjectNotFoundException, 
            MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        //we read the subnets of the folder
        List<BusinessObjectLight> subnetsInFolder = aem.getPoolItems(folder.getId(), -1);
        for (BusinessObjectLight subnet : subnetsInFolder) {
            //we save the subnet
            if(subnets.get(subnet) == null)
                subnets.put(subnet, new ArrayList<>());
            if(ips.get(subnet) == null)
                ips.put(subnet, new ArrayList<>());
            if(!subnetsInFolder.isEmpty())//we get the subnets inside folders
                readCurrentSubnetChildren(subnet);
        }
    }
    
    /**
     * Reads recursively the subnets its sub-subnets and its IPs addresses 
     * @param subnet a given subnet
     * @throws ApplicationObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException 
     */
    private void readCurrentSubnetChildren(BusinessObjectLight subnet) 
        throws ApplicationObjectNotFoundException, 
        MetadataObjectNotFoundException, BusinessObjectNotFoundException 
    {
        //we get the ips and the subnets inside subents
        List<BusinessObjectLight> subnetChildren = bem.getObjectSpecialChildren(subnet.getClassName(), subnet.getId());
        for (BusinessObjectLight subnetChild : subnetChildren) {
            if(subnetChild.getClassName().equals(Constants.CLASS_SUBNET_IPV4) || 
                subnetChild.getClassName().equals(Constants.CLASS_SUBNET_IPV6))
                    subnets.get(subnet).add(subnetChild);
            else
                ips.get(subnet).add(subnetChild);
            
            if(!subnetChildren.isEmpty())
                readCurrentSubnetChildren(subnetChild);
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
}
