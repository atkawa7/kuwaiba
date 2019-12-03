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
package org.kuwaiba.web.modules.welcome;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import javax.inject.Inject;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.web.KuwaibaConst;
import org.kuwaiba.web.MainLayout;
import org.kuwaiba.web.modules.osp.dashboard.SimpleMapDashboardWidget;

/**
 * The welcome screen
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Route(value = KuwaibaConst.PAGE_WELCOME, layout = MainLayout.class)
@PageTitle(KuwaibaConst.TITLE_WELCOME)
public class WelcomeComponent extends VerticalLayout {
    @Inject
    private WebserviceBean webserviceBean;
    
    public WelcomeComponent() {
    }
        
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        SimpleMapDashboardWidget wdtMap = new SimpleMapDashboardWidget("Geolocated Buildings", webserviceBean);
        setSizeFull();
        add(wdtMap);
    }
}
