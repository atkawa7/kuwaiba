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

import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.DialogOspViews;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.DialogMarker;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowContainers;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowEdge;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.OutsidePlantService;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapConstants;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapEdge;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowDeleteOspView;
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
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowNewContainer;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import java.util.Objects;
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
    private PhysicalConnectionsService physicalConnectionsService;
    /**
     * Reference to the New Business Object Visual Action
     */
    private NewBusinessObjectVisualAction newBusinessObjectVisualAction;
    /**
     * The Outside Plant View Component
     */
    private Div component;
    
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
    
    private final HashMap<BusinessObjectViewEdge, MapEdge> edges = new HashMap();
        
    public OutsidePlantView(
        ApplicationEntityManager aem, 
        BusinessEntityManager bem, 
        MetadataEntityManager mem, 
        TranslationService ts, 
        ResourceFactory resourceFactory,
        PhysicalConnectionsService physicalConnectionsService, 
        NewBusinessObjectVisualAction newBusinessObjectVisualAction) {
        
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(resourceFactory);
        Objects.requireNonNull(physicalConnectionsService);
        Objects.requireNonNull(newBusinessObjectVisualAction);
                        
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        this.physicalConnectionsService = physicalConnectionsService;
        this.newBusinessObjectVisualAction = newBusinessObjectVisualAction;
        this.viewTools = true;
    }
    
    public OutsidePlantView(
        ApplicationEntityManager aem, 
        BusinessEntityManager bem, 
        MetadataEntityManager mem, 
        TranslationService ts, 
        ResourceFactory resourceFactory) {
        
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(resourceFactory);
                        
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        this.viewTools = false;
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
    
    private void setDrawingMarkerMode(BusinessObjectLight businessObject) {
        if (mapProvider != null)
            mapProvider.setDrawingMarkerMode(coordinate -> {
                BusinessObjectViewNode newViewNode = new BusinessObjectViewNode(businessObject);
                newViewNode.getProperties().put(MapConstants.ATTR_LAT, coordinate.getLatitude());
                newViewNode.getProperties().put(MapConstants.ATTR_LON, coordinate.getLongitude());
                viewMap.addNode(newViewNode);
                addNode(businessObject, newViewNode.getProperties());
            });
    }
    
    private void setDrawingPolylineMode() {
        if (mapProvider != null) {
            mapProvider.setDrawingEdgeMode((parameters, callbackEdgeHelperCancel) -> {
                BusinessObjectLight source = (BusinessObjectLight) parameters.get(MapConstants.BUSINESS_OBJECT_SOURCE);
                BusinessObjectLight target = (BusinessObjectLight) parameters.get(MapConstants.BUSINESS_OBJECT_TARGET);
                List<GeoCoordinate> controlPoints = (List) parameters.get(MapConstants.PROPERTY_CONTROL_POINTS);
                
                WindowNewContainer dialogNewContainer = new WindowNewContainer(
                    source, target, ts, aem, bem, mem, physicalConnectionsService, 
                    container -> {
                        if (!controlPoints.isEmpty()) {
                            BusinessObjectViewEdge viewEdge = new BusinessObjectViewEdge(container);
                            viewEdge.getProperties().put(MapConstants.PROPERTY_CONTROL_POINTS, controlPoints);
                            viewMap.addEdge(viewEdge);
                            viewMap.attachSourceNode(viewEdge, viewMap.findNode(source));
                            viewMap.attachTargetNode(viewEdge, viewMap.findNode(target));
                            addEdge(container, source, target, viewEdge.getProperties());
                            
                            callbackEdgeHelperCancel.run();
                        }
                    },
                    () -> callbackEdgeHelperCancel.run()
                );
                dialogNewContainer.open();
            });
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
                    mapProvider.createComponent(aem, mem, resourceFactory, ts);
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
                            
                            Icon iconNewOspView = new Icon(VaadinIcon.PLUS);
                            
                            Tab tabNewOspView = new Tab(iconNewOspView);
                            tabNewOspView.setClassName("ospman-tab");
                            tabNewOspView.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.osp-view.new"));
                            
                            Tab tabOpenOspView = new Tab(new Icon(VaadinIcon.FOLDER_OPEN_O));
                            tabOpenOspView.setClassName("ospman-tab");
                            tabOpenOspView.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.osp-view.open"));

                            Tab tabSaveOspView = new Tab(new Icon(VaadinIcon.DOWNLOAD));
                            tabSaveOspView.setClassName("ospman-tab");
                            tabSaveOspView.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.osp-view.save"));

                            Tab tabDeleteOspView = new Tab(new Icon(VaadinIcon.CLOSE_CIRCLE_O));
                            tabDeleteOspView.setClassName("ospman-tab");
                            tabDeleteOspView.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.osp-view.delete"));

                            Tab tabHand = new Tab(new Icon(VaadinIcon.HAND));
                            tabHand.setClassName("ospman-tab");
                            tabHand.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.hand"));
                            
                            Image imgMapMarkerAdd = new Image("map-marker-add.svg", "map-marker-add");
                            imgMapMarkerAdd.setWidth("24px");
                            imgMapMarkerAdd.setHeight("24px");
                            Tab tabMarker = new Tab(imgMapMarkerAdd);
                            tabMarker.setClassName("ospman-tab");
                            tabMarker.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.marker"));

                            Tab tabPolyline = new Tab(new Icon(VaadinIcon.PLUG));
                            tabPolyline.setClassName("ospman-tab");
                            tabPolyline.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.polyline"));
                            
                            Image imgWireAdd = new Image("wire-add.svg", "wire-add");
                            imgWireAdd.setWidth("24px");
                            imgWireAdd.setHeight("24px");

                            Tab tabWire = new Tab(imgWireAdd);
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
                                        mapProvider.setPathSelectionMode((edges, callbackPathSelectionCancel) -> {
                                            WindowContainers wdwContainer = new WindowContainers(edges, aem, bem, mem, ts, callbackPathSelectionCancel);
                                            wdwContainer.open();
                                        });
                                    }
                                }
                            });
                            component.add(componentTabs);
                            newOspView(true);
                        }
                        mapProvider.addIdleEventListener(event -> {
                            mapProvider.removeIdleEventListener(event.getListener());
                            
                            viewMap.getNodes().forEach(viewNode -> 
                                addNode((BusinessObjectLight) viewNode.getIdentifier(), viewNode.getProperties())
                            );
                            viewMap.getEdges().forEach(viewEdge -> 
                                addEdge(
                                    (BusinessObjectLight) viewEdge.getIdentifier(),
                                    (BusinessObjectLight) viewMap.getEdgeSource(viewEdge).getIdentifier(),
                                    (BusinessObjectLight) viewMap.getEdgeTarget(viewEdge).getIdentifier(),
                                    viewEdge.getProperties()
                                )
                            );
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
        
        mapProvider = null;
        
        tabs.clear();
        tools.clear();
        componentTabs = null;
        selectedTab = null;
        
        if (component != null)
            component.removeAll();
        
        edges.clear();
        
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
        BusinessObjectViewNode viewNode = (BusinessObjectViewNode) viewMap.findNode(businessObject.getId());
        MapNode mapNode = mapProvider.addNode(viewNode);
        if (viewTools) {
            mapNode.setDraggableNode(true);
            mapNode.addPositionChangedEventListener(event -> {
                GeoCoordinate geoCoordinate = new GeoCoordinate(event.getLat(), event.getLng());
                viewNode.getProperties().put(MapConstants.ATTR_LAT, geoCoordinate.getLatitude());
                viewNode.getProperties().put(MapConstants.ATTR_LON, geoCoordinate.getLongitude());
                
                viewMap.getEdges().forEach(edge -> {
                    List<GeoCoordinate> controlPoints = (List) edge.getProperties().get(MapConstants.PROPERTY_CONTROL_POINTS);
                    
                    if (viewNode.equals(viewMap.getEdgeSource(edge))) {
                        controlPoints.remove(0);
                        controlPoints.add(0, geoCoordinate);
                        
                        edges.get((BusinessObjectViewEdge) edge).setControlPoints(controlPoints);
                    }
                    else if (viewNode.equals(viewMap.getEdgeTarget(edge))) {
                        controlPoints.remove(controlPoints.size() - 1);
                        controlPoints.add(geoCoordinate);
                        
                        edges.get((BusinessObjectViewEdge) edge).setControlPoints(controlPoints);
                    }
                });
            });
            mapNode.addRightClickEventListener(event -> openWindowNode(viewNode));
        }
        return viewNode;
    }
    
    @Override
    public AbstractViewEdge addEdge(BusinessObjectLight businessObject, BusinessObjectLight sourceBusinessObject, BusinessObjectLight targetBusinessObject, Properties properties) {
        AbstractViewNode sourceNode = this.viewMap.findNode(sourceBusinessObject.getId());
        if (sourceNode == null)
            return null;
        AbstractViewNode targetNode = this.viewMap.findNode(targetBusinessObject.getId());
        if (targetNode == null)
            return null;
        BusinessObjectViewEdge viewEdge = (BusinessObjectViewEdge) viewMap.findEdge(businessObject.getId());
        MapEdge mapEdge = mapProvider.addEdge(viewEdge);
        edges.put(viewEdge, mapEdge);
        if (viewTools) {
            mapEdge.addPathChangedEventListener(event -> 
                viewEdge.getProperties().put(MapConstants.PROPERTY_CONTROL_POINTS, event.getControlPoints())
            );
            mapEdge.addClickEventListener(event -> {
                edges.values().forEach(edge -> edge.setEditableEdge(false));
                mapEdge.setEditableEdge(!mapEdge.getEditableEdge());
            });
            mapEdge.addRightClickEventListener(event -> openWindowEdge(viewEdge));
        }
        return viewEdge;
    }

    @Override
    public void removeNode(BusinessObjectLight businessObject) {
        AbstractViewNode viewNode = viewMap.findNode(businessObject.getId());
        if (viewNode instanceof BusinessObjectViewNode) {
            List<BusinessObjectViewEdge> viewEdgesToRemove = new ArrayList();
            viewMap.getEdges().forEach(viewEdge -> {
                if (viewNode.equals(viewMap.getEdgeSource(viewEdge)) || viewNode.equals(viewMap.getEdgeTarget(viewEdge)))
                    viewEdgesToRemove.add((BusinessObjectViewEdge) viewEdge);
            });
            viewEdgesToRemove.forEach(viewEdge -> removeEdge(viewEdge.getIdentifier()));
            mapProvider.removeNode((BusinessObjectViewNode) viewNode);
            viewMap.getNodes().remove(viewNode);
        }
    }

    @Override
    public void removeEdge(BusinessObjectLight businessObject) {
        AbstractViewEdge viewEdge = viewMap.findEdge(businessObject.getId());
        if (viewEdge instanceof BusinessObjectViewEdge) {
            mapProvider.removeEdge((BusinessObjectViewEdge) viewEdge);
            viewMap.getEdges().remove(viewEdge);
        }
    }

    @Override
    public void addNodeClickListener(ViewEventListener listener) {
        // No need to add node click listener in the Outside Plant View
    }

    @Override
    public void addEdgeClickListener(ViewEventListener listener) {
        // No need to add edge click listener in the Outside Plant View
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
