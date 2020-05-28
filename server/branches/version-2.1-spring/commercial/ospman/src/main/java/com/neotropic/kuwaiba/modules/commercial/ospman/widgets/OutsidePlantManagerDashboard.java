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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.neotropic.kuwaiba.modules.commercial.ospman.OutsidePlantView;
import com.neotropic.kuwaiba.modules.commercial.ospman.actions.NewOspViewAction;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.List;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.integration.AbstractDashboard;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.kuwaiba.visualization.views.ViewNodeIconGenerator;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * The visual entry point to the Outside Plan Module.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OutsidePlantManagerDashboard extends VerticalLayout implements AbstractDashboard {
    /**
     * Reference to the translation service.
     */
    private final TranslationService ts;
    /**
     * Factory to build resources from data source.
     */
    private final ResourceFactory resourceFactory;
    /**
     * Reference to the Application Entity Manager.
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    private final MetadataEntityManager mem;
    
    private double longitude;
    private double latitude;
    private int zoom;
    
    private HorizontalLayout hlyQuickActions;
    private VerticalLayout vlyContent;
    
    private NewOspViewAction newOspViewAction;
    
    public OutsidePlantManagerDashboard(
        TranslationService ts, 
        ResourceFactory resourceFactory,
        ApplicationEntityManager aem, 
        BusinessEntityManager bem, 
        MetadataEntityManager mem,
        NewOspViewAction newOspViewAction) {
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        this.newOspViewAction = newOspViewAction;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        hlyQuickActions = new HorizontalLayout(buildQuickActionsMenu());
        
        vlyContent = new VerticalLayout();
        vlyContent.setSizeFull();
        VerticalLayout vlySearch = new VerticalLayout();
        vlySearch.setAlignItems(Alignment.CENTER);
        VerticalLayout vlySearchResults = new VerticalLayout();
        vlySearchResults.setSizeFull();
                
        TextField txtSearch = new TextField();
        txtSearch.setClassName("search-box-large");
        txtSearch.setPlaceholder(ts.getTranslatedString("module.general.messages.search"));
        txtSearch.addKeyPressListener(event -> {
            if (event.getKey().matches(Key.ENTER.getKeys().get(0))) {
                try {
                    List<ViewObjectLight> viewObjs = aem.getOSPViews();
                    vlySearchResults.removeAll();
                    
                    List<ViewObjectLight> filteredViewObjs = viewObjs.stream()
                        .filter(viewObject -> 
                            viewObject.getName().toLowerCase().contains(txtSearch.getValue().toLowerCase())
                        )
                        .collect(Collectors.toList());
                    if (filteredViewObjs.isEmpty()) {
                        vlySearchResults.add(new Label(ts.getTranslatedString("module.general.messages.no-search-result")));
                    } else {
                        Grid<ViewObjectLight> grdResults = new Grid();
                        grdResults.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
                        grdResults.setItems(filteredViewObjs);
                        grdResults.addColumn(new ViewObjectLightSearchResultRenderer());
                        vlySearchResults.add(grdResults);
                    }
                } catch (InvalidArgumentException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage()).open();
                }
            }
        });
        vlySearch.add(txtSearch);
        
        vlyContent.add(vlySearch, vlySearchResults);
        
        add(hlyQuickActions, vlyContent);
        
////        try {
////            OutsidePlantView outsidePlantView = new OutsidePlantView(mem, aem, bem, ts);
//            AbstractView outsidePlantView = new ViewFactory(mem, aem, bem).createViewInstance(
//                    "com.neotropic.kuwaiba.modules.commercial.ospman.google.OutsidePlantView");
////            outsidePlantView.buildEmptyView();
////            add(outsidePlantView.getAsComponent());
////        } catch (InvalidArgumentException ex) {
////            Logger.getLogger(OutsidePlantManagerDashboard.class.getName()).log(Level.SEVERE, null, ex);
////        }

//        } catch (InstantiationException ex) {
//            Logger.getLogger(OutsidePlantManagerDashboard.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InvalidArgumentException ex) {
//            Logger.getLogger(OutsidePlantManagerDashboard.class.getName()).log(Level.SEVERE, null, ex);
//        }
/*
try {
this.latitude = (double) aem.getConfigurationVariableValue("widgets.simplemap.centerLatitude");
} catch (InventoryException | ClassCastException ex) {
this.latitude = OutsidePlantConstants.DEFAULT_CENTER_LATITUDE;
}
try {
this.longitude = (double) aem.getConfigurationVariableValue("widgets.simplemap.centerLongitide");
} catch (InventoryException | ClassCastException ex) {
this.longitude = OutsidePlantConstants.DEFAULT_CENTER_LONGITUDE;
}
try {
this.zoom = (int) aem.getConfigurationVariableValue("widgets.simplemap.zoom");
} catch (InventoryException | ClassCastException ex) {
this.zoom = OutsidePlantConstants.DEFAULT_ZOOM;
}
try {
super.onAttach(attachEvent);
setSizeFull();
String className = (String) aem.getConfigurationVariableValue("general.maps.provider"); //NOI18N
Class mapProviderClass = Class.forName(className);
if (AbstractMapProvider.class.isAssignableFrom(mapProviderClass)) {
String apiKey = (String) aem.getConfigurationVariableValue("general.maps.apiKey"); //NOI18N
Properties mapProperties = new Properties();
mapProperties.put("apiKey", apiKey); //NOI18N
mapProperties.put("center", new GeoCoordinate(latitude, longitude)); //NOI18N
mapProperties.put("zoom", zoom);
mapProperties.put("bem", bem);

AbstractMapProvider mapProvider = (AbstractMapProvider) mapProviderClass.newInstance();
mapProvider.initialize(mapProperties);
add(mapProvider.getComponent());
////                SplitLayout splitLayout = new SplitLayout();
////                splitLayout.addToPrimary(buildTree());
////                splitLayout.addToSecondary(mapProvider.getComponent());
////                splitLayout.setSplitterPosition(25);
////                splitLayout.addThemeVariants(SplitLayoutVariant.LUMO_SMALL);
////                splitLayout.setSizeFull();
////                add(splitLayout);
}
} catch (Exception ex) {
new SimpleNotification(
ts.getTranslatedString("module.general.messages.error"),
ex.getLocalizedMessage()
).open();
}
*/
    }
    private MenuBar buildQuickActionsMenu() {
        MenuBar mnuQuickActions = new MenuBar();
        mnuQuickActions.setWidthFull();
        mnuQuickActions.addItem(newOspViewAction.getDisplayName(), event -> {
            addOutsidePlantView(null);
        });
        return mnuQuickActions;
    }
    private void addOutsidePlantView(ViewObjectLight viewObjectLight) {
        try {
            removeAll();
            OutsidePlantView outsidePlantView = new OutsidePlantView(mem, aem, bem, ts, 
                new ViewNodeIconGenerator(resourceFactory));
            if (viewObjectLight == null)
                outsidePlantView.buildEmptyView();
            else {
                ViewObject viewObject = aem.getOSPView(viewObjectLight.getId());
                outsidePlantView.getProperties().put(Constants.PROPERTY_ID, viewObjectLight.getId());
                outsidePlantView.getProperties().put(Constants.PROPERTY_NAME, viewObjectLight.getName());
                outsidePlantView.getProperties().put(Constants.PROPERTY_DESCRIPTION, viewObjectLight.getDescription());
                outsidePlantView.buildWithSavedView(viewObject.getStructure());
            }
            add(outsidePlantView.getAsComponent());
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage()).open();
        }
    }
    public class ViewObjectLightSearchResultRenderer extends ComponentRenderer<VerticalLayout, ViewObjectLight> {
        @Override
        public VerticalLayout createComponent(ViewObjectLight viewObjectLight) {
            VerticalLayout vltSearchResult = new VerticalLayout();
            vltSearchResult.setSizeFull();
            vltSearchResult.setPadding(false);
            vltSearchResult.setMargin(false);
            Div divTitle = new Div(new Label(viewObjectLight.getName()));
            divTitle.setClassName("search-result-title");
            divTitle.setWidthFull();
            divTitle.addClickListener(event -> {
                addOutsidePlantView(viewObjectLight);
            });
            vltSearchResult.add(divTitle);
            return vltSearchResult;
        }
    }
////    private HierarchicalDataProvider getDataProvider() {
////        return new AbstractBackEndHierarchicalDataProvider<InventoryObjectNode, Void>() {
////            @Override
////            protected Stream<InventoryObjectNode> fetchChildrenFromBackEnd(HierarchicalQuery<InventoryObjectNode, Void> query) {
////                InventoryObjectNode parent = query.getParent();
////                if (parent != null) {
////                    BusinessObjectLight object = parent.getObject();
////                    try {
////                        List<BusinessObjectLight> children = bem.getObjectChildren(
////                            object.getClassName(), object.getId(), query.getOffset(), query.getLimit());
////                        List<InventoryObjectNode> nodes = new ArrayList();
////                        for (BusinessObjectLight child : children)
////                            nodes.add(new InventoryObjectNode(child));
////                        return nodes.stream();
////                    } catch (InvalidArgumentException ex) {
////                        new SimpleNotification(
////                            ts.getTranslatedString("module.general.messages.error"), 
////                            ex.getMessage()).open();
////                        return new ArrayList().stream();
////                    }
////                } else {
////                    return Arrays.asList(new InventoryObjectNode(
////                        new BusinessObjectLight(Constants.DUMMY_ROOT, null, "Root")
////                    )).stream();
////                }
////            }
////
////            @Override
////            public int getChildCount(HierarchicalQuery<InventoryObjectNode, Void> query) {
////                InventoryObjectNode parent = query.getParent();
////                if (parent != null) {
////                    BusinessObjectLight object = parent.getObject();
////                    try {
////                        return (int) bem.getObjectChildrenCount(object.getClassName(), object.getId());
////                    } catch (Exception ex) {
////                        new SimpleNotification(
////                            ts.getTranslatedString("module.general.messages.error"), 
////                            ex.getMessage()).open();
////                        return 0;
////                    }
////                } else {
////                    return 1;
////                }
////            }
////
////            @Override
////            public boolean hasChildren(InventoryObjectNode node) {
////                return true;
////            }
////        };
////    }
////    private NavigationTree buildTree() {
////        NavigationTree navigationTree = new NavigationTree(getDataProvider(), new BasicIconGenerator(resourceFactory));
////        navigationTree.setSizeFull();
////        return navigationTree;
////    }
    
    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }
    
}
