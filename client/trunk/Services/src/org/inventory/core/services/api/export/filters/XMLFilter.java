/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JPanel;

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
        WAX xmlWriter = new WAX(os);
        StartTagWAX mainTag = xmlWriter.start("result");     //NOI18N
        mainTag.attr("version", FORMAT_VERSION);      //NOI18N

        StartTagWAX headerTag = mainTag.start("header");     //NOI18N
        if (result.length != 0){
            for (Object header : result[0]){
                StartTagWAX currentColumn = headerTag.start("column");      //NOI18N
                currentColumn.attr("type", "");      //NOI18N
                currentColumn.text(header==null ? "" : header.toString());  //NOI18N
                currentColumn.end();
            }
        }
        headerTag.end();

        StartTagWAX recordsTag = mainTag.start("records");     //NOI18N
        for (int i = 1; i < result.length; i++){
            StartTagWAX currentRecord = recordsTag.start("record");     //NOI18N
            for (int j = 0; j < result[i].length; j++){
                StartTagWAX value = currentRecord.start("value");     //NOI18N
                value.text(result[i][j].toString());
                value.end();
            }
            currentRecord.end();
        }
        recordsTag.end();
        mainTag.end().close();
        //It's not necessary to close the stream, since the line above closes it automatically
        return true;
    }

    @Override
    public JPanel getExportSettingsPanel() {
        return null;
    }
}
