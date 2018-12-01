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
package com.neotropic.kuwaiba.sync.connectors.snmp.reference;

import com.neotropic.kuwaiba.sync.model.SyncFinding;
import com.neotropic.kuwaiba.sync.model.SyncResult;
import com.neotropic.kuwaiba.sync.model.SyncUtil;
import com.neotropic.kuwaiba.sync.model.TableData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.Pool;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessObject;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.util.i18n.I18N;
import org.openide.util.Exceptions;


/**
 * Loads data from a SNMP file to replace/update an existing element in the
 * inventory
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class EntPhysicalSynchronizer {
    /**
     * The class name of the object
     */
    private final String className;
    /**
     * Device id
     */
    private final long id;
    /**
     * Device Data Source Configuration id
     */
    private final long dsConfigId;
    /**
     * Current structure of the device
     */
    private final HashMap<Long, List<BusinessObjectLight>> currentObjectStructure;
    /**
     * To load the structure of the actual device
     */
    private final List<BusinessObjectLight> currentVirtualPorts;
    /**
     * The current first level children of the actual device
     */
    private List<BusinessObjectLight> currentFirstLevelChildren;
    /**
     * The current ports in the device
     */
    private final List<BusinessObjectLight> currentPorts;
    /**
     * Current MPLS tunnels
     */
    private final List<BusinessObjectLight> currentMplsTunnels;
    /**
     * Current bridge domains
     */
    private final List<BusinessObjectLight> currentBridgeDomains;
    /**
     * Current VLANS 
     */
    private final List<BusinessObjectLight> currentVlans;
    /**
     * To keep a trace of the new ports created during synchronization
     */
    private final List<JsonObject> newPorts;
    /**
     * The ports of the device before the synchronization
     */
    private final List<JsonObject> notMatchedPorts;
    /**
     * To keep a trace of the list types evaluated, to not create them twice
     */
    private final List<String> listTypeEvaluated;
    /**
     * An aux variable, used to store the branch of the old object structure
     * while the objects are checked, before the creations of the branch
     */
    private List<BusinessObjectLight> tempAuxOldBranch = new ArrayList<>();
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
     * The entity table loaded into the memory
     */
    private final HashMap<String, List<String>> entityData;
    /**
     * The if-mib table loaded into the memory
     */
    private final HashMap<String, List<String>> ifXTable;
    /**
     * Default initial ParentId in the SNMP table data
     */
    private String INITAL_ID;
    /**
     * To keep the objects during synchronization
     */
    private List<JsonObject> branch;
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
    /**
     * Helper used to read the actual structure recursively
     */
    private long k = 0;
    /**
    * debugMode
    */
    private boolean debugMode;
    
    public EntPhysicalSynchronizer(long dsConfigId, BusinessObjectLight obj, List<TableData> data) {
        
        try {
            PersistenceService persistenceService = PersistenceService.getInstance();
            bem = persistenceService.getBusinessEntityManager();
            aem = persistenceService.getApplicationEntityManager();
            mem = persistenceService.getMetadataEntityManager();
        } catch (IllegalStateException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE,
                    ex.getClass().getSimpleName() + ": {0}", ex.getMessage()); //NOI18N
            bem = null;
            aem = null;
            mem = null;
        }
        this.className = obj.getClassName();
        this.id = obj.getId();
        this.dsConfigId = dsConfigId;
        entityData = (HashMap<String, List<String>>)data.get(0).getValue();
        ifXTable = (HashMap<String, List<String>>)data.get(1).getValue();
        currentObjectStructure = new HashMap<>();
        newPorts = new ArrayList<>();
        currentPorts = new ArrayList<>();
        listTypeEvaluated = new ArrayList<>();
        branch = new ArrayList<>();
        notMatchedPorts = new ArrayList<>();
        currentFirstLevelChildren = new ArrayList<>();
        currentVirtualPorts = new ArrayList<>();
        currentMplsTunnels = new ArrayList<>();
        currentBridgeDomains = new ArrayList<>();
        currentVlans = new ArrayList<>();
        debugMode = (boolean)aem.getConfiguration().get("debugMode");
    }

    public List<SyncFinding> sync() throws MetadataObjectNotFoundException,
            BusinessObjectNotFoundException, InvalidArgumentException,
            OperationNotPermittedException, ApplicationObjectNotFoundException, ArraySizeMismatchException, NotAuthorizedException, ServerSideException {
        readData();
        loadClassHierarchy();
        readCurrentFirstLevelChildren();
        readCurrentDeviceStructure(bem.getObjectChildren(className, id, -1));
        readCurrentSpecialStructure(bem.getObjectSpecialChildren(className, id));
        //printData(); //<- for debuging 
        checkObjects(Long.toString(id), "", "");
        checkPortsToMigrate();
        checkDataToBeDeleted();
        checkPortsWithNoMatch();
        syncIfMibData();
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
        INITAL_ID = entityData.get("entPhysicalContainedIn").get(entityData.get("entPhysicalClass").indexOf("3"));
        if (entityData.get("entPhysicalContainedIn").contains(INITAL_ID))
            createTreeFromFile(INITAL_ID);
        else 
            findings.add(new SyncFinding(dsConfigId, SyncFinding.EVENT_ERROR,
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
        for (int i = 0; i < entityData.get("entPhysicalContainedIn").size(); i++) {
            if (entityData.get("entPhysicalContainedIn").get(i).equals(parentId)) {
                if (isClassUsed(entityData.get("entPhysicalClass").get(i),
                        entityData.get("entPhysicalDescr").get(i))) {
                    saveInTreeMap(parentId, entityData.get("instance").get(i));
                    createTreeFromFile(entityData.get("instance").get(i));
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
        keysToremove.forEach((key) -> {
            mapOfFile.remove(key);
        });
    }

    /**
     * Creates the Hash map of classes to create the hierarchy containment
     */
    private void createMapOfClasses() {
        mapOfClasses = new HashMap<>();
        for (String key : mapOfFile.keySet()) {
            if (!key.equals("0")) {
                List<String> childrenId = mapOfFile.get(key);
                int w = entityData.get("instance").indexOf(key);
                String patentClassParsed = parseClass(
                        entityData.get("entPhysicalModelName").get(w),
                        entityData.get("entPhysicalClass").get(w),
                        entityData.get("entPhysicalName").get(w),
                        entityData.get("entPhysicalDescr").get(w)//NOI18N
                );

                if(patentClassParsed != null){
                    List<String> childrenParsed = mapOfClasses.get(patentClassParsed);
                    if (childrenParsed == null) 
                        childrenParsed = new ArrayList<>();

                    for (String child : childrenId) {
                        int indexOfChild = entityData.get("instance").indexOf(child);
                        String childParsedClass = parseClass(
                                entityData.get("entPhysicalModelName").get(w),
                                entityData.get("entPhysicalClass").get(indexOfChild),
                                entityData.get("entPhysicalName").get(indexOfChild),
                                entityData.get("entPhysicalDescr").get(indexOfChild)//NOI18N
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
            List<ClassMetadataLight> currentPossibleChildren = mem.getPossibleChildren(parentClass);
            List<String> possibleChildrenToAdd = mapOfClasses.get(parentClass);
            
            if (possibleChildrenToAdd != null) {
                JsonArray children = Json.createArrayBuilder().build();
                for (String possibleChildToAdd : possibleChildrenToAdd){
                    boolean isPossibleChild = false;
                    for(ClassMetadataLight currentPossibleClassName : currentPossibleChildren){
                        if(possibleChildToAdd.equals(currentPossibleClassName.getName())){
                            isPossibleChild = true;
                            break;
                        }
                    }        
                    
                    if(!isPossibleChild){    
                        JsonObject jchild = Json.createObjectBuilder().add("child", possibleChildToAdd).build();//NOI18N
                        children = SyncUtil.jArrayBuilder(children).add(jchild).build();    
                    }
                }
                if(!children.isEmpty())
                    jsonHierarchy = SyncUtil.joBuilder(jsonHierarchy).add(parentClass, children).build();  
            }
        }//end for
        jHierarchyFinding = SyncUtil.joBuilder(jHierarchyFinding).add("hierarchy",jsonHierarchy).build();//NOI18N
        if(!jsonHierarchy.isEmpty()){
            jHierarchyFinding = SyncUtil.joBuilder(jHierarchyFinding).add("hierarchy",jsonHierarchy).build();//NOI18N
            findings.add(new SyncFinding(dsConfigId, SyncFinding.EVENT_NEW,
                    I18N.gm("error"),
                    jHierarchyFinding.toString()));
        }
    }

    /**
     * Create into kuwaiba's objects the lines read it from the SNMP
     * it creates a branch every time it finds a port or an element with 
     * no children
     * e.g. branch 1)
     *               slot0/0[Slot]+
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
     *      branch 4) 
     *               slot0/1[Slot]+
     *                            |_board0/1[Board]
     *                               (end of branch) 
     * -------------------------------------------------------------------------                           
     * @param parentId
     * @param parentName
     * @param parentClassName
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws InvalidArgumentException
     * @throws ApplicationObjectNotFoundException
     */
    private void checkObjects(String parentId, String parentName, String parentClassName)
            throws MetadataObjectNotFoundException,
            BusinessObjectNotFoundException, OperationNotPermittedException,
            InvalidArgumentException, ApplicationObjectNotFoundException 
    {
        if(mapOfFile.isEmpty())
            throw new InvalidArgumentException("The router model you are trying to synchronize is not yet supported. Contact your administrator");
        
        if (Long.valueOf(parentId) == id)//If is the first element
            parentId = INITAL_ID;

        List<String> childrenIds = mapOfFile.get(parentId);
        if (childrenIds != null) {
            for (String childId : childrenIds) {
                
                int i = entityData.get("instance").indexOf(childId); //NOI18N
                parentId = entityData.get("entPhysicalContainedIn").get(i); //NOI18N
                if (parentClassName.equals(className)) //if is the chassis we must keep the id
                    parentId = Long.toString(id);

                String objectName = entityData.get("entPhysicalName").get(i); //NOI18N
                //We parse the class Id from SNMP into kuwaiba's class name
                String mappedClass = parseClass(
                        entityData.get("entPhysicalModelName").get(i),
                        entityData.get("entPhysicalClass").get(i), 
                        objectName, entityData.get("entPhysicalDescr").get(i)); //NOI18N
                //We standarized the port names
                if(!className.equals(mappedClass) && SyncUtil.isSynchronizable(objectName) && mappedClass.toLowerCase().contains("port") && !objectName.contains("Power") && !mappedClass.contains("Power"))
                    objectName = SyncUtil.wrapPortName(objectName);
                                
                if(mappedClass == null) //it was impossible to parse the SNMP class into kuwaiba's class
                    findings.add(new SyncFinding(dsConfigId, SyncFinding.EVENT_ERROR,
                                I18N.gm("empty_fields_in_the_data"),
                                Json.createObjectBuilder().add("type","error")
                                        .add("className", entityData.get("entPhysicalClass").get(i))
                                        .add("InstanceId", childId).build().toString()));
                else{
                    HashMap<String, String> newAttributes = createNewAttributes(mappedClass, i);
                    //This applies only for the chassis, we only update
                    if (className.contains(mappedClass)) {
                        newAttributes.remove("name"); //NOI18N the router name won't be changed
                        HashMap<String, String> comparedAttributes = SyncUtil.compareAttributes(bem.getObject(id).getAttributes(), newAttributes);
                        if (!comparedAttributes.isEmpty()) {
                            comparedAttributes.put("name", bem.getObject(id).getAttributes().get("name"));//we need to add the name as atribute again to show the name in the results
                            findings.add(new SyncFinding(dsConfigId, SyncFinding.EVENT_UPDATE,
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
                                .add("attributes", SyncUtil.parseAttributesToJson(newAttributes))
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
                            findings.add(new SyncFinding(dsConfigId, SyncFinding.EVENT_NEW,
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
     * @throws BusinessObjectNotFoundException
     */
    private boolean isBranchAlreadyCreated(List<JsonObject> newBranchToEvalueate) throws InvalidArgumentException, 
            MetadataObjectNotFoundException, BusinessObjectNotFoundException 
    {
        List<List<BusinessObjectLight>> oldBranchesWithMatches = searchInOldStructure(newBranchToEvalueate);
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
            List<BusinessObjectLight> oldBranch = oldBranchesWithMatches.get(indexOfTheLargestsize);
            return  partOfTheBranchMustBeCreated(oldBranch, newBranchToEvalueate);
        }
        return false; //if is empty all the branch should by created
    }
    
    private boolean partOfTheBranchMustBeCreated(List<BusinessObjectLight> oldBranch, 
            List<JsonObject> newBranchToEvalueate) throws InvalidArgumentException, 
            BusinessObjectNotFoundException, MetadataObjectNotFoundException
    {
        //A new element to add we look for the parent id of the new element
        if(oldBranch.size() < newBranchToEvalueate.size()){ 
            List<JsonObject> newPartOfTheBranch = newBranchToEvalueate.subList(oldBranch.size(), newBranchToEvalueate.size());
            if(!newPartOfTheBranch.isEmpty()){//lets search if the part exists
                List<List<BusinessObjectLight>> oldBranchesWithMatches = searchInOldStructure(newPartOfTheBranch);
                if(oldBranchesWithMatches != null && !oldBranchesWithMatches.isEmpty()){//we found something
                    oldBranch.addAll(oldBranchesWithMatches.get(0));
                    return partOfTheBranchMustBeCreated(oldBranch, newBranchToEvalueate);
                }
                if(oldBranchesWithMatches == null)//we find the other part, the branch exists
                    return true; 
                else if(oldBranchesWithMatches.isEmpty()){ //the new part of the branch doesn't exists, so we are going to create it an remove the part that exists 
                    BusinessObjectLight oldObj = oldBranch.get(oldBranch.size() -1);
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
                        newObj = SyncUtil.joBuilder(newObj).add("deviceParentId", Long.toString(oldObj.getId())).build();
                        if(newObj.getString("className").contains("Port"))
                            editNewPortWithDeviceParentId(newObj);
                        branch.set(oldBranch.size(), newObj);  
                    }
                    //we remove the part of the branch that already exists, and update the attributes if is necessary
                    int matchesToDelete = 0;
                    for (int i = 0; i < oldBranch.size(); i++) {
                        oldObj = oldBranch.get(i);
                        BusinessObjectLight oldParent = bem.getParent(oldObj.getClassName(), oldObj.getId());
                        oldParent = SyncUtil.wrapPortName(oldParent);
                        
                        HashMap<String, String> oldAttributes = bem.getObject(oldObj.getId()).getAttributes();

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
            List<BusinessObjectLight> subOldBranchToRemove = oldBranch.subList(branch.size(), oldBranch.size()-1);
            for (BusinessObjectLight removedObj : subOldBranchToRemove) {
                JsonObject jdevice = Json.createObjectBuilder()
                        .add("deviceId", Long.toString(removedObj.getId()))
                        .add("deviceName", removedObj.getName())
                        .add("deviceClassName", removedObj.getClassName())
                        .build();

                findings.add(new SyncFinding(dsConfigId, SyncFinding.EVENT_DELETE,
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
     * @throws BusinessObjectNotFoundException 
     */
    private List <List<BusinessObjectLight>> searchInOldStructure(List<JsonObject> newBranchToEvalueate) throws InvalidArgumentException, 
            MetadataObjectNotFoundException, BusinessObjectNotFoundException
    {
        List <List<BusinessObjectLight>> oldBranchesWithMatches = new ArrayList<>();
        List<BusinessObjectLight> foundPath = new ArrayList<>();
        boolean hasBeenFound = false; //This is usded because some branch are in disorder
        
        for (long i : currentObjectStructure.keySet()) {
            foundPath = new ArrayList<>();
            List<BusinessObjectLight> oldBranch = currentObjectStructure.get(i);
            int end = oldBranch.size() > newBranchToEvalueate.size() ? newBranchToEvalueate.size() : oldBranch.size();
            for (int w=0; w < end; w++) {
                String objClassName = newBranchToEvalueate.get(w).getString("className");
                String objParentName = newBranchToEvalueate.get(w).getString("parentName");
                String objParentClassName = newBranchToEvalueate.get(w).getString("parentClassName");
                JsonObject objAttributes = newBranchToEvalueate.get(w).getJsonObject("attributes");
                String newObjName = objAttributes.getString("name");
                if (!className.equals(objClassName)) {
                    BusinessObjectLight oldObj = oldBranch.get(w);
                    BusinessObjectLight oldParent = bem.getParent(oldObj.getClassName(), oldObj.getId());
                    oldParent = SyncUtil.wrapPortName(oldParent); //we standardize if the parent object is a port, important in ASR9001 hwere ports are not leafs in the structure
                    
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
                    BusinessObjectLight oldObj = foundPath.get(j);
                    HashMap<String, String> oldAttributes = bem.getObject(oldObj.getId()).getAttributes();
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
                        BusinessObjectLight oldObj = oldBranch.get(o-1);
                        BusinessObjectLight oldParent = bem.getParent(oldObj.getClassName(), oldObj.getId());
                        oldParent = SyncUtil.wrapPortName(oldParent); //we standardize if the parent object is a port, important in ASR9001 hwere ports are not leafs in the structure
                                
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
                    for (int j = foundPath.size() - 1, r = 0; j > -1; j--, r++) { 
                        BusinessObjectLight oldObj = foundPath.get(j);
                        HashMap<String, String> oldAttributes = bem.getObject(oldObj.getId()).getAttributes();
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
            for (long i : currentObjectStructure.keySet()) {
                List<BusinessObjectLight> oldBranch = currentObjectStructure.get(i);
                String objClassName = newBranchToEvalueate.get(0).getString("className");
                String objParentName = newBranchToEvalueate.get(0).getString("parentName");
                String objParentClassName = newBranchToEvalueate.get(0).getString("parentClassName");
                JsonObject objAttributes = newBranchToEvalueate.get(0).getJsonObject("attributes");
                String newObjName = objAttributes.getString("name");
                if (!className.equals(objClassName)) {
                    BusinessObjectLight oldObj = oldBranch.get(oldBranch.size()-1);
                    BusinessObjectLight oldParent = bem.getParent(oldObj.getClassName(), oldObj.getId());
                    oldParent = SyncUtil.wrapPortName(oldParent);//we standardize if the parent object is a port, important in ASR9001 hwere ports are not leafs in the structure
                    
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
                jObj = SyncUtil.joBuilder(jObj).add("deviceParentId", Long.toString(id)).build();
                branch.set(0, jObj);
                return new ArrayList<>();
            }
            //This is the less accurate method 
            //we only check for the parent name of the first element of the new 
            //branch with the last element of the old branch.
            for (long i : currentObjectStructure.keySet()) {
                List<BusinessObjectLight> oldBranch = currentObjectStructure.get(i);
                String objParentName = newBranchToEvalueate.get(0).getString("parentName");
                String objParentClassName = newBranchToEvalueate.get(0).getString("parentClassName");
                if(oldBranch.size() > 2){ //we only check old branches with more than one element, otherwise they are ports
                    BusinessObjectLight oldParent = oldBranch.get(oldBranch.size() - 2);
                    if (oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName)){
                        JsonObject jObj = branch.get(0);
                        jObj = SyncUtil.joBuilder(jObj).add("deviceParentId", Long.toString(oldParent.getId())).build();
                        if(jObj.getString("className").contains("Port"))
                            editNewPortWithDeviceParentId(jObj);
                        branch.set(0, jObj);
                        break;
                    }
                }
            }
            //if the branch could not be found still its necessary to check again 
            //in the old branch for the second element (an IPboard)
            for (long i : currentObjectStructure.keySet()) {
                List<BusinessObjectLight> oldBranch = currentObjectStructure.get(i);
                String objParentName = newBranchToEvalueate.get(0).getString("parentName");
                String objParentClassName = newBranchToEvalueate.get(0).getString("parentClassName");
                if(oldBranch.size()>1){
                    BusinessObjectLight oldParent = oldBranch.get(1);
                    if (oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName)){
                        JsonObject jObj = branch.get(0);
                        jObj = SyncUtil.joBuilder(jObj).add("deviceParentId", Long.toString(oldParent.getId())).build();
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
   
    private void updateAttributesInBranch(BusinessObjectLight oldObj, HashMap<String, String> oldAttributes, 
            JsonObject newObj) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException
    {
        JsonObject objAttributes = newObj.getJsonObject("attributes");
        HashMap<String, String> attributeChanges = SyncUtil.compareAttributes(oldAttributes, objAttributes);
        if(!attributeChanges.isEmpty()){
            newObj = SyncUtil.joBuilder(newObj).add("deviceId", Long.toString(oldObj.getId())).build();
            newObj = SyncUtil.joBuilder(newObj).add("type", "device").build();
            newObj = SyncUtil.joBuilder(newObj).add("deviceClassName", oldObj.getClassName()).build();
            newObj = SyncUtil.joBuilder(newObj).add("attributes", SyncUtil.parseAttributesToJson(attributeChanges)).build();
            newObj = SyncUtil.joBuilder(newObj).add("oldAttributes", SyncUtil.parseOldAttributesToJson(oldAttributes)).build();
            findings.add(new SyncFinding(dsConfigId, SyncFinding.EVENT_UPDATE, String.format(I18N.gm("object_attributes_changed"), oldObj.toString()), newObj.toString()));
        }
    }
    
    /**
     * checks if the first level of children has changes
     * @param json the new object
     * @return true if is already created
     */
    private boolean isDeviceAlreadyCreated(JsonObject json){
        BusinessObjectLight objFound = null;
        for (BusinessObjectLight currentFirstLevelChild : currentFirstLevelChildren) {
            if(currentFirstLevelChild.getClassName().equals(json.getString("className")) && 
                    currentFirstLevelChild.getName().equals(json.getJsonObject("attributes").getString("name"))){
                objFound = currentFirstLevelChild;
                break;
            }
        }   
        if(objFound != null)
            currentFirstLevelChildren.remove(objFound);
        return false;
    }

    /**
     * Reads the device's special children 
     * @param children special children
     * @throws MetadataObjectNotFoundException If the class could not be found
     * @throws BusinessObjectNotFoundException If the object could not be found
     */
    private void readCurrentSpecialStructure(List<BusinessObjectLight> children) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException
    {
        for (BusinessObjectLight child : children) {
            if (child.getClassName().equals(Constants.CLASS_MPLSTUNNEL))
                currentMplsTunnels.add(child);
            else if (child.getClassName().equals(Constants.CLASS_VIRTUALPORT))
                currentVirtualPorts.add(child);
            readCurrentSpecialStructure(bem.getObjectSpecialChildren(child.getClassName(), child.getId()));
        }
    }
    
    /**
     * Reads the current object and make a copy of the structure, from this
     * structure the ports can be updated and moved to the new created tree in
     * order to keep the special relationships.
     * @param objects the list of elements of a level
     * @throws MetadataObjectNotFoundException if something goes wrong with the class metadata
     * @throws BusinessObjectNotFoundException if some object can not be find
     */
    private void readCurrentDeviceStructure(List<BusinessObjectLight> objects)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        for (BusinessObjectLight object : objects) {
            if (!mem.isSubClass("GenericLogicalPort", object.getClassName()) && !mem.isSubClass("Pseudowire", object.getClassName())){ 
                //We standarized the port names
                object = SyncUtil.wrapPortName(object);
                tempAuxOldBranch.add(object);
            }
            
            if (object.getClassName().contains("Port") && !object.getClassName().contains("Virtual") && !object.getClassName().contains("Power")) 
                currentPorts.add(object);
            
            else if (object.getClassName().contains("Virtual")) 
                currentVirtualPorts.add(object);
            
            List<BusinessObjectLight> children = bem.getObjectChildren(object.getClassName(), object.getId(), -1);
            if (!children.isEmpty()) 
                readCurrentDeviceStructure(children);
            else {
                if(!tempAuxOldBranch.isEmpty())
                    currentObjectStructure.put(k, tempAuxOldBranch);
                tempAuxOldBranch = new ArrayList<>();
                k++;
            }
        }
    }

    private void readCurrentFirstLevelChildren() {
        try {
            currentFirstLevelChildren = bem.getObjectChildren(className, id, -1);
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
            Logger.getLogger(EntPhysicalSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    private JsonObject listToJson(List<JsonObject> branch, String type) {
        JsonObject json = Json.createObjectBuilder().add("type", type).build();
        JsonArray children = Json.createArrayBuilder().build();

        for (JsonObject jo : branch) 
            children = SyncUtil.jArrayBuilder(children).add(Json.createObjectBuilder().add("child", jo)).build();
        
        json = SyncUtil.joBuilder(json).add("children", children).build();

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
    private JsonObject createExtraInfoToUpdateAttributesInObject(String deviceId, String deviceClassName, HashMap<String, String> newAttributes, HashMap<String, String> oldAttributes) {
        JsonObject jsonObj = Json.createObjectBuilder().add("type", "device").add("deviceId", deviceId).add("deviceClassName", deviceClassName).build();
        JsonObject jNewAttributes = Json.createObjectBuilder().build();
        JsonObject jOldAttributes = Json.createObjectBuilder().build();
        for (String key : newAttributes.keySet()){ 
            jNewAttributes = SyncUtil.joBuilder(jNewAttributes).add(key, newAttributes.get(key).trim()).build();
            if(oldAttributes.get(key) != null)
                jOldAttributes = SyncUtil.joBuilder(jOldAttributes).add(key, oldAttributes.get(key).trim()).build();
        }
        jsonObj = SyncUtil.joBuilder(jsonObj).add("attributes", jNewAttributes).add("oldAttributes", jOldAttributes).build();
        return jsonObj;
    }

    /**
     * Create a hash map for the attributes of the given index encathc of the data read it from SNMP
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
               
        String objectName = entityData.get("entPhysicalName").get(index);//NOI18N
        //We standarized the port names
        if(!className.equals(mappedClass) && SyncUtil.isSynchronizable(objectName) && mappedClass.toLowerCase().contains("port") && !objectName.contains("Power") && !mappedClass.contains("Power"))
            objectName = SyncUtil.wrapPortName(objectName);
        
        attributes.put("name", objectName);//NOI18N
        String description = entityData.get("entPhysicalDescr").get(index).trim();
        if(!description.isEmpty()){
            if(mappedClassMetadata.getAttribute("description") == null)
                createAttributeError(mappedClass, "description", "String");
            
            attributes.put("description", description);
        }
        if (!entityData.get("entPhysicalMfgName").get(index).isEmpty()) {
            if(mappedClassMetadata.getAttribute("vendor") != null){
                String vendor = findingListTypeId(index, "EquipmentVendor");//NOI18N
                if (vendor != null) 
                    attributes.put("vendor", vendor); //NOI18N
            }
            else
                 createAttributeError(mappedClass, "vendor", "EquipmentVendor");    
        }
        if (!entityData.get("entPhysicalSerialNum").get(index).isEmpty()){
            if(mappedClassMetadata.getAttribute("serialNumber") == null)
                createAttributeError(mappedClass, "serialNumber", "String");
        
            attributes.put("serialNumber", entityData.get("entPhysicalSerialNum").get(index).trim());//NOI18N
        }
        if (!entityData.get("entPhysicalModelName").get(index).isEmpty()){ 
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
       findings.add(new SyncFinding(dsConfigId, SyncFinding.EVENT_ERROR,
                                    String.format(I18N.gm("attribute_does_not_exist_in_class"), attributeName, type, aClass),
                                    Json.createObjectBuilder().add("type","error")
                                            .add("className", aClass)
                                            .add("attributeType", type)  
                                            .add("attributeName", attributeName).build().toString()));
    }
    

    //Things to be deleted
    public void removeObjectFromDelete(BusinessObjectLight obj) {
        for (long branchId : currentObjectStructure.keySet()) 
            currentObjectStructure.get(branchId).remove(obj);
    }
    
    public void checkDataToBeDeleted() throws MetadataObjectNotFoundException {
        JsonObject json = Json.createObjectBuilder().add("type", "old_object_to_delete").build();
        for (BusinessObjectLight currentChildFirstLevel : currentFirstLevelChildren) {
            if (!mem.isSubClass("Pseudowire", currentChildFirstLevel.getClassName()) 
                    && !currentChildFirstLevel.getName().toLowerCase().equals("gi0")) 
            {
                JsonObject jdevice = Json.createObjectBuilder()
                            .add("deviceId", Long.toString(currentChildFirstLevel.getId()))
                            .add("deviceName", currentChildFirstLevel.getName())
                            .add("deviceClassName", currentChildFirstLevel.getClassName())
                            .build();
                
                json = SyncUtil.joBuilder(json).add("device", jdevice).build();
                findings.add(new SyncFinding(dsConfigId, SyncFinding.EVENT_DELETE,
                            String.format("The %s - %s and all its children WILL BE DELETED. Perhaps it was removed physically from the device. If you are not sure select SKIP this action", 
                                    currentChildFirstLevel.toString(), Long.toString(currentChildFirstLevel.getId())),
                            json.toString()));
            }
        }
            
        for (long key : currentObjectStructure.keySet()) {
            List<BusinessObjectLight> branchToDelete = currentObjectStructure.get(key);
            for (BusinessObjectLight deviceToDelete : branchToDelete) {
                JsonObject jsont = Json.createObjectBuilder().add("type", "old_object_to_delete").build();
                if (deviceToDelete.getClassName().contains("Transceiver")){
                    try {
                    BusinessObjectLight tParent = bem.getParent(deviceToDelete.getClassName(), deviceToDelete.getId());
                    
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
                    
                    jsont = SyncUtil.joBuilder(jsont).add("device", jsonpt).build();
                    jsont = SyncUtil.joBuilder(jsont).add("deviceParent", jsonp).build();
                    
                    if(tParent.getClassName().contains("Port"))
                        findings.add(new SyncFinding(dsConfigId, SyncFinding.EVENT_DELETE,
                           String.format("This device %s was child of %s, but it should be its parent. A new one was place correctly. Would you like to delete the old element? This operations is completely safe", 
                                   deviceToDelete.toString(), tParent.toString()), jsont.toString()));
                    
                    } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                        Logger.getLogger(EntPhysicalSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
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
            json = SyncUtil.joBuilder(json).add("device", oldPort).build();

            findings.add(new SyncFinding(dsConfigId, SyncFinding.EVENT_DELETE,
                    String.format("There was no match for port: %s [%s] - id: %s. Do you want to delete this port after the sync process?",
                            oldPort.getString("deviceName"), oldPort.getString("deviceClassName"), oldPort.getString("deviceId")),
                    json.toString()));
        }

        for (JsonObject jnewPort : newPorts) {
            jnewPort = SyncUtil.joBuilder(jnewPort).add("type", "object_port_no_match_new").build();

            findings.add(new SyncFinding(dsConfigId, SyncFinding.EVENT_NEW,
                    String.format("There was no match for port: %s [%s]. Do you want to create this port after the sync process?",
                            jnewPort.getJsonObject("attributes").getString("name"), jnewPort.getString("className")),
                    jnewPort.toString()));
        }
    }

    /**
     * Compare the old ports with the new ones, the new ports should have been 
     * created in the last steps if they didn't exists, after the creation we compare 
     * the names of the old ports with the names of the new ones, if a match is 
     * found the old port will me moved to its new location, and the new 
     * port won't be created, this way we keep the relationships of the old port
     *
     * @throws InvalidArgumentException
     * @throws BusinessObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws OperationNotPermittedException
     */
    private void checkPortsToMigrate() throws InvalidArgumentException,
            BusinessObjectNotFoundException, MetadataObjectNotFoundException,
            OperationNotPermittedException {
        //List<BusinessObjectLight> foundOldPorts = new ArrayList<>(); //for debug
        List<JsonObject> foundNewPorts = new ArrayList<>(); //for debug
        for (BusinessObjectLight oldPort : currentPorts) {
            if(!oldPort.getName().toLowerCase().equals("gi0")){
                //We copy the new attributes into the old port, to keep the relationships
                BusinessObjectLight oldPortParent = bem.getParent(oldPort.getClassName(), oldPort.getId());
                JsonObject portFound = searchOldPortInNewPorts(oldPort);

                if (portFound != null) {
                    String parentName = oldPortParent.getName();
                    //foundOldPorts.add(oldPort); 
                    foundNewPorts.add(portFound); //for debug
                    if(SyncUtil.isSynchronizable(portFound.getJsonObject("attributes").getString("name")))
                        portFound = SyncUtil.wrapPortName(portFound);
                    //We found the port, but needs to be moved
                    if(!parentName.equals(portFound.getString("parentName"))){ 
                        portFound = SyncUtil.joBuilder(portFound).add("type", "object_port_move").build();
                        portFound = SyncUtil.joBuilder(portFound).add("childId", Long.toString(oldPort.getId())).build();
                        
                        long parentId = getParentPortIdIfExists(portFound);
                        if(parentId > 0 )
                            portFound = SyncUtil.joBuilder(portFound).add("deviceParentId", Long.toString(parentId)).build();

                        findings.add(new SyncFinding(dsConfigId, SyncFinding.EVENT_UPDATE,
                                String.format("The port %s with id: %s will be moved to a new location to match the structure reported by the SNMP agent, do you want to proceed?", oldPort.toString(), oldPort.getId()),
                                portFound.toString()));
                    }
                    else{//The port its in the rigth place but its attributes needs to be updated
                        HashMap<String, String> oldAttributes = bem.getObject(oldPort.getId()).getAttributes();
                        HashMap<String, String> changedAttributes = SyncUtil.compareAttributes(oldAttributes, portFound.getJsonObject("attributes"));
                        if(!changedAttributes.isEmpty()){ 
                            portFound = SyncUtil.joBuilder(portFound).add("type", "device").build();
                            portFound = SyncUtil.joBuilder(portFound).add("deviceClassName", oldPort.getClassName()).build();
                            portFound = SyncUtil.joBuilder(portFound).add("deviceId", Long.toString(oldPort.getId())).build();
                            portFound = SyncUtil.joBuilder(portFound).add("attributes", SyncUtil.parseAttributesToJson(changedAttributes)).build();
                            portFound = SyncUtil.joBuilder(portFound).add("oldAttributes", SyncUtil.parseOldAttributesToJson(oldAttributes)).build();
                            findings.add(new SyncFinding(dsConfigId, SyncFinding.EVENT_UPDATE,
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
            }
        }//end for
        //we remove the ports from both lists the old port and the new ones
        //for debuging don't delete        
//        for (BusinessObjectLight goodPort : foundOldPorts)
//            oldPorts.remove(goodPort);

        //we must delete de new ports that were found in the old structure, the 
        //remaining new ports that were not found will be created
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
     * @param oldPort the old port
     * @return a String pair key = The new parent Id (in the SNMP map) value = the new port
     * @throws InvalidArgumentException
     * @throws BusinessObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     */
    private JsonObject searchOldPortInNewPorts(BusinessObjectLight oldPort) throws InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        for (JsonObject jsonPort : newPorts) {
            if (SyncUtil.compareLegacyPortNames(oldPort.getName(), oldPort.getClassName(),
                    jsonPort.getJsonObject("attributes").getString("name"),
                    jsonPort.getString("className"))
                ) 
                return jsonPort;
        }
        return null;
    }
    
    private long getParentPortIdIfExists(JsonObject portFound) 
            throws InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException 
    {
        String objParentName = portFound.getString("parentName");
        String objParentClassName = portFound.getString("parentClassName");

        for(long key : currentObjectStructure.keySet()){
            List<BusinessObjectLight> oldBranch = currentObjectStructure.get(key);
            for (BusinessObjectLight oldObj : oldBranch) {
                HashMap<String, String> oldAttributes = bem.getObject(oldObj.getId()).getAttributes();
                if(oldAttributes.get(Constants.PROPERTY_NAME).equals(objParentName)
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
        
        if (!entityData.get(SNMPoid).get(i).isEmpty()) {
            String listTypeNameFromSNMP = entityData.get(SNMPoid).get(i);
            String listTypeId = "";
            Long id_ = matchListTypeNames(listTypeNameFromSNMP, listTypeClassName);
            if (id_ > 0) {
                listTypeId = Long.toString(id_);
            } else {//The list type doesn't exist, we create a finding
                if (!listTypeEvaluated.contains(listTypeNameFromSNMP)) {
                    long createListTypeItem = aem.createListTypeItem(listTypeClassName, listTypeNameFromSNMP.trim(), listTypeNameFromSNMP.trim());
                    findings.add(new SyncFinding(dsConfigId, SyncFinding.EVENT_NEW,
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
        List<BusinessObjectLight> listTypeItems = aem.getListTypeItems(listTypeClassName);
        List<String> onlyNameListtypes = new ArrayList<>();
        for(BusinessObjectLight createdLitType : listTypeItems)
            onlyNameListtypes.add(createdLitType.getName());
        
        if(onlyNameListtypes.contains(listTypeNameToLoad))
                return listTypeItems.get(onlyNameListtypes.indexOf(listTypeNameToLoad)).getId();
        
        for (BusinessObjectLight createdLitType : listTypeItems) {
            int matches = 0;
            int maxLength = listTypeNameToLoad.length() > createdLitType.getName().length() ? listTypeNameToLoad.length() : createdLitType.getName().length();
            listTypeNameToLoad = listTypeNameToLoad.toLowerCase().trim();
            String nameCreatedInKuwaiba = createdLitType.getName().toLowerCase().trim();
            if (listTypeNameToLoad.equals(nameCreatedInKuwaiba)) {
                return createdLitType.getId();
            }
            for (int i = 1; i < maxLength; i++) {
                String a, b;
                if (listTypeNameToLoad.length() < i)
                    break;
                else 
                    a = listTypeNameToLoad.substring(i - 1, i);
                if (nameCreatedInKuwaiba.length() < i)
                    break;
                else 
                    b = nameCreatedInKuwaiba.substring(i - 1, i);
                if (a.equals(b)) 
                    matches++;
            }
            if (matches == listTypeNameToLoad.length()) 
                return createdLitType.getId();
        }
        return -1;
    }
    //</editor-fold>

    private void syncIfMibData() throws MetadataObjectNotFoundException,
            BusinessObjectNotFoundException, InvalidArgumentException, 
            OperationNotPermittedException, ApplicationObjectNotFoundException, ArraySizeMismatchException
    {
        List<String> services = ifXTable.get("ifAlias"); //NOI18N
        List<String> portNames = ifXTable.get("ifName"); //NOI18N
        List<String> portSpeeds = ifXTable.get("ifHighSpeed"); //NOI18N
        
        JsonObject ifMibj = Json.createObjectBuilder().build();
        
        int status = -2, serviceStatus = -1;
        List<JsonObject> jsonResults = new ArrayList<>();
        for(String ifName : portNames){
            HashMap<String, String> attributes = new HashMap<>();
            String ifAlias = services.get(portNames.indexOf(ifName));
            String portSpeed = portSpeeds.get(portNames.indexOf(ifName));
            String createdClassName = "";
            long createdId = 0; 
            boolean wasHighSpeedUpdated = false;
            attributes.put(Constants.PROPERTY_NAME, SyncUtil.wrapPortName(ifName));
            attributes.put("highSpeed", portSpeed);  
            checkVirtualPorts();
            //We must create the Mngmnt Port, virtualPorts, tunnels and Loopbacks
            if(SyncUtil.isSynchronizable(ifName)){
                BusinessObjectLight currrentInterface;
                //First we search if the port is the current virtual ports
                if(ifName.contains(".")){
                    //GE 0/0/0/12.10
                    String x = SyncUtil.wrapPortName(ifName);
                    currrentInterface = searchInCurrentStructure(SyncUtil.wrapPortName(ifName), 2);
                    if(currrentInterface == null)
                        currrentInterface = searchInCurrentStructure(ifName.split("\\.")[1], 2);
                }
                else if(ifName.toLowerCase().contains("lo")) //NOI18N
                    currrentInterface = searchInCurrentStructure(SyncUtil.wrapPortName(ifName), 2);
                //We must add the s when we look for po ports because posx/x/x ports has no s in the if mib
                else if(ifName.toLowerCase().contains("po")) //NOI18N
                    currrentInterface = searchInCurrentStructure(SyncUtil.wrapPortName(ifName), 1);
                //MPLS Tunnel
                else if(ifName.toLowerCase().contains("tu")) //NOI18N
                    currrentInterface = searchInCurrentStructure(ifName, 3);
                else 
                    currrentInterface = searchInCurrentStructure(ifName, 1);
                
                //The virtual port doesn't exists, so we will create it
                if(ifName.contains(".") && currrentInterface == null){
                    //we search for the physical port parent of the virtual port 
                    currrentInterface = searchInCurrentStructure(SyncUtil.wrapPortName(ifName.split("\\.")[0]), 1);
                    if(currrentInterface != null && currrentInterface.getName().equals(SyncUtil.wrapPortName(ifName.split("\\.")[0]))){
                        String className;
                        if(ifName.toLowerCase().contains(".si")){
                            className = "ServiceInstance";
                            attributes.put(Constants.PROPERTY_NAME, ifName.split("\\.")[2]);
                        }else{
                            className = "Constants.CLASS_VIRTUALPORT";
                            attributes.put(Constants.PROPERTY_NAME, ifName.split("\\.")[1]);
                        }
                        
                        createdId = bem.createObject(Constants.CLASS_VIRTUALPORT, currrentInterface.getClassName(), currrentInterface.getId(), attributes, -1);
                        createdClassName = "VirtualPort";
                        status = 1;
                    }
                    else
                        status = -1;
                    
                }//We create the Mngmnt port
                else if(currrentInterface == null && ifName.toLowerCase().equals("gi0")){ 
                    createdId = bem.createObject(Constants.CLASS_ELECTRICALPORT, className, id, attributes, -1);
                    status = 1;
                }//MPLS Tunnel
                else if(currrentInterface == null && ifName.toLowerCase().contains("tu")){
                    attributes.put("ifAlias", ifAlias);
                    createdId = bem.createSpecialObject(Constants.CLASS_MPLSTUNNEL, className, id, attributes, -1); 
                    currentMplsTunnels.add(new BusinessObject(Constants.CLASS_MPLSTUNNEL, createdId, ifName));
                    createdClassName = Constants.CLASS_MPLSTUNNEL;
                    status = 1;
                }//LoopBacks
                else if(currrentInterface == null && ifName.toLowerCase().contains("lo")){
                    createdId = bem.createSpecialObject(Constants.CLASS_VIRTUALPORT, className, id, attributes, -1);
                    currentVirtualPorts.add(new BusinessObject(Constants.CLASS_VIRTUALPORT, createdId, ifName));
                    createdClassName = "VirtualPort";
                    status = 1;
                }else if (currrentInterface == null && ifName.toLowerCase().contains("se")){
                    bem.createObject(Constants.CLASS_SERIALPORT, className, id, attributes, -1);
                    createdClassName = Constants.CLASS_SERIALPORT;
                    status = 1;
                }//we Update attributes, for now only high speed
                else if(currrentInterface != null){ 
                    if(ifName.contains(".") && attributes.get(Constants.PROPERTY_NAME).contains(".")){
                        attributes.put(Constants.PROPERTY_NAME,ifName.split("\\.")[1]);
                        bem.updateObject(currrentInterface.getClassName(), currrentInterface.getId(), attributes);
                        status = 3;
                    }
                    
                    attributes = bem.getObject(currrentInterface.getId()).getAttributes();
                    //The name should be in lowercase
                    if(!attributes.get(Constants.PROPERTY_NAME).toLowerCase().equals(attributes.get(Constants.PROPERTY_NAME))){
                        attributes.put(Constants.PROPERTY_NAME, SyncUtil.wrapPortName(attributes.get(Constants.PROPERTY_NAME)));
                        bem.updateObject(currrentInterface.getClassName(), currrentInterface.getId(), attributes);
                        status = 3;
                    }
                    if(!ifName.toLowerCase().contains("tu")){
                        String currenthighSpeed = attributes.get("highSpeed");
                        if(currenthighSpeed != null && !currenthighSpeed.equals(portSpeed)){
                            attributes.put("highSpeed", portSpeed);
                            wasHighSpeedUpdated = true;
                        }
                    }
                    else if(ifName.toLowerCase().contains("tu"))
                        attributes.put("ifAlias", ifAlias);
                    if(wasHighSpeedUpdated){
                        bem.updateObject(currrentInterface.getClassName(), currrentInterface.getId(), attributes);
                        createdClassName = currrentInterface.getClassName();
                        createdId = currrentInterface.getId();
                        status = 2;
                    }
                }
            }
            if(status != -2){
                if(!createdClassName.isEmpty())
                    serviceStatus = checkServices(ifAlias, createdId, createdClassName);
                
                jsonResults.add(Json.createObjectBuilder()
                            .add("ifName", SyncUtil.wrapPortName(ifName))
                            .add("ifAlias", ifAlias)
                            .add("status", Integer.toString(status))
                            .add("related-service", Integer.toString(serviceStatus))
                            .build());
            }
        }//end for ifNames
                        
        JsonArray ifMibSyncResult = Json.createArrayBuilder().build();
        for (JsonObject jo : jsonResults) 
            ifMibSyncResult = SyncUtil.jArrayBuilder(ifMibSyncResult).add(Json.createObjectBuilder().add("result", jo)).build();
        if(!jsonResults.isEmpty()){
        ifMibj = SyncUtil.joBuilder(ifMibj).add("type", "ifbmib").add("ifmibsync", ifMibSyncResult).build();
            findings.add(new SyncFinding(dsConfigId, SyncResult.TYPE_INFORMATION, 
                           "if-mib synchronization result", ifMibj.toString()));
        }
    }
    
    /**
     * Checks if a given service name exists in kuwaiba in order to 
     * associate the resource read it form the if-mib 
     * @param serviceName the service read it form the  if-mib
     * @param portId the port of the resource created
     * @param portClassName the class name of the port read it form the if-mib 
     * @throws ApplicationObjectNotFoundException
     * @throws BusinessObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     * @throws OperationNotPermittedException 
     */
    private int checkServices(String serviceName, long portId, String portClassName) throws ApplicationObjectNotFoundException, BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException {
        List<BusinessObjectLight> servicesCreatedInKuwaiba = new ArrayList<>();
        //We get the services created in kuwaiba
        List<Pool> serviceRoot = aem.getRootPools("GenericCustomer", 2, false);
        for(Pool customerPool: serviceRoot){
            //TelecoOperators
            List<BusinessObjectLight> poolItems = aem.getPoolItems(customerPool.getId(), -1);
            for(BusinessObjectLight telecoOperator : poolItems){
                List<Pool> poolsInObject = aem.getPoolsInObject(telecoOperator.getClassName(), telecoOperator.getId(), "GenericService");
                //Service Pool
                for(Pool servicePool : poolsInObject){
                    List<BusinessObjectLight> actualServices = aem.getPoolItems(servicePool.getId(), -1);
                    actualServices.forEach((actualService) -> {
                        servicesCreatedInKuwaiba.add(actualService);
                    });
                }
            }
        }//Now we check the resources with the given serviceName or ifAlias
        for(BusinessObjectLight currentService : servicesCreatedInKuwaiba){
            //The service is al ready created in kuwaiba
            if(serviceName.equals(currentService.getName())){
                List<BusinessObjectLight> serviceResources = bem.getSpecialAttribute(currentService.getClassName(), currentService.getId(), "uses");
                for (BusinessObjectLight resource : serviceResources) {
                    if(resource.getId() == portId) //The port is already a resource of the service
                        return 1; //"Service is already related with the interface";
                }
                bem.createSpecialRelationship(currentService.getClassName(), currentService.getId(), portClassName, portId, "uses", true);
                return 0; //"Related successfully with the interface";
            }
        }
        return -1; //doesn't exists
    }
    
    private void checkVirtualPorts(){
        for(BusinessObjectLight currentPort: currentPorts){
            try {
                List<BusinessObjectLight> portChildren = bem.getObjectChildren(currentPort.getClassName(), currentPort.getId(), -1);
                boolean found = false;
                for(int i=0; i< portChildren.size(); i++){
                    for(int j=0; j<portChildren.size(); j++){
                        if(i != j && (portChildren.get(i).getClassName().equals(Constants.CLASS_VIRTUALPORT) && 
                                portChildren.get(j).getClassName().equals(Constants.CLASS_VIRTUALPORT))){
                            if(portChildren.get(i).getName().contains(".")){
                                String afterPoint = portChildren.get(i).getName().split("\\.")[1];
                                if(afterPoint.equals(portChildren.get(j).getName())){
                                    found = true;
                                    HashMap<String, List<BusinessObjectLight>> specialAttributes = bem.getSpecialAttributes(portChildren.get(i).getClassName(), portChildren.get(i).getId());
                                    for (Map.Entry<String, List<BusinessObjectLight>> entry : specialAttributes.entrySet()) {
                                        String key = entry.getKey();
                                        for (BusinessObjectLight businessObjectLight : entry.getValue()) {
                                            try {
                                                bem.createSpecialRelationship(portChildren.get(j).getClassName(), portChildren.get(j).getId(),
                                                businessObjectLight.getClassName(), businessObjectLight.getId(), key, false);
                                            } catch (OperationNotPermittedException ex) {
                                                Exceptions.printStackTrace(ex);
                                            }
                                        }
                                    }
                                    try {
                                        //we delete the port after migrate its relationships
                                        bem.deleteObject(portChildren.get(i).getClassName(), portChildren.get(i).getId(), true);
                                    } catch (OperationNotPermittedException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }
                        }   
                    }
                    if(portChildren.get(i).getName().contains(".") && !found)
                        System.out.println("to delete: "+ portChildren.get(i));
                }
                
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    /**
     * Checks if a given port exists in the current structure
     * @param ifName a given name for port, virtual port or MPLS Tunnel
     * @param type 1 port, 2 virtual port, 3 MPLSTunnel, 4 bdi, 5 VLAN
     * @return the object, null doesn't exists in the current structure
     */
    private BusinessObjectLight searchInCurrentStructure(String ifName, int type){
        switch(type){
            case 1:
                for(BusinessObjectLight currentPort: currentPorts){
                    if(currentPort.getName().toLowerCase().equals(ifName.toLowerCase()))
                        return currentPort;
                }
                break;
            case 2:
                for(BusinessObjectLight currentVirtualPort: currentVirtualPorts){
                    if(currentVirtualPort.getName().toLowerCase().equals(ifName.toLowerCase()))
                        return currentVirtualPort;
                }
                break;
            case 3:
                for(BusinessObjectLight currentMPLSTunnel: currentMplsTunnels){
                    if(currentMPLSTunnel.getName().toLowerCase().equals(ifName.toLowerCase()))
                        return currentMPLSTunnel;
                }
                break;
        }
        return null;
    }
}
