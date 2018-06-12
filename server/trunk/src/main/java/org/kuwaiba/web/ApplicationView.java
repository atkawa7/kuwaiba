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
package org.kuwaiba.web;

import com.google.common.eventbus.EventBus;
import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.nodes.properties.PropertySheetModule;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.web.custom.CustomButton;
import org.kuwaiba.web.modules.lists.ListManagerModule;
import org.kuwaiba.web.modules.navtree.NavigationTreeModule;
import org.kuwaiba.web.modules.osp.OutsidePlantModule;
import org.kuwaiba.web.view.MainView;

/**
 * Main application component
 * @author Charles Edward Bedon Cortazar<charles.bedon@kuwaiba.org>
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@CDIView("app")
class ApplicationView extends CustomComponent implements View {
    static String VIEW_NAME = "app";
    
    EventBus eventBus = new EventBus();
    @Inject
    private WebserviceBeanLocal wsBean;
    
    private CssLayout lytLeft;
    private CssLayout lytTop;
    private CssLayout lytRight;
    private CssLayout lytBottom;
    private CssLayout lytWorkarea;
    CustomButton btnShowLeft;
    CustomButton btnHideLeft;
    CustomButton btnShowRight;
    CustomButton btnHideRight;
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        if (true) {
            //getUI().getNavigator().navigateTo(FormManagerView.VIEW_NAME);
            //getUI().getNavigator().navigateTo(ProcessInstanceView.VIEW_NAME);
            getUI().getNavigator().navigateTo(MainView.VIEW_NAME);
            
            return;
        }
        
        final RemoteSession session = (RemoteSession)getSession().getAttribute("session");
        
        if (session == null) //NOI18N
             getUI().getNavigator().navigateTo(LoginView.class.getName());
        else {
            Page.getCurrent().setTitle(String.format("%s - [%s]", "Kuwaiba Open Network Inventory", session.getUsername()));
            setSizeFull();
            buttons();
            initLayouts();
            
            //MenuBar mnuMain = new MenuBar();
            
            //MenuBar.MenuItem mnuTools =  mnuMain.addItem("Tools", null, null);
            //mnuMain.addItem("Logout", null, new MenuBar.Command() {

//                @Override
//                public void menuSelected(MenuBar.MenuItem selectedItem) {
//                    try {
//                        wsBean.closeSession(session.getSessionId(), Page.getCurrent().getWebBrowser().getAddress());
//                        getSession().setAttribute("session", null);
//                        getUI().getNavigator().navigateTo(LoginView.VIEW_NAME);
//                    } catch (ServerSideException ex) {
//                        NotificationsUtil.showError(ex.getMessage());
//                    }
//                }
//            });
            
            // final HorizontalSplitPanel pnlSplitMain = new HorizontalSplitPanel();
            final NavigationTreeModule mdlNavTree = new NavigationTreeModule(eventBus, wsBean, session);
            final OutsidePlantModule mdlOutsidePlant = new OutsidePlantModule(eventBus, wsBean, session);
            //final ContainmentManagerModule mdlContainment = new ContainmentManagerModule(eventBus, wsBean, session);
            final PropertySheetModule mdlPropertySheet = new PropertySheetModule(eventBus, wsBean, session);
            final ListManagerModule mdlListManager = new ListManagerModule(eventBus, wsBean, session);

//            mnuTools.addItem(mdlNavTree.getName(), mdlNavTree.getIcon(), new MenuBar.Command() {
//
//                @Override
//                public void menuSelected(MenuBar.MenuItem selectedItem) {
//                    pnlSplitMain.setFirstComponent(mdlNavTree.open());
//                }
//            });
//            
//            mnuTools.addItem(mdlListManager.getName(), mdlListManager.getIcon(), new MenuBar.Command() {
//
//                @Override
//                public void menuSelected(MenuBar.MenuItem selectedItem) {
//                    pnlSplitMain.setFirstComponent(mdlListManager.open());
//                }
//            });
//            
//            mnuTools.addItem(mdlContainment.getName(), mdlContainment.getIcon(), new MenuBar.Command() {
//
//                @Override
//                public void menuSelected(MenuBar.MenuItem selectedItem) {
//                    pnlSplitMain.setSecondComponent(mdlContainment.open());
//                }
//            });
//            
//            MenuBar.MenuItem mnuNavigation = mnuTools.addItem("Navigation", null);
//            mnuNavigation.addItem(mdlOutsidePlant.getName(), mdlOutsidePlant.getIcon(), new MenuBar.Command() {
//
//                @Override
//                public void menuSelected(MenuBar.MenuItem selectedItem) {
//                    pnlSplitMain.setSecondComponent(mdlOutsidePlant.open());
//                }
//            });
            
//            MenuBar.MenuItem mnuNavigation = mnuTools.addItem("Advanced", null);
//            mnuNavigation.addItem(mdlOutsidePlant.getName(), mdlOutsidePlant.getIcon(), new MenuBar.Command() {
//
//                @Override
//                public void menuSelected(MenuBar.MenuItem selectedItem) {
//                    //pnlSplitMain.setSecondComponent(mdlOutsidePlant.open());
//                }
//            });
            //nav tree
            Panel panel = new Panel();
            panel.setContent(mdlNavTree.open());
            lytLeft.addComponent(panel);
            Component map = mdlOutsidePlant.open();
            
            lytRight.addComponent(mdlPropertySheet.open());
            
            HorizontalLayout headerButtons = new HorizontalLayout();
            headerButtons.addComponent(btnShowLeft);
            headerButtons.setComponentAlignment(btnShowLeft, Alignment.TOP_LEFT);
            headerButtons.setWidth("100%");
            headerButtons.addComponent(btnShowRight);
            headerButtons.setComponentAlignment(btnShowRight, Alignment.TOP_RIGHT);
            
            VerticalLayout content = new VerticalLayout(headerButtons, map);
            content.setSizeFull();
            content.addStyleName("red");
            content.setExpandRatio(headerButtons, 0.05f);
            content.setExpandRatio(map, 0.95f);
            lytWorkarea.addComponent(lytLeft);
            lytWorkarea.addComponent(content);
            lytWorkarea.addComponent(lytRight);
            setCompositionRoot(lytWorkarea);
        }
    }
    
    private void show(boolean show, int panel){
        switch (panel) {
            case 1:
                if (!show) {
                    lytLeft.addStyleName("visible-left");
                    lytLeft.setEnabled(true);
                } else {
                    lytLeft.removeStyleName("visible-left");
                    lytLeft.setEnabled(false);
                }
                break;
            case 2:
                if (!show) {
                    lytTop.addStyleName("visible-top");
                    lytTop.setEnabled(true);
                } else {
                    lytTop.removeStyleName("visible-top");
                    lytTop.setEnabled(false);
                }
                break;
            case 3:
                if (!show) {
                    lytRight.addStyleName("visible-right");
                    lytRight.setEnabled(true);
                } else {
                    lytRight.removeStyleName("visible-right");
                    lytRight.setEnabled(false);
                }
                break;
            case 4:
                if (!show) {
                    lytBottom.addStyleName("visible-bottom");
                    lytBottom.setEnabled(true);
                } else {
                    lytBottom.removeStyleName("visible-bottom");
                    lytBottom.setEnabled(false);
                }
                break;
        }
    }
    
    private void buttons(){
        btnShowLeft = new CustomButton();
        btnShowLeft.setIcon(FontAwesome.CHEVRON_RIGHT);
        btnShowLeft.addClickListener((Button.ClickEvent event1) -> {
            show(false, 1);
        });

        btnHideLeft = new CustomButton();
        btnHideLeft.setIcon(FontAwesome.CHEVRON_LEFT);
        btnHideLeft.addClickListener(new Button.ClickListener() {
        @Override
            public void buttonClick(Button.ClickEvent event) {
                show(true,1);
            }
        });

        btnShowRight = new CustomButton();
        btnShowRight.setIcon(FontAwesome.CHEVRON_LEFT);
        btnShowRight.addClickListener(new Button.ClickListener() {
        @Override
            public void buttonClick(Button.ClickEvent event) {
                show(false,3);
            }
        });

        btnHideRight = new CustomButton();
        btnHideRight.setIcon(FontAwesome.CHEVRON_RIGHT);
        btnHideRight.addClickListener(new Button.ClickListener() {
        @Override
            public void buttonClick(Button.ClickEvent event) {
                show(true,3);
            }
        });
    }
    
    private void initLayouts(){
        lytLeft = new CssLayout();
        lytLeft.addStyleName("left-area");
        lytLeft.addComponent(btnHideLeft);
        
        lytTop = new CssLayout();
        lytTop.addStyleName("top-area");
        
        lytRight = new CssLayout();
        lytRight.addStyleName("right-area");
        lytRight.addComponent(btnHideRight);
        
        lytBottom = new CssLayout();
        lytBottom.addStyleName("bottom-area");

        lytWorkarea = new CssLayout();
        lytWorkarea.addStyleName("work-area");
        lytWorkarea.setSizeFull();
    }
}
