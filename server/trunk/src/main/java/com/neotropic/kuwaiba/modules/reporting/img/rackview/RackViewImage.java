/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteAttributeMetadata;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.openide.util.Exceptions;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RackViewImage {
    private static RackViewImage instance;
    private WebserviceBean webserviceBean;
    private RemoteSession remoteSession;
    private String ipAddress;
    
    private RackViewImage() {
    }
    
    public static RackViewImage getInstance() {
        return instance == null ? instance = new RackViewImage() : instance;
    }
    
    protected final WebserviceBean getWebserviceBean() {
        return webserviceBean;
    }
    
    public void setWebserviceBean(WebserviceBean webserviceBean) {
        this.webserviceBean = webserviceBean;                
    }    
    
    protected final RemoteSession getRemoteSession() {
        return remoteSession;        
    }
    
    public void setRemoteSession(RemoteSession remoteSession) {
        this.remoteSession = remoteSession;
    }
    
    protected final String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public static boolean stringArrayhasValue(String[] stringArray, String value) {
        return stringArrayIndexOfValue(stringArray, value) != -1;
    }
    
    public static int stringArrayIndexOfValue(String[] stringArray, String value) {
        if (stringArray == null || value == null)
            return -1;
        
        for (int i = 0; i < stringArray.length; i+= 1) {
            
            if (stringArray[i].equals(value))
                return i;
        }
        return -1;
    }
    
    public static boolean classMayHaveDeviceLayout(String className) {
        try {
            boolean hasAttribute = RackViewImage.getInstance().getWebserviceBean().hasAttribute(
                    className,
                    "model", //NOI18N
                    RackViewImage.getInstance().getIpAddress(),
                    RackViewImage.getInstance().getRemoteSession().getSessionId());
            
            if (hasAttribute) {
                RemoteAttributeMetadata remoteAttributeMetadata = RackViewImage.getInstance().getWebserviceBean().getAttribute(
                    className, 
                    "model", //NOI18N
                    RackViewImage.getInstance().getIpAddress(), 
                    RackViewImage.getInstance().getRemoteSession().getSessionId());
                
                boolean isSubclassOf = RackViewImage.getInstance().getWebserviceBean().isSubclassOf(
                    remoteAttributeMetadata.getType(), 
                    "GenericObjectList", //NOI18N
                    RackViewImage.getInstance().getIpAddress(), 
                    RackViewImage.getInstance().getRemoteSession().getSessionId());
                
                return isSubclassOf;
            }
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }
    
    public static RemoteObject getListTypeItemAttributeValue(String objectClass, String objectId, String attributeName) {
        
        try {
            RemoteObject remoteObject = RackViewImage.getInstance().getWebserviceBean().getObject(
                objectClass,
                objectId, 
                RackViewImage.getInstance().getIpAddress(), 
                RackViewImage.getInstance().getRemoteSession().getSessionId());
            
            RemoteClassMetadata remoteClassMetadata = RackViewImage.getInstance().getWebserviceBean().getClass(
                objectClass, 
                RackViewImage.getInstance().getIpAddress(), 
                RackViewImage.getInstance().getRemoteSession().getSessionId());
                        
            String attributeValue = remoteObject.getAttribute(attributeName);
                        
            int index = RackViewImage.stringArrayIndexOfValue(remoteClassMetadata.getAttributesNames(), attributeName);
            
            String attributeType = remoteClassMetadata.getAttributesTypes()[index];
            
            if (attributeValue != null) {
                                    
            RemoteObject listTypeItem = RackViewImage.getInstance().getWebserviceBean().getObject(
                attributeType, 
                attributeValue, 
                RackViewImage.getInstance().getIpAddress(), 
                RackViewImage.getInstance().getRemoteSession().getSessionId());
            
                return listTypeItem;
            }
            
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    public List<RemoteObject> getDevices(RemoteObject rack) {
        String message = "";
        RemoteClassMetadata classCustomShape = null;

        try {
            classCustomShape = getWebserviceBean().getClass("CustomShape", getIpAddress(), getRemoteSession().getSessionId());
        } catch (ServerSideException ex) {
            //Exceptions.printStackTrace(ex);
        }
        
        if (classCustomShape == null) {
            Notification.show("This database seems outdated. Contact your administrator to apply the necessary patches to add the CustomShape class", 
                Notification.Type.ERROR_MESSAGE);
            return null;
        }
                
        if (rack == null) {
            
        } else {
            Integer rackUnits = null;
            try {
                rackUnits = Integer.valueOf(rack.getAttribute("rackUnits")); //NOI18N
            } catch(NumberFormatException numberFormatException) {
            }
            if (rackUnits == null || rackUnits == 0) {
                message += String.format("Attribute %s in rack %s does not exist or is not set correctly\n", "rackUnits", rack); //NOI18N                                                        
            } else {
                List<RemoteObjectLight> devicesLight = null;
                try {
                    devicesLight = getWebserviceBean().getObjectChildren(rack.getClassName(), rack.getId(), 0, getIpAddress(), getRemoteSession().getSessionId());
                } catch (ServerSideException ex) {
                    Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                    //Exceptions.printStackTrace(ex);
                }
                if (devicesLight != null) {
                    List<RemoteObject> devices = new ArrayList<>();
                    
                    for (RemoteObjectLight deviceLight : devicesLight) {
                        RemoteObject device = null;
                        try {
                            device = getWebserviceBean().getObject(deviceLight.getClassName(), deviceLight.getId(), getIpAddress(), getRemoteSession().getSessionId());
                        } catch (ServerSideException ex) {
                            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                            //Exceptions.printStackTrace(ex);
                        }
                        
                        if (device != null) {
                            RemoteClassMetadata lcm = null;
                            try {
                                lcm = getWebserviceBean().getClass(deviceLight.getClassName(), getIpAddress(), getRemoteSession().getSessionId());
                            } catch (ServerSideException ex) {
                                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                                //Exceptions.printStackTrace(ex);
                            }
                            
                            if (lcm == null) {
                                return null;
                            }
                            
                            if (!stringArrayhasValue(lcm.getAttributesNames(), "position")) //NOI18N
                                message += String.format("The %s attribute does not exist in class %s\n", "position", lcm.toString());
                            else {
                                int index = stringArrayIndexOfValue(lcm.getAttributesNames(), "position"); //NOI18N
                                                                
                                if (index != -1 && !"Integer".equals(lcm.getAttributesTypes()[index])) //NOI18N
                                    message += String.format("The %s attribute type in class %s must be an Integer\n", "position", lcm.toString());
                            }
                            if (!stringArrayhasValue(lcm.getAttributesNames(), "rackUnits")) //NOI18N
                                message += String.format("The %s attribute does not exist in class %s\n", "rackUnits", lcm.toString());
                            else {
                                int index = stringArrayIndexOfValue(lcm.getAttributesNames(), "rackUnits"); //NOI18N
                                
                                if (index != -1 && !"Integer".equals(lcm.getAttributesTypes()[index])) //NOI18N
                                    message += String.format("The %s attribute type in class %s must be an Integer\n", "rackUnits", lcm.toString());
                            }
                            if (!message.isEmpty())
                                break;
                            
                            devices.add(device);

                            int devicePosition = device.getAttribute("position") != null ? Integer.valueOf(device.getAttribute("position")) : 0; //NOI18N
                            if (devicePosition < 0)
                                message += String.format("The %s in %s must be greater than or equal to zero\n", "position", device.toString());
                            else {
                                if (devicePosition > rackUnits)
                                    message += String.format("The %s in %s is greater than the number of rack units\n", "position", device.toString());
                            }
                            int deviceRackUnits = device.getAttribute("rackUnits") != null ? Integer.valueOf(device.getAttribute("rackUnits")) : 0;

                            if (deviceRackUnits < 0)
                                message += String.format("The %s in %s must be greater than or equal to zero\n", "rackUnits", device.toString());
                            else {
                                if (deviceRackUnits > rackUnits)
                                    message += String.format("The %s in %s is greater than the number of rack units\n", "rackUnits", device.toString());
                            }
                        } else {
                            return null;
                        }
                    }
                    if (message.isEmpty()) {
                        HashMap<Integer, RemoteObjectLight> rackUnitsMap = new HashMap();
                        
                        for (RemoteObject device : devices) {
                            try {
                                int devicePosition = Integer.valueOf(device.getAttribute("position"));
                                int deviceRackUnits = Integer.valueOf(device.getAttribute("rackUnits"));

                                for (int i = devicePosition; i < devicePosition + deviceRackUnits; i += 1) {

                                    if (!rackUnitsMap.containsKey(devicePosition))
                                        rackUnitsMap.put(i, device);
                                    else {
                                        RemoteObjectLight lol = rackUnitsMap.get(devicePosition);

                                        if (!lol.equals(device))
                                            message += String.format("The Position %s set in %s is used by the %s\n", i, device, lol);
                                    }
                                }
                            } catch(NumberFormatException ex) {
                                message += String.format("Device %s [%s] position or rackUnits not set", device.getName(), device.getId());
                            }
                        }
                    }
                    if (message.isEmpty()) {
                        return devices;
                    }
                } else {
                    return null;
                }
            }
        }
        Notification.show(message, Notification.Type.ERROR_MESSAGE);
        return null;
    }
}
