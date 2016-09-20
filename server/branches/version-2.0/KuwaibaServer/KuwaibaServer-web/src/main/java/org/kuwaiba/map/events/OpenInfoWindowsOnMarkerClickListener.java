/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.map.events;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapInfoWindow;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.Notification;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.beans.WebserviceBeanRemote;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.propertySheet.NodeGoogleMapMarker;
import org.kuwaiba.propertySheet.events.DateValueChangeListener;
import org.kuwaiba.propertySheet.events.StringValueChangeListener;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;

/**
 *
 * @author johnyortega
 */
public class OpenInfoWindowsOnMarkerClickListener implements MarkerClickListener {
    GoogleMap googleMap;
    List<GoogleMapPolyline> edges;
    
    public OpenInfoWindowsOnMarkerClickListener(GoogleMap googleMap, List<GoogleMapPolyline> edges) {
        this.googleMap = googleMap;
        this.edges = edges;
    }
    
    @Override
    public void markerClicked(GoogleMapMarker clickedMarker) {
        if (clickedMarker instanceof NodeGoogleMapMarker) {
            System.out.println(((NodeGoogleMapMarker) clickedMarker).getRemoteObject().getOid());
            
            RemoteObject remoteObject = ((NodeGoogleMapMarker) clickedMarker).getRemoteObject();
            ClassInfo classInfo = ((NodeGoogleMapMarker) clickedMarker).getClassInfo();
            Form form = ((NodeGoogleMapMarker) clickedMarker).getForm();
            WebserviceBeanRemote wsBean = ((NodeGoogleMapMarker) clickedMarker).getWsBean();
            String sessionId = ((NodeGoogleMapMarker) clickedMarker).getSessionId();
            String ipAddress = ((NodeGoogleMapMarker) clickedMarker).getIpAddress();
            
            PropertysetItem item = new PropertysetItem();
                        List<String> values = new ArrayList<>();
            for (String[] value : remoteObject.getValues()) {
                if (value.length == 0)
                    values.add("<null value>");
                else
                    values.add(value[0]);
            }
            
            int i = 0;
            for (String attributeName : classInfo.getAttributeNames()) {
                String attributeType = classInfo.getAttributeTypes()[i];
                
                if (attributeType.equals("String")) {
                    ObjectProperty<String> attributeString = new ObjectProperty<>("");
                    attributeString.addValueChangeListener(new StringValueChangeListener(attributeName, remoteObject, wsBean, sessionId, ipAddress));
                    
                    item.addItemProperty(attributeName, attributeString);
                    continue;
                }
                if (attributeType.equals("Date")) {
                    ObjectProperty<Date> attributeDate = new ObjectProperty<>(new Date());
                    attributeDate.addValueChangeListener(new DateValueChangeListener(attributeName, remoteObject, wsBean, sessionId, ipAddress));
                    
                    item.addItemProperty(attributeName, attributeDate);
                    continue;
                }
                if (attributeType.equals("Boolean")) {
                    ObjectProperty<Boolean> attributeBoolean = new ObjectProperty<>(new Boolean(false));
                    
                    item.addItemProperty(attributeName, attributeBoolean);
                    continue;
                }
                if (attributeType.equals("Float")) {
                    ObjectProperty<Float> attributeFloat = new ObjectProperty<>(Float.valueOf("0.0"));
                    
                    item.addItemProperty(attributeName, attributeFloat);
                    continue;
                }
                if (attributeType.equals("Integer")) {
                    ObjectProperty<Integer> attributeInteger = new ObjectProperty<>(Integer.valueOf("0"));
                    
                    item.addItemProperty(attributeName, attributeInteger);
                    continue;
                }
                ObjectProperty<List<String>> attributeList = new ObjectProperty<>(new ArrayList<String>());
                item.addItemProperty(attributeName, attributeList);
                i += 1;
            }
            
        
            i = 0;
            for (String attribute : remoteObject.getAttributes()) {
                if (item.getItemProperty(attribute).getValue() instanceof Date) {
                    item.getItemProperty(attribute).setValue(new Date(Long.valueOf(values.get(i))));
                    continue;
                }
                if (item.getItemProperty(attribute).getValue() instanceof Boolean) {
                    item.getItemProperty(attribute).setValue(Boolean.valueOf(values.get(i)));
                    continue;
                }
                if (item.getItemProperty(attribute).getValue() instanceof String) {
                    item.getItemProperty(attribute).setValue(values.get(i));
                    continue;
                }
                if (item.getItemProperty(attribute).getValue() instanceof Float) {
                    item.getItemProperty(attribute).setValue(Float.valueOf(values.get(i)));
                    continue;
                }
                if (item.getItemProperty(attribute).getValue() instanceof Integer) {
                    item.getItemProperty(attribute).setValue(Integer.valueOf(values.get(i)));
                    continue;
                }
                
                try {
                    RemoteObjectLight[] list = wsBean.getListTypeItems(attribute, ipAddress, sessionId);
                    
                    List<String> elements = new ArrayList();
                    for (RemoteObjectLight element : list)
                        elements.add(element.getName());
                    item.getItemProperty(attribute).setValue(elements);
                } catch (ServerSideException ex) {
                    Logger.getLogger(OpenInfoWindowsOnMarkerClickListener.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                i += 1;
            }
            form.setItemDataSource(item);
        }
        /*
        GoogleMapInfoWindow clicketMarkerWindow = 
                new GoogleMapInfoWindow(clickedMarker.getCaption(), clickedMarker);
        googleMap.setInfoWindowContents(clicketMarkerWindow, new Button("Add Connection", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                //Notification.show("hello there!");
                
                ArrayList<LatLon> points = new ArrayList();
                for(GoogleMapMarker marker : googleMap.getMarkers())
                    points.add(marker.getPosition());
                
                GoogleMapPolyline edge = new GoogleMapPolyline(points, "#d31717", 0.8, 10);
                googleMap.addPolyline(edge);
                edges.add(edge);
            }
        }));
        googleMap.openInfoWindow(clicketMarkerWindow);
        */
    }
    
}
