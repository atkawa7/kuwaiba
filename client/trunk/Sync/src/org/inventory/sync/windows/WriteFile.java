/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.inventory.sync.windows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Implements the logic necessary to export to CSV
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class WriteFile {

    public boolean writeLog(byte[] results, String fileName) throws IOException {
        FileOutputStream fileOuputStream = null;
        try{
            File file = new File(fileName);
            if(!file.exists())
                file.createNewFile();
            fileOuputStream = new FileOutputStream(file, false);
            fileOuputStream.write(results);
            fileOuputStream.flush();
            fileOuputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fileOuputStream != null)
                    fileOuputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }   
    
}

    

