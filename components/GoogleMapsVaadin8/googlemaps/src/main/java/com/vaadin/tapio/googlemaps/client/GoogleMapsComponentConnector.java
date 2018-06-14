package com.vaadin.tapio.googlemaps.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.user.client.ui.Widget;

import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentContainerConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;
import com.vaadin.tapio.googlemaps.GoogleMapsComponent;
import com.vaadin.tapio.googlemaps.client.events.EdgeClickListener;
import com.vaadin.tapio.googlemaps.client.events.EdgeCompleteListener;
import com.vaadin.tapio.googlemaps.client.events.EdgeRightClickListener;
import com.vaadin.tapio.googlemaps.client.events.InfoWindowClosedListener;
import com.vaadin.tapio.googlemaps.client.events.MapClickListener;
import com.vaadin.tapio.googlemaps.client.events.MapDblClickListener;
import com.vaadin.tapio.googlemaps.client.events.MapMouseMoveListener;
import com.vaadin.tapio.googlemaps.client.events.MapMouseOverListener;
import com.vaadin.tapio.googlemaps.client.events.MapMoveListener;
import com.vaadin.tapio.googlemaps.client.events.MapRightClickListener;
import com.vaadin.tapio.googlemaps.client.events.MapTypeChangeListener;
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.events.MarkerDblClickListener;
import com.vaadin.tapio.googlemaps.client.events.MarkerDragListener;
import com.vaadin.tapio.googlemaps.client.events.MarkerRightClickListener;
import com.vaadin.tapio.googlemaps.client.events.PolygonClickListener;
import com.vaadin.tapio.googlemaps.client.events.PolygonCompleteListener;
import com.vaadin.tapio.googlemaps.client.events.PolygonDblClickListener;
import com.vaadin.tapio.googlemaps.client.events.PolygonRightClickListener;
import com.vaadin.tapio.googlemaps.client.events.PolylineClickListener;
import com.vaadin.tapio.googlemaps.client.events.PolylineCompleteListener;
import com.vaadin.tapio.googlemaps.client.events.PolylineDblClickListener;
import com.vaadin.tapio.googlemaps.client.events.PolylineRightClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapInfoWindow;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.tapio.googlemaps.client.rpcs.EdgeClickedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.EdgeCompletedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.EdgeRightClickedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.InfoWindowClosedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.MapClickedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.MapDblClickedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.MapMouseMovedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.MapMouseOverRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.MapMovedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.MapRightClickedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.MapTypeChangedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.MarkerClickedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.MarkerDblClickedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.MarkerDraggedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.MarkerRightClickedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.PolygonClickedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.PolygonCompletedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.PolygonDblClickedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.PolygonRightClickedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.PolylineClickedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.PolylineCompletedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.PolylineDblClickedRpc;
import com.vaadin.tapio.googlemaps.client.rpcs.PolylineRightClickedRpc;

@Connect(GoogleMapsComponent.class)
public class GoogleMapsComponentConnector extends AbstractComponentContainerConnector
    implements MarkerClickListener, MapMoveListener, MapClickListener,
        MarkerDragListener, InfoWindowClosedListener, MapTypeChangeListener,
        MapDblClickListener, MapRightClickListener, MarkerDblClickListener, MarkerRightClickListener,
        PolygonClickListener, PolygonDblClickListener, PolygonRightClickListener, PolygonCompleteListener, 
        PolylineClickListener, PolylineDblClickListener, PolylineRightClickListener,
        PolylineCompleteListener, MapMouseMoveListener, MapMouseOverListener,
        EdgeClickListener, EdgeCompleteListener, EdgeRightClickListener {

    private static final long serialVersionUID = -357262975672050103L;

    public static boolean apiLoaded = false;

    public static boolean loadingApi = false;

    private final List<GoogleMapsComponentInitListener> initListeners = new ArrayList<>();

    private final MarkerClickedRpc markerClickedRpc = RpcProxy
        .create(MarkerClickedRpc.class, this);
    private final MarkerRightClickedRpc markerRightClickedRpc = RpcProxy
        .create(MarkerRightClickedRpc.class, this);
    private final MarkerDblClickedRpc markerDblClickedRpc = RpcProxy
        .create(MarkerDblClickedRpc.class, this);
        
    private final PolygonClickedRpc polygonClickedRpc = RpcProxy
        .create(PolygonClickedRpc.class, this);
    private final PolygonDblClickedRpc polygonDblClickedRpc = RpcProxy
        .create(PolygonDblClickedRpc.class, this);
    private final PolygonRightClickedRpc polygonRightClickedRpc = RpcProxy
        .create(PolygonRightClickedRpc.class, this);
    private final PolygonCompletedRpc polygonCompletedRpc = RpcProxy
        .create(PolygonCompletedRpc.class, this);
    
    private final PolylineClickedRpc polylineClickedRpc = RpcProxy
        .create(PolylineClickedRpc.class, this);    
    private final PolylineDblClickedRpc polylineDblClickedRpc = RpcProxy
        .create(PolylineDblClickedRpc.class, this);
    private final PolylineRightClickedRpc polylineRightClickedRpc = RpcProxy
        .create(PolylineRightClickedRpc.class, this);
    private final PolylineCompletedRpc polylineCompletedRpc = RpcProxy
        .create(PolylineCompletedRpc.class, this);
    
    private final EdgeClickedRpc edgeClickedRpc = RpcProxy
        .create(EdgeClickedRpc.class, this);
    private final EdgeCompletedRpc edgeCompletedRpc = RpcProxy
        .create(EdgeCompletedRpc.class, this);
    private final EdgeRightClickedRpc edgeRightClickedRpc = RpcProxy
        .create(EdgeRightClickedRpc.class, this);
        
    private final MapMovedRpc mapMovedRpc = RpcProxy
        .create(MapMovedRpc.class, this);
    private final MapClickedRpc mapClickedRpc = RpcProxy
        .create(MapClickedRpc.class, this);
    private final MapDblClickedRpc mapDblClickedRpc = RpcProxy
        .create(MapDblClickedRpc.class, this);
    private final MapRightClickedRpc mapRightClickedRpc = RpcProxy
        .create(MapRightClickedRpc.class, this);
    private final MarkerDraggedRpc markerDraggedRpc = RpcProxy
        .create(MarkerDraggedRpc.class, this);
    private final InfoWindowClosedRpc infoWindowClosedRpc = RpcProxy
        .create(InfoWindowClosedRpc.class, this);
    private final MapTypeChangedRpc mapTypeChangedRpc = RpcProxy
        .create(MapTypeChangedRpc.class, this);
    private final MapMouseMovedRpc mapMouseMovedRpc = RpcProxy
        .create(MapMouseMovedRpc.class, this);
    private final MapMouseOverRpc mapMouseOverRpc = RpcProxy
        .create(MapMouseOverRpc.class, this);

    public GoogleMapsComponentConnector() {
    }

    protected void loadMapApi() {
        if (loadingApi) {
            return;
        }
        loadingApi = true;
        ArrayList<LoadApi.LoadLibrary> loadLibraries = new ArrayList<>();
        loadLibraries.add(LoadLibrary.ADSENSE);
        loadLibraries.add(LoadLibrary.DRAWING);
        loadLibraries.add(LoadLibrary.GEOMETRY);
        loadLibraries.add(LoadLibrary.PANORAMIO);
        loadLibraries.add(LoadLibrary.PLACES);
        loadLibraries.add(LoadLibrary.WEATHER);
        loadLibraries.add(LoadLibrary.VISUALIZATION);
        
        Runnable onLoad = new Runnable() {
            @Override
            public void run() {
                apiLoaded = true;
                loadingApi = false;
                for (GoogleMapsComponentInitListener listener : initListeners) {
                    listener.mapsApiLoaded();
                }
                initMap();
            }
        };

        LoadApi.Language language = null;
        if (getState().language != null) {
            language = LoadApi.Language.fromValue(getState().language);
        }

        String params = null;
        if (getState().clientId != null) {
            params = "client=" + getState().clientId;
        } else if (getState().apiKey != null) {
            params = "key=" + getState().apiKey;
        }

        if (getState().apiUrl != null) {
            AjaxLoader.init(getState().apiKey, getState().apiUrl);
        }

        LoadApi.go(onLoad, loadLibraries, false, language, params);
    }

    protected void initMap() {
        getWidget().initMap(getState().center, getState().zoom,
            getState().mapTypeId);
        getWidget().setMarkerClickListener(this);
        getWidget().setMarkerDblClickListener(this);
        getWidget().setMarkerRightClickListener(this);
        
        getWidget().setPolygonClickListener(this);
        getWidget().setPolygonDblClickListener(this);
        getWidget().setPolygonRightClickListener(this);
        getWidget().setPolygonCompleteListener(this);
        
        getWidget().setPolylineClickListener(this);
        getWidget().setPolylineDblClickListener(this);
        getWidget().setPolylineRightClickListener(this);
        getWidget().setPolylineCompleteListener(this);
        
        getWidget().setEdgeClickListener(this);
        getWidget().setEdgeCompleteListener(this);
        getWidget().setEdgeRightClickListener(this);
                
        getWidget().setMapMoveListener(this);
        getWidget().setMapClickListener(this);
        getWidget().setMapDblClickListener(this);
        getWidget().setMapRightClickListener(this);
        getWidget().setMarkerDragListener(this);
        getWidget().setInfoWindowClosedListener(this);
        getWidget().setMapTypeChangeListener(this);
                
        getWidget().setMapMouseOverListener(this);
        
        getLayoutManager().addElementResizeListener(getWidget().getElement(),
            new ElementResizeListener() {
                @Override
                public void onElementResize(ElementResizeEvent e) {
                    getWidget().triggerResize();
                }
            });
        MapWidget map = getWidget().getMap();
        updateFromState(true);
        for (GoogleMapsComponentInitListener listener : initListeners) {
            listener.mapWidgetInitiated(map);
        }
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        if (!apiLoaded) {
            loadMapApi();
            return;
        } else if (getWidget().getMap() == null) {
            initMap();
        }
        updateFromState(stateChangeEvent.isInitialStateChange());
    }

    protected void updateFromState(boolean initial) {
        updateVisibleAreaAndCenterBoundLimits();

        LatLng center = LatLng.newInstance(getState().center.getLat(),
            getState().center.getLon());
        getWidget().setCenter(center);
        getWidget().setZoom(getState().zoom);
        getWidget().setTrafficLayerVisible(getState().trafficLayerVisible);
        getWidget().setMarkers(getState().markers.values());
        getWidget().setPolygonOverlays(getState().polygons);
        getWidget().setPolylineOverlays(getState().polylines);
        getWidget().setKmlLayers(getState().kmlLayers);
        getWidget().setMapType(getState().mapTypeId);
        getWidget().setControls(getState().controls);
        getWidget().setDraggable(getState().draggable);
        getWidget().setKeyboardShortcutsEnabled(
            getState().keyboardShortcutsEnabled);
        getWidget().setScrollWheelEnabled(getState().scrollWheelEnabled);
        getWidget().setDisableDoubleClickZoom(getState().disableDoubleClickZoom);
        getWidget().setMinZoom(getState().minZoom);
        getWidget().setMaxZoom(getState().maxZoom);
        getWidget().setInfoWindows(getState().infoWindows.values());
                
        if (getState().fitToBoundsNE != null
            && getState().fitToBoundsSW != null) {
            getWidget().fitToBounds(getState().fitToBoundsNE,
                getState().fitToBoundsSW);
        }
        getWidget().updateOptionsAndPanning();
        if (initial) {
            getWidget().triggerResize();
        }
        getWidget().setDrawingMode(getState().drawingMode);
        getWidget().setSource(getState().markerSource);
        getWidget().showMarkerLabels(getState().showMarkerLabels);
        getWidget().showPolylineLabels(getState().showPolylineLabels);
        getWidget().showPolygonLabels(getState().showPolygonLabels);
        getWidget().setMeasureEdgeDistance(getState().mesaureEdgeDistance);
        getWidget().setEdges(getState().edges);
        getWidget().showEdgeLabels(getState().showEdgeLabels);
        
        getWidget().setMeasureDistance(getState().measureDistance);
                
        onConnectorHierarchyChange(null);
    }

    protected void updateVisibleAreaAndCenterBoundLimits() {
        if (getState().limitCenterBounds) {
            getWidget().setCenterBoundLimits(getState().centerNELimit,
                getState().centerSWLimit);
        } else {
            getWidget().clearCenterBoundLimits();
        }

        if (getState().limitVisibleAreaBounds) {
            getWidget().setVisibleAreaBoundLimits(getState().visibleAreaNELimit,
                getState().visibleAreaSWLimit);
        } else {
            getWidget().clearVisibleAreaBoundLimits();
        }
    }

    @Override
    protected Widget createWidget() {
        return GWT.create(GoogleMapWidget.class);
    }

    @Override
    public GoogleMapWidget getWidget() {
        return (GoogleMapWidget) super.getWidget();
    }

    @Override
    public GoogleMapState getState() {
        return (GoogleMapState) super.getState();
    }

    @Override
    public void infoWindowClosed(GoogleMapInfoWindow window) {
        infoWindowClosedRpc.infoWindowClosed(window.getId());
    }

    @Override
    public void markerDragged(GoogleMapMarker draggedMarker,
        LatLon oldPosition) {
        markerDraggedRpc.markerDragged(draggedMarker.getId(),
            draggedMarker.getPosition());
    }

    @Override
    public void mapClicked(LatLon position) {
        mapClickedRpc.mapClicked(position);
    }

    @Override
    public void mapMoved(int zoomLevel, LatLon center, LatLon boundsNE,
        LatLon boundsSW) {
        mapMovedRpc.mapMoved(zoomLevel, center, boundsNE, boundsSW);
    }
    
    @Override
    public void mapDblClicked(LatLon position) {
        mapDblClickedRpc.mapDblClicked(position);
    }
    
    @Override
    public void mapRightClicked(LatLon position) {
        mapRightClickedRpc.mapRightClicked(position);
    }

    @Override
    public void markerClicked(GoogleMapMarker clickedMarker) {
        markerClickedRpc.markerClicked(clickedMarker.getId());
    }
    
    @Override
    public void markerDblClicked(GoogleMapMarker clickedMarker) {
        markerDblClickedRpc.markerDblClicked(clickedMarker.getId());
    }

    @Override
    public void markerRightClicked(GoogleMapMarker clickedMarker) {
        markerRightClickedRpc.markerRightClicked(clickedMarker.getId());
    }
    
    @Override
    public void polygonClicked(GoogleMapPolygon clickedPolygon) {
        polygonClickedRpc.polygonClicked(clickedPolygon);
    }
    
    @Override
    public void polygonDblClicked(GoogleMapPolygon clickedPolygon) {
        polygonDblClickedRpc.polygonDblClicked(clickedPolygon.getId());
    }
    
    @Override
    public void polygonRightClicked(GoogleMapPolygon clickedPolygon) {
        polygonRightClickedRpc.polygonRightClicked(clickedPolygon.getId());
    }
    
    @Override
    public void polygonCompleted(GoogleMapPolygon completedPolygon){
        polygonCompletedRpc.polygonComplete(completedPolygon);
    }
    
    @Override
    public void polylineClicked(GoogleMapPolyline clickedPolyline) {
        polylineClickedRpc.polylineClicked(clickedPolyline.getId());
    }

    @Override
    public void polylineDblClicked(GoogleMapPolyline clickedPolyline) {
        polylineDblClickedRpc.polylineDblClicked(clickedPolyline.getId());
    }

    @Override
    public void polylineRightClicked(GoogleMapPolyline clickedPolyline) {
        polylineRightClickedRpc.polylineRightClicked(clickedPolyline.getId());
    }
    
    @Override
    public void polylineCompleted(GoogleMapPolyline completedPolyline) {
        polylineCompletedRpc.polylineCompleted(completedPolyline);
    }
    
    @Override
    public void edgeClicked(GoogleMapPolyline clickedEdge) {
        edgeClickedRpc.edgeClicked(clickedEdge);
    }
    
    @Override
    public void edgeCompleted(GoogleMapPolyline completedEdge) {
        edgeCompletedRpc.edgeCompleted(completedEdge);
    }
    
    @Override
    public void edgeRightClicked(GoogleMapPolyline rightClickedEdge) {
        edgeRightClickedRpc.edgeRightClicked(rightClickedEdge);
    }

    @Override
    public void mapTypeChanged(MapTypeId mapTypeId) {
        mapTypeChangedRpc.mapTypeChanged(mapTypeId.toString());
    }
    
    @Override
    public void mapMouseMoved(LatLon position) {
        mapMouseMovedRpc.mapMouseMoved(position);
    }
    
    @Override
    public void mapMouseOver(LatLon position) {
        mapMouseOverRpc.mapMouseOver(position);
    }

    public void addInitListener(GoogleMapsComponentInitListener listener) {
        if (apiLoaded) {
            listener.mapsApiLoaded();
        }
        if (getWidget().getMap() != null) {
            listener.mapWidgetInitiated(getWidget().getMap());
        }
        initListeners.add(listener);
    }

    @Override
    public void onConnectorHierarchyChange(
        ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
        Map<Long, Widget> infoWindowContents = new HashMap<>();
        List<ComponentConnector> children = getChildComponents();
        for (ComponentConnector connector : children) {
            for (String style : connector.getState().styles) {
                if (style.startsWith("content-for-infowindow-")) {
                    String identifier = style
                        .replace("content-for-infowindow-", "");
                    Long id = Long.parseLong(identifier);
                    infoWindowContents.put(id, connector.getWidget());
                    getWidget().setInfoWindowContents(infoWindowContents);
                }
            }
        }
    }

    @Override
    public void updateCaption(ComponentConnector connector) {

    }
}
