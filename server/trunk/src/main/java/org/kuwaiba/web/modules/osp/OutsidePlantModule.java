 /*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.osp;

import com.google.common.eventbus.EventBus;
import com.vaadin.navigator.View;
import com.vaadin.server.ThemeResource;
import org.kuwaiba.apis.web.gui.modules.AbstractModule;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 * The definition of the OSP module.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class OutsidePlantModule extends AbstractModule {
    /**
     * The actual component
     */
    private OutsidePlantComponent gisView;
    /**
     * Reference to the WebserviceBean
     */
    private WebserviceBean wsBean;
    /**
     * Reference to the current session
     */
    private RemoteSession session;
    
    public OutsidePlantModule(EventBus eventBus, WebserviceBean wsBean, RemoteSession session) {
        super(eventBus);
        this.wsBean = wsBean;
        this.session = session;
        icon = new ThemeResource("img/mod_icon_osp.png");
    }
    
    @Override
    public String getName() {
        return "Outside Plant Module";
    }

    @Override
    public String getDescription() {
        return "Manage your outside plant and locate your assets on a map";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS";
    }

    @Override
    public int getType() {
        return MODULE_TYPE_FREE_CORE;
    }

    @Override
    public String getMenuEntry() {
        return "Tools/Advanced";
    }

    @Override
    public View open() {
        gisView = new OutsidePlantComponent(eventBus, wsBean, session);
        //Register components in the event bus
        gisView.registerComponents();
        return gisView;
    }

    @Override
    public void close() {
        //Unregister components from the event bus
        gisView.unregisterComponents();
    }
}
