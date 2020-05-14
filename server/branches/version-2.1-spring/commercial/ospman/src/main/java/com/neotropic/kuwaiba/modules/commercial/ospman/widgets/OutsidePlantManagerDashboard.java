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
import java.util.Properties;
import com.neotropic.kuwaiba.modules.commercial.ospman.AbstractMapProvider;
import com.neotropic.kuwaiba.modules.commercial.ospman.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.OutsidePlantConstants;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayoutVariant;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.integration.AbstractDashboard;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.icons.BasicIconGenerator;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.NavigationTree;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.nodes.InventoryObjectNode;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
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
    
    public OutsidePlantManagerDashboard(
        TranslationService ts, 
        ResourceFactory resourceFactory,
        ApplicationEntityManager aem, 
        BusinessEntityManager bem, 
        MetadataEntityManager mem) {
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        setPadding(false);
        setMargin(false);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
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
