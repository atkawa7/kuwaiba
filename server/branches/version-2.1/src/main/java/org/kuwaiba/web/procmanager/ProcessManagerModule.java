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
package org.kuwaiba.web.procmanager;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import org.kuwaiba.apis.web.gui.modules.AbstractModule;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * The definition of the Process Manager module. This module allows to create and manage Processes and BPMN sections
 * @author Jalbersson Guillemo Plazas {@literal <jalbersson.plazas@kuwaiba.org>}
 */
public class ProcessManagerModule extends AbstractModule {

    public ProcessManagerModule(WebserviceBean wsBean, RemoteSession session) {
        super(wsBean, session);
    }

    @Override
    public String getName() {
        return "Process Manager";
    }

    @Override
    public String getDescription() {
        return "This module allows to create and manage Processes and BPMN sections";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>";
    }

    @Override
    public int getType() {
        return MODULE_TYPE_COMMERCIAL;
    }

    @Override
    public void attachToMenu(MenuBar menuBar) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void attachToMenu(Tabs tabs) {
        RouterLink routerLink = new RouterLink(null, TimelineView.class);
        routerLink.add(VaadinIcon.CHART_GRID.create());
        routerLink.add(getName());
        tabs.add(new Tab(routerLink));
    }

    @Override
    public Component open() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
