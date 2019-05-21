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

package com.neotropic.kuwaiba.sync.connectors.ssh.mpls;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import static com.neotropic.kuwaiba.modules.ipam.IPAMModule.RELATIONSHIP_IPAMHASADDRESS;
import com.neotropic.kuwaiba.modules.mpls.MPLSModule;
import static com.neotropic.kuwaiba.modules.mpls.MPLSModule.RELATIONSHIP_MPLSENDPOINTA;
import static com.neotropic.kuwaiba.modules.mpls.MPLSModule.RELATIONSHIP_MPLSENDPOINTB;
import static com.neotropic.kuwaiba.modules.mpls.MPLSModule.RELATIONSHIP_MPLSLINK;
import com.neotropic.kuwaiba.sync.connectors.ssh.mpls.entities.MPLSLink;
import com.neotropic.kuwaiba.sync.connectors.ssh.mpls.parsers.MplsSyncDefaultParser;
import com.neotropic.kuwaiba.sync.connectors.ssh.mpls.parsers.MplsSyncASR9001Parser;
import com.neotropic.kuwaiba.sync.model.AbstractDataEntity;
import com.neotropic.kuwaiba.sync.model.AbstractSyncProvider;
import com.neotropic.kuwaiba.sync.model.PollResult;
import com.neotropic.kuwaiba.sync.model.SyncAction;
import com.neotropic.kuwaiba.sync.model.SyncDataSourceConfiguration;
import com.neotropic.kuwaiba.sync.model.SyncFinding;
import com.neotropic.kuwaiba.sync.model.SyncResult;
import com.neotropic.kuwaiba.sync.model.SyncUtil;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.util.i18n.I18N;
import org.openide.util.Exceptions;

/**
 * This provider connects to Cisco routers via SSH, retrieves the MPLS data, and creates/updates the MPLS views
  * @author Adrian Martinez Molina Edward Bedon Cortazar {@literal <adrian.martinez@kuwaiba.org>}
 */
public class MplsSyncProvider extends AbstractSyncProvider {
    /**
     * The current map of subnets and sub-subnets
     */
    private HashMap<BusinessObjectLight, List<BusinessObjectLight>> subnets;
    /**
     * The current map of subnets with its ips addresses
     */
    private HashMap<BusinessObjectLight, List<BusinessObjectLight>> ips;
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
     * a list with the results after sync
     */
    private List<SyncResult> res;
    
    @Override
    public String getDisplayName() {
        return "MPLS links and VCids Sync Provider";
    }

    @Override
    public String getId() {
        return MplsSyncProvider.class.getName();
    }

    @Override
    public boolean isAutomated() {
        return true;
    }

    @Override
    public List<AbstractDataEntity> unmappedPoll(SynchronizationGroup syncGroup) {
        throw new UnsupportedOperationException("This provider does not support unmapped polling");
    }

    @Override
    public PollResult mappedPoll(SynchronizationGroup syncGroup) {
        System.out.println(">>> entering mpls provider");
        List<SyncDataSourceConfiguration> syncDataSourceConfigurations = syncGroup.getSyncDataSourceConfigurations();
        
        PollResult res = new PollResult();
        BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();

        JSch sshShell = new JSch();
        Session session = null;
        ChannelExec channel =  null;
        ChannelExec channelDetail =  null;
         
        for (SyncDataSourceConfiguration dataSourceConfiguration : syncDataSourceConfigurations) {
            try {
                String deviceId;
                int port;
                String className, host, user, password;

                if (dataSourceConfiguration.getParameters().containsKey("deviceId")) //NOI18N
                    deviceId = dataSourceConfiguration.getParameters().get("deviceId"); //NOI18N
                else {
                    res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_not_defined"), "deviceId", syncGroup.getName())));
                    continue;
                }

                if (dataSourceConfiguration.getParameters().containsKey("deviceClass")) //NOI18N
                    className = dataSourceConfiguration.getParameters().get("deviceClass"); //NOI18N
                else {
                    res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_not_defined"), "deviceClass", syncGroup.getName()))); //NOI18N
                    continue;
                }

                if (dataSourceConfiguration.getParameters().containsKey("ipAddress")) //NOI18N
                    host = dataSourceConfiguration.getParameters().get("ipAddress"); //NOI18N
                else {
                    res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_not_defined"), "ipAddress", syncGroup.getName()))); //NOI18N
                    continue;
                }

                if (dataSourceConfiguration.getParameters().containsKey("sshPort")) //NOI18N
                    port = Integer.valueOf(dataSourceConfiguration.getParameters().get("sshPort")); //NOI18N
                else {
                    res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_not_defined"), "sshPort", syncGroup.getName())));
                    continue;
                }

                if (dataSourceConfiguration.getParameters().containsKey("sshUser")) //NOI18N
                    user = dataSourceConfiguration.getParameters().get("sshUser"); //NOI18N
                else {
                    res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_not_defined"), "sshUser", syncGroup.getName()))); //NOI18N
                    continue;
                }

                if (dataSourceConfiguration.getParameters().containsKey("sshPassword")) //NOI18N
                    password = dataSourceConfiguration.getParameters().get("sshPassword"); //NOI18N
                else {
                    res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_not_defined"), "sshPassword", syncGroup.getName()))); //NOI18N
                    continue;
                }
    
                BusinessObjectLight currentObject = bem.getObjectLight(className, deviceId);
                
                session = sshShell.getSession(user, host, port);
                session.setPassword(password);
                //Enable to -not recommended- disable host key checking
                session.setConfig("StrictHostKeyChecking", "no");
//                String knownHostsFileLocation = (String)PersistenceService.getInstance().getApplicationEntityManager().
//                        getConfigurationVariableValue("sync.bdi.knownHostsFile");
//                sshShell.setKnownHosts(knownHostsFileLocation);
                session.connect(6000); //Connection timeout
                channel = (ChannelExec) session.openChannel("exec");

                String modelString = currentObject.getName().split("-")[0];
                
                switch (modelString) { //The model of the device is taken from its name. Alternatively, this could be taken from its actual "model" attribute.
                    case "ASR1002":
                    case "ASR1006":
                    case "ASR920": 
                    case "ME3600":{
                        channel.setCommand("sh mpls l2transport vc"); //NOI18N
                        channel.connect();
                        
                        MplsSyncDefaultParser parser = new MplsSyncDefaultParser();       
                        String data = readCommandExecutionResult(channel);
                        
                        List<AbstractDataEntity> parseResult = parser.parseVcs(data);
                        
                        //We must check the details of every vcid
                        for (AbstractDataEntity mplsLink : parseResult) {
                            if(((MPLSLink)mplsLink).getLocalInterface().toLowerCase().startsWith("pw") && !((MPLSLink)mplsLink).getVcId().isEmpty()){
                                session = sshShell.getSession(user, host, port);
                                session.setPassword(password);
                                //Enable to -not recommended- disable host key checking
                                session.setConfig("StrictHostKeyChecking", "no");
                                session.connect(2000); //Connection timeout
                                channelDetail = (ChannelExec) session.openChannel("exec");
                                System.out.println(((MPLSLink)mplsLink).getVcId());
                                channelDetail.setCommand("sh show mpls l2transport vc " + ((MPLSLink)mplsLink).getVcId() + " detail"); //NOI18N
                                channelDetail.connect();
                                String x = readCommandExecutionResult(channelDetail);
                                mplsLink = parser.parseVcDetails(x, (MPLSLink)mplsLink);
                            }
                        }//end for
                        
                        res.getResult().put(dataSourceConfiguration, parser.findRelatedPseudowires(parseResult));                            
                        break;
                    }
                    case "ASR9001": {
                        channel.setCommand("sh l2vpn xconnect"); //NOI18N
                        channel.connect();
                                                
                        MplsSyncASR9001Parser parser = new MplsSyncASR9001Parser();        
                        res.getResult().put(dataSourceConfiguration, parser.parseVcIds(readCommandExecutionResult(channel)));
                        break;
                    }
                    default:
                        res.getExceptions().put(dataSourceConfiguration, Arrays.asList(new InvalidArgumentException(String.format("Model %s is not supported. Check your naming conventions [ASR920-XXX, ASR1002-XXX, ASR9001-XXX, ME3600-XXX]", modelString))));
                }
            } catch (Exception ex) {
                res.getExceptions().put(dataSourceConfiguration, Arrays.asList(ex));
            } finally {
                if (session != null)
                    session.disconnect();
                if (channel != null)
                    channel.disconnect();
            }
        }
        return res;
    }

    private void readIpam(){
        try {
            List<Pool> ipv4RootPools;
            ipv4RootPools = bem.getRootPools(Constants.CLASS_SUBNET_IPV4, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
            ipv4Root = ipv4RootPools.get(0);
            readcurrentIPAMFolders(ipv4RootPools);
            readCurrentSubnets(ipv4RootPools.get(0));
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException | MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    
    @Override
    public List<SyncResult> automatedSync(PollResult pollResult) {
        subnets = new HashMap<>();
        ips = new HashMap<>();
        bem = PersistenceService.getInstance().getBusinessEntityManager();
        aem = PersistenceService.getInstance().getApplicationEntityManager();
        mem = PersistenceService.getInstance().getMetadataEntityManager();
        readIpam();
        res = new ArrayList<>();
        //First, we inject the unexpected errors
        for (SyncDataSourceConfiguration dsConfig : pollResult.getExceptions().keySet()) {
            for (Exception ex : pollResult.getExceptions().get(dsConfig))
                res.add(new SyncResult(dsConfig.getId(), 
                        SyncResult.TYPE_ERROR, String.format("Severe error while processing data source configuration %s", 
                                dsConfig.getName()), ex.getLocalizedMessage()));
        }
        
        for (SyncDataSourceConfiguration dataSourceConfiguration : pollResult.getResult().keySet()) {
            try {
                BusinessObjectLight relatedOject = bem.getObjectLight(dataSourceConfiguration.getParameters().get("deviceClass"), 
                                                        dataSourceConfiguration.getParameters().get("deviceId"));
                //we get the current interfaces physicalPorts, VirtualPorts, Pseudowires
                List<BusinessObjectLight> currentInterfaces = bem.getChildrenOfClassLightRecursive(relatedOject.getId(), relatedOject.getClassName(), "GenericPort", 0);
                currentInterfaces.addAll(bem.getChildrenOfClassLight(relatedOject.getId(), relatedOject.getClassName(), "MPLSTunnel", 0));
                //TODO check if here we have virtual ports and pw
                //check here after update of the pollResult from BusinessObjectLight to SyncDataSourceConfiguration
                List<AbstractDataEntity> mplsTransportLinks = pollResult.getResult().get(dataSourceConfiguration);
                
                if(mplsTransportLinks.isEmpty())
                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_ERROR, "No data was found", 
                                "There were an error a no data was retrieve")); 
                else{
                    //We process every entry got it from ssh mpls info
                    for (AbstractDataEntity mplsSyncEntry : mplsTransportLinks) {
                        String localInterfaceNameFromSync = ((MPLSLink)mplsSyncEntry).getLocalInterface();
                        String localIpFromSync = ((MPLSLink)mplsSyncEntry).getLocalInterfaceIp();
                        String vcIdFromSync = ((MPLSLink)mplsSyncEntry).getVcId();
                        String localInterfaceDetailFromSync = ((MPLSLink)mplsSyncEntry).getLocalInterfaceDetail();
                        String outputInterfaceFromSync = ((MPLSLink)mplsSyncEntry).getOutputInterface();
                        String serviceNameFromSync = ((MPLSLink)mplsSyncEntry).getService();
                        BusinessObjectLight currentLocalIp;

                        List<BusinessObjectLight> pseudowireRels;
                        //fisrt we deal with the the localInterfaceIp
                        if(localIpFromSync != null){
                            currentLocalIp = checkSubentsIps(localIpFromSync, "", true, dataSourceConfiguration.getId());
                            if(currentLocalIp != null){
                                List<BusinessObjectLight> relatedPorts = bem.getSpecialAttribute(currentLocalIp.getClassName(), currentLocalIp.getId(), RELATIONSHIP_IPAMHASADDRESS);
                                if(!relatedPorts.isEmpty())
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_INFORMATION, "Cheking interface related ipAddr", 
                                        String.format("ipAddr: %s is related with: %s", currentLocalIp, relatedPorts.get(0))));
                            }
                        }
                        //we search for the current interface within the device
                        BusinessObjectLight matchingLocalInterface = null;
                        for (BusinessObjectLight currentInterface : currentInterfaces) {
                            if (SyncUtil.normalizePortName(currentInterface.getName()).equals(SyncUtil.normalizePortName(localInterfaceNameFromSync))){
                                matchingLocalInterface = currentInterface;
                                break;
                            }
                        }//the interface doesn't exist but is a pw we should created
                        if(matchingLocalInterface == null && !localInterfaceNameFromSync.startsWith("pw"))
                            res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_WARNING, "Searching interface to connect MPLSLink", 
                                        String.format("Rreading vcid: %s ,Interface: %s was not found within: %s", vcIdFromSync, localInterfaceNameFromSync, relatedOject)));
                        //if there is no match with any port in the related object but is pseudowire we must created
                        else if(matchingLocalInterface == null && localInterfaceNameFromSync.startsWith("pw")) {
                            HashMap<String, String> defaultAttributes = new HashMap<>();
                            defaultAttributes.put(Constants.PROPERTY_NAME, localInterfaceNameFromSync);
                            String newPwId = bem.createObject("Pseudowire", relatedOject.getClassName(), relatedOject.getId(), defaultAttributes, null);
                            //The new pseudowire
                            matchingLocalInterface = new BusinessObjectLight("Pseudowire", newPwId, localInterfaceNameFromSync);
                            //we add the new pw to the current interfaces
                            currentInterfaces.add(matchingLocalInterface); 
                            aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s [Pseudowire] (id:%s)", localInterfaceNameFromSync, newPwId));
                            res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Creating a Pseudowire interface %s within %s", localInterfaceNameFromSync, relatedOject), 
                                        "The inteface was created successfully"));

                        }
                        else if(matchingLocalInterface != null){
                            //we must check the relationships with the interface, 
                            pseudowireRels = bem.getSpecialAttribute(matchingLocalInterface.getClassName(), matchingLocalInterface.getId(), MPLSModule.RELATIONSHIP_MPLS_PW_ISRELATEDWITH_PW);
                            //Local interface in detail, local interface in detail an local interface should be diferent 
                            BusinessObjectLight relatedInterfaceInDetail = null;
                            if(localInterfaceDetailFromSync != null && localInterfaceDetailFromSync.toLowerCase().startsWith("pw")){
                                //we search for the (Pseudowire)localInterface got it from the vcid detail in the current interfaces
                                for (BusinessObjectLight currentInterface : currentInterfaces) {
                                    if (SyncUtil.normalizePortName(currentInterface.getName()).equals(SyncUtil.normalizePortName(localInterfaceDetailFromSync))){
                                        relatedInterfaceInDetail = currentInterface;
                                        break;
                                    }
                                }//if we don't find the related pseudowire we must created it, any way it will be created later
                                if(relatedInterfaceInDetail == null){ 
                                    HashMap<String, String> defaultAttributes = new HashMap<>();
                                    defaultAttributes.put(Constants.PROPERTY_NAME, localInterfaceDetailFromSync);
                                    String newPwId = bem.createObject(Constants.CLASS_PSEUDOWIRE, relatedOject.getClassName(), 
                                            relatedOject.getId(), defaultAttributes, null);
                                    relatedInterfaceInDetail = new BusinessObjectLight(Constants.CLASS_PSEUDOWIRE, newPwId, localInterfaceDetailFromSync);

                                    aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s [Pseudowire] (id:%s)", localInterfaceNameFromSync, newPwId));
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Creating a an interface %s within: %s", localInterfaceNameFromSync, relatedOject), 
                                                "The inteface was created successfully"));
                                }
                                //if both the interface and the localInterface in detail are pseudowires we must relate them
                                if(matchingLocalInterface.getClassName().equals(Constants.CLASS_PSEUDOWIRE) && 
                                        !pseudowireRels.contains(relatedInterfaceInDetail) && 
                                        !relatedInterfaceInDetail.getId().equals(matchingLocalInterface.getId()))
                                {//we must create a relationship pseudowire bewtween the pseudowires
                                    bem.createSpecialRelationship(relatedInterfaceInDetail.getClassName(), relatedInterfaceInDetail.getId(), matchingLocalInterface.getClassName(), matchingLocalInterface.getId(), MPLSModule.RELATIONSHIP_MPLS_PW_ISRELATEDWITH_PW, true); //NOI18N
                                    aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s - pseudowire - %s", matchingLocalInterface, relatedInterfaceInDetail));
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, "Creating pesudowire relationship",
                                                        String.format("Creating pesudowire relationship between %s - %s", matchingLocalInterface, relatedInterfaceInDetail)));
                                }
                            }
                            //Output interface
                            pseudowireRels = bem.getSpecialAttribute(matchingLocalInterface.getClassName(), matchingLocalInterface.getId(), MPLSModule.RELATIONSHIP_MPLS_PSEUDOWIRE_HASOUTPUTINTERFACE);
                            BusinessObjectLight outputInterface = null;
                            if(outputInterfaceFromSync != null){//if the interface is a Pseudowire the real endpoint of the mpls is the output interface(a port) not the pseudowire
                                //we search the output interface it in the current interfaces within the realted object
                                for (BusinessObjectLight currentInterface : currentInterfaces) {
                                    if (SyncUtil.normalizePortName(currentInterface.getName()).equals(SyncUtil.normalizePortName(outputInterfaceFromSync))){
                                        outputInterface = currentInterface;
                                        break;
                                    }
                                }
                            }
                            //we must create the rel between the pseudowire and the output interface
                            if(!pseudowireRels.contains(outputInterface) && outputInterface != null && matchingLocalInterface.getClassName().equals(Constants.CLASS_PSEUDOWIRE)){
                                bem.createSpecialRelationship(outputInterface.getClassName(), outputInterface.getId(), matchingLocalInterface.getClassName(), matchingLocalInterface.getId(), MPLSModule.RELATIONSHIP_MPLS_PSEUDOWIRE_HASOUTPUTINTERFACE, true); //NOI18N
                                aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s - pseudowire - %s", outputInterface, matchingLocalInterface));
                                res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, "Creating pesudowire relationship",
                                        String.format("Creating pesudowire relationship between %s - %s", outputInterface, matchingLocalInterface)));
                            }
                            else if(outputInterfaceFromSync != null && matchingLocalInterface.getClassName().equals(Constants.CLASS_PSEUDOWIRE))
                                res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_ERROR, "Searching outputInterface",
                                        String.format("No output interface was found", outputInterfaceFromSync)));
                            
                        //VC-IDS 
                            //All vcid in inventory
                            List<BusinessObject> currentMplsLinks = bem.getObjectsOfClass(Constants.CLASS_MPLSLINK, -1);
                            BusinessObjectLight currentVcId = null;
                            for(BusinessObjectLight currentMplsLink : currentMplsLinks){
                                if(currentMplsLink.getName().toLowerCase().equals("vc " + vcIdFromSync) || currentMplsLink.getName().contains(vcIdFromSync)){
                                    currentVcId = currentMplsLink;
                                    break;
                                }
                            }
                            //we search for the current vcIds created and related within the device that we are sync 
                            List<BusinessObjectLight> currentMplsLinksRelated = bem.getSpecialAttribute(relatedOject.getClassName(), relatedOject.getId(), MPLSModule.RELATIONSHIP_MPLSLINK);
                            BusinessObjectLight currentVcIdRelated = null;
                            for(BusinessObjectLight currentMplsLinkRelated : currentMplsLinksRelated){
                                if(currentMplsLinkRelated.getName().toLowerCase().equals("vc " + vcIdFromSync) || currentMplsLinkRelated.getName().contains(vcIdFromSync)){
                                    currentVcIdRelated = currentMplsLinkRelated;
                                    break;
                                }
                            }
                            
                            //now the vc id and its connections
                            String relSide = null;
                            BusinessObjectLight endpointA = null, endpointB = null;
                            boolean sideA = false, sideB = false; //used to check wich side of the link needs to be connected to the destiny pseudowire
                            //we found a VCid in a different device
                            
                            
//                            if(currentVcId != null && currentVcRelatedId != null && currentVcId.getId().equals(currentVcRelatedId.getId())){
//                                
//                            
//                            
//                            }



                            //we check if the localinterface has already relationships with interface in detail or the output interface 
                            if(currentVcId == null){ //We must create the MPLSLink, if doesn't exists
                                HashMap<String, String> attributesToBeSet = new HashMap<>();
                                attributesToBeSet.put(Constants.PROPERTY_NAME, "VC " + vcIdFromSync);
                                //First we create the mpls link, the name is the vcId
                                String newMplsLinkId = bem.createSpecialObject("MPLSLink", null, "-1", attributesToBeSet, null); //NOI18N
                                BusinessObjectLight newMplsLink = new BusinessObjectLight("MPLSLink", newMplsLinkId, vcIdFromSync); //NOI18N
                                bem.createSpecialRelationship(relatedOject.getClassName(), relatedOject.getId(), "MPLSLink", newMplsLinkId, RELATIONSHIP_MPLSLINK, false); //NOI18N
                                //the RelatedObject need to be relat4ed with the mpls link anyway
                                aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s - mplsLink - %s", relatedOject, newMplsLink));
                                res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("A new created %s was related with %s", newMplsLink, relatedOject), 
                                                        "The inteface was created successfully"));
                                //pseudowires needs special treatment
                                if(matchingLocalInterface.getClassName().equals("Pseudowire") && outputInterface != null && !outputInterface.getClassName().equals("MPLSTunnel")){
                                        bem.createSpecialRelationship("MPLSLink", newMplsLinkId, outputInterface.getClassName(), outputInterface.getId(), RELATIONSHIP_MPLSENDPOINTA, true); //NOI18N
                                        aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s endpointA connected with %s", newMplsLink, outputInterface));
                                        res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, "Connecting MPLSLink with local interface", 
                                                            String.format("The  endpointA of %s was connected to: %s", newMplsLink, outputInterface))); 
                                }//end if pseudowires then we relate the mpls link with the interface
                                else if(mem.isSubclassOf(Constants.CLASS_GENERICPORT, matchingLocalInterface.getClassName())){
                                    //Because is a new mplslink we always connect the endpointA
                                    bem.createSpecialRelationship("MPLSLink", newMplsLinkId, matchingLocalInterface.getClassName(), matchingLocalInterface.getId(), RELATIONSHIP_MPLSENDPOINTA, true); //NOI18N
                                    aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s endpointA connected with %s", newMplsLink, matchingLocalInterface));
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_ERROR, "Connecting MPLSLink with local interface", 
                                                        String.format("The  endpointA of %s was connected to: %s", newMplsLink, matchingLocalInterface))); 
                                }
                                if(serviceNameFromSync != null)
                                    checkServices(serviceNameFromSync, newMplsLinkId, vcIdFromSync, dataSourceConfiguration.getId()); 
                            }//end if vcid is null
                            else{ //the vcid exists
                                boolean isVcidUnique = true;
                                boolean hasRelationWithVcId = false;
                                List<BusinessObjectLight> relatedObjects = bem.getSpecialAttribute(currentVcId.getClassName(), currentVcId.getId(), MPLSModule.RELATIONSHIP_MPLSLINK);
                                for(BusinessObjectLight objs : relatedObjects){
                                    if(objs.getId().equals(relatedOject.getId())){
                                       hasRelationWithVcId = true;
                                       break;
                                    }
                                }
                                if(currentVcIdRelated != null && currentVcIdRelated.getName().equals(currentVcId.getName()) && 
                                        !currentVcId.getId().equals(currentVcIdRelated.getId())){
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_ERROR, 
                                                String.format("Two MPLSLin has the same name VC %s", vcIdFromSync),
                                                String.format("MPLS sync can not be done in MPLSLinks with repeated names")));
                                        isVcidUnique = false;
                                }
                                
                                if(isVcidUnique){
                                    //if the vcid is created but not related we need to create the rel with the device
                                    if(!hasRelationWithVcId)
                                        bem.createSpecialRelationship(relatedOject.getClassName(), relatedOject.getId(), currentVcId.getClassName(), currentVcId.getId(), RELATIONSHIP_MPLSLINK, false); //NOI18N
                                    
                                    List<BusinessObjectLight> endpointARelationship = bem.getSpecialAttribute(currentVcId.getClassName(), currentVcId.getId(), "mplsEndpointA");
                                    if (!endpointARelationship.isEmpty()) 
                                        endpointA = endpointARelationship.get(0);
                                    List<BusinessObjectLight> endpointBRelationship = bem.getSpecialAttribute(currentVcId.getClassName(), currentVcId.getId(), "mplsEndpointB");
                                    if (!endpointBRelationship.isEmpty()) 
                                        endpointB = endpointBRelationship.get(0);

                                    if(endpointA != null && endpointA.getId().equals(matchingLocalInterface.getId()) || endpointA != null && outputInterface != null && endpointA.getId().equals(outputInterface.getId())){
                                        res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_INFORMATION, "Checking MPLSLink endpoints",
                                           String.format("%s exists and one of its sides is connected with %s", currentVcId, matchingLocalInterface)));
                                        sideA = true;
                                    }
                                    else if(endpointB != null && endpointB.getId().equals(matchingLocalInterface.getId()) || endpointB != null && outputInterface != null && endpointB.getId().equals(outputInterface.getId())){
                                        res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_INFORMATION, "Checking MPLSLink endpoints",
                                           String.format("%s exists and one of it sides is connected with %s", currentVcId, matchingLocalInterface)));
                                        sideB = true;
                                    }

                                    if(!sideA && !sideB && endpointA != null)//no side of the mpls has the right relationships so nothing can be done
                                        res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_WARNING, String.format("%s exists but non of its endpoints are connected with %s", currentVcId, matchingLocalInterface),
                                           String.format("%s not of its endpoints will be modify, please release at least one endpoint, o related manually with %s", currentVcId, matchingLocalInterface)));

                                    if(endpointA == null && !sideB)//something connected but belongs to other object, so we connect the sideB that is free
                                        relSide = RELATIONSHIP_MPLSENDPOINTA;
                                    else if(endpointB == null && !sideA) //something connected but belongs to other object, so we connect the sideB that is free
                                        relSide = RELATIONSHIP_MPLSENDPOINTB;

                                    if(relSide != null){
                                        if(matchingLocalInterface.getClassName().equals(Constants.CLASS_PSEUDOWIRE) && outputInterface != null && !outputInterface.getClassName().equals("MPLSTunnel")){
                                                //second relationship between the output interface and the mplsLink, because is a new mplslink we always connect the endpointA
                                                bem.createSpecialRelationship(currentVcId.getName(), currentVcId.getId(), outputInterface.getClassName(), outputInterface.getId(), relSide, true); //NOI18N
                                                aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s endpointA connected with %s", currentVcId, outputInterface));
                                                res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, "Connecting MPLSLink with local interface", 
                                                                    String.format("The %s was connected to: %s", currentVcId, outputInterface))); 
                                        }//end dealing with pseudowires
                                        else if(mem.isSubclassOf(Constants.CLASS_GENERICPORT, matchingLocalInterface.getClassName())){
                                            bem.createSpecialRelationship(currentVcId.getClassName(), currentVcId.getId(), matchingLocalInterface.getClassName(), matchingLocalInterface.getId(), relSide, true); //NOI18N
                                            aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s - %s", currentVcId, matchingLocalInterface));
                                            res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, "Connecting MPLSLink with local interface", 
                                                                    String.format("The %s was connected to: %s", currentVcId, matchingLocalInterface))); 
                                        }
                                    }
                                }//end for vc id unique
                                if(serviceNameFromSync != null)
                                    checkServices(serviceNameFromSync, currentVcId.getId(), currentVcId.getName(), dataSourceConfiguration.getId()); 
                            }//end if interface is not null
                        }//end else inteface was found in the sync device
                    }//end for iterate over mpls links got it from ssh
                }
            } catch (InventoryException ex) {
                res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_ERROR, "MPLS Information Processing", ex.getLocalizedMessage()));
            }
        }
        
        return res;
    }
    
    /**
     * Checks if a given service name exists in kuwaiba in order to related with 
     * some resource got it form the sync
     * @param vcName the service read it form the  if-mib
     * @param vcId the ip address id of the resource created
     * @throws ApplicationObjectNotFoundException
     * @throws BusinessObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     * @throws OperationNotPermittedException 
     */
    private void checkServices(String serviceName, String vcId, String vc, long dsConfigId){
        try{
            List<BusinessObjectLight> servicesCreatedInKuwaiba = new ArrayList<>();
            //We get the services created in kuwaiba
            List<Pool> serviceRoot = bem.getRootPools(Constants.CLASS_GENERICCUSTOMER, 2, false);
            for(Pool customerPool: serviceRoot){
                //TelecoOperators
                List<BusinessObjectLight> poolItems = bem.getPoolItems(customerPool.getId(), -1);
                for(BusinessObjectLight telecoOperator : poolItems){
                    List<Pool> poolsInObject = bem.getPoolsInObject(telecoOperator.getClassName(), telecoOperator.getId(), "GenericService");
                    //Service Pool
                    for(Pool servicePool : poolsInObject){
                        List<BusinessObjectLight> actualServices = bem.getPoolItems(servicePool.getId(), -1);
                        actualServices.forEach((actualService) -> {
                            servicesCreatedInKuwaiba.add(actualService);
                        });
                    }
                }
            }
            boolean related = false;
            //Now we check the resources with the given serviceName or ifAlias
            for(BusinessObjectLight currentService : servicesCreatedInKuwaiba){
                //The service is already created in kuwaiba
                if(!currentService.getName().isEmpty() && 
                        (serviceName.equals(currentService.getName()) || serviceName.toLowerCase().contains(currentService.getName().toLowerCase()))){
                    List<BusinessObjectLight> serviceResources = bem.getSpecialAttribute(currentService.getClassName(), currentService.getId(), "uses");
                    for (BusinessObjectLight resource : serviceResources) {
                        if(resource.getId().equals(vcId)){ //The port is already a resource of the service
                            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_INFORMATION,
                                    "Searching service",
                                    String.format("The service: %s is related with the: %s ", serviceName, vc)));
                            related = true;
                            break;
                        }
                    }//end for search for ip in the resources
                    if(!related){
                        bem.createSpecialRelationship(currentService.getClassName(), currentService.getId(), Constants.CLASS_IP_ADDRESS, vcId, "uses", true);
                        related = true;
                        res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                "Searching service",
                                String.format("The service: %s was related with the: %s ", serviceName, vc)));
                    }
                }
            }//end for
            if(!related)
                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_WARNING, 
                        "Searching service", String.format("The service: %s Not found, : %s will not be related", serviceName, vc)));
            
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
                res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, 
                        String.format("Serching service %s, related with: %s ", serviceName, vc),
                        String.format("due to: %s ", ex.getLocalizedMessage())));
        } catch (InvalidArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public List<SyncResult> automatedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support automated sync for unmapped pollings");
    }
    
    @Override
    public List<SyncFinding> supervisedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support supervised sync");
    }
    
    @Override
    public List<SyncFinding> supervisedSync(PollResult pollResult) {
        throw new UnsupportedOperationException("This provider does not support supervised sync");
    }

    @Override
    public List<SyncResult> finalize(List<SyncAction> actions) {
        throw new UnsupportedOperationException("This provider does not support this operation"); //Not used for now
    }

    /**
     * Reads the channel's input stream into a string.
     * @param channel The session's channel.
     * @return The string with the result of the command execution.
     * @throws InvalidArgumentException if there was an error executing the command or reading its result.
     */
    private String readCommandExecutionResult (ChannelExec channel) {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(channel.getInputStream()))) {
            String result = buffer.lines().collect(Collectors.joining("\n"));
            return channel.getExitStatus() == 0 ? result : null;
        } catch (IOException ex) {
            Logger.getLogger(MplsSyncProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
    * Reads the current folders in the IPAM 
    * @param ifName a given name for port, virtual port or MPLS Tunnel
    * @return the object, null doesn't exists in the current structure
    */
    private void readcurrentIPAMFolders(List<Pool> folders) throws ApplicationObjectNotFoundException,
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
     * Search for a given IP address got it from the ipAddrTableMIB data
     * if doesn't exists it will be created
     * @param ipAddr the ip address
     * @param syncMask the ip address mask from sync
     * @return an IP address created in kuwaiba
     */
    private BusinessObjectLight checkSubentsIps(String ipAddr, String syncMask, boolean createSubnetIp, long dsConfigId){
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
        if(createSubnetIp && currentSubnet == null)
            currentSubnet = createSubnet(newSubnet, dsConfigId);
        
        //with the subnet found we must search if the Ip address exists
        List<BusinessObjectLight> currentIps = ips.get(currentSubnet);
        //we found the subnet but has no IPs so we create the ip
        if(currentIps != null && !currentIps.isEmpty()){
            for (BusinessObjectLight currentIpLight : currentIps) {
                if(currentIpLight.getName().equals(ipAddr)){
                    try {//we must check the mask if the IP already exists and if its attributes are updated
                        BusinessObject currentIp = bem.getObject(currentIpLight.getClassName(), currentIpLight.getId());
                        String oldMask = currentIp.getAttributes().get(Constants.PROPERTY_MASK);
                        if(!oldMask.equals(syncMask)){
                            currentIp.getAttributes().put(Constants.PROPERTY_MASK, syncMask);
                            bem.updateObject(currentIp.getClassName(), currentIp.getId(), currentIp.getAttributes());
                            //AuditTrail
                            aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT, String.format("%s (id:%s)", currentIp.toString(), currentIp.getId()));
            
                            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, String.format("Updating the mask of %s", currentIp), String.format("From: %s to: %s", oldMask, syncMask)));
                        }
                        return currentIpLight;
                    } catch (ApplicationObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                        res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,  String.format("Updating the mask of ipAddr: %s", currentIpLight), ex.getLocalizedMessage()));
                    }
                }
            }
        }//we create the ip address if doesn't exists in the current subnet
        if(createSubnetIp) 
            return createIp(currentSubnet, ipAddr, syncMask, dsConfigId);
        else
            return null;
    }
    
    /**
     * Creates a new subnet
     * @param newSubnet a given subnet name
     * @return the created subnet
     */
    private BusinessObjectLight createSubnet(String newSubnet, long dsConfigId){
        BusinessObjectLight currentSubnet = null;
        String [] attributeNames = {"name", "description", "networkIp", "broadcastIp", "hosts"};
        String [] attributeValues = {newSubnet + ".0/24", "created with sync", newSubnet + ".0", newSubnet + ".255", "254"};
        try {
            currentSubnet = bem.getObject(Constants.CLASS_SUBNET_IPV4, bem.createPoolItem(ipv4Root.getId(), 
                    ipv4Root.getClassName(), attributeNames, attributeValues, null));
            //AuditTrail
            aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s (id:%s)", currentSubnet.toString(), currentSubnet.getId()));
            
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
    private BusinessObject createIp(BusinessObjectLight subnet, String ipAddr, String syncMask, long dsConfigId){
        BusinessObject createdIp = null;
        HashMap<String, String> ipAttributes = new HashMap<>();
        ipAttributes.put(Constants.PROPERTY_NAME, ipAddr);
        ipAttributes.put(Constants.PROPERTY_DESCRIPTION, "Created with sync");
        ipAttributes.put(Constants.PROPERTY_MASK, syncMask); //TODO set the list types attributes
        try { 
            String newIpAddrId = bem.createSpecialObject(Constants.CLASS_IP_ADDRESS, subnet.getClassName(), subnet.getId(), ipAttributes, null);
            //AuditTrail
            aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s [IPAddress] (%s)", ipAddr, newIpAddrId));
            
            createdIp = bem.getObject(Constants.CLASS_IP_ADDRESS, newIpAddrId);
            ips.get(subnet).add(createdIp);
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS, "Add IP to Subnet", String.format("IP address %s was successfully added to subnet %s", ipAddr, subnet)));
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
            res.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, 
                        String.format("Adding IP address %s to subnet %s", ipAddr, subnet), 
                        ex.getLocalizedMessage()));
        }
        return createdIp;
    }
    
}
