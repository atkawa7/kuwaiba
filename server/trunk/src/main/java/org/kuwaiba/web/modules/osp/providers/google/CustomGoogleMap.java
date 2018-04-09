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
package org.kuwaiba.web.modules.osp.providers.google;

import com.google.common.eventbus.Subscribe;
//import com.vaadin.data.Property;
//import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ResourceReference;
import org.kuwaiba.web.modules.osp.providers.google.overlays.MarkerNode;
//import com.vaadin.tapio.googlemaps.GoogleMap;
//import com.vaadin.tapio.googlemaps.client.LatLon;
//import com.vaadin.tapio.googlemaps.client.events.EdgeClickListener;
//import com.vaadin.tapio.googlemaps.client.events.EdgeRightClickListener;
//import com.vaadin.tapio.googlemaps.client.events.MapMouseOverListener;
//import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
//import com.vaadin.tapio.googlemaps.client.events.MarkerDragListener;
//import com.vaadin.tapio.googlemaps.client.events.MarkerRightClickListener;
//import com.vaadin.tapio.googlemaps.client.events.PolygonCompleteListener;
//import com.vaadin.tapio.googlemaps.client.events.PolygonRightClickListener;
//import com.vaadin.tapio.googlemaps.client.events.PolylineClickListener;
//import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
//import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
//import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Window;
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
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.icons.ClassIcon;
import org.kuwaiba.apis.web.gui.menus.RawContextMenu;
import org.kuwaiba.apis.web.gui.modules.EmbeddableComponent;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.ClassInfoLight;
import org.kuwaiba.web.custom.tree.DynamicTree;
import org.kuwaiba.web.custom.wizards.physicalconnection.NewContainerWizard;
import org.kuwaiba.web.custom.wizards.physicalconnection.NewLinkWizard;
import org.kuwaiba.web.modules.osp.providers.google.overlays.ConnectionPolyline;
import org.kuwaiba.web.custom.wizards.physicalconnection.WizardInterface;
import org.kuwaiba.web.modules.osp.AbstractGISView;
import org.kuwaiba.web.modules.osp.providers.google.overlays.Polygon;

/**
 * Custom GoogleMap for Kuwaiba
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CustomGoogleMap /*extends GoogleMap*/ implements /*AbstractGISView, */
        EmbeddableComponent, Window.CloseListener {

    @Override
    public TopComponent getTopComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowClose(Window.CloseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    /**
     * Set implementation for edge, map, marker and polygon action listener
     */
//    private MarkerClickListener markerClickListener = new MarkerClickListener() {
//
//        @Override
//        public void markerClicked(GoogleMapMarker clickedMarker) {
//            if (clickedMarker instanceof MarkerNode) {
//                if (DRAWING_MODE_EDGE.equals(getState().drawingMode)) {
//                    if (newConnection == null) {
//                        newConnection = new ConnectionPolyline((MarkerNode) clickedMarker);
//                                                
//                        getState().markerSource = clickedMarker;
//                    }
//                    else {
//                        if (getState().markerSource.equals(clickedMarker))
//                            return;
//                        
//                        getState().markerSource = null;                        
//                        
//                        newConnection.setTarget((MarkerNode) clickedMarker);
//                        
//                        WebserviceBeanLocal wsBean = getTopComponent().getWsBean();
//                        String ipAddress = getUI().getPage().getWebBrowser().getAddress();
//                        String sessioId = getTopComponent().getApplicationSession().getSessionId();
//
//                        List<ClassInfoLight> linkClasses = null;
//                        List<ClassInfoLight> containerClasses = null;
//////                        try {
//////                            linkClasses = wsBean.getSubClassesLight(Constants.CLASS_GENERICPHYSICALLINK, false, false, ipAddress, sessioId);
//////                            containerClasses = wsBean.getSubClassesLight(Constants.CLASS_GENERICPHYSICALCONTAINER, false, false, ipAddress, sessioId);
//////                            
//////                            PhysicalConnectionWizard wizard = new PhysicalConnectionWizard(parentComponent, newConnection, linkClasses, containerClasses);
//////                            wizard.addCloseListener(CustomGoogleMap.this);
//////
//////                            getUI().addWindow(wizard);
//                            if (isContainer) {
//                                NewContainerWizard newContainerWizard = new NewContainerWizard(parentComponent, newConnection);
//                                newContainerWizard.addCloseListener(CustomGoogleMap.this);
//                                getUI().addWindow(newContainerWizard);
//                            } else {
//                                NewLinkWizard newLinkWizard = new NewLinkWizard(parentComponent, newConnection);
//                                newLinkWizard.addCloseListener(CustomGoogleMap.this);
//                                getUI().addWindow(newLinkWizard);
//                            }
//////                        } catch (ServerSideException ex) {
//////                            NotificationsUtil.showError(ex.getMessage());
//////                        }
//                    }
//                }
//                MarkerNode clickedMarkerNode = (MarkerNode) clickedMarker;
//                RemoteObjectLight clickedObjectNode = clickedMarkerNode.getRemoteObjectLight();
//                
//                //updatePropertySheet(clickedObjectNode);
//            }
//        }
//    };
//    
//    private MarkerRightClickListener markerRightClickListener = new MarkerRightClickListener() {
//
//        @Override
//        public void markerRightClicked(GoogleMapMarker clickedMarker) {
//            RawContextMenu rawContextMenu = new RawContextMenu(
//                ((MarkerNode) clickedMarker).getActions(), 
//                CustomGoogleMap.this, 
//                clickedMarker
//            );
//            rawContextMenu.show();
//        }
//    };
//    
//    private MarkerDragListener markerDragListener = new MarkerDragListener() {
//
//        @Override
//        public void markerDragged(GoogleMapMarker draggedMarker, LatLon oldPosition) {
//            updateView = true;
//        }
//    };
//        
//    private PolygonRightClickListener polygonRightClickListener = new PolygonRightClickListener() {
//
//        @Override
//        public void polygonRightClicked(GoogleMapPolygon clickedPolygon) {
//            RawContextMenu rawContextMenu = new RawContextMenu(
//                ((Polygon) clickedPolygon).getActions(), CustomGoogleMap.this, clickedPolygon);
//            rawContextMenu.show();
//        }
//    };
//    
//    private PolygonCompleteListener polygonCompleteListener = new PolygonCompleteListener() {
//
//        @Override
//        public void polygonCompleted(GoogleMapPolygon completedPolygon) {
//            if (DRAWING_MODE_POLYGON.equals(getState().drawingMode)) {
//                Polygon polygon = new Polygon(completedPolygon);
//                
//                getState().polygons.put(polygon.getId(), polygon);
//            }
//            else {
//                if (DRAWING_MODE_EDGE.equals(getState().drawingMode))
//                    toolState(DRAWING_MODE_EDGE);
//                else
//                    getState().polygons.get(completedPolygon.getId()).setCoordinates(completedPolygon.getCoordinates());
//            }
//            setUpdateView(true);
//        }
//    };
//    
//    private EdgeClickListener edgeClickListener = new EdgeClickListener() {
//
//        @Override
//        public void edgeClicked(GoogleMapPolyline clickedEdge) {
//            ConnectionPolyline clickedPhyConn = (ConnectionPolyline) clickedEdge;
//            RemoteObjectLight clickedPhyConnObject = clickedPhyConn.getConnectionInfo();
//
//            //updatePropertySheet(clickedPhyConnObject);
//        }
//    };
//        
//    private EdgeRightClickListener edgeRightClickListener = new EdgeRightClickListener() {
//
//        @Override
//        public void edgeRightClicked(GoogleMapPolyline clickedEdge) {
//            RawContextMenu rawContextMenu = new RawContextMenu(
//                ((ConnectionPolyline) clickedEdge).getActions(), 
//                CustomGoogleMap.this, 
//                clickedEdge
//            );
//            rawContextMenu.show();
//        }
//    };
//    /** 
//     * Listener to end the draw of a physical connection. 
//     */
//    private PolylineClickListener polylineClickListener = new PolylineClickListener() {
//
//        @Override
//        public void polylineClicked(GoogleMapPolyline clickedPolyline) {
//            if (clickedPolyline.getId() == -1) {
//                newConnection = null;
//                getState().markerSource = null;
//            }
//        }
//    };
//    
//    private MapMouseOverListener mapMouseOverListener = new MapMouseOverListener() {
//
//        @Override
//        public void mapMouseOver(LatLon position) {
//            if (markerNodeAdded != null) {
//                
//                markerNodeAdded.setPosition(position);
//                getState().markers.put(markerNodeAdded.getId(), markerNodeAdded);
//                
//                nodeClassesFilter.add(markerNodeAdded.getRemoteObjectLight().getClassName());
//                markerNodeAdded = null;
//                
//                setUpdateView(true);
//            }
//        }
//    };
//    /**
//     * Constants
//     */    
//    private static final String FORMAT_VERSION = "0.1";
//    private static final String DRAWING_MODE_POLYGON = "Polygon";
//    private static final String DRAWING_MODE_EDGE = "Polyline";
//            
//    private final TopComponent parentComponent;
//    /**
//     * List of edges for markerNode
//     */
//    private final Map<Long, List<ConnectionPolyline>> edgesForNode = new HashMap<>();
//            
//    /**
//     * The marker dragged to the map
//     */
//    private MarkerNode markerNodeAdded = null;
//    private ConnectionPolyline newConnection = null;
//    
//    private final List<String> nodeClassesFilter = new ArrayList();
//    private final List<String> phyConnClassesFilter = new ArrayList();
//    /**
//     * The view is update when nodes or physical connection was removed
//     */
//    private boolean updateView = false;
//    
//    private List<AbstractAction> actions = null;
//    
//    private boolean isContainer = false;
//            
//    public CustomGoogleMap(TopComponent parentComponent, String apiKey, String clientId, String language) {
//        super(apiKey, clientId, language);
//        
//        this.parentComponent = parentComponent;
//                
//        setDisableDoubleClickZoom(true);
//        getState().showEdgeLabels = true;
//        getState().showMarkerLabels = true;
//        getState().showPolygonLabels = false;
//        
//        GoogleMapPolyline dummyPolyline = new GoogleMapPolyline();
//        dummyPolyline.setId(-1);
//        getState().polylines.put(dummyPolyline.getId(), dummyPolyline);
//                
//        addMarkerClickListener(markerClickListener);
//        addMarkerDragListener(markerDragListener);
//        addMarkerRightClickListener(markerRightClickListener);
//        
//        addPolygonRightClickListener(polygonRightClickListener);
//        addPolygonCompleteListener(polygonCompleteListener);
//        
//        addPolylineClickListener(polylineClickListener);
//        
//        addEdgeClickListener(edgeClickListener);
//        addEdgeRightClickListener(edgeRightClickListener);
//        
//        addMapMouseOverListener(mapMouseOverListener);
//    }
//    
//    public boolean getUpdateView() {
//        return updateView;
//    }
//    
//    public void setUpdateView(boolean updateView) {
//        this.updateView = updateView;
//    }
//    
//    public void register() {
//        if (parentComponent != null)
//            parentComponent.getEventBus().register(this);
//    }
//    
//    public void unregister() {
//        if (parentComponent != null)
//            parentComponent.getEventBus().unregister(this);
//    }
//
//    @Override
//    public TopComponent getTopComponent() {
//        return parentComponent;
//    }
//    
//    @Override
//    public String getName() {
//        return "OSP Module for Google Maps";
//    }
//
//    @Override
//    public String getDescription() {
//        return "OSP Module that uses Google Maps as map provider";
//    }
//
//    @Override
//    public String getVersion() {
//        return "0.1";
//    }
//
//    @Override
//    public String getVendor() {
//        return "Neotropic SAS";
//    }
//
//    @Override
//    public void clear() {
//        updateView = true;
//        
//        for (GoogleMapPolyline edge : getState().edges.keySet())
//            deletePhysicalConnection((ConnectionPolyline) edge);
//        removeEdges();
//        getState().polygons.clear();
//        getState().markers.clear();
//    }
//    
//    public void newMap() {
//        updateView = false;
//        for (GoogleMapPolyline edge : getState().edges.keySet()) {
//            if (!((ConnectionPolyline) edge).isSaved()) {
//                deletePhysicalConnection((ConnectionPolyline) edge);
//            }
//        }
//        removeEdges();
//        getState().polygons.clear();
//        getState().markers.clear();
//    }
//            
//    private void toolState(String drawingMode) {
//        if (DRAWING_MODE_EDGE.equals(drawingMode) && 
//                DRAWING_MODE_POLYGON.equals(getState().drawingMode))
//            getState().drawingMode = null; // for some reason need set enable the select tool first
//        
//        getState().drawingMode = drawingMode;
//    }
//    
//    void enableSelectTool() {
//        toolState(null);
//    }
//    
//    public void enableConnectionTool(boolean isContainer) {
//        toolState(DRAWING_MODE_EDGE);
//        getState().markerSource = null;
//        newConnection = null;
//        this.isContainer = isContainer;
//    }
//            
//    public void enablePolygonTool() {
//        toolState(DRAWING_MODE_POLYGON);
//    }
//    
//    public void enableMeasureTool() {
//        toolState(null);
//        setMeasureDistance(!getMeasureDistance());
//    }
//        
//    public void enableCleanTool() {
//        toolState(null);
//        clear();
//    }
//    
//    void enableShowNodeLabelsTool() {
//        getState().showMarkerLabels = !getState().showMarkerLabels;
//    }
//
//    void enableShowConnectionLabelsTool() {
//        getState().showEdgeLabels = !getState().showEdgeLabels;
//    }
//    
//    void enableShowPolygonLabelsTool() {
//        getState().showPolygonLabels = !getState().showPolygonLabels;
//    }
//    
//    public void deleteMarkerNode(MarkerNode removeMarker) {
//        if (edgesForNode.containsKey(removeMarker.getId())) {
//            int size = edgesForNode.get(removeMarker.getId()).size();
//            
//            while (size > 0) {
//                ConnectionPolyline edge = edgesForNode.get(removeMarker.getId()).get(size - 1);
//                deletePhysicalConnection(edge);
//                removeEdge(edge);
//                
//                size = edgesForNode.get(removeMarker.getId()).size();                        
//            }
//        }
//        getState().markers.remove(removeMarker.getId());
//    }
//        
//    public void deletePhysicalConnection(ConnectionPolyline physicalConnection) {
//        try {
//            RemoteObjectLight phyConnInfo = physicalConnection.getConnectionInfo();
//            
//            getTopComponent().getWsBean().deletePhysicalConnection(
//                phyConnInfo.getClassName(), 
//                phyConnInfo.getOid(), 
//                Page.getCurrent().getWebBrowser().getAddress(), 
//                getTopComponent().getApplicationSession().getSessionId());
//            
//            edgesForNode.get(physicalConnection.getSource().getId())
//                    .remove(physicalConnection);
//            edgesForNode.get(physicalConnection.getTarget().getId())
//                    .remove(physicalConnection);
//        } catch (ServerSideException ex) {
//            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
//        }
//    }
//    
//    public void physicalConnectionsSaved() {
//        for (GoogleMapPolyline edge : getState().edges.keySet())
//            ((ConnectionPolyline) edge).setSaved(true);
//    }
//    
//    @Override
//    public byte[] getAsXML() {
//        try {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
//            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
//            XMLEventFactory xmlef = XMLEventFactory.newInstance();
//            
//            QName qnView = new QName("view");
//            xmlew.add(xmlef.createStartElement(qnView, null, null));
//            xmlew.add(xmlef.createAttribute(new QName("version"), FORMAT_VERSION));
//            
//            QName qnClass = new QName("class");
//            xmlew.add(xmlef.createStartElement(qnClass, null, null));
//            xmlew.add(xmlef.createCharacters("GISView"));
//            xmlew.add(xmlef.createEndElement(qnClass, null));
//            
//            QName qnCenter = new QName("center");
//            xmlew.add(xmlef.createStartElement(qnCenter, null, null));
//            xmlew.add(xmlef.createAttribute(new QName("lat"), Double.toString(getCenter().getLat())));
//            xmlew.add(xmlef.createAttribute(new QName("lon"), Double.toString(getCenter().getLon())));
//            xmlew.add(xmlef.createEndElement(qnCenter, null));
//            
//            QName qnZoom = new QName("zoom");
//            xmlew.add(xmlef.createStartElement(qnZoom, null, null));
//            xmlew.add(xmlef.createAttribute(new QName("value"), 
//                    Integer.toString(getZoom())));
//            xmlew.add(xmlef.createEndElement(qnZoom, null));
//            // nodes
//            QName qnNodes = new QName("nodes");
//            xmlew.add(xmlef.createStartElement(qnNodes, null, null));
//            
//            for (GoogleMapMarker gmMarker : getState().markers.values()) {
//                if (gmMarker instanceof MarkerNode) {
//                    QName qnameNode = new QName("node");
//                    xmlew.add(xmlef.createStartElement(qnameNode, null, null));
//                    
//                    MarkerNode node = (MarkerNode) gmMarker;
//                    
//                    xmlew.add(xmlef.createAttribute(new QName("lat"), 
//                            Double.toString(node.getPosition().getLat())));
//                    xmlew.add(xmlef.createAttribute(new QName("lon"), 
//                            Double.toString(node.getPosition().getLon())));
//                    
//                    RemoteObjectLight object = node.getRemoteObjectLight();
//                    
//                    xmlew.add(xmlef.createAttribute(new QName("class"), 
//                            object.getClassName()));
//                    xmlew.add(xmlef.createAttribute(new QName("id"), 
//                            Long.toString(object.getOid())));
//                    xmlew.add(xmlef.createAttribute(new QName("visible"), 
//                            Boolean.toString(node.isVisible())));
//                    
//                    xmlew.add(xmlef.createEndElement(qnameNode, null));
//                }
//            }
//            xmlew.add(xmlef.createEndElement(qnNodes, null));
//            // edges or connection
//            QName qnEdges = new QName("edges");
//            xmlew.add(xmlef.createStartElement(qnEdges, null, null));
//            
//            for (GoogleMapPolyline gmPolyline : getState().edges.keySet()) {
//                if (gmPolyline instanceof ConnectionPolyline) {
//                    ConnectionPolyline physicalConnection = (ConnectionPolyline) gmPolyline;
//                    
//                    QName qnEdge = new QName("edge");
//                    xmlew.add(xmlef.createStartElement(qnEdge, null, null));
//                    
//                    xmlew.add(xmlef.createAttribute(new QName("id"), 
//                            Long.toString(physicalConnection.getConnectionInfo().getOid())));
//                    xmlew.add(xmlef.createAttribute(new QName("strokeColor"), 
//                            physicalConnection.getStrokeColor()));
//                    xmlew.add(xmlef.createAttribute(new QName("strokeOpacity"), 
//                            Double.toString(physicalConnection.getStrokeOpacity())));
//                    xmlew.add(xmlef.createAttribute(new QName("strokeWeight"), 
//                            Integer.toString(physicalConnection.getStrokeWeight())));
//                    xmlew.add(xmlef.createAttribute(new QName("visible"), 
//                            Boolean.toString(physicalConnection.isVisible())));
//                    xmlew.add(xmlef.createAttribute(new QName("name"), 
//                            physicalConnection.getConnectionInfo().getName()));
//                    xmlew.add(xmlef.createAttribute(new QName("objectClass"), 
//                            physicalConnection.getConnectionInfo().getClassName()));
//
//                    xmlew.add(xmlef.createAttribute(new QName("aside"), 
//                            Long.toString(physicalConnection.getSource().getId())));
//                    xmlew.add(xmlef.createAttribute(new QName("bside"), 
//                            Long.toString(physicalConnection.getTarget().getId())));
//
//                    for (LatLon coordinate : physicalConnection.getCoordinates()) {
//                        QName qnCoordinate = new QName("coordinate");
//                        xmlew.add(xmlef.createStartElement(qnCoordinate, null, null));
//                        xmlew.add(xmlef.createAttribute(new QName("lat"), 
//                                Double.toString(coordinate.getLat())));
//                        xmlew.add(xmlef.createAttribute(new QName("lon"), 
//                                Double.toString(coordinate.getLon())));
//                        xmlew.add(xmlef.createEndElement(qnCoordinate, null));
//                    }
//                    xmlew.add(xmlef.createEndElement(qnEdge, null));
//                }
//            }
//            xmlew.add(xmlef.createEndElement(qnEdges, null));
//            // polygon
//            QName qnPolygons = new QName("polygons");
//            xmlew.add(xmlef.createStartElement(qnPolygons, null, null));
//            for (GoogleMapPolygon polygon : getState().polygons.values()) {
//                
//                QName qnPolygon = new QName("polygon");
//                xmlew.add(xmlef.createStartElement(qnPolygon, null, null));
//                
//                xmlew.add(xmlef.createAttribute(new QName("fillColor"), 
//                        polygon.getFillColor()));
//                xmlew.add(xmlef.createAttribute(new QName("fillOpacity"), 
//                        Double.toString(polygon.getFillOpacity())));
//                xmlew.add(xmlef.createAttribute(new QName("strokeColor"), 
//                        polygon.getStrokeColor()));
//                xmlew.add(xmlef.createAttribute(new QName("strokeOpacity"), 
//                        Double.toString(polygon.getStrokeOpacity())));
//                xmlew.add(xmlef.createAttribute(new QName("strokeWeight"), 
//                        Integer.toString(polygon.getStrokeWeight())));
//                xmlew.add(xmlef.createAttribute(new QName("visible"), 
//                        Boolean.toString(polygon.isVisible())));
//                for (LatLon coordinate : polygon.getCoordinates()) {
//                    QName qnCoordinate = new QName("coordinate");
//                    xmlew.add(xmlef.createStartElement(qnCoordinate, null, null));
//                    xmlew.add(xmlef.createAttribute(new QName("lat"), 
//                            Double.toString(coordinate.getLat())));
//                    xmlew.add(xmlef.createAttribute(new QName("lon"), 
//                            Double.toString(coordinate.getLon())));
//                    xmlew.add(xmlef.createEndElement(qnCoordinate, null));
//                }
//                xmlew.add(xmlef.createEndElement(qnPolygon, null));
//            }
//            xmlew.add(xmlef.createEndElement(qnPolygons, null));
//                       
//            xmlew.add(xmlef.createEndElement(qnView, null));
//            xmlew.close();
//            
//            return baos.toByteArray();
//        } catch (XMLStreamException ex) {
//            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
//        }
//        return null;
//    }
//    
//    @Override
//    public void render(byte[] structure) throws IllegalArgumentException {
//        try {
//            XMLInputFactory xmlif = XMLInputFactory.newInstance();
//            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
//            XMLStreamReader reader = xmlif.createXMLStreamReader(bais);
//            
//            WebserviceBeanLocal wsBean = getTopComponent().getWsBean();
//            String ipAddress = getUI().getPage().getWebBrowser().getAddress();
//            String sessioId = getTopComponent().getApplicationSession().getSessionId();
//            
//            QName qnCenter = new QName("center");
//            QName qnZoom = new QName("zoom");
//            QName qnNode = new QName("node");
//            QName qnEdge = new QName("edge");
//            QName qnPolygon = new QName("polygon");
//            QName qnCoordinate = new QName("coordinate");
//            
//            while (reader.hasNext()) {
//                int event = reader.next();
//                if (event == XMLStreamConstants.START_ELEMENT) {
//                    if (reader.getName().equals(qnCenter)) {
//                        double lat = Double.parseDouble(reader.getAttributeValue(null, "lat"));
//                        double lon = Double.parseDouble(reader.getAttributeValue(null, "lon"));
//                        setCenter(new LatLon(lat, lon));
//                    }
//                    if (reader.getName().equals(qnZoom)) {
//                        int zoom = Integer.parseInt(reader.getAttributeValue(null, "value"));
//                        setZoom(zoom);
//                    }
//                    if (reader.getName().equals(qnNode)) {
//                        double lat = Double.parseDouble(reader.getAttributeValue(null, "lat"));
//                        double lon = Double.parseDouble(reader.getAttributeValue(null, "lon"));
//                        long oid = Long.parseLong(reader.getAttributeValue(null, "id"));
//                        String objectClass = reader.getAttributeValue(null, "class");
//                        boolean visible = Boolean.valueOf(
//                                reader.getAttributeValue(null, "visible"));
//                                                                                                
//                        try {
//                            RemoteObjectLight objectNode = wsBean
//                                .getObjectLight(objectClass, oid, ipAddress, sessioId);
//                            
//                            MarkerNode markerNode = new MarkerNode(objectNode);
//                            markerNode.setId(oid);
//                            markerNode.setCaption(objectNode.toString());
//                            markerNode.setPosition(new LatLon(lat, lon));
//                            markerNode.setVisible(visible);
//                            markerNode.setIconUrl(getMarkerNodeIconUrl(objectNode));
//                            
//                            getState().markers.put(oid, markerNode);
//                            nodeClassesFilter.add(objectNode.getClassName());
//                        } catch (ServerSideException ex) {
//                            //Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
//                            updateView = true;
//                        }
//                    }
//                    else {
//                        if (reader.getName().equals(qnEdge)) {                            
//                            long oid = Long.parseLong(reader.getAttributeValue(null, "id"));
//                            String strokeColor = reader.getAttributeValue(
//                                    null, "strokeColor");
//                            double strokeOpacity = Double.valueOf(
//                                    reader.getAttributeValue(null, "strokeOpacity"));
//                            int strokeWeight = Integer.valueOf(
//                                    reader.getAttributeValue(null, "strokeWeight"));
//                            boolean visible = Boolean.valueOf(
//                                    reader.getAttributeValue(null, "visible"));
//                            String name = reader.getAttributeValue(null, "name");
//                            String objectClass = reader.getAttributeValue(null, "objectClass");                            
//                            
//                            long aside = Long.parseLong(reader.getAttributeValue(null, "aside"));
//                            long bside = Long.parseLong(reader.getAttributeValue(null, "bside"));
//                            
//                            try {
//                                RemoteObjectLight edgeInfo = wsBean.getObjectLight(objectClass, oid, ipAddress, sessioId);
//                                
//                                MarkerNode source = (MarkerNode) getState().markers.get(aside);
//                                MarkerNode target = (MarkerNode) getState().markers.get(bside);
//
//                                ConnectionPolyline edge = new ConnectionPolyline(source, target);
//                                edge.setId(oid);
//                                edge.setCaption(name);
//                                edge.setStrokeColor(strokeColor);
//                                edge.setStrokeOpacity(strokeOpacity);
//                                edge.setStrokeWeight(strokeWeight);
//                                edge.setVisible(visible);
//                                edge.setConnectionInfo(edgeInfo);
//
//                                List<LatLon> coordinates = new ArrayList();
//                                while (true) {
//                                    reader.nextTag();
//                                    if (reader.getName().equals(qnCoordinate)) {
//                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
//                                            Double lat = Double.parseDouble(
//                                                    reader.getAttributeValue(null, "lat"));
//                                            Double lon = Double.parseDouble(
//                                                    reader.getAttributeValue(null, "lon"));
//                                            coordinates.add(new LatLon(lat, lon));
//                                        }
//                                    }
//                                    else {
//                                        edge.setCoordinates(coordinates);
//                                        break;
//                                    }
//                                }
//                                edge.setSaved(true);
//
//                                if (!edgesForNode.containsKey(source.getId()))
//                                    edgesForNode.put(source.getId(), new ArrayList());
//                                edgesForNode.get(source.getId()).add(edge);
//
//                                if (!edgesForNode.containsKey(target.getId()))
//                                    edgesForNode.put(target.getId(), new ArrayList());
//                                edgesForNode.get(target.getId()).add(edge);
//
//                                if (!phyConnClassesFilter.contains(edge.getConnectionInfo().getClassName()))
//                                    phyConnClassesFilter.add(edge.getConnectionInfo().getClassName());
//
//                                addEdge(edge, edge.getSource(), edge.getTarget());                                
//                            } catch (ServerSideException ex) {
//                                updateView = true;
//                            }
//                        }
//                        else {
//                            if (reader.getName().equals(qnPolygon)) {                                
//                                String fillColor = reader.getAttributeValue(
//                                    null, "fillColor");
//                                double fillOpacity = Double.valueOf(reader
//                                        .getAttributeValue(null, "fillOpacity"));
//                                String strokeColor = reader.getAttributeValue(
//                                        null, "strokeColor");
//                                double strokeOpacity = Double.valueOf(reader.
//                                        getAttributeValue(null, "strokeOpacity"));
//                                int strokeWeight = Integer.valueOf(reader
//                                        .getAttributeValue(null, "strokeWeight"));
//                                boolean visible = Boolean.valueOf(reader
//                                        .getAttributeValue(null, "visible"));
//                                
//                                Polygon polygon = new Polygon();
//                                polygon.setFillColor(fillColor);
//                                polygon.setFillOpacity(fillOpacity);
//                                polygon.setStrokeColor(strokeColor);
//                                polygon.setStrokeOpacity(strokeOpacity);
//                                polygon.setStrokeWeight(strokeWeight);
//                                polygon.setVisible(visible);
//                                
//                                List<LatLon> coordinates = new ArrayList();
//                                while (true) {
//                                    reader.nextTag();
//                                    if (reader.getName().equals(qnCoordinate)) {
//                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
//                                            Double lat = Double.parseDouble(
//                                                    reader.getAttributeValue(
//                                                            null, "lat"));
//                                            Double lon = Double.parseDouble(
//                                                    reader.getAttributeValue(
//                                                            null, "lon"));
//                                            coordinates.add(new LatLon(lat, lon));
//                                        }
//                                    }
//                                    else {                                                                                
//                                        polygon.setCoordinates(coordinates);                                        
//                                        break;
//                                    }
//                                }                                
//                                getState().polygons.put(polygon.getId(), polygon);
//                            } // end if polygons
//                        } // end if edges
//                    } // end if nodes
//                } // end if
//            } // end while
//            reader.close();
//        } catch(XMLStreamException ex) {
//            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
//        }
//    }
//
//    @Override
//    public void windowClose(Window.CloseEvent e) {
//        if (e.getWindow() instanceof WizardInterface) {
//            WizardInterface wizardInterface = (WizardInterface) e.getWindow();
//                    
//            if (!wizardInterface.isWizardCompleted())
//                Notification.show("Physical connection wizard was cancelled", 
//                        Notification.Type.TRAY_NOTIFICATION);
//
//            if (wizardInterface.isWizardCompleted()) {
//                if (newConnection.getConnectionInfo().getOid() != -1L) {
//
//                    if (!edgesForNode.containsKey(newConnection.getSource().getId()))
//                        edgesForNode.put(newConnection.getSource().getId(), new ArrayList());
//                    edgesForNode.get(newConnection.getSource().getId()).add(newConnection);
//
//                    if (!edgesForNode.containsKey(newConnection.getTarget().getId()))
//                        edgesForNode.put(newConnection.getTarget().getId(), new ArrayList());
//                    edgesForNode.get(newConnection.getTarget().getId()).add(newConnection);
//
//                    addEdge(newConnection, newConnection.getSource(), newConnection.getTarget());
//
//                    if (!getConnectionsFilter().contains(newConnection.getConnectionInfo().getClassName()))
//                        getConnectionsFilter().add(newConnection.getConnectionInfo().getClassName());
//
//                    newConnection.setSaved(false);
//                    setUpdateView(true);
//                }
//            }
//            getUI().removeWindow(e.getWindow());
//        }
//        newConnection = null;
//    }
//    
//    /*
//     * Initialize the object dropped in the map 
//     */
//    public void addNodeMarker(RemoteObjectLight objectNode) {
//        if (getState().markers.containsKey(objectNode.getOid())) {
//            Notification.show(String.format("This view already contains the object %s", objectNode), Notification.Type.WARNING_MESSAGE);
//            return;
//        }
//        
//        WebserviceBeanLocal wsBean = getTopComponent().getWsBean();
//        String ipAddress = getUI().getPage().getWebBrowser().getAddress();
//        String sessioId = getTopComponent().getApplicationSession().getSessionId();
//        
//        try {
//            if (!wsBean.isSubclassOf(objectNode.getClassName(), "ViewableObject", ipAddress, sessioId)) { //NOI18N
//                Notification.show("Only ViewableObject are allowed", Notification.Type.WARNING_MESSAGE);
//                return;
//            }
//        } catch(ServerSideException ex) {
//            Notification.show(String.format("Unexpected error: %s", ex.getMessage()), Notification.Type.ERROR_MESSAGE);
//        }
//        markerNodeAdded = new MarkerNode(objectNode);
//        markerNodeAdded.setId(objectNode.getOid());
//        markerNodeAdded.setCaption(objectNode.toString());
//                                
//        markerNodeAdded.setIconUrl(getMarkerNodeIconUrl(objectNode));
//    }
//    
//    private String getMarkerNodeIconUrl(RemoteObjectLight objectNode) {
//        Resource icon = ClassIcon.newInstance(parentComponent).getIcon(objectNode.getClassName());
//        
//        setResource(Long.toString(objectNode.getOid()), icon);
//        ResourceReference rr = ResourceReference.create(icon, this, Long.toString(objectNode.getOid()));
//        
//        return ClassIcon.newInstance(parentComponent).getIconUrl(rr);
//    }
//    
//    public void removeConnectionUnsaved() {
//        for (GoogleMapPolyline edge : getState().edges.keySet()) {
//            ConnectionPolyline phyConn = (ConnectionPolyline) edge;
//            
//            if (!phyConn.isSaved()) {
//                deletePhysicalConnection(phyConn);
//                getState().edges.remove(edge);
//            }
//        }
//    }
//            
//    public void filterby(List<String> nodesFilterBy, List<String> connectionsFilterBy) {
//        
//        for (Long nodeID : edgesForNode.keySet()) {
//            MarkerNode markerNode = (MarkerNode) getState().markers.get(nodeID);
//            
//            if (nodesFilterBy.contains(markerNode.getRemoteObjectLight().getClassName())) {
//                if (!markerNode.isVisible()) {
//                    markerNode.setVisible(true);
//                }
//            }
//            else {
//                if (markerNode.isVisible() != false)
//                    markerNode.setVisible(false);
//            }
//        }
//        
//        for (GoogleMapPolyline edge : getState().edges.keySet()) {
//            ConnectionPolyline phyConn = (ConnectionPolyline) edge;
//            
//            if (connectionsFilterBy.contains(phyConn.getConnectionInfo().getClassName())) {
//                
//                if (phyConn.getSource().isVisible() && phyConn.getTarget().isVisible()) {
//                    
//                    if (!phyConn.isVisible()) {
//                        phyConn.setVisible(true);
//                    }
//                }
//                else {
//                    
//                    if (phyConn.isVisible())
//                        phyConn.setVisible(false);
//                }
//            }
//            else {
//                if (phyConn.getSource().isVisible() && phyConn.getTarget().isVisible()) {
//                    
//                    if (phyConn.isVisible() != false)
//                        phyConn.setVisible(false);
//                }
//                else {
//                    
//                    if (phyConn.isVisible())
//                        phyConn.setVisible(false);
//                }
//            }
//        }
//    }
//            
//    public List<Object> getVisbleNodesAndConnections() {
//        List<Object> objects = new ArrayList();
//        
//        for (Long markerID : getState().markers.keySet()) {
//            if (getState().markers.get(markerID).isVisible()) {
//                objects.add(getState().markers.get(markerID));
//            }
//        }
//        
//        for (Long nodeID : edgesForNode.keySet()) {
//            MarkerNode markerNode = (MarkerNode) getState().markers.get(nodeID);
//            
//            if (markerNode.isVisible()) {
//                                
//                for (ConnectionPolyline edge : edgesForNode.get(nodeID)) {
//                    if (edge.isVisible())
//                        objects.add(edge);
//                }
//            }
//        }
//        return objects;
//    }
//
//    public void moveMapToOverlay(Object overlay) {
//        if (overlay instanceof MarkerNode) 
//            setCenter(((MarkerNode) overlay).getPosition());
//        
//        if (overlay instanceof ConnectionPolyline)
//            setCenter(((ConnectionPolyline) overlay).getSource().getPosition());
//    }
//    
//    public boolean isEmpty() {
//        return getState().markers.isEmpty() && getState().polygons.isEmpty() && 
//                getState().edges.isEmpty();
//    }
//    
//    public List<String> getNodeFilter() {
//        return nodeClassesFilter;        
//    }
//    
//    public List<String> getConnectionsFilter() {
//        return phyConnClassesFilter;
//    }
            
//    @Subscribe
//    public void nodeChange(Property.ValueChangeEvent[] event) {
//        long oid = (Long) event[0].getProperty().getValue();
//        String newValue = (String) event[1].getProperty().getValue();
//        
//        MarkerNode markerNode = (MarkerNode) getState().markers.get(oid);
//        
//        if (markerNode != null) {
//            markerNode.getRemoteObjectLight().setName(newValue);
//            markerNode.setCaption(markerNode.getRemoteObjectLight().toString());
//        }
//    }
//    
//    private void updatePropertySheet(RemoteObjectLight object) {
//        InventoryObjectNode clickedInventoryObjectNode = new InventoryObjectNode(object);
//        DynamicTree dynamicTree = new DynamicTree(clickedInventoryObjectNode, parentComponent);
//        
//        clickedInventoryObjectNode.setTree(dynamicTree);
//        
//        ItemClickEvent itemClickEvent = new ItemClickEvent(dynamicTree, null, clickedInventoryObjectNode, null, null);
//        parentComponent.getEventBus().post(itemClickEvent);
//    }
}