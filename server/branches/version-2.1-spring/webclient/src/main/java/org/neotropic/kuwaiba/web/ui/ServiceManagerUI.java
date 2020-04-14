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

package org.neotropic.kuwaiba.web.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import java.util.Arrays;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewCustomerPoolVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewCustomerVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewServicePoolVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewServiceVisualAction;
import org.neotropic.kuwaiba.modules.optional.serviceman.widgets.ServiceManagerDashboard;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the service manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route(value = "serviceman", layout = MainLayout.class)
public class ServiceManagerUI extends VerticalLayout {
    /**
     * The main dashboard.
     */
    private ServiceManagerDashboard dashboard;
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
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    
    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        getUI().ifPresent( ui -> ui.getPage().setTitle(ts.getTranslatedString("module.serviceman.title")));
        
        this.dashboard = new ServiceManagerDashboard(Arrays.asList(actNewCustomer, actNewService, 
                actNewCustomerPool, actNewServicePool), ts, mem, aem, bem);
        add(this.dashboard);
        
        this.actNewCustomer.registerActionCompletedLister(this.dashboard);
        this.actNewService.registerActionCompletedLister(this.dashboard);
        this.actNewCustomerPool.registerActionCompletedLister(this.dashboard);
        this.actNewServicePool.registerActionCompletedLister(this.dashboard);
//        
//        Button btnAddCustomer = new Button(this.actNewCustomer.getModuleAction().getDisplayName(), (event) -> {
//            this.actNewCustomer.getVisualComponent().open();
//        });
//        
//        Button btnAddService = new Button(this.actNewService.getModuleAction().getDisplayName(), (event) -> {
//            this.actNewService.getVisualComponent().open();
//        });
//        
//        add(btnAddCustomer);
//        add(btnAddService);
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.actNewCustomer.unregisterListener(this.dashboard);
        this.actNewService.unregisterListener(this.dashboard);
        this.actNewCustomerPool.unregisterListener(this.dashboard);
        this.actNewServicePool.unregisterListener(this.dashboard);
    }

    
}
