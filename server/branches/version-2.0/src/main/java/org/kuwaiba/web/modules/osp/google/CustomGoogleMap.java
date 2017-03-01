 /*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kuwaiba.web.modules.osp.google;

import com.google.common.eventbus.Subscribe;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Page;
import org.kuwaiba.web.modules.osp.google.overlays.MarkerNode;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.MapClickListener;
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.web.gui.modules.EmbeddableComponent;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.custom.tree.DynamicTree;
import org.kuwaiba.web.modules.osp.google.overlays.ConnectionPolyline;
import org.kuwaiba.web.custom.wizards.physicalconnection.PhysicalConnectionWizard;
import org.kuwaiba.web.modules.osp.AbstractGISView;
import org.kuwaiba.web.modules.osp.events.PolylineClickListenerImpl;
import org.kuwaiba.web.modules.osp.events.MarkerDblClickListenerImpl;
import org.kuwaiba.web.modules.osp.events.MarkerDragListenerImpl;
import org.kuwaiba.web.modules.osp.events.PolygonClickListenerImpl;
import org.kuwaiba.web.modules.osp.events.PolygonDblClickListenerImpl;
import org.kuwaiba.web.modules.osp.events.PolylineDblClickListenerImpl;
import org.kuwaiba.web.modules.osp.google.overlays.Marker;
import org.kuwaiba.web.modules.osp.google.overlays.MarkerPoint;
import org.kuwaiba.web.modules.osp.google.overlays.Polygon;
import org.kuwaiba.web.modules.osp.google.overlays.Polyline;
import org.kuwaiba.web.modules.physicalconnections.windows.ConnectLinksWindow;

/**
 * Custom GoogleMap for Kuwaiba
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CustomGoogleMap extends GoogleMap implements AbstractGISView, 
        EmbeddableComponent, PropertyChangeListener, Window.CloseListener {
    /** 
     * Google Maps event names.
     */
    public static String GM_EVENT_NAME_ENDING_POLYGON = "endingPolygon";
    public static String GM_EVENT_NAME_ADD_POLYGON = "addPolygon";
    public static String GM_EVENT_NAME_UPDATE_POLYGON = "updatePolygon";
    public static String GM_EVENT_NAME_HIDE_POLYGON = "hidePolygon";
    public static String GM_EVENT_NAME_SHOW_POLYGON = "showPolygon";
    public static String GM_EVENT_NAME_REMOVE_POLYGON = "removePolygon";
    
    public static String GM_EVENT_NAME_ADD_POLYLINE = "addPolyline";    
    public static String GM_EVENT_NAME_UPDATE_POLYLINE = "updatePolyline";
    public static String GM_EVENT_NAME_UPDATE_POLYLINES = "updatePolylines";
    public static String GM_EVENT_NAME_HIDE_POLYLINE = "hidePolyline";
    public static String GM_EVENT_NAME_HIDE_POLYLINES = "hidePolylines";
    public static String GM_EVENT_NAME_SHOW_POLYLINE = "showPolyline";
    public static String GM_EVENT_NAME_SHOW_POLYLINES = "showPolylines";
    public static String GM_EVENT_NAME_REMOVE_POLYLINE = "removePolyline";
    
    public static String GM_EVENT_NAME_ADD_MARKER = "addMarker";
    public static String GM_EVENT_NAME_UPDATE_MARKER = "updateMarker";
    public static String GM_EVENT_NAME_UPDATE_MARKERS = "updateMarkers";
    public static String GM_EVENT_NAME_SHOW_MARKERS = "showMarkers";
    public static String GM_EVENT_NAME_HIDE_MARKERS = "hideMarkers";
    public static String GM_EVENT_NAME_REMOVE_MARKER = "removeMarker";
    public static String GM_EVENT_NAME_REMOVE_MARKERS = "removeMarkers";
        
    private static final String FORMAT_VERSION = "0.1";
    
    private final TopComponent parentComponent;
    /**
     * List of connections for each node
     */
    private final Map<MarkerNode, List<ConnectionPolyline>> connectionsForNode = new HashMap<>();
    /**
     * List of node for each connection
     */
    private final Map<ConnectionPolyline, List<MarkerNode>> nodesForConnection = new HashMap<>();
    /**
     * List of filters
     */
    private List<String> nodesFilter = new ArrayList();
    private List<String> connectionsFilter = new ArrayList();
        
    private ConnectionPolyline newConnection = null;
    private Polygon newPolygon = null;
    
    private boolean connectionEnable = false;
    private boolean drawPolygonEnable = false;    
    
    public CustomGoogleMap(TopComponent parentComponent, String apiKey, String clientId, String language) {
        super(apiKey, clientId, language);
        this.parentComponent = parentComponent;
        
        addMapClickListener(new MapClickListenerImpl());
                
        addMarkerClickListener(new MarkerClickListenerImpl());
        addMarkerDblClickListener(new MarkerDblClickListenerImpl());
        addMarkerDragListener(new MarkerDragListenerImpl());
        
        addPolygonClickListener(new PolygonClickListenerImpl());
        addPolygonDblClickListener(new PolygonDblClickListenerImpl());
        
        addPolylineClickListener(new PolylineClickListenerImpl());
        addPolylineDblClickListener(new PolylineDblClickListenerImpl());
    }
    
    public void addNodeMarker(RemoteObjectLight objectNode) {
        if (getState().markers.containsKey(objectNode.getOid())) {
            Notification.show("The map containt the object", Notification.Type.WARNING_MESSAGE);
            return;
        }
        WebserviceBeanLocal wsBean = getTopComponent().getWsBean();
        String ipAddress = getUI().getPage().getWebBrowser().getAddress();
        String sessioId = getTopComponent().getApplicationSession().getSessionId();
                
        if (!wsBean.isSubclassOf(objectNode.getClassName(), "ViewableObject", ipAddress, sessioId)) { //NOI18N
            Notification.show("Only ViewableObject are allowed", Notification.Type.WARNING_MESSAGE);
            return;
        }
        MarkerNode markerNode = new MarkerNode(objectNode);
        markerNode.setId(objectNode.getOid());
        markerNode.setCaption(objectNode.toString());
        markerNode.setPosition(getCenter());
        
        markerNode.addPropertyChangeListener(this);
        
        getState().markers.put(objectNode.getOid(), markerNode);
        
        connectionsForNode.put(markerNode, new ArrayList());
        
        nodesFilter.add(objectNode.getClassName());        
    }
    
    public void register() {
        if (parentComponent != null)
            parentComponent.getEventBus().register(this);
    }
    
    public void unregister() {
        if (parentComponent != null)
            parentComponent.getEventBus().unregister(this);
    }

    @Override
    public TopComponent getTopComponent() {
        return parentComponent;
    }
    
    public void enableConnectionTool(boolean value) {
        connectionEnable = !connectionEnable;
        if (connectionEnable)
            Notification.show("Connection Tool Enable", Notification.Type.TRAY_NOTIFICATION);
        else
            Notification.show("Connection Tool Disable", Notification.Type.TRAY_NOTIFICATION);
        newConnection = null;            
    }
    
    public void enablePolygonTool(boolean value) {
        drawPolygonEnable = !drawPolygonEnable;
        if (drawPolygonEnable)
            Notification.show("Polygon Tool Enable", Notification.Type.TRAY_NOTIFICATION);
        else
            Notification.show("Polygon Tool Disable", Notification.Type.TRAY_NOTIFICATION);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null || evt.getPropertyName() == null)
            return;
        
        if (evt.getPropertyName().equals(GM_EVENT_NAME_ENDING_POLYGON)) {
            newPolygon = null;
            return;
        } 
        if (evt.getPropertyName().equals(GM_EVENT_NAME_ADD_POLYLINE)) {
            GoogleMapPolyline gmPolyline = 
                    (GoogleMapPolyline) evt.getNewValue();
            getState().polylines.put(gmPolyline.getId(), gmPolyline);
            return;
        }
        if (evt.getPropertyName().equals(GM_EVENT_NAME_UPDATE_POLYLINE)) {
            getState().polylinesChanged
                    .add((GoogleMapPolyline) evt.getNewValue());
            return;
        }
        
        if (evt.getPropertyName().equals(GM_EVENT_NAME_UPDATE_POLYLINES)) {
            getState().polylinesChanged = (ArrayList<GoogleMapPolyline>) evt.getNewValue();
            return;
        }
        
        if (evt.getPropertyName().equals(GM_EVENT_NAME_HIDE_POLYLINE)) {
            GoogleMapPolyline polyline = (GoogleMapPolyline) evt.getNewValue();
            polyline.setVisible(false);
            getState().polylinesChanged.add(polyline);
            return;
        }
        
        if (evt.getPropertyName().equals(GM_EVENT_NAME_HIDE_POLYLINES)) {
            List<GoogleMapPolyline> polylines = (ArrayList<GoogleMapPolyline>) evt.getNewValue();
            for (GoogleMapPolyline polyline : polylines)
                polyline.setVisible(false);
            getState().polylinesChanged = polylines;
            return;
        }
        
        if (evt.getPropertyName().equals(GM_EVENT_NAME_HIDE_POLYLINES)) {
            List<GoogleMapPolyline> polylines = (ArrayList<GoogleMapPolyline>) evt.getNewValue();
            for (GoogleMapPolyline polyline : polylines)
                polyline.setVisible(true);
            getState().polylinesChanged = polylines;
            return;
        }
        
        if (evt.getPropertyName().equals(GM_EVENT_NAME_SHOW_POLYLINE)) {
            GoogleMapPolyline polyline = (GoogleMapPolyline) evt.getNewValue();
            polyline.setVisible(true);
            getState().polylinesChanged.add(polyline);
            return;
        }
        
        if (evt.getPropertyName().equals(GM_EVENT_NAME_REMOVE_MARKERS)) {
            List<Marker> markers = 
                    (ArrayList<Marker>) evt.getNewValue();
            for (Marker marker : markers)
                removeMarker((Marker) marker);
            return;
        }
        if (evt.getPropertyName().equals(GM_EVENT_NAME_ADD_POLYGON)) {
            GoogleMapPolygon gmPolygon = (GoogleMapPolygon) evt.getNewValue();
            getState().polygons.put(gmPolygon.getId(), gmPolygon);
            return;
        }
        
        if (evt.getPropertyName().equals(GM_EVENT_NAME_HIDE_POLYGON)) {
            GoogleMapPolygon gmPolygon = (GoogleMapPolygon) evt.getNewValue();
            gmPolygon.setVisible(false);
            getState().polygonsChanged.add(gmPolygon);
            return;
        }
        if (evt.getPropertyName().equals(GM_EVENT_NAME_SHOW_POLYGON)) {
            GoogleMapPolygon gmPolygon = (GoogleMapPolygon) evt.getNewValue();
            gmPolygon.setVisible(true);
            getState().polygonsChanged.add(gmPolygon);
            return;
        }
        
        if (evt.getPropertyName().equals(GM_EVENT_NAME_UPDATE_POLYGON)) {
            getState().polygonsChanged
                    .add((GoogleMapPolygon) evt.getNewValue());
            return;
        }
        if (evt.getPropertyName().equals(GM_EVENT_NAME_ADD_MARKER)) {
            GoogleMapMarker marker = (GoogleMapMarker) evt.getNewValue();
            getState().markers.put(marker.getId(), marker);
            return;
        }
        if (evt.getPropertyName().equals(GM_EVENT_NAME_REMOVE_MARKER)) {
            removeMarker((Marker) evt.getNewValue());
            return;
        }
        if (evt.getPropertyName().equals(GM_EVENT_NAME_UPDATE_MARKER)) {
            if (evt.getNewValue() instanceof Marker)
                getState().markersChanged.add((GoogleMapMarker) evt.getNewValue());
            return;
        }
        if (evt.getPropertyName().equals(GM_EVENT_NAME_UPDATE_MARKERS)) {
            getState().markersChanged = 
                    (ArrayList<GoogleMapMarker>) evt.getNewValue();
        }
        
        if (evt.getPropertyName().equals(GM_EVENT_NAME_SHOW_MARKERS)) {
            List<GoogleMapMarker> markers = (ArrayList<GoogleMapMarker>) evt.getNewValue();
            for (GoogleMapMarker marker : markers)
                marker.setVisible(true);
            getState().markersChanged = markers;
            return;
        }
        if (evt.getPropertyName().equals(GM_EVENT_NAME_HIDE_MARKERS)) {
            List<GoogleMapMarker> markers = (ArrayList<GoogleMapMarker>) evt.getNewValue();
            for (GoogleMapMarker marker : markers)
                marker.setVisible(false);
            getState().markersChanged = markers;
            return;
        }
        
        if (evt.getPropertyName().equals(GM_EVENT_NAME_REMOVE_POLYGON)) {
            removePolygon((Polygon) evt.getSource());
            return;
        }
        if (evt.getPropertyName().equals(GM_EVENT_NAME_REMOVE_POLYLINE)) {
            removePolyline((Polyline) evt.getSource());
        }
        
        if (evt.getPropertyName().equals("showConnectionLinksWindow")) {
            getUI().addWindow(new ConnectLinksWindow(parentComponent, (RemoteObjectLight) evt.getNewValue()));
        }
    }
    
    private void removeMarker(Marker marker) {
        if (marker instanceof MarkerNode) {
            MarkerNode node = ((MarkerNode) marker);
                        
            if (connectionsForNode.get(node) != null) {
                for (ConnectionPolyline conn : connectionsForNode.get(node)) {

                    if (node.equals(conn.getSource()))
                        connectionsForNode.get(conn.getTarget()).remove(conn);

                    if (node.equals(conn.getTarget()))
                        connectionsForNode.get(conn.getSource()).remove(conn);

                    removePolyline(conn);
                }
                connectionsForNode.get(node).clear();  
            }            
            node.removedFromView(true);
            
            getState().markers.remove(node.getId());
            
            connectionsForNode.remove(node);
            nodesFilter.remove(node.getRemoteObjectLight().getClassName());
            return;
        }
        if (marker instanceof MarkerPoint) {
            if (!marker.getRemoved())
                marker.removeAllPropertyChangeListener();
            
            getState().markers.remove(marker.getId());
            return;
        }
    }
    
    private void removePolygon(Polygon polygon) {
        polygon.getPolyline().removedFromView(true);
        removePolyline(polygon.getPolyline());
        polygon.removedFromView(true);
        getState().polygons.remove(polygon.getId());
    }
    
    private void removePolyline(Polyline polyline) {
        if (polyline instanceof ConnectionPolyline) {
            try {
                ConnectionPolyline connection = (ConnectionPolyline) polyline;

                MarkerNode source = connection.getSource();
                source.removePropertyChangeListener(connection);

                MarkerNode target = connection.getTarget();
                target.removePropertyChangeListener(connection);
                
                RemoteObjectLight connInfo = connection.getConnectionInfo();

                WebserviceBeanLocal wsBean = getTopComponent().getWsBean();
                String ipAddress = getUI().getPage().getWebBrowser().getAddress();
                String sessioId = getTopComponent().getApplicationSession().getSessionId();
                                
                wsBean.deletePhysicalConnection(connInfo.getClassName(), connInfo.getOid(), ipAddress, sessioId);
                
                nodesForConnection.remove(connection);
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        }           
        List<MarkerPoint> points = polyline.getPoints();
        if (!points.isEmpty()) {
            for (Marker point : points) {
                point.removeAllPropertyChangeListener();
                getState().markers.remove(point.getId());
            }
            points.clear();
        }
        if (polyline.getRemoved())
            polyline.removeAllPropertyChangeListener();
        else
            polyline.removedFromView(true);
        
        getState().polylines.remove(polyline.getId());
    }

    @Override
    public String getName() {
        return "OSP Module for Google Maps";
    }

    @Override
    public String getDescription() {
        return "OSP Module that uses Google Maps as map provider";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS";
    }

    @Override
    public void clear() {
        getState().markers.clear();
        getState().polygons.clear();
        
        getState().polylines.clear(); // TODO: remove all connection in the database
        //getState().infoWindows.clear(); //TODO: no used yet
        connectionsForNode.clear();
    }
    
    public void removeAllPhysicalConnection() {
        for (GoogleMapPolyline gmPolyline : getState().polylines.values())
            if (gmPolyline instanceof ConnectionPolyline)
                removePolyline((Polyline) gmPolyline);
    }

    @Override
    public byte[] getAsXML() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName qnView = new QName("view");
            xmlew.add(xmlef.createStartElement(qnView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), FORMAT_VERSION));
            
            QName qnClass = new QName("class");
            xmlew.add(xmlef.createStartElement(qnClass, null, null));
            xmlew.add(xmlef.createCharacters("GISView"));
            xmlew.add(xmlef.createEndElement(qnClass, null));
            
            QName qnCenter = new QName("center");
            xmlew.add(xmlef.createStartElement(qnCenter, null, null));
            xmlew.add(xmlef.createAttribute(new QName("lat"), Double.toString(getCenter().getLat())));
            xmlew.add(xmlef.createAttribute(new QName("lon"), Double.toString(getCenter().getLon())));
            xmlew.add(xmlef.createEndElement(qnCenter, null));
            
            QName qnZoom = new QName("zoom");
            xmlew.add(xmlef.createStartElement(qnZoom, null, null));
            xmlew.add(xmlef.createAttribute(new QName("value"), 
                    Integer.toString(getZoom())));
            xmlew.add(xmlef.createEndElement(qnZoom, null));
            // nodes
            QName qnNodes = new QName("nodes");
            xmlew.add(xmlef.createStartElement(qnNodes, null, null));
            
            for (GoogleMapMarker gmMarker : getState().markers.values()) {
                if (gmMarker instanceof MarkerNode) {
                    QName qnameNode = new QName("node");
                    xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                    
                    MarkerNode node = (MarkerNode) gmMarker;
                    
                    xmlew.add(xmlef.createAttribute(new QName("lat"), 
                            Double.toString(node.getPosition().getLat())));
                    xmlew.add(xmlef.createAttribute(new QName("lon"), 
                            Double.toString(node.getPosition().getLon())));
                    
                    RemoteObjectLight object = node.getRemoteObjectLight();
                    
                    xmlew.add(xmlef.createAttribute(new QName("class"), 
                            object.getClassName()));
                    xmlew.add(xmlef.createAttribute(new QName("id"), 
                            Long.toString(object.getOid())));
                    xmlew.add(xmlef.createAttribute(new QName("visible"), 
                            Boolean.toString(node.isVisible())));
                    
                    xmlew.add(xmlef.createEndElement(qnameNode, null));
                }
            }
            xmlew.add(xmlef.createEndElement(qnNodes, null));
            // edges or connection
            QName qnEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnEdges, null, null));
            for (GoogleMapPolyline gmPolyline : getState().polylines.values()) {
                if (gmPolyline instanceof ConnectionPolyline) {
                    ConnectionPolyline physicalConnection = (ConnectionPolyline) gmPolyline;
                    
                    QName qnEdge = new QName("edge");
                    xmlew.add(xmlef.createStartElement(qnEdge, null, null));
                    
                    xmlew.add(xmlef.createAttribute(new QName("id"), 
                            Long.toString(physicalConnection.getId())));
                    xmlew.add(xmlef.createAttribute(new QName("strokeColor"), 
                            physicalConnection.getStrokeColor()));
                    xmlew.add(xmlef.createAttribute(new QName("strokeOpacity"), 
                            Double.toString(physicalConnection.getStrokeOpacity())));
                    xmlew.add(xmlef.createAttribute(new QName("strokeWeight"), 
                            Integer.toString(physicalConnection.getStrokeWeight())));
                    xmlew.add(xmlef.createAttribute(new QName("visible"), 
                            Boolean.toString(physicalConnection.isVisible())));
                    xmlew.add(xmlef.createAttribute(new QName("name"), 
                            physicalConnection.getConnectionInfo().getName()));
                    xmlew.add(xmlef.createAttribute(new QName("objectClass"), 
                            physicalConnection.getConnectionInfo().getClassName()));

                    xmlew.add(xmlef.createAttribute(new QName("aside"), 
                            Long.toString(physicalConnection.getSource().getId())));
                    xmlew.add(xmlef.createAttribute(new QName("bside"), 
                            Long.toString(physicalConnection.getTarget().getId())));


                    for (LatLon coordinate : physicalConnection.getCoordinates()) {
                        QName qnCoordinate = new QName("coordinate");
                        xmlew.add(xmlef.createStartElement(qnCoordinate, null, null));
                        xmlew.add(xmlef.createAttribute(new QName("lat"), 
                                Double.toString(coordinate.getLat())));
                        xmlew.add(xmlef.createAttribute(new QName("lon"), 
                                Double.toString(coordinate.getLon())));
                        xmlew.add(xmlef.createEndElement(qnCoordinate, null));
                    }
                    xmlew.add(xmlef.createEndElement(qnEdge, null));
                }
            }
            xmlew.add(xmlef.createEndElement(qnEdges, null));
            // polygon
            QName qnPolygons = new QName("polygons");
            xmlew.add(xmlef.createStartElement(qnPolygons, null, null));
            for (GoogleMapPolygon gmPolygon : getState().polygons.values()) {
                if (gmPolygon instanceof Polygon) {
                    Polygon polygon = (Polygon) gmPolygon;
                    
                    QName qnPolygon = new QName("polygon");
                    xmlew.add(xmlef.createStartElement(qnPolygon, null, null));
                    
                    xmlew.add(xmlef.createAttribute(new QName("fillColor"), 
                            polygon.getFillColor()));
                    xmlew.add(xmlef.createAttribute(new QName("fillOpacity"), 
                            Double.toString(polygon.getFillOpacity())));
                    xmlew.add(xmlef.createAttribute(new QName("strokeColor"), 
                            polygon.getStrokeColor()));
                    xmlew.add(xmlef.createAttribute(new QName("strokeOpacity"), 
                            Double.toString(polygon.getStrokeOpacity())));
                    xmlew.add(xmlef.createAttribute(new QName("strokeWeight"), 
                            Integer.toString(polygon.getStrokeWeight())));
                    xmlew.add(xmlef.createAttribute(new QName("visible"), 
                            Boolean.toString(polygon.isVisible())));
                    
                    for (LatLon coordinate : polygon.getCoordinates()) {
                        QName qnCoordinate = new QName("coordinate");
                        xmlew.add(xmlef.createStartElement(qnCoordinate, null, null));
                        xmlew.add(xmlef.createAttribute(new QName("lat"), 
                                Double.toString(coordinate.getLat())));
                        xmlew.add(xmlef.createAttribute(new QName("lon"), 
                                Double.toString(coordinate.getLon())));
                        xmlew.add(xmlef.createEndElement(qnCoordinate, null));
                    }
                    xmlew.add(xmlef.createEndElement(qnPolygon, null));
                }
            }
            xmlew.add(xmlef.createEndElement(qnPolygons, null));
                       
            xmlew.add(xmlef.createEndElement(qnView, null));
            xmlew.close();
            
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            
        }
        return null;
    }
    
    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
        try {
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = xmlif.createXMLStreamReader(bais);
            
            WebserviceBeanLocal wsBean = getTopComponent().getWsBean();
            String ipAddress = getUI().getPage().getWebBrowser().getAddress();
            String sessioId = getTopComponent().getApplicationSession().getSessionId();
            
            QName qnCenter = new QName("center");
            QName qnZoom = new QName("zoom");
            QName qnNode = new QName("node");
            QName qnEdge = new QName("edge");
            QName qnPolygon = new QName("polygon");
            QName qnCoordinate = new QName("coordinate");
            
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qnCenter)) {
                        double lat = Double.parseDouble(reader.getAttributeValue(null, "lat"));
                        double lon = Double.parseDouble(reader.getAttributeValue(null, "lon"));
                        setCenter(new LatLon(lat, lon));
                    }
                    if (reader.getName().equals(qnZoom)) {
                        int zoom = Integer.parseInt(reader.getAttributeValue(null, "value"));
                        setZoom(zoom);
                    }
                    if (reader.getName().equals(qnNode)) {
                        double lat = Double.parseDouble(reader.getAttributeValue(null, "lat"));
                        double lon = Double.parseDouble(reader.getAttributeValue(null, "lon"));
                        long oid = Long.parseLong(reader.getAttributeValue(null, "id"));
                        String objectClass = reader.getAttributeValue(null, "class");
                        boolean visible = Boolean.valueOf(
                                reader.getAttributeValue(null, "visible"));
                        
                        RemoteObjectLight remoteObjectLight = wsBean.getObjectLight(objectClass, oid, ipAddress, sessioId);
                        
                        MarkerNode node = new MarkerNode(remoteObjectLight);
                        node.setId(oid);
                        node.setCaption(remoteObjectLight.toString());
                        node.setPosition(new LatLon(lat, lon));
                        node.setVisible(visible);
                        node.addPropertyChangeListener(this);
                        //TODO: set icon url
                        //node.setIconUrl(ipAddress);
                        getState().markers.put(oid, node);
                        connectionsForNode.put(node, new ArrayList());
                        nodesFilter.add(remoteObjectLight.getClassName());
                    }
                    else {
                        if (reader.getName().equals(qnEdge)) {
                            Long oid = Long.parseLong(reader.getAttributeValue(null, "id"));
                            String strokeColor = reader.getAttributeValue(
                                    null, "strokeColor");
                            double strokeOpacity = Double.valueOf(
                                    reader.getAttributeValue(null, "strokeOpacity"));
                            int strokeWeight = Integer.valueOf(
                                    reader.getAttributeValue(null, "strokeWeight"));
                            boolean visible = Boolean.valueOf(
                                    reader.getAttributeValue(null, "visible"));
                            String name = reader.getAttributeValue(null, "name");
                            String objectClass = reader.getAttributeValue(null, "objectClass");
                            
                            Long aside = Long.parseLong(reader.getAttributeValue(null, "aside"));
                            Long bside = Long.parseLong(reader.getAttributeValue(null, "bside"));
                            
                            MarkerNode source = (MarkerNode) getState().markers.get(aside);
                            MarkerNode target = (MarkerNode) getState().markers.get(bside);
                            
                            ConnectionPolyline edge = new ConnectionPolyline(source, target);
                            edge.setId(oid);
                            edge.setStrokeColor(strokeColor);
                            edge.setStrokeOpacity(strokeOpacity);
                            edge.setStrokeWeight(strokeWeight);
                            edge.setVisible(visible);
                            RemoteObjectLight edgeInfo = new RemoteObjectLight(oid, name, objectClass);
                            edge.setConnectionInfo(edgeInfo);
                            
                            List<LatLon> coordinates = new ArrayList();
                            while (true) {
                                reader.nextTag();
                                if (reader.getName().equals(qnCoordinate)) {
                                    if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                        Double lat = Double.parseDouble(
                                                reader.getAttributeValue(null, "lat"));
                                        Double lon = Double.parseDouble(
                                                reader.getAttributeValue(null, "lon"));
                                        coordinates.add(new LatLon(lat, lon));
                                    }
                                }
                                else {
                                    edge.setCoordinates(coordinates);
                                    break;
                                }
                            }
                            addConnectionForNode(source, edge);
                            addConnectionForNode(target, edge);
                            
                            edge.addPropertyChangeListener(this);
                            edge.setSaved(true);
                            getState().polylines.put(edge.getId(), edge);                            
                        }
                        else {
                            if (reader.getName().equals(qnPolygon)) {                                
                                String fillColor = reader.getAttributeValue(
                                    null, "fillColor");
                                double fillOpacity = Double.valueOf(reader
                                        .getAttributeValue(null, "fillOpacity"));
                                String strokeColor = reader.getAttributeValue(
                                        null, "strokeColor");
                                double strokeOpacity = Double.valueOf(reader.
                                        getAttributeValue(null, "strokeOpacity"));
                                int strokeWeight = Integer.valueOf(reader
                                        .getAttributeValue(null, "strokeWeight"));
                                boolean visible = Boolean.valueOf(reader
                                        .getAttributeValue(null, "visible"));
                                
                                Polygon polygon = new Polygon(this);
                                polygon.setFillColor(fillColor);
                                polygon.setFillOpacity(fillOpacity);
                                polygon.setStrokeColor(strokeColor);
                                polygon.setStrokeOpacity(strokeOpacity);
                                polygon.setStrokeWeight(strokeWeight);
                                polygon.setVisible(visible);
                                
                                List<LatLon> coordinates = new ArrayList();
                                while (true) {
                                    reader.nextTag();
                                    if (reader.getName().equals(qnCoordinate)) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                            Double lat = Double.parseDouble(
                                                    reader.getAttributeValue(
                                                            null, "lat"));
                                            Double lon = Double.parseDouble(
                                                    reader.getAttributeValue(
                                                            null, "lon"));
                                            coordinates.add(new LatLon(lat, lon));
                                        }
                                    }
                                    else {                                                                                
                                        polygon.setCoordinates(coordinates);
                                        
                                        for (LatLon coordinate : coordinates)
                                            polygon.getPolyline().getCoordinates()
                                                .add(coordinate);
                                        // close the polygon
                                        polygon.getPolyline().getCoordinates()
                                                .add(coordinates.get(0));
                                        break;
                                    }
                                }                                
                                getState().polygons.put(polygon.getId(), polygon);
                            } // end if polygons
                        } // end if edges
                    } // end if nodes
                } // end if
            } // end while
            reader.close();
        } catch(ServerSideException | NumberFormatException | XMLStreamException ex) {
            //TODO: handler
        }                        
    }

    @Override
    public void windowClose(Window.CloseEvent e) {
        PhysicalConnectionWizard wizard = (PhysicalConnectionWizard) e.getWindow();
        
        if (wizard.getSelectedButton() == 
                PhysicalConnectionWizard.SELECTED_CANCEL_BUTTON)
            Notification.show("Physical connection wizard was cancelled", 
                    Notification.Type.TRAY_NOTIFICATION);
        
        if (wizard.getSelectedButton() == 
                PhysicalConnectionWizard.SELECTED_FINISH_BUTTON) {
            if (newConnection.getId() != -1L) {
                newConnection.addPropertyChangeListener(this);
                
                getState().polylines.put(newConnection.getId(), newConnection);
                
                addConnectionForNode(newConnection.getSource(), newConnection);
                addConnectionForNode(newConnection.getTarget(), newConnection);
            }
        }
        getUI().removeWindow(wizard);
        newConnection = null;
    }

    public void connectionsSaved() {
        for (GoogleMapPolyline gmPolyline : getState().polylines.values()) {
            if (gmPolyline instanceof ConnectionPolyline) {
                ConnectionPolyline connection = (ConnectionPolyline) gmPolyline;
                if (!connection.isSaved())
                    connection.setSaved(true);
            }
        }
    }

    public void removeConnectionsUnsave() {
        for (GoogleMapPolyline gmPolyline : getState().polylines.values()) {
            if (gmPolyline instanceof ConnectionPolyline) {
                ConnectionPolyline connection = (ConnectionPolyline) gmPolyline;
                if (!connection.isSaved())
                    removePolyline(connection);
            }
        }
    }

    public void filterby(List<String> nodesFilterBy, List<String> connectionsFilterBy) {
        boolean visible = false;
        
        if (nodesFilterBy.isEmpty())
            visible = true;
        
        List<Marker> visibleMarkers = new ArrayList();
        List<ConnectionPolyline> visibleConnections = new ArrayList();
        // Set the visualization for the nodes
        for (MarkerNode node : connectionsForNode.keySet()) {
            if (visible || !nodesFilterBy.contains(node.getRemoteObjectLight().getClassName()))
                setVisibleMakers(node, visible, visibleMarkers);
            else
                setVisibleMakers(node, !visible, visibleMarkers);
        }
        // Set the visualization for the connections        
        for (MarkerNode node : connectionsForNode.keySet())
                setVisibleConnections(connectionsFilterBy, node, visibleMarkers, visibleConnections);
        
        propertyChange(new PropertyChangeEvent(this, GM_EVENT_NAME_UPDATE_MARKERS , null, visibleMarkers));
        propertyChange(new PropertyChangeEvent(this, GM_EVENT_NAME_UPDATE_POLYLINES, null, visibleConnections));
    }
    
    private void setVisibleMakers(MarkerNode node, boolean visible, List<Marker> visibleMarkers) {
        if (node.isVisible() != visible) {
            node.setVisible(visible);
            visibleMarkers.add(node);
        }
    }
    
    private void setVisibleConnections(List<String> connectionsFilterBy, MarkerNode node, List<Marker> visibleMarkers, List<ConnectionPolyline> visibleConnections) {
        for (ConnectionPolyline conn : connectionsForNode.get(node)) {
            boolean visible = true;
            String className = conn.getConnectionInfo().getClassName();
                        
            if (!connectionsFilterBy.isEmpty() && !connectionsFilterBy.contains(className))
                visible = false;
            
            MarkerNode sourceNode = nodesForConnection.get(conn).get(0);
            MarkerNode targetNode = nodesForConnection.get(conn).get(1);
                        
            if (sourceNode.isVisible() == false)
                visible = false;
            else
                if (targetNode.isVisible() == false)
                    visible = false;
                            
            if (conn.isVisible() != visible) {
                
                if (conn.isEditable()) {
                    
                    for (MarkerPoint point : conn.getPoints()) {
                        point.setVisible(visible);
                        visibleMarkers.add(point);
                    }
                }
                conn.setVisible(visible);
                visibleConnections.add(conn);
            }
        }
    }

    public List<Object> getVisbleNodesAndConnections() {
        List<Object> objects = new ArrayList();
        
        for (MarkerNode markerNode : connectionsForNode.keySet())
            if (markerNode.isVisible())
                objects.add(markerNode);
        
        for (ConnectionPolyline connection : nodesForConnection.keySet())
            if (connection.isVisible())
                objects.add(connection);
        
        return objects;
    }

    void moveMapToOverlay(Object overlay) {
        if (overlay instanceof MarkerNode) 
            setCenter(((MarkerNode) overlay).getPosition());
        
        if (overlay instanceof ConnectionPolyline)
            setCenter(((ConnectionPolyline) overlay).getSource().getPosition());
            
    }
    
    private class MarkerClickListenerImpl implements MarkerClickListener {
        
        public MarkerClickListenerImpl() {
        }

        @Override
        public void markerClicked(GoogleMapMarker clickedMarker) {
            if (clickedMarker instanceof Marker)
                ((Marker) clickedMarker)
                        .firePropertyChangeEvent("markerClicked", null, null);            
            
            
            if (clickedMarker instanceof MarkerNode) {
                if (connectionEnable) {
                    if (newConnection == null) {
                        newConnection = new ConnectionPolyline((MarkerNode) clickedMarker);
                        
                        Notification notification = new Notification(
                                "Source node selected the next step is select "
                                        + "the target node", 
                                Notification.Type.HUMANIZED_MESSAGE);
                        notification.setDelayMsec(-1);
                        notification.show(Page.getCurrent());
                        /*
                        Notification.show("Source node of connection", 
                                Notification.Type.TRAY_NOTIFICATION);
                        */
                    }
                    else {
                        newConnection.setTarget((MarkerNode) clickedMarker);
                        
                        PhysicalConnectionWizard wizard = new PhysicalConnectionWizard(parentComponent, newConnection);
                        wizard.addCloseListener(CustomGoogleMap.this);
                        
                        getUI().addWindow(wizard);
                        // For test only.
                        //addNewPolyline();
                    }
                }
                
                MarkerNode clickedMarkerNode = (MarkerNode) clickedMarker;
                RemoteObjectLight clickedObjectNode = clickedMarkerNode.getRemoteObjectLight();
                InventoryObjectNode clickedInventoryObjectNode = new InventoryObjectNode(clickedObjectNode);
                DynamicTree dynamicTree = new DynamicTree(clickedInventoryObjectNode, parentComponent);
                
                clickedInventoryObjectNode.setTree(dynamicTree);
                                
                ItemClickEvent itemClickEvent = new ItemClickEvent(dynamicTree, null, clickedInventoryObjectNode, null, null);
                
                parentComponent.getEventBus().post(itemClickEvent);
            }
        }
    }
    
    private class MapClickListenerImpl implements MapClickListener {
        public MapClickListenerImpl() {
        }

        @Override
        public void mapClicked(LatLon position) {
            if (drawPolygonEnable) {
                MarkerPoint point = new MarkerPoint(position, true);
                getState().markers.put(point.getId(), point);
                
                if (newPolygon == null)
                    newPolygon = new Polygon(point, CustomGoogleMap.this);
                else
                    newPolygon.addPoint(point);
            }
        }
    }
    
    private void addConnectionForNode(MarkerNode node, ConnectionPolyline connection) {
        if (connectionsForNode.get(node) == null)
            connectionsForNode.put(node, new ArrayList());
        connectionsForNode.get(node).add(connection);
        
        if (nodesForConnection.get(connection) == null)
            nodesForConnection.put(connection, new ArrayList());
        nodesForConnection.get(connection).add(node);
        
        RemoteObjectLight connInfo = connection.getConnectionInfo();
        if (!getConnectionsFilter().contains(connInfo.getClassName()))
            getConnectionsFilter().add(connInfo.getClassName());
    }
    
    public boolean isEmpty() {
        return getState().markers.isEmpty() && getState().polygons.isEmpty() && 
                getState().polylines.isEmpty();
    }
    
    public List<String> getNodeFilter() {
        return nodesFilter;        
    }
    
    public List<String> getConnectionsFilter() {
        return connectionsFilter;
    }
        
    @Subscribe
    public void nodeChange(Property.ValueChangeEvent[] event) {
        long oid = (Long) event[0].getProperty().getValue();
        String newValue = (String) event[1].getProperty().getValue();
        
        MarkerNode markerNode = (MarkerNode) getState().markers.get(oid);
        
        if (markerNode != null) {
            markerNode.getRemoteObjectLight().setName(newValue);
            markerNode.setCaption(markerNode.getRemoteObjectLight().toString());
            
            propertyChange(new PropertyChangeEvent(this, GM_EVENT_NAME_UPDATE_MARKER, null, markerNode));
        }
    }
    
    /**
     * For test only.
     */
    private void addNewPolyline() {
        newConnection.setStrokeColor(Polyline.defaultPolylineColor);
        newConnection.setStrokeOpacity(1);
        newConnection.setStrokeWeight(3);

        newConnection.addPropertyChangeListener(this);

        getState().polylines.put(newConnection.getId(), newConnection);
        
        addConnectionForNode(newConnection.getSource(), newConnection);
        addConnectionForNode(newConnection.getTarget(), newConnection);
        
        newConnection = null;
    }
}
