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

package com.neotropic.kuwaiba.modules.commercial.ipam;

import static com.neotropic.kuwaiba.modules.commercial.ipam.IpamModule.RELATIONSHIP_IPAMBELONGSTOVLAN;
import static com.neotropic.kuwaiba.modules.commercial.ipam.IpamModule.RELATIONSHIP_IPAMBELONGSTOVRFINSTACE;
import static com.neotropic.kuwaiba.modules.commercial.ipam.IpamModule.RELATIONSHIP_IPAMHASADDRESS;
import static com.neotropic.kuwaiba.modules.commercial.ipam.IpamModule.RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ArraySizeMismatchException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The logic behind the IPAM module.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class IpamModuleService {
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
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
     * Get the default pool nodes for IPv4 and IPv6 subnets
     * @return default pool for IPv4 and IPv6
     * @throws NotAuthorizedException 
     */
    private List<Pool> getDefaultIPAMRootNodes() throws NotAuthorizedException, MetadataObjectNotFoundException, InvalidArgumentException{
        List<Pool> ipv4RootPools = bem.getRootPools(Constants.CLASS_SUBNET_IPV4, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        List<Pool> ipv6RootPools = bem.getRootPools(Constants.CLASS_SUBNET_IPV6, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        
        List<Pool> rootSubnetPools = new ArrayList<>();
        if(ipv4RootPools.isEmpty() || ipv6RootPools.isEmpty()){
            createRootNodes();
            ipv4RootPools = bem.getRootPools(Constants.CLASS_SUBNET_IPV4, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
            ipv6RootPools = bem.getRootPools(Constants.CLASS_SUBNET_IPV6, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        }
        
        rootSubnetPools.addAll(ipv4RootPools);
        rootSubnetPools.addAll(ipv6RootPools);
        
        return rootSubnetPools;
    }
    
    /**
     * Create the IPv4 and IPv6 default nodes if they don't exist.
     * @throws MetadataObjectNotFoundException If the class IPAddress don't exist
     * @throws NotAuthorizedException If the user is not authorized to create pool nodes
     */
    private void createRootNodes() throws MetadataObjectNotFoundException, NotAuthorizedException{
        aem.createRootPool(Constants.NODE_IPV6ROOT, Constants.NODE_IPV6ROOT, 
                Constants.CLASS_SUBNET_IPV6, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
        aem.createRootPool(Constants.NODE_IPV4ROOT, Constants.NODE_IPV4ROOT, 
                Constants.CLASS_SUBNET_IPV4, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
        //getDefaultIPAMRootNodes();
    }
    
    /**
     * Creates a pool of subnets if the parentId is -1 the pool will be created 
     * in the default root for pools of subnets
     * @param parentId the given parent id if the id is -1 it means the parent 
     * could be one of the default root nodes.
     * @param subnetPoolName subnet pool name
     * @param subnetPoolDescription subnet pool description
     * @param className if is a IPv4 subnet or if is a IPv6 subnet
     * @return the id of the created new subnet pool
     * @throws InvalidArgumentException if something goes wrong and can't reach the backend
     * @throws MetadataObjectNotFoundException If the class IPv4 o IPv6 doesn't exists
     * @throws NotAuthorizedException If the user is not authorized to create pool nodes
     * @throws ApplicationObjectNotFoundException if the IPAM root nodes doesn't exists
     */
    public String createSubnetsPool(String parentId, String subnetPoolName, 
            String subnetPoolDescription, String className) throws InvalidArgumentException, 
            MetadataObjectNotFoundException, NotAuthorizedException, ApplicationObjectNotFoundException 
    {
        if (aem == null)
           throw new InvalidArgumentException("Can't reach the backend. Contact your administrator");
        return aem.createPoolInPool(parentId, subnetPoolName, subnetPoolDescription, className, 2);
    }
    
    /**
     * Get a subnet
     * @param className if is a IPv4 subnet or if is a IPv6 subnet
     * @param oid subnet id
     * @return the subnet
     * @throws MetadataObjectNotFoundException If the class IPv4 o IPv6 can't be find
     * @throws BusinessObjectNotFoundException if the requested object(subnet) can't be found
     * @throws InvalidArgumentException if the requested object(subnet) can't be found
     * @throws NotAuthorizedException If the user is not authorized to get subnets
     */
    public BusinessObject getSubnet(String className, String oid) throws MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, InvalidArgumentException, NotAuthorizedException
    {
        return bem.getObject(className, oid);
    }
    
    /**
     * Get a subnet pool
     * @param oid subnet id
     * @return a subnet pool
     * @throws ApplicationObjectNotFoundException if the subnet pool can't be found
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException It the subnet pool does not have uuid
     */
    public Pool getSubnetPool(String oid) throws NotAuthorizedException, InvalidArgumentException, 
            ApplicationObjectNotFoundException
    {
        return bem.getPool(oid);
    }
    
    /**
     * Get a set of subnet pools from a pool of subnets or from the root
     * @param parentId parent id
     * @param className if is a IPv4 subnet or if is a IPv6 subnet
     * @return a list of subnet pools
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws ApplicationObjectNotFoundException if can't get the pools of a subnet pool
     * @throws MetadataObjectNotFoundException if there are not IPAM root nodes
     * @throws InvalidArgumentException If the parent does not have uuid
     */
    public List<Pool> getSubnetPools(String parentId, String className) 
            throws NotAuthorizedException, ApplicationObjectNotFoundException, InvalidArgumentException, 
            MetadataObjectNotFoundException {
        if("-1".equals(parentId) && className == null)
            return getDefaultIPAMRootNodes();
        
        return bem.getPoolsInPool(parentId, className);
    }
   
    /**
     * Get a set of subnets from a pool of subnets
     * @param limit limit of results, no limit -1
     * @param subnetPoolId subnet pool id
     * @return a list of subnets
     * @throws ApplicationObjectNotFoundException if the given subnet pool id is not valid
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the subnet pool does not have uuid
     */
    public List<BusinessObjectLight> getSubnets(int limit, 
            String subnetPoolId) throws ApplicationObjectNotFoundException, 
            NotAuthorizedException, InvalidArgumentException
    {
        return bem.getPoolItems(subnetPoolId, limit);
    }
    
    /**
     * create a subnet
     * @param parentId subnet pool id
     * @param className subnet class name
     * @param attributeNames subnet attributes, networkIP, broadcastIP, hosts
     * @param attributeValues subnet attribute values
     * @return new subnet id
     * @throws ApplicationObjectNotFoundException can't find the parent(a subnet pool) to create the subnet
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @throws ArraySizeMismatchException If attributeNames and attributeValues have different sizes.
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws MetadataObjectNotFoundException If can't find IPv4 or IPV6 classes
     * @throws BusinessObjectNotFoundException Thrown if the parent id is not found
     * @throws OperationNotPermittedException  If the update can't be performed due to a format issue
     */
    public String createSubnet(String parentId, String className, String[] attributeNames, 
            String[] attributeValues) throws InvalidArgumentException, 
            ArraySizeMismatchException, NotAuthorizedException,  
            MetadataObjectNotFoundException, OperationNotPermittedException, 
            BusinessObjectNotFoundException, ApplicationObjectNotFoundException
    {
        try {
            Map<String, String> attributes = IntStream.range(0, attributeNames.length).boxed()
             .collect(Collectors.toMap(i -> attributeNames[i], i -> attributeValues[i]));
            return bem.createPoolItem(parentId, className, new HashMap(attributes), null);
        } catch (ApplicationObjectNotFoundException ex) {
            
            HashMap<String, String> attributes = new HashMap<>();
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], attributeValues[i]);
            
            return bem.createSpecialObject(className, className, parentId, attributes, null);
        }
    }
    
    
    /**
     * Deletes a subnet
     * @param subnetIds subnet ids
     * @param className subnets class name
     * @param releaseRelationships release any relationship 
     * @throws BusinessObjectNotFoundException If the requested subnet can't be found
     * @throws MetadataObjectNotFoundException If the requested object class can't be found (problems with IPv4 or IPV6 classes)
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked or it has relationships and releaseRelationships is false
     * @throws InvalidArgumentException If it was not possible to release the possible unique attributes
     */
    public void deleteSubnets(String className, List<String> subnetIds, boolean releaseRelationships) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, 
            OperationNotPermittedException, InvalidArgumentException {
        HashMap<String, List<String>> objectsToBeDeleted = new HashMap<>();
        objectsToBeDeleted.put(className, subnetIds);
        bem.deleteObjects(objectsToBeDeleted, releaseRelationships);
    }
    
    /**
     * deletes a subnet Pool
     * @param subnetPoolsId subnet ids 
     * @throws OperationNotPermittedException If any of the objects in the pool can not be deleted because it's not a business related instance (it's more a security restriction)
     * @throws ApplicationObjectNotFoundException  If the subnet pool can't be found
     */
    public void deleteSubnetPools(String[] subnetPoolsId) 
            throws OperationNotPermittedException, 
            ApplicationObjectNotFoundException
    {
        aem.deletePools(subnetPoolsId);
    }

    /**
     * creates an IP address inside a subnet
     * @param parentSubnetId subnet Id
     * @param parentSubnetClassName if is a IPv4 or an IPv6 subnet
     * @param ipAttributes ip Address attributes, name description
     * @return IP address id
     * @throws ApplicationObjectNotFoundException Can't find the parent(a subnet) to create the IP address
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @throws ArraySizeMismatchException If attributeNames and attributeValues have different sizes.
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws MetadataObjectNotFoundException if can't find IPv4 or IPV6 classes
     * @throws BusinessObjectNotFoundException Thrown if the parent(the subnet) id is not found
     * @throws OperationNotPermittedException If the update can't be performed due to a format issue
     */
    public String addIPAddress(String parentSubnetId, String parentSubnetClassName, HashMap<String, String> ipAttributes) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, 
            ArraySizeMismatchException, NotAuthorizedException, MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, OperationNotPermittedException
    {
        return bem.createSpecialObject(Constants.CLASS_IP_ADDRESS, parentSubnetClassName, parentSubnetId, ipAttributes, null);
    }

    /**
     * Removes an IP address from a subnet
     * @param ipIds IP addresses ids
     * @param releaseRelationships release existing relationships
     * @throws BusinessObjectNotFoundException If the requested IP address can't be found
     * @throws MetadataObjectNotFoundException If can't find IPv4 or IPV6 classes
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked or it has relationships and releaseRelationships is false
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the ip ids do no have uuid
     */
    public void removeIP(String[] ipIds, boolean releaseRelationships) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, 
            OperationNotPermittedException, NotAuthorizedException, InvalidArgumentException
    {
        if(ipIds != null)
            bem.deleteObject(Constants.CLASS_IP_ADDRESS, ipIds[0], releaseRelationships);
    }
    
    /**
     * Relates an IP address with a generic communication port
     * @param subnetId subnet id
     * @param portClassName Generic communications element
     * @param portId generic communications id
     * @throws BusinessObjectNotFoundException If any of the objects can't be found
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     * @throws MetadataObjectNotFoundException If any of the classes provided can not be found
     * @throws InvalidArgumentException If the subnet/port do not have uuid
     */
    public void relateIPtoPort(String subnetId, String portClassName, String portId) throws BusinessObjectNotFoundException,
            OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        bem.createSpecialRelationship(portClassName, portId, Constants.CLASS_IP_ADDRESS, subnetId, RELATIONSHIP_IPAMHASADDRESS, true);
    }
    
    /**
     * Relate a Subnet with a VLAN, this method also allow to relate VLANs to 
     * BDIs, VFRIs.
     * @param subnetId subnet id
     * @param className if the subnet has IPv4 or IPv6 addresses
     * @param vlanId VLAN id
     * @throws BusinessObjectNotFoundException If any of the objects can't be found
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     * @throws MetadataObjectNotFoundException If any of the classes provided can not be found
     * @throws InvalidArgumentException If the subnet/vlan do not have uuid
     */
    public void relateSubnetToVLAN(String subnetId, String className, String vlanId)
        throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException
    {
        bem.createSpecialRelationship(Constants.CLASS_VLAN, vlanId, className, subnetId, RELATIONSHIP_IPAMBELONGSTOVLAN, true);
    }
    
    /**
     * Release a relationship between a subnet and a VLAN, this method also 
     * allow to relate VLANs to BDIs, VFRIs.
     * @param vlanId the vlan Id
     * @param subnetId the subnet id
     * @throws BusinessObjectNotFoundException If any of the objects can't be found
     * @throws MetadataObjectNotFoundException If any of the classes provided can not be found
     * @throws InvalidArgumentException If the subnet/vlan do not have uuid
     */
    public void releaseSubnetFromVLAN(String subnetId, String vlanId)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException
    {
        bem.releaseSpecialRelationship(Constants.CLASS_VLAN, vlanId, subnetId, RELATIONSHIP_IPAMBELONGSTOVLAN);
    }
    
    /**
     * Relate a Subnet with a VRF
     * @param subnetId subnet id
     * @param className if the subnet has IPv4 or IPv6 addresses
     * @param vrfId VLAN id
     * @throws BusinessObjectNotFoundException If any of the objects can't be found
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     * @throws MetadataObjectNotFoundException If any of the classes provided can not be found
     * @throws InvalidArgumentException If the subnet/vrf do not have uuid
     */
    public void relateSubnetToVRF(String subnetId, String className, String vrfId)
        throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException
    {
        bem.createSpecialRelationship(Constants.CLASS_VRF_INSTANCE, vrfId, className, subnetId, RELATIONSHIP_IPAMBELONGSTOVRFINSTACE, true);
    }
    
    /**
     * Release the relationship between a GenericPort and an 
     * IP Address.
     * @param portClass GenericCommunications Element
     * @param portId GenericCommunications id
     * @param id IP address id 
     * @throws BusinessObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException If the port class can not be found
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the port does not have uuid
     */
    public void releasePortFromIP(String portClass, String portId, String id)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException,
            NotAuthorizedException
    {
        bem.releaseSpecialRelationship(portClass, portId, id, RELATIONSHIP_IPAMHASADDRESS);
        
    }
    
    /**
     * Release a relationship between a subnet and a VRF
     * @param subnetId the subnet id
     * @param vrfId the VRF Id
     * @throws BusinessObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException If the port class can not be found
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the subnet/vrf do not have uuid
     */
    public void releaseSubnetFromVRF(String subnetId, String vrfId)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException,
            NotAuthorizedException, InvalidArgumentException
    {
        bem.releaseSpecialRelationship(Constants.CLASS_VRF_INSTANCE, vrfId, subnetId, RELATIONSHIP_IPAMBELONGSTOVRFINSTACE);
    }

    /**
     * Retrieves all the IP address created in a subnet
     * @param id subnet id
     * @param className If the subnet has IPv4 or an IPv6 addresses
     * @return the next free IP address in the subnet 
     * @throws MetadataObjectNotFoundException If can't find IPv4 or IPV6 classes
     * @throws BusinessObjectNotFoundException If the object(subnet) could not be found.
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the subnet does not have uuid
     */
    public List<BusinessObjectLight> getSubnetUsedIps(String id, String className) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException,  
            NotAuthorizedException, InvalidArgumentException
    {
        List<BusinessObjectLight> children = bem.getObjectSpecialChildren(className, id);
        List<BusinessObjectLight> usedIps = new ArrayList<>();
        for (BusinessObjectLight child : children) {
            if(child.getClassName().equals(Constants.CLASS_IP_ADDRESS))
                usedIps.add(child);
        }
        return usedIps;
    }
    
    /**
     * Retrieves all the Subnets created inside a subnet
     * @param id subnet id
     * @param className If the subnet has IPv4 or an IPv6 addresses
     * @return A list of subnets for a given subnet
     * @throws MetadataObjectNotFoundException If can't find IPv4 or IPV6 classes
     * @throws BusinessObjectNotFoundException If the object(subnet) could not be found.
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the subnet does not have uuid
     */
    public List<BusinessObjectLight> getSubnetsInSubnet(String id, String className) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException,
            NotAuthorizedException, InvalidArgumentException
    {
        List<BusinessObjectLight> children = bem.getObjectSpecialChildren(className, id);
        List<BusinessObjectLight> subnets = new ArrayList<>();
        for (BusinessObjectLight child : children) {
            if(child.getClassName().equals(Constants.CLASS_SUBNET_IPV4) || 
                    child.getClassName().equals(Constants.CLASS_SUBNET_IPV6))
            subnets.add(child);
        }
        return subnets;
    }
    
    /**
     * Relates an interface with a GenericCommunicationPort
     * @param portId port id
     * @param portClassName the class name of the configuration you want to relate with
     * @param interfaceClassName the interface class name
     * @param interfaceId interface id
     * @throws BusinessObjectNotFoundException If any of the objects can't be found
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     * @throws MetadataObjectNotFoundException If any of the classes provided can not be found
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the port/interface do not have uuid
     */
    public void relatePortToInterface(String portId, String portClassName, 
            String interfaceClassName, String interfaceId) throws BusinessObjectNotFoundException,
            OperationNotPermittedException, MetadataObjectNotFoundException, NotAuthorizedException, InvalidArgumentException {
        bem.createSpecialRelationship(interfaceClassName, interfaceId, portClassName, portId, RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE, true);
    }
    
    /**
     * Release the relationship between a GenericCommunicationPort and an interface
     * @param interfaceClassName interface's class
     * @param interfaceId interface id
     * @param portId port id 
     * @throws BusinessObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException If the port class can not be found
     * @throws NotAuthorizedException If the user is not authorized to use the IPAM module
     * @throws InvalidArgumentException If the interface/port do not have uuid
     */
    public void releasePortFromInterface(String interfaceClassName, String interfaceId, String portId)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException,
            NotAuthorizedException, InvalidArgumentException
    {
        bem.releaseSpecialRelationship(interfaceClassName, interfaceId, portId, RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE);
    }
   
    /**
     * Checks if the new subnet overlaps with in the created subnets
     * @param networkIp
     * @param broadcastIp
     * @return true if overlaps, false of not
     */
    public boolean itOverlaps(String networkIp, String broadcastIp){
        return false;
    }
}
