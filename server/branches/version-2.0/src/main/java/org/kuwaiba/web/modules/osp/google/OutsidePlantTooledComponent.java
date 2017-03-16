 /*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.osp.google;

import com.vaadin.event.FieldEvents;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.modules.EmbeddableComponent;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.web.custom.core.AbstractTooledComponent;

/**
 * The tooled component that wrapped the map of Google Map
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class OutsidePlantTooledComponent extends AbstractTooledComponent implements EmbeddableComponent {
    public static final String ACTION_NEW_CAPTION = "New";
    public static final String ACTION_OPEN_CAPTION = "Open";
    public static final String ACTION_SAVE_CAPTION = "Save";
    public static final String ACTION_DELETE_CAPTION = "Delete";
    public static final String ACTION_SELECT_CAPTION = "Select";
    public static final String ACTION_CONNECT_CAPTION = "Connect";
    public static final String ACTION_POLYGON_CAPTION = "Draw polygon";
//    public static final String ACTION_EDIT_CAPTION = "Edit";
    public static final String ACTION_SHOW_NODE_LABELS = "Show/hide node labels";
    public static final String ACTION_SHOW_CONNECTION_LABELS = "Show/hide connection labels";
    public static final String ACTION_SHOW_POLYGON_LABELS = "Show/hide polygon labels";
    public static final String ACTION_CLEAN_CAPTION = "Clean";
    public static final String ACTION_FILTER_CAPTION = "Filter";
    public static final String ACTION_SEARCH_CAPTION = "Search";
    
    TopComponent parentComponent;
    
    List<AbstractAction> actions;
    List<Button> btnsActions;
    
    ComboBox cboSearch;
    Button btnSearch;    
    
    private ToolBarSize toolBarSize = AbstractTooledComponent.ToolBarSize.NORMAL;
    
    public OutsidePlantTooledComponent(TopComponent parentComponent) {
        super(new AbstractAction[0], 
                AbstractTooledComponent.TOOLBAR_ORIENTATION_HORIZONTAL, 
                AbstractTooledComponent.ToolBarSize.NORMAL);
        
        this.parentComponent = parentComponent;
                
        initActions();
        initToolbarLayout();
        toggleButtons(true);
        
        setSizeFull();
    }    
    
    private void initActions() {
        actions = new ArrayList();
        
        actions.add(new AbstractActionImpl(ACTION_NEW_CAPTION, 
                new ThemeResource("img/mod_osp_icon_add.png")));
        
        actions.add(new AbstractActionImpl(ACTION_OPEN_CAPTION, 
                new ThemeResource("img/mod_osp_icon_open.png")));
        
        actions.add(new AbstractActionImpl(ACTION_SAVE_CAPTION, 
                new ThemeResource("img/mod_osp_icon_save.png")));
                        
        actions.add(new AbstractActionImpl(ACTION_SHOW_NODE_LABELS, 
                new ThemeResource("img/mod_osp_icon_hide_node_labels.png")));
        
        actions.add(new AbstractActionImpl(ACTION_SHOW_CONNECTION_LABELS, 
                new ThemeResource("img/mod_osp_icon_hide_connection_labels.png")));
        
        actions.add(new AbstractActionImpl(ACTION_SHOW_POLYGON_LABELS, 
                new ThemeResource("img/mod_osp_icon_hide_polygon_labels.png")));
                
        actions.add(new AbstractActionImpl(ACTION_SELECT_CAPTION,
                new ThemeResource("img/mod_osp_icon_select.png")));
        
        actions.add(new AbstractActionImpl(ACTION_CONNECT_CAPTION, 
                new ThemeResource("img/mod_osp_icon_connect.png")));
        
        actions.add(new AbstractActionImpl(ACTION_POLYGON_CAPTION, 
                new ThemeResource("img/mod_osp_icon_polygon.png")));
        
//        actions.add(new AbstractActionImpl(ACTION_EDIT_CAPTION, 
//                new ThemeResource("img/mod_osp_icon_edit.png")));
        
        actions.add(new AbstractActionImpl(ACTION_CLEAN_CAPTION, 
                new ThemeResource("img/mod_osp_icon_clean.png")));
        
        actions.add(new AbstractActionImpl(ACTION_FILTER_CAPTION, 
                new ThemeResource("img/mod_osp_icon_filter.png")));
    }
    
    private void initBtnAction(final Button btnAction, AbstractAction action) {
        btnAction.setDescription(action.getCaption());
        btnAction.setStyleName("v-button-icon-only");
        btnAction.setWidth(toolBarSize.size() + 2 * TOOLBAR_HORIZONTAL_PADDING, Unit.PIXELS);
        btnAction.setHeight(toolBarSize.size() + 2 * TOOLBAR_VERTICAL_PADDING, Unit.PIXELS);
        btnAction.addClickListener(new Button.ClickListener() {
            
            @Override
            public void buttonClick(Button.ClickEvent event) {
                action.actionPerformed(btnAction, event);
            }
        });
    }
    
    private void initToolbarLayout() {
        btnsActions = new ArrayList();
        AbstractOrderedLayout toolbarLayout = (AbstractOrderedLayout) getToolbarLayout();
        
        for (final AbstractAction action : actions) {
            Button btnAction = new Button(action.getIcon());
            initBtnAction(btnAction, action);
            toolbarLayout.addComponent(btnAction);
        }
                
        cboSearch = new ComboBox();
        cboSearch.setDescription("Search");
        cboSearch.addFocusListener(new FieldEvents.FocusListener() {

            @Override
            public void focus(FieldEvents.FocusEvent event) {
                cboSearch.setValue(null);
                getTopComponent().getEventBus().post(cboSearch);
            }
        });
        cboSearch.setNullSelectionAllowed(true);
        cboSearch.setValue(null);
        cboSearch.setNullSelectionItemId(null);
        cboSearch.setHeight(toolBarSize.size() + 2 * TOOLBAR_VERTICAL_PADDING, Unit.PIXELS);
                                
        AbstractAction searchAction = new AbstractAction(
                ACTION_SEARCH_CAPTION, 
                new ThemeResource("img/mod_osp_icon_search.png")) {
            
            @Override
            public void actionPerformed(Object sourceComponent, Object targetObject) {
                getTopComponent().getEventBus().post(cboSearch);
            }
        };
        btnSearch = new Button(searchAction.getIcon());
        initBtnAction(btnSearch, searchAction);
        
        toolbarLayout.addComponent(cboSearch);
        toolbarLayout.addComponent(btnSearch);
    }
        
    public void toggleButtons(boolean enabled) {
        for (Button btnAction : btnsActions) {
            if (!btnAction.getDescription().equals(ACTION_NEW_CAPTION) 
                    || !btnAction.getDescription().equals(ACTION_OPEN_CAPTION)) {
                
                btnAction.setReadOnly(!enabled);
            }
        }
    }
        
    public void register() {
        if (parentComponent != null)
            parentComponent.getEventBus().register(this);
    }
    
    public void unregister() {
        if (parentComponent != null)
            parentComponent.getEventBus().unregister(this);
    }

    @Override
    public TopComponent getTopComponent() {
        return parentComponent;
    }
    
    private class AbstractActionImpl extends AbstractAction {        
        
        public AbstractActionImpl(String caption, Resource icon) {
            super(caption, icon);
        }

        @Override
        public void actionPerformed(Object sourceComponent, Object targetObject) {
            getTopComponent().getEventBus().post(sourceComponent);
        }
    };
}
