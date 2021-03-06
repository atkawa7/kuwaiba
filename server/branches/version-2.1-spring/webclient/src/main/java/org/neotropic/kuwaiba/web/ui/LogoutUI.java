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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;

/**
 * Simple page that implements the closing session logic and redirects to the login page.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route("logout")
public class LogoutUI extends VerticalLayout {
    
    @Override
    public void onAttach(AttachEvent ev) {
        getUI().ifPresent( ui -> { 
            ui.getSession().setAttribute(Session.class, null); // Closing the session doesn't -oddly- clean up the session attributes
            ui.getSession().close();
            ui.navigate(LoginUI.class);
        });
    }
}
