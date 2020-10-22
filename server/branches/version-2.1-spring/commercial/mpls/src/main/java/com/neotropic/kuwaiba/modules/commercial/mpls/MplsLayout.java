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

package com.neotropic.kuwaiba.modules.commercial.mpls;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
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

/**
 * The general layout to be used in the MPLS module.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@StyleSheet("css/main.css")
@StyleSheet("css/main-layout.css")
public class MplsLayout extends FlexLayout implements RouterLayout {
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
    private TranslationService ts;
    /**
     * Reference to the module registry.
     */
    @Autowired
    private ModuleRegistry moduleRegistry;

    public MplsLayout() {
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
        
        mnuNewBar.addItem(ts.getTranslatedString("module.login.ui.home"), ev -> UI.getCurrent().navigate("home"));
        this.moduleRegistry.getModules().values().stream().forEach( aModule -> {
            mnuNewBar.addItem(aModule.getName(), ev -> UI.getCurrent().navigate(aModule.getId()));
        });
        mnuNewBar.addItem(ts.getTranslatedString("module.login.ui.logout"), ev -> UI.getCurrent().navigate("logout"));

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
