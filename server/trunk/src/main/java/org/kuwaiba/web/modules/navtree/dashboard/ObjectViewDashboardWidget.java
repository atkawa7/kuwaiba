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

package org.kuwaiba.web.modules.navtree.dashboard;

import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboard;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.views.AbstractView;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * Implements an object view. That is, a view that show the direct children of an object that the connections between them
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ObjectViewDashboardWidget extends AbstractDashboardWidget {
    /**
     * Reference to the currently selected object
     */
    private RemoteObjectLight selectedObject;
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;
    public ObjectViewDashboardWidget(AbstractDashboard parentDashboard, RemoteObjectLight selectedObject, WebserviceBean wsBean) {
        super(String.format("Object View of %s", selectedObject), parentDashboard);
        this.wsBean = wsBean;
        this.selectedObject = selectedObject;
        this.createCover();
    }

    @Override
    public void createCover() {
        VerticalLayout lytSpecialChildrenWidgetCover = new VerticalLayout();
        Label lblText = new Label("Object View");
        lblText.setStyleName("text-bottomright");
        lytSpecialChildrenWidgetCover.addLayoutClickListener((event) -> {
            if (event.getButton() == MouseEventDetails.MouseButton.LEFT) {
                this.createContent();
                swap();
            }
        });
        
        lytSpecialChildrenWidgetCover.addComponent(lblText);
        lytSpecialChildrenWidgetCover.setSizeFull();
        lytSpecialChildrenWidgetCover.setStyleName("dashboard_cover_widget-darkpink");
        this.coverComponent = lytSpecialChildrenWidgetCover;
        addComponent(coverComponent);
    }

    @Override
    public void createContent() {
        VerticalLayout lytContent = new VerticalLayout();
        try {
            AbstractView objectViewInstance = PersistenceService.getInstance().getViewFactory().
                    createViewInstance("org.kuwaiba.web.modules.navtree.views.ObjectView"); //NOI18N
            objectViewInstance.buildWithBusinessObject(selectedObject);
            lytContent.addComponent(objectViewInstance.getAsComponent());
        } catch (InstantiationException | InvalidArgumentException ex) {
            Notifications.showError(String.format("Object view could not be launched: %s", ex.getLocalizedMessage()));
        }
        
        this.contentComponent = lytContent;
    }
}
