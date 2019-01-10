/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.apis.web.gui.dashboards.layouts;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;

/**
 * This layout takes its name because it resembles the way a theater looks like from upside: 
 * A big screen at the top, and the chairs are a bunch of small rectangles below. This layout 
 * is aimed to present the information with a big dashboard widget on top (usually used to display charts) and small ones below.
 * functionality of regular layouts
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class TheaterDashboardLayout extends VerticalLayout {
    /**
     * A panel that will contain the largest dashboard widget (the "screen") 
     */
    private HorizontalLayout lytScreen;
    /**
     * A grid that will contain the "chair" dashboard widgets
     */
    private GridLayout lytChairs;
    /**
     * Default constructor
     * @param lowLevelColumns Number of columns the lower part of the "theater chairs" will have
     * @param lowLevelRows  Number of rows the lower part of the "theater chairs" will have
     */
    public TheaterDashboardLayout(int lowLevelColumns, int lowLevelRows) {
        this.lytScreen = new HorizontalLayout();
        this.lytScreen.setSizeFull();
        
        this.lytChairs = new GridLayout(lowLevelColumns, lowLevelRows);
        this.lytChairs.setWidth(100, Unit.PERCENTAGE);
        this.lytChairs.setHeightUndefined();
        this.lytChairs.setSpacing(true);
        
        VerticalSplitPanel verticalSplitPanel = new VerticalSplitPanel();
        verticalSplitPanel.setSplitPosition(50, Unit.PERCENTAGE);
        verticalSplitPanel.setFirstComponent(this.lytScreen);
        verticalSplitPanel.setSecondComponent(this.lytChairs);
        
        this.setSizeFull();
        this.addComponents(verticalSplitPanel);
        this.setExpandRatio(verticalSplitPanel, 5);
    }
    
    /**
     * Sets the "screen" dashboard widget
     * @param screenWidget The component to be embedded
     */
    public void setScreenWidget(AbstractDashboardWidget screenWidget) {
        if (lytScreen.getComponentCount() != 0)
            this.lytScreen.replaceComponent(this.lytScreen.getComponent(0), screenWidget);
        else
            this.lytScreen.addComponent(screenWidget);
        
        this.lytScreen.setComponentAlignment(screenWidget, Alignment.TOP_CENTER);
    }
    
    /**
     * Puts a chair widget in a particular position of the lower grid. If the position is already been used, an {@link GridLayout.OverlapsException}.
     * @param column The column where the widget will be located
     * @param row The row where the widget will be located
     * @param chairWidget The widget itself 
     */
    public void setChairWidget(int column, int row, AbstractDashboardWidget chairWidget){
        this.lytChairs.addComponent(chairWidget, column, row);
    }
    
}
