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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class XMLUtil {
    private static XMLUtil instance;
    
    private XMLUtil() {
    }
    
    public static XMLUtil getInstance() {
        return instance == null ? instance = new XMLUtil() : instance;
    }
    
    public boolean createAttribute(XMLEventWriter xmlew, XMLEventFactory xmlef, String attrName, String attrValue) throws XMLStreamException {
        if (xmlew == null || xmlef == null || attrName == null || attrValue == null)
            return false;
        
        xmlew.add(xmlef.createAttribute(new QName(attrName), attrValue));
                
        return true;
    }
            
}
