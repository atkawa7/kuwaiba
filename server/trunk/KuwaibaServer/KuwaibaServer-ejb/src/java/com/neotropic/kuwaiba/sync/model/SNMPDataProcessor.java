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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.ws.todeserialize.StringPair;


/**
 * Loads data from a SNMP file to replace/update an existing element in 
 * the inventory
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SNMPDataProcessor {
    
    /**
     * The class name of the object
     */
    private String className;
    /**
     * Device id
     */
    private long id;
    
    private HashMap<Long, List<RemoteBusinessObjectLight>> oldObjectStructure;
    private List<RemoteBusinessObjectLight> oldPorts;
    private List<JsonObject> newPorts;
    private List<JsonObject> notMatchedPorts;
    private List<StringPair> oldBoards;
    private List<StringPair> newBoards;
    private List<StringPair> newPortsWithNoMatch;
    private List<String> listTypeEvaluated;
    private int j = 0;
    /**
     * An aux variable, used to store the branch of the old object structure while the objects are checked, before the creations of the branch
     */
    private  List<RemoteBusinessObjectLight> tempAuxOldBranch = new ArrayList<>();
    /**
     * An aux variable, used to store the branch while the objects are checked, before the creations of the branch
     */
    //private  List<JsonObject> tempAuxBranch = new ArrayList<>();
    /**
     * a map of the file to create the objects
     */
    private HashMap<String, List<String>> mapOfFile = new HashMap<>();
    /**
     * a map of the file to create the classes in the containment hierarchy
     */
    private HashMap<String, List<String>> mapOfClasses = new HashMap<>();
    /**
     * the result finding list
     */
    private List<SyncFinding> findings = new ArrayList<>();
    /**
     * The Data table loaded into the memory
     */
    private HashMap<String, List<String>> allData;
    /**
     * Default initial ParentId in the SNMP table data
     */
    private String INITAL_ID = "0";
        
    List<String> messages = new ArrayList<>();
    List<JsonObject> branch;
    
    private BusinessEntityManager bem;
    private ApplicationEntityManager aem;
    private MetadataEntityManager mem;

    public SNMPDataProcessor(String className, long id, HashMap<String, List<String>> data) {
        connect();
        this.className = className;
        this.id = id;
        allData = data;
        oldObjectStructure = new HashMap<>();
        newPorts = new ArrayList<>();
        newBoards = new ArrayList<>();
        oldBoards = new ArrayList<>();
        oldPorts = new ArrayList<>();
        listTypeEvaluated = new ArrayList<>();
        branch = new ArrayList<>();
        notMatchedPorts = new ArrayList<>();
    }
    
    public List<SyncFinding> load() throws MetadataObjectNotFoundException, 
            ObjectNotFoundException, InvalidArgumentException, 
            OperationNotPermittedException, ApplicationObjectNotFoundException
            
    {
        messages.add("Starting to map... ");
        readData();
        messages.add("Creating the class hierarchy");
        loadClassHierarchy();
        messages.add("The actual data for this element will be loaded");
        readOldDeviceStructure(id, bem.getObjectChildren(className, id, -1));
        //printData(); //<- for debuging 
        checkObjects(Long.toString(id), "", "");
        checkPortsToMigrate();
        checkDataToBeDeleted();
        checkPortsWithNoMatch();
        return findings;
    }
    
    /**
     * Reads the data loaded into memory
     */
    public void readData(){
        //we look for the Initial parent
        if(allData.get("entPhysicalContainedIn").contains(INITAL_ID))
            createTreeFromFile(INITAL_ID);
        else
            messages.add("No Chassis was found");
        removeChildrenless();
        createMapOfClasses();
    }
    
    private void printData(){
        for(String key : mapOfFile.keySet()){
            String parsedClass;
            List<String> children = mapOfFile.get(key);
            if(!key.equals("0")){
                int i = allData.get("instance").indexOf(key);
                parsedClass = parseClass(allData.get("entPhysicalClass").get(i), 
                        allData.get("entPhysicalName").get(i),
                        allData.get("entPhysicalDescr").get(i));
                System.out.println("id: " + key + " " + allData.get("entPhysicalName").get(i) + "["+parsedClass+"]");
            }
            else
                System.out.println("R A C K -> ");
            
            for (String child : children) {
                int childIndex = allData.get("instance").indexOf(child);
                parsedClass = parseClass(allData.get("entPhysicalClass").get(childIndex), 
                    allData.get("entPhysicalName").get(childIndex),
                    allData.get("entPhysicalDescr").get(childIndex));
                System.out.println("P:" + allData.get("entPhysicalContainedIn").get(childIndex) + " -id: " + child + " " + allData.get("entPhysicalName").get(childIndex) + "["+parsedClass+"]");
            }
        }
    }
    
    /**
     * Creates the hierarchy model in Kuwaiba if doesn't exist
     */
    private void loadClassHierarchy() throws MetadataObjectNotFoundException{
        HashMap<String, String[]> possibleChildren = new HashMap<>();
        for (String parentClass : mapOfClasses.keySet()) {
            List<String> possibleChildrenToAdd = mapOfClasses.get(parentClass);
            if(possibleChildrenToAdd != null){
                for(ClassMetadataLight possibleChild : mem.getPossibleChildren(parentClass)){
                    if(possibleChildrenToAdd.contains(possibleChild.getName()))
                        possibleChildrenToAdd.remove(possibleChild.getName());
                }
                if(possibleChildrenToAdd.size() > 0)
                    possibleChildren.put(parentClass, possibleChildrenToAdd.toArray(new String[possibleChildrenToAdd.size()]));
            }
        }//end for
        
        JsonObject jsonHierarchy = Json.createObjectBuilder().add("type", "hierarchy").build();
        JsonArray children = Json.createArrayBuilder().build();
        
        for (String parentClass : possibleChildren.keySet()){ 
            for(String possibleChild : possibleChildren.get(parentClass))
                children = jsonArrayToBuilder(children).add(Json.createObjectBuilder().add("child", possibleChild)).build();
        
            jsonHierarchy = jsonObjectToBuilder(jsonHierarchy).add(parentClass, children).build();
        }
        
        if(!possibleChildren.isEmpty())
            findings.add(new SyncFinding(SyncFinding.EVENT_NEW, 
                String.format("Your containment hierarchy needs to be updated in "
                        + "order to sync from SNMP this changes are need: %s  \nwould you want to update to continue?", jsonHierarchy.toString()),
                jsonHierarchy.toString()));
    }
    
    /**
     * Create into kuwaiba's objects the lines read it from the SNMP
     * @param parentId 
     * @param parentClassName
     * @param parentName
     * @param parentDesc
     * @param parentId
     * @param isFirst
     * @throws MetadataObjectNotFoundException
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws InvalidArgumentException
     * @throws ApplicationObjectNotFoundException
      */
    private void checkObjects(String parentId, String parentName, String parentClassName) 
            
            throws MetadataObjectNotFoundException, 
            ObjectNotFoundException, OperationNotPermittedException, 
            InvalidArgumentException, ApplicationObjectNotFoundException
    {
        
        if(Long.valueOf(parentId) == id)//If is the first element
            parentId = "0";
        
        List<String> childrenIds = mapOfFile.get(parentId);
        if(childrenIds != null){
            for(String childId : childrenIds){
                int i = allData.get("instance").indexOf(childId);
                parentId = allData.get("entPhysicalContainedIn").get(i);
                if(parentId.equals("1"))
                    parentId = Long.toString(id);
                //Name
                String objectName = allData.get("entPhysicalName").get(i);
                if(objectName.contains("GigabitEthernet"))
                    objectName = objectName.replace("GigabitEthernet" , "Gi");
                //We parse the class Id from SNMP into kuwaiba's class name
                String mappedClass = parseClass(allData.get("entPhysicalClass").get(i), objectName, allData.get("entPhysicalDescr").get(i));
                HashMap<String, String> newAttributes = createNewAttributes(i);
                
                //The chassis can be only updated
                if(mappedClass.contains("Router") || objectName.contains("Chassis")){
                    HashMap<String, String> comparedAttributes = compareAttributes(bem.getObject(id).getAttributes(), newAttributes);
                    
                    if(!comparedAttributes.isEmpty())
                        findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE, 
                            String.format("The chassis has changes, attributes %s has changed, would you want to update?", comparedAttributes),
                            createExtraInfoToUpdateAttributesInObject(Long.toString(id), mappedClass, comparedAttributes).toString()));
                }
                //All except the Chassis
                else{
                    JsonObject jsonNewObj = Json.createObjectBuilder()
                        .add("type", "object")
                        .add("childId", childId)
                        .add("parentId", parentId)   
                        .add("parentName", parentName) 
                        .add("parentClassName", parentClassName) 
                        .add("className", mappedClass)
                        .add("attributes", parseAttributesToJson(newAttributes))
                        .build();
                    
                    if(mappedClass.contains("Port") && !objectName.contains("Power"))
                        newPorts.add(jsonNewObj);

                    System.out.println("|__ id:" + childId + "-"+objectName+ " [" + mappedClass + "] p:" + parentId);
                    branch.add(jsonNewObj);
                    
                }
                checkObjects(childId, objectName, mappedClass);
                
                //End of a branch
                if ((i == childrenIds.size() - 1 && mapOfFile.get(childId) == null)|| mappedClass.contains("Port")) {
                    System.out.println("---------------------------------------------");
                    //The is first time is tryng to sync from SNMP
                    if(!isBranchAlreadyCreated(branch)){
                        //Loaded from snmp first time
                        findings.add(new SyncFinding(SyncFinding.EVENT_NEW, 
                                "A new branch of Slots, Boards, Port were, created the structure and ?", 
                                listToJson(branch, "branch").toString()));
                        branch = new ArrayList<>();
                    }
                }
            }//end of each
        }
    }
    
    /**
     * if is the second time that you are running the sync with the SNMP, this
     * method check the structure of the object and search for a given element.
     * @param name objName
     * @param className objClassName
     * @param serialNumber the attribute serial number of the given obj
     * @return a RemoteBusinessObjectLight with the object if exists otherwise returns null
     * @throws InvalidArgumentException
     * @throws MetadataObjectNotFoundException
     * @throws ObjectNotFoundException 
     */
    private boolean isBranchAlreadyCreated(List<JsonObject> branch) 
            throws InvalidArgumentException, MetadataObjectNotFoundException, 
            ObjectNotFoundException
    {
        boolean areFindings = false;
        for (JsonObject newObj : branch) {
            boolean hasSerialNumber = false;
            String objClassName = newObj.getString("className");
            String objParentName = newObj.getString("parentName");
            String objParentClassName = newObj.getString("parentClassName");
            JsonObject objAttributes = newObj.getJsonObject("attributes");
            String newObjName = objAttributes.getString("name");
            String serialNumber = "";
            if(objAttributes.get("serialNumber") != null){ //Is Board 
                serialNumber = objAttributes.getString("serialNumber");
                hasSerialNumber = true;
            }
            if(!className.equals(objClassName)){
                for(long i : oldObjectStructure.keySet()){
                    List<RemoteBusinessObjectLight> oldBranch = oldObjectStructure.get(i);
                    
                    for (RemoteBusinessObjectLight oldObj : oldBranch) {
                        RemoteBusinessObjectLight oldParent = bem.getParent(oldObj.getClassName(), oldObj.getId());

                        if(oldObj.getName().equals(newObjName) && oldObj.getClassName().equals(objClassName)){
                            if(oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName)){
                                if(hasSerialNumber){
                                    RemoteBusinessObject completeObj = bem.getObject(oldObj.getId());
                                    HashMap<String, List<String>> attributes = completeObj.getAttributes();
                                    if(!attributes.get("serialNumber").get(0).equals(serialNumber)){ //&& attributes.get("isLoadFromRegistry").get(0).equals("true")){
                                        findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE, 
                                        String.format("Would you want to overwrite the atributes values in the object %s, with id: %s ", oldObj.toString(), oldObj.getId()),
                                        newObj.toString()));
                                        areFindings = true;
                                        removeObjectFromDelete(oldObj);
                                    }//end
                                }   // same serial
                            }//end same parent
                            else{
                                if(hasSerialNumber){
                                    RemoteBusinessObject completeObj = bem.getObject(oldObj.getId());
                                    HashMap<String, List<String>> attributes = completeObj.getAttributes();
                                    if(!attributes.get("serialNumber").get(0).equals(serialNumber)){ //&& attributes.get("isLoadFromRegistry").get(0).equals("true")){
                                        findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE, 
                                        String.format("it seems that this object object %s, with id: %s \" has been moved ", oldObj.toString(), oldObj.getId()),
                                        newObj.toString()));
                                        areFindings = true;
                                    }//end
                                }   // same serial
                            }
                        }//end name igual
                    }//end for old objs
                }//end old branch
            }//end if not router
        }
        return areFindings;
    }
    
    /**
     * Reads the actual object and make a copy of the structure, from this 
     * structure the ports can be updated and moved to the new created tree 
     * in order to keep the special relationships.
     * @param objects the list of elements of a level
     * @throws MetadataObjectNotFoundException if something is wrong with the metadata
     * @throws ObjectNotFoundException if some object can not be find
     */
    private void readOldDeviceStructure(long parentId, List<RemoteBusinessObjectLight> objects) 
            throws MetadataObjectNotFoundException, ObjectNotFoundException
    {
        for (int i=0; i<objects.size(); i++) {
            if(!objects.get(i).getClassName().contains("Port") && !objects.get(i).getClassName().equals("ServiceInstance")){
                tempAuxOldBranch.add(objects.get(i));
                if(objects.get(i).getClassName().contains("Board"))
                    oldBoards.add(new StringPair(Long.toString(parentId), objects.get(i).getName()));
            }
            else if(objects.get(i).getClassName().contains("Port") && !objects.get(i).getClassName().contains("Virtual"))
                oldPorts.add(objects.get(i));
            
            
            List<RemoteBusinessObjectLight> children = bem.getObjectChildren(objects.get(i).getClassName(), objects.get(i).getId(), -1);
            if(!children.isEmpty())
                readOldDeviceStructure(objects.get(i).getId(), children);
            else if(i == objects.size() -1 && children.isEmpty()){
                oldObjectStructure.put(parentId, tempAuxOldBranch);
                tempAuxOldBranch = new ArrayList<>();
                j++;
            }
        }
    }
    
    /**
     * Translate the Hash map lists into a map with parentsIds and his childrenIds
     * @param parentId alleged parent Id
     */
    private void createTreeFromFile(String parentId){
        for(int i = 0; i < allData.get("entPhysicalContainedIn").size(); i++){
            if(allData.get("entPhysicalContainedIn").get(i).equals(parentId)){
                if(isClassUsed(allData.get("entPhysicalClass").get(i), 
                        allData.get("entPhysicalDescr").get(i)))
                {
                    saveInTreeMap(parentId, allData.get("instance").get(i));
                    createTreeFromFile(allData.get("instance").get(i));
                }
            }
        }
    }
    
    /**
     * Puts the data into a HashMap
     * @param parent the parent's class
     * @param child the child's classes
     */
    private void saveInTreeMap(String parent, String child){
        List<String> childrenLines = mapOfFile.get(parent);
        if(childrenLines == null){
            childrenLines = new ArrayList<>();
            childrenLines.add(child);
            mapOfFile.put(parent, childrenLines);
        }
        else
            mapOfFile.get(parent).add(child);
    }
    
    /**
     * Removes keys without children
     */
    private void removeChildrenless(){
        List<String> keysToremove = new ArrayList<>();
        for(String key : mapOfFile.keySet()){
            List<String> children = mapOfFile.get(key);
            if(children.isEmpty())
                keysToremove.add(key);
        }
        for(String key : keysToremove)
            mapOfFile.remove(key);
    }  
    
    /**
     * Creates the Hash map of classes to create the hierarchy containment
     */
    private void createMapOfClasses(){
        mapOfClasses = new HashMap<>();
        for (String key : mapOfFile.keySet()) {
            if(!key.equals("0")){
                List<String> childrenId = mapOfFile.get(key);
                int j = allData.get("instance").indexOf(key);
                String patentClassParsed = parseClass(allData.get("entPhysicalClass").get(j), 
                        allData.get("entPhysicalName").get(j), 
                        allData.get("entPhysicalDescr").get(j)
                );
                
                List<String> childrenParsed = mapOfClasses.get(patentClassParsed);
                if(childrenParsed == null)
                    childrenParsed = new ArrayList<>();

                for (String child : childrenId) {
                    int indexOfChild = allData.get("instance").indexOf(child);
                    String childParsedClass = parseClass(
                            allData.get("entPhysicalClass").get(indexOfChild), 
                            allData.get("entPhysicalName").get(indexOfChild),
                            allData.get("entPhysicalDescr").get(indexOfChild)
                    );
                    
                    if(!childrenParsed.contains(childParsedClass))
                        childrenParsed.add(childParsedClass);
                }
                mapOfClasses.put(patentClassParsed, childrenParsed);
            }
        }
    }
    
    /**
     * Creates a kuwaiba's class hierarchy from the SNMP file
     * @param className_ the given class name
     * @param name name of the element
     * @param descr description of the element
     * @return equivalent kuwaiba's class
     */        
    public String parseClass(String className_, String name, String descr){
        int classId = Integer.valueOf(className_);
        if(classId == 3) //chassis
            return className;
        else if(classId == 10){ //port
            if(name.contains("usb") || descr.contains("usb"))
                return "USBPort";
            if(descr.contains("Ethernet"))
                return "ElectricalPort";
            else
                return "OpticalPort";
        }
        else if(classId == 5){ //container
            if(!descr.contains("Disk"))
                return "Slot";
        }
        else if(classId == 6 && name.contains("Power") && !name.contains("Module"))
            return "PowerPort";
        
        else if(classId == 6 && name.contains("Module"))
            return "HybridBoard";    
        
        else if(classId == 9){ //module
            if(name.contains("transceiver") || descr.contains("transceiver"))
                return "Transceiver";
            return "IPBoard";
        }
        
        return null;
    }
    
    /**
     * returns if the class is used or not
     * @param line the line of the SNMP to extract the className and the name of
     * the element
     * @return false if is a sensor, true in the most of the cases for now
     */
    private boolean isClassUsed(String classId_, String descr){
        int classId = Integer.valueOf(classId_);
        //chassis(3) port(10) powerSupply(6) module(9) container(5) 
        if(classId == 3 || classId == 10 || classId == 6 || classId == 9)
            return true;
        else 
            return classId == 5 && !descr.trim().toLowerCase().contains("disk");
    }
    
    private void connect(){
        try{
            PersistenceService persistenceService = PersistenceService.getInstance();
            bem = persistenceService.getBusinessEntityManager();
            aem = persistenceService.getApplicationEntityManager();
            mem = persistenceService.getMetadataEntityManager();
        }catch(Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE,
                    ex.getClass().getSimpleName() + ": {0}",ex.getMessage()); //NOI18N
            bem = null;
            aem = null;
            mem = null;
        }
    }

     private JsonObject listToJson(List<JsonObject> branch, String type){
        JsonObject json = Json.createObjectBuilder().add("type", type).build();
        JsonArray children = Json.createArrayBuilder().build();
        
        for (JsonObject jo : branch) 
            children = jsonArrayToBuilder(children).add(Json.createObjectBuilder().add("child", jo)).build();
        json = jsonObjectToBuilder(json).add("children", children).build();
        
        return json;
     }
    
    /**
     * Creates the extra info need it for the finding in a JSON format
     * @param deviceId the device id
     * @param attributes the attributes to create the JSON
     * @return a json object
     */
    private JsonObject createExtraInfoToUpdateAttributesInObject(String deviceId, String deviceClassName, HashMap<String, String> attributes){
        JsonObject jsonObj = Json.createObjectBuilder().add("type","device").add("deviceId", deviceId).add("deviceClassName", deviceClassName).build();
        JsonObject jattributes = Json.createObjectBuilder().build();
        for(String key :attributes.keySet())
            jattributes = jsonObjectToBuilder(jattributes).add(key, attributes.get(key)).build();
        jsonObj = jsonObjectToBuilder(jsonObj).add("attributes", jattributes).build();
        return jsonObj;
    }
    
     /**
     * Creates the extra info need it for the finding in a JSON format
     * @param deviceId the device id
     * @param attributes the attributes to create the JSON
     * @return a json object
     */
    private JsonObject parseAttributesToJson(HashMap<String, String> attributes){
        JsonObject jsonObj = Json.createObjectBuilder().build();
        for(String key : attributes.keySet())
            jsonObj = jsonObjectToBuilder(jsonObj).add(key, attributes.get(key)).build();
        
        return jsonObj;
    }

    /**
     * Utility that allow to edit a created jSON
     * @param jo the created JSON
     * @return the edited JSON with new fields
     */
    private JsonObjectBuilder jsonObjectToBuilder(JsonObject jo) {
        JsonObjectBuilder job = Json.createObjectBuilder();

        for (Entry<String, JsonValue> entry : jo.entrySet()) 
            job.add(entry.getKey(), entry.getValue());
        
        return job;
    }
    
     /**
     * Utility that allow to edit a created jSON Array
     * @param ja the created JSON
     * @return the edited JSON with new fields
     */
    private JsonArrayBuilder jsonArrayToBuilder(JsonArray ja) {
        JsonArrayBuilder jao = Json.createArrayBuilder();
        for (JsonValue v : ja)
            jao.add(v);
        
        return jao;
    }
    
    private HashMap<String, String> createNewAttributes(int index) throws MetadataObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException{
        
        
        HashMap<String, String> attributes = new HashMap<>();
        
        String objectName = allData.get("entPhysicalName").get(index);
        //For optical ports
        if(objectName.contains("GigabitEthernet"))
            objectName = objectName.replace("GigabitEthernet" , "Gi");
        
        attributes.put("name", objectName);
        attributes.put("description", allData.get("entPhysicalDescr").get(index));
        if(!allData.get("entPhysicalMfgName").get(index).isEmpty()){
            String vendor = findingListTypeId(index);
            if(vendor != null)
                attributes.put("vendor", vendor);
        }
        if(!allData.get("entPhysicalSerialNum").get(index).isEmpty())
            attributes.put("serialNumber", allData.get("entPhysicalSerialNum").get(index));
        if(!allData.get("entPhysicalModelName").get(index).isEmpty())
            attributes.put("modelName", allData.get("entPhysicalModelName").get(index));
      return attributes;  
    }
    
    /**
     * Compares the old and the new list of attributes an return the changes
     * @param oldObjectAttributes the old list of attributes
     * @param newObjectAttributes the list read it from SNMP
     * @return the a map with the attributes changed
     */
    private HashMap<String, String> compareAttributes(
            HashMap<String, List<String>> oldObjectAttributes, 
            HashMap<String, String> newObjectAttributes)
    {
        HashMap<String, String> changesInAttributes = new HashMap<>();
        
        for(String attributeName : newObjectAttributes.keySet()){
            String newAttributeValue = newObjectAttributes.get(attributeName);
            if(oldObjectAttributes.containsKey(attributeName)){
                List<String> oldAttributeValues = oldObjectAttributes.get(attributeName);
                if(oldAttributeValues != null && newAttributeValue != null){
                    if(!oldAttributeValues.get(0).equals(newAttributeValue))
                        changesInAttributes.put(attributeName, newAttributeValue);
                }
            }
            else
                changesInAttributes.put(attributeName, newAttributeValue);//an added attribute
        }
        return changesInAttributes;
    }
    
    //<collapse List Types>
    /**
     * Returns the listTypeId if exists or creates a Finding in case that the list type doesn't exist in Kuwaiba
     * @param i the index of the list type in the SNMP table
     * @return the list type (is exist, otherwise is an empty String)
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException 
     */
    private String findingListTypeId(int i) throws MetadataObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException {
        
        if(!allData.get("entPhysicalMfgName").get(i).isEmpty()){
            String vendor = "";
            Long listTypeId = matchVendorNames(allData.get("entPhysicalMfgName").get(i));
            if(listTypeId > 0)
               vendor =  Long.toString(listTypeId);

            else{ // if the list type doesn't exist we also create a finding
                if(!listTypeEvaluated.contains(allData.get("entPhysicalMfgName").get(i))){
                    long createListTypeItem = aem.createListTypeItem("EquipmentVendor",allData.get("entPhysicalMfgName").get(i), allData.get("entPhysicalMfgName").get(i));
                    
                    findings.add(new SyncFinding(SyncFinding.EVENT_ERROR,
                    "The list type: " + allData.get("entPhysicalMfgName").get(i) + " it was created", 
                    Json.createObjectBuilder()
                            .add("type", "listType")
                            .add("name", allData.get("entPhysicalMfgName").get(i))
                            .build().toString()));
                    
                    listTypeEvaluated.add(allData.get("entPhysicalMfgName").get(i));
                    return Long.toString(createListTypeItem);
                }
            }
            return vendor;
        }
        return null;
    }
    
    /**
     * Compare the names from the SNMP file in order to find one that match with 
     * a created list item in kuwaiba
     * @param listTypeNameToLoad the list type name
     * @return the kuwaiba's list type item id
     * @throws MetadataObjectNotFoundException 
     * @throws InvalidArgumentException 
     */
    private long matchVendorNames(String listTypeNameToLoad) throws MetadataObjectNotFoundException, InvalidArgumentException{
        List<RemoteBusinessObjectLight> listTypeItems = aem.getListTypeItems("EquipmentVendor");
        for(RemoteBusinessObjectLight createdLitType : listTypeItems){
            int matches = 0;
            int maxLength = listTypeNameToLoad.length() > createdLitType.getName().length() ? listTypeNameToLoad.length() : createdLitType.getName().length();
            listTypeNameToLoad = listTypeNameToLoad.toLowerCase().trim();
            String nameCreatedInKuwaiba = createdLitType.getName().toLowerCase().trim();
            if(listTypeNameToLoad.equals(nameCreatedInKuwaiba))
                return createdLitType.getId();
            for (int i=1; i< maxLength; i++){
                String a, b;
                if(listTypeNameToLoad.length() < i )
                    break;
                else
                    a = listTypeNameToLoad.substring(i-1, i);
                if(nameCreatedInKuwaiba.length() < i )
                    break;
                else
                    b = nameCreatedInKuwaiba.substring(i-1, i);
                if(a.equals(b))
                    matches++;
            }
            if(matches == listTypeNameToLoad.length())
                return createdLitType.getId();
        }
        return -1;
    }
    
    
    
    //Things to be deleted
    public void removeObjectFromDelete(RemoteBusinessObjectLight obj){
        for(long branchId : oldObjectStructure.keySet())
            oldObjectStructure.get(branchId).remove(obj);
    }
    
    public void checkDataToBeDeleted(){
        for(long branchId : oldObjectStructure.keySet()){
            JsonObject json = Json.createObjectBuilder().build();
            List<JsonObject> toDelete = new ArrayList<>();
            String path = "";
            
            for (RemoteBusinessObjectLight obj : oldObjectStructure.get(branchId)) {
                if(branchId == id){
                    json = jsonObjectToBuilder(json)
                            .add("type", "old_object_to_delete")
                            .add("deviceId", Long.toString(obj.getId()))
                            .add("deviceName", obj.getName())
                            .add("deviceClassName", obj.getClassName()) 
                            .build();
                    toDelete.add(json);
                    path += obj.getName()+"/";

                    findings.add(new SyncFinding(SyncFinding.EVENT_DELETE, 
                        String.format("Old objects will be deleted, this branch is no longer updated %s",path),
                        listToJson(toDelete, "branch_to_delete").toString()));
                }
            }
        }
    }
    
    public void checkPortsWithNoMatch(){
        for (JsonObject jnewPort : notMatchedPorts) {
            jnewPort = jsonObjectToBuilder(jnewPort).add("type", "object_port_no_match").build();
            
            findings.add(new SyncFinding(SyncFinding.EVENT_ERROR, 
                String.format("There was no match with this old port, check the name %s  ", jnewPort.toString()),
                jnewPort.toString()));
        }
    }
    
    //Ports
    /**
     * Compare the old port names with the new name, the first load of the SNMP 
     * sync depends of the name of the ports because this names are the only 
     * common stating point to begin the search and creation of the device structure 
     * @param oldName the old port name
     * @param oldClassName the old port class 
     * @param newName the new port name
     * @param newClassName the new port class 
     * @return boolean if the name match
     */
    private boolean comparePortNames(String oldName, String oldClassName, 
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
    
    /**
     * This copy the old ports into their new locations
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws OperationNotPermittedException 
     */
    private void checkPortsToMigrate() throws InvalidArgumentException,
            ObjectNotFoundException, MetadataObjectNotFoundException,
            OperationNotPermittedException
    {
        List<RemoteBusinessObjectLight> foundOldPorts = new ArrayList<>();
        List<JsonObject> foundNewPorts = new ArrayList<>();
        for(RemoteBusinessObjectLight oldPort : oldPorts){
            //We copy the new attributes into the new port, to keep the relationships
            JsonObject portInfo = searchOldPortInNewPorts(oldPort);
            if(portInfo != null){
                JsonObject jsonPortToMove = searchOldPortInNewPorts(oldPort);
                if(jsonPortToMove != null){
                    jsonPortToMove = jsonObjectToBuilder(jsonPortToMove).add("type", "object_port_move").build();
                    jsonPortToMove = jsonObjectToBuilder(jsonPortToMove).add("chilId", Long.toString(oldPort.getId())).build();

                    findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE, 
                                    String.format("Would you want to overwrite the atributes values in the port %s, with id: %s ", oldPort.toString(), oldPort.getId()),
                                    jsonPortToMove.toString()));
                    foundOldPorts.add(oldPort);
                    foundNewPorts.add(portInfo);
                }
                else
                    notMatchedPorts.add(Json.createObjectBuilder()
                            .add("id",Long.toString(oldPort.getId()))
                            .add("name", oldPort.getName())
                            .add("className",oldPort.getClassName())
                            .build());
            }
        }//end for
        for (RemoteBusinessObjectLight goodPort : foundOldPorts)
            oldPorts.remove(goodPort);
        
        for (RemoteBusinessObjectLight badPort : oldPorts) 
            findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE, 
                                String.format("Something went wrong with this ports %s, with id: %s , can't suggest action inspect manually", badPort.toString(), badPort.getId()),""));
    }
    
    /**
     * Given an old port search for it in the list of the new created ports, by name 
     * @param oldPort the old port
     * @return a String pair key = The new parent Id (in the SNMP map) value = the new port 
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     * @throws MetadataObjectNotFoundException 
     */
    private JsonObject searchOldPortInNewPorts(RemoteBusinessObjectLight oldPort) throws InvalidArgumentException, ObjectNotFoundException, MetadataObjectNotFoundException{
        for (JsonObject jsonPort : newPorts) {
            JsonObject jsonAttributes = jsonPort.getJsonObject("attributes");
            String newPortName = jsonAttributes.getString("name");
            
            if(comparePortNames(oldPort.getName(), oldPort.getClassName(), newPortName, jsonPort.getString("className")))
                return jsonPort;
        }
        return null;
    }
}
