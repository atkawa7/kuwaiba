/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;
//import org.kuwaiba.web.IndexUI;
import org.kuwaiba.web.modules.servmanager.dashboard.ServiceManagerDashboard;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteValidator;

/**
 * Main view for the Service Manager module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route("servmanager")
public class ServiceManagerComponent extends AbstractTopComponent {
    /**
     * View identifier
     */
    public static String VIEW_NAME = "servmanager";
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
    /**
     * main panel
     */
    private SplitLayout pnlMain;
    /**
     * A list with the existing styles used to render the nodes so they can be reused
     */
    private List<String> existingNodeStyles;

    public ServiceManagerComponent() {
        
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
//    public void initResources() {
        addClassName("dashboards");
        
        pnlMain = new SplitLayout();
        pnlMain.setSplitterPosition(33);
//        MenuBar mnuMain = ((IndexUI)getUI()).getMainMenu();
        MenuBar mnuMain = new MenuBar();
//        getUI().ifPresent(ui -> {
//            mnuMain = ui.getSession().getAttribute(MenuBar.class);
//        });
        
        this.existingNodeStyles = new ArrayList<>();
        
//        add(mnuMain);
        add(new Button("boton de prueba"));
        add(pnlMain);
//        setExpandRatio(mnuMain, 0.5f);
//        setExpandRatio(pnlMain, 9.5f);
        setSizeFull();
        
        try {
            RemoteSession currentSession = UI.getCurrent().getSession().getAttribute(RemoteSession.class);
            List<RemoteObjectLight> currentCustomers = wsBean.getObjectsOfClassLight(Constants.CLASS_GENERICCUSTOMER, -1, UI.getCurrent().getRouter().getUrl(ServiceManagerComponent.class), 
                    currentSession.getSessionId());
            
            cmbCustomers = new ComboBox<>("", currentCustomers);
            cmbCustomers.setSizeFull();
            cmbCustomers.setPlaceholder("Select a Customer...");
            
            cmbCustomers.addValueChangeListener(event -> {
                RemoteObjectLight selectedCustomer = event.getValue();
                try {
                    List<RemoteObjectLight> servicesForCustomer = wsBean.getServicesForCustomer(selectedCustomer.getClassName(), selectedCustomer.getId(), -1, UI.getCurrent().getRouter().getUrl(ServiceManagerComponent.class), 
                            currentSession.getSessionId());

                    tblServices.setItems(servicesForCustomer);

                } catch (ServerSideException ex) {
                    new Notification("Se produjo un error con el objeto seleccionado. Por favor contacte al administrador", 3000, Position.BOTTOM_END).open();
                }
            });
            
            txtServiceFilter = new TextField();
            txtServiceFilter.setPlaceholder("Type a service name or class...");
            txtServiceFilter.addValueChangeListener(this::onTxtFilterChange);
            txtServiceFilter.setSizeFull();
            
            tblServices = new Grid<>();
            tblServices.addColumn((remoteObjectLight) -> { 
                return remoteObjectLight.toString();
            }).setHeader("Name");
//                    .setStyleGenerator((aServiceNode) -> {
//
//                String definitiveColor = null;
//                
//                for (RemoteValidator aValidator : aServiceNode.getValidators()) {
//                    String validatorColor = aValidator.getProperty(Constants.PROPERTY_COLOR);
//                    if(validatorColor != null)
//                        definitiveColor = validatorColor; //If many different validator define different colors, we only care about the last one
//                }
//                
//                if (definitiveColor == null) //No validator define a color for the given object
//                    return null;
//                else {
//                    if (!existingNodeStyles.contains(definitiveColor)) {
//                        UI.getCurrent().getPage().getStyles().add(String.format(".v-grid-cell.color-table-%s { color: #%s }", definitiveColor, definitiveColor));
//                        existingNodeStyles.add(definitiveColor);
//                    }
//                    
//                    return "color-table-" + definitiveColor;
//                }
//            });
            tblServices.addColumn(RemoteObjectLight::getClassName).setHeader("Type");
            tblServices.setSizeFull();
            tblServices.setSelectionMode(Grid.SelectionMode.SINGLE);
            tblServices.addSelectionListener(selectionEvent -> {
                if (!selectionEvent.getAllSelectedItems().isEmpty()) {
                    Optional<RemoteObjectLight> selectedService = selectionEvent.getFirstSelectedItem();
                    ServiceManagerDashboard secondComponent = new ServiceManagerDashboard(cmbCustomers.getValue(), selectedService.get(), wsBean);
//                    ServiceManagerDashboard secondComponent = new ServiceManagerDashboard();
                    pnlMain.addToSecondary(secondComponent);
                }
            });

            FormLayout lytFilter = new FormLayout(cmbCustomers, txtServiceFilter);
//            lytFilter.setMargin(true);

            lytLeftPanel = new VerticalLayout(lytFilter, tblServices);
//            lytLeftPanel.setExpandRatio(lytFilter, 2);
//            lytLeftPanel.setExpandRatio(tblServices, 8);
            lytLeftPanel.setSizeFull();
            pnlMain.addToPrimary(lytLeftPanel);
        } catch (ServerSideException ex) {
            new Notification("Hubo un problema al cargar el componente. Por favor contacte al Administrador", 3000, Position.BOTTOM_END).open();
        }
        
    }
    
    private void onTxtFilterChange(ComponentValueChangeEvent<TextField, String> event) {
        ListDataProvider<RemoteObjectLight> dataProvider = (ListDataProvider<RemoteObjectLight>) tblServices.getDataProvider();
        dataProvider.setFilter((source) -> {
            String filterAsLowerCase = getTxtServiceFilter().getValue().toLowerCase();
            return source.getName().toLowerCase().contains(filterAsLowerCase) || source.getClassName().toLowerCase().contains(filterAsLowerCase);
        });
    }

    @Override
    public void registerComponents() { }

    @Override
    public void unregisterComponents() { }

    public SplitLayout getPnlMain() {
        return pnlMain;
    }

    public TextField getTxtServiceFilter() {
        return txtServiceFilter;
    }

    public void setTxtServiceFilter(TextField txtServiceFilter) {
        this.txtServiceFilter = txtServiceFilter;
    }
    
    
}
