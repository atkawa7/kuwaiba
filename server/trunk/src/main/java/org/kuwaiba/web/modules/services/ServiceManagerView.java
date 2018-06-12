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
package org.kuwaiba.web.modules.services;

import com.vaadin.cdi.CDIView;
import com.vaadin.data.HasValue;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.selection.SingleSelectionEvent;
import com.vaadin.event.selection.SingleSelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * Main view for the Service Manager module
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */

@CDIView("smanager")
public class ServiceManagerView extends HorizontalSplitPanel implements View {
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
    private WebserviceBeanLocal wsBean;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setStyleName("processmanager");
        addStyleName("darklayout");

        setSplitPosition(33, Unit.PERCENTAGE);
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
                        List<RemoteObjectLight> servicesForCustomer = wsBean.getServicesForCustomer(selectedCustomer.getClassName(), selectedCustomer.getId(), -1, Page.getCurrent().getWebBrowser().getAddress(), 
                                ((RemoteSession) getSession().getAttribute("session")).getSessionId());
                        
                        tblServices.setItems(servicesForCustomer);
                        
                    } catch (ServerSideException ex) {
                        NotificationsUtil.showError(ex.getMessage());
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
            
//            lstServices.setItems(currentServices);
//            lstServices.setSelectionMode(Grid.SelectionMode.SINGLE);
//            lstServices.addColumn(RemoteObjectLight::getName).setCaption("Available Services");
//            lstServices.setSizeUndefined();
//            
//            lstServices.addSelectionListener(new SelectionListener<RemoteObjectLight>() {
//                @Override
//                public void selectionChange(SelectionEvent<RemoteObjectLight> event) {
//                    if (!lstServices.getSelectedItems().isEmpty())
//                        setSecondComponent(new EndToEndView(lstServices.getSelectedItems().iterator().next(), wsBean, 
//                                                Page.getCurrent().getWebBrowser().getAddress(), ((RemoteSession) getSession().getAttribute("session")).getSessionId()));
//                }
//            });

            FormLayout lytFilter = new FormLayout(cmbCustomers, txtServiceFilter);
            lytFilter.setMargin(true);

            lytLeftPanel = new VerticalLayout(lytFilter, tblServices);
            lytLeftPanel.setExpandRatio(lytFilter, 2);
            lytLeftPanel.setExpandRatio(tblServices, 8);
            lytLeftPanel.setSizeFull();
            setFirstComponent(lytLeftPanel);
        } catch (ServerSideException ex) {
            NotificationsUtil.showError(ex.getMessage());
        }
        
    }
    
    private void onTxtFilterChange(HasValue.ValueChangeEvent<String> event) {
        ListDataProvider<RemoteObjectLight> dataProvider = (ListDataProvider<RemoteObjectLight>) tblServices.getDataProvider();
        dataProvider.setFilter((source) -> {
            String filterAsLowerCase = event.getValue().toLowerCase();
            return source.getName().toLowerCase().contains(filterAsLowerCase) || source.getClassName().toLowerCase().contains(filterAsLowerCase);
        });
    }
    
}
