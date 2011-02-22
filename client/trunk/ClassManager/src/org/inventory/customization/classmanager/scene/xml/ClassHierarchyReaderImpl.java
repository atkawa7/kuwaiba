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
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.core.services.interfaces.LocalClassWrapper;
import org.inventory.core.services.interfaces.xml.ClassHierarchyReader;
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

    public String getDocumentVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getServerVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Date getDate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<LocalClassWrapper> getRootClasses() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void read(byte[] xmlDocument) throws XMLStreamException{
        QName hierarchyTag = new QName("hierarchy"); //NOI18N
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        ByteArrayInputStream bais = new ByteArrayInputStream(xmlDocument);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

        while (reader.hasNext()){
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT){
                if (reader.getName().equals(hierarchyTag)){
                    this.documentVersion = reader.getAttributeValue(null, "documentVersion");
                    this.serverVersion = reader.getAttributeValue(null, "serverVersion");
                    //this.date = new DateFormat() {}
                }
            }
        }
    }

}
