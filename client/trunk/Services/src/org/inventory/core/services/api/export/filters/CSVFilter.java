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
import java.nio.charset.Charset;
import javax.swing.JPanel;
import org.inventory.core.services.api.export.filters.panels.CSVExportSettingsPanel;

/**
 * Implements the logic necessary to export to CSV
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class CSVFilter extends TextExportFilter {

    //Singleton
    private static CSVFilter self;
    private CSVExportSettingsPanel exportPanel;
    //Possible separators
    public static Character CHARACTER_COMMA = ','; //NOI18N
    public static Character CHARACTER_TAB = '\t'; //NOI18N
    public static Character CHARACTER_SPACE = ' '; //NOI18N
    public static Character CHARACTER_PIPE = '|'; //NOI18N
    public static Character CHARACTER_TILDE = '~'; //NOI18N

    private CSVFilter() {}

    public static CSVFilter getInstance(){
        if (self == null)
            self = new CSVFilter();
        return self;
    }
    
    @Override
    public String getDisplayName() {
        return "CSV - Plain text";
    }

    @Override
    public String getExtension() {
        return ".csv";
    }

    @Override
    public boolean export(Object[][] results, FileOutputStream writer) {
        try {
            for (Object[] record : results){
                String currentRecord = "";
                for (Object column : record)
                    currentRecord += (column == null ? "" : column) + 
                            (exportPanel == null ? CHARACTER_COMMA.toString() : exportPanel.getSelectedCharacter().toString()); //NOI18N
                currentRecord = currentRecord.substring(0,currentRecord.length() - 1);
                writer.write(currentRecord.getBytes(Charset.forName("UTF-8")));
                writer.write('\n');
            }
            writer.flush(); //We don't close the stream here
            return true;
        } catch(IOException ex){
            //TODO: Put some log4j stuff here
            return false;
        }
    }
    
    @Override
    public JPanel getExportSettingsPanel() {
        if (exportPanel == null)
            exportPanel = new CSVExportSettingsPanel();
        return exportPanel;
    }
}
