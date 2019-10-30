/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.apis.forms.elements;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ElementComboBox extends AbstractElementSelector {
    private boolean sort = true;
    
    public ElementComboBox() {
    }
    
    public boolean getSort() {
        return sort;
    }
    
    @Override
    public String getTagName() {
        return Constants.Tag.COMBO_BOX;
    }
    
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        super.initFromXML(reader);
        setCaption(reader);
    }
    
    private void setCaption(XMLStreamReader reader) {
        sort = Boolean.valueOf(reader.getAttributeValue(null, Constants.Attribute.SORT));
    }
}
