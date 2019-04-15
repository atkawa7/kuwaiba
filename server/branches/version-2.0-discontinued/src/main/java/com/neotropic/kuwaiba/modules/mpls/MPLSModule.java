/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * This class implements the functionality corresponding to the MPLS module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
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
     * The relationship used to connect two GenericCommunicationsEquipment to 
     * represent that ports within the equipment are connected with MPLS Links. 
     * This is used to ease the way to find routes between elements
     */
    public static String RELATIONSHIP_MPLSLINK = "mplsLink";
    /**
     * TODO: place this relationships in other place
     * This relationship is used to relate a network element with extra logical configuration
     */
    public static final String RELATIONSHIP_MPLSPORTBELONGSTOINTERFACE = "mplsportbelongtointerface";
    
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
        this.mem = mem;
        this.bem = bem;
    }
    
    //The actual methods
    public long createMPLSLink(String classNameEndpointA, long idEndpointA, 
            String classNameEndpointB, long idEndpointB, String linkType, String defaultName) throws ServerSideException {
        if (bem == null || mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        long newConnectionId = -1;
        try {
            if (!mem.isSubClass("GenericLogicalConnection", linkType)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not subclass of GenericLogicalConnection", linkType));

            HashMap<String, List<String>> attributesToBeSet = new HashMap<>();
            attributesToBeSet.put(Constants.PROPERTY_NAME, Arrays.asList(new String[] { defaultName == null ? "" : defaultName }));
            
            RemoteBusinessObject communicationsEquipmentA = bem.getParentOfClass(classNameEndpointA, idEndpointA, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
            if (communicationsEquipmentA == null)
                throw new ServerSideException(String.format("The specified port (%s : %s) doesn't seem to be located in a communications equipment", classNameEndpointA, idEndpointA));
            
            RemoteBusinessObject communicationsEquipmentB = bem.getParentOfClass(classNameEndpointB, idEndpointB, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
            if (communicationsEquipmentB == null)
                throw new ServerSideException(String.format("The specified port (%s : %s) doesn't seem to be located in a communications equipment", classNameEndpointB, idEndpointB));
            
            newConnectionId = bem.createSpecialObject(linkType, null, -1, attributesToBeSet, 0);                      
                       
            bem.createSpecialRelationship(linkType, newConnectionId, classNameEndpointA, idEndpointA, RELATIONSHIP_MPLSENDPOINTA, true);
            bem.createSpecialRelationship(linkType, newConnectionId, classNameEndpointB, idEndpointB, RELATIONSHIP_MPLSENDPOINTB, true);
            
            //We add a relationship between the shelves and the Transport LInks so we can easily find a route between two equipment when creatin low order connections
            //based on TransportLinks paths            
            bem.createSpecialRelationship(communicationsEquipmentA.getClassName(), communicationsEquipmentA.getId(), 
                    linkType, newConnectionId, RELATIONSHIP_MPLSLINK, false);
            
            bem.createSpecialRelationship(linkType, newConnectionId, communicationsEquipmentB.getClassName(), 
                    communicationsEquipmentB.getId(), RELATIONSHIP_MPLSLINK, false);
            
            return newConnectionId;
        } catch (Exception e) {
            //TODO: This should be replace with a transaction that lasts as long as everything in this method has been done, instead of
            //doing commits in every call to the BEM
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newConnectionId != -1) {
                try {
                    bem.deleteObject(linkType, newConnectionId, true);
                } catch (Exception ex) {
                    Logger.getLogger(MPLSModule.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }

            throw new ServerSideException(e.getMessage());
        }
    }
    
    //The actual methods
    public void deleteMPLSLink(String linkClass, long linkId, boolean forceDelete) 
            throws ServerSideException, InventoryException, NotAuthorizedException {
        if (bem == null || mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        
        if (!linkClass.equals("MPLSLink")) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a of MPLSLink", linkClass));
        
        List<RemoteBusinessObjectLight> containerLinks = bem.getSpecialAttribute(linkClass, linkId, RELATIONSHIP_MPLSLINK);
        
        bem.deleteObject(linkClass, linkId, forceDelete);
    }
    /**
     * Relates an interface with a generic communication port
     * @param portId port id
     * @param portClassName the classname of the configuration you want to relate with
     * @param interfaceClassName interface's class
     * @param interfaceId interface id
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws MetadataObjectNotFoundException 
     */
    public void relatePortToInterface(long portId, String portClassName, String interfaceClassName, long interfaceId) throws ObjectNotFoundException,
            OperationNotPermittedException, MetadataObjectNotFoundException{
        bem.createSpecialRelationship(interfaceClassName, interfaceId, portClassName, portId, RELATIONSHIP_MPLSPORTBELONGSTOINTERFACE, true);
    }
    
    /**
     * Release the relationship between a GenericPort and an interface
     * @param interfaceClassName interface's class
     * @param interfaceId interface id
     * @param portId port id 
     * @throws ObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws ApplicationObjectNotFoundException
     * @throws NotAuthorizedException 
     */
    public void releasePortFromInterface(String interfaceClassName, long interfaceId ,long portId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException,
            ApplicationObjectNotFoundException, NotAuthorizedException
    {
        bem.releaseSpecialRelationship(interfaceClassName, interfaceId, portId, RELATIONSHIP_MPLSPORTBELONGSTOINTERFACE);
    }
    
}
