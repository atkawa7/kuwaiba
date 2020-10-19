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
package com.neotropic.kuwaiba.modules.commercial.ospman.widgets;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dependency.CssImport;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractDashboard;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * The visual entry point to the Outside Plant Module.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@CssImport(value = "css/ospman.css")
public class OutsidePlantManagerDashboard extends VerticalLayout implements AbstractDashboard {
    /**
     * Reference to the translation service.
     */
    private final TranslationService ts;
    /**
     * Factory to build resources from data source.
     */
    private final ResourceFactory resourceFactory;
    /**
     * Reference to the Application Entity Manager.
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    private final MetadataEntityManager mem;
    /**
     * Reference to the Physical Connection Service.
     */
    private final PhysicalConnectionsService physicalConnectionService;
    /**
     * Reference to the new business object visual action.
     */
    private final NewBusinessObjectVisualAction newBusinessObjectVisualAction;
    
    public OutsidePlantManagerDashboard(
        TranslationService ts, 
        ResourceFactory resourceFactory,
        ApplicationEntityManager aem, 
        BusinessEntityManager bem, 
        MetadataEntityManager mem,
        PhysicalConnectionsService physicalConnectionService, 
        NewBusinessObjectVisualAction newBusinessObjectVisualAction) {
        
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        this.physicalConnectionService = physicalConnectionService;
        this.newBusinessObjectVisualAction = newBusinessObjectVisualAction;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        setSpacing(false);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        init();
    }
    
    private void init() {
        removeAll();
        try {
            OutsidePlantView ospView = new OutsidePlantView(aem, bem, mem, ts, resourceFactory, 
                physicalConnectionService, newBusinessObjectVisualAction, true);
            ospView.buildEmptyView();
            if (ospView.getAsComponent() != null)
                add(ospView.getAsComponent());
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage()
            ).open();
        }
    }
    
    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }
}
