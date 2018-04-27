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
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.utils.i18n.I18N;
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
    private String INITAL_ID;
    /**
     * To keep the objects during synchronization
     */
    private List<JsonObject> branch;
    /**
     * It's used to store the object info we are trying to update
     */
    private RemoteBusinessObjectLight obj;
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
    long k = 0;

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

    //<editor-fold desc="Methods to read the data and load it into memory" defaultstate="collapsed">
    /**
     * Reads the data loaded into memory
     * @throws InvalidArgumentException if the table info load is corrupted and
     * has no chassis
     */
    public void readData() throws InvalidArgumentException {
        //The initial id is the id of the chassis in most of cases is 0, except in the model SG500 
        INITAL_ID = allData.get("entPhysicalContainedIn").get(allData.get("entPhysicalClass").indexOf("3"));
        if (allData.get("entPhysicalContainedIn").contains(INITAL_ID))
            createTreeFromFile(INITAL_ID);
        else 
            findings.add(new SyncFinding(SyncFinding.EVENT_ERROR,
                                            I18N.gm("no_inital_id_was_found"),
                                            I18N.gm("check_initial_id_in_snmp_data")));
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
                String patentClassParsed = parseClass(
                        allData.get("entPhysicalModelName").get(w),
                        allData.get("entPhysicalClass").get(w),
                        allData.get("entPhysicalName").get(w),
                        allData.get("entPhysicalDescr").get(w)//NOI18N
                );

                if(patentClassParsed != null){
                    List<String> childrenParsed = mapOfClasses.get(patentClassParsed);
                    if (childrenParsed == null) 
                        childrenParsed = new ArrayList<>();

                    for (String child : childrenId) {
                        int indexOfChild = allData.get("instance").indexOf(child);
                        String childParsedClass = parseClass(
                                allData.get("entPhysicalModelName").get(w),
                                allData.get("entPhysicalClass").get(indexOfChild),
                                allData.get("entPhysicalName").get(indexOfChild),
                                allData.get("entPhysicalDescr").get(indexOfChild)//NOI18N
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
     * @param deviceModel the device model
     * @param className_ the given class name
     * @param name name of the element
     * @param descr description of the element
     * @return equivalent kuwaiba's class
     */
    public String parseClass(String deviceModel, String className_, String name, String descr) {
        if (className_.isEmpty())
            return null;
        
        int classId = Integer.valueOf(className_);
        if (classId == 3 && (!name.isEmpty() && !descr.isEmpty())) //chassis
            return className;
        else if (deviceModel != null && classId == 10 && deviceModel.contains("2960")) 
            return "ElectricalPort";
        else if (classId == 10){ //port
            if (name.toLowerCase().contains("usb") || descr.toLowerCase().contains("usb")) //NOI18N
                return "USBPort";//NOI18N
            else if (name.toLowerCase().contains("fastethernet") || name.toLowerCase().contains("mgmteth") || 
                    name.toLowerCase().contains("cpu") || name.toLowerCase().contains("control") ||
                    (descr.toLowerCase().contains("ethernet") && !descr.toLowerCase().contains("gigabit")) ||
                    descr.toLowerCase().contains("fast") || descr.toLowerCase().contains("management")) //NOI18N
                return "ElectricalPort";//NOI18N
            else if(!name.isEmpty() && !descr.isEmpty())
                return "OpticalPort";//NOI18N
            
        } else if (classId == 5) { //container
            if (!descr.contains("Disk")) //NOI18N
                return "Slot";//NOI18N
            
        } else if (classId == 6 && name.toLowerCase().contains("power") && !name.toLowerCase().contains("module") || classId == 6 && descr.toLowerCase().contains("power")) 
            return "PowerPort";//NOI18N
        else if (classId == 6 && name.contains("Module")) //NOI18N
            return "HybridBoard"; //NOI18N
        else if (classId == 9) { //module
            //In Routers ASR9001 the Transceivers, some times have an empty desc
            if ((name.split("/").length > 3 || name.toLowerCase().contains("transceiver") || descr.toLowerCase().contains("transceiver") || (descr.toLowerCase().contains("sfp") 
                    || descr.toLowerCase().contains("xfp") || descr.toLowerCase().contains("cpak") || descr.toLowerCase().equals("ge t"))) && !name.toLowerCase().contains("spa") && !descr.toLowerCase().contains("spa"))
                return "Transceiver"; //NOI18N

            return "IPBoard"; //NOI18N
        }
        else if(classId == 1 && descr.contains("switch processor"))
            return "SwitchProcessor"; //NOI18N
        
        return null;
    }

    /**
     * Returns if the class is used or not
     *
     * @param line the line of the SNMP to extract the className and the name of
     * the element
     * @return false if is a sensor, true in the most of the cases for now
     */
    private boolean isClassUsed(String classId_, String descr) {
        int classId = Integer.valueOf(classId_);
        //chassis(3) port(10) powerSupply(6) module(9) container(5) 
        switch(classId){
            case 3:
            case 10:
            case 6:
            case 9:
                return true;
            case 1:
                return descr.trim().toLowerCase().contains("switch processor");//NOI18N
            case 5:
                return !descr.trim().toLowerCase().contains("disk");//NOI18N
        }
        return false;
    }
    //</editor-fold>
    
    /**
     * Creates the hierarchy model in Kuwaiba if doesn't exist
     * Json structure
     * {type:hierarchy
     *  hierarchy:{
     *       slot[{"child":"Transceiver"},{"child":"IPBoard"},{"child":"HybridBoard"}], 
     *       parenClass, [{child:possibleChild},...]
     *    }
     * }
     */
    private void loadClassHierarchy() throws MetadataObjectNotFoundException {
        JsonObject jHierarchyFinding = Json.createObjectBuilder().add("type", "hierarchy").build();//NOI18N
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
                        JsonObject jchild = Json.createObjectBuilder().add("child", possibleChildToAdd).build();//NOI18N
                        children = jsonArrayToBuilder(children).add(jchild).build();    
                    }
                }
                if(!children.isEmpty())
                    jsonHierarchy = jsonObjectToBuilder(jsonHierarchy).add(parentClass, children).build();  
            }
        }//end for
        jHierarchyFinding = jsonObjectToBuilder(jHierarchyFinding).add("hierarchy",jsonHierarchy).build();//NOI18N
        if(!jsonHierarchy.isEmpty()){
            jHierarchyFinding = jsonObjectToBuilder(jHierarchyFinding).add("hierarchy",jsonHierarchy).build();//NOI18N
            findings.add(new SyncFinding(SyncFinding.EVENT_NEW,
                    I18N.gm("the_contaimnet_hierarchy_needs_to_be_updated"),
                    jHierarchyFinding.toString()));
        }
    }

    /**
     * Create into kuwaiba's objects the lines read it from the SNMP
     * it creates a branch every time he finds a port or an element with no children
     * e.g. branch 1)slot0/0[Slot]+
     *                            |_board0/0[Board]+
     *                                             |_ port0/0/0[Port] 
     *                                             (end of branch)
     * -------------------------------------------------------------------------
     *      branch 2)                              |_0/0/1[Port] 
     *                                             (end of branch)
     * -------------------------------------------------------------------------
     *      branch 3)                              |_0/0/2[Port]
     *                                             (end of branch)
     * -------------------------------------------------------------------------     
     *      branch 4)  slot0/1[Slot]
     *                              |_board0/1[Board]
     *                               (end of branch) 
     * -------------------------------------------------------------------------                           
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
        if(mapOfFile.isEmpty())
            throw new InvalidArgumentException("The router model you are trying to synchronize is not yet supported. Contact your administrator");
        
        if (Long.valueOf(parentId) == id)//If is the first element
            parentId = INITAL_ID;

        List<String> childrenIds = mapOfFile.get(parentId);
        if (childrenIds != null) {
            for (String childId : childrenIds) {
                
                int i = allData.get("instance").indexOf(childId); //NOI18N
                parentId = allData.get("entPhysicalContainedIn").get(i); //NOI18N
                if (parentClassName.equals(className)) //if is the chassis we must keep the id
                    parentId = Long.toString(id);

                String objectName = allData.get("entPhysicalName").get(i); //NOI18N
                if (objectName.contains("GigabitEthernet")) 
                    objectName = objectName.replace("GigabitEthernet", "Gi"); //NOI18N
                
                //We parse the class Id from SNMP into kuwaiba's class name
                String mappedClass = parseClass(
                        allData.get("entPhysicalModelName").get(i),
                        allData.get("entPhysicalClass").get(i), 
                        objectName, allData.get("entPhysicalDescr").get(i)); //NOI18N
                
                if(mappedClass == null) //it was impossible to parse the SNMP class into kuwaiba's class
                    findings.add(new SyncFinding(SyncFinding.EVENT_ERROR,
                                I18N.gm("empty_fields_in_the_data"),
                                Json.createObjectBuilder().add("type","error")
                                        .add("className", allData.get("entPhysicalClass").get(i))
                                        .add("InstanceId", childId).build().toString()));
                else{
                    HashMap<String, String> newAttributes = createNewAttributes(mappedClass, i);
                    //The chassis can be only updated
                    if (className.contains(mappedClass)) {
                        newAttributes.remove("name"); //NOI18N //the router name won't be changed
                        HashMap<String, String> comparedAttributes = compareAttributes(bem.getObject(id).getAttributes(), newAttributes);
                        if (!comparedAttributes.isEmpty()) {
                            comparedAttributes.put("name", bem.getObject(id).getAttributes().get("name").get(0)); //we need this to show the name in the results
                            findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE,
                                    I18N.gm("router_has_changes"),
                                    createExtraInfoToUpdateAttributesInObject(Long.toString(id), mappedClass, comparedAttributes, bem.getObject(id).getAttributes()).toString()));
                        }
                    //all the data except the chassis
                    } else { 
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
                        isDeviceAlreadyCreated(jsonNewObj);
                        branch.add(jsonNewObj);

                    }
                    checkObjects(childId, objectName, mappedClass);

                    //End of a branch
                    if (((mapOfFile.get(childId) == null) || mappedClass.contains("Port")) && !branch.isEmpty()) {
                        //The is first time is tryng to sync from SNMP
                        if (!isBranchAlreadyCreated(branch)) {
                            //Loaded from snmp first time
                            findings.add(new SyncFinding(SyncFinding.EVENT_NEW,
                                    I18N.gm("new_branch_to_sync"),
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
     * method check the structure of the object and search for a given branch of elements.
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
    private boolean isBranchAlreadyCreated(List<JsonObject> newBranchToEvalueate) throws InvalidArgumentException, 
            MetadataObjectNotFoundException, ObjectNotFoundException 
    {
        List<List<RemoteBusinessObjectLight>> oldBranchesWithMatches = searchInOldStructure(newBranchToEvalueate);
        if(oldBranchesWithMatches == null)//we found the branch in the current structure, nothing else to do
            return true;
        else if(!oldBranchesWithMatches.isEmpty()){//at least a part of the branch its already created
            int indexOfTheLargestsize = 0; //first we find the the longest path, if there are more than one
            int sizeOfThelargestPath = oldBranchesWithMatches.get(0).size(); 
            for (int i=1; i < oldBranchesWithMatches.size(); i++) {
                if(sizeOfThelargestPath < oldBranchesWithMatches.get(1).size()){
                    sizeOfThelargestPath = oldBranchesWithMatches.get(1).size();
                    indexOfTheLargestsize = i;
                }
            }
            List<RemoteBusinessObjectLight> oldBranch = oldBranchesWithMatches.get(indexOfTheLargestsize);
            return  partOfTheBranchMustBeCreated(oldBranch, newBranchToEvalueate);
        }
        return false; //if is empty all the branch should by created
    }
    
    private boolean partOfTheBranchMustBeCreated(List<RemoteBusinessObjectLight> oldBranch, 
            List<JsonObject> newBranchToEvalueate) throws InvalidArgumentException, 
            ObjectNotFoundException, MetadataObjectNotFoundException
    {
        //A new element to add we look for the parent id of the new element
        if(oldBranch.size() < newBranchToEvalueate.size()){ 
            List<JsonObject> newPartOfTheBranch = newBranchToEvalueate.subList(oldBranch.size(), newBranchToEvalueate.size());
            if(!newPartOfTheBranch.isEmpty()){//lets search if the part exists
                List<List<RemoteBusinessObjectLight>> oldBranchesWithMatches = searchInOldStructure(newPartOfTheBranch);
                if(oldBranchesWithMatches != null && !oldBranchesWithMatches.isEmpty()){//we found something
                    oldBranch.addAll(oldBranchesWithMatches.get(0));
                    return partOfTheBranchMustBeCreated(oldBranch, newBranchToEvalueate);
                }
                if(oldBranchesWithMatches == null)//we find the other part, the branch exists
                    return true; 
                else if(oldBranchesWithMatches.isEmpty()){ //the new part of the branch doesn't exists, so we are going to create it an remove the part that exists 
                    RemoteBusinessObjectLight oldObj = oldBranch.get(oldBranch.size() -1);
                    //The last object found in the current structure and the new evaluated branch
                    JsonObject currentObj = branch.get(oldBranch.size()-1);
                    String currentObjClassName = currentObj.getString("className");
                    JsonObject objAttributes = branch.get(oldBranch.size()-1).getJsonObject("attributes");
                    String currentObjName = objAttributes.getString("name");
                    //new object
                    JsonObject newObj = branch.get(oldBranch.size());
                    String objParentName = newObj.getString("parentName");
                    String objParentClassName = newObj.getString("parentClassName");
                    //We check again if the parenst match
                    if(currentObjName.equals(oldObj.getName()) && currentObjClassName.equals(oldObj.getClassName()) &&
                            oldObj.getName().equals(objParentName) && oldObj.getClassName().equals(objParentClassName))
                    {
                        newObj = jsonObjectToBuilder(newObj).add("deviceParentId", Long.toString(oldObj.getId())).build();
                        if(newObj.getString("className").contains("Port"))
                            editNewPortWithDeviceParentId(newObj);
                        branch.set(oldBranch.size(), newObj);
                    }
                    //we remove the part of the branch that already exists, and update the attributes if is necessary
                    int matchesToDelete = 0;
                    for (int i = 0; i < oldBranch.size(); i++) {
                        oldObj = oldBranch.get(i);
                        RemoteBusinessObjectLight oldParent = bem.getParent(oldObj.getClassName(), oldObj.getId());
                        HashMap<String, List<String>> oldAttributes = bem.getObject(oldObj.getId()).getAttributes();

                        currentObj = branch.get(i);
                        currentObjClassName = currentObj.getString("className");
                        objAttributes = branch.get(i).getJsonObject("attributes");
                        currentObjName = objAttributes.getString("name");
                        objParentName = currentObj.getString("parentName");
                        objParentClassName = currentObj.getString("parentClassName");
                        if(oldParent.getClassName().equals(className) && objParentClassName.equals(className))
                            objParentName = oldParent.getName();
                        
                        if(currentObjName.equals(oldObj.getName()) && currentObjClassName.equals(oldObj.getClassName()) &&
                            oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName))
                        {
                            updateAttributesInBranch(oldObj, oldAttributes, currentObj);
                            matchesToDelete++;
                        }
                    }
                    for (int m = 0; m < matchesToDelete; m++)
                        branch.remove(0);
                    
                    return false;
                }
            }
        }else{//something has been removed
            List<RemoteBusinessObjectLight> subOldBranchToRemove = oldBranch.subList(branch.size(), oldBranch.size()-1);
            for (RemoteBusinessObjectLight removedObj : subOldBranchToRemove) {
                JsonObject jdevice = Json.createObjectBuilder()
                        .add("deviceId", Long.toString(removedObj.getId()))
                        .add("deviceName", removedObj.getName())
                        .add("deviceClassName", removedObj.getClassName())
                        .build();

                findings.add(new SyncFinding(SyncFinding.EVENT_DELETE,
                        String.format("The object %s WILL BE DELETED, perhaps it was removed physically from the device. If you are not sure, SKIP this action", 
                                removedObj.toString()),
                        jdevice.toString()));
            }
            return true;
        }
        return true;
    }
    
    /**
     * Search a new branch in the current structure if finds a part of the new 
     * branch in the o
     * @param newBranchToEvalueate
     * @return
     * @throws InvalidArgumentException
     * @throws MetadataObjectNotFoundException
     * @throws ObjectNotFoundException 
     */
    private List <List<RemoteBusinessObjectLight>> searchInOldStructure(List<JsonObject> newBranchToEvalueate) throws InvalidArgumentException, 
            MetadataObjectNotFoundException, ObjectNotFoundException
    {
        List <List<RemoteBusinessObjectLight>> oldBranchesWithMatches = new ArrayList<>();
        List<RemoteBusinessObjectLight> foundPath = new ArrayList<>();
        boolean hasBeenFound = false; //This is usded because some branch are in disorder
        
        for (long i : oldObjectStructure.keySet()) {
            foundPath = new ArrayList<>();
            List<RemoteBusinessObjectLight> oldBranch = oldObjectStructure.get(i);
            int end = oldBranch.size() > newBranchToEvalueate.size() ? newBranchToEvalueate.size() : oldBranch.size();
            for (int w=0; w < end; w++) {
                String objClassName = newBranchToEvalueate.get(w).getString("className");
                String objParentName = newBranchToEvalueate.get(w).getString("parentName");
                String objParentClassName = newBranchToEvalueate.get(w).getString("parentClassName");
                JsonObject objAttributes = newBranchToEvalueate.get(w).getJsonObject("attributes");
                String newObjName = objAttributes.getString("name");
                if (!className.equals(objClassName)) {
                    RemoteBusinessObjectLight oldObj = oldBranch.get(w);
                    RemoteBusinessObjectLight oldParent = bem.getParent(oldObj.getClassName(), oldObj.getId());
                    //The name of the Router will be diferent in most of the cases so we must avoid the name in this case..
                    if(oldParent.getClassName().equals(className) && objParentClassName.equals(className))
                        objParentName = oldParent.getName();
                    if (oldObj.getName().equals(newObjName) && oldObj.getClassName().equals(objClassName)){
                        if(oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName))
                            foundPath.add(oldObj);
                    }
                }//end if not router
            }//we find the whole path
            if(foundPath.size() == newBranchToEvalueate.size()){
                for (int j=0; j<foundPath.size(); j++) { 
                    RemoteBusinessObjectLight oldObj = foundPath.get(j);
                    HashMap<String, List<String>> oldAttributes = bem.getObject(oldObj.getId()).getAttributes();
                    JsonObject newObj = newBranchToEvalueate.get(j); //this is the new object from SNMP
                    updateAttributesInBranch(oldObj, oldAttributes, newObj);//we check if some attributes need to be updated
                }
                return null; //we find the whole path, we return null
            }
            //Some paths exists but in order to find them we should look for them backwards in the branch, because the SNMP doesn't order the 
            //first son in the same way of kuwaiba
            //e.g. from kuwaiba:                  form SNMP:
            //          Board0/2                   Board0/2
            //                 |_Gi0/0/2/0[Port]          |_Gi0/0/2/16[Port]<---the first son from SNMP is not in the same
            //current branch in kuwaiba:
            //slot0/2, Board0/2, Gi0/0/2/0[Port]
            //from SNMP:
            //slot0/2, Board0/2, Gi0/0/2/16[Port] <-- this branch exists (the first branch will always bring the parents)
            //                   Gi0/0/2/00[Port] <-- this too, but is not in the same order  (the other children don't bring the parents)
            if(foundPath.isEmpty()){
                for (int n=newBranchToEvalueate.size(), o= oldBranch.size(); (n*o)>0; n--, o--) {
                    String objClassName = newBranchToEvalueate.get(n-1).getString("className");
                    String objParentName = newBranchToEvalueate.get(n-1).getString("parentName");
                    String objParentClassName = newBranchToEvalueate.get(n-1).getString("parentClassName");
                    JsonObject objAttributes = newBranchToEvalueate.get(n-1).getJsonObject("attributes");
                    String newObjName = objAttributes.getString("name");
                    if (!className.equals(objClassName)) {
                        RemoteBusinessObjectLight oldObj = oldBranch.get(o-1);
                        RemoteBusinessObjectLight oldParent = bem.getParent(oldObj.getClassName(), oldObj.getId());
                        //The name of the Router will be diferent in most of the cases so we must avoid the name in this case..
                        if(oldParent.getClassName().equals(className) && objParentClassName.equals(className))
                            objParentName = oldParent.getName();
                        if (oldObj.getName().equals(newObjName) && oldObj.getClassName().equals(objClassName) && (oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName))){
                            foundPath.add(oldObj);
                            hasBeenFound = true;
                        }
                    }//end if not router
                } 
                //we find the whole path, if the path si found backwards, the comparation of the attributes should be do it backwards too
                if(foundPath.size() == newBranchToEvalueate.size()){
                    for (int j=foundPath.size()-1, r=0; j>-1; j--, r++) { 
                        RemoteBusinessObjectLight oldObj = foundPath.get(j);
                        HashMap<String, List<String>> oldAttributes = bem.getObject(oldObj.getId()).getAttributes();
                        JsonObject newObj = newBranchToEvalueate.get(r); //this is the new object from SNMP
                        updateAttributesInBranch(oldObj, oldAttributes, newObj);//we check if some attributes need to be updated
                    }
                    return null;
                }
            }
            else if(!foundPath.isEmpty())
                oldBranchesWithMatches.add(foundPath);
        }//end for
        if(foundPath.isEmpty()){
            //we must search again to check if there are is a match between the last element of the old structure and the first element of the new branch
            for (long i : oldObjectStructure.keySet()) {
                List<RemoteBusinessObjectLight> oldBranch = oldObjectStructure.get(i);
                String objClassName = newBranchToEvalueate.get(0).getString("className");
                String objParentName = newBranchToEvalueate.get(0).getString("parentName");
                String objParentClassName = newBranchToEvalueate.get(0).getString("parentClassName");
                JsonObject objAttributes = newBranchToEvalueate.get(0).getJsonObject("attributes");
                String newObjName = objAttributes.getString("name");
                if (!className.equals(objClassName)) {
                    RemoteBusinessObjectLight oldObj = oldBranch.get(oldBranch.size()-1);
                    RemoteBusinessObjectLight oldParent = bem.getParent(oldObj.getClassName(), oldObj.getId());
                    //The name of the Router will be diferent in most of the cases so we must avoid the name in this case..
                    if(oldParent.getClassName().equals(className) && objParentClassName.equals(className))
                        objParentName = oldParent.getName();
                    if (oldObj.getName().equals(newObjName) && oldObj.getClassName().equals(objClassName) && oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName)){
                        foundPath.add(oldObj);
                        break;
                    }
                }//end if not router
            }
        }
        if(!foundPath.isEmpty() && !oldBranchesWithMatches.contains(foundPath))
            oldBranchesWithMatches.add(foundPath);
        //we check if the branch starts in the first level, direct child of the chassis
        else if(!hasBeenFound && oldBranchesWithMatches.isEmpty()){
            if(newBranchToEvalueate.get(0).getString("parentClassName").equals(className)){
                JsonObject jObj = newBranchToEvalueate.get(0);
                jObj = jsonObjectToBuilder(jObj).add("deviceParentId", Long.toString(id)).build();
                branch.set(0, jObj);
                return new ArrayList<>();
            }
            //This iare the less accurate methods 
            //we only check for the parent name of the first element of the new 
            //branch with the last element of the old branch.
            for (long i : oldObjectStructure.keySet()) {
                List<RemoteBusinessObjectLight> oldBranch = oldObjectStructure.get(i);
                String objParentName = newBranchToEvalueate.get(0).getString("parentName");
                String objParentClassName = newBranchToEvalueate.get(0).getString("parentClassName");
                if(oldBranch.size() > 2){ //we only check old branches with more than one element, otherwise they are ports
                    RemoteBusinessObjectLight oldParent = oldBranch.get(oldBranch.size()-2);
                    if (oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName)){
                        JsonObject jObj = branch.get(0);
                        jObj = jsonObjectToBuilder(jObj).add("deviceParentId", Long.toString(oldParent.getId())).build();
                        if(jObj.getString("className").contains("Port"))
                            editNewPortWithDeviceParentId(jObj);
                        branch.set(0, jObj);
                        break;
                    }
                }
            }
            //if the branch could not be found still its necessary to check again 
            //in the old branch for the second element (an IPboard)
            for (long i : oldObjectStructure.keySet()) {
                List<RemoteBusinessObjectLight> oldBranch = oldObjectStructure.get(i);
                String objParentName = newBranchToEvalueate.get(0).getString("parentName");
                String objParentClassName = newBranchToEvalueate.get(0).getString("parentClassName");
                if(oldBranch.size()>1){
                    RemoteBusinessObjectLight oldParent = oldBranch.get(1);
                    if (oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName)){
                        JsonObject jObj = branch.get(0);
                        jObj = jsonObjectToBuilder(jObj).add("deviceParentId", Long.toString(oldParent.getId())).build();
                        if(jObj.getString("className").contains("Port"))
                            editNewPortWithDeviceParentId(jObj);
                        branch.set(0, jObj);
                        break;
                    }
                }
            }
        }
        return oldBranchesWithMatches;
    }
   
    private void updateAttributesInBranch(RemoteBusinessObjectLight oldObj, HashMap<String, List<String>> oldAttributes, JsonObject newObj) throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException{
        JsonObject objAttributes = newObj.getJsonObject("attributes");
        HashMap<String, String> attributeChanges = compareAttributes(oldAttributes, objAttributes);
        if(!attributeChanges.isEmpty()){
            newObj = jsonObjectToBuilder(newObj).add("deviceId", Long.toString(oldObj.getId())).build();
            newObj = jsonObjectToBuilder(newObj).add("type", "device").build();
            newObj = jsonObjectToBuilder(newObj).add("deviceClassName", oldObj.getClassName()).build();
            newObj = jsonObjectToBuilder(newObj).add("attributes", parseAttributesToJson(attributeChanges)).build();
            newObj = jsonObjectToBuilder(newObj).add("oldAttributes", parseOldAttributesToJson(oldAttributes)).build();
            findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE, String.format(I18N.gm("object_attributes_changed"), oldObj.toString()), newObj.toString()));
        }
    }
    
    /**
     * checks if the first level of children has changes
     * @param json the new object
     * @return true if is already created
     */
    private boolean isDeviceAlreadyCreated(JsonObject json){
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
        for (RemoteBusinessObjectLight object : objects) {
            if (!mem.isSubClass("GenericLogicalPort", object.getClassName())) 
                tempAuxOldBranch.add(object);
            
            if (object.getClassName().contains("Port") && !object.getClassName().contains("Virtual") && !object.getClassName().contains("Power")) 
                oldPorts.add(object);
            
            List<RemoteBusinessObjectLight> children = bem.getObjectChildren(object.getClassName(), object.getId(), -1);
            if (!children.isEmpty()) 
                readActualDeviceStructure(object.getId(), children);
            else {
                if(!tempAuxOldBranch.isEmpty())
                    oldObjectStructure.put(k, tempAuxOldBranch);
                tempAuxOldBranch = new ArrayList<>();
                k++;
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
    

    private JsonObject listToJson(List<JsonObject> branch, String type) {
        JsonObject json = Json.createObjectBuilder().add("type", type).build();
        JsonArray children = Json.createArrayBuilder().build();

        for (JsonObject jo : branch) 
            children = jsonArrayToBuilder(children).add(Json.createObjectBuilder().add("child", jo)).build();
        
        json = jsonObjectToBuilder(json).add("children", children).build();

        return json;
    }

    /**
     * Creates the extra info need it for the finding in a JSON format.
     * JSON 
     * attributes{
     *  attributeName : attributeValue 
     * }
     * oldAttributes{
     *  attributeName : attributeValue 
     * }
     * @param deviceId the device id
     * @param newAttributes the attributes to create the JSON
     * @return a json object
     */
    private JsonObject createExtraInfoToUpdateAttributesInObject(String deviceId, String deviceClassName, HashMap<String, String> newAttributes, HashMap<String, List<String>> oldAttributes) {
        JsonObject jsonObj = Json.createObjectBuilder().add("type", "device").add("deviceId", deviceId).add("deviceClassName", deviceClassName).build();
        JsonObject jNewAttributes = Json.createObjectBuilder().build();
        JsonObject jOldAttributes = Json.createObjectBuilder().build();
        for (String key : newAttributes.keySet()){ 
            jNewAttributes = jsonObjectToBuilder(jNewAttributes).add(key, newAttributes.get(key).trim()).build();
            if(oldAttributes.get(key) != null)
                jOldAttributes = jsonObjectToBuilder(jOldAttributes).add(key, oldAttributes.get(key).get(0).trim()).build();
        }
        jsonObj = jsonObjectToBuilder(jsonObj).add("attributes", jNewAttributes).add("oldAttributes", jOldAttributes).build();
        return jsonObj;
    }

    /**
     * Parse a hash map with the attributes of the objects to a JSON format
     * @param attributes the attributes to create the JSON
     * @return a JSON object with the attributes
     */
    private JsonObject parseAttributesToJson(HashMap<String, String> attributes) {
        JsonObject jsonObj = Json.createObjectBuilder().build();
        for (String key : attributes.keySet()) 
            jsonObj = jsonObjectToBuilder(jsonObj).add(key, attributes.get(key).trim()).build();

        return jsonObj;
    }
    
     /**
     * Parse a hash map with the attributes of the objects to a JSON format
     * @param attributes the attributes to create the JSON
     * @return a JSON object with the attributes
     */
    private JsonObject parseOldAttributesToJson(HashMap<String, List<String>> attributes) {
        JsonObject jsonObj = Json.createObjectBuilder().build();
        for (String key : attributes.keySet()) 
            jsonObj = jsonObjectToBuilder(jsonObj).add(key, attributes.get(key).get(0).trim()).build();

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

    /**
     * Create a hash map for the attributes of the given index entry of the data read it from SNMP
     * @param index the index of the entry
     * @return a HashMap with attribute name(key) attribute value (value)
     * @throws MetadataObjectNotFoundException if the attributes we are trying to sync doesn't exists in the classmetadata
     * @throws InvalidArgumentException
     * @throws OperationNotPermittedException 
     */
    private HashMap<String, String> createNewAttributes(String mappedClass, int index) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException
    {
        HashMap<String, String> attributes = new HashMap<>();
        ClassMetadata mappedClassMetadata = mem.getClass(mappedClass);
               
        String objectName = allData.get("entPhysicalName").get(index);//NOI18N
        
        if (objectName.contains("GigabitEthernet"))//NO18N For optical ports
            objectName = objectName.replace("GigabitEthernet", "Gi");//NOI18N
        
        attributes.put("name", objectName);//NOI18N
        String description = allData.get("entPhysicalDescr").get(index).trim();
        if(!description.isEmpty()){
            if(mappedClassMetadata.getAttribute("description") == null)
                createAttributeError(mappedClass, "description", "String");
            
            attributes.put("description", description);
        }
        if (!allData.get("entPhysicalMfgName").get(index).isEmpty()) {
            if(mappedClassMetadata.getAttribute("vendor") != null){
                String vendor = findingListTypeId(index, "EquipmentVendor");//NOI18N
                if (vendor != null) 
                    attributes.put("vendor", vendor); //NOI18N
            }
            else
                 createAttributeError(mappedClass, "vendor", "EquipmentVendor");    
        }
        if (!allData.get("entPhysicalSerialNum").get(index).isEmpty()){
            if(mappedClassMetadata.getAttribute("serialNumber") == null)
                createAttributeError(mappedClass, "serialNumber", "String");
        
            attributes.put("serialNumber", allData.get("entPhysicalSerialNum").get(index).trim());//NOI18N
        }
        if (!allData.get("entPhysicalModelName").get(index).isEmpty()){ 
            if(mappedClass != null){
                AttributeMetadata modelAttribute = mappedClassMetadata.getAttribute("model");
                if(modelAttribute != null){
                    String model = findingListTypeId(index, modelAttribute.getType());//NOI18N
                    if(model != null)
                        attributes.put("model", model);//NOI18N
                }
                else
                    createAttributeError(mappedClass, "model", "EquipmentModel or any list type");
            }
        }
        return attributes;
    }
   public void createAttributeError(String aClass, String attributeName, String type){
       
       findings.add(new SyncFinding(SyncFinding.EVENT_ERROR,
                                            String.format(I18N.gm("attribute_does_not_exist_in_class"), attributeName, type, aClass),
                                            Json.createObjectBuilder().add("type","error")
                                             .add("className", aClass)
                                             .add("attributeType", type)  
                                             .add("attributeName", attributeName).build().toString()));
   }
    /**
     * Compares the old and the new list of attributes an return the changes
     * @param oldObjectAttributes the old list of attributes
     * @param newObjectAttributes the list read it from SNMP
     * @return the a map with the attributes changed
     */
    private HashMap<String, String> compareAttributes(HashMap<String, List<String>> oldObjectAttributes, HashMap<String, String> newObjectAttributes){
        HashMap<String, String> updatedAttributes = new HashMap<>();
        for (String attributeName : newObjectAttributes.keySet()) {
            String newAttributeValue = newObjectAttributes.get(attributeName);
            if (oldObjectAttributes.containsKey(attributeName)) {
                List<String> oldAttributeValues = oldObjectAttributes.get(attributeName);
                if (oldAttributeValues != null && newAttributeValue != null) {
                    if (!oldAttributeValues.get(0).equals(newAttributeValue)) 
                        updatedAttributes.put(attributeName, newAttributeValue);
                }
            } else
                updatedAttributes.put(attributeName, newAttributeValue);//an added attribute
        }
        return updatedAttributes;
    }
    
    /**
     * Compares the old and the new list of attributes an return the changes
     * @param oldObjectAttributes the old list of attributes
     * @param newObjectAttributes the list read it from SNMP
     * @return the a map with the attributes changed
     */
    private HashMap<String, String> compareAttributes(HashMap<String, List<String>> oldObjectAttributes,  JsonObject newObjectAttributes){
        HashMap<String, String> updatedAttributes = new HashMap<>();
        for (String attributeName : newObjectAttributes.keySet()) {
            String newAttributeValue = newObjectAttributes.getString(attributeName);
            if (oldObjectAttributes.containsKey(attributeName)) {
                List<String> oldAttributeValues = oldObjectAttributes.get(attributeName);
                if (oldAttributeValues != null && newAttributeValue != null) {
                    if (!oldAttributeValues.get(0).equals(newAttributeValue)) 
                        updatedAttributes.put(attributeName, newAttributeValue);
                }
            } else
                updatedAttributes.put(attributeName, newAttributeValue);//an added attribute
        }
        return updatedAttributes;
    }

    //Things to be deleted
    public void removeObjectFromDelete(RemoteBusinessObjectLight obj) {
        for (long branchId : oldObjectStructure.keySet()) {
            oldObjectStructure.get(branchId).remove(obj);
        }
    }
    
    public void checkDataToBeDeleted() throws MetadataObjectNotFoundException {
        JsonObject json = Json.createObjectBuilder().add("type", "old_object_to_delete").build();
        for (RemoteBusinessObjectLight actualChildFirstLevel : actualFirstLevelChildren) {
            if (!mem.isSubClass("GenericLogicalPort", actualChildFirstLevel.getClassName())) {
                
                JsonObject jdevice = Json.createObjectBuilder()
                            .add("deviceId", Long.toString(actualChildFirstLevel.getId()))
                            .add("deviceName", actualChildFirstLevel.getName())
                            .add("deviceClassName", actualChildFirstLevel.getClassName())
                            .build();
                
                json = jsonObjectToBuilder(json).add("device", jdevice).build();
                findings.add(new SyncFinding(SyncFinding.EVENT_DELETE,
                            String.format("The %s - %s and all its children WILL BE DELETED. Perhaps it was removed physically from the device. If you are not sure select SKIP this action", 
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
                           String.format("This device %s was child of %s, but it should be its parent. A new one was place correctly. Would you like to delete the old element? This operations is completely safe", 
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
            JsonObject json = Json.createObjectBuilder().add("type", "old_object_to_delete").build();
            json = jsonObjectToBuilder(json).add("device", oldPort).build();

            findings.add(new SyncFinding(SyncFinding.EVENT_DELETE,
                    String.format("There was no match for port: %s [%s] - id: %s. Do you want to delete this port after the sync process?",
                            oldPort.getString("deviceName"), oldPort.getString("deviceClassName"), oldPort.getString("deviceId")),
                    json.toString()));
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
     * Compare the old ports with the new ones, the new ports should have been 
     * created in the last steps if they didn't exists, after the creation we compare 
     * the names of the old ports with the names of the new ones, if a match is 
     * found the old port will me moved to its new location, and the new port
     * will disappear, we make this in this way to keep the relationships of the old port
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
        List<JsonObject> foundNewPorts = new ArrayList<>(); //for debug
        
        for (RemoteBusinessObjectLight oldPort : oldPorts) {
            //We copy the new attributes into the new port, to keep the relationships
            RemoteBusinessObjectLight oldPortParent = bem.getParent(oldPort.getClassName(), oldPort.getId());
            JsonObject portFound = searchOldPortInNewPorts(oldPort);

            if (portFound != null) {
                String parentName = oldPortParent.getName();
                foundOldPorts.add(oldPort); foundNewPorts.add(portFound); //for debug
                    
                if(!parentName.equals(portFound.getString("parentName"))){ //the port need to be moved

                    portFound = jsonObjectToBuilder(portFound).add("type", "object_port_move").build();
                    portFound = jsonObjectToBuilder(portFound).add("childId", Long.toString(oldPort.getId())).build();
                    long parentId = getParentPortIdIfExists(portFound);
                    if(parentId > 0 )
                        portFound = jsonObjectToBuilder(portFound).add("deviceParentId", Long.toString(parentId)).build();
                    
                    findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE,
                            String.format("The port %s with id: %s will be moved to a new location to match the structure reported by the SNMP agent, do you want to proceed?", oldPort.toString(), oldPort.getId()),
                            portFound.toString()));
                }
                else{
                    HashMap<String, List<String>> oldAttributes = bem.getObject(oldPort.getId()).getAttributes();
                    HashMap<String, String> changedAttributes = compareAttributes(oldAttributes, portFound.getJsonObject("attributes"));
                    
                    if(!changedAttributes.isEmpty()){ //if the port its in the rigth place but the attributes have been updated
                        portFound = jsonObjectToBuilder(portFound).add("type", "device").build();
                        portFound = jsonObjectToBuilder(portFound).add("deviceClassName", oldPort.getClassName()).build();
                        portFound = jsonObjectToBuilder(portFound).add("deviceId", Long.toString(oldPort.getId())).build();
                        portFound = jsonObjectToBuilder(portFound).add("attributes", parseAttributesToJson(changedAttributes)).build();
                        portFound = jsonObjectToBuilder(portFound).add("oldAttributes", parseOldAttributesToJson(oldAttributes)).build();
                        findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE,
                            String.format("Would you like to overwrite the attributes in port %s, with those in port with id %s?", oldPort.toString(), oldPort.getId()),
                            portFound.toString()));
                    }
                }
            } 
            else {
                JsonObject oldPortWithNoMatch = Json.createObjectBuilder()
                        .add("deviceId", Long.toString(oldPort.getId()))
                        .add("deviceClassName", oldPort.getClassName())
                        .add("deviceName", oldPort.getName())
                        .build();

                notMatchedPorts.add(oldPortWithNoMatch);
            }
        }//end for
//we remove the ports from both lists the old port and the new ones
// for debuging don't delete        
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
     * Search by name an old port in the list of the new created ports
     *
     * @param oldPort the old port
     * @return a String pair key = The new parent Id (in the SNMP map) value = the new port
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     */
    private JsonObject searchOldPortInNewPorts(RemoteBusinessObjectLight oldPort) throws InvalidArgumentException, ObjectNotFoundException, MetadataObjectNotFoundException {
        for (JsonObject jsonPort : newPorts) {
            if (comparePortNames(oldPort.getName(), oldPort.getClassName(),
                    jsonPort.getJsonObject("attributes").getString("name"),
                    jsonPort.getString("className"))
                ) 
                return jsonPort;
        }
        return null;
    }

    /**
     * Compare the old port names with the new name, the first load of the SNMP
     * sync depends of the name of the ports because this names are the only
     * common point to start the search and update/creation of the device.
     *  Supported cases:
     *  Ge
     *  gi1/1/13
     *  Gi8/18
     *  GigabitEthernet0/9
     *  GigabitEthernet0/0/5
     *  POS0/1/0
     * structure
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
    
    private long getParentPortIdIfExists(JsonObject portFound) 
            throws InvalidArgumentException, ObjectNotFoundException, MetadataObjectNotFoundException 
    {
        String objParentName = portFound.getString("parentName");
        String objParentClassName = portFound.getString("parentClassName");

        for(long key : oldObjectStructure.keySet()){
            List<RemoteBusinessObjectLight> oldBranch = oldObjectStructure.get(key);
            for (RemoteBusinessObjectLight oldObj : oldBranch) {
                HashMap<String, List<String>> oldAttributes = bem.getObject(oldObj.getId()).getAttributes();
                if(oldAttributes.get(Constants.PROPERTY_NAME).get(0).equals(objParentName)
                        && objParentClassName.equals(oldObj.getClassName()))
                    return oldObj.getId();
            }
        }
        return 0;
    }
    
    private void editNewPortWithDeviceParentId(JsonObject newPortWithDeviceParentId) {
        for (int i=0; i<newPorts.size(); i++) {
            if(newPortWithDeviceParentId.getJsonObject("attributes").getString("name").equals(newPorts.get(i).getJsonObject("attributes").getString("name")) &&
                    newPortWithDeviceParentId.getString("className").equals(newPorts.get(i).getString("className"))){
                newPorts.set(i, newPortWithDeviceParentId);
            }
        }
    }
   //</editor-fold>
    
    //<editor-fold desc="List Types" defaultstate="collapsed">
    /**
     * Returns the listTypeId if exists or creates a finding to create the list-type 
     * in case that the list type doesn't exist in Kuwaiba
     * 
     * JSon structure
     * {
     *  type: listType
     *  name: "the new list type name"
     * }
     * 
     * @param i the index of the list type in the SNMP data
     * @return the list type (if exists, otherwise is an empty String)
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     */
    private String findingListTypeId(int i, String listTypeClassName) throws MetadataObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException {
        String SNMPoid; 
        if(listTypeClassName.equals("EquipmentVendor")) //NOI18N
            SNMPoid = "entPhysicalMfgName"; //NOI18N
        else
            SNMPoid = "entPhysicalModelName"; //NOI18N
        
        if (!allData.get(SNMPoid).get(i).isEmpty()) {
            String listTypeNameFromSNMP = allData.get(SNMPoid).get(i);
            String listTypeId = "";
            Long id_ = matchListTypeNames(listTypeNameFromSNMP, listTypeClassName);
            if (id_ > 0) {
                listTypeId = Long.toString(id_);
            } else {//The list type doesn't exist, we create a finding
                if (!listTypeEvaluated.contains(listTypeNameFromSNMP)) {
                    long createListTypeItem = aem.createListTypeItem(listTypeClassName, listTypeNameFromSNMP.trim(), listTypeNameFromSNMP.trim());
                    findings.add(new SyncFinding(SyncFinding.EVENT_NEW,
                            String.format(I18N.gm("list_type_will_be_created"), listTypeNameFromSNMP),
                            Json.createObjectBuilder()
                            .add("type", "listtype") //NOI18N
                            .add("name", listTypeNameFromSNMP) //NOI18N
                            .build().toString()));

                    listTypeEvaluated.add(listTypeNameFromSNMP);
                    return Long.toString(createListTypeItem);
                }
            }
            return listTypeId;
        }
        return null;
    }

    /**
     * Compare the names from the SNMP file in order to find one that match with
     * a created list types in Kuwaiba
     * @param listTypeNameToLoad the list type name
     * @return the kuwaiba's list type item id
     * @throws MetadataObjectNotFoundException if the list type doesn't exists
     * @throws InvalidArgumentException if the class name provided is not a list type
     */
    private long matchListTypeNames(String listTypeNameToLoad, String listTypeClassName) throws MetadataObjectNotFoundException, InvalidArgumentException {
        List<RemoteBusinessObjectLight> listTypeItems = aem.getListTypeItems(listTypeClassName);
        List<String> onlyNameListtypes = new ArrayList<>();
        for(RemoteBusinessObjectLight createdLitType : listTypeItems)
            onlyNameListtypes.add(createdLitType.getName());
        
        if(onlyNameListtypes.contains(listTypeNameToLoad))
                return listTypeItems.get(onlyNameListtypes.indexOf(listTypeNameToLoad)).getId();
        
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
