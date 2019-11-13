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
package org.kuwaiba.apis.web.gui.dashboards;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kuwaiba.apis.web.gui.dashboards.widgets.SimpleLabelDashboardWidget;

/**
 * This will hold all the tabs to show the Kuwaiba Widgets
 * @author Jalbersson Guillermo Plazas {@literal <jalbersson.plazas@kuwaiba.org>}
 */

public class TabsHolder extends VerticalLayout {
    private Tabs allTabs;
    private SimpleLabelDashboardWidget title;
    private final List<AbstractTab> widgetTabs;
    Map<Tab, Div> tabsToPages = new HashMap<>();
    
    /**
     * Default constructor
     * @param title The title of the Tab
     * @param subtitle
     * @param widgetTabs
     */
    public TabsHolder(String title, String subtitle, List<AbstractTab> widgetTabs) {
        this.title = new SimpleLabelDashboardWidget(title, subtitle);
        //this.addComponent(dashboardLayout);
        this.widgetTabs = widgetTabs;
        add(getTitle());
        this.setSizeFull();
        setAllTabs(new Tabs());
        createTabs();
    }

    public SimpleLabelDashboardWidget getTitle() {
        return title;
    }

    public void setTitle(SimpleLabelDashboardWidget title) {
        this.title = title;
    }

    public List<AbstractTab> getTabs() {
        return widgetTabs;
    }

    public Tabs getAllTabs() {
        return allTabs;
    }

    public void setAllTabs(Tabs allTabs) {
        this.allTabs = allTabs;
    }

    public Map<Tab, Div> getTabsToPages() {
        return tabsToPages;
    }

    public void setTabsToPages(Map<Tab, Div> tabsToPages) {
        this.tabsToPages = tabsToPages;
    }

    public List<AbstractTab> getWidgetTabs() {
        return widgetTabs;
    }

    public void createTabs(){
        Div pages = new Div();
        for(AbstractTab tab : getWidgetTabs()){
            getTabsToPages().put(tab, tab.getContentPage());
            getAllTabs().add(tab);
            pages.add(tab.getContentPage());
        }
        
        getAllTabs().addSelectedChangeListener(event -> {
            for(AbstractTab tab : getWidgetTabs()){
                if(event.getSource().getSelectedTab() == tab)
                    tab.getContentPage().setVisible(true);
                else
                    tab.getContentPage().setVisible(false);
            }
        });
        
        add(getAllTabs());
        add(pages);
    }
}
