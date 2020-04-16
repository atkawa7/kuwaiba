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

package org.neotropic.kuwaiba.modules.optional.serviceman.widgets;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.kuwaiba.core.apis.integration.AbstractDashboard;
import org.neotropic.kuwaiba.core.apis.integration.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.ActionRegistry;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionException;

/**
 * The visual entry point to the Service Manager module.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ServiceManagerDashboard extends VerticalLayout implements AbstractDashboard {
    /**
     * Used in the main filter to indicate that the search will be performed on services.
     */
    private static final int OPTION_SEARCH_SERVICES = 1;
    /**
     * Used in the main filter to indicate that the search will be performed on customers.
     */
    private static final int OPTION_SEARCH_CUSTOMERS = 2;
    /**
     * Reference to the action registry.
     */
    private ActionRegistry actionRegistry;
    /**
     * Actions associated to services.
     */
    private List<AbstractVisualInventoryAction> serviceActions;
    /**
     * Actions associated to customers.
     */
    private List<AbstractVisualInventoryAction> customerActions;
    /**
     * Reference to the translation service.
     */
    private TranslationService ts;
    /**
     * Sub-header with shortcut to common actions such as creating a service or a customer.
     */
    private HorizontalLayout lytQuickActions;
    /**
     * The actual content. Initially it is just a search box then it can become a page displaying the service/customer details.
     */
    private VerticalLayout lytContent;
    /**
     * Reference to the Business Entity Manager.
     */
    private BusinessEntityManager bem;

    public ServiceManagerDashboard(ActionRegistry actionRegistry, TranslationService ts, MetadataEntityManager mem, 
            ApplicationEntityManager aem, BusinessEntityManager bem) {
        this.actionRegistry = actionRegistry;
        this.ts = ts;
        this.bem = bem;
        this.customerActions = actionRegistry.getActionsApplicableTo(Constants.CLASS_GENERICCUSTOMER);
        this.serviceActions = actionRegistry.getActionsApplicableTo(Constants.CLASS_GENERICSERVICE);
        
    }

    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        this.lytQuickActions = new HorizontalLayout(buildHeaderSubmenu());
        this.lytQuickActions.setWidthFull();
        this.lytQuickActions.setAlignItems(Alignment.CENTER);
        
        this.lytContent = new VerticalLayout();
        this.lytContent.setSizeFull();
        
        VerticalLayout lytSearch = new VerticalLayout();
        lytSearch.setId("serviceman-search-component");
        lytSearch.setAlignItems(Alignment.CENTER);
        
        VerticalLayout lytSearchResults = new VerticalLayout();
        lytSearchResults.setSizeFull();
        
        TextField txtSearch = new TextField();
        txtSearch.setClassName("search-box-large");
        txtSearch.setPlaceholder(ts.getTranslatedString("module.general.messages.search"));
        
        RadioButtonGroup<Integer> chkMainFilter = new RadioButtonGroup();
        chkMainFilter.setClassName("radio-button-filters-large");
        chkMainFilter.setItems(OPTION_SEARCH_SERVICES, OPTION_SEARCH_CUSTOMERS);
        chkMainFilter.setValue(OPTION_SEARCH_SERVICES);
        chkMainFilter.setRenderer(new TextRenderer<>(item -> {
            return item == OPTION_SEARCH_SERVICES ? ts.getTranslatedString("module.serviceman.dashboard.ui.search-services") :
                                ts.getTranslatedString("module.serviceman.dashboard.ui.search-customers");
        }));
        
        txtSearch.addKeyPressListener( event -> {
            if (event.getKey().getKeys().get(0).equals(Key.ENTER.getKeys().get(0))) { // Weirdly enough, event.getKey().equals(Key.Enter) returns false ALWAYS
                
                try {
                    List<BusinessObjectLight> searchResults = bem.getSuggestedObjectsWithFilter(txtSearch.getValue(), chkMainFilter.getValue() == OPTION_SEARCH_SERVICES ? Constants.CLASS_GENERICSERVICE :
                            Constants.CLASS_GENERICCUSTOMER, -1);
                    
                    lytSearchResults.removeAll();
                    
                    if (searchResults.isEmpty())
                        lytSearchResults.add(new Label(ts.getTranslatedString("module.general.messages.no-search-results")));
                    else {
                        Grid<BusinessObjectLight> tblResults = new  Grid<>();
                        tblResults.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
                        tblResults.setItems(searchResults);
                        tblResults.addColumn(new SearchResultRenderer(chkMainFilter.getValue() == OPTION_SEARCH_SERVICES ? 
                                serviceActions : customerActions));
                        lytSearchResults.add(tblResults);
                    }
                } catch (Exception ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage()).open();
                }
            }
        });
        
        lytSearch.add(txtSearch, new HorizontalLayout(chkMainFilter));
        
        
        this.lytContent.add(lytQuickActions, lytSearch, lytSearchResults);

        add(this.lytContent);
    }
    
    /**
     * Builds a header menu with the options exclusive to this module (new customer, new service, new pools, etc).
     */
    private MenuBar buildHeaderSubmenu() {
        MenuBar mnuQuickActions = new MenuBar();
        MenuItem mnuServices = mnuQuickActions.addItem(ts.getTranslatedString("module.serviceman.dashboard.ui.services"));
        MenuItem mnuCustomers = mnuQuickActions.addItem(ts.getTranslatedString("module.serviceman.dashboard.ui.customers"));
        
        this.serviceActions.stream().forEach( aServiceAction -> {
            if (aServiceAction.isQuickAction()) {
                mnuServices.getSubMenu().addItem(aServiceAction.getModuleAction().getDisplayName(), event -> {
                    ((Dialog)aServiceAction.getVisualComponent()).open();
                });
            }
            
        });
        
        this.customerActions.stream().forEach( aCustomerAction -> {
            if (aCustomerAction.isQuickAction()) {
                mnuCustomers.getSubMenu().addItem(aCustomerAction.getModuleAction().getDisplayName(), event -> {
                    ((Dialog)aCustomerAction.getVisualComponent()).open();
                });
            }
            
        });
        return mnuQuickActions;
    }
    
    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }
    
    private class SearchResult {
        private BusinessObjectLight businessObject;
        List<AbstractVisualAction> actions;

        public BusinessObjectLight getBusinessObject() {
            return businessObject;
        }

        public void setBusinessObject(BusinessObjectLight businessObject) {
            this.businessObject = businessObject;
        }

        public List<AbstractVisualAction> getActions() {
            return actions;
        }

        public void setActions(List<AbstractVisualAction> actions) {
            this.actions = actions;
        }
    }
    
    private class SearchResultRenderer extends ComponentRenderer<VerticalLayout, BusinessObjectLight> {
        List<AbstractVisualInventoryAction> actions;

        public SearchResultRenderer(List<AbstractVisualInventoryAction> actions) {
            super();
            this.actions = actions;
        }
        
        @Override
        public VerticalLayout createComponent(BusinessObjectLight result) {
            VerticalLayout lytSearchResult = new VerticalLayout();
            lytSearchResult.setSizeFull();
            Label lblTitle = new Label(result.toString());
            lblTitle.setClassName("search-result-title");
            HorizontalLayout lytActions = new HorizontalLayout();
            lytActions.setClassName("search-result-action");
            actions.stream().forEach( anAction -> {
                Button btnAction = new Button(anAction.getModuleAction().getDisplayName());
                btnAction.getElement().setProperty("title", anAction.getModuleAction().getDescription());
                btnAction.addClickListener( event -> {
                    try {
                        anAction.getModuleAction().getCallback().execute();
                    } catch (ModuleActionException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage()).open();
                    }
                });
                lytActions.add(btnAction);
            });
            
            lytSearchResult.add(lblTitle, lytActions);
            return lytSearchResult;
        } 
    }    
}
