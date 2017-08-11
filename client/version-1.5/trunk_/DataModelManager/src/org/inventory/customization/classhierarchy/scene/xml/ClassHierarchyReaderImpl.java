/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.customization.classhierarchy.scene.xml;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalClassWrapper;
import org.inventory.core.services.api.xml.ClassHierarchyReader;

/**
 * Implementation of Class Hierarchy Reader used for Class Hierarchy Service
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ClassHierarchyReaderImpl implements ClassHierarchyReader {
    private String documentVersion;
    private String serverVersion;
    private Date date;
    private LocalClassMetadata root;
    private List<LocalClassMetadata> roots;

    @Override
    public String getDocumentVersion() {
        return documentVersion;
    }

    @Override
    public String getServerVersion() {
        return serverVersion;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public List<LocalClassWrapper> getRootClasses() {
        return null;
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void read(byte[] xmlDocument) throws Exception {
        QName hierarchyTag = new QName("hierarchy"); //NOI18N
        QName classTag = new QName("class"); //NOI18N
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        ByteArrayInputStream bais = new ByteArrayInputStream(xmlDocument);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
        roots = new ArrayList();
        
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (reader.getName().equals(hierarchyTag)) {
                    documentVersion = reader.getAttributeValue(null, "documentVersion"); //NOI18N
                    
                    serverVersion = reader.getAttributeValue(null, "serverVersion"); //NOI18N
                    date = new Date(Long.valueOf(reader.getAttributeValue(null, "date"))); //NOI18N
                }
                else
                    if (reader.getName().equals(classTag)) {
                        roots.add(0, readClassNode(reader, null));
                    }
            }
        }
        root = roots.get(0);
        reader.close();
    }
    
    public LocalClassMetadata getRoot() {
        return root;
    }
    
    public List<LocalClassMetadata> getRoots() {
        return roots;
    }
    
    private LocalClassMetadata readClassNode(XMLStreamReader reader, String parentName) throws XMLStreamException {
        long id = Long.valueOf(reader.getAttributeValue(null, "id")); //NOI18N
        String className = reader.getAttributeValue(null, "name"); //NOI18N
        int javaModifiers = Integer.valueOf(reader.getAttributeValue(null, "javaModifiers")); //NOI18N
        
        List<String> attributesNames = new ArrayList();
        List<String> attributesTypes = new ArrayList();
        
        QName attributeTag = new QName("attribute"); //NOI18N
        QName classTag = new QName("class"); //NOI18N
                
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (reader.getName().equals(classTag)) {
                    roots.add(0, readClassNode(reader, className));
                }
                else {
                    if (reader.getName().equals(attributeTag)) {
                        attributesNames.add(reader.getAttributeValue(null, "name"));
                        attributesTypes.add(reader.getAttributeValue(null, "type"));
                    }
                }
            }
            else {
                if (event == XMLStreamConstants.END_ELEMENT) {
                    if (reader.getName().equals(classTag))
                        break;
                }
            }
        }
        LocalClassMetadata lcm = new LocalClassMetadata(
                id, className, "", parentName, 
                Modifier.isAbstract(javaModifiers), false, false, false, false, 
                new byte[0], 0, new HashMap(), new byte[0], "", new ArrayList(), 
                attributesNames.toArray(new String[0]), 
                attributesTypes.toArray(new String[0]), new String[0], 
                new ArrayList(),new ArrayList(), new ArrayList(), new String[0]);
        
        return lcm;
    }
}
