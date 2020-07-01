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

/**
 * Class used to represent device information like the device layout, hierarchy 
 * and nested devices layouts
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeviceLayoutStructure { 
    private final HashMap<LocalObjectLight, List<LocalObjectLight>> hierarchy;
    private final HashMap<LocalObjectListItem, LocalObjectView> layouts;
    
    public DeviceLayoutStructure(LocalObjectLight device) {
        hierarchy = new HashMap();
        layouts = new HashMap();
        initDeviceLayoutStructure(device);
    }
    
    public void initDeviceLayoutStructure(LocalObjectLight device) {
        byte[] structure = CommunicationsStub.getInstance().getDeviceLayoutStructure(device.getId(), device.getClassName());
        
        if (structure == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
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
            HashMap<LocalObjectLight, String> devices = new HashMap();
            
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
                        String id = xmlsr.getAttributeValue(null, Constants.PROPERTY_ID);
                        
                        if (!id.equals(device.getId())) {
                            String className = xmlsr.getAttributeValue(null, Constants.PROPERTY_CLASSNAME);
                            String name = xmlsr.getAttributeValue(null, Constants.PROPERTY_NAME);
                            String parentId = xmlsr.getAttributeValue(null, "parentId"); //NOI18N
                            devices.put(new LocalObjectLight(id, name, className), parentId);
                        }
                        if (xmlsr.hasNext()) {
                            event = xmlsr.next();
                            
                            if (event == XMLStreamConstants.START_ELEMENT) {
                                if (xmlsr.getName().equals(tagModel)) {
                                    id = xmlsr.getAttributeValue(null, Constants.PROPERTY_ID);
                                    String className = xmlsr.getAttributeValue(null, Constants.PROPERTY_CLASSNAME);
                                    String name = xmlsr.getAttributeValue(null, Constants.PROPERTY_NAME);
                                    
                                    LocalObjectListItem modelObj = new LocalObjectListItem(id, className, name);
                                    
                                    if (xmlsr.hasNext()) {
                                        event = xmlsr.next();

                                        if (event == XMLStreamConstants.START_ELEMENT) {
                                            if (xmlsr.getName().equals(tagView)) {
                                                id = xmlsr.getAttributeValue(null, Constants.PROPERTY_ID);
                                                className = xmlsr.getAttributeValue(null, Constants.PROPERTY_CLASSNAME);
                                                
                                                if (xmlsr.hasNext()) {
                                                    event = xmlsr.next();
                                                    if (event == XMLStreamConstants.START_ELEMENT) {
                                                        if (xmlsr.getName().equals(tagStructure)) {
                                                            byte [] modelStructure = DatatypeConverter.parseBase64Binary(xmlsr.getElementText());                                                            
                                                            LocalObjectView lov = new LocalObjectView(Long.valueOf(id), className, null, null, modelStructure, null);
                                                            layouts.put(modelObj, lov);
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
            for (LocalObjectLight child : devices.keySet())
                hierarchy.put(child, new ArrayList());
            //
            LocalObjectLight dummyParent = new LocalObjectLight();
            
            for (LocalObjectLight child : devices.keySet()) {
                dummyParent.setOid(devices.get(child));
                
                for (LocalObjectLight aParent : hierarchy.keySet()) {
                    if (aParent.getId().equals(dummyParent.getId()))
                        hierarchy.get(aParent).add(child);
                }
            }
            
            xmlsr.close();
        } catch (XMLStreamException ex) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, "");
        }
    }
    
    public HashMap<LocalObjectListItem, LocalObjectView> getLayouts() {
        return layouts;
    }
    
    public HashMap<LocalObjectLight, List<LocalObjectLight>> getHierarchy() {
        return hierarchy;
    }
}
