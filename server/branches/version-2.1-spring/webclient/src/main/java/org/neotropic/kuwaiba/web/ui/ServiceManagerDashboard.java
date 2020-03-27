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

package org.neotropic.kuwaiba.web.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.neotropic.kuwaiba.core.apis.integration.AbstractModuleDashboard;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.serviceman.actions.NewCustomerVisualAction;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main dashboard page for the service manager module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route("serviceman")
public class ServiceManagerDashboard extends VerticalLayout implements AbstractModuleDashboard {
    @Autowired
    private NewCustomerVisualAction actNewCustomer;
    @Autowired
    private TranslationService ts;
    
    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        getUI().ifPresent( ui -> ui.getPage().setTitle(ts.getTranslatedString("module.serviceman.title")));
        this.actNewCustomer.registerActionCompletedLister(this);
        Button btnAddCustomer = new Button(this.actNewCustomer.getModuleAction().getDisplayName(), (event) -> {
            this.actNewCustomer.getVisualComponent().open();
        });
        
        add(btnAddCustomer);
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.actNewCustomer.unregisterListener(this);
    }

    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }
}
