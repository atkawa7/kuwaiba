/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
 *
 */
package org.inventory.communications.util;

import javax.xml.bind.DatatypeConverter;

/**
 * A local representation of an file like an array of bytes
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Binary {
    private String fileName;
    private String fileExtension;
    private byte [] byteArray;
        
    public Binary(String string) {
        inicializarParameters(string);
    }
    
    private void inicializarParameters(String string) {
        String [] stringArray = string.split(";/;");
        if (stringArray.length == 3) {
            fileName = stringArray[0];
            fileExtension = stringArray[1];
            
            String fileEncoded = stringArray[2];        
            
            byteArray = DatatypeConverter.parseBase64Binary(fileEncoded);
        }
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public String getFileExtension() {
        return fileExtension;
    }
    
    public byte [] getByteArray() {
        return byteArray;
    }
}
