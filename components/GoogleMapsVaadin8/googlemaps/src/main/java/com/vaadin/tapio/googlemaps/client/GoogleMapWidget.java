package com.vaadin.tapio.googlemaps.client;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.ajaxloader.client.Properties;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.maps.client.MapImpl;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.base.Size;
import com.google.gwt.maps.client.controls.ControlPosition;
import com.google.gwt.maps.client.drawinglib.DrawingControlOptions;
import com.google.gwt.maps.client.drawinglib.DrawingManager;
import com.google.gwt.maps.client.drawinglib.DrawingManagerOptions;
import com.google.gwt.maps.client.drawinglib.OverlayType;
import com.google.gwt.maps.client.events.center.CenterChangeMapEvent;
import com.google.gwt.maps.client.events.center.CenterChangeMapHandler;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.closeclick.CloseClickMapEvent;
import com.google.gwt.maps.client.events.closeclick.CloseClickMapHandler;
import com.google.gwt.maps.client.events.dblclick.DblClickMapEvent;
import com.google.gwt.maps.client.events.dblclick.DblClickMapHandler;
import com.google.gwt.maps.client.events.dragend.DragEndMapEvent;
import com.google.gwt.maps.client.events.dragend.DragEndMapHandler;
import com.google.gwt.maps.client.events.idle.IdleMapEvent;
import com.google.gwt.maps.client.events.idle.IdleMapHandler;
import com.google.gwt.maps.client.events.insertat.InsertAtMapEvent;
import com.google.gwt.maps.client.events.insertat.InsertAtMapHandler;
import com.google.gwt.maps.client.events.maptypeid.MapTypeIdChangeMapEvent;
import com.google.gwt.maps.client.events.maptypeid.MapTypeIdChangeMapHandler;
import com.google.gwt.maps.client.events.mousedown.MouseDownMapEvent;
import com.google.gwt.maps.client.events.mousedown.MouseDownMapHandler;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapEvent;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapHandler;
import com.google.gwt.maps.client.events.mouseover.MouseOverMapEvent;
import com.google.gwt.maps.client.events.mouseover.MouseOverMapHandler;
import com.google.gwt.maps.client.events.mouseup.MouseUpMapEvent;
import com.google.gwt.maps.client.events.mouseup.MouseUpMapHandler;
import com.google.gwt.maps.client.events.overlaycomplete.polygon.PolygonCompleteMapEvent;
import com.google.gwt.maps.client.events.overlaycomplete.polygon.PolygonCompleteMapHandler;
import com.google.gwt.maps.client.events.removeat.RemoveAtMapEvent;
import com.google.gwt.maps.client.events.removeat.RemoveAtMapHandler;
import com.google.gwt.maps.client.events.rightclick.RightClickMapEvent;
import com.google.gwt.maps.client.events.rightclick.RightClickMapHandler;
import com.google.gwt.maps.client.events.setat.SetAtMapEvent;
import com.google.gwt.maps.client.events.setat.SetAtMapHandler;
import com.google.gwt.maps.client.geometrylib.SphericalUtils;
import com.google.gwt.maps.client.layers.KmlLayer;
import com.google.gwt.maps.client.layers.KmlLayerOptions;
import com.google.gwt.maps.client.layers.TrafficLayer;
import com.google.gwt.maps.client.mvc.MVCArray;
import com.google.gwt.maps.client.overlays.*;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.tapio.googlemaps.client.events.EdgeClickListener;
import com.vaadin.tapio.googlemaps.client.events.EdgeCompleteListener;
import com.vaadin.tapio.googlemaps.client.events.EdgeRightClickListener;

import com.vaadin.tapio.googlemaps.client.events.InfoWindowClosedListener;
import com.vaadin.tapio.googlemaps.client.events.MapClickListener;
import com.vaadin.tapio.googlemaps.client.events.MapDblClickListener;
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
import com.vaadin.tapio.googlemaps.client.layers.GoogleMapKmlLayer;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleGwtMapPolygon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleGwtMapPolygonOptions;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleGwtMapLabel;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapInfoWindow;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import java.util.Date;

public class GoogleMapWidget extends FlowPanel implements RequiresResize {

    public static final String CLASSNAME = "googlemap";
    protected MapWidget map;
    protected MapImpl mapImpl;

    protected MapOptions mapOptions;
    protected Map<Marker, GoogleMapMarker> markerMap = new HashMap<>();
    protected Map<GoogleMapMarker, Marker> gmMarkerMap = new HashMap<>();
    protected Map<GoogleGwtMapPolygon, GoogleMapPolygon> polygonMap = new HashMap<>();
    protected Map<GoogleMapPolygon, GoogleGwtMapPolygon> gmPolygonMap = new HashMap<>();
    protected Map<Polyline, GoogleMapPolyline> polylineMap = new HashMap<>();
    protected Map<GoogleMapPolyline, Polyline> gmPolylineMap = new HashMap<>();
    
    protected Map<GoogleMapPolyline, Polyline> gmEdgeMap = new HashMap<>();
    protected Map<Polyline, GoogleMapPolyline> edgeMap = new HashMap<>();
    
    protected Map<CustomInfoWindow, GoogleMapInfoWindow> infoWindowMap = new HashMap<>();
    protected Map<GoogleMapInfoWindow, CustomInfoWindow> gmInfoWindowMap = new HashMap<>();
    protected Map<Long, CustomInfoWindow> infoWindowIDs = new HashMap<>();

    protected Map<KmlLayer, GoogleMapKmlLayer> kmlLayerMap = new HashMap<>();
    protected MarkerClickListener markerClickListener = null;
    protected MarkerDblClickListener markerDblClickListener = null;
    protected MarkerRightClickListener markerRightClickListener = null;
    protected MarkerDragListener markerDragListener = null;
    protected InfoWindowClosedListener infoWindowClosedListener = null;    
    
    protected PolygonClickListener polygonClickListener = null;
    protected PolygonDblClickListener polygonDblClickListener = null;
    protected PolygonRightClickListener polygonRightClickListener = null;
    protected PolygonCompleteListener polygonCompleteListener = null;
    
    protected PolylineClickListener polylineClickListener = null;
    protected PolylineDblClickListener polylineDblClickListener = null;
    protected PolylineRightClickListener polylineRightClickListener = null;
    protected PolylineCompleteListener polylineCompleteListener = null;
    
    protected EdgeClickListener edgeClickListener = null;
    protected EdgeCompleteListener edgeCompleteListener = null;
    protected EdgeRightClickListener edgeRightClickListener = null;
    
    protected Map<Marker, Long> markerDragCounter = new HashMap<>();
        
    protected MapMoveListener mapMoveListener = null;
    protected LatLngBounds allowedBoundsCenter = null;
    protected LatLngBounds allowedBoundsVisibleArea = null;

    protected MapClickListener mapClickListener = null;
    protected MapDblClickListener mapDblClickListener = null;
    protected MapRightClickListener mapRightClickListener = null;
    protected MapTypeChangeListener mapTypeChangeListener = null;
    
    protected MapMouseOverListener mapMouseOverListener = null;

    protected boolean forceBoundUpdate = false;
    protected boolean mapOptionsChanged = false;
    protected boolean panningNeeded = false;
    protected TrafficLayer trafficLayer = null;
    
    private GoogleMapMarker sourceTempPolyline = null;
    protected Polyline tempPolyline;
        
    private Map<GoogleMapMarker, List<GoogleMapPolyline>> sourceNodeEdges = new HashMap<>();
    private Map<GoogleMapMarker, List<GoogleMapPolyline>> targetNodeEdges = new HashMap<>();
    private Map<GoogleMapPolyline, List<GoogleMapMarker>> edgeNodes = new HashMap<>();
    
    private Map<Marker, GoogleGwtMapLabel> markerLabelsMap = new HashMap<>();
    private Map<Polyline, GoogleGwtMapLabel> polylineLabelsMap = new HashMap<>();
    private Map<GoogleGwtMapPolygon, GoogleGwtMapLabel> polygonLabelsMap = new HashMap<>();
    private Map<Polyline, GoogleGwtMapLabel> edgeLabelsMap = new HashMap<>();
    
    private boolean markerLabels = false;
    private boolean polylineLabels = false;
    private boolean polygonLabels = false;
    private boolean edgeLabels = false;
    
    private DrawingManager drawingManager;
    
    private boolean measureEdgeDistance = false;
    private FlowPanel mesaureEdgeDistancePanel = null;
    private HTML htmlEdgeDistance = null;
    
    private boolean measureDistance = false;
    private Polyline measurePolyline = null;
    private int lengthPath = 0;    
    private FlowPanel measureDistancePanel = null;
    private HTML htmlTotalDistance = null;
    
    public GoogleMapWidget() {
        setStyleName(CLASSNAME);
    }
    
    public final native void browserLog(Object obj) /*-{
        $wnd.console.log(obj);
    }-*/;

    public void initMap(LatLon center, int zoom, String mapTypeId) {

        mapOptions = MapOptions.newInstance();
        mapOptions.setMapTypeId(MapTypeId.fromValue(mapTypeId.toLowerCase()));
        mapOptions.setCenter(
            LatLng.newInstance(center.getLat(), center.getLon()));
        mapOptions.setZoom(zoom);        
        
        mapImpl = MapImpl.newInstance(getElement(), mapOptions);

        map = MapWidget.newInstance(mapImpl);
        
        mapImpl.addMouseOverHandler(new MouseOverMapHandler(){

            @Override
            public void onEvent(MouseOverMapEvent event) {
                if (mapMouseOverListener != null) {
                    LatLon position = new LatLon(
                            event.getMouseEvent().getLatLng().getLatitude(),
                            event.getMouseEvent().getLatLng().getLongitude());
                    mapMouseOverListener.mapMouseOver(position);
                }
            }
        });
        mapImpl.addMouseMoveHandler(new MouseMoveMapHandler() {

            @Override
            public void onEvent(MouseMoveMapEvent event) {
                if (sourceTempPolyline != null) {
                    LatLng position = event.getMouseEvent().getLatLng();
                    if (tempPolyline.getPath().getLength() == 1)
                        tempPolyline.getPath().push(position);
                    else
                        tempPolyline.getPath().setAt(1, position);
                }
                
                if (measureDistance) {
                    if (lengthPath > 0) {
                        if (measurePolyline.getPath().getLength() == lengthPath) {
                            measurePolyline.getPath().push(event.getMouseEvent().getLatLng());
                        } else {
                            measurePolyline.getPath().pop();
                            measurePolyline.getPath().push(event.getMouseEvent().getLatLng());
                        }
                        updateTotalDistanceHTML(htmlTotalDistance, 
                            SphericalUtils.computeLength(measurePolyline.getPath()));
                    }
                }
            }
        });
        // always when center has changed, check that it does not go out from
        // the given bounds
        mapImpl.addCenterChangeHandler(new CenterChangeMapHandler() {
            @Override
            public void onEvent(CenterChangeMapEvent event) {
                forceBoundUpdate = checkVisibleAreaBoundLimits();
                forceBoundUpdate = checkCenterBoundLimits();
            }
        });

        // do all updates when the map has stopped moving
        mapImpl.addIdleHandler(new IdleMapHandler() {
            @Override
            public void onEvent(IdleMapEvent event) {
                updateBounds(forceBoundUpdate);
            }
        });        

        mapImpl.addClickHandler(new ClickMapHandler() {
            @Override
            public void onEvent(ClickMapEvent event) {
                if (mapClickListener != null) {
                    LatLon position = new LatLon(
                        event.getMouseEvent().getLatLng().getLatitude(),
                        event.getMouseEvent().getLatLng().getLongitude());
                    mapClickListener.mapClicked(position);
                }
                
                if (measureDistance) {
                    if (measurePolyline == null || measurePolyline.getPath().getLength() == 0) {
                        initializeMeasurePolyline();
                        measurePolyline.getPath().push(event.getMouseEvent().getLatLng());
                        lengthPath = 1;
                    }
                }
            }
        });

        mapImpl.addMapTypeIdChangeHandler(new MapTypeIdChangeMapHandler() {
            @Override
            public void onEvent(MapTypeIdChangeMapEvent event) {
                MapTypeId id = mapImpl.getMapTypeId();
                if (mapTypeChangeListener != null) {
                    mapTypeChangeListener.mapTypeChanged(id);
                }
            }
        });
        
        mapImpl.addDblClickHandler(new DblClickMapHandler() {

            @Override
            public void onEvent(DblClickMapEvent event) {
                if (mapDblClickListener != null) {
                    LatLon position = new LatLon(
                        event.getMouseEvent().getLatLng().getLatitude(),
                        event.getMouseEvent().getLatLng().getLongitude());
                    mapDblClickListener.mapDblClicked(position);
                }
            }
        });
        
        mapImpl.addRightClickHandler(new RightClickMapHandler() {

            @Override
            public void onEvent(RightClickMapEvent event) {
                if (mapRightClickListener != null) {                    
                    LatLon position = new LatLon(
                        event.getMouseEvent().getLatLng().getLatitude(),
                        event.getMouseEvent().getLatLng().getLongitude());
                    mapRightClickListener.mapRightClicked(position);
                }
            }
        });
        
        drawingTool();
    }

    private boolean checkVisibleAreaBoundLimits() {
        if (allowedBoundsVisibleArea == null) {
            return false;
        }
        double newCenterLat = map.getCenter().getLatitude();
        double newCenterLng = map.getCenter().getLongitude();

        LatLng mapNE = map.getBounds().getNorthEast();
        LatLng mapSW = map.getBounds().getSouthWest();

        LatLng limitNE = allowedBoundsVisibleArea.getNorthEast();
        LatLng limitSW = allowedBoundsVisibleArea.getSouthWest();

        double mapWidth = mapNE.getLongitude() - mapSW.getLongitude();
        double mapHeight = mapNE.getLatitude() - mapSW.getLatitude();

        double maxWidth = limitNE.getLongitude() - limitSW.getLongitude();
        double maxHeight = limitNE.getLatitude() - limitSW.getLatitude();

        if (mapWidth > maxWidth) {
            newCenterLng = allowedBoundsVisibleArea.getCenter().getLongitude();
        } else if (mapNE.getLongitude() > limitNE.getLongitude()) {
            newCenterLng -= (mapNE.getLongitude() - limitNE.getLongitude());
        } else if (mapSW.getLongitude() < limitSW.getLongitude()) {
            newCenterLng += (limitSW.getLongitude() - mapSW.getLongitude());
        }

        if (mapHeight > maxHeight) {
            newCenterLat = allowedBoundsVisibleArea.getCenter().getLatitude();
        } else if (mapNE.getLatitude() > limitNE.getLatitude()) {
            newCenterLat -= (mapNE.getLatitude() - limitNE.getLatitude());
        } else if (mapSW.getLatitude() < limitSW.getLatitude()) {
            newCenterLat += (limitSW.getLatitude() - mapSW.getLatitude());
        }

        LatLng newCenter = LatLng.newInstance(newCenterLat, newCenterLng);
        if (!newCenter.equals(map.getCenter())) {
            moveCenterTo(newCenter);
            return true;
        }

        return false;
    }

    protected void moveCenterTo(LatLng position) {
        if (!map.getCenter().equals(position)) {
            map.setCenter(position);
        }
    }
    
    private void updateBounds(boolean forceUpdate) {
        int zoom = mapOptions.getZoom();
        LatLng center = mapOptions.getCenter();

        if (forceUpdate || zoom != map.getZoom() || center == null
            || !center.equals(map.getCenter())) {
            zoom = map.getZoom();
            center = map.getCenter();
            mapOptions.setZoom(zoom);
            mapOptions.setCenter(center);
            mapOptionsChanged = true;

            if (mapMoveListener != null) {
                mapMoveListener.mapMoved(map.getZoom(),
                    new LatLon(map.getCenter().getLatitude(),
                        map.getCenter().getLongitude()),
                    new LatLon(map.getBounds().getNorthEast().getLatitude(),
                        map.getBounds().getNorthEast().getLongitude()),
                    new LatLon(map.getBounds().getSouthWest().getLatitude(),
                        map.getBounds().getSouthWest().getLongitude()));
            }
        }
        updateOptionsAndPanning();
    }

    public void updateOptionsAndPanning() {
        if (panningNeeded) {
            map.panTo(mapOptions.getCenter());
            map.setZoom(mapOptions.getZoom());
            panningNeeded = false;
        }
        if (mapOptionsChanged) {
            map.setOptions(mapOptions);
            mapOptionsChanged = false;
        }
    }

    private boolean checkCenterBoundLimits() {
        LatLng center = map.getCenter();
        if (allowedBoundsCenter == null
            || allowedBoundsCenter.contains(center)) {
            return false;
        }

        double lat = center.getLatitude();
        double lng = center.getLongitude();

        LatLng northEast = allowedBoundsCenter.getNorthEast();
        LatLng southWest = allowedBoundsCenter.getSouthWest();
        if (lat > northEast.getLatitude()) {
            lat = northEast.getLatitude();
        }
        if (lng > northEast.getLongitude()) {
            lng = northEast.getLongitude();
        }
        if (lat < southWest.getLatitude()) {
            lat = southWest.getLatitude();
        }
        if (lng < southWest.getLongitude()) {
            lng = southWest.getLongitude();
        }

        LatLng newCenter = LatLng.newInstance(lat, lng);
        moveCenterTo(newCenter);
        return true;
    }

    public void setCenter(LatLng center) {
        if (map.getCenter().equals(center)) {
            return;
        }

        mapOptions.setCenter(center);
        mapOptionsChanged = true;
        panningNeeded = true;
    }

    private List<GoogleMapMarker> getRemovedMarkers(
        Collection<GoogleMapMarker> newMarkers) {
        List<GoogleMapMarker> result = new ArrayList<>();

        for (GoogleMapMarker oldMarker : gmMarkerMap.keySet()) {
            if (!newMarkers.contains(oldMarker)) {
                result.add(oldMarker);
            }
        }
        return result;
    }    
    
    private void removeMarkers(List<GoogleMapMarker> markers) {
        for (GoogleMapMarker gmarker : markers) {

            Marker marker = gmMarkerMap.get(gmarker);
            marker.close();
            marker.setMap((MapWidget) null);

            markerMap.remove(marker);
            gmMarkerMap.remove(gmarker);
            
            markerLabelsMap.get(marker).setVisible(false);
            markerLabelsMap.remove(marker);
        }
    }

    public void setMarkers(Collection<GoogleMapMarker> markers) {
        if (markers.size() == markerMap.size()
            && markerMap.values().containsAll(markers)) {
            
            for (GoogleMapMarker googleMapMarker : markers) {
                updateMarker(googleMapMarker);
            }
            return;
        }

        List<GoogleMapMarker> removedMarkers = getRemovedMarkers(markers);
        removeMarkers(removedMarkers);

        for (GoogleMapMarker googleMapMarker : markers) {
            if (!gmMarkerMap.containsKey(googleMapMarker)) {

                final Marker marker = addMarker(googleMapMarker);
                
                if (!googleMapMarker.getCaption().equals("")) {
                    markerLabelsMap.put(marker, new GoogleGwtMapLabel(map, marker.getPosition(), googleMapMarker.getCaption()));
                    
                    markerLabelsMap.get(marker).setVisible(markerLabels);
                }
                
                markerMap.put(marker, googleMapMarker);
                gmMarkerMap.put(googleMapMarker, marker);
                
                // since some browsers can't handle clicks properly, needed to
                // define a click as a mousedown + mouseup that last less
                // than 150 ms
                marker.addMouseDownHandler(new MouseDownMapHandler() {
                    @Override
                    public void onEvent(MouseDownMapEvent event) {
                        markerDragCounter.put(marker, new Date().getTime());
                    }
                });

                marker.addMouseUpHandler(new MouseUpMapHandler() {
                    @Override
                    public void onEvent(MouseUpMapEvent event) {
                        Long timeWhenPressed = markerDragCounter.remove(marker);
                        Long currentTime = new Date().getTime();
                        if (currentTime - timeWhenPressed < 150) {
                            if (markerClickListener != null) {
                                markerClickListener
                                    .markerClicked(markerMap.get(marker));
                            }
                        }
                    }
                });
                
                marker.addDragEndHandler(new DragEndMapHandler() {
                    @Override
                    public void onEvent(DragEndMapEvent event) {
                        GoogleMapMarker gmMarker = markerMap.get(marker);
                        
                        LatLon oldPosition = gmMarker.getPosition();
                        
                        gmMarker.setPosition(
                            new LatLon(marker.getPosition().getLatitude(),
                                marker.getPosition().getLongitude()));
                        
                        nodeMoved(gmMarker, null);
                        
                        if (markerDragListener != null) {
                            markerDragListener.markerDragged(gmMarker,
                                oldPosition);
                        }
                    }
                });
                
                marker.addDblClickHandler(new DblClickMapHandler() {

                    @Override
                    public void onEvent(DblClickMapEvent event) {
                        if (markerDblClickListener != null) {
                                markerDblClickListener
                                    .markerDblClicked(markerMap.get(marker));
                            }
                    }
                });
                
                marker.addRightClickHandler(new RightClickMapHandler() {

                    @Override
                    public void onEvent(RightClickMapEvent event) {
                        if (markerRightClickListener != null) {
                            markerRightClickListener.markerRightClicked(markerMap.get(marker));
                        }
                    }
                });
            } else {
                updateMarker(googleMapMarker);
            }
        }
    }
        
    private void updateMarkerLabel(GoogleMapMarker gmMarker) {
        Marker marker = gmMarkerMap.get(gmMarker);
        
        if (markerLabelsMap.containsKey(marker)) {
            markerLabelsMap.get(marker).setVisible(false);
            markerLabelsMap.remove(marker);
            
            markerLabelsMap.put(marker, new GoogleGwtMapLabel(map, marker.getPosition(), gmMarker.getCaption()));
            markerLabelsMap.get(marker).setVisible(markerLabels);
        }
    }
 
    private void updateMarker(GoogleMapMarker newGmMarker) {
        Marker marker = gmMarkerMap.get(newGmMarker);
        GoogleMapMarker oldGmMarker = markerMap.get(marker);
        
        if (!oldGmMarker.hasSameFieldValues(newGmMarker)) {
            MarkerOptions options = createMarkerOptions(newGmMarker);
            marker.setOptions(options);
            
            nodeMoved(newGmMarker, null);
            updateMarkerLabel(newGmMarker);
        }
        gmMarkerMap.put(newGmMarker, marker);
        markerMap.put(marker, newGmMarker);
    }

    public void setMarkerClickListener(MarkerClickListener listener) {
        markerClickListener = listener;
    }
    
    public void setMarkerDblClickListener(MarkerDblClickListener listener) {
        markerDblClickListener = listener;
    }
    
    public void setMarkerRightClickListener(MarkerRightClickListener listener) {
        markerRightClickListener = listener;
    }
    
    public void setPolygonClickListener(PolygonClickListener listener) {
        polygonClickListener = listener;
    }
    
    public void setPolygonDblClickListener(PolygonDblClickListener listener) {
        polygonDblClickListener = listener;
    }
    
    public void setPolygonRightClickListener(PolygonRightClickListener listener) {
        polygonRightClickListener = listener;
    }
    
    public void setPolygonCompleteListener(PolygonCompleteListener listener) {
        polygonCompleteListener = listener;
    }
    
    public void setPolylineClickListener(PolylineClickListener listener) {
        polylineClickListener = listener;
    }
    
    public void setPolylineDblClickListener(PolylineDblClickListener listener) {
        polylineDblClickListener = listener;
    }
    
    public void setPolylineRightClickListener(PolylineRightClickListener listener) {
        polylineRightClickListener = listener;
    }
    
    public void setPolylineCompleteListener(PolylineCompleteListener listener) {
        polylineCompleteListener = listener;
    }
    
    public void setEdgeClickListener(EdgeClickListener listener) {
        edgeClickListener = listener;
    }
    
    public void setEdgeCompleteListener(EdgeCompleteListener listener) {
        edgeCompleteListener = listener;
    }
    
    public void setEdgeRightClickListener(EdgeRightClickListener listener) {
        edgeRightClickListener = listener;
    }

    public void setMapMoveListener(MapMoveListener listener) {
        mapMoveListener = listener;
    }

    public void setMapClickListener(MapClickListener listener) {
        mapClickListener = listener;
    }
    
    public void setMapDblClickListener(MapDblClickListener listener) {
        mapDblClickListener = listener;
    }
    
    public void setMapRightClickListener(MapRightClickListener listener) {
        mapRightClickListener = listener;
    }
    
    public void setMarkerDragListener(MarkerDragListener listener) {
        markerDragListener = listener;
    }

    public void setInfoWindowClosedListener(InfoWindowClosedListener listener) {
        infoWindowClosedListener = listener;
    }

    public void setMapTypeChangeListener(MapTypeChangeListener listener) {
        mapTypeChangeListener = listener;
    }
    
    public void setMapMouseOverListener(MapMouseOverListener listener) {
        mapMouseOverListener = listener;
    }
        
    private Marker addMarker(GoogleMapMarker googleMapMarker) {
        MarkerOptions options = createMarkerOptions(googleMapMarker);

        final Marker marker = Marker.newInstance(options);
        marker.setMap(map);

        return marker;
    }

    private MarkerOptions createMarkerOptions(GoogleMapMarker googleMapMarker) {
        LatLng center = LatLng.newInstance(
            googleMapMarker.getPosition().getLat(),
            googleMapMarker.getPosition().getLon());
        MarkerOptions options = MarkerOptions.newInstance();
        options.setPosition(center);
        options.setDraggable(googleMapMarker.isDraggable());
        options.setOptimized(googleMapMarker.isOptimized());
        options.setVisible(googleMapMarker.isVisible());
        
        if (googleMapMarker.isAnimationEnabled()) {
            options.setAnimation(Animation.DROP);
        }

        if (googleMapMarker.getIconUrl() != null) {
            options.setIcon(googleMapMarker.getIconUrl());
        }
        return options;
    }

    public double getZoom() {
        return map.getZoom();
    }

    public void setZoom(int zoom) {
        if (mapOptions.getZoom() == zoom) {
            return;
        }
        mapOptions.setZoom(zoom);
        mapOptionsChanged = true;
        panningNeeded = true;
    }

    public LatLng getCenter() {
        return map.getCenter();
    }

    public double getLatitude() {
        return map.getCenter().getLatitude();
    }

    public double getLongitude() {
        return map.getCenter().getLongitude();
    }

    public void setCenterBoundLimits(LatLon limitNE, LatLon limitSW) {
        allowedBoundsCenter = LatLngBounds.newInstance(
            LatLng.newInstance(limitSW.getLat(), limitSW.getLon()),
            LatLng.newInstance(limitNE.getLat(), limitNE.getLon()));
    }

    public void clearCenterBoundLimits() {
        allowedBoundsCenter = null;
    }

    public void setVisibleAreaBoundLimits(LatLon limitNE, LatLon limitSW) {
        allowedBoundsVisibleArea = LatLngBounds.newInstance(
            LatLng.newInstance(limitSW.getLat(), limitSW.getLon()),
            LatLng.newInstance(limitNE.getLat(), limitNE.getLon()));
    }

    public void clearVisibleAreaBoundLimits() {
        allowedBoundsVisibleArea = null;
    }
        
    private List<GoogleMapPolygon> getRemovedPolygons(Map<Long, GoogleMapPolygon> newPolyOverlays) {
        List<GoogleMapPolygon> result = new ArrayList<>();
        
        for (GoogleMapPolygon oldPolygon : gmPolygonMap.keySet()) {
            if (!newPolyOverlays.containsValue(oldPolygon)) {
                result.add(oldPolygon);
            }
        }
        return result;
    }
            
    private GoogleGwtMapPolygonOptions createPolygonOptions(GoogleMapPolygon gmPolygon) {
        //PolygonOptions options = PolygonOptions.newInstance();
        GoogleGwtMapPolygonOptions options = GoogleGwtMapPolygonOptions.extNewInstance();
        options.setDraggable(true);
        options.setFillColor(gmPolygon.getFillColor());
        options.setFillOpacity(gmPolygon.getFillOpacity());
        options.setGeodesic(gmPolygon.isGeodesic());
        options.setStrokeColor(gmPolygon.getStrokeColor());
        options.setStrokeOpacity(gmPolygon.getStrokeOpacity());
        options.setStrokeWeight(gmPolygon.getStrokeWeight());
        options.setVisible(gmPolygon.isVisible());
        options.setEditable(gmPolygon.isEditable());
        options.setZindex(gmPolygon.getzIndex());
        return options;
    }
    
    private void updatePolygonLabel(GoogleMapPolygon gmPolygon) {
        GoogleGwtMapPolygon polygon = gmPolygonMap.get(gmPolygon);
        
        polygonLabelsMap.get(polygon).setVisible(false);
        polygonLabelsMap.remove(polygon);
        
        polygonLabelsMap.put(polygon, new GoogleGwtMapLabel(map, polygon.getPath().get(0), gmPolygon.getCaption()));
        polygonLabelsMap.get(polygon).setVisible(polygonLabels);
    }
        
    private GoogleGwtMapPolygon addPolygon(final GoogleMapPolygon gmPolygon) {
        final MVCArray<LatLng> points = MVCArray.newInstance();
        for (LatLon latLon : gmPolygon.getCoordinates()) {
            LatLng latLng = LatLng.newInstance(latLon.getLat(), 
                    latLon.getLon());
            points.push(latLng);
        }
        
        points.addInsertAtHandler(new InsertAtMapHandler() {

            @Override
            public void onEvent(InsertAtMapEvent event) {
                setCoordinates(points, gmPolygon.getCoordinates());
                updatePolygonLabel(gmPolygon);
                
                if (polygonCompleteListener != null) {
                    polygonCompleteListener.polygonCompleted(gmPolygon);
                }
            }
        });
        
        points.addRemoveAtHandler(new RemoveAtMapHandler() {

            @Override
            public void onEvent(RemoveAtMapEvent event) {
                setCoordinates(points, gmPolygon.getCoordinates());
                updatePolygonLabel(gmPolygon);
                
                if (polygonCompleteListener != null) {
                    polygonCompleteListener.polygonCompleted(gmPolygon);
                }
            }
        });
        
        points.addSetAtHandler(new SetAtMapHandler() {

            @Override
            public void onEvent(SetAtMapEvent event) {
                setCoordinates(points, gmPolygon.getCoordinates());
                updatePolygonLabel(gmPolygon);
                
                if (polygonCompleteListener != null) {
                    polygonCompleteListener.polygonCompleted(gmPolygon);
                }
            }
        });
        
        GoogleGwtMapPolygonOptions options = createPolygonOptions(gmPolygon);
        
        final GoogleGwtMapPolygon polygon = GoogleGwtMapPolygon.extNewInstance(options);
        polygon.setPath(points);
        polygon.setMap(map);
        
        return polygon;
    }
    
    private void setPolygons(Map<Long, GoogleMapPolygon> polyOverlays) {
        for (GoogleGwtMapPolygon polygon : polygonMap.keySet())
            polygon.setMap((MapWidget) null);        
        polygonMap.clear();
        gmPolygonMap.clear();
        
        for (GoogleGwtMapPolygon polygon : polygonLabelsMap.keySet())
            polygonLabelsMap.get(polygon).setVisible(false);
        polygonLabelsMap.clear();
        
        for (GoogleMapPolygon gmPolygon : polyOverlays.values()) {
            
            final GoogleGwtMapPolygon polygon = addPolygon(gmPolygon);
            polygonMap.put(polygon, gmPolygon);
            gmPolygonMap.put(gmPolygon, polygon);
            
            if (!gmPolygon.getCaption().equals("")) {
                polygonLabelsMap.put(polygon, new GoogleGwtMapLabel(map, polygon.getPath().get(0), gmPolygon.getCaption()));
                polygonLabelsMap.get(polygon).setVisible(polygonLabels);
            }
                                    
            polygon.addClickHandler(new ClickMapHandler() {
                
                @Override
                public void onEvent(ClickMapEvent event) {
                    try {
                        if (event.getProperties().getNumber("vertex") == null) {
                            polygon.setEditable(!polygon.getEditable());
                            polygonMap.get(polygon).setEditable(polygon.getEditable());
                            
                            if (polygonClickListener != null) {
                                polygonClickListener.polygonClicked(polygonMap.get(polygon));
                            }
                        }
                    } catch (Properties.TypeException ex) {
                        browserLog("com.vaadin.tapio.googlemaps.client.GoogleMapWidget " + ex.getMessage());
                    }
                }
            });
            
            polygon.addDblClickHandler(new DblClickMapHandler() {
                
                @Override
                public void onEvent(DblClickMapEvent event) {
                    try {
                        if (event.getProperties().getNumber("vertex") != null) {
                            
                            int vetexIndex = event.getProperties().getNumber("vertex").intValue();
                            polygon.getPath().removeAt(vetexIndex);
                        }
                    } catch (Properties.TypeException ex) {
                        browserLog("com.vaadin.tapio.googlemaps.client.GoogleMapWidget " + ex.getMessage());
                    }
                    /*
                    if (polygonDblClickListener != null) {
                        polygonDblClickListener.polygonDblClicked(polygonMap.get(polygon));
                    }
                    */
                }
            });
                        
            polygon.addRightClickHandler(new RightClickMapHandler() {
                
                @Override
                public void onEvent(RightClickMapEvent event) {
                    if (polygonRightClickListener != null) {
                        polygonRightClickListener.polygonRightClicked(polygonMap.get(polygon));
                    }
                }
            });
        }
    }    
        
    public void setPolygonOverlays(Map<Long, GoogleMapPolygon> polyOverlays) {
        if (polygonMap.size() == polyOverlays.size() &&
                polygonMap.values().containsAll(polyOverlays.values()))
            return;
        
        setPolygons(polyOverlays);
    }
        
    private List<GoogleMapPolyline> getRemovedPolylines(Map<Long, GoogleMapPolyline> newPolylines) {
        List<GoogleMapPolyline> result = new ArrayList<>();
        
        for (GoogleMapPolyline oldPolyline : gmPolylineMap.keySet())
            if (!newPolylines.containsValue(oldPolyline))
                result.add(oldPolyline);
        
        return result;
    }
    
    private PolylineOptions createPolylineOptions(GoogleMapPolyline gmPolyline) {
        PolylineOptions options = PolylineOptions.newInstance();
        options.setGeodesic(gmPolyline.isGeodesic());
        options.setStrokeColor(gmPolyline.getStrokeColor());
        options.setStrokeOpacity(gmPolyline.getStrokeOpacity());
        options.setStrokeWeight(gmPolyline.getStrokeWeight());
        options.setVisible(gmPolyline.isVisible());        
        options.setZindex(gmPolyline.getzIndex());
        return options;
    }
    
    private void setCoordinates(MVCArray<LatLng> points, List<LatLon> coordinates) {
        coordinates.clear();
                
        for (int i = 0; i < points.getLength(); i += 1) {
                        
            LatLon coordinate = new LatLon(
                points.get(i).getLatitude(),
                points.get(i).getLongitude());
            
            coordinates.add(coordinate);
        }
    }
    
    private Polyline addPolyline(final GoogleMapPolyline gmPolyline) {
        final MVCArray<LatLng> points = MVCArray.newInstance();
        
        for (LatLon latLon : gmPolyline.getCoordinates()) {
            LatLng latLng = LatLng.newInstance(latLon.getLat(), 
                    latLon.getLon());
            points.push(latLng);
        }
        
        PolylineOptions options = createPolylineOptions(gmPolyline);
        
        final Polyline polyline = Polyline.newInstance(options);
        polyline.setPath(points);
        polyline.setMap(map);
        polyline.setEditable(gmPolyline.isEditable());
        
        return polyline;
    }
                
    private void setPolyline(GoogleMapPolyline gmPolyline) {
        final Polyline polyline = addPolyline(gmPolyline);
        polylineMap.put(polyline, gmPolyline);
        gmPolylineMap.put(gmPolyline, polyline);
        
        if (!gmPolyline.getCaption().equals("")) {
            LatLng midpoint = polylineMidpoint(polyline);
            
            polylineLabelsMap.put(polyline, new GoogleGwtMapLabel(map, midpoint, gmPolyline.getCaption()));
            polylineLabelsMap.get(polyline).setVisible(polylineLabels);
        }
        
        polyline.addClickHandler(new ClickMapHandler() {
            
            @Override
            public void onEvent(ClickMapEvent event) {
                if (polylineClickListener != null) {
                    polylineClickListener.polylineClicked(polylineMap.get(polyline));
                }
            }
        });
        
        polyline.addDblClickHandler(new DblClickMapHandler() {
            
            @Override
            public void onEvent(DblClickMapEvent event) {
                if (polylineDblClickListener != null) {
                    polylineDblClickListener.polylineDblClicked(polylineMap.get(polyline));
                }
            }
        });
        
        polyline.addRightClickHandler(new RightClickMapHandler() {
            
            @Override
            public void onEvent(RightClickMapEvent event) {
                if (polylineRightClickListener != null) {
                    polylineRightClickListener.polylineRightClicked(polylineMap.get(polyline));
                }
            }
        });
    }
    
    private void setPolylines(Map<String, GoogleMapPolyline> polylineOverlays) {
        for (Polyline polyline : polylineMap.keySet())
            polyline.setMap((MapWidget) null);
        
        polylineMap.clear();
        gmPolylineMap.clear();
        
        for (Polyline polyline : polylineLabelsMap.keySet())
            polylineLabelsMap.get(polyline).setVisible(false);
        polylineLabelsMap.clear();
        
        for (GoogleMapPolyline gmPolyline : polylineOverlays.values()) {
            setPolyline(gmPolyline);
        }
    }
    
    public void setPolylineOverlays(Map<String, GoogleMapPolyline> polylineOverlays) {
        if (polylineOverlays.size() == polylineMap.size() 
                && polylineMap.values().containsAll(polylineOverlays.values()))
            return;
        
        setPolylines(polylineOverlays);
    }
    
    public void setKmlLayers(Collection<GoogleMapKmlLayer> layers) {

        // no update needed if old layers match
        if (kmlLayerMap.size() == layers.size()
            && kmlLayerMap.values().containsAll(layers)) {
            return;
        }

        for (KmlLayer kmlLayer : kmlLayerMap.keySet()) {
            kmlLayer.setMap(null);
        }
        kmlLayerMap.clear();

        for (GoogleMapKmlLayer gmLayer : layers) {
            KmlLayerOptions options = KmlLayerOptions.newInstance();
            options.setClickable(gmLayer.isClickable());
            options.setPreserveViewport(gmLayer.isViewportPreserved());
            options.setSuppressInfoWindows(
                gmLayer.isInfoWindowRenderingDisabled());

            KmlLayer kmlLayer = KmlLayer.newInstance(gmLayer.getUrl(), options);
            kmlLayer.setMap(map);

            kmlLayerMap.put(kmlLayer, gmLayer);
        }
    }

    public void setMapType(String mapTypeId) {
        MapTypeId id = MapTypeId.fromValue(mapTypeId.toLowerCase());
        if (id == mapOptions.getMapTypeId()) {
            return;
        }
        mapOptions.setMapTypeId(MapTypeId.fromValue(mapTypeId.toLowerCase()));

        // avoid infinite loops
        if (id != mapImpl.getMapTypeId()) {
            mapOptionsChanged = true;
        }

    }

    public void setControls(Set<GoogleMapsComponentControl> controls) {

        // check if there's been a real change in selected controls
        Set<GoogleMapsComponentControl> currentControls = new HashSet<>();
        if (mapOptions.getMapTypeControl()) {
            currentControls.add(GoogleMapsComponentControl.MapType);
        }
        if (mapOptions.getOverviewMapControl()) {
            currentControls.add(GoogleMapsComponentControl.OverView);
        }
        if (mapOptions.getPanControl()) {
            currentControls.add(GoogleMapsComponentControl.Pan);
        }
        if (mapOptions.getRotateControl()) {
            currentControls.add(GoogleMapsComponentControl.Rotate);
        }
        if (mapOptions.getScaleControl()) {
            currentControls.add(GoogleMapsComponentControl.Scale);
        }
        if (mapOptions.getStreetViewControl()) {
            currentControls.add(GoogleMapsComponentControl.StreetView);
        }
        if (mapOptions.getZoomControl()) {
            currentControls.add(GoogleMapsComponentControl.Zoom);
        }

        if (controls.size() == currentControls.size()
            && currentControls.containsAll(controls)) {
            return;
        }

        mapOptions
            .setMapTypeControl(controls.contains(GoogleMapsComponentControl.MapType));
        mapOptions.setOverviewMapControl(
            controls.contains(GoogleMapsComponentControl.OverView));
        mapOptions.setPanControl(controls.contains(GoogleMapsComponentControl.Pan));
        mapOptions.setRotateControl(controls.contains(GoogleMapsComponentControl.Rotate));
        mapOptions.setScaleControl(controls.contains(GoogleMapsComponentControl.Scale));
        mapOptions.setStreetViewControl(
            controls.contains(GoogleMapsComponentControl.StreetView));
        mapOptions.setZoomControl(controls.contains(GoogleMapsComponentControl.Zoom));
        mapOptionsChanged = true;

    }

    public void setDraggable(boolean draggable) {
        if (mapOptions.getDraggable() == draggable) {
            return;
        }

        mapOptions.setDraggable(draggable);
        mapOptionsChanged = true;
    }

    public void setKeyboardShortcutsEnabled(boolean keyboardShortcutsEnabled) {
        if (mapOptions.getKeyboardShortcuts() == keyboardShortcutsEnabled) {
            return;
        }
        mapOptions.setKeyboardShortcuts(keyboardShortcutsEnabled);
        mapOptionsChanged = true;
    }

    public void setScrollWheelEnabled(boolean scrollWheelEnabled) {
        if (mapOptions.getScrollWheel() == scrollWheelEnabled) {
            return;
        }
        mapOptions.setScrollWheel(scrollWheelEnabled);
        mapOptionsChanged = true;
    }
    
    public void setDisableDoubleClickZoom(boolean disableDoubleClickZoom) {
        if (mapOptions.getDisableDoubleClickZoom() == disableDoubleClickZoom)
            return;
        mapOptions.setDisableDoubleClickZoom(disableDoubleClickZoom);
        mapOptionsChanged = true;
    }

    public void setMinZoom(int minZoom) {
        if (mapOptions.getMinZoom() == minZoom) {
            return;
        }
        mapOptions.setMinZoom(minZoom);
        mapOptionsChanged = true;
    }

    public void setMaxZoom(int maxZoom) {
        if (mapOptions.getMaxZoom() == maxZoom) {
            return;
        }
        mapOptions.setMaxZoom(maxZoom);
        mapOptionsChanged = true;
    }

    public MapWidget getMap() {
        return map;
    }

    public void triggerResize() {
        AnimationScheduler.get().requestAnimationFrame(new AnimationScheduler.AnimationCallback() {
            @Override
            public void execute(double timestamp) {
                Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        map.triggerResize();
                        map.setZoom(mapOptions.getZoom());
                        map.setCenter(mapOptions.getCenter());
                    }
                });
            }
        });
    }

    InfoWindowOptions createInfoWindowOptions(GoogleMapInfoWindow gmWindow) {
        InfoWindowOptions options = InfoWindowOptions.newInstance();
        options.setDisableAutoPan(gmWindow.isAutoPanDisabled());
        if (gmWindow.getMaxWidth() != null) {
            options.setMaxWidth(gmWindow.getMaxWidth());
        }
        if (gmWindow.getPixelOffsetHeight() != null
                && gmWindow.getPixelOffsetWidth() != null) {
            options.setPixelOffet(
                    Size.newInstance(gmWindow.getPixelOffsetWidth(),
                            gmWindow.getPixelOffsetHeight()));
        }
        if (gmWindow.getPosition() != null) {
            options.setPosition(
                    LatLng.newInstance(gmWindow.getPosition().getLat(),
                            gmWindow.getPosition().getLon()));
        }
        if (gmWindow.getzIndex() != null) {
            options.setZindex(gmWindow.getzIndex());
        }
        return options;
    }

    DivElement createContentFromGMWindow(GoogleMapInfoWindow gmWindow) {
        String content = gmWindow.getContent();

        // wrap the contents inside a div if there's a defined width or
        // height
        if (gmWindow.getHeight() != null
                || gmWindow.getWidth() != null) {
            StringBuilder contentWrapper = new StringBuilder(
                    "<div style=\"");
            if (gmWindow.getWidth() != null) {
                contentWrapper.append("width:");
                contentWrapper.append(gmWindow.getWidth());
                contentWrapper.append(";");
            }
            if (gmWindow.getHeight() != null) {
                contentWrapper.append("height:");
                contentWrapper.append(gmWindow.getHeight());
                contentWrapper.append(";");
            }
            contentWrapper.append("\" >");
            contentWrapper.append(content);
            contentWrapper.append("</div>");
            content = contentWrapper.toString();
        }
        

        DivElement div = Document.get().createDivElement();
        div.setInnerHTML(content);
        return div;
    }

    public void setInfoWindows(Collection<GoogleMapInfoWindow> infoWindows) {

        for(GoogleMapInfoWindow gmWindow : infoWindows) {
            InfoWindowOptions options = createInfoWindowOptions(gmWindow);

            CustomInfoWindow window;

            if(!gmInfoWindowMap.containsKey(gmWindow)) {
                // Create new InfoWindow with contents
                final CustomInfoWindow w = window = new CustomInfoWindow();
                window.setContent(createContentFromGMWindow(gmWindow));
                window.addCloseClickListener(new CloseClickMapHandler() {
                    @Override
                    public void onEvent(CloseClickMapEvent event) {
                        if (infoWindowClosedListener != null) {
                            infoWindowClosedListener
                                    .infoWindowClosed(infoWindowMap.get(w));
                        }
                    }
                });
                adopt(window);

                // Register new info window
                infoWindowMap.put(window,gmWindow);
                gmInfoWindowMap.put(gmWindow,window);
                infoWindowIDs.put(gmWindow.getId(),window);
            } else {
                // Get reference to old window and close it
                window = gmInfoWindowMap.get(gmWindow);
                window.close();
            }

            // Open the window
            if (gmMarkerMap.containsKey(gmWindow.getAnchorMarker())) {
                window.open(options, map, gmMarkerMap.get(gmWindow.getAnchorMarker()));
            } else {
                window.open(options, map);
            }
        }
    }

    public void fitToBounds(LatLon boundsNE, LatLon boundsSW) {
        LatLng ne = LatLng.newInstance(boundsNE.getLat(), boundsNE.getLon());
        LatLng sw = LatLng.newInstance(boundsSW.getLat(), boundsSW.getLon());

        LatLngBounds bounds = LatLngBounds.newInstance(sw, ne);
        final LatLngBounds mapBounds = map.getBounds();
        if (mapBounds != null && mapBounds.equals(bounds)) {
            return;
        }
        map.fitBounds(bounds);
        updateBounds(false);
    }

    @Override
    public void onResize() {
        triggerResize();
    }

    public void setTrafficLayerVisible(boolean trafficLayerVisible) {
        if (trafficLayerVisible) {
            if (trafficLayer == null) {
                trafficLayer = TrafficLayer.newInstance();
            }
            trafficLayer.setMap(map);
        } else {
            if (trafficLayer != null) {
                trafficLayer.setMap(null);
            }
            trafficLayer = null;
        }
    }


    public void setInfoWindowContents(Map<Long, Widget> contents) {
        for(long id : contents.keySet()) {
            CustomInfoWindow win = infoWindowIDs.get(id);
            Widget w = contents.get(id);
            if(win != null && win.getContents() != w) {
                win.setContent(w);
            }
        }
    }
    
    public void setDrawingMode(String drawingMode) {
        if (drawingMode == null) {
            drawingManager.setDrawingMode(null);
            
            mapOptions.setDraggableCursor(null);
            mapOptions.setDraggingCursor(null);
            map.setOptions(mapOptions);
            mapOptionsChanged = false;
            return;
        }
        
        if ("Polyline".equals(drawingMode)) {
            mapOptions.setDraggableCursor("crosshair");
            mapOptions.setDraggingCursor("crosshair");
            map.setOptions(mapOptions);
            mapOptionsChanged = false;
            
            return;
        }
        
        if ("Polygon".equals(drawingMode)) {
            drawingManager.setDrawingMode(OverlayType.POLYGON);
            mapOptions.setDraggableCursor("crosshair");
            mapOptions.setDraggingCursor("crosshair");
            map.setOptions(mapOptions);
            mapOptionsChanged = false;
        }
    }
    
    private void drawingTool() {
        DrawingControlOptions drawingControlOptions = DrawingControlOptions.newInstance();
        drawingControlOptions.setPosition(ControlPosition.TOP_CENTER);
        drawingControlOptions.setDrawingModes(new OverlayType[]{OverlayType.POLYGON, OverlayType.POLYLINE});
        
        DrawingManagerOptions drawingManagerOptions = DrawingManagerOptions.newInstance();
        drawingManagerOptions.setMap(map);
        drawingManagerOptions.setDrawingControl(false);
        drawingManagerOptions.setDrawingControlOptions(drawingControlOptions);
        
        drawingManager = DrawingManager.newInstance(drawingManagerOptions);
        
        drawingManager.addPolygonCompleteHandler(new PolygonCompleteMapHandler() {

            @Override
            public void onEvent(PolygonCompleteMapEvent event) {
                Polygon polygon = event.getPolygon();
                
                List<LatLon> coordinates = new ArrayList();
                
                setCoordinates(polygon.getPath(), coordinates);
                polygon.setMap((MapWidget) null);
                
                if (polygonCompleteListener != null) {
                    GoogleMapPolygon gmPolygon = new GoogleMapPolygon();
                    gmPolygon.setCoordinates(coordinates);
                    polygonCompleteListener.polygonCompleted(gmPolygon);
                }
            }
        });
    }
    
    public void showMarkerLabels(boolean showLabels) {
        markerLabels = showLabels;
        
        for (Marker marker : markerLabelsMap.keySet()) {
            if (markerLabels && !marker.getVisible()) {
                markerLabelsMap.get(marker).setVisible(false);
                continue;
            }
            markerLabelsMap.get(marker).setVisible(markerLabels);
        }
    }
    
    public void showPolygonLabels(boolean showLabels) {
        polygonLabels = showLabels;
        
        for (GoogleGwtMapPolygon polygon : polygonLabelsMap.keySet()) {
            if (polygonLabels && !polygon.getVisible()) {
                polygonLabelsMap.get(polygon).setVisible(false);
                continue;
            }
            polygonLabelsMap.get(polygon).setVisible(polygonLabels);
        }
    }
    
    public void showPolylineLabels(boolean showLabels) {
        polylineLabels = showLabels;
        
        for (Polyline polyline : polylineLabelsMap.keySet()) {
            polylineLabelsMap.get(polyline).setVisible(polylineLabels);
        }
    }
    
    public void showEdgeLabels(boolean showLabels) {
        edgeLabels = showLabels;
        
        for (Polyline edge : edgeLabelsMap.keySet()) {
            
            GoogleMapPolyline gmEdge = edgeMap.get(edge);
            
            if (edgeLabels && !gmEdge.isVisible()) {
                edgeLabelsMap.get(edge).setVisible(false);
                continue;
            }
            edgeLabelsMap.get(edge).setVisible(edgeLabels);
        }
    }
            
    private LatLng polylineMidpoint(Polyline polyline) {
        int ncoordinates = polyline.getPath().getLength();
        
        if (ncoordinates % 2 == 0) {
            int p2Idx = ncoordinates / 2;
            int p1Idx = p2Idx - 1;
            
            LatLng p1 = polyline.getPath().get(p1Idx);
            LatLng p2 = polyline.getPath().get(p2Idx);
            
            return SphericalUtils.computeOffset(p1, 
                    (int) SphericalUtils.computeDistanceBetween(p1, p2) / 2, 
                    (int) SphericalUtils.computeHeading(p1, p2));
        }
        else {
            return polyline.getPath().get((int) (ncoordinates / 2));
        }
    }
        
    public void setSource(GoogleMapMarker source) {
        this.sourceTempPolyline = source;     
        if (source == null) {
            if (tempPolyline != null) {
                tempPolyline.getPath().clear();
                tempPolyline.setMap((MapWidget) null);
            }
        }
        else {
            if (tempPolyline == null) {
                PolylineOptions options = PolylineOptions.newInstance();
                options.setStrokeColor("black");
                options.setStrokeOpacity(1);
                options.setStrokeWeight(3);

                tempPolyline = Polyline.newInstance(options);
                
                tempPolyline.addClickHandler(new ClickMapHandler() {

                    @Override
                    public void onEvent(ClickMapEvent event) {
                        GoogleMapPolyline gmPolyline = new GoogleMapPolyline();
                        gmPolyline.setId("-1");
                        polylineClickListener.polylineClicked(gmPolyline);
                    }
                });
            }
            LatLng latLng = LatLng.newInstance(
                source.getPosition().getLat(), 
                source.getPosition().getLon());
            
            MVCArray<LatLng> points = MVCArray.newInstance();
            points.push(latLng);
            
            tempPolyline.setPath(points);
            tempPolyline.setMap(map);
        }
    }
        
    public void setEdges(Map<GoogleMapPolyline, List<GoogleMapMarker>> newGmEdges) {
        for (GoogleMapPolyline gmEdge : edgeNodes.keySet()) {
            gmEdgeMap.get(gmEdge).setMap((MapWidget) null);
        }
        edgeNodes.clear();
        sourceNodeEdges.clear();
        targetNodeEdges.clear();
        
        for (Polyline label : edgeLabelsMap.keySet())
            edgeLabelsMap.get(label).setVisible(false);
        edgeLabelsMap.clear();
        
        gmEdgeMap.clear();
        edgeMap.clear();
                
        for (GoogleMapPolyline newGmEdge : newGmEdges.keySet()) {
            setEdge(newGmEdge);
            
            GoogleMapMarker sourceNode = newGmEdges.get(newGmEdge).get(0);
            GoogleMapMarker targetNode = newGmEdges.get(newGmEdge).get(1);
            
            if (!sourceNodeEdges.containsKey(sourceNode))
                sourceNodeEdges.put(sourceNode, new ArrayList());
            if (!targetNodeEdges.containsKey(targetNode))
                targetNodeEdges.put(targetNode, new ArrayList());
            
            edgeNodes.put(newGmEdge, new ArrayList());
            
            sourceNodeEdges.get(sourceNode).add(newGmEdge);
            targetNodeEdges.get(targetNode).add(newGmEdge);
            
            edgeNodes.get(newGmEdge).add(sourceNode);
            edgeNodes.get(newGmEdge).add(targetNode);
        }
    }
            
    private void updateEdgeLabel(GoogleMapPolyline gmEdge) {
        LatLng midpoint = polylineMidpoint(gmEdgeMap.get(gmEdge));
        
        edgeLabelsMap.get(gmEdgeMap.get(gmEdge)).setVisible(false);
        edgeLabelsMap.remove(gmEdgeMap.get(gmEdge));
        
        edgeLabelsMap.put(gmEdgeMap.get(gmEdge), new GoogleGwtMapLabel(map, midpoint, gmEdge.getCaption()));
        edgeLabelsMap.get(gmEdgeMap.get(gmEdge)).setVisible(edgeLabels);
    }
    
    private Polyline addEdge(final GoogleMapPolyline gmEdge) {
        
        final MVCArray<LatLng> points = MVCArray.newInstance();
        
        for (LatLon coordinate : gmEdge.getCoordinates()) {
            LatLng point = LatLng.newInstance(
                    coordinate.getLat(), coordinate.getLon());
            points.push(point);
        }
        
        points.addInsertAtHandler(new InsertAtMapHandler() {

            @Override
            public void onEvent(InsertAtMapEvent event) {
                setCoordinates(points, gmEdge.getCoordinates());
                updateEdgeLabel(gmEdge);
                
                if (measureEdgeDistance) {
                    updateTotalDistanceHTML(htmlEdgeDistance, 
                        SphericalUtils.computeLength(points));
                }
                if (edgeCompleteListener != null) {
                    edgeCompleteListener.edgeCompleted(gmEdge);
                }
            }
        });
        
        points.addRemoveAtHandler(new RemoveAtMapHandler() {

            @Override
            public void onEvent(RemoveAtMapEvent event) {
                setCoordinates(points, gmEdge.getCoordinates());
                updateEdgeLabel(gmEdge);
                
                if (measureEdgeDistance) {
                    updateTotalDistanceHTML(htmlEdgeDistance, 
                        SphericalUtils.computeLength(points));
                }
                if (edgeCompleteListener != null) {
                    edgeCompleteListener.edgeCompleted(gmEdge);
                }
            }
        });
        
        points.addSetAtHandler(new SetAtMapHandler() {

            @Override
            public void onEvent(SetAtMapEvent event) {
                setCoordinates(points, gmEdge.getCoordinates());
                updateEdgeLabel(gmEdge);
                
                if (event.getIndex() == 0)
                    firstControlPointMoved(gmEdge);
                
                if (event.getIndex() == points.getLength() - 1)
                    lastControlPointMoved(gmEdge);
                
                if (measureEdgeDistance) {
                    updateTotalDistanceHTML(htmlEdgeDistance, 
                        SphericalUtils.computeLength(points));
                }
                if (edgeCompleteListener != null) {
                    edgeCompleteListener.edgeCompleted(gmEdge);
                }
            }
        });
        
        PolylineOptions edgeOptions = createPolylineOptions(gmEdge);
        
        Polyline edge = Polyline.newInstance(edgeOptions);
        edge.setPath(points);
        edge.setMap(map);
        edge.setEditable(gmEdge.isEditable());
                
        return edge;
    }
        
    private void setEdge(GoogleMapPolyline gmEdge) {
        final Polyline edge = addEdge(gmEdge);
        edgeMap.put(edge, gmEdge);
        gmEdgeMap.put(gmEdge, edge);
        
        if (!gmEdge.getCaption().equals("")) {
            LatLng midpoint = polylineMidpoint(edge);
            
            edgeLabelsMap.put(edge, new GoogleGwtMapLabel(map, midpoint, gmEdge.getCaption()));
            edgeLabelsMap.get(edge).setVisible(edgeLabels);
        }
        
        edge.addClickHandler(new ClickMapHandler() {

            @Override
            public void onEvent(ClickMapEvent event) {
                try {
                    if (event.getProperties().getNumber("vertex") == null) {
                        edge.setEditable(!edge.getEditable());
                        edgeMap.get(edge).setEditable(edge.getEditable());
                        
                        if (measureEdgeDistance) {
                            updateTotalDistanceHTML(htmlEdgeDistance, 
                                SphericalUtils.computeLength(edge.getPath()));
                        }
                    }
                } catch (Properties.TypeException ex) {
                    browserLog("com.vaadin.tapio.googlemaps.client.GoogleMapWidget " + ex.getMessage());
                }
                if (edgeClickListener != null) {                    
                    edgeClickListener.edgeClicked(edgeMap.get(edge));
                }
            }
        });
        
        edge.addDblClickHandler(new DblClickMapHandler() {
            
            @Override
            public void onEvent(DblClickMapEvent event) {
                try {
                    if (event.getProperties().getNumber("vertex") != null) {
                        
                        int vetexIndex = event.getProperties().getNumber("vertex").intValue();
                        
                        int  size = edge.getPath().getLength();
                        // The first and the last control point cannot be removed
                        if (vetexIndex != 0 && vetexIndex != size - 1) {
                            edge.getPath().removeAt(vetexIndex);
                        }
                    }
                } catch (Properties.TypeException ex) {
                    browserLog("com.vaadin.tapio.googlemaps.client.GoogleMapWidget " + ex.getMessage());
                }
            }
        });
        
        edge.addRightClickHandler(new RightClickMapHandler() {

            @Override
            public void onEvent(RightClickMapEvent event) {
                if (edgeRightClickListener != null) {
                    if (htmlEdgeDistance == null) {
                        htmlEdgeDistance = new HTML();
                    }
                    updateTotalDistanceHTML(htmlEdgeDistance, 
                        SphericalUtils.computeLength(edge.getPath()));
                    
                    edgeRightClickListener.edgeRightClicked(edgeMap.get(edge));
                }
            }
        });
    }
        
    private void firstControlPointMoved(GoogleMapPolyline edge) {
        if (this.edgeNodes.containsKey(edge)) {
            
            LatLng position = gmEdgeMap.get(edge).getPath().get(0);
            GoogleMapMarker sourceNode = edgeNodes.get(edge).get(0);
            gmMarkerMap.get(sourceNode).setPosition(position);
            // update marker
            sourceNode.setPosition(new LatLon(position.getLatitude(), position.getLongitude()));
            
            nodeMoved(sourceNode, edge);
        }
    }
    
    private void lastControlPointMoved(GoogleMapPolyline edge) {
        if (this.edgeNodes.containsKey(edge)) {
            
            int size = gmEdgeMap.get(edge).getPath().getLength();
            
            LatLng position = gmEdgeMap.get(edge).getPath().get(size - 1);
            GoogleMapMarker targetNode = this.edgeNodes.get(edge).get(1);
            gmMarkerMap.get(targetNode).setPosition(position);
            // update marker
            targetNode.setPosition(new LatLon(position.getLatitude(), position.getLongitude()));
            
            nodeMoved(targetNode, edge);
        }
    }
    
    private void nodeMoved(GoogleMapMarker node, GoogleMapPolyline sourceOfTheMove) {
        if (gmMarkerMap.containsKey(node)) {
            updateMarkerLabel(node);
        }
        
        if (sourceNodeEdges.containsKey(node)) {
            
            for (GoogleMapPolyline edge : sourceNodeEdges.get(node)) {
                
                if (!edge.equals(sourceOfTheMove)) {
                    LatLng position = gmMarkerMap.get(node).getPosition();
                    
                    gmEdgeMap.get(edge).getPath().removeAt(0);
                    gmEdgeMap.get(edge).getPath().insertAt(0, position);
                }
            }
        }
        
        if (targetNodeEdges.containsKey(node)) {
            
            for (GoogleMapPolyline edge : targetNodeEdges.get(node)) {
                
                if (!edge.equals(sourceOfTheMove)) {
                    LatLng position = gmMarkerMap.get(node).getPosition();
                    
                    gmEdgeMap.get(edge).getPath().pop();
                    gmEdgeMap.get(edge).getPath().push(position);
                }
            }
        }
    }
    
    public void setMeasureDistance(boolean measureDistance) {
        this.measureDistance = measureDistance;
        if (!measureDistance) {
            if (measurePolyline != null) {
                measurePolyline.setMap(null);
                measurePolyline.getPath().clear();
            }
            lengthPath = 0;
            if (measureDistancePanel != null) {
                measureDistancePanel.removeFromParent();
            }
        }
    }
    
    public void setMeasureEdgeDistance(boolean measureEdgeDistance) {
        this.measureEdgeDistance = measureEdgeDistance;
        if (measureEdgeDistance) {
            
            if (mesaureEdgeDistancePanel == null) {
                
                mesaureEdgeDistancePanel = new FlowPanel();
                mesaureEdgeDistancePanel.add(new HTML("<b> Measure distance </b>"));

                //htmlEdgeDistance = new HTML("");
                mesaureEdgeDistancePanel.add(htmlEdgeDistance);

                mesaureEdgeDistancePanel.getElement().getStyle().setBackgroundColor("#e6e6e6");
                mesaureEdgeDistancePanel.getElement().getStyle().setPadding(10, Style.Unit.PX);
                mesaureEdgeDistancePanel.getElement().getStyle().setMargin(100, Style.Unit.PX);
                DOM.setStyleAttribute(mesaureEdgeDistancePanel.getElement(), "border", "3px solid #FF0000");
            }
            map.setControls(ControlPosition.TOP_RIGHT, mesaureEdgeDistancePanel);
        } else {
            if (mesaureEdgeDistancePanel != null) {
                mesaureEdgeDistancePanel.removeFromParent();
            }
        }
    }
    
    private void updateTotalDistanceHTML(HTML html, double totalDistance) {
        String distanceAsString = NumberFormat.getFormat("#.00").format(totalDistance);
        
        html.setHTML("<b>Total distance: </b>" + distanceAsString + " m");
    }
        
    private void initializeMeasurePolyline() {
        if (measurePolyline == null) {
            PolylineOptions options = PolylineOptions.newInstance();
            options.setStrokeColor("black");
            options.setStrokeOpacity(1);
            options.setStrokeWeight(3);
            options.setMap(map);
            
            measurePolyline = Polyline.newInstance(options);
            measurePolyline.setEditable(true);
            
            measurePolyline.getPath().addInsertAtHandler(new InsertAtMapHandler() {
                
                @Override
                public void onEvent(InsertAtMapEvent event) {
                    updateTotalDistanceHTML(htmlTotalDistance, 
                        SphericalUtils.computeLength(measurePolyline.getPath()));
                }
            });
            
            measurePolyline.getPath().addRemoveAtHandler(new RemoveAtMapHandler() {
                
                @Override
                public void onEvent(RemoveAtMapEvent event) {
                    updateTotalDistanceHTML(htmlTotalDistance, 
                        SphericalUtils.computeLength(measurePolyline.getPath()));
                }
            });
                
            measurePolyline.getPath().addSetAtHandler(new SetAtMapHandler() {
                
                @Override
                public void onEvent(SetAtMapEvent event) {
                    updateTotalDistanceHTML(htmlTotalDistance, 
                        SphericalUtils.computeLength(measurePolyline.getPath()));
                }
            });
            
            
            measurePolyline.addClickHandler(new ClickMapHandler() {

                @Override
                public void onEvent(ClickMapEvent event) {
                    measurePolyline.getPath().insertAt(lengthPath + 1, event.getMouseEvent().getLatLng());
                    lengthPath += 1;
                    
                    updateTotalDistanceHTML(htmlTotalDistance, 
                        SphericalUtils.computeLength(measurePolyline.getPath()));
                }
            });
            
            measurePolyline.addDblClickHandler(new DblClickMapHandler(){

                @Override
                public void onEvent(DblClickMapEvent event) {
                    measureDistance = false;
                }
            });
        }
        else {
            measurePolyline.setMap(map);
        }
        
        if (measureDistancePanel == null) {
            measureDistancePanel = new FlowPanel();
            measureDistancePanel.add(new HTML("<b>Measure distance</b>"));
            
            htmlTotalDistance = new HTML("");
            measureDistancePanel.add(htmlTotalDistance);
            
            measureDistancePanel.getElement().getStyle().setBackgroundColor("#e6e6e6");
            measureDistancePanel.getElement().getStyle().setPadding(10, Style.Unit.PX);
            measureDistancePanel.getElement().getStyle().setMargin(100, Style.Unit.PX);
            DOM.setStyleAttribute(measureDistancePanel.getElement(), "border", "3px solid #FF0000");
        }
        map.setControls(ControlPosition.RIGHT_CENTER, measureDistancePanel);
    }
}

