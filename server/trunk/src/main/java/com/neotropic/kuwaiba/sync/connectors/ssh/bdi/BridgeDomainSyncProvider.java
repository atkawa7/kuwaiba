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

package com.neotropic.kuwaiba.sync.connectors.ssh.bdi;

import com.neotropic.kuwaiba.sync.connectors.ssh.bdi.entities.BridgeDomain;
import com.neotropic.kuwaiba.sync.connectors.ssh.bdi.entities.NetworkInterface;
import com.neotropic.kuwaiba.sync.connectors.ssh.bdi.parsers.BridgeDomainsASR1002Parser;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
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
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class BridgeDomainSyncProvider extends AbstractSyncProvider {

    @Override
    public String getDisplayName() {
        return "Bridge Domains and Bridge Domain Interfaces Sync Provider";
    }

    @Override
    public String getId() {
        return "BridgeDomainSyncProvider";
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
        
        final SSHClient ssh = new SSHClient();
        PollResult res = new PollResult();
        
        BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
        
        for (SyncDataSourceConfiguration dataSourceConfiguration : syncDataSourceConfigurations) {
            Session session = null;
            try {
                long deviceId;
                int port;
                String className, host, user, password;

                if (dataSourceConfiguration.getParameters().containsKey("deviceId")) //NOI18N
                    deviceId = Long.valueOf(dataSourceConfiguration.getParameters().get("deviceId")); //NOI18N
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

                if (dataSourceConfiguration.getParameters().containsKey("port")) //NOI18N
                    port = Integer.valueOf(dataSourceConfiguration.getParameters().get("port")); //NOI18N
                else {
                    res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_not_defined"), "port", syncGroup.getName())));
                    continue;
                }

                if (dataSourceConfiguration.getParameters().containsKey("user")) //NOI18N
                    user = dataSourceConfiguration.getParameters().get("user"); //NOI18N
                else {
                    res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_not_defined"), "user", syncGroup.getName()))); //NOI18N
                    continue;
                }

                if (dataSourceConfiguration.getParameters().containsKey("password")) //NOI18N
                    password = dataSourceConfiguration.getParameters().get("password"); //NOI18N
                else {
                    res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_not_defined"), "password", syncGroup.getName()))); //NOI18N
                    continue;
                }
    
                BusinessObjectLight currentObject = bem.getObjectLight(className, deviceId);
                
                ssh.loadKnownHosts();
                ssh.connect(host, port);
                ssh.authPassword(user, password);
                session = ssh.startSession();
                
                String modelString = currentObject.getName().split("-")[0];
                
                switch (modelString) { //The model of the device is taken from its name. Alternatively, this could be taken from its actual model
                    case "ASR920": {
                        Session.Command cmd = session.exec("sh bridge-domain"); //NOI18N
                        
                        BridgeDomainsASR920Parser parser = new BridgeDomainsASR920Parser();               

                        cmd.join(5, TimeUnit.SECONDS);
                        if (cmd.getExitStatus() != 0) 
                            res.getExceptions().put(dataSourceConfiguration, Arrays.asList(new InvalidArgumentException("The command to retrieve the bridge domain information could not be retrieved. Check the syntax and the firmware version")));
                        else 
                            res.getResult().put(currentObject, 
                                    parser.parse(IOUtils.readFully(cmd.getInputStream()).toString()));
                        break;
                    }
                    case "ASR1002": {
                        Session.Command cmd = session.exec("sh bridge-domain"); //NOI18N
                        
                        BridgeDomainsASR1002Parser parser = new BridgeDomainsASR1002Parser();               

                        cmd.join(5, TimeUnit.SECONDS);
                        if (cmd.getExitStatus() != 0) 
                            res.getExceptions().put(dataSourceConfiguration, Arrays.asList(new InvalidArgumentException(cmd.getExitErrorMessage())));
                        else 
                            res.getResult().put(currentObject, 
                                    parser.parse(IOUtils.readFully(cmd.getInputStream()).toString()));
                        break;
                    }
                    case "ASR9001": {
                        Session.Command cmd = session.exec("sh l2vpn bridge-domain"); //NOI18N
                        BridgeDomainsASR9001Parser parser = new BridgeDomainsASR9001Parser();               

                        cmd.join(5, TimeUnit.SECONDS);
                        if (cmd.getExitStatus() != 0) 
                            res.getExceptions().put(dataSourceConfiguration, Arrays.asList(new InvalidArgumentException(cmd.getExitErrorMessage())));
                        else 
                            res.getResult().put(currentObject, 
                                    parser.parse(IOUtils.readFully(cmd.getInputStream()).toString()));
                        break;
                    }
                    case "ME3600": {
                        Session.Command cmd = session.exec("sh bridge-domain"); //NOI18N
                        BridgeDomainsME3600Parser parser = new BridgeDomainsME3600Parser();               

                        cmd.join(5, TimeUnit.SECONDS);
                        if (cmd.getExitStatus() != 0) 
                            res.getExceptions().put(dataSourceConfiguration, Arrays.asList(new InvalidArgumentException(cmd.getExitErrorMessage())));
                        else 
                            res.getResult().put(currentObject, 
                                    parser.parse(IOUtils.readFully(cmd.getInputStream()).toString()));
                        break;
                    }
                    
                    default:
                        res.getExceptions().put(dataSourceConfiguration, Arrays.asList(new InvalidArgumentException(String.format("Model %s is not supported. Check your naming conventions, as an hyphen is expected as separator", modelString))));
                }
            } catch (Exception ex) {
                res.getExceptions().put(dataSourceConfiguration, Arrays.asList(ex));
            } finally {
                try {
                    if (session != null)
                        session.close();
                    ssh.disconnect();
                } catch (IOException e) { }
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
                res.add(new SyncResult(SyncResult.TYPE_ERROR, String.format("Severe error while processing data source configuration %s", dsConfig.getName()), ex.getLocalizedMessage()));
        }
        
        for (BusinessObjectLight relatedOject : pollResult.getResult().keySet()) {
            try {
                List<BusinessObjectLight> existingBridgeDomains = bem.getSpecialChildrenOfClassLight(relatedOject.getId(), 
                        relatedOject.getClassName(), "BridgeDomain", -1);
                
                List<AbstractDataEntity> bridgeDomainsInDevice = pollResult.getResult().get(relatedOject);
                
                for (AbstractDataEntity bridgeDomainInDevice : bridgeDomainsInDevice) { //First we check if the bridge domains exists within the device. If they do not, they will be created, if they do, we will check the interfaces
                    BusinessObjectLight matchingBridgeDomain = null;
                    List<BusinessObjectLight> bridgeDomainInterfaces = null; //These objects are retrieved lazily
                    List<BusinessObjectLight> physicalInterfaces = null; //These objects are retrieved lazily
                    
                    for (BusinessObjectLight existingBridgeDomain : existingBridgeDomains) {
                        if (existingBridgeDomain.getName().equals(((BridgeDomain)bridgeDomainInDevice).getName())) {
                            res.add(new SyncResult(SyncResult.TYPE_INFORMATION, String.format("Check if Bridge Domain %s exists within %s", existingBridgeDomain, relatedOject), 
                                    "The Bridge Domain exists and was not modified"));
                            matchingBridgeDomain = existingBridgeDomain;
                            break;
                        }
                    }
                    
                    if (matchingBridgeDomain == null) {
                        HashMap<String, String> defaultAttributes = new HashMap<>();
                        defaultAttributes.put(Constants.PROPERTY_NAME, bridgeDomainInDevice.getName());
                        long newBridgeDomain = bem.createSpecialObject("BridgeDomain", relatedOject.getClassName(), relatedOject.getId(), defaultAttributes, -1);
                        aem.createGeneralActivityLogEntry("admin", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s [BridgeDomain] (id:%s)", bridgeDomainInDevice.getName(), newBridgeDomain));
                        res.add(new SyncResult(SyncResult.TYPE_SUCCESS, String.format("Check if Bridge Domain %s exists within %s", bridgeDomainInDevice.getName(), relatedOject), 
                                    "The Bridge Domain did not exist and was created successfully"));
                        matchingBridgeDomain = new BusinessObjectLight("BridgeDomain", newBridgeDomain, bridgeDomainInDevice.getName());
                        bridgeDomainInterfaces = new ArrayList<>();
                    }
                    
                    //Now we check if the network interfaces exist and relate them if necessary
                    for (NetworkInterface networkInterface : ((BridgeDomain)bridgeDomainInDevice).getNetworkInterfaces()) {
                        if (networkInterface.getNetworkInterfaceType() == NetworkInterface.TYPE_VFI) {
                            res.add(new SyncResult(SyncResult.TYPE_WARNING, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                    String.format("VFI %s was ignored", networkInterface.getName())));
                            continue;
                        }
                        
                        if (networkInterface.getNetworkInterfaceType() == NetworkInterface.TYPE_BDI) {
                            
                            if (bridgeDomainInterfaces == null)
                                bridgeDomainInterfaces = bem.getSpecialChildrenOfClassLight(matchingBridgeDomain.getId(), 
                                        matchingBridgeDomain.getClassName(), "BridgeDomainInterface", -1);
                            
                            BusinessObjectLight matchingBridgeDomainInterface = null;
                            for (BusinessObjectLight bridgeDomainInterface : bridgeDomainInterfaces) {
                                if (bridgeDomainInterface.getName().equals(networkInterface.getName())) {
                                    matchingBridgeDomainInterface = bridgeDomainInterface;
                                    res.add(new SyncResult(SyncResult.TYPE_INFORMATION, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("BDI %s already exists. No changes were made", networkInterface.getName())));
                                    break;
                                }
                            }
                            
                            if (matchingBridgeDomainInterface == null) {
                                HashMap<String, String> defaultAttributes = new HashMap<>();
                                defaultAttributes.put(Constants.PROPERTY_NAME, networkInterface.getName());
                                long newBridgeDomainInterface = bem.createSpecialObject("BridgeDomainInterface", "BridgeDomain", matchingBridgeDomain.getId(), defaultAttributes, -1);
                                res.add(new SyncResult(SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("The BDI %s did not exist and was created.", networkInterface.getName())));
                                aem.createGeneralActivityLogEntry("sync", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                                        String.format("%s [BridgeDomainInterface] (id:%s)", networkInterface.getName(), newBridgeDomainInterface));
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
                            String standardName = SyncUtil.wrapPortName(interfaceNameTokens[0]);
                            for (BusinessObjectLight physicalInterface : physicalInterfaces) {
                                
                                if (physicalInterface.getName().equals(standardName)) { //Checks for the extended and the condensed interface name formats (GigabitEthernetXXX vs GiXXXX)
                                    matchingPhysicalInterface = physicalInterface;
                                    break;
                                }
                            }
                            
                            if (matchingPhysicalInterface == null) 
                                res.add(new SyncResult(SyncResult.TYPE_ERROR, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
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
                                    long newServiceInstance = bem.createObject(Constants.CLASS_SERVICE_INSTANCE, matchingPhysicalInterface.getClassName(), matchingPhysicalInterface.getId(), 
                                            defaultAttributes, -1);
                                    
                                    res.add(new SyncResult(SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("Service Instance %s did not exist and was created.", networkInterface.getName())));
                                    
                                    matchingServiceInstance = new BusinessObjectLight(Constants.CLASS_SERVICE_INSTANCE, newServiceInstance, interfaceNameTokens[interfaceNameTokens.length - 1]);
                                } else
                                    res.add(new SyncResult(SyncResult.TYPE_INFORMATION, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("Service Instance %s already exists. No changes were made.", matchingServiceInstance.getName())));
                                
                                List<BusinessObjectLight> relatedBridgeDomain = bem.getSpecialAttribute(matchingServiceInstance.getClassName(), matchingServiceInstance.getId(), "networkBridgesInterface");
                                if (relatedBridgeDomain.isEmpty()) {
                                    bem.createSpecialRelationship("BridgeDomain", matchingBridgeDomain.getId(), Constants.CLASS_SERVICE_INSTANCE, matchingServiceInstance.getId(), "networkBridgesInterface", true);
                                    res.add(new SyncResult(SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                            String.format("Service instace %s was successfully related to the bridge domain %s", matchingServiceInstance.getName(), matchingBridgeDomain.getName())));
                                } else {
                                    if (relatedBridgeDomain.get(0).getId() == matchingBridgeDomain.getId())
                                        res.add(new SyncResult(SyncResult.TYPE_INFORMATION, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                            String.format("Service instace %s is already related to bridge domain %s. No changes were made.", matchingServiceInstance.getName(), matchingBridgeDomain.getName())));
                                    else {
                                        bem.releaseRelationships(matchingServiceInstance.getClassName(), matchingServiceInstance.getId(), Arrays.asList("networkBridgesInterface"));
                                        bem.createSpecialRelationship("BridgeDomain", matchingBridgeDomain.getId(), Constants.CLASS_SERVICE_INSTANCE, matchingServiceInstance.getId(), "networkBridgesInterface", true);
                                        
                                        res.add(new SyncResult(SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s", bridgeDomainInDevice.getName()), 
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
                            String standardName = SyncUtil.wrapPortName(interfaceNameTokens[0]);
                            for (BusinessObjectLight physicalInterface : physicalInterfaces) {
                                
                                if (physicalInterface.getName().equals(standardName)) { //Checks for the extended and the condensed interface name formats (GigabitEthernetXXX vs GiXXXX)
                                    matchingPhysicalInterface = physicalInterface;
                                    break;
                                }
                            }
                            
                            if (matchingPhysicalInterface == null) 
                                res.add(new SyncResult(SyncResult.TYPE_ERROR, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
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
                                    long newVirtualPort = bem.createObject(Constants.CLASS_VIRTUALPORT, matchingPhysicalInterface.getClassName(), matchingPhysicalInterface.getId(), 
                                            defaultAttributes, -1);
                                    
                                    res.add(new SyncResult(SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("Subinterface %s did not exist and was created.", networkInterface)));
                                    
                                    matchingVirtualPort = new BusinessObjectLight(Constants.CLASS_VIRTUALPORT, newVirtualPort, interfaceNameTokens[interfaceNameTokens.length - 1]);
                                } else
                                    res.add(new SyncResult(SyncResult.TYPE_INFORMATION, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                        String.format("Subinterface %s already exists. No changes were made.", matchingVirtualPort)));
                                
                                List<BusinessObjectLight> relatedBridgeDomain = bem.getSpecialAttribute(matchingVirtualPort.getClassName(), matchingVirtualPort.getId(), "networkBridgesInterface");
                                if (relatedBridgeDomain.isEmpty()) {
                                    bem.createSpecialRelationship("BridgeDomain", matchingBridgeDomain.getId(), Constants.CLASS_VIRTUALPORT, matchingVirtualPort.getId(), "networkBridgesInterface", true);
                                    res.add(new SyncResult(SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                            String.format("Subinterface %s was successfully related to the bridge domain %s", matchingVirtualPort, matchingBridgeDomain.getName())));
                                } else {
                                    if (relatedBridgeDomain.get(0).getId() == matchingBridgeDomain.getId())
                                        res.add(new SyncResult(SyncResult.TYPE_INFORMATION, String.format("Checking network interfaces related to Bridge Domain %s in router %s", bridgeDomainInDevice.getName(), relatedOject), 
                                            String.format("Subinterface %s is already related to bridge domain %s. No changes were made.", matchingVirtualPort, matchingBridgeDomain.getName())));
                                    else {
                                        bem.releaseRelationships(matchingVirtualPort.getClassName(), matchingVirtualPort.getId(), Arrays.asList("networkBridgesInterface"));
                                        bem.createSpecialRelationship("BridgeDomain", matchingBridgeDomain.getId(), Constants.CLASS_VIRTUALPORT, matchingVirtualPort.getId(), "networkBridgesInterface", true);
                                        
                                        res.add(new SyncResult(SyncResult.TYPE_SUCCESS, String.format("Checking network interfaces related to Bridge Domain %s", bridgeDomainInDevice.getName()), 
                                            String.format("Subinterface %s was related to bridge domain %s, but the relationship was changed to bridge domain %s", matchingVirtualPort, 
                                                    relatedBridgeDomain.get(0).getName(), matchingBridgeDomain.getName())));
                                    }
                                }
                            }
                        }
                    }
                }
                
            } catch (InventoryException ex) {
                res.add(new SyncResult(SyncResult.TYPE_ERROR, "Bridge Domain Information Processing", ex.getLocalizedMessage()));
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
