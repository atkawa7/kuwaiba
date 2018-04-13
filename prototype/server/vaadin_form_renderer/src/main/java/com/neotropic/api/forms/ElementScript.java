/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
 */
package com.neotropic.api.forms;

import java.util.HashMap;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ElementScript implements Tag {
    private HashMap<String, String> functions;
        
    public ElementScript() {
    }
    
    public HashMap<String, String> getFunctions() {
        return functions;
    }
    
    public void setFunctions(HashMap<String, String> functions) {
        this.functions = functions;
    }
    
    @Override
    public void initFromXMl(XMLStreamReader reader) throws XMLStreamException {
        QName tagScript = new QName(Constants.Tag.SCRIPT);
        QName tagFunction = new QName(Constants.Tag.FUNCTION);
                
        functions = new HashMap();
        
        while (true) {
            reader.nextTag();
            
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                
                if (reader.getName().equals(tagFunction)) {
                    
                    String functionName = reader.getAttributeValue(null, Constants.Attribute.NAME);
                    String blockOfCode = reader.getElementText();
                    
                    if (blockOfCode != null) {
                        
                        if (functionName == null)
                            functionName = Constants.Function.GLOBAL;
                                                
                        if (functions.containsKey(functionName)) {
                            
                            blockOfCode = functions.get(functionName) + " " + blockOfCode;
                            
                            functions.put(functionName, blockOfCode);
                            
                        } else
                            functions.put(functionName, blockOfCode);
                    }
                }
            }
            if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                
                if (reader.getName().equals(tagScript))
                    return;
            }
        }
    }
}
