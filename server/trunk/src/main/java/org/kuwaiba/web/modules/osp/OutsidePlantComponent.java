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

package org.kuwaiba.web.modules.osp;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.grid.ItemClickListener;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.dashboards.DashboardEventBus;
import org.kuwaiba.apis.web.gui.dashboards.widgets.NavigationTreeDashboardWidget;
import org.kuwaiba.apis.web.gui.dashboards.widgets.PropertySheetDashboardWidget;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.apis.web.gui.navigation.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.navigation.trees.BasicTree;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.IndexUI;
import org.kuwaiba.web.modules.osp.dashboard.FtthOspNodeInternalView;
import org.kuwaiba.web.modules.osp.dashboard.FtthPhysicalPath;
import org.kuwaiba.web.modules.osp.dashboard.OutsidePlantViewDashboardWidget;

/**
 * Main window of the Outside Plant module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@CDIView("osp")
public class OutsidePlantComponent extends AbstractTopComponent {
    /**
     * The name of the view
     */
    public static String VIEW_NAME = "osp"; //NOI18N
    /**
     * Reference to the backend bean
     */
    @Inject
    private WebserviceBean wsBean;
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        RemoteSession remoteSession = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
        addStyleName("navtree");
        
        MenuBar mnuMain = ((IndexUI)getUI()).getMainMenu();

        addComponent(mnuMain);
        
        DashboardEventBus eventBus = new DashboardEventBus();
        
        PropertySheetDashboardWidget propertySheetWidget = new PropertySheetDashboardWidget(wsBean);
        eventBus.addSubscriber(propertySheetWidget);
                
        NavigationTreeDashboardWidget navTreeWidget = new NavigationTreeDashboardWidget(eventBus, wsBean);
        VerticalLayout lytExplorer = new VerticalLayout(navTreeWidget, propertySheetWidget);
        lytExplorer.setExpandRatio(navTreeWidget, 7);
        lytExplorer.setExpandRatio(propertySheetWidget, 3);
        
        lytExplorer.setSizeFull();
        
        HorizontalSplitPanel pnlSplitMain = new HorizontalSplitPanel(lytExplorer, 
                new OutsidePlantViewDashboardWidget(eventBus, wsBean));
        
        pnlSplitMain.setSizeFull();
        pnlSplitMain.setSplitPosition(25, Unit.PERCENTAGE);
        
        addComponent(pnlSplitMain);
        
        setExpandRatio(mnuMain, 0.3f);
        setExpandRatio(pnlSplitMain, 9.7f);

        setSizeFull();
        
        for (int i = 0; i < navTreeWidget.getComponentCount(); i++) {
            if (navTreeWidget.getComponent(i) instanceof BasicTree) {
                BasicTree basicTree = (BasicTree) navTreeWidget.getComponent(i);
                basicTree.addItemClickListener(new Tree.ItemClickListener() {
                    @Override
                    public void itemClick(Tree.ItemClick event) {
                        InventoryObjectNode inventoryObjectNode = (InventoryObjectNode) event.getItem();
                        RemoteObjectLight object = inventoryObjectNode.getObject();
                        final String CLASS_OPTICAL_PORT = "OpticalPort"; //NOI18N
                        final List<String> CLASSES = Arrays.asList("FiberSplitter", "Manhole", "SpliceBox", "Building", "CTO");//NOI18N
                        final String ACTION_SHOW_PHYSICAL_PATH = "Show Physical Path";
                        final String ACTION_FTTH_INTERNAL_VIEW = "FTTH Internal View";
                        
                        if (CLASS_OPTICAL_PORT.equals(object.getClassName())) {
                            Grid<String> grdActions = new Grid();
                            grdActions.addColumn(String::toString).setCaption("Actions");
                            grdActions.setItems(ACTION_SHOW_PHYSICAL_PATH);
                            grdActions.addStyleName(ValoTheme.TABLE_BORDERLESS);
                            Component oldComponent = lytExplorer.getComponent(1);
                            lytExplorer.replaceComponent(oldComponent, grdActions);
                            grdActions.addItemClickListener(new ItemClickListener<String>() {
                                @Override
                                public void itemClick(Grid.ItemClick<String> event) {
                                    if (ACTION_SHOW_PHYSICAL_PATH.equals(event.getItem())) {
                                        Window window = new Window();
                                        FtthPhysicalPath ftthPhysicalPath = new FtthPhysicalPath(wsBean, remoteSession, null, object);
                                        window.setModal(true);
                                        window.setWidth("30%");
                                        window.setHeight("50%");
                                        window.setContent(ftthPhysicalPath);
                                        UI.getCurrent().addWindow(window);
                                    }
                                }
                            });
                        } else if (CLASSES.contains(object.getClassName())) {
                            Grid<String> grdActions = new Grid();
                            grdActions.addColumn(String::toString).setCaption("Actions");
                            grdActions.setItems(ACTION_FTTH_INTERNAL_VIEW);
                            grdActions.addStyleName(ValoTheme.TABLE_BORDERLESS);
                            Component oldComponent = lytExplorer.getComponent(1);
                            lytExplorer.replaceComponent(oldComponent, grdActions);
                            grdActions.addItemClickListener(new ItemClickListener<String>() {
                                @Override
                                public void itemClick(Grid.ItemClick<String> event) {
                                    if (ACTION_FTTH_INTERNAL_VIEW.equals(event.getItem())) {
                                        Window window = new Window();
                                        FtthOspNodeInternalView ftthOspNodeInternalView = new FtthOspNodeInternalView(wsBean, remoteSession, object);
                                        if (ftthOspNodeInternalView.canBuildInternalView()) {
                                            window.setModal(true);
                                            window.setWidth("75%");
                                            window.setHeight("75%");
                                            window.setContent(ftthOspNodeInternalView.buildInternalView());
                                            UI.getCurrent().addWindow(window);
                                        }
                                    }
                                }
                            });                            
                        } else {
                            Component oldComponent = lytExplorer.getComponent(1);
                            lytExplorer.replaceComponent(oldComponent, propertySheetWidget);
                        }
                    }
                });
                break;
            }
        }
    }
    
    @Override
    public void registerComponents() { }

    @Override
    public void unregisterComponents() { }

}
