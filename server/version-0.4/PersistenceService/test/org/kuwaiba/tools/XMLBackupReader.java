/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kuwaiba.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.persistence.exceptions.ConnectionException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.persistenceservice.impl.ConnectionManagerImpl;
import org.kuwaiba.persistenceservice.impl.MetadataEntityManagerImpl;

/**
 *
 * @author adrian
 */
public class XMLBackupReader {

    private String documentVersion;
    private String serverVersion;
    private Date date;
    private List<LocalClassWrapper> roots;

    MetadataEntityManagerImpl mem;
    ConnectionManagerImpl cm;

    public XMLBackupReader() {
        try {
            cm = new ConnectionManagerImpl();
            cm.openConnection();
            mem = new MetadataEntityManagerImpl(cm);
        } catch (ConnectionException ex) {
            Logger.getLogger(XMLBackupReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    String sub= "";    

    public void read(byte[] xmlDocument) throws Exception{
        QName hierarchyTag = new QName("hierarchy"); //NOI18N
        QName classTag = new QName("class"); //NOI18N
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        ByteArrayInputStream bais = new ByteArrayInputStream(xmlDocument);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
        roots = new ArrayList<LocalClassWrapper>();

        while (reader.hasNext()){
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT){
                if (reader.getName().equals(hierarchyTag)){
                    this.documentVersion = reader.getAttributeValue(null, "documentVersion"); //NOI18N
                    this.serverVersion = reader.getAttributeValue(null, "serverVersion"); //NOI18N
                    this.date = new Date(Long.valueOf(reader.getAttributeValue(null, "date"))); //NOI18N
                }else
                    if (reader.getName().equals(classTag)){
                        roots.add(readClassNode(reader));
                    }
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

        aClass.setName(reader.getAttributeValue(null, "name"));
        aClass.setApplicationModifiers(Integer.valueOf(reader.getAttributeValue(null, "applicationModifiers")));
        aClass.setJavaModifiers(Integer.valueOf(reader.getAttributeValue(null, "javaModifiers")));
        aClass.setClassType(Integer.valueOf(reader.getAttributeValue(null, "classType")));
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
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    public void axu(){
        try {
            readRoots(roots, "");
        } catch (Exception ex) {
            Logger.getLogger(XMLBackupReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void readRoots(List<LocalClassWrapper> listNodes, String parentClassName) throws Exception{

        ClassMetadata clmt = new ClassMetadata();
        byte[] x = new byte[1];

//        dC.setName("defaul-category");
//        dC.setDisplayName("defaul-category");
//        dC.setDescription("Default category to test");

        for (LocalClassWrapper lcw: listNodes) {

            clmt.setAbstractClass(Modifier.isAbstract(lcw.getJavaModifiers()));
            clmt.setCategory(null);
            clmt.setColor(0);
            clmt.setCountable((lcw.getApplicationModifiers() & LocalClassWrapper.MODIFIER_NOCOUNT) != LocalClassWrapper.MODIFIER_NOCOUNT);
            clmt.setCustom(false);
            clmt.setDescription("");
            clmt.setDisplayName("");
            clmt.setIcon(x);
            clmt.setInterfaces(null);
            clmt.setListType(false);
            clmt.setName(lcw.getName());
            clmt.setParentClassName(parentClassName);
            clmt.setSmallIcon(x);

            List<AttributeMetadata> attList = new ArrayList<AttributeMetadata>();
            
            for (LocalAttributeWrapper law : lcw.getAttributes())
            {
                AttributeMetadata attr = new AttributeMetadata();

                attr.setName(law.getName());
                attr.setDisplayName("");
                attr.setDescription("");
                attr.setType(law.getType());
                //everithing is technicial by default
                attr.setAdministrative(false);

                if(law.getType().equals("Float") || law.getType().equals("Long") || law.getType().equals("String")
                    || law.getType().equals("Integer") || law.getType().equals("Boolean") || law.getType().equals("byte[]"))
                    attr.setMapping(AttributeMetadata.MAPPING_PRIMITIVE);
                else if(law.getType().equals("Date"))
                    attr.setMapping(AttributeMetadata.MAPPING_DATE);
                else
                    attr.setMapping(AttributeMetadata.MAPPING_MANYTOONE);
                
                int applicationModifiers = law.getApplicationModifiers();

                attr.setReadOnly((applicationModifiers & LocalAttributeWrapper.MODIFIER_READONLY) != LocalAttributeWrapper.MODIFIER_READONLY);
                attr.setNoCopy((applicationModifiers & LocalAttributeWrapper.MODIFIER_NOCOPY) != LocalAttributeWrapper.MODIFIER_NOCOPY);
                attr.setNoSerialize((applicationModifiers & LocalAttributeWrapper.MODIFIER_NOSERIALIZE) != LocalAttributeWrapper.MODIFIER_NOSERIALIZE);
                attr.setUnique(false);
                attr.setVisible(true);

                if(!attr.getName().equals("id"))
                    attList.add(attr);
            }
            clmt.setAttributes(attList);

            mem.createClass(clmt);

            if(lcw.getDirectSubClasses().size() >1){
                
                readRoots(lcw.getDirectSubClasses(), lcw.getName());
            }
            
        }
    }
}
