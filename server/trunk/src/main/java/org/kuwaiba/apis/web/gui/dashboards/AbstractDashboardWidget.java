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

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.kuwaiba.web.modules.servmanager.ServiceManagerComponent;

/**
 * A small embeddable component that can be inserted into an AbstractDashboard. A DashboardWidget has two "faces": 
 * A cover that shows a summary or simply a title, and content, with the actual information to be shown. This can be seen as a 
 * Tile in a MS Windows Metro interface
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
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
     * the component where this dashboard its been displayed
     */
    protected Component parentComponent;
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
    
    public AbstractDashboardWidget(String title, Component parentComponent) {
        this.colSpan = 1;
        this.rowSpan = 1;
        this.title = title;
        this.parentComponent = parentComponent;
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
     * Flips the current displayed component. That is, instead of the cover component, the component widget will be displayed
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
    
    /**
     * Displays the contents of the content widget in a separate modal window
     */
    public void launch() {
        if (contentComponent != null) {
            Window wnwContent = new Window(title);
            wnwContent.setModal(true);
            wnwContent.setContent(contentComponent);
            getUI().addWindow(wnwContent);
        }
    }
    
    /**
     * Displays the contents of the content widget replacing the whole dashboard space
     */
    public void swap() {
        if (contentComponent != null) {
            Component oldContent = ((ServiceManagerComponent)parentComponent).getPnlMain().getSecondComponent();
        
            Button btnBack = new Button(VaadinIcons.CHEVRON_LEFT, click -> {
                    ((ServiceManagerComponent)parentComponent).getPnlMain().setSecondComponent(oldContent);
                });
            btnBack.setCaption(title);
            btnBack.addStyleNames(ValoTheme.BUTTON_BORDERLESS , "v-button-borderless-back");

            VerticalLayout content =  new VerticalLayout(btnBack, contentComponent);
            ((ServiceManagerComponent)parentComponent).getPnlMain().setSecondComponent(content);
        }
    }
    
    /**
     * Creates the cover component. Note that implementors must set the coverComponent attribute and manage the respective events
     * The default implementation creates a colored rectangle displaying the title of the widget without any style. For simple widgets it's recommended to use 
     * this implementation (that is, call super.createCover()) and set a style afterwards. 
     */
    public void createCover() { 
        VerticalLayout lytContactsWidgetCover = new VerticalLayout();
        Label lblText = new Label(title);
        lblText.setStyleName("text-bottomright");
        lytContactsWidgetCover.addLayoutClickListener((event) -> {
            if (event.getButton() == MouseEventDetails.MouseButton.LEFT) {
                this.createContent();
                launch();
            }
        });
        
        lytContactsWidgetCover.addComponent(lblText);
        lytContactsWidgetCover.setSizeFull();
        
        this.coverComponent = lytContactsWidgetCover;
        addComponent(coverComponent);
    }
    public abstract void createContent();
    
    public enum ActiveContent {
        CONTENT_COVER,
        CONTENT_CONTENT
    }
   
}
