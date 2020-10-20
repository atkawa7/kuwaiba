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

import com.neotropic.flow.component.googlemap.LatLng;
import com.neotropic.kuwaiba.modules.commercial.ospman.helpers.HelperEdgeDraw;
import com.neotropic.kuwaiba.modules.commercial.ospman.helpers.HelperContainerSelector;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.DialogOverlay;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.DialogOspViews;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.DialogMarker;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowContainers;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowEdge;
import com.neotropic.kuwaiba.modules.commercial.ospman.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.GeoBounds;
import com.neotropic.kuwaiba.modules.commercial.ospman.MapOverlay;
import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.Point;
import com.neotropic.kuwaiba.modules.commercial.ospman.OutsidePlantService;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowDeleteOspView;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowNewContainer;
import com.neotropic.kuwaiba.modules.commercial.ospman.GeoPoint;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResourceRegistry;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
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
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.views.util.UtilHtml;
import com.neotropic.kuwaiba.modules.commercial.ospman.MapProvider;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.html.Label;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectEdge;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectNode;

/**
 * Graphically displays Outside Plant elements on a map.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OutsidePlantView extends AbstractView<BusinessObjectLight, Component> {
    private final String TAG_VIEW = "view"; //NOI18N
    private final String TAG_CLASS = "class"; //NOI18N
    private final String TAG_CENTER = "center"; //NOI18N
    private final String TAG_ZOOM = "zoom"; //NOI18N
    private final String TAG_OVERLAYS = "overlays"; //NOI18N
    private final String TAG_OVERLAY = "overlay"; //NOI18N
    private final String TAG_NODES = "nodes"; //NOI18N
    private final String TAG_NODE = "node"; //NOI18N
    private final String TAG_EDGES = "edges"; //NOI18N
    private final String TAG_EDGE = "edge"; //NOI18N
    private final String TAG_COORDINATE = "coordinate"; //NOI18N
    private final String TAG_CONTROL_POINT = "controlPoint"; //NOI18N

    private final String ATTR_LAT = "lat"; //NOI18N
    private final String ATTR_LON = "lng"; //NOI18N
    private final String ATTR_ID = "id"; //NOI18N
    private final String ATTR_CLASS = "class"; //NOI18N
    private final String ATTR_TITLE = "title"; //NOI18N
    private final String ATTR_ENABLED = "enabled"; //NOI18N
    private final String ATTR_SELECTED = "selected"; //NOI18N
    private final String ATTR_OVERLAY_ID = "overlayid"; //NOI18N
    private final String ATTR_A_SIDE_ID = "asideid"; //NOI18N
    private final String ATTR_A_SIDE_CLASS = "asideclass"; //NOI18N
    private final String ATTR_B_SIDE_ID = "bsideid"; //NOI18N
    private final String ATTR_B_SIDE_CLASS = "bsideclass"; //NOI18N
    private final String ATTR_WIDTH = "width"; //NOI18N
    private final String ATTR_VERSION = "version"; ///NOI18N
        
    private class PropertyNames {
        public static final String LAT = "lat"; //NOI18N
        public static final String LON = "lon"; //NOI18N
        public static final String CONTROL_POINTS = "controlPoints"; //NOI18N
        public static final String COLOR = "color"; //NOI18N
        public static final String OVERLAY_ID = "overlayId"; //NOI18N
        public static final String CENTER = "center"; //NOI18N
        public static final String ZOOM = "zoom"; //NOI18N
        public static final String POSITION = "position"; //NOI18N
        public static final String OVERLAY = "overlay"; //NOI18N
    }
    
    private static final String LOADING = "_loading";
    /**
     * Map in the Outside Plant View
     */
    private MapProvider map;
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
    
    private final List<MapOverlay> overlays = new ArrayList();
    private final HashMap<String, MapOverlay> overlayIds = new HashMap();
    
    private final HashMap<MapOverlay, MxGraph> mapOverlays = new LinkedHashMap();
    private final HashMap<MxGraph, Boolean> graphLoaded = new HashMap();
    private Div component;
    private MapOverlay selectedOverlay;
    
    private final HashMap<BusinessObjectViewNode, MxBusinessObjectNode> mapNodeVertex = new HashMap();
    private final HashMap<MxBusinessObjectNode, BusinessObjectViewNode> mapVertexNode = new HashMap();
    private final HashMap<BusinessObjectViewEdge, MxBusinessObjectEdge> mapEdgeVertex = new HashMap();
    private final HashMap<MxBusinessObjectEdge, BusinessObjectViewEdge> mapVertexEdge = new HashMap();
    
    private HelperEdgeDraw polylineDrawHelper;
    private HelperContainerSelector wiresHelper;
    
    enum Tool {
        Hand,
        Overlay,
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
    
    public OutsidePlantView(
        ApplicationEntityManager aem, 
        BusinessEntityManager bem, 
        MetadataEntityManager mem, 
        TranslationService ts, 
        ResourceFactory resourceFactory,
        PhysicalConnectionsService physicalConnectionsService, 
        NewBusinessObjectVisualAction newBusinessObjectVisualAction,
        boolean viewTools) {
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        this.physicalConnectionsService = physicalConnectionsService;
        this.newBusinessObjectVisualAction = newBusinessObjectVisualAction;
        this.viewTools = viewTools;
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
     * }</pre>
     */    
    @Override
    public byte[] getAsXml() {
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
        
        viewMap.getProperties().put(PropertyNames.CENTER, map.getCenter());
        viewMap.getProperties().put(PropertyNames.ZOOM, map.getZoom());
        
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
            
            xmlew.add(xmlef.createStartElement(tagOverlays, null, null));
            for (MapOverlay overlay : overlays) {
                xmlew.add(xmlef.createStartElement(tagOverlay, null, null));
                xmlew.add(xmlef.createAttribute(attrId, overlay.getId()));
                if (overlay.getTitle() != null)
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
        if (disableTabs != null)
            disableTabs.forEach(tab -> tab.setEnabled(false));
        if (enableTabs != null)
            enableTabs.forEach(tab -> tab.setEnabled(true));
    }
    
    private void addOverlay(GeoBounds bounds) {
        MapOverlay newOverlay = map.createOverlay(bounds);
        newOverlay.setEnabled(true);
        
        MxGraph newGraph = new MxGraph();
        newGraph.setOverrideCurrentStyle(true);
        newGraph.setFullSize();
        newGraph.setOverflow(null);
        
        newOverlay.getComponent().add(newGraph);
        
        overlays.add(newOverlay);
        overlayIds.put(newOverlay.getId(), newOverlay);
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
    
    private void addOverlay(String id, String title, Double width0, boolean enabled, boolean selected, GeoBounds bounds, byte[] view) {
        MapOverlay newOverlay = map.createOverlay(bounds);
        newOverlay.setId(id);
        newOverlay.setTitle(title);
        newOverlay.setEnabled(enabled);
        
        MxGraph newGraph = new MxGraph();
        newGraph.setOverrideCurrentStyle(true);
        newGraph.setFullSize();
        newGraph.setOverflow(null);
        
        newOverlay.getComponent().add(newGraph);
        
        overlays.add(newOverlay);
        overlayIds.put(newOverlay.getId(), newOverlay);
        mapOverlays.put(newOverlay, newGraph);
        graphLoaded.put(newGraph, false);

        Consumer<Double> setGraphScaleConsumer = width -> {
            newGraph.getElement().executeJs("this.graph.view.setScale($0 / $1)", width, newOverlay.getWidth()); //NOI18N
            overlayReady(id, view);
        };

        newGraph.addGraphLoadedListener(graphLoadedEvent-> {
            newGraph.getElement().executeJs("mxUtils.getCurrentStyle = () => {return null;}").then(nil -> {  //NOI18N
                if (newOverlay.getWidth() != null)
                    setGraphScaleConsumer.accept(newOverlay.getWidth());
                graphLoaded.put(newGraph, true);
            });
        });
        newOverlay.addWidthChangedConsumer(width -> {
            if (newOverlay.getWidth() == null) {
                if (width0 != null)
                    newOverlay.setWidth(width0);
                else
                    newOverlay.setWidth(width);
            }
            if (graphLoaded.get(newGraph))
                setGraphScaleConsumer.accept(width);
        });
        if (selected)
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
    
    private void addOverlay(GeoBounds bounds, Tabs tabs, Tab tabHand, Tab tabMarker, Tab tabPolyline) {
        addOverlay(bounds);
        setDrawingHandMode(tabs, tabHand);
        tabMarker.setEnabled(true);
        tabPolyline.setEnabled(true);
    }
    
    private void setDrawingMarkerMode(BusinessObjectLight businessObject) {
        if (selectedOverlay == null) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.warning"), 
                ts.getTranslatedString("module.ospman.view.select-overlay")
            ).open();
            return;
        }
        if (map != null)
            map.setDrawingMarkerMode(coordinate -> {
                Properties nodeProperties = new Properties();
                nodeProperties.put(PropertyNames.POSITION, coordinate);
                nodeProperties.put(PropertyNames.OVERLAY, selectedOverlay);
                addNode(businessObject, nodeProperties);
            });
    }
    
    private void setDrawingPolylineMode() {
        if (selectedOverlay == null) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.warning"), 
                ts.getTranslatedString("module.ospman.view.select-overlay")
            ).open();
            return;
        }
        if (map != null) {
            MxGraph graph = mapOverlays.get(selectedOverlay);
            polylineDrawHelper = new HelperEdgeDraw(map, selectedOverlay, graph, helper-> {
                BusinessObjectLight source = (BusinessObjectLight) mapVertexNode.get(helper.getSource()).getIdentifier();
                BusinessObjectLight target = (BusinessObjectLight) mapVertexNode.get(helper.getTarget()).getIdentifier();
                List<GeoCoordinate> coordinates = helper.getCoordintates();
                List<Point> graphPoints = helper.getPoints();

                WindowNewContainer dialogNewContainer = new WindowNewContainer(
                    source, target, ts, aem, bem, mem, physicalConnectionsService, 
                    container -> {
                        try {
                            Properties edgeProperties = new Properties();
                            edgeProperties.put(PropertyNames.CONTROL_POINTS, coordinates);
                            edgeProperties.put(PropertyNames.COLOR, UtilHtml.toHexString(new Color(mem.getClass(container.getClassName()).getColor())));

                            JsonArray points = Json.createArray();
                            for (int i = 0; i < graphPoints.size(); i++) {
                                JsonObject point = Json.createObject();
                                point.put("x", graphPoints.get(i).getX()); //NOI18N
                                point.put("y", graphPoints.get(i).getY()); //NOI18N
                                points.set(i, point);
                            }
                            points.remove(points.length() - 1);
                            points.remove(0);
                            edgeProperties.put("points", points.toJson()); //NOI18N
                            edgeProperties.put(PropertyNames.OVERLAY, selectedOverlay);
                            addEdge(container, source, target, edgeProperties);
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
    
    public void selectOverlay(MapOverlay selectedOverlay) {
        if (this.selectedOverlay != null)
            mapOverlays.get(this.selectedOverlay).getStyle().set("outline", "none"); //NOI18N
        this.selectedOverlay = selectedOverlay;
        if (this.selectedOverlay != null)
            mapOverlays.get(selectedOverlay).getStyle().set("outline", "2px dotted red"); //NOI18N
    }
    
    public void newOspView(boolean init) {
        if (!init) {
            buildEmptyView();
            try {
                getAsComponent();
            } catch (InvalidArgumentException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage()
                ).open();
            }
        }
        map.getBounds(bounds -> {
            disableEnableTabs(
                Arrays.asList(tools.get(Tool.Marker), tools.get(Tool.Polyline)),
                Arrays.asList(tools.get(Tool.SaveView), tools.get(Tool.DeleteView), tools.get(Tool.Hand), tools.get(Tool.Overlay), tools.get(Tool.Wire))
            );
            componentTabs.setSelectedTab(tools.get(Tool.Hand));
            addOverlay(bounds, componentTabs, tools.get(Tool.Hand), tools.get(Tool.Marker), tools.get(Tool.Polyline));
        });
    }
    
    @Override
    public Component getAsComponent() throws InvalidArgumentException {
        if (map == null) {
            String generalMapsProvider = null;
            try {
                generalMapsProvider = (String) aem.getConfigurationVariableValue("general.maps.provider");
                Class mapClass = Class.forName(generalMapsProvider);
                if (MapProvider.class.isAssignableFrom(mapClass)) {
                    map = (MapProvider) mapClass.getDeclaredConstructor().newInstance();
                    map.createComponent(aem, ts);
                    if (map.getComponent() != null) {
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

                            Tab tabSaveOspView = new Tab(new Icon(VaadinIcon.SAFE));
                            tabSaveOspView.setClassName("ospman-tab");
                            tabSaveOspView.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.osp-view.save"));

                            Tab tabDeleteOspView = new Tab(new Icon(VaadinIcon.FILE_REMOVE));
                            tabDeleteOspView.setClassName("ospman-tab");
                            tabDeleteOspView.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.osp-view.delete"));

                            Tab tabHand = new Tab(new Icon(VaadinIcon.HAND));
                            tabHand.setClassName("ospman-tab");
                            tabHand.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.hand"));

                            Tab tabOverlay = new Tab(new Icon(VaadinIcon.SQUARE_SHADOW));
                            tabOverlay.setClassName("ospman-tab");
                            tabOverlay.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.overlay"));

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
                            tabs.put(tabNewOspView, Tool.NewView);
                            tabs.put(tabOpenOspView, Tool.OpenView);
                            tabs.put(tabSaveOspView, Tool.SaveView);
                            tabs.put(tabDeleteOspView, Tool.DeleteView);

                            tools.put(Tool.Hand, tabHand);
                            tools.put(Tool.Overlay, tabOverlay);
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
                                    if (selectedTab.equals(tabNewOspView)) {
                                        newOspView(false);
                                    } else if (selectedTab.equals(tabOpenOspView)) {
                                        componentTabs.setSelectedTab(tools.get(Tool.Hand));
                                        DialogOspViews ospViewDialog = new DialogOspViews(tabOpenOspView, aem, ts, viewObject -> {
                                            buildEmptyView();
                                            getProperties().put(Constants.PROPERTY_ID, viewObject.getId());
                                            getProperties().put(Constants.PROPERTY_NAME, viewObject.getName());
                                            getProperties().put(Constants.PROPERTY_DESCRIPTION, viewObject.getDescription());
                                            try {
                                                getAsComponent();
                                            } catch (InvalidArgumentException ex) {
                                                new SimpleNotification(
                                                    ts.getTranslatedString("module.general.messages.error"), 
                                                    ex.getLocalizedMessage()
                                                ).open();
                                            }
                                            buildWithSavedView(viewObject.getStructure());
                                                                                        
                                            disableEnableTabs(null, Arrays.asList(
                                                tools.get(Tool.SaveView), tools.get(Tool.DeleteView), tools.get(Tool.Hand), tools.get(Tool.Overlay), tools.get(Tool.Marker), tools.get(Tool.Polyline), tools.get(Tool.Wire)
                                            ));
                                        });
                                        componentTabs.add(ospViewDialog);
                                        ospViewDialog.open();
                                    } else if (selectedTab.equals(tabSaveOspView)) {
                                        componentTabs.setSelectedTab(tabHand);
                                        if (viewMap.getNodes().isEmpty()) {
                                            new SimpleNotification(
                                                ts.getTranslatedString("module.general.messages.information"), 
                                                ts.getTranslatedString("module.ospman.empty-view")
                                            ).open();
                                        }
                                        else
                                            saveOspView();
                                    } else if (selectedTab.equals(tabDeleteOspView)) {
                                        deleteOspView();
                                    } else if (selectedTab.equals(tabHand))
                                        map.setHandMode();
                                    else if (selectedTab.equals(tabOverlay)) {
                                        componentTabs.setSelectedTab(tabHand);
                                        DialogOverlay overlayDialog = new DialogOverlay(
                                            tabOverlay, ts, selectedOverlay, mapOverlays, 
                                            () -> setDrawingOverlayMode(componentTabs, tabHand, tabMarker, tabPolyline), 
                                            overlay -> selectOverlay(overlay)
                                        );
                                        componentTabs.add(overlayDialog);
                                        overlayDialog.open();
                                    } else if (selectedTab.equals(tabMarker)) {
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
                                        if (selectedOverlay != null) {
                                            wiresHelper = new HelperContainerSelector(mapOverlays.get(selectedOverlay));
                                            wiresHelper.start();
                                        }
                                    }
                                }
                            });
                            component.add(componentTabs);
                            newOspView(true);
                        }
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
            } catch (IllegalAccessException | IllegalArgumentException | 
                InstantiationException | NoSuchMethodException | 
                SecurityException | InvocationTargetException  ex) {
                
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
    
    /**
     * <pre>{@code
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
     * }</pre>
     */
    @Override
    public void buildWithSavedView(byte[] view) {
        try {
            QName tagView = new QName(TAG_VIEW);
            QName tagCenter = new QName(TAG_CENTER);
            QName tagZoom = new QName(TAG_ZOOM);
            QName tagOverlay = new QName(TAG_OVERLAY);
            QName tagCoordinate = new QName(TAG_COORDINATE);
                        
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(view);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
            while (reader.hasNext()) {
                reader.next();
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (tagView.equals(reader.getName())) {
                        String version = reader.getAttributeValue(null, ATTR_VERSION);
                        if (!OutsidePlantService.VIEW_VERSION.equals(version)) {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                String.format(ts.getTranslatedString("module.ospman.view.update-view-version"), 
                                    version, OutsidePlantService.VIEW_VERSION)
                            ).open();
                            break;
                        }
                    } else if (tagCenter.equals(reader.getName())) {
                        double lat = Double.valueOf(reader.getAttributeValue(null, ATTR_LAT));
                        double lon = Double.valueOf(reader.getAttributeValue(null, ATTR_LON));
                        GeoCoordinate mapCenter = new GeoCoordinate(lat, lon);
                        viewMap.getProperties().put(PropertyNames.CENTER, mapCenter);
                        map.setCenter(mapCenter);
                    } else if (tagZoom.equals(reader.getName())) {
                        double zoom = Double.valueOf(reader.getElementText());
                        viewMap.getProperties().put(PropertyNames.ZOOM, zoom);
                        map.setZoom(zoom);
                    } else if (tagOverlay.equals(reader.getName())) {
                        String overlayId = reader.getAttributeValue(null, ATTR_ID);
                        String overlayTitle = reader.getAttributeValue(null, ATTR_TITLE);
                        double overlayWidth = Double.valueOf(reader.getAttributeValue(null, ATTR_WIDTH));
                        boolean enabled = Boolean.valueOf(reader.getAttributeValue(null, ATTR_ENABLED));
                        boolean selected = Boolean.valueOf(reader.getAttributeValue(null, ATTR_SELECTED));
                        
                        List<GeoCoordinate> coordinates = new ArrayList();
                        while (true) {
                            reader.nextTag();
                            if (tagCoordinate.equals(reader.getName())) {
                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                    coordinates.add(new GeoCoordinate(
                                        Double.valueOf(reader.getAttributeValue(null, ATTR_LAT)), 
                                        Double.valueOf(reader.getAttributeValue(null, ATTR_LON))
                                    ));
                                }
                            }
                            else
                                break;
                        }
                        addOverlay(overlayId, overlayTitle, overlayWidth, enabled, selected, 
                            new GeoBounds(coordinates.get(1), coordinates.get(0)), view
                        );
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException ex) {
            Logger.getLogger(OutsidePlantView.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ts.getTranslatedString("module.general.messages.unexpected-error")
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
        
        map = null;
        overlays.clear();
        overlayIds.clear();
        mapOverlays.clear();
        graphLoaded.clear();
        selectedOverlay = null;
        mapNodeVertex.clear();
        mapVertexNode.clear();
        mapEdgeVertex.clear();
        mapVertexEdge.clear();
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
        this.viewMap.getProperties().put("center", new GeoCoordinate(mapCenterLatitude, mapCenterLongitude));
        try {
            this.viewMap.getProperties().put("zoom", aem.getConfigurationVariableValue("widgets.simplemap.zoom")); //NOI18N
        } catch(ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            this.viewMap.getProperties().put("zoom", OutsidePlantService.DEFAULT_ZOOM); //NOI18N
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
            MapOverlay overlay = (MapOverlay) properties.get("overlay");
            if (overlay != null) {
                newNode.getProperties().put(PropertyNames.OVERLAY_ID, overlay.getId());
                MxGraph graph = mapOverlays.get(overlay);
                
                overlay.getProjectionFromLatLngToDivPixel(overlay.getBounds().getSouthwest(), sw -> {
                    overlay.getProjectionFromLatLngToDivPixel(overlay.getBounds().getNortheast(), ne -> {
                        graph.getElement().executeJs("return this.graph.view.scale").then(Double.class, scale -> {
                            overlay.getProjectionFromLatLngToDivPixel(position, point -> {
                                double x = (point.getX() - sw.getX()) / scale;
                                double y = (point.getY() - ne.getY()) / scale;
                                MxBusinessObjectNode vertex = new MxBusinessObjectNode(businessObject);
                                vertex.setUuid(businessObject.getId());
                                vertex.setLabel(businessObject.getName());
                                vertex.setGeometry((int) x, (int) y, 24, 24);
                                vertex.setIsVertex(true);
                                LinkedHashMap<String, String> styles = new LinkedHashMap();
                                styles.put(
                                    MxConstants.STYLE_IMAGE, 
                                    StreamResourceRegistry.getURI(resourceFactory.getClassIcon(businessObject.getClassName())).toString()
                                );
                                styles.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_IMAGE);
                                styles.put(MxConstants.STYLE_RESIZABLE, String.valueOf(0));
                                vertex.setRawStyle(styles);
                                vertex.addRightClickCellListener(event -> {
                                    if (viewTools)
                                        openWindowNode(newNode);
                                });
                                graph.addNode(vertex);
                                
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
            MapOverlay overlay = (MapOverlay) properties.get(PropertyNames.OVERLAY);
            if (overlay != null) {
                newEdge.getProperties().put(PropertyNames.OVERLAY_ID, overlay.getId());
                MxGraph graph = mapOverlays.get(overlay);
                
                List<GeoCoordinate> coordinates = new ArrayList((List<GeoCoordinate>) properties.get(PropertyNames.CONTROL_POINTS));
                overlay.getProjectionFromLatLngToDivPixel(overlay.getBounds().getSouthwest(), sw -> {
                    overlay.getProjectionFromLatLngToDivPixel(overlay.getBounds().getNortheast(), ne -> {
                        graph.getElement().executeJs("return this.graph.view.scale").then(Double.class, scale -> {
                            // Delaying the edge addition
                            overlay.getProjectionFromLatLngToDivPixel(new GeoCoordinate(0.0, 0.0), dummy -> {
                                List<Point> newPoints = new ArrayList();
                                setPoints(overlay, newPoints, new ArrayList(coordinates), 
                                    new Point(sw.getX(), sw.getY()), 
                                    new Point(ne.getX(), ne.getY()), 
                                    scale, () -> {
                                        if (!properties.containsKey(LOADING)) {
                                            newPoints.remove(newPoints.size() - 1);
                                            newPoints.remove(0);
                                        }
                                        MxBusinessObjectEdge edge = new MxBusinessObjectEdge(businessObject);
                                        if (!newPoints.isEmpty())
                                            edge.setPoints(newPoints);
                                        edge.setUuid(businessObject.getId());
                                        edge.setLabel(businessObject.getName());
                                        edge.setStrokeWidth(1);
                                        edge.setStrokeColor(properties.getProperty(PropertyNames.COLOR));
                                        edge.setSource(sourceBusinessObject.getId());
                                        edge.setTarget(targetBusinessObject.getId());

                                        edge.addRightClickCellListener(event -> {
                                            if (viewTools)
                                                openWindowEdge(newEdge);
                                        });
                                        edge.addCellAddedListener(event -> {
                                            edge.orderCell(true);
                                            event.unregisterListener();
                                        });
                                        graph.addEdge(edge);
                                        mapEdgeVertex.put(newEdge, edge);
                                        mapVertexEdge.put(edge, newEdge);
                                });
                            });
                        });
                    });
                });
            }
            return newEdge;
        }
        else
            return viewEdge;
    }

    @Override
    public void removeNode(BusinessObjectLight businessObject) {
        AbstractViewNode node = viewMap.getNode(businessObject);
        if (node instanceof BusinessObjectViewNode) {
            BusinessObjectViewNode objectNode = (BusinessObjectViewNode) node;
            String overlayId = (String) objectNode.getProperties().get(PropertyNames.OVERLAY_ID);
            MapOverlay overlay = overlayIds.get(overlayId);
            MxGraph graph = mapOverlays.get(overlay);
            MxBusinessObjectNode objectVertex = mapNodeVertex.get(objectNode);
            if (graph != null && objectVertex != null) {
                
                List<BusinessObjectLight> edgesToRemove = new ArrayList();
                mapVertexEdge.forEach((vertex, edge) -> {
                    if (businessObject.getId().equals(vertex.getSource()) || 
                        businessObject.getId().equals(vertex.getTarget())) {
                        
                        edgesToRemove.add(edge.getIdentifier());
                    }
                });
                edgesToRemove.forEach(edge -> removeEdge(edge));
                graph.removeNode(objectVertex);
                
                viewMap.getNodes().remove(objectNode);
                mapNodeVertex.remove(objectNode);
                mapVertexNode.remove(objectVertex);
            }
        }
    }

    @Override
    public void removeEdge(BusinessObjectLight businessObject) {
        AbstractViewEdge edge = viewMap.getEdge(businessObject);
        if (edge instanceof BusinessObjectViewEdge) {
            BusinessObjectViewEdge objectEdge = (BusinessObjectViewEdge) edge;
            String overlayId = (String) objectEdge.getProperties().getProperty(PropertyNames.OVERLAY_ID);
            MapOverlay overlay = overlayIds.get(overlayId);
            MxGraph graph = mapOverlays.get(overlay);
            MxBusinessObjectEdge objectVertex = mapEdgeVertex.get(objectEdge);
            if (graph != null && objectVertex != null) {
                graph.removeEdge(objectVertex);
                
                viewMap.getEdges().remove(objectEdge);
                mapEdgeVertex.remove(objectEdge);
                mapVertexEdge.remove(objectVertex);
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
                wiresHelper.getEdges().forEach(edge -> 
                    edges.add(mapVertexEdge.get(edge))
                );
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
                getAsXml(structure -> {
                    try {
                        if (this.properties.get(Constants.PROPERTY_ID).equals(-1)) {
                            long newOSPViewId = aem.createOSPView(txtName.getValue(), txtDescription.getValue(), structure);
                            this.getProperties().put(Constants.PROPERTY_ID, newOSPViewId);
                        } else {
                            aem.updateOSPView((long) this.getProperties().get(Constants.PROPERTY_ID), 
                                txtName.getValue(), txtDescription.getValue(), structure);
                        }
                        this.getProperties().put(Constants.PROPERTY_NAME, txtName.getValue());
                        this.getProperties().put(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue());
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.success"), 
                            ts.getTranslatedString("module.ospman.view-saved")
                        ).open();
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage()
                        ).open();
                    }
                });
            }
        );
        confirmDialog.open();
    }
    
    private void deleteOspView() {
        WindowDeleteOspView confirmDialog = new WindowDeleteOspView((long) this.getProperties().get(Constants.PROPERTY_ID), ts, aem, 
            () -> componentTabs.setSelectedTab(tools.get(Tool.NewView))
        );
        confirmDialog.open();
    }
    
    private void overlayReady(String overlayReadyId, byte[] view) {
        try {
            QName tagNode = new QName(TAG_NODE);
            QName tagEdge = new QName(TAG_EDGE);
            QName tagControlPoint = new QName(TAG_CONTROL_POINT);
            
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(view);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
            while (reader.hasNext()) {
                reader.next();
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (tagNode.equals(reader.getName())) {
                        try {
                            String overlayId = reader.getAttributeValue(null, ATTR_OVERLAY_ID);
                            if (overlayReadyId.equals(overlayId)) {
                                String objectClass = reader.getAttributeValue(null, ATTR_CLASS);
                                double lat = Double.valueOf(reader.getAttributeValue(null, ATTR_LAT));
                                double lon = Double.valueOf(reader.getAttributeValue(null, ATTR_LON));                            
                                String objectId = reader.getElementText();

                                Properties nodeProperties = new Properties();
                                nodeProperties.put(PropertyNames.POSITION, new GeoCoordinate(lat, lon));
                                nodeProperties.put(PropertyNames.OVERLAY, overlayIds.get(overlayId));
                                addNode(bem.getObjectLight(objectClass, objectId), nodeProperties);
                            }
                        } catch (InventoryException ex) {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                ex.getLocalizedMessage()
                            ).open();
                        }
                    } else if (tagEdge.equals(reader.getName())) {
                        try {
                            String overlayId = reader.getAttributeValue(null, ATTR_OVERLAY_ID);
                            if (overlayReadyId.equals(overlayId)) {
                                String objectId = reader.getAttributeValue(null, ATTR_ID);
                                String objectClass = reader.getAttributeValue(null, ATTR_CLASS);
                                String aSideId = reader.getAttributeValue(null, ATTR_A_SIDE_ID);
                                String aSideClass = reader.getAttributeValue(null, ATTR_A_SIDE_CLASS);
                                String bSideId = reader.getAttributeValue(null, ATTR_B_SIDE_ID);
                                String bSideClass = reader.getAttributeValue(null, ATTR_B_SIDE_CLASS);


                                List<GeoCoordinate> controlPoints = new ArrayList();
                                while (true) {
                                    reader.nextTag();
                                    if (tagControlPoint.equals(reader.getName())) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                            controlPoints.add(new GeoCoordinate(
                                                Double.valueOf(reader.getAttributeValue(null, ATTR_LAT)), 
                                                Double.valueOf(reader.getAttributeValue(null, ATTR_LON))
                                            ));
                                        }
                                    }
                                    else
                                        break;
                                }
                                Properties edgeProperties = new Properties();
                                edgeProperties.put(PropertyNames.CONTROL_POINTS, controlPoints);
                                edgeProperties.put(PropertyNames.COLOR, UtilHtml.toHexString(new Color(mem.getClass(objectClass).getColor())));
                                edgeProperties.put(PropertyNames.OVERLAY, overlayIds.get(overlayId));
                                edgeProperties.put(LOADING, true);
                                
                                addEdge(bem.getObjectLight(objectClass, objectId),
                                        bem.getObjectLight(aSideClass, aSideId),
                                        bem.getObjectLight(bSideClass, bSideId), edgeProperties);
                            }
                        } catch (InventoryException ex) {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                ex.getLocalizedMessage()
                            ).open();
                        }
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException ex) {
            Logger.getLogger(OutsidePlantView.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ts.getTranslatedString("module.general.messages.unexpected-error")
            ).open();
        }
    }
    //<editor-fold desc="Helpers" defaultstate="collapsed">
    private void setPoints(MapOverlay mapOverlay, List<Point> points, List<GeoCoordinate> coordinates, Point sw, Point ne, double scale, Command cmd) {
        if (points != null && !coordinates.isEmpty()) {
            mapOverlay.getProjectionFromLatLngToDivPixel(coordinates.remove(0), point -> {
                double x = (point.getX() - sw.getX()) / scale;
                double y = (point.getY() - ne.getY()) / scale;
                points.add(new Point(x, y));
                setPoints(mapOverlay, points, coordinates, sw, ne, scale, cmd);
            });
        }
        else
            cmd.execute();
    }
    
    private void setPoints(List<GeoCoordinate> inout, MapOverlay mapOverlay, List<Point> points, Command cmd) {
        if (inout != null && points != null && !points.isEmpty() && mapOverlay != null) {
            Point point = points.remove(0);
            
            MxGraph graph = mapOverlays.get(mapOverlay);
            
            mapOverlay.getProjectionFromLatLngToDivPixel(mapOverlay.getBounds().getSouthwest(), sw -> {
                mapOverlay.getProjectionFromLatLngToDivPixel(mapOverlay.getBounds().getNortheast(), ne -> {
                    graph.getElement().executeJs("return this.graph.view.scale").then(Double.class, scale -> {
                        double x = sw.getX() + point.getX() * scale;
                        double y = ne.getY() + point.getY() * scale;
                        
                        mapOverlay.getProjectionFromDivPixelToLatLng(new GeoPoint(x, y), coordinate -> {
                            inout.add(coordinate);
                            setPoints(inout, mapOverlay, points, cmd);
                        });
                    });
                });
            });

        } else if (cmd != null) {
            cmd.execute();
        }
    }
    private void getAsXml(Consumer<byte[]> consumer) {
        updateNodes(new ArrayList(viewMap.getNodes()), new ArrayList(viewMap.getEdges()), consumer);
    }
    private void updateNodes(List<AbstractViewNode> viewNodeCopies, List<AbstractViewEdge> viewEdgeCopies, Consumer<byte[]> consumer) {
        if (!viewNodeCopies.isEmpty()) {
            AbstractViewNode viewNode = viewNodeCopies.remove(0);
            if (viewNode instanceof BusinessObjectViewNode) {
                BusinessObjectViewNode objectViewNode = (BusinessObjectViewNode) viewNode;
                String overlayId = (String) objectViewNode.getProperties().get(PropertyNames.OVERLAY_ID);

                MxBusinessObjectNode objectNode = mapNodeVertex.get(objectViewNode);
                MapOverlay overlay = overlayIds.get(overlayId);
                MxGraph graph = mapOverlays.get(overlay);

                if (objectNode != null && overlay != null && graph != null) {
                    overlay.getProjectionFromLatLngToDivPixel(overlay.getBounds().getSouthwest(), sw -> {
                        overlay.getProjectionFromLatLngToDivPixel(overlay.getBounds().getNortheast(), ne -> {
                            graph.getElement().executeJs("return this.graph.view.scale").then(Double.class, scale -> {
                                double x = sw.getX() + objectNode.getX() * scale;
                                double y = ne.getY() + objectNode.getY() * scale;
                                
                                overlay.getProjectionFromDivPixelToLatLng(new GeoPoint(x, y), coordinate -> {
                                    objectViewNode.getProperties().put(PropertyNames.LAT, coordinate.getLatitude());
                                    objectViewNode.getProperties().put(PropertyNames.LON, coordinate.getLongitude());

                                    updateNodes(viewNodeCopies, viewEdgeCopies, consumer);
                                });
                            });
                        });
                    });
                }
            }
        } else {
            updateEdges(viewEdgeCopies, consumer);
        }
    }
    private void updateEdges(List<AbstractViewEdge> viewEdgeCopies, Consumer<byte[]> consumer) {
        if (!viewEdgeCopies.isEmpty()) {
            AbstractViewEdge viewEdge = viewEdgeCopies.remove(0);
            if (viewEdge instanceof BusinessObjectViewEdge) {
                BusinessObjectViewEdge objectViewEdge = (BusinessObjectViewEdge) viewEdge;
                String overlayId = objectViewEdge.getProperties().getProperty(PropertyNames.OVERLAY_ID);
                
                MxBusinessObjectEdge objectEdge = mapEdgeVertex.get(objectViewEdge);
                MapOverlay overlay = overlayIds.get(overlayId);
                
                if (objectEdge != null && overlay != null) {
                    List<GeoCoordinate> coordinates = new ArrayList();
                    setPoints(coordinates, overlay, new ArrayList(objectEdge.getPointList()), 
                        () -> {
                            objectViewEdge.getProperties().put(PropertyNames.CONTROL_POINTS, coordinates);
                            
                            updateEdges(viewEdgeCopies, consumer);
                        }
                    );
                }
            }
        } else {
            consumer.accept(getAsXml());
        }
    }
    //</editor-fold>
}
