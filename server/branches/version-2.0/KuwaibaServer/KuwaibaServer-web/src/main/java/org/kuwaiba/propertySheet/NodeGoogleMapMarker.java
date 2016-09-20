/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.propertySheet;

import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.Form;
import org.kuwaiba.beans.WebserviceBeanRemote;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;

/**
 *
 * @author johnyortega
 */
public class NodeGoogleMapMarker extends GoogleMapMarker {
    private RemoteObject remoteObject;
    private ClassInfo classInfo;
    private Form form;
    private WebserviceBeanRemote wsBean;
    private String sessionId;
    private String ipAddress;
    
    public NodeGoogleMapMarker(RemoteObject remoteObject, ClassInfo classInfo, WebserviceBeanRemote wsBean, String sessionId, String ipAddress, Form form, String name) {
        super(name, new LatLon(2.4449, -76.6148), true, "VAADIN/img/building_32.png");
        this.remoteObject = remoteObject;
        this.classInfo = classInfo;
        this.form = form;
        this.wsBean = wsBean;
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
    }
    public RemoteObject getRemoteObject() {
        return remoteObject;
    }
    
    public ClassInfo getClassInfo() {
        return classInfo;
    }
    
    public Form getForm() {
        return form;
    }
    
    public WebserviceBeanRemote getWsBean() {
        return wsBean;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
