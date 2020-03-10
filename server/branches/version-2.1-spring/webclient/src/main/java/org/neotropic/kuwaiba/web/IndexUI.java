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

package org.neotropic.kuwaiba.web;

import org.neotropic.kuwaiba.core.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.persistence.application.ApplicationEntityManager;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import java.util.Locale;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route("")
public class IndexUI extends VerticalLayout {

    //@Autowired
    private ApplicationEntityManager cmn;
    
    
    @Autowired
    public IndexUI(ConnectionManager cmn, ApplicationEntityManager aem, TranslationService ts) {
        try {
            add(new Label("Testing internationalization support: " + ts.getTranslatedString(Locale.ENGLISH, "txt.hola")));
            cmn.openConnection();
            aem.createSession("admin", "dsadsad", 1, "127.0.0.1");
            add(new Label("It works!"));
        } catch (Exception ex) {
            add(new Label("An error occurred: " + ex.getMessage()));
        } finally {
            cmn.closeConnection();
            add(new Label("Session closed"));
        }
    }
    
}
