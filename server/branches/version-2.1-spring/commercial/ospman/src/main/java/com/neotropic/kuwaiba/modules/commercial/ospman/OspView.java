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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private final TranslationService ts;
    /**
     * Reference to the Application Entity Manager
     */
    private final ApplicationEntityManager aem;
    
    private final List<MapOverlay> overlays;
    
    private final HashMap<MapOverlay, MxGraph> mapOverlays;
    private final HashMap<MxGraph, Boolean> graphLoaded;
    private Div component;
    private MapOverlay selectedOverlay;
    
    public OspView(ApplicationEntityManager aem, TranslationService ts) {
        this.aem = aem;
        this.ts = ts;
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
        if (selectedOverlay != null)
            mapOverlays.get(selectedOverlay).getStyle().set("outline", "none"); //NOI18N
        selectedOverlay = newOverlay;
        newGraph.getStyle().set("outline", "1px solid black"); //NOI18N
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
    
    private void setDrawingMarkerMode() {
        if (map != null)
            map.setDrawingMarkerMode(coordinate -> {
                setDrawingMarkerMode();
            });
    }
    
    private void setDrawingPolylineMode() {
        if (map != null)
            map.setDrawingPolylineMode(coordinates -> {
                setDrawingPolylineMode();
            });
    }
    
    private void drawingOverlayMode(Tabs tabs, Tab tabHand, Tab tabMarker, Tab tabPolyline) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setWidth("50%");
        dialog.setHeight("50%");
        
        Grid<MapOverlay> grid = new Grid();
        grid.setSizeFull();
        grid.setItems(overlays);
        
        grid.addSelectionListener(event -> {
            MapOverlay newSelectedOverlay = null;
            if (event.getFirstSelectedItem().isPresent()) {
                newSelectedOverlay = event.getFirstSelectedItem().get();
                tabMarker.setEnabled(true);
                tabPolyline.setEnabled(true);
            } else {
                tabMarker.setEnabled(false);
                tabPolyline.setEnabled(false);
            }
            /**
             * Removes the outline to the selected overlay
             */
            if (selectedOverlay != null)
                mapOverlays.get(selectedOverlay).getStyle().set("outline", "none"); //NOI18N
            
            selectedOverlay = newSelectedOverlay;
            /**
             * Adds the outline to the selected overlay
             */
            if (selectedOverlay != null)
                mapOverlays.get(selectedOverlay).getStyle().set("outline", "1px solid black"); //NOI18N
        });
        
        if (selectedOverlay != null)
            grid.select(selectedOverlay);
        
        Button btnNewOverlay = new Button(ts.getTranslatedString("module.ospman.add-overlay"), new Icon(VaadinIcon.PLUS));
        btnNewOverlay.addClickListener(clickEvent -> {
            setDrawingOverlayMode(tabs, tabHand, tabMarker, tabPolyline);
            dialog.close();
        });
        grid.addComponentColumn(MapOverlayComponent::new);
        
        VerticalLayout lytDialog = new VerticalLayout();
        lytDialog.setSizeFull();
        
        VerticalLayout lytVertical = new VerticalLayout();
        lytVertical.add(btnNewOverlay);
        lytVertical.add(grid);
        
        HorizontalLayout lytHorizontal = new HorizontalLayout();
        Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"));
        btnClose.addClickListener(clickEvent -> {
            dialog.close();
            setDrawingHandMode(tabs, tabHand);
        });
        lytHorizontal.add(btnClose);
        lytDialog.add(lytVertical);
        lytDialog.add(lytHorizontal);
        lytDialog.expand(lytVertical);
        lytDialog.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, lytHorizontal);
        
        dialog.add(lytDialog);
        dialog.open();
    }
    
    private class MapOverlayComponent extends HorizontalLayout {
        private MxGraph graph;
        
        public MapOverlayComponent(MapOverlay mapOverlay) {
            setGraph(mapOverlay);
            setMapOverlay(mapOverlay, graph);
        }
        
        public MxGraph getGraph() {
            return graph;
        }
        
        public void setGraph(MapOverlay mapOverlay) {
            if (mapOverlays.containsKey(mapOverlay))
                this.graph = mapOverlays.get(mapOverlay);
        }
        
        private void setMapOverlay(MapOverlay mapOverlay, MxGraph graph) {
            Checkbox chkEnabled = new Checkbox();
            chkEnabled.addValueChangeListener(event -> {
                mapOverlay.setEnabled(event.getValue());
                if (mapOverlay.getEnabled()) {
                    graph.setVisible(true);
                }
                else
                    graph.setVisible(false);
            });
            chkEnabled.setValue(mapOverlay.getEnabled());
            add(chkEnabled);
            if (mapOverlay.getTitle() != null)
                add(new Label(mapOverlay.getTitle()));
            else
                add(new Label(ts.getTranslatedString("module.ospman.untitled-overlay")));
//            Button btnEdit = new Button(new Icon(VaadinIcon.PENCIL));
//            Button btnDelete = new Button(new Icon(VaadinIcon.TRASH));
//            add(btnEdit);
//            add(btnDelete);
        }
    }
    
    @Override
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
                                    Arrays.asList(tabMarker, tabPolyline),
                                    Arrays.asList(tabSaveOspView, tabDeleteOspView, tabHand, tabOverlay)
                                );
                                tabs.setSelectedTab(tabHand);
                            } else if (selectedChangeEvent.getSelectedTab().equals(tabOpenOspView)) {
                                tabs.setSelectedTab(selectedChangeEvent.getPreviousTab());
                            } else if (selectedChangeEvent.getSelectedTab().equals(tabSaveOspView)) {
                                tabs.setSelectedTab(selectedChangeEvent.getPreviousTab());
                            } else if (selectedChangeEvent.getSelectedTab().equals(tabDeleteOspView)) {
                                tabs.setSelectedTab(selectedChangeEvent.getPreviousTab());
                            } else if (selectedChangeEvent.getSelectedTab().equals(tabHand))
                                map.setHandMode();
                            else if (selectedChangeEvent.getSelectedTab().equals(tabOverlay)) {
                                drawingOverlayMode(tabs, tabHand, tabMarker, tabPolyline);
                            } else if (selectedChangeEvent.getSelectedTab().equals(tabMarker))
                                setDrawingMarkerMode();
                            else if (selectedChangeEvent.getSelectedTab().equals(tabPolyline))
                                setDrawingPolylineMode();
                        });
                        component.add(tabs);
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
