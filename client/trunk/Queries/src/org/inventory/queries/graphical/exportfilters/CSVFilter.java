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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Implements the logic necessary to export to CSV
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class CSVFilter implements ExportFilter{

    private Character separator;

    //Possible separators
    public static Character CHARACTER_COMMA = ','; //NOI18N
    public static Character CHARACTER_TAB = '\t'; //NOI18N
    public static Character CHARACTER_SPACE = ' '; //NOI18N
    public static Character CHARACTER_PIPE = '|'; //NOI18N
    public static Character CHARACTER_TILDE = '~'; //NOI18N

    public String getDisplayName() {
        return "CSV - Plain text";
    }

    public String getExtension() {
        return ".csv";
    }

    public boolean export(Object[][] results, String fileName) {
        if (separator == null)
            separator = CHARACTER_COMMA;

        File file = new File(fileName);
        try {
            FileWriter writer = new FileWriter(file);
            for (Object[] record : results){
                String currentRecord="";
                for (Object column : record)
                    currentRecord += (column==null ? "" : column)+separator.toString(); //NOI18N
                currentRecord = currentRecord.substring(0,currentRecord.length() - 1);
                writer.write(currentRecord);
                writer.write('\n');
            }
            writer.flush();
            writer.close();
            return true;
        } catch(IOException ex){
            //TODO: Put some log4j stuff here
            return false;
        }
    }

    public Character getSeparator() {
        return separator;
    }

    public void setSeparator(Character separator) {
        this.separator = separator;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
