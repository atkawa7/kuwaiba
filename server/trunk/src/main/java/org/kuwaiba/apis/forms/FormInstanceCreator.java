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
package org.kuwaiba.apis.forms;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import org.kuwaiba.apis.forms.elements.AbstractElementField;
import org.kuwaiba.apis.forms.elements.AbstractFormInstanceCreator;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.FileInformation;
import org.kuwaiba.apis.forms.elements.FormStructure;
import org.kuwaiba.apis.forms.elements.XMLUtil;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.beans.WebserviceBean;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FormInstanceCreator extends AbstractFormInstanceCreator {
    private final WebserviceBean wsBean;
    private final RemoteSession session;

    public FormInstanceCreator(FormStructure formStructure, WebserviceBean wsBean, RemoteSession session) {
        super(formStructure);
        this.wsBean = wsBean;
        this.session = session;
    }
    
    @Override
    public void addGridRow(XMLEventWriter xmlew, XMLEventFactory xmlef, List<Object> row) throws XMLStreamException {
        QName tagRow = new QName(Constants.Tag.ROW);
        QName tagData = new QName(Constants.Tag.DATA);
                
        xmlew.add(xmlef.createStartElement(tagRow, null, null));
                        
        for (Object data : row) {
            
            xmlew.add(xmlef.createStartElement(tagData, null, null));
            
            if (data instanceof RemoteObjectLight) {
                RemoteObjectLight remoteObjectLight = (RemoteObjectLight) data;

                try {
                    RemoteClassMetadata classInfo = wsBean.getClass(remoteObjectLight.getClassName(), session.getIpAddress(), session.getSessionId());
                    
                    XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.REMOTE_OBJECT_LIGTH);
                    XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.OBJECT_ID, String.valueOf(remoteObjectLight.getId()));
                    XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.OBJECT_NAME, remoteObjectLight.getName());
                    XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_ID, String.valueOf(classInfo.getId()));

                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getMessage());
                }
            }
            else if(data instanceof FileInformation) {
                
                FileInformation fileInfo = (FileInformation) data;
                
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.ATTACHMENT);
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.NAME, fileInfo.getName());
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.PATH, fileInfo.getPath());
                
            }
            else if (data instanceof String) {
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.STRING);
            }
            else if (data instanceof Integer) {
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.INTEGER);
            }
            else if (data instanceof LocalDate) {
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.DATE);
            }
            xmlew.add(xmlef.createCharacters(data == null ? "" : data.toString()));
            xmlew.add(xmlef.createEndElement(tagData, null));
        }        
        xmlew.add(xmlef.createEndElement(tagRow, null));
    }

    @Override
    protected void addRemoteObjectLight(XMLEventWriter xmlew, XMLEventFactory xmlef, Object object) throws XMLStreamException {
        if (object instanceof RemoteObjectLight) {
                        
            RemoteObjectLight remoteObjectLight = (RemoteObjectLight) object;
                        
            try {
                RemoteClassMetadata classInfo = wsBean.getClass(remoteObjectLight.getClassName(), session.getIpAddress(), session.getSessionId());
                
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.OBJECT_ID, String.valueOf(remoteObjectLight.getId()));
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.OBJECT_NAME, remoteObjectLight.getName());
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_ID, String.valueOf(classInfo.getId()));
                
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getMessage());
            }
        }
    }

    @Override
    protected void addClassInfoLight(XMLEventWriter xmlew, XMLEventFactory xmlef, AbstractElementField element) throws XMLStreamException {
        if (element.getValue() instanceof RemoteClassMetadataLight) {
            
            RemoteClassMetadataLight classInfoLight = (RemoteClassMetadataLight) element.getValue();
            
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_ID, String.valueOf(classInfoLight.getId()));
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_NAME, classInfoLight.getClassName());
        }
    }
    
    @Override
    protected void addAttachment(XMLEventWriter xmlew, XMLEventFactory xmlef, Object object) throws XMLStreamException {
        if (object instanceof FileInformation) {
            
            FileInformation fileInfo = (FileInformation) object;
            
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.NAME, fileInfo.getName());
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.PATH, fileInfo.getPath());
        }
    }
    
    @Override
    protected boolean isRemoteObjectLight(Object object) {
        return object instanceof RemoteObjectLight;
    }
    
    @Override
    protected boolean isAttachment(Object object) {
        return object instanceof FileInformation;
    }
    
    @Override
    protected HashMap<String, String> getRemoteObjectLightInformation(Object object) {
        try {
            
            if (object instanceof RemoteObjectLight) {
                
                RemoteObjectLight rol = (RemoteObjectLight) object;

                HashMap<String, String> info = new HashMap();
                
                RemoteClassMetadata classInfo = wsBean.getClass(rol.getClassName(), session.getIpAddress(), session.getSessionId());
                                
                info.put(Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.REMOTE_OBJECT_LIGTH);
                info.put(Constants.Attribute.OBJECT_NAME, rol.getName());
                info.put(Constants.Attribute.OBJECT_ID, String.valueOf(rol.getId()));
                info.put(Constants.Attribute.CLASS_ID, String.valueOf(classInfo.getId()));
                info.put(Constants.Attribute.CLASS_NAME, String.valueOf(classInfo.getClassName()));
                
                return info;            
            }
            return null;
        } catch (ServerSideException ex) {
            return null;
        }
    }
    
    @Override
    protected HashMap<String, String> getAttachmentInformation(Object object) {
        if (object instanceof FileInformation) {

            FileInformation fileInfo = (FileInformation) object;

            HashMap<String, String> info = new HashMap();

            info.put(Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.ATTACHMENT);
            info.put(Constants.Attribute.NAME, fileInfo.getName());
            info.put(Constants.Attribute.PATH, fileInfo.getPath());
            
            return info;            
        }
        return null;
    }
    
}
