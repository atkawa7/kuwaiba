/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.web.modules.ipam;

import com.vaadin.cdi.CDIView;
import com.vaadin.event.ShortcutAction;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteQuery;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteSuggestion;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteSuggestionProvider;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteTextField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.web.IndexUI;

/**
 * The main view for the IP Address Manager module
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
@CDIView("ipam")
public class IPAddressManagerComponent extends AbstractTopComponent {

    /**
     * View identifier
     */
    public static String VIEW_NAME = "ipam";
    /**
     * Combo box containing the current customers
     */
    private ComboBox<RemoteObjectLight> cmbCustomers;
    /**
     * Text field to filter the services
     */
    private AutocompleteTextField txtFilter;
    /**
     * The table with the results
     */
    private Grid<RemoteObjectLight> tblIps;
    /**
     * Layout for all the graphic components on the left side
     */
    private VerticalLayout lytLeftPanel;
    /**
     * Reference to the current session
     */
    private RemoteSession session;
    /**
     * The backend bean
     */
    @Inject
    private WebserviceBean wsBean;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setStyleName("dashboards");
        addStyleName("misc");
        
        HorizontalSplitPanel pnlMain = new HorizontalSplitPanel();
        pnlMain.setSplitPosition(33, Unit.PERCENTAGE);
        MenuBar mnuMain = ((IndexUI)getUI()).getMainMenu();

        addComponent(mnuMain);
        addComponent(pnlMain);
        setExpandRatio(mnuMain, 0.3f);
        setExpandRatio(pnlMain, 9.7f);
        setSizeFull();
        
        this.session = ((RemoteSession) getSession().getAttribute("session"));
        this.lytLeftPanel = new VerticalLayout();

        this.txtFilter = new AutocompleteTextField();
        this.txtFilter.setWidth(100, Unit.PERCENTAGE);
        this.txtFilter.setPlaceholder("Search...");
        this.txtFilter.setMinChars(3);
        this.txtFilter.setDelay(500);
        this.txtFilter.setTypeSearch(true);
        this.txtFilter.setSuggestionProvider(new AutocompleteSuggestionProvider() {
            @Override
            public Collection<AutocompleteSuggestion> querySuggestions(AutocompleteQuery query) {
                try {
                    
                    List<RemoteObjectLight> suggestedObjects = wsBean.getSuggestedObjectsWithFilter(query.getTerm(), 
                            Constants.CLASS_GENERICADDRESS, 15, Page.getCurrent().getWebBrowser().getAddress(),
                            session.getSessionId());
                    
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
        
        this.txtFilter.addSelectListener((e) -> {
            this.tblIps.setItems();
            this.tblIps.setItems((RemoteObjectLight)e.getSuggestion().getData());
            
        });
        
        Button btnSearch = new Button(VaadinIcons.SEARCH, (e) -> {
            
            if (this.txtFilter.getValue().length() < 2) {
                Notifications.showInfo("Please refine your search");
                return;
            }
            try {
                this.tblIps.setItems();

                List<RemoteObjectLight> suggestedObjects = wsBean.getSuggestedObjectsWithFilter(this.txtFilter.getValue(), 
                        Constants.CLASS_GENERICADDRESS, -1, Page.getCurrent().getWebBrowser().getAddress(),
                        session.getSessionId());
                
                if (suggestedObjects.isEmpty())
                    Notifications.showInfo("Your search has 0 results");
                else
                    tblIps.setItems(suggestedObjects);
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getLocalizedMessage());
            }
        });
        btnSearch.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        
        this.tblIps = new Grid<>();
        this.tblIps.addColumn(RemoteObjectLight::getName).setCaption("Name");
        this.tblIps.addColumn(RemoteObjectLight::getClassName).setCaption("Type");
        this.tblIps.setSizeFull();
        
        this.tblIps.addSelectionListener((e) -> {
            if ((e.getAllSelectedItems().isEmpty() || e.getAllSelectedItems().size() > 1) && pnlMain.getSecondComponent() != null) 
                    pnlMain.removeComponent(pnlMain.getSecondComponent());
                
            else 
                pnlMain.setSecondComponent(new IPAddressManagerDashboard((RemoteObjectLight)e.getFirstSelectedItem().get(), wsBean));
        });
        
        HorizontalLayout lytFilter = new HorizontalLayout(txtFilter, btnSearch);
        lytFilter.setWidth(100, Unit.PERCENTAGE);
        lytFilter.setMargin(true);
        
        lytLeftPanel.addComponents(lytFilter, tblIps);
        lytLeftPanel.setExpandRatio(tblIps, 9.5f);
        lytLeftPanel.setExpandRatio(lytFilter, 0.5f);
        lytLeftPanel.setSizeFull();
        pnlMain.setFirstComponent(lytLeftPanel);
        
        Page.getCurrent().setTitle(String.format("Kuwaiba Open Network Inventory - IP Address Manager - [%s]", session.getUsername()));
    }
    
    @Override
    public void registerComponents() { }

    @Override
    public void unregisterComponents() { }

}
