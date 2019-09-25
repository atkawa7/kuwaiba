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

package com.neotropic.kuwaiba.sync.connectors.ssh.bdi;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.neotropic.kuwaiba.sync.connectors.ssh.bdi.entities.BridgeDomain;
import com.neotropic.kuwaiba.sync.connectors.ssh.bdi.entities.NetworkInterface;
import com.neotropic.kuwaiba.sync.connectors.ssh.bdi.parsers.BridgeDomainsASR1002Parser;
import com.neotropic.kuwaiba.sync.connectors.ssh.bdi.parsers.BridgeDomainsASR1006Parser;
import com.neotropic.kuwaiba.sync.connectors.ssh.bdi.parsers.BridgeDomainsASR9001Parser;
import com.neotropic.kuwaiba.sync.connectors.ssh.bdi.parsers.BridgeDomainsASR920Parser;
import com.neotropic.kuwaiba.sync.connectors.ssh.bdi.parsers.BridgeDomainsME3600Parser;
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
import java.util.stream.Collectors;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.util.i18n.I18N;

/**
 * This provider connects to Cisco routers via SSH, retrieves the bridge domain configuration, and creates/updates the relationships between
 * the bridge domains and the logical/physical 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class BridgeDomainSyncProvider extends AbstractSyncProvider {

    @Override
    public String getDisplayName() {
        //Bridge Domains and Bridge Domain Interfaces Sync Provider
        return "Bridge Domains";
    }

    @Override
    public String getId() {
        return BridgeDomainSyncProvider.class.getName();
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
        List<SyncDataSourceConfiguration> syncDataSourceConfigurations = syncGroup.getSyncDataSourceConfigurations();
        
        PollResult res = new PollResult();
        BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();

        JSch sshShell = new JSch();
        Session session = null;
        ChannelExec channel =  null;
        
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
                session.connect(10000); //Connection timeout
                channel = (ChannelExec) session.openChannel("exec");

                String modelString = currentObject.getName().split("-")[0];
                
                switch (modelString) { //The model of the device is identified from its name. Alternatively, this could be taken from its actual "model" attribute, but this implementation takes the quickest approach.
                    case "ASR920": {
                        channel.setCommand("sh bridge-domain"); //NOI18N
                        channel.connect();
                        
                        BridgeDomainsASR920Parser parser = new BridgeDomainsASR920Parser();               
                        res.getResult().put(dataSourceConfiguration, 
                                parser.parse(readCommandExecutionResult(channel)));                            
                        break;
                    }
                    case "ASR1002": {
                        channel.setCommand("sh bridge-domain"); //NOI18N
                        channel.connect();
                        
                        BridgeDomainsASR1002Parser parser = new BridgeDomainsASR1002Parser();               

                        res.getResult().put(dataSourceConfiguration, 
                                parser.parse(readCommandExecutionResult(channel)));
                        break;
                    }
                    case "ASR1006": {
                        channel.setCommand("sh bridge-domain"); //NOI18N
                        channel.connect();
                        
                        BridgeDomainsASR1006Parser parser = new BridgeDomainsASR1006Parser();               

                        res.getResult().put(dataSourceConfiguration, 
                                parser.parse(readCommandExecutionResult(channel)));
                        break;
                    }
                    case "ASR9001": {
                        channel.setCommand("sh l2vpnxconnect"); //NOI18N
                        channel.connect();
                        
                        BridgeDomainsASR9001Parser parser = new BridgeDomainsASR9001Parser();               

                        res.getResult().put(dataSourceConfiguration, 
                                parser.parse(readCommandExecutionResult(channel)));
                        break;
                    }
                    case "ME3600": {
                        channel.setCommand("sh bridge-domain"); //NOI18N
                        channel.connect();
                        BridgeDomainsME3600Parser parser = new BridgeDomainsME3600Parser();               

                        res.getResult().put(dataSourceConfiguration, 
                                parser.parse(readCommandExecutionResult(channel)));
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

    @Override
    public List<SyncResult> automatedSync(PollResult pollResult) {
        List<SyncResult> res = new ArrayList<>();
        BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
        ApplicationEntityManager aem = PersistenceService.getInstance().getApplicationEntityManager();
        
        //First, we inject the unexpected errors
        for (SyncDataSourceConfiguration dsConfig : pollResult.getExceptions().keySet()) {
            for (Exception ex : pollResult.getExceptions().get(dsConfig))
                res.add(new SyncResult(dsConfig.getId(), 
                        SyncResult.TYPE_ERROR, String.format("Severe error while processing data source configuration %s", 
                                dsConfig.getName()), ex.getLocalizedMessage()));
        }
        
        for (SyncDataSourceConfiguration dataSourceConfiguration : pollResult.getResult().keySet()) {
            try {
                BusinessObjectLight relatedOject = new BusinessObjectLight(
                                                        dataSourceConfiguration.getParameters().get("deviceClass"), 
                                                        dataSourceConfiguration.getParameters().get("deviceId"), 
                                                        "");
                //The bridge domains in Kuwaiba in the synchronized element
                List<BusinessObjectLight> existingBridgeDomains = bem.getSpecialChildrenOfClassLight(relatedOject.getId(), 
                        relatedOject.getClassName(), "BridgeDomain", -1);
                //The bridge domains found in the real device
                List<AbstractDataEntity> bridgeDomainsInDevice = pollResult.getResult().get(dataSourceConfiguration);
                //The VFIs found the device
                List<BusinessObjectLight> currentVFIs = bem.getChildrenOfClassLight(relatedOject.getId(), relatedOject.getClassName(), "VFI", 0);
                
                for (AbstractDataEntity bridgeDomainInDevice : bridgeDomainsInDevice) { //First we check if the bridge domains exists within the device. If they do not, they will be created, if they do, we will check the interfaces
                    BusinessObjectLight matchingBridgeDomain = null;
                    List<BusinessObjectLight> bridgeDomainInterfaces = null; //These objects are retrieved lazily
                    List<BusinessObjectLight> physicalInterfaces = null; //These objects are retrieved lazily
                    
                    for (BusinessObjectLight existingBridgeDomain : existingBridgeDomains) {
                        if (existingBridgeDomain.getName().equals(((BridgeDomain)bridgeDomainInDevice).getName())) {
                            res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_INFORMATION, String.format("Check if Bridge Domain %s exists within %s", existingBridgeDomain, relatedOject), 
                                    "The Bridge Domain already exists and was not modified"));
                            matchingBridgeDomain = existingBridgeDomain;
                            break;
                        }
                    }
                    
                    if (matchingBridgeDomain == null) {
                        HashMap<String, String> defaultAttributes = new HashMap<>();
                        defaultAttributes.put(Constants.PROPERTY_NAME, bridgeDomainInDevice.getName());
                        String newBridgeDomain = bem.createSpecialObject("BridgeDomain", relatedOject.getClassName(), 
                                relatedOject.getId(), defaultAttributes, null);
                        aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s [BridgeDomain] (%s)", bridgeDomainInDevice.getName(), newBridgeDomain));
                        res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Check if Bridge Domain %s exists within %s", bridgeDomainInDevice.getName(), relatedOject), 
                                    "The Bridge Domain did not exist and was created successfully"));
                        
                        matchingBridgeDomain = new BusinessObjectLight("BridgeDomain", newBridgeDomain, bridgeDomainInDevice.getName());
                        existingBridgeDomains.add(matchingBridgeDomain);
                        bridgeDomainInterfaces = new ArrayList<>();
                    }
                    
                    //Now we check if the network interfaces exist and relate them if necessary
                    for (NetworkInterface networkInterface : ((BridgeDomain)bridgeDomainInDevice).getNetworkInterfaces()) {
                        if (networkInterface.getNetworkInterfaceType() == NetworkInterface.TYPE_VFI) { //The VFI are not created automatically in this provider
                            BusinessObjectLight matchingVFI = null;
                            if(networkInterface.getName() != null){
                                for (BusinessObjectLight vfi : currentVFIs) {
                                    if(vfi.getName().equals(networkInterface.getName())){
                                        matchingVFI = vfi;
                                        break;
                                    }
                                }
                                //The VFI doesn't exists so it must be created
                                if(matchingVFI == null){
                                    HashMap<String, String> defaultAttributes = new HashMap<>();
                                    defaultAttributes.put(Constants.PROPERTY_NAME, networkInterface.getName());
                                    String newVfiId =  bem.createSpecialObject("VFI", relatedOject.getClassName(), relatedOject.getId(), defaultAttributes, null);
                                    matchingVFI = new BusinessObjectLight("VFI", newVfiId, networkInterface.getName());
                                    currentVFIs.add(matchingVFI);
                                    aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s [VFI] (%s)", networkInterface.getName(), matchingVFI.getId()));
                                    
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, 
                                            String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("The VFI %s did not exist and was created.", networkInterface.getName())));
                                }
                                //maybe it was creadted it MPLS sync, now with must create the relationship if doen't exists.
                                List<BusinessObjectLight> relatedBDs = bem.getSpecialAttribute(matchingVFI.getClassName(), matchingVFI.getId(), "networkHasBridgeInterface");
                                
                                if(!relatedBDs.contains(matchingBridgeDomain)){
                                    bem.createSpecialRelationship("BridgeDomain", matchingBridgeDomain.getId(), "VFI", matchingVFI.getId(), "networkHasBridgeInterface", false);
                                   
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("The VFI %s was related it with the Bridge Domain.", networkInterface.getName())));
                                    aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                                        String.format("%s [BridgeDomainInterface] (%s)", networkInterface.getName(), matchingVFI.getId()));
                                }
                            }
                            continue;
                        }
                        
                        if (networkInterface.getNetworkInterfaceType() == NetworkInterface.TYPE_BDI) {                            
                            if (bridgeDomainInterfaces == null) 
                                bridgeDomainInterfaces = bem.getObjectsOfClassLight("BridgeDomainInterface", -1); //We assume that there are not multiple BDIs with the same name 
                            
                            BusinessObjectLight matchingBridgeDomainInterface = null;
                            for (BusinessObjectLight bridgeDomainInterface : bridgeDomainInterfaces) {
                                if (bridgeDomainInterface.getName().equals(networkInterface.getName())) {
                                    matchingBridgeDomainInterface = bridgeDomainInterface;
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_INFORMATION, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("BDI %s already exists. No changes were made", networkInterface.getName())));
                                    break;
                                }
                            }
                            
                            if (matchingBridgeDomainInterface == null) {
                                HashMap<String, String> defaultAttributes = new HashMap<>();
                                defaultAttributes.put(Constants.PROPERTY_NAME, networkInterface.getName());
                                String newBridgeDomainInterfaceId = bem.createHeadlessObject("BridgeDomainInterface", defaultAttributes, null);
                                bem.createSpecialRelationship(relatedOject.getClassName(), relatedOject.getId(), "BridgeDomain", matchingBridgeDomain.getId(), "networkHasBridgeInterface", false);
                                
                                bridgeDomainInterfaces.add(new BusinessObjectLight("BridgeDomainInterface", newBridgeDomainInterfaceId, networkInterface.getName()));
                                
                                res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("The BDI %s did not exist and was created.", networkInterface.getName())));
                                aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                                        String.format("%s [BridgeDomainInterface] (%s)", networkInterface.getName(), newBridgeDomainInterfaceId));
                            }
                            continue;
                        }
                        
                        if (networkInterface.getNetworkInterfaceType() == NetworkInterface.TYPE_SERVICE_INSTANCE) {
                            String[] interfaceNameTokens = networkInterface.getName().replace(" (split-horizon)", "").split(" "); //The interface name would look like this: GigabitEthernet0/0/2 service instance 10
                                                                                                                                  //Some entries have an extra " (split-horizon)" at the end that can be discarded
                            if (physicalInterfaces == null)
                                physicalInterfaces = bem.getChildrenOfClassLightRecursive(relatedOject.getId(), 
                                        relatedOject.getClassName(), "GenericCommunicationsPort", -1);
                            
                            BusinessObjectLight matchingPhysicalInterface = null;
                            String standardName = SyncUtil.normalizePortName(interfaceNameTokens[0]);
                            for (BusinessObjectLight physicalInterface : physicalInterfaces) {
                                if (physicalInterface.getName().equals(standardName)) { //Checks for the extended and the condensed interface name formats (GigabitEthernetXXX vs GiXXXX)
                                    matchingPhysicalInterface = physicalInterface;
                                    break;
                                }
                            }
                            
                            if (matchingPhysicalInterface == null) 
                                res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_ERROR, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("The physical interface %s was not found. The service instance %s will not be created nor related to the bridge domain", standardName, networkInterface.getName())));
                            else {
                                List<BusinessObjectLight> serviceInstances = bem.getChildrenOfClassLight(matchingPhysicalInterface.getId(), 
                                        matchingPhysicalInterface.getClassName(), Constants.CLASS_SERVICE_INSTANCE, -1);
                                
                                BusinessObjectLight matchingServiceInstance = null;
                                for (BusinessObjectLight serviceInstace : serviceInstances) {
                                    if (serviceInstace.getName().equals(interfaceNameTokens[interfaceNameTokens.length - 1])) {
                                        matchingServiceInstance = serviceInstace;
                                        break;
                                    }
                                }
                                
                                if (matchingServiceInstance == null) {
                                    HashMap<String, String> defaultAttributes = new HashMap<>();
                                    defaultAttributes.put(Constants.PROPERTY_NAME, interfaceNameTokens[interfaceNameTokens.length - 1]);
                                    String newServiceInstance = bem.createObject(Constants.CLASS_SERVICE_INSTANCE, matchingPhysicalInterface.getClassName(), matchingPhysicalInterface.getId(), 
                                            defaultAttributes, null);
                                    
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("Service Instance %s did not exist and was created.", networkInterface.getName())));
                                    
                                    matchingServiceInstance = new BusinessObjectLight(Constants.CLASS_SERVICE_INSTANCE, newServiceInstance, interfaceNameTokens[interfaceNameTokens.length - 1]);
                                } else
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_INFORMATION, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("Service Instance %s already exists. No changes were made.", matchingServiceInstance.getName())));
                                
                                List<BusinessObjectLight> relatedBridgeDomain = bem.getSpecialAttribute(matchingServiceInstance.getClassName(), matchingServiceInstance.getId(), "networkHasBridgeInterface");
                                if (relatedBridgeDomain.isEmpty()) {
                                    bem.createSpecialRelationship("BridgeDomain", matchingBridgeDomain.getId(), Constants.CLASS_SERVICE_INSTANCE, matchingServiceInstance.getId(), "networkHasBridgeInterface", true);
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                            String.format("Service instace %s was successfully related to the bridge domain %s", matchingServiceInstance.getName(), matchingBridgeDomain.getName())));
                                } else {
                                    if (relatedBridgeDomain.get(0).getId() != null && 
                                        matchingBridgeDomain.getId() != null &&
                                        relatedBridgeDomain.get(0).getId().equals(matchingBridgeDomain.getId()))
                                        res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_INFORMATION, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                            String.format("Service instace %s is already related to bridge domain %s. No changes were made.", matchingServiceInstance.getName(), matchingBridgeDomain.getName())));
                                    else {
                                        bem.releaseRelationships(matchingServiceInstance.getClassName(), matchingServiceInstance.getId(), Arrays.asList("networkHasBridgeInterface"));
                                        bem.createSpecialRelationship("BridgeDomain", matchingBridgeDomain.getId(), Constants.CLASS_SERVICE_INSTANCE, matchingServiceInstance.getId(), "networkHasBridgeInterface", true);
                                        
                                        res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s", bridgeDomainInDevice.getName()), 
                                            String.format("Service instace %s was related to bridge domain %s, but the relationship was changed to bridge domain %s", matchingServiceInstance.getName(), 
                                                    relatedBridgeDomain.get(0).getName(), matchingBridgeDomain.getName())));
                                    }
                                }
                            }
                            continue;
                        }
                        
                        if (networkInterface.getNetworkInterfaceType() == NetworkInterface.TYPE_GENERIC_SUBINTERFACE) {
                            String[] interfaceNameTokens = networkInterface.getName().replace(" (split-horizon)", "").split(" "); //The interface name would look like this: GigabitEthernet0/0/2 10
                                                                                                                                  //Some entries have an extra " (split-horizon)" at the end that can be discarded
                            if (physicalInterfaces == null)
                                physicalInterfaces = bem.getChildrenOfClassLightRecursive(relatedOject.getId(), 
                                        relatedOject.getClassName(), "GenericCommunicationsPort", -1);
                            
                            BusinessObjectLight matchingPhysicalInterface = null;
                            String standardName = SyncUtil.normalizePortName(interfaceNameTokens[0]);
                            for (BusinessObjectLight physicalInterface : physicalInterfaces) {
                                if (physicalInterface.getName().equals(standardName)) { //Checks for the extended and the condensed interface name formats (GigabitEthernetXXX vs GiXXXX)
                                    matchingPhysicalInterface = physicalInterface;
                                    break;
                                }
                            }
                            
                            if (matchingPhysicalInterface == null) 
                                res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_ERROR, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("The physical interface %s was not found. The subinterface %s will not be created nor related to the bridge domain", standardName, networkInterface.getName())));
                            else {
                                List<BusinessObjectLight> virtualPorts = bem.getChildrenOfClassLight(matchingPhysicalInterface.getId(), 
                                        matchingPhysicalInterface.getClassName(), Constants.CLASS_VIRTUALPORT, -1);
                                
                                BusinessObjectLight matchingVirtualPort = null;
                                for (BusinessObjectLight virtualPort : virtualPorts) {
                                    if (virtualPort.getName().equals(interfaceNameTokens[interfaceNameTokens.length - 1])) {
                                        matchingVirtualPort = virtualPort;
                                        break;
                                    }
                                }
                                
                                if (matchingVirtualPort == null) {
                                    HashMap<String, String> defaultAttributes = new HashMap<>();
                                    defaultAttributes.put(Constants.PROPERTY_NAME, interfaceNameTokens[interfaceNameTokens.length - 1]);
                                    String newVirtualPort = bem.createObject(Constants.CLASS_VIRTUALPORT, matchingPhysicalInterface.getClassName(), matchingPhysicalInterface.getId(), 
                                            defaultAttributes, null);
                                    
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("Subinterface %s did not exist and was created.", networkInterface)));
                                    
                                    matchingVirtualPort = new BusinessObjectLight(Constants.CLASS_VIRTUALPORT, newVirtualPort, interfaceNameTokens[interfaceNameTokens.length - 1]);
                                } else
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_INFORMATION, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("Subinterface %s already exists. No changes were made.", matchingVirtualPort)));
                                
                                List<BusinessObjectLight> relatedBridgeDomain = bem.getSpecialAttribute(matchingVirtualPort.getClassName(), matchingVirtualPort.getId(), "networkHasBridgeInterface");
                                if (relatedBridgeDomain.isEmpty()) {
                                    bem.createSpecialRelationship("BridgeDomain", matchingBridgeDomain.getId(), Constants.CLASS_VIRTUALPORT, matchingVirtualPort.getId(), "networkHasBridgeInterface", true);
                                    res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                            String.format("Subinterface %s was successfully related to the bridge domain %s", matchingVirtualPort, matchingBridgeDomain.getName())));
                                } else {
                                    if (relatedBridgeDomain.get(0).getId() != null &&
                                        matchingBridgeDomain.getId() != null &&
                                        relatedBridgeDomain.get(0).getId().equals(matchingBridgeDomain.getId()))
                                        res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_INFORMATION, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                            String.format("Subinterface %s is already related to bridge domain %s. No changes were made.", matchingVirtualPort, matchingBridgeDomain.getName())));
                                    else {
                                        bem.releaseRelationships(matchingVirtualPort.getClassName(), matchingVirtualPort.getId(), Arrays.asList("networkHasBridgeInterface"));
                                        bem.createSpecialRelationship("BridgeDomain", matchingBridgeDomain.getId(), Constants.CLASS_VIRTUALPORT, matchingVirtualPort.getId(), "networkHasBridgeInterface", true);
                                        
                                        res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s", bridgeDomainInDevice.getName()), 
                                            String.format("Subinterface %s was related to bridge domain %s, but the relationship was changed to bridge domain %s", matchingVirtualPort, 
                                                    relatedBridgeDomain.get(0).getName(), matchingBridgeDomain.getName())));
                                    }
                                }
                            }
                        }
                    }
                }                
            } catch (InventoryException ex) {
                res.add(new SyncResult(dataSourceConfiguration.getId(), SyncResult.TYPE_ERROR, "Bridge Domain Information Processing", ex.getLocalizedMessage()));
            }
        }
        
        return res;
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
    private String readCommandExecutionResult (ChannelExec channel) throws InvalidArgumentException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(channel.getInputStream()))) {
            String result = buffer.lines().collect(Collectors.joining("\n"));
            return channel.getExitStatus() == 0 ? result : null;
        } catch (IOException ex) {
            throw new InvalidArgumentException(String.format("Error reading the command execution result: %s", ex.getLocalizedMessage()));
        }
    }

}
