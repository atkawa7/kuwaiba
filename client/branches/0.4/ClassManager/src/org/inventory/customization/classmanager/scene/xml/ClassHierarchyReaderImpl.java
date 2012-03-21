/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package org.inventory.customization.classmanager.scene.xml;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.LocalStuffFactory;
import org.inventory.core.services.api.metadata.LocalAttributeWrapper;
import org.inventory.core.services.api.metadata.LocalClassWrapper;
import org.inventory.core.services.api.xml.ClassHierarchyReader;
//import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the XML reader for this document
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=ClassHierarchyReader.class)
public class ClassHierarchyReaderImpl implements ClassHierarchyReader{
    private String documentVersion;
    private String serverVersion;
    private Date date;
    private List<LocalClassWrapper> roots;

    public String getDocumentVersion() {
        return documentVersion;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public Date getDate() {
        return date;
    }

    public List<LocalClassWrapper> getRootClasses() {
        return roots;
    }

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
        LocalClassWrapper aClass = LocalStuffFactory.createLocalClassWrapper();
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
                        LocalAttributeWrapper att = LocalStuffFactory.createLocalAttributeWrapper();
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
}
