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
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;

/**
 * An instance of this class define an action to be performed upon a sync finding
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SyncAction {
    
    private final static String ACTION_CONTAINMENT_HIERARCHY = "The containment hierarchy was updated parent %s, children %s";
    private final static String ACTION_OBJECT_UPDATED = "The object %s [%s] with id %s was updated";
    private final static String ACTION_OBJECT_CANNOT_UPDATED = "The object %s [%s] was not updated: ";
    private final static String ACTION_UPDATED = "Updated successfully";
    private final static String ACTION_CREATED = "Created successfully";
    private final static String ACTION_DELETED = "Deleted successfully";
    private final static String ACTION_OBJECT_CREATED = "The object %s [%s] with id %s was created";
    private final static String ACTION_OBJECT_CANNOT_CREATED = "The object %s [%s] with wasn't created";
    private final static String ACTION_OBJECT_DELETED = "The object %s [%s] with id %s was deleted";
    private final static String ACTION_LISTTYPE_CREATED = "A list type %s was created";
    private final static String ACTION_PORT_NO_MATCH = "This %s has no match, please check it manually";
    private final static String ACTION_ERROR_DELETING = "The object %s [%s] with id %s wasn't deleted";
    private final static String ACTION_ERROR = "Error";
    
    private List<SyncFinding> findings;
    private List<SyncResult> results;
    private BusinessEntityManager bem;
    private MetadataEntityManager mem;
    private HashMap<Long, Long> createdIdsToMap;
    
    private HashMap<String, String> newAttributes;
    private List<Long> portsToBeDeleted;
    private List<RemoteBusinessObjectLight> newPortsWithNoMatch;
    private HashMap<Long, List<Long>> newCreatedPorts;
    
    public SyncAction(List<SyncFinding> findings) {
        this.findings = findings;
        PersistenceService persistenceService = PersistenceService.getInstance();
        bem = persistenceService.getBusinessEntityManager();
        mem = persistenceService.getMetadataEntityManager();
        newCreatedPorts = new HashMap<>();
        results = new ArrayList<>();
        newAttributes = new HashMap<>();
        portsToBeDeleted = new ArrayList<>();
        newPortsWithNoMatch = new ArrayList<>();
        createdIdsToMap = new HashMap<>();
    }
    
    public List<SyncResult> execute() throws InvalidArgumentException{
        //Create the list type first
        for (SyncFinding finding : findings) {
            JsonObject jsonObj;
            String type;
            try (final JsonReader jsonReader = Json.createReader(new StringReader(finding.getExtraInformation()))) {
                jsonObj = jsonReader.readObject();
                type = jsonObj.getString("type");
            }
            switch (type) {
                case "hierarchy":
                    updateContaimentHiearchy(jsonObj);
                    break;
                case "listType":
                    createMissingListTypes(jsonObj);
                    break;
                case "device":
                    manageDevice(jsonObj, finding);
                    break;
                case "branch":
                    JsonArray children = jsonObj.getJsonArray("children");
                    for (JsonValue jObj : children) {
                        try (final JsonReader childReader = Json.createReader(new StringReader(jObj.toString()))) {
                            JsonObject child = childReader.readObject();
                            manageObjectOfBranch(child.getJsonObject("child"), finding);
                        }
                    }   
                    break;
                case "object_port_move":
                    migrateOldPortsIntoNewPosition(jsonObj);
                    break;
                case "branch_to_delete":
                    deleteOldStructure(jsonObj);
                    break;
                case "object_port_no_match":
                    results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_PORT_NO_MATCH, jsonObj.toString()), "No match was found, please check"));
                    break;
            }
            results.add(new SyncResult(SyncResult.SUCCESS, finding.getDescription() + " " + finding.getExtraInformation(), "No action was perfomed, the finding was skipped"));
        }
        return results;
    }
    
    
    private void updateContaimentHiearchy(JsonObject jo){
        HashMap<String, List<String>> classes = new HashMap<>();
        
        for (Map.Entry<String, JsonValue> entry : jo.entrySet()) {
            List<String> possibleChildren = classes.get(entry.getKey());
            if(possibleChildren == null)
                possibleChildren = new ArrayList<>();
            possibleChildren.add(entry.getValue().toString());
            classes.put(entry.getKey(), possibleChildren);
        }
        for (Map.Entry<String, List<String>> entrySet : classes.entrySet()) {
            String key = entrySet.getKey();
            List<String> possibleChildrenToAdd = entrySet.getValue();
            try {
                mem.addPossibleChildren(key, possibleChildrenToAdd.toArray(new String[possibleChildrenToAdd.size()]));
                results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_CONTAINMENT_HIERARCHY, key, possibleChildrenToAdd), ACTION_UPDATED));
                 
            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                Logger.getLogger(SyncAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void createMissingListTypes(JsonObject jo){
        results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_LISTTYPE_CREATED, jo.getString("name")), ACTION_CREATED));
    }
    
    private void manageDevice(JsonObject device, SyncFinding find){
        HashMap<String, String> attributes = new HashMap<>();
        long deviceId = Long.valueOf(device.getString("deviceId"));
        String deviceClassName = device.getString("deviceClassName");
        JsonObject jsonAttributes = device.getJsonObject("attributes");
        attributes.put("name", jsonAttributes.getString("name"));
        attributes.put("description", jsonAttributes.getString("description"));
        
        if(jsonAttributes.get("serialNumber") != null)
            attributes.put("serialNumber", jsonAttributes.getString("serialNumber"));
        if(jsonAttributes.get("vendor") != null)
            attributes.put("vendor", jsonAttributes.getString("vendor"));
        if(jsonAttributes.get("modelName") != null)
            attributes.put("modelName", jsonAttributes.getString("modelName")); 
        if (find.getType() == SyncFinding.EVENT_UPDATE){
            try{
                bem.updateObject(deviceClassName, deviceId, attributes);
                results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_UPDATED, attributes.get("name"), deviceClassName, Long.toString(deviceId)), ACTION_UPDATED));
            } catch (InvalidArgumentException | ObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_CANNOT_UPDATED, attributes.get("name"), deviceClassName), ACTION_ERROR));
            }
        }
    }
    
    private void manageObjectOfBranch(JsonObject child, SyncFinding find){
        
        HashMap<String, String> attributes = new HashMap<>();
        Long childId = Long.valueOf(child.getString("childId"));
        String className = child.getString("className");
        String parentClassName = child.getString("parentClassName");
        Long tempParentId = Long.valueOf(child.getString("parentId"));

        JsonObject jsonAttributes = child.getJsonObject("attributes");
        attributes.put("name", jsonAttributes.getString("name"));
        attributes.put("description", jsonAttributes.getString("description"));
        
        if(jsonAttributes.get("serialNumber") != null)
            attributes.put("serialNumber", jsonAttributes.getString("serialNumber"));
        if(jsonAttributes.get("vendor") != null)
            attributes.put("vendor", jsonAttributes.getString("vendor"));
        if(jsonAttributes.get("modelName") != null)
            attributes.put("modelName", jsonAttributes.getString("modelName"));
        //attributes.put("isLoadFromRegistry", "true");

        if (find.getType() == SyncFinding.EVENT_NEW){
            try{
                Long parentId = createdIdsToMap.get(tempParentId);
                if(parentId == null)
                    parentId = tempParentId;

                if(!className.contains("Port") || attributes.get("name").contains("Power")){
                    long createdObjectId = bem.createObject(className, parentClassName, parentId, attributes, -1);
                    createdIdsToMap.put(childId, createdObjectId);
                    results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_CREATED, attributes.get("name"), className, Long.toString(createdObjectId)), ACTION_CREATED));
                }
            } catch (InvalidArgumentException | ObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
                results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_CANNOT_CREATED, attributes.get("name"), className), ACTION_ERROR));
            }
        }
        else if (find.getType() == SyncFinding.EVENT_UPDATE){
            try{
                bem.updateObject(className, childId, attributes);
                results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_UPDATED, attributes.get("name"), className, Long.toString(-1)), ACTION_UPDATED));
            } catch (InvalidArgumentException | ObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_CANNOT_UPDATED, attributes.get("name"), className), ACTION_ERROR));
            }
        }
    }
    
    public enum EVENT {
        EXECUTE,
        IGNORE,
        POSTPONE
    }   
    
    public void migrateOldPortsIntoNewPosition(JsonObject jsonPort){
        Long childId = Long.valueOf(jsonPort.getString("childId"));
        String className = jsonPort.getString("className");
        Long tempParentId = Long.valueOf(jsonPort.getString("parentId"));
        String parentClassName = jsonPort.getString("parentClassName");
        JsonObject jsonPortAttributes = jsonPort.getJsonObject("attributes");
        
        HashMap<String, long[]> objectsToMove = new HashMap<>();
        long[] ids = {childId} ;
        objectsToMove.put(className, ids);
        try {
            Long parentId = createdIdsToMap.get(tempParentId);
            if(parentId != null){
                //move the old port into the new location
                bem.moveObjects(parentClassName, parentId, objectsToMove);
                results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_UPDATED, jsonPortAttributes.get("name"), className, Long.toString(childId)), ACTION_UPDATED));
            }
            results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_CANNOT_UPDATED + "No valid parent found", jsonPortAttributes.get("name"), className, Long.toString(childId)), ACTION_ERROR));
        } catch (MetadataObjectNotFoundException | ObjectNotFoundException | OperationNotPermittedException ex) {
            results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_CANNOT_UPDATED, jsonPortAttributes.get("name"), className, Long.toString(childId)), ACTION_ERROR));
        }
    }
    
    
    private void deleteOldStructure(JsonObject json){
        JsonArray childs = json.getJsonArray("children");
        for (JsonValue child : childs) {
            JsonReader jsonReader = Json.createReader(new StringReader(child.toString()));
            JsonObject obj = jsonReader.readObject().getJsonObject("child");
            String className = obj.getString("deviceClassName");
            try {
                bem.deleteObject(className, Long.valueOf(obj.getString("deviceId")), true);
                results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_OBJECT_DELETED, obj.get("deviceName"), className, obj.getString("deviceId")), ACTION_DELETED));
                
            } catch (ObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                results.add(new SyncResult(SyncResult.SUCCESS, String.format(ACTION_ERROR_DELETING, obj.get("deviceName"), className, obj.getString("deviceId")), ACTION_ERROR));
            }
        }
    }
}
