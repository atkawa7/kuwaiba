package com.neotropic.vaadin.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("")
public class View extends Div {

    public View() {
        BpmnModeler paperSlider = new BpmnModeler();
        add(paperSlider);
    }
}
