/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>.
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
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.WrongMappingException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.psremoteinterfaces.ApplicationEntityManagerRemote;
import org.kuwaiba.psremoteinterfaces.BusinessEntityManagerRemote;
import org.kuwaiba.psremoteinterfaces.MetadataEntityManagerRemote;

/**
 * Manages the load, update and sync services
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public final class LoadDataFromFile implements Runnable{
    /**
     * Date format for sync 
     */
    private static final String DATE_HOUR_FORMAT = "-yyyy-MM-dd'T'HH-mm-ss";
    /**
     * Path to upload file
     */
    private static final String PATH_DATA_LOAD_FILES = "../kuwaiba/upload-files/";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_PARENT_NAME = "parentName";
    private static final String ATTRIBUTE_PARENT_CLASS = "parentClass";
    private static final String ATTRIBUTE_TEMPLATE = "template";
    /**
     * Path to log file after a bulkupload
     */
    private static final String PATH_DATA_LOAD_LOGS = "../kuwaiba/logs/";
    /**
     * Path to errors file after bulkupload
     */
    private static final String PATH_DATA_LOAD_ERRORS = "../kuwaiba/errors/";
    /**
     * 
     */
    private static final int MINIMUN_LISTTYPE_FIELDS = 2;
    /**
     * 
     */
    private static final int MINIMUN_CLASSTYPE_FIELDS = 4;
    /**
     * 
     */
    private static final String NONE = "none";
    /**
     * if the parent is the dummy root
     */
    private static final String ROOT = "root";

    private static final String VALUE_OPERATOR = ":";
    
    
    private int commitSize;
    private long userId;
    private File uploadFile;
    private String IPAddress;
    private String sessionId;
    private int dataType;
    private byte [] uploadData;
    
    private HashMap<String, Long> currentCreatedObjects;
    private BusinessEntityManagerRemote bem;
    private ApplicationEntityManagerRemote aem;
    private MetadataEntityManagerRemote mem;
    
    private List<RemoteBusinessObject> data;
    
    public LoadDataFromFile(byte [] uploadData,  int commitSize, int dataType, String IPAddress, String sessionId) {
        connect();
        this.commitSize = commitSize;
        this.IPAddress = IPAddress;
        this.dataType = dataType;
        this.sessionId = sessionId;
        this.uploadData = uploadData;
        data = new ArrayList<RemoteBusinessObject>();
        currentCreatedObjects = new HashMap<String, Long>();
    }
    
    public void uploadFile() throws ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException, MetadataObjectNotFoundException, InvalidArgumentException, ObjectNotFoundException, OperationNotPermittedException, WrongMappingException{
        this.userId = aem.getUserInSession(IPAddress, sessionId).getId();
        
        DateFormat dateFormat = new SimpleDateFormat(DATE_HOUR_FORMAT);
        FileOutputStream fileOuputStream = null;
        String fileName = Long.toString(userId) + dateFormat.format(new Date());

        try {
            new File(PATH_DATA_LOAD_FILES + userId).mkdirs();
            uploadFile = new File(PATH_DATA_LOAD_FILES + userId + fileName);
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
    }
    
    private void loadObjects(){
        boolean errors = false;
        String errorsMsgs = "";
        String errorLines = "";
        HashMap<String, String> insetertedAttributes = new HashMap<String, String>();
        
        try {
            BufferedReader input = new BufferedReader(new FileReader(uploadFile));
            String line;
            int currentFileLine = 0;
            int commitCounter = 0;

            while ((line = input.readLine()) != null) {
                currentFileLine++;
                errors = false;
                HashMap<String, List<String>> attributes = new HashMap<String, List<String>>();
                String[] splitLine = line.split(";");
                //not enough fields in the line
                if (splitLine.length < MINIMUN_LISTTYPE_FIELDS) {
                    errorsMsgs += java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_IN_LINE")+currentFileLine+
                                 java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_LISTTYPE_NOT_ENOUGH_FIELDS")+"\n";
                    errors = true;
                }
                else{
                    try{
                        String className = splitLine[0];
                        //TODO implement templates support
                        for(int i = 2; i < splitLine.length; i+=2){
                            List<String> attirbuteValues = new  ArrayList<String>();
                            String attributeType = "";
                            if(insetertedAttributes.containsKey(splitLine[i]))
                                attributeType = insetertedAttributes.get(splitLine[i]);
                            else{    
                                AttributeMetadata attribute = mem.getAttribute(className, splitLine[i-1]);
                                insetertedAttributes.put(splitLine[i], attribute.getType());
                                attributeType = attribute.getType();
                            }
                            if (!isPrimitive(attributeType))
                               attirbuteValues.add(Long.toString(aem.getListTypeItem(splitLine[i], IPAddress, sessionId).getId()));
                            else
                                attirbuteValues.add(splitLine[i]);
                            attributes.put(splitLine[i-1],attirbuteValues);
                        }
                        data.add(new RemoteBusinessObject(-1, className, attributes));
                        commitCounter++;
                    }catch(Exception ex){
                        errorsMsgs += ex.getMessage();
                        //theres something wrong whit the line x please check your list types uppercase
                        // attribute name 
                    }
                }
                if(commitSize == commitCounter && !errors){
                    commitObjects();
                    commitCounter = 0;
                    data = new ArrayList<RemoteBusinessObject>();
                }
            }// end while read line
            input.close();
        } catch (IOException ex) {
            ex.getMessage();
        }
    }
    
    public void commitObjects(){
        try{
            long oid;
            for(RemoteBusinessObject object : data){
                String parentClassName = object.getAttributes().remove(ATTRIBUTE_PARENT_CLASS).get(0);
                String parentName = object.getAttributes().remove(ATTRIBUTE_PARENT_NAME).get(0);
                long template = Long.parseLong(object.getAttributes().remove(ATTRIBUTE_TEMPLATE).get(0));
                
                if (parentClassName.equals(ROOT) && parentName.equals(ROOT)) 
                    oid = bem.createObject(object.getClassName(), null, -1, 
                            object.getAttributes(), 
                            template, IPAddress, sessionId);
                else
                    oid = bem.createObject(object.getClassName(), 
                            parentClassName, 
                            currentCreatedObjects.get(parentName), 
                            object.getAttributes(), 
                            template, IPAddress, sessionId);

                currentCreatedObjects.put(object.getAttributes().get(ATTRIBUTE_NAME).get(0), oid);
            }//end for
        }catch(Exception ex){
            System.out.println(ex.getMessage()+"eeror en la linea");
            //check your containment hierarchy
        }
    }
    
    private void loadListTypes() {
        boolean errors = false;
        String errorsMsgs = "";
        HashMap<String, String> insetertedAttributes = new HashMap<String, String>();
        
        try {
            BufferedReader input = new BufferedReader(new FileReader(uploadFile));
            int currentFileLine = 0;
            int counter = 0;
            String line;

            while ((line = input.readLine()) != null) {
                currentFileLine++;
                errors = false;
                HashMap<String, List<String>> attributes = new HashMap<String, List<String>>();
                String[] splitLine = line.split(";");
                //not enough fields in the line
                if (splitLine.length < MINIMUN_LISTTYPE_FIELDS) {
                    errorsMsgs += java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_IN_LINE")+currentFileLine+
                                 java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_LISTTYPE_NOT_ENOUGH_FIELDS")+"\n";
                    errors = true;
                }
                else{
                    try{
                        String className = splitLine[0];

                        for(int i = 2; i < splitLine.length; i+=2){
                            List<String> attirbuteValues = new  ArrayList<String>();
                            String attributeType = "";
                            if(insetertedAttributes.containsKey(splitLine[i]))
                                attributeType = insetertedAttributes.get(splitLine[i]);
                            else{    
                                AttributeMetadata attribute = mem.getAttribute(className, splitLine[i-1]);
                                insetertedAttributes.put(splitLine[i], attribute.getType());
                                attributeType = attribute.getType();
                            }
                            if (!isPrimitive(attributeType))
                               attirbuteValues.add(Long.toString(aem.getListTypeItem(splitLine[i], IPAddress, sessionId).getId()));
                            else
                                attirbuteValues.add(splitLine[i]);
                            attributes.put(splitLine[i-1],attirbuteValues);
                        }
                        data.add(new RemoteBusinessObject(-1, className, attributes));
                        counter++;
                    }catch(Exception ex){
                        errorsMsgs += ex.getMessage();
                    }
                }
                if(commitSize == counter && !errors){
                    commitLisTypes();
                    counter = 0;
                    data = new ArrayList<RemoteBusinessObject>();
                }
            }// end while read line
            input.close();
        } catch (IOException ex) {
            ex.getMessage();
        }
    }
    
    public void commitLisTypes(){
        for(RemoteBusinessObject object : data){ 
            try {
                long oid = aem.createListTypeItem(object.getClassName(), "", "", IPAddress , sessionId);
                bem.updateObject(object.getClassName(), oid, object.getAttributes(), IPAddress, sessionId);
            } catch (Exception ex) {
                Logger.getLogger(LoadDataFromFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void log(String fileName, String msgs, String fileLines) throws IOException{
        FileWriter aWriter = new FileWriter(PATH_DATA_LOAD_ERRORS+fileName, true);
        aWriter.write(fileLines);
        aWriter.flush();
        aWriter.close();
        
        aWriter = new FileWriter(PATH_DATA_LOAD_LOGS+fileName, true);
        aWriter.write(msgs);
        aWriter.flush();
        aWriter.close();
    }

    protected void connect(){
        try{
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            bem = (BusinessEntityManagerRemote) registry.lookup(BusinessEntityManagerRemote.REFERENCE_BEM);
            aem = (ApplicationEntityManagerRemote) registry.lookup(ApplicationEntityManagerRemote.REFERENCE_AEM);
            mem = (MetadataEntityManagerRemote) registry.lookup(MetadataEntityManagerRemote.REFERENCE_MEM);
        }catch(Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE,
                    ex.getClass().getSimpleName()+": {0}",ex.getMessage()); //NOI18N
            bem = null;
            aem = null;
            mem = null;
        }
    }
    
    public byte[] downloadErrors(String fileName) throws IOException{
        File file = new File(PATH_DATA_LOAD_ERRORS + fileName);
        return getByteArrayFromFile(file);
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
                throw new IOException("Could not completely read file "+f.getName());
            }
        }else{
            throw new IOException("File too big "+f.getName());
        }
        is.close();
        return bytes;
    }

    private boolean isPrimitive(String type){
        if (type.equals("String") || type.equals("Long")
                || type.equals("Date") || type.equals("Float")
                || type.equals("Integer") || type.equals("Boolean")) 
            return true;
        else
            return false;
    }
    
    @Override
    public void run() {
        try {
            uploadFile();
        } catch (ApplicationObjectNotFoundException ex) {
            Logger.getLogger(LoadDataFromFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotAuthorizedException ex) {
            Logger.getLogger(LoadDataFromFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(LoadDataFromFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MetadataObjectNotFoundException ex) {
            Logger.getLogger(LoadDataFromFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(LoadDataFromFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ObjectNotFoundException ex) {
            Logger.getLogger(LoadDataFromFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OperationNotPermittedException ex) {
            Logger.getLogger(LoadDataFromFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrongMappingException ex) {
            Logger.getLogger(LoadDataFromFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}