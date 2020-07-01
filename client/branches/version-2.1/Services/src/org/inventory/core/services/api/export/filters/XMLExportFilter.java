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
package org.inventory.core.services.api.export.filters;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Exports filter for a simple XML file
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class XMLExportFilter extends TextExportFilter {

    //Singleton
    private static XMLExportFilter self;
    private JComplexDialogPanel myPanel;

    /**
     * Singleton construction
     * 
     * @return 
     */
    public static XMLExportFilter getInstance() {
        if (self == null) {
            self = new XMLExportFilter();
        }
        return self;
    }

    /**
     * return extension used for swing
     * 
     * @return
     */
    public String getExtensionFileChooser() {

        return "xml"; //NOI18N
    }

    /**
     * default action, create a 'xml' file
     * 
     * @param filename
     * @return
     * @throws IOException
     */
    public boolean export(String filename) throws IOException {
        CommunicationsStub communication = CommunicationsStub.getInstance();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        try {
            byte[] bytes = communication.getClassHierarchy(true);
            OutputFormat format = new OutputFormat("XML", "UTF-8", true);
            format.setIndenting(true);
            format.setIndent(5);

            factory.setNamespaceAware(true);
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(bytes));

            java.io.Writer writer = new java.io.FileWriter(filename);
            XMLSerializer xml = new XMLSerializer(writer, format);
            xml.serialize(doc);
            return true;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            Exceptions.printStackTrace(e);
        }
        return false;
    }

    /**
     * default action for export settings
     * 
     * @return
     */
    @Override
    public JPanel getExportSettingsPanel() {
        return null;
    }

    /**
     *
     * @param result
     * @param out
     * @return
     * @throws IOException
     */
    @Override
    public boolean export(Object[][] result, FileOutputStream out) throws IOException {
        return true;
    }

    /**
     * return 'xml' default description
     * 
     * @return
     */
    @Override
    public String getDisplayName() {
        return "XML - Markup Language";
    }

    /**
     * return 'xml' extension
     * 
     * @return
     */
    @Override
    public String getExtension() {
        if (myPanel != null) {
            if (((JComboBox) myPanel.getComponent("cmbFormat")).
                    getSelectedItem().equals(".xml")) {
                return ".xml";
            }
        }
        return ".xml"; //NOI18N
    }
}
