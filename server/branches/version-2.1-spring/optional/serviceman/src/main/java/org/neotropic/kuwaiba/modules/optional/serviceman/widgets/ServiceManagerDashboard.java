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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.AbstractModuleDashboard;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * The visual entry point to the Service Manager module.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ServiceManagerDashboard extends VerticalLayout implements AbstractModuleDashboard {
    /**
     * Reference to the translation service.
     */
    private TranslationService ts;
    /**
     * Sub-header with shortcut to common actions such as creating a service or a customer.
     */
    private HorizontalLayout lytQuickActions;
    private VerticalLayout lytContent;

    public ServiceManagerDashboard(TranslationService ts) {
        this.ts = ts;
    }

    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        this.lytContent = new VerticalLayout();
        this.lytContent.setSizeFull();
        
        VerticalLayout lytSearch = new VerticalLayout();
        lytSearch.setHeight("100px");
        lytSearch.setWidth("30%");
        lytSearch.setAlignItems(Alignment.CENTER);
        TextField txtSearch = new TextField();
        txtSearch.setPlaceholder(ts.getTranslatedString("module.general.messages.search"));
        txtSearch.addKeyDownListener((event) -> {
            if (event.getKey() == Key.ENTER) {
                Notification.show("Hola :D");
            }
        });
        
        Checkbox chkSearchServices = new Checkbox(ts.getTranslatedString("module.serviceman.dashboard.ui.search-services"), true);
        Checkbox chkSearchCustomers = new Checkbox(ts.getTranslatedString("module.serviceman.dashboard.ui.search-customers"), false);
        CheckboxGroup<Checkbox> chkMainFilter = new CheckboxGroup();
        chkMainFilter.setItems(chkSearchCustomers, chkSearchServices);
        
        lytSearch.add(txtSearch, new HorizontalLayout(chkSearchCustomers, chkSearchServices));
        
        this.lytContent.add(new HorizontalLayout(), lytSearch, new HorizontalLayout());

        add(this.lytContent);
    }
    
    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }
}
