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
package org.kuwaiba.apis.web.gui.dashboards;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * A small embeddable component that can be inserted into an AbstractDashboard. A DashboardWidget has two "faces": 
 * A cover that shows a summary or simply a title, and content, with the actual information to be shown. This can be seen as a 
 * Tile in a MS Windows Metro interface
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public abstract class AbstractDashboardWidget extends VerticalLayout {
    /**
     * The number of consecutive horizontal cells this dashboard widget will use
     */
    protected int colSpan;
    /**
     * The number of consecutive vertical cells this dashboard widget will use
     */
    protected int rowSpan;
    /**
     * The active content to be displayed (cover or content)
     */
    protected ActiveContent activeContent;
    /**
     * The component with the cover information
     */
    protected Component coverComponent;
    /**
     * The component with the detailed information (actual content)
     */
    protected Component contentComponent;
    /**
     * Dashboard widget title
     */
    protected String title;
    public AbstractDashboardWidget(String title) {
        this.colSpan = 1;
        this.rowSpan = 1;
        this.title = title;
        this.activeContent = ActiveContent.CONTENT_COVER;
        this.setMargin(true);
    }


    public int getColSpan() {
        return colSpan;
    }

    public void setColSpan(int colSpan) {
        this.colSpan = colSpan;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }

    public ActiveContent getActiveContent() {
        return activeContent;
    }

    public void setActiveContent(ActiveContent activeContent) {
        this.activeContent = activeContent;
    }

    /**
     * Flips the current displayed component
     */
    public void flip() {
        if (this.activeContent == ActiveContent.CONTENT_COVER) {
            replaceComponent(coverComponent, contentComponent);
            activeContent = ActiveContent.CONTENT_CONTENT;
        } else {
            replaceComponent(contentComponent, coverComponent);
            activeContent = ActiveContent.CONTENT_COVER;
        }
    }
    
    public void launch() {
        Window wnwContent = new Window(title);
        wnwContent.setModal(true);
        wnwContent.setContent(contentComponent);
        getUI().addWindow(wnwContent);
    }
    
    /**
     * Creates the cover component. Note that implementors must set the coverComponent attribute and manage the respective events
     */
    public abstract void createCover();
    public abstract void createContent();
    
    public enum ActiveContent {
        CONTENT_COVER,
        CONTENT_CONTENT
    }
}
