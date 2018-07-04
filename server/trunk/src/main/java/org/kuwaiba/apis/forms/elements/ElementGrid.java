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
package org.kuwaiba.apis.forms.elements;

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
    private List<ElementColumn> columns;
    private List<List<Object>> rows;
    private boolean shared = false;
        
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
    
    public boolean addRow(List<Object> row) {
        if (row == null)                
            return false;
        
        if (rows == null)
            rows = new ArrayList();
        
        rows.add(row);
        
        return true;
    }
    
    public void setRows(List<List<Object>> rows) {
        this.rows = rows;
    }
    
    public boolean isShared() {
        return shared;
    }
    
    public void setShared(boolean shared) {
        this.shared = shared;        
    }
    
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        super.initFromXML(reader);
        setShared(reader);
        
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
    
    private void setShared(XMLStreamReader reader) {
        shared = Boolean.valueOf(reader.getAttributeValue(null, Constants.Attribute.SHARED));
    }
    
    @Override
    public void onComponentEvent(EventDescriptor event) {
        super.onComponentEvent(event);        
    }
    
    @Override
    public String getTagName() {
        return Constants.Tag.GRID;       
    }
    
    @Override
    public void fireOnLoad() {
        super.fireOnLoad(); 
        
        if (hasProperty(Constants.EventAttribute.ONLOAD, Constants.Property.ROWS)) {
            
            List<String> list = getEvents().get(Constants.EventAttribute.ONLOAD).get(Constants.Property.ROWS);
            
            loadValue(list);
        }                        
    }
    
    private void loadValue(List<String> list) {
        if (list != null && !list.isEmpty()) {

            String functionName = list.get(0);

            Runner runner = getFormStructure().getElementScript().getFunctionByName(functionName);

            List parameters = new ArrayList();

            for (int i = 1; i < list.size(); i += 1) {
                AbstractElement anElement = getFormStructure().getElementById(list.get(i));
                parameters.add(anElement != null ? anElement : list.get(i));
            }

            Object newValue = runner.run(parameters);
            
            if (newValue != null) {
                
                setRows((List<List<Object>>) newValue);

                for (List<Object> row : getRows()) {

                    fireElementEvent(new EventDescriptor(
                        Constants.EventAttribute.ONPROPERTYCHANGE, 
                        Constants.Property.ROWS, 
                        row, 
                        null));
                }
            }
//            fireElementEvent(new EventDescriptor(
//                Constants.EventAttribute.ONPROPERTYCHANGE, 
//                Constants.Property.VALUE, newValue, null));
        }
    }
    
}
