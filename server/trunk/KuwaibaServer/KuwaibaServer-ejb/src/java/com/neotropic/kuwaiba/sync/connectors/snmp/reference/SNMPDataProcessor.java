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
package com.neotropic.kuwaiba.sync.connectors.snmp.reference;

import com.neotropic.kuwaiba.sync.model.SyncFinding;
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
 * Loads data from a SNMP file to replace/update an existing element in the
 * inventory
 *
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
    /**
     * To load the structure of the actual device
     */
    private HashMap<Long, List<RemoteBusinessObjectLight>> oldObjectStructure;
    /**
     * The actual first level children of the actual device
     */
    private List<RemoteBusinessObjectLight> actualFirstLevelChildren;
    /**
     * The actual ports of the device
     */
    private List<RemoteBusinessObjectLight> oldPorts;
    /**
     * To keep a trace of the new ports created during synchronization
     */
    private List<JsonObject> newPorts;
    /**
     * The ports of the device before the synchronization
     */
    private List<JsonObject> notMatchedPorts;
    /**
     * The boards of the device before the synchronization
     */
    private List<StringPair> oldBoards;
    /**
     * To keep a trace of the new boards created during synchronization
     */
    private List<StringPair> newBoards;
    /**
     * To keep a trace of the list types evaluated, to not create them twice
     */
    private List<String> listTypeEvaluated;
    /**
     * An aux variable, used to store the branch of the old object structure
     * while the objects are checked, before the creations of the branch
     */
    private List<RemoteBusinessObjectLight> tempAuxOldBranch = new ArrayList<>();
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

    /**
     * To keep the objects during synchronization
     */
    private List<JsonObject> branch;

    /**
     * the device that we are updating
     */
    RemoteBusinessObjectLight obj;
    /**
     * reference to the bem
     */
    private BusinessEntityManager bem;
    /**
     * Reference to de aem
     */
    private ApplicationEntityManager aem;
    /**
     * Reference to de mem
     */
    private MetadataEntityManager mem;

    public SNMPDataProcessor(RemoteBusinessObjectLight obj, HashMap<String, List<String>> data) {
        try {
            PersistenceService persistenceService = PersistenceService.getInstance();
            bem = persistenceService.getBusinessEntityManager();
            aem = persistenceService.getApplicationEntityManager();
            mem = persistenceService.getMetadataEntityManager();
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE,
                    ex.getClass().getSimpleName() + ": {0}", ex.getMessage()); //NOI18N
            bem = null;
            aem = null;
            mem = null;
        }
        this.obj = obj;
        this.className = obj.getClassName();
        this.id = obj.getId();
        allData = data;
        oldObjectStructure = new HashMap<>();
        newPorts = new ArrayList<>();
        newBoards = new ArrayList<>();
        oldBoards = new ArrayList<>();
        oldPorts = new ArrayList<>();
        listTypeEvaluated = new ArrayList<>();
        branch = new ArrayList<>();
        notMatchedPorts = new ArrayList<>();
        actualFirstLevelChildren = new ArrayList<>();
    }

    public List<SyncFinding> load() throws MetadataObjectNotFoundException,
            ObjectNotFoundException, InvalidArgumentException,
            OperationNotPermittedException, ApplicationObjectNotFoundException {
        readData();
        loadClassHierarchy();
        readActualFirstLevelChildren();
        readActualDeviceStructure(id, bem.getObjectChildren(className, id, -1));
        //printData(); //<- for debuging 
        checkObjects(Long.toString(id), "", "");
        checkPortsToMigrate();
        checkDataToBeDeleted();
        checkPortsWithNoMatch();
        return findings;
    }

    /**
     * Reads the data loaded into memory
     *
     * @throws InvalidArgumentException if the table info load is corrupted and
     * has no chassis
     */
    public void readData() throws InvalidArgumentException {
        //we look for the Initial parent
        if (allData.get("entPhysicalContainedIn").contains(INITAL_ID))
            createTreeFromFile(INITAL_ID);
        else 
            findings.add(new SyncFinding(SyncFinding.EVENT_ERROR,
                                            String.format("No Chassis was found in the SNMP sync"),
                                            "no extra infromation"));
        
        removeChildrenless();
        createMapOfClasses();
    }

//<editor-fold desc="for debuging" defaultstate="collapsed">
//    private void printData(){
//        for(String key : mapOfFile.keySet()){
//            String parsedClass;
//            List<String> children = mapOfFile.get(key);
//            if(!key.equals("0")){
//                int i = allData.get("instance").indexOf(key);
//                parsedClass = parseClass(allData.get("entPhysicalClass").get(i), 
//                        allData.get("entPhysicalName").get(i),
//                        allData.get("entPhysicalDescr").get(i));
//                System.out.println("id: " + key + " " + allData.get("entPhysicalName").get(i) + "["+parsedClass+"]");
//            }
//            else
//                System.out.println("R A C K -> ");
//            
//            for (String child : children) {
//                int childIndex = allData.get("instance").indexOf(child);
//                parsedClass = parseClass(allData.get("entPhysicalClass").get(childIndex), 
//                    allData.get("entPhysicalName").get(childIndex),
//                    allData.get("entPhysicalDescr").get(childIndex));
//                System.out.println("P:" + allData.get("entPhysicalContainedIn").get(childIndex) + " -id: " + child + " " + allData.get("entPhysicalName").get(childIndex) + "["+parsedClass+"]");
//            }
//        }
//    }
//</editor-fold>
    
    /**
     * Creates the hierarchy model in Kuwaiba if doesn't exist
     * Json 
     * {type:hierarchy
     *  hierarchy:{
     *       slot[{"child":"Transceiver"},{"child":"IPBoard"},{"child":"HybridBoard"}], 
     *       parenClass, [{child:possibleChild},...]
     *    }
     * }
     */
    private void loadClassHierarchy() throws MetadataObjectNotFoundException {
        JsonObject jHierarchyFinding = Json.createObjectBuilder().add("type", "hierarchy").build();

        JsonObject jsonHierarchy = Json.createObjectBuilder().build();
        for (String parentClass : mapOfClasses.keySet()) {
            
            List<ClassMetadataLight> actualPossibleChildren = mem.getPossibleChildren(parentClass);
            List<String> possibleChildrenToAdd = mapOfClasses.get(parentClass);
            
            if (possibleChildrenToAdd != null) {
                JsonArray children = Json.createArrayBuilder().build();
                for (String possibleChildToAdd : possibleChildrenToAdd){
                    boolean isPossibleChild = false;
                    for(ClassMetadataLight actualPossibleClassName : actualPossibleChildren){
                        if(possibleChildToAdd.equals(actualPossibleClassName.getName())){
                            isPossibleChild = true;
                            break;
                        }
                    }        
                    
                    if(!isPossibleChild){    
                        JsonObject jchild = Json.createObjectBuilder().add("child", possibleChildToAdd).build();
                        children = jsonArrayToBuilder(children).add(jchild).build();    
                    }
                    
                }
                if(!children.isEmpty())
                    jsonHierarchy = jsonObjectToBuilder(jsonHierarchy).add(parentClass, children).build();  
            }
        }//end for
        jHierarchyFinding = jsonObjectToBuilder(jHierarchyFinding).add("hierarchy",jsonHierarchy).build();
        
        if(!jsonHierarchy.isEmpty()){
            jHierarchyFinding = jsonObjectToBuilder(jHierarchyFinding).add("hierarchy",jsonHierarchy).build();

            findings.add(new SyncFinding(SyncFinding.EVENT_NEW,
                    String.format("Your containment hierarchy needs to be updated like this %s\nDo you want to proceed?", jsonHierarchy.toString()),
                    jHierarchyFinding.toString()));
        }
    }

    /**
     * Create into kuwaiba's objects the lines read it from the SNMP
     *
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
            InvalidArgumentException, ApplicationObjectNotFoundException {

        if (Long.valueOf(parentId) == id)//If is the first element
            parentId = "0";

        List<String> childrenIds = mapOfFile.get(parentId);
        if (childrenIds != null) {
            for (String childId : childrenIds) {
                boolean isAlreadyCreated = false;
            
                int i = allData.get("instance").indexOf(childId);
                parentId = allData.get("entPhysicalContainedIn").get(i);
                if (parentClassName.equals(className)) //if is the chassis 
                    parentId = Long.toString(id);

                String objectName = allData.get("entPhysicalName").get(i);
                if (objectName.contains("GigabitEthernet"))
                    objectName = objectName.replace("GigabitEthernet", "Gi");
                
                //We parse the class Id from SNMP into kuwaiba's class name
                String mappedClass = parseClass(allData.get("entPhysicalClass").get(i), objectName, allData.get("entPhysicalDescr").get(i));
                HashMap<String, String> newAttributes = createNewAttributes(i);
                if(mappedClass == null)
                    findings.add(new SyncFinding(SyncFinding.EVENT_ERROR,
                                String.format("The data your are trying to load has empty fields, the ClassName of the row with id: %s - %s "
                                        + "could not be determined due to these empty fields, the -entPhysicalDescr- or the -entPhysicalName- maybe are empty,  please check the data you are trying to load", childId, newAttributes),
                                null));
                //it was impossible to parse the SNMP class into kuwaiba's class
                else{
                    //The chassis can be only updated
                    if (className.contains(mappedClass)) {
                        newAttributes.remove("name"); //the router name won't be changed
                        HashMap<String, String> comparedAttributes = compareAttributes(bem.getObject(id).getAttributes(), newAttributes);
                        if (!comparedAttributes.isEmpty()) {
                            comparedAttributes.put("name", obj.getName());
                            findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE,
                                    String.format("The chassis has changes, attributes %s have changed, would you like to update it?", comparedAttributes),
                                    createExtraInfoToUpdateAttributesInObject(Long.toString(id), mappedClass, comparedAttributes).toString()));
                        }
                    }//All except the Chassis
                    else {
                        JsonObject jsonNewObj = Json.createObjectBuilder()
                                .add("type", "object")
                                .add("childId", childId)
                                .add("parentId", parentId)
                                .add("parentName", parentName)
                                .add("parentClassName", parentClassName)
                                .add("className", mappedClass)
                                .add("attributes", parseAttributesToJson(newAttributes))
                                .build();

                        if (mappedClass.contains("Port") && !objectName.contains("Power") && !mappedClass.contains("Power"))
                            newPorts.add(jsonNewObj);

                        //check if is already created
                        isAlreadyCreated = isDeviceAlreadyCreated(jsonNewObj, newAttributes);
                        if(!isAlreadyCreated)
                            branch.add(jsonNewObj);

                    }
                    checkObjects(childId, objectName, mappedClass);

                    //End of a branch
                    if (!isAlreadyCreated && ((i == childrenIds.size() - 1 && mapOfFile.get(childId) == null) || mappedClass.contains("Port"))) {
                        //The is first time is tryng to sync from SNMP
                        if (!isBranchAlreadyCreated(branch)) {
                            //Loaded from snmp first time
                            findings.add(new SyncFinding(SyncFinding.EVENT_NEW,
                                    "A new branch was found. Do you want to create the structure for this branch?",
                                    listToJson(branch, "branch").toString()));
                        }
                        branch = new ArrayList<>();
                    }
                }//end of each
            }
        }
    }

    /**
     * if is the second time that you are running the sync with the SNMP, this
     * method check the structure of the object and search for a given element.
     *
     * @param name objName
     * @param className objClassName
     * @param serialNumber the attribute serial number of the given object
     * @return a RemoteBusinessObjectLight with the object if exists otherwise
     * returns null
     * @throws InvalidArgumentException
     * @throws MetadataObjectNotFoundException
     * @throws ObjectNotFoundException
     */
    private boolean isBranchAlreadyCreated(List<JsonObject> branch)
            throws InvalidArgumentException, MetadataObjectNotFoundException,
            ObjectNotFoundException {
        int numberOfMatch = 0;
        for (JsonObject newObj : branch) {
            boolean hasSerialNumber = false;
            String objClassName = newObj.getString("className");
            String objParentName = newObj.getString("parentName");
            String objParentClassName = newObj.getString("parentClassName");
            JsonObject objAttributes = newObj.getJsonObject("attributes");
            String newObjName = objAttributes.getString("name");
            String serialNumber = "";
            if (objAttributes.get("serialNumber") != null) { //Is Board 
                serialNumber = objAttributes.getString("serialNumber");
                hasSerialNumber = true;
            }
            if (!className.equals(objClassName)) {
                for (long i : oldObjectStructure.keySet()) {
                    List<RemoteBusinessObjectLight> oldBranch = oldObjectStructure.get(i);

                    for (RemoteBusinessObjectLight oldObj : oldBranch) {
                        
                        RemoteBusinessObjectLight oldParent = bem.getParent(oldObj.getClassName(), oldObj.getId());
                        RemoteBusinessObject completeObj = bem.getObject(oldObj.getId());
                        HashMap<String, List<String>> attributes = completeObj.getAttributes();
                        String oldSerialNumber = "";
                        if(attributes.get("serialNumber") != null)
                            oldSerialNumber = attributes.get("serialNumber").get(0);
                        HashMap<String, String> changes = compareAttributes(completeObj.getAttributes(), objAttributes);
                        if (oldObj.getName().equals(newObjName) && oldObj.getClassName().equals(objClassName)) {
                            //has the same parent
                            if(objParentClassName.equals(className)){
                                objParentName = oldParent.getName();
                                numberOfMatch++;
                            }
                            if (oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName)) {
                                
                                if (hasSerialNumber) {
                                    if (oldSerialNumber.equals(serialNumber)) {
                                        if(!changes.isEmpty()){
                                            newObj = jsonObjectToBuilder(newObj).add("deviceId", Long.toString(oldObj.getId())).build();
                                            findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE,
                                                    String.format("Would you like to overwrite the attributes values in the object %s, with id: %s ", oldObj.toString(), oldObj.getId()),
                                                    newObj.toString()));
                                        }

                                    }//end
                                }// has serial
                                else{
                                    if(!changes.isEmpty()){
                                        newObj = jsonObjectToBuilder(newObj).add("deviceId", Long.toString(oldObj.getId())).build();
                                        findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE,
                                                String.format("Would you like to overwrite the attributes values in the object %s, with id: %s ", oldObj.toString(), oldObj.getId()),
                                                newObj.toString()));
                                    }
                                }
                            }//end same parent
                            else 
                                break;
                            
                            numberOfMatch++;
                        }//end name igual
                    }//end for old objs
                }//end old branch
            }//end if not router
            
            if(objClassName.contains("Port"))
                numberOfMatch++;
        }
        return (branch.size() == numberOfMatch);
    }
    
    /**
     * checks if the first level of children has changes
     * @param json the new object
     * @return true if is already created
     */
    private boolean isDeviceAlreadyCreated(JsonObject json, HashMap<String, String> newAttributes){
        RemoteBusinessObjectLight objFound = null;
        for (RemoteBusinessObjectLight actualFirstLevelChild : actualFirstLevelChildren) {
            if(actualFirstLevelChild.getClassName().equals(json.getString("className")) && 
                    actualFirstLevelChild.getName().equals(json.getJsonObject("attributes").getString("name"))){
                objFound = actualFirstLevelChild;
                break;
            }
        }   
        if(objFound != null)
            actualFirstLevelChildren.remove(objFound);
        return false;
    }

    /**
     * Reads the actual object and make a copy of the structure, from this
     * structure the ports can be updated and moved to the new created tree in
     * order to keep the special relationships.
     * @param objects the list of elements of a level
     * @throws MetadataObjectNotFoundException if something goes wrong with the class metadata
     * @throws ObjectNotFoundException if some object can not be find
     */
    private void readActualDeviceStructure(long parentId, List<RemoteBusinessObjectLight> objects)
            throws MetadataObjectNotFoundException, ObjectNotFoundException {
        for (int i = 0; i < objects.size(); i++) {
            if (!objects.get(i).getClassName().contains("Port") && !objects.get(i).getClassName().equals("ServiceInstance")) {
                tempAuxOldBranch.add(objects.get(i));
                if (objects.get(i).getClassName().contains("Board")) 
                    oldBoards.add(new StringPair(Long.toString(parentId), objects.get(i).getName()));
                
            } else if (objects.get(i).getClassName().contains("Port") && !objects.get(i).getClassName().contains("Virtual") && !objects.get(i).getClassName().contains("Power"))
                oldPorts.add(objects.get(i));

            List<RemoteBusinessObjectLight> children = bem.getObjectChildren(objects.get(i).getClassName(), objects.get(i).getId(), -1);
            if (!children.isEmpty())
                readActualDeviceStructure(objects.get(i).getId(), children);
            else if (i == objects.size() - 1 && children.isEmpty()) {
                oldObjectStructure.put(parentId, tempAuxOldBranch);
                tempAuxOldBranch = new ArrayList<>();
            }
        }
    }

    private void readActualFirstLevelChildren() {
        try {
            actualFirstLevelChildren = bem.getObjectChildren(className, id, -1);
        } catch (MetadataObjectNotFoundException | ObjectNotFoundException ex) {
            Logger.getLogger(SNMPDataProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Translate the Hash map lists into a map with parentsIds and his
     * childrenIds
     *
     * @param parentId alleged parent Id
     */
    private void createTreeFromFile(String parentId) {
        for (int i = 0; i < allData.get("entPhysicalContainedIn").size(); i++) {
            if (allData.get("entPhysicalContainedIn").get(i).equals(parentId)) {
                if (isClassUsed(allData.get("entPhysicalClass").get(i),
                        allData.get("entPhysicalDescr").get(i))) {
                    saveInTreeMap(parentId, allData.get("instance").get(i));
                    createTreeFromFile(allData.get("instance").get(i));
                }
            }
        }
    }

    /**
     * Puts the data into a HashMap
     *
     * @param parent the parent's class
     * @param child the child's classes
     */
    private void saveInTreeMap(String parent, String child) {
        List<String> childrenLines = mapOfFile.get(parent);
        if (childrenLines == null) {
            childrenLines = new ArrayList<>();
            childrenLines.add(child);
            mapOfFile.put(parent, childrenLines);
        } else {
            mapOfFile.get(parent).add(child);
        }
    }

    /**
     * Removes keys without children
     */
    private void removeChildrenless() {
        List<String> keysToremove = new ArrayList<>();
        for (String key : mapOfFile.keySet()) {
            List<String> children = mapOfFile.get(key);
            if (children.isEmpty()) {
                keysToremove.add(key);
            }
        }
        for (String key : keysToremove) {
            mapOfFile.remove(key);
        }
    }

    /**
     * Creates the Hash map of classes to create the hierarchy containment
     */
    private void createMapOfClasses() {
        mapOfClasses = new HashMap<>();
        for (String key : mapOfFile.keySet()) {
            if (!key.equals("0")) {
                List<String> childrenId = mapOfFile.get(key);
                int w = allData.get("instance").indexOf(key);
                String patentClassParsed = parseClass(allData.get("entPhysicalClass").get(w),
                        allData.get("entPhysicalName").get(w),
                        allData.get("entPhysicalDescr").get(w)
                );

                if(patentClassParsed != null){
                    List<String> childrenParsed = mapOfClasses.get(patentClassParsed);
                    if (childrenParsed == null) 
                        childrenParsed = new ArrayList<>();

                    for (String child : childrenId) {
                        int indexOfChild = allData.get("instance").indexOf(child);
                        String childParsedClass = parseClass(
                                allData.get("entPhysicalClass").get(indexOfChild),
                                allData.get("entPhysicalName").get(indexOfChild),
                                allData.get("entPhysicalDescr").get(indexOfChild)
                        );

                        if(childParsedClass != null && !childrenParsed.contains(childParsedClass))
                            childrenParsed.add(childParsedClass);
                    }
                    mapOfClasses.put(patentClassParsed, childrenParsed);
                }
            }
        }
    }

    /**
     * Creates a kuwaiba's class hierarchy from the SNMP file
     *
     * @param className_ the given class name
     * @param name name of the element
     * @param descr description of the element
     * @return equivalent kuwaiba's class
     */
    public String parseClass(String className_, String name, String descr) {
        if (descr.isEmpty() || className_.isEmpty() ||  name.isEmpty())
            return null;
        
        int classId = Integer.valueOf(className_);
        if (classId == 3) //chassis
            return className;
        else if (classId == 10) { //port
            if (name.toLowerCase().contains("usb") || descr.toLowerCase().contains("usb")) 
                return "USBPort";
            
            if (name.toLowerCase().contains("mgmteth") || name.toLowerCase().contains("cpu") || name.toLowerCase().contains("control") ||
                    (descr.toLowerCase().contains("ethernet") && !descr.toLowerCase().contains("gigabit")) ||
                    descr.toLowerCase().contains("fast")) 
                return "ElectricalPort";
            else 
                return "OpticalPort";
            
        } else if (classId == 5) { //container
            if (!descr.contains("Disk")) 
                return "Slot";
            
        } else if (classId == 6 && name.contains("Power") && !name.contains("Module")) 
            return "PowerPort";
        else if (classId == 6 && name.contains("Module")) 
            return "HybridBoard";
        else if (classId == 9) { //module
            if (name.contains("transceiver") || descr.contains("transceiver") || descr.toLowerCase().contains("sfp") 
                    || descr.toLowerCase().contains("xfp") || descr.toLowerCase().contains("cpak") || descr.toLowerCase().equals("ge t")) 
                return "Transceiver";
            
            return "IPBoard";
        }
        return null;
    }

    /**
     * returns if the class is used or not
     *
     * @param line the line of the SNMP to extract the className and the name of
     * the element
     * @return false if is a sensor, true in the most of the cases for now
     */
    private boolean isClassUsed(String classId_, String descr) {
        int classId = Integer.valueOf(classId_);
        //chassis(3) port(10) powerSupply(6) module(9) container(5) 
        if (classId == 3 || classId == 10 || classId == 6 || classId == 9)
            return true;
        else 
            return classId == 5 && !descr.trim().toLowerCase().contains("disk");
    }

    private JsonObject listToJson(List<JsonObject> branch, String type) {
        JsonObject json = Json.createObjectBuilder().add("type", type).build();
        JsonArray children = Json.createArrayBuilder().build();

        for (JsonObject jo : branch) 
            children = jsonArrayToBuilder(children).add(Json.createObjectBuilder().add("child", jo)).build();
        
        json = jsonObjectToBuilder(json).add("children", children).build();

        return json;
    }

    /**
     * Creates the extra info need it for the finding in a JSON format
     *
     * @param deviceId the device id
     * @param attributes the attributes to create the JSON
     * @return a json object
     */
    private JsonObject createExtraInfoToUpdateAttributesInObject(String deviceId, String deviceClassName, HashMap<String, String> attributes) {
        JsonObject jsonObj = Json.createObjectBuilder().add("type", "device").add("deviceId", deviceId).add("deviceClassName", deviceClassName).build();
        JsonObject jattributes = Json.createObjectBuilder().build();
        for (String key : attributes.keySet()) 
            jattributes = jsonObjectToBuilder(jattributes).add(key, attributes.get(key)).build();
        
        jsonObj = jsonObjectToBuilder(jsonObj).add("attributes", jattributes).build();
        return jsonObj;
    }

    /**
     * Creates the extra info need it for the finding in a JSON format
     *
     * @param deviceId the device id
     * @param attributes the attributes to create the JSON
     * @return a json object
     */
    private JsonObject parseAttributesToJson(HashMap<String, String> attributes) {
        JsonObject jsonObj = Json.createObjectBuilder().build();
        for (String key : attributes.keySet()) 
            jsonObj = jsonObjectToBuilder(jsonObj).add(key, attributes.get(key)).build();

        return jsonObj;
    }

    /**
     * Utility that allow to edit a created jSON
     *
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
     *
     * @param ja the created JSON
     * @return the edited JSON with new fields
     */
    private JsonArrayBuilder jsonArrayToBuilder(JsonArray ja) {
        JsonArrayBuilder jao = Json.createArrayBuilder();
        for (JsonValue v : ja) 
            jao.add(v);
        
        return jao;
    }

    private HashMap<String, String> createNewAttributes(int index) throws MetadataObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException {
        HashMap<String, String> attributes = new HashMap<>();

        String objectName = allData.get("entPhysicalName").get(index);
        //For optical ports
        if (objectName.contains("GigabitEthernet")) 
            objectName = objectName.replace("GigabitEthernet", "Gi");

        attributes.put("name", objectName);
        attributes.put("description", allData.get("entPhysicalDescr").get(index));
        if (!allData.get("entPhysicalMfgName").get(index).isEmpty()) {
            String vendor = findingListTypeId(index);
            if (vendor != null) 
                attributes.put("vendor", vendor);
        }
        if (!allData.get("entPhysicalSerialNum").get(index).isEmpty()) 
            attributes.put("serialNumber", allData.get("entPhysicalSerialNum").get(index));
        
        if (!allData.get("entPhysicalModelName").get(index).isEmpty()) 
            attributes.put("modelName", allData.get("entPhysicalModelName").get(index));
        
        return attributes;
    }

    /**
     * Compares the old and the new list of attributes an return the changes
     * @param oldObjectAttributes the old list of attributes
     * @param newObjectAttributes the list read it from SNMP
     * @return the a map with the attributes changed
     */
    private HashMap<String, String> compareAttributes(HashMap<String, List<String>> oldObjectAttributes, HashMap<String, String> newObjectAttributes){
        
        HashMap<String, String> changesInAttributes = new HashMap<>();

        for (String attributeName : newObjectAttributes.keySet()) {
            String newAttributeValue = newObjectAttributes.get(attributeName);
            if (oldObjectAttributes.containsKey(attributeName)) {
                List<String> oldAttributeValues = oldObjectAttributes.get(attributeName);
                if (oldAttributeValues != null && newAttributeValue != null) {
                    if (!oldAttributeValues.get(0).equals(newAttributeValue)) 
                        changesInAttributes.put(attributeName, newAttributeValue);
                }
            } else
                changesInAttributes.put(attributeName, newAttributeValue);//an added attribute
        }
        return changesInAttributes;
    }
    
    /**
     * Compares the old and the new list of attributes an return the changes
     * @param oldObjectAttributes the old list of attributes
     * @param newObjectAttributes the list read it from SNMP
     * @return the a map with the attributes changed
     */
    private HashMap<String, String> compareAttributes(HashMap<String, List<String>> oldObjectAttributes,  JsonObject newObjectAttributes){
        
        HashMap<String, String> changesInAttributes = new HashMap<>();

        for (String attributeName : newObjectAttributes.keySet()) {
            String newAttributeValue = newObjectAttributes.getString(attributeName);
            if (oldObjectAttributes.containsKey(attributeName)) {
                List<String> oldAttributeValues = oldObjectAttributes.get(attributeName);
                if (oldAttributeValues != null && newAttributeValue != null) {
                    if (!oldAttributeValues.get(0).equals(newAttributeValue)) 
                        changesInAttributes.put(attributeName, newAttributeValue);
                }
            } else
                changesInAttributes.put(attributeName, newAttributeValue);//an added attribute
        }
        return changesInAttributes;
    }

    //Things to be deleted
    public void removeObjectFromDelete(RemoteBusinessObjectLight obj) {
        for (long branchId : oldObjectStructure.keySet()) {
            oldObjectStructure.get(branchId).remove(obj);
        }
    }

    public void deleteBranch(String space, RemoteBusinessObjectLight actualChildFirstLevel){
        
        try {
            List<RemoteBusinessObjectLight> objectChildren = bem.getObjectChildren(actualChildFirstLevel.getClassName(), actualChildFirstLevel.getId(), 0);
            if(!objectChildren.isEmpty()){
                for (RemoteBusinessObjectLight objectChildren1 : objectChildren) {
                    deleteBranch(space, objectChildren1);
                }
                space += " ";
            }
        } catch (MetadataObjectNotFoundException | ObjectNotFoundException ex) {
            Logger.getLogger(SNMPDataProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    
    }
    
    public void checkDataToBeDeleted() {
        JsonObject json = Json.createObjectBuilder().add("type", "old_object_to_delete").build();
        for (RemoteBusinessObjectLight actualChildFirstLevel : actualFirstLevelChildren) {
            if (!actualChildFirstLevel.getClassName().contains("Port") && !actualChildFirstLevel.getClassName().contains("ServiceInstance")) {
                
                JsonObject jdevice = Json.createObjectBuilder()
                            .add("deviceId", Long.toString(actualChildFirstLevel.getId()))
                            .add("deviceName", actualChildFirstLevel.getName())
                            .add("deviceClassName", actualChildFirstLevel.getClassName())
                            .build();
                
                json = jsonObjectToBuilder(json).add("device", jdevice).build();
                findings.add(new SyncFinding(SyncFinding.EVENT_DELETE,
                            String.format("The %s - %s and all its children WILL BE DELETED, please OPEN The NavigationTree an double check before delete this object, if you are not sure select \"skip\"", 
                                    actualChildFirstLevel.toString(), Long.toString(actualChildFirstLevel.getId())),
                            json.toString()));
            }
        }
            
        for (long key : oldObjectStructure.keySet()) {
            List<RemoteBusinessObjectLight> branchToDelete = oldObjectStructure.get(key);
            for (RemoteBusinessObjectLight deviceToDelete : branchToDelete) {
                JsonObject jsont = Json.createObjectBuilder().add("type", "old_object_to_delete").build();
                if (deviceToDelete.getClassName().contains("Transceiver")){
                    try {
                    RemoteBusinessObjectLight tParent = bem.getParent(deviceToDelete.getClassName(), deviceToDelete.getId());
                    
                    JsonObject jsonp = Json.createObjectBuilder()
                            .add("deviceId", Long.toString(tParent.getId()))
                            .add("deviceName", tParent.getName())
                            .add("deviceClassName", tParent.getClassName())
                            .build();
                    
                    JsonObject jsonpt = Json.createObjectBuilder()
                            .add("deviceId", Long.toString(deviceToDelete.getId()))
                            .add("deviceName", deviceToDelete.getName())
                            .add("deviceClassName", deviceToDelete.getClassName())
                            .build();
                    
                    jsont = jsonObjectToBuilder(jsont).add("device", jsonpt).build();
                    jsont = jsonObjectToBuilder(jsont).add("deviceParent", jsonp).build();
                    
                    if(tParent.getClassName().contains("Port"))
                        findings.add(new SyncFinding(SyncFinding.EVENT_DELETE,
                           String.format("This device %s was child of %s, but it should be its parent, a new one was moved and updated, would you like to delete this old device?, this operations its totally safe", 
                                   deviceToDelete.toString(), tParent.toString()), jsont.toString()));
                    
                    } catch (ObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                        Logger.getLogger(SNMPDataProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
               }
            }
        }
    }

    //<editor-fold desc="Ports" defaultstate="collapsed">
    /**
     * Create findings with the ports with no match
     */
    public void checkPortsWithNoMatch() {
        for (JsonObject oldPort : notMatchedPorts) {
            oldPort = jsonObjectToBuilder(oldPort).add("type", "object_port_no_match").build();

            findings.add(new SyncFinding(SyncFinding.EVENT_DELETE,
                    String.format("There was no match for port: %s [%s] - id: %s. Do you want to delete this port after the sync process?",
                            oldPort.getJsonObject("attributes").getString("name"), oldPort.getString("className"), oldPort.getString("id")),
                    oldPort.toString()));
        }

        for (JsonObject jnewPort : newPorts) {
            jnewPort = jsonObjectToBuilder(jnewPort).add("type", "object_port_no_match_new").build();

            findings.add(new SyncFinding(SyncFinding.EVENT_NEW,
                    String.format("There was no match for port: %s [%s]. Do you want to create this port after the sync process?",
                            jnewPort.getJsonObject("attributes").getString("name"), jnewPort.getString("className")),
                    jnewPort.toString()));
        }
    }

    /**
     * Compare copy the old ports into their new locations
     *
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws OperationNotPermittedException
     */
    private void checkPortsToMigrate() throws InvalidArgumentException,
            ObjectNotFoundException, MetadataObjectNotFoundException,
            OperationNotPermittedException {
        List<RemoteBusinessObjectLight> foundOldPorts = new ArrayList<>();
        List<JsonObject> foundNewPorts = new ArrayList<>();
        
        for (RemoteBusinessObjectLight oldPort : oldPorts) {
            //We copy the new attributes into the new port, to keep the relationships
            RemoteBusinessObjectLight oldPortParent = bem.getParent(oldPort.getClassName(), oldPort.getId());
            JsonObject portFound = searchOldPortInNewPorts(oldPort);

            if (portFound != null) {
                String parentName = oldPortParent.getName();
                
                foundOldPorts.add(oldPort);
                foundNewPorts.add(portFound);
                    
                if(!parentName.equals(portFound.getString("parentName"))){
                    portFound = jsonObjectToBuilder(portFound).add("type", "object_port_move").build();
                    portFound = jsonObjectToBuilder(portFound).add("childId", Long.toString(oldPort.getId())).build();

                    findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE,
                            String.format("Would you like to overwrite the attribute values in port %s, with id: %s?", oldPort.toString(), oldPort.getId()),
                            portFound.toString()));
                }
                else{
                    HashMap<String, String> attributes = compareAttributes(bem.getObject(oldPort.getId()).getAttributes(), portFound.getJsonObject("attributes"));
                    
                    if(!attributes.isEmpty()){
                        portFound = jsonObjectToBuilder(portFound).add("type", "device").build();
                        portFound = jsonObjectToBuilder(portFound).add("deviceClassName", oldPort.getClassName()).build();
                        JsonObject newAttributes = createExtraInfoToUpdateAttributesInObject(Long.toString(oldPort.getId()), oldPort.getClassName(), attributes);
                        portFound = jsonObjectToBuilder(portFound).add("attributes", newAttributes).build();
                        portFound = jsonObjectToBuilder(portFound).add("deviceId", Long.toString(oldPort.getId())).build();
                        findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE,
                            String.format("Would you like to overwrite the attributes in port %s, with id: %s?", oldPort.toString(), oldPort.getId()),
                            portFound.toString()));
                    }
                }
            } 
            else {
                JsonObject oldPortWithNoMatch = Json.createObjectBuilder()
                        .add("id", Long.toString(oldPort.getId()))
                        .add("className", oldPort.getClassName())
                        .add("attributes", Json.createObjectBuilder().add("name", oldPort.getName()).build())
                        .build();

                notMatchedPorts.add(oldPortWithNoMatch);
            }
        }//end for
        for (RemoteBusinessObjectLight goodPort : foundOldPorts)
            oldPorts.remove(goodPort);

        for (JsonObject foundNewPort : foundNewPorts) {
            int index = removeMatchedNewPorts(foundNewPort);
            if (index > -1) 
                newPorts.remove(index);
        }
    }

    private int removeMatchedNewPorts(JsonObject jnewportFound) {
        for (int i = 0; i < newPorts.size(); i++) {
            if (newPorts.get(i).getJsonObject("attributes").getString("name")
                    .equals(jnewportFound.getJsonObject("attributes").getString("name"))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Given an old port search for it in the list of the new created ports, by
     * name
     *
     * @param oldPort the old port
     * @return a String pair key = The new parent Id (in the SNMP map) value =
     * the new port
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     */
    private JsonObject searchOldPortInNewPorts(RemoteBusinessObjectLight oldPort) throws InvalidArgumentException, ObjectNotFoundException, MetadataObjectNotFoundException {
        for (JsonObject jsonPort : newPorts) {
            if (comparePortNames(oldPort.getName(), oldPort.getClassName(),
                    jsonPort.getJsonObject("attributes").getString("name"),
                    jsonPort.getString("className"))) {
                return jsonPort;
            }
        }
        return null;
    }

    /**
     * Compare the old port names with the new name, the first load of the SNMP
     * sync depends of the name of the ports because this names are the only
     * common stating point to begin the search and creation of the device
     * structure
     *
     * @param oldName the old port name
     * @param oldClassName the old port class
     * @param newName the new port name
     * @param newClassName the new port class
     * @return boolean if the name match
     */
    private boolean comparePortNames(String oldName, String oldClassName, String newName, String newClassName) {
        if (oldClassName.equals(newClassName)) {
            oldName = oldName.toLowerCase().trim();
            newName = newName.toLowerCase().trim();
            if (!oldName.equals(newName)) {
                
                String[] splitOldName;
                if(!oldName.contains("/") && !newName.contains("/"))
                    return newName.trim().contains(oldName.toLowerCase().replace("port", "").trim());
                
                splitOldName = oldName.toLowerCase().split("/");
                String[] splitNewName = newName.toLowerCase().split("/");

                boolean allPartsAreEquals = true;
                if (splitNewName.length == splitOldName.length) {
                    for (int i = 0; i < splitOldName.length; i++) {
                        if (!splitOldName[i].equals(splitNewName[i]))
                            allPartsAreEquals = false;
                    }
                    if (allPartsAreEquals) 
                        return true;

                    //first part
                    boolean firstPart= false;
                    String oldPart1 = splitOldName[0];
                    oldPart1 = oldPart1.replaceAll("[-._:,]", "");
                    
                    String newPart1 = splitNewName[0];
                    
                    newPart1 = newPart1.replaceAll("[-._:,]", "");
                    if (oldPart1.equals(newPart1))
                        firstPart = true;
                    
                    else if(newPart1.contains("tentigt")) //TenGigE
                        newPart1 = newPart1.replace("tentigt", "tt");
                    else if(newPart1.contains("tengige")) //TenGigE
                        newPart1 = newPart1.replace("tengige", "te");
                    else if(newPart1.contains("gigabitethernet"))
                        newPart1 = newPart1.replace("gigabitethernet", "ge");
                    else if(newPart1.contains("mgmteth"))
                        newPart1 = newPart1.replace("mgmteth", "mg");
                    else if(newPart1.contains("gi") && newPart1.length() < 4)
                        newPart1 = newPart1.replace("gi", "ge");
                    else if(newPart1.contains("tengi"))
                        newPart1 = newPart1.replace("tengi", "te");
                    
                    if (oldPart1.replaceAll("\\s+","").equals(newPart1.replaceAll("\\s+","")) && !firstPart)
                            firstPart = true;

                    //the other parts
                    boolean lastPartAreEquals = true;
                    if (splitOldName.length > 1 && splitNewName.length > 1) {
                        for (int i = 1; i < splitOldName.length; i++) {
                            if (!splitOldName[i].equals(splitNewName[i])) 
                                lastPartAreEquals = false;
                        }
                    }
                    return (firstPart && lastPartAreEquals) ;
                   
                } else 
                    return false;
            }//end kind of port optical
            else 
                return true;
        }
        return false;
    }
    //</editor-fold>
    
    //<editor-fold desc="List Types" defaultstate="collapsed">
    /**
     * Returns the listTypeId if exists or creates a Finding in case that the
     * list type doesn't exist in Kuwaiba
     *
     * @param i the index of the list type in the SNMP table
     * @return the list type (is exist, otherwise is an empty String)
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     */
    private String findingListTypeId(int i) throws MetadataObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException {

        if (!allData.get("entPhysicalMfgName").get(i).isEmpty()) {
            String vendor = "";
            Long listTypeId = matchVendorNames(allData.get("entPhysicalMfgName").get(i));
            if (listTypeId > 0) {
                vendor = Long.toString(listTypeId);
            } else { // if the list type doesn't exist we also create a finding
                if (!listTypeEvaluated.contains(allData.get("entPhysicalMfgName").get(i))) {
                    long createListTypeItem = aem.createListTypeItem("EquipmentVendor", allData.get("entPhysicalMfgName").get(i), allData.get("entPhysicalMfgName").get(i));

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
     *
     * @param listTypeNameToLoad the list type name
     * @return the kuwaiba's list type item id
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     */
    private long matchVendorNames(String listTypeNameToLoad) throws MetadataObjectNotFoundException, InvalidArgumentException {
        List<RemoteBusinessObjectLight> listTypeItems = aem.getListTypeItems("EquipmentVendor");
        for (RemoteBusinessObjectLight createdLitType : listTypeItems) {
            int matches = 0;
            int maxLength = listTypeNameToLoad.length() > createdLitType.getName().length() ? listTypeNameToLoad.length() : createdLitType.getName().length();
            listTypeNameToLoad = listTypeNameToLoad.toLowerCase().trim();
            String nameCreatedInKuwaiba = createdLitType.getName().toLowerCase().trim();
            if (listTypeNameToLoad.equals(nameCreatedInKuwaiba)) {
                return createdLitType.getId();
            }
            for (int i = 1; i < maxLength; i++) {
                String a, b;
                if (listTypeNameToLoad.length() < i) {
                    break;
                } else {
                    a = listTypeNameToLoad.substring(i - 1, i);
                }
                if (nameCreatedInKuwaiba.length() < i) {
                    break;
                } else {
                    b = nameCreatedInKuwaiba.substring(i - 1, i);
                }
                if (a.equals(b)) {
                    matches++;
                }
            }
            if (matches == listTypeNameToLoad.length()) {
                return createdLitType.getId();
            }
        }
        return -1;
    }
    //</editor-fold>
}
