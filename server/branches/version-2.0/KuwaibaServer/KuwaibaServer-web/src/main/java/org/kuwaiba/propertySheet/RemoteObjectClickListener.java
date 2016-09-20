/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.propertySheet;

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Form;
import org.kuwaiba.beans.WebserviceBeanRemote;
import org.kuwaiba.explorer.NodeButton;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;

/**
 *
 * @author johnyortega
 */
public class RemoteObjectClickListener implements ClickListener {
    private static final long serialVersionUID = 1L;
    private final GoogleMap googleMap;
        
    public RemoteObjectClickListener(GoogleMap googleMap) {
        this.googleMap = googleMap;                
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        ///TODO: add marker when press the button with the name of the remote object
        RemoteObject remoteObject = ((NodeButton) event.getSource()).getRemoteObject();
        ClassInfo classInfo = ((NodeButton) event.getSource()).getClassInfo();
        Form form = ((NodeButton) event.getSource()).getForm();
        WebserviceBeanRemote wsBean = ((NodeButton) event.getSource()).getWsBean();
        String sessionId = ((NodeButton) event.getSource()).getSessionId();
        String ipAddress = ((NodeButton) event.getSource()).getIpAddress();
            
        String name = "";
        for (int i = 0; i < remoteObject.getAttributes().length; i++){
            if (remoteObject.getAttributes()[i].equals("name")) {
                name = remoteObject.getValues()[i][0];
            }
        }
        name += " [" + remoteObject.getClassName() + "]";
        
        System.out.print("RemoteObjectClickListener.buttonClick ");
        //googleMap.addMarker(new GoogleMapMarker(name, new LatLon(2.4449, -76.6148), true, "VAADIN/img/building_32.png"));
        googleMap.addMarker(new NodeGoogleMapMarker(remoteObject, classInfo, wsBean, sessionId, ipAddress, form, name));
    }
    
}
