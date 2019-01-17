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
     * A simple cache for ASNs names to avoid consulting the peeringBD every time
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
    private final String LOCAL_ASN="16591"; //TODO move this into de variable module.
    /**
     * The class name of the object
     */
    private final String className;
    /**
     * Device id
     */
    private final long id;
    /**
     * Device Data Source Configuration id
     */
    private final long dsConfigId;
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
     * Executes the synchronization to associate the interfaces get it 
     * from the ifmib table with the Ip addresses get it from the ipAddrTable
     * @return list of findings
     */
    public List<SyncResult> execute() {
        try {
            readCurrentStructure(bem.getObjectChildren(className, id, -1), 1);
            readCurrentStructure(bem.getObjectSpecialChildren(className, id), 2);
            //we get the rood nodes for the ipv4
            List<Pool> ipv4RootPools = bem.getRootPools(Constants.CLASS_SUBNET_IPV4, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
            ipv4Root = ipv4RootPools.get(0);
            try {
                readcurrentFolder(ipv4RootPools);
                readCurrentSubnets(ipv4Root);
            } catch (ApplicationObjectNotFoundException ex) {
                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, 
                        "Unexpected error reading current structure", 
                        ex.getLocalizedMessage()));
            }
           readMibData();
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return res;
    }
   
       
    /**
     * Reads the MIB data to create the BGP links
     */
    private void readMibData(){
        //This are the ips related to the neighbors 
        List<String> bgpPeerIdentifier = bgpTable.get("bgpPeerIdentifier");
        List<String> bgpPeerRemoteAddr = bgpTable.get("bgpPeerRemoteAddr");
        //This are the local IP address asign to a local port
        List<String> bgpPeerLocalAddr = bgpTable.get("bgpPeerLocalAddr");
        List<String> bgpPeerRemotePort = bgpTable.get("bgpPeerRemotePort");
        //This are te ASN numbers that we use to know if custmer routers or not
        List<String> bgpPeerRemoteAs = bgpTable.get("bgpPeerRemoteAs");
        //first we must check it the bgpLocalAs is not a customer router
        if(!bgpLocalTable.get("bgpLocalAs").isEmpty() && bgpLocalTable.get("bgpLocalAs").get(0).equals(LOCAL_ASN)){
            //String foreignersDevicesList = "asn,asnName,ip,bgpPeerIdentifier\n";
            for(int i = 0; i < bgpPeerRemoteAs.size(); i++){
                //In order to sync the BGP data a port of the device should be related to an ip address
                BusinessObjectLight localPort = searchPortInCurrentStructure(bgpPeerLocalAddr.get(i));
                if(localPort == null)
                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_WARNING,
                            "Finding the local port related with the bgpPeerLocalAddr",
                            String.format("No port has been related with ip address %s", bgpPeerLocalAddr.get(i))));
                else
                    createBGPLink(bgpPeerRemoteAs.get(i), bgpPeerLocalAddr.get(i), localPort, bgpPeerRemoteAddr.get(i), bgpPeerIdentifier.get(i), bgpPeerRemotePort.get(i));
                    // <editor-fold desc="To Generete the ASN, ASN name, ip file" defaultstate="collapsed">
                    //if(!bgpPeerRemoteAs.get(i).equals(AFRIX_ASN)){//This devices are not in the company{
                        //String asname = checkPeeringDB(bgpPeerRemoteAs.get(i), 
                            //bgpPeerRemoteAddr.get(i));
                        //if(asname != null)
                           // foreignersDevicesList += bgpPeerRemoteAs.get(i) + "," + asname + "," + bgpPeerRemoteAddr.get(i) + "," + bgpPeerIdentifier.get(i)  + "\n" ;
                    //}
                    //</editor-fold>
                
            }//end for
            // <editor-fold desc="To Generete the ASN, ASN name, ip file" defaultstate="collapsed">
//            try {
//                BusinessObject syncDevice = bem.getObject(id);
//                Writer output = null;
//                output = new BufferedWriter(new FileWriter("/opt/glassfish4/glassfish/domains/staging/kuwaiba/" + syncDevice.getName() + "_bgp-sync.csv"));
//                output.write(foreignersDevicesList);
//                output.close();
//                
//            } catch (Exception ex) {
//                Exceptions.printStackTrace(ex);
//            }
            // </editor-fold>
        }
        else
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "reading bgpLocalAs", "the value is empty"));
    }
   
    /**
     * Search a provider(cloud) in the same city of the device that is been sync
     * remote ip addr port
     * @param asn asn number form snmp
     * @param asnName asn name from peeringDB
     * @param bgpPeerRemoteAddr remote ip addr from snmp
     * @param bgpPeerIdentifier peering id from snmp
     * @return the created cloud
     */
    private BusinessObject searchProvider(String asn, String asnName, String bgpPeerRemoteAddr, String bgpPeerIdentifier){
        try{
            if(!asnName.isEmpty()){
                BusinessObject location = bem.getParentOfClass(className, id, "City");
                if(location == null)
                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Seacrhing device location",
                            "The sync device has no parent subclass of City"));
                else{
                    List<BusinessObjectLight> objectChildren = bem.getObjectChildren(location.getClassName(), location.getId(), -1);
                    for (BusinessObjectLight child : objectChildren) {
                        BusinessObject obj = bem.getObject(child.getId());
                        HashMap<String, String> attributes = obj.getAttributes();
                        if(child.getName().equals(asnName) && 
                                attributes.get("asn").equals(asn) && 
                                attributes.get("bgpPeerIdentifier").equals(bgpPeerIdentifier)){
                        
                            return obj;
                        }
                    }
                    return null;
                }
            }
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Creating provider", 
                    String.format("no provider was created for asn %s related with ip: %s due to: %s", 
                            asn, bgpPeerRemoteAddr, ex.getLocalizedMessage())));
        }
        return null;
    }
    
    /**
     * Creates a provider(cloud) a port and relates thar port with the 
     * remote ip addr port
     * @param asn asn number form snmp
     * @param asnName asn name from peeringDB
     * @param bgpPeerRemoteAddr remote ip addr from snmp
     * @param bgpPeerIdentifier peering id from snmp
     * @return the created cloud
     */
    private BusinessObject createProvider(String asn, String asnName, String bgpPeerRemoteAddr, String bgpPeerIdentifier){
        try{
            if(!asnName.isEmpty()){
                BusinessObject location = bem.getParentOfClass(className, id, "City");
                if(location == null)
                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Seacrhing device location",
                            "The sync device has no parent subclass of City"));
                else{
                    HashMap<String, String> attributes = new HashMap<>();
                    attributes.put("asn", asn);
                    attributes.put("bgpPeerIdentifier", bgpPeerIdentifier);
                    attributes.put(Constants.PROPERTY_NAME, asnName);
                    long createObjectId = bem.createObject("Cloud", location.getClassName(), location.getId(), attributes, -1);

                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_INFORMATION, "creating Provider",
                        String.format("Due to no port was related with the remoteAddrIp: %s", bgpPeerRemoteAddr)));

                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "creating Provider",
                        String.format("a Cloud with asn: %s(%s) was created in %s", asnName, asn, location)));

                    return bem.getObject(createObjectId);
                }
            }
        } catch (OperationNotPermittedException | ApplicationObjectNotFoundException | BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Creating provider", 
                    String.format("no provider was created for asn %s related with ip: %s due to: %s", 
                            asn, bgpPeerRemoteAddr, ex.getLocalizedMessage())));
        }
        return null;
    }
    
    /**
     * Finds the remote device, searching for the parent of the remote port
     * we also update the attributes of the remote Device
     * @param asn number got it from mib data
     * @param asnName name of the asn after searchin in peeringDB
     * @param bgpPeerRemoteAddr remote ip addr 
     * @param remotePort remote port
     * @param bgpPeerIdentifier bgpPeerId to update attribtues
     * @return the remote device
     */
    private BusinessObject findRemoteDevice(String asn, String bgpPeerRemoteAddr, BusinessObjectLight remotePort, String bgpPeerIdentifier){
        try{
            BusinessObject remoteDevice = bem.getParentOfClass(remotePort.getClassName(), remotePort.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
            if(remoteDevice == null)//it could be a virtual Port so it is a specialchildren
                remoteDevice = bem.getObject(bem.getParent(remotePort.getClassName(), remotePort.getId()).getId());
            if(remoteDevice == null)
                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "creating BGP Link", 
                            String.format("parent of %s was not found, could be not subclass of GenericCommunicationsElement", remotePort)));
            else{
                //We must update the attributes of the neighbor
                HashMap<String, String> attributes = remoteDevice.getAttributes();
                if(!asn.equals(LOCAL_ASN)){ //only for foreigner devices
                    String currentAsn = attributes.get("asn");
                    if(currentAsn == null || !currentAsn.equals(asn)){
                        attributes.put("asn", asn);
                        res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "update attribute", 
                                String.format("asn was updated from: %s to: %s, in device: %s", currentAsn, asn, remoteDevice)));
                    }
                }
                String currentBgpPeerIdentifier = attributes.get("bgpPeerIdentifier");
                if(currentBgpPeerIdentifier == null || !currentBgpPeerIdentifier.equals(bgpPeerIdentifier)){
                    attributes.put("bgpPeerIdentifier", bgpPeerIdentifier);
                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "update attribute", 
                            String.format("bgpPeerIdentifier was updated from: %s to: %s, in device: %s", currentBgpPeerIdentifier, bgpPeerIdentifier, remoteDevice)));
                }
            }
            return remoteDevice;
            
        } catch (InvalidArgumentException | BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Searching Parent", 
                    String.format("no parent was found for: %s related with ip: %s due to: %s", 
                            remotePort, bgpPeerRemoteAddr, ex.getLocalizedMessage())));
        }
        return null;
    }
    
    private BusinessObjectLight checkBGPLink(BusinessObjectLight sourcePort, BusinessObjectLight destinyPort) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException
    {
        HashMap<String, List<BusinessObjectLight>> sourcePortRels = bem.getSpecialAttributes(sourcePort.getClassName(), sourcePort.getId());
        List<BusinessObjectLight> sourceBgpLinksA = new ArrayList<>();
        List<BusinessObjectLight> sourceBgpLinksB = new ArrayList<>();
        for (Map.Entry<String, List<BusinessObjectLight>> entry : sourcePortRels.entrySet()) {
            if(entry.getKey().equals(RELATIONSHIP_BGPLINKENDPOINTA))
                sourceBgpLinksA = entry.getValue();
            if( entry.getKey().equals(RELATIONSHIP_BGPLINKENDPOINTB))
                sourceBgpLinksB = entry.getValue();
        }
               
        HashMap<String, List<BusinessObjectLight>> destinyPortRels = bem.getSpecialAttributes(destinyPort.getClassName(), destinyPort.getId());
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
     * @param asn the asn number from snmp
     * @param bgpPeerLocalAddr the local ip
     * @param localPort
     * @param bgpPeerRemoteAddr
     * @param bgpPeerIdentifier 
     */
    private void createBGPLink(String asn, String bgpPeerLocalAddr, 
            BusinessObjectLight localPort, String bgpPeerRemoteAddr, 
            String bgpPeerIdentifier, String bgpPeerRemotePort)
    {
        try{ 
            String asnName = LOCAL_ASN;
            if(!asn.equals(LOCAL_ASN))
               asnName = checkPeeringDB(asn, bgpPeerLocalAddr, bgpPeerRemoteAddr);
            //We search the remote port in neighbor device
            BusinessObjectLight remotePort = searchPortByAddr(bgpPeerRemoteAddr);
            //if no port was related with the remoteAddr we create a provider(Cloud)
            BusinessObject remoteDevice = null;
            
            if(remotePort != null)
                remoteDevice = findRemoteDevice(asn, bgpPeerRemoteAddr, remotePort, bgpPeerIdentifier);
            
            else if(!asn.equals(LOCAL_ASN)){ //we only create a provider if is a foreigner asn
                remoteDevice = createProvider(asn, asnName, bgpPeerRemoteAddr, bgpPeerIdentifier);
                if(remoteDevice != null)
                    remotePort = createRemoteProviderPort(remoteDevice, bgpPeerRemoteAddr, bgpPeerRemotePort);
            }
            //we only create the BGPLink if we have both sides
            //the destiny side
            if(remotePort != null && remoteDevice != null){
                BusinessObjectLight sourceBgpLink = checkBGPLink(localPort, remotePort);
                if(sourceBgpLink == null){
                    HashMap<String, String> attributesToBeSet = new HashMap<>();
                    attributesToBeSet.put(Constants.PROPERTY_NAME, asn);
                    long bgpLinkId = bem.createSpecialObject(BGPLINK, null, -1, attributesToBeSet, -1);
                    sourceBgpLink = new BusinessObject(BGPLINK, bgpLinkId, asn);
                    //We create the endpoints of the relationship, we also create a relationship between the devices and the bgp link 
                    //endpointA
                    bem.createSpecialRelationship(BGPLINK, bgpLinkId, localPort.getClassName(), localPort.getId(), RELATIONSHIP_BGPLINKENDPOINTA, false);
                    bem.createSpecialRelationship(BGPLINK, bgpLinkId, className, id, RELATIONSHIP_BGPLINK, false);
                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "BGPLink source endpoint created", 
                                    String.format("in this device - %s, for asn: %s(%s)", localPort, asnName, asn)));
                    //endpointB
                    bem.createSpecialRelationship(BGPLINK, sourceBgpLink.getId(), remotePort.getClassName(), remotePort.getId(), RELATIONSHIP_BGPLINKENDPOINTB, false);
                    bem.createSpecialRelationship(BGPLINK, sourceBgpLink.getId(), remoteDevice.getClassName(), remoteDevice.getId(), RELATIONSHIP_BGPLINK, false);
                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "BGPLink destiny endpoint created", 
                                String.format("in %s - %s, for asn: %s(%s)", remoteDevice, remotePort, asnName, asn)));
                }
                else
                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_INFORMATION, "BGPLink exists", 
                       String.format("asn: %s(%s), has endpoints %s to %s in device %s", 
                               localPort, asnName, asn, remoteDevice, remotePort)));
            }
            else
                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_INFORMATION, String.format("BGPLink will not be created for asn: %s(%s)", asnName, asn), 
                   String.format("only source endpoint %s was found, no destiny port was found realted with ip %s", 
                           localPort, bgpPeerRemoteAddr)));
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException | MetadataObjectNotFoundException |OperationNotPermittedException ex) {
                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "creating BGP Link", 
                    String.format("Could not create BGPLink, due to: %s", ex.getLocalizedMessage())));
        }
    }
    
    /**
     * Search a given IP in the IPAM module to get if there is a port 
     * related and the get the communication element
     * @param iPaddress a given IP address
     * @return the communications element related with the given IP 
     */
    private BusinessObjectLight searchPortByAddr(String iPaddress){
        for(BusinessObjectLight subnet : ips.keySet()){
            for (BusinessObjectLight ip :  ips.get(subnet)) {
                try {
                    if(ip.getName().equals(iPaddress)){
                        List<BusinessObjectLight> relatedPort = bem.getSpecialAttribute(ip.getClassName(), ip.getId(), RELATIONSHIP_IPAMHASADDRESS);
                        if(!relatedPort.isEmpty())
                            return relatedPort.get(0);
                    }
                } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "searching in current IPAM", ex.getLocalizedMessage()));
                }
            }
        }
        return null;
    }
    
    /**
     * Gets the name of a given asn number
     * @param asn the asn number
     * @param bgpPeerLocalAddr the local ip, use it only for exception message
     * @param bgpPeerRemoteAddr the remote ip, use it only for exception message
     * @return asn name, or an empty String if can not find the asn
     */
    private String checkPeeringDB(String asn, String bgpPeerLocalAddr, String bgpPeerRemoteAddr){
        String asnName = asnCache.get(asn);
        try {    
            if(asnName == null){
                URL url = new URL("https://peeringdb.com/api/net?asn=" + URLEncoder.encode(asn, "UTF-8"));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "application/json");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                int status = con.getResponseCode();
                if(status != 200)
                    res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "searching PeeringDB", 
                            String.format("Request error looking for the ASN: %s", asn)));
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
                    return asnName;
                }
            }
        }catch (IOException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, "Searching ASN in PeeringDB",
                            String.format("The ASN: %s, localAddr: %s - remoteAddr: %s, was found due to: %s", asn, bgpPeerLocalAddr, bgpPeerRemoteAddr, ex.getLocalizedMessage())));
        }
        return "";
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
                readcurrentFolder(bem.getPoolsInPool(folder.getId(), folder.getClassName()));
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
     * Checks if a given port exists in the current structure and is related with a given ip
     * @param bgpPeerLocalAddr a given Ip address to search a port that is related with
     * @return the current port, null doesn't maybe exists in the current structure but is not related with the given ip
     */
    private BusinessObjectLight searchPortInCurrentStructure(String bgpPeerLocalAddr){
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
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_INFORMATION, 
                    String.format("Searching local port associated to %s", bgpPeerLocalAddr), 
                    ex.getLocalizedMessage()));
        }
        return null;
    }    

    /**
     * We must create an new IP because is a provider ip a there is no way to sync
     * @param remoteDevice the remote device(cloud)
     * @param bgpPeerRemoteAddr remote ipd addr, to create
     * @param bgpPeerRemotePort use it for the port name
     * @return remote port
     */
    private BusinessObjectLight createRemoteProviderPort(BusinessObject remoteDevice, String bgpPeerRemoteAddr, String bgpPeerRemotePort) {
        try {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(Constants.PROPERTY_NAME, bgpPeerRemotePort);
            long newPortId = bem.createObject("OpticalPort", remoteDevice.getClassName(), remoteDevice.getId(), attributes, -1);
            BusinessObjectLight newIpAddress = updateSubentsIps(bgpPeerRemoteAddr, "");
            bem.createSpecialRelationship("OpticalPort", newPortId,
                                                newIpAddress.getClassName(), newIpAddress.getId(), RELATIONSHIP_IPAMHASADDRESS, true);
            return bem.getObject(newPortId);
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     * Search for a given IP address got it from the ipAddrTableMIB data
     * if doesn't exists it will be created
     * @param ipAddr the ip address
     * @param syncMask the ip address mask from sync
     * @return an IP address created in kuwaiba
     */
    private BusinessObjectLight updateSubentsIps(String ipAddr, String syncMask){
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
        if(currentSubnet == null)
            currentSubnet = createSubnet(newSubnet);
        
        //with the subnet found we must search if the Ip address exists
        List<BusinessObjectLight> currentIps = ips.get(currentSubnet);
        //we found the subnet but has no IPs so we create the ip
        if(currentIps != null && !currentIps.isEmpty()){
            for (BusinessObjectLight currentIpLight : currentIps) {
                if(currentIpLight.getName().equals(ipAddr)){
                    try {//we must check the mask if the IP already exists and if its attributes are updated
                        BusinessObject currentIp = bem.getObject(currentIpLight.getId());
                        String oldMask = currentIp.getAttributes().get(Constants.PROPERTY_MASK);
                        if(!oldMask.equals(syncMask)){
                            currentIp.getAttributes().put(Constants.PROPERTY_MASK, syncMask);
                            bem.updateObject(currentIp.getClassName(), currentIp.getId(), currentIp.getAttributes());
                            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, 
                                String.format("updating the mask of %s", currentIp),
                                String.format("from: %s to: %s", oldMask, syncMask)));
                        }
                        return currentIpLight;
                    } catch (InvalidArgumentException | BusinessObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                        res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,  String.format("updating the mask of %s", currentIpLight), ex.getLocalizedMessage()));
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
            currentSubnet = bem.getObject(bem.createPoolItem(ipv4Root.getId(), ipv4Root.getClassName(), attributeNames, attributeValues, 0));
        } catch (ApplicationObjectNotFoundException | ArraySizeMismatchException | BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, 
                    String.format("%s [Subnet] can't be created", newSubnet + ".0/24"), 
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
        ipAttributes.put(Constants.PROPERTY_DESCRIPTION, "created with sync");
        ipAttributes.put(Constants.PROPERTY_MASK, syncMask); //TODO set the list types attributes
        try { 
            long newIpId = bem.createSpecialObject(Constants.CLASS_IP_ADDRESS, subnet.getClassName(), subnet.getId(), ipAttributes, -1);
            createdIp = bem.getObject(newIpId);
            ips.get(subnet).add(createdIp);
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "Add IP to Subnet", String.format("%s was added to %s successfully", ipAddr, subnet)));
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, 
                        String.format("%s was not added tot %s", ipAddr, subnet), 
                        ex.getLocalizedMessage()));
        }
        return createdIp;
    }
    
}
