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

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public abstract class AbstractFormInstanceLoader {
    private String formid;
    private HashMap<String, Object> values;
            
    public AbstractFormInstanceLoader() {
        values = new HashMap();
    }
    
    public abstract Object getRemoteObjectLight(long classId, long objectId);
    public abstract Object getClassInfoLight(long classId);
    
    private Object getValue(XMLStreamReader reader, String dataType) {
        switch (dataType) {
            case Constants.Attribute.DataType.CLASS_INFO_LIGTH:
                
                String classId = reader.getAttributeValue(null, Constants.Attribute.CLASS_ID);
                
                String className = reader.getAttributeValue(null, Constants.Attribute.CLASS_NAME);
                                
                if (classId != null) {
                    
                    Object cli = getClassInfoLight(Long.valueOf(classId));
                    
                    if (cli != null)
                        return cli;
                }
                if (className != null) {
                    //TODO: In the null case, load only the name, remenber make a instance of the expected data type
                }
            break;
            case Constants.Attribute.DataType.REMOTE_OBJECT_LIGTH:
                                
                String objectId = reader.getAttributeValue(null, Constants.Attribute.OBJECT_ID);
                classId = reader.getAttributeValue(null, Constants.Attribute.CLASS_ID);
                
                String objectName = reader.getAttributeValue(null, Constants.Attribute.OBJECT_NAME);
                
                if (objectId != null && classId != null) {
                    
                    Object rol = getRemoteObjectLight(Long.valueOf(classId), Long.valueOf(objectId));
                                        
                    if (rol != null)
                        return rol;
                }
                                
                if (objectName != null) {
                    //TODO: In the null case, load only the name, remenber make a instance of the expected data type
                }
            break;
            case Constants.Attribute.DataType.STRING:
                
                String value = reader.getAttributeValue(null, Constants.Attribute.VALUE);
                
                if (value != null)
                    return value;
            break;
            default:
                return null;
        }
        return null;
    }
        
    public FormDefinitionLoader load(byte[] definition, byte[] content) {

        try {
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(content);
            XMLStreamReader reader = xmlif.createXMLStreamReader(bais);
            
            while (reader.hasNext()) {
                int event = reader.next();
                
                if (event == XMLStreamConstants.START_ELEMENT) {
                                        
                    if (formid == null)
                        formid = reader.getAttributeValue(null, Constants.Attribute.FORM_ID);
                    
                    String id = reader.getAttributeValue(null, Constants.Attribute.ID);
                    if (id != null) {
                        String dataType = reader.getAttributeValue(null, Constants.Attribute.DATA_TYPE);
                        if (dataType != null) {
                            
                            Object value = getValue(reader, dataType);
                                                        
                            if (value != null)
                                values.put(id, value);
                        }
                    }
                }
            }
            reader.close();
                        
        } catch (XMLStreamException ex) {
            Logger.getLogger(AbstractFormInstanceLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (definition != null) {
            
            FormDefinitionLoader formLoader = new FormDefinitionLoader(definition);
            formLoader.build();

            FormStructure formStructure = formLoader.getRoot().getFormStructure();

            for (String id : values.keySet()) {
                
                AbstractElement element = formStructure.getElementById(id);
                
                if (element != null) {
                    if (element instanceof AbstractElementField) {
                        ((AbstractElementField) element).setValue(values.get(id));
                    }
                }
            }
            return formLoader;
        }
        return null;
    }    
}