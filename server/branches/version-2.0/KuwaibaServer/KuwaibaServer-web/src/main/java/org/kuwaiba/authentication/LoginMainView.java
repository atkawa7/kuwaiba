/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.authentication;

import com.mycompany.map.events.AddMarkerOnMapClickListener;
import com.mycompany.map.events.MoveMarkerOnMarkerDragListener;
import com.mycompany.map.events.OpenInfoWindowsOnMarkerClickListener;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.kuwaiba.beans.WebserviceBeanRemote;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.explorer.NodeButton;
import org.kuwaiba.propertySheet.RemoteObjectClickListener;
import org.kuwaiba.propertySheet.events.DateValueChangeListener;
import org.kuwaiba.propertySheet.events.StringValueChangeListener;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;

/**
 *
 * @author johnyortega
 */
//@CDIView("")
public class LoginMainView extends CustomComponent implements View {
    private final String APIKey = "AIzaSyAjL_-MSWR1yTlP6Zi02hkXtvI_ZhY-Sck";
    private List<GoogleMapPolyline> edges =  new ArrayList();
    
    private final GoogleMap googleMap = new GoogleMap(APIKey, null, "english");
    //@EJB
//    @Inject
    private WebserviceBeanRemote wsBean = lookupWebserviceBeanRemoteBean();
    
    NodeButton nodeButton1283;
    NodeButton nodeButton1292;
    //List<NodeButton> nodeButtons = new ArrayList<>();
    
    public static final String NAME = "";
    Label text = new Label();
    Form form = new Form();
    FieldGroup fieldGroup = new FieldGroup();
    Button logout = new Button("Logout", new Button.ClickListener() {
        
        @Override
        public void buttonClick(Button.ClickEvent event) {
            try {
                String ipAddress = String.valueOf(getSession().getAttribute("ipAddress"));
                String sessionId = String.valueOf(getSession().getAttribute("sessionId"));
                                
                wsBean.closeSession(sessionId, ipAddress);
            } catch(Exception e) {
                if (e instanceof ServerSideException) {
                    try {
                        throw e;
                    } catch (Exception ex) {
                        Logger.getLogger(LoginMainView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                /*
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in closeSession: " + e.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
                */
            }
            getSession().setAttribute("username", null);
            getSession().setAttribute("sessionId", null);
            getSession().setAttribute("ipAddress", null);
            //getSession().setAttribute(RemoteSession.class, null);
            
            getUI().getNavigator().navigateTo(LoginView.NAME);
        }
    });
    
    public LoginMainView() {
        setCompositionRoot(new CssLayout(text, form, googleMap, logout));
    }
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        googleMap.setCenter(new LatLon(2.4448, -76.6147));
        googleMap.setZoom(17);
        googleMap.setHeight("500px");
        googleMap.setWidth("650px");
        
        AddMarkerOnMapClickListener addMarker = new AddMarkerOnMapClickListener(googleMap);
        googleMap.addMapClickListener(addMarker);
        OpenInfoWindowsOnMarkerClickListener openInfoWindow = new OpenInfoWindowsOnMarkerClickListener(googleMap, edges);
        googleMap.addMarkerClickListener(openInfoWindow);
        MoveMarkerOnMarkerDragListener moveMarker = new MoveMarkerOnMarkerDragListener(googleMap, edges);
        googleMap.addMarkerDragListener(moveMarker);
        
        String username = String.valueOf(getSession().getAttribute("username"));
        String sessionId = String.valueOf(getSession().getAttribute("sessionId"));
        String ipAddress = String.valueOf(getSession().getAttribute("ipAddress"));
        
        text.setValue("user name = " + username + " session id = " + sessionId + " ip address = " + ipAddress);
        
        try {
            ClassInfo classInfoBuilding = wsBean.getClass("Building", ipAddress, sessionId);
            
            RemoteObject remoteObject1283 = wsBean.getObject("Building", Long.valueOf("1283"), ipAddress, sessionId);
            
            String name = "";
            for (int i = 0; i < remoteObject1283.getAttributes().length; i++){
                if (remoteObject1283.getAttributes()[i].equals("name")) {
                    name = remoteObject1283.getValues()[i][0];
                }
            }
            name += " [" + remoteObject1283.getClassName() + "]";
            nodeButton1283 = new NodeButton(remoteObject1283, classInfoBuilding, form, wsBean, sessionId, ipAddress, name, new RemoteObjectClickListener(googleMap));
            
            RemoteObject remoteObject1292 = wsBean.getObject("Building", Long.valueOf("1292"), ipAddress, sessionId);
            name = "";
            for (int i = 0; i < remoteObject1292.getAttributes().length; i++){
                if (remoteObject1292.getAttributes()[i].equals("name")) {
                    name = remoteObject1292.getValues()[i][0];
                }
            }
            name += " [" + remoteObject1292.getClassName() + "]";
            nodeButton1292 = new NodeButton(remoteObject1292, classInfoBuilding, form, wsBean, sessionId, ipAddress, name, new RemoteObjectClickListener(googleMap));
            
            setCompositionRoot(new CssLayout(text, nodeButton1283, nodeButton1292, form, googleMap, logout));
            
            RemoteObject remoteObject = wsBean.getObject("City", Long.valueOf("1280"), ipAddress, sessionId);
            ClassInfo classInfo = wsBean.getClass("City", ipAddress, sessionId);
            
//            RemoteObject remoteObject = wsBean.getObject("Rack", Long.valueOf("1295"), ipAddress, sessionId);
//            ClassInfo classInfo = wsBean.getClass("Rack", ipAddress, sessionId);
            
            PropertysetItem item = new PropertysetItem();
            /*
            item.addPropertySetChangeListener(new Item.PropertySetChangeListener() {

                @Override
                public void itemPropertySetChange(Item.PropertySetChangeEvent event) {
                    System.out.println("Inside change property");
                }
            });
            */
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
                i += 1;
                
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
                //
                ObjectProperty<List<String>> attributeList = new ObjectProperty<>(new ArrayList<String>());
                item.addItemProperty(attributeName, attributeList);
                try {
                    RemoteObjectLight[] list = wsBean.getListTypeItems(attributeType, ipAddress, sessionId);                                        
                    List<String> elements = new ArrayList();
                    elements.add("");
                    for (RemoteObjectLight element : list)
                        elements.add(element.getName());
                    item.getItemProperty(attributeName).setValue(elements);
                } catch (ServerSideException ex) {
                    Logger.getLogger(OpenInfoWindowsOnMarkerClickListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        
            i = 0;
            for (String attribute : remoteObject.getAttributes()) {
                
                if (item.getItemProperty(attribute).getValue() instanceof Date) {
                    item.getItemProperty(attribute).setValue(new Date(Long.valueOf(values.get(i))));
                    i += 1;
                    continue;
                }
                if (item.getItemProperty(attribute).getValue() instanceof Boolean) {
                    item.getItemProperty(attribute).setValue(Boolean.valueOf(values.get(i)));
                    i += 1;
                    continue;
                }
                if (item.getItemProperty(attribute).getValue() instanceof String) {
                    item.getItemProperty(attribute).setValue(values.get(i));
                    i += 1;
                    continue;
                }
                if (item.getItemProperty(attribute).getValue() instanceof Float) {
                    item.getItemProperty(attribute).setValue(Float.valueOf(values.get(i)));
                    i += 1;
                    continue;
                }
                if (item.getItemProperty(attribute).getValue() instanceof Integer) {
                    item.getItemProperty(attribute).setValue(Integer.valueOf(values.get(i)));
                    i += 1;
                    continue;
                }
                ((List) item.getItemProperty(attribute).getValue()).set(0, values.get(i));
                i += 1;
            }
            form.setItemDataSource(item);
            
        } catch (ServerSideException ex) {
            Logger.getLogger(LoginMainView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private WebserviceBeanRemote lookupWebserviceBeanRemoteBean() {
        try {
            Context c = new InitialContext();
            return (WebserviceBeanRemote) c.lookup("java:global/KuwaibaServer-ear-1.0.0/KuwaibaServer-ejb-1.0.0/WebserviceBean!org.kuwaiba.beans.WebserviceBeanRemote");
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }
}