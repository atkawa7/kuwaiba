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
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
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


/**
 * Loads data from a SNMP file to replace/update an existing element in 
 * the inventory
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class MapFromSNMP {
    
    /**
     * The class name of the object
     */
    private String className;
    private long id;
    private String fileName;
    
    private List<RemoteBusinessObjectLight> oldObjectStructure;
    //List<RemoteBusinessObjectLight> oldPorts;
    
    /**
     * a map of the file to create the objects
     */
    HashMap<String, List<String>> mapOfFile = new HashMap<>();
    /**
     * a map of the file to create the classes in the containment hierarchy
     */
    HashMap<String, List<String>> mapOfClasses = new HashMap<>();
    /**
    * The list of objects to create in a proper order.
    */
    List<String> objectsToCreate = new ArrayList<>();
    /**
     * the result list
     */
    List<SyncFinding> findings = new ArrayList<>();
    /**
     * The file loaded into the memory
     */
    HashMap<String, List<String>> allData;
    
    String INITAL_ID = "0";
    
        
    List<String> messages = new ArrayList<>();
    
    private BusinessEntityManager bem;
    private ApplicationEntityManager aem;
    private MetadataEntityManager mem;

    public MapFromSNMP(String className, long id, HashMap<String, List<String>> data) {
        connect();
        this.className = className;
        this.id = id;
        allData = data;
        oldObjectStructure = new ArrayList<>();
    }
    
    public List<SyncFinding> load() throws MetadataObjectNotFoundException, 
            ObjectNotFoundException, InvalidArgumentException, 
            OperationNotPermittedException, ApplicationObjectNotFoundException
            
    {
        messages.add("Staring to mapping... ");
        readData();
        messages.add("Creating the class hierarchy");
        loadClassHierarchy();
        messages.add("The class hierarchy was updated");
        messages.add("The actual data for this element will be loaded");
        readChildren(bem.getObjectChildren(className, id, -1));
        //printMapOfile();
        checkOldObject(Long.toString(id), "Rack", "000");

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
    
    private void printMapOfile(){
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
                int j = allData.get("instance").indexOf(child);
                parsedClass = parseClass(allData.get("entPhysicalClass").get(j), 
                    allData.get("entPhysicalName").get(j),
                    allData.get("entPhysicalDescr").get(j));
                System.out.println("---id: " + child + " " + allData.get("entPhysicalName").get(j) + "["+parsedClass+"]");
            }
        }
    }
    
    /**
     * Creates the hierarchy model in Kuwaiba if doesn't exist
     */
    private void loadClassHierarchy(){
        for (String parentClass : mapOfClasses.keySet()) {
            List<String> possibleChildrenToAdd = mapOfClasses.get(parentClass);
            if(possibleChildrenToAdd != null){
                try {
                    for(ClassMetadataLight possibleChild : mem.getPossibleChildren(parentClass)){
                        if(possibleChildrenToAdd.contains(possibleChild.getName())){
                            possibleChildrenToAdd.remove(possibleChild.getName());
                            messages.add(possibleChild.getName() + " is already possible children of "+ parentClass);
                        }
                    }
                    if(possibleChildrenToAdd.size() > 0){
                        mem.addPossibleChildren(parentClass, possibleChildrenToAdd.toArray(new String[possibleChildrenToAdd.size()]));
                        messages.add(possibleChildrenToAdd + " were added as possible children of "+ parentClass);
                    }
                    
                } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                    Logger.getLogger(MapFromSNMP.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    /**
     * Create into kuwaiba's objects the lines read it from the SNMP
     * @param objId 
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
    private void checkOldObject(String objId, String parentClassName, String parentId) 
            throws MetadataObjectNotFoundException, 
            ObjectNotFoundException, OperationNotPermittedException, 
            InvalidArgumentException, ApplicationObjectNotFoundException
    {
        if(Long.valueOf(objId) == id)
            objId = "0";
        List<String> childrenIds = mapOfFile.get(objId);

        if(childrenIds != null){
            for(String childId : childrenIds){
                int i = allData.get("instance").indexOf(childId);
                String objectName = allData.get("entPhysicalName").get(i);
                if(objectName.contains("GigabitEthernet"))
                    objectName = objectName.replace("GigabitEthernet" , "Gi");
              
                String vendor = ""; 
                if(!allData.get("entPhysicalMfgName").get(i).isEmpty()){
                    Long listTypeId = matchVendorNames(allData.get("entPhysicalMfgName").get(i));
                    if(listTypeId != null)
                       vendor =  Long.toString(listTypeId);
                    else{
                        JsonObject jsonObj = Json.createObjectBuilder()
                                .add("type", "listType")
                                .add("id", childId)
                                .add("name", allData.get("entPhysicalMfgName").get(i))
                                .build();
                        findings.add(new SyncFinding(SyncFinding.EVENT_NEW, 
                        "The list type: " + allData.get("entPhysicalMfgName").get(i) + " doesn't exist in kuwaiba", 
                        jsonObj.toString()));
                    }
                }
               
                //We parse the class Id from SNMP into kuwaiba's class name
                String mappedClass = parseClass(allData.get("entPhysicalClass").get(i), 
                        objectName, 
                        allData.get("entPhysicalDescr").get(i));
 
                //If is the router it is al ready created, so only have to update.
                if(mappedClass.contains("Router") || objectName.contains("Chassis")){
                    RemoteBusinessObject object = bem.getObject(id);
                    HashMap<String, List<String>> oldObjectAttributes = object.getAttributes();
                    List<String> oldAttributeValue = oldObjectAttributes.get("serialNumber");
                    
                    JsonObject jsonObj = createJson(Long.toString(id), parentId, parentClassName, mappedClass, 
                            objectName, 
                            allData.get("entPhysicalSerialNum").get(i), 
                            vendor,
                            allData.get("entPhysicalDescr").get(i), 
                            allData.get("entPhysicalModelName").get(i));
                    
                    if(oldAttributeValue != null){
                        if(oldAttributeValue.get(0).equals(allData.get("entPhysicalSerialNum").get(i)))
                            findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE, 
                                String.format("Serial number will be upated from %s  -- to --> %s", oldAttributeValue, allData.get("entPhysicalSerialNum").get(i)),
                                jsonObj.toString()));
                    }
                    
                    else
                        findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE, 
                            "Your router has no serial number, and some attributes could has old data, would you want to set this attributes?", 
                            jsonObj.toString())); 
                }
                else{
                    RemoteBusinessObjectLight alreadyCreated = isObjectAlreadyCreated(
                            objectName, 
                            mappedClass, 
                            allData.get("entPhysicalSerialNum").get(i));
                    if(alreadyCreated != null){

                        JsonObject jsonObj = createJson(Long.toString(alreadyCreated.getId()), allData.get("entPhysicalContainedIn").get(i), parentClassName, mappedClass, 
                                objectName, 
                                allData.get("entPhysicalSerialNum").get(i), 
                                vendor,
                                allData.get("entPhysicalDescr").get(i), 
                                allData.get("entPhysicalModelName").get(i));
                    
                        findings.add(new SyncFinding(SyncFinding.EVENT_UPDATE, 
                                String.format("Would you want to overwrite the atributes values in the object %s, with id: %s ", alreadyCreated.toString(), alreadyCreated.getId()),
                                jsonObj.toString()));
                    
                        oldObjectStructure.remove(alreadyCreated);
                    }
                    else{
                        JsonObject jsonObj = createJson(childId, allData.get("entPhysicalContainedIn").get(i), parentClassName, mappedClass, 
                                objectName, 
                                allData.get("entPhysicalSerialNum").get(i), 
                                vendor,
                                allData.get("entPhysicalDescr").get(i), 
                                allData.get("entPhysicalModelName").get(i));
                      
                        findings.add(new SyncFinding(SyncFinding.EVENT_NEW, 
                                "Would you want to import into kuwaiba this object from SNMP?", 
                                jsonObj.toString()));
                    }
                }
                id++;
                checkOldObject(childId, mappedClass, 
                        allData.get("entPhysicalContainedIn").get(i));
            }//end of each
        }
    }
    
    private RemoteBusinessObjectLight isObjectAlreadyCreated(String name, 
            String className, String serialNumber) 
            throws InvalidArgumentException, MetadataObjectNotFoundException, 
            ObjectNotFoundException
    {
        for (RemoteBusinessObjectLight obj : oldObjectStructure) {
            if(obj.getName().equals(name) && obj.getClassName().equals(className)){
                if(!serialNumber.isEmpty()){
                    RemoteBusinessObject completeObj = bem.getObject(obj.getId());
                    HashMap<String, List<String>> attributes = completeObj.getAttributes();
                    if(!attributes.get("serialNumber").get(0).equals(serialNumber) && attributes.get("isLoadFromRegistry").get(0).equals("true")){
                        messages.add("This element was created and is updated, would you wanna to overwrite the attributes? ");
                        return obj;
                    }
                    else if(attributes.get("serialNumber").get(0).equals(serialNumber) && attributes.get("isLoadFromRegistry").get(0).equals("false")){
                        messages.add("This element was created by hand, it has the same serial, would you want to delete and replaced?");
                        return null;
                    }
                }
            }
        }
        messages.add("This element is not created it will be created from scratch");
        return null;
    }
    
    /**
     * Reads the actual object and make a copy of the structure, from this 
     * structure the ports can be updated and moved to the new created tree 
     * in order to keep the special relationships.
     * @param parents the list of elements of a level
     * @throws MetadataObjectNotFoundException if something is wrong with the metadata
     * @throws ObjectNotFoundException if some object can not be find
     */
    private void readChildren(List<RemoteBusinessObjectLight> parents) 
            throws MetadataObjectNotFoundException, ObjectNotFoundException
    {
        for (RemoteBusinessObjectLight objectParent : parents) {
            if(!objectParent.getClassName().contains("Port") && !objectParent.getClassName().equals("ServiceInstance"))
                oldObjectStructure.add(objectParent);
//            else 
//                oldPorts.add(objectParent);
            List<RemoteBusinessObjectLight> children = bem.getObjectChildren(objectParent.getClassName(), objectParent.getId(), -1);
            if(!children.isEmpty())
                readChildren(children);
            else
                return;
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
     * Search for the parent of an element in the created map 
     * @param child child line
     * @return the parent line
     */
    private String getParent(String child){
        for(String parent : mapOfFile.keySet()){
            List<String> children = mapOfFile.get(parent);
            if(children != null && children.contains(child))
                return parent;
        }
        return null;
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
                    
                    if(patentClassParsed.isEmpty())
                        patentClassParsed = className;
                    
                    if(!childrenParsed.contains(childParsedClass))
                        childrenParsed.add(childParsedClass);
                }
                mapOfClasses.put(patentClassParsed, childrenParsed);
            }
        }
    }
    
    public void readObjectsInOrder(String parent, String space){
        space = space + " ";
        List<String> children = mapOfFile.get(parent);
        if(children != null){
            for (String child : children) {
                String[] split = child.split(",");
                objectsToCreate.add(space + split[0]+ "[" +split[5] + "]" + split[2]);
                readObjectsInOrder(child, space);
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
    
    /**
     * checks if a given string text is numeric 
     * @param s String text
     * @return true if the text is a numeric value 
     */
    private boolean isNumeric(String s) {  
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
    } 
    
    /**
     * Compare the names from the SNMP file in order to find one that match with 
     * a created list item in kuwaiba
     * @param listTypeNameToLoad
     * @return the kuwaiba's list type item
     * @throws MetadataObjectNotFoundException 
     * @throws InvalidArgumentException 
     */
    private Long matchVendorNames(String listTypeNameToLoad) throws MetadataObjectNotFoundException, InvalidArgumentException{
        List<RemoteBusinessObjectLight> listTypeItems = aem.getListTypeItems("EquipmentVendor");
        for(RemoteBusinessObjectLight createdLitType : listTypeItems){
            int matches = 0;
            int maxLength = listTypeNameToLoad.length() > createdLitType.getName().length() ? listTypeNameToLoad.length() : createdLitType.getName().length();
            listTypeNameToLoad = listTypeNameToLoad.toLowerCase().trim();
            String nameCreatedInKuwaiba = createdLitType.getName().toLowerCase().trim();
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
            if(matches == nameCreatedInKuwaiba.length())
                return createdLitType.getId();
        }
        return null;
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
    
    private JsonObject createJson(String childId, String parentId, 
            String parentClassName, String mappedClass, String objectName, 
            String serialNumber, String vendor, String description, String modelName){
        JsonObject jsonObj = Json.createObjectBuilder()
            .add("type", "object")
            .add("id", childId)   
            .add("parentId", parentId) 
            .add("parentClassName", parentClassName) 
            .add("className", mappedClass)
            .add("attributes", Json.createObjectBuilder()
                .add("name", objectName)
                .add("serialNumber", serialNumber)
                .add("vendor", vendor)   
                .add("description", description)
                .add("modelName", modelName))
            .build();
        return jsonObj;
    }
}
