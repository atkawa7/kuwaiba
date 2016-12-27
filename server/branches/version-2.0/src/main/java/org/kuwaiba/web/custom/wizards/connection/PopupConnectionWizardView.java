/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.custom.wizards.connection;

import com.google.common.eventbus.EventBus;
import com.vaadin.ui.Component;
import com.vaadin.ui.PopupView;
import org.kuwaiba.web.modules.osp.google.overlays.ConnectionPolyline;
import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
import org.vaadin.teemu.wizards.event.WizardProgressListener;
import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PopupConnectionWizardView extends PopupView implements WizardProgressListener {
    private final EventBus eventBus;
    private Wizard wizard = null;   
    private ConnectionPolyline connection;
    
    public PopupConnectionWizardView(final EventBus eventBus, ConnectionPolyline connection) {
        this.eventBus = eventBus;
        this.connection = connection;
        
        setHideOnMouseOut(false);
        initWizard();
        setContent(new PopupView.Content() {

            @Override
            public String getMinimizedValueAsHTML() {
                return "";
            }

            @Override
            public Component getPopupComponent() {
                return wizard;
            }
        });
    }
    
    private void initWizard() {
        wizard = new Wizard();
        wizard.setUriFragmentEnabled(true);
        wizard.addStep(new FirstStep(), "first");
        wizard.addStep(new SecondStep(this), "second");
        wizard.addStep(new LastStep(), "last");
        wizard.setHeight("400px");
        wizard.setWidth("500px");
        wizard.addListener(this);
    }
    
    public ConnectionPolyline getConnection() {
        return connection;
    }
    
    public Wizard getWizard() {
        return wizard;
    }

    @Override
    public void activeStepChanged(WizardStepActivationEvent event) {
        //TODO:
    }

    @Override
    public void stepSetChanged(WizardStepSetChangedEvent event) {
        //TODO:
    }

    @Override
    public void wizardCompleted(WizardCompletedEvent event) {
        eventBus.post(event);
        this.setPopupVisible(false);
//        wizard.finish();
    }

    @Override
    public void wizardCancelled(WizardCancelledEvent event) {
        this.setPopupVisible(false);
//        wizard.cancel();
    }
    
}
