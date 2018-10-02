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

package org.kuwaiba.web.modules.osp;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.dashboards.widgets.PropertySheetDashboardWidget;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.web.IndexUI;
import org.kuwaiba.web.modules.osp.dashboard.OutsidePlantDashboard;
import org.kuwaiba.web.modules.osp.dashboard.OutsidePlantViewDashboardWidget;

/**
 * Main window of the Outside Plant module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@CDIView("osp")
public class OutsidePlantComponent extends AbstractTopComponent implements View {
    /**
     * The name of the view
     */
    public static String VIEW_NAME = "osp";
    @Inject
    private WebserviceBean wsBean;
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
//        setStyleName("dashboards");
//        addStyleName("navtree");
        
        MenuBar mnuMain = ((IndexUI)getUI()).getMainMenu();

        addComponent(mnuMain);
        HorizontalSplitPanel pnlSplitMain = new HorizontalSplitPanel(new PropertySheetDashboardWidget(wsBean), 
                new OutsidePlantViewDashboardWidget(wsBean));
        
        pnlSplitMain.setSizeFull();
        pnlSplitMain.setSplitPosition(30, Unit.PERCENTAGE);
        
        addComponent(pnlSplitMain);
        
        setExpandRatio(mnuMain, 0.3f);
        setExpandRatio(pnlSplitMain, 9.7f);

        setSizeFull();
    }
    
    @Override
    public void registerComponents() { }

    @Override
    public void unregisterComponents() { }

}
