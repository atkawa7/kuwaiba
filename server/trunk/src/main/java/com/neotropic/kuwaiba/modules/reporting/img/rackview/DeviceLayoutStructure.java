/**
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
 * 
 */
package com.neotropic.kuwaiba.modules.reporting.img.rackview;

import com.vaadin.ui.Notification;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.persistence.application.ViewObject;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
/*
package org.inventory.core.templates.layouts;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
*/
/**
 * Class used to storage device information like the device layout, hierarchy 
 * and nested devices layouts
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeviceLayoutStructure { 
    private final HashMap<RemoteObjectLight, List<RemoteObjectLight>> hierarchy;
    private final HashMap<RemoteObjectLight, RemoteViewObject> layouts;
    
    public DeviceLayoutStructure(RemoteObjectLight device) {
        hierarchy = new HashMap();
        layouts = new HashMap();
        initDeviceLayoutStructure(device);
    }
    
    public final void initDeviceLayoutStructure(RemoteObjectLight device) {
        byte[] structure = null;
                
        try {
            structure = RackViewImage.getInstance().getWebserviceBean().getDeviceLayoutStructure(
                device.getId(), 
                device.getClassName(), 
                RackViewImage.getInstance().getIpAddress(), 
                RackViewImage.getInstance().getRemoteSession().getSessionId());
            //PersistenceService.getInstance().getApplicationEntityManager().getDeviceLayoutStructure(device.getId(), device.getClassName());
        } catch (ServerSideException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            return;
            //Exceptions.printStackTrace(ex);
        }
        
////        byte[] structure = CommunicationsStub.getInstance().getDeviceLayoutStructure(device.getId(), device.getClassName());
        
        if (structure == null) {
////            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
////                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
        try {
            HashMap<RemoteObjectLight, Long> devices = new HashMap();
            
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            XMLStreamReader xmlsr = xmlif.createXMLStreamReader(bais);
            
            QName tagDevice = new QName("device"); //NOI18N
            QName tagModel = new QName("model"); //NOI18N
            QName tagView = new QName("view"); //NOI18N
            QName tagStructure = new QName("structure"); //NOI18N
            
            while (xmlsr.hasNext()) {
                int event = xmlsr.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (xmlsr.getName().equals(tagDevice)) {
                        long id = Long.valueOf(xmlsr.getAttributeValue(null, "id")); //NOI18N
                        
                        if (id != device.getId()) {
                            String className = xmlsr.getAttributeValue(null, "className"); //NOI18N
                            String name = xmlsr.getAttributeValue(null, "name"); //NOI18N
                            long parentId = Long.valueOf(xmlsr.getAttributeValue(null, "parentId")); //NOI18N
                            devices.put(new RemoteObjectLight(className, id, name), parentId);
                        }
                        if (xmlsr.hasNext()) {
                            event = xmlsr.next();
                            
                            if (event == XMLStreamConstants.START_ELEMENT) {
                                if (xmlsr.getName().equals(tagModel)) {
                                    id = Long.valueOf(xmlsr.getAttributeValue(null, "id")); //NOI18N
                                    String className = xmlsr.getAttributeValue(null, "className"); //NOI18N
                                    String name = xmlsr.getAttributeValue(null, "name"); //NOI18N
                                    
                                    RemoteObjectLight modelObj = new RemoteObjectLight(className, id, name);
                                    
                                    if (xmlsr.hasNext()) {
                                        event = xmlsr.next();

                                        if (event == XMLStreamConstants.START_ELEMENT) {
                                            if (xmlsr.getName().equals(tagView)) {
                                                id = Long.valueOf(xmlsr.getAttributeValue(null, ""));
                                                className = xmlsr.getAttributeValue(null, "className");//NOI18N
                                                
                                                if (xmlsr.hasNext()) {
                                                    event = xmlsr.next();
                                                    if (event == XMLStreamConstants.START_ELEMENT) {
                                                        if (xmlsr.getName().equals(tagStructure)) {
                                                            byte [] modelStructure = DatatypeConverter.parseBase64Binary(xmlsr.getElementText());                                                            
                                                            // (long id, String name, String description, String viewClassName)
                                                            ViewObject viewObject = new ViewObject(id, className, null, null);
                                                            viewObject.setStructure(modelStructure);
                                                                                                                                                                                    
                                                            RemoteViewObject remoteViewObject = new RemoteViewObject(viewObject);
                                                                                                                        
                                                            layouts.put(modelObj, remoteViewObject);
                                                        }
                                                    }                                                    
                                                    
                                                }                                                
                                            }
                                        }
                                    }
                                }
                            }                       
                        }
                    }
                }
            }            
            // 
            hierarchy.put(device, new ArrayList());
            for (RemoteObjectLight child : devices.keySet())
                hierarchy.put(child, new ArrayList());
            //
            
            for (RemoteObjectLight child : devices.keySet()) {
                
                RemoteObjectLight dummyParent = new RemoteObjectLight(null, devices.get(child), null);
                
                hierarchy.get(dummyParent).add(child);
            }
            
            xmlsr.close();
        } catch (XMLStreamException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
////            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
////                NotificationUtil.ERROR_MESSAGE, "");
        }
    }
    
    public HashMap<RemoteObjectLight, RemoteViewObject> getLayouts() {
        return layouts;
    }
    
    public HashMap<RemoteObjectLight, List<RemoteObjectLight>> getHierarchy() {
        return hierarchy;
    }
}

