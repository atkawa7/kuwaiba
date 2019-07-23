package com.neotropic.demo;

import com.neotropic.component.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends VerticalLayout {

    public MainView() {
        add(new Label("The tag <neotropic-label> is an autonomous custom element which shows text with yellow highlight"));
    }
}
