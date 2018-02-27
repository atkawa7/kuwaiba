/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package com.neotropic.kuwaiba.sync.model;

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
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.utils.i18n.I18N;
import org.kuwaiba.ws.todeserialize.StringPair;

/**
 * An instance of this class define an action to be performed upon a sync finding
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SyncAction {
    
    private final static String ACTION_CONTAINMENT_HIERARCHY = "The containment hierarchy was updated parent: [%s] => children: %s";
    private final static String ACTION_OBJECT_UPDATED = "The object %s [%s] with id %s was updated";
    private final static String ACTION_UPDATED = "Updated successfully";
    private final static String ACTION_CREATED = "Created successfully";
    private final static String ACTION_DELETED = "Deleted successfully";
    private final static String ACTION_OBJECT_CREATED = "The object %s [%s] with id %s was created";
    private final static String ACTION_OBJECT_DELETED = "The object %s [%s] with id %s was deleted";
    private final static String ACTION_LISTTYPE_CREATED = "A list type %s was created";
    private final static String ACTION_PORT_NO_MATCH = "This %s has no match, please check it manually";
    
    
    private List<SyncFinding> findings;
    private List<SyncResult> results;
    private BusinessEntityManager bem;
    private MetadataEntityManager mem;
    private HashMap<Long, Long> createdIdsToMap;
   
    private HashMap<Long, List<String>> newCreatedPortsToCreate;
    private List<StringPair> nameOfCreatedPorts;
        
    public SyncAction(List<SyncFinding> findings) {
        this.findings = findings;
        PersistenceService persistenceService = PersistenceService.getInstance();
        bem = persistenceService.getBusinessEntityManager();
        mem = persistenceService.getMetadataEntityManager();
        newCreatedPortsToCreate = new HashMap<>();
        results = new ArrayList<>();
        createdIdsToMap = new HashMap<>();
        nameOfCreatedPorts = new ArrayList<>();
    }
    
    public List<SyncResult> execute() throws InvalidArgumentException, ApplicationObjectNotFoundException{
        //Create the list type first
        for (SyncFinding finding : findings) {
            JsonObject jsonObj = null;
            String type = null;
            if (finding.getExtraInformation() != null) {
                try (final JsonReader jsonReader = Json.createReader(new StringReader(finding.getExtraInformation()))) {
                    jsonObj = jsonReader.readObject();
                    if (jsonObj.get("type") != null)
                        type = jsonObj.getString("type");
                }
            }
            if (type != null && jsonObj != null) {
                switch (type) {
                    case "hierarchy":
                        updateContaimentHiearchy(jsonObj.getJsonObject("hierarchy"));
                        break;
                    case "listType":
                        createMissingListTypes(jsonObj);
                        break;
                    case "device":
                        manageDevices(jsonObj, finding);
                        break;
                    case "branch":
                        manageObjectOfBranch(jsonObj, finding);
                        break;
                    case "object_port_move":
                        migrateOldPortsIntoNewPosition(jsonObj, finding);
                        break;
                    case "old_object_to_delete":
                        deleteOldStructure(jsonObj, finding);
                        break;
                    case "object_port_no_match":
                        results.add(new SyncResult(SyncResult.WARNING, String.format(ACTION_PORT_NO_MATCH, jsonObj.toString()), " There is nothing kuwaiba could do with this port, its name has no match with the data got it from SNMP, please check manually in order to move it or delete it"));
                        break;
                    case "object_port_no_match_new":
                        createNewPorts(jsonObj, finding);
                        break;    
                }
            } else {
                if (finding.getType() == SyncFinding.EVENT_ERROR)
                    results.add(new SyncResult(SyncResult.ERROR, finding.getDescription(), I18N.gm("error")));
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
    private void updateContaimentHiearchy(JsonObject jo){
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
                    results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_CONTAINMENT_HIERARCHY, key, possibleChildrenToAdd), ACTION_UPDATED));
            }
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            results.add(new SyncResult(SyncResult.ERROR, "Updating the class hierarchy", "Possible cause: " + ex.getMessage() + "Please check and run the sync again"));
        }
    }
    
    private void createMissingListTypes(JsonObject jo){
        results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_LISTTYPE_CREATED, jo.getString("name")), ACTION_CREATED));
    }
    
    private void manageDevices(JsonObject device, SyncFinding find){
        HashMap<String, String> attributes = new HashMap<>();
        long deviceId = Long.valueOf(device.getString("deviceId"));
        String deviceClassName = device.getString("deviceClassName");
        JsonObject jsonAttributes = device.getJsonObject("attributes");
        String name = "";
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
                    name = bem.getObject(deviceId).getAttributes().get("name").get(0);
                else
                    name = attributes.get("name");
                results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_UPDATED, name, deviceClassName, Long.toString(deviceId)), ACTION_UPDATED));
            } catch (InvalidArgumentException | ObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                results.add(new SyncResult(SyncResult.ERROR, find.getDescription(), "Possible cause: " + ex.getMessage() + " Please check and run the sync again"));
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
                                results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_CREATED, attributes.get("name"), className, Long.toString(createdObjectId)), ACTION_CREATED));
                            }
                           
                        } catch (InvalidArgumentException | ObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
                            results.add(new SyncResult(SyncResult.ERROR, find.getDescription(), "Possible cause: " + ex.getMessage() + " Please check and run the sync again"));
                            break;
                        }
                    }
                }
            }
        }//end for
    }
    
    public enum EVENT {
        EXECUTE,
        IGNORE,
        POSTPONE
    }   
    
    public void migrateOldPortsIntoNewPosition(JsonObject jsonPort, SyncFinding find) throws ApplicationObjectNotFoundException{
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
                results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_UPDATED, jsnPrtAttrs.get("name"), className, Long.toString(childId)), ACTION_UPDATED));
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
                                results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_CREATED, attributes.get("name"), className, Long.toString(createdObjectId)), ACTION_CREATED));
                            }
                        }
                    }
                }
            }
            else
                results.add(new SyncResult(SyncResult.WARNING, find.getDescription(), "Kuwaiba was not able to find the new parent to move the old port, please move it manually"));
        } catch (MetadataObjectNotFoundException | ObjectNotFoundException | OperationNotPermittedException ex) {
            results.add(new SyncResult(SyncResult.ERROR, find.getDescription()," Possible cause: " + ex.getMessage() + " Please check and run the sync again"));
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
                results.add(new SyncResult(SyncResult.WARNING, find.getDescription(),  
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
                        results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_CREATED, attributes.get("name"), className, Long.toString(createdObjectId)), ACTION_CREATED));
                    } catch (InvalidArgumentException | ObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
                        results.add(new SyncResult(SyncResult.WARNING, find.getDescription(), " Possible cause: " + ex.getMessage() + " Please check and run the sync again"));
                    }
                }
            }
            else{
                try{
                    long createdObjectId = bem.createObject(className, parentClassName, parentId, attributes, -1);
                    createdIdsToMap.put(childId, createdObjectId);
                    results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_CREATED, attributes.get("name"), className, Long.toString(createdObjectId)), ACTION_CREATED));
                } catch (InvalidArgumentException | ObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
                    results.add(new SyncResult(SyncResult.WARNING, find.getDescription(), "Possible cause: " + ex.getMessage()));
                }
            }
        }
    }
    
    private void deleteOldStructure(JsonObject json, SyncFinding find){
        JsonObject jdevice = json.getJsonObject("device");
        String className = jdevice.getString("deviceClassName");
        try {
            bem.deleteObject(className, Long.valueOf(jdevice.getString("deviceId")), false);
            results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_DELETED, jdevice.get("deviceName"), className, jdevice.getString("deviceId")), ACTION_DELETED));

        } catch (ObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
            results.add(new SyncResult(SyncResult.WARNING, find.getDescription(), 
                                        ex.getMessage() + "T his structure could not be deleted, because some elements has relationships (services, IP, links, etc), please check this structure and migrate ports manually.\n" +
                    "Remeber kuwaiba is able to move the ports if they have a similiar name in the current navigation tree and the data got from the SNMP, otherwise is not possible to move the ports. Please check and run the sync again"));
        }
        
    }
}
