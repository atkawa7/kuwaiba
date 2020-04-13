package com.neotropic.vaadin.component;

import com.neotropic.vaadin.component.PaperAutocomplete;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("")
public class View extends Div {

    public View() {
        PaperAutocomplete paperSlider = new PaperAutocomplete();
        add(paperSlider);
    }
}
