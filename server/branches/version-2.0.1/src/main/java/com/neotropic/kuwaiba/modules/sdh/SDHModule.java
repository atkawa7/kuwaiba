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
package com.neotropic.kuwaiba.modules.sdh;

import com.neotropic.kuwaiba.modules.GenericCommercialModule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.AnnotatedBusinessObjectLight;
import org.kuwaiba.apis.persistence.business.BusinessObject;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.business.BusinessObjectLightList;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * This class implements the functionality corresponding to the SDH module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SDHModule implements GenericCommercialModule {

    /**
     * The MetadataEntityManager instance
     */
    private MetadataEntityManager mem;
    /**
     * The BusinessEntityManager instance
     */
    private BusinessEntityManager bem;
    
    //Constants
    /**
     * Root class of all high order tributary links (VC4)
     */
    public static String CLASS_GENERICSDHHIGHORDERTRIBUTARYLINK = "GenericSDHHighOrderTributaryLink";
    /**
     * Root class of all low order tributary links (VC12/VC3)
     */
    public static String CLASS_GENERICSDHLOWORDERTRIBUTARYLINK = "GenericSDHLowOrderTributaryLink";
    /**
     * A side in a transport link
     */
    public static String RELATIONSHIP_SDHTLENDPOINTA = "sdhTLEndpointA";
    /**
     * B side in a transport link
     */
    public static String RELATIONSHIP_SDHTLENDPOINTB = "sdhTLEndpointB";
    /**
     * The relationship used to connect two GenericCommunicationsEquipment to represent that ports within the equipment are connected with Transport Links. This is used to ease the way to find routes between elements
     */
    public static String RELATIONSHIP_SDHTRANSPORTLINK = "sdhTransportLink";
    /**
     * The relationship used to connect two GenericCommunicationsEquipment to represent that ports within the equipment are connected with Container Links. This is used to ease the way to find routes between elements
     */
    public static String RELATIONSHIP_SDHCONTAINERLINK = "sdhContainerLink";
    /**
     * A side in a tributary link
     */
    public static String RELATIONSHIP_SDHTTLENDPOINTA = "sdhTTLEndpointA";
    /**
     * B side in a tributary link
     */
    public static String RELATIONSHIP_SDHTTLENDPOINTB = "sdhTTLEndpointB";
    /**
     * This relationship describes how a Transport Link carries a Container Link
     */
    public static String RELATIONSHIP_SDHTRANSPORTS = "sdhTransports";
    /**
     * This relationship describes how a Container Link carries another Container link of a lower order
     */
    public static String RELATIONSHIP_SDHCONTAINS = "sdhContains";
    /**
     * This relationship describes how a Container Link carries a Tributary Link
     */
    public static String RELATIONSHIP_SDHDELIVERS = "sdhDelivers";
    /**
     * The timeslot used by a container in a transport link or in another container
     */
    public static String PROPERTY_SDHPOSITION = "sdhPosition";
    
    @Override
    public String getName() {
        return "SDH Networks Module"; //NOI18N
    }

    @Override
    public String getDescription() {
        return "Simple SDH module that allows the creation of STMX links, high and low order virtual circuits and finding routes between multiplexers";
    }

    @Override
    public String getVersion() {
        return "1.1";
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
        
        //Registers the display names
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHTLENDPOINTA, "SDH Transport Link A Side");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHTLENDPOINTB, "SDH Transport Link B Side");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHTRANSPORTLINK, "SDH Transport Link Connecting To");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHCONTAINERLINK, "SDH Container Link Connecting To");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHTTLENDPOINTA, "SDH Tributary Link A Side");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHTTLENDPOINTB, "SDH Tributary Link B Side");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHTRANSPORTS, "Transported SDH Container Links");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHCONTAINS, "Contained SDH Container Links");
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_SDHDELIVERS, "Delivered SDH Tributary Links");
    }
    
    //The actual methods
    /**
     * Creates an SDH transport link (STMX)
     * @param classNameEndpointA The class name of the endpoint A (some kind of port)
     * @param idEndpointA Id of endpoint A
     * @param classNameEndpointB  The class name of the endpoint B (some kind of port)
     * @param idEndpointB Id of endpoint B
     * @param linkType Type of link (STM1, STM4, STM16, STM256, etc)
     * @param defaultName The default name of the element
     * @return The id of the newly created transport link
     * @throws ServerSideException In case something goes wrong with the creation process
     */
    public String createSDHTransportLink(String classNameEndpointA, String idEndpointA, 
            String classNameEndpointB, String idEndpointB, String linkType, String defaultName) throws ServerSideException {

        if (bem == null || mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        
        String newConnectionId = null;
        try {
            if (!mem.isSubclassOf("GenericSDHTransportLink", linkType)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not subclass of GenericSDHTransportLink", linkType));

            HashMap<String, String> attributesToBeSet = new HashMap<>();
            attributesToBeSet.put(Constants.PROPERTY_NAME, defaultName == null ? "" : defaultName );
            
            BusinessObject communicationsEquipmentA = bem.getParentOfClass(classNameEndpointA, idEndpointA, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
            if (communicationsEquipmentA == null)
                throw new ServerSideException(String.format("The specified port (%s : %s) doesn't seem to be located in a communications equipment", classNameEndpointA, idEndpointA));
            
            BusinessObject communicationsEquipmentB = bem.getParentOfClass(classNameEndpointB, idEndpointB, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
            if (communicationsEquipmentB == null)
                throw new ServerSideException(String.format("The specified port (%s : %s) doesn't seem to be located in a communications equipment", classNameEndpointB, idEndpointB));
            
            newConnectionId = bem.createSpecialObject(linkType, null, "-1", attributesToBeSet, null);                      
                       
            bem.createSpecialRelationship(linkType, newConnectionId, classNameEndpointA, idEndpointA, RELATIONSHIP_SDHTLENDPOINTA, true);
            bem.createSpecialRelationship(linkType, newConnectionId, classNameEndpointB, idEndpointB, RELATIONSHIP_SDHTLENDPOINTB, true);
            
            //We add a relationship between the shelves and the Transport LInks so we can easily find a route between two equipment when creatin low order connections
            //based on TransportLinks paths            
            bem.createSpecialRelationship(communicationsEquipmentA.getClassName(), communicationsEquipmentA.getId(), 
                    linkType, newConnectionId, RELATIONSHIP_SDHTRANSPORTLINK, false);
            
            bem.createSpecialRelationship(linkType, newConnectionId, communicationsEquipmentB.getClassName(), 
                    communicationsEquipmentB.getId(), RELATIONSHIP_SDHTRANSPORTLINK, false);
            
            return newConnectionId;
        } catch (Exception e) {
            //TODO: This should be replace with a transaction that lasts as long as everything in this method has been done, instead of
            //doing commits in every call to the BEM
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newConnectionId != null) {
                try {
                    bem.deleteObject(linkType, newConnectionId, true);
                } catch (Exception ex) {
                    Logger.getLogger(SDHModule.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }

            throw new ServerSideException(e.getMessage());
        }
    }
    
    /**
     * Creates an SDH container link (VCX). In practical terms, it's always a high order container, such a VC4XXX
     * @param classNameEndpointA The class name of the endpoint A (a GenericCommunicationsEquipment)
     * @param idEndpointA Id of endpoint A
     * @param classNameEndpointB  The class name of the endpoint B (GenericCommunicationsEquipment)
     * @param idEndpointB Id of endpoint B
     * @param linkType Type of link (VC4, VC3, V12, etc. A VC12 alone doesn't make much sense, though)
     * @param positions This param specifies the transport links and positions used by the container. For more details on how this works, please read the "SDH Model: Technical Design and Tools" document. Please note that is greatly advisable to provide them already sorted
     * @param defaultName the name to be assigned to the new element. If null, an empty string will be used
     * @return The id of the newly created container link
     * @throws ServerSideException In case something goes wrong with the creation process
     */
    public String createSDHContainerLink(String classNameEndpointA, String idEndpointA, 
            String classNameEndpointB, String idEndpointB, String linkType, List<SDHPosition> positions, String defaultName) throws ServerSideException {
        if (bem == null || mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        
        String newConnectionId = null;
        try {
            if (!mem.isSubclassOf("GenericSDHContainerLink", linkType)) //NOI18N
                throw new ServerSideException("Class %s is not subclass of GenericSDHContainerLink");

            if (!mem.isSubclassOf("GenericCommunicationsElement", classNameEndpointA) || !mem.isSubclassOf("GenericCommunicationsElement", classNameEndpointB))
                throw new ServerSideException("The endpoints must be subclasses of GenericCommunicationsElement");
                
            HashMap<String, String> attributesToBeSet = new HashMap<>();
            attributesToBeSet.put(Constants.PROPERTY_NAME, defaultName == null ? "" : defaultName );
            
            
            newConnectionId = bem.createSpecialObject(linkType, null, "-1", attributesToBeSet, null);                      
            
            //We add a relationship between the shelves so we can easily find a route between two equipment when creatin low order connections
            //based on ContainerLink paths           
            bem.createSpecialRelationship(classNameEndpointA, idEndpointA, 
                    linkType, newConnectionId, RELATIONSHIP_SDHCONTAINERLINK, false);
            
            bem.createSpecialRelationship(linkType, newConnectionId, classNameEndpointB, 
                    idEndpointB, RELATIONSHIP_SDHCONTAINERLINK, false);
            
            for (SDHPosition position : positions) {
                HashMap<String, Object> positionAsAproperty = new HashMap<>();
                positionAsAproperty.put("sdhPosition", position.getPosition());                
                bem.createSpecialRelationship(position.getLinkClass(), position.getLinkId(), linkType, 
                        newConnectionId, RELATIONSHIP_SDHTRANSPORTS, false, positionAsAproperty);
            }
            
            return newConnectionId;
        } catch (Exception e) {
            //TODO: This should be replace with a transaction that lasts as long as everything in this method has been done, instead of
            //doing commits in every call to the BEM
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newConnectionId != null) {
                try {
                    bem.deleteObject(linkType, newConnectionId, true);
                } catch (Exception ex) {
                    Logger.getLogger(SDHModule.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }

            throw new ServerSideException(e.getMessage());
        }
    }
    
    /**
     * Creates an SDH tributary link (VCXTributaryLink)
     * @param classNameEndpointA The class name of the endpoint A (some kind of tributary port)
     * @param idEndpointA Id of endpoint A
     * @param classNameEndpointB  The class name of the endpoint B (some kind of tributary port)
     * @param idEndpointB Id of endpoint B
     * @param linkType Type of link (VC4TributaryLink, VC3TributaryLink, V12TributaryLink, etc)
     * @param positions This param specifies the transport links and positions used by the container. For more details on how this works, please read the SDH Model: Technical Design and Tools document. Please note that is greatly advisable to provide them already sorted. Please note that creating a tributary link automatically creates a container link to deliver it
     * @param defaultName the name to be assigned to the new element
     * @return The id of the newly created tributary link
     * @throws ServerSideException In case something goes wrong with the creation process
     */
    public String createSDHTributaryLink(String classNameEndpointA, String idEndpointA, 
            String classNameEndpointB, String idEndpointB, String linkType, List<SDHPosition> positions, String defaultName) throws ServerSideException {
        if (bem == null || mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        
        String newTributaryLinkId = null;
        String newContainerLinkId;
        
        try {
            if (!mem.isSubclassOf("GenericSDHTributaryLink", linkType)) //NOI18N
                throw new ServerSideException("Class %s is not subclass of GenericSDHTributaryLink");

            HashMap<String, String> attributesToBeSet = new HashMap<>();
            attributesToBeSet.put(Constants.PROPERTY_NAME, defaultName == null ? "" : defaultName);
            
            //All tributary links must be delivered using a container link
            String containerLinkType = linkType.replace("TributaryLink", ""); //The name of the corresponding container link is the same as the tributary link without the suffix "TributaryLink"
            newContainerLinkId = bem.createSpecialObject(containerLinkType, null, "-1", attributesToBeSet, null);
            
            //The new tributary link
            newTributaryLinkId = bem.createSpecialObject(linkType, null, "-1", attributesToBeSet, null);
            
            //Relate the new tributary link to the endpoints (ports)
            bem.createSpecialRelationship(linkType, newTributaryLinkId, classNameEndpointA, idEndpointA, RELATIONSHIP_SDHTTLENDPOINTA, true);
            bem.createSpecialRelationship(linkType, newTributaryLinkId, classNameEndpointB, idEndpointB, RELATIONSHIP_SDHTTLENDPOINTB, true);
            
            //Associate the link to the container
            bem.createSpecialRelationship(containerLinkType, newContainerLinkId, linkType, newTributaryLinkId, RELATIONSHIP_SDHDELIVERS, true);
            
            //If the tributary link is a low level circuit (VC12/VC3), its transported by a container link, however, if it's a high level one,
            //it's transported by a transport link. There's a different relationship dipending on the case
            String relationship;
            
            if (mem.isSubclassOf(CLASS_GENERICSDHHIGHORDERTRIBUTARYLINK, linkType))
                relationship = RELATIONSHIP_SDHTRANSPORTS;
            else {
                if (mem.isSubclassOf(CLASS_GENERICSDHLOWORDERTRIBUTARYLINK, linkType))
                    relationship = RELATIONSHIP_SDHCONTAINS;
                else
                    throw new ServerSideException(String.format("Class %s does not appear to be either a high or low order tributary link", linkType));
            }
            
            for (SDHPosition position : positions) {
                HashMap<String, Object> positionAsAproperty = new HashMap<>();
                positionAsAproperty.put("sdhPosition", position.getPosition());
                bem.createSpecialRelationship(position.getLinkClass(), position.getLinkId(), 
                        containerLinkType, newContainerLinkId, relationship, false, positionAsAproperty);
            }
            
            return newTributaryLinkId;
        } catch (Exception e) {
            //TODO: This should be replace with a transaction that lasts as long as everything in this method has been done, instead of
            //doing commits in every call to the BEM
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newTributaryLinkId != null) {
                try {
                    bem.deleteObject(linkType, newTributaryLinkId, true);
                } catch (Exception ex) {
                    Logger.getLogger(SDHModule.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }

            throw new ServerSideException(e.getMessage());
        }
    }
    
    /**
     * Deletes a transport link
     * @param transportLinkClass Transport Link class
     * @param transportLinkId Transport link id
     * @param forceDelete Delete recursively all SDH elements transported by the transport link
     * @throws org.kuwaiba.exceptions.ServerSideException If something goes wrong
     * @throws InventoryException If the transport link could not be found
     */
    public void deleteSDHTransportLink(String transportLinkClass, String transportLinkId, boolean forceDelete) 
            throws ServerSideException, InventoryException {
        if (bem == null || mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        
        if (!mem.isSubclassOf("GenericSDHTransportLink", transportLinkClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not subclass of GenericSDHContainerLink", transportLinkClass));
        
        List<BusinessObjectLight> containerLinks = bem.getSpecialAttribute(transportLinkClass, transportLinkId, RELATIONSHIP_SDHTRANSPORTS);
        
        for (BusinessObjectLight containerLink : containerLinks)
            deleteSDHContainerLink(containerLink.getClassName(), containerLink.getId(), forceDelete);
       
        bem.deleteObject(transportLinkClass, transportLinkId, forceDelete);
        
    }
    /**
     * Deletes a container link. 
     * @param containerLinkClass Container link class
     * @param containerLinkId Container class id
     * @param forceDelete Delete recursively all sdh elements contained by the container link
     * @throws ServerSideException If some high level thing goes wrong
     * @throws InventoryException  If some low level thing goes wrong
     */
    public void deleteSDHContainerLink(String containerLinkClass, String containerLinkId, boolean forceDelete) throws ServerSideException, InventoryException {
        if (bem == null || mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        
        if (!mem.isSubclassOf("GenericSDHContainerLink", containerLinkClass)) //NOI18N
                throw new ServerSideException("Class %s is not subclass of GenericSDHContainerLink");

        //The container could carry a tributary link (easy!) or carry more containers inside, in which case, we need to dig one more level.
        //There's a special case, where the container has no relationships to containers nor to tributary links, those are the empty structured VC4XX
        List<BusinessObjectLight> tributaryLinks = bem.getSpecialAttribute(containerLinkClass, containerLinkId, RELATIONSHIP_SDHDELIVERS);

        if (!tributaryLinks.isEmpty())
            //This will delete both the tributary link and the container
            deleteSDHTributaryLink(tributaryLinks.get(0).getClassName(), tributaryLinks.get(0).getId());
        else {
            List<BusinessObjectLight> containerLinks = bem.getSpecialAttribute(containerLinkClass, containerLinkId, RELATIONSHIP_SDHCONTAINS);
            if (!containerLinks.isEmpty()) {
                for (BusinessObjectLight containerLink : containerLinks)
                    deleteSDHContainerLink(containerLink.getClassName(), containerLink.getId(), forceDelete);
            } else
                bem.deleteObject(containerLinkClass, containerLinkId, forceDelete);
        }
    }
    /**
     * Deletes a tributary link and its corresponding container link. This method will delete all the object relationships
     * @param tributaryLinkClass The class of the tributary link
     * @param tributaryLinkId the id of the tributary link
     * @throws ServerSideException If some high level thing goes wrong
     * @throws InventoryException  If some low level thing goes wrong
     */
    public void deleteSDHTributaryLink(String tributaryLinkClass, String tributaryLinkId) throws ServerSideException, InventoryException {
        if (bem == null || mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        
        
        if (!mem.isSubclassOf("GenericSDHTributaryLink", tributaryLinkClass)) //NOI18N
                throw new ServerSideException("Class %s is not subclass of GenericSDHTributaryLink");

        //There should be only one
        List<BusinessObjectLight> containers = bem.getSpecialAttribute(tributaryLinkClass, tributaryLinkId, RELATIONSHIP_SDHDELIVERS);

        //A tributary link has always a container assigned that should be removed as well
        for (BusinessObjectLight container : containers)
            bem.deleteObject(container.getClassName(), container.getId(), true);

        bem.deleteObject(tributaryLinkClass, tributaryLinkId, true);
    }
    
    /**
     * Finds a route between two GenericCommunicationsEquipment based on the TransportLinks network map (for more details on how this works, please read the SDH Model: Technical Design and Tools document)
     * @param communicationsEquipmentClassA The class of one of the route endpoints
     * @param communicationsEquipmentIdA The id of one of the route endpoints
     * @param communicationsEquipmentClassB The class of the other route endpoint
     * @param communicationsEquipmentIB The id of the other route endpoint
     * @return A sorted list of RemoteObjectLights containing the route. This list includes the transport links and the nodes in between, including the very endpoints
     * @throws MetadataObjectNotFoundException If the core classes that support the SDH networks model don't exist
     * @throws InvalidArgumentException If the given communication equipment is no subclass of GenericCommunicationsEquipment 
     */
    public List<BusinessObjectLightList> findSDHRoutesUsingTransportLinks(String communicationsEquipmentClassA, 
                                            String communicationsEquipmentIdA, String communicationsEquipmentClassB, 
                                            String communicationsEquipmentIB) throws InvalidArgumentException, MetadataObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, communicationsEquipmentClassA))
                throw new InvalidArgumentException(String.format("Class %s is not a GenericCommunicationsEquipment", communicationsEquipmentClassA));
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, communicationsEquipmentClassB))
                throw new InvalidArgumentException(String.format("Class %s is not a GenericCommunicationsEquipment", communicationsEquipmentClassB));
        
        return bem.findRoutesThroughSpecialRelationships(communicationsEquipmentClassA, communicationsEquipmentIdA, communicationsEquipmentClassB, 
                        communicationsEquipmentIB, RELATIONSHIP_SDHTRANSPORTLINK);
    }
    
    /**
     * Finds a route between two GenericcommunicationsEquipment based on the ContainerLinks network map (for more details on how this works, please read the SDH Model: Technical Design and Tools document)
     * @param communicationsEquipmentClassA The class of one of the route endpoints
     * @param communicationsEquipmentIdA The id of one of the route endpoints
     * @param communicationsEquipmentClassB The class of the other route endpoint
     * @param communicationsEquipmentIB The id of the other route endpoint
     * @return A sorted list of RemoteObjectLights containing the route. This list includes the transport links and the nodes in between, including the very endpoints
     * @throws InvalidArgumentException If the given communication equipment is no subclass of GenericCommunicationsEquipment 
     * @throws MetadataObjectNotFoundException If the core classes that support the SDH networks model don't exist
     */
    public List<BusinessObjectLightList> findSDHRoutesUsingContainerLinks(String communicationsEquipmentClassA, 
                                            String communicationsEquipmentIdA, String communicationsEquipmentClassB, 
                                            String communicationsEquipmentIB) throws InvalidArgumentException, MetadataObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, communicationsEquipmentClassA))
                throw new InvalidArgumentException(String.format("Class %s is not a GenericCommunicationsEquipment", communicationsEquipmentClassA));
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, communicationsEquipmentClassB))
                throw new InvalidArgumentException(String.format("Class %s is not a GenericCommunicationsEquipment", communicationsEquipmentClassB));
        
        return bem.findRoutesThroughSpecialRelationships(communicationsEquipmentClassA, communicationsEquipmentIdA, communicationsEquipmentClassB, 
                        communicationsEquipmentIB, RELATIONSHIP_SDHCONTAINERLINK);
    }
    
    /**
     * Retrieves the container links within a transport link (e.g. the VC4XX in and STMX)
     * @param transportLinkClass TransportLink's class
     * @param transportLinkId TransportLink's id
     * @return The list of the containers that go through that transport link
     * @throws InvalidArgumentException If the given transport link is no subclass of GenericSDHTransportLink
     * @throws BusinessObjectNotFoundException if the given transport link does not exist
     * @throws MetadataObjectNotFoundException If any of the core classes that support the SDH networks model does not exist
     */
    public List<SDHContainerLinkDefinition> getSDHTransportLinkStructure(String transportLinkClass, String transportLinkId) 
            throws InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        
        if (!mem.isSubclassOf("GenericSDHTransportLink", transportLinkClass))
                throw new InvalidArgumentException(String.format("Class %s is not a GenericSDHTransportLink", transportLinkClass));
        
        ArrayList<SDHContainerLinkDefinition> containers = new ArrayList<>();
        
        List<AnnotatedBusinessObjectLight> relatedContainers = bem.getAnnotatedSpecialAttribute(transportLinkClass, 
                transportLinkId, RELATIONSHIP_SDHTRANSPORTS);
        
        for (AnnotatedBusinessObjectLight container : relatedContainers) {
            List<BusinessObjectLight> relatedLinks = bem.getSpecialAttribute(container.getObject().getClassName(), 
                    container.getObject().getId(), RELATIONSHIP_SDHDELIVERS);
                                   
            if (!container.getProperties().containsKey("sdhPosition")) //NOI18N
                throw new MetadataObjectNotFoundException(String.
                        format("The container %s (id %s) is related to the transport link with id %s, but no position is specified", 
                                container.getObject().getName(), container.getObject().getId(), transportLinkId));
            
            List<SDHPosition> position = new ArrayList<>();
            position.add(new SDHPosition(transportLinkClass, transportLinkId, (Integer)container.getProperties().get("sdhPosition"))); //NOI18N
            
            containers.add(new SDHContainerLinkDefinition(container.getObject(), relatedLinks.isEmpty(), position)); //an unstructured container would have just one SDHDELIVERS relationship
                                                                                                                      //Note that the "positions" array here is filled ONLY with the position used in this particular transport link and does not represents the whole path
        }
        
        return containers;
    }
    
    /**
     * Gets the internal structure of a container link. This is useful to provide information about the occupation of a link. This is only applicable to VC4XX
     * @param containerLinkClass Container class
     * @param containerLinkId Container Id
     * @return The list of containers contained in the container
     * @throws InvalidArgumentException If the container supplied is not subclass of GenericSDHHighOrderContainerLink
     * @throws BusinessObjectNotFoundException If the container could not be found
     * @throws MetadataObjectNotFoundException If the class could not be found
     */
    public List<SDHContainerLinkDefinition> getSDHContainerLinkStructure(String containerLinkClass, String containerLinkId) 
            throws InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        
        if (!mem.isSubclassOf("GenericSDHHighOrderContainerLink", containerLinkClass))
                throw new InvalidArgumentException(String.format("Class %s is not a GenericSDHHighOrderContainerLink", containerLinkClass));
        
        ArrayList<SDHContainerLinkDefinition> containers = new ArrayList<>();
        
        List<AnnotatedBusinessObjectLight> relatedContainers = bem.getAnnotatedSpecialAttribute(containerLinkClass, 
                containerLinkId, RELATIONSHIP_SDHCONTAINS);
        
        for (AnnotatedBusinessObjectLight container : relatedContainers) {
            List<BusinessObjectLight> relatedLinks = bem.getSpecialAttribute(container.getObject().getClassName(), 
                    container.getObject().getId(), RELATIONSHIP_SDHCONTAINS);
                                   
            if (!container.getProperties().containsKey("sdhPosition"))
                throw new MetadataObjectNotFoundException(String.
                        format("The container %s (id %s) is related to the transport link with id %s, but no position is specified", 
                                container.getObject().getName(), container.getObject().getId(), containerLinkId));
            
            List<SDHPosition> position = new ArrayList<>();
            position.add(new SDHPosition(containerLinkClass, containerLinkId, (Integer)container.getProperties().get("sdhPosition")));
            
            containers.add(new SDHContainerLinkDefinition(container.getObject(), !relatedLinks.isEmpty(), position)); //an unstructured container would have just one SDHDELIVERS relationship
                                                                                                                      //Note that the "positions" array here is filled ONLY with the position used in this particular transport link and does not represents the whole path
        }
        
        return containers;
    }
    
    /**
     * Calculates a link capacity based on the class name
     * @param connectionClass The class of the link to be evaluated
     * @return The maximum number of timeslots in a transport link
     */
    public static int calculateTransportLinkCapacity(String connectionClass) {
        try {
            String positionsToBeOccupied = connectionClass.replace("STM", "");
            return Integer.valueOf(positionsToBeOccupied);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
    
    /**
     * Calculates a link capacity based on the class name
     * @param connectionClass The class of the link to be evaluated
     * @return The maximum number of timeslots in a container or transport link
     */
    public static int calculateContainerLinkCapacity(String connectionClass) {
        try {
             String positionsToBeOccupied = connectionClass.replace("VC4", "");
             if (!positionsToBeOccupied.isEmpty())
                 return Math.abs(Integer.valueOf(positionsToBeOccupied));
             return 1;
        } catch (NumberFormatException ex) {
             return 0;
        }  
    }
}
