/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.propertySheet.events;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.beans.WebserviceBeanRemote;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.ws.toserialize.business.RemoteObject;

/**
 *
 * @author johnyortega
 */
public class StringValueChangeListener implements ValueChangeListener {
    private String attributeName;
    private String sessionId;
    private String ipAddress;
    private RemoteObject remoteObject;
    private WebserviceBeanRemote wsBean;
    
    public StringValueChangeListener(String attributeName, RemoteObject remoteObject, WebserviceBeanRemote wsBean, String sessionId, String ipAddress) {
        this.attributeName = attributeName;
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
        this.remoteObject = remoteObject;
        this.wsBean = wsBean;
    }
    
    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        String value = (String) event.getProperty().getValue();
        
        String [][] atributeValues = new String[1][1];
        atributeValues[0] = new String[]{value};
        // Change this because when login modified the data base
        try {
            wsBean.updateObject(remoteObject.getClassName(), remoteObject.getOid(), new String[]{attributeName}, atributeValues, ipAddress, sessionId);
            remoteObject = wsBean.getObject(remoteObject.getClassName(), remoteObject.getOid(), ipAddress, sessionId);
        
        } catch (ServerSideException ex) {
            Logger.getLogger(StringValueChangeListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("StringValueChangeListener.valueChange");
    }
}
