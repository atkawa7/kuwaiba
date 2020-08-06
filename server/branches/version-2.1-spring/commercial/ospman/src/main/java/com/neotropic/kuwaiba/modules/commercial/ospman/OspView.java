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
package com.neotropic.kuwaiba.modules.commercial.ospman;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphCell;
import com.neotropic.flow.component.mxgraph.Point;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.DialogNewContainer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.server.StreamResourceRegistry;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractView;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewEdge;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewEventListener;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewMap;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.kuwaiba.modules.optional.physcon.persistence.PhysicalConnectionsService;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.views.util.UtilHtml;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OspView extends AbstractView<BusinessObjectLight, Component> {
    private class PropertyNames {
        public static final String LAT = "lat"; //NOI18N
        public static final String LON = "lon"; //NOI18N
        public static final String CONTROL_POINTS = "controlPoints"; //NOI18N
        public static final String COLOR = "color"; //NOI18N
        public static final String OVERLAY_ID = "overlayId"; //NOI18N
        public static final String CENTER = "center"; //NOI18N
        public static final String ZOOM = "zoom"; //NOI18N
    }
    /**
     * Map in the Outside Plant View
     */
    private Map map;
    /**
     * Reference to the translation service.
     */
    private final TranslationService ts;
    /**
     * Reference to the Application Entity Manager
     */
    private final ApplicationEntityManager aem;
    
    private final BusinessEntityManager bem;
    
    private final MetadataEntityManager mem;
    
    private final ResourceFactory resourceFactory;
    
    private final PhysicalConnectionsService physicalConnectionsService;
    
    private final List<MapOverlay> overlays;
    
    private final HashMap<MapOverlay, MxGraph> mapOverlays;
    private final HashMap<MxGraph, Boolean> graphLoaded;
    private Div component;
    private MapOverlay selectedOverlay;
    
    private HashMap<BusinessObjectViewNode, MxGraphCell> mapNodeVertex = new HashMap();
    private HashMap<MxGraphCell, BusinessObjectViewNode> mapVertexNode = new HashMap();
    private HashMap<BusinessObjectViewEdge, MxGraphCell> mapEdgeVertex = new HashMap();
    private HashMap<MxGraphCell, BusinessObjectViewEdge> mapVertexEdge = new HashMap();
    
    private PolylineDrawHelper polylineDrawHelper;
    private WiresHelper wiresHelper;
    
    enum Tool {
        Hand,
        Overlay,
        Marker,
        Polyline,
        Wire
    }
    private HashMap<Tab, Tool> tabs = new HashMap();
    private HashMap<Tool, Tab> tools = new HashMap();
    private Tab selectedTab;
    
    public OspView(
        ApplicationEntityManager aem, 
        BusinessEntityManager bem, 
        MetadataEntityManager mem, 
        TranslationService ts, 
        ResourceFactory resourceFactory,
        PhysicalConnectionsService physicalConnectionsService) {
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        this.physicalConnectionsService = physicalConnectionsService;
        this.overlays = new ArrayList();
        this.mapOverlays = new LinkedHashMap();
        this.graphLoaded = new HashMap();
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
     * <view version="">
     *  <class>OSPView</class>
     *  <center lon="" lat=""></center>
     *  <zoom>0</zoom>
     *  <overlays>
     *   <overlay id="" title="" scale="" enabled="" selected="">
     *    <coordinate lat="" lng=""></coordinate>
     *    <coordinate lat="" lng=""></coordinate>
     *   <overlay/>
     *  </overlays>
     *  <nodes>
     *   <node lon="" lat="" class="businessObjectClass" overlayid="">businessObjectId</node>
     *  </nodes>
     *  <edge>
     *   <edge id="" class="" asideid="" asideclass="" bsideid="" bsideclass="" overlayid="">
     *    <controlpoint lon="" lat=""></controlpoint>
     *   </edge>
     *  </edge>
     * </view>
     */    
    @Override
    public byte[] getAsXml() {
        final String TAG_VIEW = "view"; //NOI18N
        final String TAG_CLASS = "class"; //NOI18N
        final String TAG_CENTER = "center"; //NOI18N
        final String TAG_ZOOM = "zoom"; //NOI18N
        final String TAG_OVERLAYS = "overlays"; //NOI18N
        final String TAG_OVERLAY = "overlay"; //NOI18N
        final String TAG_NODES = "nodes"; //NOI18N
        final String TAG_NODE = "node"; //NOI18N
        final String TAG_EDGES = "edges"; //NOI18N
        final String TAG_EDGE = "edge"; //NOI18N
        final String TAG_COORDINATE = "coordinate"; //NOI18N
        final String TAG_CONTROL_POINT = "controlPoint"; //NOI18N
        
        final String ATTR_LAT = "lat"; //NOI18N
        final String ATTR_LON = "lng"; //NOI18N
        final String ATTR_ID = "id"; //NOI18N
        final String ATTR_CLASS = "class"; //NOI18N
        final String ATTR_TITLE = "title"; //NOI18N
        final String ATTR_ENABLED = "enabled"; //NOI18N
        final String ATTR_SELECTED = "selected"; //NOI18N
        final String ATTR_OVERLAY_ID = "overlayid"; //NOI18N
        final String ATTR_A_SIDE_ID = "asideid"; //NOI18N
        final String ATTR_A_SIDE_CLASS = "asideclass"; //NOI18N
        final String ATTR_B_SIDE_ID = "bsideid"; //NOI18N
        final String ATTR_B_SIDE_CLASS = "bsideclass"; //NOI18N
        final String ATTR_WIDTH = "width"; //NOI18N
        final String ATTR_VERSION = "version"; ///NOI18N
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            final QName tagView = new QName(TAG_VIEW);
            final QName tagClass = new QName(TAG_CLASS);
            final QName tagCenter = new QName(TAG_CENTER);
            final QName tagZoom = new QName(TAG_ZOOM);
            final QName tagOverlays = new QName(TAG_OVERLAYS);
            final QName tagOverlay = new QName(TAG_OVERLAY);
            final QName tagNodes = new QName(TAG_NODES);
            final QName tagNode = new QName(TAG_NODE);
            final QName tagEdges = new QName(TAG_EDGES);
            final QName tagEdge = new QName(TAG_EDGE);
            final QName tagCoordinate = new QName(TAG_COORDINATE);
            final QName tagControlpoint = new QName(TAG_CONTROL_POINT);
            
            final QName attrLon = new QName(ATTR_LON);
            final QName attrLat = new QName(ATTR_LAT);
            final QName attrId = new QName(ATTR_ID);
            final QName attrTitle = new QName(ATTR_TITLE);
            final QName attrEnabled = new QName(ATTR_ENABLED);
            final QName attrSelected = new QName(ATTR_SELECTED);
            final QName attrWidth = new QName(ATTR_WIDTH);
            final QName attrClass = new QName(ATTR_CLASS);
            final QName attrOverlayId = new QName(ATTR_OVERLAY_ID);
            final QName attrAsideId = new QName(ATTR_A_SIDE_ID);
            final QName attrAsideClass = new QName(ATTR_A_SIDE_CLASS);
            final QName attrBsideId = new QName(ATTR_B_SIDE_ID);
            final QName attrBsideClass = new QName(ATTR_B_SIDE_CLASS);
            final QName attrVersion = new QName(ATTR_VERSION);
            
            xmlew.add(xmlef.createStartElement(tagView, null, null));
            xmlew.add(xmlef.createAttribute(attrVersion, OutsidePlantConstants.VIEW_VERSION));
            
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
            
            xmlew.add(xmlef.createStartElement(tagOverlays, null, null));
            for (MapOverlay overlay : overlays) {
                xmlew.add(xmlef.createStartElement(tagOverlay, null, null));
                xmlew.add(xmlef.createAttribute(attrId, overlay.getId()));
                xmlew.add(xmlef.createAttribute(attrTitle, overlay.getTitle()));
                xmlew.add(xmlef.createAttribute(attrWidth, String.valueOf(overlay.getWidth())));
                if (overlay.getEnabled())
                    xmlew.add(xmlef.createAttribute(attrEnabled, String.valueOf(true)));
                if (selectedOverlay != null && overlay.getId().equals(selectedOverlay.getId()))
                    xmlew.add(xmlef.createAttribute(attrSelected, String.valueOf(true)));
                
                xmlew.add(xmlef.createStartElement(tagCoordinate, null, null));
                xmlew.add(xmlef.createAttribute(attrLat, String.valueOf(overlay.getBounds().getSouthwest().getLatitude())));
                xmlew.add(xmlef.createAttribute(attrLon, String.valueOf(overlay.getBounds().getSouthwest().getLongitude())));
                xmlew.add(xmlef.createEndElement(tagCoordinate, null));
                
                xmlew.add(xmlef.createStartElement(tagCoordinate, null, null));
                xmlew.add(xmlef.createAttribute(attrLat, String.valueOf(overlay.getBounds().getNortheast().getLatitude())));
                xmlew.add(xmlef.createAttribute(attrLon, String.valueOf(overlay.getBounds().getNortheast().getLongitude())));
                xmlew.add(xmlef.createEndElement(tagCoordinate, null));
                
                xmlew.add(xmlef.createEndElement(tagOverlay, null));
            }
            xmlew.add(xmlef.createEndElement(tagOverlays, null));
            
            xmlew.add(xmlef.createStartElement(tagNodes, null, null));
            for (AbstractViewNode node : viewMap.getNodes()) {
                xmlew.add(xmlef.createStartElement(tagNode, null, null));
                xmlew.add(xmlef.createAttribute(attrLat, String.valueOf(node.getProperties().get(PropertyNames.LAT))));
                xmlew.add(xmlef.createAttribute(attrLon, String.valueOf(node.getProperties().get(PropertyNames.LON))));
                xmlew.add(xmlef.createAttribute(attrOverlayId, String.valueOf(node.getProperties().get(PropertyNames.OVERLAY_ID))));
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
                xmlew.add(xmlef.createAttribute(attrOverlayId, String.valueOf(edge.getProperties().get(PropertyNames.OVERLAY_ID))));
                
                for (GeoCoordinate controlPoint : (List<GeoCoordinate>) edge.getProperties().get(PropertyNames.CONTROL_POINTS)) {
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
        for (Tab tab : disableTabs)
            tab.setEnabled(false);
        for (Tab tab : enableTabs)
            tab.setEnabled(true);
    }
    
    private void addOverlay(GeoBounds bounds) {
        MapOverlay newOverlay = map.createOverlay(bounds);
        newOverlay.setEnabled(true);
        
        MxGraph newGraph = new MxGraph();
        newGraph.setFullSize();
        newGraph.setOverflow(null);
        
        newOverlay.getComponent().add(newGraph);
        
        overlays.add(newOverlay);
        mapOverlays.put(newOverlay, newGraph);
        graphLoaded.put(newGraph, false);

        Consumer<Double> setGraphScaleConsumer = width -> {
            newGraph.getElement().executeJs("this.graph.view.setScale($0 / $1)", width, newOverlay.getWidth()); //NOI18N
        };

        newGraph.addGraphLoadedListener(graphLoadedEvent-> {
            newGraph.getElement().executeJs("mxUtils.getCurrentStyle = () => {return null;}").then(nil -> {  //NOI18N
                if (newOverlay.getWidth() != null)
                    setGraphScaleConsumer.accept(newOverlay.getWidth());
                graphLoaded.put(newGraph, true);
            });
        });
        newOverlay.addWidthChangedConsumer(width -> {
            if (newOverlay.getWidth() == null)
                newOverlay.setWidth(width);
            if (graphLoaded.get(newGraph))
                setGraphScaleConsumer.accept(width);
        });
        selectOverlay(newOverlay);
    }
    
    private void setDrawingHandMode(Tabs tabs, Tab tab) {
        if (map != null) {
            map.setHandMode();
            tabs.setSelectedTab(tab);
        }
    }
    
    private void setDrawingOverlayMode(Tabs tabs, Tab tabHand, Tab tabMarker, Tab tabPolyline) {
        if (map != null)
            map.setDrawingOverlayMode(bounds -> {
                addOverlay(bounds);
                setDrawingHandMode(tabs, tabHand);
                tabMarker.setEnabled(true);
                tabPolyline.setEnabled(true);
            });
    }
    
    private void setDrawingMarkerMode(BusinessObjectLight businessObject) {
        if (map != null)
            map.setDrawingMarkerMode(coordinate -> {
                Properties properties = new Properties();
                properties.put("position", coordinate); //NOI18N
                addNode(businessObject, properties);
            });
    }
    
    private void setDrawingPolylineMode() {
        if (map != null) {
            if (selectedOverlay != null) {
                MxGraph graph = mapOverlays.get(selectedOverlay);
                polylineDrawHelper = new PolylineDrawHelper(map, selectedOverlay, graph, helper-> {
                    BusinessObjectLight source = (BusinessObjectLight) mapVertexNode.get(helper.getSource()).getIdentifier();
                    BusinessObjectLight target = (BusinessObjectLight) mapVertexNode.get(helper.getTarget()).getIdentifier();
                    List<GeoCoordinate> coordinates = helper.getCoordintates();
                    List<Point> graphPoints = helper.getPoints();
                    
                    DialogNewContainer dialogNewContainer = new DialogNewContainer(
                        source, target, ts, aem, bem, mem, physicalConnectionsService, 
                        container -> {
                            try {
                                Properties properties = new Properties();
                                properties.put(PropertyNames.CONTROL_POINTS, coordinates);
                                properties.put(PropertyNames.COLOR, UtilHtml.toHexString(new Color(mem.getClass(container.getClassName()).getColor())));
                                                                
                                JsonArray points = Json.createArray();
                                for (int i = 0; i < graphPoints.size(); i++) {
                                    JsonObject point = Json.createObject();
                                    point.put("x", graphPoints.get(i).getX()); //NOI18N
                                    point.put("y", graphPoints.get(i).getY()); //NOI18N
                                    points.set(i, point);
                                }
                                points.remove(points.length() - 1);
                                points.remove(0);
                                properties.put("points", points.toJson()); //NOI18N
                                addEdge(container, source, target, properties);
                            } catch (MetadataObjectNotFoundException ex) {
                                new SimpleNotification(
                                    ts.getTranslatedString("module.general.messages.error"), 
                                    ex.getLocalizedMessage()
                                ).open();
                            }
                        }
                    );
                    dialogNewContainer.open();
                    setDrawingPolylineMode();
                });
                polylineDrawHelper.start();
            }
        }
    }
    
    public void selectOverlay(MapOverlay selectedOverlay) {
        if (this.selectedOverlay != null)
            mapOverlays.get(this.selectedOverlay).getStyle().set("outline", "none"); //NOI18N
        this.selectedOverlay = selectedOverlay;
        if (this.selectedOverlay != null)
            mapOverlays.get(selectedOverlay).getStyle().set("outline", "1px solid black"); //NOI18N
    }
    
    public Component getAsComponent() throws InvalidArgumentException {
        if (map == null) {
            String generalMapsProvider = null;
            try {
                generalMapsProvider = (String) aem.getConfigurationVariableValue("general.maps.provider");
                Class mapClass = Class.forName(generalMapsProvider);
                if (Map.class.isAssignableFrom(mapClass)) {
                    map = (Map) mapClass.getDeclaredConstructor().newInstance();
                    map.createComponent(aem, ts);
                    if (map.getComponent() != null) {
                        component = new Div();
                        component.setClassName("ospman-div");
                        Tabs componentTabs = new Tabs();
                        componentTabs.addClassName("ospman-tabs");
                        componentTabs.setAutoselect(false);
                        
                        Tab tabNewOspView = new Tab(new Icon(VaadinIcon.FILE_ADD));
                        tabNewOspView.setClassName("ospman-tab");
                        
                        Tab tabOpenOspView = new Tab(new Icon(VaadinIcon.FILE_SEARCH));
                        tabOpenOspView.setClassName("ospman-tab");
                        
                        Tab tabSaveOspView = new Tab(new Icon(VaadinIcon.SAFE));
                        tabSaveOspView.setClassName("ospman-tab");
                        
                        Tab tabDeleteOspView = new Tab(new Icon(VaadinIcon.FILE_REMOVE));
                        tabDeleteOspView.setClassName("ospman-tab");
                        
                        Tab tabHand = new Tab(new Icon(VaadinIcon.HAND));
                        tabHand.setClassName("ospman-tab");
                        
                        Tab tabOverlay = new Tab(new Icon(VaadinIcon.SQUARE_SHADOW));
                        tabOverlay.setClassName("ospman-tab");
                        
                        Tab tabMarker = new Tab(new Icon(VaadinIcon.MAP_MARKER));
                        tabMarker.setClassName("ospman-tab");
                        
                        Tab tabPolyline = new Tab(new Icon(VaadinIcon.PLUG));
                        tabPolyline.setClassName("ospman-tab");
                        
                        Tab tabWire = new Tab(new Icon(VaadinIcon.DOT_CIRCLE));
                        tabWire.setClassName("ospman-tab");
                        
                        disableEnableTabs(
                            Arrays.asList(tabSaveOspView, tabDeleteOspView, tabHand, tabOverlay, tabMarker, tabPolyline, tabWire),
                            Arrays.asList()
                        );
                        
                        componentTabs.add(
                            tabNewOspView, 
                            tabOpenOspView, 
                            tabSaveOspView, 
                            tabDeleteOspView, 
                            tabHand, 
                            tabOverlay, 
                            tabMarker, 
                            tabPolyline,
                            tabWire
                        );
                        tabs.put(tabHand, Tool.Hand);
                        tabs.put(tabOverlay, Tool.Overlay);
                        tabs.put(tabMarker, Tool.Marker);
                        tabs.put(tabPolyline, Tool.Polyline);
                        tabs.put(tabWire, Tool.Wire);
                        
                        tools.put(Tool.Hand, tabHand);
                        tools.put(Tool.Overlay, tabOverlay);
                        tools.put(Tool.Marker, tabMarker);
                        tools.put(Tool.Polyline, tabPolyline);
                        tools.put(Tool.Wire, tabWire);
                        
                        componentTabs.addSelectedChangeListener(selectedChangeEvent -> {
                            if (polylineDrawHelper != null)
                                polylineDrawHelper.cancel();
                            if (wiresHelper != null)
                                wiresHelper.cancel();
                            
                            selectedTab = selectedChangeEvent.getSelectedTab();
                            if (selectedTab != null) {
                                if (selectedTab.equals(tabNewOspView)) {
                                    disableEnableTabs(
                                        Arrays.asList(tabMarker, tabPolyline),
                                        Arrays.asList(tabSaveOspView, tabDeleteOspView, tabHand, tabOverlay, tabWire)
                                    );
                                    componentTabs.setSelectedTab(tabHand);
                                } else if (selectedTab.equals(tabOpenOspView)) {
                                    componentTabs.setSelectedTab(selectedChangeEvent.getPreviousTab());
                                } else if (selectedTab.equals(tabSaveOspView)) {
                                    componentTabs.setSelectedTab(selectedChangeEvent.getPreviousTab());
                                } else if (selectedTab.equals(tabDeleteOspView)) {
                                    componentTabs.setSelectedTab(selectedChangeEvent.getPreviousTab());
                                } else if (selectedTab.equals(tabHand))
                                    map.setHandMode();
                                else if (selectedTab.equals(tabOverlay)) {
                                    componentTabs.setSelectedTab(tabHand);
                                    OverlayDialog overlayDialog = new OverlayDialog(
                                        tabOverlay, ts, selectedOverlay, mapOverlays, 
                                        () -> setDrawingOverlayMode(componentTabs, tabHand, tabMarker, tabPolyline), 
                                        overlay -> selectOverlay(overlay)
                                    );
                                    componentTabs.add(overlayDialog);
                                    overlayDialog.open();
                                } else if (selectedTab.equals(tabMarker)) {
                                    componentTabs.setSelectedTab(tabHand);
                                    MarkerDialog markerDialog = new MarkerDialog(
                                        tabMarker, aem, bem, ts, viewMap.getNodes(), 
                                        businessObject -> setDrawingMarkerMode(businessObject)
                                    );
                                    componentTabs.add(markerDialog);
                                    markerDialog.open();
                                }
                                else if (selectedTab.equals(tabPolyline))
                                    setDrawingPolylineMode();
                                else if (selectedTab.equals(tabWire)) {
                                    if (selectedOverlay != null) {
                                        wiresHelper = new WiresHelper(mapOverlays.get(selectedOverlay));
                                        wiresHelper.start();
                                    }
                                }
                            }
                        });
                        component.add(componentTabs);
                        component.add(map.getComponent());
                    }
                } else {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        String.format(ts.getTranslatedString("module.ospman.not-valid-map-provider"), mapClass.getCanonicalName())
                    ).open();
                }
            } catch (ClassNotFoundException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    String.format(ts.getTranslatedString("module.ospman.not-valid-map-provider"), generalMapsProvider)
                ).open();
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage()
                ).open();
            } catch (Exception ex) {
                Logger.getLogger(OutsidePlantView.class.toString()).log(Level.SEVERE, ex.getLocalizedMessage());
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ts.getTranslatedString("module.general.messages.unexpected-error")
                ).open();
            }
        }
        else
            return component;
        return component;
    }

    @Override
    public void buildWithSavedView(byte[] view) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        this.getProperties().put(Constants.PROPERTY_ID, -1);
        this.getProperties().put(Constants.PROPERTY_NAME, "");
        this.getProperties().put(Constants.PROPERTY_DESCRIPTION, "");
        
        Double mapCenterLatitude = OutsidePlantConstants.DEFAULT_CENTER_LATITUDE;;
        Double mapCenterLongitude = OutsidePlantConstants.DEFAULT_CENTER_LONGITUDE;
        
        try {
            mapCenterLatitude = (double) aem.getConfigurationVariableValue("widgets.simplemap.centerLatitude"); //NOI18N
            mapCenterLongitude = (double) aem.getConfigurationVariableValue("widgets.simplemap.centerLongitude"); //NOI18N
        } catch(Exception ex) {
            //Nothing to do
        }
        this.viewMap.getProperties().put("center", new GeoCoordinate(mapCenterLatitude, mapCenterLongitude));
        try {
            this.viewMap.getProperties().put("zoom", aem.getConfigurationVariableValue("widgets.simplemap.zoom")); //NOI18N
        } catch(Exception ex) {
            this.viewMap.getProperties().put("zoom", OutsidePlantConstants.DEFAULT_ZOOM); //NOI18N
        }
    }

    @Override
    public AbstractViewNode addNode(BusinessObjectLight businessObject, Properties properties) {
        AbstractViewNode node = this.viewMap.findNode(businessObject.getId());
        if (node == null) {
            GeoCoordinate position = (GeoCoordinate) properties.get("position"); //NOI18N
            BusinessObjectViewNode newNode = new BusinessObjectViewNode(businessObject);
            newNode.getProperties().put(PropertyNames.LAT, position.getLatitude());
            newNode.getProperties().put(PropertyNames.LON, position.getLongitude());
            this.viewMap.addNode(newNode);
            if (selectedOverlay != null) {
                newNode.getProperties().put(PropertyNames.OVERLAY_ID, selectedOverlay.getId());
                MxGraph graph = mapOverlays.get(selectedOverlay);
                
                selectedOverlay.getProjectionFromLatLngToDivPixel(selectedOverlay.getBounds().getSouthwest(), sw -> {
                    selectedOverlay.getProjectionFromLatLngToDivPixel(selectedOverlay.getBounds().getNortheast(), ne -> {
                        graph.getElement().executeJs("return this.graph.view.scale").then(Double.class, scale -> {
                            selectedOverlay.getProjectionFromLatLngToDivPixel(position, point -> {
                                double x = (point.getX() - sw.getX()) / scale;
                                double y = (point.getY() - ne.getY()) / scale;
                                MxGraphCell vertex = new MxGraphCell();
                                vertex.setUuid(businessObject.getId());
                                vertex.setLabel(businessObject.getName());
                                vertex.setGeometry((int) x, (int) y, 24, 24);
                                vertex.setIsVertex(true);
                                List<Pair<String, String>> listRawStyle = new ArrayList();
                                listRawStyle.add(new Pair(
                                    MxConstants.STYLE_IMAGE, 
                                    StreamResourceRegistry.getURI(resourceFactory.getClassIcon(businessObject.getClassName())).toString())
                                );
                                listRawStyle.add(new Pair(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_IMAGE));
                                String rawStyle = getRawStyle(listRawStyle);
                                if (rawStyle != null)
                                    vertex.setRawStyle(rawStyle);
                                vertex.addRightClickEdgeListener(event -> openNodeDialog(newNode));
                                graph.addCell(vertex);
                                
                                mapNodeVertex.put(newNode, vertex);
                                mapVertexNode.put(vertex, newNode);
                            });
                        });
                    });
                });
            }
            return newNode;
        }
        else
            return node;
    }
    
    private String getRawStyle(List<Pair<String, String>> list) {
        if (list != null && !list.isEmpty()) {
            StringBuilder rawStyleBuilder = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                Pair<String, String> pair = list.get(i);
                if (i < list.size() - 1)
                    rawStyleBuilder.append(String.format("%s=%s;", pair.getKey(), pair.getValue()));
                else
                    rawStyleBuilder.append(String.format("%s=%s", pair.getKey(), pair.getValue()));
            }
            return rawStyleBuilder.toString();
        }
        return null;
    }
    
    @Override
    public AbstractViewEdge addEdge(BusinessObjectLight businessObject, BusinessObjectLight sourceBusinessObject, BusinessObjectLight targetBusinessObject, Properties properties) {
        AbstractViewEdge viewEdge = this.viewMap.findEdge(businessObject.getId());
        if (viewEdge == null) {
            AbstractViewNode sourceNode = this.viewMap.findNode(sourceBusinessObject.getId());
            if (sourceNode == null)
                return null;
            AbstractViewNode targetNode = this.viewMap.findNode(targetBusinessObject.getId());
            if (targetNode == null)
                return null;
            BusinessObjectViewEdge newEdge = new BusinessObjectViewEdge(businessObject);
            newEdge.getProperties().put(PropertyNames.CONTROL_POINTS, properties.get(PropertyNames.CONTROL_POINTS));
            newEdge.getProperties().put(PropertyNames.COLOR, properties.get(PropertyNames.COLOR));
            this.viewMap.addEdge(newEdge);
            this.viewMap.attachSourceNode(newEdge, sourceNode);
            this.viewMap.attachTargetNode(newEdge, targetNode);
            if (selectedOverlay != null) {
                newEdge.getProperties().put(PropertyNames.OVERLAY_ID, selectedOverlay.getId());
                MxGraph graph = mapOverlays.get(selectedOverlay);
                MxGraphCell edge = new MxGraphCell();
                edge.setUuid(businessObject.getId());
                edge.setLabel(businessObject.getName());
                edge.setIsEdge(true);
                edge.setStrokeWidth(1);
                edge.setStrokeColor(properties.getProperty(PropertyNames.COLOR));
                edge.setSource(sourceBusinessObject.getId());
                edge.setTarget(targetBusinessObject.getId());
                edge.setPoints(properties.getProperty("points")); //NOI18N
                edge.addRightClickEdgeListener(event -> openEdgeDialog(newEdge));
                graph.addCell(edge);
                
                mapEdgeVertex.put(newEdge, edge);
                mapVertexEdge.put(edge, newEdge);
            }
            return newEdge;
        }
        else
            return viewEdge;
    }

    @Override
    public void removeNode(BusinessObjectLight businessObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeEdge(BusinessObjectLight businessObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addNodeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addEdgeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
    private void openNodeDialog(BusinessObjectViewNode viewNode) {
        if (viewNode != null) {
            DialogNode dialog = new DialogNode(viewNode, ts);
            dialog.open();
        }
    }
    
    private void openEdgeDialog(BusinessObjectViewEdge viewEdge) {
        if (viewEdge != null) {
            if (selectedTab != null && tabs.containsKey(selectedTab) && 
                Tool.Wire.equals(tabs.get(selectedTab))) {
                List<BusinessObjectViewEdge> edges = new ArrayList();
                for (MxGraphCell edge : wiresHelper.getEdges())
                    edges.add(mapVertexEdge.get(edge));
                DialogWires dialog = new DialogWires(edges, bem, ts);
                wiresHelper.cancel();
                wiresHelper.start();
                dialog.open();
                return;
            }
            DialogEdge dialog = new DialogEdge(viewEdge, ts);
            dialog.open();
        }
    }
}
