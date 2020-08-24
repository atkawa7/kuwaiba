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
package com.neotropic.kuwaiba.modules.commercial.ospman;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import com.neotropic.kuwaiba.modules.commercial.ospman.widgets.OutsidePlantManagerDashboard;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.kuwaiba.modules.optional.physcon.persistence.PhysicalConnectionsService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the outside plant manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Route(value = "ospman", layout = OutsidePlantManagerLayout.class)
public class OutsidePlantManagerUI extends VerticalLayout {
    /**
     * The main dashboard.
     */
    private OutsidePlantManagerDashboard dashboard;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Factory to build resources from data source.
     */
    @Autowired
    private ResourceFactory resourceFactory;
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
    /**
     * Reference to the Physical Connection Service.
     */
    @Autowired
    private PhysicalConnectionsService physicalConnectionService;
    
    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        setMargin(false);
        setPadding(false);
        setSpacing(false);
        
        getUI().ifPresent( ui -> ui.getPage().setTitle(ts.getTranslatedString("module.ospman.title")));
        
        this.dashboard = new OutsidePlantManagerDashboard(ts, resourceFactory, aem, bem, mem, physicalConnectionService);
        add(this.dashboard);
    }
}
