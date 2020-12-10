/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ospman.widgets;

import com.neotropic.kuwaiba.modules.commercial.ospman.helpers.HelperEdgeDraw;
import com.neotropic.kuwaiba.modules.commercial.ospman.helpers.HelperContainerSelector;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.DialogOspViews;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.DialogMarker;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowContainers;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowEdge;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapOverlay;
import com.neotropic.flow.component.mxgraph.Point;
import com.neotropic.kuwaiba.modules.commercial.ospman.OutsidePlantService;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoPoint;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapConstants;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapEdge;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapGraph;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowDeleteOspView;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowNewContainer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractView;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewEdge;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewEventListener;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewMap;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.SimpleNotification;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapProvider;
import com.vaadin.flow.component.html.Label;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.notifications.AbstractNotification;

/**
 * Graphically displays Outside Plant elements on a map.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OutsidePlantView extends AbstractView<BusinessObjectLight, Component> {
    /**
     * Set of Outside Plant View XML tags
     */
    private final String TAG_VIEW = "view"; //NOI18N
    private final String TAG_CLASS = "class"; //NOI18N
    private final String TAG_CENTER = "center"; //NOI18N
    private final String TAG_ZOOM = "zoom"; //NOI18N
    private final String TAG_NODES = "nodes"; //NOI18N
    private final String TAG_NODE = "node"; //NOI18N
    private final String TAG_EDGES = "edges"; //NOI18N
    private final String TAG_EDGE = "edge"; //NOI18N
    private final String TAG_CONTROL_POINT = "controlpoint"; //NOI18N
    /**
     * Set of Outside Plant View XML attributes
     */
    private final String ATTR_ID = "id"; //NOI18N
    private final String ATTR_CLASS = "class"; //NOI18N
    private final String ATTR_A_SIDE_ID = "asideid"; //NOI18N
    private final String ATTR_A_SIDE_CLASS = "asideclass"; //NOI18N
    private final String ATTR_B_SIDE_ID = "bsideid"; //NOI18N
    private final String ATTR_B_SIDE_CLASS = "bsideclass"; //NOI18N
    private final String ATTR_VERSION = "version"; ///NOI18N
    /**
     * Set of Outside Plant View properties
     */
    private class PropertyNames {
        public static final String CENTER = "center"; //NOI18N
        public static final String ZOOM = "zoom"; //NOI18N
    }    
    /**
     * Map Provider to the Outside Plant View
     */
    private MapProvider mapProvider;
    /**
     * Reference to the translation service.
     */
    private final TranslationService ts;
    /**
     * Reference to the Application Entity Manager
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager
     */
    private final MetadataEntityManager mem;
    /**
     * Reference to the Resource Factory
     */
    private final ResourceFactory resourceFactory;
    /**
     * Reference to the Physical Connections Service
     */
    private final PhysicalConnectionsService physicalConnectionsService;
    /**
     * Reference to the New Business Object Visual Action
     */
    private final NewBusinessObjectVisualAction newBusinessObjectVisualAction;
    /**
     * The Outside Plant View Component
     */
    private Div component;
    
    private HelperEdgeDraw polylineDrawHelper;
    private HelperContainerSelector wiresHelper;
    
    enum Tool {
        Hand,
        Marker,
        Polyline,
        Wire,
        NewView,
        OpenView,
        SaveView,
        DeleteView
    }
    final private HashMap<Tab, Tool> tabs = new HashMap();
    final private HashMap<Tool, Tab> tools = new HashMap();
    private Tabs componentTabs;
    private Tab selectedTab;
    private final boolean viewTools;
    /**
     * Contains the graph of nodes and edges
     */
    private MapOverlay mapOverlay;
    /**
     * The graph containing the nodes and edges
     */
    private MapGraph mapGraph;
    
    private final String jsRedrawGraph;
    
    public OutsidePlantView(
        ApplicationEntityManager aem, 
        BusinessEntityManager bem, 
        MetadataEntityManager mem, 
        TranslationService ts, 
        ResourceFactory resourceFactory,
        PhysicalConnectionsService physicalConnectionsService, 
        NewBusinessObjectVisualAction newBusinessObjectVisualAction,
        boolean viewTools, String jsRedrawGraph) {
        Objects.requireNonNull(jsRedrawGraph);
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        this.physicalConnectionsService = physicalConnectionsService;
        this.newBusinessObjectVisualAction = newBusinessObjectVisualAction;
        this.viewTools = viewTools;
        this.jsRedrawGraph = jsRedrawGraph;
    }
    
    @Override
    public String getName() {
        return ts.getTranslatedString("module.ospman.name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.ospman.description");
    }

    @Override
    public String getVersion() {
        return "1.2";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>";
    }
    
    /**
     * <pre>{@code
     * <view version="">
     *  <class>OSPView</class>
     *  <center lon="" lat=""></center>
     *  <zoom>0</zoom>
     *  <nodes>
     *   <node lon="" lat="" class="businessObjectClass">businessObjectId</node>
     *  </nodes>
     *  <edge>
     *   <edge id="" class="" asideid="" asideclass="" bsideid="" bsideclass="">
     *    <controlpoint lon="" lat=""></controlpoint>
     *   </edge>
     *  </edge>
     * </view>
     * }</pre>
     */    
    @Override
    public byte[] getAsXml() {
        final QName tagView = new QName(TAG_VIEW);
        final QName tagClass = new QName(TAG_CLASS);
        final QName tagCenter = new QName(TAG_CENTER);
        final QName tagZoom = new QName(TAG_ZOOM);
        final QName tagNodes = new QName(TAG_NODES);
        final QName tagNode = new QName(TAG_NODE);
        final QName tagEdges = new QName(TAG_EDGES);
        final QName tagEdge = new QName(TAG_EDGE);
        final QName tagControlpoint = new QName(TAG_CONTROL_POINT);

        final QName attrLon = new QName(MapConstants.ATTR_LON);
        final QName attrLat = new QName(MapConstants.ATTR_LAT);
        final QName attrClass = new QName(ATTR_CLASS);
        final QName attrAsideId = new QName(ATTR_A_SIDE_ID);
        final QName attrAsideClass = new QName(ATTR_A_SIDE_CLASS);
        final QName attrBsideId = new QName(ATTR_B_SIDE_ID);
        final QName attrBsideClass = new QName(ATTR_B_SIDE_CLASS);
        final QName attrVersion = new QName(ATTR_VERSION);
        
        viewMap.getProperties().put(PropertyNames.CENTER, mapProvider.getCenter());
        viewMap.getProperties().put(PropertyNames.ZOOM, mapProvider.getZoom());
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            xmlew.add(xmlef.createStartElement(tagView, null, null));
            xmlew.add(xmlef.createAttribute(attrVersion, OutsidePlantService.VIEW_VERSION));
            
            xmlew.add(xmlef.createStartElement(tagClass, null, null));
            xmlew.add(xmlef.createCharacters("OSPView")); //NOI18N
            xmlew.add(xmlef.createEndElement(tagClass, null));
            
            xmlew.add(xmlef.createStartElement(tagCenter, null, null));
            xmlew.add(xmlef.createAttribute(attrLat, Double.toString(((GeoCoordinate) viewMap.getProperties().get(PropertyNames.CENTER)).getLatitude())));
            xmlew.add(xmlef.createAttribute(attrLon, Double.toString(((GeoCoordinate) viewMap.getProperties().get(PropertyNames.CENTER)).getLongitude())));
            xmlew.add(xmlef.createEndElement(tagCenter, null));
            
            xmlew.add(xmlef.createStartElement(tagZoom, null, null));
            xmlew.add(xmlef.createCharacters(String.valueOf(viewMap.getProperties().get(PropertyNames.ZOOM))));
            xmlew.add(xmlef.createEndElement(tagZoom, null));
            
            xmlew.add(xmlef.createStartElement(tagNodes, null, null));
            for (AbstractViewNode node : viewMap.getNodes()) {
                xmlew.add(xmlef.createStartElement(tagNode, null, null));
                xmlew.add(xmlef.createAttribute(attrLat, String.valueOf(node.getProperties().get(MapConstants.ATTR_LAT))));
                xmlew.add(xmlef.createAttribute(attrLon, String.valueOf(node.getProperties().get(MapConstants.ATTR_LON))));
                xmlew.add(xmlef.createAttribute(attrClass, ((BusinessObjectLight) node.getIdentifier()).getClassName()));
                xmlew.add(xmlef.createCharacters(((BusinessObjectLight) node.getIdentifier()).getId()));
                xmlew.add(xmlef.createEndElement(tagNode, null));
            }
            xmlew.add(xmlef.createEndElement(tagNodes, null));
            
            xmlew.add(xmlef.createStartElement(tagEdges, null, null));
            for (AbstractViewEdge edge : viewMap.getEdges()) {
                BusinessObjectLight businessObject = (BusinessObjectLight) edge.getIdentifier();
                
                xmlew.add(xmlef.createStartElement(tagEdge, null, null));
                xmlew.add(xmlef.createAttribute(ATTR_ID, businessObject.getId()));
                xmlew.add(xmlef.createAttribute(ATTR_CLASS, businessObject.getClassName()));
                BusinessObjectLight source = (BusinessObjectLight) viewMap.getEdgeSource(edge).getIdentifier();
                BusinessObjectLight target = (BusinessObjectLight) viewMap.getEdgeTarget(edge).getIdentifier();
                xmlew.add(xmlef.createAttribute(attrAsideId, source.getId()));
                xmlew.add(xmlef.createAttribute(attrAsideClass, source.getClassName()));
                xmlew.add(xmlef.createAttribute(attrBsideId, target.getId()));
                xmlew.add(xmlef.createAttribute(attrBsideClass, target.getClassName()));
                                
                for (GeoCoordinate controlPoint : (List<GeoCoordinate>) edge.getProperties().get(MapConstants.PROPERTY_CONTROL_POINTS)) {
                    xmlew.add(xmlef.createStartElement(tagControlpoint, null, null));
                    xmlew.add(xmlef.createAttribute(attrLat, String.valueOf(controlPoint.getLatitude())));
                    xmlew.add(xmlef.createAttribute(attrLon, String.valueOf(controlPoint.getLongitude())));
                    xmlew.add(xmlef.createEndElement(tagControlpoint, null));
                }
                xmlew.add(xmlef.createEndElement(tagEdge, null));
            }
            xmlew.add(xmlef.createEndElement(tagEdges, null));
            
            xmlew.add(xmlef.createEndElement(tagView, null));
            xmlew.close();
            
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            return new byte[0];
        }
    }

    @Override
    public byte[] getAsImage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void disableEnableTabs(List<Tab> disableTabs, List<Tab> enableTabs) {
        if (disableTabs != null)
            disableTabs.forEach(tab -> tab.setEnabled(false));
        if (enableTabs != null)
            enableTabs.forEach(tab -> tab.setEnabled(true));
    }
    
    private void newOverlay() {
        mapOverlay = mapProvider.createOverlay();
        
        Div divGraph = new Div();
        divGraph.setSizeFull();
        mapOverlay.addWidthChangedEventListener(widthChangedEvent -> {
            mapOverlay.removeWidthChangedEventListener(widthChangedEvent.getListener());
            mapGraph = new MapGraph();
            mapGraph.addGraphLoadedListener(event -> {
                event.unregisterListener();
                drawGraph();
            });
            divGraph.add(mapGraph);
        });
        mapOverlay.getComponent().add(divGraph);
    }
    
    private void redrawGraph() {
        HashMap<String, List<GeoCoordinate>> coordinates = new HashMap();        
        HashMap<String, GeoCoordinate> coordinatesToContainsLocations = new HashMap();
        HashMap<String, String> labels = new HashMap();
        
        viewMap.getNodes().forEach(node -> {
            BusinessObjectLight identifier = (BusinessObjectLight) node.getIdentifier();
            
            coordinates.put(
                identifier.getId(), 
                Arrays.asList(new GeoCoordinate(
                    (double) node.getProperties().get(MapConstants.ATTR_LAT), 
                    (double) node.getProperties().get(MapConstants.ATTR_LON)
                ))
            );
            coordinatesToContainsLocations.put(
                identifier.getId(), 
                new GeoCoordinate(
                    (double) node.getProperties().get(MapConstants.ATTR_LAT),
                    (double) node.getProperties().get(MapConstants.ATTR_LON)
                )
            );
            labels.put(identifier.getId(), identifier.getName());
        });
        viewMap.getEdges().forEach(edge -> {
            BusinessObjectLight identifier = (BusinessObjectLight) edge.getIdentifier();
            
            coordinates.put(
                identifier.getId(),
                (List) edge.getProperties().get(MapConstants.PROPERTY_CONTROL_POINTS)
            );
            labels.put(identifier.getId(), identifier.getName());
        });
        if (!coordinates.isEmpty()) {
            List<List<GeoCoordinate>> paths = new ArrayList();            
            /**
             *  The rectangle represents the visible area of the map.
             *  nw --------------- ne
             *     |             |
             *     |             |
             *     |             |
             *  sw --------------- se
             */
            GeoCoordinate northeast = mapProvider.getBounds().getNortheast();
            GeoCoordinate southwest = mapProvider.getBounds().getSouthwest();

            GeoCoordinate nw = new GeoCoordinate(northeast.getLatitude(), southwest.getLongitude());
            GeoCoordinate se = new GeoCoordinate(southwest.getLatitude(), northeast.getLongitude());

            paths.add(Arrays.asList(nw, northeast, se, southwest, nw));
        
            mapProvider.callbackContainsLocations(coordinatesToContainsLocations, paths, containsLocations -> {
                String boundsId = UUID.randomUUID().toString();

                coordinates.put(boundsId, Arrays.asList(
                    mapProvider.getBounds().getNortheast(),
                    mapProvider.getBounds().getSouthwest()
                ));
                mapOverlay.getProjectionFromLatLngToDivPixel(coordinates, pixelCoordinates -> {
                    GeoPoint ne = pixelCoordinates.get(boundsId).get(0);
                    GeoPoint sw = pixelCoordinates.get(boundsId).get(1);

                    JsonObject nodes = Json.createObject();
                    viewMap.getNodes().forEach(node -> {
                        BusinessObjectLight businessObject = (BusinessObjectLight) node.getIdentifier();
                        GeoPoint point = pixelCoordinates.get(businessObject.getId()).get(0);

                        JsonArray jsonPoints = Json.createArray();
                        JsonObject jsonPoint = Json.createObject();
                        jsonPoint.put(MapConstants.X, point.getX() - sw.getX());
                        jsonPoint.put(MapConstants.Y, point.getY() - ne.getY());
                        jsonPoints.set(0, jsonPoint);
                        nodes.put(businessObject.getId(), jsonPoints);
                    });
                    JsonObject edges = Json.createObject();
                    viewMap.getEdges().forEach(edge -> {
                        BusinessObjectLight businessObject = (BusinessObjectLight) edge.getIdentifier();
                        if (pixelCoordinates.containsKey(businessObject.getId())) {
                            List<GeoPoint> points = pixelCoordinates.get(businessObject.getId());
                            if (!points.isEmpty()) {
                                JsonArray jsonPoints = Json.createArray();
                                for (int i = 0; i < points.size(); i++) {
                                    GeoPoint point = points.get(i);
                                    JsonObject jsonPoint = Json.createObject();
                                    jsonPoint.put(MapConstants.X, point.getX() - sw.getX());
                                    jsonPoint.put(MapConstants.Y, point.getY() - ne.getY());
                                    jsonPoints.set(i, jsonPoint);
                                }
                                edges.put(businessObject.getId(), jsonPoints);
                            }
                        }
                    });
                    JsonObject jsonContainsLocations = Json.createObject();
                    containsLocations.forEach((key, value) -> jsonContainsLocations.put(key, value));
                    
                    JsonObject jsonLabels = Json.createObject();
                    labels.forEach((key, value) -> jsonLabels.put(key, value));

                    mapGraph.getElement().executeJs(jsRedrawGraph,
                        mapProvider.getComponent(), //JS parameter $0
                        nodes, //JS parameter $1
                        edges, //JS parameter $2
                        jsonContainsLocations, //JS parameter $3
                        mapProvider.getZoom(), //JS parameter $4
                        mapProvider.getMinZoomForLabels(), //JS parameter $5
                        jsonLabels //JS parameter $6
                    );
                });
            });
        }
        else
            hideMapGraph(false);
    }
    
    private void hideMapGraph(boolean hidden) {
        if (mapGraph != null) {
            StringBuilder expression = new StringBuilder();
            // Necessary checks to not modify the DOM multiple times.
            expression.append("if ($0) {").append("\n");
            expression.append("  this.style.opacity = 0;").append("\n");
            expression.append("} else {").append("\n");
            expression.append("  this.style.opacity = 1;").append("\n");
            expression.append("}");
            mapGraph.getElement().executeJs(expression.toString(), hidden);
        }
    }
    
    private void drawGraph() {
        HashMap<String, List<GeoCoordinate>> coordinates = new HashMap();

        viewMap.getNodes().forEach(node -> 
            coordinates.put(
                ((BusinessObjectLight) node.getIdentifier()).getId(), 
                Arrays.asList(new GeoCoordinate(
                    (double) node.getProperties().get(MapConstants.ATTR_LAT), 
                    (double) node.getProperties().get(MapConstants.ATTR_LON)
                ))
            )
        );
        viewMap.getEdges().forEach(edge -> coordinates.put(
            ((BusinessObjectLight) edge.getIdentifier()).getId(), 
            (List) edge.getProperties().get(MapConstants.PROPERTY_CONTROL_POINTS)
        ));
        if (!coordinates.isEmpty()) {
            String boundsId = UUID.randomUUID().toString();
            coordinates.put(boundsId, Arrays.asList(
                mapProvider.getBounds().getNortheast(),
                mapProvider.getBounds().getSouthwest()
            ));
            mapOverlay.getProjectionFromLatLngToDivPixel(coordinates, pixelCoordinates -> {
                List<GeoPoint> bounds = pixelCoordinates.get(boundsId);                        
                GeoPoint ne = bounds.get(0);
                GeoPoint sw = bounds.get(1);

                viewMap.getNodes().forEach(node -> {
                    BusinessObjectViewNode viewNode = (BusinessObjectViewNode) node;
                    GeoPoint pixelCoordinate = pixelCoordinates.get(viewNode.getIdentifier().getId()).get(0);

                    viewNode.getProperties().put(MapConstants.X, pixelCoordinate.getX() - sw.getX());
                    viewNode.getProperties().put(MapConstants.Y, pixelCoordinate.getY() - ne.getY());

                    addNode(viewNode.getIdentifier(), viewNode.getProperties());
                });
                viewMap.getEdges().forEach(edge -> {
                    BusinessObjectViewEdge viewEdge = (BusinessObjectViewEdge) edge;
                    AbstractViewNode source = viewMap.getEdgeSource(edge);
                    AbstractViewNode target = viewMap.getEdgeTarget(edge);

                    List<GeoPoint> points = new ArrayList();
                    if (pixelCoordinates.containsKey(viewEdge.getIdentifier().getId())) {
                        pixelCoordinates.get(viewEdge.getIdentifier().getId()).forEach(point -> 
                            points.add(new GeoPoint(
                                point.getX() - sw.getX(), 
                                point.getY() - ne.getY()
                            ))
                        );
                    }
                    edge.getProperties().put(MapConstants.POINTS, points);

                    addEdge(
                        (BusinessObjectLight) edge.getIdentifier(), 
                        (BusinessObjectLight) source.getIdentifier(), 
                        (BusinessObjectLight) target.getIdentifier(), 
                        edge.getProperties()
                    );
                });
            });
        }
        else
            mapGraph.endUpdate();
    }
            
    private void setDrawingMarkerMode(BusinessObjectLight businessObject) {
        if (mapProvider != null)
            mapProvider.setDrawingMarkerMode(coordinate -> {
                List<GeoCoordinate> coordinates = new ArrayList();
                coordinates.add(mapProvider.getBounds().getNortheast());
                coordinates.add(mapProvider.getBounds().getSouthwest());
                coordinates.add(coordinate);
                mapOverlay.getProjectionFromLatLngToDivPixel(coordinates, pixelCoordinates -> {
                    GeoPoint ne = pixelCoordinates.get(0);
                    GeoPoint sw = pixelCoordinates.get(1);
                    GeoPoint pixelCoordinate = pixelCoordinates.get(2);

                    BusinessObjectViewNode newViewNode = new BusinessObjectViewNode(businessObject);
                    newViewNode.getProperties().put(MapConstants.ATTR_LAT, coordinate.getLatitude());
                    newViewNode.getProperties().put(MapConstants.ATTR_LON, coordinate.getLongitude());

                    newViewNode.getProperties().put(MapConstants.X, pixelCoordinate.getX() - sw.getX());
                    newViewNode.getProperties().put(MapConstants.Y, pixelCoordinate.getY() - ne.getY());
                    
                    newViewNode.getProperties().put(MapConstants.FROM_CLIENT_ADD_NODE, true);

                    viewMap.addNode(newViewNode);
                    mapGraph.beginUpdate();                    
                    addNode(businessObject, newViewNode.getProperties());
                });
            });
    }
    
    private void setDrawingPolylineMode() {
        if (mapProvider != null) {
            polylineDrawHelper = new HelperEdgeDraw(mapProvider, mapGraph, helper-> {
                BusinessObjectLight source = helper.getSource().getBusinessObject();
                BusinessObjectLight target = helper.getTarget().getBusinessObject();
                List<GeoCoordinate> controlPoints = new ArrayList(helper.getCoordintates());
                
                WindowNewContainer dialogNewContainer = new WindowNewContainer(
                    source, target, ts, aem, bem, mem, physicalConnectionsService, 
                    container -> {
                        if (controlPoints.size() >= 2) {
                            controlPoints.remove(controlPoints.size() - 1);
                            controlPoints.remove(0);
                        }
                        if (!controlPoints.isEmpty()) {
                            List<GeoCoordinate> coordinates = new ArrayList();
                            coordinates.add(mapProvider.getBounds().getNortheast());
                            coordinates.add(mapProvider.getBounds().getSouthwest());
                            coordinates.addAll(controlPoints);
                            
                            mapOverlay.getProjectionFromLatLngToDivPixel(coordinates, pixelCoordinates -> {
                                GeoPoint sw = pixelCoordinates.remove(1);
                                GeoPoint ne = pixelCoordinates.remove(0);
                                List<Point> points = new ArrayList();
                                pixelCoordinates.forEach(point -> 
                                    points.add(new Point(point.getX() - sw.getX(), point.getY() - ne.getY()))                                        
                                );
                                BusinessObjectViewEdge viewEdge = new BusinessObjectViewEdge(container);
                                viewEdge.getProperties().put(MapConstants.PROPERTY_CONTROL_POINTS, controlPoints);
                                viewEdge.getProperties().put(MapConstants.POINTS, points);
                                viewMap.addEdge(viewEdge);
                                viewMap.attachSourceNode(viewEdge, viewMap.findNode(source));
                                viewMap.attachTargetNode(viewEdge, viewMap.findNode(target));
                                mapGraph.beginUpdate();
                                addEdge(container, source, target, viewEdge.getProperties());
                            });
                        } else {
                            BusinessObjectViewEdge viewEdge = new BusinessObjectViewEdge(container);
                            viewEdge.getProperties().put(MapConstants.PROPERTY_CONTROL_POINTS, controlPoints);
                            viewEdge.getProperties().put(MapConstants.POINTS, Collections.EMPTY_LIST);
                            viewMap.addEdge(viewEdge);
                            viewMap.attachSourceNode(viewEdge, viewMap.findNode(source));
                            viewMap.attachTargetNode(viewEdge, viewMap.findNode(target));
                            mapGraph.beginUpdate();
                            addEdge(container, source, target, viewEdge.getProperties());
                        }
                    }
                );
                dialogNewContainer.open();
                setDrawingPolylineMode();
            });
            polylineDrawHelper.start();
        }
    }
    
    public void newOspView(boolean init) {
        if (!init) {
            buildEmptyView();
            try {
                getAsComponent();
            } catch (InvalidArgumentException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
        }
        disableEnableTabs(
            null,
            Arrays.asList(tools.get(Tool.SaveView), tools.get(Tool.DeleteView), tools.get(Tool.Hand), tools.get(Tool.Wire), tools.get(Tool.Marker), tools.get(Tool.Polyline))
        );
        componentTabs.setSelectedTab(tools.get(Tool.Hand));
    }
    
    @Override
    public Component getAsComponent() throws InvalidArgumentException {
        if (mapProvider == null) {
            String generalMapsProvider = null;
            try {
                generalMapsProvider = (String) aem.getConfigurationVariableValue("general.maps.provider");
                Class mapClass = Class.forName(generalMapsProvider);
                if (MapProvider.class.isAssignableFrom(mapClass)) {
                    mapProvider = (MapProvider) mapClass.getDeclaredConstructor().newInstance();
                    mapProvider.createComponent(aem, ts);
                    if (mapProvider.getComponent() != null) {
                        if (viewMap.getProperties().containsKey(PropertyNames.CENTER))
                            mapProvider.setCenter((GeoCoordinate) viewMap.getProperties().get(PropertyNames.CENTER));
                        if (viewMap.getProperties().containsKey(PropertyNames.ZOOM))
                            mapProvider.setZoom(Double.valueOf(String.valueOf(viewMap.getProperties().get(PropertyNames.ZOOM))));
                        
                        if (component == null) {
                            component = new Div();
                            component.setClassName("ospman-div");
                        }
                        if (viewTools) {
                            componentTabs = new Tabs();
                            componentTabs.addClassName("ospman-tabs");
                            
                            Icon iconNewOspView = new Icon(VaadinIcon.FILE_ADD);
                            
                            Tab tabNewOspView = new Tab(iconNewOspView);
                            tabNewOspView.setClassName("ospman-tab");
                            tabNewOspView.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.osp-view.new"));
                            
                            Tab tabOpenOspView = new Tab(new Icon(VaadinIcon.FILE_SEARCH));
                            tabOpenOspView.setClassName("ospman-tab");
                            tabOpenOspView.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.osp-view.open"));

                            Tab tabSaveOspView = new Tab(new Icon(VaadinIcon.DOWNLOAD));
                            tabSaveOspView.setClassName("ospman-tab");
                            tabSaveOspView.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.osp-view.save"));

                            Tab tabDeleteOspView = new Tab(new Icon(VaadinIcon.FILE_REMOVE));
                            tabDeleteOspView.setClassName("ospman-tab");
                            tabDeleteOspView.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.osp-view.delete"));

                            Tab tabHand = new Tab(new Icon(VaadinIcon.HAND));
                            tabHand.setClassName("ospman-tab");
                            tabHand.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.hand"));
                            
                            Tab tabMarker = new Tab(new Icon(VaadinIcon.MAP_MARKER));
                            tabMarker.setClassName("ospman-tab");
                            tabMarker.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.marker"));

                            Tab tabPolyline = new Tab(new Icon(VaadinIcon.PLUG));
                            tabPolyline.setClassName("ospman-tab");
                            tabPolyline.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.polyline"));

                            Tab tabWire = new Tab(new Icon(VaadinIcon.DOT_CIRCLE));
                            tabWire.setClassName("ospman-tab");
                            tabWire.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.wire"));

                            disableEnableTabs(
                                Arrays.asList(tabSaveOspView, tabDeleteOspView, tabHand, tabMarker, tabPolyline, tabWire),
                                Arrays.asList()
                            );
                            componentTabs.add(
                                tabNewOspView, 
                                tabOpenOspView, 
                                tabSaveOspView, 
                                tabDeleteOspView, 
                                tabHand, 
                                tabMarker, 
                                tabPolyline,
                                tabWire
                            );                            
                            tabs.put(tabHand, Tool.Hand);
                            tabs.put(tabMarker, Tool.Marker);
                            tabs.put(tabPolyline, Tool.Polyline);
                            tabs.put(tabWire, Tool.Wire);
                            tabs.put(tabNewOspView, Tool.NewView);
                            tabs.put(tabOpenOspView, Tool.OpenView);
                            tabs.put(tabSaveOspView, Tool.SaveView);
                            tabs.put(tabDeleteOspView, Tool.DeleteView);

                            tools.put(Tool.Hand, tabHand);
                            tools.put(Tool.Marker, tabMarker);
                            tools.put(Tool.Polyline, tabPolyline);
                            tools.put(Tool.Wire, tabWire);
                            tools.put(Tool.NewView, tabNewOspView);
                            tools.put(Tool.OpenView, tabOpenOspView);
                            tools.put(Tool.SaveView, tabSaveOspView);
                            tools.put(Tool.DeleteView, tabDeleteOspView);

                            componentTabs.addSelectedChangeListener(selectedChangeEvent -> {
                                if (polylineDrawHelper != null)
                                    polylineDrawHelper.cancel();
                                if (wiresHelper != null)
                                    wiresHelper.cancel();

                                selectedTab = selectedChangeEvent.getSelectedTab();
                                if (selectedTab != null) {
                                    if (selectedTab.equals(tabNewOspView))
                                        newOspView(false);
                                    else if (selectedTab.equals(tabOpenOspView)) {
                                        componentTabs.setSelectedTab(tools.get(Tool.Hand));
                                        DialogOspViews ospViewDialog = new DialogOspViews(tabOpenOspView, aem, ts, viewObject -> {
                                            buildEmptyView();
                                            getProperties().put(Constants.PROPERTY_ID, viewObject.getId());
                                            getProperties().put(Constants.PROPERTY_NAME, viewObject.getName());
                                            getProperties().put(Constants.PROPERTY_DESCRIPTION, viewObject.getDescription());
                                            buildWithSavedView(viewObject.getStructure());
                                            disableEnableTabs(null, Arrays.asList(
                                                tools.get(Tool.SaveView), tools.get(Tool.DeleteView), tools.get(Tool.Hand), tools.get(Tool.Marker), tools.get(Tool.Polyline), tools.get(Tool.Wire)
                                            ));
                                        });
                                        componentTabs.add(ospViewDialog);
                                        ospViewDialog.open();
                                    } else if (selectedTab.equals(tabSaveOspView)) {
                                        componentTabs.setSelectedTab(tabHand);
                                        if (viewMap.getNodes().isEmpty()) {
                                            new SimpleNotification(
                                                ts.getTranslatedString("module.general.messages.information"), 
                                                ts.getTranslatedString("module.ospman.empty-view"), 
                                                AbstractNotification.NotificationType.INFO, ts
                                            ).open();
                                        }
                                        else
                                            saveOspView();
                                    } else if (selectedTab.equals(tabDeleteOspView))
                                        deleteOspView();
                                    else if (selectedTab.equals(tabHand))
                                        mapProvider.setHandMode();
                                    else if (selectedTab.equals(tabMarker)) {
                                        componentTabs.setSelectedTab(tabHand);
                                        DialogMarker markerDialog = new DialogMarker(
                                            tabMarker, aem, bem, mem, ts, viewMap.getNodes(), 
                                            businessObject -> setDrawingMarkerMode(businessObject)
                                        );
                                        componentTabs.add(markerDialog);
                                        markerDialog.open();
                                    }
                                    else if (selectedTab.equals(tabPolyline))
                                        setDrawingPolylineMode();
                                    else if (selectedTab.equals(tabWire)) {
                                        wiresHelper = new HelperContainerSelector(mapGraph);
                                        wiresHelper.start();
                                    }
                                }
                            });
                            component.add(componentTabs);
                            newOspView(true);
                        }
                        mapProvider.addBoundsChangedEventListener(event -> hideMapGraph(true));
                        mapProvider.addIdleEventListener(event -> {
                            if (mapOverlay == null)
                                newOverlay();
                            else
                                redrawGraph();
                        });
                        component.add(mapProvider.getComponent());
                    }
                } else 
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        String.format(ts.getTranslatedString("module.ospman.not-valid-map-provider"), mapClass.getCanonicalName()), 
                        AbstractNotification.NotificationType.ERROR, ts  
                    ).open();
                
            } catch (ClassNotFoundException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    String.format(ts.getTranslatedString("module.ospman.not-valid-map-provider"), generalMapsProvider), 
                    AbstractNotification.NotificationType.ERROR, ts
                ).open();
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, ts
                ).open();
            } catch (IllegalAccessException | IllegalArgumentException | 
                InstantiationException | NoSuchMethodException | 
                SecurityException | InvocationTargetException  ex) {
                
                Logger.getLogger(OutsidePlantView.class.toString()).log(Level.SEVERE, ex.getLocalizedMessage());
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ts.getTranslatedString("module.general.messages.unexpected-error"), 
                    AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
        }
        else
            return component;
        return component;
    }
    
    /**
     * <pre>{@code
     * <view version="">
     *  <class>OSPView</class>
     *  <center lon="" lat=""></center>
     *  <zoom>0</zoom>
     *  <nodes>
     *   <node lon="" lat="" class="businessObjectClass" overlayid="">businessObjectId</node>
     *  </nodes>
     *  <edge>
     *   <edge id="" class="" asideid="" asideclass="" bsideid="" bsideclass="" overlayid="">
     *    <controlpoint lon="" lat=""></controlpoint>
     *   </edge>
     *  </edge>
     * </view>
     * }</pre>
     */
    @Override
    public void buildWithSavedView(byte[] view) {
        try {
            QName tagView = new QName(TAG_VIEW);
            QName tagCenter = new QName(TAG_CENTER);
            QName tagZoom = new QName(TAG_ZOOM);
            QName tagNode = new QName(TAG_NODE);
            QName tagEdge = new QName(TAG_EDGE);
            QName tagControlPoint = new QName(TAG_CONTROL_POINT);
            QName tmpTagControlPoint = new QName("controlPoint");
            boolean tmpBadNames = false;
                                    
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(view);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
            while (reader.hasNext()) {
                reader.next();
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (tagView.equals(reader.getName())) {
                        String version = reader.getAttributeValue(null, ATTR_VERSION);
                        if ("2.1".equals(version)) //TODO: remove temporals and lng, 2.1 hard code references
                            tmpBadNames = true;
                    }
                    if (tagCenter.equals(reader.getName())) {
                        double lat = Double.valueOf(reader.getAttributeValue(null, MapConstants.ATTR_LAT));
                        double lon = Double.valueOf(reader.getAttributeValue(null, tmpBadNames ? "lng" : MapConstants.ATTR_LON));
                        GeoCoordinate mapCenter = new GeoCoordinate(lat, lon);
                        viewMap.getProperties().put(PropertyNames.CENTER, mapCenter);
                    } else if (tagZoom.equals(reader.getName())) {
                        double zoom = Double.valueOf(reader.getElementText());
                        viewMap.getProperties().put(PropertyNames.ZOOM, zoom);
                    }
                    else if (tagNode.equals(reader.getName())) {
                        try {
                            String objectClass = reader.getAttributeValue(null, ATTR_CLASS);
                            double lat = Double.valueOf(reader.getAttributeValue(null, MapConstants.ATTR_LAT));
                            double lon = Double.valueOf(reader.getAttributeValue(null, tmpBadNames ? "lng" : MapConstants.ATTR_LON));                            
                            String objectId = reader.getElementText();
                            
                            BusinessObjectViewNode viewNode = new BusinessObjectViewNode(bem.getObjectLight(objectClass, objectId));
                            viewNode.getProperties().put(MapConstants.ATTR_LAT, lat);
                            viewNode.getProperties().put(MapConstants.ATTR_LON, lon);
                            getAsViewMap().addNode(viewNode);
                        } catch (InventoryException ex) {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                ex.getLocalizedMessage(), 
                                AbstractNotification.NotificationType.ERROR, ts
                            ).open();
                        }
                    } else if (tagEdge.equals(reader.getName())) {
                        try {
                            String objectId = reader.getAttributeValue(null, ATTR_ID);
                            String objectClass = reader.getAttributeValue(null, ATTR_CLASS);
                            String aSideId = reader.getAttributeValue(null, ATTR_A_SIDE_ID);
                            String aSideClass = reader.getAttributeValue(null, ATTR_A_SIDE_CLASS);
                            String bSideId = reader.getAttributeValue(null, ATTR_B_SIDE_ID);
                            String bSideClass = reader.getAttributeValue(null, ATTR_B_SIDE_CLASS);
                            
                            List<GeoCoordinate> controlPoints = new ArrayList();
                            while (true) {
                                reader.nextTag();
                                if (tmpBadNames ? tmpTagControlPoint.equals(reader.getName()) : tagControlPoint.equals(reader.getName())) {
                                    if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                        controlPoints.add(new GeoCoordinate(
                                            Double.valueOf(reader.getAttributeValue(null, MapConstants.ATTR_LAT)), 
                                            Double.valueOf(reader.getAttributeValue(null, tmpBadNames ? "lng" : MapConstants.ATTR_LON))
                                        ));
                                    }
                                }
                                else
                                    break;
                            }
                            BusinessObjectViewEdge viewEdge = new BusinessObjectViewEdge(bem.getObjectLight(objectClass, objectId));   
                            viewEdge.getProperties().put(MapConstants.PROPERTY_CONTROL_POINTS, controlPoints);
                            viewMap.addEdge(viewEdge);
                            viewMap.attachSourceNode(viewEdge, viewMap.findNode(bem.getObjectLight(aSideClass, aSideId)));
                            viewMap.attachTargetNode(viewEdge, viewMap.findNode(bem.getObjectLight(bSideClass, bSideId)));
                        } catch (InventoryException ex) {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                ex.getLocalizedMessage(), 
                                AbstractNotification.NotificationType.ERROR, ts
                            ).open();
                        }
                    }
                }
            }
            reader.close();
            getAsComponent();
        } catch (XMLStreamException ex) {
            Logger.getLogger(OutsidePlantView.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ts.getTranslatedString("module.general.messages.unexpected-error"), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ts.getTranslatedString("module.general.messages.unexpected-error"), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
    }

    @Override
    public void buildWithBusinessObject(BusinessObjectLight businessObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void buildEmptyView() {
        if (this.viewMap == null)
            this.viewMap = new ViewMap();
        else
            this.viewMap.clear();
        
        if (mapOverlay != null)
            mapOverlay.removeAllWidthChangedEventListener();
        if (mapProvider != null) {
            mapProvider.removeAllBoundsChangedEventListener();
            mapProvider.removeAllIdleEventListener();
        }
        mapGraph = null;
        mapOverlay = null;
        mapProvider = null;
        
        polylineDrawHelper = null;
        wiresHelper = null;
        tabs.clear();
        tools.clear();
        componentTabs = null;
        selectedTab = null;
        
        if (component != null)
            component.removeAll();
        
        this.getProperties().put(Constants.PROPERTY_ID, -1);
        this.getProperties().put(Constants.PROPERTY_NAME, "");
        this.getProperties().put(Constants.PROPERTY_DESCRIPTION, "");
        
        Double mapCenterLatitude = OutsidePlantService.DEFAULT_CENTER_LATITUDE;
        Double mapCenterLongitude = OutsidePlantService.DEFAULT_CENTER_LONGITUDE;
        
        try {
            mapCenterLatitude = (double) aem.getConfigurationVariableValue("widgets.simplemap.centerLatitude"); //NOI18N
            mapCenterLongitude = (double) aem.getConfigurationVariableValue("widgets.simplemap.centerLongitude"); //NOI18N
        } catch(ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            //Nothing to do
        }
        this.viewMap.getProperties().put(PropertyNames.CENTER, new GeoCoordinate(mapCenterLatitude, mapCenterLongitude));
        try {
            this.viewMap.getProperties().put(PropertyNames.ZOOM, aem.getConfigurationVariableValue("widgets.simplemap.zoom")); //NOI18N
        } catch(ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            this.viewMap.getProperties().put(PropertyNames.ZOOM, OutsidePlantService.DEFAULT_ZOOM); //NOI18N
        }
    }

    @Override
    public AbstractViewNode addNode(BusinessObjectLight businessObject, Properties properties) {
        MapNode mapNode = mapGraph.findNode(businessObject);
        if (mapNode == null) {
            BusinessObjectViewNode viewNode = (BusinessObjectViewNode) viewMap.findNode(businessObject.getId());
            MapNode newMapNode = new MapNode(viewNode, 
                (double) properties.get(MapConstants.X),
                (double) properties.get(MapConstants.Y),
                mapProvider, mapOverlay, resourceFactory);
            newMapNode.addRightClickCellListener(event -> {
                if (viewTools)
                    openWindowNode(viewNode);
            });
            newMapNode.addCellAddedListener(event -> {
                event.unregisterListener();
                if (properties.containsKey(MapConstants.FROM_CLIENT_ADD_NODE) && 
                    (boolean) properties.remove(MapConstants.FROM_CLIENT_ADD_NODE)) {
                        mapGraph.endUpdate();
                        return;
                }
                if (viewMap.getEdges().isEmpty() && viewNode.equals(viewMap.getNodes().get(viewMap.getNodes().size() - 1)))
                    mapGraph.endUpdate();
            });
            mapGraph.addNode(newMapNode);
        }
        return this.viewMap.findNode(businessObject.getId());
    }
    
    @Override
    public AbstractViewEdge addEdge(BusinessObjectLight businessObject, BusinessObjectLight sourceBusinessObject, BusinessObjectLight targetBusinessObject, Properties properties) {
        MapEdge mapEdge = mapGraph.findEdge(businessObject);
        if (mapEdge == null) {
            AbstractViewNode sourceNode = this.viewMap.findNode(sourceBusinessObject.getId());
            if (sourceNode == null)
                return null;
            AbstractViewNode targetNode = this.viewMap.findNode(targetBusinessObject.getId());
            if (targetNode == null)
                return null;
            BusinessObjectViewEdge viewEdge = (BusinessObjectViewEdge) viewMap.findEdge(businessObject);
            MapEdge newMapEdge = new MapEdge(
                viewEdge, 
                sourceBusinessObject, targetBusinessObject, 
                (List) properties.get(MapConstants.POINTS), 
                mem, ts, mapProvider, mapOverlay, mapGraph
            );
            newMapEdge.addRightClickCellListener(event -> {
                if (viewTools)
                    openWindowEdge((BusinessObjectViewEdge) viewMap.findEdge(businessObject));
            });
            newMapEdge.addCellAddedListener(event -> {
                event.unregisterListener();
                if (viewEdge.equals(viewMap.getEdges().get(viewMap.getEdges().size() - 1)))
                    mapGraph.endUpdate();
            });
            mapGraph.addEdge(newMapEdge);
        }
        return this.viewMap.findEdge(businessObject.getId());
    }

    @Override
    public void removeNode(BusinessObjectLight businessObject) {
        AbstractViewNode node = viewMap.getNode(businessObject);
        if (node instanceof BusinessObjectViewNode) {
            BusinessObjectViewNode objectNode = (BusinessObjectViewNode) node;
            if (mapGraph != null) {
                MapNode mapNode = mapGraph.findNode(businessObject);
                if (mapNode != null) {
                    List<BusinessObjectLight> edgesToRemove = new ArrayList();
                    mapGraph.getEdges().forEach(edge -> {
                        if (edge instanceof MapEdge && (
                            businessObject.getId().equals(edge.getSource()) || 
                            businessObject.getId().equals(edge.getTarget())
                           )) {
                            edgesToRemove.add(((MapEdge) edge).getBusinessObject());
                        }
                    });
                    edgesToRemove.forEach(edge -> removeEdge(edge));
                    mapGraph.removeNode(mapNode);
                    viewMap.getNodes().remove(objectNode);
                }
            }
        }
    }

    @Override
    public void removeEdge(BusinessObjectLight businessObject) {
        AbstractViewEdge edge = viewMap.getEdge(businessObject);
        if (edge instanceof BusinessObjectViewEdge) {
            BusinessObjectViewEdge objectEdge = (BusinessObjectViewEdge) edge;
            if (mapGraph != null) {
                MapEdge mapEdge = mapGraph.findEdge(businessObject);
                if (mapEdge != null) {
                    mapGraph.removeEdge(mapEdge);
                    viewMap.getEdges().remove(objectEdge);
                }
            }
        }
    }

    @Override
    public void addNodeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addEdgeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
    private void openWindowNode(BusinessObjectViewNode viewNode) {
        if (viewNode != null) {
            WindowNode wdwNode = new WindowNode(viewNode, aem, bem, mem, ts, 
                physicalConnectionsService, newBusinessObjectVisualAction,
                () -> {
                    new ConfirmDialog(ts, 
                        new Label(String.format(ts.getTranslatedString("module.ospman.view-node.tool.remove.confirm"), viewNode.getIdentifier().getName())), 
                        ts.getTranslatedString("module.general.messages.ok"), 
                        () -> removeNode(viewNode.getIdentifier())
                    ).open();
                }
            );
            wdwNode.open();
        }
    }
    
    private void openWindowEdge(BusinessObjectViewEdge viewEdge) {
        if (viewEdge != null) {
            if (selectedTab != null && tabs.containsKey(selectedTab) && 
                Tool.Wire.equals(tabs.get(selectedTab))) {
                List<BusinessObjectViewEdge> edges = new ArrayList();
                wiresHelper.getEdges().forEach(cell -> {
                    if (cell instanceof MapEdge) {
                        MapEdge businessObjectEdge = (MapEdge) cell;
                        AbstractViewEdge edge = viewMap.findEdge(businessObjectEdge.getBusinessObject());
                        if (edge instanceof BusinessObjectViewEdge)
                            edges.add((BusinessObjectViewEdge) edge);
                    }
                });
                WindowContainers wdwContainer = new WindowContainers(edges, aem, bem, mem, ts);
                wiresHelper.cancel();
                wiresHelper.start();                
                wdwContainer.open();
                return;
            }
            WindowEdge wdwEdge = new WindowEdge(viewEdge, ts, 
                () -> {
                    new ConfirmDialog(ts, 
                        new Label(String.format(ts.getTranslatedString("module.ospman.view-edge.tool.remove.confirm"), viewEdge.getIdentifier().getName())), 
                        ts.getTranslatedString("module.general.messages.ok"), 
                        () -> removeEdge(viewEdge.getIdentifier())
                    ).open();
                }
            );
            wdwEdge.open();
        }
    }
    
    private void saveOspView() {
        FormLayout fly = new FormLayout();
        TextField txtName = new TextField();
        txtName.setRequiredIndicatorVisible(true);
        txtName.setValue(this.getProperties().getProperty(Constants.PROPERTY_NAME) == null ? 
            "" : this.getProperties().getProperty(Constants.PROPERTY_NAME));
        TextField txtDescription = new TextField();
        txtDescription.setValue(this.getProperties().getProperty(Constants.PROPERTY_DESCRIPTION) == null ? 
            "" : this.getProperties().getProperty(Constants.PROPERTY_DESCRIPTION));
        fly.addFormItem(txtName, ts.getTranslatedString("module.general.labels.name"));
        fly.addFormItem(txtDescription, ts.getTranslatedString("module.general.labels.description"));

        ConfirmDialog confirmDialog = new ConfirmDialog(ts, 
            ts.getTranslatedString("module.ospman.save-view"), fly, 
            ts.getTranslatedString("module.general.messages.ok"), () -> {
                try {
                    if (this.properties.get(Constants.PROPERTY_ID).equals(-1)) {
                        long newOSPViewId = aem.createOSPView(txtName.getValue(), txtDescription.getValue(), getAsXml());
                        this.getProperties().put(Constants.PROPERTY_ID, newOSPViewId);
                    } else {
                        aem.updateOSPView((long) this.getProperties().get(Constants.PROPERTY_ID), 
                            txtName.getValue(), txtDescription.getValue(), getAsXml());
                    }
                    this.getProperties().put(Constants.PROPERTY_NAME, txtName.getValue());
                    this.getProperties().put(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue());
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.success"), 
                        ts.getTranslatedString("module.ospman.view-saved"), 
                        AbstractNotification.NotificationType.INFO, ts
                    ).open();
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }
            }
        );
        confirmDialog.open();
    }
    
    private void deleteOspView() {
        if (this.getProperties().get(Constants.PROPERTY_ID) instanceof Integer && (int) this.getProperties().get(Constants.PROPERTY_ID) == -1)
            return;
        WindowDeleteOspView confirmDialog = new WindowDeleteOspView((long) this.getProperties().get(Constants.PROPERTY_ID), ts, aem, this);
        confirmDialog.open();
    }
}
