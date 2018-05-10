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

import com.neotropic.forms.KuwaibaClient;
import com.neotropic.forms.Variable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.wsclient.ClassInfo;
import org.inventory.communications.wsclient.RemoteObjectLight;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FormInstanceLoader {
    private String formid;
    private HashMap<String, Object> values;
        
    public FormInstanceLoader() {
        values = new HashMap();
    }
    
    private Object getValue(XMLStreamReader reader, String dataType) {
        switch (dataType) {
            case Constants.Attribute.DataType.CLASS_INFO_LIGTH:
                
                String classId = reader.getAttributeValue(null, Constants.Attribute.CLASS_ID);
                
                ClassInfo classInfo = null;
                
                if (classId != null)
                    classInfo = KuwaibaClient.getInstance().getClass(Long.valueOf(classId));
                
                if (classInfo != null)
                    return classInfo;
                else {
                    
                    String className = reader.getAttributeValue(null, Constants.Attribute.CLASS_NAME);
                    
                    if (className != null) {
                    }
                }
            break;
            case Constants.Attribute.DataType.REMOTE_OBJECT_LIGTH:
                
                classId = null;
                classInfo = null;
                                
                String objectId = reader.getAttributeValue(null, Constants.Attribute.OBJECT_ID);
                classId = reader.getAttributeValue(null, Constants.Attribute.CLASS_ID);
                String objectName = reader.getAttributeValue(null, Constants.Attribute.OBJECT_NAME);
                
                if (objectId != null && classId != null) {
                    
                    classInfo = KuwaibaClient.getInstance().getClass(Long.valueOf(classId));
                                        
                    RemoteObjectLight rol = null;
                                        
                    if (classInfo != null)
                        rol = KuwaibaClient.getInstance().getObjectLight(classInfo.getClassName(), Long.valueOf(objectId));
                                        
                    if (rol != null)
                        return rol;
                }
                                
                if (objectName != null) {

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
        
    public FormLoader load(byte[] structure) {

        try {
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
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
            Logger.getLogger(FormInstanceLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        if (formid != null) {
            String filePath = Variable.FORM_RESOURCE_STRUCTURES + "/" + formid + ".xml";
            File file = new File(filePath);
            byte [] byteArray = XMLUtil.getFileAsByteArray(file);
            
            FormLoader formLoader = new FormLoader(byteArray);
            formLoader.build();
            
            FormStructure formStructure = formLoader.getRoot().getFormStructure();
            
            for (String iid : values.keySet()) {
                AbstractElement element = formStructure.getElementById(iid);
                if (element != null) {
                    if (element instanceof AbstractElementField) {
                        ((AbstractElementField) element).setValue(values.get(iid));
                    }
                }
            }
            return formLoader;
        }
        return null;
    }    
}
