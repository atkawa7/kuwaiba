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

import com.neotropic.flow.component.mxgraph.MxGraph;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractView;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewEdge;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewEventListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OspView extends AbstractView<BusinessObjectLight, Component> {
    /**
     * Map in the Outside Plant View
     */
    private Map map;
    /**
     * Reference to the translation service.
     */
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager
     */
    private ApplicationEntityManager aem;
    
    private HashMap<MapOverlay, MxGraph> overlays;
    private HashMap<MxGraph, Boolean> graphLoaded;
    private Div div;
    
    //private List<MapOverlay> overlays;
    
    public OspView(ApplicationEntityManager aem, TranslationService ts) {
        this.aem = aem;
        this.ts = ts;
        this.overlays = new HashMap();
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

    @Override
    public byte[] getAsXml() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    @Override
    public Component getAsComponent() throws InvalidArgumentException {
        if (map == null) {
            try {
                Class mapClass = Class.forName((String) aem.getConfigurationVariableValue("general.maps.provider"));
                if (Map.class.isAssignableFrom(mapClass)) {
                    map = (Map) mapClass.getDeclaredConstructor().newInstance();
                    map.createComponent(aem, ts);
                    if (map.getComponent() != null) {
                        div = new Div();
                        div.setClassName("ospman-div");
                        Tabs tabs = new Tabs();
                        tabs.addClassName("ospman-tabs");
                        tabs.setAutoselect(false);
                        
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
                        
                        disableEnableTabs(
                            Arrays.asList(tabSaveOspView, tabDeleteOspView, tabHand, tabOverlay, tabMarker, tabPolyline),
                            Arrays.asList()
                        );
                        
                        tabs.add(
                            tabNewOspView, 
                            tabOpenOspView, 
                            tabSaveOspView, 
                            tabDeleteOspView, 
                            tabHand, 
                            tabOverlay, 
                            tabMarker, 
                            tabPolyline
                        );
                        
                        tabs.addSelectedChangeListener(selectedChangeEvent -> {
                            if (selectedChangeEvent.getSelectedTab().equals(tabNewOspView)) {
                                disableEnableTabs(
                                    Arrays.asList(),
                                    Arrays.asList(tabSaveOspView, tabDeleteOspView, tabHand, tabOverlay, tabMarker, tabPolyline)
                                );
                            } else if (selectedChangeEvent.getSelectedTab().equals(tabOpenOspView)) {

                            } else if (selectedChangeEvent.getSelectedTab().equals(tabSaveOspView)) {

                            } else if (selectedChangeEvent.getSelectedTab().equals(tabDeleteOspView)) {

                            } else if (selectedChangeEvent.getSelectedTab().equals(tabHand)) {
                                map.setHandMode();
                            } else if (selectedChangeEvent.getSelectedTab().equals(tabOverlay)) {
                                map.setDrawingOverlayMode(bounds -> {
                                    MxGraph graph = new MxGraph();
                                    graph.getElement().getStyle().set("background", "black");
                                    graph.setFullSize();
                                    graph.setOverflow("");
                                    
                                    graphLoaded.put(graph, false);
                                    
                                    MapOverlay overlay = map.createOverlay(bounds);
                                    overlay.getComponent().add(graph);
                                    overlays.put(overlay, graph);
                                    
                                    Consumer<Double> setGraphScaleConsumer = width -> {
                                        graph.getElement().executeJs("this.graph.view.setScale($0 / $1)", width, overlay.getWidth());
                                    };
                                    
                                    graph.addGraphLoadedListener(graphLoadedEvent-> {
                                        graph.getElement().executeJs("mxUtils.getCurrentStyle = () => {return null;}").then(nill -> {
                                            if (overlay.getWidth() != null)
                                                setGraphScaleConsumer.accept(overlay.getWidth());
                                            graphLoaded.put(graph, true);
                                        });
                                    });
                                    overlay.addWidthChangedConsumer(width -> {
                                        if (overlay.getWidth() == null)
                                            overlay.setWidth(width);
                                        if (graphLoaded.get(graph))
                                            setGraphScaleConsumer.accept(width);
                                    });
                                });
                            } else if (selectedChangeEvent.getSelectedTab().equals(tabMarker)) {
                                map.setDrawingMarkerMode(coordinate -> {
                                    
                                });
                            } else if (selectedChangeEvent.getSelectedTab().equals(tabPolyline)) {
                                map.setDrawingPolylineMode(coordinates -> {
                                    
                                });
                            }
                        });
                        div.add(tabs);
                        div.add(map.getComponent());
                    }
                } else {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        String.format("Class %s is not a valid map provider", mapClass.getCanonicalName())
                    ).open();
                }
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
            return div;
////            return map.getComponent();
        return div;
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AbstractViewNode addNode(BusinessObjectLight businessObject, Properties properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AbstractViewEdge addEdge(BusinessObjectLight businessObject, BusinessObjectLight sourceBusinessObject, BusinessObjectLight targetBusinessObject, Properties properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    
}
