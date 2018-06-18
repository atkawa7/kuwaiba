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
package org.kuwaiba.web.modules.servmanager.dashboard;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;

/**
 * Just a cover
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class DummyDashboardWidget extends AbstractDashboardWidget {
    
    private String style;
    
    public DummyDashboardWidget(String title, String style) {
        super(title);
        this.style = style;
        this.createCover();
    }
    
    @Override
    public void createCover() {
        VerticalLayout lytViewsWidgetCover = new VerticalLayout();
        Label lblText = new Label(title);
        lblText.setStyleName("text-bottomright");
        
        lytViewsWidgetCover.addLayoutClickListener((event) -> {
            createContent();
            launch();
        });
        
        lytViewsWidgetCover.addComponent(lblText);
        lytViewsWidgetCover.setSizeFull();
        lytViewsWidgetCover.setStyleName(style);
        this.coverComponent = lytViewsWidgetCover;
        addComponent(coverComponent);
    }

    @Override
    public void createContent() {
        this.contentComponent = new Label("This is a dummy dashboard widget");
    }
}
