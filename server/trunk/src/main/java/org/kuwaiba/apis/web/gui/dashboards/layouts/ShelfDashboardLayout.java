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

package org.kuwaiba.apis.web.gui.dashboards.layouts;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;

/**
 * This layout resembles a shelf. It will split the dashboard in two (horizontally). The left side is intended
 * to show a single widget, while the one on the right will display a pile of tiles with unflipped widgets
 * 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ShelfDashboardLayout extends VerticalLayout {
    /**
     * The layout that will hold the pile of unflipped widgets
     */
    private VerticalLayout lytRightPanel;
    /**
     * The main panel
     */
    private HorizontalSplitPanel pnlMain;
    
    public ShelfDashboardLayout(String title, String subtitle) {
        setSizeFull();
        pnlMain = new HorizontalSplitPanel();
        pnlMain.setSplitPosition(70, Unit.PERCENTAGE);
        pnlMain.setLocked(true);
        pnlMain.setSizeFull();
        lytRightPanel = new VerticalLayout();
        lytRightPanel.setWidth(100, Unit.PERCENTAGE);
        //lytRightPanel.setHeightUndefined();
        
        pnlMain.setSecondComponent(lytRightPanel);
        Label lblTitle = new Label(String.format("<b>%s</b><h2>%s</h2>", subtitle, title), ContentMode.HTML);
        addComponents(lblTitle, pnlMain);
        setExpandRatio(pnlMain, 9.4f);
        setExpandRatio(lblTitle, 0.6f);
    }
    
    public void addToPile (AbstractDashboardWidget aDashboardWidget) {
        lytRightPanel.addComponent(aDashboardWidget);
    }
    
    public void removeFromPile (AbstractDashboardWidget aDashboardWidget) {
        lytRightPanel.removeComponent(aDashboardWidget);
    }
    
    public void setMainDashboardWidget (AbstractDashboardWidget aDashboardwidget) {
        pnlMain.setFirstComponent(aDashboardwidget);
    }
}
