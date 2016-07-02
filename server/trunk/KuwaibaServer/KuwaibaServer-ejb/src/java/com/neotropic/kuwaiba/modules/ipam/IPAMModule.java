/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.modules.ipam;

import com.neotropic.kuwaiba.modules.GenericCommercialModule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.Pool;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.ws.toserialize.application.RemotePool;

/**
 * IP address manager module
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class IPAMModule implements GenericCommercialModule{

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
    
    /**
     * The subnets could belong grouped in order to improve the IP Address
     * management, by default if no pool this set for a IPv4 subnet it belong 
     * to this pool
     */
    private static final String DEFAULT_IPV4_POOL = "ipv4 Pool";
    /**
     * The subnets could belong grouped in order to improve the IP Address
     * management, by default if no pool this set for a IPv6 subnet it belong 
     * to this pool
     */
    private static final String DEFAULT_IPV6_POOL = "ipv6 Pool";
    /**
     * This relationship is used to connect a GenericCommunicationElement with
     * a subnet's IP address 
     */
    public static final String RELATIONSHIP_IPAMHASADDRESS = "ipamHasIpAddress";
    
    /**
     * This relationship is used to connect a VLAN with a Subnet
     */
    public static final String RELATIONSHIP_IPAMBELONGSTOVLAN = "ipamBelongsToVlan";
    
    @Override
    public String getName() {
        return "IPAM Module"; //NOI18N
    }

    @Override
    public String getDescription() {
        return "IP Address Management Module";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS";
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
        this.aem = aem;
        this.mem = mem;
        this.bem = bem;
        
        //Registers the display names
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_IPAMBELONGSTOVLAN, "IPAM Subnet belong to a VLAN");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_IPAMHASADDRESS, "IPAM GenericCommunicationElement has an IP Address");

    }
    
    /**
     * Get the default pool nodes for IPv4 and IPv6 subnets
     * @return default pool for IPv4 and IPv6
     * @throws NotAuthorizedException 
     */
    private List<RemotePool> getDefaultIPAMRootNodes() throws NotAuthorizedException, MetadataObjectNotFoundException{
        List<Pool> ipv4RootPools = aem.getRootPools(Constants.CLASS_SUBNET_IPV4, 2);
        List<Pool> ipv6RootPools = aem.getRootPools(Constants.CLASS_SUBNET_IPV6, 2);
        
        List<RemotePool> rootSubnetPools = new ArrayList<>();
        if(ipv4RootPools.isEmpty() || ipv6RootPools.isEmpty())
            createRootNodes();
        
        for (Pool rootPool : ipv4RootPools) 
            rootSubnetPools.add(new RemotePool(rootPool));
        
        for (Pool rootPool : ipv6RootPools) 
            rootSubnetPools.add(new RemotePool(rootPool));
        
        return rootSubnetPools;
    }
    
    /**
     * Create the IPv4 and IPv6 default nodes if they don't exists.
     * @throws MetadataObjectNotFoundException
     * @throws NotAuthorizedException 
     */
    private void createRootNodes() throws MetadataObjectNotFoundException, NotAuthorizedException{
        aem.createRootPool(Constants.NODE_IPV6ROOT, Constants.NODE_IPV6ROOT, Constants.CLASS_SUBNET_IPV6, 2);
        aem.createRootPool(Constants.NODE_IPV4ROOT, Constants.NODE_IPV4ROOT, Constants.CLASS_SUBNET_IPV4, 2);
        getDefaultIPAMRootNodes();
    }
    
    /**
     * Creates a pool of subnets if the parentId is -1 the pool will be created 
     * in the default root for pools of subnets
     * @param parentId
     * @param subnetPoolName subnet pool name
     * @param subnetPoolDescription subnet pool description
     * @param className if is a IPv4 subnet or 6 if is a IPv6 subnet
     * @return new subnet pool id
     * @throws ServerSideException
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     * @throws NotAuthorizedException 
     * @throws org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException 
     */
    public long createSubnetsPool(long parentId, String subnetPoolName, 
            String subnetPoolDescription, String className) throws ServerSideException, 
            MetadataObjectNotFoundException, InvalidArgumentException, ObjectNotFoundException, 
            NotAuthorizedException, ApplicationObjectNotFoundException 
    {
        if (aem == null)
           throw new ServerSideException("Can't reach the backend. Contact your administrator");
        return aem.createPoolInPool(parentId, subnetPoolName, subnetPoolDescription, className, 2);
    }
    
    /**
     * Get a subnet
     * @param className if is a IPv4 subnet or if is a IPv6 subnet
     * @param oid subnet id
     * @return the subnet
     * @throws MetadataObjectNotFoundException
     * @throws ObjectNotFoundException
     * @throws InvalidArgumentException
     * @throws ApplicationObjectNotFoundException
     * @throws NotAuthorizedException 
     */
    public RemoteBusinessObject getSubnet(String className, long oid) throws MetadataObjectNotFoundException, 
            ObjectNotFoundException, InvalidArgumentException, 
            ApplicationObjectNotFoundException, NotAuthorizedException
    {
        return bem.getObject(className, oid);
    }
    
    /**
     * Get a subnet pool
     * @param oid subnet id
     * @return a subnet pool
     * @throws MetadataObjectNotFoundException
     * @throws ObjectNotFoundException
     * @throws InvalidArgumentException
     * @throws ApplicationObjectNotFoundException
     * @throws NotAuthorizedException 
     */
    public RemotePool getSubnetPool(long oid) throws MetadataObjectNotFoundException, 
            ObjectNotFoundException, InvalidArgumentException, 
            ApplicationObjectNotFoundException, NotAuthorizedException
    {
        return new RemotePool(aem.getPool(oid));
    }
    
    /**
     * Get a set of subnet pools from a pool of subnets or from the root
     * @param limit limit of the result set, -1 no limit
     * @param parentId parent id
     * @param className if is a IPv4 subnet or if is a IPv6 subnet
     * @return a list of subnet pools
     * @throws NotAuthorizedException
     * @throws ObjectNotFoundException 
     * @throws ApplicationObjectNotFoundException 
     * @throws MetadataObjectNotFoundException 
     */
    public List<RemotePool> getSubnetPools(int limit, 
            long parentId, String className) throws NotAuthorizedException, ObjectNotFoundException, 
            ApplicationObjectNotFoundException, MetadataObjectNotFoundException
    {
        List<RemotePool> remotePools = new ArrayList<>();
        if(parentId == -1 && className == null)
            return getDefaultIPAMRootNodes();
        
        for (Pool pool : aem.getPoolsInPool(parentId, className)) 
            remotePools.add(new RemotePool(pool));
        return remotePools;
    }
   
    /**
     * Get a set of subnets from a pool of subnets
     * @param limit limit of results, no limit -1
     * @param subnetPoolId subnet pool id
     * @return a list of subnets
     * @throws ApplicationObjectNotFoundException
     * @throws NotAuthorizedException 
     */
    public List<RemoteBusinessObjectLight> getSubnets(int limit, 
            long subnetPoolId) throws ApplicationObjectNotFoundException, 
            NotAuthorizedException
    {
        return aem.getPoolItems(subnetPoolId, limit);
    }
    
    /**
     * create a subnet
     * @param parentId subnet pool id
     * @param attributeNames subnet attributes, networkIP, broadcastIP, hosts
     * @param attributeValues subnet attribute values
     * @return new subnet id
     * @throws ApplicationObjectNotFoundException
     * @throws InvalidArgumentException
     * @throws ArraySizeMismatchException
     * @throws NotAuthorizedException
     * @throws MetadataObjectNotFoundException 
     */
    public long createSubnet(long parentId, String className, String[] attributeNames, 
            String[][] attributeValues) throws ApplicationObjectNotFoundException, 
            InvalidArgumentException, ArraySizeMismatchException, NotAuthorizedException, 
            MetadataObjectNotFoundException
    {
        return bem.createPoolItem(parentId, className, attributeNames, attributeValues, 0);
    }
    
    
    /**
     * deletes a subnet
     * @param ids subnet ids
     * @param releaseRelationships release any relationship 
     * @throws ObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws NotAuthorizedException 
     */
    public void deleteSubnets(long[] ids, String className, boolean releaseRelationships) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, 
            OperationNotPermittedException, NotAuthorizedException
    {
        if(ids != null)
            bem.deleteObject(className, ids[0], releaseRelationships);
    }
    
    /**
     * deletes a subnet Pool
     * @param subnetsId subnet ids 
     * @throws InvalidArgumentException
     * @throws OperationNotPermittedException
     * @throws NotAuthorizedException 
     * @throws org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException 
     */
    public void deleteSubnetPools(long[] subnetsId) 
            throws InvalidArgumentException, OperationNotPermittedException, 
            NotAuthorizedException, ApplicationObjectNotFoundException
    {
        aem.deletePools(subnetsId);
    }

    /**
     * creates an IP address inside a subnet
     * @param parentId subnet Id
     * @param parentClassName if is a IPv4 or an IPv6 subnet
     * @param attributes ip Address attributes, name description
     * @return IP address id
     * @throws ApplicationObjectNotFoundException
     * @throws InvalidArgumentException
     * @throws ArraySizeMismatchException
     * @throws NotAuthorizedException
     * @throws MetadataObjectNotFoundException
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws DatabaseException 
     */
    public long addIP(long parentId, String parentClassName, HashMap<String,List<String>> attributes) throws ApplicationObjectNotFoundException, 
            InvalidArgumentException, ArraySizeMismatchException, NotAuthorizedException, 
            MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, DatabaseException
    {
        return bem.createObject(Constants.CLASS_IP_ADDRESS, parentClassName, parentId, attributes, 0);
    }

    /**
     * Removes an ip address from a subnet
     * @param ids IP addresses ids
     * @param releaseRelationships release existing relationships
     * @throws ObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws NotAuthorizedException 
     */
    public void removeIP(long[] ids, boolean releaseRelationships) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, 
            OperationNotPermittedException, NotAuthorizedException
    {
        if(ids != null)
            bem.deleteObject(Constants.CLASS_IP_ADDRESS, ids[0], releaseRelationships);
    }
    
    /**
     * Relates a subnet with a generic communications element
     * @param id subnet id
     * @param deviceClass Generic communications element
     * @param deviceId generic communications id
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws MetadataObjectNotFoundException 
     */
    public void relateIPtoDevice(long id, String deviceClass, long deviceId) throws ObjectNotFoundException,
            OperationNotPermittedException, MetadataObjectNotFoundException{
        bem.createSpecialRelationship(deviceClass, deviceId, Constants.CLASS_IP_ADDRESS, id, RELATIONSHIP_IPAMHASADDRESS, true);
    }
    
    /**
     * Relate a Subnet with a VLAN
     * @param id subnet id
     * @param className if the subnet has IPv4 or IPv6 addresses
     * @param vlanId VLAN id
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws MetadataObjectNotFoundException 
     */
    public void relateSubnetToVLAN(long id, String className, long vlanId)
        throws ObjectNotFoundException,
            OperationNotPermittedException, MetadataObjectNotFoundException{
        bem.createSpecialRelationship(Constants.CLASS_VLAN, vlanId, className, id, RELATIONSHIP_IPAMBELONGSTOVLAN, true);
    }
    
    /**
     * Release the relationship between a GenericCommunicationElement and an 
     * IP Address.
     * @param deviceClass GenericCommunications Element
     * @param deviceId GenericCommunications id
     * @param id IP address id 
     * @throws ObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws ApplicationObjectNotFoundException
     * @throws NotAuthorizedException 
     */
    public void releaseIPfromDevice(String deviceClass, long deviceId, long id)
            throws ObjectNotFoundException, MetadataObjectNotFoundException,
            ApplicationObjectNotFoundException, NotAuthorizedException
    {
        bem.releaseSpecialRelationship(deviceClass, deviceId, id, RELATIONSHIP_IPAMHASADDRESS);
        
    }
    
    /**
     * Release a relationship between a subnet and a VLAN
     * @param vlanId the vlan Id
     * @param id the subnet id
     * @throws ObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws ApplicationObjectNotFoundException
     * @throws NotAuthorizedException 
     */
    public void releaseSubnetFromVLAN(long vlanId, long id)throws ObjectNotFoundException, MetadataObjectNotFoundException,
            ApplicationObjectNotFoundException, NotAuthorizedException
    {
        bem.releaseSpecialRelationship(Constants.CLASS_VLAN, vlanId, id, RELATIONSHIP_IPAMBELONGSTOVLAN);
    }
    
    /**
     * Retrieves all the IP address created in a subnet
     * @param id subnet id
     * @param className If the subnet has IPv4 or an IPv6 addresses
     * @return the next free IP address in the subnet 
     * @throws MetadataObjectNotFoundException 
     * @throws ObjectNotFoundException 
     * @throws ApplicationObjectNotFoundException 
     * @throws NotAuthorizedException 
     */
    public List<RemoteBusinessObjectLight> getSubnetUsedIps(long id, String className) 
            throws MetadataObjectNotFoundException, 
            ObjectNotFoundException, ApplicationObjectNotFoundException, 
            NotAuthorizedException
    {
        return bem.getObjectChildren(className, id, 0);
    }
    /**
     * checks if the new subnet overlaps with in the created subnets
     * @param networkIp
     * @param broadcastIp
     * @return true if overlaps, false of not
     */
    public boolean itOverlaps(String networkIp, String broadcastIp){
        return false;
    }
}
