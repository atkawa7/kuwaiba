/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
package org.inventory.customization.classhierarchy.importdb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.core.LocalClassMetadata;

/**
 * This class work as a thread and parce a 'xml' file and can returning a
 * metadata list List<LocalClassMetadata>
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class FileReaderTask extends SwingWorker<Void, Integer> {

    private List<LocalClassMetadata> roots;
    private String documentVersion;
    private String serverVersion;
    private LocalClassMetadata root;
    private Date date;
    private final File xmFile;
    private final HashMap<String, String> errors;

    /**
     * default constructor
     *
     * @param xmFile
     */
    public FileReaderTask(File xmFile) {
        this.xmFile = xmFile;
        this.errors = new HashMap<>();
    }

    /**
     * read a byte array and transform into LocalClassMetadata
     *
     * @param xmlDocument: byte[]
     * @throws Exception
     */
    public void read(byte[] xmlDocument) {
        QName hierarchyTag = new QName("hierarchy"); //NOI18N
        QName classTag = new QName("class"); //NOI18N
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        setRoots(new ArrayList<>());

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(xmlDocument);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(hierarchyTag)) {
                        setDocumentVersion(reader.getAttributeValue(null, "documentVersion")); //NOI18N
                        setServerVersion(reader.getAttributeValue(null, "serverVersion")); //NOI18N
                        setDate(new Date(Long.valueOf(reader.getAttributeValue(null, "date")))); //NOI18N

                    } else if (reader.getName().equals(classTag)) {
                        getRoots().add(0, readClassNode(reader, null));
                    }
                }

            }
            setRoot(getRoots().get(0));
            reader.close();
        } catch (Exception ex) {
            errors.put("Reading hierarchy", ex.getMessage());
        }
    }

    /**
     *
     * Receives a 'xml' header and transform into Local ClassMetadata
     *
     * @param reader
     * @param parentName
     * @return
     * @throws XMLStreamException
     * @throws Exception
     */
    private LocalClassMetadata readClassNode(XMLStreamReader reader, String parentName) {

        List<String> attributesNames = new ArrayList<>();
        List<String> attributesTypes = new ArrayList<>();
        QName attributeTag = new QName("attribute"); //NOI18N
        QName classTag = new QName("class"); //NOI18N
        LocalClassMetadata lcm = null;

        try {
            long id = Long.valueOf(reader.getAttributeValue(null, "id")); //NOI18N
            String className = reader.getAttributeValue(null, "name"); //NOI18N
            int javaModifiers = Integer.valueOf(reader.getAttributeValue(null, "javaModifiers")); //NOI18N

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(classTag)) {
                        getRoots().add(0, readClassNode(reader, className));
                        //System.out.println("roots: "+readClassNode(reader, className));
                    } else {
                        if (reader.getName().equals(attributeTag)) {
                            attributesNames.add(reader.getAttributeValue(null, "name"));
                            attributesTypes.add(reader.getAttributeValue(null, "type"));

                        }
                    }
                } else {
                    if (event == XMLStreamConstants.END_ELEMENT) {
                        if (reader.getName().equals(classTag)) {
                            break;
                        }
                    }
                }
            }

            lcm = new LocalClassMetadata(
                    id //long id
                    ,
                     className //String className
                    ,
                     "" //String displayName
                    ,
                     parentName //String parentName
                    ,
                     Modifier.isAbstract(javaModifiers) //boolean _abstract
                    ,
                     false //boolean viewable
                    ,
                     false //boolean listType
                    ,
                     false //boolean custom
                    ,
                     false //boolean inDesign
                    ,
                     new byte[0] //byte[] smallIcon
                    ,
                     0 //int color
                    ,
                     new byte[0] //byte[] icon
                    ,
                     "" //String description
                    ,
                     new ArrayList<>() //List<Long> attributesIds
                    ,
                     attributesNames.toArray(new String[0])//String[] attributesNames
                    ,
                     attributesTypes.toArray(new String[0])//String[] attributesTypes
                    ,
                     new String[0] //String[] attributesDisplayNames
                    ,
                     new String[0] //String[] attributStringesDescriptions
                    ,
                     new ArrayList<>() //List<Boolean> attributesMandatories
                    ,
                     new ArrayList<>() //List<Boolean> attributesMultiples
                    ,
                     new ArrayList<>() //List<Boolean> attributesUniques
                    ,
                     new ArrayList<>() //List<Boolean> attributesVisibles
                    ,
                     new ArrayList<>() //List<Integer> attributesOrders
            );
        } catch (XMLStreamException ex) {
            errors.put("Reading meta class", "XML malformed");
        } catch (NumberFormatException ex) {
            errors.put("Reading meta class", "Number Format");
        }
        return lcm;
    }

    /**
     * Main thread, it will be execute in background
     *
     * @return @throws Exception
     */
    @Override
    protected Void doInBackground() throws Exception {
        try {
            read(Files.readAllBytes(xmFile.toPath()));
        } catch (Exception ex) {

            errors.put(ex.getCause().toString(), ex.getMessage());

            JOptionPane.showMessageDialog(null, "Error uploading file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            setProgress(0);
            cancel(true);
        }
        return null;
    }

    //getters and setters
    /**
     * @return the roots
     */
    public List<LocalClassMetadata> getRoots() {
        return roots;
    }

    /**
     * @param roots the roots to set
     */
    public void setRoots(List<LocalClassMetadata> roots) {
        this.roots = roots;
    }

    /**
     * @return the documentVersion
     */
    public String getDocumentVersion() {
        return documentVersion;
    }

    /**
     * @param documentVersion the documentVersion to set
     */
    public void setDocumentVersion(String documentVersion) {
        this.documentVersion = documentVersion;
    }

    /**
     * @return the serverVersion
     */
    public String getServerVersion() {
        return serverVersion;
    }

    /**
     * @param serverVersion the serverVersion to set
     */
    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    /**
     * @return the root
     */
    public LocalClassMetadata getRoot() {
        return root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(LocalClassMetadata root) {
        this.root = root;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the errors
     */
    public HashMap<String, String> getErrors() {
        return errors;
    }
}
