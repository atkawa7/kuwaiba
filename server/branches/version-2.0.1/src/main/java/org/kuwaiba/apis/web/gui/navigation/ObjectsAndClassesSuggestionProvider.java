/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.web.gui.navigation;

import com.vaadin.server.Page;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteQuery;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteSuggestion;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteSuggestionProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * An autocomplete suggestions provider to be used with he 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ObjectsAndClassesSuggestionProvider implements AutocompleteSuggestionProvider {
    /**
     * Backend bean reference.
     */
    private WebserviceBean wsBean;
    /**
     * Current session id.
     */
    private String session;

    public ObjectsAndClassesSuggestionProvider(WebserviceBean wsBean, String session) {
        this.wsBean = wsBean;
        this.session = session;
    }
    
    @Override
    public Collection<AutocompleteSuggestion> querySuggestions(AutocompleteQuery query) {
        try {

            List<RemoteObjectLight> suggestedObjects = wsBean.getSuggestedObjectsWithFilter(query.getTerm(), 15, Page.getCurrent().getWebBrowser().getAddress(),
                    session);
            List<AutocompleteSuggestion> suggestions = new ArrayList<>();

            for (RemoteObjectLight aSuggestedObject : suggestedObjects) {
                AutocompleteSuggestion suggestion = new AutocompleteSuggestion(aSuggestedObject.getName(), "<b>" + aSuggestedObject.getClassName() + "</b>");
                suggestion.setData(aSuggestedObject);
                suggestions.add(suggestion);
            }
            return suggestions;
        } catch (ServerSideException ex) {
            return Arrays.asList(new AutocompleteSuggestion(ex.getLocalizedMessage()));
        }
    }
}
