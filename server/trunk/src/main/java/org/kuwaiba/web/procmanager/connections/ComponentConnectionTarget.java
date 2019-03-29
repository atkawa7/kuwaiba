/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.web.procmanager.connections;

import com.vaadin.event.ShortcutAction;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteQuery;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteSuggestion;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteSuggestionProvider;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteTextField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.kuwaiba.apis.web.gui.navigation.nodes.ChildrenProvider;
import org.kuwaiba.apis.web.gui.navigation.trees.BasicTree;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import java.util.Arrays;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.navigation.BasicIconGenerator;
import org.kuwaiba.apis.web.gui.navigation.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.navigation.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentConnectionTarget extends VerticalLayout {
    private final WebserviceBean webserviceBean;
    private BasicTree tree;
    private AutocompleteTextField txtFilter;
    private RemoteObjectLight selectedTargetDevice;
    private RemoteObjectLight root;
        
    public ComponentConnectionTarget(RemoteObjectLight root, WebserviceBean webserviceBean) {
        this.webserviceBean = webserviceBean;
        this.root = root;
        initializeComponent();                
    }
    
    public RemoteObjectLight getSelectedTargetDevice() {
        return selectedTargetDevice;
    }
    
    public List<RemoteObjectLight> getSelectItems() {
        List<RemoteObjectLight> selectedItems = new ArrayList();
        
        if (tree != null && tree.getSelectedItems() != null) {
            for (Object selectedItem : tree.getSelectedItems()) {
                if (selectedItem instanceof InventoryObjectNode)
                    selectedItems.add(((InventoryObjectNode) selectedItem).getObject());
            }
        }
        return selectedItems;
    }
    
    private void initializeComponent() {                
        txtFilter = new AutocompleteTextField();
        txtFilter.setWidth(100, Unit.PERCENTAGE);
        txtFilter.setPlaceholder("Search...");
        txtFilter.setMinChars(3);
        txtFilter.setDelay(500);
        txtFilter.setSuggestionProvider(new AutocompleteSuggestionProvider() {
            @Override
            public Collection<AutocompleteSuggestion> querySuggestions(AutocompleteQuery query) {
                try {
                    
                    List<RemoteObjectLight> suggestedObjects = webserviceBean.getSuggestedObjectsWithFilter(
                        query.getTerm(), 
                        15, 
                        Page.getCurrent().getWebBrowser().getAddress(),
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
        
        txtFilter.addSelectListener((e) -> {
            tree.resetTo(new InventoryObjectNode((RemoteObjectLight)e.getSuggestion().getData()));
        });
        
        
        Button btnSearch = new Button(VaadinIcons.SEARCH, (e) -> {
            
            if (txtFilter.getValue().length() < 3) {
                Notifications.showInfo("Please refine your search");
                return;
            }
            try {
                List<RemoteObjectLight> suggestedObjects = webserviceBean.getSuggestedObjectsWithFilter(
                    txtFilter.getValue(),
                    -1, 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                
                if (suggestedObjects.isEmpty())
                    Notifications.showInfo("Your search has 0 results");
                else
                    tree.resetTo(InventoryObjectNode.asNodeList(suggestedObjects));
                
                
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getLocalizedMessage());
            }
        });
        btnSearch.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        tree = new BasicTree(
                new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                        @Override
                        public List<RemoteObjectLight> getChildren(RemoteObjectLight c) {
                            try {
                                return webserviceBean.getObjectChildren(
                                    c.getClassName(), 
                                    c.getId(), 
                                    -1, 
                                    Page.getCurrent().getWebBrowser().getAddress(),
                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

                            } catch (ServerSideException ex) {
                                Notifications.showError(ex.getLocalizedMessage());
                                return new ArrayList<>();
                            }
                        }
                    }, new BasicIconGenerator(webserviceBean, ((RemoteSession) UI.getCurrent().getSession().getAttribute("session"))), 
                    new AbstractNode<RemoteObjectLight>(root != null ? root : new RemoteObjectLight(Constants.DUMMY_ROOT, "-1", "Navigation Root")) {
                        @Override
                        public AbstractAction[] getActions() { return new AbstractAction[0]; }

                        @Override
                        public void refresh(boolean recursive) { }
                });
        
        tree.setSelectionMode(Grid.SelectionMode.MULTI);
        /*
        tree.addSelectionListener((e) -> {
            
            int i = 0;  
            selectItems = new ArrayList();
//            if (e.getFirstSelectedItem().get() instanceof InventoryObjectNode)
//                selectedTargetDevice = (RemoteObjectLight)e.getFirstSelectedItem().get().getObject();
        });
        */
        
        HorizontalLayout lytFilter = new HorizontalLayout(txtFilter, btnSearch);
        lytFilter.setMargin(true);
        lytFilter.setSizeFull();
        
        addComponents(lytFilter, tree);
        setExpandRatio(tree, 9.5f);
        setExpandRatio(lytFilter, 0.5f);
        setSizeFull();
    }    

    public BasicTree getTree() {
        return tree;
    }

}
