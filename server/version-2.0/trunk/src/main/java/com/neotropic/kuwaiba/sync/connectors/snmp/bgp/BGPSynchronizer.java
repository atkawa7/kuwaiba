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
package com.neotropic.kuwaiba.sync.connectors.snmp.bgp;

import static com.neotropic.kuwaiba.modules.ipam.IPAMModule.RELATIONSHIP_IPAMHASADDRESS;
import com.neotropic.kuwaiba.sync.model.SyncResult;
import com.neotropic.kuwaiba.sync.model.TableData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
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
 * Synchronizer for the BGPTable data
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class BGPSynchronizer {
    /**
     * Cache for ASNs names to avoid consulting the peeringBD every time
     */
    public Map<String, String> asnCache;
    /**
     * A side in a tributary link
     */
    public static final String RELATIONSHIP_BGPLINKENDPOINTA = "bgpLinkEndpointA";
    /**
     * B side in a tributary link
     */
    public static final String RELATIONSHIP_BGPLINKENDPOINTB = "bgpLinkEndpointB";
    /**
     * Relationship used to connect two GenericCommunicationsEquipment 
     * with BGP technology
    */
    public static String BGPLINK = "BGPLink";
    /**
     * to relate the GenericCommunicationsEquipment as parent of the 
     * BGPLink 
     */
    public static String RELATIONSHIP_BGPLINK = "bgpLink";
    /**
     * ASN Number for won devices
     */
    private String LOCAL_ASN;
    /**
     * The class name of the object
     */
    private final String className;
    /**
     * Device id
     */
    private final String id;
    /**
     * Device Data Source Configuration id
     */
    private final long dsConfigId;
    /**
     * To load the structure of the actual device
     */
    private final List<BusinessObjectLight> currentVirtualPorts;
    /**
     * The current map of subnets and sub-subnets
     */
    private final HashMap<BusinessObjectLight, List<BusinessObjectLight>> subnets;
    /**
     * The current map of subnets with its ips addresses
     */
    private final HashMap<BusinessObjectLight, List<BusinessObjectLight>> ips;
    /**
     * The current ports in the device
     */
    private final List<BusinessObjectLight> currentPorts;
    /**
     * The ipAddrTable table loaded into the memory
     */
    private final HashMap<String, List<String>> bgpTable;
    /**
     * The ifXTable table loaded into the memory
     */
    private final HashMap<String, List<String>> bgpLocalTable;
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
    /**
     * 
     */
    private List<SyncResult> res;
    
    public BGPSynchronizer(long dsConfigId, BusinessObjectLight obj, List<TableData> data) {
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
        res = new ArrayList<>();
        this.className = obj.getClassName();
        this.id = obj.getId();
        this.dsConfigId = dsConfigId;
        bgpTable = (HashMap<String, List<String>>)data.get(0).getValue();
        bgpLocalTable = (HashMap<String, List<String>>)data.get(1).getValue();
        currentPorts = new ArrayList<>();
        subnets = new HashMap<>();
        ips = new HashMap<>();
        currentVirtualPorts = new ArrayList<>();
        asnCache = new HashMap<>();
    }
    
    /**
     * Executes the BGP synchronization
     * @return list of results
     */
    public List<SyncResult> execute() {
        try {
            readCurrentStructure(bem.getObjectChildren(className, id, -1), 1);
            readCurrentStructure(bem.getObjectSpecialChildren(className, id), 2);
            //we get the rood nodes for the ipv4
            List<Pool> ipv4RootPools = bem.getRootPools(Constants.CLASS_SUBNET_IPV4, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
            ipv4Root = ipv4RootPools.get(0);
            try {
                readcurrentIPAMFolders(ipv4RootPools);
                readCurrentSubnets(ipv4Root);
            } catch (ApplicationObjectNotFoundException ex) {
                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, 
                        "Unexpected error reading current structure", 
                        ex.getLocalizedMessage()));
            }
            try {
                LOCAL_ASN = String.valueOf(aem.getConfigurationVariableValue("sync.bgp.localAsn"));
                readMibData();
            } catch (InvalidArgumentException ex) {
               res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Retrieving Local ASN",
                            "The configuration variable sync.bgp.localAsn is not a number"));
            } catch (ApplicationObjectNotFoundException ex) {
                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Retrieving local ASN",
                            "The configuration variable sync.bgp.localAsn has not been set"));
            }
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
       
        return res;
    }
   
       
    /**
     * Reads the MIB data to create the BGP links
     */
    private void readMibData(){
        //This are the neighbor's data the ip, identifier and port
        List<String> bgpPeerIdentifier = bgpTable.get("bgpPeerIdentifier");
        List<String> bgpPeerRemoteAddr = bgpTable.get("bgpPeerRemoteAddr");
        List<String> bgpPeerRemotePort = bgpTable.get("bgpPeerRemotePort");
        //This are te ASN numbers that we use to know if custmer routers or not
        List<String> bgpPeerRemoteAs = bgpTable.get("bgpPeerRemoteAs");
        //This are the local ip addresses asign to a local port
        List<String> bgpPeerLocalAddr = bgpTable.get("bgpPeerLocalAddr");
        
        //first we must check if the device that we are synchronizing has the bgpLocalAs
        if(!bgpLocalTable.get("bgpLocalAs").isEmpty() && bgpLocalTable.get("bgpLocalAs").get(0).equals(LOCAL_ASN)){
            Map<BusinessObjectLight, List<BusinessObject>> connectedThings = new HashMap<>();
            //String foreignersDevicesList = "asn,asnName,ip,bgpPeerIdentifier\n";
            for(int i = 0; i < bgpPeerRemoteAs.size(); i++){
                //In order to sync the BGP data a port of the device should be related to an ip address
                BusinessObjectLight localPort = searchPortByAddrInCurrentStructure(bgpPeerLocalAddr.get(i));
                String asnNumber = bgpPeerRemoteAs.get(i);
                if(localPort == null)
                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_WARNING,
                            "Finding the local port related with the bgpPeerLocalAddr",
                            String.format("No port has been related with ipAddr: %s, try running ipAddress sync", bgpPeerLocalAddr.get(i))));
                else if(!asnNumber.equals("0")){//we found the local port, so we can continue
                    String asnName = checkPeerDB(asnNumber, bgpPeerLocalAddr.get(i), bgpPeerRemoteAddr.get(i));
                    //We search the remote port with the remote addr
                    BusinessObjectLight remotePort = searchPortByIpAddrInIPAM(bgpPeerRemoteAddr.get(i));
                    BusinessObject remoteDevice = null;
                    if(remotePort != null)
                        remoteDevice = findRemoteDevice(asnNumber, bgpPeerRemoteAddr.get(i), remotePort, bgpPeerIdentifier.get(i));
                    
                    if(asnNumber.equals(LOCAL_ASN)){// if the remote device has the local ASN we can create the BGPLink
                        if(remotePort == null || remoteDevice == null)
                            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_WARNING, 
                                    String.format("BGPLink will not be created for ASN %s(%s)", asnName, asnNumber), 
                                    String.format("Only local endpoint: <%s> was found, no destination port was found related with ipAddr: %s", localPort, bgpPeerRemoteAddr.get(i))));
                        else
                            createBGPLink(asnName, asnNumber, localPort, bgpPeerLocalAddr.get(i),
                                    remoteDevice, remotePort,
                                    bgpPeerRemoteAddr.get(i), bgpPeerIdentifier.get(i));  
                    }
                    else{
                        HashMap<String, String> newAttributes = new HashMap<>();
                        newAttributes.put("asnNumber", asnNumber);
                        newAttributes.put("bgpPeerIdentifier", bgpPeerIdentifier.get(i));
                        newAttributes.put("bgpPeerRemoteAddr", bgpPeerRemoteAddr.get(i));
                        newAttributes.put("bgpPeerRemotePort", bgpPeerRemotePort.get(i));
                        newAttributes.put(Constants.PROPERTY_NAME, asnName);
                        //we create a map to  check how many connections has the local port
                        List<BusinessObject> possibleProviders = connectedThings.get(localPort);
                        if(possibleProviders == null)
                            possibleProviders = new ArrayList<>();
                        //if we didn't find a remote device we create a temporal one
                        if(remoteDevice == null){
                            remoteDevice = new BusinessObject("", "-1", asnName, newAttributes);
                            //we add the remote device otherwise we will try to create the BGP link
                            possibleProviders.add(remoteDevice);
                        } //we made this if a ExternalEquipment and a port were craated manually and a relatationship between the created port and the bgpPeerRemoteAddr was created manually
                        else if(remotePort != null && remoteDevice.getClassName().equals("ExternalEquipment")){ 

                            updateAttribtues(remoteDevice, asnNumber, bgpPeerRemoteAddr.get(i));
                            createBGPLink(asnName, remoteDevice.getAttributes().get("asnNumber"), 
                                localPort, bgpPeerLocalAddr.get(i), remoteDevice, remotePort, 
                                remoteDevice.getAttributes().get("bgpPeerRemoteAddr"), bgpPeerRemotePort.get(i)); 
                            
                        }
                        connectedThings.put(localPort, possibleProviders);
                    }
                }//end if local port not found
// <editor-fold desc="To Generete the ASN, ASN name, ip file" defaultstate="collapsed">
//if(!bgpPeerRemoteAs.get(i).equals(AFRIX_ASN)){//This devices are not in the company{
    //String asname = checkPeeringDB(bgpPeerRemoteAs.get(i), 
        //bgpPeerRemoteAddr.get(i));
    //if(asname != null)
       // foreignersDevicesList += bgpPeerRemoteAs.get(i) + "," + asname + "," + bgpPeerRemoteAddr.get(i) + "," + bgpPeerIdentifier.get(i)  + "\n" ;
//}
//</editor-fold>
            }//end for
            //now we check what is ExternalEquipment(those with no ports related to an ipAddr) and what is Peering
            for (Map.Entry<BusinessObjectLight, List<BusinessObject>> entry : connectedThings.entrySet()) {
                BusinessObjectLight localPort = entry.getKey();
                List<BusinessObject> bgpPeers = entry.getValue();
                //this are ExternalEquipments
                if(bgpPeers.size() <= 3){//create a sync-room, externalEquipments
                    for (BusinessObject bgpPeer : bgpPeers) {
                        BusinessObjectLight remoteAddrIp = checkSubentsIps(bgpPeer.getAttributes().get("bgpPeerRemoteAddr"), "255.255.255.0");
                        res.add(new SyncResult(dsConfigId, SyncResult.TYPE_INFORMATION, 
                                    String.format("Possible ExternalEquipment found, with asnName: %s, asnNumber: %s", bgpPeer.getName(), bgpPeer.getAttributes().get("asnNumber")), 
                                    String.format("Please create ExternalEquipment, with at least one OpticalPort an relate that port with the ipAddr: %s", remoteAddrIp)));
                    }
                }else{
                    for(BusinessObject bgpPeer : bgpPeers) {
                        BusinessObjectLight remotePeer = searchBGPPeer(bgpPeer.getAttributes().get("asnNumber"), bgpPeer.getName(), 
                                bgpPeer.getAttributes().get("bgpPeerRemoteAddr"));
                        
                        BusinessObjectLight remotePort = null;
                        if(remotePeer == null)
                            remotePeer= createBGPPeer(bgpPeer.getAttributes().get("asnNumber"), bgpPeer.getName(), 
                                    bgpPeer.getAttributes().get("bgpPeerRemoteAddr"));
                        
                        if(remotePeer != null)
                            remotePort = createRemoteBGPPeerInterface(remotePeer, bgpPeer.getAttributes().get("bgpPeerRemoteAddr"), 
                                    bgpPeer.getAttributes().get("bgpPeerRemotePort"));
                        
                        //we only create the BGPLink if we have both sides
                        if(remotePort != null && remotePeer != null)
                            createBGPLink(bgpPeer.getName(), 
                                    bgpPeer.getAttributes().get("asnNumber"), 
                                    localPort, 
                                    bgpPeer.getAttributes().get("bgpPeerLocalAddr"), 
                                    remotePeer, remotePort, 
                                    bgpPeer.getAttributes().get("bgpPeerRemoteAddr"), 
                                    bgpPeer.getAttributes().get("bgpPeerIdentifier"));
                    }
                }
            }
        }
        else
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "reading bgpLocalAs from mib", "the value is empty"));
    }
   
    private void updateAttribtues(BusinessObject remoteDevice, String asnNumber, String bgpPeerRemoteAddr){
        try {
            boolean mustUpadeteAttributes = false;
            if(remoteDevice.getAttributes().get("bgpPeerRemoteAddr") == null){
                remoteDevice.getAttributes().put("bgpPeerRemoteAddr", bgpPeerRemoteAddr);
                mustUpadeteAttributes = true;
            }
            else if(!remoteDevice.getAttributes().get("bgpPeerRemoteAddr").equals(bgpPeerRemoteAddr)){
                remoteDevice.getAttributes().put("bgpPeerRemoteAddr", bgpPeerRemoteAddr);
                mustUpadeteAttributes = true;
            }
            if(remoteDevice.getAttributes().get("asnNumber") == null){
                remoteDevice.getAttributes().put("asnNumber", asnNumber);
                mustUpadeteAttributes = true;
            }
            else if(!remoteDevice.getAttributes().get("asnNumber").equals(asnNumber)){
                remoteDevice.getAttributes().put("asnNumber", asnNumber);
                mustUpadeteAttributes = true;
            }

            if(mustUpadeteAttributes){
                bem.updateObject(remoteDevice.getClassName(), remoteDevice.getId(), remoteDevice.getAttributes());
                //AuditTrail
                aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT, 
                    String.format("%s (id:%s), %s, %s", remoteDevice.toString(), remoteDevice.getId(), asnNumber, bgpPeerRemoteAddr));

                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, 
                        String.format("Attributes for: %s were updated", remoteDevice), 
                        String.format("AttributesAdded asnNumber: %s, bgpPeerRemoteAddr: %s", asnNumber, bgpPeerRemoteAddr)));
            }
        } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException | InvalidArgumentException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, String.format("Creating BGPLink with: %s", remoteDevice), String.format("Due to %s", ex.getLocalizedMessage())));
        }
    }
    
    /**
     * Search a provider(cloud) in the same city of the device that is been sync
     * remote  IP address port
     * @param asnNumber ASN number form SNMP
     * @param asnName ASN name from peeringDB
     * @param bgpPeerRemoteAddr remote  IP address from SNMP
     * @param bgpPeerIdentifier peer id from SNMP
     * @return The newly created cloud.
     */
    private BusinessObject searchBGPPeer(String asnNumber, String asnName, String bgpPeerRemoteAddr){
        try{
            if(!asnName.isEmpty()){
                BusinessObject location = bem.getParentOfClass(className, id, "City");
                if(location == null)
                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Searching device location",
                            "The device to be synchronized does not have a parent subclass of City"));
                else{
                    BusinessObjectLight providersParent = null;
                    List<BusinessObjectLight> children = bem.getObjectChildren(location.getClassName(), location.getId(), 10);
                    for (BusinessObjectLight child : children) {
                        if(child.getClassName().equals("Provider"))
                            providersParent = child;
                    }
                    if(providersParent == null)                        
                        res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Searching provider location",
                            "To sync BGP information, it is necessary to create an object of class Provider as child of the specified City"));
                    else{
                        List<BusinessObjectLight> peers = bem.getObjectChildren(providersParent.getClassName(), providersParent.getId(), -1);
                        for (BusinessObjectLight peer : peers) {
                            BusinessObject obj = bem.getObject(Constants.CLASS_BGPPEER, peer.getId());
                            HashMap<String, String> attributes = obj.getAttributes();
                            if(peer.getName().equals(asnName) && attributes.get("asnNumber").equals(asnNumber)){
                                //if we found the BGPPeer we must check the bgpPeerRemoteAddr
                                String currentBgpPeerRemoteAddr = obj.getAttributes().get("bgpPeerRemoteAddr");
                                if(currentBgpPeerRemoteAddr != null && !currentBgpPeerRemoteAddr.contains(bgpPeerRemoteAddr)){
                                    currentBgpPeerRemoteAddr += "; "+ bgpPeerRemoteAddr;
                                    obj.getAttributes().put("bgpPeerRemoteAddr", currentBgpPeerRemoteAddr);
                                    try {
                                        bem.updateObject(obj.getClassName(), obj.getId(), obj.getAttributes());
                                        res.add(new SyncResult(dsConfigId, SyncResult.TYPE_INFORMATION, "Grouping Peers",
                                            String.format("A new IP address %s was added to the peer %s", bgpPeerRemoteAddr, obj.toString())));
                                    } catch (OperationNotPermittedException ex) {
                                        res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Grouping Peers",
                                               ex.getLocalizedMessage()));
                                    }
                                }
                                return obj;
                            }
                        }
                        return null;
                    }
                }
            }
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Creating provider", 
                    String.format("No provider was created for ASN %s with IP address %s because %s", 
                            asnNumber, bgpPeerRemoteAddr, ex.getLocalizedMessage())));
        }
        return null;
    }
    
    /**
     * Creates a BGPPeer and a port and relates thar port with the remote IP address port
     * @param asnNumber ASN number form SNMP
     * @param asnName ASN name from peeringDB
     * @param bgpPeerRemoteAddr remote  IP address from SNMP
     * @return the created BGPPeer
     */
    private BusinessObjectLight createBGPPeer(String asnNumber, String asnName, String bgpPeerRemoteAddr){
        try{
            if(!asnName.isEmpty()){
                BusinessObject location = bem.getParentOfClass(className, id, "City");
                if(location == null)
                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Searching Device Location",
                            "The device being synchronized is not located in a City"));
                else{
                    HashMap<String, String> attributes = new HashMap<>();
                    BusinessObjectLight providersParent = null;
                    List<BusinessObjectLight> children = bem.getObjectChildren(location.getClassName(), location.getId(), 10);
                    for (BusinessObjectLight child : children) {
                        if(child.getClassName().equals("Provider"))
                            providersParent = child;
                    }
                    if(providersParent == null){
                        attributes.put(Constants.PROPERTY_NAME, "Providers");
                        String parentCreatedProvidersId = bem.createObject("Provider", "City", location.getId(), attributes, null);
                        providersParent = new BusinessObjectLight("Provider", parentCreatedProvidersId, "Providers");
                        res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "Peer Creation",
                            String.format("An object to group the peers was created in %s", location)));
                    }
                    
                    attributes = new HashMap<>();
                    attributes.put("asnNumber", asnNumber);
                    attributes.put(Constants.PROPERTY_DESCRIPTION, "Created by the BGP sync provider");
                    attributes.put("bgpPeerRemoteAddr", bgpPeerRemoteAddr);
                    attributes.put(Constants.PROPERTY_NAME, asnName);

                    String createdBGPPeerId = bem.createObject(Constants.CLASS_BGPPEER, providersParent.getClassName(), providersParent.getId(), attributes, null);
                    BusinessObjectLight createdBGPPeer = new BusinessObjectLight(Constants.CLASS_BGPPEER, createdBGPPeerId, asnName);
                    //AuditTrail
                    aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                                String.format("%s (%s)", createdBGPPeer, createdBGPPeer.getId()));
                    
                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "BGPPeer Creation",
                        String.format("Since no port was related to the remote IP address %s, a Peer instance with asnName %s (asnNumber: %s) was created in: %s", bgpPeerRemoteAddr, asnName, asnNumber, location)));

                    return createdBGPPeer;
                }
            }
        } catch (OperationNotPermittedException | ApplicationObjectNotFoundException | BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "BGPPeer Creation", 
                    String.format("No provider was created for asnNumber %s with IP address %s because %s", 
                            asnNumber, bgpPeerRemoteAddr, ex.getLocalizedMessage())));
        }
        return null;
    }
    
    /**
     * We must create an new IP because is a provider ip a there is no way to sync
     * @param remoteDevice the remote device(cloud)
     * @param bgpPeerRemoteAddr remote ipAddr, to create
     * @param bgpPeerRemotePort use it for the port name
     * @return remote port
     */
    private BusinessObjectLight createRemoteBGPPeerInterface(BusinessObjectLight remoteDevice, String bgpPeerRemoteAddr, String bgpPeerRemotePort) {
        try {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(Constants.PROPERTY_NAME, bgpPeerRemotePort);
            String newPortId = bem.createObject("VirtualPort", remoteDevice.getClassName(), remoteDevice.getId(), attributes, null);
            BusinessObjectLight remotePort = new BusinessObjectLight("VirtualPort", newPortId, bgpPeerRemotePort);
            //AuditTrail
            aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                        String.format("%s (%s)", remotePort.toString(), remotePort.getId()));
            
            BusinessObjectLight newIpAddress = checkSubentsIps(bgpPeerRemoteAddr, "");
            bem.createSpecialRelationship("VirtualPort", newPortId, newIpAddress.getClassName(), newIpAddress.getId(), RELATIONSHIP_IPAMHASADDRESS, true);
            
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "New Network Interface", 
                            String.format("%s was related to %s", remotePort, newIpAddress)));
            //AuditTrail
            aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                    String.format("%s (id), %s, %s (id)", remotePort.toString(), remotePort.getId(), RELATIONSHIP_IPAMHASADDRESS, newIpAddress.toString(), newIpAddress.getId()));
            
            return remotePort;
            
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    /**
     * Finds the remote device, searching for the parent of the remote port
     * we also update the attributes of the remote Device
     * @param asnNumber The number fetched from MIB data.
     * @param asnName The name of the ASN after searching in peeringDB.
     * @param bgpPeerRemoteAddr remote  IP address 
     * @param remotePort remote port
     * @param bgpPeerIdentifier bgpPeerId to update attributes
     * @return the remote device
     */
    private BusinessObject findRemoteDevice(String asnNumber, String bgpPeerRemoteAddr, BusinessObjectLight remotePort, String bgpPeerIdentifier){
        try{
            BusinessObject remoteDevice = bem.getParentOfClass(remotePort.getClassName(), remotePort.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
            if(remoteDevice == null) {//It could be a virtual port so it is a special child
                BusinessObjectLight remotePortParent = bem.getParent(remotePort.getClassName(), remotePort.getId());
                remoteDevice = bem.getObject(remotePortParent.getClassName(), remotePortParent.getId());
            }
            
            if(remoteDevice == null)
                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Remote Device Search", 
                            String.format("The parent of %s was not found,      ", remotePort)));

            return remoteDevice;
            
        } catch (InvalidArgumentException | BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Searching Parent", 
                    String.format("No parent was found for %s with IP address %s because %s", remotePort, bgpPeerRemoteAddr, ex.getLocalizedMessage())));
        }
        return null;
    }
    
    /**
     * Checks if exists a BGPLink created
     * @param sourcePort the source port
     * @param destinyPort the destiny port
     * @param bgpPeerIdentifier a identifier saved in the link as property
     * @return the BGPLink
     * @throws MetadataObjectNotFoundException problems with the port className
     * @throws BusinessObjectNotFoundException problems finding source or destiny port
     */
    private BusinessObjectLight isBGPLinkCreated(BusinessObjectLight sourcePort, BusinessObjectLight destinyPort, String bgpPeerIdentifier){
        HashMap<String, List<BusinessObjectLight>> sourcePortRels = new HashMap<>();
        HashMap<String, List<BusinessObjectLight>> destinyPortRels = new HashMap<>();
        try {
            sourcePortRels = bem.getSpecialAttributes(sourcePort.getClassName(), sourcePort.getId());
            destinyPortRels = bem.getSpecialAttributes(destinyPort.getClassName(), destinyPort.getId());
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
        List<BusinessObjectLight> sourceBgpLinksA = new ArrayList<>();
        List<BusinessObjectLight> sourceBgpLinksB = new ArrayList<>();
        for (Map.Entry<String, List<BusinessObjectLight>> entry : sourcePortRels.entrySet()) {
            if(entry.getKey().equals(RELATIONSHIP_BGPLINKENDPOINTA))
                sourceBgpLinksA = entry.getValue();
            if( entry.getKey().equals(RELATIONSHIP_BGPLINKENDPOINTB))
                sourceBgpLinksB = entry.getValue();
        }

        List<BusinessObjectLight> destinyBgpLinksA = new ArrayList<>();
        List<BusinessObjectLight> destinyBgpLinksB = new ArrayList<>();
        for (Map.Entry<String, List<BusinessObjectLight>> entry : destinyPortRels.entrySet()) {
            if(entry.getKey().equals(RELATIONSHIP_BGPLINKENDPOINTA))
                destinyBgpLinksA = entry.getValue();
            if(entry.getKey().equals(RELATIONSHIP_BGPLINKENDPOINTB))
                destinyBgpLinksB = entry.getValue();
        }
        
        for (BusinessObjectLight sourceBgpLink : sourceBgpLinksA) {
            if(destinyBgpLinksB.contains(sourceBgpLink))
                return sourceBgpLink;
        }
        
        for (BusinessObjectLight sourceBgpLink : sourceBgpLinksB) {
            if(destinyBgpLinksA.contains(sourceBgpLink))
                return sourceBgpLink;
        }
        
        return null;
    }
    
    /**
     * Creates a link between the local and remote device
     * @param asnNumber the ASN number from SNMP
     * @param bgpPeerLocalAddr the local ip
     * @param localPort
     * @param bgpPeerRemoteAddr
     * @param bgpPeerIdentifier 
     */
    private void createBGPLink(String asnName, String asnNumber, BusinessObjectLight localPort, String bgpPeerLocalAddr,
            BusinessObjectLight remoteDevice, BusinessObjectLight remotePort, 
            String bgpPeerRemoteAddr, String bgpPeerIdentifier)
    {
        try{ 
            BusinessObjectLight bgpLink = isBGPLinkCreated(localPort, remotePort, bgpPeerIdentifier);
            if(bgpLink == null){
                HashMap<String, String> attributesToBeSet = new HashMap<>();
                attributesToBeSet.put(Constants.PROPERTY_NAME, asnName);
                attributesToBeSet.put("bgpPeerIdentifier", bgpPeerIdentifier);
                
                String bgpLinkId = bem.createSpecialObject(BGPLINK, null, "-1", attributesToBeSet, null);
                bgpLink = new BusinessObject(BGPLINK, bgpLinkId, asnNumber);
                //AuditTrail
                aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                        String.format("%s (%s)", bgpLink.toString(), bgpLink.getId()));
                
                //We create the endpoints of the relationship, we also create a relationship between the devices and the bgp link 
                //endpointA
                bem.createSpecialRelationship(BGPLINK, bgpLinkId, localPort.getClassName(), localPort.getId(), RELATIONSHIP_BGPLINKENDPOINTA, false);
                bem.createSpecialRelationship(BGPLINK, bgpLinkId, className, id, RELATIONSHIP_BGPLINK, false);
                //AuditTrail
                aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                        String.format("%s (%s), %s,  %s (%s)", RELATIONSHIP_BGPLINKENDPOINTB, bgpLink.toString(), bgpLink.getId(), localPort.toString() , localPort.getId()));
                
                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "New BGPLink Source", 
                                String.format("%s related to IP address %s and ASN %s(%s)", localPort, bgpPeerLocalAddr, asnName, asnNumber)));
                //endpointB
                bem.createSpecialRelationship(BGPLINK, bgpLink.getId(), remotePort.getClassName(), remotePort.getId(), RELATIONSHIP_BGPLINKENDPOINTB, false);
                bem.createSpecialRelationship(BGPLINK, bgpLink.getId(), remoteDevice.getClassName(), remoteDevice.getId(), RELATIONSHIP_BGPLINK, false);
                //AuditTrail
                aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                        String.format("%s (%s), %s,  %s (%s)", RELATIONSHIP_BGPLINKENDPOINTB, bgpLink.toString(), bgpLink.getId(), remotePort.toString() , remotePort.getId()));
                
                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "New BGPLink destination", 
                            String.format("in %s - %s, related with ip: %s, for ASN %s(%s), bgpPeerIdentifier: %s", remoteDevice, remotePort, bgpPeerRemoteAddr, asnName, asnNumber, bgpPeerIdentifier)));
            }
            else
                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_INFORMATION, String.format("BGPLink exists with ASN %s(%s)", asnName, asnNumber), 
                   String.format("Has local endpoint in: %s and remote endpoint: %s in device %s", localPort, remotePort, remoteDevice)));

        } catch (ApplicationObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException | MetadataObjectNotFoundException |OperationNotPermittedException ex) {
                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "New BGP Link", 
                    String.format("Could not create BGPLink: %s", ex.getLocalizedMessage())));
        }
    }
    
    /**
     * Search a given IP in the IPAM module to get if there is a port 
     * related and the get the communication element
     * @param iPaddress a given IP address
     * @return the communications element related with the given IP 
     */
    private BusinessObjectLight searchPortByIpAddrInIPAM(String iPaddress){
        for(BusinessObjectLight subnet : ips.keySet()){
            for (BusinessObjectLight ip :  ips.get(subnet)) {
                try {
                    if(ip.getName().equals(iPaddress)){
                        List<BusinessObjectLight> relatedPort = bem.getSpecialAttribute(ip.getClassName(), ip.getId(), RELATIONSHIP_IPAMHASADDRESS);
                        if(!relatedPort.isEmpty())
                            return relatedPort.get(0);
                    }
                } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Searching in current IP address structure", ex.getLocalizedMessage()));
                }
            }
        }
        return null;
    }
    
    /**
     * Gets the name of a given ASN number
     * @param ASN the ASN number
     * @param bgpPeerLocalAddr the local ip, use it only for exception message
     * @param bgpPeerRemoteAddr the remote ip, use it only for exception message
     * @return ASN name, or an empty String if can not find the asn
     */
    private String checkPeerDB(String asn, String bgpPeerLocalAddr, String bgpPeerRemoteAddr){
        String asnName = asnCache.get(asn);
        if(asnName == null){
            try {
                URL url = new URL("https://peeringdb.com/api/net?asn=" + URLEncoder.encode(asn, "UTF-8"));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "application/json");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                int status = con.getResponseCode();
                if(status != 200)
                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Searching PeeringDB", 
                            String.format("Request error looking up ASN %s", asn)));
                else{    
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }

                    try (final JsonReader jsonReader = Json.createReader(new StringReader(content.toString()))) {
                        JsonObject jsonObj = jsonReader.readObject();
                        JsonArray jsonArray  = jsonObj.getJsonArray("data");
                        try (final JsonReader valuedReader = Json.createReader(new StringReader(jsonArray.get(0).toString()))){
                            JsonObject value = valuedReader.readObject();
                            asnName = value.getString("name");
                            asnCache.put(asn, asnName);
                        }
                    }
                    in.close();
                    con.disconnect();
                }
            }catch (IOException ex) {
                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Searching ASN in PeeringDB",
                                String.format("The ASN: %s, localAddr: %s - remoteAddr: %s, was NOT found due to: %s", asn, bgpPeerLocalAddr, bgpPeerRemoteAddr, ex.getLocalizedMessage())));
            }
        }
        
        if(asnName == null)
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_WARNING, "Searching ASN in PeeringDB", String.format("The ASN: %s, was NOT found", asn)));
        
        return asnName;
    }
    
    /**
     * Reads the device's current structure (ports, and logical ports)
     * @param children a given set of children
     * @param childrenType 1 children, 2 special children
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException 
     */
    private void readCurrentStructure(List<BusinessObjectLight> children, int childrenType) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException
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
    private void readcurrentIPAMFolders(List<Pool> folders) 
            throws ApplicationObjectNotFoundException, 
            MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException
    {
        for (Pool folder : folders) {
            if(!folders.isEmpty())
                readcurrentIPAMFolders(bem.getPoolsInPool(folder.getId(), folder.getClassName()));
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
            MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        //we read the subnets of the folder
        List<BusinessObjectLight> subnetsInFolder = bem.getPoolItems(folder.getId(), -1);
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
        MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException 
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
     * Checks if a given port exists in the current structure and is related with a given ip
     * @param bgpPeerLocalAddr a given ip address to search a port that is related with
     * @return the current port, null doesn't maybe exists in the current structure but is not related with the given ip
     */
    private BusinessObjectLight searchPortByAddrInCurrentStructure(String bgpPeerLocalAddr){
        try {
            for(BusinessObjectLight currentPort: currentPorts){
                for (BusinessObjectLight ip : bem.getSpecialAttribute(currentPort.getClassName(), currentPort.getId(), RELATIONSHIP_IPAMHASADDRESS)) {
                    if(ip.getName().equals(bgpPeerLocalAddr))
                        return currentPort;
                }
            }
            for(BusinessObjectLight currentVirtualPort: currentVirtualPorts){
                for (BusinessObjectLight ip : bem.getSpecialAttribute(currentVirtualPort.getClassName(), currentVirtualPort.getId(), RELATIONSHIP_IPAMHASADDRESS)) {
                    if(ip.getName().equals(bgpPeerLocalAddr))
                        return currentVirtualPort;
                }
            }
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, 
                    String.format("Searching local port associated to %s", bgpPeerLocalAddr), 
                    ex.getLocalizedMessage()));
        }
        return null;
    }    

    /**
     * Search for a given IP address got it from the ipAddrTableMIB data
     * if doesn't exists it will be created
     * @param ipAddr The ip address
     * @param syncMask The IP address mask from sync
     * @return an IP address created in Kuwaiba
     */
    private BusinessObjectLight checkSubentsIps(String ipAddr, String syncMask){
        //We will consider only a /24 subnet 
        if(syncMask == null || syncMask.isEmpty())
            syncMask = "255.255.255.0";
        
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
        if(currentSubnet == null)
            currentSubnet = createSubnet(newSubnet);
        
        //with the subnet found we must search if the Ip address exists
        List<BusinessObjectLight> currentIps = ips.get(currentSubnet);
        //we found the subnet but has no IPs so we create the ip
        if(currentIps != null && !currentIps.isEmpty()){
            for (BusinessObjectLight currentIpLight : currentIps) {
                if(currentIpLight.getName().equals(ipAddr)){
                    try {//we must check the mask if the IP already exists and if its attributes are updated
                        BusinessObject currentIp = bem.getObject(Constants.CLASS_IP_ADDRESS, currentIpLight.getId());
                        String oldMask = currentIp.getAttributes().get(Constants.PROPERTY_MASK);
                        if(!oldMask.equals(syncMask)){
                            currentIp.getAttributes().put(Constants.PROPERTY_MASK, syncMask);
                            bem.updateObject(currentIp.getClassName(), currentIp.getId(), currentIp.getAttributes());
                            //AuditTrail
                            aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT, String.format("%s (%s)", 
                                    currentIp, currentIp.getId()));
            
                            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, String.format("Updating the netmask for IP address %s", currentIp), String.format("From: %s to: %s", oldMask, syncMask)));
                        }
                        return currentIpLight;
                    } catch (ApplicationObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                        res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,  String.format("Updating the netmask for IP address %s", currentIpLight), ex.getLocalizedMessage()));
                    }
                }
            }
        }//we create the ip address if doesn't exists in the current subnet
        return createIp(currentSubnet, ipAddr, syncMask);
    }
    
    /**
     * Creates a new subnet
     * @param newSubnet a given subnet name
     * @return the created subnet
     */
    private BusinessObjectLight createSubnet(String newSubnet){
        BusinessObjectLight currentSubnet = null;
        String [] attributeNames = {"name", "description", "networkIp", "broadcastIp", "hosts"};
        String [] attributeValues = {newSubnet + ".0/24", "created with sync", newSubnet + ".0", newSubnet + ".255", "254"};
        try {
            //TODO change this for create subnet of the IPAM module
            currentSubnet = bem.getObject(Constants.CLASS_SUBNET_IPV4, 
                    bem.createPoolItem(ipv4Root.getId(), ipv4Root.getClassName(), attributeNames, attributeValues, null));
            //AuditTrail
            aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s (id:%s)", currentSubnet.toString(), currentSubnet.getId()));
            
        } catch (ApplicationObjectNotFoundException | ArraySizeMismatchException | BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, 
                    String.format("%s [Subnet] ca not be created", newSubnet + ".0/24"), 
                    ex.getLocalizedMessage()));
        }//we must add the new subnet into the current subnets and ips
        subnets.put(currentSubnet, new ArrayList<>()); 
        ips.put(currentSubnet, new ArrayList<>());
        return currentSubnet;
    }
    
    /**
     * Creates an IP address in a given subnet
     * @param subnet a given subnet
     * @param ipAddr a new ip address to be created
     * @param syncMask a mask for the given ip address
     * @return the new created ip address
     */
    private BusinessObject createIp(BusinessObjectLight subnet, String ipAddr, String syncMask){
        BusinessObject createdIp = null;
        HashMap<String, String> ipAttributes = new HashMap<>();
        ipAttributes.put(Constants.PROPERTY_NAME, ipAddr);
        ipAttributes.put(Constants.PROPERTY_DESCRIPTION, "Created by the BGP sync provider");
        ipAttributes.put(Constants.PROPERTY_MASK, syncMask); //TODO set the list types attributes
        try { 
            String newIpAddrId = bem.createSpecialObject(Constants.CLASS_IP_ADDRESS, subnet.getClassName(), subnet.getId(), ipAttributes, null);
            //AuditTrail
            aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s [IPAddress] (%s)", ipAddr, newIpAddrId));
            
            createdIp = bem.getObject(Constants.CLASS_IP_ADDRESS, newIpAddrId);
            ips.get(subnet).add(createdIp);
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "Added IP address to Subnet", String.format("%s was added to subnet %s successfully", ipAddr, subnet)));
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, 
                        String.format("ipAddr: %s was not added to subnet: %s", ipAddr, subnet), 
                        ex.getLocalizedMessage()));
        }
        return createdIp;
    }
    
}
