/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.web;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.brandingeti18n.Branding;
import org.kuwaiba.core.min.apis.persistence.PersistenceService;
import org.kuwaiba.core.min.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.core.min.apis.persistence.application.Session;
import org.kuwaiba.core.min.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.core.min.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.core.min.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.core.min.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.core.min.apis.persistence.metadata.MetadataEntityManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Login view
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Route("")
public class LoginView extends VerticalLayout{

    @Autowired
    private Branding b;
    
    public LoginView() throws MetadataObjectNotFoundException {
        
        PersistenceService persistenceService = PersistenceService.getInstance();
        PersistenceService.EXECUTION_STATE state = persistenceService.getState();
        ApplicationEntityManager aem = persistenceService.getApplicationEntityManager();
        MetadataEntityManager mem = persistenceService.getMetadataEntityManager();

        try {
            Session session = aem.createSession("admin", "kuwaiba", 1, "127.0.0.1");
            
            ClassMetadata classMetadata = mem.getClass("Router");
            
            add(new Label("Login into " + Branding.COMPANY_NAME + session.getToken() + " " + classMetadata.toString()));
        } catch (ApplicationObjectNotFoundException | NotAuthorizedException ex) {
            Logger.getLogger(LoginView.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
