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
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
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
public final class SyncServicesManager{
    
    /**
     * Date format for sync 
     */
    private static final String DATE_HOUR_FORMAT = "yyyy-MM-dd'T'HH-mm-ss'U'";
    /**
     * Path to sync's files
     */
    private static final String PATH_DATA_LOAD_FILES = "sync/load/kuwaiba_load_data_";
    private static final String PATH_DATA_LOAD_LOGS = "sync/logs/kuwaiba_load_data.log_";
    private static final String PATH_DATA_LOAD_ERRORS = "sync/errors/kuwaiba_load_data.errors_";
    /**
     * 
     */
    private static final String NONE = "none";
    /**
     * if the parent is the dummy root
     */
    private static final String ROOT = "root";
    private static final String OBJECTROOTNAME = "-1";
    private static final String VALUE_OPERATOR = ":";
    
    private BusinessEntityManagerRemote bem;
    private MetadataEntityManagerRemote mem;
    private ApplicationEntityManagerRemote aem;
    
    public SyncServicesManager() {
        connect();
    }
    
    public String loadDataFromFile(byte[] choosenFile, long userId) throws MetadataObjectNotFoundException, InvalidArgumentException, ObjectNotFoundException, OperationNotPermittedException, DatabaseException, WrongMappingException, IOException {
        String fileName = saveFile(choosenFile, userId);
        File aFile = new File(PATH_DATA_LOAD_FILES + fileName);
        boolean errors = false;
        String errorsMsgs = "";
    String errorLines = "";
        try {
            BufferedReader input = new BufferedReader(new FileReader(aFile));
            String line;
            HashMap<String, Long> currentCreatedObjects = new HashMap<String, Long>();
            long oid;
            long templateId = 0;
            int currentFileLine = 0;
            String savedObjectName = "";

            while ((line = input.readLine()) != null) {
                currentFileLine++;
                errors = false;
                HashMap<String, List<String>> attributes = new HashMap<String, List<String>>();
                String[] splitLine = line.split(";");
                //not enough fields in the line
                if (splitLine.length < 4) {
                    errorsMsgs += java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_IN_LINE")+currentFileLine+
                                 java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_NOT_ENOUGH_FIELDS")+"\n";
                    errors = true;
                }
                else{
                    String className = splitLine[0];
                    String template = splitLine[1];
                    if (template.equals(NONE)) //TODO implmenet templates support
                        templateId = 0;
                    String parentClassName = splitLine[2];
                    String objectParentName = "";
                    //puede existir error si no se ponene :
                    String[] parentNameSplit = splitLine[4].split(VALUE_OPERATOR);
                    if(parentNameSplit.length<2){
                        errorsMsgs += java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_IN_LINE")+currentFileLine+
                                     java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_NO_PARENT_NAME")+"\n";
                        errors = true;
                    }
                    else if(!errors){
                        objectParentName = parentNameSplit[1];
                        //Read Attributes 
                        for (int i = 5; i < splitLine.length - 1; i += 2) {
                            List<String> values = new ArrayList<String>();
                            String attributeName = splitLine[i];
                            //error si no se pone :
                            String[] attribute = splitLine[i + 1].split(VALUE_OPERATOR);
                            if(attribute.length<2){
                                errorsMsgs += java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_IN_LINE")+currentFileLine+
                                     java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_NOT_ATTIRBUTE_VALUE")+"\n";
                                errors = true;
                            }
                            else{
                                String type = attribute[0];
                                String value = attribute[1];
                                AttributeMetadata theAttribute =  null;
//                                try{
//                                    theAttribute = mem.getAttribute(className, attributeName);
//                                }catch(Exception ex){
//                                    errorsMsgs += java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_IN_LINE")+currentFileLine + ex.getMessage()+"\n";
//                                    errors = true;
//                                    break;
//                                }
                                if (theAttribute == null) {
                                    errorsMsgs += java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_IN_LINE")+ currentFileLine +
                                            java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_ATTRIBUTE_NOT_FOUND")+ attributeName+"\n";
                                    errors = true;
                                }
                                else if (theAttribute.getType().equals(type)) {
                                    if (type.equals("String") || type.equals("Long")
                                            || type.equals("Date") || type.equals("Float")
                                            || type.equals("Integer") || type.equals("Boolean")) {
                                        values.add(value);
                                        attributes.put(attributeName, values);
                                        if (attributeName.equals("name")) {
                                            savedObjectName = value;
                                        }
                                    } else {
                                        //The type is not primitive.
                                        RemoteBusinessObjectLight listTypeItem = null;
                                        try{
                                            //listTypeItem = aem.getListTypeItem(value);
                                        }catch(Exception ex){
                                            errorsMsgs += java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_IN_LINE")+currentFileLine+
                                                    ex.getMessage()+java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_LISTTYPE_NOTFOUND")+value+"\n";
                                            errors = true;
                                        }
                                        if(listTypeItem!=null){
                                            values.add(Long.toString(listTypeItem.getId()));
                                            attributes.put(attributeName, values);
                                        }
                                        else{
                                            errorsMsgs += java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_IN_LINE")+currentFileLine+
                                                    java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_LISTTYPE_NOTFOUND")+value+"\n";
                                            errors = true;
                                        }
                                    }//end else type is not primitive
                                }//end if to eval attribute type 
                            }//end if attributos estan bien formados
                        }//end for read Attributes
//                        if(!errors){
//                            //is a son of dummy root
//                            try{
//                                if (parentClassName.equals(ROOT) && objectParentName.equals(OBJECTROOTNAME))
//                                    oid = bem.createObject(className, null, -1, attributes, templateId);
//                                else// si no esta en la jerarquia de contentencia va a salir error! si el padre no existe! La clase del padre no coincide con el nombre de padre objeto dado
//                                    oid = bem.createObject(className, parentClassName, currentCreatedObjects.get(objectParentName), attributes, templateId);
//                                    currentCreatedObjects.put(savedObjectName, oid);
//                            }catch(Exception ex){
//                                errorsMsgs += java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_IN_LINE")+currentFileLine +
//                                        java.util.ResourceBundle.getBundle("org/kuwaiba/sync/Errors").getString("ERROR_PARENT_BAD_CLASS")+"\n";
//                                errors = true;
//                            }
//                        }
                        //I keep the name object and the oid of the saved object to create new ones 
                    }//else suficientes campos
                }//else el nombre del padre esta mal formado
                if(errors)
                    errorLines += line+"\n";
            }//end while read file
            input.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        log(fileName,  errorsMsgs,  errorLines);
        //porque tocar retornar el progreso!
        return fileName;
    }

    public int upadteDataFromFile(byte[] choosenFile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int syncData() {
        throw new UnsupportedOperationException("Not supported yet.");
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
    
    public String saveFile(byte[] choosenFile, long userId) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_HOUR_FORMAT);
        FileOutputStream fileOuputStream = null;
        String fileName = dateFormat.format(new Date())+Long.toString(userId);
        try {
            File file = new File(PATH_DATA_LOAD_FILES+fileName);
            if(!file.exists())
                file.createNewFile();
            fileOuputStream = new FileOutputStream(file, false);
            fileOuputStream.write(choosenFile);
            fileOuputStream.flush();
	    fileOuputStream.close();
            
            File logFile = new File(PATH_DATA_LOAD_LOGS+fileName);
            if(!logFile.exists())
                logFile.createNewFile();
            
            File errorsFile = new File(PATH_DATA_LOAD_ERRORS+fileName);
            if(!errorsFile.exists())
                errorsFile.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOuputStream != null)
                    fileOuputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }

    protected void connect(){
        try{
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            mem = (MetadataEntityManagerRemote) registry.lookup(MetadataEntityManagerRemote.REFERENCE_MEM);
            bem = (BusinessEntityManagerRemote) registry.lookup(BusinessEntityManagerRemote.REFERENCE_BEM);
            aem = (ApplicationEntityManagerRemote) registry.lookup(ApplicationEntityManagerRemote.REFERENCE_AEM);
        }catch(Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE,
                    ex.getClass().getSimpleName()+": {0}",ex.getMessage()); //NOI18N
            mem = null;
            bem = null;
            aem = null;
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
}
