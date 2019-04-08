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
package com.neotropic.kuwaiba.modules.mpls;

import com.neotropic.kuwaiba.modules.GenericCommercialModule;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessObject;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessRuleException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * This class implements the functionality corresponding to the MPLS module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class MPLSModule implements GenericCommercialModule {

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
    //Constants
    /**
     * A side in a tributary link
     */
    public static String RELATIONSHIP_MPLSENDPOINTA = "mplsEndpointA";
    /**
     * B side in a tributary link
     */
    public static String RELATIONSHIP_MPLSENDPOINTB = "mplsEndpointB";
    /**
     * Relates a pseudowire and its output interface, the output interface is the endpoint of a MPLS link if is a port
     */
    public static String RELATIONSHIP_MPLSPSEUDOWIREHASOUTPUTINTERFACE = "mplsHasOutputInterface";
    /**
     * Relates two pseudowires that are logical linked inside a MPLS device
     */
    public static String RELATIONSHIP_MPLSPSEUDOWIRE = "mplsRelatedPseudowire";
    /**
     * Relates the MPLS link directly with the GenericNetworkElements parents of 
     * the end points of the MPLS link, it is used to explore the MPLS links in a 
     * MPLS device or the routing between devices
     */
    public static String RELATIONSHIP_MPLSLINK = "mplsLink";
            
    @Override
    public String getName() {
        return "MPLS Networks Module"; //NOI18N
    }

    @Override
    public String getDescription() {
        return "MPLS Module, ";
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
     * Creates a MPLS Link
     * @param endpointAClassName className of the endpoint side A
     * @param endpointAId id of the end point side A
     * @param endpointBClassName className of the endpoint side B
     * @param endpointBId ind of the endpoint side B
     * @param attributesToBeSet attributes for the new MPLS link
     * @return the id of the new MPLS link
     * @throws ServerSideException If the given linkType is no subclass of GenericLogicalConnection
     *                              If any of the requested objects can't be found
     *                              If any of the classes provided can not be found
     *                              If any of the objects involved can't be connected
     */
    public String createMPLSLink(String endpointAClassName, String endpointAId, 
            String endpointBClassName, String endpointBId, 
            HashMap<String, String> attributesToBeSet) throws ServerSideException {
        if (bem == null || mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        String newConnectionId = null;
        try {
            if (!mem.isSubclassOf("GenericLogicalConnection", Constants.CLASS_MPLSLINK))
                throw new ServerSideException(String.format("Class %s is not subclass of GenericLogicalConnection", Constants.CLASS_MPLSLINK));
            
            BusinessObject communicationsEquipmentA, communicationsEquipmentB;
            //at least one side should be not null to create the MPLS link
            if(endpointAClassName != null && endpointAId != null || endpointBClassName != null && endpointBId != null){
                newConnectionId = bem.createSpecialObject(Constants.CLASS_MPLSLINK, null, "-1", attributesToBeSet, -1);
                aem.createGeneralActivityLogEntry("mplsModule", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s [%s] created", attributesToBeSet.get(Constants.PROPERTY_NAME), Constants.CLASS_MPLSLINK));
            }
            //Side A
            if(endpointAClassName != null && endpointAId != null){
                communicationsEquipmentA= bem.getParentOfClass(endpointAClassName, endpointAId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
            
                if (communicationsEquipmentA == null)
                    throw new ServerSideException(String.format("The specified port (%s : %s) doesn't seem to be located in a communications equipment", endpointAClassName, endpointAId));
             
                if(mem.isSubclassOf(Constants.CLASS_GENERICPORT, endpointAClassName)){
                    String endPointName = bem.getAttributeValueAsString(endpointAClassName, endpointAId, Constants.PROPERTY_NAME);
                    bem.createSpecialRelationship(Constants.CLASS_MPLSLINK, newConnectionId, endpointAClassName, endpointAId, RELATIONSHIP_MPLSENDPOINTA, true);
                    aem.createGeneralActivityLogEntry("mplsModule", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s[%s] - %s - %s", attributesToBeSet.get(Constants.PROPERTY_NAME), RELATIONSHIP_MPLSLINK, RELATIONSHIP_MPLSENDPOINTB, endPointName));
                }
                else 
                    throw new ServerSideException(String.format("%s is not subClass of GenericPort, can not be endpoint of a mplsLink", endpointAClassName));
                //besides the reletionship of the MPLS link with its endpoints, we create a direct relatioship between the mplsLink and the device 
                //this relationships helps to easily check the mplsLink in a device and to creates a simple MPLS map
                bem.createSpecialRelationship(communicationsEquipmentA.getClassName(), communicationsEquipmentA.getId(), Constants.CLASS_MPLSLINK, newConnectionId, RELATIONSHIP_MPLSLINK, false);
                aem.createGeneralActivityLogEntry("mplsModule", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s - %s - %s", communicationsEquipmentA.getName(), RELATIONSHIP_MPLSLINK, attributesToBeSet.get(Constants.PROPERTY_NAME)));
            }
            //Side B
            if(endpointBClassName != null && endpointBId != null){
                communicationsEquipmentB  = bem.getParentOfClass(endpointBClassName, endpointBId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
           
                if (communicationsEquipmentB == null)
                    throw new ServerSideException(String.format("The specified port (%s : %s) doesn't seem to be located in a communications equipment", endpointBClassName, endpointBId));
            
                if(mem.isSubclassOf(Constants.CLASS_GENERICPORT, endpointBClassName)){
                    String endPointName = bem.getAttributeValueAsString(endpointBClassName, endpointBId, Constants.PROPERTY_NAME);
                    bem.createSpecialRelationship(Constants.CLASS_MPLSLINK, newConnectionId, endpointBClassName, endpointBId, RELATIONSHIP_MPLSENDPOINTB, true);
                    aem.createGeneralActivityLogEntry("mplsModule", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s[%s] - %s - %s", attributesToBeSet.get(Constants.PROPERTY_NAME), RELATIONSHIP_MPLSLINK, RELATIONSHIP_MPLSENDPOINTB, endPointName));
                }
                else 
                    throw new ServerSideException(String.format("%s is not subClass of GenericPort, can not be endpoint of a mplsLink", endpointBClassName));
                //Direct relationship with the device
                bem.createSpecialRelationship(Constants.CLASS_MPLSLINK, newConnectionId, communicationsEquipmentB.getClassName(), communicationsEquipmentB.getId(), RELATIONSHIP_MPLSLINK, false);
                aem.createGeneralActivityLogEntry("mplsModule", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s - %s - %s", communicationsEquipmentB.getName(), RELATIONSHIP_MPLSLINK, attributesToBeSet.get(Constants.PROPERTY_NAME)));
            }

            return newConnectionId;
        } catch (Exception e) {
            //TODO: This should be replace with a transaction that lasts as long as everything in this method has been done, instead of
            //doing commits in every call to the BEM
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newConnectionId != null) {
                try {
                    bem.deleteObject(Constants.CLASS_MPLSLINK, newConnectionId, true);
                } catch (Exception ex) {
                    Logger.getLogger(MPLSModule.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }

            throw new ServerSideException(e.getMessage());
        }
    }
    
    /**
     * Deletes a MPLS Link
     * 
     * @param linkClass
     * @param linkId
     * @param forceDelete
     * @throws ServerSideException
     * @throws InventoryException If the object can not be found
     *                            If either the object class or the attribute can not be found
     *                            If the class could not be found
     *                            If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     * @throws NotAuthorizedException
     */
    public void deleteMPLSLink(String linkId, boolean forceDelete) 
            throws ServerSideException, InventoryException, NotAuthorizedException {
        if (bem == null || mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        
        BusinessObjectLight mplsLink =  bem.getObjectLight(Constants.CLASS_MPLSLINK, linkId);
        if (!mplsLink.getClassName().equals(Constants.CLASS_MPLSLINK)) //NOI18N
                throw new ServerSideException(String.format("Only links of class MPLSLink can be deleted, class: %s can be deleted", mplsLink.getClassName()));
        
        bem.deleteObject(mplsLink.getClassName(), linkId, forceDelete);
        aem.createGeneralActivityLogEntry("mplsModule", ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, String.format("%s deleted", mplsLink, Constants.CLASS_MPLSLINK));
    }
    
    /**
     * Relates a given pseudowire with a pseudowire or relates the pseudowire 
     * with an output interface (if is a GenericPhysicalPort or a VirtualPort)
     * @param pseudoWireId pseudowire id
     * @param interfaceClassName interface class name
     * @param interfaceId interface id
     * @throws org.kuwaiba.exceptions.ServerSideException
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException
     * @throws org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException
     * @throws org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException
     * @throws org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException
     */
    public void relatePseudowires(String pseudoWireId, String interfaceClassName, String interfaceId) throws 
            ServerSideException, InvalidArgumentException, 
            MetadataObjectNotFoundException, BusinessObjectNotFoundException, 
            ApplicationObjectNotFoundException, OperationNotPermittedException{

        if (bem == null || mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");

        if(pseudoWireId != null){
            BusinessObjectLight pseudowire = bem.getObjectLight(Constants.CLASS_PSEUDOWIRE, pseudoWireId);
            if(pseudowire != null){
                if(interfaceClassName.equals(Constants.CLASS_PSEUDOWIRE)){
                    String relatedInterface = bem.getAttributeValueAsString(interfaceClassName, interfaceId, Constants.PROPERTY_NAME);
                    bem.createSpecialRelationship(Constants.CLASS_PSEUDOWIRE, pseudoWireId, interfaceClassName, interfaceId, RELATIONSHIP_MPLSPSEUDOWIRE, true); //NOI18N
                    aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s - pseudowire - %s", pseudowire, relatedInterface));
                }else if(mem.isSubclassOf(Constants.CLASS_GENERICPORT, interfaceClassName)){
                    String outputIntfName = bem.getAttributeValueAsString(interfaceClassName, interfaceId, Constants.PROPERTY_NAME);
                    bem.createSpecialRelationship(pseudowire.getClassName(), pseudowire.getId(), interfaceClassName, interfaceId, RELATIONSHIP_MPLSPSEUDOWIREHASOUTPUTINTERFACE, true); //NOI18N
                    aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s - " + RELATIONSHIP_MPLSPSEUDOWIREHASOUTPUTINTERFACE + " - %s", pseudowire, outputIntfName));
                }
            }
        }
    }
    
    /**
     * Get the MPLS link endpoints
     * @param connectionId MPLS link id
     * @return MPLS link endpoints
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException 
     * @throws org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException 
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException 
     * @throws org.kuwaiba.exceptions.ServerSideException 
     */
    public BusinessObjectLight[] getMPLSLinkEndpoints(String connectionId) 
            throws InvalidArgumentException, BusinessObjectNotFoundException, 
            MetadataObjectNotFoundException, ServerSideException 
    {
        if (bem == null || mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        
        String connectionClass = bem.getObject(Constants.CLASS_MPLSLINK, connectionId).getClassName();
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, connectionClass)) //NOI18N
                throw new InvalidArgumentException(String.format("Class %s is not MPLSLink connection", connectionClass));
            
        List<BusinessObjectLight> endpointA = bem.getSpecialAttribute(Constants.CLASS_MPLSLINK, connectionId, RELATIONSHIP_MPLSENDPOINTA);
        List<BusinessObjectLight> endpointB = bem.getSpecialAttribute(Constants.CLASS_MPLSLINK, connectionId, RELATIONSHIP_MPLSENDPOINTB);
        return new BusinessObjectLight[] {endpointA.isEmpty() ? null : endpointA.get(0), endpointB.isEmpty() ? null : endpointB.get(0)};
    }
    
    /**
     * Connect MPLS links
     * @param sideAClassNames endpoint side A class names
     * @param sideAIds endpoint side A ids
     * @param linksIds MPLS links ids
     * @param sideBClassNames endpoint side B class names
     * @param sideBIds endpoint side B ids
     * @throws ServerSideException 
     * @throws MetadataObjectNotFoundException 
     * @throws BusinessObjectNotFoundException 
     * @throws InvalidArgumentException 
     * @throws BusinessRuleException 
     * @throws OperationNotPermittedException 
     */
    public void connectMplsLink(String[] sideAClassNames, String[] sideAIds, 
            String[] linksIds, String[] sideBClassNames, String[] sideBIds)
            throws ServerSideException, MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, BusinessRuleException, OperationNotPermittedException
    { 
        try{
            for (int i = 0; i < linksIds.length; i++){
                BusinessObjectLight mplsLink = null;
                if (linksIds[i] != null)
                    mplsLink = bem.getObjectLight(Constants.CLASS_MPLSLINK, linksIds[i]);
                if(mplsLink != null && mplsLink.getClassName().equals(Constants.CLASS_MPLSLINK)) //NOI18N
                    throw new ServerSideException(String.format("Class %s is not a MPLS link", mplsLink.getClassName()));
                if (sideAClassNames[i] != null && !mem.isSubclassOf(Constants.CLASS_GENERICPORT, sideAClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Class %s is not a port", sideAClassNames[i]));
                if (sideBClassNames[i] != null && !mem.isSubclassOf(Constants.CLASS_GENERICPORT, sideBClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Class %s is not a port", sideBClassNames[i]));
                if (Objects.equals(sideAIds[i], sideBIds[i]))
                    throw new ServerSideException("Can not connect a port to itself");

                if(mplsLink != null){
                    String endpointAName = RELATIONSHIP_MPLSENDPOINTA, endpointBName = RELATIONSHIP_MPLSENDPOINTB;

                    List<BusinessObjectLight> aEndpointList = bem.getSpecialAttribute(mplsLink.getClassName(), mplsLink.getId(), endpointAName); //NOI18N
                    List<BusinessObjectLight> bEndpointList = bem.getSpecialAttribute(mplsLink.getClassName(), mplsLink.getId(), endpointBName); //NOI18N

                    if (!aEndpointList.isEmpty()){
                        if (Objects.equals(aEndpointList.get(0).getId(), sideAIds[i]) || Objects.equals(aEndpointList.get(0).getId(), sideBIds[i]))
                            throw new ServerSideException("The link is already related to at least one of the endpoints");
                    }

                    if (!bEndpointList.isEmpty()){
                        if (Objects.equals(bEndpointList.get(0).getId(), sideAIds[i]) || Objects.equals(bEndpointList.get(0).getId(), sideBIds[i]))
                            throw new ServerSideException("The link is already related to at least one of the endpoints");
                    }

                    if (sideAIds[i] != null && sideAClassNames[i] != null) {
                        if (!bem.getSpecialAttribute(sideAClassNames[i], sideAIds[i], endpointAName).isEmpty() || //NOI18N
                            !bem.getSpecialAttribute(sideAClassNames[i], sideAIds[i], endpointBName).isEmpty()) //NOI18N
                            throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(sideAClassNames[i], sideAIds[i])));

                        if (aEndpointList.isEmpty()) {
                            aem.checkRelationshipByAttributeValueBusinessRules(mplsLink.getClassName(), mplsLink.getId(), sideAClassNames[i], sideAIds[i]);
                            bem.createSpecialRelationship(mplsLink.getClassName(), mplsLink.getId(), sideAClassNames[i], sideAIds[i], endpointAName, true); //NOI18N
                        }
                        else
                            throw new ServerSideException(String.format("Link %s already has an endpoint A", bem.getObjectLight(mplsLink.getClassName(), mplsLink.getId())));
                    }
                    if (sideBIds[i] != null && sideBClassNames[i] != null) {
                        if (!bem.getSpecialAttribute(sideBClassNames[i], sideBIds[i], endpointBName).isEmpty() || //NOI18N
                            !bem.getSpecialAttribute(sideBClassNames[i], sideBIds[i], endpointAName).isEmpty()) //NOI18N
                            throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(sideBClassNames[i], sideBIds[i])));

                        if (bEndpointList.isEmpty()) {
                            aem.checkRelationshipByAttributeValueBusinessRules(mplsLink.getClassName(), mplsLink.getId(), sideBClassNames[i], sideBIds[i]);
                            bem.createSpecialRelationship(mplsLink.getClassName(), mplsLink.getId(), sideBClassNames[i], sideBIds[i], endpointBName, true); //NOI18N
                        }
                        else
                            throw new ServerSideException(String.format("Link %s already has an endpoint B", bem.getObjectLight(mplsLink.getClassName(), mplsLink.getId())));
                    }
                }
            }
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    
    /**
     * Disconnect a mplsLink from its endpoints
     * @param connectionId MPLS link id
     * @param sideToDisconnect if is side A or side B or both sides
     * @throws org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException 
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException 
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException 
     * @throws org.kuwaiba.exceptions.ServerSideException 
     * @throws org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException 
     */
    public void disconnetMPLSLink(String connectionId, int sideToDisconnect) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, 
            MetadataObjectNotFoundException, ApplicationObjectNotFoundException, 
            ServerSideException
    {
        if (bem == null || mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        
        String  affectedProperties = "", oldValues = "";
        switch (sideToDisconnect) {
            case 1: //A side
                BusinessObjectLight endpointA = bem.getSpecialAttribute(Constants.CLASS_MPLSLINK, connectionId, RELATIONSHIP_MPLSENDPOINTA).get(0); //NOI18N                    
                bem.releaseRelationships(Constants.CLASS_MPLSLINK, connectionId, Arrays.asList(RELATIONSHIP_MPLSENDPOINTA)); //NOI18N

                affectedProperties += RELATIONSHIP_MPLSENDPOINTA + " "; //NOI18N
                oldValues += endpointA.getId() + " ";
                break;
            case 2: //B side
                BusinessObjectLight endpointB = bem.getSpecialAttribute(Constants.CLASS_MPLSLINK, connectionId, "endpointB").get(0); //NOI18N                    
                bem.releaseRelationships(Constants.CLASS_MPLSLINK, connectionId, Arrays.asList("endpointB")); //NOI18N

                affectedProperties += "endpointB" + " "; //NOI18N
                oldValues += endpointB.getId() + " ";
                break;
            case 3: //Both sides
                endpointA = bem.getSpecialAttribute(Constants.CLASS_MPLSLINK, connectionId, RELATIONSHIP_MPLSENDPOINTA).get(0); //NOI18N
                endpointB = bem.getSpecialAttribute(Constants.CLASS_MPLSLINK, connectionId, "endpointB").get(0); //NOI18N
                bem.releaseRelationships(Constants.CLASS_MPLSLINK, connectionId, Arrays.asList(RELATIONSHIP_MPLSENDPOINTA, "endpointB")); //NOI18N

                affectedProperties += RELATIONSHIP_MPLSENDPOINTA + " "; //NOI18N
                oldValues += endpointA.getId() + " ";

                affectedProperties += "endpointB" + " "; //NOI18N
                oldValues += endpointB.getId() + " ";
                break;
            default:
                throw new InvalidArgumentException(String.format("Wrong side to disconnect option"));
        }
        aem.createObjectActivityLogEntry("mplsModule", Constants.CLASS_MPLSLINK, connectionId, 
            ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
            affectedProperties, oldValues, "", ""); //NOI18N
    }
}