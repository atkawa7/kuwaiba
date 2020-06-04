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

package org.neotropic.kuwaiba.modules.optional.serviceman;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.ActionRegistry;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.DeleteCustomerVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.DeleteServiceVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewCustomerPoolVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewCustomerVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewServicePoolVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewServiceVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.widgets.CustomerDashboard;
import org.neotropic.kuwaiba.modules.optional.serviceman.widgets.ServiceDashboardFactory;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the service manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route(value = "serviceman", layout = ServiceManagerLayout.class)
public class ServiceManagerUI extends VerticalLayout implements ActionCompletedListener {
        /**
     * Reference to the action that creates customers.
     */
    @Autowired
    private NewCustomerVisualAction actNewCustomer;
    /**
     * Reference to the action that creates services.
     */
    @Autowired
    private NewServiceVisualAction actNewService;
    /**
     * Reference to the action that creates customer pools.
     */
    @Autowired
    private NewCustomerPoolVisualAction actNewCustomerPool;
    /**
     * Reference to the action that creates service pools.
     */
    @Autowired
    private NewServicePoolVisualAction actNewServicePool;
    /**
     * Reference to the action that deletes customers.
     */
    @Autowired
    private DeleteCustomerVisualAction actDeleteCustomer;
    /**
     * Reference to the action that deletes services.
     */
    @Autowired
    private DeleteServiceVisualAction actDeleteService;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private ActionRegistry actionRegistry;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Used in the main filter to indicate that the search will be performed on services.
     */
    private static final int OPTION_SEARCH_SERVICES = 1;
    /**
     * Used in the main filter to indicate that the search will be performed on customers.
     */
    private static final int OPTION_SEARCH_CUSTOMERS = 2;
    /**
     * Sub-header with shortcut to common actions such as creating a service or a customer.
     */
    private HorizontalLayout lytQuickActions;
    /**
     * The actual content. Initially it is just a search box then it can become a page displaying the service/customer details.
     */
    private VerticalLayout lytContent;    
    @Autowired
    private ServiceDashboardFactory serviceDashboardFactory;


    @Override
    public void onAttach(AttachEvent ev) {
        setPadding(false);
        setMargin(false);
        setSizeFull();
        
        getUI().ifPresent( ui -> ui.getPage().setTitle(ts.getTranslatedString("module.serviceman.title")));
        
        this.lytQuickActions = new HorizontalLayout(buildQuickActionsMenu());
        this.lytQuickActions.setId("serviceman-quick-actions");
        
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
            if (event.getKey().getKeys().get(0).equals(Key.ENTER.getKeys().get(0))) { // Weirdly enough, event.getKey().equals(Key.Enter) ALWAYS returns false
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
                        tblResults.addColumn(new BusinessObjectSearchResultRenderer(chkMainFilter.getValue() == OPTION_SEARCH_SERVICES ? 
                                this.actionRegistry.getActionsApplicableTo(Constants.CLASS_GENERICSERVICE) : 
                                this.actionRegistry.getActionsApplicableTo(Constants.CLASS_GENERICCUSTOMER), chkMainFilter.getValue() == OPTION_SEARCH_SERVICES ?
                                        new ServiceSearchResultCallback() : new CustomerSearchResultCallback()));
                        lytSearchResults.add(tblResults);
                    }
                } catch (Exception ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage()).open();
                }
            }
        });
        
        lytSearch.add(txtSearch, new HorizontalLayout(chkMainFilter));
        this.lytContent.add(lytSearch, lytSearchResults);

        add(lytQuickActions, this.lytContent);
        
        // Register action listeners
        this.actNewCustomer.registerActionCompletedLister(this);
        this.actNewService.registerActionCompletedLister(this);
        this.actNewCustomerPool.registerActionCompletedLister(this);
        this.actNewServicePool.registerActionCompletedLister(this);
        this.actDeleteCustomer.registerActionCompletedLister(this);
        this.actDeleteService.registerActionCompletedLister(this);
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.actNewCustomer.unregisterListener(this);
        this.actNewService.unregisterListener(this);
        this.actNewCustomerPool.unregisterListener(this);
        this.actNewServicePool.unregisterListener(this);
        this.actDeleteCustomer.unregisterListener(this);
        this.actDeleteService.unregisterListener(this);
    }
    
    /**
     * Builds a header menu with the options exclusive to this module (new customer, new service, new pools, etc).
     */
    private MenuBar buildQuickActionsMenu() {
        MenuBar mnuQuickActions = new MenuBar();
        mnuQuickActions.setWidthFull();
        
        this.actionRegistry.getActionsForModule(ServiceManagerModule.MODULE_ID).stream().forEach(anAction -> {
            if (anAction.isQuickAction()) 
                mnuQuickActions.addItem(anAction.getModuleAction().getDisplayName(), 
                        event -> ((Dialog)anAction.getVisualComponent(new ModuleActionParameterSet())).open());
        });
        
        return mnuQuickActions;
    }
    
    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }
    
    public void replaceContent(Component newContent) {
        this.lytContent.removeAll();
        this.lytContent.add(newContent);
    }
    
    /**
     * Functional interface intended to be used to create the content that will be placed in the page when a search result 
     * is clicked.
     * @param <T> The type of the search result.
     */
    public interface SearchResultCallback<T> {
        /**
         * Given a search result, builds content to be displayed in the page.
         * @param searchResult The search result to be expanded.
         * @return The visual component that will show the detailed information about the search result.
         */
        public Component buildSearchResultDetailsPage(T searchResult);
    }
    
    public class CustomerSearchResultCallback implements SearchResultCallback<BusinessObjectLight> {

        @Override
        public Component buildSearchResultDetailsPage(BusinessObjectLight searchResult) {
            return new CustomerDashboard(searchResult);
        }
    }
    
    public class ServiceSearchResultCallback implements SearchResultCallback<BusinessObjectLight> {

        @Override
        public Component buildSearchResultDetailsPage(BusinessObjectLight searchResult) {
            return serviceDashboardFactory.build(searchResult);
        }
    }
    
    private class BusinessObjectSearchResultRenderer extends ComponentRenderer<VerticalLayout, BusinessObjectLight> {
        private List<AbstractVisualInventoryAction> actions;
        private SearchResultCallback<BusinessObjectLight> resultCallback;
        
        /**
         * Main constructor.
         * @param actions The list of actions associated to the present search result.
         * @param searchResultCallback What code should be trigger upon clicking on a search result.
         */
        public BusinessObjectSearchResultRenderer(List<AbstractVisualInventoryAction> actions, 
                SearchResultCallback<BusinessObjectLight> searchResultCallback) {
            super();
            this.actions = actions;
            this.resultCallback = searchResultCallback;
        }
        
        @Override
        public VerticalLayout createComponent(BusinessObjectLight result) {
            VerticalLayout lytSearchResult = new VerticalLayout();
            lytSearchResult.setSizeFull();
            lytSearchResult.setPadding(false);
            Div divTitle = new Div(new Label(result.toString()));
            divTitle.setClassName("search-result-title");
            divTitle.setWidthFull();
            divTitle.addClickListener( e -> {
                replaceContent(this.resultCallback.buildSearchResultDetailsPage(result));
            });
            
            HorizontalLayout lytActions = new HorizontalLayout();
            lytActions.setClassName("search-result-actions");
            actions.stream().forEach( anAction -> {
                Button btnAction = new Button(anAction.getModuleAction().getDisplayName());
                btnAction.setClassName("search-result-action-button");
                btnAction.getElement().setProperty("title", anAction.getModuleAction().getDescription());
                btnAction.addClickListener( event -> {
                    ((Dialog)anAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter(Constants.PROPERTY_RELATED_OBJECT, result)))).open();
                });
                lytActions.add(btnAction);
            });
            
            lytSearchResult.add(divTitle, lytActions);
            return lytSearchResult;
        } 
    }
}
