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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;

/**
 * An instance of this class define an action to be performed upon a sync difference
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SyncAction {
    
    
    private List<SyncFinding> findings;
    private List<Boolean> actions;
    private BusinessEntityManager bem;
    private HashMap<Long, List<RemoteBusinessObjectLight>> newPorts;
    private List<RemoteBusinessObjectLight> oldPorts;
    
    private HashMap<String, String> newAttributes;
    private List<RemoteBusinessObjectLight> portsToBeDeleted;
    private List<RemoteBusinessObjectLight> newPortsWithNoMatch;
    
    public SyncAction(List<Boolean> actions, List<SyncFinding> results) {
        this.actions = actions;
        this.findings = results;
        PersistenceService persistenceService = PersistenceService.getInstance();
        bem = persistenceService.getBusinessEntityManager();
        newPorts = new HashMap<>();
        oldPorts = new ArrayList<>();
        
        newAttributes = new HashMap<>();
        portsToBeDeleted = new ArrayList<>();
        newPortsWithNoMatch = new ArrayList<>();
    }
    
    public List<String> executeResults() {
        String className;
        String parentClassName;
        Long tempParentId;
        HashMap<String, String> attributes;
        HashMap<Long, Long> createdIdsToMap = new HashMap<>();
        
        //crear primero los list types
        try {
            for (SyncFinding find : findings) {
               
                attributes = new HashMap<>();
                JsonReader jsonReader = Json.createReader(new StringReader(find.getExtraInformation()));
                JsonObject jsonObj = jsonReader.readObject();
                
                String type = jsonObj.getString("type");
                if(!type.equals("listType")){
                    Long id = Long.valueOf(jsonObj.getString("id"));
                    className = jsonObj.getString("className");
                    parentClassName = jsonObj.getString("parentClassName");
                    tempParentId = Long.valueOf(jsonObj.getString("parentId"));

                    JsonObject jsonAttributes = jsonObj.getJsonObject("attributes");
                    attributes.put("name", jsonAttributes.getString("name"));
                    attributes.put("description", jsonAttributes.getString("description"));
                    if(!jsonAttributes.getString("serialNumber").isEmpty())
                        attributes.put("serialNumber", jsonAttributes.getString("serialNumber"));
                    if(!jsonAttributes.getString("vendor").isEmpty())
                        attributes.put("vendor", jsonAttributes.getString("vendor"));
                    if(!jsonAttributes.getString("modelName").isEmpty())
                        attributes.put("modelName", jsonAttributes.getString("modelName"));
                    //attributes.put("isLoadFromRegistry", "true");

                    if (find.getType() == SyncFinding.EVENT_NEW){
                        long parentId = createdIdsToMap.get(tempParentId);
                        long createdObjectId = bem.createObject(className, parentClassName, parentId, attributes, -1);
                        createdIdsToMap.put(id, createdObjectId);

                        if(className.contains("Port")){
                            List<RemoteBusinessObjectLight> listOfCreatedPorts = newPorts.get(parentId);
                            if(listOfCreatedPorts == null)
                                listOfCreatedPorts = new ArrayList();

                            listOfCreatedPorts.add(new RemoteBusinessObject(createdObjectId, attributes.get("name"), className));
                            newPorts.put(parentId, listOfCreatedPorts);  
                        }
                    }
                    else if (find.getType() == SyncFinding.EVENT_UPDATE){
                        bem.updateObject(className, id, attributes);
                        if(tempParentId == 0l)
                            createdIdsToMap.put(1L, id);
                    }
                }
            }
            migratePorts();
        } catch (InvalidArgumentException | ObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
            Logger.getLogger(SyncAction.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    /**
     * This copy the old ports into their new locations
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws OperationNotPermittedException 
     */
    private void migratePorts() throws InvalidArgumentException,
            ObjectNotFoundException, MetadataObjectNotFoundException,
            OperationNotPermittedException
    {
        for(RemoteBusinessObjectLight oldPort : oldPorts){
            //We copy the new attributes into the new port, to keep the relationships
            Long newParentId = getNewAttributes(oldPort);
            if(newParentId != null){
                
                bem.updateObject(oldPort.getClassName(), oldPort.getId(), newAttributes);
                
                HashMap<String, Long[]> objectsToMove = new HashMap<>();
                Long[] ids = {oldPort.getId()} ;
                objectsToMove.put(oldPort.getClassName(), ids);
                RemoteBusinessObject parent = bem.getObject(newParentId);
                //move the old port into the new location
                bem.moveObjects(parent.getClassName(), newParentId, new HashMap(objectsToMove));
            }
            
        }//end for
       
        //messages.add("The ports were migrated");
        for(RemoteBusinessObjectLight p : portsToBeDeleted){
            //messages.add("trying to delete port:" + p.getName() +" - id: " + p.getId() + " after migration");
            bem.deleteObject(p.getClassName(), p.getId(), false);
        }
    }
    
    private Long getNewAttributes(RemoteBusinessObjectLight oldPort) 
            throws InvalidArgumentException, ObjectNotFoundException, 
            MetadataObjectNotFoundException
    {
        for (long parentOfNewPort : newPorts.keySet()) {
            List<RemoteBusinessObjectLight> listOfCreatedPorts = newPorts.get(parentOfNewPort);
            for (RemoteBusinessObjectLight p : listOfCreatedPorts) {
                RemoteBusinessObject tempNewPort = bem.getObject(p.getId());
                if(compareNames(oldPort.getName(), oldPort.getClassName(), p.getName(), p.getClassName()))
                {
                    HashMap<String, List<String>> tempAttributes = tempNewPort.getAttributes();
                    for(String attr : tempAttributes.keySet()){
                        List<String> listOfValues = tempAttributes.get(attr);
                        newAttributes.put(attr, listOfValues.get(0));
                    }
                    //We no longer need the new port, so we deletes the new port
                    if(!portsToBeDeleted.contains(tempNewPort))
                        portsToBeDeleted.add(tempNewPort);
                    return parentOfNewPort;
                }
                else
                    newPortsWithNoMatch.add(tempNewPort);
            }
        }
        return null;
    }
    
    public enum EVENT {
        EXECUTE,
        IGNORE,
        POSTPONE
    }   
    
    
    private boolean compareNames(String oldName, String oldClassName, 
            String newName, String newClassName)
    {
        if(oldClassName.equals(newClassName)){
            oldName = oldName.toLowerCase();
            newName = newName.toLowerCase();
            if(oldName.equals(newName))
                return true;
            else if(oldClassName.equals("ElectricalPort")){
                String[] split = oldName.split(" ");
                return newName.contains(split[0]);
            }
            else{    
                int matchCounter = 0;
                String[] splitOldName = oldName.toLowerCase().split("/");
                String[] splitNewName = newName.toLowerCase().split("/");
                int matchSuccess = splitOldName.length+1;

                if(splitOldName[0].substring(0, 1).equals(splitNewName[0].substring(0,1)))
                        matchCounter++;

                if(splitOldName[0].substring(splitOldName[0].length()-1, splitOldName[0].length())
                        .equals(splitNewName[0].substring(splitNewName[0].length()-1,splitNewName[0].length())))
                    matchCounter++;

                for(int i=splitNewName.length-1; i>0; i--){
                    for(int j= splitOldName.length-1; j>0; j--){
                        if(splitOldName[j].equals(splitNewName[i]))
                            matchCounter++;
                    }
                }
                return matchCounter >= matchSuccess;    
            }
        }
        return false;
    }
    
//    private void deleteOldStructure() throws ObjectNotFoundException, 
//            MetadataObjectNotFoundException, OperationNotPermittedException
//    {
//        messages.add("Deleteing old estructure!");
//        for(RemoteBusinessObjectLight objToDelete : oldObjectStructure){ 
//            if(objToDelete.getClassName().equals("Slot") || objToDelete.getClassName().equals("Transceiver")){
//                messages.add("trying to delete slot " + objToDelete.toString() +" and all its children id: " + objToDelete.getId() + " after migration");
//                bem.deleteObject(objToDelete.getClassName(), objToDelete.getId(), false);
//            }
//        }
//        messages.add("The old data was removed");
//    }
}
