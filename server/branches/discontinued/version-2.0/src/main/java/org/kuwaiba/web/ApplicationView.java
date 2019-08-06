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
package org.kuwaiba.web;

import com.byteowls.vaadin.chartjs.ChartJs;
import com.byteowls.vaadin.chartjs.config.BarChartConfig;
import com.byteowls.vaadin.chartjs.config.PolarAreaChartConfig;
import com.byteowls.vaadin.chartjs.data.BarDataset;
import com.byteowls.vaadin.chartjs.data.Dataset;
import com.byteowls.vaadin.chartjs.data.LineDataset;
import com.byteowls.vaadin.chartjs.data.PolarAreaDataset;
import com.byteowls.vaadin.chartjs.options.InteractionMode;
import com.byteowls.vaadin.chartjs.options.Position;
import com.byteowls.vaadin.chartjs.options.scale.Axis;
import com.byteowls.vaadin.chartjs.options.scale.DefaultScale;
import com.byteowls.vaadin.chartjs.options.scale.RadialLinearScale;
import com.byteowls.vaadin.chartjs.utils.ColorUtils;
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
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.nodes.properties.PropertySheetModule;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.util.ChartUtil;
import org.kuwaiba.web.custom.CustomButton;
import org.kuwaiba.web.modules.containment.ContainmentManagerModule;
import org.kuwaiba.web.modules.lists.ListManagerModule;
import org.kuwaiba.web.modules.navtree.NavigationTreeModule;
import org.kuwaiba.web.modules.osp.OutsidePlantModule;

/**
 * Main application component
 * @author Charles Edward Bedon Cortazar<charles.bedon@kuwaiba.org>
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@CDIView("app")
class ApplicationView extends CustomComponent implements View {
    static String VIEW_NAME = "app";
    
    static final String PATH = "localhost:8080/imgs/";
    
    
    EventBus eventBus = new EventBus();
    @Inject
    private WebserviceBeanLocal wsBean;
    
    private CssLayout lytLeft;
    private CssLayout lytTop;
    private CssLayout lytRight;
    private CssLayout lytBottom;
    private CssLayout lytWrapper;
    private CssLayout lytWorkArea;
    private CustomButton btnHideLeft;
    private CustomButton btnHideRight;
    private Button btnSingout;
    private Button btnListType;
    private Button btnHierarchyContainment;
    private Button btnNavigationTree;
    private Button btnPropertySheet;
    private Button btnDashboard;
    private Button btnMap;
    private RemoteSession session;
    private boolean isMapAlreadyAdded;
    
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        session = (RemoteSession)getSession().getAttribute("session");
        isMapAlreadyAdded = false;
        
        if (session == null) //NOI18N
             getUI().getNavigator().navigateTo(LoginView.class.getName());
        else {
            Page.getCurrent().setTitle(String.format("%s - [%s]", "Kuwaiba Open Network Inventory", session.getUsername()));
            setSizeFull();
            buttons();
            initLayouts();
            final ListManagerModule mdlListManager = new ListManagerModule(eventBus, wsBean, session);
            final OutsidePlantModule mdlOutsidePlant = new OutsidePlantModule(eventBus, wsBean, session);
            final NavigationTreeModule mdlNavTree = new NavigationTreeModule(eventBus, wsBean, session);
            final ContainmentManagerModule mdlContainment = new ContainmentManagerModule(eventBus, wsBean, session);
            final PropertySheetModule mdlPropertySheet = new PropertySheetModule(eventBus, wsBean, session);
            Component navtree = mdlNavTree.open();
            Component listType = mdlListManager.open();
            VerticalLayout chartsLayout = addChartsLayout();
            //Main tool bar
            createToolbar();
            btnDashboard.addClickListener(click -> {
                if(!isMapAlreadyAdded){
                    lytWorkArea.removeAllComponents();
                    lytWorkArea.addComponent(chartsLayout);
                    isMapAlreadyAdded = true;
                }
            });
            
            btnMap.addClickListener(click -> {
                if(!isMapAlreadyAdded){
                    lytWorkArea.removeAllComponents();
                    lytWorkArea.addComponent(mdlOutsidePlant.open());
                    isMapAlreadyAdded = true;
                }
            });
            
            btnHierarchyContainment.addClickListener(click -> {
                lytWorkArea.removeAllComponents();
                lytWorkArea.addComponent(mdlContainment.open());
                isMapAlreadyAdded = false;
            });
            
            btnNavigationTree.addClickListener(click -> {
                lytLeft.removeComponent(listType);
                lytLeft.addComponent(navtree);
            });
            
            btnListType.addClickListener(click -> {
                lytLeft.removeComponent(navtree);
                lytLeft.addComponent(listType);
            });
            
            lytRight.addComponent(mdlPropertySheet.open());
            
            HorizontalLayout toolBar = new HorizontalLayout();
            
            toolBar.addComponents(btnNavigationTree, btnDashboard, btnMap, btnListType, btnHierarchyContainment, btnPropertySheet, btnSingout);
            toolBar.setWidth("100%");
            toolBar.setComponentAlignment(btnSingout, Alignment.TOP_RIGHT);
            
            lytWorkArea = new CssLayout();
            lytWorkArea.setSizeFull();
            lytWorkArea.addStyleName("v-scrollable");
            lytWorkArea.addComponent(chartsLayout);
            VerticalLayout mainLayout = new VerticalLayout(toolBar, lytWorkArea);
            mainLayout.setSizeFull();
            mainLayout.setExpandRatio(toolBar, 0.05f);
            mainLayout.setExpandRatio(lytWorkArea, 0.95f);
            lytWrapper.addComponent(lytLeft);
            lytWrapper.addComponent(mainLayout);
            lytWrapper.addComponent(lytRight);
            setCompositionRoot(lytWrapper);
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
        btnNavigationTree = new CustomButton();
        btnNavigationTree = new Button(FontAwesome.CODE_FORK);
        btnNavigationTree.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        btnNavigationTree.addStyleName("button-header");
        btnNavigationTree.setDescription("Navigation Tree");
        btnNavigationTree.addClickListener((Button.ClickEvent event1) -> {
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

        btnPropertySheet = new CustomButton();
        btnPropertySheet.setIcon(FontAwesome.TH_LIST);
        btnPropertySheet.addStyleName("button-header");
        btnPropertySheet.setDescription("Property Sheet");
        btnPropertySheet.addClickListener(new Button.ClickListener() {
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

        lytWrapper = new CssLayout();
        lytWrapper.addStyleName("dashboard");
        lytWrapper.setSizeFull();
    }
    
    private void createToolbar(){
        btnSingout = new Button();
        btnSingout.setIcon(FontAwesome.SIGN_OUT);
        btnSingout.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        btnSingout.addStyleName("button-header");
        btnSingout.setDescription("Close Session");
        
        btnSingout.addClickListener((Button.ClickEvent event) -> {
            try {
                wsBean.closeSession(session.getSessionId(), Page.getCurrent().getWebBrowser().getAddress());
                getSession().setAttribute("session", null);
                getUI().getNavigator().navigateTo(LoginView.VIEW_NAME);
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
            }
        });
        
        btnDashboard = new Button(FontAwesome.DASHBOARD);
        btnDashboard.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        btnDashboard.addStyleName("button-header");
        btnDashboard.setDescription("Dashboard");
        
        btnListType =  new Button(FontAwesome.LIST_ALT);
        btnListType.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        btnListType.addStyleName("button-header");
        btnListType.setDescription("List Types Editor");
                
        btnHierarchyContainment =  new Button();
        btnHierarchyContainment.setIcon(FontAwesome.SITEMAP);
        btnHierarchyContainment.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        btnHierarchyContainment.addStyleName("button-header");
        btnHierarchyContainment.setDescription("Containment Hierarchy");
        
        btnMap = new Button();
        btnMap.setIcon(FontAwesome.MAP);
        btnMap.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        btnMap.addStyleName("button-header");
        btnMap.setDescription("Outside Plant Module");
    }

    private VerticalLayout addChartsLayout(){
        
        VerticalLayout chartsLayout = new VerticalLayout();
        chartsLayout.setSizeUndefined();
        Label dummyMessage = new Label("Dashboard");
        dummyMessage.addStyleName(ValoTheme.LABEL_HUGE);
        chartsLayout.addComponent(dummyMessage);
        ChartJs ndaCoverageChart = addNdaCoverageChart();
        ChartJs activatedServicesLayout = addActivatedServicesLayout();
        ChartJs ipAddressesUsageChart = addIpAddressesUsageChart();
        chartsLayout.addComponent(ipAddressesUsageChart);
        chartsLayout.addComponent(ndaCoverageChart);
        chartsLayout.addComponent(activatedServicesLayout);

        return chartsLayout;
    }
    
    private ChartJs addActivatedServicesLayout(){
         BarChartConfig config = new BarChartConfig();
        config.data()
            .labels("January", "February", "March", "April", "May", "June", "July")
            .addDataset(new BarDataset().label("ELINE").backgroundColor(ColorUtils.randomColor(0.7)))
            .addDataset(new BarDataset().label("IP-TRANSIT").backgroundColor(ColorUtils.randomColor(0.7)))
            .addDataset(new BarDataset().label("EoMPLS").backgroundColor(ColorUtils.randomColor(0.7)))
            .and()
        .options()
            .responsive(true)
            .title()
                .display(true)
                .text("Activated Services")
                .and()
            .tooltips()
                .mode(InteractionMode.INDEX)
                .intersect(false)
                .and()
            .scales()
            .add(Axis.X, new DefaultScale()
                    .stacked(true))
            .add(Axis.Y, new DefaultScale()
                    .stacked(true))
            .and()
            .done();
        
        // add random data for demo
        List<String> labels = config.data().getLabels();
        for (Dataset<?, ?> ds : config.data().getDatasets()) {
            BarDataset lds = (BarDataset) ds;
            List<Double> data = new ArrayList<>();
            for (int i = 0; i < labels.size(); i++) {
                data.add((double) (Math.random() > 0.5 ? -1 : 1) * Math.round(Math.random() * 100));
            }
            lds.dataAsList(data);
        }

        ChartJs chart = new ChartJs(config);
        chart.addClickListener((a, b) -> {
            BarDataset dataset = (BarDataset) config.data().getDatasets().get(a);
            ChartUtil.notification(a, b, dataset);
        });
        chart.setJsLoggingEnabled(true);
        chart.setWidth("850px");
        return  chart;
    }
    
    private ChartJs addNdaCoverageChart(){
        BarChartConfig config = new BarChartConfig();
        config
            .data()
                .labels("January", "February", "March", "April", "May", "June", "July")
                .addDataset(new BarDataset().type().label("Devices no SLA").backgroundColor(ColorUtils.randomColor(0.7)))
                .addDataset(new LineDataset().type().label("SLA Coverage").backgroundColor("rgba(151,187,205,0.5)").borderColor("white").borderWidth(2))
                .addDataset(new BarDataset().type().label("Devecies with SLA").backgroundColor(ColorUtils.randomColor(0.7)))
                .and();
        
        config.
            options()
                .responsive(true)
                .title()
                    .display(true)
                    .position(Position.TOP)
                    .text("SLA Contracts Coverage")
                    .and()
               .done();
        
        List<String> labels = config.data().getLabels();
        for (Dataset<?, ?> ds : config.data().getDatasets()) {
            List<Double> data = new ArrayList<>();
            for (int i = 0; i < labels.size(); i++) {
                data.add((double) (Math.random() > 0.5 ? 1.0 : -1.0) * Math.round(Math.random() * 100));
            }
            
            if (ds instanceof BarDataset) {
                BarDataset bds = (BarDataset) ds;
                bds.dataAsList(data);    
            }
                
            if (ds instanceof LineDataset) {
                LineDataset lds = (LineDataset) ds;
                lds.dataAsList(data);    
            }
        }
        
        ChartJs chart = new ChartJs(config);
        chart.setJsLoggingEnabled(true);
        chart.setWidth("850px");
        return chart;
    }
    
    private ChartJs addIpAddressesUsageChart(){
        PolarAreaChartConfig config = new PolarAreaChartConfig();
        config
            .data()
                .labels("185.30.144.1/26", "185.19.130.0/28", "185.49.141.1/30", "10.19.140.0/28", "20:F::29/128")
                .addDataset(new PolarAreaDataset().label("IPs Added in use").backgroundColor(ColorUtils.randomColor(0.7), ColorUtils.randomColor(0.7), ColorUtils.randomColor(0.7), ColorUtils.randomColor(0.7), ColorUtils.randomColor(0.7)))
                .and();

        config.
            options()
                .responsive(true)
                .title()
                    .display(true)
                    .text("Subnet Usage")
                    .and()
                .scale(new RadialLinearScale().ticks().beginAtZero(true).and().reverse(false))
                .animation()
                    .animateScale(true)
                    .animateRotate(false)
                    .and()
               .done();

        List<String> labels = config.data().getLabels();
        for (Dataset<?, ?> ds : config.data().getDatasets()) {
            PolarAreaDataset lds = (PolarAreaDataset) ds;
            List<Double> data = new ArrayList<>();
            for (int i = 0; i < labels.size(); i++) {
                data.add((double) (Math.round(Math.random() * 100)));
            }
            lds.dataAsList(data);
        }

        ChartJs chart = new ChartJs(config);
        chart.setJsLoggingEnabled(true);
        chart.addClickListener((a,b) ->
        ChartUtil.notification(a, b, config.data().getDatasets().get(a)));
        chart.setWidth("450px");
        return chart;
    }
    
}
