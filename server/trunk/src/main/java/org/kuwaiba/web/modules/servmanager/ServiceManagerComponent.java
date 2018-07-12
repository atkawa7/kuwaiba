/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.web.modules.servmanager;

import com.vaadin.cdi.CDIView;
import com.vaadin.data.HasValue;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.selection.SingleSelectionEvent;
import com.vaadin.event.selection.SingleSelectionListener;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.web.IndexUI;
import org.kuwaiba.web.modules.servmanager.dashboard.ServiceManagerDashboard;
import org.kuwaiba.beans.WebserviceBean;

/**
 * Main view for the Service Manager module
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
@CDIView("smanager")
public class ServiceManagerComponent extends AbstractTopComponent {
    /**
     * View identifier
     */
    public static String VIEW_NAME = "smanager";
    /**
     * Combo box containing the current customers
     */
    private ComboBox<RemoteObjectLight> cmbCustomers;
    /**
     * Text field to filter the services
     */
    private TextField txtServiceFilter;
    /**
     * The table with the results
     */
    private Grid<RemoteObjectLight> tblServices;
    /**
     * Layout for all the graphic components on the left side
     */
    private VerticalLayout lytLeftPanel;
    /**
     * The backend bean
     */
    @Inject
    private WebserviceBean wsBean;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setStyleName("dashboards");
        addStyleName("misc");
        
        HorizontalSplitPanel pnlMain = new HorizontalSplitPanel();
        pnlMain.setSplitPosition(33, Unit.PERCENTAGE);
        MenuBar mnuMain = ((IndexUI)getUI()).getMainMenu();

        addComponent(mnuMain);
        addComponent(pnlMain);
        setExpandRatio(mnuMain, 0.5f);
        setExpandRatio(pnlMain, 9.5f);
        setSizeFull();
        
        try {
            List<RemoteObjectLight> currentCustomers = wsBean.getObjectsOfClassLight(Constants.CLASS_GENERICCUSTOMER, -1, Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) getSession().getAttribute("session")).getSessionId());
            
            cmbCustomers = new ComboBox<>("Customer", currentCustomers);
            cmbCustomers.setSizeFull();
            cmbCustomers.addSelectionListener(new SingleSelectionListener<RemoteObjectLight>() {
                @Override
                public void selectionChange(SingleSelectionEvent<RemoteObjectLight> event) {
                    RemoteObjectLight selectedCustomer = event.getValue();
                    try {
                        if (cmbCustomers.getSelectedItem().isPresent()) {
                            List<RemoteObjectLight> servicesForCustomer = wsBean.getServicesForCustomer(selectedCustomer.getClassName(), selectedCustomer.getId(), -1, Page.getCurrent().getWebBrowser().getAddress(), 
                                    ((RemoteSession) getSession().getAttribute("session")).getSessionId());

                            tblServices.setItems(servicesForCustomer);
                        } else {
                            tblServices.setItems();
                            txtServiceFilter.clear();
                        }
                        
                        if (pnlMain.getSecondComponent() != null)
                            pnlMain.removeComponent(pnlMain.getSecondComponent());
                        
                    } catch (ServerSideException ex) {
                        Notifications.showError(ex.getMessage());
                    }
                }
            });
            
            txtServiceFilter = new TextField("Filter");
            txtServiceFilter.addValueChangeListener(this::onTxtFilterChange);
            txtServiceFilter.setSizeFull();
            
            tblServices = new Grid<>();
            tblServices.addColumn(RemoteObjectLight::getName).setCaption("Name");
            tblServices.addColumn(RemoteObjectLight::getClassName).setCaption("Type");
            tblServices.setSizeFull();
            tblServices.setSelectionMode(Grid.SelectionMode.SINGLE);
            tblServices.addSelectionListener((selectionEvent) -> {
                if (!selectionEvent.getAllSelectedItems().isEmpty()) {
                    Optional<RemoteObjectLight> selectedService = selectionEvent.getFirstSelectedItem();
                    pnlMain.setSecondComponent(new ServiceManagerDashboard(cmbCustomers.getSelectedItem().get(), selectedService.get(), wsBean));
                }
            });
            

            FormLayout lytFilter = new FormLayout(cmbCustomers, txtServiceFilter);
            lytFilter.setMargin(true);

            lytLeftPanel = new VerticalLayout(lytFilter, tblServices);
            lytLeftPanel.setExpandRatio(lytFilter, 2);
            lytLeftPanel.setExpandRatio(tblServices, 8);
            lytLeftPanel.setSizeFull();
            pnlMain.setFirstComponent(lytLeftPanel);
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        
    }
    
    private void onTxtFilterChange(HasValue.ValueChangeEvent<String> event) {
        ListDataProvider<RemoteObjectLight> dataProvider = (ListDataProvider<RemoteObjectLight>) tblServices.getDataProvider();
        dataProvider.setFilter((source) -> {
            String filterAsLowerCase = event.getValue().toLowerCase();
            return source.getName().toLowerCase().contains(filterAsLowerCase) || source.getClassName().toLowerCase().contains(filterAsLowerCase);
        });
    }

    @Override
    public void registerComponents() { }

    @Override
    public void unregisterComponents() { }
    
}
