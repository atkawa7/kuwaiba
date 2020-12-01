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
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route(value= "dashboard", layout = NavigationLayout.class)
public class TestObjectDashboard extends VerticalLayout {
    
    
    
//    @Override
//    public void onAttach(AttachEvent ev) {UI.getCurrent().na
//        setSizeFull();
//        try {
//            ObjectDashboard dashboard = new ObjectDashboard(bem.getObjectsOfClass("City", 1).get(0), mem, aem, bem, 
//                    actionRegistry,resourceFactory, ts);
//            dashboard.createContent();
//            add(dashboard.getContentComponent());
//        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
//            Logger.getLogger(TestObjectDashboard.class.getName()).log(Level.SEVERE, null, ex);
//            add(new Label("Can't add shit"));
//        }
//    }
}
