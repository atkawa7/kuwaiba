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
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.WrongMappingException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;

/**
 *
 * @author adrian
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
    
    //constants
    private static final int IPV4 = 4;
    private static final int IPV6 = 6;
    
    /**
     * Class subnet
     */
    private static final String CLASS_SUBNET = "Subnet";
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
    
    @Override
    public String getName() {
        return "IPAM Module"; //NOI18N
    }

    @Override
    public String getDescription() {
        return "IP address manager module";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getVendor() {
        return "neotropic";
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
    }
    
    public List<RemoteBusinessObjectLight> getDefaultIPAMRootNodes() throws NotAuthorizedException, ObjectNotFoundException{
        return null;
    } 
    
    /**
     * Creates a pool of subnets if the parentId is -1 the pool will be created 
     * in the default root for pools of subnets
     * @param parentId
     * @param subnetPoolName subnet pool name
     * @param subnetPoolDescription subnet pool description
     * @param type 4 if is a IPv4 subnet or 6 if is a IPv6 subnet
     * @return 
     * @throws ServerSideException
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     * @throws NotAuthorizedException 
     * @throws org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException 
     * @throws org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException 
     * @throws org.kuwaiba.apis.persistence.exceptions.WrongMappingException 
     */
    public long createSubnetsPool(long parentId, String subnetPoolName, 
            String subnetPoolDescription, int type) throws ServerSideException, 
            MetadataObjectNotFoundException, InvalidArgumentException, 
            ObjectNotFoundException, NotAuthorizedException, ApplicationObjectNotFoundException, 
            OperationNotPermittedException, WrongMappingException
    {
        if (aem == null)
           throw new ServerSideException("Can't reach the backend. Contact your administrator");
        return aem.createPool(parentId, subnetPoolName, subnetPoolDescription, Constants.CLASS_SUBNET, type);
    }
    
    public RemoteBusinessObject getSubnet(long oid) throws MetadataObjectNotFoundException, 
            ObjectNotFoundException, InvalidArgumentException, 
            ApplicationObjectNotFoundException, NotAuthorizedException
    {
        return bem.getObject(Constants.CLASS_SUBNET, oid);
    }
    
    public RemoteBusinessObject getSubnetPool(long oid) throws MetadataObjectNotFoundException, 
            ObjectNotFoundException, InvalidArgumentException, 
            ApplicationObjectNotFoundException, NotAuthorizedException
    {
        return aem.getPool(Constants.CLASS_SUBNET, oid);
    }
    
    public List<RemoteBusinessObjectLight> getSubnetPools(int limit, 
            long parentId) throws NotAuthorizedException, ObjectNotFoundException
    {
        return aem.getPools(limit, parentId, Constants.CLASS_SUBNET);
    }
   
    public List<RemoteBusinessObjectLight> getSubnets(int limit, 
            long subnetPoolId) throws ApplicationObjectNotFoundException, 
            NotAuthorizedException
    {
        return aem.getPoolItems(subnetPoolId, limit);
    }
    
    public long createSubnet(long parentId, String[] attributeNames, 
            String[][] attributeValues) throws ApplicationObjectNotFoundException, 
            InvalidArgumentException, ArraySizeMismatchException, NotAuthorizedException, 
            MetadataObjectNotFoundException
    {
        return bem.createPoolItem(parentId, Constants.CLASS_SUBNET, attributeNames, attributeValues, 0);
    }
    private void updateSubnet(long subnetId, String[] attributeNames, 
            String[][] attributeValues){
    }
    public void deleteSubnets(long[] ids, boolean releaseRelationships) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, 
            OperationNotPermittedException, NotAuthorizedException
    {
        if(ids != null)
            bem.deleteObject(Constants.CLASS_SUBNET, ids[0], releaseRelationships);
    }
    
    public void deleteSubnetPools(long[] subnetsId) 
            throws InvalidArgumentException, OperationNotPermittedException, 
            NotAuthorizedException
    {
        aem.deletePools(subnetsId);
    }
    
    private void relateIP(String IP, long deviceId){
        
    }
    private void releaseIP(long deviceId){
    }
}
