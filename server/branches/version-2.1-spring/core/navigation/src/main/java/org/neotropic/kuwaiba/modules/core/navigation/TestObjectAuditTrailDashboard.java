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
package org.neotropic.kuwaiba.modules.core.navigation;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route("dashboard/audit-trail")
public class TestObjectAuditTrailDashboard extends VerticalLayout {
    @Autowired
    MetadataEntityManager mem;
    @Autowired
    ApplicationEntityManager aem;
    @Autowired
    BusinessEntityManager bem;
    @Autowired
    TranslationService ts;
    
        @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        try {
            ObjectAuditTrailDashboard dashboard = new ObjectAuditTrailDashboard(bem.getObjectsOfClass("Building", 1).get(0), mem, aem, bem, ts);
            dashboard.createContent();
            add(dashboard.getContentComponent());
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(TestObjectAuditTrailDashboard.class.getName()).log(Level.SEVERE, null, ex);
            add(new Label("Can't add shit"));
        }
    }
}
