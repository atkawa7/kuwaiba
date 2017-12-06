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
package org.kuwaiba.sync;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.kuwaiba.services.persistence.util.Constants;

/**
 * Loads data from a SNMP file to replace/update an existing element in 
 * the inventory
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class LoadDataFromSNMP {
    
    /**
     * The class name of the object
     */
    private String className;
    private long id;
    private String fileName;
    
    List<RemoteBusinessObjectLight> toRemove;
    List<RemoteBusinessObjectLight> oldPorts;
    HashMap<Long, List<Long>> newPorts;
    HashMap<String, String> newAttributes;
    List<RemoteBusinessObjectLight> portsToBeDeleted;
    List<RemoteBusinessObjectLight> newPortsWithNoMatch;
    
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
     * The file loaded into the memory
     */
    List<String> allData;
    
    String INITAL_ID = "0";
    String ROOT = "Rack";
    String path="../kuwaiba/upload-files/";
    
    List<String> messages = new ArrayList<>();
    
    private BusinessEntityManager bem;
    private ApplicationEntityManager aem;
    private MetadataEntityManager mem;

    public LoadDataFromSNMP(String className, long id, String fileName) {
        connect();
        this.className = className;
        this.id = id;
        this.fileName = fileName;
        
        toRemove = new ArrayList<>();
        oldPorts = new ArrayList<>();
        newPorts = new HashMap<>();
        portsToBeDeleted = new ArrayList<>();
        newAttributes = new HashMap<>();
    }
    
    public String load() throws MetadataObjectNotFoundException, 
            ObjectNotFoundException, InvalidArgumentException, 
            OperationNotPermittedException, ApplicationObjectNotFoundException
            
    {
        messages.add("Loading file into memory");
        loadFile(fileName);
        messages.add("Creating the class hierarchy");
        loadClassHierarchy();
        messages.add("The class hierarchy was updated");
        messages.add("The actual data for this element will be loaded");
        readChildren(bem.getObjectChildren(className, id, -1));
        createObjects("Rack", "Chassis", "", "", id, true);
        migratePorts();
        deleteOldStructure();
        
        String x="";
        for(String msg: messages)
            x+= msg +"\n";
        
        return x;
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
                    Logger.getLogger(LoadDataFromSNMP.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    /**
     * Create into kuwaiba's objects the lines read it from the SNMP
     * @param initOfMap 
     * @param parentClassName
     * @param parentName
     * @param parentDesc
     * @param parentId
     * @param isFirst
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException
     * @throws org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException
     * @throws org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException
     * @throws org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException
      */
    private void createObjects(String initOfMap, String parentClassName, 
            String parentName, String parentDesc, long parentId, 
            boolean isFirst) throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException, ApplicationObjectNotFoundException
    {
        List<String> children = mapOfFile.get(initOfMap);
        boolean isVendorEquipmentCreated = false;
        if(children != null){
            for(String child : children){
                String[] split = child.split(",");
                String objectName = split[7];
                if(objectName.contains("GigabitEthernet"))
                    objectName = objectName.replace("GigabitEthernet" , "Gi");
                //We set the attributes
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(Constants.PROPERTY_NAME, objectName);
                attributes.put(Constants.PROPERTY_DESCRIPTION, split[2]);
                        
                if (!split[11].isEmpty())
                    attributes.put("serialNumber", split[11]);

//               if(!split[9].isEmpty())
//                  attributes.put('firmWare', split[9]);

                if(!split[13].isEmpty())    
                    attributes.put("modelName", split[13]);

                if(!split[12].isEmpty()){
                    Long listTypeId = matchNames(split[12]);
                    if(listTypeId != null){
                        attributes.put("vendor", Long.toString(listTypeId));
                        isVendorEquipmentCreated = true;
                    }
                    
                }
               
                String mappedClass = parseClass(split[5], split[7], split[2]);
                String mappedParentClass = parentClassName;
                if(!isFirst)
                    mappedParentClass = parseClass(parentClassName, parentName, parentDesc);

                long createdObjectId = 0;
                if(mappedClass.contains("Router") || objectName.contains("Chassis")){
                    //move attributes and keep the old element
                    bem.updateObject(mappedClass, parentId, attributes);
                    createdObjectId = parentId;
                }
                else{
                    RemoteBusinessObjectLight alreadyCreated = isAlreadyCreated(attributes.get(Constants.PROPERTY_NAME), mappedClass, split[11]);
                    if(alreadyCreated != null){
                        bem.updateObject(alreadyCreated.getClassName(), alreadyCreated.getId(), attributes);
                        toRemove.remove(alreadyCreated);
                        messages.add("the object: " + alreadyCreated.toString() + "it was not created, but its attributes were updated");
                    }
                    else{
//-> pregunta aquí si desea crear nuevo elemento.                        
                        createdObjectId = bem.createObject(mappedClass, mappedParentClass, parentId, attributes, -1);
                        messages.add("The object: " + attributes.get("name") + " [" + mappedClass +"] was not created");
                        if(mappedClass.contains("Port")){
                            List<Long> listOfCreatedPorts = newPorts.get(parentId);
                            if(listOfCreatedPorts == null)
                                listOfCreatedPorts = new ArrayList();
                            listOfCreatedPorts.add(createdObjectId);
                            newPorts.put(parentId, listOfCreatedPorts);   
                        }
                        if(!isVendorEquipmentCreated && !split[12].isEmpty())
                          messages.add("The list type: "+ split[12] + "doesn't exist in kuwaiba, so it could not be loaded, please create this list type manually and add it to the object with id: " + createdObjectId);
                    }
                }
                createObjects(child, split[5], split[7], split[2], createdObjectId, false);
            }//end of each
        }
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
            //We copy the new attributes intoRemoteBusinessObjectLight the new port, to keep the relationships
            Long newParentId = getNewAttributes(oldPort);
            if(newParentId != null){
                messages.add("A match with the created data was found, would you want to overwrite the attributes?");
                bem.updateObject(oldPort.getClassName(), oldPort.getId(), newAttributes);
                HashMap<String, Long[]> objectsToMove = new HashMap<>();
                Long[] ids = {oldPort.getId()} ;
                objectsToMove.put(oldPort.getClassName(), ids);
                RemoteBusinessObject parent = bem.getObject(newParentId);
                //move the old port into the new location
                bem.moveObjects(parent.getClassName(), newParentId, new HashMap(objectsToMove));
            }
            
        }//end for
       
        messages.add("The ports were migrated");
        for(RemoteBusinessObjectLight p : portsToBeDeleted){
            messages.add("trying to delete port:" + p.getName() +" - id: " + p.getId() + " after migration");
            bem.deleteObject(p.getClassName(), p.getId(), false);
        }
    }

    private void deleteOldStructure() throws ObjectNotFoundException, 
            MetadataObjectNotFoundException, OperationNotPermittedException
    {
        messages.add("Deleteing old estructure!");
        for(RemoteBusinessObjectLight objToDelete : toRemove){ 
            if(objToDelete.getClassName().equals("Slot") || objToDelete.getClassName().equals("Transceiver")){
                messages.add("trying to delete slot " + objToDelete.toString() +" and all its children id: " + objToDelete.getId() + " after migration");
                bem.deleteObject(objToDelete.getClassName(), objToDelete.getId(), false);
            }
        }
        messages.add("The old data was removed");
    }
    
    private Long getNewAttributes(RemoteBusinessObjectLight oldPort) 
            throws InvalidArgumentException, ObjectNotFoundException, 
            MetadataObjectNotFoundException
    {
        for (long parentOfNewPort : newPorts.keySet()) {
            List<Long> listOfCreatedPorts = newPorts.get(parentOfNewPort);
            for (long p : listOfCreatedPorts) {
                RemoteBusinessObject tempNewPort = bem.getObject(p);
                if(compareNames(oldPort.getName(), oldPort.getClassName(), tempNewPort.getName(), tempNewPort.getClassName()))
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
    
    private RemoteBusinessObjectLight isAlreadyCreated(String newObjName, 
            String newObjClassName, String serialNumber) 
            throws InvalidArgumentException, MetadataObjectNotFoundException, 
            ObjectNotFoundException
    {
        //0 not created
        //1 created no changes
        //2 loaded form registry but diferent serial
        //3 created by hand but same serial
        boolean loadedFromRegistry = false;
        for (RemoteBusinessObjectLight obj : toRemove) {
            if(obj.getName().equals(newObjName) && obj.getClassName().equals(newObjClassName)){
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
     * Reads the actual object and make a copy of the old created ports, this 
     * ports will be updated and moved to the new created tree in order to keep 
     * the special relationships.
     * @param parents the list of elements of a level
     * @throws MetadataObjectNotFoundException if something is wrong with the metadata
     * @throws ObjectNotFoundException if some object can not be find
     */
    private void readChildren(List<RemoteBusinessObjectLight> parents) 
            throws MetadataObjectNotFoundException, ObjectNotFoundException
    {
        for (RemoteBusinessObjectLight objectParent : parents) {
            if(!objectParent.getClassName().contains("Port") && !objectParent.getClassName().equals("ServiceInstance"))
                toRemove.add(objectParent);
            else 
                oldPorts.add(objectParent);
            List<RemoteBusinessObjectLight> children = bem.getObjectChildren(objectParent.getClassName(), objectParent.getId(), -1);
            if(!children.isEmpty())
                readChildren(children);
            else
                return;
        }
    }
    
    /**
     * Loads the file into memory
     */
    private void loadFile(String fileName){
        allData = new ArrayList<>();
        try (BufferedReader input = new BufferedReader(new FileReader(path + fileName))) {
            String line;
            while ((line = input.readLine()) != null){
                //to avoid the header and footer
                String[] splitLine = line.split(",");
                if(splitLine.length >= 19){
                    if(isNumeric(splitLine[0]))
                        allData.add(line);
                }
            }
            if(allData.isEmpty())
                messages.add("The file was not loaded in memory, check the csv separator, should be a \",\"");
            else{
                messages.add( allData.size() + " lines loaded in memory");
                readData();
            }
        } catch (IOException ex) {
            ex.getMessage();
        }   
    }
    
    /**
     * Reads the data loaded into memory
     */
    public void readData(){
        String lineOfParent = null;
        //we look for the root
        for (String line : allData) {
            String[] splitLine = line.split(",");
            String parentId = splitLine[4]; //04 parent id
            if(parentId.equals(INITAL_ID)){
                lineOfParent = line;
                break;
            }
        }
        if(lineOfParent != null) //no parend was found
            createTreeFromFile(lineOfParent, INITAL_ID);
        else
            messages.add("No Chassis was found");
    
        removeUnusedClasses();
        removeUnusedChildren();
        removeChildrenless();
        createMapOfClasses();
    }
    
    /**
     * Translate the plain file into a map with parents and his children 
     * @param evaluatedLine line to evaluate if is child of the parentId
     * @param tmpParentId alleged parent
     */
    private void createTreeFromFile(String evaluatedLine, String tmpParentId){
        for (String line : allData) {
            String[] splitLine = line.split(",");
            String id = splitLine[0];
            String parentId = splitLine[4];
            
            if(parentId.equals(tmpParentId)){
                if(line.equals(evaluatedLine))
                    saveInTreeMap(ROOT, line);
                else
                    saveInTreeMap(evaluatedLine, line);
                createTreeFromFile(line, id);
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
    private String searchParent(String child){
        for(String parent : mapOfFile.keySet()){
            List<String> children = mapOfFile.get(parent);
            if(children != null){
                if(children.contains(child))
                    return parent;
            }
        }
        return null;
    }
    
    /**
     * Removes the unused classes such sensors
     */
    private void removeUnusedClasses(){
        List<String> toRemove = new ArrayList<>();
        for(String key : mapOfFile.keySet()){
            mapOfFile.get(key);
            if(!isAnUsedClass(key)){
                String searchedParent = searchParent(key);
                if(searchedParent != null){ //if has parent
                    //look for the children of the unused class
                    List<String> childrenToMove = mapOfFile.get(key);
                    //remove unsued class form the list of the parent's children
                    List<String> siblins = mapOfFile.get(searchedParent);
                    siblins.remove(key);
                    //add the children to the parent of the unused class
                    siblins.addAll(childrenToMove);
                    mapOfFile.put(searchedParent, siblins);
                    //save classes to be remove
                    toRemove.add(key);
                    mapOfFile.put(key, null);
                }    
            }
        }//end for
        for (String c : toRemove) //remove the unused classes from the final Hash Map
            mapOfFile.remove(c);
    }
    
    /**
     * Removes the children of class that wont not used
     */
    private void removeUnusedChildren(){
        List<String> unusedChildren = new ArrayList<>();
        for(String key : mapOfFile.keySet()){
            List<String> children = mapOfFile.get(key);
            if(children != null){
                for(String child : children) {
                    if(!isAnUsedClass(child))
                        unusedChildren.add(child);
                }
            }
            mapOfFile.get(key).removeAll(unusedChildren);
        }
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
    * Creates the has map of classes to create the hierarchy containmet
    */
    private void createMapOfClasses(){
        mapOfClasses = new HashMap<>();
        
        for (String key : mapOfFile.keySet()) {
            String newKey = ROOT;
            if(!key.equals(ROOT)){
                String[] split = key.split(",");
                newKey = parseClass(split[5], split[7], split[2]);
            }
            List<String> childrenWithoutParse = mapOfFile.get(key);
            List<String> childrenParsed = mapOfClasses.get(newKey);
            if(childrenParsed == null)
                childrenParsed = new ArrayList<>();
            
            for (String childWithoutParse : childrenWithoutParse) {
                String[] childSplit = childWithoutParse.split(",");
                String classParsed = parseClass(childSplit[5], childSplit[7], childSplit[2]);
                if(!childrenParsed.contains(classParsed))
                    childrenParsed.add(classParsed);
            }
            mapOfClasses.put(newKey, childrenParsed);
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
     * @param className the given class name
     * @param name name of the elment
     * @param desc description of the element
     * @return equivalent kuwaiba's class
     */        
    public String parseClass(String className, String name, String desc){
        if(className.contains("chassis"))
            return "MPLSRouter";
        else if(className.contains("port")){
            if(name.contains("usb") || desc.contains("usb"))
                return "USBPort";
            if(desc.contains("Ethernet"))
                return "ElectricalPort";
            else
                return "OpticalPort";
        }
            
        else if(className.contains("container")){
            if(name.contains("transceiver"))
                return "Slot";//Slot-T";
            else if(name.contains("Power"))
                return "Slot";
            else if(name.contains("SPA"))
                return "Solt";
            return "Slot";
        }
        else if(className.contains("powerSupply") && name.contains("Power") && !name.contains("Module"))
            return "PowerPort";
        else if(className.contains("powerSupply") && name.contains("Module"))
            return "HybridBoard";    
        
        else if(className.contains("module")){
            //if(name.contains("SPA")) 
              //  return "SPAModule";
            if(name.contains("transceiver") || desc.contains("transceiver"))
                return "Transceiver";
            return "IPBoard";
        }
        else
            return null;
    }
    
    /**
     * returns if the class is used or not
     * @param line the line of the SNMP to extract the className and the name of
     * the element
     * @return false if is a sensor, true in the most of the cases for now
     */
    private boolean isAnUsedClass(String line){
        String[] splitLine;
        String className; //05 class
        String name; //07 name
        if(!line.equals(ROOT)){
            splitLine = line.split(",");
            className = splitLine[5]; //05 class
            name = splitLine[7]; //07 name
            
            if(className.contains("chassis") || className.contains("port") || className.contains("powerSupply") || className.contains("module")) 
                return true;
            else if(className.contains("container"))
                return !name.contains("sensor");
        }
        
        return false;
    }
    
    /**
     * checks if a given string text is numeric 
     * @param s String text
     * @return true if the text is a numeric value 
     */
    private boolean isNumeric(String s) {  
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
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
    
    /**
     * Compare the names from the SNMP file in order to find one that match with 
     * a created list item in kuwaiba
     * @param nameToLoad
     * @return the kuwaiba's list type item
     * @throws MetadataObjectNotFoundException 
     * @throws InvalidArgumentException 
     */
    public Long matchNames(String nameToLoad) throws MetadataObjectNotFoundException, InvalidArgumentException{
        List<RemoteBusinessObjectLight> listTypeItems = aem.getListTypeItems("EquipmentVendor");
        for(RemoteBusinessObjectLight createdLitType : listTypeItems){
            int matches = 0;
            int maxLength = nameToLoad.length() > createdLitType.getName().length() ? nameToLoad.length() : createdLitType.getName().length();
            nameToLoad = nameToLoad.toLowerCase().trim();
            String nameCreatedInKuwaiba = createdLitType.getName().toLowerCase().trim();
            for (int i=1; i< maxLength; i++){
                String a, b;
                if(nameToLoad.length() < i )
                    break;
                else
                    a = nameToLoad.substring(i-1, i);
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
}
