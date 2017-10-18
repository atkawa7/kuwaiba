/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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
package com.neotropic.kuwaiba.sync.snmp.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * maps a given SNMP data into Kuwaiba's data
 * @author Adrian Martinez Molina <adrian.martinez@neotropic.co>
 */
public class EtlSNMP  implements Serializable{

    /**
     * The initial parent Id for the root
     */
    private static final String INITAL_ID = "0";
    public static final String ROOT = "DummyRoot";
    //this two are for debuging purposes 
    private HashMap<String, List<String>> mapOfFileOnlyId = new HashMap<>();
    private HashMap<String, List<String>> mapOfParceResume = new HashMap<>();
    /**
     * a map of the file to create the objects
     */
    private HashMap<String, List<String>> mapOfFile = new HashMap<>();
    /**
     * a map of the file to create the classes in the containment hierarchy
     */
    private HashMap<String, List<String>> mapOfClasses = new HashMap<>();
    private List<String> objectsToCreate = new ArrayList<>();
    /**
     * the file loaded into the memory
     */
    private List<String> allData;

    public List<String> getObjects() {
        return objectsToCreate;
    }

    public HashMap<String, List<String>> getMapOfFile() {
        return mapOfFile;
    }
    
    public HashMap<String, List<String>> getMapOfClasses() {
        return mapOfClasses;
    }
    
    public void loadFile(String path){
        allData = new ArrayList<>();
        try (BufferedReader input = new BufferedReader(new FileReader(path))) {
            String line;
            //we load the file in memory
            while ((line = input.readLine()) != null){
                //to avoid the header and footer
                String[] splitLine = line.split(",");
                if(splitLine.length >= 19){
                    if(isNumeric(splitLine[0])){
                        allData.add(line);
                    }
                }
            }
        } catch (IOException ex) {
            ex.getMessage();
        }    
    }
    
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
        
        removeUnusedClasses();
        removeUnusedChildren();
        removeChildrenless();
        createMapOfClasses();
    }
    
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
     * Translate the plain file into a map with parents and his children 
     * @param evaluatedLine line to evaluate if is child of the parentId
     * @param tmpParentId allegated parent
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
     * Removes the unused classes such sensor, could be used  in subslots
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
    
    private void removeUnusedChildren(){
        List<String> unusedChildren = new ArrayList<>();
        for(String key : mapOfFile.keySet()){
            List<String> children = mapOfFile.get(key);
            if(children != null){
                for(String child : children) {
                    if(!isAnUsedClass(child)){
                        unusedChildren.add(child);
                    }
                }
            }
            mapOfFile.get(key).removeAll(unusedChildren);
        }
    }
    
    /**
     * removes keys without children
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
     * For debuging purposes translate the line in only ID and class name
     *
    private void translate(){
        mapOfFileOnlyId = new HashMap<>();
        mapOfParceResume = new HashMap<>();
        for(String key : mapOfFile.keySet()){
            List<String> children = mapOfFile.get(key);
            String[] splitLine;
            String id; //0 id
            String className; //05 class
            String name = "";
            String parsedClassName;
            String desc = "";
            if(key.equals(ROOT)){
                id = "0";
                className= ROOT;
            }
            else{
                splitLine = key.split(","); 
                id = splitLine[0]; //0 id
                className = splitLine[5]; //05 class
                name = splitLine[7]; //07 name
                desc = splitLine[2];
            }
            List<String> childrenIdOnly = mapOfFileOnlyId.get(id+"-"+className);
            
            if(childrenIdOnly == null)
                childrenIdOnly = new ArrayList<>();
            
            for (String c : children) {
                String[] splitLineC = c.split(",");
                String idC = splitLineC[0]; //0 id
                String classNameC = splitLineC[5]; //05 class
                childrenIdOnly.add(idC + "-" + classNameC);
            }
            mapOfFileOnlyId.put(id+"-"+className, childrenIdOnly);
            
            parsedClassName = parseClass(className, name, desc);
            List<String> childrenIdOnlyC = mapOfParceResume.get(id + "-" + parsedClassName);
            
             if(childrenIdOnlyC == null)
                childrenIdOnlyC = new ArrayList<>();
            
            for (String c : children) {
                String[] splitLineC = c.split(",");
                String idC = splitLineC[0]; //0 id
                String nameC = splitLineC[7];
                String classNameC = splitLineC[5]; //05 class
                String descC = splitLineC[2];
                childrenIdOnlyC.add(idC + "-" +  parseClass(classNameC, nameC, descC));
                
            }
            mapOfParceResume.put(id+"-"+className, childrenIdOnlyC);
        }
        
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
   
    /**
     * Maps the SNMP file classes into kuwaiba's classes
     * @param className the given class name
     * @param name name of the elment
     * @param desc description of the element
     * @return equivalent kuwaiba's class
     */        
    public String parseClass(String className, String name, String desc){
        if(className.contains("chassis"))
            return "Router";
        else if(className.contains("port")){
            if(name.contains("usb"))
                return "USBPort";
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
        else if(className.contains("powerSupply") && name.contains("Power"))
            return "PowerPort";
        
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
     * retunrs if the class is used or not
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
        }
        else{
            className = ROOT;
            name = ROOT;
        }
            
        if(className.contains("chassis") || className.contains("port")) 
            return true;
        else if(className.contains("powerSupply"))
            return true;
        else if(className.contains("module"))
            return true;
        else if(className.contains("container")){
            return !name.contains("sensor");
        }
        else
            return false;
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
    
    private boolean isNumeric(String s) {  
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
    }  
}
