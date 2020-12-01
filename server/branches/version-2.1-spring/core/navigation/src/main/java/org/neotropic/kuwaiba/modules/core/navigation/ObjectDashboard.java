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

package org.neotropic.kuwaiba.modules.core.navigation;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractDetailedView;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.widgets.AbstractDashboardWidget;

/**
 * The main object editing interface. This dashboard provides a property sheet, custom actions,
 * views, explorers (special children, relationships, etc) as well as some room to add widgets 
 * in order to display contextual information about the selected object.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ObjectDashboard extends AbstractDashboardWidget {
    /**
     * The panel containing the accordions with options, property sheets and explorers.
     */
    private Div pnlLeft;
    /**
     * Panel used as view port for the views and custom widgets.
     */
    private Div pnlRight;
    /**
     * Reference to the object being edited/explored currently.
     */
    private BusinessObjectLight selectedObject;
    /**
     * Reference to the action registry.
     */
    private ActionRegistry actionRegistry;
    /**
     * Reference to the view registry.
     */
    private ViewRegistry viewRegistry;
    /**
     * Reference to the resource factory.
     */
    private ResourceFactory resourceFactory;
    
    public ObjectDashboard(BusinessObjectLight selectedObject, MetadataEntityManager mem, 
            ApplicationEntityManager aem, BusinessEntityManager bem, ActionRegistry actionRegistry, 
            ResourceFactory resourceFactory, TranslationService ts) {
        super(mem, aem, bem, ts);
        this.actionRegistry = actionRegistry;
        this.resourceFactory = resourceFactory;
        this.selectedObject = selectedObject;
    }
    
    @Override
    public void createContent() {
        SplitLayout lytContent = new SplitLayout();
        lytContent.setSplitterPosition(20);
        Accordion accOptions = new Accordion();
        try {
            PropertySheet shtMain = new PropertySheet(ts, PropertyFactory.
                    propertiesFromBusinessObject(bem.getObject(selectedObject.getClassName(), selectedObject.getId()), ts, aem, mem), "");
            accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-properties"), shtMain);
            Grid<AbstractVisualInventoryAction> tblCoreActions = new Grid<>();
            tblCoreActions.setItems(actionRegistry.getActionsForModule(NavigationModule.MODULE_ID));
            accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-core-actions"), tblCoreActions);
            Grid<AbstractVisualInventoryAction> tblCustomActions = new Grid<>();
            tblCustomActions.setItems(actionRegistry.getActionsApplicableToRecursive(selectedObject.getClassName()));
            accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-custom-actions"), tblCustomActions);
            Grid<AbstractDetailedView> tblViews = new Grid<>();
            //tblViews.setItems(List.of(new ObjectView(selectedObject, mem, aem, bem, ts, resourceFactory)));
            accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-views"), tblViews);
            Grid<AbstractVisualInventoryAction> tblExplorers = new Grid<>();
            tblCustomActions.setItems(actionRegistry.getActionsApplicableToRecursive(selectedObject.getClassName()));
            accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-explorers"), tblExplorers);
            accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-context"), new Label("Nothing to see here for now"));
            lytContent.addToPrimary(accOptions);
            this.contentComponent = lytContent;
        } catch (InventoryException ex) {
            this.contentComponent = new Label(ex.getLocalizedMessage());
        }
        add(this.contentComponent);
    }
}
