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
package org.kuwaiba.web.modules.sync;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import java.util.Properties;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.web.modules.sync.miniapps.ConfigureSyncGroupMiniApp;

/**
 * Main view for the Synchronization Manager module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@CDIView("syncmanager")
public class SyncManagerComponent extends AbstractTopComponent {
    /**
     * View identifier
     */
    public static String VIEW_NAME = "syncmanager";
    
    /**
     * The backend bean
     */
    @Inject
    private WebserviceBean wsBean;


    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setStyleName("dashboards");
        Button btnLaunchMiniApp = new Button("Launch Mini App");
        btnLaunchMiniApp.addClickListener((e) -> {
            Properties defaultproperties = new Properties();
            defaultproperties.put("deviceId", "29390");
            defaultproperties.put("deviceClass", "MPLSRouter");
            ConfigureSyncGroupMiniApp miniApp = new ConfigureSyncGroupMiniApp(defaultproperties);
            miniApp.setWebserviceBean(wsBean);
            UI.getCurrent().addWindow(miniApp.launchDetached());
        });
        addComponent(btnLaunchMiniApp);
    }

    @Override
    public void registerComponents() { }

    @Override
    public void unregisterComponents() { }
}
