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
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
@Push
@Route
@PWA(name = "Project Base for Vaadin Flow with Spring", shortName = "Project Base")
public class MainView extends VerticalLayout {
    private final String API_KEY = "API-KEY";
    private final String CLIENT_ID = null;
    private Label lblMarkerClick;
    private Label lblMarkerDblClick;
    private Label lblMarkerRightClick;

    public MainView(@Autowired MessageBean bean) {
        setSizeFull();
        GoogleMap googleMap = new GoogleMap(API_KEY, CLIENT_ID);
        GoogleMapMarker googleMapMarker = new GoogleMapMarker(2.4573831, -76.6699746);
        setMarkerListeners(googleMapMarker);
        googleMap.newMarker(googleMapMarker);
        add(googleMap);
        /*
        GoogleMapPoly googleMapPolyline = new GoogleMapPoly();

        Button addPointsButton = new Button("Add Points");

        addPointsButton.addClickListener(click -> {
            List<GoogleMapPoint> path = new ArrayList();
            path.add(new GoogleMapPoint(0.5, -0.5)); 
            path.add(new GoogleMapPoint(-0.5, 0.5));

            googleMapPolyline.setPath(path);
            System.out.println(">>> ADDING PATH POLY"+ googleMapPolyline);
        });


        googleMap.addMarkerAddedListener(new ComponentEventListener<GoogleMapMarkerAdded>() {
           @Override
           public void onComponentEvent(GoogleMapMarkerAdded t) {
               Notification notification = new Notification();
               notification.add(new Label("marker added"));
               notification.setDuration(3000);
               notification.open();
               System.out.println(String.format(">>> EVENT MARKER ADDED %s", t.getSource().getElement().getChildren()));

           }
        });
        
        googleMap.addClickListener(e -> {
            Notification.show("Clicked at " + e.getX() + "," + e.getY(), 1000, Notification.Position.TOP_CENTER);
        });
        googleMap.addPolyLine(googleMapPolyline);
        */
           
        
        SplitLayout splitLayoutMain = new SplitLayout();
        splitLayoutMain.addToPrimary(googleMap);
        
        Tabs tabs = new Tabs();
        
        tabs.setOrientation(Tabs.Orientation.HORIZONTAL);
        
        Tab tabMapEvents = new Tab("Map Events");
        Tab tabMarkerEvents = new Tab("Marker Events");
        tabs.add(tabMapEvents);
        tabs.add(tabMarkerEvents);
        tabs.setSelectedTab(tabMapEvents);
        VerticalLayout verticalLayoutMapEvents = new VerticalLayout();
        
        Label lblMapClick = new Label("map-click (New Marker)"); //NOI18N
        lblMapClick.setWidthFull();
        Label lblMapDblClick = new Label("map-dbl-click"); //NOI18N
        lblMapDblClick.setWidthFull();
        Label lblMapRightClick = new Label("map-right-click"); //NOI18N
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
        
        lblMarkerClick = new Label("marker-click"); //NOI18N
        lblMarkerClick.setWidthFull();
        lblMarkerDblClick = new Label("marker-dbl-click"); //NOI18N
        lblMarkerDblClick.setWidthFull();
        lblMarkerRightClick = new Label("marker-right-click"); //NOI18N
        lblMarkerRightClick.setWidthFull();
        
        verticalLayoutMarkerEvents.add(lblMarkerClick);
        verticalLayoutMarkerEvents.add(lblMarkerDblClick);
        verticalLayoutMarkerEvents.add(lblMarkerRightClick);
                
        HashMap<Tab, VerticalLayout> pages = new HashMap();
        
        pages.put(tabMapEvents, verticalLayoutMapEvents);
        pages.put(tabMarkerEvents, verticalLayoutMarkerEvents);                
                
        VerticalLayout verticalLayoutMain = new VerticalLayout();        
        verticalLayoutMain.setSizeFull();
        
        verticalLayoutMain.add(tabs);
        verticalLayoutMain.add(verticalLayoutMapEvents);
        
        tabs.addSelectedChangeListener(new ComponentEventListener<SelectedChangeEvent>() {
            @Override
            public void onComponentEvent(SelectedChangeEvent event) {
                if (pages.containsKey(event.getSelectedTab())) {
                    verticalLayoutMain.removeAll();
                    verticalLayoutMain.add(tabs);
                    verticalLayoutMain.add(pages.get(event.getSelectedTab()));
                }
            }
        });
        
        googleMap.addMapClickListener(new ComponentEventListener<GoogleMapEvent.MapClickEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.MapClickEvent event) {
                GoogleMapMarker googleMapMarker = new GoogleMapMarker(event.getLat(), event.getLng());
                setMarkerListeners(googleMapMarker);
                googleMap.newMarker(googleMapMarker);
                setBackgroundLabel(lblMapClick);
                //Notification.show(String.format("lat:%f, lng:%f", event.getLat(), event.getLng()));
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
    
    public void setMarkerListeners(GoogleMapMarker googleMapMarker) {
        googleMapMarker.addMarkerClickListener(new ComponentEventListener<GoogleMapEvent.MarkerClickEvent>() {
            @Override
            public void onComponentEvent(GoogleMapEvent.MarkerClickEvent event) {
                setBackgroundLabel(lblMarkerClick);
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
