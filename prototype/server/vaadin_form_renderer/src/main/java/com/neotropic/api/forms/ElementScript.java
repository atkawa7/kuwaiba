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
    private HashMap<String, Runner> functions;
        
    public ElementScript() {
    }
    
    public HashMap<String, Runner> getFunctions() {
        if (functions == null)
            functions = new HashMap();
        
        return functions;
    }
    
    public void setFunctions(HashMap<String, Runner> functions) {
        this.functions = functions;
    }
    
    public Runner getFunctionByName(String name) {
        return functions != null ? functions.get(name) : null;
    }
    
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        QName tagScript = new QName(Constants.Tag.SCRIPT);
        QName tagFunction = new QName(Constants.Tag.FUNCTION);
                        
        functions = new HashMap();
        
        while (true) {
            reader.nextTag();
            
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                
                if (reader.getName().equals(tagFunction)) {
                    
                    String functionName = reader.getAttributeValue(null, Constants.Attribute.NAME);
                    String functionType = reader.getAttributeValue(null, Constants.Attribute.TYPE);
                    
                    if (functionName != null && functionType != null) {
                        
                        String parameterNames = reader.getAttributeValue(null, Constants.Attribute.PARAMETER_NAMES);
                        String queryName = reader.getAttributeValue(null, Constants.Attribute.QUERY_NAME);
                        String message = reader.getAttributeValue(null, Constants.Attribute.MESSAGE);
                        String blockOfCode = reader.getElementText();
                        
                        if (Constants.Function.Type.FUNCTION.equals(functionType)) {
                            if (blockOfCode != null)
                                functions.put(functionName, new Function(functionName, parameterNames, blockOfCode));
                        }
                        if (Constants.Function.Type.QUERY.equals(functionType)) {
                            if (queryName != null)
                                functions.put(functionName, new Query(functionName, queryName, parameterNames));
                        }
                        if (Constants.Function.Type.VALIDATOR.equals(functionType)) {
                            if (message != null)
                                functions.put(functionName, new Validator(functionName, parameterNames, blockOfCode, message));
                        }
                    }
                }
            }
            if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                
                if (reader.getName().equals(tagScript))
                    return;
            }
        }
    }
    
    @Override
    public String getTagName() {
        return Constants.Tag.SCRIPT;
    }
    
}
