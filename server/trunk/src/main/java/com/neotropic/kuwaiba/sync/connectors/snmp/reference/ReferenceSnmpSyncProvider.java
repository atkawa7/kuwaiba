/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
package com.neotropic.kuwaiba.sync.connectors.snmp.reference;

import com.neotropic.kuwaiba.sync.connectors.snmp.SnmpManager;
import com.neotropic.kuwaiba.sync.model.AbstractDataEntity;
import com.neotropic.kuwaiba.sync.model.AbstractSyncProvider;
import com.neotropic.kuwaiba.sync.model.PollResult;
import com.neotropic.kuwaiba.sync.model.SyncAction;
import com.neotropic.kuwaiba.sync.model.SyncDataSourceConfiguration;
import com.neotropic.kuwaiba.sync.model.SyncFinding;
import com.neotropic.kuwaiba.sync.model.SyncResult;
import com.neotropic.kuwaiba.sync.model.SyncUtil;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import com.neotropic.kuwaiba.sync.model.TableData;
import java.io.StringReader;
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
import javax.json.JsonValue;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.ConnectionException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.util.i18n.I18N;
import org.openide.util.Exceptions;
import org.snmp4j.smi.OID;

/**
 * Synchronization provider to SNMP agents
 * This class implement the logic to connect with a group of SNMP agents to 
 * retrieve the data and compare the differences with the management objects
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ReferenceSnmpSyncProvider extends AbstractSyncProvider {

    private final static String ACTION_CONTAINMENT_HIERARCHY = "The containment hierarchy was updated parent: [%s] => children: %s";
    private final static String ACTION_OBJECT_UPDATED = "The object %s [%s] with id %s was updated";
    private final static String ACTION_OBJECT_CREATED = "The object %s [%s] with id %s was created";
    private final static String ACTION_OBJECT_DELETED = "The object %s [%s] with id %s was deleted";
    private final static String ACTION_LISTTYPE_CREATED = "A list type %s was created";
    private final static String ACTION_PORT_NO_MATCH = "This %s has no match, please check it manually";
    
    private List<SyncResult> results;
    private BusinessEntityManager bem;
    private MetadataEntityManager mem;
    private HashMap<Long, Long> createdIdsToMap;
    private HashMap<Long, List<String>> newCreatedPortsToCreate;
    private List<StringPair> nameOfCreatedPorts;
        
    @Override
    public String getDisplayName() {
        return "Reference SNMP Synchronization Provider";
    }

    @Override
    public String getId() {
        return "ReferenceSnmpSyncProvider"; //NOI18N
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
        PollResult pollResult = new PollResult();
        
        for (SyncDataSourceConfiguration dsConfig : syncGroup.getSyncDataSourceConfigurations()) {
            long id = -1L;
            String className = null;                
            String address = null;
            String port = null;
            //String community = null;

            if (dsConfig.getParameters().containsKey("deviceId")) //NOI18N
                id = Long.valueOf(dsConfig.getParameters().get("deviceId")); //NOI18N
            else 
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_deviceId_no_defined"), syncGroup.getName(), syncGroup.getId())));

            if (dsConfig.getParameters().containsKey("deviceClass")) //NOI18N
                className = dsConfig.getParameters().get("deviceClass"); //NOI18N
            else
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_deviceClass_no_defined"), syncGroup.getName(), syncGroup.getId())));

            if (dsConfig.getParameters().containsKey("ipAddress")) //NOI18N
                address = dsConfig.getParameters().get("ipAddress"); //NOI18N
            else 
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_ipAddress_no_defined"), syncGroup.getName(), syncGroup.getId())));

            if (dsConfig.getParameters().containsKey("port")) //NOI18N 
                port = dsConfig.getParameters().get("port"); //NOI18N
            else 
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_port_no_defined"), syncGroup.getName(), syncGroup.getId())));

            String version = SnmpManager.VERSION_2c;
            if (dsConfig.getParameters().containsKey(Constants.PROPERTY_SNMP_VERSION))
                version = dsConfig.getParameters().get(Constants.PROPERTY_SNMP_VERSION);

            if (SnmpManager.VERSION_2c.equals(version)) {
                if (!dsConfig.getParameters().containsKey(Constants.PROPERTY_COMMUNITY))
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_community_no_defined"), syncGroup.getName(), syncGroup.getId())));
            }
            if (SnmpManager.VERSION_3.equals(version)) {
                if (!dsConfig.getParameters().containsKey(Constants.PROPERTY_AUTH_PROTOCOL))
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_auth_protocol_no_defined"), syncGroup.getName(), syncGroup.getId())));
                
                if (!dsConfig.getParameters().containsKey(Constants.PROPERTY_SECURITY_NAME))
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_security_name_no_defined"), syncGroup.getName(), syncGroup.getId())));
            }

            if (pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).isEmpty()) {

                BusinessObjectLight mappedObjLight = null;

                try {
                    mappedObjLight = PersistenceService.getInstance().getBusinessEntityManager().getObjectLight(className, id);
                } catch(InventoryException ex) {
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(I18N.gm("snmp_sync_object_not_found"), ex.getMessage())));
                }
                if (mappedObjLight != null) {
                    SnmpManager snmpManager = SnmpManager.getInstance();

                    snmpManager.setAddress(String.format("udp:%s/%s", address, port)); //NOI18N
                    snmpManager.setVersion(version);

                    if (SnmpManager.VERSION_2c.equals(version))
                        snmpManager.setCommunity(dsConfig.getParameters().get(Constants.PROPERTY_COMMUNITY));

                    if (SnmpManager.VERSION_3.equals(version)) {
                        snmpManager.setAuthProtocol(dsConfig.getParameters().get(Constants.PROPERTY_AUTH_PROTOCOL));
                        snmpManager.setAuthPass(dsConfig.getParameters().get(Constants.PROPERTY_AUTH_PASS));
                        snmpManager.setSecurityLevel(dsConfig.getParameters().get(Constants.PROPERTY_SECURITY_LEVEL));
                        snmpManager.setContextName(dsConfig.getParameters().get(Constants.PROPERTY_CONTEXT_NAME));
                        snmpManager.setSecurityName(dsConfig.getParameters().get(Constants.PROPERTY_SECURITY_NAME));
                        snmpManager.setPrivacyProtocol(dsConfig.getParameters().get(Constants.PROPERTY_PRIVACY_PROTOCOL));
                        snmpManager.setPrivacyPass(dsConfig.getParameters().get(Constants.PROPERTY_PRIVACY_PASS));
                    }
                    //ENTITY-MIB table
                    ReferenceSnmpEntPhysicalTableResourceDefinition entPhysicalTable = new ReferenceSnmpEntPhysicalTableResourceDefinition();
                    List<List<String>> tableAsString = snmpManager.getTableAsString(entPhysicalTable.values().toArray(new OID[0]));
                        
                    if (tableAsString == null) {
                        pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                            new ConnectionException(String.format(I18N.gm("snmp_agent_connection_exception"), mappedObjLight.toString())));
                        return pollResult;
                    }
                    
                    pollResult.getResult().put(dsConfig, new ArrayList<>());
                    pollResult.getResult().get(dsConfig).add(
                            new TableData("entPhysicalTable", SyncUtil.parseMibTable("instance", entPhysicalTable, tableAsString))); //NOI18N
                
                    //IF_MIB
                    SnmpifXTableResocurceDefinition ifMibTable = new SnmpifXTableResocurceDefinition();
                    List<List<String>> ifMibTableAsString = snmpManager.getTableAsString(ifMibTable.values().toArray(new OID[0]));

                    if (ifMibTableAsString == null) {
                        pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                            new ConnectionException(String.format(I18N.gm("snmp_agent_connection_exception"), mappedObjLight.toString())));
                        return pollResult;
                    }
                    
                    pollResult.getResult().get(dsConfig).add(
                            new TableData("ifMibTable", SyncUtil.parseMibTable("instance", ifMibTable, ifMibTableAsString))); //NOI18N
                }
            }
        }
        return pollResult;
    }
    
    @Override
    public List<SyncFinding> supervisedSync(PollResult pollResult) {
         throw new UnsupportedOperationException("This provider does not support unmapped polling");
    }

    @Override
    public List<SyncFinding> supervisedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support unmapped polling");
    }

    @Override
    public List<SyncResult> automatedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support supervised sync for unmapped pollings");
    }

    @Override
    public  List<SyncResult> automatedSync(PollResult pollResult) {
        HashMap<SyncDataSourceConfiguration, List<AbstractDataEntity>> originalData = pollResult.getResult();
        List<SyncResult> results = new ArrayList<>();
        // Adding to findings list the not blocking execution exception found during the mapped poll
        for (SyncDataSourceConfiguration agent : pollResult.getExceptions().keySet()) {
            for (Exception exception : pollResult.getExceptions().get(agent))
                results.add(new SyncResult(agent.getId(), SyncFinding.EVENT_ERROR, 
                        exception.getMessage(), 
                        Json.createObjectBuilder().add("type","ex").build().toString()));
        }
        for (Map.Entry<SyncDataSourceConfiguration, List<AbstractDataEntity>> entrySet : originalData.entrySet()) {
            List<TableData> mibTables = new ArrayList<>();
            entrySet.getValue().forEach((value) -> {
                mibTables.add((TableData)value);
            });
            
            EntPhysicalSynchronizer x = new EntPhysicalSynchronizer(entrySet.getKey().getId(),
                    new BusinessObjectLight(entrySet.getKey().getParameters().get("deviceClass"), 
                    Long.valueOf(entrySet.getKey().getParameters().get("deviceId")), ""),
                    mibTables);
            
            try {
                results.addAll(x.sync());
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException | OperationNotPermittedException | ApplicationObjectNotFoundException | ArraySizeMismatchException | NotAuthorizedException | ServerSideException ex) {
                Exceptions.printStackTrace(ex);
            }
           
        }
        return results;
    }

    @Override
    public List<SyncResult> finalize(List<SyncAction> actions) {
        PersistenceService persistenceService = PersistenceService.getInstance();
        bem = persistenceService.getBusinessEntityManager();
        mem = persistenceService.getMetadataEntityManager();
        newCreatedPortsToCreate = new HashMap<>();
        results = new ArrayList<>();
        createdIdsToMap = new HashMap<>();
        nameOfCreatedPorts = new ArrayList<>();
        
        for(SyncAction action : actions){
            JsonObject jsonObj = null;
            String type = null;
            if (action.getFinding().getExtraInformation() != null) {
                try (final JsonReader jsonReader = Json.createReader(new StringReader(action.getFinding().getExtraInformation()))) {
                    jsonObj = jsonReader.readObject();
                    if (jsonObj.get("type") != null)
                        type = jsonObj.getString("type");
                }
            }
            if (type != null && jsonObj != null) {
                switch (type) {
                    case "hierarchy":
                        updateContaimentHiearchy(action.getFinding().getDataSourceId(), jsonObj.getJsonObject("hierarchy"));
                        break;
                    case "listType":
                        createMissingListTypes(action.getFinding().getDataSourceId(), jsonObj);
                        break;
                    case "device":
                        manageDevices(jsonObj, action.getFinding());
                        break;
                    case "branch":
                        manageObjectOfBranch(jsonObj, action.getFinding());
                        break;
                    case "object_port_move":
                        try {
                            migrateOldPortsIntoNewPosition(jsonObj, action.getFinding());
                        } catch (ApplicationObjectNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        break;
                    case "old_object_to_delete":
                        deleteOldStructure(jsonObj, action.getFinding());
                        break;
                    case "object_port_no_match":
                        results.add(new SyncResult(action.getFinding().getDataSourceId(),
                                SyncResult.TYPE_WARNING, String.format(ACTION_PORT_NO_MATCH, 
                                        jsonObj.toString()), " There is nothing kuwaiba could do with this port, its name has no match with the data got it from SNMP, please check manually in order to move it or delete it"));
                        break;
                    case "object_port_no_match_new":
                        createNewPorts(jsonObj, action.getFinding());
                        break;
                    case "ifbmib":
                        try (final JsonReader jsonReader = Json.createReader(new StringReader(action.getFinding().getExtraInformation()))) {
                            jsonObj = jsonReader.readObject();
                            if (jsonObj.get("ifmibsync") != null){
                                JsonArray jsonArray = jsonObj.getJsonArray("ifmibsync");
                                for (JsonValue jsonValue : jsonArray) {
                                    try (final JsonReader childReader = Json.createReader(new StringReader(jsonValue.toString()))){
                                        JsonObject child = childReader.readObject();
                                        String ifName = "", ifalias = "", status = "", serviceStatus = "";
                                        if(child.getJsonObject("result").get("ifName") != null)
                                            ifName = child.getJsonObject("result").getString("ifName");
                                        if(child.getJsonObject("result").get("ifAlias") != null)
                                            ifalias = child.getJsonObject("result").getString("ifAlias");
                                        if(child.getJsonObject("result").get("status") != null)
                                            status = child.getJsonObject("result").getString("status");
                                        if(child.getJsonObject("result").get("related-service") != null)
                                            serviceStatus = child.getJsonObject("result").getString("related-service");
                                        //This means that something was done with the interface
                                        if((!status.isEmpty() && Integer.valueOf(status) != -2) || (!serviceStatus.isEmpty() && Integer.valueOf(serviceStatus) > -1)){
                                            String service = "";
                                            String m = "";
                                            if(!ifalias.isEmpty() && !serviceStatus.isEmpty()){
                                                if(null != Integer.valueOf(serviceStatus))
                                                    switch (Integer.valueOf(serviceStatus)) {
                                                    case 0:
                                                        m = "successfully related with the interface";
                                                        break;
                                                    case 1:
                                                        m = "is already related with the interface";
                                                        break;
                                                    case -1:
                                                        m = "doesn't exists in kuwaiba";
                                                        break;
                                                    default:
                                                        break;
                                                }
                                                
                                                service = String.format(" - the service %s, %s", ifalias, m);
                                            } 
                                            String s =  "";
                                            if(null != Integer.valueOf(status))
                                                switch (Integer.valueOf(status)) {
                                                case -1:
                                                    s = "not found";
                                                    break;
                                                case 1:
                                                    s = "created";
                                                    break;
                                                case 2:
                                                    s = "attribute highSpeed, updated";
                                                    break;
                                                case 3:
                                                    s = "attribute name, updated";
                                                    break;
                                                default:
                                                    break;
                                            }
                                            results.add(new SyncResult(action.getFinding().getDataSourceId(), SyncResult.TYPE_SUCCESS, 
                                                "ifmib Synchronization",
                                                String.format("The interface: %s, %s", ifName, s) + service));
                                        }
                                    }                                
                                }
                            }
                        }
                        break;
                    case "ciscomib":
                        try (final JsonReader jsonReader = Json.createReader(new StringReader(action.getFinding().getExtraInformation()))) {
                            jsonObj = jsonReader.readObject();
                            if (jsonObj.get("ciscomibsync") != null){
                                JsonArray jsonArray = jsonObj.getJsonArray("ciscomibsync");
                                for (JsonValue jsonValue : jsonArray) {
                                    try (final JsonReader childReader = Json.createReader(new StringReader(jsonValue.toString()))){
                                        JsonObject child = childReader.readObject();
                                        String vcid = child.getJsonObject("result").getString("vcid");
                                        String sourcePort = child.getJsonObject("result").getString("sourcePort");
                                        String destinyPort = child.getJsonObject("result").getString("destinyPort");
                                        String device = child.getJsonObject("result").getString("device");
                                        String service = child.getJsonObject("result").getString("service");
                                        results.add(new SyncResult(action.getFinding().getDataSourceId(), SyncResult.TYPE_SUCCESS, 
                                                String.format("In divice: %s was created the VcID: %s, sourcePort: %s, destinyPort: %s, service name: %s", device, vcid, sourcePort, destinyPort, service)
                                                , "Info"));
                                    }                                
                                }
                            }
                        }
                        break;
                    case "ciscoTemib":
                        try (final JsonReader jsonReader = Json.createReader(new StringReader(action.getFinding().getExtraInformation()))) {
                            jsonObj = jsonReader.readObject();
                            if (jsonObj.get("ciscoTemibsync") != null){
                                JsonArray jsonArray = jsonObj.getJsonArray("ciscoTemibsync");
                                for (JsonValue jsonValue : jsonArray) {
                                    try (final JsonReader childReader = Json.createReader(new StringReader(jsonValue.toString()))){
                                        JsonObject child = childReader.readObject();
                                        String vcid = child.getJsonObject("result").getString("vcid");
                                        String tunnel = child.getJsonObject("result").getString("tunnel");
                                        String ipSource = child.getJsonObject("result").getString("ipSource");
                                        String ipDestiny = child.getJsonObject("result").getString("ipDestiny");
                                        String description = child.getJsonObject("result").getString("description");
                                        results.add(new SyncResult(action.getFinding().getDataSourceId(), SyncResult.TYPE_SUCCESS, 
                                                String.format("The tunnel: %s was related with VcID: %s, ipSource: %s - ipDestiny: %s, service: %s", tunnel, vcid, ipSource, ipDestiny, description)
                                                , "Info"));
                                    }                                
                                }
                            }
                        }
                        break;    
                }
            } else {
                if (action.getFinding().getType() == SyncFinding.EVENT_ERROR)
                    results.add(new SyncResult(action.getFinding().getDataSourceId(), 
                            SyncResult.TYPE_ERROR, action.getFinding().getDescription(), I18N.gm("error")));
            }
        }
        return results;
    }
    
    /**
     * json 
     * {type:hierarchy
     *  hierarchy:{
     *             slot[{"child":"Transceiver"},{"child":"IPBoard"},{"child":"HybridBoard"}], 
     *             parenClass, [{child:possibleChild},...]
     *            }
     * }
     */
    private void updateContaimentHiearchy(long dataSourceConfigId, JsonObject jo){
        try {
            HashMap<String, List<String>> classes = new HashMap<>();
            for (Map.Entry<String, JsonValue> entry : jo.entrySet()) {
                JsonReader childReader = Json.createReader(new StringReader(entry.getValue().toString()));
                JsonArray children = childReader.readArray();

                List<String> possibleChildren = classes.get(entry.getKey());
                if(possibleChildren == null)
                    possibleChildren = new ArrayList<>();
                for (JsonValue child : children) {
                    JsonReader classReader = Json.createReader(new StringReader(child.toString()));
                    JsonObject childObj = classReader.readObject();
                    
                    List<ClassMetadataLight> actualPossiblechildren = mem.getPossibleChildren(entry.getKey());
                    boolean isAlreadyChild = false;
                    String className = childObj.getString("child");
                    for(ClassMetadataLight possibleChild : actualPossiblechildren){
                        if(possibleChild.getName().equals(className))
                            isAlreadyChild=true;
                    }
                    if(!isAlreadyChild)
                        possibleChildren.add(className);
                }
               
                if(!possibleChildren.isEmpty())
                    classes.put(entry.getKey(), possibleChildren);
            }
            for (Map.Entry<String, List<String>> entrySet : classes.entrySet()) {
                String key = entrySet.getKey();
                List<String> possibleChildrenToAdd = entrySet.getValue();

                    mem.addPossibleChildren(key, possibleChildrenToAdd.toArray(new String[possibleChildrenToAdd.size()]));
                    results.add(new SyncResult(dataSourceConfigId, SyncResult.TYPE_SUCCESS, String.format(ACTION_CONTAINMENT_HIERARCHY, key, possibleChildrenToAdd), 
                            "Updated successfully"));
            }
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            results.add(new SyncResult(dataSourceConfigId, SyncResult.TYPE_ERROR, "Updating the class hierarchy", "Possible cause: " + ex.getMessage() + "Please check and run the sync again"));
        }
    }
    
    private void createMissingListTypes(long dataSourceConfigId, JsonObject jo){
        results.add(new SyncResult(dataSourceConfigId, SyncResult.TYPE_SUCCESS, String.format(ACTION_LISTTYPE_CREATED, jo.getString("name")), "Created successfully"));
    }
    
    private void manageDevices(JsonObject device, SyncFinding find){
        HashMap<String, String> attributes = new HashMap<>();
        long deviceId = Long.valueOf(device.getString("deviceId"));
        String deviceClassName = device.getString("deviceClassName");
        JsonObject jsonAttributes = device.getJsonObject("attributes");
        
        String name;
        if(jsonAttributes.get("name") != null)
           attributes.put("name", jsonAttributes.getString("name"));
        if(jsonAttributes.get("description") != null)
            attributes.put("description", jsonAttributes.getString("description"));
        if(jsonAttributes.get("serialNumber") != null)
            attributes.put("serialNumber", jsonAttributes.getString("serialNumber"));
        if(jsonAttributes.get("vendor") != null)
            attributes.put("vendor", jsonAttributes.getString("vendor"));
        if(jsonAttributes.get("model") != null)
            attributes.put("model", jsonAttributes.getString("model")); 
        if (find.getType() == SyncFinding.EVENT_UPDATE){
            try{
                bem.updateObject(deviceClassName, deviceId, attributes);
                if(attributes.get("name") == null)
                    name = bem.getObject(deviceId).getAttributes().get("name");
                else
                    name = attributes.get("name");
                results.add(new SyncResult(find.getDataSourceId(), SyncResult.TYPE_SUCCESS, String.format(ACTION_OBJECT_UPDATED, name, deviceClassName, Long.toString(deviceId)), "Updated successfully"));
            } catch (InvalidArgumentException | BusinessObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                results.add(new SyncResult(find.getDataSourceId(), SyncResult.TYPE_ERROR, find.getDescription(), "Possible cause: " + ex.getMessage() + " Please check and run the sync again"));
            }
        }
    }
    
    private void manageObjectOfBranch(JsonObject jsonObj, SyncFinding find){
        JsonArray children = jsonObj.getJsonArray("children");
        List<String> portsToCreate = new ArrayList<>();
        long portId = -1;
        boolean segmentDependsOfPort = false; 
        for (JsonValue jObj : children) {
            try (final JsonReader childReader = Json.createReader(new StringReader(jObj.toString()))) 
            {
                JsonObject child = childReader.readObject();
                child.getJsonObject("child");

                HashMap<String, String> attributes = new HashMap<>();
                Long childId = Long.valueOf(child.getJsonObject("child").getString("childId"));
                String className = child.getJsonObject("child").getString("className");
                String parentClassName = child.getJsonObject("child").getString("parentClassName");
                Long tempParentId = Long.valueOf(child.getJsonObject("child").getString("parentId"));
                Long parentId = 0l;
                if(child.getJsonObject("child").get("deviceParentId") != null)
                    parentId = Long.valueOf(child.getJsonObject("child").getString("deviceParentId"));

                JsonObject jsonAttributes = child.getJsonObject("child").getJsonObject("attributes");
                attributes.put("name", jsonAttributes.getString("name"));
                if(jsonAttributes.get("description") != null)
                    attributes.put("description", jsonAttributes.getString("description"));
                if(jsonAttributes.get("serialNumber") != null)
                    attributes.put("serialNumber", jsonAttributes.getString("serialNumber"));
                if(jsonAttributes.get("vendor") != null)
                    attributes.put("vendor", jsonAttributes.getString("vendor"));
                if(jsonAttributes.get("model") != null)
                    attributes.put("model", jsonAttributes.getString("model"));

                if (find.getType() == SyncFinding.EVENT_NEW){
                    if(className.equals("OpticalPort") || segmentDependsOfPort){
                        if(portId < 0)
                            portId = childId;
                        
                        segmentDependsOfPort = true;
                        portsToCreate.add(child.toString());
                        newCreatedPortsToCreate.put(portId, portsToCreate);
                        nameOfCreatedPorts.add(new StringPair(jsonAttributes.getString("name"), Long.toString(childId)));
                    }
                    else{
                        try{
                            if(child.getJsonObject("child").get("deviceParentId") == null){
                                parentId = createdIdsToMap.get(tempParentId);
                                if(parentId == null)
                                    parentId = tempParentId;
                            }
                            else //if we are updating a branch
                                createdIdsToMap.put(tempParentId, parentId);

                            if(!className.contains("Port") || attributes.get("name").contains("Power") || className.contains("PowerPort")){
                                long createdObjectId = bem.createObject(className, parentClassName, parentId, attributes, -1);
                                createdIdsToMap.put(childId, createdObjectId);
                                results.add(new SyncResult(find.getDataSourceId(),
                                        SyncResult.TYPE_SUCCESS, 
                                        String.format(ACTION_OBJECT_CREATED, attributes.get("name"), 
                                                className, Long.toString(createdObjectId)), "Created successfully"));
                            }
                           
                        } catch (InvalidArgumentException | BusinessObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
                            results.add(new SyncResult(find.getDataSourceId(), SyncResult.TYPE_ERROR, find.getDescription(), "Possible cause: " + ex.getMessage() + " Please check and run the sync again"));
                            break;
                        }
                    }
                }
            }
        }//end for
    }
    
    private void migrateOldPortsIntoNewPosition(JsonObject jsonPort, SyncFinding find) throws ApplicationObjectNotFoundException{
        Long childId = Long.valueOf(jsonPort.getString("childId"));
        String className = jsonPort.getString("className");
        Long tempParentId = Long.valueOf(jsonPort.getString("parentId"));
        String parentClassName = jsonPort.getString("parentClassName");
        JsonObject jsnPrtAttrs = jsonPort.getJsonObject("attributes");
        HashMap<String, String> newAttributes = new HashMap<>();
        newAttributes.put("name", jsnPrtAttrs.getString("name"));
        if(jsnPrtAttrs.get("description") != null)
            newAttributes.put("description", jsnPrtAttrs.getString("description"));
        
        HashMap<String, long[]> objectsToMove = new HashMap<>();
        long[] ids = {childId} ;
        objectsToMove.put(className, ids);
        
        try {
            Long parentId = createdIdsToMap.get(tempParentId);
            //we check if the parent of the port is already created in an old structure
            if(parentId == null && jsonPort.get("deviceParentId") != null)
                parentId = Long.valueOf(jsonPort.getString("deviceParentId"));
                
            if(parentId != null){
                //move the old port into the new location
                bem.updateObject(className, childId, newAttributes);
                bem.moveObjects(parentClassName, parentId, objectsToMove);
                results.add(new SyncResult(find.getDataSourceId(), SyncResult.TYPE_SUCCESS, 
                        String.format(ACTION_OBJECT_UPDATED, jsnPrtAttrs.get("name"), className, Long.toString(childId)), "Updated successfully"));
                parentId = childId; //the port id
                
                for(StringPair port : nameOfCreatedPorts) {
                    String portName = port.getKey();
                    if(jsnPrtAttrs.getString("name").equals(portName)){
                        List<String> slots = newCreatedPortsToCreate.get(Long.valueOf(port.getValue()));
                        for (String slot : slots) {
                            JsonReader childReader = Json.createReader(new StringReader(slot));
                            JsonObject child = childReader.readObject();
                            child = child.getJsonObject("child");
                            HashMap<String, String> attributes = new HashMap<>();
                            childId = Long.valueOf(child.getString("childId"));
                            className = child.getString("className");
                            parentClassName = child.getString("parentClassName");
                            tempParentId = Long.valueOf(child.getString("parentId"));

                            JsonObject jsonAttributes = child.getJsonObject("attributes");
                            attributes.put("name", jsonAttributes.getString("name"));
                            if(jsonAttributes.get("description") != null)
                                attributes.put("description", jsonAttributes.getString("description"));
                            if(jsonAttributes.get("serialNumber") != null)
                                attributes.put("serialNumber", jsonAttributes.getString("serialNumber"));
                            if(jsonAttributes.get("vendor") != null)
                                attributes.put("vendor", jsonAttributes.getString("vendor"));
                            if(jsonAttributes.get("model") != null)
                                attributes.put("model", jsonAttributes.getString("model"));
                            
                            if(!className.contains("Port")){
                                if(className.contains("Transceiver"))
                                    parentId = createdIdsToMap.get(tempParentId);
                                                            
                                long createdObjectId = bem.createObject(className, parentClassName, parentId, attributes, -1);
                                createdIdsToMap.put(childId, createdObjectId);
                                results.add(new SyncResult(find.getDataSourceId(), SyncResult.TYPE_SUCCESS, String.format(ACTION_OBJECT_CREATED, attributes.get("name"), className, Long.toString(createdObjectId)), "Created successfully"));
                            }
                        }
                    }
                }
            }
            else
                results.add(new SyncResult(find.getDataSourceId(), SyncResult.TYPE_WARNING, find.getDescription(), "Kuwaiba was not able to find the new parent to move the old port, please move it manually"));
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException ex) {
            results.add(new SyncResult(find.getDataSourceId(), SyncResult.TYPE_ERROR, find.getDescription()," Possible cause: " + ex.getMessage() + " Please check and run the sync again"));
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(SyncAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void createNewPorts(JsonObject json, SyncFinding find){
        HashMap<String, String> attributes = new HashMap<>();
        Long childId = Long.valueOf(json.getString("childId"));
        String className = json.getString("className");
        String parentClassName = json.getString("parentClassName");
        Long tempParentId = Long.valueOf(json.getString("parentId"));

        JsonObject jsonAttributes = json.getJsonObject("attributes");
        attributes.put("name", jsonAttributes.getString("name"));
        if(jsonAttributes.get("description") != null)
            attributes.put("description", jsonAttributes.getString("description"));
        if(jsonAttributes.get("serialNumber") != null)
            attributes.put("serialNumber", jsonAttributes.getString("serialNumber"));
        if(jsonAttributes.get("vendor") != null)
            attributes.put("vendor", jsonAttributes.getString("vendor"));
        if(jsonAttributes.get("model") != null)
            attributes.put("model", jsonAttributes.getString("model"));
        
        Long parentId;
        if(json.get("deviceParentId") != null)
            parentId = Long.valueOf(json.getString("deviceParentId"));
        else
            parentId = createdIdsToMap.get(tempParentId);
        
        if(parentId == null){
                results.add(new SyncResult(find.getDataSourceId(), SyncResult.TYPE_WARNING, find.getDescription(),  
                    "The port could not be created because the parent doesn't "
                            + "exists, please check if there is an error than "
                            + "prevented the creation of some elements in the "
                            + "synchronization process, maybe some attributes "
                            + "are missing in the classes your are tryng to "
                            + "load, please check the list of results and runs "
                            + "the synchornization again. Please check and runs the sync again"));
        }
        else{
            List<String> toCreate = newCreatedPortsToCreate.get(childId);
            if(toCreate != null){ //if the port has children
                for (String objtoCreate : toCreate) {
                    JsonReader childReader = Json.createReader(new StringReader(objtoCreate));
                    JsonObject child = childReader.readObject();
                    child = child.getJsonObject("child");
                    attributes = new HashMap<>();
                    childId = Long.valueOf(child.getString("childId"));
                    className = child.getString("className");
                    parentClassName = child.getString("parentClassName");
                    tempParentId = Long.valueOf(child.getString("parentId"));

                    jsonAttributes = child.getJsonObject("attributes");
                    attributes.put("name", jsonAttributes.getString("name"));
                    if(jsonAttributes.get("description") != null)
                        attributes.put("description", jsonAttributes.getString("description"));

                    if(jsonAttributes.get("serialNumber") != null)
                        attributes.put("serialNumber", jsonAttributes.getString("serialNumber"));
                    if(jsonAttributes.get("vendor") != null)
                        attributes.put("vendor", jsonAttributes.getString("vendor"));
                    if(jsonAttributes.get("model") != null)
                        attributes.put("model", jsonAttributes.getString("model"));
                    try{
                        if(child.get("deviceParentId") == null)
                            parentId = createdIdsToMap.get(tempParentId);
                        long createdObjectId = bem.createObject(className, parentClassName, parentId, attributes, -1);
                        createdIdsToMap.put(childId, createdObjectId);
                        results.add(new SyncResult(find.getDataSourceId(), SyncResult.TYPE_SUCCESS, String.format(ACTION_OBJECT_CREATED, attributes.get("name"), className, Long.toString(createdObjectId)), "Created successfully"));
                    } catch (InvalidArgumentException | BusinessObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
                        results.add(new SyncResult(find.getDataSourceId(), SyncResult.TYPE_WARNING, find.getDescription(), " Possible cause: " + ex.getMessage() + " Please check and run the sync again"));
                    }
                }
            }
            else{
                try{
                    long createdObjectId = bem.createObject(className, parentClassName, parentId, attributes, -1);
                    createdIdsToMap.put(childId, createdObjectId);
                    results.add(new SyncResult(find.getDataSourceId(), SyncResult.TYPE_SUCCESS, String.format(ACTION_OBJECT_CREATED, attributes.get("name"), className, Long.toString(createdObjectId)), "Created successfully"));
                } catch (InvalidArgumentException | BusinessObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
                    results.add(new SyncResult(find.getDataSourceId(), SyncResult.TYPE_WARNING, find.getDescription(), "Possible cause: " + ex.getMessage()));
                }
            }
        }
    }
    
    public enum EVENT {
        EXECUTE,
        IGNORE,
        POSTPONE
    }   
    
    private void deleteOldStructure(JsonObject json, SyncFinding find){
        JsonObject jdevice = json.getJsonObject("device");
        String className = jdevice.getString("deviceClassName");
        try {
            bem.deleteObject(className, Long.valueOf(jdevice.getString("deviceId")), false);
            results.add(new SyncResult(find.getDataSourceId(), SyncResult.TYPE_SUCCESS, String.format(ACTION_OBJECT_DELETED, jdevice.get("deviceName"), className, jdevice.getString("deviceId")), "Deleted successfully"));

        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
            results.add(new SyncResult(find.getDataSourceId(), SyncResult.TYPE_WARNING, find.getDescription(), 
                                        ex.getMessage() + "T his structure could not be deleted, because some elements has relationships (services, IP, links, etc), please check this structure and migrate ports manually.\n" +
                    "Remeber kuwaiba is able to move the ports if they have a similiar name in the current navigation tree and the data got from the SNMP, otherwise is not possible to move the ports. Please check and run the sync again"));
        }
    }
}
