/*
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
import org.kuwaiba.util.i18n.I18N;
import org.openide.util.Exceptions;

/**
 * Class used to represent device information like the device layout, hierarchy 
 * and nested devices layouts
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeviceLayoutStructure { 
    private final HashMap<RemoteObjectLight, List<RemoteObjectLight>> hierarchy;
    private final HashMap<RemoteObjectLight, RemoteViewObject> layouts;
    
    public DeviceLayoutStructure(RemoteObjectLight device) {
        hierarchy = new HashMap();
        layouts = new HashMap();
        initDeviceLayoutStructure(device);
    }
    
    public void initDeviceLayoutStructure(RemoteObjectLight device) {
        byte[] structure;
        try {
            structure = RackViewImage.getInstance().getWebserviceBean().getDeviceLayoutStructure(device.getId(), device.getClassName(),
                    RackViewImage.getInstance().getIpAddress(),
                    RackViewImage.getInstance().getRemoteSession().getSessionId());
        } catch (ServerSideException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            return;
        }
        
        if (structure == null) {
            return;
        }
        
          //<editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
//                             try {
//                                 FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/device_structure" + device.getId() + ".xml");
//                                 fos.write(structure);
//                                 fos.close();
//                             } catch(Exception e) {}
                     //</editor-fold>
        
        try {
            HashMap<RemoteObjectLight, String> devices = new HashMap();
            
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
                        String id = xmlsr.getAttributeValue(null, "id"); //NOI18N
                        
                        if (!id.equals(device.getId())) {
                            String className = xmlsr.getAttributeValue(null, "className"); //NOI18N
                            String name = xmlsr.getAttributeValue(null, "name"); //NOI18N
                            String parentId = xmlsr.getAttributeValue(null, "parentId"); //NOI18N
                            devices.put(new RemoteObjectLight(className, id, name), parentId);
                        }
                        if (xmlsr.hasNext()) {
                            event = xmlsr.next();
                            
                            if (event == XMLStreamConstants.START_ELEMENT) {
                                if (xmlsr.getName().equals(tagModel)) {
                                    id = xmlsr.getAttributeValue(null, "id"); //NOI18N
                                    String className = xmlsr.getAttributeValue(null, "className"); //NOI18N
                                    String name = xmlsr.getAttributeValue(null, "name"); //NOI18N
                                    
                                    RemoteObjectLight modelObj = new RemoteObjectLight(className, id, name);
                                    
                                    if (xmlsr.hasNext()) {
                                        event = xmlsr.next();

                                        if (event == XMLStreamConstants.START_ELEMENT) {
                                            if (xmlsr.getName().equals(tagView)) {
                                                long _id = Long.valueOf(xmlsr.getAttributeValue(null, "id"));
                                                className = xmlsr.getAttributeValue(null, "className"); //NOI18N
                                                
                                                if (xmlsr.hasNext()) {
                                                    event = xmlsr.next();
                                                    if (event == XMLStreamConstants.START_ELEMENT) {
                                                        if (xmlsr.getName().equals(tagStructure)) {
                                                            byte [] modelStructure = DatatypeConverter.parseBase64Binary(xmlsr.getElementText());                                                            
                                                            ViewObject viewObject = new ViewObject(_id, null, null, className);
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
                
                String parentId = devices.get(child);                
                if (parentId != null) {

                    List<RemoteObjectLight> lst = null;

                    for (RemoteObjectLight aParent : hierarchy.keySet()) {
                        if (aParent.getId() != null && aParent.getId().equals(parentId))
                            lst = hierarchy.get(aParent);
                    }
                                        
                    if (lst != null)
                        lst.add(child);
                    else {
                        int i = 0;
                    }
                } else {
                    int i = 0;
                }
            }
            
            xmlsr.close();
        } catch (XMLStreamException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }
    
    public HashMap<RemoteObjectLight, RemoteViewObject> getLayouts() {
        return layouts;
    }
    
    public HashMap<RemoteObjectLight, List<RemoteObjectLight>> getHierarchy() {
        return hierarchy;
    }
}
