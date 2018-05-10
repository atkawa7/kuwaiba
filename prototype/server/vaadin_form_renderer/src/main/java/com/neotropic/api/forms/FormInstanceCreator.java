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
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.inventory.communications.wsclient.ClassInfo;
import org.inventory.communications.wsclient.ClassInfoLight;
import org.inventory.communications.wsclient.RemoteObjectLight;

/**
 * Create an instance of a Form layout
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FormInstanceCreator {
    private final FormStructure formStructure;            
    
    public FormInstanceCreator(FormStructure formStructure) {
        this.formStructure = formStructure;
    }
        
    public byte[] getStructure() {
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            xmlew.add(xmlef.createStartElement(FormLoader.TAG_ROOT, null, null));
            
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.VERSION, formStructure.getVersion());
            
            getStructureRecursive(xmlew, xmlef, formStructure.getElements().get(0));
                        
            xmlew.add(xmlef.createEndElement(FormLoader.TAG_ROOT, null));
            
            xmlew.close();
            return baos.toByteArray();
            
        } catch (XMLStreamException ex) {
            Logger.getLogger(FormInstanceCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    private void getStructureRecursive(XMLEventWriter xmlew, XMLEventFactory xmlef, AbstractElement parent) throws XMLStreamException {
        if (parent != null) {
            String tagName = parent.getTagName();
            
            if (Constants.Tag.FORM.equals(tagName))
                tagName = Constants.Tag.FORM_INSTANCE;
                        
            QName tag = new QName(tagName);
                        
            xmlew.add(xmlef.createStartElement(tag, null, null));
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.ID, parent.getId());
            
            if (Constants.Tag.FORM_INSTANCE.equals(tagName))
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.FORM_ID, ((ElementForm) parent).getFormId());
            
            if (parent instanceof AbstractElementField) {
                                
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.DATA_TYPE, ((AbstractElementField) parent).getDataType());
                addTagAttributes(xmlew, xmlef, ((AbstractElementField) parent));
                
            } else if (parent instanceof AbstractElementContainer) {
                List<AbstractElement> children = ((AbstractElementContainer) parent).getChildren();

                if (children != null) {

                    for (AbstractElement child : children) {
                        getStructureRecursive(xmlew, xmlef, child);
                        
                    }
                }
            }
            xmlew.add(xmlef.createEndElement(tag, null));
        }
    }
    
    /**
     * Add a set of attributes based on a given data type
     */
    private void addTagAttributes(XMLEventWriter xmlew, XMLEventFactory xmlef, AbstractElementField element) throws XMLStreamException {
        if (element.getDataType() == null)
            return;
        
        switch(element.getDataType()) {
            case Constants.Attribute.DataType.REMOTE_OBJECT_LIGTH:
                addRemoteObjectLight(xmlew, xmlef, element);                
            break;
            case Constants.Attribute.DataType.CLASS_INFO_LIGTH:
                addClassInfoLight(xmlew, xmlef, element);
            break;
            case Constants.Attribute.DataType.STRING:
                if (element.getValue() instanceof String)
                    XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.VALUE, String.valueOf(element.getValue()));
            break;
            case Constants.Attribute.DataType.INTEGER:
                try {
                    
                    if (Integer.valueOf(String.valueOf(element.getValue())) instanceof Integer)                    
                        XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.VALUE, String.valueOf(element.getValue()));
                    
                } catch(NumberFormatException nfe) {
                    
                }
            break;
            case Constants.Attribute.DataType.DATE:
                if (element.getValue() instanceof LocalDate) {
                    LocalDate localDate = (LocalDate) element.getValue();
                                        
                    XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.VALUE, localDate.toString());
                }
            break;
        }
    }
    
    private void addRemoteObjectLight(XMLEventWriter xmlew, XMLEventFactory xmlef, AbstractElementField element) throws XMLStreamException {
        
        if (element.getValue() instanceof RemoteObjectLight) {
            
            RemoteObjectLight remoteObjectLight = (RemoteObjectLight) element.getValue();

            ClassInfo classInfo = KuwaibaClient.getInstance().getClass(remoteObjectLight.getClassName());
            
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.OBJECT_ID, String.valueOf(remoteObjectLight.getOid()));
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.OBJECT_NAME, remoteObjectLight.getName());
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_ID, String.valueOf(classInfo.getId()));
        }
    }
    
    private void addClassInfoLight(XMLEventWriter xmlew, XMLEventFactory xmlef, AbstractElementField element) throws XMLStreamException {
        if (element.getValue() instanceof ClassInfoLight) {
            
            ClassInfoLight classInfoLight = (ClassInfoLight) element.getValue();
            
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_ID, String.valueOf(classInfoLight.getId()));
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_NAME, classInfoLight.getClassName());
        }
    }
    
}
