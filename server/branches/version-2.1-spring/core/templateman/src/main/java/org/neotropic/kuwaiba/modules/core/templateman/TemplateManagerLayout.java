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
package org.neotropic.kuwaiba.modules.core.templateman;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@StyleSheet("css/main.css")
@StyleSheet("css/main-layout.css")
@StyleSheet(value = "css/tepman.css")
@CssImport(value = "./styles/vaadin-menu-bar-buttons.css", themeFor = "vaadin-menu-bar")
public class TemplateManagerLayout extends FlexLayout implements RouterLayout {

    /**
     * Header component.
     */
    private final HorizontalLayout lytHeader;
    /**
     * Content to be injected.
     */
    private final VerticalLayout lytContent;
    /**
     * Footer content.
     */
    private final VerticalLayout lytFooter;

    /**
     * translation service
     */
    @Autowired
    private TranslationService ts;

    /**
     * Reference to the module registry.
     */
    @Autowired
    private ModuleRegistry moduleRegistry;
    
    public TemplateManagerLayout() {        
        setId("main-layout");
        setSizeFull();
        this.lytHeader = new HorizontalLayout();
        this.lytHeader.setWidthFull();
        this.lytContent = new VerticalLayout();
        this.lytFooter = new VerticalLayout();

        this.lytHeader.setId("main-layout-header");
        this.lytHeader.setWidthFull();
        this.lytHeader.setAlignItems(Alignment.END);

        this.lytContent.setId("main-layout-content");

        this.lytFooter.setId("main-layout-footer");
        this.lytFooter.setAlignItems(Alignment.CENTER);
        this.lytFooter.setWidthFull();

        add(this.lytHeader);
        add(this.lytContent);
        add(this.lytFooter);
    }

    @Override
    public void onAttach(AttachEvent ev) {
        this.lytHeader.removeAll();
        this.lytFooter.add(new Html("Copyright <a href=\"https://www.neotropic.co\" target=\"_blank\">Neotropic SAS</a> 2010 - 2020"));

        getUI().ifPresent( ui -> { // If there isn't any active session, redirect to the login ui
            if (ui.getSession().getAttribute(Session.class) == null)
                ui.navigate("");
            else {
                this.lytHeader.removeAll();
                this.lytHeader.add(buildMenu(ui.getSession().getAttribute(Session.class)));
            }
            
        });

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
    public void onDetach(DetachEvent ev) {

    }

    public void showActionCompledMessages(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
            } catch (Exception ex) {
                Logger.getLogger(TemplateManagerLayout.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
        }

    }

   @Override
    public void showRouterLayoutContent(HasElement content) {
      if (content != null) {
        this.lytContent.removeAll();
        this.lytContent.add(Objects.requireNonNull((Component)content));
      }
    }    

}
