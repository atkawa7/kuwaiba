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

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.web.gui.notifications.Notifications;

/**
 * A small embeddable component that can be inserted into an AbstractDashboard. A DashboardWidget has two "faces": 
 * A cover that shows a summary or simply a title, and content, with the actual information to be shown. This can be seen as a 
 * Tile in a MS Windows Metro interface in most of the cases.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractDashboardWidget extends VerticalLayout {
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
    protected AbstractDashboard parentDashboard;
    /**
     * Reference to the event bus so the widget can share information with other widgets
     */
    protected DashboardEventBus eventBus;
    /**
     * Dashboard widget title
     */
    protected String title;
    public AbstractDashboardWidget(String title) {
        this.title = title;
        this.activeContent = ActiveContent.CONTENT_COVER;
        this.setMargin(true);
    }
    
    public AbstractDashboardWidget(String title, AbstractDashboard parentDashboard) {
        this(title);
        this.parentDashboard = parentDashboard;
    }

    public AbstractDashboardWidget(String title, DashboardEventBus eventBus) {
        this.eventBus = eventBus;
    }
    
    /**
     * Loads the configuration (if any) of the widget. In most cases, the configuration is extracted from configuration variables.
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException If the minimum configuration parameters for the widget to work are not available.
     */
    protected void loadConfiguration() throws InvalidArgumentException { }
        
    public ActiveContent getActiveContent() {
        return activeContent;
    }

    public void setActiveContent(ActiveContent activeContent) {
        this.activeContent = activeContent;
    }
    
    public final void fireEvent(DashboardEventListener.DashboardEvent event) {
        if (eventBus != null)
            eventBus.notifySubscribers(event);
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
            Window wdwContent = new Window(title);
            wdwContent.setModal(true);
            wdwContent.setWidth(90, Unit.PERCENTAGE);
            wdwContent.setContent(contentComponent);
            wdwContent.center();
            UI.getCurrent().addWindow(wdwContent);
        } else Notifications.showError("The content component has not been set. Please check your createContent method");
    }

    /**
     * Displays the contents of the content widget replacing the whole dashboard space
     */
    public void swap() {
        if (contentComponent != null && parentDashboard != null) {
            Component formerContent = parentDashboard.getContent();
        
            Button btnBack = new Button(VaadinIcons.CHEVRON_LEFT, click -> {
                    parentDashboard.setContent(formerContent);
                });
            btnBack.setCaption(title);
            btnBack.addStyleNames(ValoTheme.BUTTON_BORDERLESS , "v-button-borderless-back");

            VerticalLayout content =  new VerticalLayout(btnBack, contentComponent);
            parentDashboard.setContent(content);
        }else 
            getUI().addWindow(new Window("Error", new Label("The parent or content components have not been set. Please check your scene constructor or the createContent method")));
    }
    
    /**
     * Creates the cover component. Note that implementors must set the coverComponent attribute and manage the respective events
     * The default implementation creates a colored rectangle displaying the title of the widget without any style. For simple widgets it's recommended to use 
     * this implementation (that is, call super.createCover()) and set a style afterwards. 
     */
    public void createCover() { 
        VerticalLayout lytDefaultWidgetCover = new VerticalLayout();
        Label lblText = new Label(title);
        lblText.setStyleName("text-bottomright");
        lytDefaultWidgetCover.addLayoutClickListener((event) -> {
            if (event.getButton() == MouseEventDetails.MouseButton.LEFT) {
                this.createContent();
                launch();
            }
        });
        
        lytDefaultWidgetCover.addComponent(lblText);
        lytDefaultWidgetCover.setSizeFull();
        
        this.coverComponent = lytDefaultWidgetCover;
        addComponent(coverComponent);
    }
    public abstract void createContent();
    
    public enum ActiveContent {
        CONTENT_COVER,
        CONTENT_CONTENT
    }
   
}
