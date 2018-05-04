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

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
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
            QName tag = new QName(parent.getTagName());
            
            xmlew.add(xmlef.createStartElement(tag, null, null));
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.ID, parent.getId());
            
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
                if (element.getValue() instanceof RemoteObjectLight) {
                    RemoteObjectLight remoteObjectLight = (RemoteObjectLight) element.getValue();
                    
                    XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.OBJECT_ID, String.valueOf(remoteObjectLight.getOid()));
                }
            break;
        }
    }
    
}
