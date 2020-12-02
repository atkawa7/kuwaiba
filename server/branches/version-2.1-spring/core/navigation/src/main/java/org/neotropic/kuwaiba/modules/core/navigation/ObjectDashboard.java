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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionRegistry;
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
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The main object editing interface. This dashboard provides a property sheet, custom actions,
 * views, explorers (special children, relationships, etc) as well as some room to add widgets 
 * in order to display contextual information about the selected object.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route(value= "object-dashboard", layout = NavigationLayout.class)
public class ObjectDashboard extends VerticalLayout implements  HasDynamicTitle {
    /**
     * Reference to the object being edited/explored currently.
     */
    private BusinessObjectLight selectedObject;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private ActionRegistry actionRegistry;
    /**
     * Reference to the resource factory.
     */
    @Autowired
    private ResourceFactory resourceFactory;

    
     @Override
    public void onAttach(AttachEvent ev) {
        UI ui = UI.getCurrent();
        if (ui != null) {
            this.selectedObject = ui.getSession().getAttribute(BusinessObjectLight.class);
            SplitLayout lytMain = new SplitLayout();
            lytMain.setSplitterPosition(30);
            lytMain.setSizeFull();
            
            VerticalLayout lytDetails = new VerticalLayout();
            lytDetails.setSizeFull();
            
            Accordion accOptions = new Accordion();
            try {
                PropertySheet shtMain = new PropertySheet(ts, PropertyFactory.
                        propertiesFromBusinessObject(bem.getObject(selectedObject.getClassName(), selectedObject.getId()), ts, aem, mem), "");
                accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-properties"), shtMain);
                Grid<AbstractVisualInventoryAction> tblCoreActions = new Grid<>();
                tblCoreActions.setItems(actionRegistry.getActionsForModule(NavigationModule.MODULE_ID));
                accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-core-actions"), tblCoreActions);
                Grid<AbstractVisualInventoryAction> tblCustomActions = new Grid<>();
                tblCoreActions.addColumn(AbstractVisualInventoryAction::getName);
                tblCoreActions.setSelectionMode(Grid.SelectionMode.SINGLE);
                tblCoreActions.addSelectionListener( evt -> {
                    if (evt.getFirstSelectedItem().isPresent()) {
                        ModuleActionParameterSet parameters = new ModuleActionParameterSet(new ModuleActionParameter<>("businessObject", selectedObject));
                        Dialog wdwObjectAction = (Dialog)evt.getFirstSelectedItem().get().getVisualComponent(parameters);
                        wdwObjectAction.open();
                    }
                });
                tblCustomActions.setItems(actionRegistry.getActionsApplicableToRecursive(selectedObject.getClassName()));
                accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-custom-actions"), tblCustomActions);
                Grid<AbstractDashboardWidget> tblViews = new Grid<>();
                tblViews.addColumn(AbstractDashboardWidget::getTitle);
                try {
                    AbstractDashboardWidget objectViewWidget = (AbstractDashboardWidget)Class.forName("org.neotropic.kuwaiba.modules.optional.physcon.widgets.ObjectViewWidget").getDeclaredConstructor(BusinessObjectLight.class,
                            MetadataEntityManager.class, ApplicationEntityManager.class, BusinessEntityManager.class,
                            TranslationService.class, ResourceFactory.class).newInstance(selectedObject, mem, aem, bem, ts, resourceFactory);
                    
                    AbstractDashboardWidget rackViewWidget = null;
                    if (selectedObject.getClassName().equals("Rack"))
                        rackViewWidget = (AbstractDashboardWidget)Class.forName("org.neotropic.kuwaiba.modules.optional.physcon.widgets.RackViewWidget").getDeclaredConstructor(BusinessObjectLight.class,
                            MetadataEntityManager.class, ApplicationEntityManager.class, BusinessEntityManager.class,
                            TranslationService.class, ResourceFactory.class).newInstance(selectedObject, mem, aem, bem, ts, resourceFactory);
                    
                    if (rackViewWidget == null)
                        tblViews.setItems(objectViewWidget);
                    else
                        tblViews.setItems(objectViewWidget, rackViewWidget);
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                    Logger.getLogger(ObjectDashboard.class.getName()).log(Level.SEVERE, null, ex);
                }

                tblViews.setSelectionMode(Grid.SelectionMode.SINGLE);
                tblViews.addSelectionListener((evt) -> {
                    if (evt.getFirstSelectedItem().isPresent())
                        lytDetails.add((VerticalLayout)evt.getFirstSelectedItem().get().getContentComponent());
                });

                accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-views"), tblViews);
                Grid<AbstractVisualInventoryAction> tblExplorers = new Grid<>();
                tblCustomActions.setItems(actionRegistry.getActionsApplicableToRecursive(selectedObject.getClassName()));
                accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-explorers"), tblExplorers);
                accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-context"), new Label("Nothing to see here for now"));
                
                lytMain.addToPrimary(accOptions);
                lytMain.addToSecondary(lytDetails);
            } catch (InventoryException ex) {
                add(new Label(ex.getLocalizedMessage()));
            }
            add(lytMain);
        };
        
    }

    @Override
    public String getPageTitle() {
        return String.format(ts.getTranslatedString("module.navigation.widgets.object-dashboard.title"), this.selectedObject);
    }
}

    
