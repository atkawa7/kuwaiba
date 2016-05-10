/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.modules.sdh;

import com.neotropic.kuwaiba.modules.GenericCommercialModule;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.telecom.BusinessEntityManager;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;

/**
 * This class implements the functionality corresponding to the SDH module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Stateless
public class SDHModule extends GenericCommercialModule {

    /**
     * The ApplicationEntityManager instance
     */
    private ApplicationEntityManager aem;
    /**
     * The MetadataEntityManager instance
     */
    private MetadataEntityManager mem;
    /**
     * The BusinessEntityManager instance
     */
    private BusinessEntityManager bem;
    
    @Override
    public String getName() {
        return "SDH Module Demo"; //NOI18N
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
        this.aem  = aem;
        this.mem = mem;
        this.bem = bem;
    }
    
    //The actual methods
    /**
     * Creates an SDH transport link (STMX)
     * @param classNameEndpointA The class name of the endpoint A (some kind of port)
     * @param idEndpointA Id of endpoint A
     * @param classNameEndpointB  The class name of the endpoint Z (some kind of port)
     * @param idEndpointB Id of endpoint Z
     * @param linkType Type of link (STM1, STM4, STM16, STM256, etc)
     * @param defaultName The default name of the element
     * @return The id of the newly created transport link
     * @throws ServerSideException In case something goes wrong with the creation process
     */
    public long createSDHTransportLink(String classNameEndpointA, long idEndpointA, 
            String classNameEndpointB, long idEndpointB, String linkType, String defaultName) throws ServerSideException {

        if (bem == null || aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        
        
        long newConnectionId = -1;
        try {
            if (!mem.isSubClass("GenericTransportLink", linkType))
                throw new ServerSideException(Level.SEVERE, "Class %s is not subclass of GenericTransportLink");

            HashMap<String, List<String>> attributesToBeSet = new HashMap<>();
            if (defaultName != null)
                attributesToBeSet.put(Constants.PROPERTY_NAME, Arrays.asList(new String[] {defaultName}));
            
            newConnectionId = bem.createSpecialObject(linkType, null, -1, attributesToBeSet, 0);
            
            bem.createSpecialRelationship(linkType, newConnectionId, classNameEndpointA, idEndpointA, "sdhTLendpointA");
            bem.createSpecialRelationship(linkType, newConnectionId, classNameEndpointB, idEndpointB, "sdhTLendpointB");
            
            return newConnectionId;
        } catch (Exception e) {
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newConnectionId != -1) {
                try {
                    bem.deleteObject(linkType, newConnectionId, true);
                } catch (Exception ex) {
                    Logger.getLogger(SDHModule.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }

            throw new ServerSideException(Level.SEVERE, e.getMessage());
        }
    }
    
    /**
     * Creates an SDH container link (VCX)
     * @param classNameEndpointA The class name of the endpoint A (some kind of port)
     * @param idEndpointA Id of endpoint A
     * @param classNameEndpointZ  The class name of the endpoint Z (some kind of port)
     * @param idEndpointZ Id of endpoint Z
     * @param linkType Type of link (VC4, VC3, V12, etc. A VC12 alone doesn't make much sense, though)
     * @param positions This param specifies the transport links and positions used by the container. For more details on how this works, please read the "SDH Model: Technical Design and Tools" document. Please note that is greatly advisable to provide them already sorted
     * @return The id of the newly created container link
     * @throws ServerSideException In case something goes wrong with the creation process
     */
    public long createSDHContainerLink(String classNameEndpointA, long idEndpointA, 
            String classNameEndpointZ, long idEndpointZ, String linkType, List<SDHContainerPosition> positions) throws ServerSideException {
    return -1;
    }
    
    /**
     * Creates an SDH tributary link (VCXTriburatyLink)
     * @param classNameEndpointA The class name of the endpoint A (some kind of tributary port)
     * @param idEndpointA Id of endpoint A
     * @param classNameEndpointZ  The class name of the endpoint Z (some kind of tributary port)
     * @param idEndpointZ Id of endpoint Z
     * @param linkType Type of link (VC4TributaryLink, VC3TributaryLink, V12TributaryLink, etc)
     * @param positions This param specifies the transport links and positions used by the container. For more details on how this works, please read the SDH Model: Technical Design and Tools document. Please note that is greatly advisable to provide them already sorted. Please note that creating a tributary link automatically creates a container link to deliver it
     * @return The id of the newly created tributary link
     * @throws ServerSideException In case something goes wrong with the creation process
     */
    public long createSDHTributaryLink(String classNameEndpointA, long idEndpointA, 
            String classNameEndpointZ, long idEndpointZ, String linkType, List<SDHContainerPosition> positions) throws ServerSideException {
    return -1;
    }
    
    /**
     * Finds a route between two GenericcommunicationsEquipment based on the TransportLinks network map (for more details on how this works, please read the SDH Model: Technical Design and Tools document)
     * @param communicationsEquipmentClassA The class of one of the route endpoints
     * @param communicationsEquipmentIdA The id of one of the route endpoints
     * @param communicationsEquipmentClassZ The class of the other route endpoint
     * @param communicationsEquipmentIZ The id of the other route endpoint
     * @return A sorted list of RemoteObjectLights containing the route. This list includes the transport links and the nodes in between, including the very endpoints
     * 
     */
    public List<RemoteObjectLight> findRouteUsingTransportLinks(String communicationsEquipmentClassA, 
                                            long  communicationsEquipmentIdA, String communicationsEquipmentClassZ, 
                                            long  communicationsEquipmentIZ) {
    return null;
    }
    
    /**
     * Finds a route between two GenericcommunicationsEquipment based on the ContainerLinks network map (for more details on how this works, please read the SDH Model: Technical Design and Tools document)
     * @param communicationsEquipmentClassA The class of one of the route endpoints
     * @param communicationsEquipmentIdA The id of one of the route endpoints
     * @param communicationsEquipmentClassZ The class of the other route endpoint
     * @param communicationsEquipmentIZ The id of the other route endpoint
     * @return A sorted list of RemoteObjectLights containing the route. This list includes the transport links and the nodes in between, including the very endpoints
     * 
     */
    public List<RemoteObjectLight> findRouteUsingContainerLinks(String communicationsEquipmentClassA, 
                                            long  communicationsEquipmentIdA, String communicationsEquipmentClassZ, 
                                            long  communicationsEquipmentIZ) {
    return null;
    }
    
    public List<SDHContainerDefinition> getContainersInTransportLink(String transportLinkClass, long transportLinkId){
        return null;
    }
    
    /**
     * Helper classes
     */
    /**
     * Instances of this class define a container
     */
    public class SDHContainerDefinition implements Serializable {
        /**
         * Container's name
         */
        private String containerName;
        
        /**
         * Says if the current container delivers a tributary link or it's the kind of link used to transport other links
         */
        private boolean deliversLink;
        /**
         * The positions used by the container
         */
        private List<SDHContainerPosition> positions;

        public SDHContainerDefinition(String containerName, boolean deliversLink, List<SDHContainerPosition> positions) {
            this.containerName = containerName;
            this.deliversLink = deliversLink;
            this.positions = positions;
        }

        public String getContainerName() {
            return containerName;
        }

        public void setContainerName(String containerName) {
            this.containerName = containerName;
        }

        public boolean isDeliversLink() {
            return deliversLink;
        }

        public void setDeliversLink(boolean deliversLink) {
            this.deliversLink = deliversLink;
        }

        public List<SDHContainerPosition> getPositions() {
            return positions;
        }

        public void setPositions(List<SDHContainerPosition> positions) {
            this.positions = positions;
        }
    }
    
    /**
     * It's a simple class representing a single position used by a container within a transport link
     */
    public class SDHContainerPosition implements Serializable {
        /**
         * Link id
         */
        private long linkId;
        /**
         * Link class, which could be either a transport link or a container link
         */
        private String linkClass;
        /**
         * Position used by container link within the transport link
         */
        private int position;

        public SDHContainerPosition(long linkId, String linkClass, int position) {
            this.linkId = linkId;
            this.linkClass = linkClass;
            this.position = position;
        }

        public long getLinkId() {
            return linkId;
        }

        public void setLinkId(long linkId) {
            this.linkId = linkId;
        }

        public String getLinkClass() {
            return linkClass;
        }

        public void setLinkClass(String linkClass) {
            this.linkClass = linkClass;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }
    
}
