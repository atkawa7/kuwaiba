/*
 * Copyright (c) 2018 johnyortega.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    johnyortega - initial API and implementation and/or initial documentation
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
 *
 * @author johnyortega
 */
public class DeviceLayoutStructrue { 
    private HashMap<LocalObjectListItem, LocalObjectView> structureRepository;
    
    public DeviceLayoutStructrue() {        
    }
    
    public HashMap<LocalObjectListItem, LocalObjectView> getStructureRepository() {
        return structureRepository;
    }
    
    public HashMap<LocalObjectLight, List<LocalObjectLight>> getObjects(LocalObjectLight device) {
        byte[] structure = CommunicationsStub.getInstance().getDeviceLayoutStructure(device.getOid(), device.getClassName());
        
        if (structure == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return null;
        }
        try {
            HashMap<LocalObjectLight, Long> devices = new HashMap();
            
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
                        long id = Long.valueOf(xmlsr.getAttributeValue(null, Constants.PROPERTY_ID));
                        
                        if (id != device.getOid()) {
                            String className = xmlsr.getAttributeValue(null, Constants.PROPERTY_CLASSNAME);
                            String name = xmlsr.getAttributeValue(null, Constants.PROPERTY_NAME);
                            long parentId = Long.valueOf(xmlsr.getAttributeValue(null, "parentId")); //NOI18N
                            devices.put(new LocalObjectLight(id, name, className), parentId);
                        }
                        if (xmlsr.hasNext()) {
                            event = xmlsr.next();
                            
                            if (event == XMLStreamConstants.START_ELEMENT) {
                                if (xmlsr.getName().equals(tagModel)) {
                                    id = Long.valueOf(xmlsr.getAttributeValue(null, Constants.PROPERTY_ID));
                                    String className = xmlsr.getAttributeValue(null, Constants.PROPERTY_CLASSNAME);
                                    String name = xmlsr.getAttributeValue(null, Constants.PROPERTY_NAME);
                                    
                                    LocalObjectListItem modelObj = new LocalObjectListItem(id, className, name);
                                    
                                    if (xmlsr.hasNext()) {
                                        event = xmlsr.next();

                                        if (event == XMLStreamConstants.START_ELEMENT) {
                                            if (xmlsr.getName().equals(tagView)) {
                                                id = Long.valueOf(xmlsr.getAttributeValue(null, Constants.PROPERTY_ID));
                                                className = xmlsr.getAttributeValue(null, Constants.PROPERTY_CLASSNAME);
                                                
                                                if (xmlsr.hasNext()) {
                                                    event = xmlsr.next();
                                                    if (event == XMLStreamConstants.START_ELEMENT) {
                                                        if (xmlsr.getName().equals(tagStructure)) {
                                                            byte [] modelStructure = DatatypeConverter.parseBase64Binary(xmlsr.getElementText());                                                            
                                                                                                                    
                                                            LocalObjectView lov = new LocalObjectView(id, className, null, null, modelStructure, null);
                                                            
                                                            if (structureRepository == null)
                                                                structureRepository = new HashMap();
                                                            structureRepository.put(modelObj, lov);
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
            HashMap<LocalObjectLight, List<LocalObjectLight>> objects = new HashMap();
            
            objects.put(device, new ArrayList());
            for (LocalObjectLight child : devices.keySet())
                objects.put(child, new ArrayList());
            //
            LocalObjectLight dummyParent = new LocalObjectLight();
            
            for (LocalObjectLight child : devices.keySet()) {
                dummyParent.setOid(devices.get(child));
                
                objects.get(dummyParent).add(child);
            }
            
            xmlsr.close();
            return objects;
            
        } catch (XMLStreamException ex) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, "");
            return null;
        }
    }
    
    
}
