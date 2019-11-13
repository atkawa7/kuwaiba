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
import com.vaadin.flow.component.tabs.Tab;

/**
 * This class abstract a common tab for a dashboard to show
 * @author Jalbersson Guillermo Plazas {@literal <jallbersson.plazas@kuwaiba.org>}
 */
public abstract class AbstractTab extends Tab {
    private Div contentPage;

    public AbstractTab() {
        this.contentPage = new Div();
    }

    public void createContent(){}

    public Div getContentPage() {
        return contentPage;
    }

    public void setContentPage(Div contentPage) {
        this.contentPage = contentPage;
    }
}
