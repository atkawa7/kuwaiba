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
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.DialogDeleteOSPView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractDashboard;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
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
import org.neotropic.kuwaiba.modules.optional.physcon.persistence.PhysicalConnectionsService;
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
    /**
     * Reference to the Physical Connection Service.
     */
    private final PhysicalConnectionsService physicalConnectionService;
    /**
     * The layout containing the submenu with the module's quick actions.
     */
    private HorizontalLayout lytQuickActions;
    /**
     * The layout containing the module's actual content.
     */
    private VerticalLayout lytContent;
    
    private NewOspViewAction newOspViewAction;
    
    public OutsidePlantManagerDashboard(
        TranslationService ts, 
        ResourceFactory resourceFactory,
        ApplicationEntityManager aem, 
        BusinessEntityManager bem, 
        MetadataEntityManager mem,
        PhysicalConnectionsService physicalConnectionService,
        NewOspViewAction newOspViewAction) {
        
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.physicalConnectionService = physicalConnectionService;
        this.newOspViewAction = newOspViewAction;
        setSizeFull();
        setPadding(false);
        setMargin(false);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        init();
    }
    
    private void init() {
        removeAll();
        lytQuickActions = new HorizontalLayout(buildQuickActionsMenu());
        lytQuickActions.setWidthFull();
        
        lytContent = new VerticalLayout();
        lytContent.setPadding(false);
        lytContent.setMargin(false);
        lytContent.setSizeFull();
        
        lytContent.add(new EmptyMapDashboardWidget(aem, bem, mem, ts, resourceFactory));
        
        add(lytQuickActions, lytContent);
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
            OutsidePlantView outsidePlantView = new OutsidePlantView(mem, aem, bem, physicalConnectionService, ts, 
                new ViewNodeIconGenerator(resourceFactory), true, () -> init());
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
        private final DataProvider<ViewObjectLight, Void> dataProvider;
        
        public ViewObjectLightSearchResultRenderer(DataProvider<ViewObjectLight, Void> dataProvider) {
            this.dataProvider = dataProvider;
        }
        
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
            HorizontalLayout lytActions = new HorizontalLayout();
            lytActions.setClassName("search-result-actions");
            
            Button btnDelete = new Button(ts.getTranslatedString("module.general.labels.delete"));
            btnDelete.setClassName("search-result-action-button");
            btnDelete.addClickListener(event -> {
                DialogDeleteOSPView dialogDeleteOSPView = new DialogDeleteOSPView(
                    viewObjectLight.getId(), ts, aem, () -> dataProvider.refreshAll()
                );
                dialogDeleteOSPView.open();
            });
            lytActions.add(btnDelete);
            vltSearchResult.add(divTitle, lytActions);
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
