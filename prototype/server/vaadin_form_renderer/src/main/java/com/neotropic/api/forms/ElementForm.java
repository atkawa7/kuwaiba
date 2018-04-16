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
public class ElementForm extends AbstractElementContainer {
    private String title;
    
    public ElementForm() {
    }
    
    public void setTitle(String title) {
        this.title = title;        
    }
    
    public String getTitle() {
        return title;
    }

    @Override
    public void initFromXMl(XMLStreamReader reader) throws XMLStreamException {
        // This variable is used to verify if the attribute exist and to avoid 
        // the exceptions that can be caused by the parse of null values
        String attrValue = reader.getAttributeValue(null, Constants.Attribute.TITLE);
        
        title = attrValue;
    }

////    @Override
////    public void componentChange(ChangeDescriptor changeDecriptor) {
////        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
////    }
    
}
