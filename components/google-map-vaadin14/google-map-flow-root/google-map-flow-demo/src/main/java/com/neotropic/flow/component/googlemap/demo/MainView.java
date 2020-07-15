package com.neotropic.flow.component.googlemap.demo;

import com.neotropic.flow.component.googlemap.Animation;
import com.neotropic.flow.component.googlemap.GoogleMapEvent;
import com.neotropic.flow.component.googlemap.GoogleMapPolyline;
import com.neotropic.flow.component.googlemap.GoogleMapMarker;
import com.neotropic.flow.component.googlemap.DrawingManager;
import com.neotropic.flow.component.googlemap.GoogleMap;
import com.neotropic.flow.component.googlemap.LatLng;
import com.neotropic.flow.component.googlemap.GoogleMapPolygon;
import com.neotropic.flow.component.googlemap.OverlayType;
import com.neotropic.flow.component.googlemap.Constants;
import com.neotropic.flow.component.googlemap.GoogleMapRectangle;
import com.neotropic.flow.component.googlemap.InfoWindow;
import com.neotropic.flow.component.googlemap.LatLngBounds;
import com.neotropic.flow.component.googlemap.OverlayView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

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
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
@Push
@Route
@PWA(name = "Project Base for Vaadin Flow with Spring", shortName = "Project Base")
public class MainView extends VerticalLayout {
    @Value("${google.maps.api-key}")
    private String apiKey;
    @Value("${google.maps.libraries}")
    private String libraries;
    
    private VerticalLayout verticalLayoutMain;
    private Tabs tabs;
    private HashMap<Tab, VerticalLayout> pages;
    private Tab tabMapEvents;
    private Tab tabMarkerEvents;
    private Tab tabPolylineEvents;
    private Tab tabPolygonEvents;
    //<editor-fold desc="Mark Labels" defaultstate="collapsed">
    private Label lblMarkerClick;
    private Label lblMarkerDblClick;
    private Label lblMarkerDragEnd;
    private Label lblMarkerDragStart;
    private Label lblMarkerMouseOut;
    private Label lblMarkerMouseOver;
    private Label lblMarkerRightClick;
    //</editor-fold>
    //<editor-fold desc="Polyline Labels" defaultstate="collapsed">
    private Label lblPolylineClick;
    private Label lblPolylineDblClick;
    private Label lblPolylineMouseOut;
    private Label lblPolylineMouseOver;
    private Label lblPolylineRightClick;
    private Label lblPolylinePathChanged;
    //</editor-fold>
    //<editor-fold desc="Polygon Labels" defaultstate="collapsed">
    private Label lblPolygonClick;
    private Label lblPolygonDblClick;
    private Label lblPolygonMouseOut;
    private Label lblPolygonMouseOver;
    private Label lblPolygonRightClick;
    private Label lblPolygonPathsChanged;
    //</editor-fold>
    public MainView() {
    }
    
    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        setMargin(false);
        setPadding(false);
        
        GoogleMap googleMap = new GoogleMap(apiKey, null, libraries);
        googleMap.setDisableDefaultUi(true);
        
        GoogleMapMarker googleMapMarker = new GoogleMapMarker(2.4574702, -76.6349535);
        googleMap.newMarker(googleMapMarker);
        googleMap.setMapTypeId(Constants.MapTypeId.ROADMAP);
        
        JsonObject label = Json.createObject();
        label.put("color", "#305F72"); //NOI18N
        label.put("text", "Marker"); //NOI18N

        googleMapMarker.setLabel(label);
        googleMapMarker.setTitle("Marker"); //NOI18N
        
        setMarkerListeners(googleMap, googleMapMarker);
        
        List<LatLng> coordinates = new ArrayList();
        coordinates.add(new LatLng(2.4574702, -76.6349535));
        coordinates.add(new LatLng(2.3512629, -76.6915093));
        coordinates.add(new LatLng(2.260897, -76.7449569));
        coordinates.add(new LatLng(2.1185563, -76.9974436));
        coordinates.add(new LatLng(2.0693058, -77.0552842));
        addNewPolyline(coordinates, googleMap);
        
        SplitLayout splitLayoutMain = new SplitLayout();
        splitLayoutMain.addToPrimary(googleMap);
        
        tabs = new Tabs();
        
        tabs.setOrientation(Tabs.Orientation.HORIZONTAL);
        
        tabMapEvents = new Tab("Map Events");
        tabMarkerEvents = new Tab("Marker Events");
        tabPolylineEvents = new Tab("Polyline Events");
        tabPolygonEvents = new Tab("Polygon Events");
        
        tabs.add(tabMapEvents);
        tabs.add(tabMarkerEvents);
        tabs.add(tabPolylineEvents);
        tabs.add(tabPolygonEvents);
        
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
        
        VerticalLayout verticalLayoutPolygonEvents = new VerticalLayout();
        lblPolygonClick = new Label("polygon-click");
        lblPolygonClick.setSizeFull();
        lblPolygonDblClick = new Label("polygon-dbl-click");
        lblPolygonDblClick.setSizeFull();
        lblPolygonMouseOut = new Label("polygon-mouse-out");
        lblPolygonMouseOut.setSizeFull();
        lblPolygonMouseOver = new Label("polygon-mouse-over");
        lblPolygonMouseOver.setSizeFull();
        lblPolygonRightClick = new Label("polygon-right-click");
        lblPolygonRightClick.setSizeFull();
        lblPolygonPathsChanged = new Label("polygon-path-changed");
        lblPolygonPathsChanged.setSizeFull();
        
        verticalLayoutPolygonEvents.add(lblPolygonClick);
        verticalLayoutPolygonEvents.add(lblPolygonDblClick);
        verticalLayoutPolygonEvents.add(lblPolygonMouseOut);
        verticalLayoutPolygonEvents.add(lblPolygonMouseOver);
        verticalLayoutPolygonEvents.add(lblPolygonRightClick);
        verticalLayoutPolygonEvents.add(lblPolygonPathsChanged);
        
        pages = new HashMap();
        
        pages.put(tabMapEvents, verticalLayoutMapEvents);
        pages.put(tabMarkerEvents, verticalLayoutMarkerEvents);
        pages.put(tabPolylineEvents, verticalLayoutPolylineEvents);
        pages.put(tabPolygonEvents, verticalLayoutPolygonEvents);
                
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
                addNewMarker(event.getLat(), event.getLng(), googleMap);
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
        DrawingManager drawingManager = new DrawingManager();
        googleMap.newDrawingManager(drawingManager);
        drawingManager.addDrawingManagerMarkerCompleteListener(event -> 
            addNewMarker(event.getLat(), event.getLng(), googleMap)
        );
        drawingManager.addDrawingManagerPolylineCompleteListener(event -> 
            addNewPolyline(event.getPath(), googleMap)
        );
        drawingManager.addDrawingManagerPolygonCompleteListener(event -> 
            addNewPolygon(event.getPaths(), googleMap)
        );
        Tabs tabsDrawingManager = new Tabs();
        tabsDrawingManager.getStyle().set("position", "absolute");
        tabsDrawingManager.getStyle().set("z-index", "5");
        tabsDrawingManager.getStyle().set("top", "10px");
        tabsDrawingManager.getStyle().set("left", "25%");
        tabsDrawingManager.getStyle().set("background-color", "#fff");
        
        Tab tabHand = new Tab(new Icon(VaadinIcon.HAND));
        Tab tabMarker = new Tab(new Icon(VaadinIcon.MAP_MARKER));
        Tab tabPolygon = new Tab(new Icon(VaadinIcon.STAR_O));
        Tab tabPolyline = new Tab(new Icon(VaadinIcon.SPARK_LINE));
        Tab tabRectangle = new Tab(new Icon(VaadinIcon.THIN_SQUARE));
        Tab tabOverlayView = new Tab(new Icon(VaadinIcon.STOP));
        Tab tabGoogleMapsMxGraph = new Tab(new Icon(VaadinIcon.GLOBE));
        
        tabsDrawingManager.add(tabHand, tabMarker, tabPolyline, tabPolygon, tabRectangle, tabOverlayView, tabGoogleMapsMxGraph);
        tabsDrawingManager.setSelectedTab(tabHand);
        tabsDrawingManager.addSelectedChangeListener(event -> {
            if (tabHand.equals(event.getSelectedTab()))
                drawingManager.setDrawingMode(null);
            else if (tabMarker.equals(event.getSelectedTab()))
                drawingManager.setDrawingMode(OverlayType.MARKER);
            else if (tabPolygon.equals(event.getSelectedTab()))
                drawingManager.setDrawingMode(OverlayType.POLYGON);
            else if (tabPolyline.equals(event.getSelectedTab()))
                drawingManager.setDrawingMode(OverlayType.POLYLINE);
            else if (tabRectangle.equals(event.getSelectedTab())) {
                drawingManager.setDrawingMode(OverlayType.RECTANGLE);
                drawingManager.addDrawingManagerRectangleCompleteListener(theEvent -> {
                    addNewRectangle(theEvent.getBounds(), googleMap);
                    theEvent.unregisterListener();
                });
            } else if (tabOverlayView.equals(event.getSelectedTab())) {
                drawingManager.setDrawingMode(OverlayType.RECTANGLE);
                drawingManager.addDrawingManagerRectangleCompleteListener(theEvent -> {
                    addNewOverlayView(theEvent.getBounds(), googleMap);
                    theEvent.unregisterListener();
                });
            } else if (tabGoogleMapsMxGraph.equals(event.getSelectedTab()))
                UI.getCurrent().navigate(OverlaysView.class);
        });
        googleMap.getElement().appendChild(tabsDrawingManager.getElement());
        
        splitLayoutMain.addToSecondary(verticalLayoutMain);
        
        splitLayoutMain.setSplitterPosition(70);
        splitLayoutMain.setSizeFull();
        add(splitLayoutMain);
        
//        JsonArray styles = Json.createArray();
//
//        JsonArray stylers = Json.createArray();
//        JsonObject color = Json.createObject();
//        color.put("color", "#ffffff");
//        stylers.set(0, color);
//        
//        JsonObject style = Json.createObject();
//        style.put("elementType", "geometry");
//        style.put("stylers", stylers);
//        styles.set(0, style);
//        googleMap.setStyles(styles);
//        JsonArray styles = Json.createArray();
//
        JsonArray styles = Json.createArray();
        JsonArray stylers = Json.createArray();
        JsonObject visibility = Json.createObject();
        visibility.put("visibility", "off");
        stylers.set(0, visibility);
        
        JsonObject style = Json.createObject();
        style.put("featureType", "poi");
        style.put("stylers", stylers);
        styles.set(0, style);
        googleMap.setStyles(styles);
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
                if (googleMapMarker.getAnimation() == null)
                    googleMapMarker.setAnimation(Animation.BOUNCE);
                else
                    googleMapMarker.setAnimation(null);
            }
        });
        googleMapMarker.addMarkerRightClickListener(event -> {
            InfoWindow infoWindow = new InfoWindow();
            googleMap.add(infoWindow);
            infoWindow.addInfoWindowAddedListener(addedEvent -> {
                VerticalLayout vlt = new VerticalLayout();
                vlt.add(new Label("Info Window"));
                Button btnClose = new Button(
                    "Close", 
                    new Icon(VaadinIcon.CLOSE), 
                    clickEvent -> infoWindow.close()
                );
                vlt.add(btnClose);
                infoWindow.add(vlt);

                infoWindow.open(googleMap, googleMapMarker);
            });
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
    
    public void addNewMarker(double lat, double lng, GoogleMap googleMap) {
        GoogleMapMarker googleMapMarker = new GoogleMapMarker(lat, lng);
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
    }
    
    public void addNewPolyline(List<LatLng> path, GoogleMap googleMap) {
        GoogleMapPolyline googleMapPolyline = new GoogleMapPolyline();
        googleMapPolyline.setEditable(true);
        googleMapPolyline.setDraggable(true);
        googleMapPolyline.setStrokeColor("#32a852");
        googleMapPolyline.setPath(path);
        
        googleMap.newPolyline(googleMapPolyline);
        
        setPolylineListeners(googleMapPolyline);
    }
    
    public void addNewPolygon(List<List<LatLng>> paths, GoogleMap googleMap) {
        GoogleMapPolygon googleMapPolygon = new GoogleMapPolygon(paths);
        googleMapPolygon.setEditable(true);
        googleMap.newPolygon(googleMapPolygon);
        setPolygonListener(googleMapPolygon);
    }
    
    public void addNewRectangle(LatLngBounds bounds, GoogleMap googleMap) {
        GoogleMapRectangle googleMapRectangle = new GoogleMapRectangle(bounds);
        googleMap.addRectangle(googleMapRectangle);
    }
    
    public void addNewOverlayView(LatLngBounds bounds, GoogleMap googleMap) {
        OverlayView overlayView = new OverlayView(bounds);
        googleMap.addOverlayView(overlayView);
        
        Div div = new Div();
        div.getStyle().set("background", "blue");
        div.getStyle().set("opacity", "0.7");
        div.add(new Label("Hello!!"));
        overlayView.add(div);
    }
    
    public void setPolylineListeners(GoogleMapPolyline googleMapPolyline) {
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
                tabs.setSelectedTab(tabPolylineEvents);
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
    private void setPolygonListener(GoogleMapPolygon googleMapPolygon) {
        googleMapPolygon.addPolygonMouseOverListener(event -> { 
            tabs.setSelectedTab(tabPolygonEvents);
            setBackgroundLabel(lblPolygonMouseOver);
        });
        googleMapPolygon.addPolygonMouseOutListener(event -> {
            tabs.setSelectedTab(tabMapEvents);
            setBackgroundLabel(lblPolygonMouseOut);
        });
        googleMapPolygon.addPolygonClickListener(event -> 
            setBackgroundLabel(lblPolygonClick)
        );
        googleMapPolygon.addPolygonDblClickListener(event -> 
            setBackgroundLabel(lblPolygonDblClick)
        );
        googleMapPolygon.addPolygonRightClickListener(event -> 
            setBackgroundLabel(lblPolygonRightClick)
        );
        googleMapPolygon.addPolygonPathsChangedListener(event -> { 
            tabs.setSelectedTab(tabPolygonEvents);
            setBackgroundLabel(lblPolygonPathsChanged);
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
                try {
                    getUI().get().access(new Command() {
                        @Override
                        public void execute() {
                            label.getStyle().set("background", "transparent"); //NOI18N
                            try {
                                getUI().get().push();
                            } catch(NoSuchElementException ex) {
                            }
                        }
                    });
                } catch(NoSuchElementException ex) {
                }
            }
        }).start();
    }
}
