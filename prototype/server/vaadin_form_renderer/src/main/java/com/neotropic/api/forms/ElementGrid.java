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

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ElementGrid extends AbstractElement {        
    List<ElementColumn> columns;
    List<List<Object>> rows;
        
    public ElementGrid() {
        
    }    
    
    public void setColumns(List<ElementColumn> columns) {
        this.columns = columns;        
    }
    
    public List<ElementColumn> getColums() {
        return columns;
    }
    
    public List<List<Object>> getRows() {
        return rows;        
    }
    
    public void setRows(List<List<Object>> rows) {
        this.rows = rows;
    }
    
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        super.initFromXML(reader);
        
        columns = new ArrayList();
        QName tagGrid = new QName(Constants.Tag.GRID);
        QName tagColumn = new QName(Constants.Tag.COLUMN);        
        
        while (true) {
            reader.nextTag();
                        
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                if (reader.getName().equals(tagColumn)) {
                    ElementColumn column = new ElementColumn();
                    column.initFromXML(reader);
                                        
                    columns.add(column);
                }
            }
            
            if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                
                if (reader.getName().equals(tagGrid))
                    return;
            }
        }
    }
    
    @Override
    public void onComponentEvent(EventDescriptor event) {
        super.onComponentEvent(event);        
    }
    
    @Override
    public String getTagName() {
        return Constants.Tag.GRID;       
    }
    
}
