/*
 * Copyright (c) 2019 rchingal.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    rchingal - initial API and implementation and/or initial documentation
 */
package org.inventory.core.services.api.export.filters;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.w3c.dom.Document;

/**
 * Exports to XML as file
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class XMLExportFilter extends TextExportFilter {

    //Singleton
    private static XMLExportFilter self;
    private JComplexDialogPanel myPanel;

    public static XMLExportFilter getInstance() {
        if (self == null) {
            self = new XMLExportFilter();
        }
        return self;
    }

    @Override
    public String getDisplayName() {
        return "XML - Markup Language";
    }

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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public JPanel getExportSettingsPanel() {
        return null;
    }

    @Override
    public boolean export(Object[][] result, FileOutputStream out) throws IOException {
        return true;
    }

}
