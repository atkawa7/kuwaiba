package com.neotropic.vaadin14.component.googlemap;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Label;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.Tabs.SelectedChangeEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.PWA;
import elemental.json.Json;
import elemental.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
@Push
@Route
@PWA(name = "Project Base for Vaadin Flow with Spring", shortName = "Project Base")
public class MainView extends VerticalLayout {
    private final String API_KEY = "[API-KEY]";
    private final String CLIENT_ID = null;
    
    private final VerticalLayout verticalLayoutMain;
    private final Tabs tabs;
    private final HashMap<Tab, VerticalLayout> pages;
    private final Tab tabMapEvents;
    private final Tab tabMarkerEvents;
    private final Tab tabPolylineEvents;
    //<editor-fold desc="Mark Labels" defaultstate="collapsed">
    private final Label lblMarkerClick;
    private final Label lblMarkerDblClick;
    private final Label lblMarkerDragEnd;
    private final Label lblMarkerDragStart;
    private final Label lblMarkerMouseOut;
    private final Label lblMarkerMouseOver;
    private final Label lblMarkerRightClick;
    //</editor-fold>
    //<editor-fold desc="Polyline Labels" defaultstate="collapsed">
    private final Label lblPolylineClick;
    private final Label lblPolylineDblClick;
    private final Label lblPolylineMouseOut;
    private final Label lblPolylineMouseOver;
    private final Label lblPolylineRightClick;
    private final Label lblPolylinePathChanged;
    //</editor-fold>
    public MainView(@Autowired MessageBean bean) {
        setSizeFull();
        GoogleMap googleMap = new GoogleMap(API_KEY, CLIENT_ID);
        
        GoogleMapMarker googleMapMarker = new GoogleMapMarker(2.4574702, -76.6349535);
        googleMap.newMarker(googleMapMarker);
        googleMap.setMapTypeId(Constants.MapTypeId.HYBRID);
        
        JsonObject label = Json.createObject();
        label.put("color", "#305F72"); //NOI18N
        label.put("text", "Marker"); //NOI18N

        googleMapMarker.setLabel(label);
        googleMapMarker.setTitle("Marker"); //NOI18N
        
        setMarkerListeners(googleMap, googleMapMarker);
        
        GoogleMapPolyline googleMapPolyline = new GoogleMapPolyline();
        googleMapPolyline.setEditable(true);
        googleMapPolyline.setDraggable(true);
        googleMapPolyline.setStrokeColor("#32a852");
        List<LatLng> coordinates = new ArrayList();
        coordinates.add(new LatLng(2.4574702, -76.6349535));
        coordinates.add(new LatLng(2.3512629, -76.6915093));
        coordinates.add(new LatLng(2.260897, -76.7449569));
        coordinates.add(new LatLng(2.1185563, -76.9974436));
        coordinates.add(new LatLng(2.0693058, -77.0552842));
        googleMapPolyline.setPath(coordinates);
        
        googleMap.newPolyline(googleMapPolyline);
        
        setPolylineListener(googleMapPolyline);
                
        SplitLayout splitLayoutMain = new SplitLayout();
        splitLayoutMain.addToPrimary(googleMap);
        
        tabs = new Tabs();
        
        tabs.setOrientation(Tabs.Orientation.HORIZONTAL);
        
        tabMapEvents = new Tab("Map Events");
        tabMarkerEvents = new Tab("Marker Events");
        tabPolylineEvents = new Tab("Polyline Events");
        
        tabs.add(tabMapEvents);
        tabs.add(tabMarkerEvents);
        tabs.add(tabPolylineEvents);
        
        tabs.setSelectedTab(tabMapEvents);
        
        VerticalLayout verticalLayoutMapEvents = new VerticalLayout();
        
        Label lblMapClick = new Label("map-click (Go To Pasto)"); //NOI18N
        lblMapClick.setWidthFull();
        Label lblMapDblClick = new Label("map-dbl-click"); //NOI18N
        lblMapDblClick.setWidthFull();
        Label lblMapRightClick = new Label("map-right-click (New Marker)"); //NOI18N
        lblMapRightClick.setWidthFull();
        Label lblMapCenterChanged = new Label("map-center-changed"); //NOI18N
        lblMapCenterChanged.setWidthFull();
        Label lblMapMouseMove = new Label("map-mouse-move"); //NOI18N
        lblMapMouseMove.setWidthFull();
        Label lblMapMouseOut = new Label("map-mouse-out"); //NOI18N
        lblMapMouseOut.setWidthFull();
        Label lblMapMouseOver = new Label("map-mouse-over"); //NOI18N
        lblMapMouseOver.setWidthFull();
        Label lblZoomChanged = new Label("map-zoom-changed"); //NOI18N
        lblZoomChanged.setWidthFull();
        
        verticalLayoutMapEvents.add(lblMapClick);
        verticalLayoutMapEvents.add(lblMapDblClick);
        verticalLayoutMapEvents.add(lblMapRightClick);
        verticalLayoutMapEvents.add(lblMapCenterChanged);
        verticalLayoutMapEvents.add(lblMapMouseMove);
        verticalLayoutMapEvents.add(lblMapMouseOut);
        verticalLayoutMapEvents.add(lblMapMouseOver);
        verticalLayoutMapEvents.add(lblZoomChanged);
        
        VerticalLayout verticalLayoutMarkerEvents = new VerticalLayout();
        
        lblMarkerClick = new Label("marker-click (Move To Silvia)"); //NOI18N
        lblMarkerClick.setWidthFull();
        lblMarkerDblClick = new Label("marker-dbl-click (Remove Marker)"); //NOI18N
        lblMarkerDblClick.setWidthFull();
        lblMarkerDragEnd = new Label("marker-drag-end");
        lblMarkerDragEnd.setWidthFull();
        lblMarkerDragStart = new Label("marker-drag-start");
        lblMarkerDragStart.setWidthFull();
        lblMarkerMouseOut = new Label("marker-mouse-out");
        lblMarkerMouseOut.setWidthFull();
        lblMarkerMouseOver = new Label("marker-mouse-over");
        lblMarkerMouseOver.setWidthFull();
        lblMarkerRightClick = new Label("marker-right-click"); //NOI18N
        lblMarkerRightClick.setWidthFull();
        
        verticalLayoutMarkerEvents.add(lblMarkerClick);
        verticalLayoutMarkerEvents.add(lblMarkerDblClick);
        verticalLayoutMarkerEvents.add(lblMarkerDragEnd);
        verticalLayoutMarkerEvents.add(lblMarkerDragStart);
        verticalLayoutMarkerEvents.add(lblMarkerMouseOut);
        verticalLayoutMarkerEvents.add(lblMarkerMouseOver);
        verticalLayoutMarkerEvents.add(lblMarkerRightClick);
        
        VerticalLayout verticalLayoutPolylineEvents = new VerticalLayout();
        
        lblPolylineClick = new Label("polyline-click");
        lblPolylineClick.setWidthFull();
        lblPolylineDblClick = new Label("polyline-dbl-click");
        lblPolylineDblClick.setWidthFull();
        lblPolylineMouseOut = new Label("polyline-mouse-out");
        lblPolylineMouseOut.setWidthFull();
        lblPolylineMouseOver = new Label("polyline-mouse-over");
        lblPolylineMouseOver.setWidthFull();
        lblPolylineRightClick = new Label("polyline-right-click");
        lblPolylineRightClick.setWidthFull();
        lblPolylinePathChanged = new Label("polyline-path-changed");
        lblPolylinePathChanged.setWidthFull();
        
        verticalLayoutPolylineEvents.add(lblPolylineClick);
        verticalLayoutPolylineEvents.add(lblPolylineDblClick);
        verticalLayoutPolylineEvents.add(lblPolylineMouseOut);
        verticalLayoutPolylineEvents.add(lblPolylineMouseOver);
        verticalLayoutPolylineEvents.add(lblPolylineRightClick);
        verticalLayoutPolylineEvents.add(lblPolylinePathChanged);
        
        pages = new HashMap();
        
        pages.put(tabMapEvents, verticalLayoutMapEvents);
        pages.put(tabMarkerEvents, verticalLayoutMarkerEvents);
        pages.put(tabPolylineEvents, verticalLayoutPolylineEvents);
                
        verticalLayoutMain = new VerticalLayout();        
        verticalLayoutMain.setSizeFull();
        
        verticalLayoutMain.add(tabs);
        verticalLayoutMain.add(verticalLayoutMapEvents);
        
        tabs.addSelectedChangeListener(new ComponentEventListener<SelectedChangeEvent>() {
            @Override
            public void onComponentEvent(SelectedChangeEvent event) {
                if (pages.containsKey(event.getPreviousTab()) && 
                    pages.containsKey(event.getSelectedTab())) {
                    verticalLayoutMain.replace(pages.get(event.getPreviousTab()), 
                        pages.get(event.getSelectedTab()));
                }
            }
        });
        
        googleMap.addMapClickListener(new ComponentEventListener<GoogleMapEvent.MapClickEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.MapClickEvent event) {
                googleMap.setCenterLat(1.2135252);
                googleMap.setCenterLng(-77.3122422);
                googleMap.setZoom(13);
                setBackgroundLabel(lblMapClick);
            }
        });
        googleMap.addMapDblClickListener(new ComponentEventListener<GoogleMapEvent.MapDblClickEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.MapDblClickEvent event) {
                setBackgroundLabel(lblMapDblClick);
            }
        });
        googleMap.addMapRightClickListener(new ComponentEventListener<GoogleMapEvent.MapRightClickEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.MapRightClickEvent event) {
                GoogleMapMarker googleMapMarker = new GoogleMapMarker(event.getLat(), event.getLng());
                setMarkerListeners(googleMap, googleMapMarker);
                googleMap.newMarker(googleMapMarker);
                googleMapMarker.setDraggable(true);
                
                JsonObject label = Json.createObject();
                label.put("color", "#305F72"); //NOI18N
                label.put("text", "New Marker"); //NOI18N
                
                googleMapMarker.setLabel(label);
                
                JsonObject icon = Json.createObject();
                JsonObject labelOrigin = Json.createObject();
                labelOrigin.put("x", 20); //NOI18N
                labelOrigin.put("y", 40); //NOI18N
                icon.put("url", "star.png"); //NOI18N
                icon.put("labelOrigin", labelOrigin); //NOI18N
                
                googleMapMarker.setIcon(icon);
                
                googleMapMarker.setTitle("New Marker");
                
                setBackgroundLabel(lblMapRightClick);
            }
        });
        googleMap.addMapCenterChangedListener(new ComponentEventListener<GoogleMapEvent.MapCenterChangedEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.MapCenterChangedEvent event) {
                setBackgroundLabel(lblMapCenterChanged);
            }
        });
        googleMap.addMapMouseMoveListener(new ComponentEventListener<GoogleMapEvent.MapMouseMoveEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.MapMouseMoveEvent event) {
                setBackgroundLabel(lblMapMouseMove);
            }
        });
        googleMap.addMapMouseOutListener(new ComponentEventListener<GoogleMapEvent.MapMouseOutEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.MapMouseOutEvent event) {
                setBackgroundLabel(lblMapMouseOut);
            }
        });
        googleMap.addMapMouseOverListener(new ComponentEventListener<GoogleMapEvent.MapMouseOverEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.MapMouseOverEvent event) {
                setBackgroundLabel(lblMapMouseOver);
            }
        });
        googleMap.addMapZoomChangedListener(new ComponentEventListener<GoogleMapEvent.MapZoomChangedEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.MapZoomChangedEvent event) {
                setBackgroundLabel(lblZoomChanged);
            }
        });        
        splitLayoutMain.addToSecondary(verticalLayoutMain);
        
        splitLayoutMain.setSplitterPosition(70);
        splitLayoutMain.setSizeFull();
        add(splitLayoutMain);
    }
        
    public void setMarkerListeners(GoogleMap googleMap, GoogleMapMarker googleMapMarker) {
        googleMapMarker.addMarkerMouseOverListener(new ComponentEventListener<GoogleMapEvent.MarkerMouseOverEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.MarkerMouseOverEvent t) {
                tabs.setSelectedTab(tabMarkerEvents);
                setBackgroundLabel(lblMarkerMouseOver);
            }
        });
        googleMapMarker.addMarkerMouseOutListener(new ComponentEventListener<GoogleMapEvent.MarkerMouseOutEvent>(){
            @Override
            public void onComponentEvent(GoogleMapEvent.MarkerMouseOutEvent t) {
                tabs.setSelectedTab(tabMapEvents);
                setBackgroundLabel(lblMarkerMouseOut);
            }
        });
        googleMapMarker.addMarkerClickListener(new ComponentEventListener<GoogleMapEvent.MarkerClickEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.MarkerClickEvent event) {
                googleMapMarker.setLat(2.6116145);
                googleMapMarker.setLng(-76.3862953);
                setBackgroundLabel(lblMarkerClick);
            }
        });
        googleMapMarker.addMarkerDragEndListener(new ComponentEventListener<GoogleMapEvent.MarkerDragEnd>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.MarkerDragEnd event) {
                setBackgroundLabel(lblMarkerDragEnd);
            }
        });
        googleMapMarker.addMarkerDragStartListener(new ComponentEventListener<GoogleMapEvent.MarkerDragStart>(){
            @Override
            public void onComponentEvent(GoogleMapEvent.MarkerDragStart event) {
                setBackgroundLabel(lblMarkerDragStart);
            }
        });
        googleMapMarker.addMarkerRightClickListener(new ComponentEventListener<GoogleMapEvent.MarkerRightClickEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.MarkerRightClickEvent event) {
                setBackgroundLabel(lblMarkerRightClick);
            }
        });
        googleMapMarker.addMarkerDblClickListener(new ComponentEventListener<GoogleMapEvent.MarkerDblClickEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.MarkerDblClickEvent event) {
                setBackgroundLabel(lblMarkerDblClick);
                googleMap.removeMarker(googleMapMarker);
            }
        });
    }
    
    public void setPolylineListener(GoogleMapPolyline googleMapPolyline) {
        googleMapPolyline.addPolylineMouseOverListener(new ComponentEventListener<GoogleMapEvent.PolylineMouseOverEvent>(){
            @Override
            public void onComponentEvent(GoogleMapEvent.PolylineMouseOverEvent t) {
                tabs.setSelectedTab(tabPolylineEvents);
                setBackgroundLabel(lblPolylineMouseOver);
            }
        });
        googleMapPolyline.addPolylineMouseOutListener(new ComponentEventListener<GoogleMapEvent.PolylineMouseOutEvent>(){
            @Override
            public void onComponentEvent(GoogleMapEvent.PolylineMouseOutEvent t) {
                tabs.setSelectedTab(tabMapEvents);
                setBackgroundLabel(lblPolylineMouseOut);
            }
        });
        googleMapPolyline.addPolylineClickListener(new ComponentEventListener<GoogleMapEvent.PolylineClickEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.PolylineClickEvent event) {
                setBackgroundLabel(lblPolylineClick);
            }
        });
        googleMapPolyline.addPolylineDblClickListener(new ComponentEventListener<GoogleMapEvent.PolylineDblClickEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.PolylineDblClickEvent event) {
                setBackgroundLabel(lblPolylineDblClick);
            }
        });
        googleMapPolyline.addPolylineRightClickListener(new ComponentEventListener<GoogleMapEvent.PolylineRightClickEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.PolylineRightClickEvent event) {
                setBackgroundLabel(lblPolylineRightClick);
            }
        });
        googleMapPolyline.addPolylinePathChangedListener(new ComponentEventListener<GoogleMapEvent.PolylinePathChangedEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.PolylinePathChangedEvent event) {
                setBackgroundLabel(lblPolylinePathChanged);
            }
        });
    }
    
    private void setBackgroundLabel(final Label label) {
        label.getStyle().set("background", "#F2F4F9"); //NOI18N
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
                }
                getUI().get().access(new Command() {
                    @Override
                    public void execute() {
                        label.getStyle().set("background", "transparent"); //NOI18N
                        getUI().get().push();
                    }
                });
            }
        }).start();
    }
}
