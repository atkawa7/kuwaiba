/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.queries.graphical.exportfilters;

import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Exports to XML in a structure explained at the <a href="http://is.gd/kcl1a">project's wiki</a>
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class XMLFilter implements ExportFilter{
    /**
     * Current format version. Visit the <a href="https://sourceforge.net/apps/mediawiki/kuwaiba/index.php?title=XML_Documents">wiki</a> for details
     */
    private static final String FORMAT_VERSION = "1.0";

    public String getDisplayName() {
        return "XML - Markup Language";
    }

    public String getExtension() {
        return ".xml"; //NOI18N
    }

    public boolean export(Object[][] result, String fileName) {
        try{
            FileWriter writer = new FileWriter(fileName);
            WAX xmlWriter = new WAX(writer);
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
        }catch (IOException ex){
            //TODO: Put some log4j stuff here
            return false;
        }
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
