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

package org.kuwaiba.web.modules.ipam.actions;

import com.vaadin.event.ShortcutAction;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteQuery;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteSuggestion;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteSuggestionProvider;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteTextField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * This action allows the user to explore the existing Tunnels and the objects related to them
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ShowMPLSTunnelExplorerAction extends AbstractAction {
    
    
    public ShowMPLSTunnelExplorerAction(WebserviceBean wsBean) {
        super("Tunnel Explorer", wsBean);
    }

    @Override
    public void actionPerformed() {
        Window wdwTunnelExplorer = new Window(getCaption());
        VerticalLayout lytContent = new VerticalLayout();

        Grid<RemoteObjectLight> tblTunnels = new Grid<>();
        tblTunnels.addColumn(RemoteObjectLight::getName).setCaption("Name");
        tblTunnels.addColumn((currentTunnel) -> {
            try {
                List<RemoteObjectLight> relatedObjects = wsBean.getSpecialAttribute(currentTunnel.getClassName(), currentTunnel.getId(), "uses", Page.getCurrent().getWebBrowser().getAddress(), 
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                
                if (relatedObjects.isEmpty())
                    return "None";
                else 
                    return relatedObjects.stream().map(n -> n.toString()).collect(Collectors.joining(", "));
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getLocalizedMessage());
                return "";
            }
        }).setCaption("Service");
        
        tblTunnels.addColumn((currentTunnel) -> {
            try {
                RemoteObjectLight parent = wsBean.getParent(currentTunnel.getClassName(), currentTunnel.getId(), Page.getCurrent().getWebBrowser().getAddress(), 
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                
                return parent ==  null || parent.getName().equals(Constants.DUMMY_ROOT) ? "No parent found" : parent.toString();
                
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getLocalizedMessage());
                return "";
            }
        }).setCaption("Parent");
        
        tblTunnels.setSizeFull();

        AutocompleteTextField txtFilter = new AutocompleteTextField();
        txtFilter.setWidth(100, Sizeable.Unit.PERCENTAGE);
        txtFilter.focus();
        txtFilter.setPlaceholder("Search...");
        txtFilter.setMinChars(3);
        txtFilter.setDelay(500);
        txtFilter.setTypeSearch(true);
        txtFilter.setSuggestionProvider(new AutocompleteSuggestionProvider() {
            @Override
            public Collection<AutocompleteSuggestion> querySuggestions(AutocompleteQuery query) {
                try {

                    List<RemoteObjectLight> suggestedObjects = wsBean.getSuggestedObjectsWithFilter(query.getTerm(), 
                            Constants.CLASS_MPLSTUNNEL, 15, Page.getCurrent().getWebBrowser().getAddress(), 
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

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
        });

        Button btnSearch = new Button(VaadinIcons.SEARCH, (e) -> {
            try {
                tblTunnels.setItems();

                List<RemoteObjectLight> suggestedObjects;
                
                if (txtFilter.getValue().isEmpty())
                    suggestedObjects = wsBean.getObjectsOfClassLight(Constants.CLASS_MPLSTUNNEL, -1, Page.getCurrent().getWebBrowser().getAddress(), 
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                else
                    suggestedObjects = wsBean.getSuggestedObjectsWithFilter(txtFilter.getValue(), 
                        Constants.CLASS_MPLSTUNNEL, -1, Page.getCurrent().getWebBrowser().getAddress(), 
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

                if (suggestedObjects.isEmpty())
                    Notifications.showInfo("Your search has 0 results");
                else
                    tblTunnels.setItems(suggestedObjects);
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getLocalizedMessage());
            }
        });
        btnSearch.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        HorizontalLayout lytFilter = new HorizontalLayout(txtFilter, btnSearch);
        lytFilter.setWidth(100, Sizeable.Unit.PERCENTAGE);
        lytFilter.setMargin(true);

        lytContent.addComponents(lytFilter, tblTunnels);
        lytContent.setExpandRatio(tblTunnels, 9.5f);
        lytContent.setExpandRatio(lytFilter, 0.5f);
        lytContent.setWidth(100, Sizeable.Unit.PERCENTAGE);
        
        wdwTunnelExplorer.setContent(lytContent);
        wdwTunnelExplorer.setWidth(40, Sizeable.Unit.PERCENTAGE);
        wdwTunnelExplorer.center();
        
        UI.getCurrent().addWindow(wdwTunnelExplorer);
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) { }

}
