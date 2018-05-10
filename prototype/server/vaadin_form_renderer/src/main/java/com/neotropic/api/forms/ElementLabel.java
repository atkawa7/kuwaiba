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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ElementLabel extends AbstractElementField {
    
    public ElementLabel() {     
        setCleanable(false);
    }
    
    @Override
    public String getValue() {
        return (String) super.getValue();
    }
                
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        super.initFromXML(reader);
        
        String strValue = getValue();
                
        if (strValue != null) {
            strValue = strValue.replace("$lt.", "<");
            strValue = strValue.replace("$gt.", ">");
            strValue = strValue.replace("$qm.", "\"");
            
            setValue(strValue);
        }
    }
    
    @Override
    public String getTagName() {
        return Constants.Tag.LABEL;
    }
    
}
