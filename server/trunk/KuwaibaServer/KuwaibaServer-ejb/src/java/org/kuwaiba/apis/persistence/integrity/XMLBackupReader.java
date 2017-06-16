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

package org.kuwaiba.apis.persistence.integrity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;

/**
 * Reads the kuwaiba data model from the class_hierarchy.xml file and create 
 * every class and it's attributes in the database.
 * @author Adrian Fernando Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class XMLBackupReader {

    private String documentVersion;
    private String serverVersion;
    private Date date;
    private List<LocalClassWrapper> roots;
    private MetadataEntityManager mem;

    public XMLBackupReader(MetadataEntityManager mem) {
        this.mem = mem;
    }
    
    public void read(byte[] xmlDocument) throws XMLStreamException {
        QName hierarchyTag = new QName("hierarchy"); //NOI18N
        QName classTag = new QName("class"); //NOI18N
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        ByteArrayInputStream bais = new ByteArrayInputStream(xmlDocument);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
        roots = new ArrayList<>();

        while (reader.hasNext()){
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT){
                if (reader.getName().equals(hierarchyTag)){
                    this.documentVersion = reader.getAttributeValue(null, "documentVersion"); //NOI18N
                    this.serverVersion = reader.getAttributeValue(null, "serverVersion"); //NOI18N
                    this.date = new Date(Long.valueOf(reader.getAttributeValue(null, "date"))); //NOI18N
                }else
                    if (reader.getName().equals(classTag))
                        roots.add(readClassNode(reader));
            }
        }
        reader.close();
    }

    /**
     * Recursive method that reads a single "class" node
     * @param reader the pointer pointing to a class "node"
     */
    private LocalClassWrapper readClassNode(XMLStreamReader reader) throws XMLStreamException{
        LocalClassWrapper aClass = new LocalClassWrapper();

        aClass.setName(reader.getAttributeValue(null, "name")); //NOI18N
        aClass.setApplicationModifiers(Integer.valueOf(reader.getAttributeValue(null, "applicationModifiers"))); //NOI18N
        aClass.setJavaModifiers(Integer.valueOf(reader.getAttributeValue(null, "javaModifiers"))); //NOI18N
        aClass.setClassType(Integer.valueOf(reader.getAttributeValue(null, "classType"))); //NOI18N
        aClass.setClassPackage(reader.getAttributeValue(null, "classPackage")); //NOI18N
        QName attributeTag = new QName("attribute"); //NOI18N
        QName classTag = new QName("class"); //NOI18N

        while (true){
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT){
                if (reader.getName().equals(classTag)){
                    aClass.getDirectSubClasses().add(readClassNode(reader));
                }else
                    if (reader.getName().equals(attributeTag)){
                        LocalAttributeWrapper att = new LocalAttributeWrapper();
                        att.setName(reader.getAttributeValue(null, "name"));
                        att.setType(reader.getAttributeValue(null, "type"));
                        att.setApplicationModifiers(Integer.valueOf(reader.getAttributeValue(null, "applicationModifiers")));
                        att.setJavaModifiers(Integer.valueOf(reader.getAttributeValue(null, "javaModifiers")));
                        aClass.getAttributes().add(att);
                    }
            }
            else{
                if (event == XMLStreamConstants.END_ELEMENT){
                    if (reader.getName().equals(classTag))
                        break;
                }
            }
        }
        return aClass;
    }


    // Returns the contents of the file in a byte array.
    public static byte[] getBytesFromFile(File file) throws IOException {
        byte[] bytes;
        // Get the size of the file
        try (InputStream is = new FileInputStream(file)) {
            // Get the size of the file
            long length = file.length();
            // You cannot create an array using a long type.
            // It needs to be an int type.
            // Before converting to an int type, check
            // to ensure that file is not larger than Integer.MAX_VALUE.
            if (length > Integer.MAX_VALUE) {
                // File is too large
            }   // Create the byte array to hold the data
            bytes = new byte[(int) length];
            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }   // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
        }
        return bytes;
    }

    public boolean load(){
        try {
            readRoots(roots, null);
            return true;
        } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(XMLBackupReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public void readRoots(List<LocalClassWrapper> listNodes, String parentClassName) 
            throws DatabaseException, MetadataObjectNotFoundException, InvalidArgumentException
    {
        ClassMetadata clmt = new ClassMetadata();
        for (LocalClassWrapper lcw: listNodes) {
            clmt.setAbstract(Modifier.isAbstract(lcw.getJavaModifiers()));
            clmt.setCategory(lcw.getClassPackage());
            clmt.setColor(0);
            clmt.setCountable((lcw.getApplicationModifiers() & LocalClassWrapper.MODIFIER_NOCOUNT) != LocalClassWrapper.MODIFIER_NOCOUNT);
            clmt.setCustom(false);
            clmt.setDescription("");
            clmt.setDisplayName("");
            clmt.setIcon(new byte[0]);
            clmt.setInterfaces(null);
            clmt.setListType(false);
            clmt.setName(lcw.getName());
            clmt.setParentClassName(parentClassName);
            clmt.setSmallIcon(new byte[0]);
            clmt.setInDesign(false);
            
            Set<AttributeMetadata> attList = new HashSet<>();
            
            for (LocalAttributeWrapper law : lcw.getAttributes()) {
                AttributeMetadata attr = new AttributeMetadata();

                attr.setName(law.getName());
                attr.setDisplayName("");
                attr.setDescription("");
                attr.setType(law.getType());
                attr.setAdministrative(false);
                
                int applicationModifiers = law.getApplicationModifiers();

                attr.setReadOnly((applicationModifiers & LocalAttributeWrapper.MODIFIER_READONLY) != LocalAttributeWrapper.MODIFIER_READONLY);
                attr.setNoCopy((applicationModifiers & LocalAttributeWrapper.MODIFIER_NOCOPY) != LocalAttributeWrapper.MODIFIER_NOCOPY);
                attr.setUnique(false);
                attr.setVisible(true);

                if(!attr.getName().equals("id"))
                    attList.add(attr);
            }
            clmt.setAttributes(attList);
            try {
                mem.createClass(clmt);
            }catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException ex){
                System.out.println(String.format("Class %s could not be created: %s", lcw.getName(), ex.getMessage()));
            }
            //The subclasses are processed even if the parent class failed
            if(lcw.getDirectSubClasses().size() > 0)
                readRoots(lcw.getDirectSubClasses(), lcw.getName());
        }
    }
}