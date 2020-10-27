/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.util.visual.general;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractModule;

/**
 * The super class of all flex layouts used in every module of the application.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@StyleSheet("css/main.css")
@StyleSheet("css/main-layout.css")
public abstract class ModuleLayout extends FlexLayout implements RouterLayout {
    /**
     * Header component.
     */
    private HorizontalLayout lytHeader;
    /**
     * Content to be injected.
     */
    private VerticalLayout lytContent;
    /**
     * Footer content.
     */
    private VerticalLayout lytFooter;
    /**
     * Reference to the translation service.
     */
    @Autowired
    protected TranslationService ts;
    /**
     * Reference to the module registry.
     */
    @Autowired
    protected ModuleRegistry moduleRegistry;

    public ModuleLayout() {
        setId("main-layout");
        setSizeFull();
        this.lytHeader = new HorizontalLayout();
        this.lytHeader.setWidthFull();
        this.lytContent = new VerticalLayout();
        this.lytFooter = new VerticalLayout();
     
        this.lytHeader.setId("main-layout-header");
        this.lytHeader.setWidthFull();
        
        this.lytContent.setId("main-layout-content");
        
        this.lytFooter.setId("main-layout-footer");
        this.lytFooter.setAlignItems(Alignment.CENTER);
        this.lytFooter.setWidthFull();
        
        add(this.lytHeader);
        add(this.lytContent);
        //add(this.lytFooter);
    }
    
    public MenuBar buildMenu(Session session) {
        MenuBar mnuNewBar = new MenuBar();
        mnuNewBar.setWidthFull();
        
        mnuNewBar.addItem(new Button(ts.getTranslatedString("module.home.menu.home"), new Icon(VaadinIcon.HOME)), ev -> UI.getCurrent().navigate("home"));
        
        MenuItem mnuNavigation = mnuNewBar.addItem(new Button(ts.getTranslatedString("module.home.menu.navigation"), new Icon(VaadinIcon.GLOBE)));
        MenuItem mnuPhysical = mnuNewBar.addItem(new Button(ts.getTranslatedString("module.home.menu.physical"), new Icon(VaadinIcon.GRID_BIG)));
        MenuItem mnuLogical = mnuNewBar.addItem(new Button(ts.getTranslatedString("module.home.menu.logical"), new Icon(VaadinIcon.GRID_BIG_O)));
        MenuItem mnuBusiness = mnuNewBar.addItem(new Button(ts.getTranslatedString("module.home.menu.business"), new Icon(VaadinIcon.CUBES)));
        MenuItem mnuPlanning = mnuNewBar.addItem(new Button(ts.getTranslatedString("module.home.menu.planning"), new Icon(VaadinIcon.MAGIC)));
        MenuItem mnuAdministration = mnuNewBar.addItem(new Button(ts.getTranslatedString("module.home.menu.administration"), new Icon(VaadinIcon.CHART_3D)));
        MenuItem mnuOther = mnuNewBar.addItem(new Button(ts.getTranslatedString("module.home.menu.other"), new Icon(VaadinIcon.FORM)));
        MenuItem mnuSettings = mnuNewBar.addItem(new Button(ts.getTranslatedString("module.home.menu.settings"), new Icon(VaadinIcon.COG)));
        
        mnuNewBar.addItem(new Button(ts.getTranslatedString("module.home.menu.about"), 
                new Icon(VaadinIcon.TOUCH)), ev -> UI.getCurrent().navigate("about"));
        mnuNewBar.addItem(new Button(ts.getTranslatedString("module.home.menu.logout"), 
                new Icon(VaadinIcon.EXIT_O)), ev -> UI.getCurrent().navigate("logout"));
        
        this.moduleRegistry.getModules().values().stream().forEach( aModule -> {
            switch(aModule.getCategory()) {
                case AbstractModule.CATEGORY_NAVIGATION:
                    mnuNavigation.getSubMenu().addItem(aModule.getName(), ev -> UI.getCurrent().navigate(aModule.getId()));
                    break;
                case AbstractModule.CATEGORY_PHYSICAL:
                    mnuPhysical.getSubMenu().addItem(aModule.getName(), ev -> UI.getCurrent().navigate(aModule.getId()));
                    break;
                case AbstractModule.CATEGORY_LOGICAL:
                    mnuLogical.getSubMenu().addItem(aModule.getName(), ev -> UI.getCurrent().navigate(aModule.getId()));
                    break;
                case AbstractModule.CATEGORY_BUSINESS:
                    mnuBusiness.getSubMenu().addItem(aModule.getName(), ev -> UI.getCurrent().navigate(aModule.getId()));
                    break;
                case AbstractModule.CATEGORY_PLANNING:
                    mnuPlanning.getSubMenu().addItem(aModule.getName(), ev -> UI.getCurrent().navigate(aModule.getId()));
                    break;
                case AbstractModule.CATEGORY_ADMINISTRATION:
                    mnuAdministration.getSubMenu().addItem(aModule.getName(), ev -> UI.getCurrent().navigate(aModule.getId()));
                    break;
                case AbstractModule.CATEGORY_SETTINGS:
                    mnuSettings.getSubMenu().addItem(aModule.getName(), ev -> UI.getCurrent().navigate(aModule.getId()));
                    break;
                case AbstractModule.CATEGORY_OTHER:
                default:
                    mnuOther.getSubMenu().addItem(aModule.getName(), ev -> UI.getCurrent().navigate(aModule.getId()));
            }
        });

        return mnuNewBar;
    }
    
    @Override
    public void onAttach(AttachEvent ev) {
        this.lytHeader.removeAll();
        this.lytFooter.add(new Html("<span>Copyright <a href=\"https://www.neotropic.co\" target=\"_blank\">Neotropic SAS</a> 2010 - 2020</span>"));
        
        getUI().ifPresent( ui -> { // If there isn't any active session, redirect to the login ui
            if (ui.getSession().getAttribute(Session.class) == null)
                ui.navigate("");
            else {
                this.lytHeader.removeAll();
                this.lytHeader.add(buildMenu(ui.getSession().getAttribute(Session.class)));
            }
            
        });
    }
    
    @Override
    public void showRouterLayoutContent(HasElement content) {
      if (content != null) {
        this.lytContent.removeAll();
        this.lytContent.add(Objects.requireNonNull((Component)content));
      }
    }
}
