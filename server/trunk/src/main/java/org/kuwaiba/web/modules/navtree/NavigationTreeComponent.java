/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.web.modules.navtree;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
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
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.apis.web.gui.navigation.AbstractNode;
import org.kuwaiba.apis.web.gui.navigation.ChildrenProvider;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.apis.web.gui.navigation.DynamicTree;
import org.kuwaiba.apis.web.gui.navigation.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.navigation.SimpleIconGenerator;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.web.IndexUI;
import org.kuwaiba.web.modules.navtree.dashboard.NavigationTreeDashboard;

/**
 * The main component of the Navigation Tree module.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@CDIView("navtree")
class NavigationTreeComponent extends AbstractTopComponent {
    /**
     * View identifier
     */
    public static String VIEW_NAME = "navtree";
    /**
     * Text field to filter the services
     */
    private AutocompleteTextField txtFilter;
    /**
     * Layout for all the graphic components on the left side
     */
    private VerticalLayout lytLeftPanel;
    /**
     * The backend bean
     */
    @Inject
    private WebserviceBean wsBean;
    /**
     * The current session
     */
    private RemoteSession session;
    /**
     * The tree
     */
    private DynamicTree tree;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setStyleName("dashboards");
        addStyleName("misc");
        addStyleName("navtree");
        
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
        this.txtFilter.setSuggestionProvider(new AutocompleteSuggestionProvider() {
            @Override
            public Collection<AutocompleteSuggestion> querySuggestions(AutocompleteQuery query) {
                try {
                    
                    List<RemoteObjectLight> suggestedObjects = wsBean.getSuggestedObjectsWithFilter(query.getTerm(), Page.getCurrent().getWebBrowser().getAddress(),
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
            this.tree.resetTo(new InventoryObjectNode((RemoteObjectLight)e.getSuggestion().getData()));
        });
        

        this.tree = new DynamicTree(
                new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                        @Override
                        public List<RemoteObjectLight> getChildren(RemoteObjectLight c) {
                            try {
                                return wsBean.getObjectChildren(c.getClassName(), 
                                    c.getId(), -1, Page.getCurrent().getWebBrowser().getAddress(),
                                    session.getSessionId());

                            } catch (ServerSideException ex) {
                                Notifications.showError(ex.getLocalizedMessage());
                                return new ArrayList<>();
                            }
                        }
                    }, new SimpleIconGenerator(wsBean, session), 
                    new AbstractNode<RemoteObjectLight>(new RemoteObjectLight(Constants.DUMMY_ROOT, -1, "Navigation Root")) {
                        @Override
                        public AbstractAction[] getActions() { return new AbstractAction[0]; }

                        @Override
                        public void refresh(boolean recursive) { }
                });
        
        this.tree.addSelectionListener((e) -> {
            if ((e.getAllSelectedItems().isEmpty() || e.getAllSelectedItems().size() > 1) && pnlMain.getSecondComponent() != null) 
                    pnlMain.removeComponent(pnlMain.getSecondComponent());
                
            else {
                if (e.getFirstSelectedItem().get() instanceof InventoryObjectNode)
                    pnlMain.setSecondComponent(
                            new NavigationTreeDashboard((RemoteObjectLight)e.getFirstSelectedItem().get().getObject(), wsBean));
                else {
                    if (pnlMain.getSecondComponent() != null)
                        pnlMain.removeComponent(pnlMain.getSecondComponent());
                }
            }
        });
        lytLeftPanel.setMargin(true);
        lytLeftPanel.addComponents(txtFilter, tree);
        lytLeftPanel.setExpandRatio(tree, 9.5f);
        lytLeftPanel.setExpandRatio(txtFilter, 0.5f);
        lytLeftPanel.setSizeFull();
        pnlMain.setFirstComponent(lytLeftPanel);
        
        Page.getCurrent().setTitle(String.format("Kuwaiba Open Network Inventory - Navigation Tree - [%s]", session.getUsername()));
    }
    
        
    @Override
    public void registerComponents() {
        //tree.register();
    }

    @Override
    public void unregisterComponents() {
        //tree.unregister();
    }
}
