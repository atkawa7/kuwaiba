package com.neotropic.demo.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

/**
 * The main layout. Contains the navigation menu.
 */
@Theme(value = Lumo.class)
@PWA(name = "Products", shortName = "Products")
@Viewport("width=device-width, minimum-scale=1, initial-scale=1, user-scalable=yes")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/menu-buttons.css", themeFor = "vaadin-button")
public class MainLayout extends AppLayout implements RouterLayout {

    public MainLayout() {

        // Header of the menu (the navbar)
        // menu toggle
        final DrawerToggle drawerToggle = new DrawerToggle();
        drawerToggle.addClassName("menu-toggle");

        addToNavbar(drawerToggle);

        // tittle layout
        final HorizontalLayout top = new HorizontalLayout();
        top.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        top.setClassName("menu-header");

        final Label title = new Label("Product List");
        top.add(title);
        addToNavbar(top);

        // Navigation items
        addToDrawer(createMenuLink(MainView.class, "Main View Link 1",
                VaadinIcon.EDIT.create(), false));

        addToDrawer(createMenuLink(MainView.class, "Main View Link 2",
                VaadinIcon.INFO_CIRCLE.create(), true)); // this link will be hidden on mobile screens

    }

    /**
     * create a new link to the drawer component
     *
     * @param viewClass class to route
     * @param caption link label
     * @param icon link icon
     * @param hideIfIsMobile true if the link must be hidden in mobile screens
     * @return the new link
     */
    private RouterLink createMenuLink(Class<? extends Component> viewClass,
            String caption, Icon icon, boolean hideIfIsMobile) {
        final RouterLink routerLink = new RouterLink(null, viewClass);
        routerLink.setClassName("menu-link");
        if (hideIfIsMobile) 
            routerLink.addClassName("hideIfIsMobile");
        
        routerLink.add(icon);
        routerLink.add(new Span(caption));
        icon.setSize("24px");
        return routerLink;
    }

}
