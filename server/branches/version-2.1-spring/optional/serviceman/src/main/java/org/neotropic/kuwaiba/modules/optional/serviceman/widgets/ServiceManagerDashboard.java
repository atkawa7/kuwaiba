/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.AbstractModuleDashboard;
import org.neotropic.kuwaiba.core.apis.integration.AbstractVisualModuleAction;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewServiceVisualAction;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * The visual entry point to the Service Manager module.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ServiceManagerDashboard extends VerticalLayout implements AbstractModuleDashboard {
    /**
     * Used in the main filter to indicate that the search will be performed on services.
     */
    private static final int OPTION_SEARCH_SERVICES = 1;
    /**
     * Used in the main filter to indicate that the search will be performed on customers.
     */
    private static final int OPTION_SEARCH_CUSTOMERS = 2;
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
     * Reference to the Metadata Entity Manager.
     */
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    private BusinessEntityManager bem;
    /**
     * The available actions
     */
    private NewServiceVisualAction actNewService;

    public ServiceManagerDashboard(List<AbstractVisualModuleAction> quickActions, TranslationService ts, MetadataEntityManager mem, 
            ApplicationEntityManager aem, BusinessEntityManager bem) {
        this.ts = ts;
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
    }

    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
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
                                ts.getTranslatedString("module.serviceman.dashboard.ui.search-customers"); //To change body of generated lambdas, choose Tools | Templates.
        }));
        
        txtSearch.addKeyPressListener( event -> {
            if (event.getKey().getKeys().get(0).equals(Key.ENTER.getKeys().get(0))) { // Weirdly enough, event.getKey().equals(Key.Enter) returns false ALWAYS
                Grid<BusinessObjectLight> tblResults = new  Grid<>();
                tblResults.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
                tblResults.setItemDetailsRenderer(new SearchResultRenderer());
                try {
                    tblResults.setItems(bem.getObjectsWithFilterLight(chkMainFilter.getValue() == OPTION_SEARCH_SERVICES ? Constants.CLASS_GENERICSERVICE :
                            Constants.CLASS_GENERICCUSTOMER, Constants.PROPERTY_NAME, txtSearch.getValue()));
                    lytSearchResults.removeAll();
                    lytSearchResults.add(tblResults);
                } catch (InventoryException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage()).open();
                }
            }
        });
        
        lytSearch.add(txtSearch, new HorizontalLayout(chkMainFilter));
        
        
        this.lytContent.add(new HorizontalLayout(), lytSearch, lytSearchResults);

        add(this.lytContent);
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
        List<AbstractVisualModuleAction> actions;

        public BusinessObjectLight getBusinessObject() {
            return businessObject;
        }

        public void setBusinessObject(BusinessObjectLight businessObject) {
            this.businessObject = businessObject;
        }

        public List<AbstractVisualModuleAction> getActions() {
            return actions;
        }

        public void setActions(List<AbstractVisualModuleAction> actions) {
            this.actions = actions;
        }
    }
    
    private class SearchResultRenderer extends ComponentRenderer<HorizontalLayout, BusinessObjectLight> {

        @Override
        public HorizontalLayout createComponent(BusinessObjectLight result) {
            return new HorizontalLayout(new Label(result.getName()), new Label(result.getClassName()));
        } 
    }
}
