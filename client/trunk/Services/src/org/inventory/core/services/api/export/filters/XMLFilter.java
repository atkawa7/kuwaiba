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

package org.inventory.core.services.api.export.filters;

import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JPanel;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.openide.util.Exceptions;

/**
 * Exports to XML as explained <a href="http://www.kuwaiba.org/kuwaiba/wiki/index.php?title=XML_Documents">here</a>
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class XMLFilter extends TextExportFilter {
    
    /**
     * Current format version. Visit the <a href="http://www.kuwaiba.neotropic.co/wiki/index.php?title=XML_Documents">wiki</a> for details
     */
    private static final String FORMAT_VERSION = "1.0";
    /**
     * Singleton
     */
    private static XMLFilter self;

    private XMLFilter() {}
    
    public static XMLFilter getInstance() {
        if (self == null)
            self = new XMLFilter();
        return self;
    }

    @Override
    public String getDisplayName() {
        return "XML - Markup Language";
    }

    @Override
    public String getExtension() {
        return ".xml"; //NOI18N
    }

    @Override
    public boolean export(Object[][] result, FileOutputStream os) throws IOException {
        try {
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(os);
            XMLEventFactory xmlef = XMLEventFactory.newFactory();
            
            QName qnameResult = new QName("result");
            xmlew.add(xmlef.createStartElement(qnameResult, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), FORMAT_VERSION));
                        
            QName qnameHeader = new QName("header");
            xmlew.add(xmlef.createStartElement(qnameHeader, null, null));
            if (result.length != 0) {
                for (Object column : result[0]) {
                    QName qnameColumn = new QName("column");
                    xmlew.add(xmlef.createStartElement(qnameColumn, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("type"), ""));
                    xmlew.add(xmlef.createCharacters(column == null ? "" : column.toString()));
                    xmlew.add(xmlef.createEndElement(qnameColumn, null));
                }
            }
            xmlew.add(xmlef.createEndElement(qnameHeader, null));
            
            QName qnameRecords = new QName("records");
            xmlew.add(xmlef.createStartElement(qnameRecords, null, null));
            for (int i = 1; i < result.length; i += 1) {
                QName qnameRecord = new QName("record");
                xmlew.add(xmlef.createStartElement(qnameRecord, null, null));
                for (Object value : result[i]) {
                    QName qnameValue = new QName("value");
                    xmlew.add(xmlef.createStartElement(qnameValue, null, null));
                    xmlew.add(xmlef.createCharacters(value.toString()));
                    xmlew.add(xmlef.createEndElement(qnameValue, null));
                }
                xmlew.add(xmlef.createEndElement(qnameRecord, null));
            }
            xmlew.add(xmlef.createEndElement(qnameRecords, null));
            
            xmlew.add(xmlef.createEndElement(qnameResult, null));
            
            xmlew.close();
            return true;
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    @Override
    public JPanel getExportSettingsPanel() {
        return null;
    }
}
