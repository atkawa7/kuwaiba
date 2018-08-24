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
package com.neotropic.kuwaiba.sync.connectors.snmp.cisco;

import static com.neotropic.kuwaiba.modules.mpls.MPLSModule.RELATIONSHIP_MPLSENDPOINTA;
import static com.neotropic.kuwaiba.modules.mpls.MPLSModule.RELATIONSHIP_MPLSENDPOINTB;
import static com.neotropic.kuwaiba.modules.mpls.MPLSModule.RELATIONSHIP_MPLSLINK;
import com.neotropic.kuwaiba.sync.model.SyncFinding;
import com.neotropic.kuwaiba.sync.model.SyncUtil;
import com.neotropic.kuwaiba.sync.model.TableData;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.ViewObjectLight;
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
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;
import org.openide.util.Exceptions;

/**
 * Loads the got it from SNMP 
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class CiscoSynchronizer {
    
    private final static String MPLSLINK = "MPLSLink";
    
    private final static String MPLSVIEW = "MPLSModuleView";
    /**
     * The class name of the object
     */
    private final String className;
    /**
     * Device id
     */
    private final long id;
    /**
     * The mpls-mib table loaded into the memory
     */
    private final HashMap<String, List<String>> mplsData;
    /**
     * The mpls-mib table loaded into the memory
     */
    private final HashMap<String, List<String>> teData;
    /**
     * The mpls-mib table loaded into the memory
     */
    private final HashMap<String, List<String>> inboundData;
    /**
     * The actual ports of the device
     */
    private final List<BusinessObjectLight> currentPorts;
    /**
     * Current virtual ports of the device
     */
    private final List<BusinessObjectLight> currentVirtualPorts;
    /**
     * Current MPLS tunnels in the device 
     */
    private List<BusinessObjectLight> currentTunnels;
    /**
     * reference to the bem
     */
    private BusinessEntityManager bem;
    /**
     * Reference to de aem
     */
    private ApplicationEntityManager aem;
    /**
     * the result finding list
     */
    private List<SyncFinding> findings = new ArrayList<>();

    /**
     * Helper used to read the actual structure recursively
     * @param obj
     * @param data
     */
    public CiscoSynchronizer(BusinessObjectLight obj, List<TableData> data) {
        try {
            PersistenceService persistenceService = PersistenceService.getInstance();
            bem = persistenceService.getBusinessEntityManager();
            aem = persistenceService.getApplicationEntityManager();
        } catch (IllegalStateException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE,
                    ex.getClass().getSimpleName() + ": {0}", ex.getMessage()); //NOI18N
            bem = null;
            aem = null;
        }
        this.className = obj.getClassName();
        this.id = obj.getId();
        mplsData = (HashMap<String, List<String>>)data.get(0).getValue();
        teData = (HashMap<String, List<String>>)data.get(1).getValue();
        inboundData = (HashMap<String, List<String>>)data.get(1).getValue();
        currentPorts = new ArrayList<>();
        currentVirtualPorts = new ArrayList<>();
        currentTunnels = new ArrayList<>();
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
            else if (child.getClassName().equals(Constants.CLASS_VIRTUALPORT))
                currentVirtualPorts.add(child);
            
            if (childrenType == 1) 
                readCurrentStructure(bem.getObjectChildren(child.getClassName(), child.getId(), -1), 1);
            else if (childrenType == 2) 
                readCurrentStructure(bem.getObjectSpecialChildren(child.getClassName(), child.getId()), 2);
        }
    }
    
    public List<SyncFinding> execute(){
        try {
            currentTunnels = bem.getSpecialChildrenOfClassLight(id, className, Constants.CLASS_MPLSTUNNEL, -1);
            readCurrentStructure(bem.getObjectChildren(className, id, -1), 1);
            readCurrentStructure(bem.getObjectSpecialChildren(className, id), 2);
            readMplsData();
            readTeMplsData();
            
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException | OperationNotPermittedException | ApplicationObjectNotFoundException | ArraySizeMismatchException | NotAuthorizedException | ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
        return findings;
    }

    private void readMplsData() throws MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, InvalidArgumentException, 
            OperationNotPermittedException, ApplicationObjectNotFoundException, 
            ArraySizeMismatchException, NotAuthorizedException, ServerSideException 
    {
        List<JsonObject> jsonResults = new ArrayList<>();
        List<String> vcIDs = mplsData.get("cpwVcID");
        for(int i = 0; i< vcIDs.size(); i++){
            String vcID = "VC " + mplsData.get("cpwVcID").get(i);
            String serviceName = mplsData.get("cpwVcDescr").get(i); //todo add into MPLS Link
            String portName = mplsData.get("cpwVcName").get(i);
            String destinyPortName = mplsData.get("cpwVcRemoteIfString").get(i);
            
            BusinessObjectLight syncSourcePort = searchInCurrentStructure(SyncUtil.wrapPortName(portName), 1);
            if(syncSourcePort == null){
                syncSourcePort = searchInCurrentStructure(SyncUtil.wrapPortName(portName), 2);
                if(syncSourcePort == null)
                    System.out.println("este puerto no esta");
            }
            //we get the parent
            if(syncSourcePort != null){
                BusinessObject syncEquipment = bem.getParentOfClass(syncSourcePort.getClassName(), syncSourcePort.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                if (syncEquipment == null)
                    throw new ServerSideException(String.format("The specified port (%s : %s) doesn't seem to be located in a communications equipment", syncSourcePort.getName(), syncSourcePort.getId()));

                List<ViewObjectLight> mplsViews = aem.getGeneralViews("MPLSModuleView", -1);
                boolean viewAlreadyCreated = false;
                for(ViewObjectLight view : mplsViews){
                    if(view.getName().equals(vcID)){ //the service name no sirve!
                        if(aem.getGeneralView(view.getId()).getStructure() == null)
                            break;
                        readView(view, syncSourcePort, destinyPortName, syncEquipment, vcID);
                        viewAlreadyCreated = true;
                    }
                }//end for
                if(!viewAlreadyCreated){ //We must create the view, because it doesn't exists
                    HashMap<String, String> attributesToBeSet = new HashMap<>();
                    attributesToBeSet.put(Constants.PROPERTY_NAME, vcID);
                    
                    long newMplsLinkId = bem.createSpecialObject(MPLSLINK, null, -1, attributesToBeSet, -1);     
                    bem.createSpecialRelationship(MPLSLINK, newMplsLinkId, syncSourcePort.getClassName(), syncSourcePort.getId(), RELATIONSHIP_MPLSENDPOINTA, true);
                    bem.createSpecialRelationship(syncEquipment.getClassName(), syncEquipment.getId(),
                            MPLSLINK, newMplsLinkId, RELATIONSHIP_MPLSLINK, false); 
                    
                    byte[] createMplsView = SyncUtil.createMplsView(//devices
                            Arrays.asList(syncEquipment, new BusinessObjectLight(-1l, "- unknown wating for sync -", className)), 
                            new BusinessObject(newMplsLinkId, vcID, MPLSLINK));

                    aem.createGeneralView(MPLSVIEW, vcID, "Synchroniced", createMplsView, null);
                }
                
                jsonResults.add((Json.createObjectBuilder()
                            .add("vcid", vcID)
                            .add("sourcePort", syncSourcePort.toString())
                            .add("destinyPort", destinyPortName)
                            .add("device", syncEquipment.toString())
                            .add("service", serviceName))
                            .build());
            }
            JsonArray ciscoMibResult = Json.createArrayBuilder().build();
            for (JsonObject jo : jsonResults) 
                ciscoMibResult = SyncUtil.jArrayBuilder(ciscoMibResult).add(Json.createObjectBuilder().add("result", jo)).build();
            
            JsonObject ciscoMib = Json.createObjectBuilder().build();
            ciscoMib = SyncUtil.joBuilder(ciscoMib).add("type", "ciscomib").add("ciscomibsync", ciscoMibResult).build();
            findings.add(new SyncFinding(SyncFinding.EVENT_INFO, 
                           "Cisco MPLS VcIDs synchronization result", ciscoMib.toString()));
        }
    }
     
    public void readView(ViewObjectLight view, BusinessObjectLight syncSourcePort, 
             String syncDestinyPortName, BusinessObject syncCommunicationsEquipment, String vcID) 
             throws IllegalArgumentException, MetadataObjectNotFoundException, 
             BusinessObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, ApplicationObjectNotFoundException, ServerSideException 
     {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        QName qEdge = new QName("edge"); //NOI18N
        try {
            byte[] structure = aem.getGeneralView(view.getId()).getStructure();
            
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
                        
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT){
                    if (reader.getName().equals(qEdge)){
                        //sino lo encontró en ningún lado, se deben desconectar ambos lados, sino no se hace nada.
                        long currentASideId = Long.valueOf(reader.getAttributeValue(null, "aside"));
                        long currentBSideId = Long.valueOf(reader.getAttributeValue(null, "bside"));
                        //we check the classname of the link
                        if(!reader.getAttributeValue(null,"class").equals(MPLSLINK))
                            throw new InvalidArgumentException(String.format("The view is corrupted, a %s is been used instead of a MPLSLink", reader.getAttributeValue(null,"class")));

                        BusinessObject mplsLink = bem.getObject(MPLSLINK, Long.valueOf(reader.getAttributeValue(null, "id")));
                         //The MPLS link has no been created an the sides are not connected
                        if (mplsLink == null) 
                            throw new InvalidArgumentException(String.format("The view is corrupted, no MPLSLink is created", reader.getAttributeValue(null,"class")));
                        //We check the name of the mpls link and update the name
                        if(!mplsLink.getName().equals(vcID)){
                            mplsLink.getAttributes().put(Constants.PROPERTY_NAME, vcID);
                            bem.updateObject(mplsLink.getClassName(), mplsLink.getId(), mplsLink.getAttributes());
                        }
                        String endpointARelationshipName, endpointBRelationshipName;
                        endpointARelationshipName = "mplsEndpointA"; //NOI18N
                        endpointBRelationshipName = "mplsEndpointB"; //NOI18N
                        BusinessObjectLight currentEndpointA = null;
                        BusinessObjectLight currentEndpointB = null;
                        //First we must get the endpoints because we only have the name of the destiny port
                        List<BusinessObjectLight> endpointARelationship = bem.getSpecialAttribute(MPLSLINK, mplsLink.getId(), endpointARelationshipName); //NOI18N
                        if(!endpointARelationship.isEmpty())
                            currentEndpointA = endpointARelationship.get(0);
                        List<BusinessObjectLight> endpointBRelationship = bem.getSpecialAttribute(MPLSLINK, mplsLink.getId(), endpointBRelationshipName); //NOI18N
                        if(!endpointBRelationship.isEmpty())
                            currentEndpointB = endpointBRelationship.get(0);
                        //both sides are connected, source port match with sideA and destiny port match with sideB
                        //>> sideA <<-----------MPLSLink-----------(sideB) 
                        if (currentASideId == id){ 
                            System.out.println("sync ok its updated side A ok");
                            if(currentBSideId != -1 && currentEndpointB != null && currentEndpointB.getName().equals(syncDestinyPortName)) //also check if the sideB match with the destiny port name
                                System.out.println("the destiny port match with the name");
                            break;
                        } 
                        //both sides are connected, source port match with sideB and destiny port match with sideA
                        //>> sideB <<-----------MPLSLink-----------(sideA) 
                        else if (currentBSideId == id){ 
                            System.out.println("sync ok its updated side B ok");
                            if(currentASideId != -1 && currentEndpointA != null && currentEndpointA.getName().equals(syncDestinyPortName)) //also check if the sideA match with the destiny port name
                                System.out.println("the destiny port match with the name");
                            break;
                        }
                        if(currentBSideId != -1 && currentBSideId == id && currentEndpointB != null && currentEndpointB.getId() != syncSourcePort.getId())//The link endpoint needs to be release
                            bem.releaseSpecialRelationship(mplsLink.getClassName(), mplsLink.getId(), RELATIONSHIP_MPLSENDPOINTB, currentEndpointB.getId());
                        //Only one side has been connected
                        if (currentBSideId ==  -1 || currentBSideId == id){
                            bem.createSpecialRelationship(MPLSLINK, mplsLink.getId(), syncSourcePort.getClassName(), syncSourcePort.getId(), RELATIONSHIP_MPLSENDPOINTB, true);
                            structure = SyncUtil.updateView(structure, syncCommunicationsEquipment, 2);
                            break;
                        }
                        if (currentASideId != -1 && currentASideId == id && currentEndpointA != null && currentEndpointA.getId() != syncSourcePort.getId())//The link endpoint needs to be release
                            bem.releaseSpecialRelationship(mplsLink.getClassName(), mplsLink.getId(), RELATIONSHIP_MPLSENDPOINTA, syncSourcePort.getId());

                        if (currentASideId ==  -1 || currentASideId == id){
                            bem.createSpecialRelationship(MPLSLINK, mplsLink.getId(), syncSourcePort.getClassName(), syncSourcePort.getId(), RELATIONSHIP_MPLSENDPOINTA, true);
                            structure = SyncUtil.updateView(structure, syncCommunicationsEquipment, 1);
                            break;
                        }
                    }
                }
            }//end while
            reader.close();
            aem.updateGeneralView(view.getId(), view.getName(), view.getDescription(), structure, null);
            
        } catch (NumberFormatException | XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
        
    private void readTeMplsData() throws MetadataObjectNotFoundException,
            BusinessObjectNotFoundException, InvalidArgumentException, 
            OperationNotPermittedException, ApplicationObjectNotFoundException, 
            ArraySizeMismatchException, NotAuthorizedException, ServerSideException
    {
        List<JsonObject> jsonResults = new ArrayList<>();
        List<String> instances = teData.get("instance");
        List<String> instancesmplsData = mplsData.get("instance");
        //List<String> teVcMappingIndex = teData.get("cpwVcMplsTeMappingVcIndex");
        
        for(int i = 0; i< instances.size(); i++){
            String[] x = instances.get(i).split("\\.");
            String tunnel = x[1];
            String ipSource = x[3] + "." + x[4] + "." + x[5] + "." + x[6];
            String ipDestiny = x[7] + "." + x[8] + "." + x[9] + "." + x[10];
            
            BusinessObjectLight foundTunnel = searchInCurrentTunnels(tunnel.replaceAll("\\D+",""));
            if(foundTunnel != null){
                String VcMapping = teData.get("cpwVcMplsTeMappingVcIndex").get(i);
                for(int j=0; j<instancesmplsData.size(); j++){
                    if(instancesmplsData.get(j).equals(VcMapping)){
                        String vcID = mplsData.get("cpwVcID").get(j);
                        String serviceName = mplsData.get("cpwVcDescr").get(i);

                        List<ViewObjectLight> mplsViews = aem.getGeneralViews("MPLSModuleView", -1);
                        for(ViewObjectLight view : mplsViews){
                            if(view.getName().replaceAll("\\D+","").equals(vcID)){ //the service name doesn't work so we search for VcID
                                if(aem.getGeneralView(view.getId()).getStructure() == null)
                                    break;
                                BusinessObject mplsLink = getMplsLinkFromView(view);
                                mplsLink.getAttributes().put("ipSource", ipSource);
                                mplsLink.getAttributes().put("ipDestiny", ipDestiny);
                                mplsLink.getAttributes().put("description", serviceName);
                                bem.updateObject(mplsLink.getClassName(), mplsLink.getId(), mplsLink.getAttributes());
                                bem.createSpecialRelationship(MPLSLINK, mplsLink.getId(), foundTunnel.getClassName(), foundTunnel.getId(), "mplstunnel", true);

                                jsonResults.add((Json.createObjectBuilder()
                                    .add("vcid", vcID)
                                    .add("tunnel", foundTunnel.toString())
                                    .add("ipSource", ipSource)
                                    .add("ipDestiny", ipDestiny)
                                    .add("description", serviceName))
                                    .build());
                            }
                        }//end for
                    }
                }
            }
            else
                jsonResults.add((Json.createObjectBuilder()
                                    .add("vcid", "Not associated because tunnel not found")
                                    .add("tunnel", tunnel.replaceAll("\\D+",""))
                                    .add("ipSource", ipSource)
                                    .add("ipDestiny", ipDestiny)
                                    .add("description", ""))
                                    .build());
        }
        JsonArray ciscoMibResult = Json.createArrayBuilder().build();
        for (JsonObject jo : jsonResults) 
            ciscoMibResult = SyncUtil.jArrayBuilder(ciscoMibResult).add(Json.createObjectBuilder().add("result", jo)).build();

        JsonObject ciscoMib = Json.createObjectBuilder().build();
        ciscoMib = SyncUtil.joBuilder(ciscoMib).add("type", "ciscoTemib").add("ciscoTemibsync", ciscoMibResult).build();
        findings.add(new SyncFinding(SyncFinding.EVENT_INFO, 
                       "Cisco MPLS Tunnels synchronization result", ciscoMib.toString()));
    }
     
    public BusinessObject getMplsLinkFromView(ViewObjectLight view) 
             throws IllegalArgumentException, MetadataObjectNotFoundException, 
             BusinessObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, ApplicationObjectNotFoundException, ServerSideException 
    {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        QName qEdge = new QName("edge"); //NOI18N
        try {
            byte[] structure = aem.getGeneralView(view.getId()).getStructure();
            
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
            
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT){
                    if (reader.getName().equals(qEdge)){
                        //we check the classname of the link
                        if(!reader.getAttributeValue(null,"class").equals(MPLSLINK))
                            throw new InvalidArgumentException(String.format("The view is corrupted, a %s is been used instead of a MPLSLink", reader.getAttributeValue(null, "class")));

                        return bem.getObject(MPLSLINK, Long.valueOf(reader.getAttributeValue(null, "id")));
                    }
                }
            }//end while
            reader.close();
        } catch (NumberFormatException | XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
 
    private BusinessObjectLight searchInCurrentTunnels(String ifName) throws MetadataObjectNotFoundException, MetadataObjectNotFoundException, BusinessObjectNotFoundException{
        for(BusinessObjectLight child : currentTunnels){
            if(child.getName().toLowerCase().replaceAll("\\D+","").contains(ifName.toLowerCase()))
                return child;
        }
        return null;
    } 
    
    
    /**
     * Checks if a given port exists in the current structure
     * @param ifName a given name for port, virtual port or MPLS Tunnel
     * @param type 1 port, 2 virtual port, 3 MPLSTunnel, 4 bdi, 5 VLAN
     * @return the object, null doesn't exists in the current structure
     */
    private BusinessObjectLight searchInCurrentStructure(String ifName, int type){
        switch(type){
            case 1:
                for(BusinessObjectLight currentPort: currentPorts){
                    if(currentPort.getName().toLowerCase().equals(ifName.toLowerCase()))
                        return currentPort;
                }
            case 2:
                for(BusinessObjectLight currentVirtualPort: currentVirtualPorts){
                    if(currentVirtualPort.getName().toLowerCase().equals(ifName.toLowerCase()))
                        return currentVirtualPort;
                }
            default:
                return null;
        }
    }
}
