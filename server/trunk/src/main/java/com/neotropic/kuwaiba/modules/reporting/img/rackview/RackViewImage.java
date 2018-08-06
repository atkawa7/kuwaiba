/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.modules.reporting.img.rackview;

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
 * @author johnyortega
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
    
    public static RemoteObject getListTypeItemAttributeValue(String objectClass, long objectId, String attributeName) {
        
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
                        
            RemoteObject listTypeItem = RackViewImage.getInstance().getWebserviceBean().getObject(
                attributeType, 
                Long.valueOf(attributeValue), 
                RackViewImage.getInstance().getIpAddress(), 
                RackViewImage.getInstance().getRemoteSession().getSessionId());
            
            return listTypeItem;
            
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
}
