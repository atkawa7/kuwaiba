/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kuwaiba.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.WrongMappingException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.beans.WebserviceBean;

/**
 * Manages the bulk load for list types an object from CSV files
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public final class LoadDataFromFile{
    /**
     * Date format for sync 
     */
    private static final String DATE_HOUR_FORMAT = "-yyyy-MM-dd'T'HH-mm-ss";
    /**
     * Path to upload file
     */
    private static final String PATH_DATA_LOAD_FILES = "../kuwaiba/upload-files/";
    /**
     * Path to log file after a bulk upload
     */
    private static final String PATH_DATA_LOAD_LOGS = "../kuwaiba/logs/";
    /**
     * Minimum fields required in a csv file to load objects
     */
    private static final int MINIMUM_CLASSTYPE_FIELDS = 4;
    /**
     * if the parent is the dummy root
     */
    private static final String ROOT = "root";
    /**
     * number of rows before commit
     */
    private int commitSize;
    private long userId;
    private File uploadFile;
    private String IPAddress;
    private String sessionId;
    private int dataType;
    private byte [] uploadData;
    
    private BusinessEntityManager bem;
    private ApplicationEntityManager aem;
    private MetadataEntityManager mem;
    
    private List<RemoteBusinessObject> data;
    
    public LoadDataFromFile(byte [] uploadData,  int commitSize, int dataType, String IPAddress, String sessionId) {
        connect();
        this.commitSize = commitSize;
        this.IPAddress = IPAddress;
        this.dataType = dataType;
        this.sessionId = sessionId;
        this.uploadData = uploadData;
    }

    public File getUploadFile() {
        return uploadFile;
    }
    
    public String uploadFile() throws ApplicationObjectNotFoundException, 
            NotAuthorizedException, RemoteException, MetadataObjectNotFoundException, 
            InvalidArgumentException, ObjectNotFoundException, OperationNotPermittedException, WrongMappingException
    {
        this.userId = aem.getUserInSession(sessionId).getId();
        
        DateFormat dateFormat = new SimpleDateFormat(DATE_HOUR_FORMAT);
        FileOutputStream fileOuputStream = null;
        String fileName = Long.toString(userId) + dateFormat.format(new Date());

        try {
            new File(PATH_DATA_LOAD_FILES).mkdirs();
            new File(PATH_DATA_LOAD_LOGS).mkdirs();
            uploadFile = new File(PATH_DATA_LOAD_FILES + fileName);
            
            if(!uploadFile.exists())
                uploadFile.createNewFile();
            fileOuputStream = new FileOutputStream(uploadFile, false);
            fileOuputStream.write(uploadData);
            fileOuputStream.flush();
	    fileOuputStream.close();
         
        } catch (IOException e) {
            e.getMessage();
        } finally {
            try {
                if (fileOuputStream != null)
                    fileOuputStream.close();
            } catch (IOException e) {
                e.getMessage();
            }
        }
        if(dataType == 1)
            loadListTypes();
        else if (dataType == 2)
            loadObjects();
        
        return uploadFile.getName();
    }
    
    private void loadObjects(){
        boolean hasErrors = false;
        String errorsMsgs = "";
        
        try {
            BufferedReader input = new BufferedReader(new FileReader(uploadFile));
            String line;
            int currentLine = 0;

            /**
             * The line must have the following format (fields surrounded by brackets are optional and of variable length):
             * OBJECT_CLASS_NAME~t~CLASS_OF_THE_PARENT_OBJECT~t~PARENT_OBJECT_NAME_DISTINGUISHABLE_ATTRIBUTE~c~VALUE_OF_THE_PARENT_OBJECT_DISTINGUISHABLE_ATTRIBUTE~t~ATTR1_NAME~c~ATTR1_VALUE~t~ATTR2_NAME~c~ATTR2_VALUE~t~...
             **/
            while ((line = input.readLine()) != null) {
                currentLine ++;
                if (line.startsWith("#") || line.trim().isEmpty()) { //Comments are ignored
                    errorsMsgs += String.format("INFO\t%s\tComment or empty line detected, ignored.\n", currentLine);
                    hasErrors = true;
                    continue;
                }
                
                String[] splitLine = line.split("~t~");
                //Not enough fields in the line
                if (splitLine.length < MINIMUM_CLASSTYPE_FIELDS) {
                    errorsMsgs += String.format("ERROR\t%s\tThe line has %s fields but %s were expected.\n", currentLine, splitLine.length, MINIMUM_CLASSTYPE_FIELDS);
                    hasErrors = true;
                    continue;
                }
                try{
                    String className = splitLine[0];
                    String parentClass = splitLine[1];
                    String[] parentFilter = splitLine[2].split("~c~");
                    
                    if (parentFilter.length != 2) {
                        errorsMsgs += String.format("ERROR\t%s\tThe parent filter definition has an unexpected number of fields (%s).\n", currentLine, parentFilter.length);
                        continue;
                    }
                    
                    HashMap<String, String> attributes = new HashMap<>();

                    for(int i = 3; i < splitLine.length; i ++){
                        String[] attributeDefinition = splitLine[i].split("~c~");
                        if (attributeDefinition.length < 2) {
                            errorsMsgs += String.format("ERROR\t%s\tAn attribute definition must have at least two components: %s\n.", currentLine, splitLine[i]);
                            hasErrors = true;
                            continue;
                        }

                        attributes.put(attributeDefinition[0], attributeDefinition[1]);
                    }

                    long template = 0; //TODO Support for templates

                    if (parentClass.equals(ROOT)){ //The parent is the navigation tree root
                        bem.createObject(className, null, -1, 
                                attributes, 
                                template);
                    }
                    else
                         bem.createObject(className, 
                                parentClass, 
                                parentFilter[0] + ":"  + parentFilter[1], 
                                attributes, 
                                template);
                }catch(Exception ex){
                    errorsMsgs += String.format("ERROR\t%s\tUnexpected error: %s.\n", currentLine, ex.getMessage());
                    hasErrors = true;
                }
                
            }// end while read line
            if(hasErrors)
                save(uploadFile.getName(), errorsMsgs);
            else
                save(uploadFile.getName(), "All lines processed successfully.");
            
            input.close();
        } catch (IOException ex) {
            ex.getMessage();
        }
    }
       
    private void loadListTypes() {
        boolean hasErrors = false;
        String errorsMsgs = "";
        data = new ArrayList<>();
        
        try {
            BufferedReader input = new BufferedReader(new FileReader(uploadFile));
            int currentLine = 0;
            String line;
            
            /**
             * The line must have the following format (fields surrounded by brackets are optional and of variable length):
             * LIST_TYPE_CLASS_NAME~t~ATTR1_NAME~c~ATTR1_VALUE~t~ATTR2_NAME~c~ATTR2_VALUE~t~...
             **/
            while ((line = input.readLine()) != null) {
                currentLine++;
                if (line.startsWith("#")) { //Comments are ignored
                    errorsMsgs += String.format("INFO\t%s\tComment line detected, ignored.\n", currentLine);
                    hasErrors = true;
                    continue;
                }
                
                String[] splitLine = line.split("~t~");
                
                //Not enough fields in the line, in this case, the line is empty
                if (splitLine.length == 0) {
                    errorsMsgs += String.format("INFO\t%s\tEmpty line detected, ignored.\n", currentLine);
                    hasErrors = true;
                    continue;
                }
                
                String className = splitLine[0];
                try{
                    
                    HashMap<String, String> attributes = new HashMap<>();
                    for(int i = 1; i < splitLine.length; i++) {
                        String[] attributeDefinitionParts = splitLine[i].split("~c~");
                        if (attributeDefinitionParts.length < 2) {
                            errorsMsgs += String.format("ERROR\t%s\tIncorrect attribute definition. At least two fields were expected.\n", currentLine);
                            hasErrors = true;
                            continue;
                        }
                        
                        attributes.put(attributeDefinitionParts[0], attributeDefinitionParts[1]);
                    }
                    
                    long oid = aem.createListTypeItem(className, "", "");
                    bem.updateObject(className, oid, attributes);
                }catch(Exception ex){
                    errorsMsgs += String.format("ERROR\t%s\tUnexpected error: %s.\n", currentLine, ex.getMessage());
                    hasErrors = true;
                }
            }// end while read line
            if(hasErrors)
                save(uploadFile.getName(), errorsMsgs);
            else
                save(uploadFile.getName(), "All lines processed successfully.");
            input.close();
        } catch (IOException ex) {
            Logger.getLogger(LoadDataFromFile.class.getName()).log(Level.SEVERE, "Check atribute names.", ex);
        } 
    }
    
    public void commitLisTypes(int currentFileLine) throws IOException{
        
    }
    
    public void save(String fileName, String text) throws IOException{
        try (FileWriter aWriter = new FileWriter(PATH_DATA_LOAD_LOGS + fileName, false)) {
            aWriter.write(text);
            aWriter.flush();
        }
    }

    protected void connect(){
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
    
    public byte[] downloadLog(String fileName) throws IOException{
        File file = new File(PATH_DATA_LOAD_LOGS + fileName);
        return getByteArrayFromFile(file);
    }
    
    /**
     * Gets the bytes from a file
     * @param f File object
     * @param format format to be read
     * @return The byte array
     */
    public static byte[] getByteArrayFromFile(File f) throws IOException{
        InputStream is = new FileInputStream(f);
        long length = f.length();
        byte[] bytes;
        if (length < Integer.MAX_VALUE) { //checks if the file is too big
            bytes = new byte[(int)length];
            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                   && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }
            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + f.getName());
            }
        }else{
            throw new IOException("File too big " + f.getName());
        }
        is.close();
        return bytes;
    }

    private boolean isPrimitive(String type){
        return type.equals("String") || type.equals("Long")
                || type.equals("Date") || type.equals("Float")
                || type.equals("Integer") || type.equals("Boolean");
    }
}