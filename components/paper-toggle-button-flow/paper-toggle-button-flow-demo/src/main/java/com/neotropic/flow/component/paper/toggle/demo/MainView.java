package com.neotropic.flow.component.paper.toggle.demo;

import com.neotropic.flow.component.paper.toggle.PaperToggleButton;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and
 * use @Route annotation to announce it in a URL as a Spring managed
 * bean.
 * Use the @PWA annotation make the application installable on phones,
 * tablets and some desktop browsers.
 * <p>
 * A new instance of this class is created for every new user and every
 * browser tab/window.
 */
@Route
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends VerticalLayout {

    /**
     * Construct a new Vaadin view.
     * <p>
     * Build the initial UI state for the user accessing the application.
     *
     */
    public MainView() {
        PaperToggleButton button = new PaperToggleButton();
        button.setSizeFull();
        button.add("button");
        
        button.addValueChangeListener(event -> {
            Dialog d = new Dialog();
            d.add(new Label(Boolean.toString(event.getValue())));
            d.open();
        } );
        
        add(button);
    }

}
